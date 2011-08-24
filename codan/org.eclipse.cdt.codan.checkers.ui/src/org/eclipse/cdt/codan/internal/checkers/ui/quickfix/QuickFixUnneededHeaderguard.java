package org.eclipse.cdt.codan.internal.checkers.ui.quickfix;

import org.eclipse.cdt.codan.internal.checkers.ui.CheckersUiActivator;
import org.eclipse.cdt.codan.internal.checkers.ui.Messages;
import org.eclipse.cdt.codan.ui.AbstractAstRewriteQuickFix;
import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorStatement;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ltk.core.refactoring.Change;

public class QuickFixUnneededHeaderguard extends AbstractAstRewriteQuickFix {
	public String getLabel() {
		// TODO Auto-generated method stub
		return Messages.UnneededHeaderguardQuickFix_Message;
	}

	@Override
	public void modifyAST(IIndex index, IMarker marker) {
		String markerFile = marker.getResource().getLocation().toString();
		System.out.println("markerFile: " + markerFile);
		int markerPos = 0;
		try {
			Object pos = marker.getAttribute(IMarker.CHAR_START);
			if(pos instanceof Integer) {
				markerPos = (Integer) pos;
			}
			else {
				return; //Couldn't find marker (TODO: log)
			}
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		IASTTranslationUnit ast;
		try {
			ITranslationUnit tu = getTranslationUnitViaEditor(marker);
			ast = tu.getAST(index, ITranslationUnit.AST_SKIP_INDEXED_HEADERS);
		} catch (CoreException e) {
			CheckersUiActivator.log(e);
			return;
		}
		
		IASTPreprocessorStatement[] statements = ast.getAllPreprocessorStatements();
		
		IASTPreprocessorStatement previous = null;
		IASTPreprocessorStatement next = null;
		
		for(IASTPreprocessorStatement statement: statements) {
			IASTFileLocation loc = statement.getFileLocation();
			System.out.println("Statement location: " + loc.getFileName());
			if(loc == null || markerFile == null || !markerFile.equals(loc.getFileName())) {
				continue;
			}
			
			int offset = loc.getNodeOffset();
			if(offset < markerPos) {
				//Go over all the earlier statements
				//until we find the last one before the marked statement
				previous = statement;
			}
			else if((offset > markerPos) && (next == null)) {
				//Get the first statement behind the marked one
				next = statement;
			}
		}
		ASTRewrite r = ASTRewrite.create(ast);
		if(previous != null && next != null) {
			r.remove(previous, null);
			r.remove(next, null);
		}
		
		Change c = r.rewriteAST();
		try {
			c.perform(new NullProgressMonitor());
			IDocument doc = openDocument(marker);
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try {
			marker.delete();
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
