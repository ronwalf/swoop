/*
 * Created on Oct 9, 2005
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
public class OverlayGraphScheme extends DefaultColorScheme implements DefaultColors 
{
	
	public Color getBackgroundColor()
	{ return Color.DARK_GRAY; }

	public Color getOntologyNodeFillColor(SwoopOntologyVertex vertex) 
	{
		if (vertex.getOutEdges().isEmpty()) 
		{
			if (vertex.getInEdges().isEmpty())
				return ALOOF_OVERLAY_COLOR;
			else
				return INDEPENDENT_OVERLAY_COLOR;
		} else
			return DEPENDENT_OVERLAY_COLOR;
	}

	public Color getOntologyNodeOutlineColor(SwoopOntologyVertex vertex) 
	{ 
		return VERTEX_OUTLINE_COLOR;
	}

	public Color getOntologyNodeSelectFillColor( SwoopOntologyVertex vertex)
	{ return getOntologyNodeFillColor( vertex ); }

	public Color getOntologyNodeSelectOutlineColor( SwoopOntologyVertex vertex)
	{ return Color.LIGHT_GRAY; }
	
	public Color getTreeNodeFillColor( ClassTreeNode node) 
	{
		if ( node.getIsOverlayed() )
			return Color.WHITE;
		else if ( node.gethasRelations() )
			return HAS_RELATION_FILL_COLOR;
		else if ( node.getIsListBrowsed() )
			return LIST_BROWSED_FILL_COLOR;
		else if ( node.getIsHighlighted() )
			return HIGHLIGHT_COLOR;
		return Color.LIGHT_GRAY;
	}	

	public Color getTreeNodeOutlineColor( ClassTreeNode node ) 
	{ 
		if ( node.getIsOverlayed() )
			return OVERLAY_EDGE_COLOR;
		else if ( node.gethasRelations() )
			return HAS_RELATION_EDGE_COLOR;
		return Color.BLACK; 
	}
}
