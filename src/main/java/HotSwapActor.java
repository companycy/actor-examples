import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;

public class HotSwapActor extends AbstractActor {
    private AbstractActor.Receive angry;
    private AbstractActor.Receive happy;

    public HotSwapActor() {
        angry =
                receiveBuilder()
                        .matchEquals("foo", s -> {
                            System.out.println("angry: receive foo");
                            getSender().tell("I am already angry?", getSelf());
                        })
                        .matchEquals("bar", s -> {
                            getContext().become(happy);
                        })
                        .match(String.class, System.out::println)
                        .build();

        happy = receiveBuilder()
                .matchEquals("bar", s -> {
                    System.out.println("happy: receive bar");
                    getSender().tell("I am already happy :-)", getSelf());
                })
                .matchEquals("foo", s -> {
                    getContext().become(angry);
                })
                .match(String.class, System.out::println)
                .build();
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .matchEquals("foo", s -> {
                            System.out.println("receive foo");
                            getContext().become(angry);
                        }
                )
                .matchEquals("bar", s -> {
                            System.out.println("receive bar");
                            getContext().become(happy);
                        }
                )
                .match(String.class, System.out::println)
                .build();
    }

    public static void main(String[] args) {
        // akka.Main.main(new String[] { Actor1.class.getName() });

        ActorSystem system = ActorSystem.create("SwapperSystem");
        ActorRef swapper = system.actorOf(Props.create(HotSwapActor.class), "swapper");
        swapper.tell("foo", swapper); // logs Hi
        swapper.tell("foo", swapper); // logs Hi

        // system.terminate();
    }

}
