package ru.kostyanoy.connection;

public interface Exchanger {
    void startExchange();
    void stopExchange();
    void serviceExchange();
    boolean hasCheckedNickName(String nickName);
    boolean sendMessage(Message message);
    void clearUnansweredMessages();
}
