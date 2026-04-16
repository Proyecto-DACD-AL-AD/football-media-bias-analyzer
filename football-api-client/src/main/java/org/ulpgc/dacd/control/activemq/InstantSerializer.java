package org.ulpgc.dacd.control.activemq;

import com.google.gson.*;
import java.time.Instant;
import java.time.format.DateTimeFormatter;

public class InstantSerializer {

    public static Gson create() {

        return new GsonBuilder()
                .registerTypeAdapter(Instant.class, (JsonSerializer<Instant>)
                        (src, typeOfSrc, context) ->
                        new JsonPrimitive(DateTimeFormatter.ISO_INSTANT.format(src)))
                .create();
    }
}