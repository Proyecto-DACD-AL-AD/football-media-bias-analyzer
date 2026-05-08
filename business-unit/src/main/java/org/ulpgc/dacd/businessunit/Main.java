package org.ulpgc.dacd.businessunit;

import org.ulpgc.dacd.businessunit.control.Controller;
import org.ulpgc.dacd.businessunit.control.api.DashboardApi;
import org.ulpgc.dacd.businessunit.control.config.Initializer;
import org.ulpgc.dacd.businessunit.control.persistence.DatabaseManager;
import org.ulpgc.dacd.businessunit.control.persistence.repositories.EventRepository;
import org.ulpgc.dacd.businessunit.control.persistence.repositories.NewsRepository;
import org.ulpgc.dacd.businessunit.control.persistence.repositories.MatchesRepository;
import jakarta.jms.Connection;

public class Main {
    public static void main(String[] args) {
        try {
            DatabaseManager dbManager = Initializer.setupDatabase();

            EventRepository newsRepository = new NewsRepository(dbManager);
            newsRepository.initTables();

            EventRepository matchesRepository = new MatchesRepository(dbManager);
            matchesRepository.initTables();

            Connection connection = Initializer.setupActiveMQConnection();
            Controller controller = Initializer.buildController(connection, newsRepository, matchesRepository);

            Initializer.loadHistoricalData(controller);

            DashboardApi api = Initializer.buildApi(dbManager);
            api.start();

            controller.start();

        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }
}