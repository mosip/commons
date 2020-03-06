/**
 * 
 */
package io.mosip.kernel.masterdata.dto.registerdevice;

import javax.validation.constraints.Size;

import io.mosip.kernel.masterdata.validator.StringFormatter;
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

	
	@StringFormatter(min = 1, max = 36)
	private String deviceId;

	@ValidPurpose(message = "Invalid Purpose received")
	private String purpose;

	private DeviceInfo deviceInfo;

	private String foundationalTrustProviderId;

}
