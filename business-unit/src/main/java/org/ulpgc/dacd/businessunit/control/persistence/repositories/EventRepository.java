package org.ulpgc.dacd.businessunit.control.persistence.repositories;

import com.google.gson.JsonObject;

public interface EventRepository {
    void save(JsonObject json);
}