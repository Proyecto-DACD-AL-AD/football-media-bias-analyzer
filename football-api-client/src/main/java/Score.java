import com.google.gson.annotations.SerializedName;

public class Score {

    @SerializedName("fullTime")
    private FullTime score;

    public FullTime getScore() {
        return score;
    }
}
