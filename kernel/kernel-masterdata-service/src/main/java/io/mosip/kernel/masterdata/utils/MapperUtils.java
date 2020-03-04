
package io.mosip.kernel.masterdata.utils;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;

import org.springframework.stereotype.Component;

import io.mosip.kernel.core.dataaccess.exception.DataAccessLayerException;
import io.mosip.kernel.core.util.EmptyCheckUtils;
import io.mosip.kernel.masterdata.dto.DeviceLangCodeDtypeDto;
import io.mosip.kernel.masterdata.dto.ExceptionalHolidayDto;
import io.mosip.kernel.masterdata.dto.HolidayDto;
import io.mosip.kernel.masterdata.dto.ReasonCategoryDto;
import io.mosip.kernel.masterdata.dto.ReasonListDto;
import io.mosip.kernel.masterdata.dto.RegisteredDevicePostReqDto;
import io.mosip.kernel.masterdata.dto.getresponse.LocationHierarchyDto;
import io.mosip.kernel.masterdata.dto.getresponse.extn.BaseDto;
import io.mosip.kernel.masterdata.dto.registerdevice.DeviceData;
import io.mosip.kernel.masterdata.dto.registerdevice.DigitalId;
import io.mosip.kernel.masterdata.dto.registerdevice.RegisterDeviceResponse;
import io.mosip.kernel.masterdata.dto.registerdevice.RegisteredDevicePostDto;
import io.mosip.kernel.masterdata.entity.BaseEntity;
import io.mosip.kernel.masterdata.entity.ExceptionalHoliday;
import io.mosip.kernel.masterdata.entity.Holiday;
import io.mosip.kernel.masterdata.entity.ReasonCategory;
import io.mosip.kernel.masterdata.entity.RegisteredDevice;

/**
 * MapperUtils class provides methods to map or copy values from source object
 * to destination object.
 * 
 * @author Bal Vikash Sharma
 * @author Urvil Joshi
 * @since 1.0.0
 * @see MapperUtils
 *
 */
@Component
@SuppressWarnings("unchecked")
public class MapperUtils {

	/*
	 * @Autowired private ObjectMapper mapper;
	 */

	private MapperUtils() {
		super();
	}

	private static final String UTC_DATETIME_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
	private static final String SOURCE_NULL_MESSAGE = "source should not be null";
	private static final String DESTINATION_NULL_MESSAGE = "destination should not be null";

	/**
	 * This flag is used to restrict copy null values.
	 */
	private static Boolean mapNullValues = Boolean.TRUE;

	/**
	 * Parse a date string of pattern UTC_DATETIME_PATTERN into
	 * {@link LocalDateTime}
	 * 
	 * @param dateTime of type {@link String} of pattern UTC_DATETIME_PATTERN
	 * @return a {@link LocalDateTime} of given pattern
	 */
	public static LocalDateTime parseToLocalDateTime(String dateTime) {
		return LocalDateTime.parse(dateTime, DateTimeFormatter.ofPattern(UTC_DATETIME_PATTERN));
	}

	/*
	 * #############Public method used for mapping################################
	 */

	/**
	 * This method map the values from <code>source</code> to
	 * <code>destination</code> if name and type of the fields inside the given
	 * parameters are same.If any of the parameters are <code>null</code> this
	 * method return <code>null</code>.This method internally check whether the
	 * source or destinationClass is DTO or an Entity type and map accordingly. If
	 * any {@link Collection} type or Entity type field is their then only matched
	 * name fields value will be set but not the embedded IDs and super class
	 * values.
	 * 
	 * @param <S>           is a type parameter
	 * @param <D>           is a type parameter
	 * @param source        which value is going to be mapped
	 * @param destination   where values is going to be mapped
	 * @param mapNullValues by default marked as true so, it will map null values
	 *                      but if marked as false then null values will be ignored
	 * @return the <code>destination</code> object
	 * @throws NullPointerException if either <code>source</code> or
	 *                              <code>destination</code> is null
	 */
	public static <S, D> D map(final S source, D destination, Boolean mapNullValues) {
		MapperUtils.mapNullValues = mapNullValues;
		return map(source, destination);
	}

	/**
	 * This method map the values from <code>source</code> to
	 * <code>destination</code> if name and type of the fields inside the given
	 * parameters are same.If any of the parameters are <code>null</code> this
	 * method return <code>null</code>.This method internally check whether the
	 * source or destinationClass is DTO or an Entity type and map accordingly. If
	 * any {@link Collection} type or Entity type field is their then only matched
	 * name fields value will be set but not the embedded IDs and super class
	 * values.
	 * 
	 * @param <S>         is a type parameter
	 * @param <D>         is a type parameter
	 * @param source      which value is going to be mapped
	 * @param destination where values is going to be mapped
	 * @return the <code>destination</code> object
	 * @throws NullPointerException if either <code>source</code> or
	 *                              <code>destination</code> is null
	 */
	public static <S, D> D map(final S source, D destination) {
		Objects.requireNonNull(source, SOURCE_NULL_MESSAGE);
		Objects.requireNonNull(destination, DESTINATION_NULL_MESSAGE);
		try {
			mapValues(source, destination);
		} catch (IllegalAccessException | InstantiationException e) {
			throw new DataAccessLayerException("KER-MSD-991", "Exception in mapping vlaues from source : "
					+ source.getClass().getName() + " to destination : " + destination.getClass().getName(), e);
		}
		return destination;
	}

	/**
	 * This method takes <code>source</code> and <code>destinationClass</code>, take
	 * all values from source and create an object of <code>destinationClass</code>
	 * and map all the values from source to destination if field name and type is
	 * same.This method internally check whether the source or destinationClass is
	 * DTO or an Entity type and map accordingly.If any {@link Collection} type or
	 * Entity type field is their then only matched name fields value will be set
	 * but not the embedded IDs and super class values.
	 * 
	 * @param <S>              is a type parameter
	 * @param <D>              is a type parameter
	 * @param source           which value is going to be mapped
	 * @param destinationClass where values is going to be mapped
	 * @return the object of <code>destinationClass</code>
	 * @throws DataAccessLayerException if exception occur during creating of
	 *                                  <code>destinationClass</code> object
	 * @throws NullPointerException     if either <code>source</code> or
	 *                                  <code>destinationClass</code> is null
	 */
	public static <S, D> D map(final S source, Class<D> destinationClass) {
		Objects.requireNonNull(source, SOURCE_NULL_MESSAGE);
		Objects.requireNonNull(destinationClass, "destination class should not be null");
		Object destination = null;
		try {
			destination = destinationClass.newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			throw new DataAccessLayerException("KER-MSD-991", "Exception in mapping vlaues from source : "
					+ source.getClass().getName() + " to destination : " + destinationClass.getClass().getName(), e);
		}
		return (D) map(source, destination);
	}

	/**
	 * This method takes <code>sourceList</code> and <code>destinationClass</code>,
	 * take all values from source and create an object of
	 * <code>destinationClass</code> and map all the values from source to
	 * destination if field name and type is same.
	 * 
	 * @param <S>              is a type parameter
	 * 
	 * @param <D>              is a type parameter
	 * @param sourceList       which value is going to be mapped
	 * @param destinationClass where values is going to be mapped
	 * @return list of destinationClass objects
	 * @throws DataAccessLayerException if exception occur during creating of
	 *                                  <code>destinationClass</code> object
	 * @throws NullPointerException     if either <code>sourceList</code> or
	 *                                  <code>destinationClass</code> is null
	 */
	public static <S, D> List<D> mapAll(final Collection<S> sourceList, Class<D> destinationClass) {
		Objects.requireNonNull(sourceList, "sourceList should not be null");
		Objects.requireNonNull(destinationClass, "destinationClass should not be null");
		return sourceList.stream().map(entity -> map(entity, destinationClass)).collect(Collectors.toList());
	}

	/**
	 * This method map values of <code>source</code> object to
	 * <code>destination</code> object. It will map field values having same name
	 * and same type for the fields. It will not map any field which is static or
	 * final.It will simply ignore those values.
	 * 
	 * @param <S>         is a type parameter
	 * 
	 * @param <D>         is a type parameter
	 * @param source      is any object which should not be null and have data which
	 *                    is going to be copied
	 * @param destination is an object in which source field values is going to be
	 *                    matched
	 * 
	 * @throws DataAccessLayerException if error raised during mapping values
	 * @throws NullPointerException     if either <code>source</code> or
	 *                                  <code>destination</code> is null
	 */
	public static <S, D> void mapFieldValues(S source, D destination) {

		Objects.requireNonNull(source, SOURCE_NULL_MESSAGE);
		Objects.requireNonNull(destination, DESTINATION_NULL_MESSAGE);
		Field[] sourceFields = source.getClass().getDeclaredFields();
		Field[] destinationFields = destination.getClass().getDeclaredFields();

		mapFieldValues(source, destination, sourceFields, destinationFields);

	}

	/**
	 * Map values from {@link BaseEntity} class source object to destination or vice
	 * versa and this method will be used to map {@link BaseEntity} values from
	 * entity to entity. Like when both <code>source</code> and
	 * <code>destination</code> are object which extends {@link BaseEntity}.
	 * 
	 * @param <S>         is a type parameter
	 * @param <D>         is a type parameter
	 * @param source      which value is going to be mapped
	 * @param destination where values is going to be mapped
	 */
	public static <S, D> void setBaseFieldValue(S source, D destination) {
		Objects.requireNonNull(source, SOURCE_NULL_MESSAGE);
		Objects.requireNonNull(destination, DESTINATION_NULL_MESSAGE);
		String sourceSupername = source.getClass().getSuperclass().getName();// super class of source object
		String destinationSupername = destination.getClass().getSuperclass().getName();// super class of destination
		// object
		String baseEntityClassName = BaseEntity.class.getName();// base entity fully qualified name
		String objectClassName = Object.class.getName();// object class fully qualified name

		String baseDtoClassName = BaseDto.class.getName();// base entity fully qualified name

		if (sourceSupername.equals(baseEntityClassName) && destinationSupername.equals(baseDtoClassName)) {
			Field[] sourceFields = source.getClass().getSuperclass().getDeclaredFields();
			Field[] destinationFields = destination.getClass().getSuperclass().getDeclaredFields();
			mapFieldValues(source, destination, sourceFields, destinationFields);
			sourceFields = source.getClass().getDeclaredFields();
			mapFieldValues(source, destination, sourceFields, destinationFields);
			return;
		}
		if (sourceSupername.equals(baseDtoClassName) && destinationSupername.equals(baseEntityClassName)) {
			Field[] sourceFields = source.getClass().getSuperclass().getDeclaredFields();
			Field[] destinationFields = destination.getClass().getSuperclass().getDeclaredFields();
			mapFieldValues(source, destination, sourceFields, destinationFields);
			destinationFields = destination.getClass().getDeclaredFields();
			mapFieldValues(source, destination, sourceFields, destinationFields);
			return;
		}

		// if source is an entity
		if (sourceSupername.equals(baseEntityClassName) && !destinationSupername.equals(baseEntityClassName)) {
			Field[] sourceFields = source.getClass().getSuperclass().getDeclaredFields();
			Field[] destinationFields = destination.getClass().getDeclaredFields();
			mapFieldValues(source, destination, sourceFields, destinationFields);
		} else if (destinationSupername.equals(baseEntityClassName) && !sourceSupername.equals(baseEntityClassName)) {
			// if destination is an entity
			Field[] sourceFields = source.getClass().getDeclaredFields();
			Field[] destinationFields = destination.getClass().getSuperclass().getDeclaredFields();
			mapFieldValues(source, destination, sourceFields, destinationFields);
		} else {
			if (!sourceSupername.equals(objectClassName) && !destinationSupername.equals(objectClassName)) {
				Field[] sourceFields = source.getClass().getSuperclass().getDeclaredFields();
				Field[] destinationFields = destination.getClass().getSuperclass().getDeclaredFields();
				mapFieldValues(source, destination, sourceFields, destinationFields);
			}
		}

	}

	/*
	 * #############Private method used for mapping################################
	 */

	/**
	 * Map values from source object to destination object.
	 * 
	 * @param <S>         is a type parameter
	 * @param <D>         is a type parameter
	 * @param source      which value is going to be mapped
	 * @param destination where values is going to be mapped
	 * @throws InstantiationException if not able to create instance of field having
	 *                                annotation {@link EmbeddedId}
	 * @throws IllegalAccessException if provided fields are not accessible
	 */
	private static <S, D> void mapValues(S source, D destination)
			throws IllegalAccessException, InstantiationException {

		mapFieldValues(source, destination);// this method simply map values if field name and type are same

		if (source.getClass().isAnnotationPresent(Entity.class)) {
			mapEntityToDto(source, destination);
		} else {
			mapDtoToEntity(source, destination);
		}
	}

	/**
	 * This method map source DTO to a class object which extends {@link BaseEntity}
	 * 
	 * @param <S>         is a type parameter
	 * @param <D>         is a type parameter
	 * @param source      which value is going to be mapped
	 * @param destination where values is going to be mapped
	 * @throws InstantiationException if not able to create instance of field having
	 *                                annotation {@link EmbeddedId}
	 * @throws IllegalAccessException if provided fields are not accessible
	 */
	private static <S, D> void mapDtoToEntity(S source, D destination)
			throws InstantiationException, IllegalAccessException {
		Field[] fields = destination.getClass().getDeclaredFields();
		setBaseFieldValue(source, destination);// map super class values
		for (Field field : fields) {
			/**
			 * Map DTO matching field values to super class field values
			 */
			if (field.isAnnotationPresent(EmbeddedId.class)) {
				Object id = field.getType().newInstance();
				mapFieldValues(source, id);
				field.setAccessible(true);
				field.set(destination, id);
				field.setAccessible(false);
				break;
			}
		}
	}

	/**
	 * Map source which extends {@link BaseEntity} to a DTO object.
	 * 
	 * @param <S>         is a type parameter
	 * @param <D>         is a type parameter
	 * @param source      which value is going to be mapped
	 * @param destination where values is going to be mapped
	 * @throws IllegalAccessException if provided fields are not accessible
	 */
	private static <S, D> void mapEntityToDto(S source, D destination) throws IllegalAccessException {
		Field[] sourceFields = source.getClass().getDeclaredFields();
		/*
		 * Here source is a Entity so we need to take values from Entity object and set
		 * the matching fields in the destination object mostly an DTO.
		 */
		boolean isIdMapped = false;// a flag to check if there any composite key is present and is mapped
		boolean isSuperMapped = false;// a flag to check is class extends the BaseEntity and is mapped
		for (Field sfield : sourceFields) {
			sfield.setAccessible(true);// mark accessible true because fields my be private, for safety
			if (!isIdMapped && sfield.isAnnotationPresent(EmbeddedId.class)) {
				/**
				 * Map the composite key values from source to destination if field name is same
				 */
				/**
				 * Take the field and get the composite key object and map all values to
				 * destination object
				 */
				mapFieldValues(sfield.get(source), destination);
				sfield.setAccessible(false);
				isIdMapped = true;// set flag so no need to check and map again
			} else if (!isSuperMapped) {
				setBaseFieldValue(source, destination);// this method check whether source is entity or destination
														// and maps values accordingly
				isSuperMapped = true;
			}
		}
	}

	/**
	 * Map values from source field to destination.
	 * 
	 * @param <S>         is a type parameter
	 * @param <D>         is a type parameter
	 * @param source      which value is going to be mapped
	 * @param destination where values is going to be mapped
	 * @param sf          source fields
	 * @param dtf         destination fields
	 */
	private static <D, S> void mapFieldValues(S source, D destination, Field[] sourceFields,
			Field[] destinationFields) {
		try {
			for (Field sfield : sourceFields) {
				// Do not set values either static or final
				if (Modifier.isStatic(sfield.getModifiers()) || Modifier.isFinal(sfield.getModifiers())) {
					continue;
				}

				// make field accessible possibly private
				sfield.setAccessible(true);

				for (Field dfield : destinationFields) {

					Class<?> sourceType = sfield.getType();
					Class<?> destinationType = dfield.getType();

					// map only those field whose name and type is same
					if (sfield.getName().equals(dfield.getName()) && sourceType.equals(destinationType)) {

						// for normal field values
						dfield.setAccessible(true);
						setFieldValue(source, destination, sfield, dfield);
						break;
					}
				}
			}
		} catch (IllegalAccessException e) {

			throw new DataAccessLayerException("KER-MSD-993", "Exception raised while mapping values form "
					+ source.getClass().getName() + " to " + destination.getClass().getName(), e);
		}
	}

	/**
	 * Take value from source field and insert value into destination field.
	 * 
	 * @param <S>         is a type parameter
	 * @param <D>         is a type parameter
	 * @param source      which value is going to be mapped
	 * @param destination where values is going to be mapped
	 * @param sf          source fields
	 * @param dtf         destination fields
	 * @throws IllegalAccessException if provided fields are not accessible
	 */
	private static <S, D> void setFieldValue(S source, D destination, Field sf, Field dtf)
			throws IllegalAccessException {
		// check whether user wants to map null values into destination object or not
		if (!mapNullValues && EmptyCheckUtils.isNullEmpty(sf.get(source))) {
			return;
		}
		dtf.set(destination, sf.get(source));
		dtf.setAccessible(false);
		sf.setAccessible(false);
	}
	// ----------------------------------------------------------------------------------------------------------------------------

	public static List<HolidayDto> mapHolidays(List<Holiday> holidays) {
		Objects.requireNonNull(holidays);
		List<HolidayDto> holidayDtos = new ArrayList<>();
		holidays.forEach(holiday -> {
			LocalDate date = holiday.getHolidayDate();
			HolidayDto dto = new HolidayDto();
			dto.setId(holiday.getHolidayId());
			dto.setHolidayDate(date);
			dto.setHolidayName(holiday.getHolidayName());
			dto.setLangCode(holiday.getLangCode());
			dto.setHolidayYear(String.valueOf(date.getYear()));
			dto.setHolidayMonth(String.valueOf(date.getMonth().getValue()));
			dto.setHolidayDay(String.valueOf(date.getDayOfWeek().getValue()));
			dto.setIsActive(holiday.getIsActive());
			dto.setLocationCode(holiday.getLocationCode());
			dto.setHolidayDesc(holiday.getHolidayDesc());
			holidayDtos.add(dto);
		});
		return holidayDtos;
	}

	public static List<ExceptionalHolidayDto> mapExceptionalHolidays(List<ExceptionalHoliday> exeptionalHolidayList) {
		Objects.requireNonNull(exeptionalHolidayList);
		List<ExceptionalHolidayDto> holidayDtos = new ArrayList<>();
		exeptionalHolidayList.forEach(holiday -> {
			LocalDate date = holiday.getHolidayDate();
			ExceptionalHolidayDto dto = new ExceptionalHolidayDto();
			dto.setHolidayDate(date);
			dto.setHolidayName(holiday.getHolidayName());
			dto.setLangCode(holiday.getLangCode());
			dto.setHolidayYear(String.valueOf(date.getYear()));
			dto.setHolidayMonth(String.valueOf(date.getMonth().getValue()));
			dto.setHolidayDay(String.valueOf(date.getDayOfWeek().getValue()));
			dto.setIsActive(holiday.getIsActive());
			dto.setHolidayReason(holiday.getHolidayReason());
			holidayDtos.add(dto);
		});
		return holidayDtos;
	}

	public static List<ReasonCategoryDto> reasonConverter(List<ReasonCategory> reasonCategories) {
		Objects.requireNonNull(reasonCategories, "list cannot be null");
		List<ReasonCategoryDto> reasonCategoryDtos = null;
		reasonCategoryDtos = reasonCategories.parallelStream()
				.map(reasonCategory -> new ReasonCategoryDto(reasonCategory.getCode(), reasonCategory.getName(),
						reasonCategory.getDescription(), reasonCategory.getLangCode(), reasonCategory.getIsActive(),
						mapAll(reasonCategory.getReasonList(), ReasonListDto.class)))
				.collect(Collectors.toList());

		return reasonCategoryDtos;

	}

	public static List<LocationHierarchyDto> objectToDtoConverter(List<Object[]> locationList) {

		List<LocationHierarchyDto> locationHierarchyDtos = new ArrayList<>();
		for (Object[] object : locationList) {
			LocationHierarchyDto locationHierarchyDto = new LocationHierarchyDto();
			locationHierarchyDto.setLocationHierarchylevel((Short) object[0]);
			locationHierarchyDto.setLocationHierarchyName((String) object[1]);
			locationHierarchyDto.setIsActive((Boolean) object[2]);
			locationHierarchyDtos.add(locationHierarchyDto);
		}
		return locationHierarchyDtos;
	}

	public static List<DeviceLangCodeDtypeDto> mapDeviceDto(List<Object[]> objects) {
		List<DeviceLangCodeDtypeDto> deviceLangCodeDtypeDtoList = new ArrayList<>();
		objects.forEach(arr -> {
			DeviceLangCodeDtypeDto deviceLangCodeDtypeDto = new DeviceLangCodeDtypeDto();
			deviceLangCodeDtypeDto.setId((String) arr[0]);
			deviceLangCodeDtypeDto.setName((String) arr[1]);
			deviceLangCodeDtypeDto.setMacAddress((String) arr[2]);
			deviceLangCodeDtypeDto.setSerialNum((String) arr[3]);
			deviceLangCodeDtypeDto.setIpAddress((String) arr[4]);
			deviceLangCodeDtypeDto.setDeviceSpecId((String) arr[5]);
			deviceLangCodeDtypeDto.setLangCode((String) arr[6]);
			deviceLangCodeDtypeDto.setIsActive((boolean) arr[7]);
			if (arr[8] != null) {
				deviceLangCodeDtypeDto.setValidityEndDateTime(((Timestamp) arr[8]).toLocalDateTime());
			}
			deviceLangCodeDtypeDto.setDeviceTypeCode((String) arr[10]);
			deviceLangCodeDtypeDtoList.add(deviceLangCodeDtypeDto);

		});
		return deviceLangCodeDtypeDtoList;
	}

	public static RegisteredDevice mapRegisteredDeviceDto(RegisteredDevicePostReqDto dto, String digitalIdJson) {

		RegisteredDevice entity = new RegisteredDevice();
		entity.setDeviceTypeCode(dto.getDigitalIdDto().getDeviceTypeCode());
		entity.setDeviceSTypeCode(dto.getDigitalIdDto().getDeviceSTypeCode());
		entity.setStatusCode(dto.getStatusCode());
		entity.setDeviceId(dto.getDeviceId());
		entity.setDeviceSubId(dto.getDeviceSubId());

		entity.setDigitalId(digitalIdJson);
		entity.setSerialNo(dto.getDigitalIdDto().getSerialNo());
		entity.setDpId(dto.getDigitalIdDto().getDpId());
		entity.setDp(dto.getDigitalIdDto().getDp());
		entity.setMake(dto.getDigitalIdDto().getMake());
		entity.setModel(dto.getDigitalIdDto().getModel());

		entity.setPurpose(dto.getPurpose());
		entity.setFirmware(dto.getFirmware());
		entity.setExpiryDate(dto.getExpiryDate());
		entity.setCertificationLevel(dto.getCertificationLevel());
		entity.setFoundationalTPId(dto.getFoundationalTPId());
		/*
		 * entity.setFoundationalTrustSignature(dto.getFoundationalTrustSignature());
		 * entity.setFoundationalTrustCertificate(dto.getFoundationalTrustCertificate())
		 * ; entity.setDeviceProviderSignature(dto.getDeviceProviderSignature());
		 */

		return entity;

	}

	public static RegisteredDevice mapRegisteredDeviceDto(RegisteredDevicePostDto registeredDevicePostDto,
			String digitalIdJson, DeviceData deviceData, DigitalId digitalId) {

		RegisteredDevice entity = new RegisteredDevice();
		entity.setDeviceTypeCode(digitalId.getType());
		entity.setDeviceSTypeCode(digitalId.getSubType());
		entity.setStatusCode("REGISTERED");
		entity.setDeviceId(deviceData.getDeviceId());
		entity.setDeviceSubId(deviceData.getDeviceInfo().getDeviceSubId());

		entity.setDigitalId(digitalIdJson);
		entity.setSerialNo(digitalId.getSerialNo());
		entity.setDpId(digitalId.getDeviceProviderId());
		entity.setDp(digitalId.getDeviceProvider());
		entity.setMake(digitalId.getMake());
		entity.setModel(digitalId.getModel());

		entity.setPurpose(deviceData.getPurpose());
		entity.setFirmware(deviceData.getDeviceInfo().getFirmware());
		entity.setExpiryDate(deviceData.getDeviceInfo().getDeviceExpiry());
		entity.setCertificationLevel(deviceData.getDeviceInfo().getCertification());
		entity.setFoundationalTPId(deviceData.getFoundationalTrustProviderId());
		/*
		 * entity.setFoundationalTrustSignature(dto.getFoundationalTrustSignature());
		 * entity.setFoundationalTrustCertificate(dto.getFoundationalTrustCertificate())
		 * ; entity.setDeviceProviderSignature(dto.getDeviceProviderSignature());
		 */

		return entity;
	}

	public static RegisterDeviceResponse mapRegisteredDeviceResponse(RegisteredDevice entity, DeviceData deviceData) {
		RegisterDeviceResponse registerDeviceResponse = new RegisterDeviceResponse();
		registerDeviceResponse.setDeviceCode(entity.getCode());
		registerDeviceResponse.setStatus(entity.getStatusCode());
		registerDeviceResponse.setTimeStamp(deviceData.getDeviceInfo().getTimeStamp());
		return registerDeviceResponse;
	}

}
