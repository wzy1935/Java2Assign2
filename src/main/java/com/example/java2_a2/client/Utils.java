package com.example.java2_a2.client;

import javafx.application.Platform;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

public class Utils {
    public static Background bg(Color color) {
        return new Background(new BackgroundFill(color, null, null));
    }

    public static Border border(Color color, int weight) {
        return new Border(new BorderStroke(color, null, null, new BorderWidths(weight)));
    }

    public static Service<Integer> async(Runnable task) {
        return new Service<Integer>() {
            @Override
            protected Task<Integer> createTask() {
                return new Task<>() {
                    @Override
                    protected Integer call() throws Exception {
                        task.run();
                        return 0;
                    }
                };
            }
        };
    }
}
