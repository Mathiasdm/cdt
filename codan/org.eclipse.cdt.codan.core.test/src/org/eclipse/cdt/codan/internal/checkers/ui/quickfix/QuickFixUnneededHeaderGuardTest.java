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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.eclipse.cdt.codan.ui.AbstractCodanCMarkerResolution;

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
	public void testSimple() {
		setQuickFix(new QuickFixUnneededHeaderGuard());
		StringBuilder[] code = getContents(1);
		File f1 = loadcode(code[0].toString());

		runCodan();
		doRunQuickFix();
		String result = getContents(f1);
		assertFalse(result.contains("#ifndef"));
		assertFalse(result.contains("#endif"));
	}

	// @file:somewhere.cpp
	// #ifndef INCLUDE_H
	// #include "include.h"
	// int foo();
	// #endif
	@SuppressWarnings({ "restriction", "nls" })
	public void testFalsePositive() {
		setQuickFix(new QuickFixUnneededHeaderGuard());
		StringBuilder[] code = getContents(1);
		File f1 = loadcode(code[0].toString());

		runCodan();
		doRunQuickFix();
		String result = getContents(f1);
		assertNotNull(result);
		assertTrue(result.contains("#ifndef"));
		assertTrue(result.contains("#endif"));
	}

	private String getContents(File f1) {
		try {
			BufferedReader reader = new BufferedReader(new FileReader(f1));
			String contents = ""; //$NON-NLS-1$
			String line;
			while((line = reader.readLine()) != null) {
				contents += line + System.getProperty("line.separator"); //$NON-NLS-1$
			}
			return contents;
		} catch (FileNotFoundException e1) {
			return null;
		} catch (IOException e) {
			return null;
		}
	}
}
