package io.mosip.commons.packet.util;

import io.mosip.kernel.core.logger.spi.Logger;
import org.springframework.stereotype.Component;

@Component
public class IdObjectsSchemaValidationOperationMapper {

	enum SyncTypeDto {

		/** The new registration. */
		NEW("NEW"),

		/** The update uin. */
		UPDATE("UPDATE"),

		/** The lost uin. */
		LOST("LOST"),


		/** The activate uin. */
		ACTIVATED("ACTIVATED"),

		/** The deactivate uin. */
		DEACTIVATED("DEACTIVATED"),

		/** The res update. */
		RES_UPDATE("RES_UPDATE"),

		/** The res re-print. */
		RES_REPRINT("RES_REPRINT");

		/** The value. */
		private String value;

		/**
		 * Instantiates a new sync type dto.
		 *
		 * @param value the value
		 */
		private SyncTypeDto (String value) {
			this.value = value;
		}

		/**
		 * Gets the value.
		 *
		 * @return the value
		 */
		public String getValue() {
			return this.value;
		}

	}

	enum IdObjectValidatorSupportedOperations {
		NEW_REGISTRATION("new-registration"),

		CHILD_REGISTRATION("child-registration"),

		OTHER("other"),

		LOST("lost");

		private String operation;

		IdObjectValidatorSupportedOperations(String operation) {
			this.operation = operation;
		}

		public String getOperation() {
			return operation;
		}
	}

	/*@Value("${mosip.kernel.applicant.type.age.limit}")
	private String ageLimit;*/
	
		/** The reg proc logger. */
	private static Logger LOGGER = PacketManagerLogger.getLogger(IdObjectsSchemaValidationOperationMapper.class);
	
	public static String getOperation(String id, String process) {
		LOGGER.debug(PacketManagerLogger.SESSIONID.toString(), PacketManagerLogger.REGISTRATIONID.toString(), "",
				"IdObjectsSchemaValidationOperationMapper::getOperation()::entry");

		if(process.equalsIgnoreCase(SyncTypeDto.NEW.getValue())) {
			/*int age = 20;//utility.getApplicantAge(id);
			int ageThreshold = Integer.parseInt(ageLimit);
			if (age < ageThreshold) {
				LOGGER.debug(PacketManagerLogger.SESSIONID.toString(), PacketManagerLogger.REGISTRATIONID.toString(), "",
						"IdObjectsSchemaValidationOperationMapper::getOperation()::exit-NEW child");
				return IdObjectValidatorSupportedOperations.CHILD_REGISTRATION;
			}*/
			LOGGER.debug(PacketManagerLogger.SESSIONID.toString(), PacketManagerLogger.REGISTRATIONID.toString(), "",
					"IdObjectsSchemaValidationOperationMapper::getOperation()::exit-NEW");
			return IdObjectValidatorSupportedOperations.NEW_REGISTRATION.getOperation();
		}
		else if(process.equalsIgnoreCase(SyncTypeDto.LOST.getValue())) {
			LOGGER.debug(PacketManagerLogger.SESSIONID.toString(), PacketManagerLogger.REGISTRATIONID.toString(), "",
					"IdObjectsSchemaValidationOperationMapper::getOperation()::exit-LOST");
			return IdObjectValidatorSupportedOperations.LOST.getOperation();
		}
		else if(process.equalsIgnoreCase(SyncTypeDto.UPDATE.getValue())) {
			LOGGER.debug(PacketManagerLogger.SESSIONID.toString(), PacketManagerLogger.REGISTRATIONID.toString(), "",
					"IdObjectsSchemaValidationOperationMapper::getOperation()::exit-UPDATE");
			return IdObjectValidatorSupportedOperations.OTHER.getOperation();
		}
		else if(process.equalsIgnoreCase(SyncTypeDto.RES_UPDATE.getValue())) {
			LOGGER.debug(PacketManagerLogger.SESSIONID.toString(), PacketManagerLogger.REGISTRATIONID.toString(), "",
					"IdObjectsSchemaValidationOperationMapper::getOperation()::exit-RES_UPDATE");
			return IdObjectValidatorSupportedOperations.OTHER.getOperation();
		}
		else if(process.equalsIgnoreCase(SyncTypeDto.ACTIVATED.getValue())) {
			LOGGER.debug(PacketManagerLogger.SESSIONID.toString(), PacketManagerLogger.REGISTRATIONID.toString(), "",
					"IdObjectsSchemaValidationOperationMapper::getOperation()::exit-ACTIVATED");
			return IdObjectValidatorSupportedOperations.OTHER.getOperation();
		}
		else if(process.equalsIgnoreCase(SyncTypeDto.DEACTIVATED.getValue())) {
			LOGGER.debug(PacketManagerLogger.SESSIONID.toString(), PacketManagerLogger.REGISTRATIONID.toString(), "",
					"IdObjectsSchemaValidationOperationMapper::getOperation()::exit-DEACTIVATED");
			return IdObjectValidatorSupportedOperations.OTHER.getOperation();
		}
		return process;
		
	}

	/*private int getApplicantAge(String registrationId) throws IOException, ApisResourceAccessException,
			PacketDecryptionFailureException, io.mosip.kernel.core.exception.IOException, RegistrationProcessorCheckedException, ApiNotAccessibleException {

		JSONObject regProcessorIdentityJson = getRegistrationProcessorMappingJson();
		String ageKey = JsonUtil.getJSONValue(JsonUtil.getJSONObject(regProcessorIdentityJson, MappingJsonConstants.AGE), VALUE);
		String dobKey = JsonUtil.getJSONValue(JsonUtil.getJSONObject(regProcessorIdentityJson, MappingJsonConstants.DOB), VALUE);


		String applicantDob = JsonUtil.getJSONValue(getDemographicIdentityJSONObject(registrationId,dobKey), dobKey);
		Integer applicantAge = JsonUtil.getJSONValue(getDemographicIdentityJSONObject(registrationId,ageKey), ageKey);
		if (applicantDob != null) {
			return calculateAge(applicantDob);
		} else if (applicantAge != null) {
			return applicantAge;

		}
	}*/
}
