/**
 * 
 */
package io.mosip.kernel.masterdata.dto.registerdevice;

import java.time.LocalDateTime;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonFormat;

import io.mosip.kernel.masterdata.validator.StringFormatter;
import io.mosip.kernel.masterdata.validator.registereddevice.ValidCertificateLevel;
import lombok.Data;

/**
 * @author Ramadurai Pandian
 *
 */
@Data
public class DeviceInfo {

	private String deviceSubId;

	@ValidCertificateLevel(message = "Invalid Certification level received")
	private String certification;

	private String digitalId;

	@NotNull
	@StringFormatter(min = 1, max = 128)
	private String firmware;

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
	private LocalDateTime deviceExpiry;

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
	private LocalDateTime timeStamp;

}
