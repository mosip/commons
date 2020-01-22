/**
 * 
 */
package io.mosip.kernel.masterdata.dto.registerdevice;

import lombok.Data;

/**
 * @author Ramadurai Pandian
 *
 */
@Data
public class DeviceData {
	
	private String deviceId;
	
	private String purpose;
	
	private DeviceInfo deviceInfo;
	
	private String foundationalTrustProviderId;

}
