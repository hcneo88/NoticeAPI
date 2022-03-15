package org.eservice.notice.model;

import java.sql.*;
import javax.persistence.*;
import lombok.Data;

@Data
@Entity(name = "org.eservice.notice.model.CmNoticefields")
@Table(name = "CM_NoticeFields")
public class CmNoticefields {

  @Id
  @Column(name = "\"field_id\"", nullable = false)
  private String fieldId;
  @Column(name = "\"notice_id\"", nullable = false)
  private String noticeId;
  @Column(name = "\"field_name\"", nullable = false)
  private String fieldName;
  @Column(name = "\"field_type_cd\"", nullable = false)
  private String fieldTypeCd;
  @Column(name = "\"mandatory_cd\"", nullable = false)
  private String mandatoryCd;
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