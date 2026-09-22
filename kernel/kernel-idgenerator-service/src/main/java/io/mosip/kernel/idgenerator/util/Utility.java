package io.mosip.kernel.idgenerator.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.function.Function;

/**
 * Reads JVM system properties used to schedule Vert.x pool-init timers.
 */
public class Utility {

    private static final Logger LOGGER = LoggerFactory.getLogger(Utility.class);

    /**
     * Parses a JVM system property with {@code parser}, falling back to {@code defaultValue}.
     *
     * @param <T>          parsed type
     * @param propertyKey  JVM system property name
     * @param defaultValue value used when the property is missing, blank, or unparseable
     * @param parser       converts the trimmed property string
     * @return parsed value or {@code defaultValue}
     */
    public static <T> T getProperty(String propertyKey, T defaultValue, Function<String, T> parser) {
        try {
            String value = System.getProperty(propertyKey);
            if (value == null || value.trim().isEmpty()) {
                LOGGER.info("{} is missing. Using default: {}", propertyKey, defaultValue);
                return defaultValue;
            }
            return parser.apply(value.trim());
        } catch (Exception e) {
            LOGGER.warn("Error reading property {}. Using default: {}", propertyKey, defaultValue, e);
            return defaultValue;
        }
    }

    /**
     * Reads a {@code long} JVM system property.
     *
     * @param propertyKey  JVM system property name
     * @param defaultValue value used when the property is missing, blank, or unparseable
     * @return parsed long or {@code defaultValue}
     */
    public static long getLongProperty(String propertyKey, long defaultValue) {
        return getProperty(propertyKey, defaultValue, Long::parseLong);
    }
}
