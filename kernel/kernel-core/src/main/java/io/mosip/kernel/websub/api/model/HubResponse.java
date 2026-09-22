package io.mosip.kernel.websub.api.model;

import lombok.Data;

/**
 * Hub acknowledgement parsed from a form-encoded hub body ({@code hub.mode} and optional
 * {@code hub.reason}).
 *
 * @author Urvil Joshi
 */
@Data
public class HubResponse {
	
	
	/**
	 * {@code hub.mode} value, typically {@code accepted} or {@code denied}.
	 */
	private String hubResult;
	
	/**
	 * {@code hub.reason} when the hub denied the request; {@code null} if omitted.
	 */
	private String errorReason;

}
