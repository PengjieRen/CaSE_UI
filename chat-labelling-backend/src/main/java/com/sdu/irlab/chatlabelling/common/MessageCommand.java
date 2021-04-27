package com.sdu.irlab.chatlabelling.common;

/**
 * 系统给前端发出的命令，用于指导前端做不同的渲染
 */
public enum MessageCommand {
    FINISH("对话结束"),
    HURRYUP("催一下"),
    WAIT4PARTNER("等待聊天对象"),
    WAIT4MESSAGE("等待对方消息"),
    SENDMESSAGE("发出一条消息"),
    STATUS("对话状态"),
    ERROR("发生错误"),
    STOP("聊天终止"),
    START("聊天开始"),
    OTHER("其他");
    private String value;

    private MessageCommand(String value) {
        this.value = value;
    }
}
