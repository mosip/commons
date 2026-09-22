package io.mosip.kernel.core.dataaccess.exception;

import io.mosip.kernel.core.exception.BaseUncheckedException;

/**
 * Unchecked exception thrown when JPA / Hibernate data access fails.
 * <p>
 * Contract: raised by {@link io.mosip.kernel.core.dataaccess.spi.repository.BaseRepository}
 * implementations wrapping persistence-provider exceptions. Callers should
 * treat this as a system error, not a client-input error.
 * </p>
 *
 * @author Dharmesh Khandelwal
 * @since 1.0.0
 */
public class DataAccessLayerException extends BaseUncheckedException {

	/**
	 * Generated serialVersionUID
	 */
	private static final long serialVersionUID = 5074628123959874252L;

	/**
	 * Constructor for DataAccessLayerException
	 * 
	 * @param errorCode    The errorcode
	 * @param errorMessage The errormessage
	 * @param cause        The cause
	 */
	public DataAccessLayerException(String errorCode, String errorMessage, Throwable cause) {
		super(errorCode, errorMessage, cause);
	}
}
