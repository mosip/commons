package io.mosip.kernel.core.idgenerator.spi;

/**
 * Generates a MOSIP partner identifier.
 * <p>
 * Contract: implementations typically increment a sequence in
 * {@code mosip_kernel} or masterdata. Call from partner-management when
 * registering a partner. Returned id is never null.
 * </p>
 *
 * @param <T> generated partner-id type, typically {@link String}
 * @author Uday Kumar
 * @since 1.0.0
 */
public interface PartnerIdGenerator<T> {

	/**
	 * Generates the next partner identifier.
	 * <p>
	 * Contract: may perform database I/O.
	 * </p>
	 *
	 * @return never-null partner id
	 */
	public T generateId();

}
