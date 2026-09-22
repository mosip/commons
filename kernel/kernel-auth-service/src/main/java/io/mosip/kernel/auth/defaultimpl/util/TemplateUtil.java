/**
 * 
 */
package io.mosip.kernel.auth.defaultimpl.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import jakarta.annotation.PostConstruct;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.mosip.kernel.auth.defaultimpl.config.MosipEnvironment;
import io.mosip.kernel.auth.defaultimpl.constant.AuthConstant;
import io.mosip.kernel.auth.defaultimpl.constant.AuthErrorCode;
import io.mosip.kernel.auth.defaultimpl.constant.OTPErrorCode;
import io.mosip.kernel.auth.defaultimpl.dto.otp.OtpTemplateDto;
import io.mosip.kernel.auth.defaultimpl.dto.otp.OtpTemplateResponseDto;
import io.mosip.kernel.auth.defaultimpl.dto.otp.email.OTPEmailTemplate;
import io.mosip.kernel.auth.defaultimpl.exception.AuthManagerException;
import io.mosip.kernel.auth.defaultimpl.exception.AuthManagerServiceException;
import io.mosip.kernel.core.authmanager.exception.AuthNException;
import io.mosip.kernel.core.authmanager.exception.AuthZException;
import io.mosip.kernel.core.authmanager.model.OtpUser;
import io.mosip.kernel.core.exception.ExceptionUtils;
import io.mosip.kernel.core.exception.ServiceError;
import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.kernel.core.templatemanager.spi.TemplateManager;
import io.mosip.kernel.templatemanager.velocity.builder.TemplateManagerBuilderImpl;

/**
 * Loads masterdata OTP email/SMS templates (kernel-core Velocity) and merges
 * the OTP value into template variables. Uses {@link DateUtils2} is not needed
 * here; templates come from kernel-core, not a separate velocity artifact.
 *
 * @author Ramadurai Pandian
 *
 */
@Component
public class TemplateUtil {

	/**
	 * Velocity template manager built after construction.
	 */
	private TemplateManager templateManager;

	/**
	 * Masterdata template API URL.
	 */
	@Autowired
	MosipEnvironment mosipEnvironment;

	/**
	 * Notification language type and primary/secondary language properties.
	 */
	@Autowired
	Environment environment;

	/**
	 * RestTemplate for masterdata template HTTP GET.
	 */
	@Qualifier("authRestTemplate")
	@Autowired
	RestTemplate restTemplate;

	/**
	 * Jackson mapper for MOSIP {@code ResponseWrapper} bodies.
	 */
	@Autowired
	private ObjectMapper mapper;

	/**
	 * Property key for notification language mode ({@code BOTH}/{@code PRIMARY}/{@code SECONDARY}).
	 */
	private static final String MOSIP_NOTIFICATION_LANGUAGE_TYPE = "mosip.notification.language-type";

	/**
	 * Property key for configured notification types (email/SMS).
	 */
	public static final String MOSIP_NOTIFICATIONTYPE = "mosip.notificationtype";

	/**
	 * Property key for primary language code.
	 */
	private static final String ENV_PRIMARY_LANGUAGE = "mosip.primary-language";

	/**
	 * Property key for secondary language code.
	 */
	private static final String ENV_SECONDARY_LANGUAGE = "mosip.secondary-language";

	/**
	 * Language mode: merge primary and secondary templates.
	 */
	private static final String BOTH = "BOTH";

	/**
	 * Language mode: primary language only.
	 */
	private static final String PRIMARY = "PRIMARY";

	/**
	 * Language mode: secondary language only.
	 */
	private static final String SECONDARY = "SECONDARY";

	/**
	 * Builds the kernel-core Velocity {@link TemplateManager}.
	 */
	@PostConstruct
	private void loadTemplateManager() {
		templateManager = new TemplateManagerBuilderImpl().build();
	}

	/**
	 * Builds an email subject and body from masterdata templates for the
	 * configured language type, merging {@code otp} into template variables.
	 *
	 * @param otp     generated OTP
	 * @param otpUser context and extra template variables
	 * @param token   internal auth token for masterdata
	 * @return email template, or {@code null} if language type is unrecognized
	 */
	public OTPEmailTemplate getEmailTemplate(String otp, OtpUser otpUser, String token) {
		OTPEmailTemplate otpEmailTemplate = null;
		String primaryLanguage = null, secondaryLanguage = null;
		if (BOTH.equals(environment.getProperty(MOSIP_NOTIFICATION_LANGUAGE_TYPE))) {
			otpEmailTemplate = new OTPEmailTemplate();
			primaryLanguage = environment.getProperty(ENV_PRIMARY_LANGUAGE);
			if (primaryLanguage == null) {
				throw new AuthManagerException(OTPErrorCode.LANGUAGENOTCONFIGURED.getErrorCode(),
						OTPErrorCode.LANGUAGENOTCONFIGURED.getErrorMessage());
			}
			secondaryLanguage = environment.getProperty(ENV_SECONDARY_LANGUAGE);
			if (secondaryLanguage == null) {
				throw new AuthManagerException(OTPErrorCode.LANGUAGENOTCONFIGURED.getErrorCode(),
						OTPErrorCode.LANGUAGENOTCONFIGURED.getErrorMessage());
			}
			String emailSubject = getEmailData(otpUser, "email-subject-template", token, primaryLanguage,
					secondaryLanguage);
			String mergedEmailSubject = null;
			if (emailSubject != null)
				mergedEmailSubject = getMergedEmailContent(otp, emailSubject, otpUser.getTemplateVariables());
			String emailContent = getEmailData(otpUser, "email-content-template", token, primaryLanguage,
					secondaryLanguage);
			String mergedEmailContent = null;
			if (emailContent != null)
				mergedEmailContent = getMergedEmailContent(otp, emailContent, otpUser.getTemplateVariables());
			otpEmailTemplate.setEmailSubject(mergedEmailSubject);
			otpEmailTemplate.setEmailContent(mergedEmailContent);
			return otpEmailTemplate;

		} else if (PRIMARY.equals(environment.getProperty(MOSIP_NOTIFICATION_LANGUAGE_TYPE))) {
			otpEmailTemplate = new OTPEmailTemplate();
			primaryLanguage = environment.getProperty(ENV_PRIMARY_LANGUAGE);
			if (primaryLanguage == null) {
				throw new AuthManagerException(OTPErrorCode.LANGUAGENOTCONFIGURED.getErrorCode(),
						OTPErrorCode.LANGUAGENOTCONFIGURED.getErrorMessage());
			}
			String emailSubject = getEmailData(otpUser, "email-subject-template", token, primaryLanguage, null);
			String mergedEmailSubject = null;
			if (emailSubject != null)
				mergedEmailSubject = getMergedEmailContent(otp, emailSubject, otpUser.getTemplateVariables());
			String emailContent = getEmailData(otpUser, "email-content-template", token, primaryLanguage, null);
			String mergedEmailContent = null;
			if (emailContent != null)
				mergedEmailContent = getMergedEmailContent(otp, emailContent, otpUser.getTemplateVariables());
			otpEmailTemplate.setEmailSubject(mergedEmailSubject);
			otpEmailTemplate.setEmailContent(mergedEmailContent);
			return otpEmailTemplate;
		} else if (SECONDARY.equals(environment.getProperty(MOSIP_NOTIFICATION_LANGUAGE_TYPE))) {
			otpEmailTemplate = new OTPEmailTemplate();
			secondaryLanguage = environment.getProperty(ENV_SECONDARY_LANGUAGE);
			if (secondaryLanguage == null) {
				throw new AuthManagerException(OTPErrorCode.LANGUAGENOTCONFIGURED.getErrorCode(),
						OTPErrorCode.LANGUAGENOTCONFIGURED.getErrorMessage());
			}
			String emailSubject = getEmailData(otpUser, "email-subject-template", token, null, secondaryLanguage);
			String mergedEmailSubject = null;
			if (emailSubject != null)
				mergedEmailSubject = getMergedEmailContent(otp, emailSubject, otpUser.getTemplateVariables());
			String emailContent = getEmailData(otpUser, "email-content-template", token, null, secondaryLanguage);
			String mergedEmailContent = null;
			if (emailContent != null)
				mergedEmailContent = getMergedEmailContent(otp, emailContent, otpUser.getTemplateVariables());
			otpEmailTemplate.setEmailSubject(mergedEmailSubject);
			otpEmailTemplate.setEmailContent(mergedEmailContent);
			return otpEmailTemplate;
		}
		return otpEmailTemplate;
	}

	/**
	 * Merges OTP (and optional extra variables) into a Velocity template string.
	 *
	 * @param otp               OTP value bound as {@code otp}
	 * @param emailContent      raw template text
	 * @param templateVariables extra variables, or {@code null} for OTP only
	 * @return merged template
	 */
	private String getMergedEmailContent(String otp, String emailContent, Map<String, Object> templateVariables) {
		String template = null;
		InputStream templateInputStream = new ByteArrayInputStream(emailContent.getBytes(Charset.forName("UTF-8")));
		InputStream resultedTemplate = null;
		try {
			if (templateVariables != null) {
				templateVariables.put("otp", otp);
				resultedTemplate = templateManager.merge(templateInputStream, templateVariables);
				template = IOUtils.toString(resultedTemplate, StandardCharsets.UTF_8.name());
			} else {
				Map<String, Object> templateVariable = new HashMap<>();
				templateVariable.put("otp", otp);
				resultedTemplate = templateManager.merge(templateInputStream, templateVariable);
				template = IOUtils.toString(resultedTemplate, StandardCharsets.UTF_8.name());
				return template;
			}

		} catch (IOException e) {
			throw new AuthManagerException(AuthErrorCode.RESPONSE_PARSE_ERROR.getErrorCode(), e.getMessage());
		}
		return template;
	}

	/**
	 * Fetches primary and/or secondary language template text and concatenates
	 * content (not subject) with a blank line.
	 *
	 * @param otpUser           OTP context
	 * @param templateType      masterdata template suffix (for example {@code email-subject-template})
	 * @param token             internal auth token
	 * @param primaryLanguage   primary language code, or {@code null}
	 * @param secondaryLanguage secondary language code, or {@code null}
	 * @return combined or single-language template text
	 */
	private String getEmailData(OtpUser otpUser, String templateType, String token, String primaryLanguage,
			String secondaryLanguage) {
		String emailPrimaryData = null, emailSecondaryData = null;
		String templateData = null;
		if (primaryLanguage != null) {
			emailPrimaryData = getMasterDataForLanguage(otpUser, templateType, token, primaryLanguage);
		}
		if (secondaryLanguage != null) {
			emailSecondaryData = getMasterDataForLanguage(otpUser, templateType, token, secondaryLanguage);
		}
		if (emailPrimaryData != null && emailSecondaryData != null) {
			if (!templateType.contains("subject")) {
				templateData = emailPrimaryData + "\n\n" + emailSecondaryData;
			} else {
				templateData = emailPrimaryData;
			}

		} else if (emailPrimaryData != null) {
			templateData = emailPrimaryData;
		} else if (emailSecondaryData != null) {
			templateData = emailSecondaryData;
		}
		return templateData;
	}

	/**
	 * GET masterdata template for {@code language} and {@code otpUser} context.
	 * Uses {@code HttpStatus} comparison; client/server errors use
	 * {@code getStatusCode().value()} for 401/403 mapping.
	 *
	 * @param otpUser      OTP context
	 * @param templateType template suffix, or {@code null} for context-only URL
	 * @param token        internal auth cookie token
	 * @param language     template language code
	 * @return template file text
	 */
	public String getMasterDataForLanguage(OtpUser otpUser, String templateType, String token, String language) {
		OtpTemplateResponseDto otpTemplateResponseDto = null;
		final String url;
		ResponseEntity<String> response = null;
		if (templateType != null) {
			url = mosipEnvironment.getMasterDataTemplateApi() + "/" + language + "/" + otpUser.getContext() + "-"
					+ templateType;
		} else {
			url = mosipEnvironment.getMasterDataTemplateApi() + "/" + language + "/" + otpUser.getContext();
		}

		HttpHeaders headers = new HttpHeaders();
		headers.set(AuthConstant.COOKIE, AuthConstant.AUTH_HEADER + token);
		try {
			response = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<Object>(headers), String.class);
			if (response.getStatusCode().equals(HttpStatus.OK)) {
				String responseBody = response.getBody();
				List<ServiceError> validationErrorsList = null;
				validationErrorsList = ExceptionUtils.getServiceErrorList(responseBody);
				Optional<ServiceError> service = validationErrorsList.stream()
						.filter(a -> a.getErrorCode().equals("KER-MSD-046")).findFirst();
				if (service.isPresent()) {
					throw new AuthManagerException(AuthErrorCode.TEMPLATE_ERROR.getErrorCode(),
							AuthErrorCode.TEMPLATE_ERROR.getErrorMessage() + language + "with context "
									+ otpUser.getContext() + "-" + templateType);
				}
				if (!validationErrorsList.isEmpty()) {
					throw new AuthManagerServiceException(validationErrorsList);
				}
				ResponseWrapper<?> responseObject;
				try {
					responseObject = mapper.readValue(response.getBody(), ResponseWrapper.class);
					otpTemplateResponseDto = mapper.readValue(mapper.writeValueAsString(responseObject.getResponse()),
							OtpTemplateResponseDto.class);
				} catch (Exception e) {
					throw new AuthManagerException(AuthErrorCode.SERVER_ERROR.getErrorCode(), e.getMessage(), e);
				}
			}
		} catch (HttpClientErrorException | HttpServerErrorException ex) {
			List<ServiceError> validationErrorsList = ExceptionUtils.getServiceErrorList(ex.getResponseBodyAsString());

			if (ex.getStatusCode().value() == 401) {
				if (!validationErrorsList.isEmpty()) {
					throw new AuthNException(validationErrorsList);
				} else {
					throw new BadCredentialsException("Authentication failed from Internal token services");
				}
			}
			if (ex.getStatusCode().value() == 403) {
				if (!validationErrorsList.isEmpty()) {
					throw new AuthZException(validationErrorsList);
				} else {
					throw new AccessDeniedException("Access denied from Internal token services");
				}
			}
			if (!validationErrorsList.isEmpty()) {
				throw new AuthManagerServiceException(validationErrorsList);
			} else {
				throw new AuthManagerException(AuthErrorCode.CLIENT_ERROR.getErrorCode(), ex.getMessage(), ex);
			}
		}
		String templateText = null;
		OtpTemplateDto templateDto = null;
		if (otpTemplateResponseDto != null) {
			List<OtpTemplateDto> otpTemplateList = otpTemplateResponseDto.getTemplates();
			if (otpTemplateList != null && otpTemplateList.size() > 0) {
				templateDto = otpTemplateList.get(0);
				templateText = templateDto.getFileText();
			}
		} else {
			throw new AuthManagerException(AuthErrorCode.CLIENT_ERROR.getErrorCode(), "response is null");
		}
		return templateText;
	}

	/**
	 * Builds the SMS OTP message from masterdata {@code sms-template} for the
	 * configured language type.
	 *
	 * @param otp     generated OTP
	 * @param otpUser context and extra template variables
	 * @param token   internal auth token
	 * @return merged SMS text, or {@code null} if language type is unrecognized
	 */
	public String getOtpSmsMessage(String otp, OtpUser otpUser, String token) {
		String primaryLanguage = null, secondaryLanguage = null;
		if (BOTH.equals(environment.getProperty(MOSIP_NOTIFICATION_LANGUAGE_TYPE))) {
			primaryLanguage = environment.getProperty(ENV_PRIMARY_LANGUAGE);
			if (primaryLanguage == null) {
				throw new AuthManagerException(OTPErrorCode.LANGUAGENOTCONFIGURED.getErrorCode(),
						OTPErrorCode.LANGUAGENOTCONFIGURED.getErrorMessage());
			}
			secondaryLanguage = environment.getProperty(ENV_SECONDARY_LANGUAGE);
			if (secondaryLanguage == null) {
				throw new AuthManagerException(OTPErrorCode.LANGUAGENOTCONFIGURED.getErrorCode(),
						OTPErrorCode.LANGUAGENOTCONFIGURED.getErrorMessage());
			}
			String smsMessage = getEmailData(otpUser, "sms-template", token, primaryLanguage, secondaryLanguage);

			String mergedMessage = null;
			if (smsMessage != null)
				mergedMessage = getMergedEmailContent(otp, smsMessage, otpUser.getTemplateVariables());
			return mergedMessage;

		} else if (PRIMARY.equals(environment.getProperty(MOSIP_NOTIFICATION_LANGUAGE_TYPE))) {
			primaryLanguage = environment.getProperty(ENV_PRIMARY_LANGUAGE);
			if (primaryLanguage == null) {
				throw new AuthManagerException(OTPErrorCode.LANGUAGENOTCONFIGURED.getErrorCode(),
						OTPErrorCode.LANGUAGENOTCONFIGURED.getErrorMessage());
			}
			String smsMessage = getEmailData(otpUser, "sms-template", token, primaryLanguage, secondaryLanguage);
			String mergedMessage = null;
			if (smsMessage != null)
				mergedMessage = getMergedEmailContent(otp, smsMessage, otpUser.getTemplateVariables());
			return mergedMessage;

		} else if (SECONDARY.equals(environment.getProperty(MOSIP_NOTIFICATION_LANGUAGE_TYPE))) {
			secondaryLanguage = environment.getProperty(ENV_SECONDARY_LANGUAGE);
			if (secondaryLanguage == null) {
				throw new AuthManagerException(OTPErrorCode.LANGUAGENOTCONFIGURED.getErrorCode(),
						OTPErrorCode.LANGUAGENOTCONFIGURED.getErrorMessage());
			}
			String smsMessage = getEmailData(otpUser, "sms-template", token, primaryLanguage, secondaryLanguage);
			String mergedMessage = null;
			if (smsMessage != null)
				mergedMessage = getMergedEmailContent(otp, smsMessage, otpUser.getTemplateVariables());
			return mergedMessage;
		}
		return null;
	}

}
