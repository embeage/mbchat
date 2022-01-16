package com.embeage.mbchat.shared;

import java.io.Serializable;
import java.util.ArrayList;

import static com.embeage.mbchat.shared.Utilities.isValidChannelName;
import static com.embeage.mbchat.shared.Utilities.isValidUsername;

/**
 * TODO: validate message method
 */
public class Message implements Serializable {

    private final MessageType type;
    private final String sender;
    private final String textMessage;
    private final String timestamp;
    private final String channel;
    private final String username;
    private final String[] channelUsers;

    public Message(MessageBuilder builder) {
        this.type = builder.type;
        this.sender = builder.sender;
        this.textMessage = builder.textMessage;
        this.timestamp = builder.timestamp;
        this.channel = builder.channel;
        this.username = builder.username;
        this.channelUsers = builder.channelUsers;
    }

    public MessageType getType() {
        return type;
    }

    public String getTextMessage() {
        return textMessage;
    }

    public String getSender() {
        return sender;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public String getChannel() {
        return channel;
    }

    public String getUsername() {
        return username;
    }
    public String[] getChannelUsers() {
        return channelUsers;
    }

    public static class MessageBuilder {

        private final MessageType type;
        private String sender;
        private String textMessage;
        private String timestamp;
        private String channel;
        private String username;
        private String[] channelUsers;

        public MessageBuilder(MessageType type) {
            this.type = type;
        }

        public MessageBuilder textMessage(String textMessage) {
            this.textMessage = textMessage;
            return this;
        }

        public MessageBuilder sender(String sender) {
            this.sender = sender;
            return this;
        }

        public MessageBuilder username(String username) {
            this.username = username;
            return this;
        }

        public MessageBuilder channel(String channel) {
            this.channel = channel;
            return this;
        }

        public MessageBuilder timestamp(String timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public MessageBuilder channelUsers(String[] channelUsers) {
            this.channelUsers = channelUsers;
            return this;
        }

        public Message build() {
            validateMessage();
            return new Message(this);
        }

        public void validateMessage() throws IllegalStateException {
            StringBuilder m = new StringBuilder();
            if (type == null) {
                m.append("Message type must not be null.\n");
            }
            if (sender != null && !isValidUsername(sender)) {
                m.append("Invalid sender username!\n");
            }

            if (username != null && !isValidUsername(username)) {
                m.append("Invalid username!\n");
            }
            if (channel != null && !isValidChannelName(channel)) {
                m.append("Invalid channel name!\n");
            }
            if (m.length() > 0) {
                throw new IllegalStateException(m.toString());
            }
        }


    }


}
