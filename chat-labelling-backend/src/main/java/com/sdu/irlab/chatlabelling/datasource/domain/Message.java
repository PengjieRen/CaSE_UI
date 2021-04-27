package com.sdu.irlab.chatlabelling.datasource.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sdu.irlab.chatlabelling.common.MessageType;

import javax.persistence.*;
import java.util.List;

@Entity
public class Message extends BaseEntity {

    @JsonIgnore
    @ManyToOne(cascade = {CascadeType.MERGE, CascadeType.REFRESH}, optional = false)
    private User sender;//发出消息者

    @JsonIgnore
    @ManyToOne(cascade = {CascadeType.MERGE, CascadeType.REFRESH}, optional = false)
    private User receiver;//收到消息者

    @JsonIgnore
    @ManyToOne(cascade = {CascadeType.MERGE, CascadeType.REFRESH}, optional = false)
    private Conversation conversation;

    private int turn;
    @Lob
    private String content;

    @Lob
    private String originMessage;

    @Lob
    private String action;


    @Enumerated(EnumType.STRING)//枚举字符串
    private MessageType type;

    private boolean sendAnother;//是否再发一句话

    @JsonIgnore
    @OneToMany(mappedBy = "message", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<OperationLog> operationLogs;

    public User getSender() {
        return sender;
    }

    public void setSender(User sender) {
        this.sender = sender;
    }

    public User getReceiver() {
        return receiver;
    }

    public void setReceiver(User receiver) {
        this.receiver = receiver;
    }

    public Conversation getConversation() {
        return conversation;
    }

    public void setConversation(Conversation conversation) {
        this.conversation = conversation;
    }

    public int getTurn() {
        return turn;
    }

    public void setTurn(int turn) {
        this.turn = turn;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public MessageType getType() {
        return type;
    }

    public void setType(MessageType type) {
        this.type = type;
    }

    public List<OperationLog> getOperationLogs() {
        return operationLogs;
    }

    public void setOperationLogs(List<OperationLog> operationLogs) {
        this.operationLogs = operationLogs;
    }

    public String getOriginMessage() {
        return originMessage;
    }

    public void setOriginMessage(String originMessage) {
        this.originMessage = originMessage;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public boolean isSendAnother() {
        return sendAnother;
    }

    public void setSendAnother(boolean sendAnother) {
        this.sendAnother = sendAnother;
    }
}
