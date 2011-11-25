package org.eclipse.cdt.codan.internal.checkers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.codan.core.cxx.model.AbstractIndexAstChecker;
import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorIncludeStatement;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorStatement;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.index.IIndexBinding;
import org.eclipse.cdt.core.index.IIndexFile;
import org.eclipse.core.runtime.CoreException;

public class IncorrectIncludeChecker extends AbstractIndexAstChecker {
	
	public static final String ER_ID = "org.eclipse.cdt.codan.internal.checkers.IncorrectIncludeChecker"; //$NON-NLS-1$
	
	private Map<String, IncludeGraphEntry> includeStructure = new HashMap<String, IncludeGraphEntry>();
	IncludeGraphEntry root;
	
	class IncludeGraphEntry {
		
		private List<IncludeGraphEntry> referencedIncludes;
		private String name;
		
		public IncludeGraphEntry(String name) {
			this.name = name;
			referencedIncludes = new ArrayList<IncludeGraphEntry>();
		}
		
		public void addReferencedInclude(IncludeGraphEntry entry) {
			referencedIncludes.add(entry);
		}
		
		public String toString() {
			String result = "Entry: " + name + " " + "\r\n";
			for(IncludeGraphEntry entry: referencedIncludes) {
				result += "-" + entry.name + " " + "\r\n";
			}
			return result;
		}
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
					System.out.println("Not instanceof");
					continue;
				}
				
				IASTPreprocessorIncludeStatement include = (IASTPreprocessorIncludeStatement) statement;
				
				IIndexFile indexfile = include.getImportedIndexFile();
				System.out.println("importedindexfile: " + indexfile);
				if(indexfile != null) {
					try {
						System.out.println("Path: " + indexfile.getLocation().getFullPath());
					} catch (CoreException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				
				if(!include.isResolved()) {
					System.out.println("Not resolved!" + include.getRawSignature());
					//TODO: handle this issue
					continue;
				}
				String path = include.getPath();
				if(path == null) {
					System.out.println("Null path!" + include.getRawSignature());
					continue;
				}
				
				IncludeGraphEntry refEntry;
				if(includeStructure.containsKey(path)) {
					refEntry = includeStructure.get(path);
				}
				else {
					refEntry = new IncludeGraphEntry(path);
					includeStructure.put(path, refEntry);
				}
				
				entry.addReferencedInclude(refEntry);
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
//		IDependencyTree tree = ast.getDependencyTree();
//		try {
//			for(IIndexFile file: ast.getIndex().getAllFiles()) {
//				System.out.println(file.getLocation().getFullPath());
//				for(IIndexInclude file2: ast.getIndex().findIncludedBy(file)) {
//					System.out.println("--" + file2.getFullName());
//				}
//			}
//		} catch (CoreException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		System.out.println("tree: " + tree);
//		for(IASTInclusionNode node: tree.getInclusions()) {
//			System.out.println(node.getIncludeDirective().getRawSignature());
//		}
		//isHeaderUnit
		//ast.getIndex().findIncludes(file);
		//ast.accept(new IncludeASTVisitor(includeStructure));
		
		
		
		
//		IASTPreprocessorIncludeStatement[] includes = ast.getIncludeDirectives();
//		for(IASTPreprocessorIncludeStatement include: includes) {
//			System.out.println(include.getRawSignature());
//			if(include.getImportedIndexFile() != null) {
//				try {
//					System.out.println(include.getImportedIndexFile().getLocation().getURI().toString());
//				} catch (CoreException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//			}
//			else {
//				System.out.println("indexfile is null :(");
//			}
//		}
		
		
		
		
		//Hyperlinktest!
//		IWorkingCopyManager manager = CUIPlugin.getDefault().getWorkingCopyManager();
//		IWorkingCopy workingCopy = manager.getWorkingCopy(editor.getEditorInput());
//		IStatus status= ASTProvider.getASTProvider().runOnAST(workingCopy, ASTProvider.WAIT_IF_OPEN, null, new ASTRunnable() {
//			public IStatus runOnAST(ILanguage lang, IASTTranslationUnit ast) {
//				return Status.OK_STATUS;
//			}
//		});
		
		
		
//		IIndex index = ast.getIndex();
//		IASTPreprocessorIncludeStatement[] includes = ast.getIncludeDirectives();
//		for(IASTPreprocessorIncludeStatement include: includes) {
//			System.out.println(include.getRawSignature());
//			//ast.getIndex().resolveInclude(include);
//		}
//		//ProjectIndexerIncludeResolutionHeuristics
//		//((TranslationUnit)ast.getOriginatingTranslationUnit()).
//		ICProject cprj = ast.getOriginatingTranslationUnit().getCProject();
//		try {
//			for(IIncludeReference ref: cprj.getIncludeReferences()) {
//				System.out.println(ref.getAffectedPath().toString());
//				//index.findBinding(IName)
//			}
//		} catch (CModelException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		//ProjectIndexerInputAdapter pathResolver = new ProjectIndexerInputAdapter(cprj);
		//ProjectIndexerIncludeResolutionHeuristics heuristics = new ProjectIndexerIncludeResolutionHeuristics(cprj.getProject(), pathResolver);
		
		
		
		//Poging: via index.getBindings zoeken!
//		ast.accept(new ASTVisitor() {
//			{
//				shouldVisitNames = true;
//				shouldVisitDeclarations = true;
//				shouldVisitInitializers = true;
//				shouldVisitParameterDeclarations = true;
//				shouldVisitDeclarators = true;
//				shouldVisitDeclSpecifiers = true;
//				shouldVisitArrayModifiers = true;
//				shouldVisitPointerOperators = true;
//				shouldVisitExpressions = true;
//				shouldVisitStatements = true;
//				shouldVisitTypeIds = true;
//				shouldVisitEnumerators = true;
//				shouldVisitTranslationUnit = true;
//				shouldVisitProblems = true;
//				shouldVisitDesignators = true;
//				shouldVisitBaseSpecifiers = true;
//				shouldVisitNamespaces = true;
//				shouldVisitTemplateParameters = true;
//				shouldVisitCaptures= true;
//				includeInactiveNodes= true;
//				shouldVisitAmbiguousNodes = true;
//				shouldVisitImplicitNames = true;
//				shouldVisitImplicitNameAlternates = true;
//			}
//		});
		
//		if(includeStructure.containsKey(ast.getContainingFilename())) {
//			//System.out.println(includeStructure.get(ast.getContainingFilename()).toString());
//		}
		IIndex index = ast.getIndex();
		IASTPreprocessorStatement[] statements = ast.getAllPreprocessorStatements();
		for(IASTPreprocessorStatement statement: statements) {
			if(statement instanceof IASTPreprocessorIncludeStatement) {
				IASTPreprocessorIncludeStatement include = (IASTPreprocessorIncludeStatement) statement;
				System.out.println("include statement info:");
				System.out.println("-----------------------");
				System.out.println(include.getImportedIndexFile());
				System.out.println(include.createsAST());
				System.out.println(include.getName());
				try {
					IIndexBinding binding = index.findBinding(include.getName());
					if(binding != null) {
						System.out.println("Binding: " + binding.getQualifiedName());
					}
				} catch (CoreException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				System.out.println("--- end of include statement info");
			}
		}
	}
}
