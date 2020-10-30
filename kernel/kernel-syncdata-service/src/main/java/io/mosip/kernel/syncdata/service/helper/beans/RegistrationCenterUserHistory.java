package io.mosip.kernel.syncdata.service.helper.beans;

import java.io.Serializable;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class RegistrationCenterUserHistory extends BaseEntity implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5133215585946271578L;

	
	private String regCntrId;

	
	private String userId;

	
	private LocalDateTime effectDateTimes;

	
	private String langCode;

}