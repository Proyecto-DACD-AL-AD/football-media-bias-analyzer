package org.ulpgc.dacd.businessunit;

import org.ulpgc.dacd.businessunit.control.Controller;
import org.ulpgc.dacd.businessunit.control.api.DashboardApi;
import org.ulpgc.dacd.businessunit.control.config.Initializer;
import org.ulpgc.dacd.businessunit.control.persistence.DatabaseManager;
import org.ulpgc.dacd.businessunit.control.persistence.repositories.*;
import jakarta.jms.Connection;

import java.util.List;

public class Main {
    public static void main(String[] args) {
        Initializer initializer = new Initializer();
        try {
            DatabaseManager dbManager = initializer.setupDatabase();

            SqlRepository newsRepository = new NewsRepository(dbManager);
            SqlRepository matchesRepository = new MatchesRepository(dbManager);
            dbManager.initialize(List.of(newsRepository, matchesRepository));

            Connection connection = initializer.setupActiveMQConnection();
            Controller controller = initializer.buildController(connection, newsRepository, matchesRepository);

            initializer.loadHistoricalData(controller);

            DashboardRepository dashboardRepository = new DashboardRepository(dbManager);
            DashboardApi api = initializer.buildApi(dashboardRepository);
            api.start();

            controller.start();

        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }
}