package org.ulpgc.dacd.eventstore.control;

import org.ulpgc.dacd.eventstore.control.persistence.EventStore;
import org.ulpgc.dacd.eventstore.control.subscriber.Subscriber;

import java.util.List;

public class Controller {

    private final List<Subscriber> subscribers;
    private final EventStore eventStore;

    public Controller(List<Subscriber> subscribers, EventStore eventStore) {
        this.subscribers = subscribers;
        this.eventStore = eventStore;
    }

    public void start() {
        for (Subscriber subscriber : subscribers) {
            subscriber.startConsuming(eventStore::save);
        }
    }
}