package com.example.bijlibachat;

import com.example.bijlibachat.ui.LoginScreen;
import javafx.application.Application;
import javafx.stage.Stage;

public class HelloApplication extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        new LoginScreen().start(stage);
    }

    public static void main(String[] args) {
        launch();
    }
}