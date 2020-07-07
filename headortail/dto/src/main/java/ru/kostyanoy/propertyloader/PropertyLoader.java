package ru.kostyanoy.propertyloader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Loads property file by file name found it in"resources" directory in the package of Class location.
 */
public class PropertyLoader {

    private static final Logger log = LoggerFactory.getLogger(PropertyLoader.class);

    /**
     * Loads optional map of parameters and its corresponding values
     * @param propertyFileName string file name included ".property"
     * @param customerClass the name of class that is in the package which stores the property file
     * @return {@link Optional}. {@link Optional} is empty for unsuccessful property file loading. Otherwise Optional contains a {@link Map} of properties
     */
    public static Optional<Map<String, String>> load(String propertyFileName, Class customerClass) {

        Properties properties = new Properties();
        try (InputStream propertyStream = customerClass.getClassLoader().getResourceAsStream(propertyFileName)) {
            if (propertyFileName.isEmpty() || propertyStream == null) {
                log.warn("\"Unable to find file '{}'", propertyFileName);
                return Optional.empty();
            } else {
                properties.load(propertyStream);
            }
        } catch (NullPointerException | IOException e) {
            log.warn("{}:\n{}", e.getClass(), e.getMessage());
            return Optional.empty();
        }

        Stream<Entry<Object, Object>> stream = properties.entrySet().stream();
        return Optional.of(stream.collect(Collectors.toMap(
                e -> String.valueOf(e.getKey()),
                e -> String.valueOf(e.getValue()))));
    }
}
