package ru.kostyanoy.connection;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.io.Serializable;
import java.time.LocalDateTime;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = Response.class, name = "response"),
        @JsonSubTypes.Type(value = Request.class, name = "request")
})
public class Message implements Serializable {

    private String id;

    private String senderName;

    private MessageType type;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime sendTime;

    private long tokens;

    //Message() {    }
    //TODO Возможно, конструктор нужен для сериализации. Удали, если не нужен

    Message(String senderName, MessageType type, long tokens) {
        this.id = UUIDGenerator.INSTANCE.nextID();
        this.senderName = senderName;
        this.sendTime = LocalDateTime.now();
        this.type = type;
        this.tokens = tokens;
    }

    public String getSenderName() {
        return senderName;
    }

    public LocalDateTime getSendTime() {
        return sendTime;
    }

    public String getId() { return id; }

    protected void setId(String id) { this.id = id; }

    public MessageType getType() { return type; }

    public long getTokens() { return tokens; }
}
