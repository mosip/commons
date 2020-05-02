package io.mosip.kernel.signature.service.impl;

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Optional;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.mosip.kernel.core.crypto.spi.CryptoCoreSpec;
import io.mosip.kernel.core.signatureutil.model.SignatureResponse;
import io.mosip.kernel.core.signatureutil.spi.SignatureUtil;
import io.mosip.kernel.core.util.CryptoUtil;
import io.mosip.kernel.core.util.DateUtils;
import io.mosip.kernel.keymanagerservice.dto.PublicKeyResponse;
import io.mosip.kernel.keymanagerservice.dto.SignatureRequestDto;
import io.mosip.kernel.keymanagerservice.dto.SignatureResponseDto;
import io.mosip.kernel.keymanagerservice.service.KeymanagerService;
import io.mosip.kernel.signature.constant.SignatureConstant;
import io.mosip.kernel.signature.constant.SignatureErrorCode;
import io.mosip.kernel.signature.dto.PublicKeyRequestDto;
import io.mosip.kernel.signature.dto.SignRequestDto;
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

	@Override
	public SignatureResponse sign(SignRequestDto signRequestDto) {
		SignatureRequestDto signatureRequestDto = new SignatureRequestDto();
		signatureRequestDto.setApplicationId(signApplicationid);
		signatureRequestDto.setReferenceId(signRefid);
		signatureRequestDto.setData(signRequestDto.getData());
		String timestamp=DateUtils.getUTCCurrentDateTimeString();
		signatureRequestDto.setTimeStamp(timestamp);
		SignatureResponseDto signatureResponseDTO=keymanagerService.sign(signatureRequestDto);
		return new SignatureResponse(signatureResponseDTO.getData(), DateUtils.convertUTCToLocalDateTime(timestamp));
	}

	@Override
	public ValidatorResponseDto validate(TimestampRequestDto timestampRequestDto) {

		PublicKeyResponse<String> publicKeyResponse=keymanagerService.getSignPublicKey(signApplicationid, DateUtils.formatToISOString(timestampRequestDto.getTimestamp()), Optional.of(signRefid));
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

}
