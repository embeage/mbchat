package com.embeage.mbchat.client.chat.channel;

import javafx.fxml.FXML;

import javafx.scene.control.*;
import org.fxmisc.richtext.InlineCssTextArea;
import java.util.List;

/**
 * Interactable element
 */
public class ChannelController {

    @FXML
    private InlineCssTextArea textArea;

    @FXML
    private Label onlineLabel;
    private int online;

    @FXML
    private ListView<String> userList;

    private String latestUser;

    private ChannelText channelText;


    @FXML
    public void initialize() {

        /*
         TODO:
         Add context menu on the user list. Support for DMs.
         Use subpackages.
         Maybe add cell factory to user list.
        */

        channelText = new ChannelText(textArea);
        online = 1;
        onlineLabel.setText("Online: " + online);

    }

    public void setUserList(String[] users) {
        userList.getItems().setAll(users);
        online = users.length;
        onlineLabel.setText("Online: " + online);

    }

    public void addUser(String username) {
        userList.getItems().add(username);
        onlineLabel.setText("Online: " + ++online);
        channelText.addUserJoined(username);
    }

    public void removeUser(String username) {
        userList.getItems().remove(username);
        onlineLabel.setText("Online: " + --online);
        channelText.addUserLeft(username);
    }

    public void disconnectedUser(String username) {
        userList.getItems().remove(username);
        onlineLabel.setText("Online: " + --online);
        channelText.addUserDisconnected(username);
    }

    public void updateUsername(String username, String newUsername) {
        userList.getItems().set(userList.getItems()
                .indexOf(username), newUsername);

        channelText.addUsernameChange(username, newUsername);
    }

    /**
     * Adds a message to the channel.
     */
    public void addSystemMessage(String message) {
        channelText.addSystemMessage(message);
    }

    /**
     * Prints a message in the specified channel from the specified username.
     * Check if latest message was from same user -> have a simple property for that
     */

    /* TODO:
    Fix emoji support via TextParser
    Ie. have all messages go through the parser
    parser looks for emojis (and presents image)
    parser also looks for hyperlink (Url)
     */
    public void addSystemMessage(String message, String username) {

        if (!username.equals(latestUser))
            channelText.addUsername(username);

        channelText.addMessage(message);

        latestUser = username;

    }

}
