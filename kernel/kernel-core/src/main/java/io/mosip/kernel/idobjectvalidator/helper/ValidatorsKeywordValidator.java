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

/**
 * JSON-schema keyword validator for MOSIP {@code validators} arrays.
 * <p>
 * Each array item with {@code type=regex} is compiled and matched against the
 * instance text. Other types currently pass.
 * </p>
 */
public class ValidatorsKeywordValidator extends AbstractKeywordValidator {

	/**
	 * Creates a validator for the {@code validators} keyword.
	 *
	 * @param digest digested schema node (unused beyond keyword name)
	 */
	public ValidatorsKeywordValidator(final JsonNode digest) {
		super(IdObjectValidatorConstant.ATTR_VALIDATORS);
	}

	/**
	 * Reports {@link IdObjectValidatorConstant#INCORRECT_MATCH} when a regex validator fails.
	 *
	 * @param processor schema processor
	 * @param report    processing report
	 * @param bundle    message bundle
	 * @param data      instance being validated
	 * @throws ProcessingException if the report cannot be updated
	 */
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

	/**
	 * Returns an empty string (fge requires {@link #toString()}).
	 *
	 * @return empty string
	 */
	@Override
	public String toString() {
		return "";
	}	
	
	/**
	 * Returns whether {@code data} matches {@code validator} ({@code type=regex} only).
	 *
	 * @param validator JSON node with {@code type} and {@code validator}
	 * @param data      instance text
	 * @return {@code true} if the value matches or the type is not {@code regex}
	 */
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
