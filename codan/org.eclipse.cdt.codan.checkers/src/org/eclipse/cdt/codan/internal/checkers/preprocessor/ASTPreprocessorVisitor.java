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

package org.eclipse.cdt.codan.internal.checkers.preprocessor;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTArrayModifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTEnumerationSpecifier.IASTEnumerator;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTInitializer;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTPointerOperator;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorStatement;
import org.eclipse.cdt.core.dom.ast.IASTProblem;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IASTTypeId;
import org.eclipse.cdt.core.dom.ast.c.ICASTDesignator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCapture;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier.ICPPASTBaseSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamespaceDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateParameter;


/**
 * This class adds preprocessor statements to the AST structure by visiting it.
 */
public class ASTPreprocessorVisitor extends ASTVisitor {
	
	private NodePreprocessorMap map;
	private PreprocessorHandler handler;
	
	{
		shouldVisitExpressions = true;
		shouldVisitStatements = true;
		shouldVisitNames = true;
		shouldVisitDeclarations = true;
		shouldVisitDeclSpecifiers = true;
		shouldVisitDeclarators = true;
		shouldVisitInitializers = true;
		shouldVisitBaseSpecifiers = true;
		shouldVisitNamespaces = true;
		shouldVisitTemplateParameters = true;
		shouldVisitParameterDeclarations = true;
	}
	
	@Override
	public int visit(IASTTranslationUnit tu) {
		return appendPreprocessorStatements(tu);
	}

	@Override
	public int visit(IASTInitializer initializer) {
		return appendPreprocessorStatements(initializer);
	}

	@Override
	public int visit(IASTParameterDeclaration parameterDeclaration) {
		return appendPreprocessorStatements(parameterDeclaration);
	}

	@Override
	public int visit(IASTDeclarator declarator) {
		return appendPreprocessorStatements(declarator);
	}

	@Override
	public int visit(IASTDeclSpecifier declSpec) {
		return appendPreprocessorStatements(declSpec);
	}

	@Override
	public int visit(IASTArrayModifier arrayModifier) {
		return appendPreprocessorStatements(arrayModifier);
	}

	@Override
	public int visit(IASTPointerOperator ptrOperator) {
		return appendPreprocessorStatements(ptrOperator);
	}

	@Override
	public int visit(IASTExpression expression) {
		return appendPreprocessorStatements(expression);
	}

	@Override
	public int visit(IASTStatement statement) {
		return appendPreprocessorStatements(statement);
	}

	@Override
	public int visit(IASTTypeId typeId) {
		return appendPreprocessorStatements(typeId);
	}

	@Override
	public int visit(IASTEnumerator enumerator) {
		return appendPreprocessorStatements(enumerator);
	}

	@Override
	public int visit(IASTProblem problem) {
		return appendPreprocessorStatements(problem);
	}

	@Override
	public int visit(ICPPASTBaseSpecifier baseSpecifier) {
		return appendPreprocessorStatements(baseSpecifier);
	}

	@Override
	public int visit(ICPPASTNamespaceDefinition namespaceDefinition) {
		return appendPreprocessorStatements(namespaceDefinition);
	}

	@Override
	public int visit(ICPPASTTemplateParameter templateParameter) {
		return appendPreprocessorStatements(templateParameter);
	}

	@Override
	public int visit(ICPPASTCapture capture) {
		return appendPreprocessorStatements(capture);
	}

	@Override
	public int visit(ICASTDesignator designator) {
		return appendPreprocessorStatements(designator);
	}
	
	@Override
	public int visit(IASTName name) {
		return appendPreprocessorStatements(name);
	}
	
	@Override
	public int visit(IASTDeclaration declaration) {
		return appendPreprocessorStatements(declaration);
	}

	public ASTPreprocessorVisitor(NodePreprocessorMap map, PreprocessorHandler handler) {
		this.map = map;
		this.handler = handler;
	}
	
	
	
	public int appendPreprocessorStatements(IASTNode node) {
		while(handler.hasMore()) {
			IASTPreprocessorStatement statement = handler.getFirst();
			if(isLeading(node, statement)) {
				map.addStatement(node, statement);
				handler.added(statement);
			}
			else {
				return ASTVisitor.PROCESS_CONTINUE;
			}
		}
		return ASTVisitor.PROCESS_ABORT; //No more, time to stop
	}
	
	public int getNodeOffset(IASTNode node) {
		IASTFileLocation location = node.getFileLocation();
		return location.getNodeOffset();
	}
	
	public int getNodeLength(IASTNode node) {
		IASTFileLocation location = node.getFileLocation();
		return location.getNodeLength();
	}
	
	public int getNodeEndPoint(IASTNode node) {
		return getNodeOffset(node) + getNodeLength(node);
	}
	
	/**
	 * Check if the preprocessor statement occurs before the node.
	 */
	public boolean isLeading(IASTNode node, IASTPreprocessorStatement statement) {
		if(statement.getFileLocation() == null || node.getFileLocation() == null) {
			return false;
		}
		
		if(getNodeEndPoint(statement) <= getNodeOffset(node)) {
			return true;
		}
		return false;
	}
}