package ru.kostyanoy.entity;

import java.io.Serializable;

public class PlayerState implements Serializable {
    private long tokenCount;
    private String nickName;

    public PlayerState(long tokenCount, String nickName) {
        this.tokenCount = tokenCount;
        this.nickName = nickName;
    }

    public long getTokenCount() {
        return tokenCount;
    }

    public String getNickName() {
        return nickName;
    }

    public void setTokenCount(long tokenCount) {
        this.tokenCount = tokenCount;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }
}
