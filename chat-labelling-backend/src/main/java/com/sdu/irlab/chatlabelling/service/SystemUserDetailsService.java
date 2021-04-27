package com.sdu.irlab.chatlabelling.service;

import com.sdu.irlab.chatlabelling.component.websocket.WebSocket;
import com.sdu.irlab.chatlabelling.datasource.domain.User;
import com.sdu.irlab.chatlabelling.datasource.repository.UserDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.transaction.annotation.Transactional;

public class SystemUserDetailsService implements UserDetailsService {

    @Autowired
    UserDAO userDAO;


    @Override
    @Transactional
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userDAO.findByName(username);
        if (user == null) {
            throw new UsernameNotFoundException("User not exists!");
        }
        if(WebSocket.userConnected(username)){
            throw new BadCredentialsException("User already connected!");
        }
        return user;
    }
}
