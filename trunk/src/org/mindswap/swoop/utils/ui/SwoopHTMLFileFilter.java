package org.mindswap.swoop.utils.ui;

import java.io.File;

import javax.swing.filechooser.FileFilter;

public class SwoopHTMLFileFilter extends FileFilter {

		public static String HTML = ".html";  // xml
		
		public boolean accept(File file) 
		{
			if ( file.getName().endsWith( HTML ))
				return true;
			return false;
		}

		public String getDescription() 
		{
			return "Swoop HTML File (*.html)";
		}

		public String getExtension() 
		{
			return HTML;
		}	
}
