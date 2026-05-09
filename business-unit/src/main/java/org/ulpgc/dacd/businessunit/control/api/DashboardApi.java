package org.ulpgc.dacd.businessunit.control.api;

import com.google.gson.Gson;
import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.plugin.bundled.CorsPluginConfig;
import org.ulpgc.dacd.businessunit.control.config.TeamsProvider;
import org.ulpgc.dacd.businessunit.control.persistence.repositories.DashboardRepository;

public class DashboardApi {
    private static final int BAD_REQUEST_STATUS = 400;
    private final DashboardRepository repository;
    private final TeamsProvider teamsProvider;
    private final Gson gson;
    private final int apiPort;

    public DashboardApi(DashboardRepository repository, int apiPort) {
        this.repository = repository;
        this.teamsProvider = new TeamsProvider();
        this.gson = new Gson();
        this.apiPort = apiPort;
    }

    public void start() {
        Javalin app = Javalin.create(config -> {
            config.bundledPlugins.enableCors(cors -> cors.addRule(CorsPluginConfig.CorsRule::anyHost));
            config.staticFiles.add("/public");
        }).start(apiPort);

        app.get("/api/teams", context -> renderJson(context, teamsProvider.getTeams()));
        app.get("/api/thermometer", context -> handleTeamQuery(context, repository::getThermometerData));
        app.get("/api/radar", context -> handleTeamQuery(context, repository::getRadarData));
        app.get("/api/scatter", context -> handleTeamQuery(context, repository::getScatterData));
        app.get("/api/global-stats", context -> renderJson(context, repository.getGlobalStats()));
    }

    private void handleTeamQuery(Context context, java.util.function.Function<String, Object> queryProvider) {
        String team = context.queryParam("team");
        if (team == null || team.isBlank()) {
            context.status(BAD_REQUEST_STATUS).result("Parameter 'team' is missing");
            return;
        }
        renderJson(context, queryProvider.apply(team));
    }

    private void renderJson(Context context, Object data) {
        context.contentType("application/json");
        context.result(gson.toJson(data));
    }
}