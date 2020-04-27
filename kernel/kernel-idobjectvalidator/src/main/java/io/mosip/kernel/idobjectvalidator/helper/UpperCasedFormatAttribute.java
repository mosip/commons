package io.mosip.kernel.idobjectvalidator.helper;

import com.github.fge.jackson.NodeType;
import com.github.fge.jsonschema.core.exceptions.ProcessingException;
import com.github.fge.jsonschema.core.report.ProcessingReport;
import com.github.fge.jsonschema.format.AbstractFormatAttribute;
import com.github.fge.jsonschema.format.FormatAttribute;
import com.github.fge.jsonschema.processors.data.FullData;
import com.github.fge.msgsimple.bundle.MessageBundle;

import io.mosip.kernel.idobjectvalidator.constant.IdObjectValidatorConstant;

public class UpperCasedFormatAttribute extends AbstractFormatAttribute {
	
	private static final FormatAttribute INSTANCE = new UpperCasedFormatAttribute(IdObjectValidatorConstant.FORMAT_LOWERCASED,
			NodeType.STRING, NodeType.values());

	protected UpperCasedFormatAttribute(String fmt, NodeType first, NodeType[] other) {
		super(fmt, first, other);
	}

	@Override
	public void validate(ProcessingReport report, MessageBundle bundle, FullData data) throws ProcessingException {
		final String value = data.getInstance().getNode().textValue();
	    if(!value.equals(value.toLowerCase())) {
	        report.error(newMsg(data, bundle, IdObjectValidatorConstant.INCORRECT_CASE_MSG_KEY).put("input", value));
	    }
	}
	
	public static FormatAttribute getInstance() {
	    return INSTANCE;
	}

}
