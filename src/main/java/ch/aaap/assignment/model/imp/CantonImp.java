package ch.aaap.assignment.model.imp;

import ch.aaap.assignment.model.Canton;
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
public class CantonImp implements Canton {

  private String code;
  private String name;

}
