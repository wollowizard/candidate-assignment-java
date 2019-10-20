package ch.aaap.assignment.model;

import java.util.Set;

public interface Model {

  public Set<PoliticalCommunity> getPoliticalCommunities();

  public Set<PostalCommunity> getPostalCommunities();

  public Set<Canton> getCantons();

  public Set<District> getDistricts();
}
