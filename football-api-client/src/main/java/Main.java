import org.ulpgc.dacd.api.ApiFootballMatchFeeder;
import org.ulpgc.dacd.filter.FootballMatchFilter;
import org.ulpgc.dacd.persistence.DatabaseFootballMatchStore;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Main {

    public static void main(String[] args) {

        ApiFootballMatchFeeder apiFeeder = new ApiFootballMatchFeeder();
        FootballMatchFilter matchFilter = new FootballMatchFilter();
        DatabaseFootballMatchStore matchStorer = new DatabaseFootballMatchStore();

        Controller controller = new Controller(apiFeeder, matchFilter, matchStorer);

        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

        Runnable feedingTask = () -> {
            System.out.println("\n--- Iniciando ciclo de actualización ---");
            controller.start();
        };

        scheduler.scheduleAtFixedRate(feedingTask, 0, 1, TimeUnit.HOURS);
    }
}
