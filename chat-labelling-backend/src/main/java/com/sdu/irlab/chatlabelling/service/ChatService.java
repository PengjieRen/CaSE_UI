package com.sdu.irlab.chatlabelling.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mysql.cj.jdbc.exceptions.MysqlDataTruncation;
import com.sdu.irlab.chatlabelling.common.ConversationStatus;
import com.sdu.irlab.chatlabelling.common.MessageType;
import com.sdu.irlab.chatlabelling.datasource.domain.*;
import com.sdu.irlab.chatlabelling.datasource.repository.*;
import com.sdu.irlab.chatlabelling.utils.ChatLabellingUtils;
import com.sun.corba.se.impl.orbutil.concurrent.Mutex;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;


@Service
public class ChatService {

    private UserDAO userDAO;
    private SystemStatusDAO systemStatusDAO;
    private ConversationDAO conversationDAO;
    private MessageDAO messageDAO;
    private OperationLogDAO operationLogDAO;
    private SearchLogDAO searchLogDAO;

    private static ObjectMapper objectMapper = new ObjectMapper();

    @Value("${backgroundFile}")
    private String backgroundFile;
    private List<String> backgrounds;

    private static Mutex mutex = new Mutex();

    @PostConstruct
    private void loadBackgrounds() {
        if (backgrounds != null)
            return;
        String content = ChatLabellingUtils.readFileAsString(backgroundFile, "UTF-8");
        try {
            backgrounds = objectMapper.readValue(content, List.class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        SystemStatus backgroundStartIndex = systemStatusDAO.findByAttrName("BackgroundStartIndex");
        if (backgroundStartIndex == null) {
            backgroundStartIndex = new SystemStatus();
            backgroundStartIndex.setAttrName("BackgroundStartIndex");
            backgroundStartIndex.setAttrValue("0");
            systemStatusDAO.save(backgroundStartIndex);
        }

        SystemStatus backgroundEndIndex = systemStatusDAO.findByAttrName("BackgroundEndIndex");
        if (backgroundEndIndex == null) {
            backgroundEndIndex = new SystemStatus();
            backgroundEndIndex.setAttrName("BackgroundEndIndex");
            backgroundEndIndex.setAttrValue((backgrounds.size() - 1) + "");
            systemStatusDAO.save(backgroundEndIndex);
        }


        SystemStatus backgroundIndex = systemStatusDAO.findByAttrName("BackgroundIndex");
        if (backgroundIndex == null) {
            backgroundIndex = new SystemStatus();
            backgroundIndex.setAttrName("BackgroundIndex");
            backgroundIndex.setAttrValue(backgroundStartIndex.getAttrValue());
            systemStatusDAO.save(backgroundIndex);
        }

        SystemStatus maxBackgroundTurn = systemStatusDAO.findByAttrName("MaxBackgroundTurn");
        if (maxBackgroundTurn == null) {
            maxBackgroundTurn = new SystemStatus();
            maxBackgroundTurn.setAttrName("MaxBackgroundTurn");
            maxBackgroundTurn.setAttrValue("1");
            systemStatusDAO.save(maxBackgroundTurn);
        }

        SystemStatus backgroundTurn = systemStatusDAO.findByAttrName("BackgroundTurn");
        if (backgroundTurn == null) {
            backgroundTurn = new SystemStatus();
            backgroundTurn.setAttrName("BackgroundTurn");
            backgroundTurn.setAttrValue("0");
            systemStatusDAO.save(backgroundTurn);
        }
    }

    private String randomBackground() {
        String background = null;
        try {
            mutex.acquire();
            SystemStatus backgroundStartIndex = systemStatusDAO.findByAttrName("BackgroundStartIndex");
            SystemStatus backgroundEndIndex = systemStatusDAO.findByAttrName("BackgroundEndIndex");
            SystemStatus backgroundIndex = systemStatusDAO.findByAttrName("BackgroundIndex");
            SystemStatus maxBackgroundTurn = systemStatusDAO.findByAttrName("MaxBackgroundTurn");
            SystemStatus backgroundTurn = systemStatusDAO.findByAttrName("BackgroundTurn");

            int start = Integer.parseInt(backgroundStartIndex.getAttrValue());
            int end = Integer.parseInt(backgroundEndIndex.getAttrValue());
            int index = Integer.parseInt(backgroundIndex.getAttrValue());
            int turn = Integer.parseInt(backgroundTurn.getAttrValue());
            int maxTurn = Integer.parseInt(maxBackgroundTurn.getAttrValue());
//            System.out.println(start + "-" + end + "-" + index + "-" + turn + "-" + maxTurn);
            if (backgrounds.size() > index && index >= start && index <= end && turn < maxTurn) {
                background = backgrounds.get(index);
                index++;
                if (index > end) {
                    index = start;
                    turn++;
                }
                backgroundIndex.setAttrValue(index + "");
                backgroundTurn.setAttrValue(turn + "");
                systemStatusDAO.saveAndFlush(backgroundIndex);
                systemStatusDAO.saveAndFlush(backgroundTurn);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            mutex.release();
        }
        return background;
    }

    @Transactional
    public void stopAllConversation(String sysName, String cusName) {
        List<Conversation> cusConversations = conversationDAO.findAllByStatusAndCusUserOrderByUpdateTimeDesc(ConversationStatus.ONGOING, userDAO.findByName(cusName));
        List<Conversation> sysConversations = conversationDAO.findAllByStatusAndSysUserOrderByUpdateTimeDesc(ConversationStatus.ONGOING, userDAO.findByName(sysName));
        cusConversations.forEach(conversation -> {
            conversation.setStatus(ConversationStatus.STOPPED);
        });
        sysConversations.forEach(conversation -> {
            conversation.setStatus(ConversationStatus.STOPPED);
        });
        conversationDAO.saveAll(cusConversations);
        conversationDAO.saveAll(sysConversations);
    }

    @Transactional
    public void changeConversationStatus(ConversationStatus status, Conversation conversation) {
        conversation.setStatus(status);
        conversationDAO.save(conversation);
    }

    public List<Message> loadHistory(String sysName, String cusName) {
        Conversation conversation = conversationDAO.findTopByCusUserAndSysUserAndStatusOrderByCreateTimeDesc(
                userDAO.findByName(cusName), userDAO.findByName(sysName), ConversationStatus.STOPPED);
        if (conversation == null)
            return new ArrayList<>();
        List<Message> messages = messageDAO.findMessageByConversationOrderByCreateTimeDesc(conversation);
        if (messages == null)
            return new ArrayList<>();
        return messages;
    }

    public List<Message> loadHistory(Conversation conversation) {
        if (conversation == null)
            return new ArrayList<>();
        List<Message> messages = messageDAO.findMessageByConversationAndTypeIsNotOrderByCreateTimeDesc(conversation, MessageType.HURRYUP);
        if (messages == null)
            return new ArrayList<>();
        return messages;
    }

    public int messageCount(Conversation conversation) {
        return messageDAO.countByConversation(conversation);
    }


    public String getRole(String userName, Conversation conversation) {
        if (userName.equals(conversation.getCusUser().getUsername())) {
            return "cus";
        }
        if (userName.equals(conversation.getSysUser().getUsername())) {
            return "sys";
        }
        return "other";
    }

    @Transactional
    public void stopConversation(String userName) {
        Conversation conversation = getCurrentConversation(userName);
        if (conversation == null)
            return;
        conversation.setStatus(ConversationStatus.STOPPED);
        //立即flush，保证下一次不会因为conversation数据不一致出现问题
        conversationDAO.saveAndFlush(conversation);
    }

    @Transactional
    public void stopConversation(String userName, Conversation conversation) {
        conversation.setStatus(ConversationStatus.STOPPED);
        //立即flush，保证下一次不会因为conversation数据不一致出现问题
        conversationDAO.saveAndFlush(conversation);
    }

    public Conversation getCurrentConversation(String username) {
        Conversation conversation = getCurrentConversationBySysName(username);
        if (conversation == null)
            conversation = getCurrentConversationByCusName(username);
        return conversation;
    }

    private Conversation getCurrentConversationBySysName(String sysName) {
        return conversationDAO.findTopByStatusAndSysUserOrderByUpdateTimeDesc(ConversationStatus.ONGOING, userDAO.findByName(sysName));
    }

    private Conversation getCurrentConversationByCusName(String cusName) {
        return conversationDAO.findTopByStatusAndCusUserOrderByUpdateTimeDesc(ConversationStatus.ONGOING, userDAO.findByName(cusName));
    }


    public String getCurrentPartnerName(String username) {
        Conversation conversation;
        if ((conversation = getCurrentConversationBySysName(username)) != null) {
            return conversation.getCusUser().getUsername();
        }
        if ((conversation = getCurrentConversationByCusName(username)) != null) {
            return conversation.getSysUser().getUsername();
        }
        return null;
    }

    public String getCurrentPartnerName(String username, Conversation conversation) {
        if (conversation == null)
            return null;
        User sysUser = conversation.getSysUser();
        User cusUser = conversation.getCusUser();
        if (sysUser.getUsername().equals(username))
            return cusUser.getUsername();
        return sysUser.getUsername();
    }

    /**
     * @param from
     * @param to
     * @param content       消息内容
     * @param originMessage 原始消息json
     * @param type          消息类型
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public Message createMessage(String from, String to, String content, boolean sendAnother, String originMessage, MessageType type, Conversation conversation) throws MysqlDataTruncation {
        JsonNode node = null;
        try {
            node = objectMapper.readTree(originMessage);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }


        if (type == MessageType.SYS2CUS) { //如果是系统对用户说话，则需要更新state
            conversation.setState(node.get("state").toString());
        } else if (type == MessageType.CUS2SYS && !sendAnother) {//如果是用户对系统说话，且没有选择再说一句话，可以开始新的一轮
            conversation.setCurrentTurn(conversation.getCurrentTurn() + 1);
        }
        conversation.setNextSpeaker((type == MessageType.SYS2CUS && sendAnother) || (type == MessageType.CUS2SYS && !sendAnother) ? "sys" : "cus");
        conversationDAO.save(conversation);

        Message message = new Message();
        message.setContent(content);
        message.setConversation(conversation);
        message.setReceiver(userDAO.findByName(to));
        message.setSender(userDAO.findByName(from));
        message.setTurn(conversation.getCurrentTurn()); //新的message和conversation的current turn相同
        message.setType(type);
        message.setSendAnother(sendAnother);
        message.setOriginMessage(originMessage);
        if (node.has("action")) {
            message.setAction(node.get("action").toString());
        }
        messageDAO.save(message);

        if (type != MessageType.HURRYUP) {
            List<OperationLog> logs = operationLogsWithoutMessage(from, conversation);
            logs.forEach(operationLog -> {
                operationLog.setMessage(message);
                operationLog.setUserType(type == MessageType.SYS2CUS ? "SYS" : "CUS");
                operationLog.setConversation(conversation);
            });
            operationLogDAO.saveAll(logs);
        }

        //系统发出的，都有搜索日志，把搜索日志与新建的message关联
        if (type == MessageType.SYS2CUS) {
            List<SearchLog> logs = searchLogsWithoutMessage(from, conversation);
            logs.forEach(searchLog -> {
                searchLog.setMessage(message);
                searchLog.setConversation(conversation);
            });
            searchLogDAO.saveAll(logs);
        }
//        throw new MysqlDataTruncation("message", 0, false, false, 0, 0, 0);
        return message;
    }

    private List<OperationLog> operationLogsWithoutMessage(String user) {
        return operationLogDAO.findAllByUserAndAndMessageIsNull(userDAO.findByName(user));
    }

    private List<OperationLog> operationLogsWithoutMessage(String user, Conversation conversation) {
        return operationLogDAO.findAllByUserAndConversationAndMessageIsNull(userDAO.findByName(user), conversation);
    }

    private List<SearchLog> searchLogsWithoutMessage(String user, Conversation conversation) {
        return searchLogDAO.findAllByUserAndConversationAndMessageIsNull(userDAO.findByName(user), conversation);
    }

    @Transactional
    public OperationLog createOperationLog(String user, String type, String content, String conversationId) {
        Conversation conversation = conversationDAO.getOne(Long.parseLong(conversationId));
        //这个地方，不给OperationLog添加message关联，因为这个时候message还没发出去
        OperationLog operationLog = new OperationLog();
        operationLog.setContent(content);
        operationLog.setType(type);
        operationLog.setUser(userDAO.findByName(user));
        operationLog.setConversation(conversation);
        operationLogDAO.save(operationLog);
        return operationLog;
    }

    public SearchLog createSearchLog(String user, String query, String content, String conversationId) {
        Conversation conversation = conversationDAO.getOne(Long.parseLong(conversationId));
        SearchLog searchLog = new SearchLog();
        searchLog.setContent(content);
        searchLog.setQuery(query);
        searchLog.setUser(userDAO.findByName(user));
        searchLog.setConversation(conversation);
        searchLogDAO.save(searchLog);
        return searchLog;
    }

    public boolean hasBackground() {
        SystemStatus backgroundStartIndex = systemStatusDAO.findByAttrName("BackgroundStartIndex");
        SystemStatus backgroundEndIndex = systemStatusDAO.findByAttrName("BackgroundEndIndex");
        SystemStatus backgroundIndex = systemStatusDAO.findByAttrName("BackgroundIndex");
        SystemStatus maxBackgroundTurn = systemStatusDAO.findByAttrName("MaxBackgroundTurn");
        SystemStatus backgroundTurn = systemStatusDAO.findByAttrName("BackgroundTurn");

        int start = Integer.parseInt(backgroundStartIndex.getAttrValue());
        int end = Integer.parseInt(backgroundEndIndex.getAttrValue());
        int index = Integer.parseInt(backgroundIndex.getAttrValue());
        int turn = Integer.parseInt(backgroundTurn.getAttrValue());
        int maxTurn = Integer.parseInt(maxBackgroundTurn.getAttrValue());
        return index < backgrounds.size() && turn < maxTurn && index >= start && index <= end;
    }


    @Transactional
    public Conversation createConversation(String sysName, String cusName) {
        String background = randomBackground();
        User sysUser = userDAO.findByName(sysName);
        User cusUser = userDAO.findByName(cusName);
        Conversation conversation = new Conversation();
        conversation.setCurrentTurn(0);
        conversation.setStatus(ConversationStatus.ONGOING);
        conversation.setCusUser(cusUser);
        conversation.setSysUser(sysUser);
        conversation.setBackground(background);
        conversation.setNextSpeaker("cus");
        //必须立即flush，立即持久化到数据库，因为之后所有的查询都和这个conversation有关
        conversationDAO.saveAndFlush(conversation);
        return conversation;
    }

    @Autowired
    public void setUserDAO(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    @Autowired
    public void setConversationDAO(ConversationDAO conversationDAO) {
        this.conversationDAO = conversationDAO;
    }

    @Autowired
    public void setMessageDAO(MessageDAO messageDAO) {
        this.messageDAO = messageDAO;
    }

    @Autowired
    public void setOperationLogDAO(OperationLogDAO operationLogDAO) {
        this.operationLogDAO = operationLogDAO;
    }

    @Autowired
    public void setSystemStatusDAO(SystemStatusDAO systemStatusDAO) {
        this.systemStatusDAO = systemStatusDAO;
    }

    @Autowired
    public void setSearchLogDAO(SearchLogDAO searchLogDAO) {
        this.searchLogDAO = searchLogDAO;
    }
}
