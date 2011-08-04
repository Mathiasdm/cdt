package org.eclipse.cdt.codan.internal.checkers.preprocessor;

import java.util.ArrayList;

import org.eclipse.cdt.core.dom.ast.IASTPreprocessorStatement;



/**
 * This class keeps a list of preprocessor statements, to remember which statements still need to be processed.
 *
 */
public class PreprocessorHandler {
	private ArrayList<IASTPreprocessorStatement> statements = null;
	
	public PreprocessorHandler(ArrayList<IASTPreprocessorStatement> statements) {
		if(statements != null) {
			this.statements = new ArrayList<IASTPreprocessorStatement>(statements);
		}
	}
	
	void added(IASTPreprocessorStatement statement) {
		statements.remove(statement);
	}
	
	public boolean hasMore() {
		return (statements.size() > 0);
	}
	
	IASTPreprocessorStatement getFirst() {
		return statements.get(0);
	}
	
	int size() {
		return statements.size();
	}
	
	public ArrayList<IASTPreprocessorStatement> getList() {
		return new ArrayList<IASTPreprocessorStatement>(statements);
	}
	
}