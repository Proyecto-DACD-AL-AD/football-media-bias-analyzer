package org.ulpgc.dacd.scraper.control.persistence;

import com.google.gson.Gson;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.ulpgc.dacd.scraper.control.api.HuggingFaceClient;
import org.ulpgc.dacd.scraper.control.api.SentimentParser;
import org.ulpgc.dacd.scraper.control.config.TokenLoader;
import org.ulpgc.dacd.scraper.model.NewsArticle;

import jakarta.jms.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.*;

public class NewsPublisher implements NewsStore {
    private final String brokerUrl;
    private final String topicName;
    private final Gson serializer;
    private final Path watermarkFile;
    private final Map<String, Instant> lastPublishedDates;
    private final HuggingFaceClient sentimentClient;
    private final Set<String> publishedUrls;
    private final Map<String, Double> sentimentCache;

    public NewsPublisher(String brokerUrl, String topicName) {
        this.brokerUrl = brokerUrl;
        this.topicName = topicName;
        this.serializer = EventSerializer.create();
        this.watermarkFile = Paths.get("state/last_dates_" + topicName + ".json");
        this.lastPublishedDates = loadLastDates();
        this.publishedUrls = new HashSet<>();
        this.sentimentCache = new HashMap<>();

        String sentimentToken = TokenLoader.loadKey("hf.api.key.sentiment");
        this.sentimentClient = new HuggingFaceClient(sentimentToken);
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
                String key = article.source() + "-" + article.team();

                Instant lastDateForKey = lastPublishedDates.getOrDefault(key, Instant.EPOCH);

                if (articleDate.isAfter(lastDateForKey)) {
                    if (publishedUrls.contains(article.link())) {
                        continue;
                    }

                    String textToAnalyze = (article.title() + ". " + article.summary()).trim();
                    double sentimentScore = 0.0;

                    if (!textToAnalyze.equals(".")) {
                        if (sentimentCache.containsKey(article.link())) {
                            sentimentScore = sentimentCache.get(article.link());
                        } else {
                            String sentimentJson = sentimentClient.analyze(textToAnalyze, "cardiffnlp/twitter-xlm-roberta-base-sentiment");
                            sentimentScore = SentimentParser.parse(sentimentJson);
                            sentimentCache.put(article.link(), sentimentScore);
                        }
                    }

                    NewsArticle scoredArticle = new NewsArticle(
                            article.title(),
                            article.summary(),
                            article.link(),
                            article.pubDate(),
                            article.source(),
                            article.team(),
                            sentimentScore,
                            article.ss(),
                            article.ts()
                    );

                    String json = serializer.toJson(scoredArticle);
                    TextMessage message = session.createTextMessage(json);
                    producer.send(message);

                    lastPublishedDates.put(key, articleDate);
                    publishedUrls.add(article.link());
                    datesUpdated = true;
                    eventsPublishedCounter++;
                }
            }
            saveLastDates(datesUpdated);

            if (eventsPublishedCounter > 0) {
                System.out.println("Se han enviado " + eventsPublishedCounter + " noticias NUEVAS al topic: '" + topicName + "'...");
            }

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
            System.err.println("Error leyendo el watermark: " + e.getMessage());
        }
        return newsPaperDatesMap;
    }

    private void saveLastDates(boolean datesUpdated) {
        try {
            if (datesUpdated) {
                if (watermarkFile.getParent() != null) {
                    Files.createDirectories(watermarkFile.getParent());
                }
                String json = serializer.toJson(lastPublishedDates);
                Files.writeString(watermarkFile, json);
            }
        } catch (Exception e) {
            System.err.println("Error escribiendo el chivato de noticias: " + e.getMessage());
        }
    }
}