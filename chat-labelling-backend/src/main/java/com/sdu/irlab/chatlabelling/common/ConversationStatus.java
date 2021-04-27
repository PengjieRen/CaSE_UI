package com.sdu.irlab.chatlabelling.common;

public enum ConversationStatus {
    ONGOING("进行中"),
    TERMINATED("异常终止"),
    STOPPED("已结束");
    private String value;
    private ConversationStatus(String value) {
        this.value = value;
    }
}
