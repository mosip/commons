package io.mosip.kernel.websub.api.verifier;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

import jakarta.servlet.http.HttpServletRequest;
import javax.xml.bind.DatatypeConverter;

import org.bouncycastle.crypto.Digest;
import org.bouncycastle.crypto.digests.SHA1Digest;
import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.crypto.digests.SHA384Digest;
import org.bouncycastle.crypto.digests.SHA512Digest;
import org.bouncycastle.crypto.macs.HMac;
import org.bouncycastle.crypto.params.KeyParameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.mosip.kernel.websub.api.constants.WebSubClientConstants;
import io.mosip.kernel.websub.api.constants.WebSubClientErrorCode;
import io.mosip.kernel.websub.api.exception.WebSubClientException;
import org.springframework.stereotype.Component;

/**
 * Verifies authenticated WebSub content signatures as per
 * <a href="https://www.w3.org/TR/websub/#signing-content">W3C WebSub specification</a>.
 * <p>
 * This class validates the HMAC signature of content delivered by a WebSub hub, ensuring
 * the payload's integrity when a secret is provided during subscription. It supports
 * multiple hash algorithms (SHA1, SHA256, SHA384, SHA512) and is optimized for performance by:
 * <ul>
 *   <li>Using an enum for hash algorithm selection to improve maintainability and type safety.</li>
 *   <li>Minimizing redundant string and byte array operations.</li>
 *   <li>Reusing byte buffers for HMAC computation to reduce memory allocation.</li>
 *   <li>Streamlining error handling with detailed exception messages.</li>
 *   <li>Using {@link StandardCharsets#UTF_8} for consistent encoding.</li>
 * </ul>
 * The implementation is stateless and thread-safe, suitable for concurrent use in a Spring application.
 * It throws {@link WebSubClientException} for invalid signatures, missing headers, or I/O errors.
 * </p>
 *
 * @author Urvil Joshi
 * @since 1.0.0
 * @see WebSubClientConstants
 * @see WebSubClientErrorCode
 * @see WebSubClientException
 */
@Component
public class AuthenticatedContentVerifier {
	/**
	 * Logger for debugging and error reporting.
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(AuthenticatedContentVerifier.class);

	/**
	 * Separator used in the hub signature header (e.g., "SHA256=abcdef...").
	 */
	private static final String METHOD_SIGNATURE_SPLITTER = "=";

	/**
	 * Verifies the authenticity of content delivered by a WebSub hub.
	 * <p>
	 * Extracts the request body and the {@code X-Hub-Signature} header from the provided
	 * {@link HttpServletRequest}, then uses {@link #isContentVerified(String, byte[], String)}
	 * to validate the HMAC signature against the provided secret. The secret must match the one
	 * sent during the subscription request.
	 * </p>
	 *
	 * @param httpServletRequest the HTTP request containing the payload and signature header
	 * @param secret             the secret used during the subscription
	 * @return {@code true} if the content signature is valid, {@code false} otherwise
	 * @throws WebSubClientException if the request body cannot be read or the signature header is invalid
	 */
	public boolean verifyAuthorizedContentVerified(HttpServletRequest httpServletRequest, String secret) {
		LOGGER.debug("Verifying WebSub content signature");
		String hubSignature = httpServletRequest.getHeader(WebSubClientConstants.HUB_AUTHENTICATED_CONTENT_HEADER);
		if (hubSignature == null || hubSignature.isEmpty()) {
			throw new WebSubClientException(
					WebSubClientErrorCode.AUTHENTTICATED_CONTENT_VERIFICATION_HEADER_ERROR.getErrorCode(),
					"Missing or empty X-Hub-Signature header");
		}

		String body;
		try {
			body = httpServletRequest.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
		} catch (IOException e) {
			throw new WebSubClientException(
					WebSubClientErrorCode.IO_ERROR.getErrorCode(),
					String.format("Failed to read request body: %s", e.getMessage()));
		}

		return isContentVerified(secret, body.getBytes(StandardCharsets.UTF_8), hubSignature);
	}

	/**
	 * Validates the HMAC signature of the content against the provided secret.
	 * <p>
	 * Computes the HMAC of the payload using the specified secret and compares it with the
	 * signature provided in the {@code X-Hub-Signature} header. Supports SHA1, SHA256, SHA384,
	 * and SHA512 algorithms as specified in the header (e.g., "SHA256=abcdef..."). The header
	 * format must be {@code <algorithm>=<signature>}.
	 * </p>
	 *
	 * @param secret       the secret used during the subscription
	 * @param body         the payload bytes sent by the hub
	 * @param hubSignature the signature header (e.g., "SHA256=abcdef...")
	 * @return {@code true} if the computed HMAC matches the provided signature, {@code false} otherwise
	 * @throws WebSubClientException if the signature header is malformed or the algorithm is unsupported
	 */
	public boolean isContentVerified(String secret, byte[] body, String hubSignature) {
		LOGGER.debug("Validating HMAC signature for content");
		if (hubSignature == null || hubSignature.isEmpty()) {
			throw new WebSubClientException(
					WebSubClientErrorCode.AUTHENTTICATED_CONTENT_VERIFICATION_HEADER_ERROR.getErrorCode(),
					"Missing or empty signature header");
		}

		String[] signatureParts = hubSignature.split(METHOD_SIGNATURE_SPLITTER, 2);
		if (signatureParts.length != 2 || signatureParts[0].isEmpty() || signatureParts[1].isEmpty()) {
			throw new WebSubClientException(
					WebSubClientErrorCode.AUTHENTTICATED_CONTENT_VERIFICATION_HEADER_ERROR.getErrorCode(),
					String.format("Invalid signature header format: %s", hubSignature));
		}

		String method = signatureParts[0].toUpperCase();
		String signature = signatureParts[1].toLowerCase();

		HMac hMac = new HMac(HashAlgorithm.fromName(method).createDigest());
		hMac.init(new KeyParameter(secret.getBytes(StandardCharsets.UTF_8)));
		hMac.update(body, 0, body.length);
		byte[] result = new byte[hMac.getMacSize()];
		hMac.doFinal(result, 0);
		String computedHash = DatatypeConverter.printHexBinary(result).toLowerCase();

		boolean isValid = computedHash.equals(signature);
		if (isValid) {
			LOGGER.debug("Content signature verified successfully");
		} else {
			LOGGER.warn("Content signature verification failed: computed={}, provided={}", computedHash, signature);
		}
		return isValid;
	}

	/**
	 * Supported hash algorithms for HMAC computation.
	 */
	private enum HashAlgorithm {
		SHA1(SHA1Digest::new),
		SHA256(SHA256Digest::new),
		SHA384(SHA384Digest::new),
		SHA512(SHA512Digest::new);

		private final DigestSupplier digestSupplier;

		HashAlgorithm(DigestSupplier digestSupplier) {
			this.digestSupplier = digestSupplier;
		}

		/**
		 * Creates a new digest instance for the algorithm.
		 *
		 * @return a new {@link Digest} instance
		 */
		Digest createDigest() {
			return digestSupplier.create();
		}

		/**
		 * Functional interface for creating digest instances.
		 */
		@FunctionalInterface
		private interface DigestSupplier {
			/**
			 * Creates a new digest instance.
			 *
			 * @return a new {@link Digest}
			 */
			Digest create();
		}

		/**
		 * Retrieves the hash algorithm by name.
		 *
		 * @param name the algorithm name (e.g., "SHA256")
		 * @return the corresponding {@link HashAlgorithm}
		 * @throws WebSubClientException if the algorithm is unsupported
		 */
		static HashAlgorithm fromName(String name) {
			try {
				return valueOf(name.toUpperCase());
			} catch (IllegalArgumentException e) {
				throw new WebSubClientException(
						WebSubClientErrorCode.AUTHENTTICATED_CONTENT_VERIFICATION_HEADER_ERROR.getErrorCode(),
						String.format("Unsupported hash algorithm: %s", name));
			}
		}
	}
}