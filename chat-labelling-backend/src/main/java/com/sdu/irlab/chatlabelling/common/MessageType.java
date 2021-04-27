package com.sdu.irlab.chatlabelling.common;

/**
 * 消息类型，来自谁，发给谁
 */
public enum MessageType {
    SYS2CUS("系统发出"),
    CUS2SYS("客户发出"),
    ERROR("发生错误"),
    HURRYUP("催一下"),
    FINISH("对话结束"),
    OTHER("其他");
    private String value;

    private MessageType(String value) {
        this.value = value;
    }

}
