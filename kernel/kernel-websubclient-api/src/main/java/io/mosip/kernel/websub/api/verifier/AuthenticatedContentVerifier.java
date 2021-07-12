package io.mosip.kernel.websub.api.verifier;

import java.io.IOException;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.xml.bind.DatatypeConverter;

import org.bouncycastle.crypto.digests.SHA1Digest;
import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.crypto.digests.SHA384Digest;
import org.bouncycastle.crypto.digests.SHA512Digest;
import org.bouncycastle.crypto.macs.HMac;
import org.bouncycastle.crypto.params.KeyParameter;
import org.springframework.stereotype.Component;

import io.mosip.kernel.websub.api.constants.WebSubClientConstants;
import io.mosip.kernel.websub.api.constants.WebSubClientErrorCode;
import io.mosip.kernel.websub.api.exception.WebSubClientException;

/**
 * This is a helper class to authenticate content when payload is pushed by hub
 * after update from publisher according to
 * <a href="https://www.w3.org/TR/websub/#signing-content">WebSub Specs</a> Note
 * that content only needed to be authenticated if we have passed secret in
 * subscribe request.
 * 
 * 
 * @author Urvil Joshi
 *
 */
//@Component
public class AuthenticatedContentVerifier {

	private static final String METHOD_SIGNATURE_SPLITTER = "=";

	/**
	 * This method retrive body and signature header from request and used
	 * {@link #isContentVerified(String, String, String)} to authenticate the content.
	 * 
	 * @param httpServletRequest request for delivery of payload.
	 * @param secret             secret send while subscribe operation.
	 * @return authentication result in boolean.
	 */
	public boolean verifyAuthorizedContentVerified(HttpServletRequest httpServletRequest, String secret) {

		String body = null;
		try {
			body = httpServletRequest.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
		} catch (IOException exception) {
			throw new WebSubClientException(WebSubClientErrorCode.IO_ERROR.getErrorCode(),
					WebSubClientErrorCode.IO_ERROR.getErrorMessage().concat(exception.getMessage()));
		}

		String hubSignature = httpServletRequest.getHeader(WebSubClientConstants.HUB_AUTHENTICATED_CONTENT_HEADER);
		return isContentVerified(secret, body, hubSignature);

	}

	/**
	 * This method authenticates the content.
	 * 
	 * @param secret       secret used while subscribe operation.
	 * @param body         payload sent by hub.
	 * @param hubSignature signature header sent by hub.
	 * @return authentication result in boolean.
	 */
	public boolean isContentVerified(String secret, String body, String hubSignature) {
		if (hubSignature != null && !hubSignature.isEmpty()) {
			String[] signatureSplit = hubSignature.split(METHOD_SIGNATURE_SPLITTER);
			String method = signatureSplit[0];
			String signature = signatureSplit[1];
			HMac hMac = null;
			switch (method) {
			case "SHA1":
				hMac = new HMac(new SHA1Digest());
				break;
			case "SHA256":
				hMac = new HMac(new SHA256Digest());
				break;
			case "SHA384":
				hMac = new HMac(new SHA384Digest());
				break;
			case "SHA512":
				hMac = new HMac(new SHA512Digest());
				break;
			default:
				hMac = new HMac(new SHA256Digest());
				break;
			}
			KeyParameter params = new KeyParameter(secret.getBytes());
			hMac.init(params);
			hMac.update(body.getBytes(), 0, body.getBytes().length);
			byte[] result = new byte[hMac.getMacSize()];
			hMac.doFinal(result, 0);
			String expectedHash = DatatypeConverter.printHexBinary(result).toLowerCase();
			return expectedHash.equals(signature);
		} else {
			throw new WebSubClientException(
					WebSubClientErrorCode.AUTHENTTICATED_CONTENT_VERIFICATION_HEADER_ERROR.getErrorCode(),
					WebSubClientErrorCode.AUTHENTTICATED_CONTENT_VERIFICATION_HEADER_ERROR.getErrorMessage());
		}
	}

}
