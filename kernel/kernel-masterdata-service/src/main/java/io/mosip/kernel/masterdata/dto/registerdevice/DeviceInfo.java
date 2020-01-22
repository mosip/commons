/**
 * 
 */
package io.mosip.kernel.masterdata.dto.registerdevice;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;

/**
 * @author Ramadurai Pandian
 *
 */
@Data
public class DeviceInfo {
	
	private String deviceSubId;
	
	private String certification;
	
	private String digitalId;
	
	private String firmware;
	
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
	private LocalDateTime deviceExpiry;
	
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
	private LocalDateTime timeStamp;

}
