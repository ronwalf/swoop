/*
 * Created on Feb 14, 2005
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
public class DatatypeExplanation extends Explanation
{	

	private String [] myExamples = null;
	
	public DatatypeExplanation(URI uri, String name, String desc, URI [] src, String [] examples)
	{
		super.myURI  = uri;
		super.myName = name;
		super.myDescription = desc;
		super.mySources     = src;
		this.myExamples    = examples;
	}
	

	public String [] getExamples()
	{
		return myExamples;
	}
}
