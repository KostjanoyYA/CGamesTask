package kostyanoy.entrypoint;

import kostyanoy.dataexchange.ServerExchanger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.kostyanoy.connection.Connection;
import ru.kostyanoy.connection.Exchanger;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Main {
    private static final Logger log = LoggerFactory.getLogger(ServerExchanger.class);

    public static void main(String[] args) {
        final int defaultPort = 1111;
        final String defaultName = "Server";
        Properties properties = new Properties();
        try {
            try (InputStream propertiesStream = Connection.class.getResourceAsStream("/server.properties")) {
                if (propertiesStream != null) {
                    properties.load(propertiesStream);
                }
            }
        } catch (IOException e) {
            log.warn("{}:\n{}", e.getClass(), e.getMessage());
        }

        int serverPort = (properties.getProperty("server.port") != null)
                ? Integer.valueOf(properties.getProperty("server.port"))
                : defaultPort;

        String serverNickName = properties.getProperty("server.nickName", defaultName);

        log.info("{}: Started", serverNickName);
        log.info("{}: The port {} is used", serverNickName, serverPort);

        Exchanger server = new ServerExchanger(serverNickName, serverPort);
        server.startExchange();
    }
}
