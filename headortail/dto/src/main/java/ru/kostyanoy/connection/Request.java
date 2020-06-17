package ru.kostyanoy.connection;

public class Request extends Message {

    public Request() {};

    public Request(String senderName, MessageСategory сategory, long tokens) {
        super(senderName, сategory, tokens);
    }

    @Override
    public String toString() {
        return super.toString();
    }
}
