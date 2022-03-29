package io.mosip.kernel.idobjectvalidator.helper;

import java.util.regex.Pattern;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.fge.jsonschema.core.exceptions.ProcessingException;
import com.github.fge.jsonschema.core.processing.Processor;
import com.github.fge.jsonschema.core.report.ProcessingReport;
import com.github.fge.jsonschema.keyword.validator.AbstractKeywordValidator;
import com.github.fge.jsonschema.processors.data.FullData;
import com.github.fge.msgsimple.bundle.MessageBundle;

import io.mosip.kernel.idobjectvalidator.constant.IdObjectValidatorConstant;

public class ValidatorsKeywordValidator extends AbstractKeywordValidator {

	public ValidatorsKeywordValidator(final JsonNode digest) {
		super(IdObjectValidatorConstant.ATTR_VALIDATORS);
	}

	@Override
	public void validate(Processor<FullData, FullData> processor, ProcessingReport report, MessageBundle bundle,
			FullData data) throws ProcessingException {		
		
		final JsonNode schema = data.getSchema().getNode();
		if(schema.hasNonNull(IdObjectValidatorConstant.ATTR_VALIDATORS)) {
			JsonNode validators = schema.get(IdObjectValidatorConstant.ATTR_VALIDATORS);
			
			for(int i=0;i<validators.size();i++) {
				JsonNode validator = validators.get(i);
				
				if(!isValid(validator, data.getInstance().getNode().asText()))
					report.error(newMsg(data, bundle, IdObjectValidatorConstant.INCORRECT_MATCH)
			                .put("matcher", validator)
			                .put("provided", data.getInstance().getNode()));
			}
		}		
	}

	@Override
	public String toString() {
		return "";
	}	
	
	//TODO --
	private boolean isValid(JsonNode validator, CharSequence data) {
		String type = validator.get("type").asText();
		switch (type) {
			case "regex":
				Pattern pattern = Pattern.compile(validator.get("validator").asText());
				return pattern.matcher(data).matches();			
	
			default: //TODO Nothing to do as of now
				break;
		}
		return true;
	}
}
