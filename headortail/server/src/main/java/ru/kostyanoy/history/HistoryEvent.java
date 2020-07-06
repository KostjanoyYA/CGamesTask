package ru.kostyanoy.history;

public interface HistoryEvent extends Cloneable {
    HistoryEvent clone();
}
