/*
 * Created on Jul 29, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.mindswap.swoop.utils.graph.hierarchy;

import java.net.URI;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;


/**
 * @author Dave Wang
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */

/*
 * @author Dave Wang
 *
 * A hashtable of sets.  Each contained set is indexed by the uri of a concept.
 * This allows for an ontology node to quickly look up (via the uri) if a node 
 * is belonged to it.
 * 
 */
public class ClassTreeNodeIndex extends Hashtable 
{
	
	
	public ClassTreeNodeIndex()
	{
		super(2000);
	}
	
	public void put( URI uri, ClassTreeNode node)
	{
		Set s = get( uri );
		if (s == null)
		{
			HashSet hs = new HashSet();
			hs.add(node);
			super.put( uri, hs);
		}
		else
		{
			s.add(node);
			super.put( uri, s);
		}
	}
	
	public Set get( URI uri)
	{
		Set set = (Set)super.get(uri);
		if (set == null)
			return new HashSet(); // return empty set if there is no match		
		return set;		
	}
}
