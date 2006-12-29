package org.mindswap.swoop.explore;

import java.util.Hashtable;
import java.util.Vector;

/*
 * A simple data structure that is a hashtable of vectors
 * 
 */
public class HashedVectors extends Hashtable
{
	
	public HashedVectors()
	{ super(); }
	
	public void add( Object key, Object item )
	{
		Vector vec = (Vector)super.get( key );
		if ( vec  == null )
			vec = new Vector();
		vec.add( item );
		super.put( key, vec );
	}
	
	public Vector getVector( Object key )
	{
		return (Vector)super.get( key );
	}
}
