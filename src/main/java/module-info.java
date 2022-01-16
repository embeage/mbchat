module com.embeage.mbchat {

    requires javafx.controls;
    requires javafx.fxml;
    requires org.fxmisc.richtext;

    opens com.embeage.mbchat.client.chat.channel to javafx.fxml;
    exports com.embeage.mbchat.client.chat.channel;

    opens com.embeage.mbchat.client.chat to javafx.fxml;
    exports com.embeage.mbchat.client.chat;

    opens com.embeage.mbchat.client.app to javafx.fxml;
    exports com.embeage.mbchat.client.app;

    exports com.embeage.mbchat.client.misc;

}