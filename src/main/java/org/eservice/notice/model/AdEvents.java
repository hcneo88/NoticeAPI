package org.eservice.notice.model;

import java.sql.*;
import javax.persistence.*;
import lombok.Data;

@Data
@Entity(name = "org.eservice.notice.model.AdEvents")
@Table(name = "AD_Events")
public class AdEvents {

  @Id
  @Column(name = "\"event_id\"", nullable = false)
  private String eventId;
  @Column(name = "\"user_name\"", nullable = true)
  private String userName;
  @Column(name = "\"event_level\"", nullable = true)
  private String eventLevel;
  @Column(name = "\"event_group\"", nullable = false)
  private String eventGroup;
  @Column(name = "\"event_cd\"", nullable = false)
  private Integer eventCd;
  @Column(name = "\"event_detail\"", nullable = true)
  private String eventDetail;
  @Column(name = "\"alert_date\"", nullable = true)
  private Timestamp alertDate;
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