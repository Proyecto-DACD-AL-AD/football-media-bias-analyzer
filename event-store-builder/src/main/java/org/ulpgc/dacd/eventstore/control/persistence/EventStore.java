package org.ulpgc.dacd.eventstore.control.persistence;

import com.google.gson.JsonObject;

public interface EventStore {
    void save(String topic, JsonObject event);
}