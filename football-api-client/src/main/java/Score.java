import com.google.gson.annotations.SerializedName;

public class Score {

    @SerializedName("fullTime")
    private FullTime fullTime;

    public FullTime getFullTime() {
        return fullTime;
    }
}
