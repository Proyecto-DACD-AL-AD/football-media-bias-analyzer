package org.ulpgc.dacd.businessunit;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.ulpgc.dacd.businessunit.control.Controller;
import org.ulpgc.dacd.businessunit.control.subscriber.Subscriber;
import jakarta.jms.Connection;
import jakarta.jms.ConnectionFactory;

public class Main {
    public static void main(String[] args) {
        try {
            ConnectionFactory factory = new ActiveMQConnectionFactory("tcp://localhost:61616");
            Connection connection = factory.createConnection();
            connection.setClientID("business-unit-client");
            connection.start();

            Subscriber newsSubscriber = new Subscriber(
                    connection,
                    "news",
                    "business-news-sub"
            );

            Subscriber matchesSubscriber = new Subscriber(
                    connection,
                    "football-matches",
                    "business-matches-sub"
            );

            Controller controller = new Controller(newsSubscriber, matchesSubscriber);
            controller.start();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}