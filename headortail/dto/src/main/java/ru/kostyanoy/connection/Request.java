package ru.kostyanoy.connection;

public class Request extends Message {

    public Request(String senderName, MessageType type) {
        super(senderName, type);
    }
}
