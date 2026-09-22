package io.mosip.kernel.core.saltgenerator.constant;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * MOSIP error codes for the kernel salt-generator job.
 * <p>
 * Contract: codes follow {@code KER-SGR-nnn}. Used by
 * {@link io.mosip.kernel.core.saltgenerator.exception.SaltGeneratorException}.
 * Does not perform I/O.
 * </p>
 *
 * @author Manoj SP
 */
public enum SaltGeneratorErrorConstants {

	/**
	 * Target salt rows already exist in the database.
	 */
	RECORD_EXISTS("KER-SGR-001", "Record(s) already exists in DB"),

	/**
	 * Salt population job failed for a reason other than existing rows.
	 */
	JOB_FAILED("KER-SGR-002", "Failed to populate salt");

	/** MOSIP error code such as {@code KER-SGR-001}. */
	private final String errorCode;

	/** Short human-readable description. */
	private final String errorMessage;

	/**
	 * Binds an error code to its message.
	 *
	 * @param errorCode    never-null MOSIP error code
	 * @param errorMessage never-null short description
	 */
	private SaltGeneratorErrorConstants(String errorCode, String errorMessage) {
		this.errorCode = errorCode;
		this.errorMessage = errorMessage;
	}

	/**
	 * Returns the MOSIP error code.
	 *
	 * @return never-null code such as {@code KER-SGR-001}
	 */
	public String getErrorCode() {
		return errorCode;
	}

	/**
	 * Returns the short error message.
	 *
	 * @return never-null description
	 */
	public String getErrorMessage() {
		return errorMessage;
	}

	/**
	 * Returns every error code defined by this enum.
	 *
	 * @return never-null unmodifiable list of codes
	 */
	public static List<String> getAllErrorCodes() {
		return Collections.unmodifiableList(Arrays.asList(SaltGeneratorErrorConstants.values()).parallelStream()
				.map(SaltGeneratorErrorConstants::getErrorCode).collect(Collectors.toList()));
	}
}
