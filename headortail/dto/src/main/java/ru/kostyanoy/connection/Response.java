package ru.kostyanoy.connection;

import ru.kostyanoy.entity.PlayerState;

public class Response extends Message {

    private Status status;
    private PlayerState playerState;

    public Response(String senderName, MessageType type, Status status, long tokens) {
        super(senderName, type, tokens);
        this.status = status;
    }

    public void setId(Request request) {
        super.setId(request.getId());
    }
}
