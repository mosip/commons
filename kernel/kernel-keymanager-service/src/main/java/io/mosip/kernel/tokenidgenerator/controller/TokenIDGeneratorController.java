package io.mosip.kernel.tokenidgenerator.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import io.mosip.kernel.core.http.ResponseFilter;
import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.kernel.tokenidgenerator.dto.TokenIDResponseDto;
import io.mosip.kernel.tokenidgenerator.service.TokenIDGeneratorService;
import io.swagger.annotations.ApiParam;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@Tag(name = "tokenidgenerator", description = "Operation related to tokenid generator")
public class TokenIDGeneratorController {

	@Autowired
	private TokenIDGeneratorService tokenIDGeneratorService;
	
	@Operation(summary = "Function to generate token id", description = "Function to generate token id", tags = { "tokenidgenerator" })
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Success or you may find errors in error array in response"),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "404", description = "Not Found", content = @Content(schema = @Schema(hidden = true))) })
	@ResponseFilter
	@GetMapping(value = "/{uin}/{partnercode}")
	@PreAuthorize("hasAnyRole('ZONAL_ADMIN','GLOBAL_ADMIN','ID_AUTHENTICATION','RESIDENT')")
	public ResponseWrapper<TokenIDResponseDto> generateTokenID(@ApiParam("uin of user") @PathVariable("uin") String uin,
			@ApiParam("Partner Code") @PathVariable("partnercode") String partnerCode) {
		ResponseWrapper<TokenIDResponseDto> response = new ResponseWrapper<>();
		response.setResponse(tokenIDGeneratorService.generateTokenID(uin.trim(), partnerCode.trim()));
		return response;
	}

}
