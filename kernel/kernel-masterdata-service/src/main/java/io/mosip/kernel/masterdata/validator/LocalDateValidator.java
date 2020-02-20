package io.mosip.kernel.masterdata.validator;

import java.time.LocalDate;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import lombok.Data;

@Data
public class LocalDateValidator implements ConstraintValidator<ValidDateFormat, String> {

	@Override
	public boolean isValid(String localDate, ConstraintValidatorContext context) {
		try {
			LocalDate.parse(localDate);
		} catch (Exception e) {
			return false;
		}
		return true;
	}

}
