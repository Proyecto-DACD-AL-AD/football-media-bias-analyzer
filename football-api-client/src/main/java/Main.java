import org.ulpgc.dacd.control.Controller;
import org.ulpgc.dacd.control.feeder.ApiFootballMatchFeeder;
import org.ulpgc.dacd.control.filter.FootballMatchFilter;
import org.ulpgc.dacd.control.persistence.FootballMatchPublisher;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Main {

    public static void main(String[] args) {

        ApiFootballMatchFeeder apiFeeder = new ApiFootballMatchFeeder();
        FootballMatchFilter matchFilter = new FootballMatchFilter();
        FootballMatchPublisher matchStorer = new FootballMatchPublisher("tcp://localhost:61616", "football-matches");

        Controller controller = new Controller(apiFeeder, matchFilter, matchStorer);

        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

        Runnable feedingTask = () -> {
            System.out.println("\n--- Iniciando ciclo de actualización ---");
            controller.start();
        };

        scheduler.scheduleAtFixedRate(feedingTask, 0, 1, TimeUnit.HOURS);
    }
}
