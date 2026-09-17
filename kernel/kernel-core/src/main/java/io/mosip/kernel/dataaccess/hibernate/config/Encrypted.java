package io.mosip.kernel.dataaccess.hibernate.config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a persistent field for encrypt-on-save and decrypt-on-load.
 * <p>
 * {@link EncryptionInterceptor} inspects this annotation when a Hibernate
 * interceptor is configured via {@code hibernate.ejb.interceptor}.
 * </p>
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Encrypted {
}