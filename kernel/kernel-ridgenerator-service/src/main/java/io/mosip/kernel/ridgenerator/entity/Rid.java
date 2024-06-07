package io.mosip.kernel.ridgenerator.entity;

import java.io.Serializable;
import java.time.LocalDateTime;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

import io.mosip.kernel.ridgenerator.entity.id.CenterAndMachineId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entity class for RID generator.
 * 
 * @author Ritesh Sinha
 * @since 1.0.0
 */
@Entity
@Table(name = "rid_seq", schema = "regprc")
@Data
@NoArgsConstructor
@AllArgsConstructor
@IdClass(CenterAndMachineId.class)
public class Rid implements Serializable {

	/**
	 * Generated serial number.
	 */
	private static final long serialVersionUID = 1L;

	@Id
	@AttributeOverrides({ @AttributeOverride(name = "code", column = @Column(name = "regcntr_id")),
			@AttributeOverride(name = "langCode", column = @Column(name = "machine_id")) })
	private String centerId;

	private String machineId;

	/**
	 * The sequence number.
	 */
	@Column(name = "curr_seq_no")
	private int currentSequenceNo;

	/**
	 * Created by.
	 */
	@Column(name = "cr_by", nullable = false, length = 256)
	private String createdBy;

	/**
	 * Created date time.
	 */
	@Column(name = "cr_dtimes", nullable = false)
	private LocalDateTime createdDateTime;

	/**
	 * Updated by.
	 */
	@Column(name = "upd_by", length = 256)
	private String updatedBy;

	/**
	 * Updated date time.
	 */
	@Column(name = "upd_dtimes")
	private LocalDateTime updatedDateTime;

}
