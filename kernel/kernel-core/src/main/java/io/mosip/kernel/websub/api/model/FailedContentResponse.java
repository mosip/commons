package io.mosip.kernel.websub.api.model;

import java.util.List;

import lombok.Data;

/**
 * MOSIP hub response for a failed-content pull: messages the subscriber missed.
 *
 * @author Urvil Joshi
 */
@Data
public class FailedContentResponse {
	
	/**
	 * Failed payloads in delivery order as returned by the hub.
	 */
	private List<Failedcontents> failedcontents; 
	
	/**
	 * One missed notification: body and hub timestamp.
	 */
	@Data
	public static class Failedcontents{
		/**
		 * Notification payload the hub failed to deliver earlier.
		 */
		private String message;
		/**
		 * Hub timestamp for this message.
		 */
		private String timestamp;
	}
}
