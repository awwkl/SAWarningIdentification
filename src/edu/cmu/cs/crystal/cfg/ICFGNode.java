/**
 * Copyright (c) 2006, 2007, 2008 Marwan Abi-Antoun, Jonathan Aldrich, Nels E. Beckman,
 * Kevin Bierhoff, David Dickey, Ciera Jaspan, Thomas LaToza, Gabriel Zenarosa, and others.
 *
 * This file is part of Crystal.
 *
 * Crystal is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Crystal is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Crystal.  If not, see <http://www.gnu.org/licenses/>.
 */
package edu.cmu.cs.crystal.cfg;

import java.util.Set;

import edu.cmu.cs.crystal.flow.ILabel;

/** Abstract
 * 
 * @author aldrich
 * @author ciera
 */
public interface ICFGNode<N> {
	public Set<? extends ICFGEdge<N>> getInputs();
	public Set<? extends ICFGEdge<N>> getOutputs();

	/** @return The Node from which this CFGNode was created,
	 * may be null if this node is a dummy node. These nodes
	 * will still have input/output edges for control
	 * flow purposes.
	 */
	public N getASTNode();
	
	public Set<? extends ICFGEdge<N>> getInputEdges(ILabel label);
	
	public Set<? extends ICFGEdge<N>> getOutputEdges(ILabel label);
	
	public ICFGNode<N> getEnd();
	
	public ICFGNode<N> getStart();
}

