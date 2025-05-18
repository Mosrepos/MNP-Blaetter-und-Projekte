package Projekt1;

        import akka.actor.typed.ActorRef;
        import akka.actor.typed.Behavior;
        import akka.actor.typed.javadsl.*;

        import java.time.Duration;
        import java.util.Random;

        public class Worker extends AbstractBehavior<Worker.Command> {

            public interface Command {}

            private static class ResetStorageCheck implements Command {}
            private boolean hasCheckedStorage = false;

            private Behavior<Command> onResetStorageCheck(ResetStorageCheck msg) {
                hasCheckedStorage = false;
                return this;
            }

            public static class BuildCarBody implements Command {
                public final int orderId;
                public final ActorRef<ProductionLine.Command> productionLine;

                public BuildCarBody(int orderId, ActorRef<ProductionLine.Command> productionLine) {
                    this.orderId = orderId;
                    this.productionLine = productionLine;
                }
            }

            public static class InstallSpecialRequests implements Command {
                public final ActorRef<LocalStorage.Command> localStorage;
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
                        .onMessage(ResetStorageCheck.class, this::onResetStorageCheck)
                        .build();
            }

            private Behavior<Command> onBuildCarBody(BuildCarBody msg) {
                getContext().getLog().info("{} baut die Karosserie für Auftrag {}", name, msg.orderId);
                getContext().scheduleOnce(Duration.ofSeconds(new Random().nextInt(6) + 5), msg.productionLine, new ProductionLine.AssignWorker(getContext().getSelf()));
                return this;
            }

            private Behavior<Command> onInstallSpecialRequests(InstallSpecialRequests msg) {
                if (!hasCheckedStorage) {
                    hasCheckedStorage = true;
                    msg.localStorage.tell(new LocalStorage.CheckSpecialRequests());
                    // Längere Wartezeit bis zur nächsten Überprüfung
                    getContext().scheduleOnce(Duration.ofSeconds(10), getContext().getSelf(),
                            new ResetStorageCheck());
                }
                return this;
            }
        }