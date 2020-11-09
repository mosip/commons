package io.mosip.kernel.masterdata.service.impl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;

import io.mosip.kernel.core.dataaccess.exception.DataAccessLayerException;
import io.mosip.kernel.core.http.RequestWrapper;
import io.mosip.kernel.masterdata.constant.HolidayErrorCode;
import io.mosip.kernel.masterdata.constant.LocationErrorCode;
import io.mosip.kernel.masterdata.constant.MasterDataConstant;
import io.mosip.kernel.masterdata.constant.RequestErrorCode;
import io.mosip.kernel.masterdata.dto.HolidayDto;
import io.mosip.kernel.masterdata.dto.HolidayIDDto;
import io.mosip.kernel.masterdata.dto.HolidayIdDeleteDto;
import io.mosip.kernel.masterdata.dto.HolidayUpdateDto;
import io.mosip.kernel.masterdata.dto.LocationDto;
import io.mosip.kernel.masterdata.dto.getresponse.HolidayResponseDto;
import io.mosip.kernel.masterdata.dto.getresponse.PageDto;
import io.mosip.kernel.masterdata.dto.getresponse.extn.HolidayExtnDto;
import io.mosip.kernel.masterdata.dto.request.FilterDto;
import io.mosip.kernel.masterdata.dto.request.FilterValueDto;
import io.mosip.kernel.masterdata.dto.request.Pagination;
import io.mosip.kernel.masterdata.dto.request.SearchDto;
import io.mosip.kernel.masterdata.dto.request.SearchFilter;
import io.mosip.kernel.masterdata.dto.request.SearchSort;
import io.mosip.kernel.masterdata.dto.response.ColumnValue;
import io.mosip.kernel.masterdata.dto.response.FilterResponseDto;
import io.mosip.kernel.masterdata.dto.response.HolidaySearchDto;
import io.mosip.kernel.masterdata.dto.response.PageResponseDto;
import io.mosip.kernel.masterdata.entity.Holiday;
import io.mosip.kernel.masterdata.entity.Location;
import io.mosip.kernel.masterdata.exception.DataNotFoundException;
import io.mosip.kernel.masterdata.exception.MasterDataServiceException;
import io.mosip.kernel.masterdata.exception.RequestException;
import io.mosip.kernel.masterdata.repository.HolidayRepository;
import io.mosip.kernel.masterdata.repository.LocationRepository;
import io.mosip.kernel.masterdata.service.HolidayService;
import io.mosip.kernel.masterdata.utils.AuditUtil;
import io.mosip.kernel.masterdata.utils.ExceptionUtils;
import io.mosip.kernel.masterdata.utils.MapperUtils;
import io.mosip.kernel.masterdata.utils.MasterDataFilterHelper;
import io.mosip.kernel.masterdata.utils.MasterdataCreationUtil;
import io.mosip.kernel.masterdata.utils.MasterdataSearchHelper;
import io.mosip.kernel.masterdata.utils.MetaDataUtils;
import io.mosip.kernel.masterdata.utils.OptionalFilter;
import io.mosip.kernel.masterdata.utils.PageUtils;
import io.mosip.kernel.masterdata.validator.FilterColumnValidator;
import io.mosip.kernel.masterdata.validator.FilterTypeEnum;
import io.mosip.kernel.masterdata.validator.FilterTypeValidator;

/**
 * Service Impl class for Holiday Data
 * 
 * @author Sidhant Agarwal
 * @author Abhishek Kumar
 * @since 1.0.0
 *
 */
@Service
public class HolidayServiceImpl implements HolidayService {
	@Autowired
	private HolidayRepository holidayRepository;
	@Autowired
	private MasterdataSearchHelper masterdataSearchHelper;
	@Autowired
	private FilterTypeValidator filterValidator;
	@Autowired
	private LocationRepository locationRepository;
	@Autowired
	private FilterColumnValidator filterColumnValidator;
	@Autowired
	private MasterDataFilterHelper masterDataFilterHelper;
	@Autowired
	private PageUtils pageUtils;
	@Autowired
	private MasterdataCreationUtil masterdataCreationUtil;

	@Autowired
	private AuditUtil auditUtil;
	
	@Value("${mosip.primary-language:eng}")
	private String primaryLang;

	@Value("${mosip.secondary-language:ara}")
	private String secondaryLang;

	private static final String UPDATE_HOLIDAY_QUERY = "UPDATE Holiday h SET h.isActive = :isActive ,h.updatedBy = :updatedBy , h.updatedDateTime = :updatedDateTime, h.holidayDesc = :holidayDesc,h.holidayId.holidayDate=:holidayDate,h.holidayId.holidayName = :holidayName   WHERE h.holidayId.locationCode = :locationCode  and h.holidayId.holidayId = :holidayId and h.holidayId.langCode = :langCode and (h.isDeleted is null or h.isDeleted = false)";
	private static final int DEFAULT_HOLIDAY_ID = 2000001;

	/*
	 * (non-Javadoc)
	 * 
	 * @see io.mosip.kernel.masterdata.service.HolidayService#getAllHolidays()
	 */
	@Override
	public HolidayResponseDto getAllHolidays() {
		HolidayResponseDto holidayResponseDto = null;
		List<HolidayDto> holidayDto = null;
		List<Holiday> holidays = null;
		try {
			holidays = holidayRepository.findAllNonDeletedHoliday();
		} catch (DataAccessException | DataAccessLayerException dataAccessException) {
			throw new MasterDataServiceException(HolidayErrorCode.HOLIDAY_FETCH_EXCEPTION.getErrorCode(),
					HolidayErrorCode.HOLIDAY_FETCH_EXCEPTION.getErrorMessage());
		}

		if (holidays != null && !holidays.isEmpty()) {
			holidayDto = MapperUtils.mapHolidays(holidays);
		} else {
			throw new DataNotFoundException(HolidayErrorCode.HOLIDAY_NOTFOUND.getErrorCode(),
					HolidayErrorCode.HOLIDAY_NOTFOUND.getErrorMessage());
		}

		holidayResponseDto = new HolidayResponseDto();
		holidayResponseDto.setHolidays(holidayDto);
		return holidayResponseDto;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see io.mosip.kernel.masterdata.service.HolidayService#getHolidayById(int)
	 */
	@Override
	public HolidayResponseDto getHolidayById(int id) {

		HolidayResponseDto holidayResponseDto = null;
		List<HolidayDto> holidayDto = null;
		List<Holiday> holidays = null;
		try {
			holidays = holidayRepository.findAllById(id);
		} catch (DataAccessException | DataAccessLayerException dataAccessException) {
			throw new MasterDataServiceException(HolidayErrorCode.HOLIDAY_FETCH_EXCEPTION.getErrorCode(),
					HolidayErrorCode.HOLIDAY_FETCH_EXCEPTION.getErrorMessage());
		}

		if (holidays != null && !holidays.isEmpty()) {
			holidayDto = MapperUtils.mapHolidays(holidays);
		} else {
			throw new DataNotFoundException(HolidayErrorCode.HOLIDAY_NOTFOUND.getErrorCode(),
					HolidayErrorCode.HOLIDAY_NOTFOUND.getErrorMessage());
		}

		holidayResponseDto = new HolidayResponseDto();
		holidayResponseDto.setHolidays(holidayDto);
		return holidayResponseDto;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see io.mosip.kernel.masterdata.service.HolidayService#
	 * getHolidayByIdAndLanguageCode(int, java.lang.String)
	 */
	@Override
	public HolidayResponseDto getHolidayByIdAndLanguageCode(int id, String langCode) {
		HolidayResponseDto holidayResponseDto = null;
		List<HolidayDto> holidayList = null;
		List<Holiday> holidays = null;
		try {
			holidays = holidayRepository.findHolidayByIdAndHolidayIdLangCode(id, langCode);
		} catch (DataAccessException | DataAccessLayerException dataAccessException) {
			throw new MasterDataServiceException(HolidayErrorCode.HOLIDAY_FETCH_EXCEPTION.getErrorCode(),
					HolidayErrorCode.HOLIDAY_FETCH_EXCEPTION.getErrorMessage());
		}

		if (holidays != null && !holidays.isEmpty()) {
			holidayList = MapperUtils.mapHolidays(holidays);
		} else {
			throw new DataNotFoundException(HolidayErrorCode.HOLIDAY_NOTFOUND.getErrorCode(),
					HolidayErrorCode.HOLIDAY_NOTFOUND.getErrorMessage());
		}
		holidayResponseDto = new HolidayResponseDto();
		holidayResponseDto.setHolidays(holidayList);
		return holidayResponseDto;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * io.mosip.kernel.masterdata.service.HolidayService#saveHoliday(io.mosip.kernel
	 * .masterdata.dto.RequestDto)
	 */
	@Override
	public HolidayIDDto saveHoliday(HolidayDto holidayDto) {
		
		Holiday entity=null;
		Holiday holiday=null;
		HolidayIDDto holidayId = new HolidayIDDto();
		try {
			if (holidayDto != null) {
				List<Location> locations =locationRepository.findByCode(holidayDto.getLocationCode());
				if(locations==null || locations.isEmpty()) {
					auditUtil.auditRequest(
							String.format(MasterDataConstant.FAILURE_CREATE, Holiday.class.getSimpleName()),
							MasterDataConstant.AUDIT_SYSTEM,
							String.format(MasterDataConstant.FAILURE_DESC,
									HolidayErrorCode.HOLIDAY_LOCATION_INVALID.getErrorCode(),
									HolidayErrorCode.HOLIDAY_LOCATION_INVALID.getErrorMessage()),
							"ADM-2163");
					throw new RequestException(HolidayErrorCode.HOLIDAY_LOCATION_INVALID.getErrorCode(),
							HolidayErrorCode.HOLIDAY_LOCATION_INVALID.getErrorMessage());
				}
				if(holidayRepository.findHolidayByHolidayNameHolidayDateLocationCodeLangCode(holidayDto.getHolidayName(),holidayDto.getHolidayDate(),
						holidayDto.getLocationCode(),holidayDto.getLangCode()) != null){
					auditUtil.auditRequest(
							String.format(MasterDataConstant.FAILURE_CREATE, Holiday.class.getSimpleName()),
							MasterDataConstant.AUDIT_SYSTEM,
							String.format(MasterDataConstant.FAILURE_DESC,
									HolidayErrorCode.DUPLICATE_REQUEST.getErrorCode(),
									HolidayErrorCode.DUPLICATE_REQUEST.getErrorMessage()),
							"ADM-2164");
					throw new RequestException(HolidayErrorCode.DUPLICATE_REQUEST.getErrorCode(),
							HolidayErrorCode.DUPLICATE_REQUEST.getErrorMessage());
				}
				holidayDto.setIsActive(getisActive(holidayDto.getHolidayName(),holidayDto.getHolidayDate(),holidayDto.getLangCode(),
						holidayDto.getLocationCode(),holidayDto.getIsActive()));
				entity = MetaDataUtils.setCreateMetaData(holidayDto, Holiday.class);
				List<Holiday> hols=holidayRepository.findHolidayByHolidayDateHolidayName(holidayDto.getHolidayDate(),holidayDto.getHolidayName());
				List<Holiday> holidays=holidayRepository.findAll();
			
				if(holidays==null || holidays.isEmpty() ) {
					entity.setHolidayId(DEFAULT_HOLIDAY_ID);
				}
				else if(hols!=null && !hols.isEmpty()){
					entity.setHolidayId(hols.get(0).getHolidayId());
				}
				else {
					List<Integer> holidayIds=new ArrayList<>();
					for(Holiday holidayEntry:holidays) {
						holidayIds.add(Integer.valueOf(holidayEntry.getHolidayId()));
					}
				
				entity.setHolidayId(Collections.max(holidayIds)+1);
			}
			
			holiday = holidayRepository.create(entity);
			if(holiday.getIsActive()==true && holiday.getLangCode().equalsIgnoreCase(secondaryLang)) {
				Holiday primholiday=holidayRepository.findHolidayByHolidayNameHolidayDateLocationCodeLangCode(holiday.getHolidayName(),holiday.getHolidayDate(),
						holiday.getLocationCode(),primaryLang);
				primholiday.setIsActive(true);
				holidayRepository.update(primholiday);
			}
			MapperUtils.map(holiday, holidayId);
			}
		}catch (DataAccessLayerException | DataAccessException | IllegalArgumentException |  SecurityException e) {
			auditUtil.auditRequest(String.format(MasterDataConstant.FAILURE_UPDATE, Holiday.class.getSimpleName()),
					MasterDataConstant.AUDIT_SYSTEM,
					String.format(MasterDataConstant.FAILURE_DESC,
							HolidayErrorCode.HOLIDAY_INSERT_EXCEPTION.getErrorCode(), ExceptionUtils.parseException(e)),
					"ADM-2163");
			throw new MasterDataServiceException(HolidayErrorCode.HOLIDAY_INSERT_EXCEPTION.getErrorCode(),
					ExceptionUtils.parseException(e));
		}
		
		auditUtil.auditRequest(String.format(MasterDataConstant.SUCCESSFUL_CREATE, Holiday.class.getSimpleName()),
				MasterDataConstant.AUDIT_SYSTEM,
				String.format(MasterDataConstant.SUCCESSFUL_CREATE_DESC, Holiday.class.getSimpleName(), holidayId),
				"ADM-2162");
		return holidayId;
	}

	private boolean getisActive(String holidayName,LocalDate holidayDate,String langCode,String locationCode,boolean isActive) {
		if(langCode.equalsIgnoreCase(primaryLang)) {
			Holiday holiday=holidayRepository.findHolidayByHolidayNameHolidayDateLocationCodeLangCode(holidayName,holidayDate,
					locationCode,secondaryLang);
			if(holiday==null) {
				return false;
			}
		}
		if(langCode.equalsIgnoreCase(secondaryLang)) {
			Holiday holiday=holidayRepository.findHolidayByHolidayNameHolidayDateLocationCodeLangCode(holidayName,holidayDate,
					locationCode,primaryLang);
			if(holiday==null) {
				throw new MasterDataServiceException(RequestErrorCode.REQUEST_INVALID_SEC_LANG_ID.getErrorCode(),
						RequestErrorCode.REQUEST_INVALID_SEC_LANG_ID.getErrorMessage());
			}
		}
		return isActive;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * io.mosip.kernel.masterdata.service.HolidayService#updateHoliday(io.mosip.
	 * kernel.masterdata.dto.RequestDto)
	 */
	@Override
	public HolidayIDDto updateHoliday(HolidayUpdateDto holidayDto) {
		HolidayIDDto idDto = null;
		
		try {
			List<Location> locations =locationRepository.findByCode(holidayDto.getLocationCode());
			if(locations==null || locations.isEmpty()) {
				auditUtil.auditRequest(
						String.format(MasterDataConstant.FAILURE_UPDATE, Holiday.class.getSimpleName()),
						MasterDataConstant.AUDIT_SYSTEM, String.format(HolidayErrorCode.UPDATE_HOLIDAY_LOCATION_INVALID.getErrorCode(),
								HolidayErrorCode.UPDATE_HOLIDAY_LOCATION_INVALID.getErrorMessage()),
						"ADM-2166");
				throw new RequestException(HolidayErrorCode.UPDATE_HOLIDAY_LOCATION_INVALID.getErrorCode(),
						HolidayErrorCode.UPDATE_HOLIDAY_LOCATION_INVALID.getErrorMessage());
			}
			holidayDto.setIsActive(getisActive(holidayDto.getHolidayName(),holidayDto.getHolidayDate(),holidayDto.getLangCode(),
					holidayDto.getLocationCode(),holidayDto.getIsActive()));
			Map<String, Object> params = bindDtoToMap(holidayDto);
			int noOfRowAffected = holidayRepository.createQueryUpdateOrDelete(UPDATE_HOLIDAY_QUERY, params);			
			if (noOfRowAffected != 0) {
				idDto = mapToHolidayIdDto(holidayDto);
			} else {
				auditUtil.auditRequest(
						String.format(MasterDataConstant.FAILURE_UPDATE, Holiday.class.getSimpleName()),
						MasterDataConstant.AUDIT_SYSTEM, String.format(HolidayErrorCode.HOLIDAY_NOTFOUND.getErrorCode(),
								HolidayErrorCode.HOLIDAY_NOTFOUND.getErrorMessage()),
						"ADM-2166");
				throw new RequestException(HolidayErrorCode.HOLIDAY_NOTFOUND.getErrorCode(),
						HolidayErrorCode.HOLIDAY_NOTFOUND.getErrorMessage());
			}

		} catch (DataAccessException | DataAccessLayerException | IllegalArgumentException | SecurityException e) {
			auditUtil.auditRequest(
					String.format(MasterDataConstant.FAILURE_UPDATE, Holiday.class.getSimpleName()),
					MasterDataConstant.AUDIT_SYSTEM, String.format(HolidayErrorCode.HOLIDAY_UPDATE_EXCEPTION.getErrorCode(),
							HolidayErrorCode.HOLIDAY_UPDATE_EXCEPTION.getErrorMessage()),
					"ADM-2166");
			throw new MasterDataServiceException(HolidayErrorCode.HOLIDAY_UPDATE_EXCEPTION.getErrorCode(),
					HolidayErrorCode.HOLIDAY_UPDATE_EXCEPTION.getErrorMessage() + ExceptionUtils.parseException(e));
		}
		auditUtil.auditRequest(
				String.format(MasterDataConstant.SUCCESSFUL_UPDATE, Holiday.class.getSimpleName()),
				MasterDataConstant.AUDIT_SYSTEM,
				String.format(MasterDataConstant.SUCCESSFUL_UPDATE_DESC, Holiday.class.getSimpleName(), idDto),
				"ADM-2165");
		return idDto;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * io.mosip.kernel.masterdata.service.HolidayService#deleteHoliday(io.mosip.
	 * kernel.masterdata.entity.id.HolidayID)
	 */
	@Override
	public HolidayIdDeleteDto deleteHoliday(RequestWrapper<HolidayIdDeleteDto> request) {
		HolidayIdDeleteDto idDto = request.getRequest();
		try {
			int affectedRows = holidayRepository.deleteHolidays(LocalDateTime.now(ZoneId.of("UTC")),
					idDto.getHolidayName(), idDto.getHolidayDate(), idDto.getLocationCode());
			if (affectedRows == 0)
				throw new RequestException(HolidayErrorCode.HOLIDAY_NOTFOUND.getErrorCode(),
						HolidayErrorCode.HOLIDAY_NOTFOUND.getErrorMessage());

		} catch (DataAccessException | DataAccessLayerException e) {
			throw new MasterDataServiceException(HolidayErrorCode.HOLIDAY_DELETE_EXCEPTION.getErrorCode(),
					HolidayErrorCode.HOLIDAY_DELETE_EXCEPTION.getErrorMessage() + ExceptionUtils.parseException(e));
		}
		return idDto;
	}

	/**
	 * Bind {@link HolidayUpdateDto} dto to {@link Map}
	 * 
	 * @param dto input {@link HolidayUpdateDto}
	 * @return {@link Map} with the named parameter and value
	 */
	private Map<String, Object> bindDtoToMap(HolidayUpdateDto dto) {
		Map<String, Object> params = new HashMap<>();
		params.put("holidayId", dto.getHolidayId());
		params.put("holidayDesc", dto.getHolidayDesc());
		params.put("isActive", dto.getIsActive());
		params.put("holidayDate", dto.getHolidayDate());
		params.put("holidayName", dto.getHolidayName());
		params.put("updatedBy", MetaDataUtils.getContextUser());
		params.put("updatedDateTime", LocalDateTime.now(ZoneId.of("UTC")));
		params.put("locationCode", dto.getLocationCode());
		params.put("langCode", dto.getLangCode());
		return params;
	}

	/**
	 * Bind the {@link HolidayUpdateDto} to {@link HolidayIDDto}
	 * 
	 * @param dto input {@link HolidayUpdateDto} to be bind
	 * @return {@link HolidayIDDto} holiday id
	 */
	private HolidayIDDto mapToHolidayIdDto(HolidayUpdateDto dto) {
		HolidayIDDto idDto;
		idDto = new HolidayIDDto();
		if (dto.getHolidayName() != null)
			idDto.setHolidayName(dto.getHolidayName());
		else
			idDto.setHolidayName(dto.getHolidayName());
		if (dto.getHolidayDate() != null)
			idDto.setHolidayDate(dto.getHolidayDate());
		else
			idDto.setHolidayDate(dto.getHolidayDate());
		idDto.setLocationCode(dto.getLocationCode());
		idDto.setLangCode(dto.getLangCode());
		return idDto;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see io.mosip.kernel.masterdata.service.HolidayService#getHolidays(int, int,
	 * java.lang.String, java.lang.String)
	 */
	@Override
	public PageDto<HolidayExtnDto> getHolidays(int pageNumber, int pageSize, String sortBy, String orderBy) {
		List<HolidayExtnDto> holidays = null;
		PageDto<HolidayExtnDto> pageDto = null;
		try {
			Page<Holiday> pageData = holidayRepository
					.findAll(PageRequest.of(pageNumber, pageSize, Sort.by(Direction.fromString(orderBy), sortBy)));
			if (pageData != null && pageData.getContent() != null && !pageData.getContent().isEmpty()) {
				holidays = MapperUtils.mapAll(pageData.getContent(), HolidayExtnDto.class);
				pageDto = new PageDto<>(pageData.getNumber(), pageData.getTotalPages(), pageData.getTotalElements(),
						holidays);
			} else {
				throw new DataNotFoundException(HolidayErrorCode.HOLIDAY_NOTFOUND.getErrorCode(),
						HolidayErrorCode.HOLIDAY_NOTFOUND.getErrorMessage());
			}
		} catch (DataAccessException | DataAccessLayerException dataAccessException) {
			throw new MasterDataServiceException(HolidayErrorCode.HOLIDAY_FETCH_EXCEPTION.getErrorCode(),
					HolidayErrorCode.HOLIDAY_FETCH_EXCEPTION.getErrorMessage());
		}
		return pageDto;
	}

	@Override
	public PageResponseDto<HolidaySearchDto> searchHolidays(SearchDto dto) {
		PageResponseDto<HolidaySearchDto> pageDto = new PageResponseDto<>();
		List<HolidayExtnDto> holidayDtos = null;
		List<SearchFilter> addList = new ArrayList<>();
		List<SearchFilter> removeList = new ArrayList<>();
		List<Location> locationList = null;
		try {
			locationList = locationRepository.findByLangCode(dto.getLanguageCode());
		} catch (DataAccessException | DataAccessLayerException e) {
			auditUtil.auditRequest(
					String.format(MasterDataConstant.FAILURE_UPDATE, HolidayUpdateDto.class.getSimpleName()),
					MasterDataConstant.AUDIT_SYSTEM,
					String.format(LocationErrorCode.LOCATION_FETCH_EXCEPTION.getErrorCode(),
							LocationErrorCode.LOCATION_FETCH_EXCEPTION.getErrorMessage()
									+ ExceptionUtils.parseException(e)),
					"ADM-804");
			throw new MasterDataServiceException(LocationErrorCode.LOCATION_FETCH_EXCEPTION.getErrorCode(),
					LocationErrorCode.LOCATION_FETCH_EXCEPTION.getErrorMessage() + ExceptionUtils.parseException(e));
		}
		if (!locationList.isEmpty()) {
			for (SearchFilter filter : dto.getFilters()) {
				String column = filter.getColumnName();
				if (column.equalsIgnoreCase("name")) {
					if (filterValidator.validate(LocationDto.class, Arrays.asList(filter))) {
						Page<Location> locations = masterdataSearchHelper.searchMasterdata(Location.class,
								new SearchDto(Arrays.asList(filter), Collections.emptyList(), new Pagination(),
										dto.getLanguageCode()),
								null);
						List<SearchFilter> locationCodeFilter = buildLocationSearchFilter(locations.getContent());
						if (locationCodeFilter.isEmpty()) {

							return pageDto;
						}
						addList.addAll(locationCodeFilter);
						removeList.add(filter);
					}
				}
			}
			dto.getFilters().removeAll(removeList);
			Pagination pagination = dto.getPagination();
			List<SearchSort> sort = dto.getSort();
			dto.setPagination(new Pagination(0, Integer.MAX_VALUE));
			dto.setSort(Collections.emptyList());
			List<HolidaySearchDto> resultDto = new ArrayList<>();
			pageUtils.validateSortField(HolidaySearchDto.class, Holiday.class, sort);
			if (filterValidator.validate(HolidaySearchDto.class, dto.getFilters())) {
				OptionalFilter optionalFilter = new OptionalFilter(addList);
				Page<Holiday> page = masterdataSearchHelper.searchMasterdata(Holiday.class, dto,
						new OptionalFilter[] { optionalFilter });
				if (page.getContent() != null && !page.getContent().isEmpty()) {
					holidayDtos = MapperUtils.mapAll(page.getContent(), HolidayExtnDto.class);
					Map<Integer, List<HolidayExtnDto>> holidayPerHolidayType = holidayDtos.stream()
							.collect(Collectors.groupingBy(HolidayExtnDto::getHolidayId));
					for (Map.Entry<Integer, List<HolidayExtnDto>> entry : holidayPerHolidayType.entrySet()) {
						HolidaySearchDto holidaySearchDto = new HolidaySearchDto();
						setMetaData(entry.getValue(), locationList, holidaySearchDto);
						MapperUtils.map(entry.getValue().get(0), holidaySearchDto);
						resultDto.add(holidaySearchDto);
					}
				}
				pageDto = pageUtils.sortPage(resultDto, sort, pagination);
			}
		} else {
			auditUtil.auditRequest(
					String.format(MasterDataConstant.FAILURE_UPDATE, HolidayUpdateDto.class.getSimpleName()),
					MasterDataConstant.AUDIT_SYSTEM,
					String.format(LocationErrorCode.LOCATION_NOT_FOUND_EXCEPTION.getErrorCode(),
							LocationErrorCode.LOCATION_NOT_FOUND_EXCEPTION.getErrorMessage()),
					"ADM-805");
			throw new DataNotFoundException(LocationErrorCode.LOCATION_NOT_FOUND_EXCEPTION.getErrorCode(),
					LocationErrorCode.LOCATION_NOT_FOUND_EXCEPTION.getErrorMessage());
		}
		return pageDto;
	}

	@Override
	public FilterResponseDto holidaysFilterValues(FilterValueDto filterValueDto) {
		FilterResponseDto filterResponseDto = new FilterResponseDto();
		List<ColumnValue> columnValueList = new ArrayList<>();
		if (filterColumnValidator.validate(FilterDto.class, filterValueDto.getFilters(), Holiday.class)) {
			for (FilterDto filterDto : filterValueDto.getFilters()) {
				List<?> filterValues = masterDataFilterHelper.filterValues(Holiday.class, filterDto, filterValueDto);
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

	/**
	 * This method return Machine Types list filters.
	 * 
	 * @param machineTypes the list of Machine Type.
	 * @return the list of {@link SearchFilter}.
	 */
	private List<SearchFilter> buildLocationSearchFilter(List<Location> locations) {
		if (locations != null && !locations.isEmpty())
			return locations.stream().filter(Objects::nonNull).map(this::buildLocations).collect(Collectors.toList());
		return Collections.emptyList();
	}

	/**
	 * This method provide search filter for provided Machine Type.
	 * 
	 * @param machineType the machine type.
	 * @return the {@link SearchFilter}.
	 */
	private SearchFilter buildLocations(Location location) {
		SearchFilter filter = new SearchFilter();
		filter.setColumnName("locationCode");
		filter.setType(FilterTypeEnum.EQUALS.name());
		filter.setValue(location.getCode());
		return filter;
	}

	private static void setMetaData(List<HolidayExtnDto> holidays, List<Location> locations,
			HolidaySearchDto searchDto) {
		Set<String> holidayLocations = holidays.stream().map(HolidayExtnDto::getLocationCode)
				.collect(Collectors.toSet());
		if (!holidayLocations.isEmpty()) {
			String locationNames = locations.stream().filter(i -> holidayLocations.contains(i.getCode()))
					.map(Location::getName).collect(Collectors.joining(","));

			searchDto.setName(locationNames);
		}
	}

}
