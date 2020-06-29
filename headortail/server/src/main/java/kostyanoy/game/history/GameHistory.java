package kostyanoy.game.history;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class GameHistory extends History {
    private final String serverNickName;
    private final Map<String, List<History>> playerHistories;

    public GameHistory(String serverNickName) {
        this.serverNickName = serverNickName;
        playerHistories = new ConcurrentHashMap<>();
    }

    public String getServerNickName() {
        return serverNickName;
    }

    public Map<String, List<History>> getPlayerHistories() {
        return playerHistories;
    }

    @Override
    public void addEvent(String nickName, History event) {
        List<History> newList = !playerHistories.containsKey(nickName)
                ? new ArrayList<>()
                : playerHistories.get(nickName);
        newList.add(event);
        playerHistories.put(nickName, newList);
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

            for (History roundHistory : playerHistories.get(playerNickName)) {
                historyString.append(roundHistory.toString());
                historyString.append("\n");
            }

            historyString.append("\n\n");
        }
        historyString.append("}\n");

        return historyString.toString();
    }
}
