package io.mosip.kernel.websub.api.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The {@code PreAuthenticateContentAndVerifyIntent} annotation is used to mark methods
 * that handle pre-authentication of content notified by a WebSub hub and verify the
 * intent of subscribe and unsubscribe operations. This annotation ensures that the
 * content received from the hub is authenticated and the intent of the operation is
 * validated before processing.
 * <p>
 * This annotation is applicable only to methods and is retained at runtime for
 * runtime processing. It requires three parameters: {@code secret}, {@code topic},
 * and {@code callback}, which are used to configure the authentication and intent
 * verification process.
 * </p>
 *
 * <p><b>Usage Example:</b></p>
 * <pre>
 * {@code
 * @PreAuthenticateContentAndVerifyIntent(
 *     secret = "my-secret-key",
 *     topic = "my-topic",
 *     callback = "http://example.com/callback"
 * )
 * public void handleWebSubNotification() {
 *     // Method implementation
 * }
 * }
 * </pre>
 *
 * @author Urvil Joshi
 * @since 1.0.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface PreAuthenticateContentAndVerifyIntent {

	/**
	 * Specifies the secret key used for authenticating the content notified by the
	 * WebSub hub. This is typically a shared secret between the subscriber and the hub
	 * to validate the authenticity of the notification.
	 *
	 * @return the secret key for authentication
	 */
	String secret();

	/**
	 * Specifies the topic identifier for the WebSub subscription. This indicates the
	 * topic for which the content is being notified or the subscription/unsubscription
	 * operation is performed.
	 *
	 * @return the topic identifier
	 */
	String topic();

	/**
	 * Specifies the callback URL used for WebSub subscription and unsubscription
	 * operations. This URL is used by the hub to send notifications or intent
	 * verification requests.
	 *
	 * @return the callback URL
	 */
	String callback();
}
