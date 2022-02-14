package io.mosip.kernel.otpmanager.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import io.mosip.kernel.core.http.RequestWrapper;
import io.mosip.kernel.core.http.ResponseFilter;
import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.kernel.core.otpmanager.spi.OtpGenerator;
import io.mosip.kernel.otpmanager.dto.GenerationDTOValidationLevels;
import io.mosip.kernel.otpmanager.dto.OtpGeneratorRequestDto;
import io.mosip.kernel.otpmanager.dto.OtpGeneratorResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * This class provides controller methods for OTP generation.
 * 
 * @author Sagar Mahapatra
 * @author Ritesh Sinha
 * @since 1.0.0
 *
 */
@RestController
@CrossOrigin
@Tag(name = "otpgenerator", description = "Operation related to Otp generation")
public class OtpGeneratorController {
	/**
	 * Autowired reference of {@link OtpGenerator}.
	 */
	@Autowired
	OtpGenerator<OtpGeneratorRequestDto, OtpGeneratorResponseDto> otpGeneratorService;

	/**
	 * This method handles the OTP generation.
	 * 
	 * @param otpDto The request DTO for OTP generation.
	 * @return The generated OTP as DTO response.
	 */
	@ResponseFilter
	@Operation(summary = "This endpoint handles the OTP generation", description = "This endpoint handles the OTP generation", tags = { "otpgenerator" })
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Success or you may find errors in error array in response"),
			@ApiResponse(responseCode = "401", description = "Unauthorized" ,content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden" ,content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "404", description = "Not Found" ,content = @Content(schema = @Schema(hidden = true)))})
	@PreAuthorize("hasAnyRole(@authorizedRoles.getPostotpgenerate())")
	@PostMapping(value = "/otp/generate")
	public ResponseWrapper<OtpGeneratorResponseDto> generateOtp(@Validated({
			GenerationDTOValidationLevels.ValidationLevel.class }) @RequestBody RequestWrapper<OtpGeneratorRequestDto> otpDto) {
		ResponseWrapper<OtpGeneratorResponseDto> responseWrapper = new ResponseWrapper<>();
		responseWrapper.setResponse(otpGeneratorService.getOtp(otpDto.getRequest()));
		return responseWrapper;
	}
}
