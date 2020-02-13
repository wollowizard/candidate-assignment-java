package ch.aaap.assignment.model.imp;

import ch.aaap.assignment.model.PostalCommunity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
public class PostalCommunityImp implements PostalCommunity {

  private String zipCode;
  private String zipCodeAddition;
  private String name;
  private String politicalCommunityNumber;

}
