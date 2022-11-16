package com.example.java2_a2.network;

import java.io.*;
import java.net.Socket;

public class Client {
    Socket server;
    int port;
    Thread connectingThread = null;

    ClientCallback callback = (id, msg) -> {
        System.out.println(id + " " + msg);
    };
    Runnable noServerCallback = () -> {};

    public void setCallback(ClientCallback callback) {
        this.callback = callback;
    }

    public void setNoServerCallback(Runnable noServerCallback) {
        this.noServerCallback = noServerCallback;
    }

    public Client(int port) {
        this.port = port;
    }

    public boolean isConnected() {
        return server != null;
    }

    public void connect(int port) {
        connectingThread = new Thread(() -> {
            while (!connectingThread.isInterrupted()) {
                try {
                    server = new Socket("localhost", port);
                    while (true) {
                        DataInputStream in = new DataInputStream(server.getInputStream());
                        callback.callback(-1, in.readUTF());
                    }
                } catch (IOException e) {
                    noServerCallback.run();
                }
            }
        });
        connectingThread.start();

    }

    public void disconnect() throws IOException {
        connectingThread.interrupt();
        if (server != null) server.close();
        server = null;
    }

    public boolean send(String msg) {
        if (server == null) return false;
        try {
            DataOutputStream out = new DataOutputStream(server.getOutputStream());
            out.writeUTF(msg);
            out.flush();
        } catch (IOException e) {
            System.out.println("server crashed");
        }
        return true;
    }

    public boolean send(String msg, int timeout) {
        while (timeout > 0) {
            try {
                if (send(msg)) return true;
                Thread.sleep(100);
                timeout -= 100;
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        return false;
    }
}
