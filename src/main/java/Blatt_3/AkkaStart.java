package Blatt_3;

import akka.actor.typed.ActorSystem;
import java.io.IOException;

public class AkkaStart {
    public static void main(String[] args) {
        final ActorSystem<AkkaMainSystem.Create> messageMain = ActorSystem.create(AkkaMainSystem.create(), "akkaMainSystem");

        messageMain.tell(new AkkaMainSystem.Create());

        try {
            System.out.println(">>> Press ENTER to exit <<<");
            System.in.read();
        } catch (IOException ignored) {
        } finally {
            messageMain.terminate();
        }
    }
}
