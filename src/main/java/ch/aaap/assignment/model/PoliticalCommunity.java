package ch.aaap.assignment.model;

import java.time.LocalDate;
import java.util.List;

public interface PoliticalCommunity {

  String getNumber();

  String getName();

  String getShortName();

  LocalDate getLastUpdate();

  List<PostalCommunity> getPostalCommunities();
}
