import java.util.*;

public class Main {

    public static void main(String[] args) {

        ApiFootballMatchFeeder apiFeeder = new ApiFootballMatchFeeder();
        FootballMatchFilter matchFilter = new FootballMatchFilter();
        DatabaseFootballMatchSerializer databaseSerializer = new DatabaseFootballMatchSerializer();

        Controller controller = new Controller(apiFeeder, matchFilter, databaseSerializer);

        controller.start();
    }
}
