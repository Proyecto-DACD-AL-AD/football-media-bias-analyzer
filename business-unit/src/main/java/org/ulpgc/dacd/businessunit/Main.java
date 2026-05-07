package org.ulpgc.dacd.businessunit;

import org.ulpgc.dacd.businessunit.control.Controller;
import org.ulpgc.dacd.businessunit.control.config.Initializer;
import org.ulpgc.dacd.businessunit.control.persistence.DatabaseManager;
import org.ulpgc.dacd.businessunit.control.persistence.repositories.EventRepository;
import org.ulpgc.dacd.businessunit.control.persistence.repositories.NewsRepository;
import jakarta.jms.Connection;

public class Main {
    public static void main(String[] args) {
        try {
            DatabaseManager dbManager = Initializer.setupDatabase();

            EventRepository newsRepository = new NewsRepository(dbManager);
            newsRepository.initTables();

            // Añadir aqui el repositorio de partidos

            Connection connection = Initializer.setupActiveMQConnection();
            Controller controller = Initializer.buildController(connection, newsRepository);

            Initializer.loadHistoricalData(controller);

            controller.start();

        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }
}