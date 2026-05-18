import org.ulpgc.dacd.api.control.Controller;
import org.ulpgc.dacd.api.control.feeder.ApiFootballMatchFeeder;
import org.ulpgc.dacd.api.control.parser.FootballMatchParser;
import org.ulpgc.dacd.api.control.persistence.FootballMatchPublisher;
import org.ulpgc.dacd.api.control.config.TokenLoader;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Main {

    public static void main(String[] args) {
        String apiToken = TokenLoader.loadKey("football.api.token");
        if (apiToken == null || apiToken.trim().isEmpty()) {
            System.err.println("API_TOKEN is not configured in application.properties");
            System.exit(1);
        }
        ApiFootballMatchFeeder apiFeeder = new ApiFootballMatchFeeder();
        FootballMatchParser matchFilter = new FootballMatchParser();
        FootballMatchPublisher matchStorer = new FootballMatchPublisher("tcp://localhost:61616", "football-matches");
        Controller controller = new Controller(apiFeeder, matchFilter, matchStorer);

        System.out.println("Starting the football match collector...");

        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(controller::start, 0, 12, TimeUnit.HOURS);
    }
}