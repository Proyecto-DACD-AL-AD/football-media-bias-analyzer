package org.ulpgc.dacd.control;

import java.util.List;

public class Controller {

    private final List<Subscriber> subscribers;

    public Controller(List<Subscriber> subscribers) {
        this.subscribers = subscribers;
    }

    public void start() {
        System.out.println("Iniciando el Controller del Event Store Builder...");
        for (Subscriber subscriber : subscribers) {
            subscriber.start();
        }
    }
}