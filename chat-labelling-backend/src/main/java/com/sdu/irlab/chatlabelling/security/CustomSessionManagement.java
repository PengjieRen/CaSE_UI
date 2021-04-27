package com.sdu.irlab.chatlabelling.security;

import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class CustomSessionManagement {
    private static ConcurrentHashMap<String, String> user2UUid = new ConcurrentHashMap<>();

    public String addOrUpdateUser(String user) {
        String uuid = generateUUid();
        user2UUid.put(user, uuid);
        return uuid;
    }

    public void removeUser(String user) {
        user2UUid.remove(user);
    }

    public boolean containsUserAndUUid(String user, String uuid) {
        return user2UUid.containsKey(user) && user2UUid.get(user).equals(uuid);
    }
    public boolean containsUser(String user){
        return user2UUid.containsKey(user);
    }

    private String generateUUid() {
        return UUID.randomUUID().toString();
    }
}
