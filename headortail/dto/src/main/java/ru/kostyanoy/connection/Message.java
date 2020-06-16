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

    private MessageСategory category;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime sendTime;

    private long tokens;

    public Message() {    }

    public Message(String senderName, MessageСategory category, long tokens) {
        this.messageID = UUIDGenerator.INSTANCE.nextID();
        this.senderName = senderName;
        this.sendTime = LocalDateTime.now();
        this.category = category;
        this.tokens = tokens;
    }

    public String getSenderName() {
        return senderName;
    }

    public LocalDateTime getSendTime() {
        return sendTime;
    }

    public String getMessageID() { return messageID; }

    protected void setMessageID(String messageID) { this.messageID = messageID; }

    public MessageСategory getCategory() { return category; }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public void setCategory(MessageСategory category) {
        this.category = category;
    }

    public void setTokens(long tokens) {
        this.tokens = tokens;
    }

    public long getTokens() { return tokens; }

    @Override
    public String toString() {
        return "Message{" +
                "id='" + messageID + '\'' + //TODO Missing type ID
                ", senderName='" + senderName + '\'' +
                ", category=" + category +
                ", sendTime=" + sendTime +
                ", tokens=" + tokens +
                '}';
    }
}
