package io.mosip.kernel.core.authmanager.model;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;

import com.fasterxml.jackson.annotation.JsonFormat;

import io.mosip.kernel.core.authmanager.constant.AuthConstant;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request to register a new user in the identity store.
 * <p>
 * Contract: {@code userName}, {@code contactNo}, {@code emailID}, and
 * {@code appId} are required and non-blank. Other fields are optional. Treat
 * {@code userPassword} as sensitive. Used by
 * {@link io.mosip.kernel.core.authmanager.spi.AuthService#registerUser(UserRegistrationRequestDto)}.
 * </p>
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserRegistrationRequestDto {
	/**
	 * Login name to create; required, non-blank.
	 */
	@NotBlank(message = AuthConstant.INVALID_REQUEST)
	private String userName;

	/**
	 * Given name; may be null.
	 */
	private String firstName;

	/**
	 * Family name; may be null.
	 */
	private String lastName;

	/**
	 * Contact phone number; required, non-blank.
	 */
	@NotBlank(message = AuthConstant.INVALID_REQUEST)
	private String contactNo;

	/**
	 * Email address; required, non-blank.
	 */
	@NotBlank(message = AuthConstant.INVALID_REQUEST)
	private String emailID;

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
	 * Role to assign at creation; may be null.
	 */
	private String role;

	/**
	 * MOSIP application identifier owning the user; required, non-blank.
	 */
	@NotBlank(message = AuthConstant.INVALID_REQUEST)
	private String appId;

	/**
	 * Initial password; may be null if the store generates one; sensitive.
	 */
	private String userPassword;
}
