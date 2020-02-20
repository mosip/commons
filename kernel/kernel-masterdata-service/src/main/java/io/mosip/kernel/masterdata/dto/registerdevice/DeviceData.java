/**
 * 
 */
package io.mosip.kernel.masterdata.dto.registerdevice;

import io.mosip.kernel.masterdata.validator.registereddevice.ValidFoundational;
import io.mosip.kernel.masterdata.validator.registereddevice.ValidPurpose;
import lombok.Data;

/**
 * @author Ramadurai Pandian
 *
 */
@Data
@ValidFoundational(baseField = "certificationLevel", matchField = { "foundationalTrustProviderId" })
public class DeviceData {

	private String deviceId;

	@ValidPurpose(message = "Invalid Purpose received")
	private String purpose;

	private DeviceInfo deviceInfo;

	private String foundationalTrustProviderId;

}
