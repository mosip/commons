package io.mosip.kernel.idgenerator.rid.impl;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;

import io.mosip.kernel.core.dataaccess.exception.DataAccessLayerException;
import io.mosip.kernel.core.idgenerator.spi.RidGenerator;
import io.mosip.kernel.core.util.MathUtils;
import io.mosip.kernel.idgenerator.rid.constant.RidGeneratorExceptionConstant;
import io.mosip.kernel.idgenerator.rid.constant.RidGeneratorPropertyConstant;
import io.mosip.kernel.idgenerator.rid.entity.Rid;
import io.mosip.kernel.idgenerator.rid.exception.EmptyInputException;
import io.mosip.kernel.idgenerator.rid.exception.InputLengthException;
import io.mosip.kernel.idgenerator.rid.exception.NullValueException;
import io.mosip.kernel.idgenerator.rid.exception.RidException;
import io.mosip.kernel.idgenerator.rid.repository.RidRepository;

/**
 * This class generate 28 digits registration id.
 * 
 * @author Ritesh Sinha
 * @author Sidhant Agarwal
 * @author Abhishek Kumar
 * @since 1.0.0
 *
 */
@Component
public class RidGeneratorImpl implements RidGenerator<String> {

	/** Center-id length from {@code mosip.kernel.registrationcenterid.length}. */
	@Value("${mosip.kernel.registrationcenterid.length:-1}")
	private int centerIdLength;

	/** Machine-id length from {@code mosip.kernel.machineid.length}. */
	@Value("${mosip.kernel.machineid.length:-1}")
	private int machineIdLength;

	/** Sequence-segment length from {@code mosip.kernel.rid.sequence-length}. */
	@Value("${mosip.kernel.rid.sequence-length:-1}")
	private int sequenceLength;

	/** Timestamp-segment length from {@code mosip.kernel.rid.timestamp-length}. */
	@Value("${mosip.kernel.rid.timestamp-length:-1}")
	private int timeStampLength;

	/** First sequence value from {@code mosip.kernel.rid.sequence-initial-value}. */
	@Value("${mosip.kernel.rid.sequence-initial-value:1}")
	private int sequenceInitialValue;

	@Autowired
	RidRepository ridRepository;

	/**
	 * Builds an RID from {@code centreId}, {@code machineId}, the next sequence, and UTC timestamp.
	 *
	 * @param centreId  registration-center id
	 * @param machineId machine id
	 * @return concatenated RID
	 * @throws NullValueException  if either id is {@code null}
	 * @throws EmptyInputException if either id is empty
	 * @throws InputLengthException if a length is invalid
	 * @throws RidException if the sequence cannot be read or written
	 */
	@Override
	public String generateId(String centreId, String machineId) {
		validateInput(centreId, machineId, centerIdLength, machineIdLength, timeStampLength);

		String randomDigitRid = sequenceNumberGenerator(sequenceLength);

		return appendString(randomDigitRid, getcurrentTimeStamp(), centreId, machineId);
	}

	/**
	 * Builds an RID using caller-supplied segment lengths.
	 *
	 * @param centreId        registration-center id
	 * @param machineId       machine id
	 * @param centerIdLength  expected center-id length
	 * @param machineIdLength expected machine-id length
	 * @param sequenceLength  sequence-segment length
	 * @param timeStampLength timestamp-segment length (validated but timestamp is always 14 digits)
	 * @return concatenated RID
	 * @throws NullValueException  if either id is {@code null}
	 * @throws EmptyInputException if either id is empty
	 * @throws InputLengthException if a length is invalid
	 * @throws RidException if the sequence cannot be read or written
	 */
	@Override
	public String generateId(String centreId, String machineId, int centerIdLength, int machineIdLength,
			int sequenceLength, int timeStampLength) {
		validateInput(centreId, machineId, centerIdLength, machineIdLength, timeStampLength);

		String randomDigitRid = sequenceNumberGenerator(sequenceLength);

		return appendString(randomDigitRid, getcurrentTimeStamp(), centreId, machineId);
	}

	/**
	 * Rejects null, empty, or wrong-length center/machine ids and non-positive lengths.
	 *
	 * @param centreId        center id
	 * @param machineId       machine id
	 * @param centerIdLength  expected center-id length
	 * @param machineIdLength expected machine-id length
	 * @param timeStampLength timestamp length (must be &gt; 0)
	 * @throws NullValueException  if either id is {@code null}
	 * @throws EmptyInputException if either id is empty
	 * @throws InputLengthException if a length is invalid
	 */
	private void validateInput(String centreId, String machineId, int centerIdLength, int machineIdLength,
			int timeStampLength) {

		if (centerIdLength <= 0 || machineIdLength <= 0 || timeStampLength <= 0) {
			throw new InputLengthException(
					RidGeneratorExceptionConstant.INVALID_CENTERID_OR_MACHINEID_TIMESTAMP_LENGTH.getErrorCode(),
					RidGeneratorExceptionConstant.INVALID_CENTERID_OR_MACHINEID_TIMESTAMP_LENGTH.getErrorMessage());
		}

		if (centreId == null || machineId == null) {

			throw new NullValueException(RidGeneratorExceptionConstant.NULL_VALUE_ERROR_CODE.getErrorCode(),
					RidGeneratorExceptionConstant.NULL_VALUE_ERROR_CODE.getErrorMessage());
		}
		if (centreId.isEmpty() || machineId.isEmpty()) {

			throw new EmptyInputException(RidGeneratorExceptionConstant.EMPTY_INPUT_ERROR_CODE.getErrorCode(),
					RidGeneratorExceptionConstant.EMPTY_INPUT_ERROR_CODE.getErrorMessage());
		}
		if (centreId.length() != centerIdLength || machineId.length() != machineIdLength) {

			throw new InputLengthException(RidGeneratorExceptionConstant.INPUT_LENGTH_ERROR_CODE.getErrorCode(),
					RidGeneratorExceptionConstant.INPUT_LENGTH_ERROR_CODE.getErrorMessage());
		}

	}

	/**
	 * Allocates the next zero-padded sequence from {@code rid_seq}, wrapping at {@code 10^sequenceLength-1}.
	 *
	 * @param sequenceLength number of sequence digits
	 * @return zero-padded sequence
	 * @throws InputLengthException if {@code sequenceLength} is not greater than zero
	 * @throws RidException if the sequence cannot be read or written
	 */
	private String sequenceNumberGenerator(int sequenceLength) {
		int sequenceId = 0;
		Rid entity = null;
		int sequenceEndvalue = MathUtils.getPow(10, sequenceLength) - 1;
		String sequenceFormat = "%0" + sequenceLength + "d";

		if (sequenceLength <= 0) {
			throw new InputLengthException(RidGeneratorExceptionConstant.INVALID_SEQ_LENGTH_EXCEPTION.getErrorCode(),
					RidGeneratorExceptionConstant.INVALID_SEQ_LENGTH_EXCEPTION.getErrorMessage());
		}

		try {
			entity = ridRepository.findLastRid();
		} catch (DataAccessException | DataAccessLayerException e) {
			throw new RidException(RidGeneratorExceptionConstant.RID_FETCH_EXCEPTION.getErrorCode(),
					RidGeneratorExceptionConstant.RID_FETCH_EXCEPTION.errorMessage, e);
		}
		try {
			if (entity == null) {
				entity = new Rid();
				sequenceId = sequenceInitialValue;
				entity.setCurrentSequenceNo(sequenceInitialValue);
				entity.setCreatedBy("SYSTEM"); // Can be changed to log in user
				entity.setCreatedDateTime(LocalDateTime.now(ZoneId.of("UTC")));
				ridRepository.save(entity);
			} else {
				entity.setUpdatedBy("SYSTEM"); // Can be changed to log in user
				entity.setUpdatedDateTime(LocalDateTime.now(ZoneId.of("UTC")));
				if (entity.getCurrentSequenceNo() == sequenceEndvalue) {
					sequenceId = sequenceInitialValue;
					ridRepository.updateRid(sequenceInitialValue, entity.getCurrentSequenceNo());
				} else {
					sequenceId = entity.getCurrentSequenceNo() + 1;
					ridRepository.updateRid(sequenceId, entity.getCurrentSequenceNo());
				}
			}
		} catch (DataAccessException | DataAccessLayerException e) {
			throw new RidException(RidGeneratorExceptionConstant.RID_UPDATE_EXCEPTION.getErrorCode(),
					RidGeneratorExceptionConstant.RID_UPDATE_EXCEPTION.errorMessage, e);
		}
		return String.format(sequenceFormat, sequenceId);
	}

	/**
	 * Concatenates center id, machine id, sequence, and timestamp.
	 *
	 * @param randomDigitRid   sequence segment
	 * @param currentTimeStamp timestamp segment
	 * @param centreId         center id
	 * @param dongleId         machine id
	 * @return concatenated RID
	 */
	private String appendString(String randomDigitRid, String currentTimeStamp, String centreId, String dongleId) {
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append(centreId).append(dongleId).append(randomDigitRid).append(currentTimeStamp);
		return (stringBuilder.toString().trim());
	}

	/**
	 * This method gets the current timestamp in yyyymmddhhmmss format.
	 * 
	 * @return current timestamp in fourteen digits
	 */
	private String getcurrentTimeStamp() {
		DateTimeFormatter format = DateTimeFormatter
				.ofPattern(RidGeneratorPropertyConstant.TIMESTAMP_FORMAT.getProperty());
		return LocalDateTime.now(ZoneId.of("UTC")).format(format);
	}

}
