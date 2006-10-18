package org.mindswap.swoop.utils.ui;

import java.io.File;

import org.mindswap.swoop.utils.treeexport.VisualizationFileFilter;

/**
 * @author UMD
 */
public class TextFileFilter extends VisualizationFileFilter {
	public static String TXT = ".txt";  // treemap
	
	public boolean accept(File file) 
	{
		if ( file.getName().endsWith( TXT ))
			return true;
		return false;
	}

	public String getDescription() 
	{
		return "Ontology Statistics File Format (*.txt)";
	}
	public String getExtension() 
	{
		return TXT;
	}
}
