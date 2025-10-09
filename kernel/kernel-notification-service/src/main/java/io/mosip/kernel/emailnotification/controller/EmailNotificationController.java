package io.mosip.kernel.emailnotification.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import io.mosip.kernel.core.http.ResponseFilter;
import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.kernel.core.notification.spi.EmailNotification;
import io.mosip.kernel.emailnotification.dto.ResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * <h1>Email Notification Controller</h1>
 *
 * <p>This controller exposes REST endpoints for sending emails with support for:
 * <ul>
 *     <li>Multiple recipients (TO, CC)</li>
 *     <li>Attachments</li>
 *     <li>Async email delivery for high throughput</li>
 * </ul>
 *
 * @author Sagar Mahapatra
 * @since 1.0.0
 *
 */

@RestController
@Tag(name = "emailnotification", description = "Operation related to email notification")
public class EmailNotificationController {
	/**
	 * Autowired reference for MailNotifierService.
	 */
	@Autowired
	private EmailNotification<MultipartFile[], ResponseDto> emailNotificationService;

	/**
	 * Sends an email with optional attachments asynchronously.
	 *
	 * @param mailTo      Array of recipient email addresses (TO). Mandatory.
	 * @param mailCc      Array of CC recipient email addresses. Optional.
	 * @param mailSubject Subject line of the email. Mandatory.
	 * @param mailContent Body content of the email. Mandatory.
	 * @param attachments Files to be attached with the email. Optional.
	 * @return A response wrapper containing the delivery status.
	 */
	@ResponseFilter
	@Operation(summary = "Endpoint for sending a email", description = "Endpoint for sending a email", tags = { "emailnotification" })
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Success or you may find errors in error array in response"),
			@ApiResponse(responseCode = "401", description = "Unauthorized" ,content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden" ,content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "404", description = "Not Found" ,content = @Content(schema = @Schema(hidden = true)))})
	@PreAuthorize("hasAnyRole(@authorizedRoles.getPostemailsend())")
	@PostMapping(value = "/email/send", consumes = "multipart/form-data")
	public @ResponseBody ResponseWrapper<ResponseDto> sendEMail(String[] mailTo, String[] mailCc, String mailSubject,
			String mailContent, MultipartFile[] attachments) {
		ResponseWrapper<ResponseDto> responseWrapper = new ResponseWrapper<>();
		responseWrapper
				.setResponse(emailNotificationService.sendEmail(mailTo, mailCc, mailSubject, mailContent, attachments));
        responseWrapper.setErrors(null); // Explicitly set errors to null
        // id and version are not set as the request is multipart form-data, not a RequestWrapper
        return responseWrapper;
	}
}
