/*
 * Created on Oct 6, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.mindswap.swoop.utils.graph.hierarchy.colors;

import java.awt.Color;

import org.mindswap.swoop.utils.graph.hierarchy.SwoopOntologyVertex;

/**
 * @author Dave Wang
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class PartitionFocusColorScheme extends DefaultColorScheme implements DefaultColors
{

	public static Color UPSTREAM   = new Color( 144, 103, 57);
	public static Color FOCUS      = new Color( 255, 238, 0);
	public static Color DOWNSTREAM = new Color( 57, 103, 144);
	public static Color NONE       = new Color( 96, 96, 96);
	
	public Color getOntologyNodeFillColor(SwoopOntologyVertex vertex) 
	{
		if ( vertex.getPartitionState() == SwoopOntologyVertex.FOCUS )
			return FOCUS;
		else if ( vertex.getPartitionState() == SwoopOntologyVertex.UPSTREAM )
			return UPSTREAM;
		else if ( vertex.getPartitionState() == SwoopOntologyVertex.DOWNSTREAM )
			return DOWNSTREAM;
		else
			return NONE;
	}

}
