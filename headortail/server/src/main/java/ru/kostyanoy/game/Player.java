package ru.kostyanoy.game;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.kostyanoy.propertyloader.PropertyLoader;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Representation of {@link Game} participant
 */
public class Player {
    private long account;
    private static long defaultAccount;
    private static final Logger log = LoggerFactory.getLogger(Player.class);

    /**
     * Loads static properties from the specified property file
     * @param propertyFileName string file name included ".property".
     */
    public static void customizePlayer(String propertyFileName) {
        if (propertyFileName == null
                || propertyFileName.isEmpty()) {
            log.warn("Cannot load property file '{}' \nDefault configuration has been used", propertyFileName);
            return;
        }
        Optional<Map<String, String>> optionalMap = PropertyLoader.load(propertyFileName, Player.class);
        if (optionalMap.isEmpty()) {
            log.warn("Cannot load property file '{}' \nDefault configuration has been used", propertyFileName);
            return;
        }
        Map<String, String> propertyMap = optionalMap.get();
        log.info("Property file '{}' has been loaded successfully", propertyFileName);
        defaultAccount = Long.parseLong(propertyMap.get("tokens"));
    }

    public Player(long account) {
        this.account = account;
    }

    /**
     * Create a {@link Player} with default parameters
     * @return {@link Player}  with default parameter of {@link Player#getAccount()}
     */
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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Player player = (Player) o;
        return account == player.account;
    }

    @Override
    public int hashCode() {
        return Objects.hash(account);
    }

    @Override
    public String toString() {
        return "Player{" +
                "account=" + account +
                '}';
    }
}
