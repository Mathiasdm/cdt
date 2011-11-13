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

	// @file:includedheader.h
	// #include "blah.h"
	// int foo();
	/* ---- */
	// @file:blah.h
	// #ifndef BLAH_H
	// #define BLAH_H
	// int bar();
	// #include "banana.h"
	// #endif
	/* ---- */
	// @file:banana.h
	// #ifndef BANANA_H
	// #define BANANA_H
	// #include "blah.h"
	// #endif
	/* ---- */
	// @file:main.c
	// #include <stdio.h>
	// #include "includedheader.h"
	// int main() {
	//   printf("Hello world!");
	// }
	public void testIncludeStructure() {
		StringBuilder[] code = getContents(4);
		File f1 = loadcode(code[0].toString());
		File f2 = loadcode(code[1].toString());
		File f3 = loadcode(code[2].toString());
		File f4 = loadcode(code[3].toString());
		runOnProject();
		checkErrorLine(f4, 1);
		checkErrorLine(f4, 2);
	}
}