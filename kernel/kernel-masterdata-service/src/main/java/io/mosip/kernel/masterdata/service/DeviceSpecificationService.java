package io.mosip.kernel.masterdata.service;

import java.util.List;

import io.mosip.kernel.masterdata.dto.DeviceSpecificationDto;
import io.mosip.kernel.masterdata.dto.getresponse.PageDto;
import io.mosip.kernel.masterdata.dto.getresponse.extn.DeviceSpecificationExtnDto;
import io.mosip.kernel.masterdata.dto.postresponse.IdResponseDto;
import io.mosip.kernel.masterdata.dto.request.FilterValueDto;
import io.mosip.kernel.masterdata.dto.request.SearchDto;
import io.mosip.kernel.masterdata.dto.response.FilterResponseDto;
import io.mosip.kernel.masterdata.dto.response.PageResponseDto;
import io.mosip.kernel.masterdata.entity.id.IdAndLanguageCodeID;
import io.mosip.kernel.masterdata.exception.DataNotFoundException;
import io.mosip.kernel.masterdata.exception.MasterDataServiceException;

/**
 * This interface has abstract methods to fetch and save Device Specification
 * Details
 * 
 * @author Uday Kumar
 * @author Megha Tanga
 * @since 1.0.0
 *
 */
public interface DeviceSpecificationService {
	/**
	 * This abstract method to fetch Device Specification Details for given language
	 * code
	 *
	 * @param languageCode Language code given by user
	 * @return List Device Specification Details for given language code
	 * @throws MasterDataServiceException if any error occurs while retrieving
	 *                                    device Specification
	 * @throws DataNotFoundException      if no Device Specification found
	 *
	 */
	public List<DeviceSpecificationDto> findDeviceSpecificationByLangugeCode(String languageCode);

	/**
	 * This abstract method to fetch Device Specification Details for given language
	 * code and device Type Code
	 * 
	 * @param languageCode   Language Code given by user
	 * @param deviceTypeCode DeviceTypeCode given by user
	 * @return List Device Specification Details for given language code and
	 *         deviceTypeCode
	 * 
	 * @throws MasterDataServiceException if any error occurs while retrieving
	 *                                    device Specification
	 * @throws DataNotFoundException      if no Device Specification found
	 *
	 */
	public List<DeviceSpecificationDto> findDeviceSpecByLangCodeAndDevTypeCode(String languageCode,
			String deviceTypeCode);

	/**
	 * Function to save Device Specification Details to the Database
	 * 
	 * @param deviceSpecification input from user DeviceSpecification DTO
	 * 
	 * @return IdResponseDto Device Specification ID which is successfully inserted
	 * @throws MasterDataServiceException if any error occurred while saving device
	 *                                    Specification
	 */
	public IdAndLanguageCodeID createDeviceSpecification(DeviceSpecificationDto deviceSpecification);

	/**
	 * Function to update Device Specification
	 * 
	 * @param deviceSpecification input from user DeviceSpecification DTO
	 * 
	 * @return IdResponseDto Device Specification ID which is successfully updated
	 * @throws MasterDataServiceException if any error occurred while updating
	 *                                    device Specification
	 */

	public IdAndLanguageCodeID updateDeviceSpecification(DeviceSpecificationDto deviceSpecification);

	/**
	 * Function to delete Device Specification
	 * 
	 * @param id input from user
	 * 
	 * @return IdResponseDto Device Specification ID which is successfully deleted
	 * @throws MasterDataServiceException if any error occurred while deleting
	 *                                    device Specification
	 */

	public IdResponseDto deleteDeviceSpecification(String id);

	/**
	 * Method to get all the device specifications
	 * 
	 * @param pageNumber next page number to get the requested data
	 * @param pageSize   number of data in the list
	 * @param sortBy     sorting data based the column name
	 * @param orderBy    order the list based on desc or asc
	 * 
	 * @return list of device specifications
	 */
	public PageDto<DeviceSpecificationExtnDto> getAllDeviceSpecifications(int pageNumber, int pageSize, String sortBy,
			String orderBy);

	public PageResponseDto<DeviceSpecificationExtnDto> searchDeviceSpec(SearchDto dto);

	/**
	 * Method that returns the column values of specific filter column name.
	 * 
	 * @param filterValueDto the request DTO that provides the column name.
	 * @return the response containing the filter values.
	 */
	public FilterResponseDto deviceSpecFilterValues(FilterValueDto filterValueDto);

}
