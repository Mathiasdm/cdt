/*******************************************************************************
 * Copyright (c) 2011 Mathias De Maré
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Mathias De Maré - Initial implementation
 *******************************************************************************/

package org.eclipse.cdt.codan.core.internal.checkers;

import org.eclipse.cdt.codan.core.test.CheckerTestCase;
import org.eclipse.cdt.codan.internal.checkers.DeadCodeChecker;

public class DeadCodeCheckerTest extends CheckerTestCase {

	@Override
	public void setUp() throws Exception {
		super.setUp();
		enableProblems(DeadCodeChecker.ER_ID);
	}

	// void foo() {
	//   return;
	//   int i = 2;
	// }
	public void testSimpleEarlyReturn() {
		loadCodeAndRun(getAboveComment());
		checkErrorLines(3);
	}

	// int foo() {
	//   int i = 0;
	//   if(a && !b) {
	//     return i;
	//     i++;
	//   }
	//   else {
	//     i--;
	//     return i;
	//   }
	// }
	public void testReturnInIf() {
		loadCodeAndRun(getAboveComment());
		checkErrorLines(5);
	}

	// int bar(long a) {
	//   int i = 0;
	//   while(i < 5) {
	//     i++;
	//   }
	// }
	public void testNoIssue() {
		loadCodeAndRun(getAboveComment());
		checkNoErrors();
	}
}
