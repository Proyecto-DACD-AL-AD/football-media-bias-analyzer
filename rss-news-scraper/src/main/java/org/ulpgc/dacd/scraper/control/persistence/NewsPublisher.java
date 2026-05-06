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
        this.watermarkFile = Paths.get("last_dates_" + topicName + ".json");
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
                String json = Files.readString(watermarkFile);
                java.lang.reflect.Type stringMapType = new com.google.gson.reflect.TypeToken<Map<String, String>>(){}.getType();
                Map<String, String> rawMap = serializer.fromJson(json, stringMapType);
                if (rawMap != null) {
                    for (Map.Entry<String, String> entry : rawMap.entrySet()) {
                        newsPaperDatesMap.put(entry.getKey(), Instant.parse(entry.getValue()));
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error real leyendo el chivato: " + e.getMessage());
            e.printStackTrace();
        }
        return newsPaperDatesMap;
    }

    private void saveLastDates(boolean datesUpdated) {
        try {
            if (datesUpdated) {
                String json = serializer.toJson(lastPublishedDates);
                Files.writeString(watermarkFile, json);
            }
        } catch (Exception e) {
            System.err.println("Error escribiendo el chivato de noticias: " + e.getMessage());
        }
    }
}