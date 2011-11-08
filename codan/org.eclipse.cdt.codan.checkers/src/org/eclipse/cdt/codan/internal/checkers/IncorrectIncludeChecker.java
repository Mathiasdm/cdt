package org.eclipse.cdt.codan.internal.checkers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.codan.core.cxx.model.AbstractIndexAstChecker;
import org.eclipse.cdt.codan.internal.checkers.IncorrectIncludeChecker.IncludeGraphEntry;
import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorIncludeStatement;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorStatement;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;

public class IncorrectIncludeChecker extends AbstractIndexAstChecker {
	
	public static final String ER_ID = "org.eclipse.cdt.codan.internal.checkers.IncorrectIncludeChecker"; //$NON-NLS-1$
	
	private Map<String, IncludeGraphEntry> includeStructure = new HashMap<String, IncludeGraphEntry>();
	
	class IncludeGraphEntry {
		
		private List<IncludeGraphEntry> referencedIncludes;
		private String name;
		
		public IncludeGraphEntry(String name) {
			this.name = name;
			referencedIncludes = new ArrayList<IncludeGraphEntry>();
		}
		
		public void addReferencedIncludes() {
			//TODO
		}
	}
	
	public List<IncludeGraphEntry> getGraphEntries(IASTPreprocessorStatement[] statements) {
		//TODO
	}
	
	class IncludeASTVisitor extends ASTVisitor {
		public IncludeASTVisitor(Map<String, IncludeGraphEntry> includeStructure) {
			shouldVisitTranslationUnit = true;
		}
		
		public int visit(IASTTranslationUnit tu) {
			if(includeStructure.containsKey(tu.getContainingFilename())) {
				//Already processed
				return PROCESS_CONTINUE;
			}
			
			//Add entry
			IncludeGraphEntry entry = new IncludeGraphEntry(tu.getContainingFilename());
			includeStructure.put(tu.getContainingFilename(), entry);
			
			//Add all entries referenced from this entry
			for(IASTPreprocessorStatement statement: tu.getAllPreprocessorStatements()) {
				if(!(statement instanceof IASTPreprocessorIncludeStatement)) {
					continue;
				}
				
				String path = ((IASTPreprocessorIncludeStatement) statement).getPath();
				if(path == null) {
					continue;
				}
				
				//TODO: add statement to containing IncludeGraphEntry
				//TODO: add statement to includeStructure
			}
			
			return PROCESS_CONTINUE;
		}
	}
	
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
		
		ast.accept(new IncludeASTVisitor(includeStructure));
		System.out.println(ast.getContainingFilename());
		//ast.getAllPreprocessorStatements()
	}
}
