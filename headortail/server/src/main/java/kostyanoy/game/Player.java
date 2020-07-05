package kostyanoy.game;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.kostyanoy.propertyloader.PropertyLoader;

public class Player {
    private long account;
    private static long defaultAccount;
    private static final Logger log = LoggerFactory.getLogger(Player.class);

    public static boolean customizePlayer(String propertyFileName){
        if (propertyFileName == null
                || propertyFileName.isEmpty()
                || !PropertyLoader.load(propertyFileName, Player.class)) {
            log.warn("Cannot load property file '{}' \nDefault configuration has been used", propertyFileName);
            return false;
        }
        else {
            log.info("Property file '{}' has been loaded successfully", propertyFileName);
            defaultAccount = Long.parseLong(PropertyLoader.getPropertyMap().get("tokens"));
        }
        return true;
    }

    public Player(long account) {
        this.account = account;
    }

    public static Player createDefaultPlayer() {
        return new Player(defaultAccount);
    }

    public long getAccount() {
        return account;
    }

    public void setAccount(long account) {
        this.account = account;
    }

    @Override
    public String toString() {
        return "Player{" +
                "account=" + account +
                '}';
    }
}
