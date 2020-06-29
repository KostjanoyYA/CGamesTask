package ru.kostyanoy.entity;

import java.io.Serializable;

public class PlayerState implements Serializable {
    private long tokenCount;

    public PlayerState() {}

    public PlayerState(long tokenCount, String nickName) {
        this.tokenCount = tokenCount;
    }

    public long getTokenCount() {
        return tokenCount;
    }

    public void setTokenCount(long tokenCount) {
        this.tokenCount = tokenCount;
    }

    @Override
    public String toString() {
        return "PlayerState{" +
                "tokenCount=" + tokenCount +
                '}';
    }
}
