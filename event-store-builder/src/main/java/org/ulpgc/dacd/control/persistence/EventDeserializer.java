package org.ulpgc.dacd.control.persistence;

import com.google.gson.JsonObject;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class EventDeserializer {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter
            .ofPattern("yyyyMMdd")
            .withZone(ZoneId.of("UTC"));

    private final JsonObject jsonObject;

    public EventDeserializer(JsonObject jsonObject) {
        this.jsonObject = jsonObject;
    }

    public String getSourceSystem() {
        return jsonObject.get("ss").getAsString();
    }

    public String getFormattedDate() {
        String tsString = jsonObject.get("ts").getAsString();
        Instant timestamp = Instant.parse(tsString);
        return DATE_FORMATTER.format(timestamp);
    }
}