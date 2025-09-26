package io.mosip.kernel.websub.api.model;

import java.util.List;

import lombok.Data;

/**
 * Basic metadata model used in request subscribe and unsubscribe operations.
 * 
 * @author Urvil Joshi
 *
 */
@Data
public class FailedContentResponse {
	
	private List<Failedcontents> failedcontents; 
	
	@Data
	public static class Failedcontents{
		private String message;
		private String timestamp;
	}
}
