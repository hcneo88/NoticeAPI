package org.eservice.notice.repository;

import java.util.List;

import org.eservice.notice.model.CmNoticeinstances;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
 
@Repository
public interface NoticeInstancesRepository extends CrudRepository<CmNoticeinstances, String> {
 
   @Query(nativeQuery = true, 
          value = "SELECT * FROM CM_NoticeInstances n WHERE n.instance_id = ?1 ")
   public CmNoticeinstances findByInstanceId(String instanceId);

   @Query(nativeQuery = true, 
          value = "SELECT * FROM CM_NoticeInstances n WHERE " +
                  "n.notice_id = ?1 AND  DATE(created_date) >=  ?2 AND DATE(created_date) <= ?3 LIMIT ?4")
   public List<CmNoticeinstances> findByNoticeId(String noticeId, String startDate, String endDate, int rowLimit);

   @Query(nativeQuery = true, 
          value = "SELECT * FROM CM_NoticeInstances n WHERE " +
                  "n.notice_num = ?1 AND  DATE(created_date) >=  ?2 AND DATE(created_date) <= ?3 LIMIT ?4")
   public List<CmNoticeinstances> findByNoticeNum(String noticeNum, String startDate, String endDate, int rowLimit);

   @Query(nativeQuery = true, 
          value = "SELECT * FROM CM_NoticeInstances n WHERE " +
                  "UPPER(n.recipient_name) = UPPER(?1) AND  created_date BETWEEN ?2 AND ?3 LIMIT ?4")
   public List<CmNoticeinstances> findByRecipientName(String recipientName, String startDate, String endDate, int rowLimt);

   

}


