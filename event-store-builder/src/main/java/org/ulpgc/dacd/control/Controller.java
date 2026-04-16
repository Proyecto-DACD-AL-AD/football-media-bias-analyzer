package org.ulpgc.dacd.control;

public class Controller {

    private final Subscriber subscriber;

    public Controller(Subscriber subscriber) {
        this.subscriber = subscriber;
    }

    public void start() {
        System.out.println("Iniciando el Controller del Event Store Builder...");
        subscriber.start();
    }
}
