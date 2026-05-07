package org.ulpgc.dacd.api.control.persistence;

import com.google.gson.Gson;
import org.apache.activemq.ActiveMQConnectionFactory;
import jakarta.jms.*;
import org.ulpgc.dacd.api.model.Match;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;

public class FootballMatchPublisher implements FootballMatchStore {

    private final String brokerUrl;
    private final String topicName;
    private final Gson serializer;
    private final Path watermarkFile;
    private Instant lastPublishedDate;

    public FootballMatchPublisher(String brokerUrl, String topicName) {
        this.brokerUrl = brokerUrl;
        this.topicName = topicName;
        this.serializer = EventSerializer.create();
        this.watermarkFile = Paths.get("state/last_date_" + topicName + ".txt");
        this.lastPublishedDate = loadLastDate();
    }

    @Override
    public void store(List<Match> matches) {

        matches.sort(Comparator.comparing(Match::date));

        try (Connection connection = createConnection()){
            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            Destination destination = session.createTopic(topicName);
            MessageProducer producer = session.createProducer(destination);

            boolean dateUpdated = false;
            int eventsPublishedCounter = 0;

            for (Match match: matches) {
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
            saveLastDate(lastPublishedDate, dateUpdated);
            System.out.println("Se han enviado " + eventsPublishedCounter + " mensajes NUEVOS al topic: '" + topicName + "'...");

        } catch (JMSException e) {
            System.err.println("Error al enviar el mensaje a ActiveMQ: " + e.getMessage());
        }
    }

    private Connection createConnection() throws JMSException {
        ConnectionFactory factory = new ActiveMQConnectionFactory(brokerUrl);
        Connection connection = factory.createConnection();
        connection.start();
        return connection;
    }

    private Instant loadLastDate() {
        try {
            if (Files.exists(watermarkFile)) {
                String dateStr = Files.readString(watermarkFile).trim();
                return Instant.parse(dateStr);
            }
        } catch (Exception e) {
            System.err.println("Error leyendo el watermark: " + e.getMessage());
        }
        return Instant.EPOCH;
    }

    private void saveLastDate(Instant date, boolean dateUpdated) {
        try {
            if(dateUpdated){
                if (watermarkFile.getParent() != null) {
                    Files.createDirectories(watermarkFile.getParent());
                }
                Files.writeString(watermarkFile, date.toString());
            }
        } catch (Exception e) {
            System.err.println("Error writing the date: " + e.getMessage());
        }
    }
}