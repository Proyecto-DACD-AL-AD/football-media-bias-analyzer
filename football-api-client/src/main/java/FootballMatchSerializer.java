import java.util.List;

public interface FootballMatchSerializer {

     void createTable();
     void insertMatches(List<MatchResponse> matches);
}
