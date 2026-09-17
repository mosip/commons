package io.mosip.kernel.core.applicanttype.spi;

import java.util.Map;

import io.mosip.kernel.core.applicanttype.exception.InvalidApplicantArgumentException;

/**
 * Resolves a MOSIP applicant type code from demographic attribute values.
 * <p>
 * Contract: implementations evaluate the supplied attribute map (typically
 * gender, age / date of birth, and similar master-data keys) and return the
 * matching applicant-type identifier. Call this SPI from registration or
 * packet processing when an applicant type must be derived before ID
 * generation. Implementations must not mutate the input map.
 * </p>
 *
 * @see InvalidApplicantArgumentException
 */
public interface ApplicantType {

	/**
	 * Resolves the applicant type for the given demographic attribute combination.
	 * <p>
	 * Contract: {@code map} must be non-null and must contain the attribute keys
	 * expected by the implementation; values must not be null. Does not perform
	 * HTTP or database I/O by itself (implementations may).
	 * </p>
	 *
	 * @param map non-null map of attribute name to value; empty or null values
	 *            are treated as invalid
	 * @return never-null applicant type code for the given combination
	 * @throws InvalidApplicantArgumentException if {@code map} is null or any
	 *                                           required attribute value is null
	 *                                           or empty
	 */
	public String getApplicantType(Map<String, Object> map) throws InvalidApplicantArgumentException;

}