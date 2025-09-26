package io.mosip.kernel.websub.api.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Data model for requesting failed content from a WebSub hub.
 * <p>
 * Represents a request to fetch failed content for a subscription. Fields align with
 * WebSub protocol (RFC 7033) requirements for subscription and content retrieval, with MOSIP-specific
 * extensions (e.g., pagination, timestamp). Includes validation to ensure compliance with RFC 7033
 * (e.g., secret length < 200 bytes).
 * </p>
 *
 * @author Urvil Joshi
 * @since 1.0.0
 */
@Data
public class FailedContentRequest {
	
	/**
	 * Url of subscribe endpoint of hub.
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
	 * REQUIRED. The timestamp after which failed content to be fetched.
	 */
	@NotBlank(message = "timestamp must not be blank")
	private String timestamp;

	/**
	 * OPTIONAL. Index user in pagination.
	 */
	private int paginationIndex;

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
	@Size(max = 200, message = "secret must be less than 200 bytes")
	private String secret;
}