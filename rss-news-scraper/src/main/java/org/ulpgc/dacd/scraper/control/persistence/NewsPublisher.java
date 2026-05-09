package org.ulpgc.dacd.scraper.control.persistence;

import com.google.gson.Gson;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.ulpgc.dacd.scraper.control.api.HuggingFaceClient;
import org.ulpgc.dacd.scraper.control.api.SentimentParser;
import org.ulpgc.dacd.scraper.control.config.TokenLoader;
import org.ulpgc.dacd.scraper.model.NewsArticle;

import jakarta.jms.*;
import java.time.Instant;
import java.util.*;

public class NewsPublisher implements NewsStore {
    private final String brokerUrl;
    private final String topicName;
    private final Gson serializer;
    private final Map<String, Instant> lastPublishedDates;
    private final HuggingFaceClient sentimentClient;
    private final Set<String> publishedUrls;
    private final Map<String, Double> sentimentCache;
    private final NewsWatermarkManager watermarkManager;

    public NewsPublisher(String brokerUrl, String topicName) {
        this.brokerUrl = brokerUrl;
        this.topicName = topicName;
        this.serializer = EventSerializer.create();
        this.publishedUrls = new HashSet<>();
        this.sentimentCache = new HashMap<>();
        this.watermarkManager = new NewsWatermarkManager(topicName);
        this.lastPublishedDates = this.watermarkManager.loadLastDates();

        String sentimentToken = TokenLoader.loadKey("hf.api.key.sentiment");
        this.sentimentClient = new HuggingFaceClient(sentimentToken);
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

                double sentimentScore = calculateSentiment(article);
                NewsArticle scoredArticle = addSentimentToArticle(article, sentimentScore);

                String json = serializer.toJson(scoredArticle);
                producer.send(session.createTextMessage(json));

                lastPublishedDates.put(key, article.pubDate());
                publishedUrls.add(article.link());
                datesUpdated = true;
                eventsPublishedCounter++;
            }
            if (datesUpdated) this.watermarkManager.saveLastDates(lastPublishedDates);

            System.out.printf("Fuente: %-18s | Equipo: %-25s | Noticias recolectadas: %d%n",
                    source, team, eventsPublishedCounter);

        } catch (JMSException e) {
            System.err.println("Fallo al enviar a ActiveMQ: " + e.getMessage());
        }
    }

    private double calculateSentiment(NewsArticle article) {
        String textToAnalyze = (article.title() + ". " + article.summary()).trim();

        if (textToAnalyze.equals(".")) return 0.0;
        if (sentimentCache.containsKey(article.link())) return sentimentCache.get(article.link());

        String sentimentJson = sentimentClient.analyze(textToAnalyze, "cardiffnlp/twitter-xlm-roberta-base-sentiment");
        double score = SentimentParser.parse(sentimentJson);
        sentimentCache.put(article.link(), score);

        return score;
    }

    private NewsArticle addSentimentToArticle(NewsArticle article, double sentimentScore) {
        return new NewsArticle(
                article.title(), article.summary(), article.link(),
                article.pubDate(), article.source(), article.team(),
                sentimentScore, article.ss(), article.ts()
        );
    }

    private Connection createConnection() throws JMSException {
        ConnectionFactory factory = new ActiveMQConnectionFactory(brokerUrl);
        Connection connection = factory.createConnection();
        connection.start();
        return connection;
    }
}