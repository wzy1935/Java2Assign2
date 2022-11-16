package com.example.java2_a2.client;

import com.example.java2_a2.network.Client;
import com.example.java2_a2.network.Message;

import java.util.HashMap;
import java.util.function.Consumer;

public class Controller {
    Client client = new Client(0);
    Consumer<Message> gameStatusUpdateCallback = msg -> {};
    Consumer<String> updateInfo = msg -> {};
    Consumer<Integer> updateTimeout = value ->{};
    HashMap<String, Message> messages = new HashMap<>();
    boolean crashed = false;

    public Controller() {
        client.setCallback((id, msgStr) -> {
            crashed = false;
            Message m = Message.unpack(msgStr);
            System.out.println("[recv] " + m.type + ": " + m);
            messages.put(m.type, m);

            if (m.type.equals("game_status")) {
                gameStatusUpdateCallback.accept(m);
            }
            if (m.type.equals("info")) {
                updateInfo.accept((String) m.get("text"));
            }
            if (m.type.equals("timeout")) {
                updateTimeout.accept((int) m.get("value"));
            }
        });
        client.setNoServerCallback(() -> {
            if (!crashed) {
                crashed = true;
                System.out.println("server crashed.");
                updateInfo.accept("server crashed.");
            }
        });
        client.connect(8080);
    }

    Message request(Message message) {
        Message resp = messages.get(message.type);
        client.send(Message.pack(message), 1000);
        int tick = 1000;
        while (tick > 0) {
            if (resp != messages.get(message.type)) {
                return messages.get(message.type);
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            tick -= 100;
        }
        return null;
    }

    public Message login(String user, String password) {
        return request(new Message("login")
                .set("user", user)
                .set("password", password));
    }

    public void setGameStatusUpdateCallback(Consumer<Message> gameStatusUpdateCallback) {
        this.gameStatusUpdateCallback = gameStatusUpdateCallback;
    }

    public void setUpdateInfo(Consumer<String> updateInfo) {
        this.updateInfo = updateInfo;
    }

    public void setUpdateTimeout(Consumer<Integer> updateTimeout) {
        this.updateTimeout = updateTimeout;
    }
}
