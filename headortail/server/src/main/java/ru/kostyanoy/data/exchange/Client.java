package ru.kostyanoy.data.exchange;

import ru.kostyanoy.connection.Connection;
import ru.kostyanoy.game.Player;

/**
 * Server representation of client
 */
public class Client {
    private Connection connection;
    private String nickName;
    private Player player;

    public Client(Connection connection) {
        this.connection = connection;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public Connection getConnection() {
        return connection;
    }

    public String getNickName() {
        return nickName;
    }

    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }
}
