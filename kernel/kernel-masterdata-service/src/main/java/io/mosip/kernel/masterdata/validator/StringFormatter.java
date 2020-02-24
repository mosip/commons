package io.mosip.kernel.masterdata.validator;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;

/**
 * Annotation used to validate field for valid language code as per ISO:639-3.
 * 
 * @author Srinivasan
 */
@Documented
@Constraint(validatedBy = StringTrimmer.class)
@Target({ ElementType.FIELD, ElementType.TYPE_USE, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
public @interface StringFormatter {

	/**
	 * Message.
	 *
	 * @return the string
	 */
	String message() default "Should not be blank or {javax.validation.constraints.Size.message}";

	/**
	 * Groups.
	 *
	 * @return the class[]
	 */
	Class<?>[] groups() default {};

	/**
	 * Payload.
	 *
	 * @return the class<? extends payload>[]
	 */
	Class<? extends Payload>[] payload() default {};

	/**
	 * Min.
	 *
	 * @return the int
	 */
	int min() default 0;

	/**
	 * Max.
	 *
	 * @return the int
	 */
	int max() default Integer.MAX_VALUE;

}
