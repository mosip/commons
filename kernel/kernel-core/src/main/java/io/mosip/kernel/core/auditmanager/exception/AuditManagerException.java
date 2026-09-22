package io.mosip.kernel.core.auditmanager.exception;

import io.mosip.kernel.core.exception.BaseUncheckedException;

/**
 * Unchecked exception thrown when an audit handler fails to write a request.
 * <p>
 * Contract: raised by {@link io.mosip.kernel.core.auditmanager.spi.AuditHandler}
 * implementations on sink, serialization, or configuration failures. Callers
 * typically log and abort the audited operation or retry depending on policy.
 * </p>
 *
 * @see io.mosip.kernel.core.exception.BaseUncheckedException
 * @see io.mosip.kernel.core.auditmanager.spi.AuditHandler
 *
 * @author Dharmesh Khandelwal
 * @since 1.0.0
 */
public class AuditManagerException extends BaseUncheckedException {

	/**
	 * Generated serial version id
	 */
	private static final long serialVersionUID = 8621530697947108810L;

	/**
	 * Constructs an audit-manager exception with MOSIP error code and message.
	 *
	 * @param errorCode    never-null MOSIP error code for this failure
	 * @param errorMessage never-null human-readable description; may be empty
	 */
	public AuditManagerException(String errorCode, String errorMessage) {
		super(errorCode, errorMessage);
	}

}
