package org.eservice.notice.model;

import java.sql.*;
import javax.persistence.*;
import lombok.Data;

@Data
@Entity(name = "org.eservice.notice.model.CmNoticeinstances")
@Table(name = "CM_NoticeInstances")
public class CmNoticeinstances {

  @Id
  @Column(name = "\"instance_id\"", nullable = false)
  private String instanceId;
  @Column(name = "\"instance_date\"", nullable = false)
  private Timestamp instanceDate;
  @Column(name = "\"notice_id\"", nullable = false)
  private String noticeId;
  @Column(name = "\"notice_num\"", nullable = false)
  private String noticeNum;
  @Column(name = "\"letterhead_id\"", nullable = false)
  private String letterheadId;
  @Column(name = "\"signatory_id\"", nullable = false)
  private String signatoryId;
  @Column(name = "\"print_type_cd\"", nullable = false)
  private String printTypeCd;
  @Column(name = "\"outsource_action_cd\"", nullable = false)
  private String outsourceActionCd;
  @Column(name = "\"outsource_status_cd\"", nullable = false)
  private String outsourceStatusCd;
  @Column(name = "\"outsource_print_date\"", nullable = true)
  private Timestamp outsourcePrintDate;
  @Column(name = "\"local_print_date\"", nullable = true)
  private Timestamp localPrintDate;
  @Column(name = "\"recipient_name\"", nullable = true)
  private String recipientName;
  @Column(name = "\"addr_type_cd\"", nullable = true)
  private String addrTypeCd;
  @Column(name = "\"blk_hse_num\"", nullable = true)
  private String blkHseNum;
  @Column(name = "\"streetName\"", nullable = true)
  private String streetname;
  @Column(name = "\"floor_num\"", nullable = true)
  private String floorNum;
  @Column(name = "\"unit_num\"", nullable = true)
  private String unitNum;
  @Column(name = "\"bldg_name\"", nullable = true)
  private String bldgName;
  @Column(name = "\"postal_cd\"", nullable = true)
  private String postalCd;
  @Column(name = "\"merge_fields\"", nullable = false)
  private String mergeFields;
  @Column(name = "\"headerfooter_fields\"", nullable = false)
  private String headerfooterFields;
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