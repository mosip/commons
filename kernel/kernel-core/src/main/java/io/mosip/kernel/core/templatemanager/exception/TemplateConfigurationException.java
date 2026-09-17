package io.mosip.kernel.core.templatemanager.exception;

import io.mosip.kernel.core.exception.BaseUncheckedException;

/**
 * Unchecked exception thrown when {@link io.mosip.kernel.core.templatemanager.spi.TemplateManagerBuilder}
 * cannot configure the template engine.
 * <p>
 * Contract: raised for invalid loader, path, or encoding settings before merge.
 * </p>
 *
 * @author Abhishek Kumar
 * @since 2018-10-9
 * @version 1.0.0
 */
public class TemplateConfigurationException extends BaseUncheckedException {

	private static final long serialVersionUID = -6167648722650250191L;

	/**
	 * Constructs the exception with MOSIP error code and message.
	 *
	 * @param errorCode    never-null MOSIP error code
	 * @param errorMessage never-null human-readable description
	 */
	public TemplateConfigurationException(String errorCode, String errorMessage) {
		super(errorCode, errorMessage);
	}

}
