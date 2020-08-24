package io.mosip.kernel.keymanagerservice.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Entity class for KeyStore
 * 
 * @author Dharmesh Khandelwal
 * @since 1.0.0
 *
 */
@Entity
@Table(name = "key_store")
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
public class KeyStore extends BaseEntity {

	/**
	 * The field alias
	 */
	@Id
	@Column(name = "id", nullable = false, length = 36)
	private String alias;

	/**
	 * The field publicKey
	 */
	@Column(name = "certificate_data")
	private String certificateData;

	/**
	 * The field privateKey
	 */
	@Column(name = "private_key")
	private String privateKey;

	/**
	 * The field masterAlias
	 */
	@Column(name = "master_key")
	private String masterAlias;

}
