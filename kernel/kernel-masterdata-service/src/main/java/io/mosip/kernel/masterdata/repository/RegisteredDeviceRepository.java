package io.mosip.kernel.masterdata.repository;

import org.springframework.stereotype.Repository;

import io.mosip.kernel.core.dataaccess.spi.repository.BaseRepository;
import io.mosip.kernel.masterdata.entity.RegisteredDevice;

// TODO: Auto-generated Javadoc
/**
 * The Interface RegisteredDeviceRepository.
 * 
 * @author Srinivasan
 * @author Megha Tanga
 * @author Ramadurai Pandian
 */
@Repository
public interface RegisteredDeviceRepository extends BaseRepository<RegisteredDevice, String> {

	/**
	 * Find by code and is active is true.
	 *
	 * @param deviceCode the device code
	 * @return the registered device
	 */
	RegisteredDevice findByCodeAndIsActiveIsTrue(String deviceCode);

	/**
	 * Find by code and dp id.
	 *
	 * @param deviceCode the device code
	 * @param providerId the provider id
	 * @return the registered device
	 */
	RegisteredDevice findByCodeAndDpId(String deviceCode, String providerId);
	
	/**
	 * Find by serial no and dp id.
	 *
	 *
	 * @param providerId the provider id
	 *  @param serialno the serial no
	 * @return the registered device
	 */	
	RegisteredDevice findByDpIdAndSerialNoAndIsActiveIsTrue(String providerId,String serialno);
}
