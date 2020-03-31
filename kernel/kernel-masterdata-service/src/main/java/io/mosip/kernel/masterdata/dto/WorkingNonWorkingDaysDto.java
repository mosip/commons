package io.mosip.kernel.masterdata.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class WorkingNonWorkingDaysDto {

	// @NotNull
	Boolean sun;
	// @NotNull
	Boolean mon;
	// @NotNull
	Boolean tue;
	// @NotNull
	Boolean wed;
	// @NotNull
	Boolean thu;
	// @NotNull
	Boolean fri;
	// @NotNull
	Boolean sat;

}
