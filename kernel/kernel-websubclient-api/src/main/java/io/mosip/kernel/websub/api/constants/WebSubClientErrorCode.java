package io.mosip.kernel.websub.api.constants;

/**
 * This {@link Enum} consist error codes for this api.
 * 
 * @author Urvil Joshi
 *
 */
public enum WebSubClientErrorCode {

	REGISTER_ERROR("KER-WSC-101", "Error occured while registering topic to hub :- "),
	
	UNREGISTER_ERROR("KER-WSC-102", "Error occured while unregistering topic to hub :- "),
	
	PUBLISH_ERROR("KER-WSC-103", "Error occured while publishing topic to hub :- "),
	
	NOTIFY_UPDATE_ERROR("KER-WSC-104", "Error occured while notify update topic to hub :- "),
	
	SUBSCRIBE_ERROR("KER-WSC-105", "Error occured while subscribing at hub :- "),
	
	UNSUBSCRIBE_ERROR("KER-WSC-112", "Error occured while unSubscribing at hub :- "),
	
	AUTHENTTICATED_CONTENT_VERIFICATION_HEADER_ERROR("KER-WSC-106", "Error occured while verifing authenticated content :- header for signature is empty or null"),
	
	AUTHENTTICATED_CONTENT_ANNOTATION_SECRET_ERROR("KER-WSC-107", "Error occured while verifing authenticated content :- secret parameter for annotation preauthenticatecontent is empty"),
	
	AUTHENTTICATED_CONTENT_ERROR("KER-WSC-108", "Error occured while verifing authenticated content :- content signature is not maching"),
	
	IO_ERROR("KER-WSC-109", "IO error occurred :- "),
  
	INSTANCE_ERROR("KER-WSC-110","Error occured while verifing authenticated content :- Request should be HttpServletRequesttype"),
	
	INPUT_VERIFICATION_ERROR("KER-WSC-111","Error occured while verifingInput :- ")
	;
	/**
	 * The error code
	 */
	private final String errorCode;
	/**
	 * The error message
	 */
	private final String errorMessage;

	/**
	 * Constructor to set error code and message
	 * 
	 * @param errorCode    the error code
	 * @param errorMessage the error message
	 */
	private WebSubClientErrorCode(final String errorCode, final String errorMessage) {
		this.errorCode = errorCode;
		this.errorMessage = errorMessage;
	}

	/**
	 * Function to get error code
	 * 
	 * @return {@link #errorCode}
	 */
	public String getErrorCode() {
		return errorCode;
	}

	/**
	 * Function to get the error message
	 * 
	 * @return {@link #errorMessage}r
	 */
	public String getErrorMessage() {
		return errorMessage;
	}

}
