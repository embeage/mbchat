package com.embeage.mbchat.client.chat;

import com.embeage.mbchat.client.chat.channel.ChannelController;
import com.embeage.mbchat.client.misc.UsernameTakenException;
import com.embeage.mbchat.client.misc.NotificationListener;
import com.embeage.mbchat.shared.Message;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;


import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static com.embeage.mbchat.shared.Utilities.isValidChannelName;
import static com.embeage.mbchat.shared.Utilities.isValidUsername;

/**
 * This class controls reacts to GUI events and listens to incoming notifications on the client
 * and updates the GUI (the view) and the client
 */

public class ChatController implements NotificationListener {

    @FXML
    private TabPane channelPane;

    @FXML
    private TextField chatField;

    private ChatClient chatClient;

    private final Map<String, ChannelController> channels;


    public ChatController() {
        this.channels = new HashMap<>();
    }

    /**
     * Initializes the controller. Automatically invoked by the FXML loader.
     */
    @FXML
    public void initialize() {

        // Listen for when ENTER is pressed in the chat field.
        chatField.setOnAction(e -> {
            try {
                String input = chatField.getText();
                chatField.clear();
                handleInput(input);
            } catch (IOException ex) {
                ex.printStackTrace();
            }

        });

    }

    /**
     * Attempts to start a new chat client which establishes
     * a connection with the server.
     * returns val th
     */
    public void initChatClient(String username, String host, String port) throws IOException,
            UsernameTakenException, ClassNotFoundException {
        this.chatClient = new ChatClient(host, port, this);
        chatClient.requestUsername(username);
        chatClient.start();
        chatClient.joinChannel("#global");
    }

    /**
     * Handle a notification from the chat client.
     */
    @Override
    public void handleNotification(Message msg) {

        /*
        TODO:

        Features:
        Right-clicking username list to DM, or to change own username.

        Fix proper delegation. Ie. some cases should go to channel controller.
        Some cases should go to DM controller.
        Have this ChatController have a map with DM controllers too.
        If message type == toChannel -> delegate to channelcontroller
        If message type == DM -> delegate to DM-controller
        If message type == server wide -> popup for all users
        CHANNEL_JOINED isnt a channel msg, its a..

        In channel, when clicking a user. Should start a DM from there.
        Ie. either communicates back to ChatController which delegates to DM
        or communicates directly with DM-controller
        */


        switch (msg.getType()) {
            case JOIN_OK:
                addChannel(msg.getChannel(), msg.getChannelUsers());
                break;
            case USERNAME_OK:
                channels.values().forEach(channelController ->
                        channelController.updateUsername(msg.getSender(), msg.getUsername())
                );
                break;
            case USERNAME_TAKEN:
                channels.get(getCurrentChannelName()).addSystemMessage(
                        "Username already taken!"
                );
                break;
            case MESSAGE_BROADCAST:
                channels.get(msg.getChannel()).addSystemMessage(msg.getTextMessage(), msg.getSender());
                break;
            case JOIN_BROADCAST:
                channels.get(msg.getChannel()).addUser(msg.getUsername());
                break;
            case LEAVE_BROADCAST:
                channels.get(msg.getChannel()).removeUser(msg.getUsername());
                break;
            case USERNAME_BROADCAST:
                channels.get(msg.getChannel()).updateUsername(msg.getSender(), msg.getUsername());
                break;
            case DISCONNECTED_BROADCAST:
                channels.get(msg.getChannel()).disconnectedUser(msg.getUsername());
                break;
            default:
/*                channels.get(getCurrentChannelName()).addMessage(
                        "Response is of type " + msg.getType().name() + ". " +
                                "Action not yet implemented."
                );*/
                break;
        }

    }

    /**
     * Handles input in the chat field from the user.
     */
    private void handleInput(String input) throws IOException {

        /* TODO
        Invalid commands shouldn't show up in a channel but in a little popup above chatField
        This is also good for when not in a channel at all
        Has nothing to do with channels
         */


        if (input.isEmpty())
            return;

        if (!input.startsWith("/")) {
            sendMessage(input);
            return;
        }

        String[] args = input.split(" ");
        String command = args[0];

        if (command.equals("/join")) try {
            String channelName = args[1];
            if (isValidChannelName(channelName)) {
                if (!chatClient.getChannels().contains(channelName))
                    chatClient.joinChannel(channelName);
                else
                    channels.get(channelName).addSystemMessage(
                            "You are already a member of that channel!"
                    );
            } else
                channels.get(getCurrentChannelName())
                        .addSystemMessage("Invalid channel name!");
        } catch (ArrayIndexOutOfBoundsException e) {
            channels.get(getCurrentChannelName())
                    .addSystemMessage("Need to specify a channel!");
        }

        else if (command.equals("/username")) try {
            String newUsername = args[1];
            if (isValidUsername(newUsername))
                chatClient.changeUsername(newUsername);
            else
                channels.get(getCurrentChannelName())
                        .addSystemMessage("Invalid username!");
        } catch (ArrayIndexOutOfBoundsException e) {
            channels.get(getCurrentChannelName())
                    .addSystemMessage("Need to specify a username!");
        }

        else {
            channels.get(getCurrentChannelName())
                    .addSystemMessage("Invalid command!");

        }

    }

    /**
     * Send a message in the current channel.
     * We don't wait for a response on this one.
     * Can do it though and warn afterwards
     */
    private void sendMessage(String message) throws IOException {
        String channelName = getCurrentChannelName();
        chatClient.sendMessage(message, channelName);
        channels.get(channelName).addSystemMessage(message,
                chatClient.getUsername());
    }


    /**
     * Adds a new channel by creating a new tab with the channel-view.fxml.
     * Associates the channel, chat area and user list nodes with the
     * channel name. Adds all current users on the channels' usernames
     * to the user list.
     */
    private void addChannel(String channelName, String[] usernames) {

        Tab channel = new Tab(channelName);

        FXMLLoader loader = new FXMLLoader(
                ChannelController.class.getResource("channel-view.fxml")
        );

        try {
            HBox channelView = loader.load(); // calls init on channelController

            ChannelController channelController = loader.getController();
            channelController.setUserList(usernames);

            channels.put(channelName, channelController);

            // Leave channel when we close tab
            channel.setOnClosed(e -> {
                try {
                    chatClient.leaveChannel(channelName);
                    channels.remove(channelName);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            });

            // Add the new channel to the channel pane
            // and switch to it.
            channel.setContent(channelView);
            channelPane.getTabs().add(channel);
            channelPane.getSelectionModel().select(channel);


        }

        catch (IOException e) {
            e.printStackTrace();
        }


    }

    /**
     * Returns the name of the channel affiliated with the currently
     * selected tab.
     */
    String getCurrentChannelName() {
        return channelPane.getSelectionModel()
                .getSelectedItem().getText();
    }

}
