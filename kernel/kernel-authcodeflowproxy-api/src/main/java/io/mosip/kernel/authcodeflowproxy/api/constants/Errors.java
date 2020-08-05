package io.mosip.kernel.authcodeflowproxy.api.constants;

//TODO Use from core when adapter api will merge
public enum Errors {

	/**
	 * Empty Cookie error
	 */
	COOKIE_NOTPRESENT_ERROR("KER-ACP-001", "Cookies are empty"),
	/**
	 * Empty Cookie error
	 */
	TOKEN_NOTPRESENT_ERROR("KER-ACP-002", "Token is not present in cookies"),
	/**
	 * IO Exception
	 */
	IO_EXCEPTION("KER-ACP-003", "IO Exception occured while passing paging request"),
	/**
	 * Cannot connect to auth service
	 */

	CANNOT_CONNECT_TO_AUTH_SERVICE("KER-ACP-004","Problem in connecting to auth service"),
	
	/**
	 * RESPONSE_PARSE_ERROR
	 */
	RESPONSE_PARSE_ERROR("KER-ACP-005", "Error occur while parsing error from response"),
	
	ACESSTOKEN_EXCEPTION("KER-ACP-006", "Error Occured while getting access token from iam"),
	
	STATE_EXCEPTION("KER-ACP-007", "state is not maching");
	


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
	private Errors(final String errorCode, final String errorMessage) {
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
