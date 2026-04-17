import org.ulpgc.dacd.control.Controller;
import org.ulpgc.dacd.control.feeder.ApiFootballMatchFeeder;
import org.ulpgc.dacd.control.filter.FootballMatchFilter;
import org.ulpgc.dacd.control.persistence.FootballMatchPublisher;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Main {

    public static void main(String[] args) {

        if(System.getenv("API_TOKEN") == null || System.getenv("API_TOKEN").trim().isEmpty()) {
            System.err.println("La variable de entorno API_TOKEN no está configurada");
            System.exit(1);
        }

        ApiFootballMatchFeeder apiFeeder = new ApiFootballMatchFeeder();
        FootballMatchFilter matchFilter = new FootballMatchFilter();
        FootballMatchPublisher matchStorer = new FootballMatchPublisher("tcp://localhost:61616", "football-matches");

        Controller controller = new Controller(apiFeeder, matchFilter, matchStorer);

        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        System.out.println("Iniciando el recolector de partidos de fútbol...");

        scheduler.scheduleAtFixedRate(controller::start, 0, 12, TimeUnit.HOURS);
    }
}