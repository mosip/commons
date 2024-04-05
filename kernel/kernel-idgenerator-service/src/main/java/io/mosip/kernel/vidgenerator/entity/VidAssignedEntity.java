package io.mosip.kernel.vidgenerator.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Entity class for vid assigned bean
 * 
 * @author Vishwanath V
 *
 */

@Entity
@Table(name = "vid_assigned", schema = "kernel")
@Data
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
public class VidAssignedEntity extends BaseEntity {

	/**
	 * Field for vid
	 */
	@Id
	@Column(name = "vid", unique = true, nullable = false, updatable = false, length = 28)
	private String vid;

	/**
	 * Field whether this vid is used
	 */
	@Column(name = "vid_status", nullable = false, length = 16)
	private String status;

	/**
	 * The field createdtimes
	 */
	@Column(name = "expiry_dtimes")
	private LocalDateTime vidExpiry;
}
