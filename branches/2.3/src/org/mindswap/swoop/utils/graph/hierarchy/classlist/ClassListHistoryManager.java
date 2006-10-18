/*
 * Created on Nov 13, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.mindswap.swoop.utils.graph.hierarchy.classlist;

import java.util.Vector;

/**
 * @author Dave Wang
 *
 * Manages a class list history -- Moving forwards/backwards in history.
 *
 */
public class ClassListHistoryManager
{		
	private Vector myModels = null;
	private int index = -1;
	
	public ClassListHistoryManager( )
	{
		myModels = new Vector();
		myModels.add( null );
		index = 0;
	}
	
	public ClassListHistoryItem goForward()
	{
		if ( index >= myModels.size() - 1)
			return null;
		index++;
		ClassListHistoryItem item = (ClassListHistoryItem)myModels.get(index);
		return item;
	}
	
	public ClassListHistoryItem goBack()
	{
		if (index <= 0)
			return null;
		index--;
		ClassListHistoryItem item = (ClassListHistoryItem)myModels.get(index);
		return item;
	}
	
	public ClassListHistoryItem getCurrent()
	{
		if ( ( index >= myModels.size()) || ( index <= 0))
			return null;
		return (ClassListHistoryItem)myModels.get(index);
	}
	
	// increments index
	public ClassListHistoryItem addItem( ClassListHistoryItem item )
	{
		// if not at the end, remove the rest
		if ( index != ( myModels.size() - 1) )
			myModels.setSize( index + 1 );
			
		myModels.add( item );
		index++;
		return item;
	}
	
	public boolean isAtEnd()
	{ return ( index == myModels.size() -1 ); }
	
	public boolean isAtBeginning()
	{  return ( index <= 1 ); } // index 1 contains real history, index 0 contains null
}