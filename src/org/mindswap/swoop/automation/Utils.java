/*
 * Created on Jan 30, 2006
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.mindswap.swoop.automation;

/**
 * @author Dave Wang
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class Utils 
{
	
	
	public static String getExceptionTrace( Exception e )
	{
		String trace = "";
		StackTraceElement [] elements = e.getStackTrace();
		for ( int i = 0 ; i < elements.length; i++ )
		{
			StackTraceElement element = elements[i];
			trace = trace + element.toString() + "\n";
		}
		return trace;
	}
}
