package org.ulpgc.dacd.businessunit;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.ulpgc.dacd.businessunit.control.Controller;
import org.ulpgc.dacd.businessunit.control.EventReader;
import org.ulpgc.dacd.businessunit.control.persistence.DatabaseManager;
import org.ulpgc.dacd.businessunit.control.persistence.SentimentRepository;
import org.ulpgc.dacd.businessunit.control.subscriber.Subscriber;
import jakarta.jms.Connection;
import jakarta.jms.ConnectionFactory;
import java.io.File;

public class Main {
    public static void main(String[] args) {
        String directory = "datamart";
        String dbPath = directory + "/datamart.db";
        String eventStorePath = "eventstore";

        new File(directory).mkdirs();
        DatabaseManager dbManager = new DatabaseManager(dbPath);
        dbManager.initDatabase();

        SentimentRepository repository = new SentimentRepository(dbManager);

        EventReader newsReader = new EventReader(eventStorePath, "news");
        EventReader matchesReader = new EventReader(eventStorePath, "football-matches");

        try {
            ConnectionFactory factory = new ActiveMQConnectionFactory("tcp://localhost:61616");
            Connection connection = factory.createConnection();
            connection.setClientID("business-unit-client");
            connection.start();

            Subscriber newsSub = new Subscriber(connection, "news", "business-news-sub");
            Subscriber matchesSub = new Subscriber(connection, "football-matches", "business-matches-sub");

            Controller controller = new Controller(newsSub, matchesSub, repository);

            newsReader.readStore(controller::processNews);
            matchesReader.readStore(controller::processMatch);

            controller.start();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}