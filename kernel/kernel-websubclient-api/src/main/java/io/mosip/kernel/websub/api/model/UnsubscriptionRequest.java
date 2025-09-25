package io.mosip.kernel.websub.api.model;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Data model for WebSub unsubscription requests.
 * <p>
 * Represents a request to unsubscribe from a WebSub topic, as per RFC 7033. Used by
 * {@link io.mosip.kernel.websub.api.client.SubscriberClientImpl} for hub communication.
 * A subset of {@link SubscriptionChangeRequest} without leaseSeconds or secret.
 * </p>
 *
 * @author Urvil Joshi
 * @since 1.0.0
 * @see SubscriptionChangeRequest
 */
@Data
public class UnsubscriptionRequest {
	/**
	 * URL of the hub's unsubscribe endpoint.
	 */
	@NotBlank(message = "hubUrl must not be blank")
	private String hubURL;
	/**
	 * REQUIRED. The topic URL that the subscriber wishes to subscribe to or
	 * unsubscribe from. Note that this MUST be the "self" URL found during the
	 * discovery step, which may be different from the URL that was used to make the
	 * discovery request.
	 */
	@NotBlank(message = "topic must not be blank")
	private String topic;
	/**
	 * REQUIRED. The subscriber's callback URL where content distribution
	 * notifications should be delivered. The callback URL SHOULD be an unguessable
	 * URL that is unique per subscription.
	 */
	@NotBlank(message = "callbackURL must not be blank")
	private String callbackURL;
}