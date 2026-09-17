package io.mosip.kernel.core.idgenerator.spi;

/**
 * Generates a MOSIP MISP license key.
 * <p>
 * Contract: implementations typically produce a random or checksummed key of
 * configured length. Call from partner-management when issuing a MISP license.
 * Returned license is never null.
 * </p>
 *
 * @param <T> generated license type, typically {@link String}
 * @author Sagar Mahapatra
 * @since 1.0.0
 */
public interface MISPLicenseGenerator<T> {
	/**
	 * Generates a MISP license key of the configured length.
	 *
	 * @return never-null license key
	 */
	public T generateLicense();
}
