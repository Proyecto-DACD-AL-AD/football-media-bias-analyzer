import org.ulpgc.dacd.control.Controller;
import org.ulpgc.dacd.control.persistence.EventStore;
import org.ulpgc.dacd.control.persistence.FileEventStore;
import org.ulpgc.dacd.control.subscriber.Subscriber;

import java.util.Arrays;
import java.util.List;

public class Main {

    public static void main(String[] args) {

        EventStore fileStore = new FileEventStore("eventstore");

        Subscriber newsSubscriber = new Subscriber(
                "tcp://localhost:61616",
                "news",
                "news-builder-client",
                fileStore
        );

        Subscriber matchesSubscriber = new Subscriber(
                "tcp://localhost:61616",
                "football-matches",
                "matches-builder-client",
                fileStore
        );

        List<Subscriber> subscribers = Arrays.asList(newsSubscriber, matchesSubscriber);

        Controller controller = new Controller(subscribers);
        controller.start();
    }
}