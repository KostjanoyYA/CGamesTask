package ru.kostyanoy.entity;

import java.io.Serializable;

public class PlayerState implements Serializable {
    private final long tokenCount;
    private final String nickName;

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
}
