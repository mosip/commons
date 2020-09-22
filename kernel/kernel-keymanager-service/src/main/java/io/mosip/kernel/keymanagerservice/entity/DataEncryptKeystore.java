package io.mosip.kernel.keymanagerservice.entity;

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
 * @author Mahammed Taheer
 *
 */

@Entity
@Table(name = "data_encrypt_keystore")
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
public class DataEncryptKeystore {

	@Id
    private Integer id;
    
	@Column(name = "key")
	private String key;
    
    @Column(name = "key_status")
	private String keyStatus;

	@Column(name = "cr_by")
	private String crBy;
    
    @Column(name = "cr_dtimes")
	private LocalDateTime crDTimes;
    
    @Column(name = "upd_by")
	private String updBy;
    
    @Column(name = "upd_dtimes")
	private LocalDateTime updDTimes;
}