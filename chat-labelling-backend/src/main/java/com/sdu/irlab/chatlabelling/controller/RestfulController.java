package com.sdu.irlab.chatlabelling.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sdu.irlab.chatlabelling.common.ConversationStatus;
import com.sdu.irlab.chatlabelling.datasource.domain.Conversation;
import com.sdu.irlab.chatlabelling.datasource.domain.Message;
import com.sdu.irlab.chatlabelling.datasource.domain.User;
import com.sdu.irlab.chatlabelling.datasource.repository.ConversationDAO;
import com.sdu.irlab.chatlabelling.datasource.repository.UserDAO;
import com.sdu.irlab.chatlabelling.security.CustomSessionManagement;
import com.sdu.irlab.chatlabelling.service.BaiduSearchService;
import com.sdu.irlab.chatlabelling.service.BingSearchService;
import com.sdu.irlab.chatlabelling.service.ChatService;
import com.sdu.irlab.chatlabelling.utils.ChatLabellingUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import java.util.*;

@RestController
@RequestMapping("/api")
public class RestfulController {

    private ChatService chatService;
    private ConversationDAO conversationDAO;
    private UserDAO userDAO;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    protected AuthenticationManager authenticationManager;

//    @Autowired
//    private BingSearchService bingSearchService;

    @Autowired
    private BaiduSearchService baiduSearchService;

    @Value("${actionFile}")
    private String actionFile;
    private static String actionContent;

    @Value("${instructionFile}")
    private String instructionFile;
    private static String instructionContent;

    @Value("${searchResultConfigFile}")
    private String searchResultConfigFile;
    private static String searchResultConfigContent;

    @Autowired
    private CustomSessionManagement customSessionManagement;

//    @RequestMapping(value = "/currentUser", method = RequestMethod.GET)
//    public String currentUser(@RequestParam(value = "uuid") String uuid,
//                              @RequestParam(value = "user") String user) {
//        return user;
//    }


    @RequestMapping(value = "/instructions", method = RequestMethod.GET)
    public String instructions() {
        return instructionContent;
    }

    @RequestMapping(value = "/addLog", method = RequestMethod.POST, headers = "Accept=application/json")
    public void addLog(@RequestBody String req, @RequestParam(value = "_user") String user) {
        try {
            Map<String, Object> map = objectMapper.readValue(req, Map.class);
            chatService.createOperationLog(user, map.get("type").toString(), map.get("content").toString(), map.get("conversationId").toString());
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    @RequestMapping(value = "/saveProfileAndLogin", method = RequestMethod.POST, headers = "Accept=application/json")
    public void saveProfileAndLogin(HttpServletRequest request, @RequestBody String req) {
        User u = new User();
        userDAO.saveAndFlush(u);//先保存一次，获取用户id
        String nameAndPass = "user-" + u.getId();
        u.setName(nameAndPass);
        u.setPassword(nameAndPass);
        u.setProfile(req);
        userDAO.saveAndFlush(u);
        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(u, nameAndPass, u.getAuthorities());
        token.setDetails(new WebAuthenticationDetails(request));
        Authentication authenticatedUser = authenticationManager.authenticate(token);
        SecurityContextHolder.getContext().setAuthentication(authenticatedUser);
        request.getSession().setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, SecurityContextHolder.getContext());
    }

    /**
     * @throws JsonProcessingException
     */
    @RequestMapping(value = "/checkProfile", method = RequestMethod.GET)
    public boolean checkProfile(@RequestParam(value = "_user") String user) {
        User u = userDAO.findByName(user);
        return u.getProfile() == null || u.getProfile().length() == 0;
    }

    @RequestMapping(value = "/saveProfile", method = RequestMethod.POST, headers = "Accept=application/json")
    public void saveProfile(@RequestBody String req, @RequestParam(value = "_user") String user) {
        User u = userDAO.findByName(user);
        u.setProfile(req);
        userDAO.saveAndFlush(u);
    }

    @RequestMapping(value = "/saveRating", method = RequestMethod.POST, headers = "Accept=application/json")
    public void saveRating(@RequestBody String req) throws JsonProcessingException {
        JsonNode node = objectMapper.readTree(req);
        String role = node.get("role").asText();
        long id = node.get("conversationId").asLong();
        Conversation conversation = conversationDAO.getOne(id);
        if (conversation == null) return;
        if (role.equals("sys")) {
            conversation.setSysRate(req);
        } else if (role.equals("cus")) {
            conversation.setCusRate(req);
        }
    }

//    @RequestMapping(value = "/loadSearch", method = RequestMethod.POST, headers = "Accept=application/json", produces = "application/json;charset=UTF-8")
//    public String loadSearch(Principal principal, @RequestBody String req) {
//        try {
//            Map<String, Object> map = objectMapper.readValue(req, Map.class);
//            List<String> states = (List<String>) map.get("states");
//            String results = baiduSearchService.searchStates(states);
//            chatService.createSearchLog(principal.getName(), objectMapper.writeValueAsString(states), results, map.get("conversationId").toString());
//            return results;
//        } catch (JsonProcessingException e) {
//            e.printStackTrace();
//            return "";
//        }
//    }

    /**
     * 解析搜索结果，真正搜索过程放在了客户端，服务器只保存搜索后解析好的果
     *
     * @param req
     */
    @RequestMapping(value = "/saveSearchResults", method = RequestMethod.POST, headers = "Accept=application/json", produces = "application/json;charset=UTF-8")
    public void saveSearchResults(@RequestBody String req, @RequestParam(value = "_user") String user) {
        try {
            JsonNode node = objectMapper.readTree(req);
            chatService.createSearchLog(user, node.get("query").asText(), node.get("data").toString(), node.get("conversationId").asText());
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }


    @RequestMapping(value = "/loadCurrentState", method = RequestMethod.GET, produces = "application/json;charset=UTF-8")
    public String loadCurrentState(@RequestParam(value = "_user") String user) {
        Conversation conversation = chatService.getCurrentConversation(user);
        return conversation != null ? conversation.getState() : "[]";
    }

    @RequestMapping(value = "/loadSearchResultConfig", method = RequestMethod.GET, produces = "application/json;charset=UTF-8")
    public String loadSearchResultConfig() {
        return searchResultConfigContent;
    }

    @RequestMapping(value = "/loadActions", method = RequestMethod.GET, produces = "application/json;charset=UTF-8")
    public String loadActions() {
        return actionContent;
    }

    @RequestMapping(value = "/loadHistoryAndBackground", method = RequestMethod.GET, produces = "application/json;charset=UTF-8", headers = "Accept=application/json")
    public Map<String, Object> loadHistoryAndBackground(
            @RequestParam(name = "sysName") String sysName,
            @RequestParam(name = "cusName") String cusName,
            @RequestParam(name = "conversationId") Long conversationId) {


        int historyLimit = 5;
        Map<String, Object> map = new HashMap<>();
        Conversation conversation = conversationId != null ? conversationDAO.getOne(conversationId) : conversationDAO.findTopByCusUserAndSysUserAndStatusOrderByCreateTimeDesc(
                userDAO.findByName(cusName), userDAO.findByName(sysName), ConversationStatus.ONGOING);
        List<Message> history = chatService.loadHistory(conversation);
        if (history.size() > historyLimit) {
            history = history.subList(0, historyLimit);
        }
        map.put("history", history);
        map.put("background", conversation.getBackground());
        return map;
    }


    @PostConstruct
    private void readActions() {
        if (actionContent != null) return;
        String content = ChatLabellingUtils.readFileAsString(actionFile, "UTF-8");
        actionContent = content;

        if (instructionContent != null) return;
        content = ChatLabellingUtils.readFileAsString(instructionFile, "UTF-8");
        instructionContent = content;

        if (searchResultConfigContent != null) return;
        content = ChatLabellingUtils.readFileAsString(searchResultConfigFile, "UTF-8");
        searchResultConfigContent = content;
    }

    @Autowired
    public void setChatService(ChatService chatService) {
        this.chatService = chatService;
    }

    @Autowired
    public void setConversationDAO(ConversationDAO conversationDAO) {
        this.conversationDAO = conversationDAO;
    }

    @Autowired
    public void setUserDAO(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

}
