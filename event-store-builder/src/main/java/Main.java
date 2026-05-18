import org.apache.activemq.ActiveMQConnectionFactory;
import jakarta.jms.Connection;
import jakarta.jms.ConnectionFactory;
import jakarta.jms.JMSException;
import org.ulpgc.dacd.eventstore.control.Controller;
import org.ulpgc.dacd.eventstore.control.persistence.EventStore;
import org.ulpgc.dacd.eventstore.control.persistence.FileEventStore;
import org.ulpgc.dacd.eventstore.control.subscriber.Subscriber;

import java.util.Arrays;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        try {
            ConnectionFactory factory = new ActiveMQConnectionFactory("tcp://localhost:61616");
            Connection connection = factory.createConnection();
            connection.setClientID("event-store-builder-client");
            connection.start();

            EventStore fileStore = new FileEventStore("eventstore");
            Subscriber newsSubscriber = new Subscriber(connection, "news", "news-sub");
            Subscriber matchesSubscriber = new Subscriber(connection, "football-matches", "matches-sub");
            List<Subscriber> subscribers = Arrays.asList(newsSubscriber, matchesSubscriber);

            Controller controller = new Controller(subscribers, fileStore);
            controller.start();
        } catch (JMSException e) {
            System.err.println("Error starting ActiveMQ: " + e.getMessage());
        }
    }
}