import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.ulpgc.dacd.api.control.filter.FootballMatchParser;
import org.ulpgc.dacd.api.model.Match;

import java.util.List;
import java.util.Map;

public class FootballMatchParserTest {
    private FootballMatchParser matchFilter;

    @Before
    public void setUp(){
         matchFilter = new FootballMatchParser();
    }

    @Test
    public void validJsonShouldReturnCorrectMap() {
        String mockJson = "{" +
                "\"standings\": [{" +
                "\"type\": \"TOTAL\"," +
                "\"table\": [" +
                "{\"position\": 1, \"team\": {\"name\": \"Real Madrid CF\"}}," +
                "{\"position\": 2, \"team\": {\"name\": \"FC Barcelona\"}}" +
                "]" +
                "}]" +
                "}";

        Map<String, Integer> result = matchFilter.parseStandings(mockJson);
        Assert.assertNotNull("El mapa resultante no debería ser nulo", result);
        Assert.assertEquals("Debería haber 2 equipos en el mapa", 2, result.size());
        Assert.assertEquals("Real Madrid posición incorrecta", Integer.valueOf(1), result.get("Real Madrid CF"));
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
                "    \"utcDate\": \"2024-05-15T19:00:00Z\"," +
                "    \"matchday\": 1," +
                "    \"status\": \"FINISHED\"," +
                "    \"homeTeam\": {\"name\": \"Real Madrid CF\"}," +
                "    \"awayTeam\": {\"name\": \"FC Barcelona\"}," +
                "    \"score\": {" +
                "      \"fullTime\": {\"home\": 2, \"away\": 1}" +
                "    }" +
                "  }," +
                "  {" +
                "    \"utcDate\": \"2024-05-16T19:00:00Z\"," +
                "    \"matchday\": 1," +
                "    \"status\": \"SCHEDULED\"," +
                "    \"homeTeam\": {\"name\": \"Sevilla FC\"}," +
                "    \"awayTeam\": {\"name\": \"Real Betis Balompié\"}," +
                "    \"score\": {" +
                "      \"fullTime\": {\"home\": null, \"away\": null}" +
                "    }" +
                "  }" +
                "]" +
                "}";

        List<Match> result = matchFilter.parseMatches(mockMatchesJson);
        Assert.assertNotNull("La lista de partidos no debe ser nula", result);
        Assert.assertEquals("Debería haber filtrado el partido no finalizado", 1, result.size());
        Assert.assertEquals("El equipo local del primer partido es incorrecto", "Real Madrid CF", result.getFirst().homeTeam());
        Assert.assertEquals("La jornada del primer partido es incorrecta", 1, result.getFirst().matchday());

    }

    @Test
    public void scheduledMatchShouldNotAppearInFilteredMatches() {
        String mockMatchesJson = "{" +
                "\"matches\": [" +
                "  {" +
                "    \"utcDate\": \"2024-05-16T19:00:00Z\"," +
                "    \"matchday\": 1," +
                "    \"status\": \"SCHEDULED\"," +
                "    \"homeTeam\": {\"name\": \"Sevilla FC\"}," +
                "    \"awayTeam\": {\"name\": \"Real Betis Balompié\"}," +
                "    \"score\": {" +
                "      \"fullTime\": {\"home\": null, \"away\": null}" +
                "    }" +
                "  }" +
                "]" +
                "}";

        List<Match> result = matchFilter.parseMatches(mockMatchesJson);
        Assert.assertEquals("Debería haber 0 partidos en la lista", 0, result.size());
    }

    @Test
    public void emptyMatchesJsonShouldReturnEmptyList() {
        String emptyJson = "{}";
        List<Match> result = matchFilter.parseMatches(emptyJson);
        Assert.assertNotNull("La lista no debe ser nula ni siquiera con JSON vacío", result);
        Assert.assertTrue("La lista debería estar vacía si el JSON no tiene partidos", result.isEmpty());
    }
}
