package org.ulpgc.dacd.businessunit.control;

import org.ulpgc.dacd.businessunit.control.subscriber.Subscriber;

public class SubscriberController {
    private final Subscriber newsSubscriber;
    private final Subscriber matchesSubscriber;
    private final RepositoryController repositoryController;

    public SubscriberController(Subscriber newsSubscriber, Subscriber matchesSubscriber, RepositoryController repositoryController) {
        this.newsSubscriber = newsSubscriber;
        this.matchesSubscriber = matchesSubscriber;
        this.repositoryController = repositoryController;
    }

    public void start() {
        newsSubscriber.startConsuming(repositoryController::processNews);
        matchesSubscriber.startConsuming(repositoryController::processMatch);
    }
}