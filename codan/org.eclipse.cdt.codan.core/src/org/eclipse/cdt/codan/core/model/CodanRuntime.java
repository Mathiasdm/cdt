/*******************************************************************************
 * Copyright (c) 2009 Alena Laskavaia 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alena Laskavaia  - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.codan.core.model;

/**
 * Runtime singleton class to get access to Codan framework parts
 * 
 */
public class CodanRuntime {
	private static CodanRuntime instance = new CodanRuntime();
	private CodanProblemReporter problemReporter = new CodanProblemReporter();

	public CodanProblemReporter getProblemReporter() {
		return problemReporter;
	}

	public void setProblemReporter(CodanProblemReporter reporter) {
		problemReporter = reporter;
	}

	public static CodanRuntime getInstance() {
		return instance;
	}
}