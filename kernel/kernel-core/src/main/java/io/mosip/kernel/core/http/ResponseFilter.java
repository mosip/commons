package io.mosip.kernel.core.http;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a controller method so MOSIP HTTP request/response wrapping is applied.
 * <p>
 * Contract: place on REST handler methods. The runtime filter wraps the return
 * value in {@link ResponseWrapper} and unwraps {@link RequestWrapper} bodies.
 * Retention is runtime; target is methods only.
 * </p>
 *
 * @author Bal Vikash Sharma
 * @see RequestWrapper
 * @see ResponseWrapper
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Documented
public @interface ResponseFilter {
}
