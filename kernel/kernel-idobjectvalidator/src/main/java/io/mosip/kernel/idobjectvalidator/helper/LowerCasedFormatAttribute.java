package io.mosip.kernel.idobjectvalidator.helper;

import com.github.fge.jackson.NodeType;
import com.github.fge.jsonschema.core.exceptions.ProcessingException;
import com.github.fge.jsonschema.core.report.ProcessingReport;
import com.github.fge.jsonschema.format.AbstractFormatAttribute;
import com.github.fge.jsonschema.format.FormatAttribute;
import com.github.fge.jsonschema.processors.data.FullData;
import com.github.fge.msgsimple.bundle.MessageBundle;

import io.mosip.kernel.idobjectvalidator.constant.IdObjectValidatorConstant;

public class LowerCasedFormatAttribute extends AbstractFormatAttribute {
	
	private static final FormatAttribute INSTANCE = new LowerCasedFormatAttribute(IdObjectValidatorConstant.FORMAT_LOWERCASED,
			NodeType.STRING, NodeType.values());

	protected LowerCasedFormatAttribute(String fmt, NodeType first, NodeType[] other) {
		super(fmt, first, other);
	}
	
	public static FormatAttribute getInstance() {
	    return INSTANCE;
	}

	@Override
	public void validate(final ProcessingReport report, final  MessageBundle bundle, final FullData data)
	    throws ProcessingException 	{
	    final String value = data.getInstance().getNode().textValue();
	    if(!value.equals(value.toLowerCase())) {
	        report.error(newMsg(data, bundle, IdObjectValidatorConstant.INCORRECT_CASE_MSG_KEY).put("input", value));
	    }
	}

}
