package io.mosip.kernel.syncdata.service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

import io.mosip.kernel.syncdata.dto.IdSchemaDto;
import io.mosip.kernel.syncdata.dto.UploadPublicKeyRequestDto;
import io.mosip.kernel.syncdata.dto.UploadPublicKeyResponseDto;
import io.mosip.kernel.syncdata.dto.response.KeyPairGenerateResponseDto;
import io.mosip.kernel.syncdata.dto.response.MasterDataResponseDto;
import io.mosip.kernel.syncdata.dto.response.SyncDataResponseDto;

/**
 * Masterdata sync handler service
 * 
 * @author Abhishek Kumar
 * @author Urvil Joshi
 * @since 1.0.0
 *
 */
public interface SyncMasterDataService {

	/**
	 * 
	 * @param regCenterId      - registration center id
	 * @param macAddress       - MAC address of the machine
	 * @param serialNumber     - serial number for either desktop or dongle
	 * @param lastUpdated      - last updated time stamp
	 * @param currentTimestamp - current time stamp
	 * @return {@link MasterDataResponseDto}
	 * @throws InterruptedException - this method will throw execution exception
	 * @throws ExecutionException   -this method will throw interrupted exception
	 */	
	@Deprecated
	MasterDataResponseDto syncData(String regCenterId, String macAddress, String serialNumber,
			LocalDateTime lastUpdated, LocalDateTime currentTimestamp, String keyIndex)
			throws InterruptedException, ExecutionException;
	
	
	/**
	 * @param regCenterId      - registration center id
	 * @param keyIndex         - registration client TPM EK public key SHA256 
	 * @param lastUpdated      - last updated time stamp
	 * @param currentTimestamp - current time stamp
	 * @return {@link SyncDataResponseDto}
	 * @throws InterruptedException - this method will throw execution exception
	 * @throws ExecutionException   -this method will throw interrupted exception
	 */	
	SyncDataResponseDto syncClientSettings(String regCenterId, String keyIndex,
			LocalDateTime lastUpdated, LocalDateTime currentTimestamp)
			throws InterruptedException, ExecutionException;

	/**
	 * Upload a public key to identify a machine
	 * 
	 * @param uploadPublicKeyRequestDto {@link UploadPublicKeyRequestDto} inatance
	 * @return {@link UploadPublicKeyResponseDto} instance
	 */
	@Deprecated
	UploadPublicKeyResponseDto uploadpublickey(UploadPublicKeyRequestDto uploadPublicKeyRequestDto);
	
	/**
	 * Verifies machine name and public key mapping in machine_master table
	 * @param dto {@link UploadPublicKeyRequestDto}
	 * @return {@link UploadPublicKeyResponseDto}
	 */
	UploadPublicKeyResponseDto validateKeyMachineMapping(UploadPublicKeyRequestDto dto);
	
	/**
	 * fetches latest published Identity schema from masterdata-service
	 * @param lastUpdated
	 * @return
	 */
	IdSchemaDto getLatestPublishedIdSchema(LocalDateTime lastUpdated, double schemaVersion);

	KeyPairGenerateResponseDto getCertificate(String applicationId, Optional<String> referenceId);
}
