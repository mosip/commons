package io.mosip.kernel.masterdata.constant;

/**
 * 
 * @author Uday Kumar
 * @author Megha Tanga
 * @since 1.0.0
 *
 */

public enum TemplateTypeErrorCode {
	TEMPLATE_TYPE_INSERT_EXCEPTION("KER-MSD-072", "Error occurred while inserting Template Type details into db"),
	TEMPLATE_TYPE_FETCH_EXCEPTION("KER-MSD-737", "Error occurred while fetching Template Type"),
	TEMPLATE_TYPE_NOT_FOUND_EXCEPTION("KER-MSD-736", "Template Type not Found");

	private final String errorCode;
	private final String errorMessage;

	private TemplateTypeErrorCode(final String errorCode, final String errorMessage) {
		this.errorCode = errorCode;
		this.errorMessage = errorMessage;
	}

	public String getErrorCode() {
		return errorCode;
	}

	public String getErrorMessage() {
		return errorMessage;
	}
}
