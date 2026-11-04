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
	 * Generates a PRID.
	 *
	 * @return never-null generated PRID
	 */
	T generateId();
}
