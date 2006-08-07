/*
 * Created on Feb 24, 2005
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.mindswap.swoop.utils.explain;

import java.net.URI;

/**
 * @author Dave
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class ExplanationHTMLSerializer 
{
	
	protected static String serializeItem(String category, String content)
	{
		if (content == null)
			return "";
		return "<p><b>"+ category +"</b></p>" + content;
	}
	
	protected static String serializeArray(String category, String [] array)
	{
		if (array == null)
			return "";
		String text = "<p><b>"+ category +"</b></p>";
		for (int i  = 0; i < array.length ; i++)
		{
			text = text + array[i] + "<br>";
		}
		return text;
	}
	
	// just write out the String without adding link for now.  Swoop cannot load normal pages
	// via activating a hyperlink right now.
	protected static String serializeItem(String category, URI uri)
	{
		if (uri == null)
			return "";
		return "<p><b>"+ category +"</b></p>" + uri.toString();
	}
	
	// just write out the String without adding link for now.  Swoop cannot load normal pages
	// via activating a hyperlink right now.
	protected static String serializeArray(String category, URI [] array)
	{
		if (array == null)
			return "";
		String text = "<p><b>"+ category +"</b></p>";
		for (int i  = 0; i < array.length ; i++)
		{
			text = text + array[i].toString() + "<br>";
		}
		return text;
	}
	

}
