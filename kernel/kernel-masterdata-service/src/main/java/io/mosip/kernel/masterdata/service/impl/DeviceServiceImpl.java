package io.mosip.kernel.masterdata.service.impl;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.mosip.kernel.core.dataaccess.exception.DataAccessLayerException;
import io.mosip.kernel.masterdata.constant.DeviceErrorCode;
import io.mosip.kernel.masterdata.constant.MasterDataConstant;
import io.mosip.kernel.masterdata.dto.DeviceDto;
import io.mosip.kernel.masterdata.dto.DeviceLangCodeDtypeDto;
import io.mosip.kernel.masterdata.dto.DevicePutReqDto;
import io.mosip.kernel.masterdata.dto.DeviceRegistrationCenterDto;
import io.mosip.kernel.masterdata.dto.DeviceTypeDto;
import io.mosip.kernel.masterdata.dto.PageDto;
import io.mosip.kernel.masterdata.dto.getresponse.DeviceLangCodeResponseDto;
import io.mosip.kernel.masterdata.dto.getresponse.DeviceResponseDto;
import io.mosip.kernel.masterdata.dto.getresponse.extn.DeviceExtnDto;
import io.mosip.kernel.masterdata.dto.postresponse.IdResponseDto;
import io.mosip.kernel.masterdata.dto.request.FilterDto;
import io.mosip.kernel.masterdata.dto.request.FilterValueDto;
import io.mosip.kernel.masterdata.dto.request.Pagination;
import io.mosip.kernel.masterdata.dto.request.SearchDto;
import io.mosip.kernel.masterdata.dto.request.SearchFilter;
import io.mosip.kernel.masterdata.dto.request.SearchSort;
import io.mosip.kernel.masterdata.dto.response.ColumnValue;
import io.mosip.kernel.masterdata.dto.response.DeviceSearchDto;
import io.mosip.kernel.masterdata.dto.response.FilterResponseDto;
import io.mosip.kernel.masterdata.dto.response.PageResponseDto;
import io.mosip.kernel.masterdata.entity.Device;
import io.mosip.kernel.masterdata.entity.DeviceHistory;
import io.mosip.kernel.masterdata.entity.DeviceSpecification;
import io.mosip.kernel.masterdata.entity.DeviceType;
import io.mosip.kernel.masterdata.entity.RegistrationCenter;
import io.mosip.kernel.masterdata.entity.RegistrationCenterDevice;
import io.mosip.kernel.masterdata.entity.RegistrationCenterMachineDevice;
import io.mosip.kernel.masterdata.entity.Zone;
import io.mosip.kernel.masterdata.exception.DataNotFoundException;
import io.mosip.kernel.masterdata.exception.MasterDataServiceException;
import io.mosip.kernel.masterdata.exception.RequestException;
import io.mosip.kernel.masterdata.repository.DeviceRepository;
import io.mosip.kernel.masterdata.repository.RegistrationCenterDeviceRepository;
import io.mosip.kernel.masterdata.repository.RegistrationCenterMachineDeviceRepository;
import io.mosip.kernel.masterdata.service.DeviceHistoryService;
import io.mosip.kernel.masterdata.service.DeviceService;
import io.mosip.kernel.masterdata.service.ZoneService;
import io.mosip.kernel.masterdata.utils.AuditUtil;
import io.mosip.kernel.masterdata.utils.DeviceUtils;
import io.mosip.kernel.masterdata.utils.ExceptionUtils;
import io.mosip.kernel.masterdata.utils.MapperUtils;
import io.mosip.kernel.masterdata.utils.MasterDataFilterHelper;
import io.mosip.kernel.masterdata.utils.MasterdataCreationUtil;
import io.mosip.kernel.masterdata.utils.MasterdataSearchHelper;
import io.mosip.kernel.masterdata.utils.MetaDataUtils;
import io.mosip.kernel.masterdata.utils.OptionalFilter;
import io.mosip.kernel.masterdata.utils.PageUtils;
import io.mosip.kernel.masterdata.utils.ZoneUtils;
import io.mosip.kernel.masterdata.validator.FilterColumnValidator;
import io.mosip.kernel.masterdata.validator.FilterTypeEnum;
import io.mosip.kernel.masterdata.validator.FilterTypeValidator;

/**
 * This class have methods to fetch and save Device Details
 * 
 * @author Megha Tanga
 * @author Sidhant Agarwal
 * @author Ravi Kant
 * @since 1.0.0
 *
 */
@Service
public class DeviceServiceImpl implements DeviceService {

	/**
	 * Field to hold Device Repository object
	 */
	@Autowired
	DeviceRepository deviceRepository;
	/**
	 * Field to hold Device Service object
	 */
	@Autowired
	DeviceHistoryService deviceHistoryService;

	@Autowired
	RegistrationCenterDeviceRepository registrationCenterDeviceRepository;

	@Autowired
	RegistrationCenterMachineDeviceRepository registrationCenterMachineDeviceRepository;

	@Autowired
	private MasterdataSearchHelper masterdataSearchHelper;

	@Autowired
	private FilterTypeValidator filterValidator;

	@Autowired
	private MasterDataFilterHelper masterDataFilterHelper;

	@Autowired
	private FilterColumnValidator filterColumnValidator;

	@Autowired
	private ZoneUtils zoneUtils;

	@Autowired
	private DeviceUtils deviceUtil;

	@Autowired
	private PageUtils pageUtils;

	@Autowired
	private ZoneService zoneService;

	@Autowired
	private MasterdataCreationUtil masterdataCreationUtil;

	@Value("${mosip.primary-language}")
	private String primaryLangCode;

	@Value("${mosip.secondary-language:ara}")
	private String secondaryLang;

	@Autowired
	private AuditUtil auditUtil;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * io.mosip.kernel.masterdata.service.DeviceService#getDeviceLangCode(java.lang.
	 * String)
	 */
	@Override
	public DeviceResponseDto getDeviceLangCode(String langCode) {
		List<Device> deviceList = null;
		List<DeviceDto> deviceDtoList = null;
		DeviceResponseDto deviceResponseDto = new DeviceResponseDto();
		try {
			deviceList = deviceRepository.findByLangCodeAndIsDeletedFalseOrIsDeletedIsNull(langCode);
		} catch (DataAccessException e) {
			throw new MasterDataServiceException(DeviceErrorCode.DEVICE_FETCH_EXCEPTION.getErrorCode(),
					DeviceErrorCode.DEVICE_FETCH_EXCEPTION.getErrorMessage() + "  " + ExceptionUtils.parseException(e));
		}
		if (deviceList != null && !deviceList.isEmpty()) {
			deviceDtoList = MapperUtils.mapAll(deviceList, DeviceDto.class);

		} else {
			throw new DataNotFoundException(DeviceErrorCode.DEVICE_NOT_FOUND_EXCEPTION.getErrorCode(),
					DeviceErrorCode.DEVICE_NOT_FOUND_EXCEPTION.getErrorMessage());
		}
		deviceResponseDto.setDevices(deviceDtoList);
		return deviceResponseDto;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see io.mosip.kernel.masterdata.service.DeviceService#
	 * getDeviceLangCodeAndDeviceType(java.lang.String, java.lang.String)
	 */
	@Override
	public DeviceLangCodeResponseDto getDeviceLangCodeAndDeviceType(String langCode, String dtypeCode) {

		List<Object[]> objectList = null;
		List<DeviceLangCodeDtypeDto> deviceLangCodeDtypeDtoList = null;
		DeviceLangCodeResponseDto deviceLangCodeResponseDto = new DeviceLangCodeResponseDto();
		try {
			objectList = deviceRepository.findByLangCodeAndDtypeCode(langCode, dtypeCode);
		} catch (DataAccessException e) {
			throw new MasterDataServiceException(DeviceErrorCode.DEVICE_FETCH_EXCEPTION.getErrorCode(),
					DeviceErrorCode.DEVICE_FETCH_EXCEPTION.getErrorMessage() + "  " + ExceptionUtils.parseException(e));
		}
		if (objectList != null && !objectList.isEmpty()) {
			deviceLangCodeDtypeDtoList = MapperUtils.mapDeviceDto(objectList);
		} else {
			throw new DataNotFoundException(DeviceErrorCode.DEVICE_NOT_FOUND_EXCEPTION.getErrorCode(),
					DeviceErrorCode.DEVICE_NOT_FOUND_EXCEPTION.getErrorMessage());
		}
		deviceLangCodeResponseDto.setDevices(deviceLangCodeDtypeDtoList);
		return deviceLangCodeResponseDto;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * io.mosip.kernel.masterdata.service.DeviceService#saveDevice(io.mosip.kernel.
	 * masterdata.dto.RequestDto)
	 */
	@Override
	@Transactional
	public DeviceExtnDto createDevice(DeviceDto deviceDto) {
		Device device = null;
		Device entity = null;
		DeviceHistory entityHistory = null;
		DeviceExtnDto deviceExtnDto = new DeviceExtnDto();
		try {
			validateZone(deviceDto.getZoneCode());
			deviceDto = masterdataCreationUtil.createMasterData(Device.class, deviceDto);
			if (deviceDto != null) {
				entity = MetaDataUtils.setCreateMetaData(deviceDto, Device.class);
				entityHistory = MetaDataUtils.setCreateMetaData(deviceDto, DeviceHistory.class);
				entityHistory.setEffectDateTime(entity.getCreatedDateTime());
				entityHistory.setCreatedDateTime(entity.getCreatedDateTime());
				// entity.setIsActive(false);
				// String id = UUID.fromString(deviceDto.getName()).toString();
				// entity.setId(id);
				device = deviceRepository.create(entity);
				deviceHistoryService.createDeviceHistory(entityHistory);
				MapperUtils.map(device, deviceExtnDto);
			}
		} catch (DataAccessLayerException | DataAccessException e) {
			auditUtil.auditRequest(String.format(MasterDataConstant.FAILURE_CREATE, DeviceDto.class.getCanonicalName()),
					MasterDataConstant.AUDIT_SYSTEM,
					String.format(MasterDataConstant.FAILURE_DESC,
							DeviceErrorCode.DEVICE_INSERT_EXCEPTION.getErrorCode(),
							DeviceErrorCode.DEVICE_INSERT_EXCEPTION.getErrorMessage() + " "
									+ ExceptionUtils.parseException(e)),
					"ADM-507");
			throw new MasterDataServiceException(DeviceErrorCode.DEVICE_INSERT_EXCEPTION.getErrorCode(),
					DeviceErrorCode.DEVICE_INSERT_EXCEPTION.getErrorMessage() + " " + ExceptionUtils.parseException(e));
		} catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e1) {
			throw new MasterDataServiceException(DeviceErrorCode.DEVICE_INSERT_EXCEPTION.getErrorCode(),
					DeviceErrorCode.DEVICE_INSERT_EXCEPTION.getErrorMessage() + " "
							+ ExceptionUtils.parseException(e1));
		}
		// IdAndLanguageCodeID idAndLanguageCodeID = new IdAndLanguageCodeID();
		// MapperUtils.map(device, idAndLanguageCodeID);
		auditUtil.auditRequest(String.format(MasterDataConstant.SUCCESSFUL_CREATE, DeviceExtnDto.class.getSimpleName()),
				MasterDataConstant.AUDIT_SYSTEM,
				String.format(MasterDataConstant.SUCCESSFUL_CREATE_DESC, device!=null?device.getId():null), "ADM-508");
		return deviceExtnDto;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see io.mosip.kernel.masterdata.service.DeviceService#deleteDevice(java.lang.
	 * String)
	 */
	@Override
	@Transactional
	public IdResponseDto deleteDevice(String id) {
		List<Device> foundDeviceList = null;
		Device deletedDevice = null;
		try {
			foundDeviceList = deviceRepository.findByIdAndIsDeletedFalseOrIsDeletedIsNull(id);

			if (foundDeviceList!=null && !foundDeviceList.isEmpty()) {
				for (Device foundDevice : foundDeviceList) {

					List<RegistrationCenterMachineDevice> registrationCenterMachineDeviceList = registrationCenterMachineDeviceRepository
							.findByDeviceIdAndIsDeletedFalseOrIsDeletedIsNull(foundDevice.getId());
					List<RegistrationCenterDevice> registrationCenterDeviceList = registrationCenterDeviceRepository
							.findByDeviceIdAndIsDeletedFalseOrIsDeletedIsNull(foundDevice.getId());
					if (registrationCenterMachineDeviceList.isEmpty() && registrationCenterDeviceList.isEmpty()) {

						MetaDataUtils.setDeleteMetaData(foundDevice);
						deletedDevice = deviceRepository.update(foundDevice);

						DeviceHistory deviceHistory = new DeviceHistory();
						MapperUtils.map(deletedDevice, deviceHistory);
						MapperUtils.setBaseFieldValue(deletedDevice, deviceHistory);

						deviceHistory.setEffectDateTime(deletedDevice.getDeletedDateTime());
						deviceHistory.setDeletedDateTime(deletedDevice.getDeletedDateTime());
						deviceHistoryService.createDeviceHistory(deviceHistory);
					} else {
						throw new RequestException(DeviceErrorCode.DEPENDENCY_EXCEPTION.getErrorCode(),
								DeviceErrorCode.DEPENDENCY_EXCEPTION.getErrorMessage());
					}
				}
			} else {
				throw new RequestException(DeviceErrorCode.DEVICE_NOT_FOUND_EXCEPTION.getErrorCode(),
						DeviceErrorCode.DEVICE_NOT_FOUND_EXCEPTION.getErrorMessage());
			}

		} catch (DataAccessLayerException | DataAccessException e) {
			throw new MasterDataServiceException(DeviceErrorCode.DEVICE_DELETE_EXCEPTION.getErrorCode(),
					DeviceErrorCode.DEVICE_DELETE_EXCEPTION.getErrorMessage() + " " + ExceptionUtils.parseException(e));
		}
		IdResponseDto idResponseDto = new IdResponseDto();
		idResponseDto.setId(id);
		return idResponseDto;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see io.mosip.kernel.masterdata.service.MachineService#
	 * getRegistrationCenterMachineMapping1(java.lang.String)
	 */
	@Override
	public PageDto<DeviceRegistrationCenterDto> getDevicesByRegistrationCenter(String regCenterId, int page, int size,
			String orderBy, String direction) {
		PageDto<DeviceRegistrationCenterDto> pageDto = new PageDto<>();
		List<DeviceRegistrationCenterDto> deviceRegistrationCenterDtoList = null;
		Page<Device> pageEntity = null;

		try {
			pageEntity = deviceRepository.findDeviceByRegCenterId(regCenterId,
					PageRequest.of(page, size, Sort.by(Direction.fromString(direction), orderBy)));
		} catch (DataAccessException e) {
			throw new MasterDataServiceException(
					DeviceErrorCode.REGISTRATION_CENTER_DEVICE_FETCH_EXCEPTION.getErrorCode(),
					DeviceErrorCode.REGISTRATION_CENTER_DEVICE_FETCH_EXCEPTION.getErrorMessage()
							+ ExceptionUtils.parseException(e));
		}
		if (pageEntity != null && !pageEntity.getContent().isEmpty()) {
			deviceRegistrationCenterDtoList = MapperUtils.mapAll(pageEntity.getContent(),
					DeviceRegistrationCenterDto.class);
			for (DeviceRegistrationCenterDto deviceRegistrationCenterDto : deviceRegistrationCenterDtoList) {
				deviceRegistrationCenterDto.setRegCentId(regCenterId);
			}
		} else {
			throw new RequestException(DeviceErrorCode.DEVICE_REGISTRATION_CENTER_NOT_FOUND_EXCEPTION.getErrorCode(),
					DeviceErrorCode.DEVICE_REGISTRATION_CENTER_NOT_FOUND_EXCEPTION.getErrorMessage());
		}
		pageDto.setPageNo(pageEntity.getNumber());
		pageDto.setPageSize(pageEntity.getSize());
		pageDto.setSort(pageEntity.getSort());
		pageDto.setTotalItems(pageEntity.getTotalElements());
		pageDto.setTotalPages(pageEntity.getTotalPages());
		pageDto.setData(deviceRegistrationCenterDtoList);

		return pageDto;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * io.mosip.kernel.masterdata.service.DeviceService#searchDevice(io.mosip.kernel
	 * .masterdata.dto.request.SearchDto)
	 */
	@Override
	public PageResponseDto<DeviceSearchDto> searchDevice(SearchDto dto) {
		PageResponseDto<DeviceSearchDto> pageDto = new PageResponseDto<>();
		List<DeviceSearchDto> devices = null;
		List<SearchFilter> addList = new ArrayList<>();
		List<SearchFilter> mapStatusList = new ArrayList<>();
		List<SearchFilter> removeList = new ArrayList<>();
		List<String> mappedDeviceIdList = null;
		List<SearchFilter> zoneFilter = new ArrayList<>();
		List<Zone> zones = null;
		boolean flag = true;
		boolean isAssigned = true;
		String typeName = null;
		String langCode = null;
		if (dto.getLanguageCode().equals("all")) {
			langCode = primaryLangCode;
		} else {
			langCode = dto.getLanguageCode();
		}
		for (SearchFilter filter : dto.getFilters()) {
			String column = filter.getColumnName();

			if (column.equalsIgnoreCase("mapStatus")) {

				if (filter.getValue().equalsIgnoreCase("assigned")) {
					mappedDeviceIdList = deviceRepository.findMappedDeviceId(langCode);
					mapStatusList.addAll(buildRegistrationCenterDeviceTypeSearchFilter(mappedDeviceIdList));
					if (!dto.getFilters().isEmpty() && mappedDeviceIdList.isEmpty()) {
						pageDto = pageUtils.sortPage(devices, dto.getSort(), dto.getPagination());
						return pageDto;
					}

				} else {
					if (filter.getValue().equalsIgnoreCase("unassigned")) {
						mappedDeviceIdList = deviceRepository.findNotMappedDeviceId(langCode);
						mapStatusList.addAll(buildRegistrationCenterDeviceTypeSearchFilter(mappedDeviceIdList));
						isAssigned = false;
						if (!dto.getFilters().isEmpty() && mappedDeviceIdList.isEmpty()) {
							pageDto = pageUtils.sortPage(devices, dto.getSort(), dto.getPagination());
							return pageDto;
						}
					} else {
						auditUtil.auditRequest(
								String.format(MasterDataConstant.SEARCH_FAILED, DeviceDto.class.getSimpleName()),
								MasterDataConstant.AUDIT_SYSTEM,
								String.format(MasterDataConstant.SEARCH_FAILED, DeviceSearchDto.class.getSimpleName()),
								"ADM-509");
						throw new RequestException(DeviceErrorCode.INVALID_DEVICE_FILTER_VALUE_EXCEPTION.getErrorCode(),
								DeviceErrorCode.INVALID_DEVICE_FILTER_VALUE_EXCEPTION.getErrorMessage());
					}

				}
				removeList.add(filter);
			}

			if (column.equalsIgnoreCase("deviceTypeName")) {
				filter.setColumnName(MasterDataConstant.NAME);
				typeName = filter.getValue();
				if (filterValidator.validate(DeviceTypeDto.class, Arrays.asList(filter))) {

					List<Object[]> dSpecs = deviceRepository
							.findDeviceSpecByDeviceTypeNameAndLangCode(filter.getValue(), langCode);

					removeList.add(filter);
					addList.addAll(buildDeviceSpecificationSearchFilter(dSpecs));
				}

			}
		}
		if (flag) {

			zones = zoneUtils.getUserZones();
			if (zones != null && !zones.isEmpty()) {
				zoneFilter.addAll(buildZoneFilter(zones));
			} else {
				auditUtil.auditRequest(String.format(MasterDataConstant.SEARCH_FAILED, DeviceDto.class.getSimpleName()),
						MasterDataConstant.AUDIT_SYSTEM,
						String.format(MasterDataConstant.SEARCH_FAILED,
								DeviceErrorCode.DEVICE_NOT_TAGGED_TO_ZONE.getErrorCode(),
								DeviceErrorCode.DEVICE_NOT_TAGGED_TO_ZONE.getErrorMessage()),
						"ADM-510");
				throw new MasterDataServiceException(DeviceErrorCode.DEVICE_NOT_TAGGED_TO_ZONE.getErrorCode(),
						DeviceErrorCode.DEVICE_NOT_TAGGED_TO_ZONE.getErrorMessage());
			}
		}

		dto.getFilters().removeAll(removeList);
		Pagination pagination = dto.getPagination();
		List<SearchSort> sort = dto.getSort();
		pageUtils.validateSortField(DeviceSearchDto.class, Device.class, sort);
		dto.setPagination(new Pagination(0, Integer.MAX_VALUE));
		dto.setSort(Collections.emptyList());
		if (filterValidator.validate(DeviceSearchDto.class, dto.getFilters())) {
			OptionalFilter optionalFilter = new OptionalFilter(addList);
			OptionalFilter zoneOptionalFilter = new OptionalFilter(zoneFilter);
			Page<Device> page = null;
			if (mapStatusList.isEmpty() || addList.isEmpty()) {
				addList.addAll(mapStatusList);
				page = masterdataSearchHelper.searchMasterdata(Device.class, dto,
						new OptionalFilter[] { optionalFilter, zoneOptionalFilter });
			} else {

				page = masterdataSearchHelper.nativeDeviceQuerySearch(dto, typeName, zones, isAssigned);

			}
			if (page.getContent() != null && !page.getContent().isEmpty()) {
				devices = MapperUtils.mapAll(page.getContent(), DeviceSearchDto.class);
				setDeviceMetadata(devices, zones);
				setDeviceTypeNames(devices);
				setMapStatus(devices, dto.getLanguageCode());
				devices.forEach(device -> {
					if (device.getMapStatus() == null) {
						device.setMapStatus("unassigned");
					}
				});
				pageDto = pageUtils.sortPage(devices, sort, pagination);

			}

		}
		return pageDto;
	}

	/**
	 * Method to set each device zone meta data.
	 * 
	 * @param list  list of {@link DeviceSearchDto}.
	 * @param zones the list of zones.
	 */
	private void setDeviceMetadata(List<DeviceSearchDto> list, List<Zone> zones) {
		list.forEach(i -> setZoneMetadata(i, zones));
	}

	/**
	 * Method to set DeviceType Name for each Device.
	 * 
	 * @param list the {@link DeviceSearchDto}.
	 */
	private void setDeviceTypeNames(List<DeviceSearchDto> list) {
		List<DeviceSpecification> deviceSpecifications = deviceUtil.getDeviceSpec();
		List<DeviceType> deviceTypes = deviceUtil.getDeviceTypes();
		list.forEach(deviceSearchDto -> {
			deviceSpecifications.forEach(s -> {
				if (s.getId().equals(deviceSearchDto.getDeviceSpecId())
						&& s.getLangCode().equals(deviceSearchDto.getLangCode())) {
					String typeCode = s.getDeviceTypeCode();
					deviceTypes.forEach(mt -> {
						if (mt.getCode().equals(typeCode) && mt.getLangCode().equals(s.getLangCode())) {
							deviceSearchDto.setDeviceTypeName(mt.getName());
						}
					});
				}
			});
		});
	}

	/**
	 * Method to set Map status of each Device.
	 * 
	 * @param list the {@link DeviceSearchDto}.
	 */
	private void setMapStatus(List<DeviceSearchDto> list, String langCode) {

		List<RegistrationCenterDevice> centerDeviceList = deviceUtil.getAllDeviceCentersList();
		List<RegistrationCenter> registrationCenterList = deviceUtil.getAllRegistrationCenters();
		list.forEach(deviceSearchDto -> {
			centerDeviceList.forEach(centerDevice -> {
				if (centerDevice.getDevice().getId().equals(deviceSearchDto.getId())
						&& centerDevice.getLangCode().equals(deviceSearchDto.getLangCode())) {
					String regId = centerDevice.getRegistrationCenter().getId();
					registrationCenterList.forEach(registrationCenter -> {
						if (registrationCenter.getId().equals(regId)
								&& centerDevice.getLangCode().equals(registrationCenter.getLangCode())) {
							deviceSearchDto.setMapStatus(registrationCenter.getName());
						}
					});
				}
			});
		});
	}

	/**
	 * Method to set Zone metadata
	 * 
	 * @param devices metadata to be added
	 * @param zones   list of zones
	 * 
	 */
	private void setZoneMetadata(DeviceSearchDto devices, List<Zone> zones) {
		Optional<Zone> zone = zones.stream()
				.filter(i -> i.getCode().equals(devices.getZoneCode()) && i.getLangCode().equals(devices.getLangCode()))
				.findFirst();
		if (zone.isPresent()) {
			devices.setZone(zone.get().getName());
		}
	}

	/**
	 * Search the zone in the based on the received input filter
	 * 
	 * @param filter search input
	 * @return {@link Zone} if successful otherwise throws
	 *         {@link MasterDataServiceException}
	 */
	public Zone getZone(SearchFilter filter) {
		filter.setColumnName(MasterDataConstant.NAME);
		Page<Zone> zones = masterdataSearchHelper.searchMasterdata(Zone.class,
				new SearchDto(Arrays.asList(filter), Collections.emptyList(), new Pagination(), null), null);
		if (zones.hasContent()) {
			return zones.getContent().get(0);
		} else {
			throw new MasterDataServiceException(DeviceErrorCode.ZONE_NOT_EXIST.getErrorCode(),
					String.format(DeviceErrorCode.ZONE_NOT_EXIST.getErrorMessage(), filter.getValue()));
		}
	}

	/**
	 * Creating Search filter from the passed zones
	 * 
	 * @param zones filter to be created with the zones
	 * @return list of {@link SearchFilter}
	 */
	public List<SearchFilter> buildZoneFilter(List<Zone> zones) {
		if (zones != null && !zones.isEmpty()) {
			return zones.stream().filter(Objects::nonNull).map(Zone::getCode).distinct().map(this::buildZoneFilter)
					.collect(Collectors.toList());
		}
		return Collections.emptyList();
	}

	/**
	 * Method to create SearchFilter for the recieved zoneCode
	 * 
	 * @param zoneCode input from the {@link SearchFilter} has to be created
	 * @return {@link SearchFilter}
	 */
	private SearchFilter buildZoneFilter(String zoneCode) {
		SearchFilter filter = new SearchFilter();
		filter.setColumnName(MasterDataConstant.ZONE_CODE);
		filter.setType(FilterTypeEnum.EQUALS.name());
		filter.setValue(zoneCode);
		return filter;
	}

	/**
	 * This method return Device Id list filters.
	 * 
	 * @param deviceIdList the Device Id list.
	 * @return the list of {@link SearchFilter}.
	 */
	private List<SearchFilter> buildRegistrationCenterDeviceTypeSearchFilter(List<String> deviceIdList) {
		if (deviceIdList != null && !deviceIdList.isEmpty())
			return deviceIdList.stream().filter(Objects::nonNull).map(this::buildRegistrationCenterDeviceType)
					.collect(Collectors.toList());
		return Collections.emptyList();
	}

	/**
	 * This method return Device Types list filters.
	 * 
	 * @param deviceTypes the list of Device Type.
	 * @return the list of {@link SearchFilter}.
	 */
	/*
	 * private List<SearchFilter> buildDeviceTypeSearchFilter(List<DeviceType>
	 * deviceTypes) { if (deviceTypes != null && !deviceTypes.isEmpty()) return
	 * deviceTypes.stream().filter(Objects::nonNull).map(this::buildDeviceType)
	 * .collect(Collectors.toList()); return Collections.emptyList(); }
	 */

	/**
	 * This method return Device Specification list filters.
	 * 
	 * @param deviceSpecs the list of Device Specification.
	 * @return the list of {@link SearchFilter}.
	 */
	private List<SearchFilter> buildDeviceSpecificationSearchFilter(List<Object[]> deviceSpecs) {
		SearchFilter filter = null;
		List<SearchFilter> searchFilters = new ArrayList<>();
		for (Object[] dSpecObj : deviceSpecs) {
			filter = new SearchFilter();
			filter.setColumnName("deviceSpecId");
			filter.setType(FilterTypeEnum.EQUALS.name());
			filter.setValue(dSpecObj[0].toString());
			searchFilters.add(filter);

		}

		return searchFilters;
	}

	/**
	 * This method provide search filter for provided device id.
	 * 
	 * @param deviceId the device id.
	 * @return the {@link SearchFilter}.
	 */
	private SearchFilter buildRegistrationCenterDeviceType(String deviceId) {
		SearchFilter filter = new SearchFilter();
		filter.setColumnName("id");
		filter.setType(FilterTypeEnum.EQUALS.name());
		filter.setValue(deviceId);
		return filter;
	}

	/**
	 * This method provide search filter for provided Device specification.
	 * 
	 * @param deviceSpecification the device specification.
	 * @return the {@link SearchFilter}.
	 */
	/*
	 * private SearchFilter buildDeviceSpecification(DeviceSpecification
	 * deviceSpecification) { SearchFilter filter = new SearchFilter();
	 * filter.setColumnName("deviceSpecId");
	 * filter.setType(FilterTypeEnum.EQUALS.name());
	 * filter.setValue(deviceSpecification.getId()); return filter; }
	 */

	/**
	 * This method provide search filter for provided Device Type.
	 * 
	 * @param deviceType the device type.
	 * @return the {@link SearchFilter}.
	 */
	/*
	 * private SearchFilter buildDeviceType(DeviceType deviceType) { SearchFilter
	 * filter = new SearchFilter(); filter.setColumnName("deviceTypeCode");
	 * filter.setType(FilterTypeEnum.EQUALS.name());
	 * filter.setValue(deviceType.getCode()); return filter; }
	 */

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * io.mosip.kernel.masterdata.service.DeviceService#deviceFilterValues(io.mosip.
	 * kernel.masterdata.dto.request.FilterValueDto)
	 */
	@Override
	public FilterResponseDto deviceFilterValues(FilterValueDto filterValueDto) {
		FilterResponseDto filterResponseDto = new FilterResponseDto();
		List<ColumnValue> columnValueList = new ArrayList<>();
		List<Zone> zones = zoneUtils.getUserZones();
		List<SearchFilter> zoneFilter = new ArrayList<>();
		if (zones != null && !zones.isEmpty()) {
			zoneFilter.addAll(buildZoneFilter(zones));
			filterValueDto.setOptionalFilters(zoneFilter);
		} else {
			return filterResponseDto;
		}
		if (filterColumnValidator.validate(FilterDto.class, filterValueDto.getFilters(), Device.class))

		{
			for (FilterDto filterDto : filterValueDto.getFilters()) {
				masterDataFilterHelper.filterValues(Device.class, filterDto, filterValueDto).forEach(filterValue -> {
					if (filterValue != null) {
						ColumnValue columnValue = new ColumnValue();
						columnValue.setFieldID(filterDto.getColumnName());
						columnValue.setFieldValue(filterValue.toString());
						columnValueList.add(columnValue);
					}
				});
			}
			filterResponseDto.setFilters(columnValueList);

		}
		return filterResponseDto;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * io.mosip.kernel.masterdata.service.DeviceService#decommissionDevice(java.lang
	 * .String)
	 */
	@Override
	@Transactional
	public IdResponseDto decommissionDevice(String deviceId) {
		IdResponseDto idResponseDto = new IdResponseDto();
		int decommissionedDevice = 0;

		// get devices from DB by given id
		List<Device> devices = deviceRepository.findDeviceByIdAndIsDeletedFalseorIsDeletedIsNullNoIsActive(deviceId);

		// device is not in DB
		if (devices.isEmpty()) {
			auditUtil
					.auditRequest(
							String.format(MasterDataConstant.FAILURE_DECOMMISSION, DeviceDto.class.getSimpleName()),
							MasterDataConstant.AUDIT_SYSTEM,
							String.format(MasterDataConstant.FAILURE_DESC,
									DeviceErrorCode.DEVICE_NOT_EXISTS_EXCEPTION.getErrorCode(), String.format(
											DeviceErrorCode.DEVICE_NOT_EXISTS_EXCEPTION.getErrorMessage(), deviceId)),
							"ADM-511");
			throw new RequestException(DeviceErrorCode.DEVICE_NOT_EXISTS_EXCEPTION.getErrorCode(),
					String.format(DeviceErrorCode.DEVICE_NOT_EXISTS_EXCEPTION.getErrorMessage(), deviceId));
		}

		List<String> zoneIds;

		// get user zone and child zones list
		List<Zone> userZones = zoneUtils.getUserZones();
		zoneIds = userZones.parallelStream().map(Zone::getCode).collect(Collectors.toList());

		// check the given device and registration center zones are come under user zone
		if (!zoneIds.contains(devices.get(0).getZoneCode())) {
			auditUtil.auditRequest(
					String.format(MasterDataConstant.FAILURE_DECOMMISSION, DeviceDto.class.getSimpleName()),
					MasterDataConstant.AUDIT_SYSTEM,
					String.format(MasterDataConstant.FAILURE_DESC, DeviceErrorCode.INVALID_DEVICE_ZONE.getErrorCode(),
							DeviceErrorCode.INVALID_DEVICE_ZONE.getErrorMessage()),
					"ADM-512");
			throw new RequestException(DeviceErrorCode.INVALID_DEVICE_ZONE.getErrorCode(),
					DeviceErrorCode.INVALID_DEVICE_ZONE.getErrorMessage());
		}
		try {
			// check the device has mapped to any reg-Center
			if (!registrationCenterDeviceRepository.findByDeviceIdAndIsDeletedFalseOrIsDeletedIsNull(deviceId)
					.isEmpty()) {
				auditUtil.auditRequest(
						String.format(MasterDataConstant.FAILURE_DECOMMISSION, DeviceDto.class.getSimpleName()),
						MasterDataConstant.AUDIT_SYSTEM,
						String.format(MasterDataConstant.FAILURE_DESC,
								DeviceErrorCode.MAPPED_TO_REGCENTER.getErrorCode(),
								DeviceErrorCode.MAPPED_TO_REGCENTER.getErrorMessage()),
						"ADM-513");
				throw new RequestException(DeviceErrorCode.MAPPED_TO_REGCENTER.getErrorCode(),
						DeviceErrorCode.MAPPED_TO_REGCENTER.getErrorMessage());
			}
			decommissionedDevice = deviceRepository.decommissionDevice(deviceId, MetaDataUtils.getContextUser(),
					MetaDataUtils.getCurrentDateTime());

			// create Device history
			for (Device device : devices) {
				DeviceHistory deviceHistory = new DeviceHistory();
				MapperUtils.map(device, deviceHistory);
				MapperUtils.setBaseFieldValue(device, deviceHistory);
				deviceHistory.setIsActive(false);
				deviceHistory.setIsDeleted(true);
				deviceHistory.setUpdatedBy(MetaDataUtils.getContextUser());
				deviceHistory.setEffectDateTime(LocalDateTime.now(ZoneId.of("UTC")));
				deviceHistory.setDeletedDateTime(LocalDateTime.now(ZoneId.of("UTC")));
				deviceHistoryService.createDeviceHistory(deviceHistory);
			}

		} catch (DataAccessException | DataAccessLayerException exception) {
			auditUtil.auditRequest(
					String.format(MasterDataConstant.FAILURE_DECOMMISSION, DeviceDto.class.getSimpleName()),
					MasterDataConstant.AUDIT_SYSTEM,
					String.format(MasterDataConstant.FAILURE_DESC,
							DeviceErrorCode.DEVICE_DELETE_EXCEPTION.getErrorCode(),
							DeviceErrorCode.DEVICE_DELETE_EXCEPTION.getErrorMessage() + exception.getCause()),
					"ADM-514");
			throw new MasterDataServiceException(DeviceErrorCode.DEVICE_DELETE_EXCEPTION.getErrorCode(),
					DeviceErrorCode.DEVICE_DELETE_EXCEPTION.getErrorMessage() + exception.getCause());
		}
		if (decommissionedDevice > 0) {
			idResponseDto.setId(deviceId);
		}
		return idResponseDto;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see io.mosip.kernel.masterdata.service.RegistrationCenterService#
	 * updateRegistrationCenter1(java.util.List)
	 */
	@Transactional
	@Override
	public DeviceExtnDto updateDevice(DevicePutReqDto devicePutReqDto) {

		Device updDevice = null;
		Device updDeviecEntity = null;
		String deviecZone = devicePutReqDto.getZoneCode();
		DeviceHistory deviceHistory = new DeviceHistory();
		DeviceExtnDto deviceExtnDto = new DeviceExtnDto();

		// call method to check the machineZone will come under Accessed user zone or
		// not
		validateZone(deviecZone);
		try {

			// find requested device is there or not in Device Table
			Device renDevice = deviceRepository.findByIdAndLangCodeAndIsDeletedFalseOrIsDeletedIsNullNoIsActive(
					devicePutReqDto.getId(), devicePutReqDto.getLangCode());

			devicePutReqDto = masterdataCreationUtil.updateMasterData(Device.class, devicePutReqDto);

			if (renDevice == null && primaryLangCode.equals(devicePutReqDto.getLangCode())) {
				auditUtil.auditRequest(
						String.format(MasterDataConstant.FAILURE_DECOMMISSION, DeviceDto.class.getSimpleName()),
						MasterDataConstant.AUDIT_SYSTEM,
						String.format(MasterDataConstant.FAILURE_DESC, DeviceErrorCode.DECOMMISSIONED.getErrorCode(),
								DeviceErrorCode.DECOMMISSIONED.getErrorMessage()),
						"ADM-515");
				throw new MasterDataServiceException(DeviceErrorCode.DECOMMISSIONED.getErrorCode(),
						DeviceErrorCode.DECOMMISSIONED.getErrorMessage());
			} else if (renDevice == null && secondaryLang.equals(devicePutReqDto.getLangCode())) {
				// create new entry
				Device crtDeviceEntity = new Device();
				crtDeviceEntity = MetaDataUtils.setCreateMetaData(devicePutReqDto, crtDeviceEntity.getClass());
				crtDeviceEntity = deviceRepository.create(crtDeviceEntity);

				// updating Device history
				MapperUtils.map(crtDeviceEntity, deviceHistory);
				MapperUtils.setBaseFieldValue(crtDeviceEntity, deviceHistory);
				deviceHistory.setEffectDateTime(crtDeviceEntity.getCreatedDateTime());
				deviceHistory.setCreatedDateTime(crtDeviceEntity.getCreatedDateTime());
				deviceHistoryService.createDeviceHistory(deviceHistory);

				deviceExtnDto = MapperUtils.map(crtDeviceEntity, DeviceExtnDto.class);
			}
			if (renDevice != null) {
				// updating registration center
				updDeviecEntity = MetaDataUtils.setUpdateMetaData(devicePutReqDto, renDevice, false);
				updDeviecEntity.setIsActive(devicePutReqDto.getIsActive());

				// updating Device
				updDevice = deviceRepository.update(updDeviecEntity);

				// updating Device history
				MapperUtils.map(updDevice, deviceHistory);
				MapperUtils.setBaseFieldValue(updDevice, deviceHistory);
				deviceHistory.setEffectDateTime(updDevice.getUpdatedDateTime());
				deviceHistory.setUpdatedDateTime(updDevice.getUpdatedDateTime());
				deviceHistoryService.createDeviceHistory(deviceHistory);
				deviceExtnDto = MapperUtils.map(updDevice, DeviceExtnDto.class);
			}

		} catch (DataAccessLayerException | DataAccessException | IllegalArgumentException | IllegalAccessException
				| NoSuchFieldException | SecurityException exception) {
			auditUtil.auditRequest(String.format(MasterDataConstant.FAILURE_UPDATE, DeviceDto.class.getSimpleName()),
					MasterDataConstant.AUDIT_SYSTEM,
					String.format(MasterDataConstant.FAILURE_UPDATE,
							DeviceErrorCode.DEVICE_UPDATE_EXCEPTION.getErrorCode(),
							DeviceErrorCode.DEVICE_UPDATE_EXCEPTION.getErrorMessage()
									+ ExceptionUtils.parseException(exception)),
					"ADM-516");
			throw new MasterDataServiceException(DeviceErrorCode.DEVICE_UPDATE_EXCEPTION.getErrorCode(),
					DeviceErrorCode.DEVICE_UPDATE_EXCEPTION.getErrorMessage()
							+ ExceptionUtils.parseException(exception));
		}

		return deviceExtnDto;

	}

	// method to check the deviceZone will come under Accessed user zone or not
	private void validateZone(String deviceZone) {
		List<String> zoneIds;
		// get user zone and child zones list
		List<Zone> userZones = zoneUtils.getUserZones();
		zoneIds = userZones.parallelStream().map(Zone::getCode).collect(Collectors.toList());

		if (!(zoneIds.contains(deviceZone))) {
			// check the given device zones will come under accessed user zones
			throw new RequestException(DeviceErrorCode.INVALID_DEVICE_ZONE.getErrorCode(),
					DeviceErrorCode.INVALID_DEVICE_ZONE.getErrorMessage());
		}
	}

}
