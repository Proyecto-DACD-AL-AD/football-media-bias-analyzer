package org.ulpgc.dacd.control.persistence;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializer;

import java.time.Instant;
import java.time.format.DateTimeFormatter;

public class EventSerializer {
    public static Gson create() {
        return new GsonBuilder()
                .registerTypeAdapter(Instant.class, (JsonSerializer<Instant>)
                        (src, typeOfSrc, context) ->
                                new JsonPrimitive(DateTimeFormatter.ISO_INSTANT.format(src)))
                .create();
    }
}