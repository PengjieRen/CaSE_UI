package com.sdu.irlab.chatlabelling.security;

import com.sdu.irlab.chatlabelling.security.CustomSessionManagement;
import org.hibernate.annotations.Filter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class SecurityInterceptor extends HandlerInterceptorAdapter {

    @Autowired
    private CustomSessionManagement customSessionManagement;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 验证权限
        if (this.hasPermission(request)) {
            return true;
        }
        // 如果没有权限 则抛403异常 springboot会处理，跳转到 /error/403 页面
        response.sendError(HttpStatus.FORBIDDEN.value(), "User and uuid id  not match!");
        return false;
    }


    /**
     * @param request
     * @return
     */
    private boolean hasPermission(HttpServletRequest request) {
        String user = request.getParameter("_user");
        String uuid = request.getParameter("_uuid");
        return user != null && uuid != null && customSessionManagement.containsUserAndUUid(user, uuid);
    }
}
