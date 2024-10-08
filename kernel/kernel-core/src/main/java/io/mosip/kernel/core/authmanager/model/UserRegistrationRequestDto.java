package io.mosip.kernel.core.authmanager.model;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;

import com.fasterxml.jackson.annotation.JsonFormat;

import io.mosip.kernel.core.authmanager.constant.AuthConstant;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserRegistrationRequestDto {
	@NotBlank(message = AuthConstant.INVALID_REQUEST)
	private String userName;

	private String firstName;

	private String lastName;

	@NotBlank(message = AuthConstant.INVALID_REQUEST)
	private String contactNo;

	@NotBlank(message = AuthConstant.INVALID_REQUEST)
	private String emailID;

	@JsonFormat(pattern = "yyyy-MM-dd")
	private LocalDate dateOfBirth;

	private String gender;

	private String role;

	@NotBlank(message = AuthConstant.INVALID_REQUEST)
	private String appId;

	private String userPassword;
}
