package com.embeage.mbchat.client.misc;


import com.embeage.mbchat.shared.Message;

@FunctionalInterface
public interface NotificationListener {
    void handleNotification(Message message);
}