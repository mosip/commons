package io.mosip.kernel.idobjectvalidator.helper;

import com.github.fge.jackson.NodeType;
import com.github.fge.jsonschema.cfg.ValidationConfiguration;
import com.github.fge.jsonschema.library.DraftV4Library;
import com.github.fge.jsonschema.library.Keyword;
import com.github.fge.jsonschema.library.Library;
import com.github.fge.jsonschema.main.JsonSchemaFactory;
import com.github.fge.jsonschema.messages.JsonSchemaValidationBundle;
import com.github.fge.msgsimple.bundle.MessageBundle;
import com.github.fge.msgsimple.load.MessageBundles;
import com.github.fge.msgsimple.source.MapMessageSource;
import com.github.fge.msgsimple.source.MessageSource;

import io.mosip.kernel.idobjectvalidator.constant.IdObjectValidatorConstant;


public class IdObjectValidatorHelper {
	
	private static final String URI = "http://json-schema.org/draft-07/schema#";
	
	private static JsonSchemaFactory jsonFactory = null;
	
	public static JsonSchemaFactory getJSONSchemaFactory() {
		if(jsonFactory != null)
			return jsonFactory;
		
		final Keyword fieldType = Keyword.newBuilder(IdObjectValidatorConstant.ATTR_FIELDTYPE)
			    .withSyntaxChecker(new DummySyntaxChecker(IdObjectValidatorConstant.ATTR_FIELDTYPE, NodeType.STRING))
			    .withSimpleDigester(NodeType.STRING, NodeType.values())
			    .freeze();
		
		final Keyword fieldCategory = Keyword.newBuilder(IdObjectValidatorConstant.ATTR_FIELD_CATEGORY)
				    .withSyntaxChecker(new DummySyntaxChecker(IdObjectValidatorConstant.ATTR_FIELD_CATEGORY, NodeType.STRING))
				    .withSimpleDigester(NodeType.STRING, NodeType.values())
				    .freeze();
		
		final Keyword bioAttributesKeyword = Keyword.newBuilder(IdObjectValidatorConstant.ATTR_BIO)
				.withSyntaxChecker(new DummySyntaxChecker(IdObjectValidatorConstant.ATTR_BIO, NodeType.ARRAY))
				.withSimpleDigester(NodeType.ARRAY, NodeType.values())
			    .freeze();
		
		final Keyword validatorsKeyword = Keyword.newBuilder(IdObjectValidatorConstant.ATTR_VALIDATORS)
				.withSyntaxChecker(new DummySyntaxChecker(IdObjectValidatorConstant.ATTR_VALIDATORS, NodeType.ARRAY))
				.withSimpleDigester(NodeType.ARRAY, NodeType.values())
			    .withValidatorClass(ValidatorsKeywordValidator.class)
			    .freeze();		
		
		final MessageSource source = MapMessageSource.newBuilder().put(IdObjectValidatorConstant.INCORRECT_CASE_MSG_KEY, 
        		IdObjectValidatorConstant.INCORRECT_CASE_MSG_VALUE).build();
        final MessageBundle bundle = MessageBundles.getBundle(JsonSchemaValidationBundle.class).thaw().appendSource(source).freeze();
		
		final Library library = DraftV4Library.get()
				.thaw()
				.addKeyword(fieldType)
				.addKeyword(fieldCategory)
				.addKeyword(validatorsKeyword)
				.addKeyword(bioAttributesKeyword)
				.addFormatAttribute(IdObjectValidatorConstant.FORMAT_LOWERCASED, LowerCasedFormatAttribute.getInstance())
				.addFormatAttribute(IdObjectValidatorConstant.FORMAT_UPPERCASED, UpperCasedFormatAttribute.getInstance())
				.freeze();
		
		final ValidationConfiguration cfg = ValidationConfiguration.newBuilder()
			    .setDefaultLibrary(URI, library)
			    .setValidationMessages(bundle)
			    .freeze();
		
		jsonFactory = JsonSchemaFactory.newBuilder().setValidationConfiguration(cfg).freeze();
		return jsonFactory;
	}

}
