package ch.aaap.assignment;

import ch.aaap.assignment.model.Model;
import ch.aaap.assignment.model.imp.ModelImp;
import ch.aaap.assignment.raw.CSVPoliticalCommunity;
import ch.aaap.assignment.raw.CSVPostalCommunity;
import ch.aaap.assignment.raw.CSVUtil;
import java.time.LocalDate;
import java.util.Map.Entry;
import java.util.Set;

public class Application {

  private Model model = null;

  public Application() {
    initModel();
  }

  public static void main(String[] args) {
    new Application();
  }

  /**
   * Reads the CSVs and initializes a in memory model.
   */
  private void initModel() {
    Set<CSVPoliticalCommunity> csvPoliticalCommunities = CSVUtil.getPoliticalCommunities();
    Set<CSVPostalCommunity> csvPostalCommunities = CSVUtil.getPostalCommunities();
    this.model = new ModelImp(csvPoliticalCommunities, csvPostalCommunities);
  }

  /**
   * Return Model.
   *
   * @return model
   */
  public Model getModel() {
    return model;
  }

  /**
   * Returns number of political communities in canton.
   *
   * @param cantonCode code of a canton (e.g. ZH)
   * @return amount of political communities in given canton
   */
  public long getAmountOfPoliticalCommunitiesInCanton(String cantonCode) {
    return model.getPoliticalCommunitiesByCanton()
        .entrySet()
        .stream()
        .filter(entry -> entry.getKey().equals(cantonCode))
        .findAny()
        .map(Entry::getValue)
        .map(Set::size)
        .orElseThrow(
            () -> new IllegalArgumentException(String.format("Can't find canton %s", cantonCode)));
  }

  /**
   * Returns number of districts in canton.
   *
   * @param cantonCode code of a canton (e.g. ZH)
   * @return amount of districts in given canton
   */
  public long getAmountOfDistrictsInCanton(String cantonCode) {
    return model.getDistrictsByCanton()
        .entrySet()
        .stream()
        .filter(entry -> entry.getKey().equals(cantonCode))
        .findAny()
        .map(Entry::getValue)
        .map(Set::size)
        .orElseThrow(
            () -> new IllegalArgumentException(String.format("Can't find canton %s", cantonCode)));
  }

  /**
   * Returns number of political communities in district.
   *
   * @param districtNumber number of a district (e.g. 101)
   * @return amount of districts in given canton
   */
  public long getAmountOfPoliticalCommunitiesInDistrict(String districtNumber) {
    return model.getPoliticalCommunitiesByDistrict()
        .entrySet()
        .stream()
        .filter(entry -> entry.getKey().equals(districtNumber))
        .findAny()
        .map(Entry::getValue)
        .map(Set::size)
        .orElseThrow(
            () -> new IllegalArgumentException(
                String.format("Can't find district %s", districtNumber)));
  }

  /**
   * Returns district for zip code.
   *
   * @param zipCode code 4 digit zip code
   * @return district that belongs to specified zip code
   */
  public String getDistrictForZipCode(String zipCode) {
    return model.getDistrictByZipCode(zipCode);
  }

  /**
   * Returns districts for zip code.
   *
   * @param zipCode code 4 digit zip code
   * @return districts that belongs to specified zip code
   */
  public Set<String> getAllDistrictsForZipCode(String zipCode) {
    return model.getAllDistrictsByZipCode(zipCode);
  }

  /**
   * Returns the date of the last update of the political community, given a postal community name.
   *
   * @param postalCommunityName community name
   * @return lastUpdate of the political community by a given postal community name
   */
  public LocalDate getLastUpdateOfPoliticalCommunityByPostalCommunityName(
      String postalCommunityName) {
    return model.getLastUpdateByPostalCommunityName(postalCommunityName);
  }

  /**
   * https://de.wikipedia.org/wiki/Kanton_(Schweiz)
   *
   * @return amount of canton
   */
  public long getAmountOfCantons() {
    return this.model.getCantons().size();
  }

  /**
   * https://de.wikipedia.org/wiki/Kommunanz
   *
   * @return amount of political communities without postal communities
   */
  public long getAmountOfPoliticalCommunityWithoutPostalCommunities() {
    return model.getPoliticalCommunitiesWithoutPostalCommunity().size();
  }
}
