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
 * Controller class for sending mail.
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
	EmailNotification<MultipartFile[], ResponseDto> emailNotificationService;

	/**
	 * @param mailTo      array of email id's, to which mail should be sent.
	 * @param mailCc      array of email id's, to which the email should be sent as
	 *                    carbon copy.
	 * @param mailSubject the subject.
	 * @param mailContent the content.
	 * @param attachments the attachments.
	 * @return the dto response.
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
		return responseWrapper;
	}
}
