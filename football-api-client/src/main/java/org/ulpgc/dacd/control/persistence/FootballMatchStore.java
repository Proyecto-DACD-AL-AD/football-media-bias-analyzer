package org.ulpgc.dacd.control.persistence;

import org.ulpgc.dacd.model.MatchResponse;

import java.util.List;

public interface FootballMatchStore {

     void createTable();
     void insertMatches(List<MatchResponse> matches);
}
