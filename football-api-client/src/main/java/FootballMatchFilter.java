import com.google.gson.Gson;
import java.util.List;

public class FootballMatchFilter {

    public List<MatchResponse> filterMatches(String rawJson) {

        Gson gson = new Gson();
        MatchListResponse matchList = gson.fromJson(rawJson, MatchListResponse.class);
        List<String> mediaTeams = List.of("Real Madrid CF", "FC Barcelona",
                "Real Betis Balompié", "Sevilla FC", "Athletic Club", "Real Sociedad de Fútbol");

        List<MatchResponse> filteredMatches = matchList.getMatches().stream()
                .filter(match -> "FINISHED".equals(match.getStatus()))
                .filter(match ->
                        mediaTeams.contains(match.getHomeTeam().getName()) ||
                                mediaTeams.contains(match.getAwayTeam().getName())
                ).toList();

        printFilteredMatches(filteredMatches);
        return filteredMatches;
    }

    private static void printFilteredMatches(List<MatchResponse> filteredMatches) {

        for (MatchResponse match : filteredMatches) {
            String homeTeam = match.getHomeTeam().getName();
            String awayTeam = match.getAwayTeam().getName();

            int homeGoals = match.getScore().getFullTime().getHomeGoals();
            int awayGoals = match.getScore().getFullTime().getAwayGoals();
            String result = homeGoals + " - " + awayGoals;

            System.out.println(homeTeam + " [" + result + "] " + awayTeam + " | Status: " + match.getStatus());
        }
    }













}
