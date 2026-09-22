package io.mosip.kernel.idgenerator.rid.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.Data;

/**
 * The entity for rid generator.
 * 
 * @author Ritesh Sinha
 * @author Abhishek Kumar
 * @since 1.0.0
 *
 */
@Entity
@Table(name = "rid_seq")
@Data
public class Rid {

	/**
	 * the current sequence no.
	 * 
	 */
	@Id
	@Column(name = "curr_seq_no")
	private int currentSequenceNo;

	/** User who created the sequence row. */
	@Column(name = "cr_by", nullable = false, length = 256)
	private String createdBy;

	/** Creation timestamp. */
	@Column(name = "cr_dtimes", nullable = false)
	private LocalDateTime createdDateTime;

	/** User who last updated the sequence. */
	@Column(name = "upd_by", length = 256)
	private String updatedBy;

	/** Last update timestamp. */
	@Column(name = "upd_dtimes")
	private LocalDateTime updatedDateTime;
}