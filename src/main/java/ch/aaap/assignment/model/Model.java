package ch.aaap.assignment.model;

import java.time.LocalDate;
import java.util.Map;
import java.util.Set;

public interface Model {

  Set<PoliticalCommunity> getPoliticalCommunities();

  Set<PostalCommunity> getPostalCommunities();

  Set<Canton> getCantons();

  Set<District> getDistricts();

  Map<String, Set<PoliticalCommunity>> getPoliticalCommunitiesByCanton();

  Map<String, Set<District>> getDistrictsByCanton();

  Map<String, Set<PoliticalCommunity>> getPoliticalCommunitiesByDistrict();

  String getDistrictByZipCode(String zipCode);

  LocalDate getLastUpdateByPostalCommunityName(String postalCommunityName);

  Set<PoliticalCommunity> getPoliticalCommunitiesWithoutPostalCommunity();

  Set<String> getAllDistrictsByZipCode(String zipCode);
}
