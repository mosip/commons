package io.mosip.kernel.core.idgenerator.spi;

/**
 * Generates a MOSIP registration-client machine identifier.
 * <p>
 * Contract: implementations typically increment a sequence in masterdata or
 * {@code mosip_kernel}. Call when registering a new machine. Returned id is
 * never null.
 * </p>
 *
 * @param <T> generated machine-id type, typically {@link String}
 * @author Sagar Mahapatra
 * @since 1.0.0
 */
public interface MachineIdGenerator<T> {
	/**
	 * Generates the next machine identifier.
	 * <p>
	 * Contract: may perform database I/O.
	 * </p>
	 *
	 * @return never-null machine id
	 */
	public T generateMachineId();

}
