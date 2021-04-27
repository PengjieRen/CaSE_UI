package com.sdu.irlab.chatlabelling.datasource.repository;

import com.sdu.irlab.chatlabelling.common.ConversationStatus;
import com.sdu.irlab.chatlabelling.datasource.domain.Conversation;
import com.sdu.irlab.chatlabelling.datasource.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ConversationDAO extends JpaRepository<Conversation, Long> {

    public List<Conversation> findAllByStatusAndCusUserOrderByUpdateTimeDesc(ConversationStatus status, User user);

    public List<Conversation> findAllByStatusAndSysUserOrderByUpdateTimeDesc(ConversationStatus status, User user);

    public Conversation findTopByStatusAndCusUserOrderByUpdateTimeDesc(ConversationStatus status, User user);

    public Conversation findTopByStatusAndSysUserOrderByUpdateTimeDesc(ConversationStatus status, User user);

    public Conversation findTopByCusUserAndSysUserAndStatusOrderByCreateTimeDesc(User cusUser, User sysUser, ConversationStatus status);

    public List<Conversation> findAllByCusUserAndSysUserOrderByCreateTimeDesc(User cusUser, User sysUser);
}
