package com.sdu.irlab.chatlabelling;

import com.sun.corba.se.impl.orbutil.concurrent.Mutex;

import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

public class TreadSafetyTest {
    public static void main(String[] args) {
        for (int i = 0; i < 10; i++) {
            Thread t = new Thread(new WebsocketClient("t" + i));
            t.start();
        }
    }
}

class WebsocketClient implements Runnable {
    String name;
    WebsocketMock websocketMock;

    WebsocketClient(String name) {
        this.name = name;
        websocketMock = new WebsocketMock(name);
    }

    @Override
    public void run() {
        websocketMock.start();
        try {
            Thread.sleep(1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        websocketMock.stop();
    }
}

class WebsocketMock {
    static int clientNumber = 0;
    static Map<String, WebsocketMock> websocketMockMap = new ConcurrentHashMap<>();
    static Mutex mutex = new Mutex();
    String username;

    WebsocketMock(String username) {
        this.username = username;
    }

    synchronized void start() {
        try {
            mutex.acquire();
            clientNumber++;
            websocketMockMap.put(username, this);
            System.out.println(username + " start, clientNumber=" + clientNumber);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            mutex.release();
        }


    }

    void onMessage() {
        System.out.println(username + " Message , clientNumber=" + clientNumber);
    }

    synchronized void stop() {
        try {
            mutex.acquire();
            clientNumber--;
            websocketMockMap.remove(username);
            System.out.println(username + " stop, clientNumber=" + clientNumber);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            mutex.release();
        }
    }
}