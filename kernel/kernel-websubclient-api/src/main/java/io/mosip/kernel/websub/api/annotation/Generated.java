package io.mosip.kernel.websub.api.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.annotation.ElementType;
import java.lang.annotation.RetentionPolicy;

/**
 * The {@code Generated} annotation is used to mark classes or methods that should be
 * excluded from code coverage reports, particularly for content that is automatically
 * generated or deprecated, such as failed content pull methods. This annotation helps
 * in suppressing coverage metrics for code that is not intended to be tested directly.
 * <p>
 * This annotation can be applied to both types (classes, interfaces, or enums) and
 * methods. It is retained at runtime and is documented in the generated JavaDoc.
 * </p>
 *
 * @since 1.0.0
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface Generated {
}