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
 * Assigned VID row in schema {@code kernel}, table {@code vid_assigned}.
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
	 * UTC expiry after which the VID may be marked {@code EXPIRED}.
	 */
	@Column(name = "expiry_dtimes")
	private LocalDateTime vidExpiry;

	/**
	 * Copies identifier, status, expiry, and audit columns from a pool {@link VidEntity}.
	 *
	 * @param vidEntity source pool row
	 */
	public VidAssignedEntity(VidEntity vidEntity) {
		super(
				vidEntity.getCreatedBy(),
				vidEntity.getCreatedtimes(),
				vidEntity.getUpdatedBy(),
				vidEntity.getUpdatedtimes(),
				vidEntity.getIsDeleted(),
				vidEntity.getDeletedtimes()
		);
		this.vid = vidEntity.getVid();
		this.status = vidEntity.getStatus();
		this.vidExpiry = vidEntity.getVidExpiry();
	}
}
