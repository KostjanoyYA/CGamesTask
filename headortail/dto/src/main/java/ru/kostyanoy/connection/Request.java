package ru.kostyanoy.connection;

import com.fasterxml.jackson.annotation.JsonSetter;

public class Request extends Message {

    public Request() {}

    public Request(String senderName, MessageCategory category, long tokens) {
        super(senderName, category, tokens, null);
    }

    public Request(String senderName, MessageCategory category, long tokens, String messageText) {
        super(senderName, category, tokens, messageText);
    }

    @JsonSetter("messageID") //TODO Убрать, если не влияет
    public void setMessageID(String messageID) {
        super.setMessageID(messageID);
    }

    @Override
    public String toString() {
        return "Request{" + super.toString() + "}";
    }
}
