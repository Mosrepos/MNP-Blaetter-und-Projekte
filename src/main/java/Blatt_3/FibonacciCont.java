package Blatt_3;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

public class FibonacciCont extends AbstractBehavior<FibonacciCont.Message> {

    private final ActorRef<Message> cust;

    public interface Message {};

    public record FibonacciContMessage(int result) implements Message {  }

    public static Behavior<Message> create(int first_value, ActorRef<Message> cust) {
        return Behaviors.setup(context -> new FibonacciCont(context, first_value, cust));
    }

    private final int first_value;

    private FibonacciCont(ActorContext<Message> context, int first_value, ActorRef<Message> cust) {
        super(context);
        this.first_value = first_value;
        this.cust = cust;
    }

    @Override
    public Receive<Message> createReceive() {
        return newReceiveBuilder()
                .onMessage(FibonacciContMessage.class, this::onExampleMessage)
                .build();
    }

    private Behavior<Message> onExampleMessage(FibonacciContMessage msg) {
        if (this.first_value == -1) {
            return FibonacciCont.create(msg.result, this.cust);
        } else {
            this.cust.tell(new FibonacciContMessage(msg.result + this.first_value));
        }
        return this;
    }
}