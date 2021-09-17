package io.mosip.kernel.auditmanager.util;

import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import io.mosip.kernel.auditmanager.constant.AuditErrorCodes;
import io.mosip.kernel.auditmanager.request.AuditRequestDto;
import io.mosip.kernel.core.auditmanager.exception.AuditManagerException;

/**
 * Utility class for Audit Manager
 * 
 * @author Dharmesh Khandelwal
 * @since 1.0.0
 *
 */

public class AuditUtils {

	private static Validator validator = null;

	static {
		ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		validator = factory.getValidator();
	}

	/**
	 * Private constructor for AuditUtils
	 */
	private AuditUtils() {

	}

	/**
	 * Function to validate {@link AuditRequestDto}
	 * 
	 * @param auditRequest The audit request
	 */
	public static void validateAuditRequest(AuditRequestDto auditRequest) {

		Set<ConstraintViolation<AuditRequestDto>> violations = validator.validate(auditRequest);

		if (!violations.isEmpty()) {
			throw new AuditManagerException(AuditErrorCodes.HANDLEREXCEPTION.getErrorCode(),
					AuditErrorCodes.HANDLEREXCEPTION.getErrorMessage());
		}

	}
}
