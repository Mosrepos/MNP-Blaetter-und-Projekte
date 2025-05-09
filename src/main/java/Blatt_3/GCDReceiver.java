package Blatt_3;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

public class GCDReceiver extends AbstractBehavior<GCDReceiver.Message> {

    public interface Message {};

    public record GCDResult(int result) implements Message {  }

    public static Behavior<Message> create() {
        return Behaviors.setup(GCDReceiver::new);
    }


    private GCDReceiver(ActorContext<Message> context) {
        super(context);
    }

    @Override
    public Receive<Message> createReceive() {
        return newReceiveBuilder()
                .onMessage(GCDResult.class, this::onResult)
                .build();
    }

    private Behavior<Message> onResult(GCDResult msg) {
        getContext().getLog().info("GCD: {}", msg.result);
        return this;
    }
}
