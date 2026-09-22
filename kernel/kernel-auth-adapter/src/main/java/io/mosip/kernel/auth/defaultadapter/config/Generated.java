package io.mosip.kernel.auth.defaultadapter.config;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marker annotation used to exclude types and methods from code-coverage
 * metrics.
 * <p>
 * Apply this to deprecated methods, servlet/Vert.x filter entry points, or
 * other standard exclusions that should not affect JaCoCo reports. The
 * annotation has no members; presence of the annotation is the only signal.
 * <p>
 * This adapter is a library other MOSIP services put on the classpath.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface Generated {
}
