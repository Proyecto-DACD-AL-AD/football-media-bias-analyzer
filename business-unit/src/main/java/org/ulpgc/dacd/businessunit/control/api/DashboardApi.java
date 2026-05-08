package org.ulpgc.dacd.businessunit.control.api;

import com.google.gson.Gson;
import io.javalin.Javalin;
import io.javalin.plugin.bundled.CorsPluginConfig;
import org.ulpgc.dacd.businessunit.control.config.TeamsProvider;
import org.ulpgc.dacd.businessunit.control.persistence.repositories.DashboardRepository;

public class DashboardApi {
    private final DashboardRepository repository;
    private final TeamsProvider teamsProvider;
    private final Gson gson;

    public DashboardApi(DashboardRepository repository) {
        this.repository = repository;
        this.teamsProvider = new TeamsProvider();
        this.gson = new Gson();
    }

    public void start() {
        Javalin app = Javalin.create(config -> {
            config.bundledPlugins.enableCors(cors -> cors.addRule(CorsPluginConfig.CorsRule::anyHost));
            config.staticFiles.add("/public");
        }).start(8080);

        app.get("/api/teams", ctx -> {
            ctx.contentType("application/json");
            ctx.result(gson.toJson(teamsProvider.getTeams()));
        });

        app.get("/api/thermometer", ctx -> {
            String team = ctx.queryParam("team");
            if (team == null || team.isEmpty()) {
                ctx.status(400).result("Falta el parametro 'team'");
                return;
            }
            ctx.contentType("application/json");
            ctx.result(gson.toJson(repository.getThermometerData(team)));
        });

        app.get("/api/radar", ctx -> {
            String team = ctx.queryParam("team");
            if (team == null || team.isEmpty()) {
                ctx.status(400).result("Falta el parametro 'team'");
                return;
            }
            ctx.contentType("application/json");
            ctx.result(gson.toJson(repository.getRadarData(team)));
        });

        app.get("/api/scatter", ctx -> {
            String team = ctx.queryParam("team");
            ctx.contentType("application/json");
            ctx.result(gson.toJson(repository.getScatterData(team)));
        });

        app.get("/api/global-stats", ctx -> {
            ctx.contentType("application/json");
            ctx.result(gson.toJson(repository.getGlobalStats()));
        });
    }
}