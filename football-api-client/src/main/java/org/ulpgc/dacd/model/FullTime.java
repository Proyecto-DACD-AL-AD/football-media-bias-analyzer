package org.ulpgc.dacd.model;

import com.google.gson.annotations.SerializedName;

public class FullTime {

    @SerializedName("home")
    private int homeGoals;

    @SerializedName("away")
    private int awayGoals;

    public int getHomeGoals() {
        return homeGoals;
    }

    public int getAwayGoals() {
        return awayGoals;
    }
}
