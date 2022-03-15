package org.eservice.notice.repository;


import org.eservice.notice.model.CmSignatory;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
//import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
 
@Repository
public interface SignatoryRepository extends CrudRepository<CmSignatory, String> {
 
   @Query(nativeQuery = true, 
          value = "SELECT * FROM CM_Signatory s WHERE s.signatory_id = ?1")
   public CmSignatory findBySignatoryId(String signatoryId);

 }


