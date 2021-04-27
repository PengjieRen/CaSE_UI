package com.sdu.irlab.chatlabelling.controller;


import com.sdu.irlab.chatlabelling.datasource.domain.User;
import com.sdu.irlab.chatlabelling.datasource.repository.UserDAO;
import com.sdu.irlab.chatlabelling.security.CustomSessionManagement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class PageController {
    @Autowired
    private UserDAO userDAO;

    @Autowired
    private CustomSessionManagement customSessionManagement;

    @RequestMapping(value = {"/profile"})
    public String profile(@RequestParam(value = "_uuid") String uuid,
                          @RequestParam(value = "_user") String user) {
        if (!customSessionManagement.containsUserAndUUid(user, uuid))
            return "expired";
        return "profile";
    }

    @RequestMapping(value = {"/index"})
    public String index(@RequestParam(value = "_uuid") String uuid,
                        @RequestParam(value = "_user") String user) {
        if (!customSessionManagement.containsUserAndUUid(user, uuid))
            return "expired";
        //connectionCount 会在登录时重置为0，访问index时置为1，每次登录，只能访问index一次
        //如果允许访问多次index，在同一个浏览器登录多个账号时，浏览器会用最新的cookie去访问，导致数据混乱
        User u = userDAO.findByName(user);
        if (u.getConnectionCount() > 0)
            return "expired";
        else {
            u.setConnectionCount(1);
            userDAO.saveAndFlush(u);

            return "index";
        }
    }

    @RequestMapping(value = {"/", "/login"})
    public String login() {
        return "login";
    }

}
