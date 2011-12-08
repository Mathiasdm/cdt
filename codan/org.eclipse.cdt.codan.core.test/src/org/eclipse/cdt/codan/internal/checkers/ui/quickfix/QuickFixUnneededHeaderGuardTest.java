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

package org.eclipse.cdt.codan.internal.checkers.ui.quickfix;

import java.io.IOException;

import org.eclipse.cdt.codan.ui.AbstractCodanCMarkerResolution;
import org.eclipse.core.runtime.CoreException;

public class QuickFixUnneededHeaderGuardTest extends QuickFixTestCase {
	@Override
	@SuppressWarnings("restriction")
	protected AbstractCodanCMarkerResolution createQuickFix() {
		// TODO Auto-generated method stub
		return new QuickFixUnneededHeaderGuard();
	}

	// @file:header.h
	// #ifndef ABC_H
	// #include "includedheader.h" //Warning
	// #endif
	// int blah();
	@SuppressWarnings({ "restriction", "nls" })
	public void testSimple() throws IOException, CoreException {
		setQuickFix(new QuickFixUnneededHeaderGuard());
		loadcode(getAboveComment());
		String result = runQuickFixOneFile();
		assertFalse(result.contains("#ifndef"));
		assertFalse(result.contains("#endif"));
	}
}
