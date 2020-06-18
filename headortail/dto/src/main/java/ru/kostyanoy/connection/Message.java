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

    private String messageID;

    private String senderName;

    private MessageCategory category;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime sendTime;

    private long tokens;

    public Message() {
        this.messageID = UUIDGenerator.INSTANCE.nextID();
        this.sendTime = LocalDateTime.now();
    }

    public Message(String senderName, MessageCategory category, long tokens) {
        this.messageID = UUIDGenerator.INSTANCE.nextID();
        this.senderName = senderName;
        this.sendTime = LocalDateTime.now();
        this.category = category;
        this.tokens = tokens;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public LocalDateTime getSendTime() {
        return sendTime;
    }

    public void setSendTime(LocalDateTime sendTime) {
        this.sendTime = sendTime;
    }

    public String getMessageID() { return messageID; }

    public void setMessageID(String messageID) { this.messageID = messageID; }

    public MessageCategory getCategory() { return category; }

    public void setCategory(MessageCategory category) {
        this.category = category;
    }

    public void setTokens(long tokens) {
        this.tokens = tokens;
    }

    public long getTokens() { return tokens; }

    @Override
    public String toString() {
        return "Message{" +
                "messageID='" + messageID + '\'' +
                ", senderName='" + senderName + '\'' +
                ", category=" + category +
                ", sendTime=" + sendTime +
                ", tokens=" + tokens +
                '}';
    }
}
