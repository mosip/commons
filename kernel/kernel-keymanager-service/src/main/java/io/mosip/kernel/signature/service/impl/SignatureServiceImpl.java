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
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import java.util.Optional;

import javax.crypto.SecretKey;

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
import io.mosip.kernel.keymanagerservice.constant.KeymanagerConstant;
import io.mosip.kernel.keymanagerservice.constant.KeymanagerErrorConstant;
import io.mosip.kernel.keymanagerservice.dto.PublicKeyResponse;
import io.mosip.kernel.keymanagerservice.dto.SignatureCertificate;
import io.mosip.kernel.keymanagerservice.exception.KeymanagerServiceException;
import io.mosip.kernel.keymanagerservice.logger.KeymanagerLogger;
import io.mosip.kernel.keymanagerservice.service.KeymanagerService;
import io.mosip.kernel.keymanagerservice.util.KeymanagerUtil;
import io.mosip.kernel.signature.constant.SignatureConstant;
import io.mosip.kernel.signature.constant.SignatureErrorCode;
import io.mosip.kernel.signature.dto.PDFSignatureRequestDto;
import io.mosip.kernel.signature.dto.SignRequestDto;
import io.mosip.kernel.signature.dto.SignatureRequestDto;
import io.mosip.kernel.signature.dto.SignatureResponseDto;
import io.mosip.kernel.signature.dto.TimestampRequestDto;
import io.mosip.kernel.signature.dto.ValidatorResponseDto;
import io.mosip.kernel.signature.exception.PublicKeyParseException;
import io.mosip.kernel.signature.exception.SignatureFailureException;
import io.mosip.kernel.signature.service.SignatureService;

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

	/**
	 * Utility to generate Metadata
	 */
	@Autowired
	KeymanagerUtil keymanagerUtil;

	@Autowired
	private PDFGenerator pdfGenerator;

	@Override
	public SignatureResponse sign(SignRequestDto signRequestDto) {
		SignatureRequestDto signatureRequestDto = new SignatureRequestDto();
		signatureRequestDto.setApplicationId(signApplicationid);
		signatureRequestDto.setReferenceId(signRefid);
		signatureRequestDto.setData(signRequestDto.getData());
		String timestamp=DateUtils.getUTCCurrentDateTimeString();
		signatureRequestDto.setTimeStamp(timestamp);
		SignatureResponseDto signatureResponseDTO = sign(signatureRequestDto);
		return new SignatureResponse(signatureResponseDTO.getData(), DateUtils.convertUTCToLocalDateTime(timestamp));
	}

	
	private SignatureResponseDto sign(SignatureRequestDto signatureRequestDto) {
		SignatureCertificate certificateResponse = keymanagerService.getSignatureCertificate (signatureRequestDto.getApplicationId(),
				Optional.of(signatureRequestDto.getReferenceId()), signatureRequestDto.getTimeStamp());
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
			status = cryptoCore.verifySignature(timestampRequestDto.getData().getBytes(), timestampRequestDto.getSignature(), publicKey);
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
		SignatureCertificate signatureCertificate = keymanagerService.getSignatureCertificate(request.getApplicationId(),
				Optional.of(request.getReferenceId()), request.getTimeStamp());
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

}
