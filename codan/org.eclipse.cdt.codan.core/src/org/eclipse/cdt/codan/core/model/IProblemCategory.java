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

public interface IProblemCategory extends IProblemElement {
	String getName();

	String getId();

	Object[] getChildren();

	/**
	 * @param id
	 * @return
	 */
	IProblem findProblem(String id);

	/**
	 * @param id
	 * @return
	 */
	IProblemCategory findCategory(String id);
}