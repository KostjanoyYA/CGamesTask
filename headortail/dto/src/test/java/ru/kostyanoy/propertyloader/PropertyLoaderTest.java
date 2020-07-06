package ru.kostyanoy.propertyloader;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullSource;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

class PropertyLoaderTest {

    private static Map<String, String> getFilledHashMap() {
        Map<String, String> map = new HashMap<>();
        map.put("property1", "1");
        map.put("property2", "2");
        return map;
    }

    private static Stream<Object[]> arrayStream() {
        return Stream.of(
                new Object[]{"loaderTest.properties", Optional.of(getFilledHashMap())},
                new Object[]{"notAchievable.properties", Optional.empty()},
                new Object[]{"", Optional.empty()}
        );
    }

    @ParameterizedTest
    @MethodSource("arrayStream")
    void load(String propertyFileName, Optional<Map<String, String>> expectedOptionalMap) {
        Optional<Map<String, String>> actualOptionalMap = PropertyLoader.load(propertyFileName, PropertyLoaderTest.class);
        Assertions.assertEquals(expectedOptionalMap, actualOptionalMap);
    }

    @ParameterizedTest
    @NullSource
    void load(String propertyFileName) {
        Optional<Map<String, String>> actualOptionalMap = PropertyLoader.load(propertyFileName, PropertyLoaderTest.class);
        Assertions.assertEquals(Optional.empty(), actualOptionalMap);
    }
}