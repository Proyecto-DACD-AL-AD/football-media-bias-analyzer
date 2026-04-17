package org.ulpgc.dacd.control.persistence;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class EventDeserializer {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter
            .ofPattern("yyyyMMdd")
            .withZone(ZoneId.of("UTC"));

    private final JsonObject jsonObject;

    public EventDeserializer(String eventJson) {
        this.jsonObject = JsonParser.parseString(eventJson).getAsJsonObject();
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