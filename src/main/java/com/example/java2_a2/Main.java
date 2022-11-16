package com.example.java2_a2;


import com.example.java2_a2.client.ClientApp;
import com.example.java2_a2.network.Client;
import com.example.java2_a2.network.Server;
import com.example.java2_a2.server.ServerApp;

import java.util.Scanner;

import static javafx.application.Application.launch;

public class Main {

    public static void main(String[] args) {
        launch(ClientApp.class, args);
    }

    static class Test1 {
        public static void main(String[] args) throws Exception {
            Server s = new Server(8080);
            s.run();
        }
    }

    static class Test2 {
        public static void main(String[] args) throws Exception {
            Scanner s = new Scanner(System.in);
            Client c = new Client(8081);
            c.connect(8080);
            while (true) {
                String msg = s.nextLine();
//                System.out.println(c.send(msg, 1000));
                if (c.isConnected()) {
                    c.disconnect();
                } else {
                    c.connect(8080);
                }
            }
        }
    }

    static class ServerTest {
        public static void main(String[] args) {
            ServerApp.main(new String[]{});
        }
    }

    static class ClientTest1 {
        public static void main(String[] args) {
            ClientApp.main(new String[]{});
        }
    }

    static class ClientTest2 {
        public static void main(String[] args) {
            ClientApp.main(new String[]{});
        }
    }

    static class ClientTest3 {
        public static void main(String[] args) {
            ClientApp.main(new String[]{});
        }
    }

    static class ClientTest4 {
        public static void main(String[] args) {
            ClientApp.main(new String[]{});
        }
    }
}
