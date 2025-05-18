package Projekt1;

    import akka.actor.typed.ActorRef;
    import akka.actor.typed.Behavior;
    import akka.actor.typed.javadsl.*;

    import java.util.List;
    import java.util.Random;

    public class ProductionLine extends AbstractBehavior<ProductionLine.Command> {

        public interface Command {}

        public static class StartProduction implements Command {
            public final int orderId;

            public StartProduction(int orderId) {
                this.orderId = orderId;
            }
        }

        public static class AssignWorker implements Command {
            public final ActorRef<Worker.Command> worker;

            public AssignWorker(ActorRef<Worker.Command> worker) {
                this.worker = worker;
            }
        }

        private final List<ActorRef<Worker.Command>> workers;
        private final ActorRef<LocalStorage.Command> localStorage;

        public static Behavior<Command> create(List<ActorRef<Worker.Command>> workers, ActorRef<LocalStorage.Command> localStorage) {
            return Behaviors.setup(context -> new ProductionLine(context, workers, localStorage));
        }

        private ProductionLine(ActorContext<Command> context, List<ActorRef<Worker.Command>> workers, ActorRef<LocalStorage.Command> localStorage) {
            super(context);
            this.workers = workers;
            this.localStorage = localStorage;
        }

        @Override
        public Receive<Command> createReceive() {
            return newReceiveBuilder()
                    .onMessage(StartProduction.class, this::onStartProduction)
                    .onMessage(AssignWorker.class, this::onAssignWorker)
                    .build();
        }

        private Behavior<Command> onStartProduction(StartProduction msg) {
            Random random = new Random();
            ActorRef<Worker.Command> worker = workers.get(random.nextInt(workers.size()));
            getContext().getLog().info("Produktionsstraße {} startet Produktion für Auftrag {} mit {}",
                getContext().getSelf().path().name(), msg.orderId, worker.path().name());
            worker.tell(new Worker.BuildCarBody(msg.orderId, getContext().getSelf()));
            return this;
        }

        private Behavior<Command> onAssignWorker(AssignWorker msg) {
            getContext().getLog().info("Produktionsstraße {} weist {} Spezialwünsche zu",
                getContext().getSelf().path().name(), msg.worker.path().name());
            msg.worker.tell(new Worker.InstallSpecialRequests(localStorage, getContext().getSelf()));
            return this;
        }
    }