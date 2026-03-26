import java.util.*;

public class Main {

    public static void main(String[] args) {

        ApiFootballMatchFeeder apiClient = new ApiFootballMatchFeeder();
        FootballMatchFilter matchFilter = new FootballMatchFilter();
        DatabaseFootballMatchSerializer databaseManager = new DatabaseFootballMatchSerializer();

        try {

            List<MatchResponse> matchesToSave = matchFilter.filterMatches(apiClient.getAllMatches());

            Set<Integer> matchdaySet = new HashSet<>();
            for (MatchResponse matchToSave : matchesToSave) matchdaySet.add(matchToSave.getMatchday());


            Map<Integer, Map<String, Integer>> standingsMap = new HashMap<>();

            for (int matchday : matchdaySet) {
                System.out.println("Pidiendo clasificación jornada " + matchday + "...");
                String jsonStandings = apiClient.getStandingsByMatchday(matchday);
                standingsMap.put(matchday, matchFilter.parseStandings(jsonStandings));


                Thread.sleep(6000);
            }


            for (MatchResponse match : matchesToSave) {
                Map<String, Integer> standingOfMatchday = standingsMap.get(match.getMatchday());
                if (standingOfMatchday != null) {
                    match.setHomeRankAfterMatchday(standingOfMatchday.get(match.getHomeTeam().getName()));
                    match.setAwayRankAfterMatchday(standingOfMatchday.get(match.getAwayTeam().getName()));
                }
            }

            databaseManager.createTable();
            databaseManager.insertMatches(matchesToSave);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
