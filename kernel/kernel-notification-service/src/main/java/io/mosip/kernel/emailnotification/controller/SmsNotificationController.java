package io.mosip.kernel.emailnotification.controller;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import io.mosip.kernel.core.http.RequestWrapper;
import io.mosip.kernel.core.http.ResponseFilter;
import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.kernel.core.notification.model.SMSResponseDto;
import io.mosip.kernel.emailnotification.dto.SmsRequestDto;
import io.mosip.kernel.emailnotification.service.SmsNotification;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * This controller class receives contact number and message in data transfer
 * object and sends SMS on the provided contact number.
 * 
 * @author Ritesh Sinha
 * @since 1.0.0
 */

@CrossOrigin
@RestController
@Tag(name = "smsnotification", description = "Operation related to sms notification")
public class SmsNotificationController {

	/**
	 * The reference that autowire sms notification service class.
	 */
	@Autowired
	SmsNotification smsNotifierService;

	/**
	 * This method sends sms to the contact number provided.
	 * 
	 * @param smsRequestDto the request dto for sms-notification.
	 * @return the status and message as dto response.
	 */
	//@PreAuthorize("hasAnyRole('INDIVIDUAL','REGISTRATION_PROCESSOR','REGISTRATION_ADMIN','REGISTRATION_SUPERVISOR','REGISTRATION_OFFICER','ID_AUTHENTICATION','AUTH', 'PRE_REGISTRATION_ADMIN','PRE_REGISTRATION_ADMIN','RESIDENT')")
	@ResponseFilter
	@Operation(summary = "Endpoint for sending a sms", description = "Endpoint for sending a sms", tags = { "smsnotification" })
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Success or you may find errors in error array in response"),
			@ApiResponse(responseCode = "401", description = "Unauthorized" ,content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden" ,content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "404", description = "Not Found" ,content = @Content(schema = @Schema(hidden = true)))})
	@PreAuthorize("hasAnyRole(@authorizedRoles.getPostsmssend())")
	@PostMapping(value = "/sms/send")
	public ResponseWrapper<SMSResponseDto> sendSmsNotification(
			@Valid @RequestBody RequestWrapper<SmsRequestDto> smsRequestDto) {
		ResponseWrapper<SMSResponseDto> responseWrapper = new ResponseWrapper<>();
		responseWrapper.setResponse(smsNotifierService.sendSmsNotification(smsRequestDto.getRequest().getNumber(),
				smsRequestDto.getRequest().getMessage()));
		return responseWrapper;
	}
}
