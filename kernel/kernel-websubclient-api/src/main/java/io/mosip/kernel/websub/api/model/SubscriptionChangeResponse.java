package io.mosip.kernel.websub.api.model;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Data model for responses to WebSub subscribe and unsubscribe operations.
 * <p>
 * Represents the hub response for subscription or unsubscription requests, as per RFC 7033.
 * Captures the hub URL and topic from the request, used by
 * {@link io.mosip.kernel.websub.api.client.SubscriberClientImpl}.
 * </p>
 *
 * @author Urvil Joshi
 * @since 1.0.0
 * @see SubscriptionChangeRequest
 */
@Data
public class SubscriptionChangeResponse {
	/**
	 *  Url passed for subscribe/unsubscribe operation.
	 */
	@NotBlank(message = "hubUrl must not be blank")
	private String hubURL;

	/**
	 *  topic passed for subscribe/unsubscribe operation.
	 */
	@NotBlank(message = "topic must not be blank")
	private String topic;
}