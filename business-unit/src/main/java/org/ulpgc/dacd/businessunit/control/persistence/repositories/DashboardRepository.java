package org.ulpgc.dacd.businessunit.control.persistence.repositories;

import com.google.gson.JsonArray;
import org.ulpgc.dacd.businessunit.control.persistence.DatabaseManager;
import org.ulpgc.dacd.businessunit.control.persistence.queries.GlobalStatsQuery;
import org.ulpgc.dacd.businessunit.control.persistence.queries.RadarQuery;
import org.ulpgc.dacd.businessunit.control.persistence.queries.ScatterQuery;
import org.ulpgc.dacd.businessunit.control.persistence.queries.ThermometerQuery;

public class DashboardRepository {
    private final ThermometerQuery thermometerQuery;
    private final RadarQuery radarQuery;
    private final ScatterQuery scatterQuery;
    private final GlobalStatsQuery globalStatsQuery;

    public DashboardRepository(DatabaseManager dbManager) {
        this.thermometerQuery = new ThermometerQuery(dbManager);
        this.radarQuery = new RadarQuery(dbManager);
        this.scatterQuery = new ScatterQuery(dbManager);
        this.globalStatsQuery = new GlobalStatsQuery(dbManager);
    }

    public JsonArray getThermometerData(String team) {
        return thermometerQuery.execute(team);
    }

    public JsonArray getRadarData(String team) {
        return radarQuery.execute(team);
    }

    public JsonArray getScatterData(String team) {
        return scatterQuery.execute(team);
    }

    public JsonArray getGlobalStats() {
        return globalStatsQuery.execute();
    }
}