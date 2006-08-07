/*
 * Created on Oct 6, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.mindswap.swoop.utils.graph.hierarchy.colors;

import java.awt.Color;

/**
 * @author Dave Wang
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public interface DefaultColors 
{
	public Color CLASS_COLOR       = new Color(128, 128, 128);
	public Color SELECT_COLOR      = new Color(255, 255, 225);
	public Color HIGHLIGHT_COLOR   = new Color(128, 255, 255);
	
	public Color VERTEX_OUTLINE_COLOR = Color.WHITE;
	
	public Color LIST_BROWSED_FILL_COLOR  = new Color(255, 220, 100);     // orangy
	public Color LIST_BROWSED_OUTLINE_COLOR = new Color(235,235, 0); // yellow
	
	public Color DEPENDENT_COLOR    = new Color(166, 17, 17);  // Reddish
	public Color INDEPENDENT_COLOR  = new Color(47, 22, 160);  // Blueish
	public Color ALOOF_COLOR        = new Color(0, 190, 0);    // Greenish
	
	public Color HAS_RELATION_FILL_COLOR = new Color( 0, 128, 255 );     // bright blue
	public Color HAS_RELATION_EDGE_COLOR = Color.WHITE;                  // white
	public Color OVERLAY_EDGE_COLOR = new Color(235, 0, 0);    // reddish
	
	public Color INDEPENDENT_OVERLAY_COLOR  = new Color(76, 76, 76);
	public Color DEPENDENT_OVERLAY_COLOR = new Color(66, 66, 66);
	public Color ALOOF_OVERLAY_COLOR =  new Color( 63, 63, 63 );
	
}
