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
 * Explanation data structure.  Stores data relevant to some item referrable by an URI
 * Every Explanation must have a Name of the object it is explaining, a Description to
 * explain the object, a URI to refer to the object, and possibly a set of sources where
 * the description is obtained (can be null).
 * 
 * Intended as a non-mutable object.
 */
public class Explanation
{
	protected URI       myURI = null;
	protected String    myName = null;
	protected String    myDescription = null;
	protected URI    [] mySources = null;
	
	public String getName()
	{
		return myName;
	}

	public String getDescription()
	{
		return myDescription;
	}
	
	public URI getURI()
	{
		return myURI;
	}
	
	public URI [] getSources()
	{
		return mySources;
	}
}
