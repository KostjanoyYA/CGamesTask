package ru.kostyanoy.connection;

public class Request extends Message {

    public Request() { //TODO Без этого конструктора ломается сервер, т.к. не может собрать сущность
    }

//    public Request(String messageID) {//TODO Без этого конструктора ломается клиент, т.к. поле messageID не может прочитать, когда мапит json в Message и подтипы.
//        super();
//        this.setMessageID(messageID);
//    }

    public Request(String senderName, MessageCategory category, long tokens) {
        super(senderName, category, tokens);
    }

    @Override
    public String toString() {
        return "Request{" + super.toString() + "}";
    }
}
