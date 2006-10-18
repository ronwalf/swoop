/*
 * Created on Dec 1, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.mindswap.swoop.utils.graph.hierarchy.ui;

import javax.swing.event.ChangeEvent;

/**
 * @author Dave Wang
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class LaunchEvent extends ChangeEvent
{
	public static final String SOLICITING_REASONER = "Soliciting for reasoner(s)...";
	public static final String BUILDING_DATA = "Building data model...";
	public static final String BUILDING_GUI  = "Building GUI...";
	public static final String RENDERING     = "Rendering view...";
	
	private String myChange = ""; 
	
	public LaunchEvent( Object source, String change )
	{
		super( source );
		myChange = change;
	}
	
	public String getChange()
	{ return myChange; }

}
