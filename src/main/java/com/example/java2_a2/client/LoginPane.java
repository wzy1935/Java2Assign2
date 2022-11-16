package com.example.java2_a2.client;

import com.example.java2_a2.network.Message;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;

public class LoginPane extends VBox {
    Controller controller;
    TextField accountField = new TextField();
    PasswordField passwordField = new PasswordField();
    Button loginBtn = new Button("Login/Register");
    Text errDisplay = new Text();
    Runnable loginSucceedCallback = () -> {};

    public LoginPane(Controller controller) {
        this.controller = controller;
        setAlignment(Pos.CENTER);
        GridPane grid = new GridPane();
        grid.setMaxWidth(300);
        grid.setHgap(5);
        grid.setVgap(5);
        grid.setBackground(Utils.bg(Color.WHITE));
        getChildren().add(grid);

        grid.add(new Text("Login / Register"), 0, 0);
        grid.add(new Text("account"), 0, 1);
        grid.add(accountField, 1, 1);
        grid.add(new Text("password"), 0, 2);
        grid.add(passwordField, 1, 2);
        grid.add(loginBtn, 0, 3);
        grid.add(errDisplay, 0, 4);

        loginBtn.setOnAction(e -> {
            loginBtnClk();
        });
    }

    void setLoginSucceedCallback(Runnable callback) {
        this.loginSucceedCallback = callback;
    }

    void loginBtnClk() {
        Utils.async(() -> {
            Message resp = controller.login(accountField.getText(), passwordField.getText());
            System.out.println(resp);
            if (resp != null) {
                int code = (Integer) resp.get("code");
                if (code == 0) Platform.runLater(() -> loginSucceedCallback.run());
                if (code == 1) setErrMsg("wrong password.");
                if (code == 2) setErrMsg("already login.");
            } else {
                setErrMsg("login failed.");
            }
        }).start();
    }

    void setErrMsg(String msg) {
        errDisplay.setText(msg);
    }


}
