package io.mosip.kernel.core.licensekeymanager.spi;

import java.util.List;

/**
 * Generates, maps, and fetches MOSIP MISP license keys and their permissions.
 * <p>
 * Contract: implementations typically persist licenses in partner / kernel
 * tables. DTOs and identifiers must be non-null. Call from partner-management
 * when issuing or looking up MISP licenses.
 * </p>
 *
 * @param <T> response / identifier type
 * @param <D> license-generation request type
 * @param <S> license-mapping request type
 * @author Sagar Mahapatra
 * @since 1.0.0
 */
public interface LicenseKeyManagerService<T, D, S> {
	/**
	 * Generates a new license key.
	 *
	 * @param licenseKeyGenerationDto never-null generation request
	 * @return never-null generated license or response DTO
	 */
	public T generateLicenseKey(D licenseKeyGenerationDto);

	/**
	 * Maps a license key to permissions.
	 *
	 * @param licenseKeyMappingDto never-null mapping request
	 * @return never-null mapping result
	 */
	public T mapLicenseKey(S licenseKeyMappingDto);

	/**
	 * Fetches permissions mapped to a license key for a TSP.
	 *
	 * @param tspID      never-null trusted-service-provider identifier
	 * @param licenseKey never-null license key
	 * @return never-null list of permissions; may be empty
	 */
	public List<T> fetchLicenseKeyPermissions(T tspID, T licenseKey);
}
