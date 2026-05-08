package org.ulpgc.dacd.businessunit.control;

import org.ulpgc.dacd.businessunit.control.subscriber.Subscriber;
import org.ulpgc.dacd.businessunit.control.persistence.repositories.EventRepository;
import com.google.gson.JsonObject;

public class Controller {
    private final Subscriber newsSubscriber;
    private final Subscriber matchesSubscriber;
    private final EventRepository newsRepository;
    private final EventRepository matchesRepository;

    public Controller(Subscriber newsSubscriber, Subscriber matchesSubscriber, EventRepository newsRepository, EventRepository matchesRepository) {
        this.newsSubscriber = newsSubscriber;
        this.matchesSubscriber = matchesSubscriber;
        this.newsRepository = newsRepository;
        this.matchesRepository = matchesRepository;
    }

    public void start() {
        newsSubscriber.startConsuming(this::processNews);
        matchesSubscriber.startConsuming(this::processMatch);
    }

    public void processNews(String topic, JsonObject json) {
        newsRepository.save(json);
    }

    public void processMatch(String topic, JsonObject json) {
        matchesRepository.save(json);
    }
}