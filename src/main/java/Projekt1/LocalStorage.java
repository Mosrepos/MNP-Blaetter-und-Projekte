package Projekt1;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.*;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class LocalStorage extends AbstractBehavior<LocalStorage.Command> {

    public interface Command {}

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
                .build();
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
            getContext().scheduleOnce(Duration.ofSeconds(new Random().nextInt(6) + 10), getContext().getSelf(), msg);
        }
        return this;
    }
}