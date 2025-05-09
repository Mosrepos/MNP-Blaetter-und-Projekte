package Blatt_3;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

import java.util.UUID;

public class Factorial extends AbstractBehavior<Factorial.Message> {

    public interface Message {};

    public record FactorialMsg(int val, ActorRef<FactorialCont.Message> cust) implements Message {  }

    public static Behavior<Message> create() {
        return Behaviors.setup(Factorial::new);
    }

    private Factorial(ActorContext<Message> context) {
        super(context);
    }

    @Override
    public Receive<Message> createReceive() {
        return newReceiveBuilder()
                .onMessage(FactorialMsg.class, this::onMsg)
                .build();
    }

    private Behavior<Message> onMsg(FactorialMsg msg) {
        if (msg.val == 0){
            msg.cust.tell(new FactorialCont.FactorialContMsg(1));
        } else {
            var cont = this.getContext().spawn(FactorialCont.create(msg.val, msg.cust), UUID.randomUUID().toString());
            this.getContext().getSelf().tell(new FactorialMsg(msg.val - 1, cont));
        }

        return this;
    }
}
