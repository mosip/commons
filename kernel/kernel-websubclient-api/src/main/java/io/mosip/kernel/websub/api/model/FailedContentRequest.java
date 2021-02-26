package io.mosip.kernel.websub.api.model;

import lombok.Data;

/**
 * Basic metadata model used in request subscribe and unsubscribe operations.
 * 
 * @author Urvil Joshi
 *
 */
@Data
public class FailedContentRequest {
	
	/**
	 * Url of subscribe endpoint of hub.
	 */
	private String hubURL;
	/**
	 * REQUIRED. The topic URL that the subscriber wishes to subscribe to or
	 * unsubscribe from. Note that this MUST be the "self" URL found during the
	 * discovery step, which may be different from the URL that was used to make the
	 * discovery request.
	 */
	private String topic;
	/**
	 * REQUIRED. The subscriber's callback URL where content distribution
	 * notifications should be delivered. The callback URL SHOULD be an unguessable
	 * URL that is unique per subscription.
	 */
	private String callbackURL;
	/**
	 * REQUIRED. The timestamp after which failed content to be fetched.
	 */
	private String timestamp;
	/**
	 * OPTIONAL. No of failed content to be fetched.
	 */
	private int messageCount;
	/**
	 * REQUIRED. A subscriber-provided cryptographically random unique secret string
	 * that will be used to compute an HMAC digest for authorized content
	 * distribution. If not supplied, the HMAC digest will not be present for
	 * content distribution requests. This parameter SHOULD only be specified when
	 * the request was made over HTTPS <a herf="https://www.w3.org/TR/websub/#bib-RFC2818">[RFC2818]</a>. This parameter MUST be less than
	 * 200 bytes in length.
	 */
	private String secret;
}
