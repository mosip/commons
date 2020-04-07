package io.mosip.kernel.fsadapter.proxy.constant;

/**
 * Constants for HDFSAdapter
 * 
 * @author Dharmesh Khandelwal
 * @since 1.0.0
 *
 */
public enum ProxyAdapterErrorCode {
	HDFS_ADAPTER_EXCEPTION("KER-FSA-001", "Exception occured in Proxy Adapter"),

	FILE_NOT_FOUND_EXCEPTION("KER-FSA-002", "Requested file not found");

	private final String errorCode;
	private final String errorMessage;

	private ProxyAdapterErrorCode(final String errorCode, final String errorMessage) {
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
