package ru.kostyanoy.propertyloader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PropertyLoader {

    private static final Logger log = LoggerFactory.getLogger(PropertyLoader.class);
    private static Map<String, String> PropertiesMap;
// TODO JavaDoc для класса
    public static boolean load(String propertyFileName, Class customerClass) {
        Properties properties = new Properties();
        try {
            try (InputStream propertiesStream = customerClass.getClassLoader().getResourceAsStream(propertyFileName)) {
                if (propertiesStream == null) {
                    log.warn("\"Unable to find {}", propertyFileName);
                }
                else {
                    properties.load(propertiesStream);
                }
            }
        } catch (IOException e) {
            log.warn("{}:\n{}", e.getClass(), e.getMessage());
            return false;
        }

        Stream<Entry<Object, Object>> stream = properties.entrySet().stream();
        PropertiesMap = stream.collect(Collectors.toMap(
                e -> String.valueOf(e.getKey()),
                e -> String.valueOf(e.getValue())));

        return true;
    }

    public static Map<String, String> getPropertiesMap() {
        return PropertiesMap;
    }
}
