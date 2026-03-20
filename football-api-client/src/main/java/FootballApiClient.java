import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;


public class FootballApiClient {

    private static final String API_TOKEN = "b16a3ee0850b41b7b3bc5ee95b7061dc";
    private static final String API_URL = "https://api.football-data.org/v4/competitions/2014/matches?season=2025";
    HttpClient client = HttpClient.newHttpClient();


    public String getAllMatches() throws IOException, InterruptedException {
        HttpRequest request = getHttpRequest();
        HttpResponse<String> response = getHttpResponse(request);


        checkStatusCodeError(response);

        return response.body();
    }


    private static void checkStatusCodeError(HttpResponse<String> response) {
        if (response.statusCode() != 200) {
            throw new RuntimeException("Error al contactar con la API, código: " + response.statusCode());
        }
    }


    private HttpResponse<String> getHttpResponse(HttpRequest request)
            throws IOException, InterruptedException {

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

