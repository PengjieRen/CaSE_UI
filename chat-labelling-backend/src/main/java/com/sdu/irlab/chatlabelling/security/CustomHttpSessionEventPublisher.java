package com.sdu.irlab.chatlabelling.security;

import org.springframework.security.web.session.HttpSessionEventPublisher;

import javax.servlet.http.HttpSessionEvent;
import java.util.concurrent.ConcurrentHashMap;

public class CustomHttpSessionEventPublisher extends HttpSessionEventPublisher {

    public void sessionCreated(HttpSessionEvent event) {
        super.sessionCreated(event);
    }

    public void sessionDestroyed(HttpSessionEvent event) {
        super.sessionDestroyed(event);
    }
}
