package org.ulpgc.dacd.control.feeder;

import java.io.IOException;

public interface FootballMatchFeeder {

    String getAllMatches() throws IOException, InterruptedException;
    String getStandingsByMatchday(int matchday) throws IOException, InterruptedException;
}
