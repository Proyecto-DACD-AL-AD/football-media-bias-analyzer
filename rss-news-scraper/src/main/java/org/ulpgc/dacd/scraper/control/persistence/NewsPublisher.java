package org.ulpgc.dacd.scraper.control.persistence;

import com.google.gson.Gson;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.ulpgc.dacd.scraper.model.NewsArticle;

import jakarta.jms.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NewsPublisher implements NewsStore {
    private final String brokerUrl;
    private final String topicName;
    private final Gson serializer;
    private final Path watermarkFile;
    private final Map<String, Instant> lastPublishedDates;

    public NewsPublisher(String brokerUrl, String topicName) {
        this.brokerUrl = brokerUrl;
        this.topicName = topicName;
        this.serializer = EventSerializer.create();
        this.watermarkFile = Paths.get("last_dates_" + topicName + ".txt");
        this.lastPublishedDates = loadLastDates();
    }

    @Override
    public void store(List<NewsArticle> articles) {

        articles.sort(Comparator.comparing(NewsArticle::pubDate));

        try (Connection connection = createConnection()) {
            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            Destination destination = session.createTopic(topicName);
            MessageProducer producer = session.createProducer(destination);

            boolean datesUpdated = false;
            int eventsPublishedCounter = 0;

            for (NewsArticle article : articles) {
                Instant articleDate = article.pubDate();
                String source = article.source();
                Instant lastDateForSource = lastPublishedDates.getOrDefault(source, Instant.EPOCH);

                if (articleDate.isAfter(lastDateForSource)) {
                    String json = serializer.toJson(article);
                    TextMessage message = session.createTextMessage(json);
                    producer.send(message);

                    lastPublishedDates.put(source, articleDate);
                    datesUpdated = true;
                    eventsPublishedCounter++;
                }
            }
            saveLastDates(datesUpdated);
            System.out.println("Se han enviado " + eventsPublishedCounter + " noticias NUEVAS al topic: '" + topicName + "'...");

        } catch (JMSException e) {
            System.err.println("Error al enviar a ActiveMQ: " + e.getMessage());
        }
    }

    private Connection createConnection() throws JMSException {
        ConnectionFactory factory = new ActiveMQConnectionFactory(brokerUrl);
        Connection connection = factory.createConnection();
        connection.start();
        return connection;
    }

    private Map<String, Instant> loadLastDates() {
        Map<String, Instant> newsPaperDatesMap = new HashMap<>();
        try {
            if (Files.exists(watermarkFile)) {
                List<String> lines = Files.readAllLines(watermarkFile);
                for (String line : lines) {
                    String[] parts = line.split("=");
                    if (parts.length == 2) {
                        newsPaperDatesMap.put(parts[0], Instant.parse(parts[1]));
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("No se pudo leer el chivato de noticias. Procesando todo desde cero.");
        }
        return newsPaperDatesMap;
    }

    private void saveLastDates(boolean datesUpdated) {
        try {
            if(datesUpdated) {
                List<String> lines = lastPublishedDates.entrySet().stream()
                        .map(entry -> entry.getKey() + "=" + entry.getValue().toString())
                        .toList();
                Files.write(watermarkFile, lines); }
        } catch (Exception e) {
            System.err.println("Error escribiendo el chivato de noticias: " + e.getMessage());
        }
    }
}