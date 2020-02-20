package io.mosip.kernel.masterdata.entity;

import java.time.LocalDateTime;

import javax.persistence.AttributeOverride;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;

import io.mosip.kernel.masterdata.entity.id.DeviceRegisterHistoryId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "registered_device_master_h", schema = "master")
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@IdClass(DeviceRegisterHistoryId.class)
public class DeviceRegisterHistory extends BaseEntity {

	@Id
	@AttributeOverride(name = "deviceCode", column = @Column(name = "code"))
	@AttributeOverride(name = "effectivetimes", column = @Column(name = "eff_dtimes"))
	private String deviceCode;

	private LocalDateTime effectivetimes;

	@Column(name = "dtype_code")
	private String dTypeCode;

	@Column(name = "dstype_code")
	private String dSubTypeCode;

	@Column(name = "digital_id")
	private String digitalId;

	@Column(name = "status_code")
	private String statusCode;

	@Column(name = "device_id")
	private String deviceId;

	@Column(name = "device_sub_id")
	private String deviceSubId;

	@Column(name = "provider_id")
	private String deviceProviderId;

	@Column(name = "provider_name")
	private String deviceProviderName;

	@Column(name = "purpose")
	private String purpose;

	@Column(name = "firmware")
	private String firmware;

	@Column(name = "make")
	private String deviceMake;

	@Column(name = "model")
	private String deviceModel;

	@Column(name = "expiry_date")
	private LocalDateTime deviceExpiry;

	@Column(name = "certification_level")
	private byte[] certification;

	@Column(name = "foundational_trust_provider_iD")
	private String foundationalTrustProviderID;

	@Column(name = "serial_number", nullable = false)
	private String serialNumber;

}
