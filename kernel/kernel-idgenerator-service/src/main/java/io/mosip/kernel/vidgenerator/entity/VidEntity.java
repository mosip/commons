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
 * Unused or assigned VID row in schema {@code kernel}, table {@code vid}.
 *
 * @author Urvil Joshi
 * @since 1.0.0
 *
 */

@Entity
@Table(name = "vid", schema = "kernel")
@Data
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
public class VidEntity extends BaseEntity {

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
}
