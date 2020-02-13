package ch.aaap.assignment;

import ch.aaap.assignment.model.Canton;
import ch.aaap.assignment.model.District;
import ch.aaap.assignment.model.Model;
import ch.aaap.assignment.model.PoliticalCommunity;
import ch.aaap.assignment.model.PostalCommunity;
import ch.aaap.assignment.model.imp.CantonImp;
import ch.aaap.assignment.model.imp.DistrictImp;
import ch.aaap.assignment.model.imp.ModelImp;
import ch.aaap.assignment.model.imp.PoliticalCommunityImp;
import ch.aaap.assignment.model.imp.PostalCommunityImp;
import ch.aaap.assignment.raw.CSVPoliticalCommunity;
import ch.aaap.assignment.raw.CSVPostalCommunity;
import ch.aaap.assignment.raw.CSVUtil;
import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

    ModelImp model = new ModelImp();
    model.setCantons(createCantons(csvPoliticalCommunities));
    model.setDistricts(createDistricts(csvPoliticalCommunities));
    Set<PostalCommunity> postalCommunities = createPostalCommunities(csvPostalCommunities);
    model.setPostalCommunities(postalCommunities);
    model.setPoliticalCommunities(
        createPoliticalCommunities(csvPoliticalCommunities, postalCommunities));
    this.model = model;
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
    return Optional.ofNullable(this.model)
        .map(Model::getCantons)
        .map(Collection::stream)
        .map(cantonStream -> cantonStream.filter(canton -> canton.getCode().equals(cantonCode)))
        .flatMap(Stream::findAny)
        .map(Canton::getPoliticalCommunityIds)
        .map(Set::size)
        .orElseThrow(() -> new IllegalArgumentException(
            String.format("Can't find canton with code %s", cantonCode)));
  }

  /**
   * Returns number of districts in canton.
   *
   * @param cantonCode code of a canton (e.g. ZH)
   * @return amount of districts in given canton
   */
  public long getAmountOfDistrictsInCanton(String cantonCode) {

    return Optional.ofNullable(model)
        .map(Model::getCantons)
        .map(Collection::stream)
        .flatMap(cantonStream -> cantonStream
            .filter(canton -> cantonCode.equals(canton.getCode()))
            .findAny())
        .map(Canton::getDistrictIds)
        .map(Set::size)
        .orElseThrow(() -> new IllegalArgumentException(
            String.format("Can't find canton with code %s", cantonCode)));
  }

  /**
   * Returns number of political communities in district.
   *
   * @param districtNumber number of a district (e.g. 101)
   * @return amount of districts in given canton
   */
  public long getAmountOfPoliticalCommunitiesInDistrict(String districtNumber) {
    return Optional.ofNullable(this.model)
        .map(Model::getDistricts)
        .map(Collection::stream)
        .map(districtStream -> districtStream
            .filter(district -> district.getNumber().equals(districtNumber)))
        .flatMap(Stream::findAny)
        .map(District::getPoliticalCommunityIds)
        .map(Set::size)
        .orElseThrow(() -> new IllegalArgumentException(
            String.format("Can't find district with number %s", districtNumber)));
  }

  /**
   * Returns districts for zip code.
   *
   * @param zipCode code 4 digit zip code
   * @return district that belongs to specified zip code
   */
  public String getDistrictForZipCode(String zipCode) {

    String politicalCommunityNumber = Optional.ofNullable(this.model)
        .map(Model::getPostalCommunities)
        .map(Collection::stream)
        .flatMap(postalCommunityStream -> postalCommunityStream
            .filter(postalCommunity -> zipCode.equals(postalCommunity.getZipCode())).findAny())
        .map(PostalCommunity::getPoliticalCommunityNumber)
        .orElseThrow(() -> new IllegalArgumentException(
            String.format("Can't find district with zip code %s", zipCode)));

    return Optional.ofNullable(this.model)
        .map(Model::getDistricts)
        .map(Collection::stream)
        .flatMap(districtStream -> districtStream.filter(
            district -> district.getPoliticalCommunityIds().contains(politicalCommunityNumber))
            .findAny())
        .map(District::getName)
        .orElseThrow(() -> new IllegalArgumentException(String
            .format("Can't find district with politicalCommunityNumber %s",
                politicalCommunityNumber)));
  }

  /**
   * Returns the date of the last update of the political community, given a postal community name.
   *
   * @param postalCommunityName community name
   * @return lastUpdate of the political community by a given postal community name
   */
  public LocalDate getLastUpdateOfPoliticalCommunityByPostalCommunityName(
      String postalCommunityName) {
    String politicalCommunityNumber = Optional.ofNullable(this.model)
        .map(Model::getPostalCommunities)
        .map(Collection::stream)
        .map(postalCommunityStream -> postalCommunityStream
            .filter(postalCommunity -> postalCommunityName.equals(postalCommunity.getName())))
        .flatMap(Stream::findAny)
        .map(PostalCommunity::getPoliticalCommunityNumber)
        .orElseThrow(() -> new IllegalArgumentException(
            String.format("Can't find Postal Community with name %s", postalCommunityName)));

    return Optional.ofNullable(this.model)
        .map(Model::getPoliticalCommunities)
        .map(Collection::stream)
        .map(politicalCommunityStream -> politicalCommunityStream.filter(
            politicalCommunity -> politicalCommunityNumber.equals(politicalCommunity.getNumber())))
        .flatMap(Stream::findAny)
        .map(PoliticalCommunity::getLastUpdate)
        .orElseThrow(() -> new IllegalArgumentException("can't fine political community"));

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
    return this.model.getPoliticalCommunities()
        .stream()
        .filter(politicalCommunity -> politicalCommunity.getPostalCommunities() == null
            || politicalCommunity.getPostalCommunities().isEmpty())
        .count();
  }

  private static Set<Canton> createCantons(Set<CSVPoliticalCommunity> csvPoliticalCommunities) {
    // O(n)
    Map<String, List<CSVPoliticalCommunity>> communitiesByCantonCode = csvPoliticalCommunities
        .stream()
        .collect(Collectors.groupingBy(CSVPoliticalCommunity::getCantonCode));

    // O(n)
    Set<CantonImp> cantons = csvPoliticalCommunities.stream()
        .map(csvPoliticalCommunity -> CantonImp.builder()
            .name(csvPoliticalCommunity.getCantonName())
            .code(csvPoliticalCommunity.getCantonCode())
            .build())
        .collect(Collectors.toSet());

    // O(n)
    cantons.forEach(canton -> {

      canton.setPoliticalCommunityIds(
          communitiesByCantonCode.get(canton.getCode()) // O(1)
          .stream()
          .map(CSVPoliticalCommunity::getNumber)
          .collect(Collectors.toSet()));
      canton.setDistrictIds(communitiesByCantonCode
          .get(canton.getCode())
          .stream()
          .map(CSVPoliticalCommunity::getDistrictNumber)
          .collect(Collectors.toSet()));
    });
    /*
    // Alternative implementation, same complexity. Used in createDistricts
    // O(n)
    Map<String, List<CSVPoliticalCommunity>> communitiesByCantonCode = csvPoliticalCommunities
        .stream()
        .collect(Collectors.groupingBy(CSVPoliticalCommunity::getCantonCode));
    Map<String, CantonImp> cantonsByCode = new HashMap<>();

    // O(n)
    communitiesByCantonCode.forEach((cantonCode, politicalCommunitiesOfCanton) -> {
      // O(1)
      CantonImp cantonImp = cantonsByCode.computeIfAbsent(cantonCode,
          k -> CantonImp.builder().code(cantonCode)
              .name(politicalCommunitiesOfCanton.iterator().next().getCantonName())
              .build()
      );
      cantonImp.setPoliticalCommunityIds(politicalCommunitiesOfCanton.stream()
          .map(CSVPoliticalCommunity::getNumber)
          .collect(Collectors.toSet())
      );

      cantonImp.setDistrictIds(politicalCommunitiesOfCanton.stream()
          .map(CSVPoliticalCommunity::getDistrictNumber)
          .collect(Collectors.toSet())
      );
    });
    return new HashSet<>(cantonsByCode.values());
     */
    return cantons.stream().map(c -> (Canton) c).collect(Collectors.toSet());
  }

  private static Set<District> createDistricts(Set<CSVPoliticalCommunity> csvPoliticalCommunities) {

    Map<String, List<CSVPoliticalCommunity>> communitiesByDistrictNumber = csvPoliticalCommunities
        .stream()
        .collect(Collectors.groupingBy(CSVPoliticalCommunity::getDistrictNumber));
    Map<String, DistrictImp> districtsByNumber = new HashMap<>();

    communitiesByDistrictNumber.forEach((districtNumber, politicalCommunitiesOfDistrict) -> {
      DistrictImp districtImp = districtsByNumber.computeIfAbsent(districtNumber,
          k -> DistrictImp.builder()
              .number(districtNumber)
              .name(politicalCommunitiesOfDistrict.iterator().next().getDistrictName())
              .build()
      );
      districtImp.setPoliticalCommunityIds(politicalCommunitiesOfDistrict.stream()
          .map(CSVPoliticalCommunity::getNumber)
          .collect(Collectors.toSet())
      );
    });

    return new HashSet<>(districtsByNumber.values());
  }

  private static Set<PoliticalCommunity> createPoliticalCommunities(
      Set<CSVPoliticalCommunity> csvPoliticalCommunities,
      Set<PostalCommunity> postalCommunities) {

    Map<String, List<PostalCommunity>> postalCommunitiesByPoliticalCommunityNumber =
        postalCommunities
            .stream()
            .collect(Collectors.groupingBy(PostalCommunity::getPoliticalCommunityNumber));

    return csvPoliticalCommunities.stream()
        .map(csvPoliticalCommunity ->
            PoliticalCommunityImp.builder()
                .name(csvPoliticalCommunity.getName())
                .number(csvPoliticalCommunity.getNumber())
                .shortName(csvPoliticalCommunity.getShortName())
                .lastUpdate(csvPoliticalCommunity.getLastUpdate())
                .postalCommunities(postalCommunitiesByPoliticalCommunityNumber
                    .get(csvPoliticalCommunity.getNumber()))
                .build()
        )
        .collect(Collectors.toSet());
  }

  private static Set<PostalCommunity> createPostalCommunities(
      Set<CSVPostalCommunity> csvPostalCommunities) {
    return csvPostalCommunities.stream()
        .map(csvPostalCommunity -> PostalCommunityImp
            .builder()
            .zipCode(csvPostalCommunity.getZipCode())
            .zipCodeAddition(csvPostalCommunity.getZipCodeAddition())
            .name(csvPostalCommunity.getName())
            .politicalCommunityNumber(csvPostalCommunity.getPoliticalCommunityNumber())
            .build()
        )
        .collect(Collectors.toSet());
  }
}
