package ru.kostyanoy.connection;

public class Response extends Message {

    private Status status;
    private String messageText;

    public Response() {}

    public Response(String senderName, MessageСategory сategory, Status status, long tokens) {
        super(senderName, сategory, tokens);
        this.status = status;
    }

    public void setMessageID(Request request) { super.setMessageID(request.getMessageID());}

    public void setStatus(Status status) {
        this.status = status;
    }

    public Status getStatus() {
        return status;
    }

    public String getMessageText() {
        return messageText;
    }

    public void setMessageText(String messageText) {
        this.messageText = messageText;
    }

    @Override
    public String toString() {
        return "Response{" +
                super.toString() +
                "status=" + status +
                ", messageText='" + messageText + '\'' +
                '}';
    }
}
