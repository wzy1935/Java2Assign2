package com.example.java2_a2.network;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class Server {
    private static int clientSerial = 0;
    Thread thread = null;
    ServerSocket socket;
    HashMap<Integer, Socket> clients = new HashMap<>();
    ClientCallback callback = (id, msg) -> {};
    Consumer<Set<Integer>> clientUpdateCallback = set -> {};

    public Server(int port) {
        try {
            socket = new ServerSocket(port);
            socket.setSoTimeout(1000);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void setCallback(ClientCallback callback) {
        this.callback = callback;
    }

    public void setClientUpdateCallback(Consumer<Set<Integer>> clientUpdateCallback) {
        this.clientUpdateCallback = clientUpdateCallback;
    }

    public void run() {
        thread = new Thread(() -> {
            while (true) {
                try {
                    Socket client = socket.accept();
                    clients.put(++clientSerial, client);


                    new Thread(() -> {
                        int clientId = clientSerial;
                        try {
                            DataInputStream in = new DataInputStream(client.getInputStream());
                            while (true) {
                                callback.callback(clientId, in.readUTF());
                            }
                        } catch (IOException e) {
                            clients.remove(clientId);
                        }
                    }).start();
                } catch (SocketTimeoutException e) {
                    clientUpdateCallback.accept(clients.keySet());
                    // System.out.println(clients);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

            }
        });
        thread.start();
    }

    public void stop() {
        thread.interrupt();
    }

    public boolean send(int clientId, String msg) {
        Socket client = clients.get(clientId);
        if (client == null) return false;
        try {
            DataOutputStream out = new DataOutputStream(client.getOutputStream());
            out.writeUTF(msg);
            out.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return true;
    }

    public boolean send(int clientId, String msg, int timeout) {
        Socket client = clients.get(clientId);
        if (client == null) return false;

        while (timeout > 0) {
            try {
                if (send(clientId, msg)) return true;
                Thread.sleep(100);
                timeout -= 100;
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        return false;
    }

    public boolean sendAll(String msg, int timeout) {
        Set<Integer> unsended = new HashSet<>(Set.copyOf(clients.keySet()));
        while (timeout > 0) {
            try {
                Set<Integer> sended = new HashSet<>();
                for (Integer clientId : unsended) {
                    if (send(clientId, msg)) sended.add(clientId);
                }
                unsended.removeAll(sended);
                if (unsended.isEmpty()) return true;
                Thread.sleep(100);
                timeout -= 100;
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        return false;
    }

    public Set<Integer> getCidSet() {
        return new HashSet<>(clients.keySet());
    }





}
