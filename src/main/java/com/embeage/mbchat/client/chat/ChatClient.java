package com.embeage.mbchat.client.chat;

import com.embeage.mbchat.client.misc.UsernameTakenException;

import com.embeage.mbchat.client.misc.NotificationListener;
import com.embeage.mbchat.shared.Message;
import javafx.application.Platform;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static com.embeage.mbchat.shared.MessageType.*;

/**
 * Extends thread because we need to read incoming socket messages on separate thread.
 * Messages will be written to socket from the GUI thread.
 */
public class ChatClient extends Thread {

    private final Socket socket;

    private String username;
    private List<String> channels;
    private final NotificationListener listener; // Listens to new updates on the client, notifies controller

    private final ObjectOutputStream outgoingMessages;
    private final ObjectInputStream incomingMessages;

    public ChatClient(String host, String port, NotificationListener listener)
                        throws IOException {

        socket = new Socket(host, Integer.parseInt(port));

        outgoingMessages = new ObjectOutputStream(socket.getOutputStream());
        incomingMessages = new ObjectInputStream(
                new BufferedInputStream(socket.getInputStream())
        );

        this.listener = listener;

    }

    public void requestUsername(String requestedUsername)
            throws UsernameTakenException, ClassNotFoundException, IOException {
        // Ask server if username requested username is OK.
        outgoingMessages.writeObject(new Message
                .MessageBuilder(CONNECT)
                .username(requestedUsername)
                .build()
        );

        // Wait a maximum of 3 seconds for a reply.
        socket.setSoTimeout(3*1000);
        Message msg = (Message) incomingMessages.readObject();

        if (msg.getType() == USERNAME_TAKEN) {
            socket.close();
            throw new UsernameTakenException();
        }

        socket.setSoTimeout(0);

        username = requestedUsername;
        channels = new ArrayList<>();

    }

    /**
     * Reads from server.
     */
    public void run() {

        while (!socket.isClosed()) {
            try {
                Message incoming;
                // Set incoming to message on the input stream
                // readObject method will wait until there is an object in the stream to receive
                if ((incoming = (Message) incomingMessages.readObject()) != null)
                    messageHandler(incoming);

            }

            catch (SocketTimeoutException e) {
                //TODO: static constants
                System.out.println("No message received for 5 sec.");

            }

            catch (SocketException ex) {
                break;
            }

            catch (IOException | ClassNotFoundException e) {
                System.out.println("Connection with server lost.");
                break;
                // TODO: try to reconnect automatically
            }
        }


    }

    /**
     * Client is interested in what changes its state, ie. it knows
     * the channels its in and its username.
     */
    public void messageHandler(Message msg) {

        switch (msg.getType()) {

            case USERNAME_OK:
                username = msg.getUsername();
                break;
            case JOIN_OK:
                channels.add(msg.getChannel());
                break;
            case LEAVE_OK:
                channels.remove(msg.getChannel());
                break;
            default:
                break;

        }

        // Notify controller.
        // GUI updates are scheduled on the JavaFX application thread.
        Platform.runLater(() -> {
            // TODO: have two methods in NotificationListener interface
            // 1. handleNotification, 2. handleMessage
            // 1 is called by GUI stuff such as ChannelController
            listener.handleNotification(msg);
        });


    }

    /**
     * Sends a message to a channel.
     */
    void sendMessage(String message, String channel) throws IOException {

        outgoingMessages.writeObject(new Message
                .MessageBuilder(MESSAGE)
                .textMessage(message)
                .channel(channel)
                .build()
        );
    }

    /**
     * perhaps have the messagequeue only in socketwriter
     * call method to add to it
     */
    void joinChannel(String channel) throws IOException {

        outgoingMessages.writeObject(new Message
                .MessageBuilder(JOIN)
                .channel(channel)
                .build()
        );

    }

    void leaveChannel(String channel) throws IOException {

        outgoingMessages.writeObject(new Message
                .MessageBuilder(LEAVE)
                .channel(channel)
                .build()
        );

    }

    void changeUsername(String username) throws IOException {

        outgoingMessages.writeObject(new Message
                .MessageBuilder(USERNAME)
                .username(username)
                .build()
        );

    }

    String getUsername() {
        return username;
    }

    List<String> getChannels() {
        return channels;
    }

}
