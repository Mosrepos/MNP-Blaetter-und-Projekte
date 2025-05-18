package Projekt1;

    import akka.actor.typed.ActorRef;
    import akka.actor.typed.Behavior;
    import akka.actor.typed.javadsl.*;

    import java.time.Duration;
    import java.util.HashMap;
    import java.util.Map;
    import java.util.Random;

    public class LocalStorage extends AbstractBehavior<LocalStorage.Command> {

        private boolean isOrdering = false;
        private static final Duration ORDER_COOLDOWN = Duration.ofSeconds(30);
        private int specialRequests = 0;
        private static final int MINIMUM_SPECIAL_REQUESTS = 2;

        public interface Command {}

        public static class CheckSpecialRequests implements Command {
            public ActorRef<Worker.Command> worker = null;

            public CheckSpecialRequests() {
            }
        }

        private static class OrderComplete implements Command {}

        public static class RequestSpecialRequests implements Command {
            public final ActorRef<Worker.Command> worker;
            public final ActorRef<ProductionLine.Command> productionLine;

            public RequestSpecialRequests(ActorRef<Worker.Command> worker, ActorRef<ProductionLine.Command> productionLine) {
                this.worker = worker;
                this.productionLine = productionLine;
            }
        }

        private final Map<String, Integer> storage = new HashMap<>();

        public static Behavior<Command> create() {
            return Behaviors.setup(LocalStorage::new);
        }

        private LocalStorage(ActorContext<Command> context) {
            super(context);
            storage.put("Ledersitze", 4);
            storage.put("Klimaautomatik", 4);
            storage.put("Elektrische Fensterheber", 4);
            storage.put("Automatikgetriebe", 4);
        }

        @Override
        public Receive<Command> createReceive() {
            return newReceiveBuilder()
                    .onMessage(RequestSpecialRequests.class, this::onRequestSpecialRequests)
                    .onMessage(CheckSpecialRequests.class, this::onCheckSpecialRequests)
                    .onMessage(OrderComplete.class, this::onOrderComplete)
                    .build();
        }

        private Behavior<Command> onCheckSpecialRequests(CheckSpecialRequests msg) {
            if (specialRequests < MINIMUM_SPECIAL_REQUESTS && !isOrdering) {
                isOrdering = true;
                getContext().getLog().info("Nicht genügend Spezialwünsche. Bestellung wird aufgegeben.");
                getContext().scheduleOnce(ORDER_COOLDOWN, getContext().getSelf(), new OrderComplete());
            }
            return this;
        }

        private Behavior<Command> onOrderComplete(OrderComplete msg) {
            isOrdering = false;
            return this;
        }

        private Behavior<Command> onRequestSpecialRequests(RequestSpecialRequests msg) {
            Random random = new Random();
            String[] requests = storage.keySet().toArray(new String[0]);
            String request1 = requests[random.nextInt(requests.length)];
            String request2 = requests[random.nextInt(requests.length)];

            if (storage.get(request1) > 0 && storage.get(request2) > 0) {
                storage.put(request1, storage.get(request1) - 1);
                storage.put(request2, storage.get(request2) - 1);
                getContext().getLog().info("Spezialwünsche {} und {} entnommen.", request1, request2);
            } else {
                msg.worker.tell(new Worker.InstallSpecialRequests(getContext().getSelf(), msg.productionLine));
                getContext().getLog().info("Nicht genügend Spezialwünsche. Bestellung wird aufgegeben.");
                getContext().scheduleOnce(Duration.ofSeconds(random.nextInt(6) + 10), getContext().getSelf(), msg);
            }
            return this;
        }
    }