import org.ulpgc.dacd.control.Controller;
import org.ulpgc.dacd.control.EventStore;
import org.ulpgc.dacd.control.Subscriber;

public class Main {


    public static void main(String[] args) {

        EventStore mockStore = (topic, eventJson) -> {
            System.out.println("Mensaje interceptado del topic: '" + topic);
            System.out.println("Contenido: " + eventJson);
            System.out.println("-------------------------------------------------");
        };


        Subscriber subscriber = new Subscriber(
                "tcp://localhost:61616",
                "football-matches",
                "event-store-builder-client",
                mockStore
        );

        Controller controller = new Controller(subscriber);
        controller.start();
    }
}