package ru.kostyanoy.history;

/**
 * Collect history as collection of events
 */
public interface History {
    /**
     * Adds event to the internal storage by associated string
     */
    void addEvent(String nickName, HistoryEvent event);
}
