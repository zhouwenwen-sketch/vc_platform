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
public class ProjectImportResultVO {

  private int projectsInserted;
  private int projectsUpdated;
  private int portraitsUpdated;
  private int financingInserted;
  private int financingUpdated;
  private int membersInserted;
  private int membersUpdated;
  private int membersSkipped;
  private int dynamicsInserted;
  private int dynamicsSkipped;
  private int imagesSaved;

  @Builder.Default private List<String> warnings = new ArrayList<>();
}
