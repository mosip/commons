package io.mosip.kernel.websub.api.aspects;

import javax.servlet.http.HttpServletRequest;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.mosip.kernel.websub.api.annotation.PreAuthenticateContentAndVerifyIntent;
import io.mosip.kernel.websub.api.constants.WebSubClientErrorCode;
import io.mosip.kernel.websub.api.exception.WebSubClientException;
import io.mosip.kernel.websub.api.verifier.AuthenticatedContentVerifier;

/**
 * This class comprises of aspects used for this library. Currently it is used
 * for verifying the content notified by Hub.
 * 
 * @author Urvil Joshi
 *
 */
@Aspect
@Component
public class WebSubClientAspect {

	@Autowired
	private AuthenticatedContentVerifier authenticatedContentVerifier;

	@Autowired(required = true)
	private HttpServletRequest request;

	/**
	 * This aspect is for Preauthentication of content notified by hub.
	 * 
	 * @param preAuthenticateContent annotation used at method level for this aspect
	 *                               as a decorator and metadata provider.
	 */
	@Before("@annotation(preAuthenticateContent)")
	public void preAuthenticateContent(PreAuthenticateContentAndVerifyIntent preAuthenticateContent) {
		if (!(request instanceof HttpServletRequest)) {
			throw new WebSubClientException(WebSubClientErrorCode.INSTANCE_ERROR.getErrorCode(),
					WebSubClientErrorCode.INSTANCE_ERROR.getErrorMessage());
		}
		String secret = preAuthenticateContent.secret();
		if (!secret.trim().isEmpty()) {
			if (!authenticatedContentVerifier.verifyAuthorizedContentVerified(request, secret)) {
				throw new WebSubClientException(WebSubClientErrorCode.AUTHENTTICATED_CONTENT_ERROR.getErrorCode(),
						WebSubClientErrorCode.AUTHENTTICATED_CONTENT_ERROR.getErrorMessage());
			}
		} else {
			throw new WebSubClientException(
					WebSubClientErrorCode.AUTHENTTICATED_CONTENT_ANNOTATION_SECRET_ERROR.getErrorCode(),
					WebSubClientErrorCode.AUTHENTTICATED_CONTENT_ANNOTATION_SECRET_ERROR.getErrorMessage());
		}

	}
}