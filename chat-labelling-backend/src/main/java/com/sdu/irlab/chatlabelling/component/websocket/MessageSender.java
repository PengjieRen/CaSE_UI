package com.sdu.irlab.chatlabelling.component.websocket;

import com.sdu.irlab.chatlabelling.datasource.domain.WebsocketLog;

public class MessageSender implements Runnable {
    @Override
    public void run() {
        long lastSendTime = 0l;
        String lastSendUser = "";
        long interval = 10;
        while (true) {
            String[] message = new String[2];
            try {
                message = WebSocket.messageQueue.take();
            } catch (InterruptedException e) {
                e.printStackTrace();
                continue;
            }
            for (WebSocket item : WebSocket.clients.values()) {
                if (item.username.equals(message[1])) {
                    if (message[1].equals(lastSendUser) && System.currentTimeMillis() - lastSendTime < interval) {
                        //如果连着给一个用户发了多条消息，这两条消息之间需要有个间隔，避免出现[TEXT_FULL_WRITING]错误
                        try {
                            Thread.sleep(interval);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    try {
                        item.session.getAsyncRemote().sendText(message[0]);
                        lastSendTime = System.currentTimeMillis();
                        lastSendUser = message[1];
                    } catch (Exception e) {
                        System.out.println("Something wrong when sending message to user " + message[1]);
                        System.out.println("Origin message is: " + message[0]);
                        e.printStackTrace();
                    }
                    break;
                }
            }

        }
    }
}
