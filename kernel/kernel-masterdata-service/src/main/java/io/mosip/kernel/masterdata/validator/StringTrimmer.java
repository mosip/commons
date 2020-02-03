package io.mosip.kernel.masterdata.validator;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import lombok.Data;


/**
 * The Class StringTrimmer.
 */
@Data
public class StringTrimmer implements ConstraintValidator<StringFormatter, String> {

	@Override
	public boolean isValid(String arg0, ConstraintValidatorContext arg1) {
		
		
			return !(arg0.trim().length()==0 || arg0.trim().isEmpty());
	}

	

}
