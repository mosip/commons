
package io.mosip.kernel.syncdata.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Response dto for Machine Detail
 * 
 * @author Megha Tanga
 * @since 1.0.0
 *
 */

@Data
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
public class MachineDto extends BaseDto {

	/**
	 * Field for machine id
	 */
	private String id;
	/**
	 * Field for machine name
	 */
	private String name;
	/**
	 * Field for machine serial number
	 */
	private String serialNum;
	/**
	 * Field for machine mac address
	 */
	private String macAddress;
	/**
	 * Field for machine IP address
	 */
	private String ipAddress;
	/**
	 * Field for machine specification Id
	 */
	private String machineSpecId;

	/**
	 * Field for is validity of the Device
	 */
	private LocalDateTime validityDateTime;

	private String keyIndex;

	private String publicKey;

}
