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

package org.eclipse.cdt.codan.core.internal.checkers;
import org.eclipse.cdt.codan.core.test.CheckerTestCase;

@SuppressWarnings("unused")
public class UnneededHeaderGuardTest extends CheckerTestCase {

	@Override
	public void setUp() throws Exception {
		super.setUp();
		enableProblems("org.eclipse.cdt.codan.internal.checkers.UnneededHeaderGuard"); //$NON-NLS-1$
	}

	// @file:header.h
	// #ifndef HEADER_H
	// #define HEADER_H
	// #ifndef ABC_H
	// #include "included.h"
	// #endif
	// #endif
	public void testUnneededSingleQuotedWithoutSurroundings() {
		loadCodeAndRun(getAboveComment());
		checkErrorLine(4);
	}

	// @file:impl.cpp
	// #ifndef DEF_H
	// #include <someheaderfile_blah_fooBar.hpp>
	// #endif
	public void testUnneededSingleBracketsWithoutSurroundings() {
		loadCodeAndRun(getAboveComment());
		checkErrorLine(2);
	}

	// @file:foo.c
	// int foo();
	// #ifndef FOOFOO_H
	// #include "foofoo.hh"
	// #endif
	public void testUnneededSingleQuotedBehindCode() {
		loadCodeAndRun(getAboveComment());
		checkErrorLine(3);
	}

	// @file:randomname.hpp
	// #ifndef SOME_MESSAGE_H
	// #include "someMessage.hpp"
	// #endif
	// void bar() {
	// int i = 1;
	// }
	public void testUnneededSingleQuotedBeforeCode() {

	}

	public void testUnneededSingleQuotedBetweenCode() {

	}

	public void testIncompleteSingleQuoted() {

	}

	public void testIntermingledSingleQuoted() {

	}

//	// @file:includedheader.h
//	// int i=1;
//	// #ifndef DEF_H //Internal header guards are at the end, check if they still work correctly
//	// #define DEF_H
//	// int foo();
//	// #endif
//	/* ---- */
//	// @file:header.h
//	// #ifndef DEF_H
//	// #include "includedheader.h" //No warning
//	// #endif
//	public void testSingleNeededGuardAtEnd() {
//		StringBuilder[] code = getContents(2);
//		File f1 = loadcode(code[0].toString());
//		File f2 = loadcode(code[1].toString());
//		runOnProject();
//		checkNoErrors();
//	}
//
//	// @file:includedheader.h
//	// int i=1; //Statement outside of the guards causes external guards to be needed
//	// #ifndef DEF_H
//	// #define DEF_H
//	// int foo();
//	// #endif
//	/* ---- */
//	// @file:header.h
//	// #ifndef DEF_H
//	// #include "includedheader.h" //No warning
//	// #endif
//	// int bar();
//	public void testSingleNeededGuard() {
//		StringBuilder[] code = getContents(2);
//		File f1 = loadcode(code[0].toString());
//		File f2 = loadcode(code[1].toString());
//		runOnProject();
//		checkNoErrors();
//	}
//
//	// @file:includedheader.h
//	// #ifndef DEF_H
//	// #define DEF_H
//	// int foo();
//	// #else
//	// //do stuff here
//	// #endif
//	/* ---- */
//	// @file:header.h
//	// #ifndef DEF_H
//	// #include "includedheader.h" //No warning
//	// #endif
//	// int bar();
//	public void testNeededGuardDueToElse() {
//		StringBuilder[] code = getContents(2);
//		File f1 = loadcode(code[0].toString());
//		File f2 = loadcode(code[1].toString());
//		runOnProject();
//		checkNoErrors();
//	}
//
//	// @file:includedheader.h
//	// #ifndef DEF_H
//	// #define DEF_H
//	// int foo();
//	// #endif
//	/* ---- */
//	// @file:header.h
//	// #ifndef DEF_H
//	// #include "includedheader.h" //Warning
//	// #endif
//	public void testUnneededGuardWithoutOtherStatements() {
//		StringBuilder[] code = getContents(2);
//		File f1 = loadcode(code[0].toString());
//		File f2 = loadcode(code[1].toString());
//		runOnProject();
//		checkErrorLine(f2, 2);
//	}
//
//	// @file:a.h
//	// #ifndef A
//	// #define A
//	// void a();
//	// #endif
//	/* ---- */
//	// @file:b.h
//	// #ifndef B
//	// #define B
//	// void b();
//	// #endif
//	/* ---- */
//	// @file:header.h
//	// #ifndef A
//	// #include "a.h" //Warning
//	// #endif
//	//
//	// #ifndef B
//	// #include "b.h" //Warning
//	// #endif
//	public void testMultipleUnneededGuards() {
//		StringBuilder[] code = getContents(3);
//		File f1 = loadcode(code[0].toString());
//		File f2 = loadcode(code[1].toString());
//		File f3 = loadcode(code[2].toString());
//		runOnProject();
//		checkErrorLine(f3, 2);
//		checkErrorLine(f3, 6);
//	}
//
//	//@file:included.hpp
//	// #ifndef INCLUDED_H
//	// #define INCLUDED_H
//	// #endif
//	// i++; //Statement after the header guards: redundant guards are needed.
//	/* ---- */
//	//@file:header.hpp
//	// int i = 1;
//	// #ifndef INCLUDED_H
//	// #include "included.hpp" //No warning
//	// #endif
//	public void testStatementAfterGuards() {
//		StringBuilder[] code = getContents(2);
//		File f1 = loadcode(code[0].toString());
//		File f2 = loadcode(code[1].toString());
//		runOnProject();
//		checkNoErrors();
//	}
//
//	//@file:included.hpp
//	// #ifndef INCLUDED_HPP
//	// #define INCLUDED_HPP
//	// #ifndef INCLUDED_INTERNAL
//	// #define INCLUDED_INTERNAL
//	// #endif
//	// int foo();
//	// #endif
//	/* ---- */
//	//@file:header.hpp
//	// int bar();
//	// char* foobar();
//	// #ifndef INCLUDED_HPP
//	// #include "included.hpp" //Warning
//	// #endif
//	//
//	public void testNestedPreprocessorStatements() {
//		StringBuilder[] code = getContents(2);
//		File f1 = loadcode(code[0].toString());
//		File f2 = loadcode(code[1].toString());
//		runOnProject();
//		checkErrorLine(f2, 4);
//	}
//
//	//@file:included.hpp
//	// #ifndef ABC_H
//	// #define ABC_H
//	// void foo(int, char*);
//	// #endif
//	// #ifndef DEF_H
//	// #define DEF_H
//	// int bar();
//	// #endif
//	/* ---- */
//	//@file:header.hpp
//	// #ifndef ABC_H
//	// #include "included.hpp" //No warning: 2 sequential header guards don't cover the entire file
//	// #endif
//	public void testSequentialPreprocessorStatements() {
//		StringBuilder[] code = getContents(2);
//		File f1 = loadcode(code[0].toString());
//		File f2 = loadcode(code[1].toString());
//		runOnProject();
//		checkNoErrors();
//
//	}
//
}