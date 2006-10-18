/*
 * Created on Oct 6, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.mindswap.swoop.utils.graph.hierarchy.colors;

import java.awt.Color;

import org.mindswap.swoop.utils.graph.hierarchy.ClassTreeNode;
import org.mindswap.swoop.utils.graph.hierarchy.SwoopOntologyVertex;

/**
 * @author Dave Wang
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public abstract class GraphColorScheme 
{
	public abstract Color getBackgroundColor();
	
	public abstract Color getOntologyNodeFillColor( SwoopOntologyVertex vertex );
	public abstract Color getOntologyNodeOutlineColor( SwoopOntologyVertex vertex );
	
	public abstract Color getOntologyNodeSelectFillColor( SwoopOntologyVertex vertex);
	public abstract Color getOntologyNodeSelectOutlineColor( SwoopOntologyVertex vertex);
	
	public abstract Color getTreeNodeFillColor( ClassTreeNode node );
	public abstract Color getTreeNodeOutlineColor( ClassTreeNode node );
	

	public abstract Color getTreeNodeSelectFillColor( ClassTreeNode node);
	public abstract Color getTreeNodeSelectOutlineColor( ClassTreeNode node);
	
	public abstract Color getTreeNodeHighlightFillColor( ClassTreeNode node);
	public abstract Color getTreeNodeHighlightOutlineColor( ClassTreeNode node);

	public abstract Color getOverlayGraphEdgeColor();
}
