package org.ulpgc.dacd.businessunit.control.config;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.ulpgc.dacd.businessunit.control.Controller;
import org.ulpgc.dacd.businessunit.control.persistence.EventReader;
import org.ulpgc.dacd.businessunit.control.persistence.DatabaseManager;
import org.ulpgc.dacd.businessunit.control.persistence.repositories.EventRepository;
import org.ulpgc.dacd.businessunit.control.subscriber.Subscriber;
import jakarta.jms.Connection;
import jakarta.jms.ConnectionFactory;
import java.io.File;

public class Initializer {
    private static final String BROKER_URL = "tcp://localhost:61616";
    private static final String CLIENT_ID = "business-unit-client";
    private static final String DIRECTORY = "datamart";
    private static final String DB_PATH = DIRECTORY + "/datamart.db";
    private static final String EVENT_STORE_PATH = "eventstore";

    public static DatabaseManager setupDatabase() {
        new File(DIRECTORY).mkdirs();
        return new DatabaseManager(DB_PATH);
    }

    public static Connection setupActiveMQConnection() throws Exception {
        ConnectionFactory factory = new ActiveMQConnectionFactory(BROKER_URL);
        Connection connection = factory.createConnection();
        connection.setClientID(CLIENT_ID);
        connection.start();
        return connection;
    }

    public static Controller buildController(Connection connection, EventRepository newsRepository, EventRepository matchesRepository) {
        Subscriber newsSub = new Subscriber(connection, "news", "business-news-sub");
        Subscriber matchesSub = new Subscriber(connection, "football-matches", "business-matches-sub");
        return new Controller(newsSub, matchesSub, newsRepository, matchesRepository);
    }

    public static void loadHistoricalData(Controller controller) {
        new EventReader(EVENT_STORE_PATH, "news").readStore(controller::processNews);
        new EventReader(EVENT_STORE_PATH, "football-matches").readStore(controller::processMatch);
    }
}