package org.eclipse.cdt.codan.internal.checkers;

import org.eclipse.cdt.codan.core.cxx.model.AbstractIndexAstChecker;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;

public class IncorrectIncludeChecker extends AbstractIndexAstChecker {
	public void processAst(IASTTranslationUnit ast) {
		/*
		 * TODO:
		 * 1. Build up a directed graph structure of includes
		 * 2. Check which includes are used directly
		 * (by going over all of the types used in the .c/cpp)
		 * 3. Mark:
		 *     * Files directly included but _not_ used
		 *     * Files used but _not_ directly included
		 */
	}
}
