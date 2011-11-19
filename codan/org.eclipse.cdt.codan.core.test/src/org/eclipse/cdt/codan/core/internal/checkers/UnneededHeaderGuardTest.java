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
		loadCodeAndRun(getAboveComment());
		checkErrorLine(2);
	}

	// @file:somewhere.cc
	// int i;
	// #ifndef THIS_IS_A_LONG_CONDITION_DONT_YOU_THINK_HPP__
	// #include "somewhereElse.hpp"
	// #endif
	// int j;
	public void testUnneededSingleQuotedBetweenCode() {
		loadCodeAndRun(getAboveComment());
		checkErrorLine(3);
	}

	// @file:incomplete.c
	// int i = 1;
	// #include "incomplete.h"
	// #endif
	public void testIncompleteSingleQuoted() {
		loadCodeAndRun(getAboveComment());
		checkNoErrors();
	}

	// @file:intermingled.hh
	// #ifndef INTER_H
	// int foo();
	// #include "inter.h"
	// #endif
	public void testIntermingledSingleQuoted() {
		loadCodeAndRun(getAboveComment());
		checkNoErrors();
	}

	// @file:largebatch.c
	// #ifndef FIRST
	// #include "first.h" //Problem
	// #endif
	// #ifndef _STDIO_H
	// #include <stdio.h> //Problem
	// #endif
	// int foo();
	// int bar();
	// #ifndef NEXT_H
	// #include "foooobar.h" //No problem
	// enum SomeEnum {EntryOne, EntryTwo, EntryThree};
	// #endif
	// #ifndef ONE
	// #include "one.h" //Problem
	// #endif
	// #ifndef TWO
	// #include "two.h" //Problem
	// #endif
	// #ifndef THREE
	// #include "three.h" //Problem
	// #endif
	// #ifndef FOUR
	// #include "four.h" //Problem
	// #endif
	// #ifndef FIVE
	// #include "five.h" //Problem
	// #endif
	public void testLargeBatch() {
		loadCodeAndRun(getAboveComment());
		checkErrorLines(2, 5, 14, 17, 20, 23, 26);
	}
}