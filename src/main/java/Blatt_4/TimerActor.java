package Blatt_4;

import akka.actor.typed.ActorRef;
import akka.actor.typed.javadsl.TimerScheduler;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

import java.time.Duration;
import java.util.ArrayList;


public class TimerActor extends AbstractBehavior<TimerActor.Message> {


    public interface Message {};

    public record Clockwise(ArrayList<ActorRef<Message>> reihenfolge) implements Message {}
    public record CounterClockwise(ArrayList<ActorRef<Message>> reihenfolge) implements Message {}
    public record Terminate() implements Message {}

    public record Clockwise_wait(ArrayList<ActorRef<Message>> reihenfolge) implements Message {}
    public record CounterClockwise_wait(ArrayList<ActorRef<Message>> reihenfolge) implements Message {}

    public static Behavior<Message> create(String name) {
        return Behaviors.setup(context -> Behaviors.withTimers(timers -> new TimerActor(context, timers, name)));
    }

    private final TimerScheduler<TimerActor.Message> timers;
    private final String name;

    private TimerActor(ActorContext<Message> context, TimerScheduler<Message> timers, String name) {
        super(context);
        this.timers = timers;
        this.name = name;
    }

    @Override
    public Receive<Message> createReceive() {
        return newReceiveBuilder()
                .onMessage(Clockwise.class, this::onClockwise)
                .onMessage(CounterClockwise.class, this::onCounterClockwise)
                .onMessage(Clockwise_wait.class, this::onClockwise_wait)
                .onMessage(CounterClockwise_wait.class, this::onCounterClockwise_wait)
                .onMessage(Terminate.class, this::onTerminate)
                .build();
    }

    private Behavior<Message> onClockwise(Clockwise msg) {
        getContext().getLog().info("Clockwise erhalten");
        this.timers.startSingleTimer(new Clockwise_wait(msg.reihenfolge), Duration.ofSeconds(3));
        return this;
    }

    private Behavior<Message> onClockwise_wait(Clockwise_wait msg) {
        getContext().getLog().info("Clockwise senden");
        var new_reihenfolge = msg.reihenfolge;
        var next = new_reihenfolge.remove(0);
        new_reihenfolge.add(next);

        next.tell(new Clockwise(new_reihenfolge));
        return this;
    }

    private Behavior<Message> onCounterClockwise(CounterClockwise msg) {
        getContext().getLog().info("CounterClockwise erhalten");
        this.timers.startSingleTimer(new CounterClockwise_wait(msg.reihenfolge), Duration.ofSeconds(5));
        return this;
    }

    private Behavior<Message> onCounterClockwise_wait(CounterClockwise_wait msg) {
        getContext().getLog().info("CounterClockwise senden");
        var new_reihenfolge = msg.reihenfolge;
        var next = new_reihenfolge.remove(0);
        new_reihenfolge.add(next);

        next.tell(new CounterClockwise(new_reihenfolge));
        return this;
    }

    private Behavior<Message> onTerminate(Terminate msg) {
        getContext().getLog().info("Terminate");
        return Behaviors.stopped();
    }
}