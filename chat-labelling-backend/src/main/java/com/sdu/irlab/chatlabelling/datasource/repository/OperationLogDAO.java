package com.sdu.irlab.chatlabelling.datasource.repository;

import com.sdu.irlab.chatlabelling.datasource.domain.Conversation;
import com.sdu.irlab.chatlabelling.datasource.domain.OperationLog;
import com.sdu.irlab.chatlabelling.datasource.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OperationLogDAO extends JpaRepository<OperationLog, Long> {

    List<OperationLog> findAllByUserAndAndMessageIsNull(User user);

    List<OperationLog> findAllByUserAndConversationAndMessageIsNull(User user, Conversation conversation);

}
