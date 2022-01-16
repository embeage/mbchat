package com.embeage.mbchat.server;

import com.embeage.mbchat.shared.Message;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static com.embeage.mbchat.shared.MessageType.*;
import static com.embeage.mbchat.shared.Utilities.isValidChannelName;
import static com.embeage.mbchat.shared.Utilities.isValidUsername;

public class Server {

    // TODO: Add logger

    private static final int PORT_NUMBER = 4444;

    /**
     * Every client is associated with a unique handler.
     * Clients are indexed by their username.
     */
    private static final ConcurrentMap<String, ClientHandler>
            handlerByUsername = new ConcurrentHashMap<>();

    /**
     * All channels open on the server.
     * Key: channel name
     * Value: clients on the channel
     */
    private static final ConcurrentMap<String, Set<ClientHandler>>
            handlersByChannel = new ConcurrentHashMap<>();

    public static void main(String[] args) {

        try (ServerSocket serverSocket = new ServerSocket(PORT_NUMBER)) {
            while (true) {

                // Create a new socket for the connecting client and dedicate
                // a client handler thread to it.
                new ClientHandler(serverSocket.accept()).start();

            }
        } catch (IOException e) {
            System.err.println("Could not listen on port " + PORT_NUMBER);
            System.exit(-1);
        }

    }

    /**
     * A client handler is responsible for a single client.
     */
    private static class ClientHandler extends Thread {

        private final Socket socket;

        private ObjectOutputStream messageOutputStream;
        private ObjectInputStream messageInputStream;

        /**
         * Username associated with this handler's client.
         */
        private String username;

        /**
         * Channels this handler's client is in.
         */
        private List<String> channels;

        private ClientHandler(Socket socket) {
            this.socket = socket;
        }

        public void run() {

            try {

                messageOutputStream = new ObjectOutputStream(
                        socket.getOutputStream());
                messageInputStream = new ObjectInputStream(
                        new BufferedInputStream(socket.getInputStream()));

                tryConnect();

                while (!socket.isClosed()) {

                    Message msg = (Message) messageInputStream.readObject();
                    messageHandler(msg);

                    // Wait until a message is received.
                    //if ((msg = (Message) messageInputStream.readObject()) != null)
                      //  messageHandler(msg);

                }
            } catch (IOException e) {
                // Log different exceptions
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } finally {
                quit();
            }


        }

        private void tryConnect() throws IOException, ClassNotFoundException {

            Message msg = (Message) messageInputStream.readObject();

            // Validate WIP
            if (msg.getType() != CONNECT) {
                socket.close();
                return;
            }

            String requestedUsername = msg.getUsername();

            if (handlerByUsername.putIfAbsent(requestedUsername, this)
                    == null) {
                sendToClient(new Message
                        .MessageBuilder(USERNAME_OK)
                        .build()
                );

                username = requestedUsername;
                channels = new ArrayList<>();

            } else {
                sendToClient(new Message
                        .MessageBuilder(USERNAME_TAKEN)
                        .build()
                );
                socket.close();
            }
        }


        /**
         * Synchronized to avoid data race on socket since this method
         * can be called by other threads that are broadcasting
         */
        private synchronized void sendToClient(Message message) {
            try {
                messageOutputStream.writeObject(message);
            }
            catch (IOException e) {
                e.printStackTrace();
            }

        }

        /**
         * A broadcast is sent to all clients on the provided channel
         * except the client associated with this handler.
         */
        private void broadcast(Message msg) {

            handlersByChannel.get(msg.getChannel()).stream()
                .filter(clientHandler -> clientHandler != this)
                .forEach(clientHandler -> clientHandler.sendToClient(msg));

        }

        private void messageHandler(Message message) throws IOException {

            switch (message.getType()) {
                case MESSAGE:
                    broadcast(new Message
                        .MessageBuilder(MESSAGE_BROADCAST)
                        .textMessage(message.getTextMessage())
                        .sender(username)
                        .channel(message.getChannel())
                        .build()
                    );
                    break;
                case JOIN:
                    joinChannel(message);
                    break;
                case LEAVE:
                    leaveChannel(message);
                    break;
                case USERNAME:
                    changeUsername(message);
                    break;
                default:
                    break;

            }

        }

        /**
         * Tries to join the requested channel, creating a new one if it doesn't exist.
         * Responds to the client.
         */
        private void joinChannel(Message message) {

            final String channel = message.getChannel();

            if (!isValidChannelName(channel)) {
                sendToClient(new Message
                        .MessageBuilder(INVALID_CHANNEL_NAME)
                        .channel(channel)
                        .build()
                );
                return;
            }

            // Create new channel if it doesn't exist. Atomic operation.
            if (handlersByChannel.putIfAbsent(channel, new HashSet<>()) != null) {

                // If channel already exists broadcast join.
                broadcast(new Message
                    .MessageBuilder(JOIN_BROADCAST)
                    .username(username)
                    .channel(channel)
                    .build()
                );

            }
            // Add this client handler to the server wide map
            handlersByChannel.get(channel).add(this);

            // Add the channel to the list of channels this client is in.
            channels.add(channel);

            // Retrieve all usernames on the channel.
            String[] channelUsernames = handlersByChannel.get(channel).stream()
                    .map(ClientHandler::getUsername)
                    .toArray(String[]::new);

            sendToClient(new Message
                    .MessageBuilder(JOIN_OK)
                    .channel(channel)
                    .channelUsers(channelUsernames)
                    .build()
            );

        }

        private void leaveChannel(Message message) {

            String channel = message.getChannel();

            // TODO: fix null pointer from get -> possibly before and send error back

            if (handlersByChannel.get(channel).remove(this)) {

                // Remove from list of channels this client is in.
                channels.remove(channel);

                sendToClient(new Message
                        .MessageBuilder(LEAVE_OK)
                        .channel(channel)
                        .build()
                );

                broadcast(new Message
                    .MessageBuilder(LEAVE_BROADCAST)
                    .username(username)
                    .channel(channel)
                    .build()
                );

            }

        }

        private void changeUsername(Message message) {

            String newUsername = message.getUsername();

            // TODO -> not needed checked by validation
            if (!isValidUsername(newUsername)) {
                sendToClient(new Message
                        .MessageBuilder(INVALID_USERNAME)
                        .username(newUsername)
                        .build()
                );
                return;
            }

            if (handlerByUsername.putIfAbsent(newUsername, this) == null) {

                sendToClient(new Message
                        .MessageBuilder(USERNAME_OK)
                        .username(newUsername)
                        .sender(username)
                        .build()
                );

                // Broadcast change on every channel client is in.
                channels.forEach(channel -> {
                    broadcast(new Message
                        .MessageBuilder(USERNAME_BROADCAST)
                        .username(newUsername)
                        .channel(channel)
                        .sender(username)
                        .build()
                    );
                });

                // Remove old entry.
                handlerByUsername.remove(username, this);
                username = newUsername;

            }
            else
                sendToClient(new Message
                    .MessageBuilder(USERNAME_TAKEN)
                    .build()
                );

        }

        private void quit() {

            // remove from all channels
            channels.forEach(channel -> {

                if (handlersByChannel.get(channel).remove(this)) {
                    broadcast(new Message
                        .MessageBuilder(DISCONNECTED_BROADCAST)
                        .username(username)
                        .channel(channel)
                        .build()
                    );
                }


            });

            // remove association with server
            handlerByUsername.remove(username, this);

            // close resources
            try {
                messageInputStream.close();
                messageOutputStream.close();
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }


        }

        private String getUsername() {
            return username;
        }

    }

}
