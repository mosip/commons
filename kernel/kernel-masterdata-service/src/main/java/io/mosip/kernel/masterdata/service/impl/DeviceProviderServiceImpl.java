package io.mosip.kernel.masterdata.service.impl;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.mosip.kernel.core.dataaccess.exception.DataAccessLayerException;
import io.mosip.kernel.core.deviceprovidermanager.spi.DeviceProviderService;
import io.mosip.kernel.core.exception.ServiceError;
import io.mosip.kernel.masterdata.constant.DeviceProviderManagementErrorCode;
import io.mosip.kernel.masterdata.constant.MasterDataConstant;
import io.mosip.kernel.masterdata.dto.DeviceProviderDto;
import io.mosip.kernel.masterdata.dto.DeviceProviderPutDto;
import io.mosip.kernel.masterdata.dto.DigitalIdDto;
import io.mosip.kernel.masterdata.dto.ValidateDeviceDto;
import io.mosip.kernel.masterdata.dto.ValidateDeviceHistoryDto;
import io.mosip.kernel.masterdata.dto.getresponse.ResponseDto;
import io.mosip.kernel.masterdata.dto.getresponse.extn.DeviceProviderExtnDto;
import io.mosip.kernel.masterdata.entity.DeviceProvider;
import io.mosip.kernel.masterdata.entity.DeviceProviderHistory;
import io.mosip.kernel.masterdata.entity.MOSIPDeviceService;
import io.mosip.kernel.masterdata.entity.MOSIPDeviceServiceHistory;
import io.mosip.kernel.masterdata.entity.RegisteredDevice;
import io.mosip.kernel.masterdata.entity.RegisteredDeviceHistory;
import io.mosip.kernel.masterdata.exception.DataNotFoundException;
import io.mosip.kernel.masterdata.exception.MasterDataServiceException;
import io.mosip.kernel.masterdata.exception.RequestException;
import io.mosip.kernel.masterdata.exception.ValidationException;
import io.mosip.kernel.masterdata.repository.DeviceProviderHistoryRepository;
import io.mosip.kernel.masterdata.repository.DeviceProviderRepository;
import io.mosip.kernel.masterdata.repository.MOSIPDeviceServiceHistoryRepository;
import io.mosip.kernel.masterdata.repository.MOSIPDeviceServiceRepository;
import io.mosip.kernel.masterdata.repository.RegisteredDeviceHistoryRepository;
import io.mosip.kernel.masterdata.repository.RegisteredDeviceRepository;
import io.mosip.kernel.masterdata.utils.AuditUtil;
import io.mosip.kernel.masterdata.utils.ExceptionUtils;
import io.mosip.kernel.masterdata.utils.MapperUtils;
import io.mosip.kernel.masterdata.utils.MetaDataUtils;

/**
 * Device provider service class
 * 
 * @author Srinivasan
 * @author Megha Tanga
 *
 */
@Service
public class DeviceProviderServiceImpl implements
		DeviceProviderService<ResponseDto, ValidateDeviceDto, ValidateDeviceHistoryDto, DeviceProviderDto, DeviceProviderExtnDto, DeviceProviderPutDto> {

	private static final String UTC_DATETIME_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";

	private static final String REGISTERED = "Registered";

	@Autowired
	AuditUtil auditUtil;

	@Autowired
	private RegisteredDeviceRepository registeredDeviceRepository;

	@Autowired
	private DeviceProviderRepository deviceProviderRepository;

	@Autowired
	private MOSIPDeviceServiceRepository deviceServiceRepository;

	@Autowired
	private DeviceProviderHistoryRepository deviceProviderHistoryRepository;

	@Autowired
	private RegisteredDeviceHistoryRepository registeredDeviceHistoryRepository;

	@Autowired
	private MOSIPDeviceServiceHistoryRepository deviceServiceHistoryRepository;

	@Override
	public ResponseDto validateDeviceProviders(ValidateDeviceDto validateDeviceDto) {
		ResponseDto responseDto = new ResponseDto();
		RegisteredDevice registeredDevice = findRegisteredDevice(validateDeviceDto.getDeviceCode());
		isDeviceProviderPresent(validateDeviceDto.getDigitalId().getDpId());
		isValidServiceSoftwareVersion(validateDeviceDto.getDeviceServiceVersion());
		checkMappingBetweenSwVersionDeviceTypeAndDeviceSubType(validateDeviceDto.getDeviceServiceVersion(),
				registeredDevice);
		validateDeviceCodeAndDigitalId(registeredDevice, validateDeviceDto.getDigitalId());
		responseDto.setStatus(MasterDataConstant.VALID);
		responseDto.setMessage("Device  details validated successfully");

		return responseDto;
	}

	private RegisteredDevice findRegisteredDevice(String deviceCode) {
		RegisteredDevice registeredDevice = null;
		try {
			registeredDevice = registeredDeviceRepository.findByCodeAndIsActiveIsTrue(deviceCode);
		} catch (DataAccessException | DataAccessLayerException e) {
			auditUtil.auditRequest(
					MasterDataConstant.DEVICE_VALIDATION_FAILURE + ValidateDeviceDto.class.getSimpleName(),
					MasterDataConstant.AUDIT_SYSTEM,
					String.format(MasterDataConstant.DEVICE_VALIDATION_API_CALLED,
							DeviceProviderManagementErrorCode.DATABASE_EXCEPTION.getErrorCode(),
							DeviceProviderManagementErrorCode.DATABASE_EXCEPTION.getErrorMessage()),
					"ADM-605");
			throw new MasterDataServiceException(DeviceProviderManagementErrorCode.DATABASE_EXCEPTION.getErrorCode(),
					String.format(DeviceProviderManagementErrorCode.DATABASE_EXCEPTION.getErrorMessage(),
							MasterDataConstant.ERROR_OCCURED_REGISTERED_DEVICE));
		}

		if (registeredDevice == null) {
			auditUtil.auditRequest(
					MasterDataConstant.DEVICE_VALIDATION_FAILURE + ValidateDeviceDto.class.getSimpleName(),
					MasterDataConstant.AUDIT_SYSTEM,
					String.format(MasterDataConstant.DEVICE_VALIDATION_API_CALLED,
							DeviceProviderManagementErrorCode.DEVICE_DOES_NOT_EXIST.getErrorCode(),
							DeviceProviderManagementErrorCode.DEVICE_DOES_NOT_EXIST.getErrorMessage()),
					"ADM-606");

			throw new DataNotFoundException(DeviceProviderManagementErrorCode.DEVICE_DOES_NOT_EXIST.getErrorCode(),
					DeviceProviderManagementErrorCode.DEVICE_DOES_NOT_EXIST.getErrorMessage());
		}
		if (!registeredDevice.getStatusCode().equalsIgnoreCase(REGISTERED)) {
			auditUtil.auditRequest(
					MasterDataConstant.DEVICE_VALIDATION_FAILURE + ValidateDeviceDto.class.getSimpleName(),
					MasterDataConstant.AUDIT_SYSTEM,
					String.format(MasterDataConstant.DEVICE_VALIDATION_API_CALLED,
							DeviceProviderManagementErrorCode.DEVICE_REVOKED_OR_RETIRED.getErrorCode(),
							DeviceProviderManagementErrorCode.DEVICE_REVOKED_OR_RETIRED.getErrorMessage()),
					"ADM-607");
			throw new RequestException(DeviceProviderManagementErrorCode.DEVICE_REVOKED_OR_RETIRED.getErrorCode(),
					DeviceProviderManagementErrorCode.DEVICE_REVOKED_OR_RETIRED.getErrorMessage());
		}

		return registeredDevice;

	}

	private boolean isDeviceProviderPresent(String deviceProviderId) {
		DeviceProvider deviceProvider = null;

		try {
			deviceProvider = deviceProviderRepository.findByIdAndIsActiveIsTrue(deviceProviderId);
		} catch (DataAccessException | DataAccessLayerException e) {
			auditUtil.auditRequest(
					MasterDataConstant.DEVICE_VALIDATION_FAILURE + ValidateDeviceDto.class.getSimpleName(),
					MasterDataConstant.AUDIT_SYSTEM,
					String.format(MasterDataConstant.FAILURE_DESC,
							DeviceProviderManagementErrorCode.DATABASE_EXCEPTION.getErrorCode(),
							DeviceProviderManagementErrorCode.DATABASE_EXCEPTION.getErrorMessage()),
					"ADM-606");
			throw new MasterDataServiceException(DeviceProviderManagementErrorCode.DATABASE_EXCEPTION.getErrorCode(),
					String.format(DeviceProviderManagementErrorCode.DATABASE_EXCEPTION.getErrorMessage(),
							MasterDataConstant.ERROR_OCCURED_DEVICE_PROVIDER));
		}
		if (deviceProvider == null) {
			auditUtil.auditRequest(
					MasterDataConstant.DEVICE_VALIDATION_FAILURE + ValidateDeviceDto.class.getSimpleName(),
					MasterDataConstant.AUDIT_SYSTEM,
					String.format(MasterDataConstant.FAILURE_DESC,
							DeviceProviderManagementErrorCode.DEVICE_PROVIDER_INACTIVE.getErrorCode(),
							DeviceProviderManagementErrorCode.DEVICE_PROVIDER_INACTIVE.getErrorMessage()),
					"ADM-607");
			throw new DataNotFoundException(DeviceProviderManagementErrorCode.DEVICE_PROVIDER_INACTIVE.getErrorCode(),
					DeviceProviderManagementErrorCode.DEVICE_PROVIDER_INACTIVE.getErrorMessage());
		}

		return true;

	}

	private boolean isValidServiceSoftwareVersion(String serviceSoftwareVersion) {
		List<MOSIPDeviceService> deviceServices = null;
		try {
			deviceServices = deviceServiceRepository.findBySwVersionAndIsActiveIsTrue(serviceSoftwareVersion);
		} catch (DataAccessException | DataAccessLayerException e) {
			auditUtil.auditRequest(
					MasterDataConstant.DEVICE_VALIDATION_FAILURE + ValidateDeviceDto.class.getSimpleName(),
					MasterDataConstant.AUDIT_SYSTEM,
					String.format(MasterDataConstant.FAILURE_DESC,
							DeviceProviderManagementErrorCode.DATABASE_EXCEPTION.getErrorCode(),
							DeviceProviderManagementErrorCode.DATABASE_EXCEPTION.getErrorMessage()),
					"ADM-608");
			throw new MasterDataServiceException(DeviceProviderManagementErrorCode.DATABASE_EXCEPTION.getErrorCode(),
					String.format(DeviceProviderManagementErrorCode.DATABASE_EXCEPTION.getErrorMessage(),
							MasterDataConstant.ERROR_OCCURED_MOSIP_DEVICE_SERVICE));
		}
		if (deviceServices.isEmpty()) {
			auditUtil.auditRequest(
					MasterDataConstant.DEVICE_VALIDATION_FAILURE + ValidateDeviceDto.class.getSimpleName(),
					MasterDataConstant.AUDIT_SYSTEM,
					String.format(MasterDataConstant.FAILURE_DESC,
							DeviceProviderManagementErrorCode.MDS_INACTIVE_STATE.getErrorCode(),
							DeviceProviderManagementErrorCode.MDS_INACTIVE_STATE.getErrorMessage()),
					"ADM-609");
			throw new DataNotFoundException(DeviceProviderManagementErrorCode.MDS_INACTIVE_STATE.getErrorCode(),
					DeviceProviderManagementErrorCode.MDS_INACTIVE_STATE.getErrorMessage());
		}

		return true;
	}

	/**
	 * Check mapping between sw version device type and device sub type.
	 *
	 * @param deviceCode the device code
	 * @return true, if successful
	 */
	private boolean checkMappingBetweenSwVersionDeviceTypeAndDeviceSubType(String swVersion,
			RegisteredDevice registeredDevice) {

		MOSIPDeviceService mosipDeviceService = null;
		try {
			mosipDeviceService = deviceServiceRepository.findByDeviceDetail(swVersion,
					registeredDevice.getDeviceTypeCode(), registeredDevice.getDeviceSTypeCode(),
					registeredDevice.getMake(), registeredDevice.getModel(), registeredDevice.getDpId());
		} catch (DataAccessException | DataAccessLayerException e) {
			auditUtil.auditRequest(
					MasterDataConstant.DEVICE_VALIDATION_FAILURE + ValidateDeviceDto.class.getSimpleName(),
					MasterDataConstant.AUDIT_SYSTEM,
					String.format(MasterDataConstant.FAILURE_DESC,
							DeviceProviderManagementErrorCode.DATABASE_EXCEPTION.getErrorCode(),
							DeviceProviderManagementErrorCode.DATABASE_EXCEPTION.getErrorMessage()),
					"ADM-611");
			throw new MasterDataServiceException(DeviceProviderManagementErrorCode.DATABASE_EXCEPTION.getErrorCode(),
					String.format(DeviceProviderManagementErrorCode.DATABASE_EXCEPTION.getErrorMessage(),
							MasterDataConstant.ERROR_OCCURED_MOSIP_DEVICE_SERVICE));
		}

		if (mosipDeviceService == null) {
			auditUtil.auditRequest(
					MasterDataConstant.DEVICE_VALIDATION_FAILURE + ValidateDeviceDto.class.getSimpleName(),
					MasterDataConstant.AUDIT_SYSTEM,
					String.format(MasterDataConstant.FAILURE_DESC,
							DeviceProviderManagementErrorCode.SOFTWARE_VERSION_IS_NOT_A_MATCH.getErrorCode(),
							DeviceProviderManagementErrorCode.SOFTWARE_VERSION_IS_NOT_A_MATCH.getErrorMessage()),
					"ADM-612");
			throw new DataNotFoundException(
					DeviceProviderManagementErrorCode.SOFTWARE_VERSION_IS_NOT_A_MATCH.getErrorCode(),
					DeviceProviderManagementErrorCode.SOFTWARE_VERSION_IS_NOT_A_MATCH.getErrorMessage());
		}

		return true;

	}

	private void validateDeviceCodeAndDigitalId(RegisteredDevice registeredDevice, DigitalIdDto digitalIdDto) {
		List<ServiceError> serviceErrors = new ArrayList<>();
		if (!registeredDevice.getMake().equals(digitalIdDto.getMake())) {
			ServiceError serviceError = new ServiceError();
			serviceError
					.setErrorCode(DeviceProviderManagementErrorCode.PROVIDER_AND_DEVICE_CODE_NOT_MAPPED.getErrorCode());
			serviceError.setMessage(String.format(
					DeviceProviderManagementErrorCode.PROVIDER_AND_DEVICE_CODE_NOT_MAPPED.getErrorMessage(),
					MasterDataConstant.MAKE));
			serviceErrors.add(serviceError);
		}
		if (!registeredDevice.getModel().equals(digitalIdDto.getModel())) {
			ServiceError serviceError = new ServiceError();
			serviceError
					.setErrorCode(DeviceProviderManagementErrorCode.PROVIDER_AND_DEVICE_CODE_NOT_MAPPED.getErrorCode());
			serviceError.setMessage(String.format(
					DeviceProviderManagementErrorCode.PROVIDER_AND_DEVICE_CODE_NOT_MAPPED.getErrorMessage(),
					MasterDataConstant.MODEL));
			serviceErrors.add(serviceError);
		}
		if (!registeredDevice.getDpId().equals(digitalIdDto.getDpId())) {
			ServiceError serviceError = new ServiceError();
			serviceError
					.setErrorCode(DeviceProviderManagementErrorCode.PROVIDER_AND_DEVICE_CODE_NOT_MAPPED.getErrorCode());
			serviceError.setMessage(String.format(
					DeviceProviderManagementErrorCode.PROVIDER_AND_DEVICE_CODE_NOT_MAPPED.getErrorMessage(),
					MasterDataConstant.DP_ID));
			serviceErrors.add(serviceError);
		}
		if (!registeredDevice.getDp().equals(digitalIdDto.getDp())) {
			ServiceError serviceError = new ServiceError();
			serviceError
					.setErrorCode(DeviceProviderManagementErrorCode.PROVIDER_AND_DEVICE_CODE_NOT_MAPPED.getErrorCode());
			serviceError.setMessage(String.format(
					DeviceProviderManagementErrorCode.PROVIDER_AND_DEVICE_CODE_NOT_MAPPED.getErrorMessage(),
					MasterDataConstant.DP));
			serviceErrors.add(serviceError);
		}
		if (!registeredDevice.getSerialNo().equals(digitalIdDto.getSerialNo())) {
			ServiceError serviceError = new ServiceError();
			serviceError
					.setErrorCode(DeviceProviderManagementErrorCode.PROVIDER_AND_DEVICE_CODE_NOT_MAPPED.getErrorCode());
			serviceError.setMessage(String.format(
					DeviceProviderManagementErrorCode.PROVIDER_AND_DEVICE_CODE_NOT_MAPPED.getErrorMessage(),
					MasterDataConstant.SERIAL_NO));
			serviceErrors.add(serviceError);
		}
		if (!registeredDevice.getDeviceTypeCode().equals(digitalIdDto.getType())) {
			ServiceError serviceError = new ServiceError();
			serviceError
					.setErrorCode(DeviceProviderManagementErrorCode.PROVIDER_AND_DEVICE_CODE_NOT_MAPPED.getErrorCode());
			serviceError.setMessage(DeviceProviderManagementErrorCode.PROVIDER_AND_TYPE_MAPPED.getErrorMessage());
			serviceErrors.add(serviceError);
		}
		if (!registeredDevice.getDeviceSTypeCode().equals(digitalIdDto.getSubType())) {
			ServiceError serviceError = new ServiceError();
			serviceError
					.setErrorCode(DeviceProviderManagementErrorCode.PROVIDER_AND_DEVICE_CODE_NOT_MAPPED.getErrorCode());
			serviceError.setMessage(DeviceProviderManagementErrorCode.PROVIDER_AND_SUBTYPE_MAPPED.getErrorMessage());
			serviceErrors.add(serviceError);
		}
		if (!serviceErrors.isEmpty()) {
			auditUtil.auditRequest(
					MasterDataConstant.DEVICE_VALIDATION_FAILURE + ValidateDeviceDto.class.getSimpleName(),
					MasterDataConstant.AUDIT_SYSTEM,
					String.format(MasterDataConstant.FAILURE_DESC, "KER-ADM-999", serviceErrors.toString()), "ADM-613");
			throw new ValidationException(serviceErrors);
		} else {
			serviceErrors = null;
		}

	}

	@Override
	public ResponseDto validateDeviceProviderHistory(ValidateDeviceHistoryDto validateDeviceDto) {
		ResponseDto responseDto = new ResponseDto();
		responseDto.setStatus(MasterDataConstant.INVALID);
		responseDto.setMessage("Device details history is invalid");
		LocalDateTime effTimes = parseToLocalDateTime(validateDeviceDto.getTimeStamp());
		RegisteredDeviceHistory registeredDeviceHistory = isRegisteredDeviceHistory(validateDeviceDto.getDeviceCode(),
				effTimes);
		isDeviceProviderHistoryPresent(validateDeviceDto.getDigitalId().getDpId(), effTimes);
		isValidServiceVersionFromHistory(validateDeviceDto.getDeviceServiceVersion(), effTimes);
		checkMappingBetweenSWVerDTypeAndDSubTypeHistory(validateDeviceDto.getDeviceServiceVersion(),
				registeredDeviceHistory, effTimes);
		validateDigitalIdWithRegisteredDeviceHistory(registeredDeviceHistory, validateDeviceDto.getDigitalId());
		responseDto.setStatus(MasterDataConstant.VALID);
		responseDto.setMessage("Device details history validated successfully");

		return responseDto;
	}

	private boolean checkMappingBetweenSWVerDTypeAndDSubTypeHistory(String swVersion,
			RegisteredDeviceHistory registeredDevice, LocalDateTime effTimes) {
		MOSIPDeviceServiceHistory mosipDeviceService = null;
		try {
			mosipDeviceService = deviceServiceHistoryRepository.findByDeviceDetailHistory(swVersion,
					registeredDevice.getDeviceTypeCode(), registeredDevice.getDeviceSTypeCode(),
					registeredDevice.getMake(), registeredDevice.getModel(), registeredDevice.getDpId(), effTimes);
		} catch (DataAccessException | DataAccessLayerException e) {
			auditUtil.auditRequest(
					MasterDataConstant.DEVICE_VALIDATION_FAILURE + ValidateDeviceDto.class.getSimpleName(),
					MasterDataConstant.AUDIT_SYSTEM,
					String.format(MasterDataConstant.FAILURE_DESC,
							DeviceProviderManagementErrorCode.DATABASE_EXCEPTION.getErrorCode(),
							DeviceProviderManagementErrorCode.DATABASE_EXCEPTION.getErrorMessage()),
					"ADM-611");
			throw new MasterDataServiceException(DeviceProviderManagementErrorCode.DATABASE_EXCEPTION.getErrorCode(),
					String.format(DeviceProviderManagementErrorCode.DATABASE_EXCEPTION.getErrorMessage(),
							MasterDataConstant.ERROR_OCCURED_MOSIP_DEVICE_SERVICE));
		}

		if (mosipDeviceService == null) {
			auditUtil.auditRequest(
					MasterDataConstant.DEVICE_VALIDATION_FAILURE + ValidateDeviceDto.class.getSimpleName(),
					MasterDataConstant.AUDIT_SYSTEM,
					String.format(MasterDataConstant.FAILURE_DESC,
							DeviceProviderManagementErrorCode.SOFTWARE_VERSION_IS_NOT_A_MATCH.getErrorCode(),
							DeviceProviderManagementErrorCode.SOFTWARE_VERSION_IS_NOT_A_MATCH.getErrorMessage()),
					"ADM-612");
			throw new DataNotFoundException(
					DeviceProviderManagementErrorCode.SOFTWARE_VERSION_IS_NOT_A_MATCH.getErrorCode(),
					DeviceProviderManagementErrorCode.SOFTWARE_VERSION_IS_NOT_A_MATCH.getErrorMessage());
		}

		return true;

	}

	private void validateDigitalIdWithRegisteredDeviceHistory(RegisteredDeviceHistory registeredDevice,
			DigitalIdDto digitalIdDto) {
		List<ServiceError> serviceErrors = new ArrayList<>();
		if (!registeredDevice.getMake().equals(digitalIdDto.getMake())) {
			ServiceError serviceError = new ServiceError();
			serviceError
					.setErrorCode(DeviceProviderManagementErrorCode.PROVIDER_AND_DEVICE_CODE_NOT_MAPPED.getErrorCode());
			serviceError.setMessage(String.format(
					DeviceProviderManagementErrorCode.PROVIDER_AND_DEVICE_CODE_NOT_MAPPED.getErrorMessage(),
					MasterDataConstant.MAKE));
			serviceErrors.add(serviceError);
		}
		if (!registeredDevice.getModel().equals(digitalIdDto.getModel())) {
			ServiceError serviceError = new ServiceError();
			serviceError
					.setErrorCode(DeviceProviderManagementErrorCode.PROVIDER_AND_DEVICE_CODE_NOT_MAPPED.getErrorCode());
			serviceError.setMessage(String.format(
					DeviceProviderManagementErrorCode.PROVIDER_AND_DEVICE_CODE_NOT_MAPPED.getErrorMessage(),
					MasterDataConstant.MODEL));
			serviceErrors.add(serviceError);
		}
		if (!registeredDevice.getDpId().equals(digitalIdDto.getDpId())) {
			ServiceError serviceError = new ServiceError();
			serviceError
					.setErrorCode(DeviceProviderManagementErrorCode.PROVIDER_AND_DEVICE_CODE_NOT_MAPPED.getErrorCode());
			serviceError.setMessage(String.format(
					DeviceProviderManagementErrorCode.PROVIDER_AND_DEVICE_CODE_NOT_MAPPED.getErrorMessage(),
					MasterDataConstant.DP_ID));
			serviceErrors.add(serviceError);
		}
		if (!registeredDevice.getDp().equals(digitalIdDto.getDp())) {
			ServiceError serviceError = new ServiceError();
			serviceError
					.setErrorCode(DeviceProviderManagementErrorCode.PROVIDER_AND_DEVICE_CODE_NOT_MAPPED.getErrorCode());
			serviceError.setMessage(String.format(
					DeviceProviderManagementErrorCode.PROVIDER_AND_DEVICE_CODE_NOT_MAPPED.getErrorMessage(),
					MasterDataConstant.DP));
			serviceErrors.add(serviceError);
		}
		if (!registeredDevice.getSerialNo().equals(digitalIdDto.getSerialNo())) {
			ServiceError serviceError = new ServiceError();
			serviceError
					.setErrorCode(DeviceProviderManagementErrorCode.PROVIDER_AND_DEVICE_CODE_NOT_MAPPED.getErrorCode());
			serviceError.setMessage(String.format(
					DeviceProviderManagementErrorCode.PROVIDER_AND_DEVICE_CODE_NOT_MAPPED.getErrorMessage(),
					MasterDataConstant.SERIAL_NO));
			serviceErrors.add(serviceError);
		}
		if (!registeredDevice.getDeviceTypeCode().equals(digitalIdDto.getType())) {
			ServiceError serviceError = new ServiceError();
			serviceError
					.setErrorCode(DeviceProviderManagementErrorCode.PROVIDER_AND_DEVICE_CODE_NOT_MAPPED.getErrorCode());
			serviceError.setMessage(DeviceProviderManagementErrorCode.PROVIDER_AND_TYPE_MAPPED.getErrorMessage());
			serviceErrors.add(serviceError);
		}
		if (!registeredDevice.getDeviceSTypeCode().equals(digitalIdDto.getSubType())) {
			ServiceError serviceError = new ServiceError();
			serviceError
					.setErrorCode(DeviceProviderManagementErrorCode.PROVIDER_AND_DEVICE_CODE_NOT_MAPPED.getErrorCode());
			serviceError.setMessage(DeviceProviderManagementErrorCode.PROVIDER_AND_SUBTYPE_MAPPED.getErrorMessage());
			serviceErrors.add(serviceError);
		}
		if (!serviceErrors.isEmpty()) {
			auditUtil.auditRequest(
					MasterDataConstant.DEVICE_VALIDATION_FAILURE + ValidateDeviceDto.class.getSimpleName(),
					MasterDataConstant.AUDIT_SYSTEM,
					String.format(MasterDataConstant.FAILURE_DESC, "KER-ADM-999", serviceErrors.toString()), "ADM-613");
			throw new ValidationException(serviceErrors);
		} else {
			serviceErrors = null;
		}

	}

	private boolean isValidServiceVersionFromHistory(String deviceServiceVersion, LocalDateTime effTimes) {
		List<MOSIPDeviceServiceHistory> deviceServiceHistory = null;
		try {
			deviceServiceHistory = deviceServiceHistoryRepository
					.findByIdAndIsActiveIsTrueAndByEffectiveTimes(deviceServiceVersion, effTimes);
		} catch (DataAccessException | DataAccessLayerException e) {
			auditUtil.auditRequest(
					MasterDataConstant.DEVICE_VALIDATION_HISTORY_FAILURE + ValidateDeviceDto.class.getSimpleName(),
					MasterDataConstant.AUDIT_SYSTEM,
					String.format(MasterDataConstant.FAILURE_DESC,
							DeviceProviderManagementErrorCode.DATABASE_EXCEPTION.getErrorCode(),
							DeviceProviderManagementErrorCode.DATABASE_EXCEPTION.getErrorMessage()),
					"ADM-614");
			throw new MasterDataServiceException(DeviceProviderManagementErrorCode.DATABASE_EXCEPTION.getErrorCode(),
					String.format(DeviceProviderManagementErrorCode.DATABASE_EXCEPTION.getErrorMessage(),
							MasterDataConstant.ERROR_OCCURED_MOSIP_DEVICE_SERVICE_HISTORY));
		}
		if (deviceServiceHistory.isEmpty()) {
			auditUtil.auditRequest(
					MasterDataConstant.DEVICE_VALIDATION_HISTORY_FAILURE + ValidateDeviceDto.class.getSimpleName(),
					MasterDataConstant.AUDIT_SYSTEM,
					String.format(MasterDataConstant.FAILURE_DESC,
							DeviceProviderManagementErrorCode.SOFTWARE_VERSION_IS_NOT_A_MATCH.getErrorCode(),
							DeviceProviderManagementErrorCode.SOFTWARE_VERSION_IS_NOT_A_MATCH.getErrorMessage()),
					"ADM-619");
			throw new RequestException(DeviceProviderManagementErrorCode.SOFTWARE_VERSION_IS_NOT_A_MATCH.getErrorCode(),
					DeviceProviderManagementErrorCode.SOFTWARE_VERSION_IS_NOT_A_MATCH.getErrorMessage());
		}

		return true;
	}

	private RegisteredDeviceHistory isRegisteredDeviceHistory(String deviceCode, LocalDateTime effTimes) {
		RegisteredDeviceHistory registeredDeviceHistory = null;
		try {
			registeredDeviceHistory = registeredDeviceHistoryRepository
					.findRegisteredDeviceHistoryByIdAndEffTimes(deviceCode, effTimes);
		} catch (DataAccessException | DataAccessLayerException e) {
			auditUtil.auditRequest(
					MasterDataConstant.DEVICE_VALIDATION_HISTORY_FAILURE + ValidateDeviceDto.class.getSimpleName(),
					MasterDataConstant.AUDIT_SYSTEM,
					String.format(MasterDataConstant.FAILURE_DESC,
							DeviceProviderManagementErrorCode.DATABASE_EXCEPTION.getErrorCode(),
							DeviceProviderManagementErrorCode.DATABASE_EXCEPTION.getErrorMessage()),
					"ADM-615");
			throw new MasterDataServiceException(DeviceProviderManagementErrorCode.DATABASE_EXCEPTION.getErrorCode(),
					String.format(DeviceProviderManagementErrorCode.DATABASE_EXCEPTION.getErrorMessage(),
							MasterDataConstant.ERROR_OCCURED_REGISTERED_DEVICE_HISTORY));
		}

		if (registeredDeviceHistory == null) {
			auditUtil.auditRequest(
					MasterDataConstant.DEVICE_VALIDATION_HISTORY_FAILURE + ValidateDeviceDto.class.getSimpleName(),
					MasterDataConstant.AUDIT_SYSTEM,
					String.format(MasterDataConstant.FAILURE_DESC,
							DeviceProviderManagementErrorCode.DEVICE_DOES_NOT_EXIST.getErrorCode(),
							DeviceProviderManagementErrorCode.DEVICE_DOES_NOT_EXIST.getErrorMessage()),
					"ADM-616");
			throw new RequestException(DeviceProviderManagementErrorCode.DEVICE_DOES_NOT_EXIST.getErrorCode(),
					DeviceProviderManagementErrorCode.DEVICE_DOES_NOT_EXIST.getErrorMessage());
		}
		if (!registeredDeviceHistory.getStatusCode().equalsIgnoreCase(REGISTERED)) {
			auditUtil.auditRequest(
					MasterDataConstant.DEVICE_VALIDATION_HISTORY_FAILURE + ValidateDeviceDto.class.getSimpleName(),
					MasterDataConstant.AUDIT_SYSTEM,
					String.format(MasterDataConstant.FAILURE_DESC,
							DeviceProviderManagementErrorCode.DEVICE_REVOKED_OR_RETIRED.getErrorCode(),
							DeviceProviderManagementErrorCode.DEVICE_REVOKED_OR_RETIRED.getErrorMessage()),
					"ADM-617");
			throw new RequestException(DeviceProviderManagementErrorCode.DEVICE_REVOKED_OR_RETIRED.getErrorCode(),
					DeviceProviderManagementErrorCode.DEVICE_REVOKED_OR_RETIRED.getErrorMessage());
		}
		return registeredDeviceHistory;

	}

	private boolean isDeviceProviderHistoryPresent(String deviceProviderId, LocalDateTime timeStamp) {
		DeviceProviderHistory deviceProviderHistory = null;

		try {
			deviceProviderHistory = deviceProviderHistoryRepository
					.findDeviceProviderHisByIdAndEffTimes(deviceProviderId, timeStamp);
		} catch (DataAccessException | DataAccessLayerException e) {
			auditUtil.auditRequest(
					MasterDataConstant.DEVICE_VALIDATION_HISTORY_FAILURE + ValidateDeviceDto.class.getSimpleName(),
					MasterDataConstant.AUDIT_SYSTEM,
					String.format(MasterDataConstant.FAILURE_DESC,
							DeviceProviderManagementErrorCode.DATABASE_EXCEPTION.getErrorCode(),
							DeviceProviderManagementErrorCode.DATABASE_EXCEPTION.getErrorMessage()),
					"ADM-618");
			throw new MasterDataServiceException(DeviceProviderManagementErrorCode.DATABASE_EXCEPTION.getErrorCode(),
					String.format(DeviceProviderManagementErrorCode.DATABASE_EXCEPTION.getErrorMessage(),
							MasterDataConstant.ERROR_OCCURED_DEVICE_PROVIDER_HISTORY));
		}
		if (deviceProviderHistory == null) {
			auditUtil.auditRequest(
					MasterDataConstant.DEVICE_VALIDATION_HISTORY_FAILURE + ValidateDeviceDto.class.getSimpleName(),
					MasterDataConstant.AUDIT_SYSTEM,
					String.format(MasterDataConstant.FAILURE_DESC,
							DeviceProviderManagementErrorCode.DEVICE_PROVIDER_NOT_EXIST.getErrorCode(),
							DeviceProviderManagementErrorCode.DEVICE_PROVIDER_NOT_EXIST.getErrorMessage()),
					"ADM-620");
			throw new RequestException(DeviceProviderManagementErrorCode.DEVICE_PROVIDER_NOT_EXIST.getErrorCode(),
					DeviceProviderManagementErrorCode.DEVICE_PROVIDER_NOT_EXIST.getErrorMessage());
		}

		return true;

	}

	public LocalDateTime parseToLocalDateTime(String dateTime) {
		return LocalDateTime.parse(dateTime, DateTimeFormatter.ofPattern(UTC_DATETIME_PATTERN));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * io.mosip.kernel.masterdata.service.DeviceProviderService#createDeviceProvider
	 * (io.mosip.kernel.masterdata.dto.DeviceProviderDto)
	 */
	@Override
	@Transactional
	public DeviceProviderExtnDto createDeviceProvider(DeviceProviderDto dto) {
		DeviceProvider entity = null;
		DeviceProvider crtDeviceProvider = null;
		try {

			DeviceProvider renDeviceProvider = deviceProviderRepository.findByNameAndAddressAndIsDeletedFalseorIsDeletedIsNullAndIsActiveTrue(dto.getVendorName(),dto.getAddress());
			
			if(renDeviceProvider!=null)
			{
				auditUtil.auditRequest(
						String.format(MasterDataConstant.FAILURE_UPDATE, DeviceProvider.class.getCanonicalName()),
						MasterDataConstant.AUDIT_SYSTEM,
						String.format(MasterDataConstant.FAILURE_DESC,
								DeviceProviderManagementErrorCode.DEVICE_PROVIDER_EXIST.getErrorCode(),
								DeviceProviderManagementErrorCode.DEVICE_PROVIDER_EXIST.getErrorMessage()),
						"ADM-725");
				throw new RequestException(DeviceProviderManagementErrorCode.DEVICE_PROVIDER_EXIST.getErrorCode(),
						String.format(DeviceProviderManagementErrorCode.DEVICE_PROVIDER_EXIST.getErrorMessage(),
								dto.getVendorName()));
			}
			entity = MetaDataUtils.setCreateMetaData(dto, DeviceProvider.class);
			entity.setId(UUID.randomUUID().toString());
			crtDeviceProvider = deviceProviderRepository.create(entity);
			
			

			// add new row to the history table
			DeviceProviderHistory entityHistory = new DeviceProviderHistory();
			MapperUtils.map(crtDeviceProvider, entityHistory);
			MapperUtils.setBaseFieldValue(crtDeviceProvider, entityHistory);
			entityHistory.setEffectivetimes(crtDeviceProvider.getCreatedDateTime());
			entityHistory.setCreatedDateTime(crtDeviceProvider.getCreatedDateTime());
			deviceProviderHistoryRepository.create(entityHistory);

		} catch (DataAccessLayerException | DataAccessException ex) {
			auditUtil.auditRequest(
					String.format(MasterDataConstant.FAILURE_CREATE, DeviceProvider.class.getCanonicalName()),
					MasterDataConstant.AUDIT_SYSTEM,
					String.format(MasterDataConstant.FAILURE_DESC,
							DeviceProviderManagementErrorCode.DEVICE_PROVIDER_INSERTION_EXCEPTION.getErrorCode(),
							DeviceProviderManagementErrorCode.DEVICE_PROVIDER_INSERTION_EXCEPTION.getErrorMessage()),
					"ADM-724");
			throw new MasterDataServiceException(
					DeviceProviderManagementErrorCode.DEVICE_PROVIDER_INSERTION_EXCEPTION.getErrorCode(),
					DeviceProviderManagementErrorCode.DEVICE_PROVIDER_INSERTION_EXCEPTION.getErrorMessage() + " "
							+ ExceptionUtils.parseException(ex));
		}
		return MapperUtils.map(crtDeviceProvider, DeviceProviderExtnDto.class);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * io.mosip.kernel.masterdata.service.DeviceProviderService#updateDeviceProvider
	 * (io.mosip.kernel.masterdata.dto.DeviceProviderDto)
	 */
	@Override
	@Transactional
	public DeviceProviderExtnDto updateDeviceProvider(DeviceProviderPutDto dto) {
		DeviceProvider entity = null;
		DeviceProvider updtDeviceProvider = null;
		DeviceProvider renDeviceProvider = null;
		DeviceProvider existingDeviceProvider = null;
		try {
			renDeviceProvider = deviceProviderRepository.findById(DeviceProvider.class, dto.getId());

			if (renDeviceProvider == null) {
				auditUtil.auditRequest(
						String.format(MasterDataConstant.FAILURE_UPDATE, DeviceProvider.class.getCanonicalName()),
						MasterDataConstant.AUDIT_SYSTEM,
						String.format(MasterDataConstant.FAILURE_DESC,
								DeviceProviderManagementErrorCode.DEVICE_PROVIDER_NOT_EXIST.getErrorCode(),
								DeviceProviderManagementErrorCode.DEVICE_PROVIDER_NOT_EXIST.getErrorMessage()),
						"ADM-725");
				throw new RequestException(DeviceProviderManagementErrorCode.DEVICE_PROVIDER_NOT_EXIST.getErrorCode(),
						String.format(DeviceProviderManagementErrorCode.DEVICE_PROVIDER_NOT_EXIST.getErrorMessage(),
								dto.getId()));
			}
			
			existingDeviceProvider = deviceProviderRepository.findByNameAndAddressAndIsDeletedFalseorIsDeletedIsNullAndIsActiveTrue(dto.getVendorName(),dto.getAddress());
			
			if(existingDeviceProvider!=null)
			{
				auditUtil.auditRequest(
						String.format(MasterDataConstant.FAILURE_UPDATE, DeviceProvider.class.getCanonicalName()),
						MasterDataConstant.AUDIT_SYSTEM,
						String.format(MasterDataConstant.FAILURE_DESC,
								DeviceProviderManagementErrorCode.DEVICE_PROVIDER_EXIST.getErrorCode(),
								DeviceProviderManagementErrorCode.DEVICE_PROVIDER_EXIST.getErrorMessage()),
						"ADM-725");
				throw new RequestException(DeviceProviderManagementErrorCode.DEVICE_PROVIDER_EXIST.getErrorCode(),
						String.format(DeviceProviderManagementErrorCode.DEVICE_PROVIDER_EXIST.getErrorMessage(),
								dto.getVendorName()));
			}
			
			entity = MetaDataUtils.setUpdateMetaData(dto, renDeviceProvider, false);
			updtDeviceProvider = deviceProviderRepository.update(entity);

			// add new row to the history table
			DeviceProviderHistory entityHistory = new DeviceProviderHistory();
			MapperUtils.map(updtDeviceProvider, entityHistory);
			MapperUtils.setBaseFieldValue(updtDeviceProvider, entityHistory);
			entityHistory.setEffectivetimes(updtDeviceProvider.getUpdatedDateTime());
			entityHistory.setCreatedDateTime(updtDeviceProvider.getUpdatedDateTime());
			deviceProviderHistoryRepository.create(entityHistory);

		} catch (DataAccessLayerException | DataAccessException ex) {
			auditUtil.auditRequest(
					String.format(MasterDataConstant.FAILURE_UPDATE, DeviceProvider.class.getCanonicalName()),
					MasterDataConstant.AUDIT_SYSTEM,
					String.format(MasterDataConstant.FAILURE_DESC,
							DeviceProviderManagementErrorCode.DEVICE_PROVIDER_UPDATE_EXCEPTION.getErrorCode(),
							DeviceProviderManagementErrorCode.DEVICE_PROVIDER_UPDATE_EXCEPTION.getErrorMessage()),
					"ADM-726");
			throw new MasterDataServiceException(
					DeviceProviderManagementErrorCode.DEVICE_PROVIDER_UPDATE_EXCEPTION.getErrorCode(),
					DeviceProviderManagementErrorCode.DEVICE_PROVIDER_UPDATE_EXCEPTION.getErrorMessage() + " "
							+ ExceptionUtils.parseException(ex));
		}
		return MapperUtils.map(updtDeviceProvider, DeviceProviderExtnDto.class);
	}

}
