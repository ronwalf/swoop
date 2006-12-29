/*
 * Created on Aug 29, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.mindswap.swoop.utils.graph.hierarchy.popup;

import java.util.Comparator;

/**
 * @author Dave Wang
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class SubclassAxiomContainerComparator implements Comparator
{
	private static SubclassAxiomContainerComparator myInstance = null;
	
	private SubclassAxiomContainerComparator()
	{ }
	
	public static SubclassAxiomContainerComparator getInstance()
	{
		if (myInstance == null)
			myInstance = new SubclassAxiomContainerComparator();
		return myInstance;
	}
	
	public int compare(Object o1, Object o2) 
	{
		try
		{
			ClassAxiomContainer sac1 = (ClassAxiomContainer)o1;
			ClassAxiomContainer sac2 = (ClassAxiomContainer)o2;
			String name1 = sac1.toString();
			String name2 = sac2.toString();
			return name1.compareTo( name2 );
		}
		catch (Exception e)
		{
			System.out.println( o1.getClass().getName() );
			System.out.println( o2.getClass().getName() );
			e.printStackTrace();
		}
		return -1;
	}
	
}