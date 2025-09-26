package io.mosip.kernel.websub.api.model;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Data model for WebSub hub responses containing failed content.
 * <p>
 * Represents the response from a hub when fetching failed content, as used by
 * {@link io.mosip.kernel.websub.api.client.SubscriberClientImpl}. Contains a list of
 * failed content messages with their timestamps, per RFC 7033 extended functionality. Used in
 * conjunction with {@link FailedContentRequest}.
 * </p>
 *
 * @author Urvil Joshi
 * @since 1.0.0
 * @see FailedContentRequest
 */
@Data
public class FailedContentResponse {

	/**
	 * List of failed content messages with their timestamps.
	 */
	private List<FailedContents> failedContents;

	/**
	 * Nested class representing a single failed content message.
	 */
	@Data
	public static class FailedContents{
		/**
		 * The failed content message.
		 */
		@NotBlank(message = "message must not be blank")
		private String message;

		/**
		 * The timestamp of the failed content (ISO 8601 format recommended).
		 */
		@NotBlank(message = "timestamp must not be blank")
		private String timestamp;
	}
}