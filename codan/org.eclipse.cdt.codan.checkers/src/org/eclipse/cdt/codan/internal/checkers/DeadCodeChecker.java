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

package org.eclipse.cdt.codan.internal.checkers;

import java.util.Iterator;

import org.eclipse.cdt.codan.core.cxx.internal.model.cfg.ControlFlowGraphBuilder;
import org.eclipse.cdt.codan.core.cxx.internal.model.cfg.CxxControlFlowGraph;
import org.eclipse.cdt.codan.core.cxx.model.AbstractAstFunctionChecker;
import org.eclipse.cdt.codan.core.model.cfg.IBasicBlock;
import org.eclipse.cdt.codan.core.model.cfg.ICfgData;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTNode;

@SuppressWarnings("restriction")
public class DeadCodeChecker extends AbstractAstFunctionChecker {
	
	public static final String ER_ID = "org.eclipse.cdt.codan.internal.checkers.DeadCodeChecker"; //$NON-NLS-1$
	public static final String PROBLEM_ID = "org.eclipse.cdt.codan.internal.checkers.DeadCodeChecker"; //$NON-NLS-1$
	
	@SuppressWarnings("restriction")
	@Override
	protected void processFunction(IASTFunctionDefinition func) {
		ControlFlowGraphBuilder builder = new ControlFlowGraphBuilder();
		CxxControlFlowGraph graph = builder.build(func);
		Iterator<IBasicBlock> deadNodeIterator = graph.getUnconnectedNodeIterator();
		while(deadNodeIterator.hasNext()) {
			IBasicBlock deadNode = deadNodeIterator.next();
			if(deadNode instanceof ICfgData) {
				Object data = ((ICfgData) deadNode).getData();
				if(data instanceof IASTNode) {
					IASTNode node = (IASTNode) data;
					reportProblem(PROBLEM_ID, node);
				}
			}
		}
	}
}
