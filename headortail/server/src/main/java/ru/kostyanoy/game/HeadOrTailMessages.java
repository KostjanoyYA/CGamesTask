package ru.kostyanoy.game;

public enum HeadOrTailMessages {
    PLAYER_NOT_ALLOWED("is not allowed"),
    BET_REJECTED("stake was rejected");

    private String message;

    HeadOrTailMessages(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
