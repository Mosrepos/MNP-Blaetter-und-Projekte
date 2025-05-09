package Blatt_3;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

public class FactorialReceiver extends AbstractBehavior<FactorialCont.Message> {

    public static Behavior<FactorialCont.Message> create() {
        return Behaviors.setup(FactorialReceiver::new);
    }


    private FactorialReceiver(ActorContext<FactorialCont.Message> context) {
        super(context);
    }

    @Override
    public Receive<FactorialCont.Message> createReceive() {
        return newReceiveBuilder()
                .onMessage(FactorialCont.FactorialContMsg.class, this::onMsg)
                .build();
    }

    private Behavior<FactorialCont.Message> onMsg(FactorialCont.FactorialContMsg msg) {
        this.getContext().getLog().info("Factorial: {}", msg.arg());
        return this;
    }
}
