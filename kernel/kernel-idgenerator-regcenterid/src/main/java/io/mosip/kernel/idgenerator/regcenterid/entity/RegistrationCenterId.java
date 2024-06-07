package io.mosip.kernel.idgenerator.regcenterid.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.Data;

/**
 * Entity class for Registration Center ID.
 * 
 * @author Sagar Mahapatra
 * @since 1.0.0
 *
 */
@Entity
@Table(schema = "master", name = "rcid_seq")
@Data
public class RegistrationCenterId {
	/**
	 * the registration center id.
	 */
	@Id
	@Column(name = "curr_seq_no")
	private int rcid;

	/**
	 * The ID created by.
	 */
	@Column(name = "cr_by", nullable = false, length = 256)
	private String createdBy;

	/**
	 * The ID created at.
	 */
	@Column(name = "cr_dtimes", nullable = false)
	private LocalDateTime createdDateTime;

	/**
	 * The ID updated by.
	 */
	@Column(name = "upd_by", length = 256)
	private String updatedBy;

	/**
	 * The ID updated at.
	 */
	@Column(name = "upd_dtimes")
	private LocalDateTime updatedDateTime;
}
