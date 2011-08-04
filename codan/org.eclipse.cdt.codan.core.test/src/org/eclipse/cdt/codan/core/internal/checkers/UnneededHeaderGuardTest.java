package org.eclipse.cdt.codan.core.internal.checkers;
import java.io.File;

import org.eclipse.cdt.codan.core.test.CheckerTestCase;


public class UnneededHeaderGuardTest extends CheckerTestCase {

	// @file:includedheader.h
	// #ifndef ABC_H
	// #define ABC_H
	// int foo();
	// #endif
	/* ---- */
	// @file:header.h
	// #ifndef ABC_H
	// #include "includedheader.h"
	// #endif
	// int blah();
	public void testSingleUnneededGuard() {
		StringBuffer[] code = getContents(2);
		File f1 = loadcode(code[0].toString());
		File f2 = loadcode(code[1].toString());
		runOnProject();
		checkErrorLine(f2, 2);
	}

	// @file:includedheader.h
	// int i=1;
	// #ifndef DEF_H
	// #define DEF_H
	// int foo();
	// #endif
	/* ---- */
	// @file:header.h
	// #ifndef DEF_H
	// #include "includedheader.h"
	// #endif
	public void testSingleNeededGuardAtEnd() {
		StringBuffer[] code = getContents(2);
		File f1 = loadcode(code[0].toString());
		File f2 = loadcode(code[1].toString());
		runOnProject();
		checkNoErrors();
	}

	// @file:includedheader.h
	// int i=1;
	// #ifndef DEF_H
	// #define DEF_H
	// int foo();
	// #endif
	/* ---- */
	// @file:header.h
	// #ifndef DEF_H
	// #include "includedheader.h"
	// #endif
	// int bar();
	public void testSingleNeededGuard() {
		StringBuffer[] code = getContents(2);
		File f1 = loadcode(code[0].toString());
		File f2 = loadcode(code[1].toString());
		runOnProject();
		checkNoErrors();
	}

	// @file:includedheader.h
	// #ifndef DEF_H
	// #define DEF_H
	// int foo();
	// #else
	// //do stuff here
	// #endif
	/* ---- */
	// @file:header.h
	// #ifndef DEF_H
	// #include "includedheader.h"
	// #endif
	// int bar();
	public void testNeededGuardDueToElse() {
		StringBuffer[] code = getContents(2);
		File f1 = loadcode(code[0].toString());
		File f2 = loadcode(code[1].toString());
		runOnProject();
		checkNoErrors();
	}

	// @file:includedheader.h
	// #ifndef DEF_H
	// #define DEF_H
	// int foo();
	// #endif
	/* ---- */
	// @file:header.h
	// #ifndef DEF_H
	// #include "includedheader.h"
	// #endif
	public void testUnneededGuardWithoutOtherStatements() {
		StringBuffer[] code = getContents(2);
		File f1 = loadcode(code[0].toString());
		File f2 = loadcode(code[1].toString());
		runOnProject();
		checkErrorLine(f2, 2);
	}

	// @file:a.h
	// #ifndef A
	// #define A
	// void a();
	// #endif
	/* ---- */
	// @file:b.h
	// #ifndef B
	// #define B
	// void b();
	// #endif
	/* ---- */
	// @file:header.h
	// #ifndef A
	// #include "a.h"
	// #endif
	//
	// #ifndef B
	// #include "b.h"
	// #endif
	public void testMultipleUnneededGuards() {
		StringBuffer[] code = getContents(3);
		File f1 = loadcode(code[0].toString());
		File f2 = loadcode(code[1].toString());
		File f3 = loadcode(code[2].toString());
		runOnProject();
		checkErrorLine(f3, 2);
		checkErrorLine(f3, 6);
	}
}