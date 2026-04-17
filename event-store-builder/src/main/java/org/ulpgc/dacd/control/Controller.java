package org.ulpgc.dacd.control;

import org.ulpgc.dacd.control.subscriber.Subscriber;

import java.util.List;

public class Controller {

    private final List<Subscriber> subscribers;

    public Controller(List<Subscriber> subscribers) {
        this.subscribers = subscribers;
    }

    public void start() {
        for (Subscriber subscriber : subscribers) {
            subscriber.startConsuming();
        }
    }
}