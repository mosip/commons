package io.mosip.kernel.masterdata.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import io.mosip.kernel.core.dataaccess.spi.repository.BaseRepository;
import io.mosip.kernel.masterdata.entity.DeviceProvider;

/**
 * The Interface DeviceProviderRepository.
 * 
 * @author Srinivasan
 * @author Megha Tanga
 * @since 1.0.0
 */
@Repository
public interface DeviceProviderRepository extends BaseRepository<DeviceProvider, String> {

	/**
	 * Find by id and is active is true.
	 *
	 * @param id the id
	 * @return the device provider
	 */
	DeviceProvider findByIdAndIsActiveIsTrue(String id);

	@Query("FROM DeviceProvider d where d.id=?1 AND (d.isDeleted is null OR d.isDeleted = false) AND d.isActive = true")
	DeviceProvider findByIdAndIsDeletedFalseorIsDeletedIsNullAndIsActiveTrue(String id);

	@Query("FROM DeviceProvider d where d.id=?1 AND d.vendorName=?2 AND (d.isDeleted is null OR d.isDeleted = false) AND d.isActive = true")
	DeviceProvider findByIdAndNameAndIsDeletedFalseorIsDeletedIsNullAndIsActiveTrue(String id, String vendorName);
	
	@Query("FROM DeviceProvider d where d.vendorName=?1 AND d.address=?2 AND (d.isDeleted is null OR d.isDeleted = false) AND d.isActive = true")
	DeviceProvider findByNameAndAddressAndIsDeletedFalseorIsDeletedIsNullAndIsActiveTrue(String vendorName, String address);

	/*
	 * @Query("FROM DeviceProvider d where d.id=?1 AND d.venderName AND (d.isDeleted is null OR d.isDeleted = false) AND d.isActive = true"
	 * ) DeviceProvider
	 * findByIdAndNameIsDeletedFalseorIsDeletedIsNullAndIsActiveTrue(String id,
	 * String vendorName);
	 */
}
