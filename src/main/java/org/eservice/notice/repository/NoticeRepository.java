package org.eservice.notice.repository;

import java.util.List;

import org.eservice.notice.model.CmNotices;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
//import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
 
@Repository
public interface NoticeRepository extends CrudRepository<CmNotices, String> {
 
   @Query(nativeQuery = true, 
          value = "SELECT * FROM CM_Notices n WHERE n.notice_num = ?1 AND " +
                  "DATE(n.eff_start_date) <= CURRENT_DATE AND " + 
                  "DATE(n.eff_end_date) >= CURRENT_DATE") 
   public List<CmNotices> findByNoticeNum(String noticeNum);

   @Query(nativeQuery = true, 
          value = "select * FROM CM_Notices n WHERE n.notice_id= ?1")
   public CmNotices findByNoticeId(String noticeId);
}


