package io.mosip.kernel.ridgenerator.service.impl;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import io.mosip.kernel.core.dataaccess.exception.DataAccessLayerException;
import io.mosip.kernel.core.util.MathUtils;
import io.mosip.kernel.ridgenerator.constant.RidGeneratorExceptionConstant;
import io.mosip.kernel.ridgenerator.constant.RidGeneratorPropertyConstant;
import io.mosip.kernel.ridgenerator.dto.RidGeneratorResponseDto;
import io.mosip.kernel.ridgenerator.entity.Rid;
import io.mosip.kernel.ridgenerator.exception.EmptyInputException;
import io.mosip.kernel.ridgenerator.exception.InputLengthException;
import io.mosip.kernel.ridgenerator.exception.RidException;
import io.mosip.kernel.ridgenerator.repository.RidRepository;
import io.mosip.kernel.ridgenerator.service.RidGeneratorService;

/**
 * Spring service that allocates the next RID sequence for a center/machine pair.
 *
 * @author Ritesh Sinha
 * @since 1.0.0
 *
 */
@Service
public class RidGeneratorServiceImpl implements RidGeneratorService<RidGeneratorResponseDto> {

	/**
	 * Registration center id length ({@code mosip.kernel.registrationcenterid.length}).
	 */
	@Value("${mosip.kernel.registrationcenterid.length:-1}")
	private int centerIdLength;

	/**
	 * Machine id length ({@code mosip.kernel.machineid.length}).
	 */
	@Value("${mosip.kernel.machineid.length:-1}")
	private int machineIdLength;

	/**
	 * Sequence digit length ({@code mosip.kernel.rid.sequence-length}).
	 */
	@Value("${mosip.kernel.rid.sequence-length:-1}")
	private int sequenceLength;

	/**
	 * Timestamp digit length ({@code mosip.kernel.rid.timestamp-length}).
	 */
	@Value("${mosip.kernel.rid.timestamp-length:-1}")
	private int timeStampLength;

	/**
	 * First sequence value for a new center/machine pair ({@code mosip.kernel.rid.sequence-initial-value}).
	 */
	@Value("${mosip.kernel.rid.sequence-initial-value:1}")
	private int sequenceInitialValue;

	/**
	 * Reference to {@link RidRepository}.
	 */
	@Autowired
	private RidRepository repository;

	/**
	 * Generates a RID from center id, machine id, next sequence, and UTC timestamp.
	 *
	 * @param centerId  registration center id
	 * @param machineId registration machine id
	 * @return DTO containing the generated RID
	 * @throws EmptyInputException when {@code centerId} or {@code machineId} is empty
	 * @throws InputLengthException when either id does not match configured length
	 * @throws RidException when the sequence row cannot be read or updated
	 */
	@Override
	public RidGeneratorResponseDto generateRid(String centerId, String machineId) {
		validateInput(centerId, machineId, centerIdLength, machineIdLength);
		String randomDigitRid = sequenceNumberGenerator(centerId, machineId, sequenceLength);
		String rid = appendString(randomDigitRid, getcurrentTimeStamp(), centerId, machineId);
		RidGeneratorResponseDto response = new RidGeneratorResponseDto();
		response.setRid(rid);
		return response;
	}

	/**
	 * Allocates the next sequence for {@code centerId} and {@code machineId}, wrapping at the max value.
	 *
	 * @param centerId       registration center id
	 * @param machineId      registration machine id
	 * @param sequenceLength number of sequence digits
	 * @return zero-padded sequence
	 * @throws RidException when the sequence row cannot be read or updated
	 */
	private String sequenceNumberGenerator(String centerId, String machineId, int sequenceLength) {
		int sequenceId = 0;
		Rid entity = null;
		int sequenceEndvalue = MathUtils.getPow(10, sequenceLength) - 1;
		String sequenceFormat = "%0" + sequenceLength + "d";
		try {
			entity = repository.findRid(centerId, machineId);
		} catch (DataAccessException | DataAccessLayerException e) {
			throw new RidException(RidGeneratorExceptionConstant.RID_FETCH_EXCEPTION.getErrorCode(),
					RidGeneratorExceptionConstant.RID_FETCH_EXCEPTION.getErrorMessage(), e);
		}
		try {

			if (entity == null) {
				entity = new Rid();
				sequenceId = sequenceInitialValue;
				entity.setCurrentSequenceNo(sequenceInitialValue);
				entity.setMachineId(machineId);
				entity.setCenterId(centerId);
				entity.setCreatedBy("SYSTEM");
				entity.setCreatedDateTime(LocalDateTime.now(ZoneId.of("UTC")));
				repository.save(entity);
			} else {
				entity.setUpdatedBy("SYSTEM"); // Can be changed to log in user
				entity.setUpdatedDateTime(LocalDateTime.now(ZoneId.of("UTC")));
				if (entity.getCurrentSequenceNo() == sequenceEndvalue) {
					sequenceId = sequenceInitialValue;
				} else {
					sequenceId = entity.getCurrentSequenceNo() + 1;
				}
				repository.updateRid(sequenceId, centerId, machineId);
			}

		} catch (DataAccessException | DataAccessLayerException e) {
			throw new RidException(RidGeneratorExceptionConstant.RID_UPDATE_EXCEPTION.getErrorCode(),
					RidGeneratorExceptionConstant.RID_UPDATE_EXCEPTION.errorMessage, e);

		}

		return String.format(sequenceFormat, sequenceId);

	}

	/**
	 * This method gets the current timestamp in yyyyMMddHHmmss format.
	 * 
	 * @return current timestamp in fourteen digits
	 */
	private String getcurrentTimeStamp() {
		DateTimeFormatter format = DateTimeFormatter
				.ofPattern(RidGeneratorPropertyConstant.TIMESTAMP_FORMAT.getProperty());
		return LocalDateTime.now(ZoneId.of("UTC")).format(format);
	}

	/**
	 * Concatenates center id, machine id, sequence, and timestamp into a RID.
	 *
	 * @param randomDigitRid   zero-padded sequence
	 * @param currentTimeStamp UTC timestamp in {@link RidGeneratorPropertyConstant#TIMESTAMP_FORMAT}
	 * @param centreId         registration center id
	 * @param machineId        registration machine id
	 * @return concatenated RID
	 */
	private String appendString(String randomDigitRid, String currentTimeStamp, String centreId, String machineId) {
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append(centreId).append(machineId).append(randomDigitRid).append(currentTimeStamp);
		return (stringBuilder.toString().trim());
	}

	/**
	 * Rejects empty or wrong-length center and machine ids.
	 *
	 * @param centreId         registration center id
	 * @param machineId        registration machine id
	 * @param centerIdLength   required center id length
	 * @param machineIdLength  required machine id length
	 * @throws EmptyInputException  when either id is empty
	 * @throws InputLengthException when either id length does not match
	 */
	private void validateInput(String centreId, String machineId, int centerIdLength, int machineIdLength) {
		if (centreId.isEmpty() || machineId.isEmpty()) {
			throw new EmptyInputException(RidGeneratorExceptionConstant.EMPTY_INPUT_ERROR_CODE.getErrorCode(),
					RidGeneratorExceptionConstant.EMPTY_INPUT_ERROR_CODE.getErrorMessage());
		}
		if (centreId.length() != centerIdLength || machineId.length() != machineIdLength) {
			throw new InputLengthException(RidGeneratorExceptionConstant.INPUT_LENGTH_ERROR_CODE.getErrorCode(),
					RidGeneratorExceptionConstant.INPUT_LENGTH_ERROR_CODE.getErrorMessage());
		}

	}

}
