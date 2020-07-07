package ru.kostyanoy.history;

/**
 * Determines behaviour of classes, containing {@link History}
 */
public interface HistoryTaker {
    /**
     * gets {@link History} collected by class implemented this interface
     * @return {@link History} of actions determined as {@link HistoryEvent}
     */
    History getHistory();
}
