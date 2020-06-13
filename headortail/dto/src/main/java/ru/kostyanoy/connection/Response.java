package ru.kostyanoy.connection;

public class Response extends Message {

    private Status status;

    public Response(String senderName, MessageType type, Status status, long tokens) {
        super(senderName, type, tokens);
        this.status = status;
    }

    public void setId(Request request) {
        super.setId(request.getId());
    }

    public Status getStatus() {
        return status;
    }
}
