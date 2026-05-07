package org.ulpgc.dacd.businessunit.control;

import org.ulpgc.dacd.businessunit.control.subscriber.Subscriber;
import org.ulpgc.dacd.businessunit.control.persistence.SentimentRepository;
import com.google.gson.JsonObject;

public class Controller {
    private final Subscriber newsSubscriber;
    private final Subscriber matchesSubscriber;
    private final SentimentRepository sentimentRepository;

    public Controller(Subscriber newsSubscriber, Subscriber matchesSubscriber, SentimentRepository sentimentRepository) {
        this.newsSubscriber = newsSubscriber;
        this.matchesSubscriber = matchesSubscriber;
        this.sentimentRepository = sentimentRepository;
    }

    public void start() {
        newsSubscriber.startConsuming(this::processNews);
        matchesSubscriber.startConsuming(this::processMatch);
    }

    public void processNews(String topic, JsonObject json) {
        sentimentRepository.save(json);
    }

    public void processMatch(String topic, JsonObject json) {
        // Completar con la parte de los partidos
    }
}