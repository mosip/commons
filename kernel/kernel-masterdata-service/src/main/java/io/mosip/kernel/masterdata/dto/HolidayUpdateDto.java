package io.mosip.kernel.masterdata.dto;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 
 * @author Abhishek Kumar
 * @since 1.0.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class HolidayUpdateDto {
	
	private Integer id;

	private String locationCode;

	@DateTimeFormat(pattern="yyyy-mm-dd")
	private LocalDate holidayDate;

	private String holidayName;

	private String holidayDesc;
	
	
	private String langCode;

	
	private Boolean isActive;


}
