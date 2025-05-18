package Projekt1;

import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.*;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class Main extends AbstractBehavior<Main.Command> {

    public interface Command {}
    private static class Start implements Command {}
    private static class NewOrderTick implements Command {}
    private static class AssignOrderToLineTick implements Command {
        final ActorRef<ProductionLine.Command> productionLine;
        AssignOrderToLineTick(ActorRef<ProductionLine.Command> productionLine) {
            this.productionLine = productionLine;
        }
    }

    private final ActorRef<OrderBook.Command> orderBook;
    private final List<ActorRef<ProductionLine.Command>> productionLines;
    private int orderCounter = 0;

    public static void main(String[] args) {
        ActorSystem<Command> system = ActorSystem.create(Main.create(), "MeinAutoFabrik");
        system.tell(new Start());
    }

    public static Behavior<Command> create() {
        return Behaviors.setup(context -> Behaviors.withTimers(timers -> new Main(context, timers)));
    }

    private Main(ActorContext<Command> context, TimerScheduler<Command> timers) {
        super(context);

        getContext().getLog().info("Initialisiere MeinAuto Fabrik...");

        // Lokales Lager erstellen
        ActorRef<LocalStorage.Command> localStorage = context.spawn(LocalStorage.create(), "localStorage");
        getContext().getLog().info("Lokales Lager erstellt");

        // 4 Arbeiter erstellen
        List<ActorRef<Worker.Command>> workers = new ArrayList<>();
        String[] workerNames = {"Max", "Simon", "Adam", "Finn"};
        for (String name : workerNames) {
            workers.add(context.spawn(Worker.create(name), name));
        }
        getContext().getLog().info("4 Arbeiter erstellt: {}", workers.stream().map(w -> w.path().name()).toList());

        // 2 Produktionsstraßen erstellen
        productionLines = new ArrayList<>();
        for (int i = 1; i <= 2; i++) {
            productionLines.add(context.spawn(
                    ProductionLine.create(workers, localStorage),
                    "productionLine" + i
            ));
        }
        getContext().getLog().info("2 Produktionsstraßen erstellt: {}",
            productionLines.stream().map(p -> p.path().name()).toList());

        // Auftragsbuch erstellen
        orderBook = context.spawn(OrderBook.create(), "orderBook");
        getContext().getLog().info("Auftragsbuch erstellt");

        // Timer für neue Aufträge alle 15 Sekunden
        timers.startTimerWithFixedDelay("newOrderTimer", new NewOrderTick(),
            Duration.ZERO, Duration.ofSeconds(15));
        getContext().getLog().info("Timer für neue Aufträge gestartet (15s Intervall)");

        // Timer für Produktionsstraßen-Prüfung
        for (int i = 0; i < productionLines.size(); i++) {
            ActorRef<ProductionLine.Command> line = productionLines.get(i);
            timers.startTimerWithFixedDelay(
                "assignOrder-" + line.path().name(),
                new AssignOrderToLineTick(line),
                    Duration.ofSeconds(5 + i * 5), // Längerer versetzter Start
                    Duration.ofSeconds(30) // Deutlich längeres Intervall
            );
        }
        getContext().getLog().info("Timer für Produktionsstraßen-Prüfung gestartet (5s Intervall)");
    }

    @Override
    public Receive<Command> createReceive() {
        return newReceiveBuilder()
                .onMessage(Start.class, this::onStart)
                .onMessage(NewOrderTick.class, this::onNewOrderTick)
                .onMessage(AssignOrderToLineTick.class, this::onAssignOrderToLineTick)
                .build();
    }

    private Behavior<Command> onStart(Start msg) {
        getContext().getLog().info("MeinAuto Fabrik startet die Produktion");
        return this;
    }

    private Behavior<Command> onNewOrderTick(NewOrderTick msg) {
        orderCounter++;
        getContext().getLog().info("Erstelle neuen Auftrag #{}", orderCounter);
        orderBook.tell(new OrderBook.NewOrder(orderCounter));
        return this;
    }

    private Behavior<Command> onAssignOrderToLineTick(AssignOrderToLineTick msg) {
        // Nur prüfen und loggen wenn nötig
        if (msg.productionLine != null) {
            orderBook.tell(new OrderBook.AssignOrder(msg.productionLine));
        }
        return this;
    }
}