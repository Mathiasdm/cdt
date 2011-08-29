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
	@SuppressWarnings("restriction")
	public void testSimple() {
		setQuickFix(new QuickFixUnneededHeaderGuard());
		StringBuilder[] code = getContents(2);
		File f1 = loadcode(code[0].toString());
		File f2 = loadcode(code[1].toString());

		getContents(f1);
		System.out.println("-----");
		getContents(f2);

		runCodan();
		doRunQuickFix();

		String result1 = getContents(f1);
		assertContainedIn("#ifndef", result1);
		assertContainedIn("#endif", result1);
		String result2 = getContents(f2);
		assertFalse(result2.contains("#ifndef"));
		assertFalse(result2.contains("#endif"));
	}

	private String getContents(File f1) {
		try {
			BufferedReader reader = new BufferedReader(new FileReader(f1));
			String contents = "";
			String line;
			while((line = reader.readLine()) != null) {
				contents += line + '\n';
			}
			System.out.println(contents);
			return contents;
		} catch (FileNotFoundException e1) {
			return null;
		} catch (IOException e) {
			return null;
		}
	}
}
