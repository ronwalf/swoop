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
public class TM3FileFilter extends VisualizationFileFilter
{
	public static String TM3 = ".tm3";  // treemap
	
	public boolean accept(File file) 
	{
		if ( file.getName().endsWith( TM3 ))
			return true;
		return false;
	}

	public String getDescription() 
	{
		return "Treemap Visualization File Format (*.tm3)";
	}
	public String getExtension() 
	{
		return TM3;
	}		
}