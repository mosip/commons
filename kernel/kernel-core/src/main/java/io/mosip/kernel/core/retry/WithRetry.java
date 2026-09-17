package io.mosip.kernel.core.retry;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a method so {@link RetryAspect} retries it using the configured retry and backoff policy.
 * <p>
 * Contract: must be placed on a Spring bean method. Retry limits and exception
 * lists come from {@link RetryConfigKeyConstants}. Does not change the method
 * signature.
 * </p>
 *
 * @author Loganathan Sekar
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface WithRetry {

}
