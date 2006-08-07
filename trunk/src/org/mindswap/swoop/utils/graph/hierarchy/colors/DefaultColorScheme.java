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
public class DefaultColorScheme extends GraphColorScheme implements DefaultColors
{

	public Color getBackgroundColor()
	{ return Color.WHITE; }
	
	public Color getOntologyNodeFillColor(SwoopOntologyVertex vertex) 
	{
		if (vertex.getOutEdges().isEmpty()) 
		{
			if (vertex.getInEdges().isEmpty())
				return ALOOF_COLOR;
			else
				return INDEPENDENT_COLOR;
		} else
			return DEPENDENT_COLOR;
	}

	public Color getOntologyNodeOutlineColor(SwoopOntologyVertex vertex) 
	{ 
		return VERTEX_OUTLINE_COLOR;
	}

	public Color getOntologyNodeSelectFillColor( SwoopOntologyVertex vertex)
	{ return getOntologyNodeFillColor( vertex ); }

	public Color getOntologyNodeSelectOutlineColor( SwoopOntologyVertex vertex)
	{ return Color.YELLOW; }

	public Color getTreeNodeFillColor( ClassTreeNode node) 
	{
		if ( node.getIsSelected() )
			return SELECT_COLOR;
		else if ( node.getIsListBrowsed() )
			return LIST_BROWSED_FILL_COLOR;
		else if ( node.getIsHighlighted() )
			return HIGHLIGHT_COLOR;
		return CLASS_COLOR;
	}	

	public Color getTreeNodeOutlineColor( ClassTreeNode node ) 
	{ 
		if ( node.getIsListBrowsed() )
			return LIST_BROWSED_OUTLINE_COLOR;
		return Color.BLACK; 
	}
	
	public Color getTreeNodeSelectFillColor( ClassTreeNode node)
	{ return SELECT_COLOR; }
	public Color getTreeNodeSelectOutlineColor( ClassTreeNode node)
	{ return Color.BLACK; }
	
	public Color getTreeNodeHighlightFillColor( ClassTreeNode node)
	{ return HIGHLIGHT_COLOR; }
	public Color getTreeNodeHighlightOutlineColor( ClassTreeNode node)
	{ return Color.BLACK; }
	
	public Color getOverlayGraphEdgeColor()
	{ return OVERLAY_EDGE_COLOR; }

}