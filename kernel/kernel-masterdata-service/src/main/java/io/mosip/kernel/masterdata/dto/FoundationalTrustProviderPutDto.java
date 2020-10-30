/**
 * 
 */
package io.mosip.kernel.masterdata.dto;

import javax.validation.constraints.NotNull;

import io.mosip.kernel.masterdata.validator.StringFormatter;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Ramadurai Pandian
 *
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FoundationalTrustProviderPutDto {

	@NotNull
	@StringFormatter(min = 1, max = 36)
	private String id;

	@NotNull
	@StringFormatter(min = 1, max = 128)
	private String name;

	@NotNull
	@StringFormatter(min = 1, max = 512)
	private String address;

	@NotNull
	@StringFormatter(min = 1, max = 256)
	private String email;

	@NotNull
	@StringFormatter(min = 1, max = 16)
	private String contactNo;

	@NotNull
	@StringFormatter(min = 1, max = 36)
	private String certAlias;

	@NotNull
	private boolean isActive;

	public boolean getIsActive() {
		return isActive;
	}

	public void setIsActive(boolean isActive) {
		this.isActive = isActive;
	}
}
