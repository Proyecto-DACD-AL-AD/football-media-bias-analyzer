package org.ulpgc.dacd.businessunit.control.persistence.repositories;

import com.google.gson.JsonArray;
import org.ulpgc.dacd.businessunit.control.persistence.DatabaseManager;
import org.ulpgc.dacd.businessunit.control.persistence.queries.ThermometerQuery;

public class DashboardRepository {
    private final ThermometerQuery thermometerQuery;

    public DashboardRepository(DatabaseManager dbManager) {
        this.thermometerQuery = new ThermometerQuery(dbManager);
    }

    public JsonArray getThermometerData(String team) {
        return thermometerQuery.execute(team);
    }
}