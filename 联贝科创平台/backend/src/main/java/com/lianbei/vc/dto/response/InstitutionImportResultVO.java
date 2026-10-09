package com.lianbei.vc.dto.response;

import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InstitutionImportResultVO {

  private int companiesInserted;
  private int companiesUpdated;
  private int membersInserted;
  private int membersUpdated;
  private int membersSkipped;
  private int imagesSaved;

  @Builder.Default private List<String> warnings = new ArrayList<>();
}
