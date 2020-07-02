package kostyanoy.history;

import java.util.List;

public interface History {
    void addEvent(String nickName, HistoryEvent event);
    List<HistoryEvent> getEvents(String nickName);
}
