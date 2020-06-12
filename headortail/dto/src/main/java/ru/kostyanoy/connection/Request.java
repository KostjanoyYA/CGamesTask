package ru.kostyanoy.connection;

public class Request extends Message {

    private long tokens;

    public Request(String senderName, MessageType type, long tokens) {
        super(senderName, type, tokens);
    }
}
