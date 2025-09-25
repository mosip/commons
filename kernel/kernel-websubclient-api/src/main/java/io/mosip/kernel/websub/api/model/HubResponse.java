package io.mosip.kernel.websub.api.model;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Data model for WebSub hub responses.
 * <p>
 * Represents the response from a WebSub hub for operations like subscription or unsubscription,
 * as per RFC 7033. Contains the result (e.g., "accepted", "rejected") and an optional error reason.
 * Parsed by {@link io.mosip.kernel.websub.api.util.ParseUtil} and used by components like
 * {@link io.mosip.kernel.websub.api.client.SubscriberClientImpl}.
 * </p>
 *
 * @author Urvil Joshi
 * @since 1.0.0
 * @see io.mosip.kernel.websub.api.util.ParseUtil
 */
@Data
public class HubResponse {

	/**
	 * The result of the hub operation (e.g., "accepted", "rejected").
	 */
	@NotBlank(message = "hubResult must not be blank")
	private String hubResult;

	/**
	 * Optional reason for operation failure (e.g., "invalid_request").
	 */
	private String errorReason;
}