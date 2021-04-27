package com.sdu.irlab.chatlabelling.component.websocket;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sdu.irlab.chatlabelling.common.ConversationStatus;
import com.sdu.irlab.chatlabelling.common.MessageCommand;
import com.sdu.irlab.chatlabelling.common.MessageType;
import com.sdu.irlab.chatlabelling.datasource.domain.Conversation;
import com.sdu.irlab.chatlabelling.datasource.domain.SystemStatus;
import com.sdu.irlab.chatlabelling.datasource.domain.User;
import com.sdu.irlab.chatlabelling.datasource.domain.WebsocketLog;
import com.sdu.irlab.chatlabelling.datasource.repository.SystemStatusDAO;
import com.sdu.irlab.chatlabelling.datasource.repository.UserDAO;
import com.sdu.irlab.chatlabelling.datasource.repository.WebsocketLogDAO;
import com.sdu.irlab.chatlabelling.service.ChatService;
import com.sun.corba.se.impl.orbutil.concurrent.Mutex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

@Component
@ServerEndpoint("/websocket/{username}")
public class WebSocket {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private static ObjectMapper objectMapper = new ObjectMapper();
    private static int onlineNumber = 0;

    protected Session session;
    protected String username;
    protected User currentUser;
    protected String userRole;
    protected String partnerRole;


    protected static BlockingQueue<String[]> messageQueue = new LinkedBlockingQueue<>();
    protected static ConcurrentHashMap<String, WebSocket> clients = new ConcurrentHashMap<>();
    protected static LinkedList<String> waitingList = new LinkedList<>();

    private static Mutex mutex = new Mutex();
    private static ChatService chatService;
    private static UserDAO userDAO;
    private static SystemStatusDAO systemStatusDAO;
    protected static WebsocketLogDAO websocketLogDAO;

    private Conversation currentConversation;

    //    private static long waitMilliseconds = 1000;
    private String waitingFor = null;//正在等待的用户
    private Timer timer = new Timer();//timerTask的定时器
    private TimerTask timerTask;

    //开线程，用来发消息
    static {
        Runnable runnable = new MessageSender();
        new Thread(runnable).start();
    }


    @Autowired
    public void setChatService(ChatService service) {
        chatService = service;
    }

    @Autowired
    public void setUserDAO(UserDAO dao) {
        userDAO = dao;
    }

    @Autowired
    public void setWebsocketLogDAO(WebsocketLogDAO websocketLogDAO) {
        WebSocket.websocketLogDAO = websocketLogDAO;
    }

    @Autowired
    public void setSystemStatusDAO(SystemStatusDAO systemStatusDAO) {
        WebSocket.systemStatusDAO = systemStatusDAO;
        SystemStatus systemStatus = systemStatusDAO.findByAttrName("WaitSeconds");
        if (systemStatus == null) {
            systemStatus = new SystemStatus();
            systemStatus.setAttrName("WaitSeconds");
            systemStatus.setAttrValue("60");
            systemStatusDAO.save(systemStatus);
        }
    }

    public static boolean userConnected(String username) {
        return clients.containsKey(username);
    }

    /**
     * 开始聊天，找不到人的话，把user放到等待队列
     *
     * @param user
     */
    private static void startChat(String user) {
        System.out.println("========================startChat ===============================");
        boolean hasBackground = chatService.hasBackground();
        List<Map<String, Object>> userMessageList = new ArrayList<>();
        List<Map<String, Object>> partnerMessageList = new ArrayList<>();
        WebSocket client = clients.get(user);
        String waitingPartner = findWaitingPartner(user);
        String newPartner = null;

        if (client == null) return;
        if (!hasBackground && waitingPartner == null) {
            //没有background且不能恢复之前的对话
            sendMessageList(Arrays.asList(createMessage(MessageCommand.ERROR, MessageType.ERROR, "", "", "No more task right now! The system is closed!")), user, -1);
            return;
        }

        String partner = null;//partner是对话的另一方，恢复的还是创建的都可以
        if (waitingPartner != null) {
            partner = waitingPartner;
            System.out.println("found waiting partner --" + partner);
            //恢复之前的对话
            client.currentConversation = clients.get(partner).currentConversation;
            changeCurrentConversationStatus(user, partner, ConversationStatus.ONGOING);
        } else if ((newPartner = findPartner(user, client.partnerRole)) != null) {
            partner = newPartner;
            System.out.println("found new partner --" + partner);
            //创建新对话
            chatService.stopAllConversation(user, partner);
            String sysName = client.userRole.equals("sys") ? user : partner;
            String cusName = client.userRole.equals("cus") ? user : partner;
            Conversation conversation = chatService.createConversation(sysName, cusName);
            client.currentConversation = conversation;
            clients.get(partner).currentConversation = conversation;
        }

        if (partner != null) {
            //找到了对话另一方
            String userRole = client.userRole;
            String partnerRole = client.partnerRole;
            //根据conversation的nextSpeaker决定下一步谁说话
            boolean shouldWait = client.currentConversation.getNextSpeaker().equals(partnerRole);
            //对话开始，确定对话双方，确定角色
            Map<String, Object> userData = new HashMap<>();
            userData.put("role", userRole);
            userData.put("partner", partner);
            userData.put("conversationId", client.currentConversation.getId());
            userMessageList.add(createMessage(MessageCommand.START, MessageType.OTHER, "", "", "", userData));

            Map<String, Object> partnerData = new HashMap<>();
            partnerData.put("role", partnerRole);
            partnerData.put("partner", user);
            partnerData.put("conversationId", client.currentConversation.getId());
            partnerMessageList.add(createMessage(MessageCommand.START, MessageType.OTHER, "", "", "", partnerData));

            //确定下一步是发消息还是等待消息
            if (shouldWait) {
                userMessageList.add(createMessage(MessageCommand.WAIT4MESSAGE, MessageType.OTHER, "", "", ""));
                partnerMessageList.add(createMessage(MessageCommand.SENDMESSAGE, MessageType.OTHER, "", "", ""));
            } else {
                partnerMessageList.add(createMessage(MessageCommand.WAIT4MESSAGE, MessageType.OTHER, "", "", ""));
                userMessageList.add(createMessage(MessageCommand.SENDMESSAGE, MessageType.OTHER, "", "", ""));
            }

        } else {//没有找到对话对象，开始等待
            waitingList.add(user);
            userMessageList.add(createMessage(MessageCommand.WAIT4PARTNER, MessageType.OTHER, "", "", ""));
        }

        Conversation conversation = client.currentConversation;
        sendMessageList(userMessageList, user, conversation == null ? -1l : conversation.getId());
        sendMessageList(partnerMessageList, partner, conversation == null ? -1l : conversation.getId());
        System.out.println("=================================================================================");
    }

    private static void sendMessageList(List<Map<String, Object>> list, String target, long conversationId) {
        if (list.size() == 0 || target == null) return;
        try {
            String message = objectMapper.writeValueAsString(list);
            sendMessageTo(message, target, conversationId);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    /**
     * user关闭了连接，对话结束
     *
     * @param user
     */
    private synchronized void userDisconnected(String user) {
        if (!clients.containsKey(user)) return;
        WebSocket client = clients.get(user);
        Conversation conversation = client.currentConversation;
        if (conversation == null) return;
        String partner = chatService.getCurrentPartnerName(user, conversation);
        //对话状态还在进行中，连接关闭，说明一方掉线，导致对话异常终止，需要通知另一方，并将对话状态改为异常终止
        if (conversation.getStatus() == ConversationStatus.ONGOING && partner != null) {
            List<Map<String, Object>> partnerMessageList = new ArrayList<>();
            partnerMessageList.add(createMessage(MessageCommand.STOP, MessageType.OTHER, "", "", ""));
            sendMessageList(partnerMessageList, partner, conversation.getId());
            chatTerminated(user, partner);
        }
    }

    private void chatTerminated(String user, String partner) {
        changeCurrentConversationStatus(user, partner, ConversationStatus.TERMINATED);
        restartWaitingTimerTask(partner, user);
    }

    private static void changeCurrentConversationStatus(String user, String partner, ConversationStatus status) {
        if (clients.containsKey(user) && clients.get(user).currentConversation.getStatus() != status) {
            chatService.changeConversationStatus(status, clients.get(user).currentConversation);
        }
        if (clients.containsKey(partner) && clients.get(partner).currentConversation.getStatus() != status) {
            chatService.changeConversationStatus(status, clients.get(user).currentConversation);
        }
    }

    private static Map<String, Object> createMessage(MessageCommand messageCommand, MessageType messageType, String from, String to, String content) {
        Map<String, Object> map = new HashMap<>();
        map.put("messageCommand", messageCommand.name());
        map.put("messageType", messageType.name());
        map.put("from", from);
        map.put("to", to);
        map.put("content", content);
        return map;
    }

    private static Map<String, Object> createMessage(MessageCommand messageCommand, MessageType messageType, String from, String to, String content, Map<String, Object> data) {
        Map<String, Object> map = createMessage(messageCommand, messageType, from, to, content);
        map.put("data", data);
        return map;
    }

    /**
     * 建立连接,执行前加锁，onOpen和onClose保证一定顺序执行
     *
     * @param session
     */
    @OnOpen
    public void onOpen(@PathParam("username") String username, Session session) {
        try {
            mutex.acquire();
            currentUser = userDAO.findByName(username);
            if (!clients.containsKey(username)) {
                this.username = username;
                this.session = session;
                clients.put(username, this);
                onlineNumber++;
                logger.info("User online! username=" + username + ", current online number=" + onlineNumber);
            } else {
                logger.error("Duplicate connection！" + "username=：" + username);
                List<Map<String, Object>> message = Arrays.asList(createMessage(MessageCommand.ERROR, MessageType.ERROR, "", "", "Already connected to server!"));
                try {
                    session.getAsyncRemote().sendText(objectMapper.writeValueAsString(message));
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }
                return;
            }

            if (currentUser == null) {
                logger.error("User does not exist！" + "username=：" + username);
                sendMessageList(Arrays.asList(createMessage(MessageCommand.ERROR, MessageType.ERROR, "", "", "User does not exist!")), username, -1);
                return;
            } else {
                userRole = currentUser.getRole();
                partnerRole = currentUser.getRole().equals("sys") ? "cus" : "sys";
            }

            this.startChat(username);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            mutex.release();
        }
    }

    public void restartChat(String user) {
        try {
            mutex.acquire();
            this.startChat(user);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            mutex.release();
        }
    }

    /**
     * 结束对话并清空实体
     *
     * @param user
     * @param partner
     */
    public void chatFinished(String user, String partner) {
        changeCurrentConversationStatus(user, partner, ConversationStatus.STOPPED);
    }

    /**
     * 连接关闭，执行前加锁，onOpen和onClose保证一定顺序执行
     * 连接关闭后，如果另一方还在线，可以让另一方进入等待队列，重新开始匹配
     */
    @OnClose
    public void onClose() {
        try {
            mutex.acquire();
            if (username == null || !clients.containsKey(username)) return;
            this.userDisconnected(username);
            onlineNumber--;
            clients.remove(username);
            waitingList.remove(username);
            logger.info("User disconnected! username=" + username + ", current online number=" + onlineNumber);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            mutex.release();
        }
    }

    @OnError
    public void onError(Session session, Throwable error) {
        error.printStackTrace();
        logger.info("Something wrong on server！" + error.getMessage());
    }


    /**
     * 收到客户端的消息
     *
     * @param message 消息
     * @param session 会话
     */
    @OnMessage
    public void onMessage(String message, Session session) {
        if (currentConversation == null) return;
        List<Map<String, Object>> userMessageList = new ArrayList<>();
        List<Map<String, Object>> partnerMessageList = new ArrayList<>();
        String partner = null;
        MessageType type = null;
        try {


            //保存log
            WebsocketLog websocketLog = new WebsocketLog();
            websocketLog.setContent(message);
            websocketLog.setReceiver("server");
            websocketLog.setSender(username);
            websocketLog.setConversationId(currentConversation.getId());
            websocketLogDAO.save(websocketLog);

            JsonNode node = null;
            node = objectMapper.readTree(message);
            String from = node.get("from").asText();
            String to = node.get("to").asText();
            type = MessageType.valueOf(node.get("type").asText());

            partner = chatService.getCurrentPartnerName(username, currentConversation);
            if (partner == null) return;
            if (type == MessageType.HURRYUP) {
                partnerMessageList.add(createMessage(MessageCommand.HURRYUP, MessageType.OTHER, "", "", ""));
                chatService.createMessage(from, to, "", false, message, type, currentConversation);
            } else if (type == MessageType.FINISH) {
                //cus用户点了finish键,告诉双方已经已经结束了
                userMessageList.add(createMessage(MessageCommand.FINISH, MessageType.OTHER, "", "", ""));
                partnerMessageList.add(createMessage(MessageCommand.FINISH, MessageType.OTHER, "", "", ""));

                //结束对话
                chatFinished(username, partner);

            } else if (!from.equals(username) || !to.equals(partner)) {
                logger.info("Message sender and receiver do not match！");
                userMessageList.add(createMessage(MessageCommand.ERROR, MessageType.ERROR, "", "", "Sender or receiver do not match!"));
            } else {
                // 把消息发给to，并给to发指令，说可以发送一条消息，同时给from发指令，等待to的消息
                String response = node.get("response").asText();
                //sendAnother表示说话者是否需要再说一句话
                boolean sendAnother = node.get("sendAnother").asBoolean();
                chatService.createMessage(from, to, response, sendAnother, message, type, currentConversation);

                if (!sendAnother) {
                    partnerMessageList.add(createMessage(MessageCommand.SENDMESSAGE, type, from, to, response));
                    userMessageList.add(createMessage(MessageCommand.WAIT4MESSAGE, MessageType.OTHER, "", "", ""));
                } else {
                    partnerMessageList.add(createMessage(MessageCommand.WAIT4MESSAGE, type, from, to, response));
                    userMessageList.add(createMessage(MessageCommand.SENDMESSAGE, MessageType.OTHER, "", "", ""));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            sendExceptionMessage(username, partner, type, currentConversation);
        }

        sendMessageList(userMessageList, username, currentConversation.getId());
        sendMessageList(partnerMessageList, partner, currentConversation.getId());


    }


    /**
     * websocket出错之后，告诉客户端，server出错了，发消息者需要重发消息，等待者继续等待
     * 只针对正常对话时的双方之间的消息
     *
     * @param user
     * @param partner
     * @param conversation
     */
    public static void sendExceptionMessage(String user, String partner, MessageType messageType, Conversation conversation) {
        if (user == null || partner == null || conversation == null) return;
        //不是对话的消息，也不用报错
        if (messageType != MessageType.SYS2CUS && messageType != MessageType.CUS2SYS) return;


        List<Map<String, Object>> userMessageList = new ArrayList<>();
        List<Map<String, Object>> partnerMessageList = new ArrayList<>();
        partnerMessageList.add(createMessage(MessageCommand.WAIT4MESSAGE, MessageType.OTHER, "", "", ""));
        userMessageList.add(createMessage(MessageCommand.ERROR, MessageType.ERROR, "", "", "Something wrong on server!"));
        userMessageList.add(createMessage(MessageCommand.SENDMESSAGE, MessageType.OTHER, "", "", ""));
        sendMessageList(userMessageList, user, conversation.getId());
        sendMessageList(partnerMessageList, partner, conversation.getId());
    }

    public static void sendMessageTo(String message, String toUserName, long conversationId) {
        messageQueue.add(new String[]{message, toUserName});
        WebsocketLog websocketLog = new WebsocketLog();
        websocketLog.setContent(message);
        websocketLog.setReceiver(toUserName);
        websocketLog.setSender("server");
        websocketLog.setConversationId(conversationId);
        websocketLogDAO.save(websocketLog);
    }

    private static String findPartner(String user) {
        String partner = null;
        //user本身不应该出现在等待列表中
        while (waitingList.contains(user)) {
            waitingList.remove(user);
        }
        if (WebSocket.waitingList.size() > 0) {
            while ((partner = WebSocket.waitingList.poll()) != null) {
                if (clients.containsKey(partner)) {
                    break;
                }
            }
        }
        return partner;
    }

    private static String findPartner(String user, String partnerRole) {
        System.out.println("finding partner--user=" + user + ",role=" + partnerRole + ",waitingList=" + waitingList);
        User u = clients.get(user).currentUser;
        List<String> blackList = new ArrayList<>();
        if (u.getBlackList() != null && u.getBlackList().length() > 0) {
            try {
                blackList = objectMapper.readValue(u.getBlackList(), List.class);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        }
        String partner = null;
        //user本身不应该出现在等待列表中
        while (waitingList.contains(user)) {
            waitingList.remove(user);
        }
        if (WebSocket.waitingList.size() > 0) {
            for (int i = 0; i < waitingList.size(); i++) {
                String tempPartner = waitingList.get(i);
                if (clients.containsKey(tempPartner) && clients.get(tempPartner).userRole.equals(partnerRole) && !blackList.contains(tempPartner)) {
                    partner = tempPartner;
                    break;
                }
            }
            if (partner != null) {
                waitingList.remove(partner);
            }
        }
        System.out.println("finish finding partner--user=" + user + ",role=" + partnerRole + ",partner=" + partner);
        return partner;
    }

    private static String findWaitingPartner(String user) {
        for (WebSocket webSocket : clients.values()) {
            if (user.equals(webSocket.waitingFor)) {
                cancelCurrentWaitingTimerTask(webSocket.username);
                return webSocket.username;
            }
        }
        return null;
    }

    private TimerTask generateWaitingTimerTask(String clientName) {
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                if (!clients.containsKey(clientName)) return;
                WebSocket client = clients.get(clientName);
                //等待了特定时间，还是没有等到对方上线（上线后waitingFor会清空）
                if (client.waitingFor != null) {
                    client.waitingFor = null;
                    restartChat(clientName);
                }
            }
        };
        return timerTask;
    }

    private void restartWaitingTimerTask(String clientName, String waitingFor) {
        if (!clients.containsKey(clientName)) return;
        cancelCurrentWaitingTimerTask(clientName);
        WebSocket client = clients.get(clientName);
        TimerTask task = generateWaitingTimerTask(clientName);
        SystemStatus waitSeconds = systemStatusDAO.findByAttrName("WaitSeconds");
        client.timer.schedule(task, Long.parseLong(waitSeconds.getAttrValue()) * 1000);
        client.timerTask = task;
        client.waitingFor = waitingFor;
    }

    private static void cancelCurrentWaitingTimerTask(String clientName) {
        if (!clients.containsKey(clientName)) return;
        WebSocket client = clients.get(clientName);
        client.waitingFor = null;
        if (client.timerTask != null)
            client.timerTask.cancel();
        client.timer.purge();
    }

}
