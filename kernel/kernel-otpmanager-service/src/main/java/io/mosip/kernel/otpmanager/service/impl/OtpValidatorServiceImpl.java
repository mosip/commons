package io.mosip.kernel.otpmanager.service.impl;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import io.mosip.kernel.core.exception.ServiceError;
import io.mosip.kernel.core.otpmanager.spi.OtpValidator;
import io.mosip.kernel.otpmanager.constant.OtpErrorConstants;
import io.mosip.kernel.otpmanager.constant.OtpStatusConstants;
import io.mosip.kernel.otpmanager.constant.SqlQueryConstants;
import io.mosip.kernel.otpmanager.dto.OtpValidatorResponseDto;
import io.mosip.kernel.otpmanager.entity.OtpEntity;
import io.mosip.kernel.otpmanager.exception.RequiredKeyNotFoundException;
import io.mosip.kernel.otpmanager.repository.OtpRepository;
import io.mosip.kernel.otpmanager.util.OtpManagerUtils;

/**
 * This class provides the implementation for the methods of OtpValidatorService
 * interface.
 * 
 * @author Sagar Mahapatra
 * @since 1.0.0
 *
 */
@RefreshScope
@Service
public class OtpValidatorServiceImpl implements OtpValidator<ResponseEntity<OtpValidatorResponseDto>> {
	
	private static final String UPDATE_VALIDATION_RETRY_COUNT_QUERY = "%s %s SET validation_retry_count = :newNumOfAttempt,upd_dtimes = :newValidationTime WHERE refId=:refId";

	private static final String UPDATE_STATUS_CODE_AND_RETRY_COUNT_QUERY = "%s %s SET status_code = :newOtpStatus, validation_retry_count = :newNumOfAttempt, upd_dtimes = :newValidationTime WHERE refId=:refId";

	/**
	 * The reference that autowires OtpRepository.
	 */
	@Autowired
	OtpRepository otpRepository;

	/**
	 * The reference that autowires OtpManagerUtils.
	 */
	@Autowired
	OtpManagerUtils otpUtils;

	@Value("${mosip.kernel.otp.validation-attempt-threshold}")
	String numberOfValidationAttemptsAllowed;

	@Value("${mosip.kernel.otp.key-freeze-time}")
	String keyFreezeDuration;

	@Value("${mosip.kernel.otp.expiry-time}")
	String otpExpiryLimit;

	@Value("${spring.profiles.active}")
	String activeProfile;

	@Value("${local.env.otp:111111}")
	String localOtp;

	@Value("${mosip.kernel.auth.proxy-otp}")
	private boolean proxyOtp;
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * io.mosip.kernel.otpmanager.service.OtpValidatorService#validateOtp(java.lang.
	 * String, java.lang.String)
	 */
	@Override
	public ResponseEntity<OtpValidatorResponseDto> validateOtp(String key, String otp) {
		ResponseEntity<OtpValidatorResponseDto> validationResponseEntity;
		if(activeProfile.equalsIgnoreCase("local")) {
		return proxyForLocalProfile(otp);
		}
		// This method validates the input parameters.
		otpUtils.validateOtpRequestArguments(key, otp);
		OtpValidatorResponseDto responseDto;
	
		// The OTP entity for a specific key.
		String refIdHash = OtpManagerUtils.getHash(key);
		Optional<OtpEntity> otpEntityOpt = otpRepository.findByRefId(refIdHash);
		responseDto = new OtpValidatorResponseDto();
		responseDto.setMessage(OtpStatusConstants.FAILURE_MESSAGE.getProperty());
		responseDto.setStatus(OtpStatusConstants.FAILURE_STATUS.getProperty());
		validationResponseEntity = new ResponseEntity<>(responseDto, HttpStatus.OK);

		requireKeyNotFound(otpEntityOpt);
		// This variable holds the update query to be performed.
		String updateString;
		// This variable holds the count of number
		OtpEntity otpEntity = otpEntityOpt.get();
		int attemptCount = otpEntity.getValidationRetryCount();
		if ((OtpManagerUtils.timeDifferenceInSeconds(otpEntity.getGeneratedDtimes(),
				OtpManagerUtils.getCurrentLocalDateTime())) > (Integer.parseInt(otpExpiryLimit))) {

			responseDto.setStatus(OtpStatusConstants.FAILURE_STATUS.getProperty());
			responseDto.setMessage(OtpStatusConstants.OTP_EXPIRED_STATUS.getProperty());
			return new ResponseEntity<>(responseDto, HttpStatus.OK);
		}
		String keyOtpHash = OtpManagerUtils.getKeyOtpHash(key, otp);
		// This condition increases the validation attempt count.
		if ((attemptCount < Integer.parseInt(numberOfValidationAttemptsAllowed))
				&& (otpEntity.getStatusCode().equals(OtpStatusConstants.UNUSED_OTP.getProperty()))) {
			updateString = String.format(UPDATE_VALIDATION_RETRY_COUNT_QUERY, SqlQueryConstants.UPDATE.getProperty(),
					OtpEntity.class.getSimpleName());
			HashMap<String, Object> updateMap = createUpdateMap(otpEntity.getRefId(), null, attemptCount + 1,
					LocalDateTime.now(ZoneId.of("UTC")));
			updateData(updateString, updateMap);
		}
		/*
		 * This condition freezes the key for a certain time, if the validation attempt
		 * reaches the maximum allowed limit.
		 */
		if ((attemptCount == Integer.parseInt(numberOfValidationAttemptsAllowed) - 1)
				&& (!keyOtpHash.equals(otpEntity.getId()))) {
			updateString = String.format(UPDATE_STATUS_CODE_AND_RETRY_COUNT_QUERY, SqlQueryConstants.UPDATE.getProperty(),
					OtpEntity.class.getSimpleName());
			HashMap<String, Object> updateMap = createUpdateMap(otpEntity.getRefId(), OtpStatusConstants.KEY_FREEZED.getProperty(), 0,
					OtpManagerUtils.getCurrentLocalDateTime());
			updateData(updateString, updateMap);
			responseDto.setStatus(OtpStatusConstants.FAILURE_STATUS.getProperty());
			responseDto.setMessage(OtpStatusConstants.FAILURE_AND_FREEZED_MESSAGE.getProperty());
			validationResponseEntity = new ResponseEntity<>(responseDto, HttpStatus.OK);
			return validationResponseEntity;

		}
		validationResponseEntity = unFreezeKey(keyOtpHash, otpEntity, attemptCount, responseDto,
				validationResponseEntity);
		/*
		 * This condition validates the OTP if neither the key is in freezed condition,
		 * nor the OTP has expired. If the OTP validation is successful the specific
		 * message is returned as response and the entire record is deleted. If the OTP
		 * is expired, the specific message is returned as response and the entire
		 * record is deleted.
		 */
		if ((otpEntity.getId().equals(keyOtpHash))
				&& (otpEntity.getStatusCode().equals(OtpStatusConstants.UNUSED_OTP.getProperty())
						&& ((OtpManagerUtils.timeDifferenceInSeconds(otpEntity.getGeneratedDtimes(),
								OtpManagerUtils.getCurrentLocalDateTime())) <= (Integer.parseInt(otpExpiryLimit))))) {
			responseDto.setStatus(OtpStatusConstants.SUCCESS_STATUS.getProperty());
			responseDto.setMessage(OtpStatusConstants.SUCCESS_MESSAGE.getProperty());
			otpRepository.deleteById(keyOtpHash);
			return new ResponseEntity<>(responseDto, HttpStatus.OK);
		}
		return validationResponseEntity;
	}

	private void requireKeyNotFound(Optional<OtpEntity> entityOpt) {
		/*
		 * Checking whether the key exists in repository or not. If not, throw an
		 * exception.
		 */
		if (entityOpt.isEmpty()) {
			List<ServiceError> validationErrorsList = new ArrayList<>();
			validationErrorsList.add(new ServiceError(OtpErrorConstants.OTP_VAL_KEY_NOT_FOUND.getErrorCode(),
					OtpErrorConstants.OTP_VAL_KEY_NOT_FOUND.getErrorMessage()));
			throw new RequiredKeyNotFoundException(validationErrorsList);
		}
	}

	private ResponseEntity<OtpValidatorResponseDto> proxyForLocalProfile(String otp) {
		ResponseEntity<OtpValidatorResponseDto> validationResponseEntity;
		OtpValidatorResponseDto responseDto = new OtpValidatorResponseDto();
		// Verify in case of local otp
		if (otp.equalsIgnoreCase(localOtp)) {
			responseDto.setStatus(OtpStatusConstants.SUCCESS_STATUS.getProperty());
			responseDto.setMessage(OtpStatusConstants.SUCCESS_MESSAGE.getProperty());
			validationResponseEntity = new ResponseEntity<>(responseDto, HttpStatus.OK);
			return validationResponseEntity;
		} else {
			responseDto.setStatus(OtpStatusConstants.FAILURE_STATUS.getProperty());
			responseDto.setMessage(OtpStatusConstants.FAILURE_MESSAGE.getProperty());
			validationResponseEntity = new ResponseEntity<>(responseDto, HttpStatus.OK);
			return validationResponseEntity;
		}
	}

	/**
	 * This method handles the freeze conditions i.e., If the key is freezed, it
	 * blocks the validation for the assigned freeze period. If the key is freezed
	 * and has completed the freeze time, it unfreezes the key.
	 * 
	 * @param key                      the key.
	 * @param otp                      the OTP.
	 * @param otpEntity              the OTP response.
	 * @param attemptCount             the attempt count.
	 * @param responseDto              the response dto.
	 * @param validationResponseEntity the validation response entity.
	 * @return the response entity.
	 */
	private ResponseEntity<OtpValidatorResponseDto> unFreezeKey(String keyOtpHash, OtpEntity otpEntity,
			int attemptCount, OtpValidatorResponseDto responseDto,
			ResponseEntity<OtpValidatorResponseDto> validationResponseEntity) {
		String updateString;
		if (otpEntity.getStatusCode().equals(OtpStatusConstants.KEY_FREEZED.getProperty())) {
			if ((OtpManagerUtils.timeDifferenceInSeconds(otpEntity.getUpdatedDtimes(),
					OtpManagerUtils.getCurrentLocalDateTime())) > (Integer.parseInt(keyFreezeDuration))) {
				updateString = String.format(UPDATE_STATUS_CODE_AND_RETRY_COUNT_QUERY, SqlQueryConstants.UPDATE.getProperty(),
						OtpEntity.class.getSimpleName());
				HashMap<String, Object> updateMap = createUpdateMap(otpEntity.getRefId(), OtpStatusConstants.UNUSED_OTP.getProperty(),
						Integer.valueOf(attemptCount + 1), OtpManagerUtils.getCurrentLocalDateTime());
				if (keyOtpHash.equals(otpEntity.getId())) {
					responseDto.setStatus(OtpStatusConstants.SUCCESS_STATUS.getProperty());
					responseDto.setMessage(OtpStatusConstants.SUCCESS_MESSAGE.getProperty());
					validationResponseEntity = new ResponseEntity<>(responseDto, HttpStatus.OK);
					otpRepository.deleteById(keyOtpHash);
				} else {
					updateData(updateString, updateMap);
				}
			} else {
				responseDto.setMessage(OtpStatusConstants.FAILURE_AND_FREEZED_MESSAGE.getProperty());
				validationResponseEntity = new ResponseEntity<>(responseDto, HttpStatus.OK);
			}
		}
		return validationResponseEntity;
	}

	/**
	 * This method creates the UPDATE map required for UPDATE operations.
	 * 
	 * @param key                the key to be updated.
	 * @param status             the status to be updated.
	 * @param newNumberOfAttempt the new number of attempt value.
	 * @param localDateTime      the new LocalDateTime.
	 * @return the map.
	 */
	private HashMap<String, Object> createUpdateMap(String key, String status, Integer newNumberOfAttempt,
			LocalDateTime localDateTime) {
		HashMap<String, Object> updateMap = new HashMap<>();
		if (key != null) {
			updateMap.put(SqlQueryConstants.REF_ID.getProperty(), key);
		}
		if (status != null) {
			updateMap.put(SqlQueryConstants.NEW_OTP_STATUS.getProperty(), status);
		}
		if (newNumberOfAttempt != null) {
			updateMap.put(SqlQueryConstants.NEW_NUM_OF_ATTEMPT.getProperty(), newNumberOfAttempt);
		}
		if (localDateTime != null) {
			updateMap.put(SqlQueryConstants.NEW_VALIDATION_TIME.getProperty(), localDateTime);
		}
		return updateMap;
	}

	/**
	 * This method handles UPDATE query operations.
	 * 
	 * @param updateString the query string.
	 * @param updateMap    the query map.
	 */
	private void updateData(String updateString, HashMap<String, Object> updateMap) {
		otpRepository.createQueryUpdateOrDelete(updateString, updateMap);
	}
}