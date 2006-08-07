/*
 * Created on Mar 1, 2006
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.mindswap.swoop;

import java.util.Hashtable;

import javax.swing.tree.DefaultMutableTreeNode;

/**
 * @author Dave Wang
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class SwoopTreeNode extends DefaultMutableTreeNode
{
	public static final String CYCLE_HEAD  = "CYCLE_HEAD";   // head class of a cycle
	public static final String IS_IN_CYCLE = "IS_IN_CYCLE";  // whether a class is in a cycle
	public static final Boolean TRUE       = new Boolean( true );
	public static final Boolean FALSE      = new Boolean( false );
	
	private Hashtable table = new Hashtable();
	
	public SwoopTreeNode()
	{ 
		super(); 
		addUserObject( SwoopTreeNode.IS_IN_CYCLE, SwoopTreeNode.FALSE );	
	}
	
	public SwoopTreeNode( Object userObj )
	{ 
		super(userObj); 
		addUserObject( SwoopTreeNode.IS_IN_CYCLE, SwoopTreeNode.FALSE );
	}
	
	public SwoopTreeNode( Object userObj, boolean allowsChildren )
	{ 
		super( userObj, allowsChildren); 
		addUserObject( SwoopTreeNode.IS_IN_CYCLE, SwoopTreeNode.FALSE );
	}
	
	public void addUserObject( String key, Object value )
	{ table.put( key, value ); }
	
	public Object getUserObject( String key )
	{ return table.get( key ); }
	
}
