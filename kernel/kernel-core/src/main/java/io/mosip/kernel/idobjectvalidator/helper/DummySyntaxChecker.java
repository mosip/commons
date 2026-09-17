package io.mosip.kernel.idobjectvalidator.helper;

import java.util.Collection;

import com.github.fge.jackson.NodeType;
import com.github.fge.jackson.jsonpointer.JsonPointer;
import com.github.fge.jsonschema.core.exceptions.ProcessingException;
import com.github.fge.jsonschema.core.keyword.syntax.checkers.AbstractSyntaxChecker;
import com.github.fge.jsonschema.core.report.ProcessingReport;
import com.github.fge.jsonschema.core.tree.SchemaTree;
import com.github.fge.msgsimple.bundle.MessageBundle;

/**
 * No-op JSON-schema syntax checker for MOSIP-specific keywords.
 * <p>
 * Keywords such as {@code fieldType} are not in Draft-04; this checker accepts
 * them without validating keyword syntax.
 * </p>
 */
public class DummySyntaxChecker extends AbstractSyntaxChecker {
	
	/**
	 * Creates a checker for {@code keyword} whose value is {@code first}.
	 *
	 * @param keyword schema keyword name
	 * @param first   expected JSON node type of the keyword value
	 */
	protected DummySyntaxChecker(String keyword, NodeType first) {
		super(keyword, first);		
	}

	/**
	 * Accepts any keyword value without reporting syntax errors.
	 *
	 * @param pointers collected JSON pointers (unused)
	 * @param bundle   message bundle (unused)
	 * @param report   processing report (unused)
	 * @param tree     schema tree (unused)
	 * @throws ProcessingException unused checked signature
	 */
	@Override
	protected void checkValue(Collection<JsonPointer> pointers, MessageBundle bundle, ProcessingReport report,
			SchemaTree tree) throws ProcessingException {		
	}

}
