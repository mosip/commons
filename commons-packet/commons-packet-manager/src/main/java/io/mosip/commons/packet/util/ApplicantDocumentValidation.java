/*
package io.mosip.commons.packet.util;

import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.kernel.packetmanager.exception.ApiNotAccessibleException;
import io.mosip.kernel.packetmanager.exception.PacketDecryptionFailureException;
import io.mosip.kernel.packetmanager.spi.PacketReaderService;
import io.mosip.registration.processor.core.constant.LoggerFileConstant;
import io.mosip.registration.processor.core.constant.MappingJsonConstants;
import io.mosip.registration.processor.core.logger.RegProcessorLogger;
import io.mosip.registration.processor.core.util.IdentityIteratorUtil;
import io.mosip.registration.processor.core.util.JsonUtil;
import io.mosip.registration.processor.packet.storage.exception.IdentityNotFoundException;
import io.mosip.registration.processor.packet.storage.utils.Utilities;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;

*/
/**
 * The Class ApplicantDocumentValidation.
 * 
 *//*

public class ApplicantDocumentValidation {

	@Autowired
	private IdSchemaUtils idSchemaUtils;
	
	private static final String VALUE = "value";

	

	
	public boolean validateDocument(String registrationId) {
		
		JSONObject regProcessorIdentityJson = utility.getRegistrationProcessorMappingJson();
		String proofOfAddressLabel = JsonUtil.getJSONValue(JsonUtil.getJSONObject(regProcessorIdentityJson, MappingJsonConstants.POA), VALUE);
		String proofOfDateOfBirthLabel = JsonUtil.getJSONValue(JsonUtil.getJSONObject(regProcessorIdentityJson, MappingJsonConstants.POB), VALUE);
		String proofOfIdentityLabel = JsonUtil.getJSONValue(JsonUtil.getJSONObject(regProcessorIdentityJson, MappingJsonConstants.POI), VALUE);
		String proofOfRelationshipLabel = JsonUtil.getJSONValue(JsonUtil.getJSONObject(regProcessorIdentityJson, MappingJsonConstants.POR), VALUE);
		String proofOfExceptionsLabel = JsonUtil.getJSONValue(JsonUtil.getJSONObject(regProcessorIdentityJson, MappingJsonConstants.POE), VALUE);
		String applicantBiometricLabel = JsonUtil.getJSONValue(JsonUtil.getJSONObject(regProcessorIdentityJson, MappingJsonConstants.INDIVIDUAL_BIOMETRICS), VALUE);
		String introducerBiometricLabel = JsonUtil.getJSONValue(JsonUtil.getJSONObject(regProcessorIdentityJson, MappingJsonConstants.PARENT_OR_GUARDIAN_BIO), VALUE);
		
		JSONObject proofOfAddress = JsonUtil.getJSONObject(utility.getDemographicIdentityJSONObject(registrationId,proofOfAddressLabel), proofOfAddressLabel);
		JSONObject proofOfDateOfBirth = JsonUtil.getJSONObject(utility.getDemographicIdentityJSONObject(registrationId,proofOfDateOfBirthLabel), proofOfDateOfBirthLabel);
		JSONObject proofOfIdentity = JsonUtil.getJSONObject(utility.getDemographicIdentityJSONObject(registrationId,proofOfIdentityLabel), proofOfIdentityLabel);
		JSONObject proofOfRelationship = JsonUtil.getJSONObject(utility.getDemographicIdentityJSONObject(registrationId,proofOfRelationshipLabel), proofOfRelationshipLabel);
		JSONObject applicantBiometric = JsonUtil.getJSONObject(utility.getDemographicIdentityJSONObject(registrationId,applicantBiometricLabel), applicantBiometricLabel);
		JSONObject proofOfExceptions = JsonUtil.getJSONObject(utility.getDemographicIdentityJSONObject(registrationId,proofOfExceptionsLabel), proofOfExceptionsLabel);
		JSONObject introducerBiometric = JsonUtil.getJSONObject(utility.getDemographicIdentityJSONObject(registrationId,introducerBiometricLabel), introducerBiometricLabel);
		
		if (proofOfAddress != null && proofOfAddress.get("value")!=null) {
			String source=idSchemaUtils.getSource(proofOfAddressLabel, packetReaderService.getIdSchemaVersionFromPacket(registrationId));
			if(source!=null) {
			if(! packetReaderService.checkFileExistence(registrationId,proofOfAddress.get("value").toString(),source)) {
				return false;
			}
			}
		}
		if (proofOfDateOfBirth != null && proofOfDateOfBirth.get("value")!=null) {
			String source=idSchemaUtils.getSource(proofOfDateOfBirthLabel, packetReaderService.getIdSchemaVersionFromPacket(registrationId));
			if(source!=null) {
			if(! packetReaderService.checkFileExistence(registrationId,proofOfDateOfBirth.get("value").toString(),source)) {
				return false;
			}
			}
		}
		if (proofOfIdentity != null && proofOfIdentity.get("value")!=null) {
			String source=idSchemaUtils.getSource(proofOfIdentityLabel, packetReaderService.getIdSchemaVersionFromPacket(registrationId));
			if(source!=null) {
			if(! packetReaderService.checkFileExistence(registrationId,proofOfIdentity.get("value").toString(),source)) {
				return false;
			}
			}
		}
		if (proofOfRelationship != null && proofOfRelationship.get("value")!=null) {
			String source=idSchemaUtils.getSource(proofOfRelationshipLabel, packetReaderService.getIdSchemaVersionFromPacket(registrationId));
			if(source!=null) {
			if(! packetReaderService.checkFileExistence(registrationId,proofOfRelationship.get("value").toString(),source)) {
				return false;
			}
			}
		}
		if (applicantBiometric != null && applicantBiometric.get("value")!=null) {
			String source=idSchemaUtils.getSource(applicantBiometricLabel, packetReaderService.getIdSchemaVersionFromPacket(registrationId));
			if(source!=null) {
			if(! packetReaderService.checkFileExistence(registrationId,applicantBiometric.get("value").toString(),source)) {
				return false;
			}
			}
		}
		if (introducerBiometric != null && introducerBiometric.get("value")!=null) {
			String source=idSchemaUtils.getSource(introducerBiometricLabel, packetReaderService.getIdSchemaVersionFromPacket(registrationId));
			if(source!=null) {
			if(! packetReaderService.checkFileExistence(registrationId,introducerBiometric.get("value").toString(),source)) {
				return false;
			}
			}
		}
		if (proofOfExceptions != null && proofOfExceptions.get("value")!=null) {
			String source=idSchemaUtils.getSource(proofOfExceptionsLabel, packetReaderService.getIdSchemaVersionFromPacket(registrationId));
			if(source!=null) {
				if(! packetReaderService.checkFileExistence(registrationId,proofOfExceptions.get("value").toString(),source)) {
				return false;
			}
			}
		}
		

		regProcLogger.debug(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.ID.toString(),
				registrationId, "ApplicantDocumentValidation::validateApplicantData::exit");
		return true;
	}

	
}
*/
