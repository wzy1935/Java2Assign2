package com.example.java2_a2.network;

import java.io.*;
import java.util.Base64;
import java.util.HashMap;

public class Message extends HashMap<String, Object> implements Serializable {
    public String type;

    public Message(String type) {
        this.type = type;
        put("code", 0);
    }

    public Message(String type, int code) {
        this.type = type;
        put("code", code);
    }

    public static String pack(Message msg) {
        try {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            ObjectOutputStream out = null;
            out = new ObjectOutputStream(buffer);
            out.writeObject(msg);
            return Base64.getEncoder().encodeToString(buffer.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Message set(String key, Object value) {
        put(key, value);
        return this;
    }

    public static Message unpack(String msg) {
        try {
            ByteArrayInputStream buffer = new ByteArrayInputStream(Base64.getDecoder().decode(msg));
            ObjectInputStream in = new ObjectInputStream(buffer);
            return (Message) in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public int code() {
        Object o = get("code");
        if (o instanceof Integer) {
            return (int) o;
        }
        throw new RuntimeException("Invalid code");
    }
}
