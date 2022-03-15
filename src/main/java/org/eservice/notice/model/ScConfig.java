package org.eservice.notice.model;

import java.sql.*;
import javax.persistence.*;
import lombok.Data;

@Data
@Entity(name = "org.eservice.notice.model.ScConfig")
@Table(name = "SC_Config")
public class ScConfig {

  @Id
  @Column(name = "\"config_id\"", nullable = false)
  private String configId;
  @Column(name = "\"config_category\"", nullable = false)
  private String configCategory;
  @Column(name = "\"config_key\"", nullable = true)
  private String configKey;
  @Column(name = "\"config_type\"", nullable = false)
  private String configType;
  @Column(name = "\"config_value\"", nullable = false)
  private String configValue;
  @Column(name = "\"config_remark\"", nullable = true)
  private String configRemark;
  @Column(name = "\"config_status\"", nullable = false)
  private String configStatus;
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