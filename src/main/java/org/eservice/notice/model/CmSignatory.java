package org.eservice.notice.model;

import java.sql.*;
import javax.persistence.*;
import lombok.Data;

@Data
@Entity(name = "org.eservice.notice.model.CmSignatory")
@Table(name = "CM_Signatory")
public class CmSignatory {

  @Id
  @Column(name = "\"signatory_id\"", nullable = false)
  private String signatoryId;
  @Column(name = "\"signatory_title\"", nullable = true)
  private String signatoryTitle;
  @Column(name = "\"signoff_text\"", nullable = true)
  private String signoffText;
  @Column(name = "\"designation_line1\"", nullable = false)
  private String designationLine1;
  @Column(name = "\"designation_line2\"", nullable = false)
  private String designationLine2;
  @Column(name = "\"designation_line3\"", nullable = false)
  private String designationLine3;
  @Column(name = "\"revision_no\"", nullable = false)
  private Integer revisionNo;
  @Column(name = "\"created_by\"", nullable = false)
  private String createdBy;
  @Column(name = "\"created_date\"", nullable = true)
  private Timestamp createdDate;
  @Column(name = "\"updated_by\"", nullable = false)
  private String updatedBy;
  @Column(name = "\"updated_date\"", nullable = true)
  private Timestamp updatedDate;
  @Column(name = "\"version_no\"", nullable = false)
  private Integer versionNo;
}