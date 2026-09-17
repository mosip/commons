package io.mosip.kernel.core.idgenerator.spi;

/**
 * Generates a MOSIP Virtual ID (VID) from the unused VID pool.
 * <p>
 * Contract: implementations typically assign a pre-generated VID from
 * {@code mosip_kernel} and may perform database I/O. Call from the ID-generator
 * HTTP API ({@code /v1/idgenerator}). Returned VID is never null when the pool
 * has unused entries.
 * </p>
 *
 * @param <T> generated VID type, typically {@link String}
 * @author Sidhant Agarwal
 * @author Megha Tanga
 * @since 1.0.0
 */
public interface VidGenerator<T> {

	/**
	 * Assigns the next unused VID from the pool.
	 * <p>
	 * Contract: may perform database I/O. Throws a MOSIP unchecked exception
	 * when the pool is exhausted (implementation-defined).
	 * </p>
	 *
	 * @return never-null generated VID
	 */
	T generateId();

}
