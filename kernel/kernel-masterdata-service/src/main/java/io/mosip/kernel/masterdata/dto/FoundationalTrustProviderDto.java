/**
 * 
 */
package io.mosip.kernel.masterdata.dto;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

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
public class FoundationalTrustProviderDto {
	
	/*@NotEmpty(message="Id must not be blank or null")
	@Size(min = 1, max = 36)
	private String id;*/

	@NotEmpty(message="name must not be blank or null")
	@Size(min = 1, max = 128)
	private String name;

	@NotEmpty(message="address must not be blank or null")
	@Size(min = 1, max = 512)
	private String address;

	@NotEmpty(message="email must not be blank or null")
	@Size(min = 1, max = 256)
	private String email;

	@NotEmpty(message="contactNo must not be blank or null")
	@Size(min = 1, max = 16)
	private String contactNo;

	@NotEmpty(message="certAlias must not be blank or null")
	@Size(min = 1, max = 36)
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
