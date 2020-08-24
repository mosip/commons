package io.mosip.kernel.signature.service;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import io.mosip.kernel.core.signatureutil.model.SignatureResponse;
import io.mosip.kernel.signature.dto.PDFSignatureRequestDto;
import io.mosip.kernel.signature.dto.SignRequestDto;
import io.mosip.kernel.signature.dto.SignatureResponseDto;
import io.mosip.kernel.signature.dto.TimestampRequestDto;
import io.mosip.kernel.signature.dto.ValidatorResponseDto;

public interface SignatureService {
	/**
	 * Validate signature
	 * 
	 * @param timestampRequestDto {@link TimestampRequestDto}
	 * @return {@link ValidatorResponseDto}
	 */
	public ValidatorResponseDto validate(TimestampRequestDto timestampRequestDto);

	/**
	 * Sign Data.
	 *
	 * @param signRequestDto the signRequestDto
	 * @return the SignatureResponse
	 */

	public SignatureResponse sign(SignRequestDto signRequestDto);


	public SignatureResponseDto signPDF(PDFSignatureRequestDto request);

}
