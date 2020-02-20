package io.mosip.kernel.masterdata.validator;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import io.mosip.kernel.core.exception.ServiceError;
import io.mosip.kernel.masterdata.constant.ValidationErrorCode;
import io.mosip.kernel.masterdata.dto.request.FilterDto;
import io.mosip.kernel.masterdata.exception.ValidationException;

/**
 * Class that validates the fields annotated with {@link FilterColumn}.
 * 
 * @author Sagar Mahapatra
 * @author Ritesh Sinha
 * @author Megha Tanga
 * @since 1.0.0
 *
 */
@Component
public class FilterColumnValidator {
	/**
	 * Value of the column name which uses {@link FilterColumn} annotation.
	 */
	private static final String TYPE_FIELD = "type";

	/**
	 * @param target  the entity name for which the filters needs to be validated.
	 * @param filters list of filters.
	 * @return true if validation is successful.
	 */
	public <T, E> boolean validate(Class<T> target, List<FilterDto> filters, Class<E> entity) {
		List<ServiceError> errors = new ArrayList<>();
		if (filters != null && !filters.isEmpty()) {
			for (FilterDto filter : filters) {
				validateFilterColumn(target, errors, filter, entity);
			}
		}
		if (!errors.isEmpty())
			throw new ValidationException(errors);

		return true;
	}

	/**
	 * Method to validate filter column values.
	 * 
	 * @param target Entity class type.
	 * @param errors errors to be returned if validation fails.
	 * @param filter the filter column type value.
	 */
	private <T, E> void validateFilterColumn(Class<T> target, List<ServiceError> errors, FilterDto filter,
			Class<E> entity) {
		try {
			if (validateFilterColumnType(filter.getType())) {
				Field field = target.getDeclaredField(TYPE_FIELD);
				Field[] childFields = entity.getDeclaredFields();
				Field[] superFields = entity.getSuperclass().getDeclaredFields();
				List<Field> fieldList = new ArrayList<>();
				fieldList.addAll(Arrays.asList(childFields));
				if (superFields != null)
					fieldList.addAll(Arrays.asList(superFields));
				Optional<Field> renField = fieldList.stream()
						.filter(i -> i.getName().equalsIgnoreCase(filter.getColumnName())).findFirst();
				if (!renField.isPresent()) {
					errors.add(new ServiceError(ValidationErrorCode.COLUMN_DOESNT_EXIST.getErrorCode(), String.format(
							ValidationErrorCode.COLUMN_DOESNT_EXIST.getErrorMessage(), filter.getColumnName())));
				}
//				if (!renField.isPresent()) {
//					errors.add(new ServiceError(ValidationErrorCode.COLUMN_DOESNT_EXIST_FILTER.getErrorCode(),
//							String.format(ValidationErrorCode.COLUMN_DOESNT_EXIST_FILTER.getErrorMessage(),
//									filter.getColumnName())));
//				}
				if (!containsFilterColumn(field, filter.getType())) {
					errors.add(new ServiceError(ValidationErrorCode.FILTER_COLUMN_NOT_SUPPORTED.getErrorCode(),
							String.format(ValidationErrorCode.FILTER_COLUMN_NOT_SUPPORTED.getErrorMessage(),
									filter.getType())));
				}
			} else {
				errors.add(new ServiceError(ValidationErrorCode.NO_FILTER_COLUMN_FOUND.getErrorCode(),
						String.format(ValidationErrorCode.NO_FILTER_COLUMN_FOUND.getErrorMessage(), filter.getType())));
			}
		} catch (NoSuchFieldException | SecurityException e) {
			errors.add(new ServiceError(ValidationErrorCode.FILTER_COLUMN_DOESNT_EXIST.getErrorCode(),
					String.format(ValidationErrorCode.FILTER_COLUMN_DOESNT_EXIST.getErrorMessage(), filter.getType())));
		}
	}

	/**
	 * Method to validate filter column values.
	 * 
	 * @param field        the name of the filter column variable.
	 * @param filterColumn the value of the filter column.
	 * @return true if the value is same as in {@link FilterColumnEnum}.
	 */
	public boolean containsFilterColumn(Field field, String filterColumn) {
		if (field.isAnnotationPresent(FilterColumn.class)) {
			FilterColumn annotation = field.getAnnotation(FilterColumn.class);
			FilterColumnEnum[] columns = annotation.columns();
			return Arrays.stream(columns).map(FilterColumnEnum::toString).map(String::toLowerCase)
					.collect(Collectors.toList()).contains(filterColumn.toLowerCase());
		}
		return false;
	}

	/**
	 * Method to validate whether the field value is empty or null.
	 * 
	 * @param filterType the filter type to be validated.
	 * @return true if it neither empty nor null.
	 */
	private boolean validateFilterColumnType(String filterType) {
		return StringUtils.isNotBlank(filterType);
	}
}
