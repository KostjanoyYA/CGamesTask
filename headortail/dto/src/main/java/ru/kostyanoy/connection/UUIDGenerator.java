package ru.kostyanoy.connection;

import java.util.UUID;

public enum UUIDGenerator {
    INSTANCE;

    public String nextID() {
        return UUID.randomUUID().toString();
    }
}