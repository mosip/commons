package io.mosip.kernel.websub.api.constants;

/**
 * Enum for error codes in the MOSIP WebSub API.
 * <p>
 * Defines error codes and messages for WebSub operations (e.g., topic registration, subscription,
 * content verification) used by components like {@link io.mosip.kernel.websub.api.client.SubscriberClientImpl},
 * {@link io.mosip.kernel.websub.api.filter.IntentVerificationFilter}, and
 * {@link io.mosip.kernel.websub.api.verifier.AuthenticatedContentVerifier}. Each error includes a
 * unique code (e.g., KER-WSC-101) and a descriptive message for debugging and reporting.
 * </p>
 *
 * @author Urvil Joshi
 * @since 1.0.0
 * @see io.mosip.kernel.websub.api.config.WebSubClientConfig
 */
public enum WebSubClientErrorCode {

	/**
	 * Error during topic registration with the hub.
	 */
	REGISTER_ERROR("KER-WSC-101", "Error occured while registering topic to hub :- "),

	/**
	 * Error during topic unregistration from the hub.
	 */
	UNREGISTER_ERROR("KER-WSC-102", "Error occured while unregistering topic to hub :- "),

	/**
	 * Error during topic publication to the hub.
	 */
	PUBLISH_ERROR("KER-WSC-103", "Error occured while publishing topic to hub :- "),

	/**
	 * Error during topic update notification to the hub.
	 */
	NOTIFY_UPDATE_ERROR("KER-WSC-104", "Error occured while notify update topic to hub :- "),

	/**
	 * Error during subscription to a topic.
	 */
	SUBSCRIBE_ERROR("KER-WSC-105", "Error occured while subscribing at hub :- "),

	/**
	 * Error during unsubscription from a topic.
	 */
	UNSUBSCRIBE_ERROR("KER-WSC-112", "Error occured while unSubscribing at hub :- "),

	/**
	 * Error when the signature header for authenticated content is missing or empty.
	 */
	AUTHENTTICATED_CONTENT_VERIFICATION_HEADER_ERROR("KER-WSC-106", "Error occured while verifing authenticated content :- header for signature is empty or null"),

	/**
	 * Error when the secret parameter in @PreAuthenticateContentAndVerifyIntent is empty.
	 */
	AUTHENTTICATED_CONTENT_ANNOTATION_SECRET_ERROR("KER-WSC-107", "Error occured while verifing authenticated content :- secret parameter for annotation preauthenticatecontent is empty"),

	/**
	 * Error when the content signature does not match.
	 */
	AUTHENTTICATED_CONTENT_ERROR("KER-WSC-108", "Error occured while verifing authenticated content :- content signature is not maching"),

	/**
	 * IO error during WebSub operations.
	 */
	IO_ERROR("KER-WSC-109", "IO error occurred :- "),

	/**
	 * Error when the request is not an HttpServletRequest.
	 */
	INSTANCE_ERROR("KER-WSC-110","Error occured while verifing authenticated content :- Request should be HttpServletRequesttype"),

	/**
	 * Error during input verification for WebSub requests.
	 */
	INPUT_VERIFICATION_ERROR("KER-WSC-111","Error occured while verifingInput :- "),

	/**
	 * Generic internal error for unexpected failures.
	 */
	INTERNAL_ERROR("KER-WSC-999","Internal Error occured:- ")
	;

	/**
	 * The error code (e.g., KER-WSC-101).
	 */
	private final String errorCode;

	/**
	 * The error message describing the issue.
	 */
	private final String errorMessage;

	/**
	 * Constructs an error code with the specified code and message.
	 *
	 * @param errorCode    the error code
	 * @param errorMessage the error message
	 */
	private WebSubClientErrorCode(final String errorCode, final String errorMessage) {
		this.errorCode = errorCode;
		this.errorMessage = errorMessage;
	}

	/**
	 * Gets the error code.
	 *
	 * @return the error code
	 */
	public String getErrorCode() {
		return errorCode;
	}

	/**
	 * Gets the error message.
	 *
	 * @return the error message
	 */
	public String getErrorMessage() {
		return errorMessage;
	}
}