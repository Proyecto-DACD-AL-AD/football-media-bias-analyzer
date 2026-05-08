package org.ulpgc.dacd.businessunit.control.api;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import io.javalin.Javalin;
import org.ulpgc.dacd.businessunit.control.persistence.repositories.DashboardRepository;

public class DashboardApi {
    private final DashboardRepository repository;
    private final Gson gson;

    public DashboardApi(DashboardRepository repository) {
        this.repository = repository;
        this.gson = new Gson();
    }

    public void start() {
        Javalin app = Javalin.create(config -> {
            config.bundledPlugins.enableCors(cors -> cors.addRule(it -> it.anyHost()));
        }).start(8080);

        app.get("/api/status", ctx -> {
            ctx.contentType("application/json");
            ctx.result("{\"status\": \"API conectada\", \"port\": 8080}");
        });

        app.get("/api/thermometer", ctx -> {
            String team = ctx.queryParam("team");
            if (team == null || team.isEmpty()) {
                ctx.status(400).result("Falta el parametro 'team'");
                return;
            }

            JsonArray data = repository.getThermometerData(team);
            ctx.contentType("application/json");
            ctx.result(gson.toJson(data));
        });
    }
}