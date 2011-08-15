package org.eclipse.cdt.codan.internal.checkers.ui.quickfix;

import org.eclipse.cdt.codan.internal.checkers.ui.Messages;
import org.eclipse.cdt.codan.ui.AbstractCodanCMarkerResolution;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.IDocument;

public class QuickFixUnneededHeaderguard extends AbstractCodanCMarkerResolution {
	public String getLabel() {
		// TODO Auto-generated method stub
		return Messages.UnneededHeaderguardQuickFix_Message;
	}

	@Override
	public void apply(IMarker marker, IDocument document) {
		System.out.println(marker.LINE_NUMBER);
		try {
			System.out.println(marker.getAttribute(IMarker.LINE_NUMBER));
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
