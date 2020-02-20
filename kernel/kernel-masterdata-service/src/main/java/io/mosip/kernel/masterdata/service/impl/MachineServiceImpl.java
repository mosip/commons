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
import io.mosip.kernel.core.util.StringUtils;
import io.mosip.kernel.masterdata.constant.MachineErrorCode;
import io.mosip.kernel.masterdata.constant.MachinePutReqDto;
import io.mosip.kernel.masterdata.constant.MasterDataConstant;
import io.mosip.kernel.masterdata.dto.MachineDto;
import io.mosip.kernel.masterdata.dto.MachinePostReqDto;
import io.mosip.kernel.masterdata.dto.MachineRegistrationCenterDto;
import io.mosip.kernel.masterdata.dto.MachineTypeDto;
import io.mosip.kernel.masterdata.dto.PageDto;
import io.mosip.kernel.masterdata.dto.getresponse.MachineResponseDto;
import io.mosip.kernel.masterdata.dto.getresponse.extn.MachineExtnDto;
import io.mosip.kernel.masterdata.dto.postresponse.IdResponseDto;
import io.mosip.kernel.masterdata.dto.request.FilterDto;
import io.mosip.kernel.masterdata.dto.request.FilterValueDto;
import io.mosip.kernel.masterdata.dto.request.Pagination;
import io.mosip.kernel.masterdata.dto.request.SearchDto;
import io.mosip.kernel.masterdata.dto.request.SearchFilter;
import io.mosip.kernel.masterdata.dto.request.SearchSort;
import io.mosip.kernel.masterdata.dto.response.ColumnValue;
import io.mosip.kernel.masterdata.dto.response.FilterResponseDto;
import io.mosip.kernel.masterdata.dto.response.MachineSearchDto;
import io.mosip.kernel.masterdata.dto.response.PageResponseDto;
import io.mosip.kernel.masterdata.entity.Machine;
import io.mosip.kernel.masterdata.entity.MachineHistory;
import io.mosip.kernel.masterdata.entity.MachineSpecification;
import io.mosip.kernel.masterdata.entity.MachineType;
import io.mosip.kernel.masterdata.entity.RegistrationCenter;
import io.mosip.kernel.masterdata.entity.RegistrationCenterHistory;
import io.mosip.kernel.masterdata.entity.RegistrationCenterMachine;
import io.mosip.kernel.masterdata.entity.RegistrationCenterMachineDevice;
import io.mosip.kernel.masterdata.entity.RegistrationCenterUserMachine;
import io.mosip.kernel.masterdata.entity.Zone;
import io.mosip.kernel.masterdata.exception.DataNotFoundException;
import io.mosip.kernel.masterdata.exception.MasterDataServiceException;
import io.mosip.kernel.masterdata.exception.RequestException;
import io.mosip.kernel.masterdata.repository.MachineHistoryRepository;
import io.mosip.kernel.masterdata.repository.MachineRepository;
import io.mosip.kernel.masterdata.repository.MachineSpecificationRepository;
import io.mosip.kernel.masterdata.repository.MachineTypeRepository;
import io.mosip.kernel.masterdata.repository.RegistrationCenterHistoryRepository;
import io.mosip.kernel.masterdata.repository.RegistrationCenterMachineDeviceRepository;
import io.mosip.kernel.masterdata.repository.RegistrationCenterMachineRepository;
import io.mosip.kernel.masterdata.repository.RegistrationCenterMachineUserRepository;
import io.mosip.kernel.masterdata.repository.RegistrationCenterRepository;
import io.mosip.kernel.masterdata.service.MachineHistoryService;
import io.mosip.kernel.masterdata.service.MachineService;
import io.mosip.kernel.masterdata.service.ZoneService;
import io.mosip.kernel.masterdata.utils.AuditUtil;
import io.mosip.kernel.masterdata.utils.ExceptionUtils;
import io.mosip.kernel.masterdata.utils.MachineUtil;
import io.mosip.kernel.masterdata.utils.MapperUtils;
import io.mosip.kernel.masterdata.utils.MasterDataFilterHelper;
import io.mosip.kernel.masterdata.utils.MasterdataCreationUtil;
import io.mosip.kernel.masterdata.utils.MasterdataSearchHelper;
import io.mosip.kernel.masterdata.utils.MetaDataUtils;
import io.mosip.kernel.masterdata.utils.OptionalFilter;
import io.mosip.kernel.masterdata.utils.PageUtils;
import io.mosip.kernel.masterdata.utils.RegistrationCenterValidator;
import io.mosip.kernel.masterdata.utils.ZoneUtils;
import io.mosip.kernel.masterdata.validator.FilterColumnValidator;
import io.mosip.kernel.masterdata.validator.FilterTypeEnum;
import io.mosip.kernel.masterdata.validator.FilterTypeValidator;

/**
 * This class have methods to fetch a Machine Details
 * 
 * @author Megha Tanga
 * @author Ritesh Sinha
 * @author Sidhant Agarwal
 * @author Ravi Kant
 * @since 1.0.0
 *
 */
@Service
public class MachineServiceImpl implements MachineService {

	/**
	 * Field to hold Machine Repository object
	 */
	@Autowired
	MachineRepository machineRepository;

	@Autowired
	private AuditUtil auditUtil;

	@Autowired
	MachineSpecificationRepository machineSpecificationRepository;

	@Autowired
	MachineHistoryService machineHistoryService;

	@Autowired
	MachineTypeRepository machineTypeRepository;

	@Autowired
	RegistrationCenterMachineRepository registrationCenterMachineRepository;

	@Autowired
	RegistrationCenterMachineUserRepository registrationCenterMachineUserRepository;

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
	private MachineUtil machineUtil;

	@Autowired
	private PageUtils pageUtils;

	@Autowired
	private ZoneService zoneService;

	@Autowired
	private MachineHistoryRepository machineHistoryRepository;

	@Autowired
	private RegistrationCenterValidator registrationCenterValidator;

	@Autowired
	private MasterdataCreationUtil masterdataCreationUtil;

	@Autowired
	private RegistrationCenterRepository registrationCenterRepository;

	@Autowired
	private RegistrationCenterHistoryRepository registrationCenterHistoryRepository;

	/**
	 * get list of secondary languages supported by MOSIP from configuration file
	 */
	@Value("${mosip.primary-language}")
	private String primaryLang;

	@Value("${mosip.primary-language}")
	private String primaryLangCode;

	/*
	 * (non-Javadoc)
	 * 
	 * @see io.mosip.kernel.masterdata.service.MachineService#getMachine(java.lang.
	 * String, java.lang.String)
	 */
	@Override
	public MachineResponseDto getMachine(String id, String langCode) {
		List<Machine> machineList = null;
		List<MachineDto> machineDtoList = null;
		MachineResponseDto machineResponseIdDto = new MachineResponseDto();
		try {
			machineList = machineRepository.findAllByIdAndLangCodeAndIsDeletedFalseorIsDeletedIsNull(id, langCode);
		} catch (DataAccessException | DataAccessLayerException e) {
			throw new MasterDataServiceException(MachineErrorCode.MACHINE_FETCH_EXCEPTION.getErrorCode(),
					MachineErrorCode.MACHINE_FETCH_EXCEPTION.getErrorMessage() + ExceptionUtils.parseException(e));
		}
		if (machineList != null && !machineList.isEmpty()) {
			machineDtoList = MapperUtils.mapAll(machineList, MachineDto.class);
		} else {

			throw new DataNotFoundException(MachineErrorCode.MACHINE_NOT_FOUND_EXCEPTION.getErrorCode(),
					MachineErrorCode.MACHINE_NOT_FOUND_EXCEPTION.getErrorMessage());

		}
		machineResponseIdDto.setMachines(machineDtoList);
		return machineResponseIdDto;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see io.mosip.kernel.masterdata.service.MachineService#getMachineAll()
	 */
	@Override
	public MachineResponseDto getMachineAll() {
		List<Machine> machineList = null;

		List<MachineDto> machineDtoList = null;
		MachineResponseDto machineResponseDto = new MachineResponseDto();
		try {
			machineList = machineRepository.findAllByIsDeletedFalseOrIsDeletedIsNull();

		} catch (DataAccessException | DataAccessLayerException e) {
			throw new MasterDataServiceException(MachineErrorCode.MACHINE_FETCH_EXCEPTION.getErrorCode(),
					MachineErrorCode.MACHINE_FETCH_EXCEPTION.getErrorMessage() + ExceptionUtils.parseException(e));
		}
		if (machineList != null && !machineList.isEmpty()) {
			machineDtoList = MapperUtils.mapAll(machineList, MachineDto.class);

		} else {
			throw new DataNotFoundException(MachineErrorCode.MACHINE_NOT_FOUND_EXCEPTION.getErrorCode(),
					MachineErrorCode.MACHINE_NOT_FOUND_EXCEPTION.getErrorMessage());
		}
		machineResponseDto.setMachines(machineDtoList);
		return machineResponseDto;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see io.mosip.kernel.masterdata.service.MachineService#getMachine(java.lang.
	 * String)
	 */
	@Override
	public MachineResponseDto getMachine(String langCode) {
		MachineResponseDto machineResponseDto = new MachineResponseDto();
		List<Machine> machineList = null;
		List<MachineDto> machineDtoList = null;
		try {
			machineList = machineRepository.findAllByLangCodeAndIsDeletedFalseOrIsDeletedIsNull(langCode);
		} catch (DataAccessException | DataAccessLayerException e) {
			throw new MasterDataServiceException(MachineErrorCode.MACHINE_FETCH_EXCEPTION.getErrorCode(),
					MachineErrorCode.MACHINE_FETCH_EXCEPTION.getErrorMessage() + ExceptionUtils.parseException(e));
		}
		if (machineList != null && !machineList.isEmpty()) {
			machineDtoList = MapperUtils.mapAll(machineList, MachineDto.class);

		} else {
			throw new DataNotFoundException(MachineErrorCode.MACHINE_NOT_FOUND_EXCEPTION.getErrorCode(),
					MachineErrorCode.MACHINE_NOT_FOUND_EXCEPTION.getErrorMessage());
		}
		machineResponseDto.setMachines(machineDtoList);
		return machineResponseDto;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * io.mosip.kernel.masterdata.service.MachineService#deleteMachine(java.lang.
	 * String)
	 */
	@Override
	@Transactional
	public IdResponseDto deleteMachine(String id) {
		Machine delMachine = null;
		try {
			List<Machine> renMachineList = machineRepository.findMachineByIdAndIsDeletedFalseorIsDeletedIsNull(id);
			if (!renMachineList.isEmpty()) {
				for (Machine renMachine : renMachineList) {

					List<RegistrationCenterMachine> registrationCenterMachineList = registrationCenterMachineRepository
							.findByMachineIdAndIsDeletedFalseOrIsDeletedIsNull(renMachine.getId());
					List<RegistrationCenterUserMachine> registrationCenterMachineUser = registrationCenterMachineUserRepository
							.findByMachineIdAndIsDeletedFalseOrIsDeletedIsNull(renMachine.getId());
					List<RegistrationCenterMachineDevice> registrationCenterMachineDevice = registrationCenterMachineDeviceRepository
							.findByMachineIdAndIsDeletedFalseOrIsDeletedIsNull(renMachine.getId());

					if (registrationCenterMachineList.isEmpty() && registrationCenterMachineUser.isEmpty()
							&& registrationCenterMachineDevice.isEmpty()) {
						MetaDataUtils.setDeleteMetaData(renMachine);
						delMachine = machineRepository.update(renMachine);

						MachineHistory machineHistory = new MachineHistory();
						MapperUtils.map(delMachine, machineHistory);
						MapperUtils.setBaseFieldValue(delMachine, machineHistory);

						machineHistory.setEffectDateTime(delMachine.getDeletedDateTime());
						machineHistory.setDeletedDateTime(delMachine.getDeletedDateTime());
						machineHistoryService.createMachineHistory(machineHistory);
					} else {
						throw new RequestException(MachineErrorCode.DEPENDENCY_EXCEPTION.getErrorCode(),
								MachineErrorCode.DEPENDENCY_EXCEPTION.getErrorMessage());
					}
				}
			} else {
				throw new RequestException(MachineErrorCode.MACHINE_NOT_FOUND_EXCEPTION.getErrorCode(),
						MachineErrorCode.MACHINE_NOT_FOUND_EXCEPTION.getErrorMessage());
			}

		} catch (DataAccessLayerException | DataAccessException e) {
			throw new MasterDataServiceException(MachineErrorCode.MACHINE_DELETE_EXCEPTION.getErrorCode(),
					MachineErrorCode.MACHINE_DELETE_EXCEPTION.getErrorMessage() + ExceptionUtils.parseException(e));
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
	public PageDto<MachineRegistrationCenterDto> getMachinesByRegistrationCenter(String regCenterId, int page, int size,
			String orderBy, String direction) {
		PageDto<MachineRegistrationCenterDto> pageDto = new PageDto<>();
		List<MachineRegistrationCenterDto> machineRegistrationCenterDtoList = null;
		Page<Machine> pageEntity = null;

		try {
			pageEntity = machineRepository.findMachineByRegCenterId(regCenterId,
					PageRequest.of(page, size, Sort.by(Direction.fromString(direction), orderBy)));
		} catch (DataAccessLayerException | DataAccessException e) {
			throw new MasterDataServiceException(
					MachineErrorCode.REGISTRATION_CENTER_MACHINE_FETCH_EXCEPTION.getErrorCode(),
					MachineErrorCode.REGISTRATION_CENTER_MACHINE_FETCH_EXCEPTION.getErrorMessage()
							+ ExceptionUtils.parseException(e));
		}
		if (pageEntity != null && !pageEntity.getContent().isEmpty()) {
			machineRegistrationCenterDtoList = MapperUtils.mapAll(pageEntity.getContent(),
					MachineRegistrationCenterDto.class);
			for (MachineRegistrationCenterDto machineRegistrationCenterDto1 : machineRegistrationCenterDtoList) {
				machineRegistrationCenterDto1.setRegCentId(regCenterId);
			}
		} else {
			throw new RequestException(MachineErrorCode.MACHINE_NOT_FOUND_EXCEPTION.getErrorCode(),
					MachineErrorCode.MACHINE_NOT_FOUND_EXCEPTION.getErrorMessage());
		}

		pageDto.setPageNo(pageEntity.getNumber());
		pageDto.setPageSize(pageEntity.getSize());
		pageDto.setSort(pageEntity.getSort());
		pageDto.setTotalItems(pageEntity.getTotalElements());
		pageDto.setTotalPages(pageEntity.getTotalPages());
		pageDto.setData(machineRegistrationCenterDtoList);

		return pageDto;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * io.mosip.kernel.masterdata.service.MachineService#searchMachine(io.mosip.
	 * kernel.masterdata.dto.request.SearchDto)
	 */
	@Override
	public PageResponseDto<MachineSearchDto> searchMachine(SearchDto dto) {
		PageResponseDto<MachineSearchDto> pageDto = new PageResponseDto<>();
		List<MachineSearchDto> machines = null;
		List<SearchFilter> addList = new ArrayList<>();
		List<SearchFilter> mapStatusList = new ArrayList<>();
		List<SearchFilter> removeList = new ArrayList<>();
		List<String> mappedMachineIdList = null;
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
			if (MasterDataConstant.ZONE.equalsIgnoreCase(column)) {
				Zone zone = getZone(filter);
				if (zone != null) {
					zones = zoneUtils.getZones(zone);
					zoneFilter.addAll(buildZoneFilter(zones));
				}
				removeList.add(filter);
				flag = false;
			}

			if (column.equalsIgnoreCase("mapStatus")) {

				if (filter.getValue().equalsIgnoreCase("assigned")) {
					mappedMachineIdList = machineRepository.findMappedMachineId(langCode);
					mapStatusList.addAll(buildRegistrationCenterMachineTypeSearchFilter(mappedMachineIdList));
					if (!dto.getFilters().isEmpty() && mappedMachineIdList.isEmpty()) {
						pageDto = pageUtils.sortPage(machines, dto.getSort(), dto.getPagination());
						return pageDto;
					}

				} else {
					if (filter.getValue().equalsIgnoreCase("unassigned")) {
						mappedMachineIdList = machineRepository.findNotMappedMachineId(langCode);
						mapStatusList.addAll(buildRegistrationCenterMachineTypeSearchFilter(mappedMachineIdList));
						isAssigned = false;
						if (!dto.getFilters().isEmpty() && mappedMachineIdList.isEmpty()) {
							pageDto = pageUtils.sortPage(machines, dto.getSort(), dto.getPagination());
							return pageDto;
						}
					} else {
						auditUtil.auditRequest(
								String.format(MasterDataConstant.SEARCH_FAILED, MachineSearchDto.class.getSimpleName()),
								MasterDataConstant.AUDIT_SYSTEM,
								String.format(MasterDataConstant.FAILURE_DESC,
										MachineErrorCode.INVALID_MACHINE_FILTER_VALUE_EXCEPTION.getErrorCode(),
										MachineErrorCode.INVALID_MACHINE_FILTER_VALUE_EXCEPTION.getErrorMessage()),
								"ADM-534");
						throw new RequestException(
								MachineErrorCode.INVALID_MACHINE_FILTER_VALUE_EXCEPTION.getErrorCode(),
								MachineErrorCode.INVALID_MACHINE_FILTER_VALUE_EXCEPTION.getErrorMessage());
					}

				}
				removeList.add(filter);
			}

			if (column.equalsIgnoreCase("machineTypeName")) {
				filter.setColumnName("name");
				typeName = filter.getValue();
				if (filterValidator.validate(MachineTypeDto.class, Arrays.asList(filter))) {
					List<Object[]> machineSpecs = machineRepository
							.findMachineSpecByMachineTypeNameAndLangCode(typeName, langCode);
					removeList.add(filter);
					addList.addAll(buildMachineSpecificationSearchFilter(machineSpecs));
				}
			}

		}
		if (flag) {
			zones = zoneUtils.getUserZones();
			if (zones != null && !zones.isEmpty()) {
				zoneFilter.addAll(buildZoneFilter(zones));
			} else {
				auditUtil.auditRequest(
						String.format(MasterDataConstant.SEARCH_FAILED, MachineSearchDto.class.getSimpleName()),
						MasterDataConstant.AUDIT_SYSTEM,
						String.format(MasterDataConstant.FAILURE_DESC,
								MachineErrorCode.MACHINE_NOT_TAGGED_TO_ZONE.getErrorCode(),
								MachineErrorCode.MACHINE_NOT_TAGGED_TO_ZONE.getErrorMessage()),
						"ADM-535");

				throw new MasterDataServiceException(MachineErrorCode.MACHINE_NOT_TAGGED_TO_ZONE.getErrorCode(),
						MachineErrorCode.MACHINE_NOT_TAGGED_TO_ZONE.getErrorMessage());
			}
		}
		pageUtils.validateSortField(MachineSearchDto.class, Machine.class, dto.getSort());
		dto.getFilters().removeAll(removeList);
		Pagination pagination = dto.getPagination();
		List<SearchSort> sort = dto.getSort();
		dto.setPagination(new Pagination(0, Integer.MAX_VALUE));
		dto.setSort(Collections.emptyList());
		if (filterValidator.validate(MachineSearchDto.class, dto.getFilters())) {

			OptionalFilter optionalFilter = new OptionalFilter(addList);
			OptionalFilter zoneOptionalFilter = new OptionalFilter(zoneFilter);
			Page<Machine> page = null;
			if (mapStatusList.isEmpty() || addList.isEmpty()) {
				addList.addAll(mapStatusList);
				page = masterdataSearchHelper.searchMasterdata(Machine.class, dto,
						new OptionalFilter[] { optionalFilter, zoneOptionalFilter });
			} else {
				page = masterdataSearchHelper.nativeMachineQuerySearch(dto, typeName, zones, isAssigned);
			}
			if (page.getContent() != null && !page.getContent().isEmpty()) {
				machines = MapperUtils.mapAll(page.getContent(), MachineSearchDto.class);
				setMachineMetadata(machines, zones);
				setMachineTypeNames(machines);
				setMapStatus(machines);
				machines.forEach(machine -> {
					if (machine.getMapStatus() == null) {
						machine.setMapStatus("unassigned");
					}
				});
				pageDto = pageUtils.sortPage(machines, sort, pagination);
			}

		}
		return pageDto;
	}

	/**
	 * Method to set each machine zone meta data.
	 * 
	 * @param list  list of {@link MachineSearchDto}.
	 * @param zones the list of zones.
	 */
	private void setMachineMetadata(List<MachineSearchDto> list, List<Zone> zones) {
		list.forEach(i -> setZoneMetadata(i, zones));
	}

	/**
	 * Method to set MachineType Name for each Machine.
	 * 
	 * @param list the {@link MachineSearchDto}.
	 */
	private void setMachineTypeNames(List<MachineSearchDto> list) {
		List<MachineSpecification> machineSpecifications = machineUtil.getMachineSpec();
		List<MachineType> machineTypes = machineUtil.getMachineTypes();
		list.forEach(machineSearchDto -> {
			machineSpecifications.forEach(s -> {
				if (s.getId().equals(machineSearchDto.getMachineSpecId())
						&& s.getLangCode().equals(machineSearchDto.getLangCode())) {
					String typeCode = s.getMachineTypeCode();
					machineTypes.forEach(mt -> {
						if (mt.getCode().equals(typeCode) && mt.getLangCode().equals(s.getLangCode())) {
							machineSearchDto.setMachineTypeName(mt.getName());
						}
					});
				}
			});
		});
	}

	/**
	 * Method to set Map status of each Machine.
	 * 
	 * @param list the {@link MachineSearchDto}.
	 */
	private void setMapStatus(List<MachineSearchDto> list) {
		List<RegistrationCenterMachine> centerMachineList = machineUtil.getAllMachineCentersList();
		List<RegistrationCenter> registrationCenterList = machineUtil.getAllRegistrationCenters();
		list.forEach(machineSearchDto -> {
			centerMachineList.forEach(centerMachine -> {
				if (centerMachine.getMachine().getId().equals(machineSearchDto.getId())
						&& centerMachine.getLangCode().equals(machineSearchDto.getLangCode())) {
					String regId = centerMachine.getRegistrationCenter().getId();
					registrationCenterList.forEach(registrationCenter -> {
						if (registrationCenter.getId().equals(regId)
								&& centerMachine.getLangCode().equals(registrationCenter.getLangCode())) {
							machineSearchDto.setMapStatus(registrationCenter.getName());
						}
					});
				}
			});
		});
	}

	/**
	 * Method to set Zone metadata
	 * 
	 * @param machines metadata to be added
	 * @param zones    list of zones
	 * 
	 */
	private void setZoneMetadata(MachineSearchDto machines, List<Zone> zones) {
		Optional<Zone> zone = zones.stream().filter(
				i -> i.getCode().equals(machines.getZoneCode()) && i.getLangCode().equals(machines.getLangCode()))
				.findFirst();
		if (zone.isPresent()) {
			machines.setZone(zone.get().getName());
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
			return null;
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
	 * This method return Machine Id list filters.
	 * 
	 * @param machineIdList the Machine Id list.
	 * @return the list of {@link SearchFilter}.
	 */
	private List<SearchFilter> buildRegistrationCenterMachineTypeSearchFilter(List<String> machineIdList) {
		if (machineIdList != null && !machineIdList.isEmpty())
			return machineIdList.stream().filter(Objects::nonNull).map(this::buildRegistrationCenterMachineType)
					.collect(Collectors.toList());
		return Collections.emptyList();
	}

	/**
	 * This method return Machine Specification list filters.
	 * 
	 * @param machineSpecification the list of Machine Specification.
	 * @return the list of {@link SearchFilter}.
	 */
	private List<SearchFilter> buildMachineSpecificationSearchFilter(List<Object[]> machineSpecification) {
		List<SearchFilter> searchFilter = new ArrayList<>();
		for (Object[] objects : machineSpecification) {
			SearchFilter filter = new SearchFilter();
			filter.setColumnName("machineSpecId");
			filter.setType(FilterTypeEnum.EQUALS.name());
			filter.setValue(objects[0].toString());
			searchFilter.add(filter);
		}
		return searchFilter;
	}

	/**
	 * This method provide search filter for provided machine id.
	 * 
	 * @param machineId the machine id.
	 * @return the {@link SearchFilter}.
	 */
	private SearchFilter buildRegistrationCenterMachineType(String machineId) {
		SearchFilter filter = new SearchFilter();
		filter.setColumnName("id");
		filter.setType(FilterTypeEnum.EQUALS.name());
		filter.setValue(machineId);
		return filter;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * io.mosip.kernel.masterdata.service.MachineService#machineFilterValues(io.
	 * mosip.kernel.masterdata.dto.request.FilterValueDto)
	 */
	@Override
	public FilterResponseDto machineFilterValues(FilterValueDto filterValueDto) {
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
		if (filterColumnValidator.validate(FilterDto.class, filterValueDto.getFilters(), Machine.class)) {
			for (FilterDto filterDto : filterValueDto.getFilters()) {
				List<?> filterValues = masterDataFilterHelper.filterValues(Machine.class, filterDto, filterValueDto);
				filterValues.forEach(filterValue -> {
					ColumnValue columnValue = new ColumnValue();
					columnValue.setFieldID(filterDto.getColumnName());
					columnValue.setFieldValue(filterValue.toString());
					columnValueList.add(columnValue);
				});
			}
			filterResponseDto.setFilters(columnValueList);
		}
		return filterResponseDto;
	}

	@Override
	@Transactional
	public IdResponseDto decommissionMachine(String machineId) {
		IdResponseDto idResponseDto = new IdResponseDto();
		int decommissionedMachine = 0;

		// get machine from DB by given id
		List<Machine> machines = machineRepository
				.findMachineByIdAndIsDeletedFalseorIsDeletedIsNullNoIsActive(machineId);

		// machine is not in DB
		if (machines.isEmpty()) {
			auditUtil.auditRequest(
					String.format(MasterDataConstant.FAILURE_DECOMMISSION, MachineSearchDto.class.getSimpleName()),
					MasterDataConstant.AUDIT_SYSTEM,
					String.format(MasterDataConstant.FAILURE_DESC,
							MachineErrorCode.MACHINE_NOT_EXIST_EXCEPTION.getErrorCode(),
							String.format(MachineErrorCode.MACHINE_NOT_EXIST_EXCEPTION.getErrorMessage(), machineId)),
					"ADM-536");
			throw new RequestException(MachineErrorCode.MACHINE_NOT_EXIST_EXCEPTION.getErrorCode(),
					String.format(MachineErrorCode.MACHINE_NOT_EXIST_EXCEPTION.getErrorMessage(), machineId));
		}

		List<String> zoneIds;

		// get user zone and child zones list
		List<Zone> userZones = zoneUtils.getUserZones();
		zoneIds = userZones.parallelStream().map(Zone::getCode).collect(Collectors.toList());

		// check the given device and registration center zones are come under user zone
		if (!zoneIds.contains(machines.get(0).getZoneCode())) {
			auditUtil.auditRequest(
					String.format(MasterDataConstant.FAILURE_DECOMMISSION, MachineSearchDto.class.getSimpleName()),
					MasterDataConstant.AUDIT_SYSTEM,
					String.format(MasterDataConstant.FAILURE_DESC,
							MachineErrorCode.INVALIDE_MACHINE_ZONE.getErrorCode(),
							MachineErrorCode.INVALIDE_MACHINE_ZONE.getErrorMessage()),
					"ADM-537");
			throw new RequestException(MachineErrorCode.INVALIDE_MACHINE_ZONE.getErrorCode(),
					MachineErrorCode.INVALIDE_MACHINE_ZONE.getErrorMessage());
		}
		try {
			if (!registrationCenterMachineRepository.findByMachineIdAndIsDeletedFalseOrIsDeletedIsNull(machineId)
					.isEmpty()) {
				auditUtil.auditRequest(
						String.format(MasterDataConstant.FAILURE_DECOMMISSION, MachineSearchDto.class.getSimpleName()),
						MasterDataConstant.AUDIT_SYSTEM,
						String.format(MasterDataConstant.FAILURE_DESC,
								MachineErrorCode.INVALIDE_MACHINE_ZONE.getErrorCode(),
								MachineErrorCode.INVALIDE_MACHINE_ZONE.getErrorMessage()),
						"ADM-538");
				throw new RequestException(MachineErrorCode.MAPPED_TO_REGCENTER.getErrorCode(),
						MachineErrorCode.MAPPED_TO_REGCENTER.getErrorMessage());
			}
			decommissionedMachine = machineRepository.decommissionMachine(machineId, MetaDataUtils.getContextUser(),
					MetaDataUtils.getCurrentDateTime());

			// create Machine history
			for (Machine machine : machines) {
				MachineHistory machineHistory = new MachineHistory();
				MapperUtils.map(machine, machineHistory);
				MapperUtils.setBaseFieldValue(machine, machineHistory);
				machineHistory.setIsActive(false);
				machineHistory.setIsDeleted(true);
				machineHistory.setUpdatedBy(MetaDataUtils.getContextUser());
				machineHistory.setEffectDateTime(LocalDateTime.now(ZoneId.of("UTC")));
				machineHistory.setDeletedDateTime(LocalDateTime.now(ZoneId.of("UTC")));
				machineHistoryRepository.create(machineHistory);
			}

		} catch (DataAccessException | DataAccessLayerException exception) {
			auditUtil.auditRequest(
					String.format(MasterDataConstant.FAILURE_DECOMMISSION, MachineSearchDto.class.getSimpleName()),
					MasterDataConstant.AUDIT_SYSTEM,
					String.format(MachineErrorCode.MACHINE_DELETE_EXCEPTION.getErrorCode(),
							MachineErrorCode.MACHINE_DELETE_EXCEPTION.getErrorMessage() + exception.getCause()),
					"ADM-539");
			throw new MasterDataServiceException(MachineErrorCode.MACHINE_DELETE_EXCEPTION.getErrorCode(),
					MachineErrorCode.MACHINE_DELETE_EXCEPTION.getErrorMessage() + exception.getCause());
		}
		if (decommissionedMachine > 0) {
			idResponseDto.setId(machineId);
		}
		return idResponseDto;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * io.mosip.kernel.masterdata.service.MachineService#createMachine1(io.mosip.
	 * kernel.masterdata.dto.MachinePostReqDto)
	 */
	@Override
	@Transactional
	public MachineExtnDto createMachine(MachinePostReqDto machinePostReqDto) {
		Machine machineEntity = null;
		MachineHistory machineHistoryEntity = null;
		Machine crtMachine = null;
		String uniqueId = "";
		String machineZone = machinePostReqDto.getZoneCode();

		// call method to check the machineZone will come under Accessed user zone or
		// not
		validateZone(machineZone);
		try {
			// call method to set isActive value based on primary/Secondary language
			machinePostReqDto = masterdataCreationUtil.createMasterData(Machine.class, machinePostReqDto);

			machineEntity = MetaDataUtils.setCreateMetaData(machinePostReqDto, Machine.class);

			if (StringUtils.isNotEmpty(primaryLang) && primaryLang.equals(machinePostReqDto.getLangCode())) {
				// MachineId from the mid_Seq Table,MachineId get by calling MachineIdGenerator
				// API method generateMachineId()
				uniqueId = registrationCenterValidator.generateMachineIdOrvalidateWithDB(uniqueId);
				machineEntity.setId(uniqueId);
			}

			// creating a Machine
			crtMachine = machineRepository.create(machineEntity);

			// creating Machine history
			machineHistoryEntity = MetaDataUtils.setCreateMetaData(crtMachine, MachineHistory.class);
			machineHistoryEntity.setEffectDateTime(crtMachine.getCreatedDateTime());
			machineHistoryEntity.setCreatedDateTime(crtMachine.getCreatedDateTime());
			machineHistoryService.createMachineHistory(machineHistoryEntity);

		} catch (DataAccessLayerException | DataAccessException | IllegalArgumentException | IllegalAccessException
				| NoSuchFieldException | SecurityException exception) {
			auditUtil.auditRequest(
					String.format(MasterDataConstant.FAILURE_DECOMMISSION, MachineDto.class.getSimpleName()),
					MasterDataConstant.AUDIT_SYSTEM,
					String.format(MasterDataConstant.FAILURE_DESC,
							MachineErrorCode.MACHINE_DELETE_EXCEPTION.getErrorCode(),
							MachineErrorCode.MACHINE_DELETE_EXCEPTION.getErrorMessage() + exception.getCause()),
					"ADM-540");
			throw new MasterDataServiceException(MachineErrorCode.MACHINE_INSERT_EXCEPTION.getErrorCode(),
					MachineErrorCode.MACHINE_INSERT_EXCEPTION.getErrorMessage()
							+ ExceptionUtils.parseException(exception));
		}
		auditUtil.auditRequest(String.format(MasterDataConstant.SUCCESSFUL_CREATE, MachineDto.class.getSimpleName()),
				MasterDataConstant.AUDIT_SYSTEM, String.format(MasterDataConstant.SUCCESSFUL_CREATE_DESC,
						MachineDto.class.getSimpleName(), crtMachine.getId()),
				"ADM-541");
		return MapperUtils.map(crtMachine, MachineExtnDto.class);

	}

	// method to check the machineZone will come under Accessed user zone or not
	private void validateZone(String machineZone) {
		List<String> zoneIds;
		// get user zone and child zones list
		List<Zone> userZones = zoneUtils.getUserZones();
		zoneIds = userZones.parallelStream().map(Zone::getCode).collect(Collectors.toList());

		if (!(zoneIds.contains(machineZone))) {
			// check the given machine zones will come under accessed user zones
			throw new RequestException(MachineErrorCode.INVALIDE_MACHINE_ZONE.getErrorCode(),
					MachineErrorCode.INVALIDE_MACHINE_ZONE.getErrorMessage());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see io.mosip.kernel.masterdata.service.RegistrationCenterService#
	 * updateRegistrationCenter1(java.util.List)
	 */
	@Transactional
	@Override
	public MachineExtnDto updateMachine(MachinePutReqDto machinePutReqDto) {

		Machine updMachine = null;
		Machine updMachineEntity = null;
		String machineZone = machinePutReqDto.getZoneCode();

		// call method to check the machineZone will come under Accessed user zone or
		// not
		validateZone(machineZone);
		try {

			// find requested machine is there or not in Machine Table
			Machine renMachine = machineRepository
					.findMachineByIdAndLangCodeAndIsDeletedFalseorIsDeletedIsNullWithoutActiveStatusCheck(
							machinePutReqDto.getId(), machinePutReqDto.getLangCode());

			if (renMachine != null) {

				machinePutReqDto = masterdataCreationUtil.updateMasterData(Machine.class, machinePutReqDto);

				// call method to update NumKiosis value in regCneter table.
				// for the regCenter id which has been mapped with the given machine id
				updateNumKiosksRegCenter(machinePutReqDto, renMachine);

				// updating registration center
				updMachineEntity = MetaDataUtils.setUpdateMetaData(machinePutReqDto, renMachine, false);

				// updating Machine
				updMachine = machineRepository.update(updMachineEntity);

				// updating Machine history
				MachineHistory machineHistory = new MachineHistory();
				MapperUtils.map(updMachine, machineHistory);
				MapperUtils.setBaseFieldValue(updMachine, machineHistory);
				machineHistory.setEffectDateTime(updMachine.getUpdatedDateTime());
				machineHistory.setUpdatedDateTime(updMachine.getUpdatedDateTime());
				machineHistoryService.createMachineHistory(machineHistory);

			} else {
				// if given Id and language code is not present in DB
				auditUtil.auditRequest(
						String.format(MasterDataConstant.FAILURE_UPDATE, MachinePutReqDto.class.getSimpleName()),
						MasterDataConstant.AUDIT_SYSTEM,
						String.format(MasterDataConstant.FAILURE_DESC,
								MachineErrorCode.MACHINE_NOT_FOUND_EXCEPTION.getErrorCode(),
								MachineErrorCode.MACHINE_NOT_FOUND_EXCEPTION.getErrorMessage()),
						"ADM-542");
				throw new RequestException(MachineErrorCode.MACHINE_NOT_FOUND_EXCEPTION.getErrorCode(),
						MachineErrorCode.MACHINE_NOT_FOUND_EXCEPTION.getErrorMessage());
			}
		} catch (DataAccessLayerException | DataAccessException | IllegalArgumentException | IllegalAccessException
				| NoSuchFieldException | SecurityException exception) {
			auditUtil.auditRequest(
					String.format(MasterDataConstant.FAILURE_UPDATE, MachinePutReqDto.class.getSimpleName()),
					MasterDataConstant.AUDIT_SYSTEM,
					String.format(MasterDataConstant.FAILURE_DESC,
							MachineErrorCode.MACHINE_UPDATE_EXCEPTION.getErrorCode(),
							MachineErrorCode.MACHINE_UPDATE_EXCEPTION.getErrorMessage()
									+ ExceptionUtils.parseException(exception)),
					"ADM-543");
			throw new MasterDataServiceException(MachineErrorCode.MACHINE_UPDATE_EXCEPTION.getErrorCode(),
					MachineErrorCode.MACHINE_UPDATE_EXCEPTION.getErrorMessage()
							+ ExceptionUtils.parseException(exception));
		}
		auditUtil.auditRequest(String.format(MasterDataConstant.SUCCESSFUL_UPDATE, MachineDto.class.getSimpleName()),
				MasterDataConstant.AUDIT_SYSTEM, String.format(MasterDataConstant.SUCCESSFUL_UPDATE_DESC,
						MachineDto.class.getSimpleName(), updMachine.getId()),
				"ADM-544");
		return MapperUtils.map(updMachine, MachineExtnDto.class);

	}

	// method to update the NumKiosis for the regCenter id which has got mapped with
	// machine id
	private void updateNumKiosksRegCenter(MachinePutReqDto machinePutReqDto, Machine renMachine) {
		// find given machine id is attached to which regCenter center id
		List<RegistrationCenterMachine> regCenterMachines = registrationCenterMachineRepository
				.findByMachineIdAndIsDeletedFalseOrIsDeletedIsNull(renMachine.getId());

		// given machine id is attached to regCenter center or not
		if (!regCenterMachines.isEmpty()) {
			String regCenterId = regCenterMachines.get(0).getRegistrationCenterMachinePk().getRegCenterId();
			List<RegistrationCenter> renRegistrationCenters = registrationCenterRepository
					.findByRegIdAndIsDeletedFalseOrNull(regCenterId);

			// requested machine is true and in DB machine false state
			if (machinePutReqDto.getIsActive() && !renMachine.getIsActive()) {

				// update the NumberOfKiosks by 1(increase)
				for (RegistrationCenter registrationCenter : renRegistrationCenters) {
					short kiosis = (short) (registrationCenter.getNumberOfKiosks() + 1);
					registrationCenter.setNumberOfKiosks(kiosis);
					registrationCenter.setUpdatedBy(MetaDataUtils.getContextUser());
					registrationCenter.setUpdatedDateTime(LocalDateTime.now(ZoneId.of("UTC")));
					RegistrationCenter updRegistrationCenter = registrationCenterRepository.update(registrationCenter);

					// call method to update RegCenter history table
					updateRegCenterHistory(updRegistrationCenter);
				}

				// requested machine is false and in DB machine true state
			} else if (!machinePutReqDto.getIsActive() && renMachine.getIsActive()) {

				// update the NumberOfKiosks by 1(Decrease)
				for (RegistrationCenter registrationCenter : renRegistrationCenters) {
					short kiosis = (short) (registrationCenter.getNumberOfKiosks() - 1);
					registrationCenter.setNumberOfKiosks(kiosis);
					registrationCenter.setUpdatedBy(MetaDataUtils.getContextUser());
					registrationCenter.setUpdatedDateTime(LocalDateTime.now(ZoneId.of("UTC")));
					RegistrationCenter updRegistrationCenter = registrationCenterRepository.update(registrationCenter);

					// call method to update RegCenter history table
					updateRegCenterHistory(updRegistrationCenter);
				}
			}
		}
	}

	// method to added new row in RegCenter History for updating NumKiosis value in
	// RegCenter Table
	private void updateRegCenterHistory(RegistrationCenter updRegistrationCenter) {
		RegistrationCenterHistory registrationCenterHistoryEntity;
		registrationCenterHistoryEntity = MetaDataUtils.setCreateMetaData(updRegistrationCenter,
				RegistrationCenterHistory.class);
		registrationCenterHistoryEntity.setEffectivetimes(updRegistrationCenter.getCreatedDateTime());
		registrationCenterHistoryEntity.setCreatedDateTime(updRegistrationCenter.getCreatedDateTime());
		registrationCenterHistoryRepository.create(registrationCenterHistoryEntity);
	}
}
