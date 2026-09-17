package io.mosip.kernel.core.authmanager.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;

/**
 * Full user-profile record as returned by identity-store user-detail APIs.
 * <p>
 * Contract: most fields are optional and may be null. Treat
 * {@code userPassword} as sensitive. Timestamps use the identity store's
 * clock. Does not perform I/O.
 * </p>
 */
@Data
public class UserDetailsDto {

	/**
	 * Unique user identifier; may be null on partial DTOs.
	 */
	private String userId;

	/**
	 * Given name; may be null.
	 */
	private String firstName;

	/**
	 * Family name; may be null.
	 */
	private String lastName;

	/**
	 * Mobile number; may be null.
	 */
	private String mobile;

	/**
	 * Email address; may be null.
	 */
	private String mail;

	/**
	 * Date of birth in {@code yyyy-MM-dd} JSON form; may be null.
	 */
	@JsonFormat(pattern = "yyyy-MM-dd")
	private LocalDate dateOfBirth;

	/**
	 * Gender code from master data; may be null.
	 */
	private String gender;

	/**
	 * Preferred language code (ISO 639); may be null.
	 */
	private String langCode;

	/**
	 * Password bytes as stored by the provider; sensitive; may be null.
	 */
	private byte[] userPassword;

	/**
	 * Display name; may be null.
	 */
	private String name;

	/**
	 * Role name or comma-separated roles; may be null.
	 */
	private String role;

	/**
	 * Registration ID associated with the user; may be null.
	 */
	private String rId;

	/**
	 * Whether the account is activated in the identity store.
	 */
	private boolean activationStatus;

	/**
	 * Whether the account is blacklisted.
	 */
	private boolean blackListedStatus;

	/**
	 * Soft-delete flag from the identity store.
	 */
	private boolean isDeleted;

	/**
	 * Creation timestamp; may be null.
	 */
	private LocalDateTime createdTimeStamp;

	/**
	 * Last-update timestamp; may be null.
	 */
	private LocalDateTime updatedTimeStamp;

	/**
	 * Soft-delete timestamp; may be null if not deleted.
	 */
	private LocalDateTime deletedTimeStamp;

	/**
	 * Administrative zone code; may be null.
	 */
	private String zone;

	/**
	 * Registration ID used at user creation; may be null.
	 */
	private String registrationId;

	/**
	 * Postal or contact address; may be null.
	 */
	private String address;

	/**
	 * Whether the user is currently active.
	 */
	private boolean isActive;
}
