package org.ulpgc.dacd.control.persistence;

public interface EventStore {
    void save(String topic, String eventJson);
}
