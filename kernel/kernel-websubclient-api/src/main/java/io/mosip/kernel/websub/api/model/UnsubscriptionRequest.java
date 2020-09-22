package io.mosip.kernel.websub.api.model;

import lombok.Data;

/**
 * Basic metadata model used in request unsubscribe operations.
 * 
 * @author Urvil Joshi
 *
 */
@Data
public class UnsubscriptionRequest {
	/**
	 * Url of unsubscribe endpoint of hub.
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
	
}
