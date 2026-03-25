import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.Map;

public class FootballMatchFilterTest {

    private FootballMatchFilter matchFilter;

    @Before
    public void setUp(){
         matchFilter = new FootballMatchFilter();
    }

    @Test
    public void validJsonShouldReturnCorrectMap() {

        String mockJson = "{" +
                "\"standings\": [{" +
                "\"type\": \"TOTAL\"," +
                "\"table\": [" +
                "{\"position\": 1, \"team\": {\"name\": \"Real Madrid\"}}," +
                "{\"position\": 2, \"team\": {\"name\": \"FC Barcelona\"}}" +
                "]" +
                "}]" +
                "}";

        Map<String, Integer> result = matchFilter.parseStandings(mockJson);
        Assert.assertNotNull("El mapa resultante no debería ser nulo", result);
        Assert.assertEquals("Debería haber 2 equipos en el mapa", 2, result.size());
        Assert.assertEquals("Real Madrid posición incorrecta", Integer.valueOf(1), result.get("Real Madrid"));
        Assert.assertEquals("FC Barcelona posición incorrecta", Integer.valueOf(2), result.get("FC Barcelona"));
    }

    @Test
    public void emptyJsonShouldReturnEmptyMap() {

        String emptyJson = "{}";
        Map<String, Integer> result = matchFilter.parseStandings(emptyJson);
        Assert.assertNotNull("El mapa resultante debe estar vacío, no ser nulo", result);
        Assert.assertTrue(result.isEmpty());
    }

    @Test
    public void validMatchesJsonShouldReturnCorrectMatchInfo() {

        String mockMatchesJson = "{" +
                "\"matches\": [" +
                "  {" +
                "    \"matchday\": 1," +
                "    \"status\": \"FINISHED\"," +
                "    \"homeTeam\": {\"name\": \"Real Madrid\"}," +
                "    \"awayTeam\": {\"name\": \"FC Barcelona\"}," +
                "    \"score\": {" +
                "      \"fullTime\": {\"homeGoals\": 2, \"awayGoals\": 1}" +
                "    }" +
                "  }," +
                "  {" +
                "    \"matchday\": 1," +
                "    \"status\": \"SCHEDULED\"," +
                "    \"homeTeam\": {\"name\": \"Sevilla FC\"}," +
                "    \"awayTeam\": {\"name\": \"Real Betis\"}," +
                "    \"score\": {" +
                "      \"fullTime\": {\"homeGoals\": null, \"awayGoals\": null}" +
                "    }" +
                "  }" +
                "]" +
                "}";

        List<MatchResponse> result = matchFilter.filterMatches(mockMatchesJson);

        Assert.assertNotNull("La lista de partidos no debe ser nula", result);
        Assert.assertEquals("El equipo local del primer partido es incorrecto", "Real Madrid", result.get(0).getHomeTeam().getName());
        Assert.assertEquals("El estado del primer partido es incorrecto", "FINISHED", result.get(0).getStatus());
        Assert.assertEquals("La jornada del primer partido es incorrecta", 1, result.get(0).getMatchday());

    }

    @Test
    public void scheduledMatchShouldNotAppearInFilteredMatches() {

        String mockMatchesJson = "{" +
                "\"matches\": [" +
                "  {" +
                "    \"matchday\": 1," +
                "    \"status\": \"SCHEDULED\"," +
                "    \"homeTeam\": {\"name\": \"Sevilla FC\"}," +
                "    \"awayTeam\": {\"name\": \"Real Betis\"}," +
                "    \"score\": {" +
                "      \"fullTime\": {\"homeGoals\": null, \"awayGoals\": null}" +
                "    }" +
                "  }" +
                "]" +
                "}";

        List<MatchResponse> result = matchFilter.filterMatches(mockMatchesJson);
        Assert.assertEquals("Debería haber 0 partidos en la lista", 0, result.size());

    }

    @Test
    public void emptyMatchesJsonShouldReturnEmptyList() {
        String emptyJson = "{}";

        java.util.List<MatchResponse> result = matchFilter.filterMatches(emptyJson);

        Assert.assertNotNull("La lista no debe ser nula ni siquiera con JSON vacío", result);
        Assert.assertTrue("La lista debería estar vacía si el JSON no tiene partidos", result.isEmpty());
    }
}
