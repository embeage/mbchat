package com.embeage.mbchat.client.app;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;

public class ChatApplication extends Application {

    // TODO: improve exception handling

    @Override
    public void start(Stage stage) throws IOException {

        stage.setOnCloseRequest(e -> {
            Platform.exit();
            System.exit(0);
        });

        FXMLLoader loader = new FXMLLoader(this.getClass().getResource("connect-view.fxml"));

        // Initialize ConnectController
        VBox connect = loader.load();

        Scene scene = new Scene(connect);

        stage.setTitle("MBChat");
        stage.setScene(scene);
        stage.show();

    }
    public static void main(String[] args) {
        launch(args);
    }
}
