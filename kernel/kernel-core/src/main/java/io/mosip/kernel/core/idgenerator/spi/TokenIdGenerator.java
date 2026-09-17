package io.mosip.kernel.core.idgenerator.spi;

/**
 * Generates a random MOSIP token ID used to correlate authentication
 * transactions.
 * <p>
 * Contract: implementations typically compute a checksummed numeric token
 * without HTTP. Call from IDA when a token ID is required. Returned value is
 * never null.
 * </p>
 *
 * @param <T> generated token type, typically {@link String}
 * @author Sagar Mahapatra
 * @author Ritesh Sinha
 * @since 1.0.0
 */
public interface TokenIdGenerator<T> {
	/**
	 * Generates a random token ID.
	 *
	 * @return never-null token ID
	 */
	T generateId();
}
