package Blatt_3;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

public class FactorialCont extends AbstractBehavior<FactorialCont.Message> {

    private final ActorRef<Message> cust;
    private final int val;

    public interface Message {};

    public record FactorialContMsg(int arg) implements Message {  }

    public static Behavior<Message> create(int val, ActorRef<FactorialCont.Message> cust) {
        return Behaviors.setup(context -> new FactorialCont(context, val, cust));
    }

    private FactorialCont(ActorContext<Message> context, int val, ActorRef<Message> cust) {
        super(context);
        this.val = val;
        this.cust = cust;
    }

    @Override
    public Receive<Message> createReceive() {
        return newReceiveBuilder()
                .onMessage(FactorialContMsg.class, this::onMsg)
                .build();
    }

    private Behavior<Message> onMsg(FactorialContMsg msg) {
        this.cust.tell(new FactorialContMsg(this.val * msg.arg));
        return this;
    }
}