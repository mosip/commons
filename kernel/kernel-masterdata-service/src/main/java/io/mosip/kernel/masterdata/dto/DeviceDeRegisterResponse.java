/**
 * 
 */
package io.mosip.kernel.masterdata.dto;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;

/**
 * @author Ramadurai Pandian
 *
 */
@Data
public class DeviceDeRegisterResponse {

	private String status;
	private String deviceCode;
	private String env;
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
	private LocalDateTime timeStamp;

}
