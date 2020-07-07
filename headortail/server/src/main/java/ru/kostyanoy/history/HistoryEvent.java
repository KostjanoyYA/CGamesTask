package ru.kostyanoy.history;

/**
 * Determines behaviour of classes which can be collected to {@link History} by {@link HistoryTaker}
 */
public interface HistoryEvent extends Cloneable {
    HistoryEvent clone();
}
