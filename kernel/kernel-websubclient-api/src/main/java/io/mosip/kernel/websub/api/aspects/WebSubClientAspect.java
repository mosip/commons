package io.mosip.kernel.websub.api.aspects;

import jakarta.servlet.http.HttpServletRequest;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.EmbeddedValueResolverAware;
import org.springframework.util.StringValueResolver;
import org.springframework.util.StringUtils;

import io.mosip.kernel.websub.api.annotation.PreAuthenticateContentAndVerifyIntent;
import io.mosip.kernel.websub.api.constants.WebSubClientErrorCode;
import io.mosip.kernel.websub.api.exception.WebSubClientException;
import io.mosip.kernel.websub.api.verifier.AuthenticatedContentVerifier;

/**
 * Aspect for pre-authenticating WebSub content notifications with high performance.
 * <p>
 * This aspect intercepts methods annotated with {@link PreAuthenticateContentAndVerifyIntent} to verify the authenticity
 * of content notified by a WebSub hub. It resolves the secret (either directly or via Spring's property resolution) and
 * uses {@link AuthenticatedContentVerifier} to validate the request's content signature (e.g., HMAC-SHA256). The implementation
 * is optimized for low latency by:
 * <ul>
 *   <li>Performing early validation of the request type</li>
 *   <li>Minimizing string operations for secret resolution</li>
 *   <li>Using efficient null/empty checks with {@link StringUtils#hasText}</li>
 *   <li>Avoiding unnecessary logging or object creation</li>
 * </ul>
 * The aspect is thread-safe due to Spring's singleton scope and immutable dependencies. It throws
 * {@link WebSubClientException} for invalid requests, missing secrets, or failed verification, ensuring fast failure
 * for misconfigured or unauthorized requests.
 * </p>
 * <p>
 * <strong>Usage Example:</strong>
 * <pre>
 * {@code
 * @PreAuthenticateContentAndVerifyIntent(secret = "${websub.secret}", topic = "my-topic", callback = "/callback")
 * public void handleWebSubCallback(HttpServletRequest request) {
 *     // Handle WebSub notification
 * }
 * }
 * </pre>
 *
 * @author Urvil Joshi
 * @since 1.0.0
 * @see PreAuthenticateContentAndVerifyIntent
 * @see AuthenticatedContentVerifier
 * @see WebSubClientException
 */
@Aspect
public class WebSubClientAspect implements EmbeddedValueResolverAware {

	/**
	 * Resolver for Spring property placeholders (e.g., <code>${my.secret}</code>).
	 * <p>
	 * This field stores the {@link StringValueResolver} injected by Spring to resolve
	 * property placeholders in the {@link PreAuthenticateContentAndVerifyIntent} annotation's
	 * secret attribute. It is immutable after initialization and thread-safe for use in
	 * aspect methods.
	 * </p>
	 */
	private StringValueResolver resolver = null;

	/**
	 * Verifier for authenticating WebSub content signatures.
	 * <p>
	 * This dependency is autowired by Spring and provides the logic to validate the
	 * HMAC-SHA256 signature of the WebSub notification content against the provided secret.
	 * </p>
	 */
	@Autowired
	private AuthenticatedContentVerifier authenticatedContentVerifier;

	/**
	 * HTTP request for accessing headers and body during verification.
	 * <p>
	 * This dependency is autowired by Spring and provides access to the incoming
	 * {@link HttpServletRequest} for extracting headers and content body during
	 * signature verification. It is required for proper functioning of the aspect.
	 * </p>
	 */
	@Autowired(required = true)
	private HttpServletRequest request;

	/**
	 * Sets the Spring property resolver for resolving secret placeholders.
	 * <p>
	 * This method is called by Spring to inject a {@link StringValueResolver} for resolving
	 * property placeholders (e.g., <code>${my.secret}</code>) in the {@link PreAuthenticateContentAndVerifyIntent}
	 * annotation's secret attribute. The resolver is stored immutably for thread-safe access.
	 * </p>
	 *
	 * @param resolver the Spring property resolver to set
	 */
	@Override
	public void setEmbeddedValueResolver(StringValueResolver resolver) {
		this.resolver = resolver;
	}

	/**
	 * Aspect method for pre-authenticating WebSub content notifications before method execution.
	 * <p>
	 * This method intercepts methods annotated with {@link PreAuthenticateContentAndVerifyIntent},
	 * validates that the request is an {@link HttpServletRequest}, resolves the secret (either directly
	 * or via Spring property placeholder), and verifies the request's content using
	 * {@link AuthenticatedContentVerifier}. It throws a {@link WebSubClientException} in the following cases:
	 * <ul>
	 *   <li>The request is not an {@link HttpServletRequest}</li>
	 *   <li>The secret is null or empty after resolution</li>
	 *   <li>Content verification fails (e.g., invalid HMAC signature)</li>
	 * </ul>
	 * The implementation is optimized for performance by:
	 * <ul>
	 *   <li>Validating the request type early to fail fast</li>
	 *   <li>Minimizing string operations during secret resolution</li>
	 *   <li>Using {@link StringUtils#hasText} for efficient null/empty checks</li>
	 * </ul>
	 * </p>
	 *
	 * @param preAuthenticateContent the {@link PreAuthenticateContentAndVerifyIntent} annotation
	 *                               providing secret, topic, and callback metadata
	 * @throws WebSubClientException if request validation, secret resolution, or content verification fails
	 */
	@Before("@annotation(preAuthenticateContent)")
	public void preAuthenticateContent(PreAuthenticateContentAndVerifyIntent preAuthenticateContent) {
		// Early validation: Fast-fail for non-HTTP requests
		if (!(request instanceof HttpServletRequest)) {
			throw new WebSubClientException(WebSubClientErrorCode.INSTANCE_ERROR.getErrorCode(),
					WebSubClientErrorCode.INSTANCE_ERROR.getErrorMessage());
		}

		// Resolve secret efficiently
		String secret = preAuthenticateContent.secret();
		if (secret.startsWith("${") && secret.endsWith("}")) {
			secret = resolver != null ? resolver.resolveStringValue(secret) : secret;
		}

		// Validate secret
		if (!StringUtils.hasText(secret)) {
			throw new WebSubClientException(
					WebSubClientErrorCode.AUTHENTTICATED_CONTENT_ANNOTATION_SECRET_ERROR.getErrorCode(),
					WebSubClientErrorCode.AUTHENTTICATED_CONTENT_ANNOTATION_SECRET_ERROR.getErrorMessage());
		}

		// Verify content
		if (!authenticatedContentVerifier.verifyAuthorizedContentVerified(request, secret)) {
			throw new WebSubClientException(WebSubClientErrorCode.AUTHENTTICATED_CONTENT_ERROR.getErrorCode(),
					WebSubClientErrorCode.AUTHENTTICATED_CONTENT_ERROR.getErrorMessage());
		}
	}
}