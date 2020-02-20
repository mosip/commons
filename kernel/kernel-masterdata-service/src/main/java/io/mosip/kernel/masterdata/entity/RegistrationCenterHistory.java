package io.mosip.kernel.masterdata.entity;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.LocalTime;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import io.mosip.kernel.masterdata.entity.id.RegistrationCenterHistoryID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * @author Dharmesh Khandelwal
 * @since 1.0.0
 *
 */
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "registration_center_h", schema = "master")
@IdClass(RegistrationCenterHistoryID.class)
public class RegistrationCenterHistory extends BaseEntity implements Serializable {

	private static final long serialVersionUID = -8541947587557590379L;

	@Id
	@AttributeOverrides({ @AttributeOverride(name = "id", column = @Column(name = "id", nullable = false, length = 36)),
			@AttributeOverride(name = "effectivetimes", column = @Column(name = "eff_dtimes", nullable = false)),
			@AttributeOverride(name = "langCode", column = @Column(name = "lang_code", nullable = false, length = 3)) })

	private String id;
	private String langCode;
	private LocalDateTime effectivetimes;

	@Column(name = "name", nullable = false, length = 128)
	private String name;

	@Column(name = "cntrtyp_code", length = 36)
	private String centerTypeCode;

	@Column(name = "addr_line1", length = 256)
	private String addressLine1;

	@Column(name = "addr_line2", length = 256)
	private String addressLine2;

	@Column(name = "addr_line3", length = 256)
	private String addressLine3;

	@Column(name = "latitude", length = 32)
	private String latitude;

	@Column(name = "longitude", length = 32)
	private String longitude;

	@Column(name = "location_code", nullable = false, length = 36)
	private String locationCode;

	@Column(name = "contact_phone", length = 16)
	private String contactPhone;

	@Column(name = "number_of_kiosks")
	private Short numberOfKiosks;

	@Column(name = "holiday_loc_code", nullable = false, length = 36)
	private String holidayLocationCode;

	@Column(name = "working_hours", length = 32)
	private String workingHours;

	@Column(name = "per_kiosk_process_time")
	private LocalTime perKioskProcessTime;

	@Column(name = "center_start_time")
	private LocalTime centerStartTime;

	@Column(name = "center_end_time")
	private LocalTime centerEndTime;

	@Column(name = "time_zone", length = 64)
	private String timeZone;

	@Column(name = "contact_person", length = 128)
	private String contactPerson;

	@Column(name = "lunch_start_time")
	private LocalTime lunchStartTime;

	@Column(name = "lunch_end_time")
	private LocalTime lunchEndTime;

	@ManyToOne(cascade = CascadeType.ALL)
	@JoinColumns({
			@JoinColumn(name = "location_code", referencedColumnName = "code", insertable = false, updatable = false),
			@JoinColumn(name = "lang_code", referencedColumnName = "lang_code", insertable = false, updatable = false), })
	private Location location;

	@Column(name = "zone_code", length = 36)
	private String zoneCode;

}
