package Blatt_4;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.*;

import java.time.Duration;
import java.util.ArrayList;

public class AkkaMainSystem extends AbstractBehavior<AkkaMainSystem.Message> {



    public interface Message { }
    public static class Create implements Message { }
    public static class Terminate implements Message { }

    public static Behavior<Message> create() {
        return Behaviors.setup(context -> Behaviors.withTimers(timers -> new AkkaMainSystem(context, timers)));
    }

    private final TimerScheduler<Message> timers;
    private ArrayList<ActorRef<TimerActor.Message>> all_actors;

    private AkkaMainSystem(ActorContext<Message> context, TimerScheduler<Message> timers) {
        super(context);
        this.timers = timers;
    }

    @Override
    public Receive<Message> createReceive() {
        return newReceiveBuilder().onMessage(Create.class, this::onCreate).onMessage(Terminate.class, this::onTerminate).build();
    }

    private Behavior<Message> onTerminate(Terminate msg) {
        for (ActorRef<TimerActor.Message> actor : all_actors) {
            actor.tell(new TimerActor.Terminate());
        }
        return this;
    }

    private Behavior<Message> onCreate(Create command) {
        var alice = this.getContext().spawn(TimerActor.create("alice"), "alice");
        var bob = this.getContext().spawn(TimerActor.create("bob"), "bob");
        var charlie = this.getContext().spawn(TimerActor.create("charlie"), "charlie");

        ArrayList<ActorRef<TimerActor.Message>> clock_reihenfolge = new ArrayList<>();
        clock_reihenfolge.add(bob);
        clock_reihenfolge.add(charlie);
        clock_reihenfolge.add(alice);

        ArrayList<ActorRef<TimerActor.Message>> counterclock_reihenfolge = new ArrayList<>();
        counterclock_reihenfolge.add(bob);
        counterclock_reihenfolge.add(charlie);
        counterclock_reihenfolge.add(alice);

        this.all_actors = new ArrayList<>(clock_reihenfolge);
        alice.tell(new TimerActor.Clockwise(clock_reihenfolge));
        alice.tell(new TimerActor.CounterClockwise(counterclock_reihenfolge));

        timers.startSingleTimer(new Terminate(), Duration.ofSeconds(60));

        return this;
    }
}
