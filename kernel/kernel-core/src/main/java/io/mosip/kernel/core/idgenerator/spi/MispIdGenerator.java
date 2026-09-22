package io.mosip.kernel.core.idgenerator.spi;

/**
 * Generates a MOSIP MISP (MOSIP Identity Service Provider) identifier.
 * <p>
 * Contract: implementations typically increment a sequence. Call from
 * partner-management when creating a MISP. Returned id is never null.
 * </p>
 *
 * @param <T> generated MISP-id type, typically {@link String}
 * @author Sidhant Agarwal
 * @author Ritesh Sinha
 * @since 1.0.0
 */
public interface MispIdGenerator<T> {
	/**
	 * Generates the next MISP identifier.
	 * <p>
	 * Contract: may perform database I/O.
	 * </p>
	 *
	 * @return never-null MISP id
	 */
	public T generateId();

}
