import org.ulpgc.dacd.api.ApiFootballMatchFeeder;
import org.ulpgc.dacd.filter.FootballMatchFilter;
import org.ulpgc.dacd.persistence.DatabaseFootballMatchSerializer;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Main {

    public static void main(String[] args) {

        ApiFootballMatchFeeder apiFeeder = new ApiFootballMatchFeeder();
        FootballMatchFilter matchFilter = new FootballMatchFilter();
        DatabaseFootballMatchSerializer databaseSerializer = new DatabaseFootballMatchSerializer();

        Controller controller = new Controller(apiFeeder, matchFilter, databaseSerializer);

        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

        Runnable feedingTask = () -> {
            System.out.println("\n--- Iniciando ciclo de actualización ---");
            controller.start();
        };

        scheduler.scheduleAtFixedRate(feedingTask, 0, 1, TimeUnit.HOURS);
    }
}
