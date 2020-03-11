package io.mosip.kernel.masterdata.service.impl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.transaction.Transactional;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;

import io.mosip.kernel.core.dataaccess.exception.DataAccessLayerException;
import io.mosip.kernel.core.exception.ServiceError;
import io.mosip.kernel.core.idgenerator.spi.RegistrationCenterIdGenerator;
import io.mosip.kernel.core.util.EmptyCheckUtils;
import io.mosip.kernel.masterdata.constant.HolidayErrorCode;
import io.mosip.kernel.masterdata.constant.MasterDataConstant;
import io.mosip.kernel.masterdata.constant.RegistrationCenterDeviceHistoryErrorCode;
import io.mosip.kernel.masterdata.constant.RegistrationCenterErrorCode;
import io.mosip.kernel.masterdata.dto.ExceptionalHolidayPutPostDto;
import io.mosip.kernel.masterdata.dto.HolidayDto;
import io.mosip.kernel.masterdata.dto.PageDto;
import io.mosip.kernel.masterdata.dto.RegCenterPostReqDto;
import io.mosip.kernel.masterdata.dto.RegCenterPutReqDto;
import io.mosip.kernel.masterdata.dto.RegistrationCenterDto;
import io.mosip.kernel.masterdata.dto.RegistrationCenterHolidayDto;
import io.mosip.kernel.masterdata.dto.WorkingNonWorkingDaysDto;
import io.mosip.kernel.masterdata.dto.getresponse.RegistrationCenterResponseDto;
import io.mosip.kernel.masterdata.dto.getresponse.ResgistrationCenterStatusResponseDto;
import io.mosip.kernel.masterdata.dto.getresponse.extn.RegistrationCenterExtnDto;
import io.mosip.kernel.masterdata.dto.postresponse.IdResponseDto;
import io.mosip.kernel.masterdata.dto.request.FilterDto;
import io.mosip.kernel.masterdata.dto.request.FilterValueDto;
import io.mosip.kernel.masterdata.dto.request.SearchDto;
import io.mosip.kernel.masterdata.dto.request.SearchFilter;
import io.mosip.kernel.masterdata.dto.response.ColumnValue;
import io.mosip.kernel.masterdata.dto.response.FilterResponseDto;
import io.mosip.kernel.masterdata.dto.response.PageResponseDto;
import io.mosip.kernel.masterdata.dto.response.RegistrationCenterSearchDto;
import io.mosip.kernel.masterdata.entity.DaysOfWeek;
import io.mosip.kernel.masterdata.entity.Holiday;
import io.mosip.kernel.masterdata.entity.Location;
import io.mosip.kernel.masterdata.entity.RegExceptionalHoliday;
import io.mosip.kernel.masterdata.entity.RegWorkingNonWorking;
import io.mosip.kernel.masterdata.entity.RegistrationCenter;
import io.mosip.kernel.masterdata.entity.RegistrationCenterDevice;
import io.mosip.kernel.masterdata.entity.RegistrationCenterHistory;
import io.mosip.kernel.masterdata.entity.RegistrationCenterMachine;
import io.mosip.kernel.masterdata.entity.RegistrationCenterMachineDevice;
import io.mosip.kernel.masterdata.entity.RegistrationCenterType;
import io.mosip.kernel.masterdata.entity.RegistrationCenterUserMachine;
import io.mosip.kernel.masterdata.entity.Zone;
import io.mosip.kernel.masterdata.exception.DataNotFoundException;
import io.mosip.kernel.masterdata.exception.MasterDataServiceException;
import io.mosip.kernel.masterdata.exception.RequestException;
import io.mosip.kernel.masterdata.exception.ValidationException;
import io.mosip.kernel.masterdata.repository.DaysOfWeekListRepo;
import io.mosip.kernel.masterdata.repository.HolidayRepository;
import io.mosip.kernel.masterdata.repository.LocationRepository;
import io.mosip.kernel.masterdata.repository.RegExceptionalHolidayRepository;
import io.mosip.kernel.masterdata.repository.RegWorkingNonWorkingRepo;
import io.mosip.kernel.masterdata.repository.RegistrationCenterDeviceRepository;
import io.mosip.kernel.masterdata.repository.RegistrationCenterHistoryRepository;
import io.mosip.kernel.masterdata.repository.RegistrationCenterMachineDeviceRepository;
import io.mosip.kernel.masterdata.repository.RegistrationCenterMachineRepository;
import io.mosip.kernel.masterdata.repository.RegistrationCenterMachineUserRepository;
import io.mosip.kernel.masterdata.repository.RegistrationCenterRepository;
import io.mosip.kernel.masterdata.repository.RegistrationCenterTypeRepository;
import io.mosip.kernel.masterdata.repository.RegistrationCenterUserRepository;
import io.mosip.kernel.masterdata.service.LocationService;
import io.mosip.kernel.masterdata.service.RegistrationCenterHistoryService;
import io.mosip.kernel.masterdata.service.RegistrationCenterService;
import io.mosip.kernel.masterdata.utils.AuditUtil;
import io.mosip.kernel.masterdata.utils.ExceptionUtils;
import io.mosip.kernel.masterdata.utils.LocationUtils;
import io.mosip.kernel.masterdata.utils.MapperUtils;
import io.mosip.kernel.masterdata.utils.MasterDataFilterHelper;
import io.mosip.kernel.masterdata.utils.MasterdataCreationUtil;
import io.mosip.kernel.masterdata.utils.MetaDataUtils;
import io.mosip.kernel.masterdata.utils.PageUtils;
import io.mosip.kernel.masterdata.utils.RegistrationCenterServiceHelper;
import io.mosip.kernel.masterdata.utils.RegistrationCenterValidator;
import io.mosip.kernel.masterdata.utils.ZoneUtils;
import io.mosip.kernel.masterdata.validator.FilterColumnValidator;
import io.mosip.kernel.masterdata.validator.FilterTypeEnum;
import io.mosip.kernel.masterdata.validator.FilterTypeValidator;

/**
 * This service class contains methods that provides registration centers
 * details based on user provided data.
 * 
 * @author Dharmesh Khandelwal
 * @author Abhishek Kumar
 * @author Urvil Joshi
 * @author Ritesh Sinha
 * @author Sagar Mahapatra
 * @author Sidhant Agarwal
 * @author Uday Kumar
 * @author Megha Tanga
 * @author Ravi Kant
 * @since 1.0.0
 *
 */

@Service
public class RegistrationCenterServiceImpl implements RegistrationCenterService {

	@Autowired
	private RegistrationCenterValidator registrationCenterValidator;

	/**
	 * Reference to RegistrationCenterRepository.
	 */
	@Autowired
	private RegistrationCenterRepository registrationCenterRepository;

	@Autowired
	private RegistrationCenterHistoryRepository registrationCenterHistoryRepository;

	@Autowired
	RegistrationCenterMachineRepository registrationCenterMachineRepository;

	@Autowired
	RegistrationCenterMachineUserRepository registrationCenterMachineUserRepository;

	@Autowired
	RegistrationCenterMachineDeviceRepository registrationCenterMachineDeviceRepository;

	@Autowired
	RegistrationCenterHistoryService registrationCenterHistoryService;

	@Autowired
	RegistrationCenterDeviceRepository registrationCenterDeviceRepository;

	@Autowired
	RegistrationCenterUserRepository registrationCenterUserRepository;

	@Autowired
	RegistrationCenterIdGenerator<String> registrationCenterIdGenerator;

	/**
	 * Reference to HolidayRepository.
	 */
	@Autowired
	private HolidayRepository holidayRepository;

	@Autowired
	private LocationService locationService;

	@Autowired
	private FilterTypeValidator filterTypeValidator;

	@Autowired
	private LocationUtils locationUtils;

	@Autowired
	private ZoneUtils zoneUtils;

	@Autowired
	private RegistrationCenterServiceHelper serviceHelper;

	@Autowired
	private MasterDataFilterHelper masterDataFilterHelper;

	@Autowired
	private FilterColumnValidator filterColumnValidator;

	@Autowired
	private RegistrationCenterTypeRepository registrationCenterTypeRepository;

	@Autowired
	private LocationRepository locationRepository;
	/**
	 * get list of secondary languages supported by MOSIP from configuration file
	 */
	@Value("${mosip.primary-language}")
	private String primaryLang;

	@Autowired
	private AuditUtil auditUtil;

	/**
	 * get list of secondary languages supported by MOSIP from configuration file
	 */
	@Value("${mosip.secondary-language}")
	private String secondaryLang;

	/**
	 * get list of secondary languages supported by MOSIP from configuration file
	 */
	@Value("#{'${mosip.secondary-language}'.split(',')}")
	private Set<String> secondaryLangList;

	private Set<String> supportedLanguages;

	/**
	 * minimum digits after decimal point in Longitude and latitude
	 */
	@Value("${mosip.min-digit-longitude-latitude:4}")
	private int minDegits;

	@Value("${mosip.kernel.registrationcenterid.length}")
	private int regCenterIDLength;

	@Autowired
	private MasterdataCreationUtil masterdataCreationUtil;

	@Autowired
	private PageUtils pageUtils;

	@Autowired
	private DaysOfWeekListRepo daysOfWeekListRepo;

	@Autowired
	private RegWorkingNonWorkingRepo regWorkingNonWorkingRepo;

	@Autowired
	private RegExceptionalHolidayRepository regExceptionalHolidayRepository;

	/**
	 * Constructing regex for matching the Latitude and Longitude format
	 */

	@PostConstruct
	public void constructRegEx() {
		supportedLanguages = new HashSet<>(Arrays.asList(secondaryLang.split(",")));
		supportedLanguages.add(primaryLang);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see io.mosip.kernel.masterdata.service.RegistrationCenterService#
	 * getRegistrationCenterHolidays(java.lang.String, int, java.lang.String)
	 */
	@Override
	public RegistrationCenterHolidayDto getRegistrationCenterHolidays(String registrationCenterId, int year,
			String langCode) {
		List<RegistrationCenterDto> registrationCenters;
		List<RegistrationCenter> registrationCenterEntity = new ArrayList<>();
		RegistrationCenterHolidayDto registrationCenterHolidayResponse = null;
		RegistrationCenterDto registrationCenterDto = null;
		RegistrationCenter registrationCenter = null;
		List<HolidayDto> holidayDto = null;
		List<Holiday> holidays = null;
		String holidayLocationCode = "";

		Objects.requireNonNull(registrationCenterId);
		Objects.requireNonNull(year);
		Objects.requireNonNull(langCode);
		try {
			registrationCenter = registrationCenterRepository.findByIdAndLangCode(registrationCenterId, langCode);
		} catch (DataAccessException | DataAccessLayerException dataAccessException) {
			throw new MasterDataServiceException(
					RegistrationCenterErrorCode.REGISTRATION_CENTER_FETCH_EXCEPTION.getErrorCode(),
					RegistrationCenterErrorCode.REGISTRATION_CENTER_FETCH_EXCEPTION.getErrorMessage()
							+ ExceptionUtils.parseException(dataAccessException));
		}
		if (registrationCenter == null) {
			throw new DataNotFoundException(RegistrationCenterErrorCode.REGISTRATION_CENTER_NOT_FOUND.getErrorCode(),
					RegistrationCenterErrorCode.REGISTRATION_CENTER_NOT_FOUND.getErrorMessage());
		} else {
			registrationCenterEntity.add(registrationCenter);
			registrationCenters = MapperUtils.mapAll(registrationCenterEntity, RegistrationCenterDto.class);
			registrationCenterDto = registrationCenters.get(0);
			try {
				holidayLocationCode = registrationCenterDto.getHolidayLocationCode();
				holidays = holidayRepository.findAllByLocationCodeYearAndLangCode(holidayLocationCode, langCode, year);
				if (holidayLocationCode != null)
					holidays = holidayRepository.findAllByLocationCodeYearAndLangCode(holidayLocationCode, langCode,
							year);
			} catch (DataAccessException | DataAccessLayerException dataAccessException) {
				throw new MasterDataServiceException(HolidayErrorCode.HOLIDAY_FETCH_EXCEPTION.getErrorCode(),
						HolidayErrorCode.HOLIDAY_FETCH_EXCEPTION.getErrorMessage());

			}
			if (holidays != null)
				holidayDto = MapperUtils.mapHolidays(holidays);
		}
		registrationCenterHolidayResponse = new RegistrationCenterHolidayDto();
		registrationCenterHolidayResponse.setRegistrationCenter(registrationCenterDto);
		registrationCenterHolidayResponse.setHolidays(holidayDto);

		return registrationCenterHolidayResponse;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see io.mosip.kernel.masterdata.service.RegistrationCenterService#
	 * getRegistrationCentersByCoordinates(double, double, int, java.lang.String)
	 */
	@Override
	public RegistrationCenterResponseDto getRegistrationCentersByCoordinates(double longitude, double latitude,
			int proximityDistance, String langCode) {
		List<RegistrationCenter> centers = null;
		try {
			centers = registrationCenterRepository.findRegistrationCentersByLat(latitude, longitude,
					proximityDistance * MasterDataConstant.METERTOMILECONVERSION, langCode);
		} catch (DataAccessLayerException | DataAccessException e) {
			throw new MasterDataServiceException(
					RegistrationCenterErrorCode.REGISTRATION_CENTER_FETCH_EXCEPTION.getErrorCode(),
					RegistrationCenterErrorCode.REGISTRATION_CENTER_FETCH_EXCEPTION.getErrorMessage()
							+ ExceptionUtils.parseException(e));
		}
		if (centers.isEmpty()) {
			throw new DataNotFoundException(RegistrationCenterErrorCode.REGISTRATION_CENTER_NOT_FOUND.getErrorCode(),
					RegistrationCenterErrorCode.REGISTRATION_CENTER_NOT_FOUND.getErrorMessage());
		}
		List<RegistrationCenterDto> registrationCenters = null;
		registrationCenters = MapperUtils.mapAll(centers, RegistrationCenterDto.class);
		RegistrationCenterResponseDto registrationCenterResponseDto = new RegistrationCenterResponseDto();
		registrationCenterResponseDto.setRegistrationCenters(registrationCenters);
		return registrationCenterResponseDto;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see io.mosip.kernel.masterdata.service.RegistrationCenterService#
	 * getRegistrationCentersByLocationCodeAndLanguageCode(java.lang.String,
	 * java.lang.String)
	 */
	@Override
	public RegistrationCenterResponseDto getRegistrationCentersByLocationCodeAndLanguageCode(String locationCode,
			String langCode) {
		List<RegistrationCenter> registrationCentersList = null;
		try {
			registrationCentersList = registrationCenterRepository.findByLocationCodeAndLangCode(locationCode,
					langCode);

		} catch (DataAccessLayerException | DataAccessException e) {
			throw new MasterDataServiceException(
					RegistrationCenterErrorCode.REGISTRATION_CENTER_FETCH_EXCEPTION.getErrorCode(),
					RegistrationCenterErrorCode.REGISTRATION_CENTER_FETCH_EXCEPTION.getErrorMessage()
							+ ExceptionUtils.parseException(e));
		}
		if (registrationCentersList.isEmpty()) {
			throw new DataNotFoundException(RegistrationCenterErrorCode.REGISTRATION_CENTER_NOT_FOUND.getErrorCode(),
					RegistrationCenterErrorCode.REGISTRATION_CENTER_NOT_FOUND.getErrorMessage());
		}
		List<RegistrationCenterDto> registrationCentersDtoList = null;
		registrationCentersDtoList = MapperUtils.mapAll(registrationCentersList, RegistrationCenterDto.class);
		RegistrationCenterResponseDto registrationCenterResponseDto = new RegistrationCenterResponseDto();
		registrationCenterResponseDto.setRegistrationCenters(registrationCentersDtoList);
		return registrationCenterResponseDto;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see io.mosip.kernel.masterdata.service.RegistrationCenterService#
	 * getRegistrationCentersByIDAndLangCode(java.lang.String, java.lang.String)
	 */
	@Override
	public RegistrationCenterResponseDto getRegistrationCentersByIDAndLangCode(String registrationCenterId,
			String langCode) {
		List<RegistrationCenterDto> registrationCenters = new ArrayList<>();

		RegistrationCenter registrationCenter = null;
		try {
			registrationCenter = registrationCenterRepository.findByIdAndLangCode(registrationCenterId, langCode);
		} catch (DataAccessLayerException | DataAccessException e) {
			throw new MasterDataServiceException(
					RegistrationCenterErrorCode.REGISTRATION_CENTER_FETCH_EXCEPTION.getErrorCode(),
					RegistrationCenterErrorCode.REGISTRATION_CENTER_FETCH_EXCEPTION.getErrorMessage()
							+ ExceptionUtils.parseException(e));
		}
		if (registrationCenter == null) {
			throw new DataNotFoundException(RegistrationCenterErrorCode.REGISTRATION_CENTER_NOT_FOUND.getErrorCode(),
					RegistrationCenterErrorCode.REGISTRATION_CENTER_NOT_FOUND.getErrorMessage());
		}

		RegistrationCenterDto registrationCenterDto = MapperUtils.map(registrationCenter, RegistrationCenterDto.class);
		registrationCenters.add(registrationCenterDto);
		RegistrationCenterResponseDto response = new RegistrationCenterResponseDto();
		response.setRegistrationCenters(registrationCenters);
		return response;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see io.mosip.kernel.masterdata.service.RegistrationCenterService#
	 * getAllRegistrationCenters()
	 */
	@Override
	public RegistrationCenterResponseDto getAllRegistrationCenters() {
		List<RegistrationCenter> registrationCentersList = null;
		try {
			registrationCentersList = registrationCenterRepository.findAllByIsDeletedFalseOrIsDeletedIsNull();

		} catch (DataAccessLayerException | DataAccessException e) {
			throw new MasterDataServiceException(
					RegistrationCenterErrorCode.REGISTRATION_CENTER_FETCH_EXCEPTION.getErrorCode(),
					RegistrationCenterErrorCode.REGISTRATION_CENTER_FETCH_EXCEPTION.getErrorMessage());
		}

		if (registrationCentersList.isEmpty()) {
			throw new DataNotFoundException(RegistrationCenterErrorCode.REGISTRATION_CENTER_NOT_FOUND.getErrorCode(),
					RegistrationCenterErrorCode.REGISTRATION_CENTER_NOT_FOUND.getErrorMessage());
		}

		List<RegistrationCenterDto> registrationCenters = null;
		registrationCenters = MapperUtils.mapAll(registrationCentersList, RegistrationCenterDto.class);
		RegistrationCenterResponseDto registrationCenterResponseDto = new RegistrationCenterResponseDto();
		registrationCenterResponseDto.setRegistrationCenters(registrationCenters);
		return registrationCenterResponseDto;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see io.mosip.kernel.masterdata.service.RegistrationCenterService#
	 * findRegistrationCenterByHierarchyLevelandTextAndLanguageCode(java.lang.
	 * String, java.lang.String, java.lang.String)
	 */
	@Override
	public RegistrationCenterResponseDto findRegistrationCenterByHierarchyLevelandTextAndLanguageCode(
			String languageCode, Short hierarchyLevel, String text) {
		List<RegistrationCenter> registrationCentersList = null;
		try {
			Set<String> codes = getLocationCode(
					locationService.getLocationByLangCodeAndHierarchyLevel(languageCode, hierarchyLevel),
					hierarchyLevel, text);
			if (!EmptyCheckUtils.isNullEmpty(codes)) {
				registrationCentersList = registrationCenterRepository.findRegistrationCenterByListOfLocationCode(codes,
						languageCode);
			} else {
				throw new DataNotFoundException(
						RegistrationCenterErrorCode.REGISTRATION_CENTER_NOT_FOUND.getErrorCode(),
						RegistrationCenterErrorCode.REGISTRATION_CENTER_NOT_FOUND.getErrorMessage());
			}

		} catch (DataAccessLayerException | DataAccessException e) {
			throw new MasterDataServiceException(
					RegistrationCenterErrorCode.REGISTRATION_CENTER_FETCH_EXCEPTION.getErrorCode(),
					RegistrationCenterErrorCode.REGISTRATION_CENTER_FETCH_EXCEPTION.getErrorMessage()
							+ ExceptionUtils.parseException(e));
		}
		if (registrationCentersList.isEmpty()) {
			throw new DataNotFoundException(RegistrationCenterErrorCode.REGISTRATION_CENTER_NOT_FOUND.getErrorCode(),
					RegistrationCenterErrorCode.REGISTRATION_CENTER_NOT_FOUND.getErrorMessage());
		}
		List<RegistrationCenterDto> registrationCentersDtoList = null;
		registrationCentersDtoList = MapperUtils.mapAll(registrationCentersList, RegistrationCenterDto.class);

		RegistrationCenterResponseDto registrationCenterResponseDto = new RegistrationCenterResponseDto();
		registrationCenterResponseDto.setRegistrationCenters(registrationCentersDtoList);
		return registrationCenterResponseDto;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see io.mosip.kernel.masterdata.service.RegistrationCenterService#
	 * validateTimestampWithRegistrationCenter(java.lang.String, java.lang.String)
	 */
	@Override
	public ResgistrationCenterStatusResponseDto validateTimeStampWithRegistrationCenter(String id, String langCode,
			String timestamp) {
		LocalDateTime localDateTime = null;
		try {
			localDateTime = MapperUtils.parseToLocalDateTime(timestamp);
		} catch (DateTimeParseException ex) {
			throw new RequestException(
					RegistrationCenterDeviceHistoryErrorCode.INVALIDE_EFFECTIVE_DATE_TIME_FORMATE_EXCEPTION
							.getErrorCode(),
					RegistrationCenterDeviceHistoryErrorCode.INVALIDE_EFFECTIVE_DATE_TIME_FORMATE_EXCEPTION
							.getErrorMessage() + ExceptionUtils.parseException(ex));
		}
		LocalDate localDate = localDateTime.toLocalDate();
		ResgistrationCenterStatusResponseDto resgistrationCenterStatusResponseDto = new ResgistrationCenterStatusResponseDto();
		try {
			/**
			 * a query is written in RegistrationCenterRepository which would check if the
			 * date is not a holiday for that center
			 *
			 */
			RegistrationCenter registrationCenter = registrationCenterRepository.findByIdAndLangCode(id, langCode);
			if (registrationCenter == null) {
				throw new DataNotFoundException(
						RegistrationCenterErrorCode.REGISTRATION_CENTER_NOT_FOUND.getErrorCode(),
						RegistrationCenterErrorCode.REGISTRATION_CENTER_NOT_FOUND.getErrorMessage());
			}
			boolean isTrue = registrationCenterRepository.validateDateWithHoliday(localDate,
					registrationCenter.getHolidayLocationCode());
			if (isTrue) {
				resgistrationCenterStatusResponseDto.setStatus(MasterDataConstant.INVALID);
			} else {

				resgistrationCenterStatusResponseDto.setStatus(MasterDataConstant.VALID);
			}

		} catch (DataAccessLayerException | DataAccessException e) {
			throw new MasterDataServiceException(
					RegistrationCenterErrorCode.REGISTRATION_CENTER_FETCH_EXCEPTION.getErrorCode(),
					RegistrationCenterErrorCode.REGISTRATION_CENTER_FETCH_EXCEPTION.getErrorMessage()
							+ ExceptionUtils.parseException(e));
		}

		return resgistrationCenterStatusResponseDto;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see io.mosip.kernel.masterdata.service.RegistrationCenterService#
	 * deleteRegistrationCenter(java.lang.String)
	 */
	@Override
	@Transactional
	public IdResponseDto deleteRegistrationCenter(String id) {
		RegistrationCenter delRegistrationCenter = null;
		try {
			List<RegistrationCenter> renRegistrationCenterList = registrationCenterRepository
					.findByRegIdAndIsDeletedFalseOrNull(id);
			if (!renRegistrationCenterList.isEmpty()) {
				for (RegistrationCenter renRegistrationCenter : renRegistrationCenterList) {

					List<RegistrationCenterMachine> registrationCenterMachineList = registrationCenterMachineRepository
							.findByMachineIdAndIsDeletedFalseOrIsDeletedIsNull(renRegistrationCenter.getId());
					List<RegistrationCenterUserMachine> registrationCenterMachineUser = registrationCenterMachineUserRepository
							.findByMachineIdAndIsDeletedFalseOrIsDeletedIsNull(renRegistrationCenter.getId());
					List<RegistrationCenterMachineDevice> registrationCenterMachineDevice = registrationCenterMachineDeviceRepository
							.findByMachineIdAndIsDeletedFalseOrIsDeletedIsNull(renRegistrationCenter.getId());
					List<RegistrationCenterDevice> registrationCenterDeviceList = registrationCenterDeviceRepository
							.findByDeviceIdAndIsDeletedFalseOrIsDeletedIsNull(renRegistrationCenter.getId());

					if (registrationCenterMachineList.isEmpty() && registrationCenterMachineUser.isEmpty()
							&& registrationCenterMachineDevice.isEmpty() && registrationCenterDeviceList.isEmpty()) {
						MetaDataUtils.setDeleteMetaData(renRegistrationCenter);
						delRegistrationCenter = registrationCenterRepository.update(renRegistrationCenter);

						RegistrationCenterHistory registrationCenterHistory = new RegistrationCenterHistory();
						MapperUtils.map(delRegistrationCenter, registrationCenterHistory);
						MapperUtils.setBaseFieldValue(delRegistrationCenter, registrationCenterHistory);

						registrationCenterHistory.setEffectivetimes(delRegistrationCenter.getDeletedDateTime());
						registrationCenterHistory.setDeletedDateTime(delRegistrationCenter.getDeletedDateTime());
						registrationCenterHistoryService.createRegistrationCenterHistory(registrationCenterHistory);
					} else {
						throw new RequestException(RegistrationCenterErrorCode.DEPENDENCY_EXCEPTION.getErrorCode(),
								RegistrationCenterErrorCode.DEPENDENCY_EXCEPTION.getErrorMessage());
					}
				}
			} else {
				throw new RequestException(RegistrationCenterErrorCode.REGISTRATION_CENTER_NOT_FOUND.getErrorCode(),
						RegistrationCenterErrorCode.REGISTRATION_CENTER_NOT_FOUND.getErrorMessage());
			}

		} catch (DataAccessLayerException | DataAccessException e) {
			throw new MasterDataServiceException(
					RegistrationCenterErrorCode.REGISTRATION_CENTER_DELETE_EXCEPTION.getErrorCode(),
					RegistrationCenterErrorCode.REGISTRATION_CENTER_DELETE_EXCEPTION.getErrorMessage()
							+ ExceptionUtils.parseException(e));
		}

		IdResponseDto idResponseDto = new IdResponseDto();
		idResponseDto.setId(id);
		return idResponseDto;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see io.mosip.kernel.masterdata.service.RegistrationCenterService#
	 * findRegistrationCenterByHierarchyLevelAndListTextAndlangCode(java.lang.
	 * String, java.lang.Integer, java.util.List)
	 */
	@Override
	public RegistrationCenterResponseDto findRegistrationCenterByHierarchyLevelAndListTextAndlangCode(
			String languageCode, Short hierarchyLevel, List<String> names) {
		List<RegistrationCenterDto> registrationCentersDtoList = null;
		List<RegistrationCenter> registrationCentersList = null;
		Set<String> uniqueLocCode = new TreeSet<>();
		try {
			Map<Short, List<Location>> parLocCodeToListOfLocation = locationService
					.getLocationByLangCodeAndHierarchyLevel(languageCode, hierarchyLevel);
			Set<String> codes = getListOfLocationCode(parLocCodeToListOfLocation, hierarchyLevel, names);
			uniqueLocCode.addAll(codes);
			if (!EmptyCheckUtils.isNullEmpty(uniqueLocCode)) {
				registrationCentersList = registrationCenterRepository
						.findRegistrationCenterByListOfLocationCode(uniqueLocCode, languageCode);
			} else {
				throw new DataNotFoundException(
						RegistrationCenterErrorCode.REGISTRATION_CENTER_NOT_FOUND.getErrorCode(),
						RegistrationCenterErrorCode.REGISTRATION_CENTER_NOT_FOUND.getErrorMessage());
			}

		} catch (DataAccessLayerException | DataAccessException e) {
			throw new MasterDataServiceException(
					RegistrationCenterErrorCode.REGISTRATION_CENTER_FETCH_EXCEPTION.getErrorCode(),
					RegistrationCenterErrorCode.REGISTRATION_CENTER_FETCH_EXCEPTION.getErrorMessage()
							+ ExceptionUtils.parseException(e));
		}
		if (registrationCentersList.isEmpty()) {
			throw new DataNotFoundException(RegistrationCenterErrorCode.REGISTRATION_CENTER_NOT_FOUND.getErrorCode(),
					RegistrationCenterErrorCode.REGISTRATION_CENTER_NOT_FOUND.getErrorMessage());
		}
		registrationCentersDtoList = MapperUtils.mapAll(registrationCentersList, RegistrationCenterDto.class);

		RegistrationCenterResponseDto registrationCenterResponseDto = new RegistrationCenterResponseDto();
		registrationCenterResponseDto.setRegistrationCenters(registrationCentersDtoList);
		return registrationCenterResponseDto;
	}

	private Set<String> getLocationCode(Map<Short, List<Location>> levelToListOfLocationMap, Short hierarchyLevel,
			String text) {
		validateLocationName(levelToListOfLocationMap, hierarchyLevel, text);
		Set<String> uniqueLocCode = new TreeSet<>();
		boolean isParent = false;
		for (Entry<Short, List<Location>> data : levelToListOfLocationMap.entrySet()) {
			if (!isParent) {
				for (Location location : data.getValue()) {
					if (text.trim().equalsIgnoreCase(location.getName().trim())) {
						uniqueLocCode.add(location.getCode());
						isParent = true;
						break;// parent code set
					}
				}
			} else if (data.getKey() > hierarchyLevel) {
				for (Location location : data.getValue()) {
					if (uniqueLocCode.contains(location.getParentLocCode())) {
						uniqueLocCode.add(location.getCode());
					}
				}
			}
		}
		return uniqueLocCode;
	}

	private Set<String> getListOfLocationCode(Map<Short, List<Location>> levelToListOfLocationMap, Short hierarchyLevel,
			List<String> texts) {

		List<String> validLocationName = validateListOfLocationName(levelToListOfLocationMap, hierarchyLevel, texts);
		Set<String> uniqueLocCode = new TreeSet<>();
		if (!validLocationName.isEmpty()) {
			for (String text : validLocationName) {
				boolean isParent = false;
				for (Entry<Short, List<Location>> data : levelToListOfLocationMap.entrySet()) {
					if (!isParent) {
						for (Location location : data.getValue()) {
							if (text.trim().equalsIgnoreCase(location.getName().trim())) {
								uniqueLocCode.add(location.getCode());
								isParent = true;
								break;// parent code set
							}
						}
					} else if (data.getKey() > hierarchyLevel) {
						for (Location location : data.getValue()) {
							if (uniqueLocCode.contains(location.getParentLocCode())) {
								uniqueLocCode.add(location.getCode());
							}
						}
					}
				}
			}
		} else {
			throw new DataNotFoundException(RegistrationCenterErrorCode.REGISTRATION_CENTER_NOT_FOUND.getErrorCode(),
					RegistrationCenterErrorCode.REGISTRATION_CENTER_NOT_FOUND.getErrorMessage());
		}
		return uniqueLocCode;
	}

	private void validateLocationName(Map<Short, List<Location>> levelToListOfLocationMap, Short hierarchyLevel,
			String text) {
		List<Location> rootLocation = levelToListOfLocationMap.get(hierarchyLevel);
		boolean isRootLocation = false;
		for (Location location : rootLocation) {
			if (location.getName().trim().equalsIgnoreCase(text)) {
				isRootLocation = true;
			}
		}
		if (!isRootLocation) {
			throw new DataNotFoundException(RegistrationCenterErrorCode.REGISTRATION_CENTER_NOT_FOUND.getErrorCode(),
					RegistrationCenterErrorCode.REGISTRATION_CENTER_NOT_FOUND.getErrorMessage());
		}
	}

	private List<String> validateListOfLocationName(Map<Short, List<Location>> levelToListOfLocationMap,
			Short hierarchyLevel, List<String> texts) {
		List<String> locationNames = new ArrayList<>();
		List<Location> rootLocation = levelToListOfLocationMap.get(hierarchyLevel);
		for (String text : texts) {
			for (Location location : rootLocation) {
				if (location.getName().trim().equalsIgnoreCase(text)) {
					locationNames.add(text);
				}
			}
		}
		return locationNames;
	}

	@Override
	public PageDto<RegistrationCenterExtnDto> getAllExistingRegistrationCenters(int pageNumber, int pageSize,
			String sortBy, String orderBy) {
		List<RegistrationCenterExtnDto> registrationCenters = null;
		PageDto<RegistrationCenterExtnDto> registrationCenterPages = null;
		try {
			Page<RegistrationCenter> pageData = registrationCenterRepository
					.findAll(PageRequest.of(pageNumber, pageSize, Sort.by(Direction.fromString(orderBy), sortBy)));
			if (pageData != null && pageData.getContent() != null && !pageData.getContent().isEmpty()) {
				registrationCenters = MapperUtils.mapAll(pageData.getContent(), RegistrationCenterExtnDto.class);
				registrationCenterPages = new PageDto<RegistrationCenterExtnDto>(pageData.getNumber(), 0, null,
						pageData.getTotalPages(), (int) pageData.getTotalElements(), registrationCenters);
			} else {
				throw new DataNotFoundException(
						RegistrationCenterErrorCode.REGISTRATION_CENTER_NOT_FOUND.getErrorCode(),
						RegistrationCenterErrorCode.REGISTRATION_CENTER_NOT_FOUND.getErrorMessage());
			}
		} catch (DataAccessLayerException | DataAccessException e) {
			throw new MasterDataServiceException(
					RegistrationCenterErrorCode.REGISTRATION_CENTER_FETCH_EXCEPTION.getErrorCode(),
					RegistrationCenterErrorCode.REGISTRATION_CENTER_FETCH_EXCEPTION.getErrorMessage());
		}
		return registrationCenterPages;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see io.mosip.kernel.masterdata.service.RegistrationCenterService#
	 * searchRegistrationCenter(io.mosip.kernel.masterdata.dto.request. SearchDto)
	 */
	@Override
	public PageResponseDto<RegistrationCenterSearchDto> searchRegistrationCenter(SearchDto dto) {
		PageResponseDto<RegistrationCenterSearchDto> pageDto = new PageResponseDto<>();
		List<SearchFilter> addList = new ArrayList<>();
		List<SearchFilter> removeList = new ArrayList<>();
		List<SearchFilter> locationFilter = new ArrayList<>();
		List<SearchFilter> zoneFilter = new ArrayList<>();
		List<Zone> zones = null;
		List<Location> locations = null;
		boolean flag = true;
		// fetching locations
		locations = serviceHelper.fetchLocations(dto.getLanguageCode());
		pageUtils.validateSortField(RegistrationCenterSearchDto.class, RegistrationCenter.class, dto.getSort());
		for (SearchFilter filter : dto.getFilters()) {
			String column = filter.getColumnName();

			// if registration center type name
			if (MasterDataConstant.CENTERTYPENAME.equalsIgnoreCase(column)) {
				serviceHelper.centerTypeSearch(addList, removeList, filter);
			}
			// if location based search
			if (serviceHelper.isLocationSearch(filter.getColumnName())
					|| MasterDataConstant.ZONE.equalsIgnoreCase(column)) {
				Location location = serviceHelper.locationSearch(filter);
				if (location != null) {
					// fetching sub-locations
					List<Location> descendants = locationUtils.getDescedants(locations, location);
					List<Location> leaves = descendants.parallelStream().filter(child -> child.getHierarchyLevel() == 5)
							.collect(Collectors.toList());
					locationFilter.addAll(serviceHelper.buildLocationSearchFilter(leaves));
				} else {
					flag = false;
				}
				removeList.add(filter);
			}
			/*
			 * // if zone based search if (MasterDataConstant.ZONE.equalsIgnoreCase(column))
			 * { Location zone = serviceHelper.getZone(filter); if (zone != null) {
			 * List<Location> descendants = locationUtils.getDescedants(locations, zone); }
			 * removeList.add(filter); flag = false; }
			 */
		}
		/*
		 * if (flag) { // fetching logged in user zones zones =
		 * serviceHelper.fetchUserZone(zoneFilter, dto.getLanguageCode()); }
		 */
		// removing already processed filters and adding new filters
		if (flag) {
			// fetching logged in user zones
			zones = serviceHelper.fetchUserZone(zoneFilter, dto.getLanguageCode());
		}
		dto.getFilters().removeAll(removeList);
		dto.getFilters().addAll(addList);

		if (filterTypeValidator.validate(RegistrationCenterSearchDto.class, dto.getFilters()) && flag) {
			// searching registration center
			pageDto = serviceHelper.searchCenter(dto, locationFilter, zoneFilter, zones, locations);
		}
		return pageDto;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see io.mosip.kernel.masterdata.service.RegistrationCenterService#
	 * registrationCenterFilterValues(io.mosip.kernel.masterdata.dto.request.
	 * FilterValueDto)
	 */
	@Override
	public FilterResponseDto registrationCenterFilterValues(FilterValueDto filterValueDto) {
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
		if (filterColumnValidator.validate(FilterDto.class, filterValueDto.getFilters(), RegistrationCenter.class)) {
			for (FilterDto filterDto : filterValueDto.getFilters()) {
				List<?> filterValues = masterDataFilterHelper.filterValues(RegistrationCenter.class, filterDto,
						filterValueDto);
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
	public IdResponseDto decommissionRegCenter(String regCenterID) {
		if (regCenterID.length() > regCenterIDLength) {
			auditException(RegistrationCenterErrorCode.INVALID_RCID_LENGTH.getErrorCode(),
					RegistrationCenterErrorCode.INVALID_RCID_LENGTH.getErrorMessage());
			throw new RequestException(RegistrationCenterErrorCode.INVALID_RCID_LENGTH.getErrorCode(),
					RegistrationCenterErrorCode.INVALID_RCID_LENGTH.getErrorMessage());
		}

		// get given registration center
		RegistrationCenter regCenter = registrationCenterRepository.findByLangCodeAndId(regCenterID, primaryLang);

		if (regCenter == null) {
			auditException(RegistrationCenterErrorCode.DECOMMISSIONED.getErrorCode(),
					RegistrationCenterErrorCode.DECOMMISSIONED.getErrorMessage());
			throw new RequestException(RegistrationCenterErrorCode.DECOMMISSIONED.getErrorCode(),
					RegistrationCenterErrorCode.DECOMMISSIONED.getErrorMessage());
		}

		List<String> zoneIds;
		// get user zone and child zones list
		List<Zone> userZones = zoneUtils.getUserZones();
		zoneIds = userZones.parallelStream().map(Zone::getCode).collect(Collectors.toList());

		// check the given registration center zone are come under
		// user zone
		if (!zoneIds.contains(regCenter.getZoneCode())) {
			auditException(RegistrationCenterErrorCode.REG_CENTER_INVALIDE_ZONE.getErrorCode(),
					RegistrationCenterErrorCode.REG_CENTER_INVALIDE_ZONE.getErrorMessage());
			throw new RequestException(RegistrationCenterErrorCode.REG_CENTER_INVALIDE_ZONE.getErrorCode(),
					RegistrationCenterErrorCode.REG_CENTER_INVALIDE_ZONE.getErrorMessage());
		}

		IdResponseDto idResponseDto = new IdResponseDto();
		int decommissionedCenters = 0;
		try {
			if (!registrationCenterUserRepository.registrationCenterUserMappings(regCenterID).isEmpty()) {
				auditException(RegistrationCenterErrorCode.MAPPED_TO_USER.getErrorCode(),
						RegistrationCenterErrorCode.MAPPED_TO_USER.getErrorMessage());
				throw new RequestException(RegistrationCenterErrorCode.MAPPED_TO_USER.getErrorCode(),
						RegistrationCenterErrorCode.MAPPED_TO_USER.getErrorMessage());
			} else if (!registrationCenterMachineRepository.findRegCenterMachineMappings(regCenterID).isEmpty()) {
				auditException(RegistrationCenterErrorCode.MAPPED_TO_MACHINE.getErrorCode(),
						RegistrationCenterErrorCode.MAPPED_TO_MACHINE.getErrorMessage());
				throw new RequestException(RegistrationCenterErrorCode.MAPPED_TO_MACHINE.getErrorCode(),
						RegistrationCenterErrorCode.MAPPED_TO_MACHINE.getErrorMessage());
			} else if (!registrationCenterDeviceRepository.registrationCenterDeviceMappings(regCenterID).isEmpty()) {
				auditException(RegistrationCenterErrorCode.MAPPED_TO_DEVICE.getErrorCode(),
						RegistrationCenterErrorCode.MAPPED_TO_DEVICE.getErrorMessage());
				throw new RequestException(RegistrationCenterErrorCode.MAPPED_TO_DEVICE.getErrorCode(),
						RegistrationCenterErrorCode.MAPPED_TO_DEVICE.getErrorMessage());
			} else {
				if (registrationCenterRepository.findByRegIdAndIsDeletedFalseOrNull(regCenterID).isEmpty()) {
					auditException(RegistrationCenterErrorCode.REGISTRATION_CENTER_NOT_FOUND.getErrorCode(),
							RegistrationCenterErrorCode.REGISTRATION_CENTER_NOT_FOUND.getErrorMessage());
					throw new DataNotFoundException(
							RegistrationCenterErrorCode.REGISTRATION_CENTER_NOT_FOUND.getErrorCode(),
							RegistrationCenterErrorCode.REGISTRATION_CENTER_NOT_FOUND.getErrorMessage());
				}
				decommissionedCenters = registrationCenterRepository.decommissionRegCenter(regCenterID,
						MetaDataUtils.getContextUser(), MetaDataUtils.getCurrentDateTime());
			}
		} catch (DataAccessException | DataAccessLayerException exception) {
			auditException(RegistrationCenterErrorCode.DECOMMISSION_FAILED.getErrorCode(),
					RegistrationCenterErrorCode.DECOMMISSION_FAILED.getErrorMessage() + exception.getCause());
			throw new MasterDataServiceException(RegistrationCenterErrorCode.DECOMMISSION_FAILED.getErrorCode(),
					RegistrationCenterErrorCode.DECOMMISSION_FAILED.getErrorMessage() + exception.getCause());
		}
		if (decommissionedCenters > 0) {
			idResponseDto.setId(regCenterID);
		}
		auditUtil.auditRequest(
				String.format(MasterDataConstant.SUCCESSFUL_CREATE, RegistrationCenterSearchDto.class.getSimpleName()),
				MasterDataConstant.AUDIT_SYSTEM, String.format(MasterDataConstant.SUCCESSFUL_CREATE_DESC,
						RegistrationCenterSearchDto.class.getSimpleName(), idResponseDto.getId()),
				"ADM-523");
		return idResponseDto;
	}

	/**
	 * @param exception
	 */
	private void auditException(String errorCode, String errorMessage) {
		auditUtil.auditRequest(
				String.format(MasterDataConstant.FAILURE_DECOMMISSION,
						RegistrationCenterSearchDto.class.getSimpleName()),
				MasterDataConstant.AUDIT_SYSTEM,
				String.format(MasterDataConstant.FAILURE_DESC, errorCode, errorMessage), "ADM-524");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see io.mosip.kernel.masterdata.service.RegistrationCenterService# <<<<<<<
	 * HEAD createRegistrationCenterAdminPriSecLang(java.util.List)
	 */
	@Transactional
	@Override
	public RegistrationCenterExtnDto createRegistrationCenter(RegCenterPostReqDto regCenterPostReqDto) {

		// RegistrationCenterReqAdmSecDto registrationCenterReqAdmSecDtos
		// reqRegistrationCenterDto
		// RegistrationCenterPostResponseDto registrationCenterPostResponseDto =
		// new RegistrationCenterPostResponseDto();
		RegistrationCenter registrationCenterEntity = null;
		RegistrationCenterHistory registrationCenterHistoryEntity = null;
		RegistrationCenter registrationCenter = null;
		RegistrationCenterExtnDto registrationCenterExtnDto = new RegistrationCenterExtnDto();
		List<ExceptionalHolidayPutPostDto> exceptionalHolidayPutPostDtoList = new ArrayList<>();
		List<ServiceError> errors = new ArrayList<>();

		// List<RegistrationCenter> registrationCenterList = new ArrayList<>();
		// List<RegistrationCenterExtnDto> registrationCenterDtoList = null;
		// List<String> inputLangCodeList = new ArrayList<>();
		String uniqueId = "";

		// List<RegCenterPostReqDto> validateRegistrationCenterDtos = new
		// ArrayList<>();
		// List<RegCenterPostReqDto> constraintViolationedSecList = new
		// ArrayList<>();
		// List<ServiceError> errors = new ArrayList<>();

		// Method to validate all mandatory fields of both primary and secondary
		// language input objects
		// registrationCenterValidator.validatePrimarySencodaryLangMandatoryFields(regCenterPostReqDto,
		// registrationCenterPostResponseDto, inputLangCodeList,
		// validateRegistrationCenterDtos,
		// constraintViolationedSecList, errors);

		// validate to if Records with duplicate language code
		/*
		 * if ((new HashSet<String>(inputLangCodeList).size()) !=
		 * inputLangCodeList.size()) { throw new RequestException(
		 * RegistrationCenterErrorCode.
		 * REGISTRATION_CENTER_LANGUAGECODE_EXCEPTION.getErrorCode(),
		 * RegistrationCenterErrorCode.
		 * REGISTRATION_CENTER_LANGUAGECODE_EXCEPTION.getErrorMessage()); }
		 */

		try {

			registrationCenterValidator.validateRegCenterCreate(regCenterPostReqDto, errors);
			if (!errors.isEmpty()) {

				throw new ValidationException(errors);
			}
			// validate zone, Center start and end time and holidayCode
			RegistrationCenterType regCenterType = registrationCenterTypeRepository
					.findByCodeAndLangCodeAndIsDeletedFalseOrIsDeletedIsNull(regCenterPostReqDto.getCenterTypeCode(),
							regCenterPostReqDto.getLangCode());
			if (regCenterType == null) {
				auditUtil.auditRequest(
						String.format(MasterDataConstant.FAILURE_CREATE, RegCenterPostReqDto.class.getSimpleName()),
						MasterDataConstant.AUDIT_SYSTEM,
						String.format(MasterDataConstant.FAILURE_DESC,
								RegistrationCenterErrorCode.REGISTRATION_CENTER_INSERT_EXCEPTION.getErrorCode(),
								MasterDataConstant.INVALID_REG_CENTER_TYPE),
						"ADM-525");
				throw new MasterDataServiceException(
						RegistrationCenterErrorCode.REGISTRATION_CENTER_INSERT_EXCEPTION.getErrorCode(),
						MasterDataConstant.INVALID_REG_CENTER_TYPE);
			}
			List<Location> location = locationRepository.findLocationHierarchyByCodeAndLanguageCode(
					regCenterPostReqDto.getLocationCode(), regCenterPostReqDto.getLangCode());
			if (CollectionUtils.isEmpty(location)) {
				auditUtil.auditRequest(
						String.format(MasterDataConstant.FAILURE_CREATE, RegCenterPostReqDto.class.getSimpleName()),
						MasterDataConstant.AUDIT_SYSTEM,
						String.format(MasterDataConstant.FAILURE_DESC,
								RegistrationCenterErrorCode.REGISTRATION_CENTER_INSERT_EXCEPTION.getErrorCode(),
								MasterDataConstant.INVALID_LOCATION_CODE),
						"ADM-526");
				throw new MasterDataServiceException(
						RegistrationCenterErrorCode.REGISTRATION_CENTER_INSERT_EXCEPTION.getErrorCode(),
						MasterDataConstant.INVALID_LOCATION_CODE);
			}

			// call method generate ID or validate with DB
			regCenterPostReqDto = masterdataCreationUtil.createMasterData(RegistrationCenter.class,
					regCenterPostReqDto);

			// creating registration center Entity
			if (regCenterPostReqDto != null) {
				registrationCenterEntity = new RegistrationCenter();
				registrationCenterEntity = MetaDataUtils.setCreateMetaData(regCenterPostReqDto,
						registrationCenterEntity.getClass());
				registrationCenterExtnDto = MapperUtils.map(registrationCenterEntity, RegistrationCenterExtnDto.class);
			}
			// registrationCenterValidator.mapBaseDtoEntity(registrationCenterEntity,
			// registrationCenterDto);

			/*
			 * RegistrationCenterID from the rcid_Seq Table,
			 * RegistrationCenterID get by calling RegistrationCenterIdGenerator
			 * API method generateRegistrationCenterId().
			 * 
			 */try {
				if (registrationCenterEntity != null) {
					if (StringUtils.isNotEmpty(primaryLang) && primaryLang.equals(regCenterPostReqDto.getLangCode())) {
						uniqueId = registrationCenterValidator.generateIdOrvalidateWithDB(uniqueId);
						registrationCenterEntity.setId(uniqueId);
					}
					/*
					 * at the time of creation of new Registration Center Number
					 * of Kiosks value will be Zero always
					 */
					registrationCenterEntity.setNumberOfKiosks((short) 0);

					// registrationCenterEntity.setIsActive(false);
					registrationCenter = registrationCenterRepository.create(registrationCenterEntity);

					registrationCenterExtnDto = MapperUtils.map(registrationCenter, RegistrationCenterExtnDto.class);
				}
			} catch (NullPointerException e) {
				auditUtil.auditRequest(
						String.format(MasterDataConstant.FAILURE_CREATE, RegCenterPostReqDto.class.getSimpleName()),
						MasterDataConstant.AUDIT_SYSTEM,
						String.format(MasterDataConstant.FAILURE_DESC,
								RegistrationCenterErrorCode.REGISTRATION_CENTER_INSERT_EXCEPTION.getErrorCode(),
								RegistrationCenterErrorCode.REGISTRATION_CENTER_INSERT_EXCEPTION.getErrorMessage()),
						"ADM-827");

				errors.add(new ServiceError(RegistrationCenterErrorCode.WORKING_NON_WORKING_NULL.getErrorCode(),
						RegistrationCenterErrorCode.WORKING_NON_WORKING_NULL.getErrorMessage()));
			}
			try {
				if (StringUtils.isNotEmpty(primaryLang) && primaryLang.equals(regCenterPostReqDto.getLangCode())) {
					// insert 7 rows in reg_working_non_working table
					try {
						if (regCenterPostReqDto.getWorkingNonWorkingDays() != null) {
							createRegWorkingNonWorking(regCenterPostReqDto.getWorkingNonWorkingDays(),
									registrationCenterEntity);
						}
					} catch (NullPointerException e) {
						errors.add(new ServiceError(RegistrationCenterErrorCode.WORKING_NON_WORKING_NULL.getErrorCode(),
								RegistrationCenterErrorCode.WORKING_NON_WORKING_NULL.getErrorMessage()));
					}
					try {
						if (!regCenterPostReqDto.getExceptionalHolidayPutPostDto().isEmpty()) {
							// Exceptional holiday create
							createExpHoliday(regCenterPostReqDto.getExceptionalHolidayPutPostDto(),
									regCenterPostReqDto.getHolidayLocationCode(), registrationCenterEntity);
						}
					} catch (NullPointerException e) {
						errors.add(new ServiceError(RegistrationCenterErrorCode.WORKING_NON_WORKING_NULL.getErrorCode(),
								RegistrationCenterErrorCode.WORKING_NON_WORKING_NULL.getErrorMessage()));
					}
				}

			} catch (NullPointerException e) {
				auditUtil.auditRequest(
						String.format(MasterDataConstant.FAILURE_CREATE, RegCenterPostReqDto.class.getSimpleName()),
						MasterDataConstant.AUDIT_SYSTEM,
						String.format(MasterDataConstant.FAILURE_DESC,
								RegistrationCenterErrorCode.WORKING_NON_WORKING_NULL.getErrorCode(),
								RegistrationCenterErrorCode.WORKING_NON_WORKING_NULL.getErrorMessage()),
						"ADM-828");
				errors.add(new ServiceError(RegistrationCenterErrorCode.WORKING_NON_WORKING_NULL.getErrorCode(),
						RegistrationCenterErrorCode.WORKING_NON_WORKING_NULL.getErrorMessage()));
			}



			if ((regCenterPostReqDto!=null && ((primaryLang.equals(regCenterPostReqDto.getLangCode())
					|| regCenterPostReqDto.getWorkingNonWorkingDays() != null)
					|| secondaryLang.equals(regCenterPostReqDto.getLangCode())))) {
				// set response for working_non_working for both primary and
				// sencodary language
				setResponseDtoWorkingNonWorking(registrationCenter, registrationCenterExtnDto);

			}
			if(registrationCenter==null) {
				throw new MasterDataServiceException(RegistrationCenterErrorCode.REGISTRATION_CENTER_INSERT_EXCEPTION.getErrorCode(),
					RegistrationCenterErrorCode.REGISTRATION_CENTER_INSERT_EXCEPTION.getErrorMessage());
			}
			// set ExpHoliday Dto
			setRegExpHolidayDto(registrationCenter, registrationCenterExtnDto, exceptionalHolidayPutPostDtoList);

			// creating registration center history
			registrationCenterHistoryEntity = MetaDataUtils.setCreateMetaData(registrationCenterEntity!=null? registrationCenterEntity:null,
					RegistrationCenterHistory.class);
			registrationCenterHistoryEntity.setEffectivetimes(registrationCenterEntity.getCreatedDateTime());
			registrationCenterHistoryEntity.setCreatedDateTime(registrationCenterEntity.getCreatedDateTime());
			registrationCenterHistoryRepository.create(registrationCenterHistoryEntity);

		} catch (DataAccessLayerException | DataAccessException | IllegalArgumentException | IllegalAccessException
				| NoSuchFieldException | SecurityException exception) {
			auditUtil.auditRequest(
					String.format(MasterDataConstant.FAILURE_CREATE, RegCenterPostReqDto.class.getSimpleName()),
					MasterDataConstant.AUDIT_SYSTEM,
					String.format(MasterDataConstant.FAILURE_DESC,
							RegistrationCenterErrorCode.REGISTRATION_CENTER_INSERT_EXCEPTION.getErrorCode(),
							RegistrationCenterErrorCode.REGISTRATION_CENTER_INSERT_EXCEPTION.getErrorMessage()),
					"ADM-527");
			throw new MasterDataServiceException(
					RegistrationCenterErrorCode.REGISTRATION_CENTER_INSERT_EXCEPTION.getErrorCode(),
					RegistrationCenterErrorCode.REGISTRATION_CENTER_INSERT_EXCEPTION.getErrorMessage() + " "
							+ ExceptionUtils.parseException(exception));
		}
		auditUtil.auditRequest(
				String.format(MasterDataConstant.SUCCESSFUL_CREATE, RegCenterPostReqDto.class.getSimpleName()),
				MasterDataConstant.AUDIT_SYSTEM,
				String.format(MasterDataConstant.SUCCESSFUL_CREATE_DESC,
						RegistrationCenterSearchDto.class.getSimpleName(), registrationCenterExtnDto.getId()),
				"ADM-528");
		return registrationCenterExtnDto;

	}

	@Transactional
	private void setRegExpHolidayDto(RegistrationCenter registrationCenter,
			RegistrationCenterExtnDto registrationCenterExtnDto,
			List<ExceptionalHolidayPutPostDto> exceptionalHolidayDtoList) {
		List<RegExceptionalHoliday> dbRegExpHolidays = regExceptionalHolidayRepository
				.findByRegIdAndLangcode(registrationCenter.getId(), primaryLang);
		if (!dbRegExpHolidays.isEmpty()) {
			for (RegExceptionalHoliday regExceptionalHoliday : dbRegExpHolidays) {
				ExceptionalHolidayPutPostDto exceptionalHolidayDto = MapperUtils.map(regExceptionalHoliday,
						ExceptionalHolidayPutPostDto.class);
				exceptionalHolidayDto
						.setExceptionHolidayDate(regExceptionalHoliday.getExceptionHolidayDate().toString());
				exceptionalHolidayDtoList.add(exceptionalHolidayDto);
			}
			registrationCenterExtnDto.setExceptionalHolidayPutPostDto(exceptionalHolidayDtoList);

		}
	}

	// set response for working_non_working for both primary and sencodary
	// language
	@Transactional
	private void setResponseDtoWorkingNonWorking(RegistrationCenter registrationCenter,
			RegistrationCenterExtnDto registrationCenterExtnDto) {
		// if((primaryLang.equals(regCenterPostReqDto.getLangCode()) ||
		// regCenterPostReqDto.getWorkingNonWorkingDays() != null) ||
		// secondaryLang.equals(regCenterPostReqDto.getLangCode()) ){
		List<RegWorkingNonWorking> workingNonWorkingDays = regWorkingNonWorkingRepo
				.findByRegCenterIdAndlanguagecode(registrationCenter.getId(), primaryLang);
		WorkingNonWorkingDaysDto workDays = new WorkingNonWorkingDaysDto();
		if (!workingNonWorkingDays.isEmpty()) {
			for (RegWorkingNonWorking working : workingNonWorkingDays)

				switch (working.getDayCode()) {
				case "101":
					workDays.setSun(working.isWorking());
					break;
				case "102":
					workDays.setMon(working.isWorking());
					break;
				case "103":
					workDays.setTue(working.isWorking());
					break;
				case "104":
					workDays.setWed(working.isWorking());
					break;
				case "105":
					workDays.setThu(working.isWorking());
					break;
				case "106":
					workDays.setFri(working.isWorking());
					break;
				case "107":
					workDays.setSat(working.isWorking());
					break;
				default:

					break;
				}
		}

		registrationCenterExtnDto.setWorkingNonWorkingDays(workDays);
		// end of if }
	}

	@Transactional
	private void createExpHoliday(List<ExceptionalHolidayPutPostDto> reqExceptionalHolidayDtos,
			String holidayLocationCode, RegistrationCenter registrationCenterEntity) {
		if (!reqExceptionalHolidayDtos.isEmpty()) {
			List<LocalDate> dbHolidayList = holidayRepository.findHolidayByLocationCode1(holidayLocationCode,
					primaryLang);

			if (!dbHolidayList.isEmpty()) {
				// List<ExceptionalHolidayDto> exceptionalHolidayDtos =
				// reqExceptionalHolidayDto;
				if (!reqExceptionalHolidayDtos.isEmpty()) { // ***
					for (ExceptionalHolidayPutPostDto expHoliday : reqExceptionalHolidayDtos) {
						if (dbHolidayList.contains(LocalDate.parse((expHoliday.getExceptionHolidayDate())))) {
							throw new MasterDataServiceException(
									RegistrationCenterErrorCode.EXP_HOLIDAY_DATE.getErrorCode(),
									RegistrationCenterErrorCode.EXP_HOLIDAY_DATE.getErrorMessage());

						}
						RegExceptionalHoliday regExceptionalHoliday = null;
						regExceptionalHoliday = MetaDataUtils.setCreateMetaData(registrationCenterEntity,
								RegExceptionalHoliday.class);
						regExceptionalHoliday.setRegistrationCenterId(registrationCenterEntity.getId());
						regExceptionalHoliday
								.setExceptionHolidayDate(LocalDate.parse(expHoliday.getExceptionHolidayDate()));
						regExceptionalHoliday.setExceptionHolidayName(expHoliday.getExceptionHolidayName());
						regExceptionalHoliday.setExceptionHolidayReson(expHoliday.getExceptionHolidayReson());
						regExceptionalHoliday.setIsActive(true);
						regExceptionalHolidayRepository.create(regExceptionalHoliday);
					}
				}
			}
		}
	}

	@Transactional
	private void createRegWorkingNonWorking(WorkingNonWorkingDaysDto workingNonWorkingDays,
			RegistrationCenter registrationCenterEntity) {
		List<String> dayCodes;
		List<DaysOfWeek> daysOfWeek = daysOfWeekListRepo.findBylangCode(primaryLang);
		dayCodes = daysOfWeek.parallelStream().map(DaysOfWeek::getCode).collect(Collectors.toList());

		// WorkingNonWorkingDaysDto workingNonWorkingDays =
		// regCenterPostReqDto.getWorkingNonWorkingDays();
		if (workingNonWorkingDays != null) {
			Boolean[] working = { workingNonWorkingDays.getSun(), workingNonWorkingDays.getMon(),
					workingNonWorkingDays.getTue(), workingNonWorkingDays.getWed(), workingNonWorkingDays.getThu(),
					workingNonWorkingDays.getFri(), workingNonWorkingDays.getSat() };

			List<RegWorkingNonWorking> regWorkingNonWorkingEntityList = new ArrayList<>();
			int i = 0;
			for (String dayCode : dayCodes) {
				RegWorkingNonWorking regWorkingNonWorkingEntity = new RegWorkingNonWorking();
				regWorkingNonWorkingEntity.setRegistrationCenterId(registrationCenterEntity.getId());
				regWorkingNonWorkingEntity.setDayCode(dayCode);
				regWorkingNonWorkingEntity.setWorking(working[i]);
				i++;
				regWorkingNonWorkingEntity.setLanguagecode(registrationCenterEntity.getLangCode());
				regWorkingNonWorkingEntity.setIsActive(true);
				regWorkingNonWorkingEntity.setCreatedBy(registrationCenterEntity.getCreatedBy());
				regWorkingNonWorkingEntity.setCreatedDateTime(registrationCenterEntity.getCreatedDateTime());
				regWorkingNonWorkingEntityList.add(regWorkingNonWorkingEntity);
			}

			regWorkingNonWorkingRepo.saveAll(regWorkingNonWorkingEntityList);
		}

	}

	// -----------------------------------------update----------------------------------------
	/*
	 * (non-Javadoc)
	 * 
	 * @see io.mosip.kernel.masterdata.service.RegistrationCenterService#
	 * updateRegistrationCenter1(java.util.List)
	 */
	@Transactional
	@Override
	public RegistrationCenterExtnDto updateRegistrationCenter(RegCenterPutReqDto regCenterPutReqDto) {
		RegistrationCenter updRegistrationCenter = null;
		RegistrationCenter updRegistrationCenterEntity = null;
		RegistrationCenterExtnDto registrationCenterExtnDto = new RegistrationCenterExtnDto();
		RegistrationCenterHistory registrationCenterHistoryEntity = null;
		List<ServiceError> errors = new ArrayList<>();
		List<ExceptionalHolidayPutPostDto> exceptionalHolidayPutPostDtoList = new ArrayList<>();

		try {

			registrationCenterValidator.validateRegCenterUpdate(regCenterPutReqDto, errors);
			if (!errors.isEmpty()) {

				throw new ValidationException(errors);
			}
			RegistrationCenterType regCenterType = registrationCenterTypeRepository
					.findByCodeAndLangCodeAndIsDeletedFalseOrIsDeletedIsNull(regCenterPutReqDto.getCenterTypeCode(),
							regCenterPutReqDto.getLangCode());
			if (regCenterType == null) {
				auditUtil.auditRequest(
						String.format(MasterDataConstant.FAILURE_UPDATE, RegCenterPutReqDto.class.getSimpleName()),
						MasterDataConstant.AUDIT_SYSTEM,
						String.format(MasterDataConstant.FAILURE_DESC,
								RegistrationCenterErrorCode.REGISTRATION_CENTER_INSERT_EXCEPTION.getErrorCode(),
								"Invalid centerTypeCode"),
						"ADM-529");
				throw new MasterDataServiceException(
						RegistrationCenterErrorCode.REGISTRATION_CENTER_INSERT_EXCEPTION.getErrorCode(),
						"Invalid centerTypeCode");
			}

			List<Location> location = locationRepository.findLocationHierarchyByCodeAndLanguageCode(
					regCenterPutReqDto.getLocationCode(), regCenterPutReqDto.getLangCode());
			if (CollectionUtils.isEmpty(location)) {
				auditUtil.auditRequest(
						String.format(MasterDataConstant.FAILURE_UPDATE, RegCenterPutReqDto.class.getSimpleName()),
						MasterDataConstant.AUDIT_SYSTEM,
						String.format(MasterDataConstant.FAILURE_DESC,
								RegistrationCenterErrorCode.REGISTRATION_CENTER_INSERT_EXCEPTION.getErrorCode(),
								"Invalid Location Code"),
						"ADM-530");
				throw new MasterDataServiceException(
						RegistrationCenterErrorCode.REGISTRATION_CENTER_INSERT_EXCEPTION.getErrorCode(),
						"Invalid Location Code");
			}

			regCenterPutReqDto = masterdataCreationUtil.updateMasterData(RegistrationCenter.class, regCenterPutReqDto);
			if (regCenterPutReqDto != null) {

				RegistrationCenter renRegistrationCenter = registrationCenterRepository
						.findByIdAndLangCodeAndIsDeletedTrue(regCenterPutReqDto.getId(),
								regCenterPutReqDto.getLangCode());
				if (renRegistrationCenter == null && primaryLang.equals(regCenterPutReqDto.getLangCode())) {
					auditUtil.auditRequest(
							String.format(MasterDataConstant.FAILURE_UPDATE, RegCenterPutReqDto.class.getSimpleName()),
							MasterDataConstant.AUDIT_SYSTEM,
							String.format(MasterDataConstant.FAILURE_DESC,
									RegistrationCenterErrorCode.DECOMMISSIONED.getErrorCode(),
									RegistrationCenterErrorCode.DECOMMISSIONED.getErrorMessage()),
							"ADM-531");
					throw new MasterDataServiceException(RegistrationCenterErrorCode.DECOMMISSIONED.getErrorCode(),
							RegistrationCenterErrorCode.DECOMMISSIONED.getErrorMessage());
				} else if (renRegistrationCenter == null && secondaryLang.equals(regCenterPutReqDto.getLangCode())) {
					RegistrationCenter registrationCenterEntity = new RegistrationCenter();
					registrationCenterEntity = MetaDataUtils.setCreateMetaData(regCenterPutReqDto,
							registrationCenterEntity.getClass());
					registrationCenterEntity = registrationCenterRepository.create(registrationCenterEntity);
					registrationCenterHistoryEntity = MetaDataUtils.setCreateMetaData(registrationCenterEntity,
							RegistrationCenterHistory.class);
					registrationCenterHistoryEntity.setEffectivetimes(registrationCenterEntity.getCreatedDateTime());
					registrationCenterHistoryEntity.setCreatedDateTime(registrationCenterEntity.getCreatedDateTime());
					registrationCenterHistoryRepository.create(registrationCenterHistoryEntity);
					registrationCenterExtnDto = MapperUtils.map(registrationCenterEntity, registrationCenterExtnDto);
				}

				if (renRegistrationCenter != null) {
					validateZoneMachineDevice(renRegistrationCenter, regCenterPutReqDto);
				}

				if (renRegistrationCenter != null) {

					// updating registration center
					updRegistrationCenterEntity = MetaDataUtils.setUpdateMetaData(regCenterPutReqDto,
							renRegistrationCenter, false);
					updRegistrationCenterEntity.setIsActive(regCenterPutReqDto.getIsActive());
					updRegistrationCenter = registrationCenterRepository.update(updRegistrationCenterEntity);

					// New code start ****
					// update operation for WNW and ExpHoliday only for primary
					// langCode
					if (StringUtils.isNotEmpty(primaryLang) && primaryLang.equals(regCenterPutReqDto.getLangCode())) {
						updateWorkingNonWorking(updRegistrationCenter, regCenterPutReqDto, errors);

						updateExpHoliday(updRegistrationCenter, regCenterPutReqDto, errors);
					}

					if ((primaryLang.equals(regCenterPutReqDto.getLangCode())
							|| regCenterPutReqDto.getWorkingNonWorkingDays() != null)
							|| secondaryLang.equals(regCenterPutReqDto.getLangCode())) {
						// set response for working_non_working for both primary
						// and sencodary language
						setResponseDtoWorkingNonWorking(updRegistrationCenter, registrationCenterExtnDto);

					}

					if ((primaryLang.equals(regCenterPutReqDto.getLangCode())
							|| regCenterPutReqDto.getExceptionalHolidayPutPostDto() != null)
							|| secondaryLang.equals(regCenterPutReqDto.getLangCode())) {
						// set expHolidayDto
						setRegExpHolidayDto(updRegistrationCenter, registrationCenterExtnDto,
								exceptionalHolidayPutPostDtoList);

					}

					// creating registration center history
					RegistrationCenterHistory registrationCenterHistory = new RegistrationCenterHistory();
					MapperUtils.map(updRegistrationCenter, registrationCenterHistory);
					MapperUtils.setBaseFieldValue(updRegistrationCenter, registrationCenterHistory);
					registrationCenterHistory.setEffectivetimes(updRegistrationCenter.getUpdatedDateTime());
					registrationCenterHistory.setUpdatedDateTime(updRegistrationCenter.getUpdatedDateTime());
					registrationCenterHistoryRepository.create(registrationCenterHistory);
					registrationCenterExtnDto = MapperUtils.map(updRegistrationCenter, registrationCenterExtnDto);
				}
			}

		} catch (DataAccessLayerException | DataAccessException | IllegalArgumentException | IllegalAccessException
				| NoSuchFieldException | SecurityException exception) {
			auditUtil.auditRequest(
					String.format(MasterDataConstant.FAILURE_UPDATE, RegCenterPutReqDto.class.getSimpleName()),
					MasterDataConstant.AUDIT_SYSTEM,
					String.format(MasterDataConstant.FAILURE_DESC,
							RegistrationCenterErrorCode.REGISTRATION_CENTER_UPDATE_EXCEPTION.getErrorCode(),
							RegistrationCenterErrorCode.REGISTRATION_CENTER_UPDATE_EXCEPTION.getErrorMessage()),
					"ADM-532");
			throw new MasterDataServiceException(
					RegistrationCenterErrorCode.REGISTRATION_CENTER_UPDATE_EXCEPTION.getErrorCode(),
					RegistrationCenterErrorCode.REGISTRATION_CENTER_UPDATE_EXCEPTION.getErrorMessage()
							+ ExceptionUtils.parseException(exception));
		}
		auditUtil.auditRequest(
				String.format(MasterDataConstant.SUCCESSFUL_UPDATE, RegCenterPutReqDto.class.getSimpleName()),
				MasterDataConstant.AUDIT_SYSTEM, String.format(MasterDataConstant.SUCCESSFUL_UPDATE_DESC,
						RegCenterPutReqDto.class.getSimpleName(), registrationCenterExtnDto.getId()),
				"ADM-533");
		return registrationCenterExtnDto;

	}

	// update expHoliday
	@Transactional
	private void updateExpHoliday(RegistrationCenter updRegistrationCenter, RegCenterPutReqDto regCenterPutReqDto,
			List<ServiceError> errors) {
		try {
			Set<LocalDate> dbRegExceptionalHolidays = null;
			// get data from DB for the ID
			List<RegExceptionalHoliday> dbRegExcpHoliday = regExceptionalHolidayRepository
					.findByRegIdAndLangcode(updRegistrationCenter.getId(), primaryLang);

			dbRegExceptionalHolidays = dbRegExcpHoliday.stream().map(date -> date.getExceptionHolidayDate())
					.collect(Collectors.toSet());

			Set<LocalDate> holidayDates = regCenterPutReqDto.getExceptionalHolidayPutPostDto().stream()
					.map(s -> LocalDate.parse(s.getExceptionHolidayDate())).collect(Collectors.toSet());

			Collection<LocalDate> insertCollection = CollectionUtils.removeAll(holidayDates, dbRegExceptionalHolidays);
			Set<LocalDate> insertSet = new HashSet<>(insertCollection);

			Collection<LocalDate> deleteCollection = CollectionUtils.removeAll(dbRegExceptionalHolidays, holidayDates);
			Set<LocalDate> deleteSet = new HashSet<>(deleteCollection);

			List<ExceptionalHolidayPutPostDto> addExpHoliday = regCenterPutReqDto.getExceptionalHolidayPutPostDto()
					.stream().filter(s -> insertSet.contains(LocalDate.parse(s.getExceptionHolidayDate()))).map(s -> s)
					.collect(Collectors.toList());

			if (dbRegExceptionalHolidays.isEmpty()) {
				createReqExpHoldayAndDBEmpty(updRegistrationCenter, regCenterPutReqDto);
			} else if (CollectionUtils.isNotEmpty(insertSet) && CollectionUtils.isNotEmpty(addExpHoliday)) {
				createExpHoliday(addExpHoliday, regCenterPutReqDto.getHolidayLocationCode(), updRegistrationCenter);
			} else if (CollectionUtils.isNotEmpty(deleteSet)) {
				for (LocalDate dbHoliday : deleteSet) {
					deleteExpHoliday(updRegistrationCenter, dbHoliday);
				}
			}

		} catch (NullPointerException exp) {
			errors.add(new ServiceError(RegistrationCenterErrorCode.EXP_HOLIDAY_NULL.getErrorCode(),
					RegistrationCenterErrorCode.EXP_HOLIDAY_NULL.getErrorMessage()));
		}
	}

	// db is empty and req has data, so create new entry with req data
	private void createReqExpHoldayAndDBEmpty(RegistrationCenter updRegistrationCenter,
			RegCenterPutReqDto regCenterPutReqDto) {
		for (ExceptionalHolidayPutPostDto reqExpHoliday : regCenterPutReqDto.getExceptionalHolidayPutPostDto()) {
			List<ExceptionalHolidayPutPostDto> addExpHoliday = new ArrayList<>();
			addExpHoliday.add(reqExpHoliday);
			// create new expHoliday in DB for the Id with new expHoliday date
			// from request
			createExpHoliday(addExpHoliday, regCenterPutReqDto.getHolidayLocationCode(), updRegistrationCenter);
		}
	}

	// db is not empty and req is not empty

	// NEVER USED. HAD TO BE COMMENTED
	// private void createReqExpHolidayAndBDNotEmpt(RegistrationCenter
	// updRegistrationCenter,
	// RegCenterPutReqDto regCenterPutReqDto, Set<LocalDate>
	// dbRegExceptionalHolidays,
	// Set<LocalDate> reqHolidayDates) {
	// Set<LocalDate> holidayDates =
	// regCenterPutReqDto.getExceptionalHolidayPutPostDto().stream()
	// .map(s ->
	// LocalDate.parse(s.getExceptionHolidayDate())).collect(Collectors.toSet());
	// Set<LocalDate> symmetricDiff = new HashSet<>(dbRegExceptionalHolidays);
	// symmetricDiff.addAll(holidayDates);
	// // symmetricDiff now contains the union
	// Set<LocalDate> tmp = new HashSet<>(dbRegExceptionalHolidays);
	// tmp.retainAll(holidayDates);
	// // tmp now contains the intersection
	// symmetricDiff.removeAll(tmp);
	// List<ExceptionalHolidayPutPostDto> addExpHoliday = new ArrayList<>();
	// for (ExceptionalHolidayPutPostDto reqExpHoliday :
	// regCenterPutReqDto.getExceptionalHolidayPutPostDto()) {
	// for(LocalDate expDate:symmetricDiff)
	// {
	// if(reqExpHoliday.equals(expDate))
	// {
	// addExpHoliday.add(reqExpHoliday);
	// }
	// }
	//
	// }
	// createExpHoliday(addExpHoliday, regCenterPutReqDto.getHolidayLocationCode(),
	// updRegistrationCenter);
	// reqHolidayDates = new HashSet<>(holidayDates);
	//
	// /* for (ExceptionalHolidayPutPostDto reqExpHoliday :
	// regCenterPutReqDto.getExceptionalHolidayPutPostDto()) {
	// if
	// (dbRegExceptionalHolidays.contains(reqExpHoliday.getExceptionHolidayDate().trim()))
	// {
	// reqHolidayDates.add(LocalDate.parse(reqExpHoliday.getExceptionHolidayDate()));
	// } else {
	// List<ExceptionalHolidayPutPostDto> addExpHoliday = new ArrayList<>();
	// addExpHoliday.add(reqExpHoliday);
	// createExpHoliday(addExpHoliday, regCenterPutReqDto.getHolidayLocationCode(),
	// updRegistrationCenter);
	// }
	// reqHolidayDates.add(LocalDate.parse(reqExpHoliday.getExceptionHolidayDate()));
	// }*/
	// }


	@Transactional
	private void deleteExpHoliday(RegistrationCenter updRegistrationCenter, LocalDate dbHoliday) {

		RegExceptionalHoliday renEntity = regExceptionalHolidayRepository
				.findByRegIdAndLangcodeAndExpHoliday(updRegistrationCenter.getId(), primaryLang, dbHoliday);
		RegExceptionalHoliday regExceptionalHoliday = MetaDataUtils.setDeleteMetaData(renEntity);
		regExceptionalHolidayRepository.update(regExceptionalHoliday);

	}

	// create and update Working_Non_Working
	@Transactional
	private void updateWorkingNonWorking(RegistrationCenter updRegistrationCenter,
			RegCenterPutReqDto regCenterPutReqDto, List<ServiceError> errors) {
		try {
			// check WorkingNonWorking is present in request or not
			if (regCenterPutReqDto.getWorkingNonWorkingDays() != null) {
				// check workingNonWorking is present in DB or not for the given
				// regCenter Id
				// request
				List<RegWorkingNonWorking> dbRegWorkingNonWorkings = regWorkingNonWorkingRepo
						.findByRegCenterIdAndlanguagecode(regCenterPutReqDto.getId(), primaryLang);
				if (!dbRegWorkingNonWorkings.isEmpty()) {
					// in Data present , update operation
					updateRegWorkingNonWorking(regCenterPutReqDto, updRegistrationCenter, dbRegWorkingNonWorkings);
				} else {
					// in No data, create new record
					createRegWorkingNonWorking(regCenterPutReqDto.getWorkingNonWorkingDays(), updRegistrationCenter);
				}

			}
		} catch (NullPointerException exp) {
			errors.add(new ServiceError(RegistrationCenterErrorCode.WORKING_NON_WORKING_NULL.getErrorCode(),
					RegistrationCenterErrorCode.WORKING_NON_WORKING_NULL.getErrorMessage()));
		}

	}

	// update the WorkingNonWorking table
	@Transactional
	private void updateRegWorkingNonWorking(RegCenterPutReqDto regCenterPutReqDto,
			RegistrationCenter updRegistrationCenter, List<RegWorkingNonWorking> dbRegWorkingNonWorkings) {

		WorkingNonWorkingDaysDto workingNonWorkingDays = regCenterPutReqDto.getWorkingNonWorkingDays();

		Boolean[] working = { workingNonWorkingDays.getSun(), workingNonWorkingDays.getMon(),
				workingNonWorkingDays.getTue(), workingNonWorkingDays.getWed(), workingNonWorkingDays.getThu(),
				workingNonWorkingDays.getFri(), workingNonWorkingDays.getSat() };

		int i = 0;
		for (RegWorkingNonWorking regWorkingNonWorking : dbRegWorkingNonWorkings) {
			regWorkingNonWorking.setRegistrationCenterId(updRegistrationCenter.getId());
			regWorkingNonWorking.setWorking(working[i]);
			i++;
			regWorkingNonWorking.setLanguagecode(updRegistrationCenter.getLangCode());
			regWorkingNonWorking.setIsActive(true);
			regWorkingNonWorking.setUpdatedBy(updRegistrationCenter.getUpdatedBy());
			regWorkingNonWorking.setUpdatedDateTime(updRegistrationCenter.getUpdatedDateTime());
			regWorkingNonWorkingRepo.update(regWorkingNonWorking);
		}
	}

	private void validateZoneMachineDevice(RegistrationCenter regRegistrationCenter,
			RegCenterPutReqDto regCenterPutReqDto) {

		if (regRegistrationCenter.getZoneCode().equals(regCenterPutReqDto.getZoneCode())) {
			boolean isTagged = false;
			List<RegistrationCenterDevice> regDevice = registrationCenterDeviceRepository
					.findByRegCenterIdAndIsDeletedFalseOrIsDeletedIsNull(regCenterPutReqDto.getId());
			List<String> deviceZoneIds = regDevice.stream().map(s -> s.getDevice().getZoneCode())
					.collect(Collectors.toList());
			List<Zone> zoneHList = zoneUtils.getChildZoneList(deviceZoneIds, regCenterPutReqDto.getZoneCode(),
					regCenterPutReqDto.getLangCode());
			List<String> zoneHIdList = zoneHList.stream().map(z -> z.getCode()).collect(Collectors.toList());
			for (String deviceZone : deviceZoneIds) {
				if (!CollectionUtils.isEmpty(zoneHIdList) && zoneHIdList.contains(deviceZone)) {
					isTagged = true;
					break;
				}
			}

			if (isTagged) {
				throw new MasterDataServiceException("KER-MSD-397",
						"Cannot change the Center’s Administrative Zone as the Center is already mapped to a Device/Machine outside the new administrative zone");
			}
		}

	}

	/**
	 * Creating Search filter from the passed zones
	 * 
	 * @param zones
	 *            filter to be created with the zones
	 * @return list of {@link SearchFilter}
	 */
	private List<SearchFilter> buildZoneFilter(List<Zone> zones) {
		if (zones != null && !zones.isEmpty()) {
			return zones.stream().filter(Objects::nonNull).map(Zone::getCode).distinct().map(this::buildZoneFilter)
					.collect(Collectors.toList());
		}
		return Collections.emptyList();
	}

	/**
	 * Method to create SearchFilter for the recieved zoneCode
	 * 
	 * @param zoneCode
	 *            input from the {@link SearchFilter} has to be created
	 * @return {@link SearchFilter}
	 */
	private SearchFilter buildZoneFilter(String zoneCode) {
		SearchFilter filter = new SearchFilter();
		filter.setColumnName(MasterDataConstant.ZONE_CODE);
		filter.setType(FilterTypeEnum.EQUALS.name());
		filter.setValue(zoneCode);
		return filter;
	}

}