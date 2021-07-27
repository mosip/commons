package io.mosip.kernel.websub.api.model;

import lombok.Data;

/**
 * Basic metadata model used in request subscribe and unsubscribe operations.
 * 
 * @author Urvil Joshi
 *
 */
@Data
public class HubResponse {
	
	
	private String hubResult;
	
	private String errorReason;

}
