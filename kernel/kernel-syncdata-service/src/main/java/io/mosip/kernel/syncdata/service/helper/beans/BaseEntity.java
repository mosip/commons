package io.mosip.kernel.syncdata.service.helper.beans;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BaseEntity {

	
	private Boolean isActive;

	
	private String createdBy;

	
	private LocalDateTime createdDateTime;

	
	private String updatedBy;

	
	private LocalDateTime updatedDateTime;

	
	private Boolean isDeleted;

	
	private LocalDateTime deletedDateTime;

}
