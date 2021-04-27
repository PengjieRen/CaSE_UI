package com.sdu.irlab.chatlabelling.datasource.repository;

import com.sdu.irlab.chatlabelling.datasource.domain.WebsocketLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.web.context.request.WebRequest;

@Repository
public interface WebsocketLogDAO extends JpaRepository<WebsocketLog, Long> {
}
