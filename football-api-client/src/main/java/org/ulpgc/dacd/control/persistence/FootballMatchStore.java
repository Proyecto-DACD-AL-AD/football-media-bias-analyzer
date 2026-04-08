package org.ulpgc.dacd.control.persistence;

import org.ulpgc.dacd.model.Match;

import java.util.List;

public interface FootballMatchStore {

     void createTable();
     void insertMatches(List<Match> matches);
}
