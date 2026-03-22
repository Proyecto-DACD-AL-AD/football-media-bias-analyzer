import com.google.gson.annotations.SerializedName;

public class MatchResponse {

    @SerializedName("utcDate")
    private String date;

    @SerializedName("status")
    private String status;

    @SerializedName("homeTeam")
    private Team homeTeam;

    @SerializedName("awayTeam")
    private Team awayTeam;

    @SerializedName("score")
    private Score score;

    @SerializedName("matchday")
    private int matchday;


    public String getStatus() {
        return status;
    }

    public String getDate() {
        return date;
    }

    public Team getHomeTeam() {
        return homeTeam;
    }

    public Team getAwayTeam() {
        return awayTeam;
    }

    public Score getScore() {
        return score;
    }

    public int getMatchday() {
        return matchday;
    }
}
