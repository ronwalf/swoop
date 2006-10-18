package org.mindswap.swoop.utils.graph.hierarchy;

public class TreeNodeSizeInfo 
{
	
	public boolean areSameSize = false;
	public boolean isFirstChildBig = false; // big == diameter of child > (75% of radius of parent)
	public ClassTreeNode myLargestNode = null;
	public int maxRadius = Integer.MIN_VALUE;
	
	private static TreeNodeSizeInfo NULLINFO = null;
	public static TreeNodeSizeInfo getNullInfo()
	{
		if ( NULLINFO == null )
			NULLINFO = new TreeNodeSizeInfo();
		return NULLINFO;
	}
}
