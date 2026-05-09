package org.ulpgc.dacd.api.control.persistence;

import com.google.gson.Gson;
import org.apache.activemq.ActiveMQConnectionFactory;
import jakarta.jms.*;
import org.ulpgc.dacd.api.model.Match;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;

public class FootballMatchPublisher implements FootballMatchStore {
    private final String brokerUrl;
    private final String topicName;
    private final Gson serializer;
    private final MatchWatermarkManager watermarkManager;
    private Instant lastPublishedDate;

    public FootballMatchPublisher(String brokerUrl, String topicName) {
        this.brokerUrl = brokerUrl;
        this.topicName = topicName;
        this.serializer = EventSerializer.create();
        this.watermarkManager = new MatchWatermarkManager(topicName);
        this.lastPublishedDate = this.watermarkManager.loadLastDate();
    }

    @Override
    public void store(List<Match> matches) {
        matches.sort(Comparator.comparing(Match::date));
        try (Connection connection = createConnection()) {
            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            Destination destination = session.createTopic(topicName);
            MessageProducer producer = session.createProducer(destination);
            boolean dateUpdated = false;
            int eventsPublishedCounter = 0;

            for (Match match : matches) {
                Instant matchDate = match.date();
                if (matchDate.isAfter(lastPublishedDate)) {
                    String jsonEvent = serializer.toJson(match);
                    TextMessage message = session.createTextMessage(jsonEvent);
                    producer.send(message);

                    lastPublishedDate = matchDate;
                    dateUpdated = true;
                    eventsPublishedCounter++;
                }
            }

            if (dateUpdated) watermarkManager.saveLastDate(lastPublishedDate);
            System.out.println(eventsPublishedCounter + " new messages have been sent to the topic '" + topicName + "'...");

        } catch (JMSException e) {
            System.err.println("Error sending message to ActiveMQ: " + e.getMessage());
        }
    }

    private Connection createConnection() throws JMSException {
        ConnectionFactory factory = new ActiveMQConnectionFactory(brokerUrl);
        Connection connection = factory.createConnection();
        connection.start();
        return connection;
    }
}