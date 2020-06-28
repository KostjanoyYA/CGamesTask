package ru.kostyanoy.connection;

import java.util.List;

public class Response extends Message {

    private Status status;
    private List<String> possibleOptions;

    public Response() {    }

    //TODO Убери конструктор, если он без него работает
//    public Response(String senderName, MessageCategory category, Status status, long tokens) {
//        super(senderName, category, tokens);
//        this.status = status;
//    }

    public void setStatus(Status status) { this.status = status;}

    public Status getStatus() {
        return status;
    }

    public List<String> getPossibleOptions() {
        return possibleOptions;
    }

    public void setPossibleOptions(List<String> possibleOptions) {
        this.possibleOptions = possibleOptions;
    }

    @Override
    public String toString() {
        return "Response{" +
                super.toString() +
                "status=" + status + '\'' +
                "possibleOptions=" + possibleOptions.toString() + '\'' +
                '}';
    }
}
