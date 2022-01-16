package com.embeage.mbchat.shared;

public enum MessageType {

    MESSAGE(Group.REQUEST),
    JOIN(Group.REQUEST),
    LEAVE(Group.REQUEST),
    USERNAME(Group.REQUEST),
    CONNECT(Group.REQUEST),

    JOIN_OK(Group.RESPONSE),
    LEAVE_OK(Group.RESPONSE),
    ALREADY_IN_CHANNEL(Group.RESPONSE), // can be part of error msg
    NOT_IN_CHANNEL(Group.RESPONSE), // can be part of error msg
    INVALID_CHANNEL_NAME(Group.RESPONSE), // error msg
    USERNAME_OK(Group.RESPONSE),
    USERNAME_TAKEN(Group.RESPONSE), // error msg
    INVALID_USERNAME(Group.RESPONSE), // error msg
    // Not doing anything with the error responses RN

    MESSAGE_BROADCAST(Group.BROADCAST),
    JOIN_BROADCAST(Group.BROADCAST),
    LEAVE_BROADCAST(Group.BROADCAST),
    USERNAME_BROADCAST(Group.BROADCAST),
    DISCONNECTED_BROADCAST(Group.BROADCAST),
    ERROR(Group.RESPONSE);

    private Group group;

    MessageType(Group group) {
        this.group = group;
    }

    public boolean inGroup(Group group) {
        return this.group == group;
    }

    public enum Group {
        REQUEST,
        RESPONSE,
        BROADCAST;
    }
}
