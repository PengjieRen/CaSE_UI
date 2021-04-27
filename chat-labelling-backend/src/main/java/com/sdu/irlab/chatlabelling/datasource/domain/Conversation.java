package com.sdu.irlab.chatlabelling.datasource.domain;

import com.sdu.irlab.chatlabelling.common.ConversationStatus;

import javax.persistence.*;
import java.util.List;

@Entity
public class Conversation extends BaseEntity {
    @Enumerated(EnumType.STRING)//枚举字符串
    private ConversationStatus status;

    @ManyToOne(cascade = {CascadeType.MERGE, CascadeType.REFRESH}, optional = false)
    private User sysUser;//扮演系统

    @ManyToOne(cascade = {CascadeType.MERGE, CascadeType.REFRESH}, optional = false)
    private User cusUser;//扮演客户

    @OneToMany(mappedBy = "conversation", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Message> messages;
    private int currentTurn;
    private String nextSpeaker;//下一个要说话的人

    @Lob
    private String background;
    @Lob
    private String state;

    @Lob
    private String sysRate;

    @Lob
    private String cusRate;


    public ConversationStatus getStatus() {
        return status;
    }

    public void setStatus(ConversationStatus status) {
        this.status = status;
    }

    public User getSysUser() {
        return sysUser;
    }

    public void setSysUser(User sysUser) {
        this.sysUser = sysUser;
    }

    public User getCusUser() {
        return cusUser;
    }

    public void setCusUser(User cusUser) {
        this.cusUser = cusUser;
    }

    public List<Message> getMessages() {
        return messages;
    }

    public void setMessages(List<Message> messages) {
        this.messages = messages;
    }

    public int getCurrentTurn() {
        return currentTurn;
    }

    public void setCurrentTurn(int currentTurn) {
        this.currentTurn = currentTurn;
    }

    public String getBackground() {
        return background;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public void setBackground(String background) {
        this.background = background;
    }

    public String getSysRate() {
        return sysRate;
    }

    public void setSysRate(String sysRate) {
        this.sysRate = sysRate;
    }

    public String getCusRate() {
        return cusRate;
    }

    public void setCusRate(String cusRate) {
        this.cusRate = cusRate;
    }

    public String getNextSpeaker() {
        return nextSpeaker;
    }

    public void setNextSpeaker(String nextSpeaker) {
        this.nextSpeaker = nextSpeaker;
    }
}
