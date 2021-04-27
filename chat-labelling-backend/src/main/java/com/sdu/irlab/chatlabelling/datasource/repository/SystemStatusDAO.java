package com.sdu.irlab.chatlabelling.datasource.repository;

import com.sdu.irlab.chatlabelling.datasource.domain.SystemStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SystemStatusDAO extends JpaRepository<SystemStatus, Long> {
    SystemStatus findByAttrName(String attrName);
}
