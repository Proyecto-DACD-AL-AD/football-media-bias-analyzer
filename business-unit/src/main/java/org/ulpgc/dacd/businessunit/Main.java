package org.ulpgc.dacd.businessunit;

import org.ulpgc.dacd.businessunit.control.RepositoryController;
import org.ulpgc.dacd.businessunit.control.SubscriberController;
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

            RepositoryController repositoryController = new RepositoryController(newsRepository, matchesRepository);
            DashboardRepository dashboardRepository = new DashboardRepository(dbManager);
            SubscriberController subscriberController = initializer.buildSubscriberController(connection, repositoryController);
            DashboardApi api = initializer.buildApi(dashboardRepository);

            initializer.loadHistoricalData(repositoryController);
            api.start();
            subscriberController.start();
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }
}