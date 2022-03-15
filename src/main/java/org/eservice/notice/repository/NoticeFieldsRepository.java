package org.eservice.notice.repository;

import org.springframework.stereotype.Repository;

import java.util.List;

import org.eservice.notice.model.CmNoticefields;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

@Repository
public interface NoticeFieldsRepository extends CrudRepository <CmNoticefields, String> {
    
    @Query(nativeQuery = true,
           value = "Select * FROM CM_NoticeFields n WHERE n.notice_id = ?1")
    public List<CmNoticefields> findAllByNoticeId(String noticeId);
}
