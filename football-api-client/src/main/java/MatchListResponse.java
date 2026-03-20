import com.google.gson.annotations.SerializedName;
import java.util.List;

public class MatchListResponse {

    @SerializedName("matches")
    private List<MatchResponse> matches;

    public List<MatchResponse> getMatches() {
        return matches;
    }
}
