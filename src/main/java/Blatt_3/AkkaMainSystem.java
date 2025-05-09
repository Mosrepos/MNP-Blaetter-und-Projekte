package Blatt_3;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.*;

public class AkkaMainSystem extends AbstractBehavior<AkkaMainSystem.Create> {

    public static class Create {
    }

    public static Behavior<Create> create() {
        return Behaviors.setup(AkkaMainSystem::new);
    }

    private AkkaMainSystem(ActorContext<Create> context) {
        super(context);
    }

    @Override
    public Receive<Create> createReceive() {
        return newReceiveBuilder().onMessage(Create.class, this::onCreate).build();
    }

    private Behavior<Create> onCreate(Create command) {
        // Factorial
        var factorialReceiver = this.getContext().spawn(FactorialReceiver.create(), "FactorialReceiver");
        var factorial = this.getContext().spawn(Factorial.create(), "Factorial");
        factorial.tell(new Factorial.FactorialMsg(5, factorialReceiver));

        // GCD
        var gcdReceiver = this.getContext().spawn(GCDReceiver.create(), "GCDReceiver");
        var gcd = this.getContext().spawn(GCD.create(), "GCD");
        gcd.tell(new GCD.GCDMessage(127, 74, gcdReceiver));

        // Fibonacci
        var fibonacciReceiver = this.getContext().spawn(FibonacciReceiver.create(), "FibonacciReceiver");
        var fibonacci = this.getContext().spawn(Fibonacci.create(), "Fibonacci");
        fibonacci.tell(new Fibonacci.FibonacciMessage(7, fibonacciReceiver));

        return this;
    }
}

