package Projekt1;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.*;

import java.time.Duration;
import java.util.Random;

public class Worker extends AbstractBehavior<Worker.Command> {

    public interface Command {}

    public static class BuildCarBody implements Command {
        public final int orderId;
        public final ActorRef<ProductionLine.Command> productionLine;

        public BuildCarBody(int orderId, ActorRef<ProductionLine.Command> productionLine) {
            this.orderId = orderId;
            this.productionLine = productionLine;
        }
    }

    public static class InstallSpecialRequests implements Command {
        public final ActorRef<Projekt1.LocalStorage.Command> localStorage;
        public final ActorRef<ProductionLine.Command> productionLine;

        public InstallSpecialRequests(ActorRef<LocalStorage.Command> localStorage, ActorRef<ProductionLine.Command> productionLine) {
            this.localStorage = localStorage;
            this.productionLine = productionLine;
        }
    }

    public static Behavior<Command> create(String name) {
        return Behaviors.setup(context -> new Worker(context, name));
    }

    private final String name;

    private Worker(ActorContext<Command> context, String name) {
        super(context);
        this.name = name;
    }

    @Override
    public Receive<Command> createReceive() {
        return newReceiveBuilder()
                .onMessage(BuildCarBody.class, this::onBuildCarBody)
                .onMessage(InstallSpecialRequests.class, this::onInstallSpecialRequests)
                .build();
    }

    private Behavior<Command> onBuildCarBody(BuildCarBody msg) {
        getContext().getLog().info("{} baut die Karosserie für Auftrag {}", name, msg.orderId);
        getContext().scheduleOnce(Duration.ofSeconds(new Random().nextInt(6) + 5), msg.productionLine, new ProductionLine.AssignWorker(getContext().getSelf()));
        return this;
    }

    private Behavior<Command> onInstallSpecialRequests(InstallSpecialRequests msg) {
        getContext().getLog().info("{} installiert Spezialwünsche.", name);
        msg.localStorage.tell(new LocalStorage.RequestSpecialRequests(getContext().getSelf(), msg.productionLine));
        return this;
    }
}