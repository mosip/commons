package io.mosip.kernel.core.datamapper.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Source-to-destination field pair used when configuring
 * {@link io.mosip.kernel.core.datamapper.spi.DataMapperBuilder#includeFields}.
 * <p>
 * Contract: field names must be non-blank Java property names.
 * {@code mapIncludeFieldNull} controls whether a null source value is copied.
 * Does not perform I/O.
 * </p>
 *
 * @author Neha
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class IncludeDataField {

	/**
	 * Source bean property name; must be non-blank when used in a builder.
	 */
	private String sourceField;
	/**
	 * Destination bean property name; must be non-blank when used in a builder.
	 */
	private String destinationField;

	/**
	 * Whether null source values are mapped for this included field.
	 */
	private boolean mapIncludeFieldNull;

}
