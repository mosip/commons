package io.mosip.kernel.uingenerator.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Entity class for uin bean
 * 
 * @author Dharmesh Khandelwal
 * @author Megha Tanga
 * @since 1.0.0
 *
 */

@Entity
@Table(name = "uin_assigned", schema = "kernel")
@Data
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
public class UinEntityAssigned extends BaseEntity {

	/**
	 * Field for uin
	 */
	@Id
	@Column(name = "uin", unique = true, nullable = false, updatable = false, length = 28)
	private String uin;

	/**
	 * Field whether this uin is used
	 */
	// @Column(name = "is_used")
	@Column(name = "uin_status", nullable = false, length = 16)
	private String status;

	public UinEntityAssigned(UinEntity uinEntity) {
		super(
				uinEntity.getCreatedBy(),
				uinEntity.getCreatedtimes(),
				uinEntity.getUpdatedBy(),
				uinEntity.getUpdatedtimes(),
				uinEntity.getIsDeleted(),
				uinEntity.getDeletedtimes()
		);
		this.uin = uinEntity.getUin();
		this.status = uinEntity.getStatus();
	}

}