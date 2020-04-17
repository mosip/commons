package io.mosip.kernel.masterdata.entity.id;

import java.io.Serializable;
import java.time.LocalDate;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Megha Tanga
 * @since 1.0.0
 *
 */
@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LocationHierarchyId implements Serializable {

	private static final long serialVersionUID = -6687447931850039408L;

	@Column(name = "hierarchy_level", nullable = false, length = 36)
	private short hierarchyLevel;

	@Column(name = "hierarchy_level_name", nullable = false, length = 64)
	private String hierarchyLevelName;

	@Column(name = "lang_code", nullable = false, length = 3)
	private String langCode;
}
