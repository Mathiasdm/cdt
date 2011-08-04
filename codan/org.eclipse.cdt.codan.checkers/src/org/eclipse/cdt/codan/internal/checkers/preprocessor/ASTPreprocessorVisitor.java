package org.eclipse.cdt.codan.internal.checkers.preprocessor;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorStatement;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;
import org.eclipse.cdt.internal.core.dom.rewrite.util.OffsetHelper;



/**
 * This class adds preprocessor statements to the AST structure by visiting it.
 *
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
	
	public ASTPreprocessorVisitor(NodePreprocessorMap map, PreprocessorHandler handler) {
		this.map = map;
		this.handler = handler;
	}
	
	@Override
	public int visit(IASTName name) {
		return appendPreprocessorStatements((IASTNode) name);
	}
	
	@Override
	public int visit(IASTDeclaration declaration) {
		return appendPreprocessorStatements((IASTNode) declaration);
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
	
	/**
	 * Check if the preprocessor statement occurs before the node.
	 */
	public boolean isLeading(IASTNode node, IASTPreprocessorStatement statement) {
		ASTNode n = (ASTNode) node;
		ASTNode stat = (ASTNode) statement;
		if(OffsetHelper.getNodeEndPoint(stat) <= OffsetHelper.getNodeOffset(n)) {
			return true;
		}
		return false;
	}
}