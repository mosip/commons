package io.mosip.kernel.masterdata.service;

import io.mosip.kernel.masterdata.dto.LocationHierarchyLevelResponseDto;
import io.mosip.kernel.masterdata.exception.DataNotFoundException;
import io.mosip.kernel.masterdata.exception.MasterDataServiceException;

/**
 * 
 * @author Megha Tanga
 * @since 1.0.0
 *
 */

public interface LocationHierarchyService {

	/**
	 * This abstract method to fetch LocationHierarchy details for given
	 * LocationHierarchy level and language code
	 * 
	 * @param level
	 *            LocationHierarchy level given by user
	 * @param langCode
	 *            Language code given by user
	 * @return LocationHierarchyLevelResponseDto LocationHierarchy details for given
	 *         LocationHierarchy level and language code
	 * @throws MasterDataServiceException
	 *             if any error occurs while retrieving LocationHierarchy Details
	 * @throws DataNotFoundException
	 *             if no Location Hierarchy found
	 *
	 */

	public LocationHierarchyLevelResponseDto getLocationHierarchyLevelAndLangCode(short level, String langCode);

	/**
	 * This abstract method to fetch LocationHierarchy level detail list based on
	 * the given language code
	 * 
	 * @param langCode
	 *            Language code given by user
	 * @return LocationHierarchyLevelResponseDto Location-Hierarchy-level details
	 *         based on the given language code
	 * @throws MasterDataServiceException
	 *             if any error occurs while retrieving LocationHierarchy level list
	 * @throws DataNotFoundException
	 *             if no LocationHierarchy level found
	 *
	 */
	public LocationHierarchyLevelResponseDto getLocationHierarchyLangCode(String langCode);
}
