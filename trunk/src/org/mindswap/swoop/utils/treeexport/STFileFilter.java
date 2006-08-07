/*
 * Created on Oct 3, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.mindswap.swoop.utils.treeexport;

import java.io.File;

/**
 * @author Dave Wang
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class STFileFilter extends VisualizationFileFilter
{
	
	public static String XML = ".xml";  // xml
	public static String ST  = XML;     // spacetree
	
	public boolean accept(File file) 
	{
		if ( file.getName().endsWith( STFileFilter.ST ))
			return true;
		return false;
	}

	public String getDescription() 
	{
		return "Spacetree Visualization File Format (*.xml)";
	}

	public String getExtension() 
	{
		return ST;
	}
}	