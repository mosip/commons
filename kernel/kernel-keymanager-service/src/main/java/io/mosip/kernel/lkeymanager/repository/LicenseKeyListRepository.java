package io.mosip.kernel.lkeymanager.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import io.mosip.kernel.lkeymanager.entity.LicenseKeyList;

/**
 * Repository class for {@link LicenseKeyList}.
 * 
 * @author Sagar Mahapatra
 * @since 1.0.0
 *
 */
@Repository
public interface LicenseKeyListRepository extends JpaRepository<LicenseKeyList, String> {
	/**
	 * Method to extract licensekey list details by license key.
	 * 
	 * @param licenseKey the license key.
	 * @return the entity response.
	 */
	public LicenseKeyList findByLicenseKey(String licenseKey);
}
