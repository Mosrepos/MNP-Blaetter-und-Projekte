package Projekt1;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.*;

import java.util.LinkedList;
import java.util.Queue;

public class OrderBook extends AbstractBehavior<OrderBook.Command> {

    public interface Command {}

    public static class NewOrder implements Command {
        public final int orderId;

        public NewOrder(int orderId) {
            this.orderId = orderId;
        }
    }

    public static class AssignOrder implements Command {
        public final ActorRef<ProductionLine.Command> productionLine;

        public AssignOrder(ActorRef<ProductionLine.Command> productionLine) {
            this.productionLine = productionLine;
        }
    }

    private final Queue<Integer> orders = new LinkedList<>();

    public static Behavior<Command> create() {
        return Behaviors.setup(OrderBook::new);
    }

    private OrderBook(ActorContext<Command> context) {
        super(context);
    }

    @Override
    public Receive<Command> createReceive() {
        return newReceiveBuilder()
                .onMessage(NewOrder.class, this::onNewOrder)
                .onMessage(AssignOrder.class, this::onAssignOrder)
                .build();
    }

    private Behavior<Command> onNewOrder(NewOrder msg) {
        orders.add(msg.orderId);
        getContext().getLog().info("Neuer Auftrag hinzugefügt: {}", msg.orderId);
        return this;
    }

    private Behavior<Command> onAssignOrder(AssignOrder msg) {
        if (!orders.isEmpty()) {
            int orderId = orders.poll();
            msg.productionLine.tell(new ProductionLine.StartProduction(orderId));
            getContext().getLog().info("Auftrag {} zugewiesen.", orderId);
        } else {
            getContext().getLog().info("Keine Aufträge verfügbar. Warte 10 Sekunden.");
            getContext().scheduleOnce(java.time.Duration.ofSeconds(10), getContext().getSelf(), msg);
        }
        return this;
    }
}