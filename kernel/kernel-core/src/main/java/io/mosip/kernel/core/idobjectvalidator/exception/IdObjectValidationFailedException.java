package io.mosip.kernel.core.idobjectvalidator.exception;

import java.util.List;

import io.mosip.kernel.core.exception.BaseCheckedException;
import io.mosip.kernel.core.exception.ServiceError;
import io.mosip.kernel.core.idobjectvalidator.constant.IdObjectValidatorErrorConstant;

/**
 * Checked exception thrown when identity JSON fails schema validation.
 * <p>
 * Contract: raised by {@link io.mosip.kernel.core.idobjectvalidator.spi.IdObjectValidator}.
 * When constructed with a {@link ServiceError} list, each error is added via
 * {@link #addInfo(String, String)}. Inspect {@link #getCodes()} for field
 * failures.
 * </p>
 *
 * @author Manoj SP
 * @author Swati Raj
 * @since 1.0.0
 */
public class IdObjectValidationFailedException extends BaseCheckedException {

	/**
	 * Generated serialization ID.
	 */
	private static final long serialVersionUID = -3849227719514230853L;

	/**
	 * Constructs the exception with MOSIP error code and message.
	 *
	 * @param errorCode    never-null MOSIP error code
	 * @param errorMessage never-null human-readable description
	 */
	public IdObjectValidationFailedException(String errorCode, String errorMessage) {
		super(errorCode, errorMessage);
	}

	/**
	 * Constructs the exception and copies each {@link ServiceError} onto this
	 * instance.
	 *
	 * @param errorConstant unused historical argument; may be null
	 * @param errors        never-null list of field errors; may be empty
	 */
	public IdObjectValidationFailedException(IdObjectValidatorErrorConstant errorConstant, List<ServiceError> errors) {
		errors.stream().forEach(error -> super.addInfo(error.getErrorCode(), error.getMessage()));
	}

}
