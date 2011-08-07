package org.eclipse.cdt.codan.internal.checkers;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.eclipse.cdt.codan.core.cxx.model.AbstractIndexAstChecker;
import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTName;
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
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;
import org.eclipse.cdt.internal.core.dom.rewrite.util.OffsetHelper;
import org.eclipse.core.runtime.CoreException;

/**
 * Checker to see if extra header guards are unneeded.
 * @author Mathias De Maré
 *
 */
public class UnneededHeaderGuard extends AbstractIndexAstChecker {
	public static final String ER_ID = "org.eclipse.cdt.codan.internal.checkers.UnneededHeaderGuard"; //$NON-NLS-1$
	
	/**
	 * This class keeps a link between preprocessor statements and nodes in the AST.
	 */
	private class NodePreprocessorMap {
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
	
	/**
	 * This class keeps a list of preprocessor statements, to remember which statements still need to be processed.
	 *
	 */
	private class PreprocessorHandler {
		private ArrayList<IASTPreprocessorStatement> statements = null;
		
		public PreprocessorHandler(ArrayList<IASTPreprocessorStatement> statements) {
			if(statements != null) {
				this.statements = new ArrayList<IASTPreprocessorStatement>(statements);
			}
		}
		
		void added(IASTPreprocessorStatement statement) {
			statements.remove(statement);
		}
		
		boolean hasMore() {
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
	
	/**
	 * This class adds preprocessor statements to the AST structure by visiting it.
	 *
	 */
	private class ASTPreprocessorVisitor extends ASTVisitor {
		
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
	
	/**
	 * 1. Execute the visitor pattern (assuming visiting goes from top to bottom):
	 * For each preprocessor statement:
	 * 	- If it is before the node, add it as leading
	 *  - If it is after the node, add it as trailing
	 *  (No need to add preprocessor statements as freestanding in our case, since it doesn't matter
	 * 2. For each node:
	 *  if the preprocessor statements are formed correctly (#ifndef, #include, #endif),
	 *  mark it as a candidate.
	 * 3. For each candidate, check if the #include file starts and ends with the same #ifndef and #endif.
	 *    If yes: place a codan warning.
	 */
	public void processAst(IASTTranslationUnit ast) {
		ArrayList<IASTPreprocessorStatement> preprocessorStatements =
				new ArrayList<IASTPreprocessorStatement>(
						Arrays.asList(ast.getAllPreprocessorStatements()));
		PreprocessorHandler handler = new PreprocessorHandler(preprocessorStatements);
		NodePreprocessorMap map = new NodePreprocessorMap();
		
		ASTPreprocessorVisitor visitor = new ASTPreprocessorVisitor(map, handler);
		//Visit AST to add preprocessor statements to map
		ast.accept(visitor);
		
		//Determine candidate issues
		List<CandidateIssue> candidateIssues = new ArrayList<CandidateIssue>();
		for(IASTNode key: map.getMap().keySet()) { //Append issues related to nodes
			ArrayList<IASTPreprocessorStatement> statements = map.getMap().get(key);
			List<CandidateIssue> candidateIssuesForNode = getCandidateIssues(statements);
			candidateIssues.addAll(candidateIssuesForNode);
		}
		candidateIssues.addAll(getCandidateIssues(handler.getList())); //Append issues that are not before any node
		
		//Check if the candidate issues are actual issues
		for(CandidateIssue candidate: candidateIssues) {
			if(isIssue(candidate)) {
				reportProblem(ER_ID, candidate.getInclude());
			}
		}
	}
	
	private class CandidateIssue {
		private String define;
		private IASTPreprocessorIncludeStatement include;
		
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
		START, IFNDEF, INCLUDE, ENDIF
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
						state = PositionState.ENDIF;
					}
					else {
						state = PositionState.START;
					}
			}
			if(state == PositionState.ENDIF) {
				candidateIssues.add(new CandidateIssue(define, include));
				state = PositionState.START;
			}
		}
		return candidateIssues;
	}
	
	private boolean isIssue(CandidateIssue candidate) {
		try {
			IASTPreprocessorIncludeStatement include = candidate.getInclude();
			
			if(!include.isResolved()) {
				//We cannot find the include, so we can't evaluate it
				return false;
			}
			
			String path = include.getPath();
			
			System.out.println(path);
			
			ITranslationUnit tu = CoreModelUtil.findTranslationUnitForLocation(new URI(path), null);
			if(tu == null) {
				return false; //Didn't find translation unit
			}
			IASTTranslationUnit ast = tu.getAST();
			
			ArrayList<IASTPreprocessorStatement> preprocessorStatements =
					new ArrayList<IASTPreprocessorStatement>(
							Arrays.asList(ast.getAllPreprocessorStatements()));
			PreprocessorHandler handler = new PreprocessorHandler(preprocessorStatements);
			NodePreprocessorMap map = new NodePreprocessorMap();
			
			ASTPreprocessorVisitor visitor = new ASTPreprocessorVisitor(map, handler);
			
			ast.accept(visitor);
			
			//First check structure of preprocessor statements
			//* First statement: #ifndef
			//* Second statement: #define
			//* Last statement: #endif
			//* No statement should be at the same 'level 0' as the first and last statement
			if(preprocessorStatements.size() < 3) {
				return false;
			}
			
			int indentationLevel = 0;
			int statementsAtLowestLevel = 0;
			for(int i=0; i<preprocessorStatements.size(); i++) {
				IASTPreprocessorStatement statement = preprocessorStatements.get(i);
				if(i == 0) {
					if(!(statement instanceof IASTPreprocessorIfndefStatement)) {
						return false;
					}
					
					String condition = new String(((IASTPreprocessorIfndefStatement) statement).getCondition());
					if(!candidate.getDefine().equals(condition)) {
						return false;
					}
				}
				else if(i == 1) {
					if(!(statement instanceof IASTPreprocessorMacroDefinition)) {
						return false;
					}
					
					IASTPreprocessorMacroDefinition macroDefinition = (IASTPreprocessorMacroDefinition) statement;
					if(!candidate.getDefine().equals(macroDefinition.getName().toString())) {
						return false;
					}
				}
				else if(i == preprocessorStatements.size() - 1) {
					if(!(statement instanceof IASTPreprocessorEndifStatement)) {
						return false;
					}
				}
				
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
			
			if(statementsAtLowestLevel > 2) {
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
			}
			
			//We need to have preprocessor statements at the end
			if(!handler.hasMore()) {
				return false;
			}
			
			return true; //All checks passed
			
		} catch (CModelException e) {
			return false; //Failed to find a translation unit
		} catch (URISyntaxException e) {
			return false; //Issue with the path
		} catch (CoreException e) {
			return false; //Failed to generate full AST
		}
	}
}
