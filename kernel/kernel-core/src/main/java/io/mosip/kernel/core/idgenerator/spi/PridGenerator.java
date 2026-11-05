package io.mosip.kernel.core.idgenerator.spi;

/**
 * Generates a MOSIP Pre-Registration ID (PRID).
 * <p>
 * Contract: implementations typically persist a sequence outside this module
 * (Pre-Registration). This repository does not ship a PRID HTTP API. Returned
 * PRID is never null.
 * </p>
 *
 * @param <T> generated PRID type, typically {@link String}
 * @author Sidhant Agarwal
 * @since 1.0.0
 */
public interface PridGenerator<T> {
	/**
	 * Generates the next Pre-Registration ID.
	 * <p>
	 * Contract: may perform database I/O in the implementing module. Never returns
	 * {@code null}. Throws a MOSIP unchecked exception when generation fails
	 * (implementation-defined).
	 * </p>
	 *
	 * @return never-null generated PRID
	 */
	T generateId();
}
