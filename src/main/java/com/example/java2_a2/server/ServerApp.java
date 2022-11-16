package com.example.java2_a2.server;

import com.example.java2_a2.Game;
import com.example.java2_a2.client.MainPane;
import com.example.java2_a2.network.Message;
import com.example.java2_a2.network.Server;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

public class ServerApp {
    static int MAX_TIMEOUT = 20;
    Server server = new Server(8080);
    HashMap<String, String> users = new HashMap<>();
    HashMap<Integer, String> usersOnline = new HashMap<>();
    ArrayList<String> waitingList = new ArrayList<>();
    String playerX = null;
    String playerO = null;
    Game game = new Game();
    int timeout = MAX_TIMEOUT;

    void registerCallbacks() {
        server.setCallback((id, msg) -> {
            Message message = Message.unpack(msg);
            System.out.println("[recv] " + message.type + ": " + message);
            if (!message.type.equals("login") && !usersOnline.containsKey(id)) {
                server.send(id, Message.pack(new Message("info")
                        .set("text", "unknown user. please restart.")));
                return;
            }

            switch (message.type) {
                case "login" -> {
                    String userName = (String) message.get("user");
                    String password = (String) message.get("password");
                    users.putIfAbsent(userName, password);

                    if (password.equals(users.get(userName))) {
                        if (usersOnline.values().contains(userName)) {
                            server.send(id, Message.pack(
                                    new Message("login", 2)
                            ));
                        } else {
                            usersOnline.put(id, userName);
                            server.send(id, Message.pack(
                                    new Message("login", 0)
                            ));
                        }
                        sendAllGameStatus();
                    } else {
                        server.send(id, Message.pack(
                                new Message("login", 1)
                        ));
                    }
                }
                case "join" -> {
                    String userName = usersOnline.get(id);
                    if (userName.equals(playerO) || userName.equals(playerX)|| waitingList.contains(userName)) {
                        server.send(id, Message.pack(new Message("join", 1)));
                    } else {
                        if (playerO == null) {
                            playerO = userName;
                        } else if (playerX == null) {
                            playerX = userName;
                        } else {
                            waitingList.add(userName);
                        }
                        server.send(id, Message.pack(new Message("join", 0)));
                        sendAllGameStatus();
                    }
                }
                case "exit" -> {
                    String userName = usersOnline.get(id);
                    if (!(userName.equals(playerO) || userName.equals(playerX)|| waitingList.contains(userName))) {
                        server.send(id, Message.pack(new Message("exit", 1)));
                    } else {
                        if (userName.equals(playerO)) {
                            playerO = null;
                            if (!waitingList.isEmpty()) playerO = waitingList.remove(0);
                        } else if (userName.equals(playerX)) {
                            playerX = null;
                            if (!waitingList.isEmpty()) playerX = waitingList.remove(0);
                        } else {
                            waitingList.remove(userName);
                        }
                        game.restart();
                        server.send(id, Message.pack(new Message("exit", 0)));
                        sendAllGameStatus();
                    }
                }
                case "restart" -> {
                    timeout = MAX_TIMEOUT;
                    String userName = usersOnline.get(id);
                    if (userName.equals(playerX) || userName.equals(playerO)) {
                        game.restart();
                        server.send(id, Message.pack(new Message("restart", 0)));
                        sendAllGameStatus();
                    } else {
                        server.send(id, Message.pack(new Message("restart", 1)));
                    }
                }
                case "play" -> {
                    String userName = usersOnline.get(id);
                    int i = (int) message.get("i");
                    int j = (int) message.get("j");
                    String successMsg = (game.getTurn() == 1 ? "X" : "O") + " play at " + i + "," + j;
                    boolean success = false;
                    if (userName.equals(playerX) && game.getTurn() == 1) {
                        success = game.play(i, j, 1);
                    } else if (userName.equals(playerO) && game.getTurn() == -1) {
                        success = game.play(i, j, -1);
                    } else {
                        server.send(id, Message.pack(new Message("play", 1)));
                    }
                    if (success) {
                        timeout = MAX_TIMEOUT;
                        server.send(id, Message.pack(new Message("play", 0)));
                        usersOnline.keySet().forEach(cid -> {
                            server.send(cid, Message.pack(new Message("info")
                                    .set("text", successMsg)));
                            if (game.winCheck() == 1 || game.winCheck() == -1) {
                                server.send(cid, Message.pack(new Message("info")
                                        .set("text", (game.winCheck() == 1 ? "X" : "O") + " wins.")));
                            } else if (game.winCheck() == 2) {
                                server.send(cid, Message.pack(new Message("info")
                                        .set("text", "It's a tie.")));
                            }
                        });
                        sendAllGameStatus();
                    } else {
                        server.send(id, Message.pack(new Message("play", 2)));
                    }
                }
                case "choose" -> {
                    String userName = usersOnline.get(id);
                    String anotherName = (String) message.get("user");
                    if (timeout > 0) {
                        server.send(id, Message.pack(new Message("choose", 1)));
                    } else if (userName.equals(anotherName) ||
                            !(waitingList.contains(anotherName)
                                    || anotherName.equals(playerO)
                                    || anotherName.equals(playerX))) {
                        server.send(id, Message.pack(new Message("choose", 2)));
                    } else {
                        timeout = MAX_TIMEOUT;
                        waitingList.remove(userName);
                        waitingList.remove(anotherName);
                        playerO = userName;
                        playerX = anotherName;
                        game.restart();
                        server.send(id, Message.pack(new Message("choose", 0)));
                        sendAllGameStatus();
                    }
                }
            }
        });
    }

    Message generateGameStatusPacket() {
        return new Message("game_status")
                .set("waiting_list", waitingList)
                .set("player_x", playerX)
                .set("player_o", playerO)
                .set("chessboard", game.getChessboard())
                .set("timeout", timeout)
                .set("turn", game.getTurn());
    }

    public void sendAllGameStatus() {
        usersOnline.keySet().forEach(cid -> {
            server.send(cid, Message.pack(
                    generateGameStatusPacket().set("user_name", usersOnline.get(cid))));
        });
    }

    public ServerApp() {
        server.setClientUpdateCallback(userSet -> {
            if (!userSet.containsAll(usersOnline.keySet())) {
                usersOnline.keySet().retainAll(userSet);
                sendAllGameStatus();
            }
        });
        server.run();
        registerCallbacks();

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                if (timeout > 0) timeout--;
                server.sendAll(Message.pack(new Message("timeout").set("value", timeout)),1000);
            }
        },0, 1000);
    }

    public static void main(String[] args) {
        new ServerApp();
    }
}
