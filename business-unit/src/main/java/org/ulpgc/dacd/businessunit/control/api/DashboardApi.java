package org.ulpgc.dacd.businessunit.control.api;

import io.javalin.Javalin;
import org.ulpgc.dacd.businessunit.control.persistence.DatabaseManager;

public class DashboardApi {
    private final DatabaseManager dbManager;

    public DashboardApi(DatabaseManager dbManager) {
        this.dbManager = dbManager;
    }

    public void start() {
        Javalin app = Javalin.create(config -> {
            config.bundledPlugins.enableCors(cors -> cors.addRule(it -> it.anyHost()));
        }).start(8080);

        app.get("/api/status", ctx -> {
            ctx.contentType("application/json");
            ctx.result("{\"status\": \"API conectada y lista para servir datos\", \"port\": 8080}");
        });
    }
}