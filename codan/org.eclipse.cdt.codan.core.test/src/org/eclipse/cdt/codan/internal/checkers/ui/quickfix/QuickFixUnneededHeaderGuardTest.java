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

	// @file:includedheader.h
	// #ifndef ABC_H
	// #define ABC_H
	// int foo();
	// #endif
	/* ---- */
	// @file:header.h
	// #ifndef ABC_H
	// #include "includedheader.h" //Warning
	// #endif
	// int blah();
	@SuppressWarnings({ "restriction", "nls" })
	public void testSimple() {
		setQuickFix(new QuickFixUnneededHeaderGuard());
		StringBuilder[] code = getContents(2);
		File f1 = loadcode(code[0].toString());
		File f2 = loadcode(code[1].toString());

		runCodan();
		doRunQuickFix();

		String result1 = getContents(f1);
		assertContainedIn("#ifndef", result1);
		assertContainedIn("#endif", result1);
		String result2 = getContents(f2);
		assertFalse(result2.contains("#ifndef"));
		assertFalse(result2.contains("#endif"));
	}

	//@file:included.h
	//#ifndef BLAH_H
	//#define BLAH_H
	//int foo();
	//#endif
	/* ---- */
	//@file:includer.h
	//#ifndef BLAH_H
	//#include "included.h"
	//#endif
	/* ---- */
	//@file:a.h
	//#ifndef A_H
	//#define A_H
	//int bar();
	//#endif
	/* ---- */
	//@file:a.c
	//#include "a.h"
	// int bar() {}
	/* ---- */
	//@file:b.h
	//#ifndef B_H
	//#define B_H
	//#include "included.h"
	//#include "a.h"
	//int the_function();
	//#endif B_H
	@SuppressWarnings({ "restriction" })
	public void testMultipleFiles() {
		setQuickFix(new QuickFixUnneededHeaderGuard());
		StringBuilder[] code = getContents(5);
		File[] files = loadcode(code);

		runCodan();
		doRunQuickFix();

		String[] contents = getContents(files);
		assertTrue(hasGuards(contents[0]));
		assertFalse(hasGuards(contents[1]));
	}

	@SuppressWarnings("nls")
	private boolean hasGuards(String s) {
		if(s.contains("#ifndef") && s.contains("#endif")) {
			return true;
		}
		return false;
	}

	private String[] getContents(File[] files) {
		String[] contents = new String[files.length];
		for(int i=0; i<files.length; i++) {
			contents[i] = getContents(files[i]);
		}
		return contents;
	}

	private File[] loadcode(StringBuilder[] code) {
		File[] files = new File[code.length];
		for(int i=0; i<code.length; i++) {
			files[i] = loadcode(code[i].toString());
		}
		return files;
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
