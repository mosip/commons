package io.mosip.kernel.masterdata.dto;

import java.time.LocalTime;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.Range;

import com.fasterxml.jackson.annotation.JsonFormat;

import io.mosip.kernel.masterdata.validator.StringFormatter;
import lombok.Data;

/**
 * This request DTO to hold the numeric fields common for create and update
 * Registration center by Admin, hold the numeric common fields.
 * 
 * @author Megha Tanga
 * 
 * 
 *
 */
@Data
public class RegcenterBaseDto {

	@NotNull
	@StringFormatter(min = 1, max = 36)
	private String centerTypeCode;

	@NotNull
	@StringFormatter(min = 1, max = 32)
	private String latitude;

	@NotNull
	@StringFormatter(min = 1, max = 32)
	private String longitude;

	@NotNull
	@StringFormatter(min = 1, max = 36)
	private String locationCode;

	@NotNull
	@StringFormatter(min = 1, max = 36)
	private String holidayLocationCode;

	@Size(min = 0, max = 16)
	private String contactPhone;

	@NotNull
	@StringFormatter(min = 1, max = 32)
	private String workingHours;

	@NotNull
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm:ss")
	private LocalTime perKioskProcessTime;

	@NotNull
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm:ss")
	private LocalTime centerStartTime;

	@NotNull
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm:ss")
	private LocalTime centerEndTime;

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm:ss")
	private LocalTime lunchStartTime;

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm:ss")
	private LocalTime lunchEndTime;

	@Size(min = 0, max = 64)
	private String timeZone;
	
	@NotNull
	@StringFormatter(min = 1, max = 36)
	private String zoneCode;

}
