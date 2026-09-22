package io.mosip.kernel.idobjectvalidator.helper;

import com.github.fge.jackson.NodeType;
import com.github.fge.jsonschema.core.exceptions.ProcessingException;
import com.github.fge.jsonschema.core.report.ProcessingReport;
import com.github.fge.jsonschema.format.AbstractFormatAttribute;
import com.github.fge.jsonschema.format.FormatAttribute;
import com.github.fge.jsonschema.processors.data.FullData;
import com.github.fge.msgsimple.bundle.MessageBundle;

import io.mosip.kernel.idobjectvalidator.constant.IdObjectValidatorConstant;

/**
 * JSON-schema format intended to require uppercase text.
 * <p>
 * The current {@link #validate} implementation compares against
 * {@code toLowerCase()} (same as {@link LowerCasedFormatAttribute}).
 * </p>
 */
public class UpperCasedFormatAttribute extends AbstractFormatAttribute {
	
	private static final FormatAttribute INSTANCE = new UpperCasedFormatAttribute(IdObjectValidatorConstant.FORMAT_LOWERCASED,
			NodeType.STRING, NodeType.values());

	/**
	 * Creates a format named {@code fmt} for {@code first} and {@code other} node types.
	 *
	 * @param fmt   format name
	 * @param first primary node type
	 * @param other additional node types
	 */
	protected UpperCasedFormatAttribute(String fmt, NodeType first, NodeType[] other) {
		super(fmt, first, other);
	}

	/**
	 * Reports {@link IdObjectValidatorConstant#INCORRECT_CASE_MSG_KEY} when the value is not lowercase.
	 *
	 * @param report processing report
	 * @param bundle message bundle
	 * @param data   instance being validated
	 * @throws ProcessingException if the report cannot be updated
	 */
	@Override
	public void validate(ProcessingReport report, MessageBundle bundle, FullData data) throws ProcessingException {
		final String value = data.getInstance().getNode().textValue();
	    if(!value.equals(value.toLowerCase())) {
	        report.error(newMsg(data, bundle, IdObjectValidatorConstant.INCORRECT_CASE_MSG_KEY).put("input", value));
	    }
	}

	/**
	 * Returns the singleton instance.
	 *
	 * @return shared format attribute
	 */
	public static FormatAttribute getInstance() {
	    return INSTANCE;
	}

}
