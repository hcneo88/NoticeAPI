package org.eservice.notice.model;

import java.sql.*;
import javax.persistence.*;
import lombok.Data;

@Data
@Entity(name = "org.eservice.notice.model.CmLetterhead")
@Table(name = "CM_LetterHead")
public class CmLetterhead {

  @Id
  @Column(name = "\"letterhead_id\"", nullable = false)
  private String letterheadId;
  @Column(name = "\"letterhead_title\"", nullable = true)
  private String letterheadTitle;
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