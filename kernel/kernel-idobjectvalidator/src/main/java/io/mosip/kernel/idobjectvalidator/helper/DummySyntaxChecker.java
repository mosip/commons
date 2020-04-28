package io.mosip.kernel.idobjectvalidator.helper;

import java.util.Collection;

import com.github.fge.jackson.NodeType;
import com.github.fge.jackson.jsonpointer.JsonPointer;
import com.github.fge.jsonschema.core.exceptions.ProcessingException;
import com.github.fge.jsonschema.core.keyword.syntax.checkers.AbstractSyntaxChecker;
import com.github.fge.jsonschema.core.report.ProcessingReport;
import com.github.fge.jsonschema.core.tree.SchemaTree;
import com.github.fge.msgsimple.bundle.MessageBundle;

public class DummySyntaxChecker extends AbstractSyntaxChecker {
	
	protected DummySyntaxChecker(String keyword, NodeType first) {
		super(keyword, first);		
	}

	@Override
	protected void checkValue(Collection<JsonPointer> pointers, MessageBundle bundle, ProcessingReport report,
			SchemaTree tree) throws ProcessingException {		
	}

}
