package io.mosip.kernel.logger.logback.util;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a method parameter as a Micrometer / metric tag source.
 *
 * @see io.mosip.kernel.core.logger.spi.Logger
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface MetricTag {

    /**
     * Tag name written to the metric.
     *
     * @return tag key
     */
    String value();
    /**
     * Optional expression or bean used to extract the tag value from the argument.
     *
     * @return extractor expression, or empty to use {@code toString()}
     */
    String extractor() default "";
}