package org.ulpgc.dacd.scraper.control.persistence;

import com.google.gson.Gson;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.ulpgc.dacd.scraper.model.NewsArticle;
import jakarta.jms.*;

import java.time.Instant;
import java.util.*;

public class NewsPublisher implements NewsStore {
    private final String brokerUrl;
    private final String topicName;
    private final Gson serializer;
    private final Map<String, Instant> lastPublishedDates;
    private final Set<String> publishedUrls;
    private final NewsWatermarkManager watermarkManager;
    private final SentimentCalculator sentimentCalculator;

    public NewsPublisher(String brokerUrl, String topicName) {
        this.brokerUrl = brokerUrl;
        this.topicName = topicName;
        this.serializer = EventSerializer.create();
        this.publishedUrls = new HashSet<>();
        this.watermarkManager = new NewsWatermarkManager(topicName);
        this.lastPublishedDates = this.watermarkManager.loadLastDates();
        this.sentimentCalculator = new SentimentCalculator();
    }

    @Override
    public void store(List<NewsArticle> articles) {
        if (articles.isEmpty()) return;

        articles.sort(Comparator.comparing(NewsArticle::pubDate));
        String source = articles.getFirst().source();
        String team = articles.getFirst().team();

        try (Connection connection = createConnection()) {
            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            MessageProducer producer = session.createProducer(session.createTopic(topicName));
            boolean datesUpdated = false;
            int eventsPublishedCounter = 0;

            for (NewsArticle article : articles) {
                String key = article.source() + "-" + article.team();
                Instant lastDateForKey = lastPublishedDates.getOrDefault(key, Instant.EPOCH);
                if (article.pubDate().isBefore(lastDateForKey)) continue;
                if (publishedUrls.contains(article.link())) continue;

                double sentimentScore = sentimentCalculator.calculateSentiment(article);
                NewsArticle scoredArticle = sentimentCalculator.addSentimentToArticle(article, sentimentScore);
                String jsonArticle = serializer.toJson(scoredArticle);
                producer.send(session.createTextMessage(jsonArticle));

                lastPublishedDates.put(key, article.pubDate());
                publishedUrls.add(article.link());
                datesUpdated = true;
                eventsPublishedCounter++;
            }
            if (datesUpdated) this.watermarkManager.saveLastDates(lastPublishedDates);

            System.out.printf("Source: %-16s | Team: %-25s | Collected News: %d%n",
                    source, team, eventsPublishedCounter);

        } catch (JMSException e) {
            System.err.println("Error sending events to ActiveMQ: " + e.getMessage());
        }
    }

    private Connection createConnection() throws JMSException {
        ConnectionFactory factory = new ActiveMQConnectionFactory(brokerUrl);
        Connection connection = factory.createConnection();
        connection.start();
        return connection;
    }
}