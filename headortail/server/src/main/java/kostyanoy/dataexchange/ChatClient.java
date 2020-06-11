package kostyanoy.dataexchange;

import ru.kostyanoy.connection.Connection;

public class ChatClient {
    private Connection connection;
    private String nickName;

    public ChatClient(Connection connection) {
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
}
