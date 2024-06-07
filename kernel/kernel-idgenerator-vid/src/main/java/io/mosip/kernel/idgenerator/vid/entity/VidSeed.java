package io.mosip.kernel.idgenerator.vid.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.Data;

/**
 * Entity class for seed number in VID generation algorithm.
 * 
 * @author Ritesh Sinha
 * @since 1.0.0
 */
@Data
@Entity
@Table(name = "vid_seed", schema = "idmap")
public class VidSeed {
	/**
	 * The seed number.
	 */
	@Id
	@Column(name = "seed_no", nullable = false)
	private String seedNumber;

	/**
	 * Created by.
	 */
	@Column(name = "cr_by", nullable = false, length = 256)
	private String createdBy;

	/**
	 * Created date time.
	 */
	@Column(name = "cr_dtimes")
	private LocalDateTime createdDateTime;

	/**
	 * Is deleted true or false.
	 */
	@Column(name = "is_deleted")
	private Boolean isDeleted;

	/**
	 * Deleted date time.
	 */
	@Column(name = "del_dtimes")
	private LocalDateTime deletedDateTime;
}
