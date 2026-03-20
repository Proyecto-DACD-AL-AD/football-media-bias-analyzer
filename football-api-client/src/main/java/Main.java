import java.util.List;

public class Main {

    public static void main(String[] args) {

        FootballApiClient apiClient = new FootballApiClient();
        FootballMatchFilter matchFilter = new FootballMatchFilter();
        FootballDatabaseManager databaseManager = new FootballDatabaseManager();

        try {
            String rawJson = apiClient.getAllMatches();
            List<MatchResponse> matchesToSave = matchFilter.filterMatches(rawJson);
            databaseManager.createTable();



        } catch (Exception e) {
            System.out.println("Error en el proceso: " + e.getMessage());
        }

    }
}
