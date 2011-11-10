package org.eclipse.cdt.codan.core.internal.checkers;

import java.io.File;

import org.eclipse.cdt.codan.core.test.CheckerTestCase;
import org.eclipse.cdt.codan.internal.checkers.IncorrectIncludeChecker;
public class IncorrectIncludeCheckerTest extends CheckerTestCase {

	@Override
	public void setUp() throws Exception {
		super.setUp();
		enableProblems(IncorrectIncludeChecker.ER_ID);
	}

	// @file:includedheader.h
	// int foo();
	/* ---- */
	// @file:main.c
	// #include <stdio.h>
	// #include "includedheader.h"
	// int main() {
	//   printf("Hello world!");
	// }
	public void testNotNeededInclude() {
		StringBuilder[] code = getContents(2);
		File f1 = loadcode(code[0].toString());
		File f2 = loadcode(code[1].toString());
		runOnProject();
		checkErrorLine(f2, 1);
		checkErrorLine(f2, 2);
	}
}