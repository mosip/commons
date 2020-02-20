package io.mosip.kernel.masterdata.dto;

import java.time.LocalTime;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonFormat;

import io.mosip.kernel.masterdata.validator.ValidLangCode;
import lombok.Data;

/**
 * @author Dharmesh Khandelwal
 * @author Abhishek Kumar
 * @author Urvil Joshi
 * @since 1.0.0
 *
 */
@Data
public class RegistrationCenterDto {
	@NotBlank
	@Size(min = 1, max = 10)
	private String id;

	@NotBlank
	@Size(min = 1, max = 128)
	private String name;

	@Size(min = 1, max = 36)
	private String centerTypeCode;

	@Size(min = 1, max = 256)
	private String addressLine1;

	@Size(min = 1, max = 256)
	private String addressLine2;

	@Size(min = 1, max = 256)
	private String addressLine3;

	@Size(min = 1, max = 32)
	private String latitude;

	@Size(min = 1, max = 32)
	private String longitude;

	@NotBlank
	@Size(min = 1, max = 36)
	private String locationCode;

	@Size(min = 1, max = 36)
	private String holidayLocationCode;

	@Size(min = 1, max = 16)
	private String contactPhone;

	@Size(min = 1, max = 32)
	private String workingHours;

	@ValidLangCode
	private String langCode;

	private Short numberOfKiosks;

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm:ss")
	private LocalTime perKioskProcessTime;

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm:ss")
	private LocalTime centerStartTime;

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm:ss")
	private LocalTime centerEndTime;

	@Size(min = 1, max = 64)
	private String timeZone;

	@Size(min = 1, max = 128)
	private String contactPerson;

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm:ss")
	private LocalTime lunchStartTime;

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm:ss")
	private LocalTime lunchEndTime;

	@NotNull
	private Boolean isActive;

	private String zoneCode;

}
