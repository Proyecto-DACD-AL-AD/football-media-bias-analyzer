package org.ulpgc.dacd.persistence;

import org.ulpgc.dacd.model.MatchResponse;

import java.util.List;

public interface FootballMatchSerializer {

     void createTable();
     void insertMatches(List<MatchResponse> matches);
}
