package ru.kostyanoy.connection;

public interface Exchanger {
    void startExchange();
    void stopExchange();
    boolean checkConnection();
    String[] getParticipants();
    String[] getIncomingMessages();
    boolean hasCheckedNickName(String nickName);
    boolean sendMessage(Message message);
    boolean sendMessage(String messageText);
    void resetMessages();
}
