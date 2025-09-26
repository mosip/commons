package io.mosip.kernel.websub.api.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Data model for WebSub subscribe and unsubscribe requests.
 * <p>
 * Represents a request for subscription or unsubscription to a WebSub topic, as per RFC 7033.
 * Used by {@link io.mosip.kernel.websub.api.client.SubscriberClientImpl} for hub communication.
 * Includes validation for required fields and compliance with RFC 7033 (e.g., secret length < 200 bytes).
 * </p>
 *
 * @author Urvil Joshi
 * @since 1.0.0
 * @see io.mosip.kernel.websub.api.client.SubscriberClientImpl
 */
@Data
public class SubscriptionChangeRequest {

	/**
	 * URL of the hub's subscribe/unsubscribe endpoint.
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

	/**
	 * OPTIONAL. Number of seconds for which the subscriber would like to have the
	 * subscription active, given as a positive decimal integer. Hubs MAY choose to
	 * respect this value or not, depending on their own policies, and MAY set a
	 * default value if the subscriber omits the parameter. This parameter MAY be
	 * present for unsubscription requests and MUST be ignored by the hub in that
	 * case.
	 */
	private int leaseSeconds;

	/**
	 * REQUIRED. A subscriber-provided cryptographically random unique secret string
	 * that will be used to compute an HMAC digest for authorized content
	 * distribution. If not supplied, the HMAC digest will not be present for
	 * content distribution requests. This parameter SHOULD only be specified when
	 * the request was made over HTTPS <a herf="https://www.w3.org/TR/websub/#bib-RFC2818">[RFC2818]</a>. This parameter MUST be less than
	 * 200 bytes in length.
	 */
	@Size(max = 200, message = "secret must be less than 200 bytes")
	private String secret;
}