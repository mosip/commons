package io.mosip.kernel.signature.service.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Security;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import javax.crypto.SecretKey;

import org.apache.commons.codec.binary.Base64;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwx.CompactSerializer;
import org.jose4j.lang.JoseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;


import io.mosip.kernel.core.crypto.spi.CryptoCoreSpec;
import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.kernel.core.pdfgenerator.model.Rectangle;
import io.mosip.kernel.core.pdfgenerator.spi.PDFGenerator;
import io.mosip.kernel.core.signatureutil.model.SignatureResponse;
import io.mosip.kernel.core.util.CryptoUtil;
import io.mosip.kernel.core.util.DateUtils;
import io.mosip.kernel.core.util.JsonUtils;
import io.mosip.kernel.core.util.exception.JsonMappingException;
import io.mosip.kernel.core.util.exception.JsonParseException;
import io.mosip.kernel.keymanagerservice.constant.KeymanagerConstant;
import io.mosip.kernel.keymanagerservice.constant.KeymanagerErrorConstant;
import io.mosip.kernel.keymanagerservice.dto.KeyPairGenerateResponseDto;
import io.mosip.kernel.keymanagerservice.dto.PublicKeyResponse;
import io.mosip.kernel.keymanagerservice.dto.SignatureCertificate;
import io.mosip.kernel.keymanagerservice.exception.KeymanagerServiceException;
import io.mosip.kernel.keymanagerservice.logger.KeymanagerLogger;
import io.mosip.kernel.keymanagerservice.service.KeymanagerService;
import io.mosip.kernel.keymanagerservice.util.KeymanagerUtil;
import io.mosip.kernel.partnercertservice.dto.CertificateTrustRequestDto;
import io.mosip.kernel.partnercertservice.dto.CertificateTrustResponeDto;
import io.mosip.kernel.partnercertservice.service.spi.PartnerCertificateManagerService;
import io.mosip.kernel.signature.constant.SignatureConstant;
import io.mosip.kernel.signature.constant.SignatureErrorCode;
import io.mosip.kernel.signature.dto.JWTSignatureRequestDto;
import io.mosip.kernel.signature.dto.JWTSignatureResponseDto;
import io.mosip.kernel.signature.dto.JWTSignatureVerifyRequestDto;
import io.mosip.kernel.signature.dto.JWTSignatureVerifyResponseDto;
import io.mosip.kernel.signature.dto.PDFSignatureRequestDto;
import io.mosip.kernel.signature.dto.SignRequestDto;
import io.mosip.kernel.signature.dto.SignatureRequestDto;
import io.mosip.kernel.signature.dto.SignatureResponseDto;
import io.mosip.kernel.signature.dto.TimestampRequestDto;
import io.mosip.kernel.signature.dto.ValidatorResponseDto;
import io.mosip.kernel.signature.exception.CertificateNotValidException;
import io.mosip.kernel.signature.exception.PublicKeyParseException;
import io.mosip.kernel.signature.exception.RequestException;
import io.mosip.kernel.signature.exception.SignatureFailureException;
import io.mosip.kernel.signature.service.SignatureService;
import io.mosip.kernel.signature.util.SignatureUtil;

/**
 * @author Uday Kumar
 * @author Urvil
 *
 */
@Service
public class SignatureServiceImpl implements SignatureService {

	private static final Logger LOGGER = KeymanagerLogger.getLogger(SignatureServiceImpl.class);

	@Autowired
	private KeymanagerService keymanagerService;

	@Autowired
	private CryptoCoreSpec<byte[], byte[], SecretKey, PublicKey, PrivateKey, String> cryptoCore;

	@Value("${mosip.kernel.keygenerator.asymmetric-algorithm-name}")
	private String asymmetricAlgorithmName;

	/** The sign applicationid. */
	@Value("${mosip.sign.applicationid:KERNEL}")
	private String signApplicationid;

	/** The sign refid. */
	@Value("${mosip.sign.refid:SIGN}")
	private String signRefid;

	@Value("${mosip.kernel.crypto.sign-algorithm-name:RS256}")
	private String signAlgorithm;

	/**
	 * Utility to generate Metadata
	 */
	@Autowired
	KeymanagerUtil keymanagerUtil;

	@Autowired
	private PDFGenerator pdfGenerator;

	/**
	 * Instance for PartnerCertificateManagerService
	 */
	@Autowired
	PartnerCertificateManagerService partnerCertManagerService;


	@Override
	public SignatureResponse sign(SignRequestDto signRequestDto) {
		SignatureRequestDto signatureRequestDto = new SignatureRequestDto();
		signatureRequestDto.setApplicationId(signApplicationid);
		signatureRequestDto.setReferenceId(signRefid);
		signatureRequestDto.setData(signRequestDto.getData());
		String timestamp = DateUtils.getUTCCurrentDateTimeString();
		signatureRequestDto.setTimeStamp(timestamp);
		SignatureResponseDto signatureResponseDTO = sign(signatureRequestDto);
		return new SignatureResponse(signatureResponseDTO.getData(), DateUtils.convertUTCToLocalDateTime(timestamp));
	}

	private SignatureResponseDto sign(SignatureRequestDto signatureRequestDto) {
		SignatureCertificate certificateResponse = keymanagerService.getSignatureCertificate(
				signatureRequestDto.getApplicationId(), Optional.of(signatureRequestDto.getReferenceId()),
				signatureRequestDto.getTimeStamp());
		keymanagerUtil.isCertificateValid(certificateResponse.getCertificateEntry(),
				DateUtils.parseUTCToDate(signatureRequestDto.getTimeStamp()));
		String encryptedSignedData = null;
		if (certificateResponse.getCertificateEntry() != null) {
			encryptedSignedData = cryptoCore.sign(signatureRequestDto.getData().getBytes(),
					certificateResponse.getCertificateEntry().getPrivateKey());
		}
		return new SignatureResponseDto(encryptedSignedData);
	}

	@Override
	public ValidatorResponseDto validate(TimestampRequestDto timestampRequestDto) {

		PublicKeyResponse<String> publicKeyResponse = keymanagerService.getSignPublicKey(signApplicationid,
				DateUtils.formatToISOString(timestampRequestDto.getTimestamp()), Optional.of(signRefid));
		boolean status;
		try {
			PublicKey publicKey = KeyFactory.getInstance(asymmetricAlgorithmName)
					.generatePublic(new X509EncodedKeySpec(CryptoUtil.decodeBase64(publicKeyResponse.getPublicKey())));
			status = cryptoCore.verifySignature(timestampRequestDto.getData().getBytes(),
					timestampRequestDto.getSignature(), publicKey);
		} catch (InvalidKeySpecException | NoSuchAlgorithmException exception) {
			throw new PublicKeyParseException(SignatureErrorCode.INTERNAL_SERVER_ERROR.getErrorCode(),
					exception.getMessage(), exception);
		}

		if (status) {
			ValidatorResponseDto response = new ValidatorResponseDto();
			response.setMessage(SignatureConstant.VALIDATION_SUCCESSFUL);
			response.setStatus(SignatureConstant.SUCCESS);
			return response;
		} else {
			throw new SignatureFailureException(SignatureErrorCode.NOT_VALID.getErrorCode(),
					SignatureErrorCode.NOT_VALID.getErrorMessage(), null);
		}

	}

	@Override
	public SignatureResponseDto signPDF(PDFSignatureRequestDto request) {
		SignatureCertificate signatureCertificate = keymanagerService.getSignatureCertificate(
				request.getApplicationId(), Optional.of(request.getReferenceId()), request.getTimeStamp());
		LOGGER.debug(KeymanagerConstant.SESSIONID, KeymanagerConstant.SESSIONID, KeymanagerConstant.SESSIONID,
				"Signature fetched from hsm " + signatureCertificate);
		Rectangle rectangle = new Rectangle(request.getLowerLeftX(), request.getLowerLeftY(), request.getUpperRightX(),
				request.getUpperRightY());
		OutputStream outputStream;
		try {
			String providerName = signatureCertificate.getProviderName();
			LOGGER.info(KeymanagerConstant.SESSIONID, KeymanagerConstant.SESSIONID, KeymanagerConstant.SESSIONID,
					" Keystore Provider Name found: " + providerName);

			Arrays.stream(Security.getProviders()).forEach(x -> {
				LOGGER.info(KeymanagerConstant.SESSIONID, KeymanagerConstant.SESSIONID, KeymanagerConstant.SESSIONID,
						"provider name " + x.getName());
				LOGGER.info(KeymanagerConstant.SESSIONID, KeymanagerConstant.SESSIONID, KeymanagerConstant.SESSIONID,
						"provider info " + x.getInfo());
			});
			LOGGER.info(KeymanagerConstant.SESSIONID, KeymanagerConstant.SESSIONID, KeymanagerConstant.SESSIONID,
					"all providers ");
			outputStream = pdfGenerator.signAndEncryptPDF(CryptoUtil.decodeBase64(request.getData()), rectangle,
					request.getReason(), request.getPageNumber(), Security.getProvider(providerName),
					signatureCertificate.getCertificateEntry(), request.getPassword());
		} catch (IOException | GeneralSecurityException e) {
			throw new KeymanagerServiceException(KeymanagerErrorConstant.INTERNAL_SERVER_ERROR.getErrorCode(),
					KeymanagerErrorConstant.INTERNAL_SERVER_ERROR.getErrorMessage() + " " + e.getMessage());
		}
		SignatureResponseDto signatureResponseDto = new SignatureResponseDto();
		signatureResponseDto.setData(CryptoUtil.encodeBase64(((ByteArrayOutputStream) outputStream).toByteArray()));
		return signatureResponseDto;
	}

	@Override
	public JWTSignatureResponseDto jwtSign(JWTSignatureRequestDto jwtSignRequestDto) {
		LOGGER.info(SignatureConstant.SESSIONID, SignatureConstant.JWT_SIGN, SignatureConstant.BLANK,
				"JWT Signature Request.");

		String reqDataToSign = jwtSignRequestDto.getDataToSign();
		if (!SignatureUtil.isDataValid(reqDataToSign)) {
			LOGGER.error(SignatureConstant.SESSIONID, SignatureConstant.JWT_SIGN, SignatureConstant.BLANK,
					"Provided Data to sign value is invalid.");
			throw new RequestException(SignatureErrorCode.INVALID_INPUT.getErrorCode(),
					SignatureErrorCode.INVALID_INPUT.getErrorMessage());
		}

		String decodedDataToSign = new String(CryptoUtil.decodeBase64(reqDataToSign));
		if (!SignatureUtil.isJsonValid(decodedDataToSign)) {
			LOGGER.error(SignatureConstant.SESSIONID, SignatureConstant.JWT_SIGN, SignatureConstant.BLANK,
					"Provided Data to sign value is invalid JSON.");
			throw new RequestException(SignatureErrorCode.INVALID_JSON.getErrorCode(),
					SignatureErrorCode.INVALID_JSON.getErrorMessage());
		}

		String timestamp = DateUtils.getUTCCurrentDateTimeString();
		String applicationId = jwtSignRequestDto.getApplicationId();
		String referenceId = jwtSignRequestDto.getReferenceId();
		if (!keymanagerUtil.isValidApplicationId(applicationId)) {
			applicationId = signApplicationid;
			referenceId = signRefid;
		}

		boolean includePayload = SignatureUtil.isIncludeAttrsValid(jwtSignRequestDto.getIncludePayload());
		boolean includeCertificate = SignatureUtil.isIncludeAttrsValid(jwtSignRequestDto.getIncludeCertificate());
		boolean includeCertHash = SignatureUtil.isIncludeAttrsValid(jwtSignRequestDto.getIncludeCertHash());
		String certificateUrl = SignatureUtil.isDataValid(
								jwtSignRequestDto.getCertificateUrl()) ? jwtSignRequestDto.getCertificateUrl(): null;

		SignatureCertificate certificateResponse = keymanagerService.getSignatureCertificate(applicationId,
				Optional.of(referenceId), timestamp);
		keymanagerUtil.isCertificateValid(certificateResponse.getCertificateEntry(),
				DateUtils.parseUTCToDate(timestamp));
		String signedData = sign(decodedDataToSign, certificateResponse, includePayload, includeCertificate,
				includeCertHash, certificateUrl);
		JWTSignatureResponseDto responseDto = new JWTSignatureResponseDto();
		responseDto.setJwtSignedData(signedData);
		responseDto.setTimestamp(DateUtils.getUTCCurrentDateTime());
		return responseDto;
	}

	private String sign(String dataToSign, SignatureCertificate certificateResponse, boolean includePayload,
			boolean includeCertificate, boolean includeCertHash, String certificateUrl) {

		JsonWebSignature jwSign = new JsonWebSignature();
		PrivateKey privateKey = certificateResponse.getCertificateEntry().getPrivateKey();
		X509Certificate x509Certificate = certificateResponse.getCertificateEntry().getChain()[0];
		if (includeCertificate)
			jwSign.setCertificateChainHeaderValue(new X509Certificate[] { x509Certificate });

		if (includeCertHash)
			jwSign.setX509CertSha256ThumbprintHeaderValue(x509Certificate);

		if (Objects.nonNull(certificateUrl))
			jwSign.setHeader("x5u", certificateUrl);

		jwSign.setPayload(dataToSign);
		jwSign.setAlgorithmHeaderValue(signAlgorithm);
		jwSign.setKey(privateKey);
		jwSign.setDoKeyValidation(false);
		try {
			if (includePayload)
				return jwSign.getCompactSerialization();

			return jwSign.getDetachedContentCompactSerialization();
		} catch (JoseException e) {
			LOGGER.error(SignatureConstant.SESSIONID, SignatureConstant.JWT_SIGN, SignatureConstant.BLANK,
					"Error occurred while Signing Data.");
			throw new SignatureFailureException(SignatureErrorCode.SIGN_ERROR.getErrorCode(),
					SignatureErrorCode.SIGN_ERROR.getErrorMessage(), e);
		}
	}

	public JWTSignatureVerifyResponseDto jwtVerify(JWTSignatureVerifyRequestDto jwtVerifyRequestDto) {

		String signedData = jwtVerifyRequestDto.getJwtSignatureData();
		if (!SignatureUtil.isDataValid(signedData)) {
			LOGGER.error(SignatureConstant.SESSIONID, SignatureConstant.JWT_SIGN, SignatureConstant.BLANK,
					"Provided Signed Data value is invalid.");
			throw new RequestException(SignatureErrorCode.INVALID_INPUT.getErrorCode(),
					SignatureErrorCode.INVALID_INPUT.getErrorMessage());
		}

		String encodedActualData = SignatureUtil.isDataValid(jwtVerifyRequestDto.getActualData())
									? jwtVerifyRequestDto.getActualData() : null;

		String reqCertData = SignatureUtil.isDataValid(jwtVerifyRequestDto.getCertificateData())
				? jwtVerifyRequestDto.getCertificateData(): null;
		String applicationId = jwtVerifyRequestDto.getApplicationId();
		String referenceId = jwtVerifyRequestDto.getReferenceId();
		if (!keymanagerUtil.isValidApplicationId(applicationId)) {
			applicationId = signApplicationid;
			referenceId = signRefid;
		}

		String[] jwtTokens = signedData.split(SignatureConstant.PERIOD, -1);

		boolean signatureValid = false;
		Certificate certToVerify = certificateExistsInHeader(jwtTokens[0]);
		if (Objects.nonNull(certToVerify)){
			signatureValid = verifySignature(jwtTokens, encodedActualData, certToVerify);
		} else {
			Certificate reqCertToVerify = getCertificateToVerify(reqCertData, applicationId, referenceId);
			signatureValid = verifySignature(jwtTokens, encodedActualData, reqCertToVerify);
		}

		JWTSignatureVerifyResponseDto responseDto = new JWTSignatureVerifyResponseDto();
		responseDto.setSignatureValid(signatureValid);
		responseDto.setMessage(signatureValid ? SignatureConstant.VALIDATION_SUCCESSFUL : SignatureConstant.VALIDATION_FAILED);
		responseDto.setTrustValid(validateTrust(jwtVerifyRequestDto, certToVerify, reqCertData));
		return responseDto;
	}

	private Certificate getCertificateToVerify(String reqCertData, String applicationId, String referenceId) {
		// 2nd precedence to consider certificate to use in signature verification (Certificate Data provided in request).
		if (reqCertData != null)
			return keymanagerUtil.convertToCertificate(reqCertData);
		
		// 3rd precedence to consider certificate to use in signature verification. (based on AppId & RefId)
		KeyPairGenerateResponseDto certificateResponse = keymanagerService.getCertificate(applicationId,
				Optional.of(referenceId));
		return keymanagerUtil.convertToCertificate(certificateResponse.getCertificate());
	}
	
	@SuppressWarnings("unchecked")
	private Certificate certificateExistsInHeader(String jwtHeader) {
		String jwtTokenHeader = new String(CryptoUtil.decodeBase64(jwtHeader));
		Map<String, Object> jwtTokenHeadersMap = null;
		try {
			jwtTokenHeadersMap = JsonUtils.jsonStringToJavaMap(jwtTokenHeader);
		} catch (JsonParseException | JsonMappingException | io.mosip.kernel.core.exception.IOException e) {
			LOGGER.error(SignatureConstant.SESSIONID, SignatureConstant.JWT_SIGN, SignatureConstant.BLANK,
					"Provided Signed Data value is invalid.");
			throw new RequestException(SignatureErrorCode.INVALID_VERIFY_INPUT.getErrorCode(),
					SignatureErrorCode.INVALID_VERIFY_INPUT.getErrorMessage());
		} 
		// 1st precedence to consider certificate to use in signature verification (JWT Header).
		if (jwtTokenHeadersMap.containsKey(SignatureConstant.JWT_HEADER_CERT_KEY)) {
			LOGGER.info(SignatureConstant.SESSIONID, SignatureConstant.JWT_SIGN, SignatureConstant.BLANK,
					"Certificate found in JWT Header.");
			List<String> certList = (List<String>) jwtTokenHeadersMap.get(SignatureConstant.JWT_HEADER_CERT_KEY);
			return keymanagerUtil.convertToCertificate(Base64.decodeBase64(certList.get(0)));
		}
		LOGGER.info(SignatureConstant.SESSIONID, SignatureConstant.JWT_SIGN, SignatureConstant.BLANK,
					"Certificate not found in JWT Header.");
		return null;
	}

	private boolean verifySignature(String[] jwtTokens, String actualData, Certificate certToVerify) {
		JsonWebSignature jws = new JsonWebSignature();
		try {
			boolean validCert = SignatureUtil.isCertificateDatesValid((X509Certificate) certToVerify);
			if (!validCert) {
				LOGGER.error(SignatureConstant.SESSIONID, SignatureConstant.JWT_SIGN, SignatureConstant.BLANK,
					"Error certificate dates are not valid.");
					throw new CertificateNotValidException(SignatureErrorCode.CERT_NOT_VALID.getErrorCode(),
								SignatureErrorCode.CERT_NOT_VALID.getErrorMessage());
			}
			
			PublicKey publicKey = certToVerify.getPublicKey();
			if (Objects.nonNull(actualData))
				jwtTokens[1] = actualData;

			jws.setCompactSerialization(CompactSerializer.serialize(jwtTokens));
			if (Objects.nonNull(publicKey))
				jws.setKey(publicKey);

			return jws.verifySignature();
		} catch (ArrayIndexOutOfBoundsException | JoseException e) {
			LOGGER.error(SignatureConstant.SESSIONID, SignatureConstant.JWT_SIGN, SignatureConstant.BLANK,
					"Provided Signed Data value is invalid.");
			throw new SignatureFailureException(SignatureErrorCode.VERIFY_ERROR.getErrorCode(),
									SignatureErrorCode.VERIFY_ERROR.getErrorMessage(), e);
		}
	}

	private String validateTrust(JWTSignatureVerifyRequestDto jwtVerifyRequestDto, Certificate headerCertificate, String reqCertData) {
		
		boolean validateTrust = SignatureUtil.isIncludeAttrsValid(jwtVerifyRequestDto.getValidateTrust());
		if (!validateTrust) {
			return SignatureConstant.TRUST_NOT_VERIFIED;
		}
		
		String domain = jwtVerifyRequestDto.getDomain();
		if(!SignatureUtil.isDataValid(domain))
			return SignatureConstant.TRUST_NOT_VERIFIED_NO_DOMAIN;
		
		String certData = null;
		if (Objects.nonNull(headerCertificate)) {
			certData = keymanagerUtil.getPEMFormatedData(headerCertificate);
		}
		String trustCertData = certData == null ? reqCertData : certData;

		if (trustCertData == null) 
			return SignatureConstant.TRUST_NOT_VERIFIED;
		
		CertificateTrustRequestDto trustRequestDto = new CertificateTrustRequestDto();
		trustRequestDto.setCertificateData(trustCertData);
		trustRequestDto.setPartnerDomain(domain);
		CertificateTrustResponeDto responseDto = partnerCertManagerService.verifyCertificateTrust(trustRequestDto);
		
		if (responseDto.getStatus()){
			return SignatureConstant.TRUST_VALID;
		}
		return SignatureConstant.TRUST_NOT_VALID;
	}
}
