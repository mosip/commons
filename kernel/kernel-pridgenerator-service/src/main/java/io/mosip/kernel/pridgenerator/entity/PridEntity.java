package io.mosip.kernel.pridgenerator.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Entity class for prid bean
 * 
 * @author Ajay J
 * @since 1.0.0
 *
 */

@Entity
@Table(name = "prid", schema = "kernel")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class PridEntity extends BaseEntity {

	/**
	 * Field for prid
	 */
	@Id
	@Column(name = "prid", unique = true, nullable = false, updatable = false, length = 28)
	private String prid;

	/**
	 * Field whether this prid is used
	 */
	@Column(name = "prid_status", nullable = false, length = 16)
	private String status;

}
