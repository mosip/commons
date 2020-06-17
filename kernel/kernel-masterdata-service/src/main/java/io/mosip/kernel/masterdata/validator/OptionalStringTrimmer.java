package io.mosip.kernel.masterdata.validator;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import lombok.Data;

@Data
public class OptionalStringTrimmer implements ConstraintValidator<OptionalStringFormatter, String> {

	/** The min. */
	private int min = 0;

	/** The max. */
	private int max = Integer.MAX_VALUE;

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.validation.ConstraintValidator#initialize(java.lang.annotation.
	 * Annotation)
	 */
	@Override
	public void initialize(OptionalStringFormatter constraintAnnotation) {
		max = constraintAnnotation.max();
		min = constraintAnnotation.min();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.validation.ConstraintValidator#isValid(java.lang.Object,
	 * javax.validation.ConstraintValidatorContext)
	 */
	@Override
	public boolean isValid(String arg0, ConstraintValidatorContext arg1) {

		return !(arg0 != null && ( arg0.trim().length() < min
				|| arg0.trim().length() > max));
	}

}

