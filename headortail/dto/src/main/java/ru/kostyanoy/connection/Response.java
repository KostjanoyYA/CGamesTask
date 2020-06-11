package ru.kostyanoy.connection;

public class Response extends Message {

    private Status status;

    public Response(String senderName, MessageType type, Status status) {
        super(senderName, type);
        this.status = status;
    }
}
