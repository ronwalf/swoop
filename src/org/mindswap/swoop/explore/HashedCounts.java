package org.mindswap.swoop.explore;

import java.util.Hashtable;


/*
 * A simple data structure that is a hashtable of keys and its counters
 * 
 */
public class HashedCounts extends Hashtable
{
	
	public HashedCounts()
	{ super(); }
	
	public void add( Object key )
	{
		Integer integer = (Integer)super.get( key );
		if ( integer  == null )
			integer = new Integer(0);
		integer = new Integer(integer.intValue() + 1);
		super.put( key, integer );
	}
	
	public int getCount( Object key )
	{
		return ((Integer)super.get( key )).intValue();
	}
	
	public void add( Object key, int value)
	{
		Integer integer = (Integer)super.get( key );
		if ( integer  == null )
			integer = new Integer(0);
		integer = new Integer( integer.intValue() + value );
		super.put( key, integer );
	}
	
}