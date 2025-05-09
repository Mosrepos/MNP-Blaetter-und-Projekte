package Blatt_3;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

import java.util.UUID;

public class Fibonacci extends AbstractBehavior<Fibonacci.Message> {

    public interface Message {};

    public record FibonacciMessage(int n, ActorRef<FibonacciCont.Message> cust) implements Message {  }

    public static Behavior<Message> create() {
        return Behaviors.setup(Fibonacci::new);
    }


    private Fibonacci(ActorContext<Message> context) {
        super(context);
    }

    @Override
    public Receive<Message> createReceive() {
        return newReceiveBuilder()
                .onMessage(FibonacciMessage.class, this::onExampleMessage)
                .build();
    }

    private Behavior<Message> onExampleMessage(FibonacciMessage msg) {
        if (msg.n <= 1) {
            msg.cust.tell(new FibonacciCont.FibonacciContMessage(msg.n));
        } else {
            var cont = this.getContext().spawn(FibonacciCont.create(-1, msg.cust), UUID.randomUUID().toString());
            this.getContext().getSelf().tell(new FibonacciMessage(msg.n - 1, cont));
            this.getContext().getSelf().tell(new FibonacciMessage(msg.n - 2, cont));
        }
        return this;
    }
}
