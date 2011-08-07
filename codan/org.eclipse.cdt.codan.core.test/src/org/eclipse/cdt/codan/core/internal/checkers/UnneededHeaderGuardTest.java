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
import java.io.File;

import org.eclipse.cdt.codan.core.test.CheckerTestCase;

@SuppressWarnings("unused")
public class UnneededHeaderGuardTest extends CheckerTestCase {

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
	public void testSingleUnneededGuard() {
		StringBuffer[] code = getContents(2);
		File f1 = loadcode(code[0].toString());
		File f2 = loadcode(code[1].toString());
		runOnProject();
		checkErrorLine(f2, 2);
	}

	// @file:includedheader.h
	// int i=1;
	// #ifndef DEF_H //Internal header guards are at the end, check if they still work correctly
	// #define DEF_H
	// int foo();
	// #endif
	/* ---- */
	// @file:header.h
	// #ifndef DEF_H
	// #include "includedheader.h" //No warning
	// #endif
	public void testSingleNeededGuardAtEnd() {
		StringBuffer[] code = getContents(2);
		File f1 = loadcode(code[0].toString());
		File f2 = loadcode(code[1].toString());
		runOnProject();
		checkNoErrors();
	}

	// @file:includedheader.h
	// int i=1; //Statement outside of the guards causes external guards to be needed
	// #ifndef DEF_H
	// #define DEF_H
	// int foo();
	// #endif
	/* ---- */
	// @file:header.h
	// #ifndef DEF_H
	// #include "includedheader.h" //No warning
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
	// #include "includedheader.h" //No warning
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
	// #include "includedheader.h" //Warning
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
	// #include "a.h" //Warning
	// #endif
	//
	// #ifndef B
	// #include "b.h" //Warning
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

	//@file:included.hpp
	// #ifndef INCLUDED_H
	// #define INCLUDED_H
	// #endif
	// i++; //Statement after the header guards: redundant guards are needed.
	/* ---- */
	//@file:header.hpp
	// int i = 1;
	// #ifndef INCLUDED_H
	// #include "included.hpp" //No warning
	// #endif
	public void testStatementAfterGuards() {
		StringBuffer[] code = getContents(2);
		File f1 = loadcode(code[0].toString());
		File f2 = loadcode(code[1].toString());
		runOnProject();
		checkNoErrors();
	}

	//@file:included.hpp
	// #ifndef INCLUDED_HPP
	// #define INCLUDED_HPP
	// #ifndef INCLUDED_INTERNAL
	// #define INCLUDED_INTERNAL
	// #endif
	// int foo();
	// #endif
	/* ---- */
	//@file:header.hpp
	// int bar();
	// char* foobar();
	// #ifndef INCLUDED_HPP
	// #include "included.hpp" //Warning
	// #endif
	//
	public void testNestedPreprocessorStatements() {
		StringBuffer[] code = getContents(2);
		File f1 = loadcode(code[0].toString());
		File f2 = loadcode(code[1].toString());
		runOnProject();
		checkErrorLine(f2, 4);
	}

	//@file:included.hpp
	// #ifndef ABC_H
	// #define ABC_H
	// void foo(int, char*);
	// #endif
	// #ifndef DEF_H
	// #define DEF_H
	// int bar();
	// #endif
	/* ---- */
	//@file:header.hpp
	// #ifndef ABC_H
	// #include "included.hpp" //No warning: 2 sequential header guards don't cover the entire file
	// #endif
	public void testSequentialPreprocessorStatements() {
		StringBuffer[] code = getContents(2);
		File f1 = loadcode(code[0].toString());
		File f2 = loadcode(code[1].toString());
		runOnProject();
		checkNoErrors();

	}

}