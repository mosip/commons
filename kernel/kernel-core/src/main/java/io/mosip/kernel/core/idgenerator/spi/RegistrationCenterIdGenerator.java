package io.mosip.kernel.core.idgenerator.spi;

/**
 * Generates a MOSIP registration-center identifier.
 * <p>
 * Contract: implementations typically increment a sequence in masterdata.
 * Call when creating a registration center. Returned id is never null.
 * </p>
 *
 * @param <T> generated center-id type, typically {@link String}
 * @author Sagar Mahapatra
 * @since 1.0.0
 */
public interface RegistrationCenterIdGenerator<T> {
	/**
	 * Generates the next registration-center identifier.
	 * <p>
	 * Contract: may perform database I/O.
	 * </p>
	 *
	 * @return never-null registration-center id
	 */
	public T generateRegistrationCenterId();
}
