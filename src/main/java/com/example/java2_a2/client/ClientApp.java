package com.example.java2_a2.client;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;


public class ClientApp extends Application {
    Controller controller = new Controller();
    LoginPane loginPane = new LoginPane(controller);
    MainPane mainPane = new MainPane(controller);

    @Override
    public void start(Stage stage) throws Exception {
        stage.setTitle("Tic-tac-toe");
        stage.setScene(new Scene(loginPane, 600, 300));
        loginPane.setLoginSucceedCallback(() -> {
            mainPane.clearInfo();
            stage.setScene(new Scene(mainPane, 600, 300));
        });
        stage.setOnCloseRequest(event -> System.exit(0));
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
