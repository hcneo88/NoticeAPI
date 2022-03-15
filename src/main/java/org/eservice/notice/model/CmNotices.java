package org.eservice.notice.model;

import java.sql.*;
import javax.persistence.*;
import lombok.Data;

@Data
@Entity(name = "org.eservice.notice.model.CmNotices")
@Table(name = "CM_Notices")
public class CmNotices {

  @Id
  @Column(name = "\"notice_id\"", nullable = false)
  private String noticeId;
  @Column(name = "\"notice_num\"", nullable = false)
  private String noticeNum;
  @Column(name = "\"notice_title\"", nullable = false)
  private String noticeTitle;
  @Column(name = "\"recipient_type_cd\"", nullable = true)
  private Integer recipientTypeCd;
  @Column(name = "\"print_type_cd\"", nullable = true)
  private String printTypeCd;
  @Column(name = "\"revision_no\"", nullable = false)
  private Integer revisionNo;
  @Column(name = "\"letterhead_id\"", nullable = true)
  private String letterheadId;
  @Column(name = "\"signatory_id\"", nullable = true)
  private String signatoryId;
  @Column(name = "\"eff_start_date\"", nullable = false)
  private Timestamp effStartDate;
  @Column(name = "\"eff_end_date\"", nullable = true)
  private Timestamp effEndDate;
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