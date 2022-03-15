package org.eservice.notice.repository;

import org.eservice.notice.model.ScConfig;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
 
@Repository
public interface ConfigRepository extends CrudRepository<ScConfig, String> {
    
}


