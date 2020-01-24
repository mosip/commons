package io.mosip.kernel.masterdata.validator;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;

@Documented
@Constraint(validatedBy = LocalDateValidator.class)
@Target({ ElementType.FIELD, ElementType.TYPE_USE, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidDateFormat {
	String message() default "Invalid Date Format";

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};
}
