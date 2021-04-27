package com.sdu.irlab.chatlabelling.datasource.domain;

import javax.persistence.Entity;
import javax.persistence.Lob;
@Entity
public class WebsocketLog extends BaseEntity {
    private String sender;
    private String receiver;
    private long conversationId;
    @Lob
    private String content;

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public long getConversationId() {
        return conversationId;
    }

    public void setConversationId(long conversationId) {
        this.conversationId = conversationId;
    }
}
