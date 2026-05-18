package org.ulpgc.dacd.businessunit.control;

import org.ulpgc.dacd.businessunit.control.persistence.repositories.EventRepository;
import com.google.gson.JsonObject;

public class RepositoryController {
    private final EventRepository newsRepository;
    private final EventRepository matchesRepository;

    public RepositoryController(EventRepository newsRepository, EventRepository matchesRepository) {
        this.newsRepository = newsRepository;
        this.matchesRepository = matchesRepository;
    }

    public void processNews(String topic, JsonObject newsEvent) {
        newsRepository.save(newsEvent);
    }

    public void processMatch(String topic, JsonObject matchEvent) {
        matchesRepository.save(matchEvent);
    }
}