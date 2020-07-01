package kostyanoy.game.history;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class GameHistory implements History {
    private final String serverNickName;
    private final Map<String, List<HistoryEvent>> playerHistories;

    public GameHistory(String serverNickName) {
        this.serverNickName = serverNickName;
        playerHistories = new ConcurrentHashMap<>();
    }

    @Override
    public void addEvent(String nickName, HistoryEvent event) {
        List<HistoryEvent> newList = !playerHistories.containsKey(nickName)
                ? new ArrayList<>()
                : playerHistories.get(nickName);
        newList.add(event.clone());
        playerHistories.put(nickName, newList);
    }

    @Override
    public List<HistoryEvent> getEvents(String nickName) {
        return playerHistories.get(nickName);
    }

    @Override
    public String toString() {
        StringBuilder historyString = new StringBuilder("GameHistory{serverNickName=");
        historyString.append(serverNickName);
        historyString.append(",\n");
        for (String playerNickName : playerHistories.keySet()) {
            historyString.append("playerNickName=");
            historyString.append(playerNickName);
            historyString.append(":\n");

            for (HistoryEvent event : playerHistories.get(playerNickName)) {
                historyString.append(event.toString());
                historyString.append("\n");
            }
        }
        historyString.append("}\n\n");

        return historyString.toString();
    }
}
