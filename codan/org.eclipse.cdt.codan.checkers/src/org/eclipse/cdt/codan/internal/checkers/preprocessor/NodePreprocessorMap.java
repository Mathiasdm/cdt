package org.eclipse.cdt.codan.internal.checkers.preprocessor;

import java.util.ArrayList;
import java.util.HashMap;

import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorStatement;

/**
 * This class keeps a link between preprocessor statements and nodes in the AST.
 */
public class NodePreprocessorMap {
	private HashMap<IASTNode, ArrayList<IASTPreprocessorStatement>> preprocessorStatements =
			new HashMap<IASTNode, ArrayList<IASTPreprocessorStatement>>();		
	/**
	 * Add a preprocessor statement to the map.
	 * Statements are kept with the node in the order they are added.
	 */
	public void addStatement(IASTNode node, IASTPreprocessorStatement statement) {
		ArrayList<IASTPreprocessorStatement> statements = preprocessorStatements.get(node);
		if(statements == null) {
			statements = new ArrayList<IASTPreprocessorStatement>();
		}
		statements.add(statement);
		preprocessorStatements.put(node, statements);
	}
	
	public HashMap<IASTNode, ArrayList<IASTPreprocessorStatement>> getMap() {
		return preprocessorStatements;
	}
}