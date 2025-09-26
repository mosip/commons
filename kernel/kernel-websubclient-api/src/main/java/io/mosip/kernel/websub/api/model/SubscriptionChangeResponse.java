package io.mosip.kernel.websub.api.model;

import lombok.Data;

/**
 * Basic response model for subscribe and unsubscribe operations.
 * 
 * @author Urvil Joshi
 *
 */
@Data
public class SubscriptionChangeResponse {
	/**
	 *  Url passed for subscribe/unsubscribe operation.
	 */
	private String hubURL;
	/**
	 *  topic passed for subscribe/unsubscribe operation.
	 */
	private String topic;
}
