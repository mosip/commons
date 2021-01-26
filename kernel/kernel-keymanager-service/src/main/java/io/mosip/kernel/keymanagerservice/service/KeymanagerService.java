package io.mosip.kernel.keymanagerservice.service;

import java.util.Optional;

import io.mosip.kernel.keymanagerservice.dto.CSRGenerateRequestDto;
import io.mosip.kernel.keymanagerservice.dto.KeyPairGenerateRequestDto;
import io.mosip.kernel.keymanagerservice.dto.KeyPairGenerateResponseDto;
import io.mosip.kernel.keymanagerservice.dto.PublicKeyResponse;
import io.mosip.kernel.keymanagerservice.dto.SignatureCertificate;
import io.mosip.kernel.keymanagerservice.dto.SymmetricKeyGenerateRequestDto;
import io.mosip.kernel.keymanagerservice.dto.SymmetricKeyGenerateResponseDto;
import io.mosip.kernel.keymanagerservice.dto.SymmetricKeyRequestDto;
import io.mosip.kernel.keymanagerservice.dto.SymmetricKeyResponseDto;
import io.mosip.kernel.keymanagerservice.dto.UploadCertificateRequestDto;
import io.mosip.kernel.keymanagerservice.dto.UploadCertificateResponseDto;

/**
 * This interface provides the methods which can be used for Key management
 * 
 * @author Dharmesh Khandelwal
 * @author Urvil Joshi
 * @since 1.0.0
 *
 */
public interface KeymanagerService {

	/**
	 * Function to decrypt symmetric key
	 * 
	 * @param symmetricKeyRequestDto symmetricKeyRequestDto
	 * @return {@link SymmetricKeyResponseDto} instance
	 */
	public SymmetricKeyResponseDto decryptSymmetricKey(SymmetricKeyRequestDto symmetricKeyRequestDto);

	/**
	 * Function to get public key
	 * 
	 * @param applicationId applicationId
	 * @param timeStamp     timeStamp
	 * @param referenceId   referenceId
	 * @return {@link PublicKeyResponse} instance
	 */
	//public PublicKeyResponse<String> getPublicKey(String applicationId, String timeStamp, Optional<String> referenceId);

	public PublicKeyResponse<String> getSignPublicKey(String applicationId, String timeStamp,
			Optional<String> referenceId);

	public SignatureCertificate getSignatureCertificate(String applicationId, Optional<String> referenceId,
													String timestamp);

	/**
	 * Function to generate Master key pair in the HSM specified in config.
	 * 
	 * @param KeyPairGenerateRequestDto request
	 * @return {@link KeyPairGenerateResponseDto} instance
	 */
	public KeyPairGenerateResponseDto generateMasterKey(String objectType, KeyPairGenerateRequestDto request);

	/**
	 * Function to get certificate for the provided appId & refId.
	 * 
	 * @param Application ID  appId
	 * @param Reference ID  refId
	 * @return {@link KeyPairGenerateResponseDto} instance
	 */
	public KeyPairGenerateResponseDto getCertificate(String appId, Optional<String> refId);

	/**
	 * Function to generate CSR for the provided appId & refId.
	 * 
	 * @param CSRGenerateRequestDto request
	 * @return {@link CSRGenerateRequestDto} instance
	 */
	public KeyPairGenerateResponseDto generateCSR(CSRGenerateRequestDto csrGenRequestDto);

	/**
	 * Function to upload certificate for the provided appId & refId.
	 * 
	 * @param UploadCertificateRequestDto request
	 * @return {@link UploadCertificateResponseDto} instance
	 */
	public UploadCertificateResponseDto uploadCertificate(UploadCertificateRequestDto uploadCertRequestDto);

	/**
	 * Function to upload other domain certificate for the provided appId & refId.
	 * 
	 * @param UploadCertificateRequestDto request
	 * @return {@link UploadCertificateResponseDto} instance
	 */
	public UploadCertificateResponseDto uploadOtherDomainCertificate(UploadCertificateRequestDto uploadCertRequestDto);


	/**
	 * Function to generate Symmetric key for the provided appId & refId.
	 * 
	 * @param SymmetricKeyGenerateRequestDto symGenRequestDto
	 * @return {@link SymmetricKeyGenerateRequestDto} instance
	 */
	public SymmetricKeyGenerateResponseDto generateSymmetricKey(SymmetricKeyGenerateRequestDto symGenRequestDto);

	/**
	 * Check certificate exists for the provided appId & refId.
	 * 
	 * @param UploadCertificateRequestDto uploadCertRequestDto
	 * @return {@link UploadCertificateResponseDto} instance
	 */
	//public UploadCertificateResponseDto isCertificateExists(UploadCertificateRequestDto uploadCertRequestDto);

}

