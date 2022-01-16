package com.embeage.mbchat.client.chat.channel;

import org.fxmisc.richtext.InlineCssTextArea;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Responsible for printing out text in a channel.
 */
class ChannelText {

    private final InlineCssTextArea textArea;

    ChannelText(InlineCssTextArea textArea) {
        this.textArea = textArea;
    }

    void addUserJoined(String username) {

        textArea.append(username,
                "-fx-fill: lightgray;" +
                "-fx-font-weight: bold;" +
                "-fx-font-size: 11px;");
        textArea.append(" joined the channel!\n",
                "-fx-fill: derive(#305a85, 50%); " +
                "-fx-font-size: 11 px;");
        textArea.requestFollowCaret();
    }

    void addUserLeft(String username) {
        textArea.append(username,
                "-fx-fill: lightgray;" +
                "-fx-font-weight: bold;" +
                "-fx-font-size: 11px;");
        textArea.append(" left the channel!\n",
                "-fx-fill: derive(#305a85, 50%); " +
                "-fx-font-size: 11 px;");
        textArea.requestFollowCaret();
    }

    void addUserDisconnected(String username) {
        textArea.append(username,
                "-fx-fill: lightgray;" +
                        "-fx-font-weight: bold;" +
                        "-fx-font-size: 11px;");
        textArea.append(" has disconnected!\n",
                "-fx-fill: derive(#305a85, 50%); " +
                        "-fx-font-size: 11 px;");
        textArea.requestFollowCaret();
    }

    void addUsernameChange(String username, String newUsername) {
        textArea.append(username,
                "-fx-fill: lightgray;" +
                "-fx-font-weight: bold;" +
                "-fx-font-size: 11px;");
        textArea.append(" changed their username to ",
                "-fx-fill: derive(#305a85, 50%); " +
                "-fx-font-size: 11px;");
        textArea.append(newUsername,
                "-fx-fill: lightgray; " +
                "-fx-font-weight: bold; " +
                "-fx-font-size: 11px;");
        textArea.append(".\n",
                "-fx-fill: derive(#305a85, 50%); " +
                "-fx-font-size: 11px;");
        textArea.requestFollowCaret();
    }

    void addUsername(String username) {

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm");
        LocalDateTime now = LocalDateTime.now();

        textArea.append(username + " ",
                "-fx-fill: white;" +
                "-fx-font-size: 14px;" +
                "-fx-font-weight: bold;");

        textArea.append(dtf.format(now) + "\n",
                "-fx-fill: lightgray; " +
                "-fx-font-size: 11px;");

        textArea.requestFollowCaret();

    }

    void addMessage(String message) {
        textArea.append("> " + message + "\n",
                "-fx-fill: white;");

        textArea.requestFollowCaret();
    }

    void addSystemMessage(String message) {

        textArea.append(message + "\n",
                "-fx-fill: #b33127;" +
                "-fx-font-size: 11px;");

        textArea.requestFollowCaret();
    }


}
