package io.mosip.kernel.masterdata.entity;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;


/**
 * 
 * @author Anusha
 *
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "identity_schema", schema = "master")
public class IdentitySchema extends BaseEntity {
	
	@Id
	@Column(name = "id", nullable = false)
	private String id;
	
	//id_version is generated only when schema is published
	@Column(name = "id_version", nullable = false)
	private double idVersion;
	
	@Column(name = "title", nullable = true, length=50)
	private String title;
	
	@Column(name = "description", nullable = false, length=50)
	private String description;
	
	@Column(name = "id_attr_json", nullable = false, length=20480)
	private String idAttributeJson;
	
	//schema_json is built only when schema is published
	@Column(name = "schema_json", nullable = false, length=10240)
	private String schemaJson;
	
	@Column(name = "status_code", nullable = false, length=16)
	private String status;
	
	@Column(name = "add_props", nullable = false)
	private boolean additionalProperties;
		 
	@Column(name = "effective_from", nullable = false)
	private LocalDateTime effectiveFrom;
	
	@Column(name = "lang_code", nullable = false)
	private String langCode;
}
