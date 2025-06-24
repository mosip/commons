package io.mosip.kernel.idgenerator.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.function.Function;

public class Utility {

    private static final Logger LOGGER = LoggerFactory.getLogger(Utility.class);

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

    public static long getLongProperty(String propertyKey, long defaultValue) {
        return getProperty(propertyKey, defaultValue, Long::parseLong);
    }
}
