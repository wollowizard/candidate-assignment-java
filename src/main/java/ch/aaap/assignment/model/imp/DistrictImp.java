package ch.aaap.assignment.model.imp;

import ch.aaap.assignment.model.District;
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
public class DistrictImp implements District {

  private String number;
  private String name;


}
