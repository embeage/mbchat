package com.embeage.mbchat.client.app;

import com.embeage.mbchat.client.chat.ChatController;

import com.embeage.mbchat.client.misc.UsernameTakenException;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.embeage.mbchat.shared.Utilities.isValidUsername;

/**
 * ConnectController is the controller class for the connect view.
 * It will start a chat client that will try to connect to a server
 * with the specified values.
 *
 * @author      Martin Björklund
 */

public class ConnectController {

    @FXML
    private MenuItem connectionSettings;

    @FXML
    private TextField usernameField;

    @FXML
    private Label errorLabel;

    @FXML
    private Button connectButton;

    private String host;
    private String port;

    /**
     * Initializes the controller. Automatically invoked by the FXML loader.
     */
    @FXML
    private void initialize() {

        // Default values for host & port number
        host = "shire";
        port = "4444";

        // Pop-up window with settings for host and port number
        connectionSettings.setOnAction(e -> {
            errorLabel.setText("");
            connectionSettingsPopup();
        });

        connectButton.setOnAction(e -> {
            errorLabel.setText("");
            initChat();
        });

        // Pressing ENTER in username field is equal to clicking connect.
        usernameField.setOnAction(e -> {
            connectButton.fire();
        });

        // Clear error messages when typing.
        usernameField.setOnKeyPressed(e -> {
            if (e.getCode() != KeyCode.ENTER)
                errorLabel.setText("");
        });


    }

    private void initChat() {

        try {

            String username = usernameField.getText();

            if (!isValidUsername(username)) {
                errorLabel.setText("Username has to be 1-16 characters long, \n" +
                        "can't contain '#', '<', '>' or ':', and can't \n" +
                        "have leading or trailing spaces.");
                return;
            }

            // Disable while waiting to connect
            connectButton.setDisable(true);

            FXMLLoader loader = new FXMLLoader(
                    ChatController.class.getResource("chat-view.fxml")
            );

            VBox chatWindow = loader.load();
            ChatController chatController = loader.getController();

            // New thread as to not block GUI thread when waiting to
            // handshake with server.
            new Thread(() -> {
                try {
                    chatController.initChatClient(username, host, port);

                    Platform.runLater(() -> {

                        // Switch root node on the current scene instead of switching out the entire scene.
                        Scene scene = usernameField.getScene();
                        scene.setRoot(chatWindow);

                    });
                    // TODO: use exception message for this
                } catch (SocketTimeoutException e) {
                    Platform.runLater(() -> {
                        errorLabel.setText("Could not connect to server. Request \n" +
                                "timed out!");
                        connectButton.setDisable(false);
                    });
                } catch (IOException e) {
                    Platform.runLater(() -> {
                        errorLabel.setText("Could not connect to server. Try \n" +
                                "different connection settings!");
                        connectButton.setDisable(false);
                    });
                } catch (UsernameTakenException e) {
                    Platform.runLater(() -> {
                        errorLabel.setText("That username is taken by another user!");
                        connectButton.setDisable(false);
                    });
                } catch (ClassNotFoundException e) {
                    Platform.runLater(() -> {
                        errorLabel.setText("Oops!");
                        connectButton.setDisable(false);
                    });
                }
            }).start();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * Creates a new stage that acts as a popup.
     */
    private void connectionSettingsPopup() {
        try {

            Stage popup = new Stage();
            popup.setTitle(connectionSettings.getText());
            popup.setResizable(false);
            popup.initModality(Modality.APPLICATION_MODAL);
            popup.initOwner(connectionSettings.getParentPopup()
                    .getScene().getWindow());

            FXMLLoader loader = new FXMLLoader(
                    this.getClass().getResource("settings-view.fxml")
            );
            AnchorPane root = loader.load();

            TextField hostField = (TextField) root.getChildren().get(2);
            TextField portField = (TextField) root.getChildren().get(3);
            Button okButton = (Button) root.getChildren().get(4);

            hostField.setText(host);
            portField.setText(port);

            okButton.setOnAction(event -> {
                host = hostField.getText();
                port = portField.getText();
                popup.close();
            });

            hostField.setOnAction(event -> {
                okButton.fire();
            });

            portField.setOnAction(event -> {
                okButton.fire();
            });

            Scene scene = new Scene(root);
            popup.setScene(scene);
            popup.show();

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }


}
