import com.google.gson.Gson;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

public class FootballApiClient {

    private static final String API_TOKEN = "b16a3ee0850b41b7b3bc5ee95b7061dc";
    private static final String API_URL = "https://api.football-data.org/v4/competitions/2014/matches?season=2025";

    public static void main(String[] args) {

        HttpClient client = HttpClient.newHttpClient();
        FootballApiClient  footballApiClient = new FootballApiClient();
        HttpRequest request = footballApiClient.getHttpRequest();

        try {

            HttpResponse<String> response = footballApiClient.getStringHttpResponse(client, request);
            if (response.statusCode() != 200) {
                System.out.println("Error al contactar con la API, código: " + response.statusCode());
                return;
            }

            footballApiClient.filterMatches(response);

        } catch (Exception e) {

            System.out.println("Error de conexión: " + e.getMessage());
        }
    }

    private void filterMatches(HttpResponse<String> response) {

        String rawJson = response.body();
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

    private HttpResponse<String> getStringHttpResponse(HttpClient client, HttpRequest request) throws IOException,
            InterruptedException {
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        System.out.println("Código de estado: " + response.statusCode());
        return response;
    }

    private HttpRequest getHttpRequest() {
        return HttpRequest.newBuilder()
                .uri(URI.create(API_URL))
                .header("X-Auth-Token", API_TOKEN)
                .build();
    }
}

