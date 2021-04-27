package com.sdu.irlab.chatlabelling.controller;

import com.sdu.irlab.chatlabelling.common.ConversationStatus;
import com.sdu.irlab.chatlabelling.datasource.domain.Conversation;
import com.sdu.irlab.chatlabelling.datasource.domain.Message;
import com.sdu.irlab.chatlabelling.datasource.domain.OperationLog;
import com.sdu.irlab.chatlabelling.datasource.domain.User;
import com.sdu.irlab.chatlabelling.datasource.repository.ConversationDAO;
import com.sdu.irlab.chatlabelling.datasource.repository.OperationLogDAO;
import com.sdu.irlab.chatlabelling.datasource.repository.UserDAO;
import com.sdu.irlab.chatlabelling.service.ChatService;
import com.sdu.irlab.chatlabelling.utils.ChatLabellingUtils;
import org.omg.Messaging.SYNC_WITH_TRANSPORT;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/test")
public class TestController {
    @Autowired
    private ConversationDAO conversationDAO;

    @Autowired
    private UserDAO userDAO;

    @Autowired
    private ChatService chatService;

    @Autowired
    private OperationLogDAO operationLogDAO;

    @RequestMapping(value = "/test1/{name}", method = RequestMethod.GET)
    public String test1(@PathVariable(value = "name") String name) {
        Conversation c = chatService.createConversation("aaa", "bbb");
//        Message m1 = chatService.createMessage("aaa", "bbb", "hello");
//        Message m2 = chatService.createMessage("bbb", "aaa", "how are you?");
        return "c.getStatus().name()";
    }

    @RequestMapping(value = "/test2", method = RequestMethod.GET)
    public String test2() {
        return ChatLabellingUtils.SEARCH_DEMO;
    }

    @RequestMapping(value = "/test3", method = RequestMethod.GET)
    public String test3() {
        String cusName="aaa";
        String sysName="bbb";
        Map<String, Object> map = new HashMap<>();
        Conversation conversation = conversationDAO.findTopByCusUserAndSysUserAndStatusOrderByCreateTimeDesc(
                userDAO.findByName(cusName), userDAO.findByName(sysName), ConversationStatus.ONGOING);
        System.out.println(conversation);
        map.put("background", conversation.getBackground());
        return "SDFSDF";
    }
}
