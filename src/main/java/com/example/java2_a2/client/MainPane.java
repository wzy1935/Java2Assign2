package com.example.java2_a2.client;

import com.example.java2_a2.network.Message;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

import java.util.List;
import java.util.function.Consumer;

public class MainPane extends HBox {
    Controller controller;
    ListView<String> infos;
    private final ObservableList<String> items = FXCollections.observableArrayList();
    private final ObservableList<String> waitingList = FXCollections.observableArrayList();
    private final Text turnText = new Text();
    private final Text timeoutText = new Text("timeout: 5");
    private final TextField chooseTF = new TextField();
    Text[][] chessboardTexts = new Text[3][3];

    public MainPane(Controller controller) {
        setSpacing(10);
        GridPane chessboard = new GridPane();
        VBox infoPane = new VBox();
        setAlignment(Pos.CENTER);
        getChildren().add(chessboard);
        getChildren().add(infoPane);

        // chessboard
        chessboard.setVgap(10);
        chessboard.setHgap(10);
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                chessboardTexts[i][j] = new Text("?");
                chessboardTexts[i][j].setFont(new Font(16));
                int finalI = i;
                int finalJ = j;

                VBox pane = new VBox();
                pane.setPrefHeight(50);
                pane.setPrefWidth(50);
                pane.setAlignment(Pos.CENTER);
                pane.getChildren().add(chessboardTexts[i][j]);
                pane.setBackground(Utils.bg(Color.WHITE));
                pane.setOnMouseClicked(e -> chessBoardClick(finalI, finalJ));
                chessboard.add(pane, j, i);
            }
        }

        // info
        infoPane.setPrefWidth(400);
        infoPane.setSpacing(10);
        infos = new ListView<>();
        infos.setItems(items);
        ListView<String> waitingListView = new ListView<>();
        waitingListView.setItems(waitingList);
        Button joinButton = new Button("Join");
        Button exitButton = new Button("Exit");
        Button restartButton = new Button("Restart");
        Button chooseButton = new Button("play with");
        infoPane.getChildren().addAll(
                turnText,
                new HBox(joinButton, exitButton, restartButton),
                new HBox(new VBox(new Text("infos:"), infos), new VBox(new Text("waiting list:"), waitingListView)),
                timeoutText,
                new HBox(chooseTF, chooseButton)
        );
        joinButton.setOnAction(e -> joinBtnClick());
        exitButton.setOnAction(e -> exitBtnClick());
        restartButton.setOnAction(e -> restartBtnClick());
        chooseButton.setOnAction(e -> chooseBtnClick());

        this.controller = controller;
        Consumer<Message> gameStatusUpdateCallback = this::statusUpdate;
        controller.setGameStatusUpdateCallback(gameStatusUpdateCallback);
        controller.setUpdateInfo(msg -> Platform.runLater(() -> info(msg)));
        controller.setUpdateTimeout(value -> Platform.runLater(() -> {setTimeout(value);}));
    }

    public void chessBoardClick(int i, int j) {
        Utils.async(() -> {
            Message resp = controller.request(new Message("play")
                    .set("i", i)
                    .set("j", j)
            );
            if (resp == null) return;
            Platform.runLater(() -> {
                switch (resp.code()) {
                    case 0 -> {}
                    case 1 -> info("not your turn.");
                    case 2 -> info("invalid step.");

                    default -> info("unknown error.");
                }
            });

        }).start();
    }

    void statusUpdate(Message message) {
        Platform.runLater(() -> {
            List<String> waitingList = (List<String>) message.get("waiting_list");
            String playerX = (String) message.get("player_x");
            String playerO = (String) message.get("player_o");
            int[][] chessboard = (int[][]) message.get("chessboard");
            int turn = (int) message.get("turn");
            String userName = (String) message.get("user_name");
            int timeout = (int) message.get("timeout");

            updateWaitingList(waitingList);
            updateTurnInfo(userName, playerX, playerO, turn);
            updateChessboard(chessboard);
            setTimeout(timeout);
        });
    }

    public void joinBtnClick() {
        Utils.async(() -> {
            Message resp = controller.request(new Message("join"));
            if (resp == null) return;
            Platform.runLater(() -> {
                if (resp.code() == 0) {
                    info("join succeed.");
                } else if (resp.code() == 1) {
                    info("already joined.");
                } else {
                    info("join failed.");
                }
            });

        }).start();
    }

    public void exitBtnClick() {
        Utils.async(() -> {
            Message resp = controller.request(new Message("exit"));
            if (resp == null) return;
            Platform.runLater(() -> {
                switch (resp.code()) {
                    case 0 -> info("exit succeed.");
                    case 1 -> info("already exited");
                    default -> info("exit failed");
                }
            });
        }).start();
    }

    public void restartBtnClick() {
        Utils.async(() -> {
            Message resp = controller.request(new Message("restart"));
            if (resp == null) return;
            Platform.runLater(() -> {
                switch (resp.code()) {
                    case 0 -> info("game restart.");
                    case 1 -> info("not your turn");
                    default -> info("unknown error.");
                }
            });

        }).start();
    }

    public void info(String msg) {
        items.add(msg);
        infos.scrollTo(items.size() - 1);
    }

    public void clearInfo() {
        items.clear();
    }

    public void updateTurnInfo(String you, String x, String o, int turn) {
        turnText.setText("You are: [" + you + "]\n" +
                "X: " + x + (turn  == 1 ? "[MOVE]" : "") + "\n" +
                "O: " + o + (turn  == -1 ? "[MOVE]" : ""));
    }

    public void updateChessboard(int[][] chessboard) {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                int token = chessboard[i][j];
                chessboardTexts[i][j].setText(switch (token) {
                    case 1 -> "X";
                    case -1 -> "O";
                    default -> "";
                });
            }

        }
    }

    public void updateWaitingList(List<String> users) {
        waitingList.clear();
        waitingList.addAll(users);
    }

    public void setTimeout(int timeout) {
        timeoutText.setText("timeout: " + timeout);
    }

    public void chooseBtnClick() {
        String otherUser = chooseTF.getText();
        Utils.async(() -> {
            Message resp = controller.request(new Message("choose").set("user", otherUser));
            if (resp == null) return;
            Platform.runLater(() -> {
                switch (resp.code()) {
                    case 0 -> info("choose succeed.");
                    case 1 -> info("not timeout.");
                    case 2 -> info("invalid opponents.");
                    default -> info("unknown error.");
                }
            });
        }).start();
    }
}
