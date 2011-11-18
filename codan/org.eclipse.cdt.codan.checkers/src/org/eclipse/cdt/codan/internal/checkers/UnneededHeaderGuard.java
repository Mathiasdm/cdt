/*******************************************************************************
 * Copyright (c) 2011 Mathias De Maré
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Mathias De Maré  - initial implementation
 *******************************************************************************/

package org.eclipse.cdt.codan.internal.checkers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.cdt.codan.core.cxx.model.AbstractIndexAstChecker;
import org.eclipse.cdt.codan.internal.checkers.preprocessor.ASTPreprocessorVisitor;
import org.eclipse.cdt.codan.internal.checkers.preprocessor.NodePreprocessorMap;
import org.eclipse.cdt.codan.internal.checkers.preprocessor.PreprocessorHandler;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorEndifStatement;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorIfndefStatement;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorIncludeStatement;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorStatement;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;

/**
 * Checks if external header guards are present.
 * <p>
 * This should not be present, since well-formed header files should already have internal header guards.
 * @author Mathias De Maré
 *
 */
public class UnneededHeaderGuard extends AbstractIndexAstChecker {
	public static final String ER_ID = "org.eclipse.cdt.codan.internal.checkers.UnneededHeaderGuard"; //$NON-NLS-1$

	public void processAst(IASTTranslationUnit ast) {
		ArrayList<IASTPreprocessorStatement> preprocessorStatements =
				new ArrayList<IASTPreprocessorStatement>(
						Arrays.asList(ast.getAllPreprocessorStatements()));
		PreprocessorHandler handler = new PreprocessorHandler(preprocessorStatements);
		NodePreprocessorMap map = new NodePreprocessorMap();
		
		ASTPreprocessorVisitor visitor = new ASTPreprocessorVisitor(map, handler);
		//Visit AST to add preprocessor statements to map
		ast.accept(visitor);
		ArrayList<ArrayList<IASTPreprocessorStatement>> statementGroups = 
				new ArrayList<ArrayList<IASTPreprocessorStatement>>(map.getMap().values());
		statementGroups.add(handler.getList()); //Add remaining issues
		
		//Determine issues
		for(ArrayList<IASTPreprocessorStatement> statements: statementGroups) {
			List<Issue> issues = getIssues(statements);
			for(Issue issue: issues) {
				reportProblem(ER_ID, issue.getInclude());
			}
		}
	}
	
	/**
	 * External header guard.
	 */
	private class Issue {
		private String define;
		private IASTPreprocessorIncludeStatement include;
		
		/**
		 * Construct a headerguard issue
		 * @param define The String checked in the '#ifndef' statement.
		 * @param include The include statement surrounded by header guards.
		 */
		public Issue(String define, IASTPreprocessorIncludeStatement include) {
			this.define = define;
			this.include = include;
		}
		
		public String getDefine() {
			return define;
		}
		
		public IASTPreprocessorIncludeStatement getInclude() {
			return include;
		}
	}
	
	/**
	 * The possible states when trying to detect an issue.
	 */
	private enum PositionState {
		START, IFNDEF, INCLUDE
	}
	
	/**
	 * Go through the preprocessor statements that directory follow each other, and check if there are any issues.
	 */
	List<Issue> getIssues(ArrayList<IASTPreprocessorStatement> statements) {
		ArrayList<Issue> issues = new ArrayList<Issue>();
		
		PositionState state = PositionState.START;
		String define = null;
		IASTPreprocessorIncludeStatement include = null;
		for(IASTPreprocessorStatement statement: statements) {
			switch(state) {
				case START:
					if(statement instanceof IASTPreprocessorIfndefStatement) {
						state = PositionState.IFNDEF;
						define = new String(((IASTPreprocessorIfndefStatement) statement).getCondition());
					}
					break;
				case IFNDEF:
					if(statement instanceof IASTPreprocessorIncludeStatement) {
						state = PositionState.INCLUDE;
						include = (IASTPreprocessorIncludeStatement) statement;
					}
					else {
						state = PositionState.START;
					}
					break;
				case INCLUDE:
					if(statement instanceof IASTPreprocessorEndifStatement) {
						issues.add(new Issue(define, include));
					}
					state = PositionState.START;
					break;
			}
		}
		return issues;
	}
}
