import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;


public class ApiFootballMatchFeeder implements FootballMatchFeeder {

    private static final String API_TOKEN = System.getenv("API_TOKEN");
    private static final String BASE_URL = "https://api.football-data.org/v4/competitions/2014/";
    private final HttpClient client = HttpClient.newHttpClient();


    public String getAllMatches() throws IOException, InterruptedException {
        String matchesUrl = BASE_URL + "matches?season=2025";
        return makeApiCall(matchesUrl);
    }


    public String getStandingsByMatchday (int matchday) throws IOException, InterruptedException {
        String standingsUrl = BASE_URL + "standings?season=2025&matchday=" + matchday;
        return makeApiCall(standingsUrl);
    }

    private String makeApiCall(String url) throws IOException, InterruptedException {

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("X-Auth-Token", API_TOKEN)
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        System.out.println("Llamando a: " + url);
        System.out.println("Código de estado: " + response.statusCode());

        checkStatusCodeError(response);

        return response.body();
    }

    private static void checkStatusCodeError(HttpResponse<String> response) {
        if (response.statusCode() != 200) {
            throw new RuntimeException("Error al contactar con la API, código: " + response.statusCode());
        }
    }
}

