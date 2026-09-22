package io.mosip.kernel.websub.api.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation indicating for Pre authentication of content notified by hub and
 * verifying intent after subscribe and unsubscribe operation.
 * 
 * @author Urvil Joshi
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface PreAuthenticateContentAndVerifyIntent {

	/**
	 * Subscriber HMAC secret used to verify {@code x-hub-signature} on content
	 * distribution. May be a Spring placeholder such as {@code ${websub.secret}}.
	 *
	 * @return secret or property placeholder
	 */
	String secret();

	/**
	 * Topic this callback is subscribed to. May be a Spring placeholder.
	 *
	 * @return topic URL or property placeholder
	 */
	String topic();

	/**
	 * Callback path registered with the hub (matched by {@code IntentVerificationFilter}).
	 * May be a Spring placeholder.
	 *
	 * @return callback URI or property placeholder
	 */
	String callback();
}
