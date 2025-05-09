package Blatt_3;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

public class GCD extends AbstractBehavior<GCD.Message> {

    public interface Message {};

    public record GCDMessage(int a, int b, ActorRef<GCDReceiver.Message> cust) implements Message {  }

    public static Behavior<Message> create() {
        return Behaviors.setup(GCD::new);
    }


    private GCD(ActorContext<Message> context) {
        super(context);
    }

    @Override
    public Receive<Message> createReceive() {
        return newReceiveBuilder()
                .onMessage(GCDMessage.class, this::onMsg)
                .build();
    }

    private Behavior<Message> onMsg(GCDMessage msg) {
        if (msg.b == 0) {
            msg.cust.tell(new GCDReceiver.GCDResult(msg.a));
        } else {
            this.getContext().getSelf().tell(new GCDMessage(msg.b, msg.a % msg.b, msg.cust));
        }
        return this;
    }
}
