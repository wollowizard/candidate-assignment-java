package ch.aaap.assignment.model.imp;

import ch.aaap.assignment.model.Canton;
import ch.aaap.assignment.model.District;
import ch.aaap.assignment.model.Model;
import ch.aaap.assignment.model.PoliticalCommunity;
import ch.aaap.assignment.model.PostalCommunity;
import ch.aaap.assignment.raw.CSVPoliticalCommunity;
import ch.aaap.assignment.raw.CSVPostalCommunity;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ModelImp implements Model {

  private Map<String, CSVPoliticalCommunity> csvPoliticalCommunitiesByNumber;
  private Collection<CSVPostalCommunity> csvPostalCommunities;

  /**
   * Ctor.
   */
  public ModelImp(Set<CSVPoliticalCommunity> csvPoliticalCommunities,
      Set<CSVPostalCommunity> csvPostalCommunities) {
    csvPoliticalCommunitiesByNumber = csvPoliticalCommunities.stream()
        .collect(Collectors.toMap(CSVPoliticalCommunity::getNumber, Function.identity()));
    this.csvPostalCommunities = csvPostalCommunities;
  }

  @Override
  public Set<PostalCommunity> getPostalCommunities() {
    return csvPostalCommunities.stream()
        .map(this::postalCommunityFromCsvPostalCommunity)
        .collect(Collectors.toSet());
  }

  @Override
  public Set<PoliticalCommunity> getPoliticalCommunities() {
    return csvPoliticalCommunitiesByNumber.values().stream()
        .map(this::politicalCommunity)
        .collect(Collectors.toSet());
  }

  @Override
  public Set<Canton> getCantons() {
    return csvPoliticalCommunitiesByNumber.values().stream()
        .map(this::cantonFromCsvPoliticalCommunity)
        .collect(Collectors.toSet());
  }

  @Override
  public Set<District> getDistricts() {
    return csvPoliticalCommunitiesByNumber.values().stream()
        .map(this::districtFromPoliticalCommunities)
        .collect(Collectors.toSet());
  }

  @Override
  public Map<String, Set<PoliticalCommunity>> getPoliticalCommunitiesByCanton() {
    return csvPoliticalCommunitiesByNumber.values().stream()
        .collect(Collectors.groupingBy(CSVPoliticalCommunity::getCantonCode,
            Collectors.mapping(this::politicalCommunity, Collectors.toSet())));
  }

  @Override
  public Map<String, Set<District>> getDistrictsByCanton() {
    return csvPoliticalCommunitiesByNumber.values().stream()
        .collect(Collectors.groupingBy(CSVPoliticalCommunity::getCantonCode,
            Collectors.mapping(this::districtFromPoliticalCommunities, Collectors.toSet())));
  }

  @Override
  public Map<String, Set<PoliticalCommunity>> getPoliticalCommunitiesByDistrict() {
    return csvPoliticalCommunitiesByNumber.values().stream()
        .collect(Collectors.groupingBy(CSVPoliticalCommunity::getDistrictNumber,
            Collectors.mapping(this::politicalCommunity, Collectors.toSet())));
  }

  /**
   * There is 1:n correspondence between zip code and postal community number. For example PLZ4 1008
   * corresponds to GDENR 5589 (Prilly), GDENR 5591	(Renens) and GDENR 5585	(Jouxtens-Mézery). Even
   * adding PLZZ to PLZ4 doesn't help. Here we are just returning one element (we are sure there is
   * one or there would be an IllegalArgumentException).
   */
  @Override
  public String getDistrictByZipCode(String zipCode) {
    return this.getAllDistrictsByZipCode(zipCode)
        .iterator().next();
  }

  /**
   * Please see added test ch.aaap.assignment.ApplicationTest#returnsCorrectDistrictsNameForZipCode()
   */
  @Override
  public Set<String> getAllDistrictsByZipCode(String zipCode) {
    Set<CSVPostalCommunity> postalCommunitiesOfZip =
        csvPostalCommunities.stream()
            .filter(csvPostalCommunity -> zipCode.equals(csvPostalCommunity.getZipCode()))
            .collect(Collectors.toSet());
    if (postalCommunitiesOfZip.isEmpty()) {
      throw new IllegalArgumentException(
          String.format("Can't find postalCommunities for zip code %s", zipCode));
    }
    Set<String> districts = postalCommunitiesOfZip.stream()
        .map(csvPostalCommunity -> this.csvPoliticalCommunitiesByNumber
            .get(csvPostalCommunity.getPoliticalCommunityNumber()))// political communities of zip
        .map(CSVPoliticalCommunity::getDistrictName) // district names of zip
        .collect(Collectors.toSet());

    if (districts.isEmpty()) {
      throw new IllegalArgumentException(
          String.format("Can't find any district name for zip code %s", zipCode));
    }
    return districts;
  }

  @Override
  public LocalDate getLastUpdateByPostalCommunityName(String postalCommunityName) {
    // a postal community name can correspond to multiple political community numbers
    // for example Zurich corresponds to 261	Zürich, 191	Dübendorf, 66	Opfikon and 97	Rümlang
    Set<String> politicalCommunityNumbersOfPostalCommunityName =
        csvPostalCommunities.stream()
            .filter(csvPostalCommunity -> postalCommunityName.equals(csvPostalCommunity.getName()))
            .map(CSVPostalCommunity::getPoliticalCommunityNumber)
            .collect(Collectors.toSet());
    if (politicalCommunityNumbersOfPostalCommunityName.isEmpty()) {
      throw new IllegalArgumentException(
          String.format("Can't find postalCommunities for name %s", postalCommunityName));
    }

    // Since we found multiple political communities, we will select one with the most recent update.
    // see max
    return politicalCommunityNumbersOfPostalCommunityName.stream()
        .map(csvPoliticalCommunitiesByNumber::get)
        .map(CSVPoliticalCommunity::getLastUpdate)
        .max(LocalDate::compareTo)
        .orElseThrow(() -> new IllegalArgumentException(String
            .format("Can't find political community for postal community name %s",
                postalCommunityName)));

  }

  @Override
  public Set<PoliticalCommunity> getPoliticalCommunitiesWithoutPostalCommunity() {
    Set<String> politicalCommunityNumbersOfPostalCommunities = csvPostalCommunities.stream()
        .map(CSVPostalCommunity::getPoliticalCommunityNumber)
        .collect(Collectors.toSet());

    return csvPoliticalCommunitiesByNumber.values().stream()
        .filter(postalCommunity ->
            !politicalCommunityNumbersOfPostalCommunities.contains(postalCommunity.getNumber()))
        .map(this::politicalCommunity)
        .collect(Collectors.toSet());
  }


  private District districtFromPoliticalCommunities(CSVPoliticalCommunity csvPoliticalCommunity) {
    return DistrictImp.builder()
        .name(csvPoliticalCommunity.getDistrictName())
        .number(csvPoliticalCommunity.getDistrictNumber())
        .build();
  }

  private Canton cantonFromCsvPoliticalCommunity(CSVPoliticalCommunity csvPoliticalCommunity) {
    return CantonImp.builder()
        .code(csvPoliticalCommunity.getCantonCode())
        .name(csvPoliticalCommunity.getCantonName())
        .build();
  }

  private PostalCommunity postalCommunityFromCsvPostalCommunity(
      CSVPostalCommunity csvPostalCommunity) {
    return PostalCommunityImp.builder()
        .name(csvPostalCommunity.getName())
        .zipCode(csvPostalCommunity.getZipCode())
        .zipCodeAddition(csvPostalCommunity.getZipCodeAddition())
        .politicalCommunityNumber(csvPostalCommunity.getPoliticalCommunityNumber())
        .build();
  }

  private PoliticalCommunity politicalCommunity(CSVPoliticalCommunity csvPoliticalCommunity) {
    return PoliticalCommunityImp.builder()
        .number(csvPoliticalCommunity.getNumber())
        .name(csvPoliticalCommunity.getShortName())
        .shortName(csvPoliticalCommunity.getShortName())
        .lastUpdate(csvPoliticalCommunity.getLastUpdate())
        .build();
  }
}
