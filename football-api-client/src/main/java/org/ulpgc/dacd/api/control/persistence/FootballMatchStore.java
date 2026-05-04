package org.ulpgc.dacd.api.control.persistence;

import org.ulpgc.dacd.api.model.Match;

import java.util.List;

public interface FootballMatchStore {

     void store(List<Match> matches);
}
