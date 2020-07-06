package ru.kostyanoy.connection;

import java.util.List;

public class Response extends Message {

    private Status status;
    private List<String> possibleOptions;

    public Response() {    }

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
                "possibleOptions=" + possibleOptions + '\'' +
                '}';
    }
}
