package com.embeage.mbchat.shared;

public class Utilities {
    // TODO: specify bunch of constants, max allowed channels to be in etc

    public static final int MIN_USERNAME_LENGTH = 1;
    public static final int MAX_USERNAME_LENGTH = 16;

    /**
     * A username has to be between 1 and 16 characters long.
     * A username can't contain '#', '<', '>' or ':'.
     * A username can't have leading or trailing spaces.
     */
    public static boolean isValidUsername(String username) {

        if (username.length() < MIN_USERNAME_LENGTH || username.length() > MAX_USERNAME_LENGTH
                || username.startsWith(" ") || username.endsWith(" "))
            return false;

        for (int i = 0; i < username.length(); i++) {
            char current = username.charAt(i);
            if (current == '#' || current == ':'
                    || current == '<' || current == '>')
                return false;
        }

        return true;

    }

    /**
     * A channel name has to start with '#'.
     * A channel name consists only of lowercase letters
     * (except leading '#')
     * A channel name can be between 2 and 32 characters long
     */
    public static boolean isValidChannelName(String channelName) {

        if (!channelName.startsWith("#") || channelName.length() < 2
                || channelName.length() > 32)
            return false;

        for (int i = 1; i < channelName.length(); i++) {
            char current = channelName.charAt(i);
            if (current < 97 || current > 122)
                return false;
        }

        return true;

    }


    /**
     * A message has to be less than 4000 characters.
     */
    public static boolean isValidMessage(String message) {

        if (message.length() > 4000)
            return false;

        return true;

    }

}
