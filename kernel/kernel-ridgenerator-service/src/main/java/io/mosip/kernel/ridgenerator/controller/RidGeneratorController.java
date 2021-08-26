package io.mosip.kernel.ridgenerator.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import io.mosip.kernel.core.http.ResponseFilter;
import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.kernel.ridgenerator.dto.RidGeneratorResponseDto;
import io.mosip.kernel.ridgenerator.service.RidGeneratorService;
import io.swagger.annotations.ApiOperation;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Controller class for RID generator.
 * 
 * @author Ritesh Sinha
 * @since 1.0.0
 */
@RestController
@CrossOrigin
@Tag(name = "ridgenerator", description = "Operation related to RID generation")
public class RidGeneratorController {

	/**
	 * Reference to {@link RidGeneratorService}.
	 */
	@Autowired
	private RidGeneratorService<RidGeneratorResponseDto> ridGeneratorService;

	/**
	 * Api to generate RID.
	 * 
	 * @param centerId  the registration center id.
	 * @param machineId the machine id.
	 * @return the response.
	 */
	@Operation(summary = "This endpoint handles the RID generation", description = "This endpoint handles the RID generation", tags = { "ridgenerator" })
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Success or you may find errors in error array in response"),
			@ApiResponse(responseCode = "401", description = "Unauthorized" ,content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden" ,content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "404", description = "Not Found" ,content = @Content(schema = @Schema(hidden = true)))})
	@ResponseFilter
	@GetMapping("/generate/rid/{centerid}/{machineid}")
	@PreAuthorize("hasAnyRole('REGISTRATION_PROCESSOR','RESIDENT')")
	public ResponseWrapper<RidGeneratorResponseDto> generateRid(@PathVariable("centerid") String centerId,
			@PathVariable("machineid") String machineId) {
		ResponseWrapper<RidGeneratorResponseDto> responseWrapper = new ResponseWrapper<>();
		responseWrapper.setResponse(ridGeneratorService.generateRid(centerId.trim(), machineId.trim()));
		return responseWrapper;
	}
}
