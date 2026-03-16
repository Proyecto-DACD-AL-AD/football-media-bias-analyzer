import com.google.gson.annotations.SerializedName;

public class Team {

    @SerializedName("name")
    private String name;


    public String getName() {
        return name;
    }
}
