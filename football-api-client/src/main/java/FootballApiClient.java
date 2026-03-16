import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;


public class FootballApiClient {

    private static final String API_TOKEN = "b16a3ee0850b41b7b3bc5ee95b7061dc";
    private static final String API_URL = "https://api.football-data.org/v4/competitions/2014/matches?season=2025";

    public static void main(String[] args) {

        HttpClient client = HttpClient.newHttpClient();
        FootballApiClient  footballApiClient = new FootballApiClient();
        HttpRequest request = footballApiClient.getHttpRequest();

        try {
            HttpResponse<String> response = footballApiClient.getStringHttpResponse(client, request);
            footballApiClient.printJson(response);

        } catch (Exception e) {

            System.out.println("Error de conexión: " + e.getMessage());

        }




    }

    private void printJson(HttpResponse<String> response) {
        String jsonCrudo = response.body();
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        JsonElement jsonElement = JsonParser.parseString(jsonCrudo);
        String json = gson.toJson(jsonElement);

        System.out.println(json);
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
