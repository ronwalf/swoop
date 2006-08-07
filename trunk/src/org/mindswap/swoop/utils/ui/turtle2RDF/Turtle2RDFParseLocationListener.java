/*
 * Created on Dec 22, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.mindswap.swoop.utils.ui.turtle2RDF;

import org.openrdf.rio.ParseLocationListener;

/**
 * @author Dave Wang
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class Turtle2RDFParseLocationListener implements ParseLocationListener
{
	public void parseLocationUpdate(int lineNum, int colNum ) 
	{
		System.out.println( "line: " + lineNum + " column: " + colNum);
	}
}
