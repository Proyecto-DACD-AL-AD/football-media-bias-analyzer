package org.ulpgc.dacd.businessunit.control;

import org.ulpgc.dacd.businessunit.control.subscriber.Subscriber;
import com.google.gson.JsonObject;

public class Controller {
    private final Subscriber newsSubscriber;
    private final Subscriber matchesSubscriber;

    public Controller(Subscriber newsSubscriber, Subscriber matchesSubscriber) {
        this.newsSubscriber = newsSubscriber;
        this.matchesSubscriber = matchesSubscriber;
    }

    public void start() {
        System.out.println("Arrancando la Business Unit...");

        newsSubscriber.startConsuming(this::processNews);
        matchesSubscriber.startConsuming(this::processMatch);
    }


    private void processNews(String topic, JsonObject json) {
        System.out.println("[NOTICIA RECIBIDA]: " + json.get("ss").getAsString());
    }

    private void processMatch(String topic, JsonObject json) {
        System.out.println("[PARTIDO RECIBIDO]: " + json.get("ss").getAsString());
    }
}