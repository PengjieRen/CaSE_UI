package com.sdu.irlab.chatlabelling.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sdu.irlab.chatlabelling.datasource.domain.User;
import com.sdu.irlab.chatlabelling.datasource.repository.UserDAO;
import org.aspectj.lang.annotation.Around;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

@Component
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {
    private ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private UserDAO userDAO;

    @Autowired
    private CustomSessionManagement customSessionManagement;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Authentication authentication) throws IOException, ServletException {

        User u = userDAO.findByName(authentication.getName());
        u.setConnectionCount(0);
        userDAO.saveAndFlush(u);
        Map<String, Object> map = new HashMap<>();
        map.put("code", 200);
        map.put("msg", "Login succeeded!");
        map.put("profile", u.getProfile());
        map.put("uuid", customSessionManagement.addOrUpdateUser(u.getName()));
        httpServletRequest.getSession().setAttribute("username", authentication.getName());
        httpServletResponse.setContentType("application/json;charset=UTF-8");
        PrintWriter out = httpServletResponse.getWriter();
        out.write(objectMapper.writeValueAsString(map));
        out.flush();
        out.close();
    }
}
