package ru.kostyanoy.connection;

import com.fasterxml.jackson.annotation.JsonSetter;

public class Request extends Message {

    public Request() {}

    public Request(String senderName, MessageCategory category, long tokens) {
        super(senderName, category, tokens);
    }

    @JsonSetter("messageID")
    public void setMessageID(String messageID) {
        super.setMessageID(messageID);
    }

    @Override
    public String toString() {
        return "Request{" + super.toString() + "}";
    }
}
