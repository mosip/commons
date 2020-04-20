package io.mosip.kernel.masterdata.entity;

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
 * @author anusha
 *
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "dynamic_field", schema = "master")
public class DynamicField extends BaseEntity {
	
	@Id
	@Column(name = "id", nullable = false)
	private String id;
	
	@Column(name = "lang_code", nullable= false, length = 3)
	private String langCode;
	
	@Column(name = "name", nullable= false, length = 15)
	private String name;
	
	@Column(name = "description", nullable= true)
	private String description;	
	
	@Column(name = "data_type", nullable= false)
	private String dataType;
	
	@Column(name = "value_json", nullable= true, length = 1000)
	private String valueJson;
}
