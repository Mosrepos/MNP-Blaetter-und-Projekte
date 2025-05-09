package Blatt_3;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

public class FibonacciReceiver extends AbstractBehavior<FibonacciCont.Message> {


    public static Behavior<FibonacciCont.Message> create() {
        return Behaviors.setup(FibonacciReceiver::new);
    }


    private FibonacciReceiver(ActorContext<FibonacciCont.Message> context) {
        super(context);
    }

    @Override
    public Receive<FibonacciCont.Message> createReceive() {
        return newReceiveBuilder()
                .onMessage(FibonacciCont.FibonacciContMessage.class, this::onExampleMessage)
                .build();
    }

    private Behavior<FibonacciCont.Message> onExampleMessage(FibonacciCont.FibonacciContMessage msg) {
        getContext().getLog().info("Fibonacci: {}",msg.result());
        return this;
    }
}
