package io.mosip.kernel.saltgenerator.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.Data;

/**
 * The Class SaltEntity.
 *
 * @author Manoj SP
 */
@Entity

/**
 * Instantiates a new salt entity.
 */
@Data
@Table
public class SaltEntity {

	/** The id. */
	@Id
	@Column(updatable = false, nullable = false, unique = true)
	private Long id;

	/** The salt. */
	@Column(updatable = true, nullable = false, unique = true)
	private String salt;

	/** The created by. */
	@Column(name = "cr_by", updatable = true, nullable = false, unique = false)
	private String createdBy;

	/** The create dtimes. */
	@Column(name = "cr_dtimes", updatable = true, nullable = false, unique = true)
	private LocalDateTime createDtimes;

	/** The updated by. */
	@Column(name = "upd_by", updatable = true, nullable = true, unique = false)
	private String updatedBy;

	/** The updated dtimes. */
	@Column(name = "upd_dtimes", updatable = true, nullable = true, unique = true)
	private LocalDateTime updatedDtimes;

}
