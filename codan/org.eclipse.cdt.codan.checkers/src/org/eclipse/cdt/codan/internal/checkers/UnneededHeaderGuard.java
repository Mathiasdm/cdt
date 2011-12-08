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

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.cdt.codan.core.cxx.model.AbstractIndexAstChecker;
import org.eclipse.cdt.codan.internal.checkers.preprocessor.ASTPreprocessorVisitor;
import org.eclipse.cdt.codan.internal.checkers.preprocessor.NodePreprocessorMap;
import org.eclipse.cdt.codan.internal.checkers.preprocessor.PreprocessorHandler;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorElifStatement;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorElseStatement;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorEndifStatement;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorIfStatement;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorIfdefStatement;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorIfndefStatement;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorIncludeStatement;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorMacroDefinition;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorStatement;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.CoreModelUtil;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.core.runtime.CoreException;

/**
 * Checks if unneeded external header guards are present.
 * <p>
 * An external header guard is unneeded if an included header already has guards itself.
 * @author Mathias De Maré
 *
 */
public class UnneededHeaderGuard extends AbstractIndexAstChecker {
	public static final String ER_ID = "org.eclipse.cdt.codan.internal.checkers.UnneededHeaderGuard"; //$NON-NLS-1$

	public void processAst(IASTTranslationUnit ast) {
		ArrayList<IASTPreprocessorStatement> preprocessorStatements =
				new ArrayList<IASTPreprocessorStatement>(
						Arrays.asList(ast.getAllPreprocessorStatements()));
		List<CandidateIssue> candidateIssues = getCandidateIssues(preprocessorStatements);
		
		//Check if the candidate issues are actual issues
		for(CandidateIssue candidate: candidateIssues) {
			if(isIssue(candidate)) {
				reportProblem(ER_ID, candidate.getInclude());
			}
		}
	}
	
	/**
	 * Potential unneeded header guard.
	 */
	private class CandidateIssue {
		private String define;
		private IASTPreprocessorIncludeStatement include;
		
		/**
		 * Construct a candidate issue
		 * @param define The String checked in the '#ifndef' statement.
		 * @param include The include statement surrounded by header guards.
		 */
		public CandidateIssue(String define, IASTPreprocessorIncludeStatement include) {
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
	 * Go through the preprocessor statements that directory follow each other, and check
	 * if there are any potential issues.
	 */
	List<CandidateIssue> getCandidateIssues(ArrayList<IASTPreprocessorStatement> statements) {
		ArrayList<CandidateIssue> candidateIssues = new ArrayList<CandidateIssue>();
		
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
						candidateIssues.add(new CandidateIssue(define, include));
					}
					state = PositionState.START;
					break;
			}
		}
		return candidateIssues;
	}
	
	/**
	 * Check if the potential issue is an actual issue by checking the included header.
	 */
	private boolean isIssue(CandidateIssue candidate) {
		IASTTranslationUnit ast = getASTForCandidateIssue(candidate);
		
		if(ast == null) {
			return false; //We can't generate an AST, so no way to make sure the issue is real
		}

		ArrayList<IASTPreprocessorStatement> preprocessorStatements =
				new ArrayList<IASTPreprocessorStatement>(
						Arrays.asList(ast.getAllPreprocessorStatements()));
		PreprocessorHandler handler = new PreprocessorHandler(preprocessorStatements);
		NodePreprocessorMap map = new NodePreprocessorMap();

		ASTPreprocessorVisitor visitor = new ASTPreprocessorVisitor(map, handler);

		//Add preprocessor statements
		ast.accept(visitor);
		
		//We need at least #ifndef, #define and #endif
		if(preprocessorStatements.size() < 3) {
			return false;
		}
		
		//The 2 first and the last statement should be #ifndef, #define and #endif and use the correct define string
		if(!isFirstPreprocessorStatementCorrect(preprocessorStatements.get(0), candidate.getDefine())
				|| !isSecondPreprocessorStatementCorrect(preprocessorStatements.get(1), candidate.getDefine())
				|| !isLastPreprocessorStatementCorrect(preprocessorStatements.get(preprocessorStatements.size() - 1))) {
			return false;
		}

		//Only the first #ifndef and last #endif should be at 'level 0'. All other preprocessor statements should be inside these 2.
		if(!isToplevelStatementCountCorrect(preprocessorStatements)) {
			return false;
		}

		IASTNode[] nodes = ast.getChildren();
		if(nodes.length > 0) {
			//#ifndef and #define need to be before the first node
			IASTNode node = nodes[0];
			ArrayList<IASTPreprocessorStatement> statements = map.getMap().get(node);
			if((statements == null) || (statements.size() < 2)) {
				return false;
			}
			//We need to have preprocessor statements at the end (to make sure the last '#endif' is below any IASTNode)
			if(!handler.hasMore()) {
				return false;
			}
		}

		return true; //All checks passed
	}

	/**
	 * Check if the amount of top level preprocessor statements is correct
	 * (only the first '#ifndef' and the last '#endif' can be at the top level).
	 */
	private boolean isToplevelStatementCountCorrect(ArrayList<IASTPreprocessorStatement> preprocessorStatements) {
		int indentationLevel = 0;
		int statementsAtLowestLevel = 0;
		for(int i=0; i<preprocessorStatements.size(); i++) {
			IASTPreprocessorStatement statement = preprocessorStatements.get(i);

			if(statement instanceof IASTPreprocessorElifStatement
					|| statement instanceof IASTPreprocessorElseStatement
					|| statement instanceof IASTPreprocessorEndifStatement) {
				indentationLevel--;
			}

			if(indentationLevel == 0) {
				statementsAtLowestLevel++;
			}

			if(statement instanceof IASTPreprocessorIfndefStatement 
					|| statement instanceof IASTPreprocessorIfdefStatement
					|| statement instanceof IASTPreprocessorIfStatement
					|| statement instanceof IASTPreprocessorElifStatement
					|| statement instanceof IASTPreprocessorElseStatement) {
				indentationLevel++;
			}
		}

		if(statementsAtLowestLevel != 2) {
			return false;
		}
		return true;
	}

	/**
	 * Get the AST for this potential issue. If it is not possible to find the AST, null is returned.
	 */
	private IASTTranslationUnit getASTForCandidateIssue(CandidateIssue candidate) {
		IASTPreprocessorIncludeStatement include = candidate.getInclude();
		
		if(!include.isResolved()) {
			//We cannot find the include, so we can't evaluate it
			return null;
		}
		
		String path = include.getPath();
		
		try {
			ITranslationUnit tu = CoreModelUtil.findTranslationUnitForLocation(new URI(path), null);
			if(tu == null) {
				return null; //Didn't find translation unit
			}
			IASTTranslationUnit ast = tu.getAST();
			return ast;
		} catch (CModelException e) {
			return null; //Failed to find a translation unit
		} catch (URISyntaxException e) {
			return null; //The path has incorrect syntax
		} catch (CoreException e) {
			return null; //Failed to generate the AST
		}
	}
	
	/**
	 * Check if the first preprocessor statement in the included header is correct.
	 * It needs to be an #ifndef and needs to have to correct define string.
	 */
	private boolean isFirstPreprocessorStatementCorrect(IASTPreprocessorStatement statement, String define) {
		if(!(statement instanceof IASTPreprocessorIfndefStatement)) {
			return false;
		}

		String condition = new String(((IASTPreprocessorIfndefStatement) statement).getCondition());
		if(!define.equals(condition)) {
			return false;
		}
		return true;
	}
	
	/**
	 * Check if the second preprocessor statement in the included header is correct.
	 * It needs to be an #define and needs to define the correct string.
	 */
	private boolean isSecondPreprocessorStatementCorrect(IASTPreprocessorStatement statement, String define) {
		if(!(statement instanceof IASTPreprocessorMacroDefinition)) {
			return false;
		}

		IASTPreprocessorMacroDefinition macroDefinition = (IASTPreprocessorMacroDefinition) statement;
		if(!define.equals(macroDefinition.getName().toString())) {
			return false;
		}
		return true;
	}
	
	/**
	 * The last preprocessor statement needs to be an #endif
	 */
	private boolean isLastPreprocessorStatementCorrect(IASTPreprocessorStatement statement) {
		if(!(statement instanceof IASTPreprocessorEndifStatement)) {
			return false;
		}
		return true;
	}
}
