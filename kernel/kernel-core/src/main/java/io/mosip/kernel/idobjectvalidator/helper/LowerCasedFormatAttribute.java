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
 * JSON-schema format that requires the instance string to be all lowercase.
 */
public class LowerCasedFormatAttribute extends AbstractFormatAttribute {
	
	private static final FormatAttribute INSTANCE = new LowerCasedFormatAttribute(IdObjectValidatorConstant.FORMAT_LOWERCASED,
			NodeType.STRING, NodeType.values());

	/**
	 * Creates a format named {@code fmt} for {@code first} and {@code other} node types.
	 *
	 * @param fmt   format name
	 * @param first primary node type
	 * @param other additional node types
	 */
	protected LowerCasedFormatAttribute(String fmt, NodeType first, NodeType[] other) {
		super(fmt, first, other);
	}
	
	/**
	 * Returns the singleton instance.
	 *
	 * @return shared format attribute
	 */
	public static FormatAttribute getInstance() {
	    return INSTANCE;
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
	public void validate(final ProcessingReport report, final  MessageBundle bundle, final FullData data)
	    throws ProcessingException 	{
	    final String value = data.getInstance().getNode().textValue();
	    if(!value.equals(value.toLowerCase())) {
	        report.error(newMsg(data, bundle, IdObjectValidatorConstant.INCORRECT_CASE_MSG_KEY).put("input", value));
	    }
	}

}
