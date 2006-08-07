package org.mindswap.swoop.utils.graph.hierarchy;

import java.awt.Color;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.net.URI;
import java.util.Collections;
import java.util.Comparator;
import java.util.Vector;

import org.mindswap.swoop.SwoopModel;
import org.mindswap.swoop.utils.graph.hierarchy.colors.GraphColorScheme;
import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLException;


/*
 * Created on Jul 15, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */

/**
 * @author Dave Wang
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class ClassTreeNode 
{
	
	class DescendingClassTreeNodeComparator implements Comparator
	{
		public int compare(Object o1, Object o2)
		{
			ClassTreeNode t1 = (ClassTreeNode)o1;
			ClassTreeNode t2 = (ClassTreeNode)o2;
			int t1_size = t1.getSubTreeSize();
			int t2_size = t2.getSubTreeSize();
			if ( t1_size > t2_size)
				return -1;
			else if (t1_size == t2_size)
				return 0;
			else
				return 1;			
		}
		
		public boolean equals(Object o2)
		{
			return false;
		}
	}
	
	private URI myURI;
	private ClassTreeNode myParent = null;
	private Vector myChildren = new Vector();
	
	private int mySubTreeSize = 0;
	private double localX = -1;
	private double localY = -1;
	
	private double globalX = -1;
	private double globalY = -1; 
	
	private double myRotationAngle = 0;
	
	private boolean isSelected = false;    // whether selected by user via mouse
	private boolean isHighlighted = false; // whether highlighted via search
	private boolean isListBrowsed = false; // whether viewed by user via list
	private boolean isOverlayed   = false; // whether this class node is overlayed
	private boolean hasRelations  = false; // whether related to other classes via some restrictions  
	
	private SwoopModel myModel = null;
	private OntologyGraphNode myOntologyGraphNode = null; // init in sortChildren(....)
	private GraphColorScheme myColorScheme = null;        // init in sortChildren(....)
	
	private AffineTransform myXform;
	private AffineTransform myGlobalXform;
	
	private int myDepth = 0;
	private int mySubtreeDepth = 0;
	
	public ClassTreeNode(SwoopModel model, URI uri, int depth )
	{
		myURI   = uri;
		myModel = model;		
		myDepth = depth;
		// myOntologyNode, myColorSchem are set by 
		// call to sortChildren by OntologyGraphNode constructor
	}
	
	public int getNumChildren()
	{ return myChildren.size(); }
	
	public int getDepth()
	{ return myDepth; }
	
	public int getSubtreeDepth()
	{ return mySubtreeDepth; }
	public void setSubtreeDepth( int subtreeDepth )
	{ mySubtreeDepth = subtreeDepth; }
	
	public OWLClass getOWLClass()
	{
		try
		{
			return myOntologyGraphNode.getOntology().getClass( myURI);
		}
		catch( OWLException e)
		{
			e.printStackTrace();
		}
		return null;
	}
	
	
	public ClassTreeNode getChild(int index)
	{
		if ( (index < 0) || (index > (myChildren.size() -1) ) )
			return null;
		
		return (ClassTreeNode)myChildren.elementAt( index );
	}
	
	public ClassTreeNode addChild( ClassTreeNode node )
	{
		node.setParent( this );
		myChildren.add( node );
		return node;
	}
	
	public void setParent( ClassTreeNode parent )
	{ myParent = parent; }
	
	public void removeChild( ClassTreeNode node)
	{
		myChildren.remove( node );
	}
	
	public ClassTreeNode findNode( String id )
	{
		for (int i = 0; i < myChildren.size(); i++)
		{
			ClassTreeNode node = (ClassTreeNode)myChildren.elementAt(i);
			if (node.getURI().toString().equals(id))
				return node;
		}
		return null;
	}
	
	// returns size of subtree rooted at THIS node.  Includes itself
	public int getSubTreeSize()
	{
		// leaf case
		if ( myChildren.size() == 0 )
			return 1;
		
		int size = 0;
		for (int i = 0; i < myChildren.size(); i ++)
		{
			ClassTreeNode node = (ClassTreeNode)myChildren.elementAt( i );
			size = size + node.getSubTreeSize();
		}
		
		return size + 1;
	}
	
	public int getRadius()
	{ return getSubTreeSize() * SizeConstants.unitSize; }
	
	public URI getURI()
	{ return myURI; }
	
	public ClassTreeNode getParent()
	{ return myParent; }
	
	public Vector getChildren()
	{ return myChildren; }
	
	// guarantees for the current Colorscheme
	public GraphColorScheme getColorScheme()
	{ return myOntologyGraphNode.getColorScheme(); }
	public Color getFillColor()
	{ return myOntologyGraphNode.getColorScheme().getTreeNodeFillColor( this ); }
	public Color getOutlineColor()
	{ return myOntologyGraphNode.getColorScheme().getTreeNodeOutlineColor( this ); }
	
	// sets the center for this treenode.  It is local coordinate (from its parent's center)
	public void setLocalCenterPoint( double newx, double newy)
	{
		ClassTreeNode parentNode = this.getParent();
		// this node is root, so set global = center = (newx, newy)
		if ( parentNode == null )
		{
			this.localX  = newx;
			this.localY  = newy;
			return;
		}
		localX = newx;
		localY = newy;
	}

	public Point2D.Double getLocalCenter()
	{ return new Point2D.Double(this.localX, this.localY);	}

	public Point2D.Double computeGlobalCenter()
	{
		Point2D point = new Point2D.Double(0,0);
		Point2D.Double destPoint = new Point2D.Double();
		destPoint = (Point2D.Double)myGlobalXform.transform( point, destPoint );
		return destPoint;
	}
	public Point2D.Double getGlobalCenter()
	{ return new Point2D.Double( globalX, globalY); }
	
	
	public double getLocalX()
	{ return this.localX; }
	public double getLocalY()
	{ return this.localY; }
	public double getGlobalX()
	{ return this.globalX; }
	public double getGlobalY()
	{ return this.globalY; }
	
	// in radians
	public double getRotationAngle()
	{ return myRotationAngle; }	
	public void setRotationAngle( double angle)
	{ myRotationAngle = angle; }
	
	public void setAffineTransform( AffineTransform form )
	{ myXform = form; }
	
	// set global transform (which transforms (0,0) to this node's center)
	//  and also set the global center
	public void setGlobalTransform( AffineTransform form )
	{ 
		myGlobalXform = form;
		Point2D gPoint = computeGlobalCenter();
		globalX = gPoint.getX();
		globalY = gPoint.getY();
	}
	
	
	
	// recursively sorts current tree node's child by (subtree)size ( largest first )
	// invoked by constructor of OntologyGraphNode.  myOntologyGraphNode is guaranteed
	// to set.  myColorScheme is guaranteed to set.
	public void sortChildren( OntologyGraphNode motherNode)
	{
		motherNode.indexNode( this.getURI(), this );
		Collections.sort( myChildren, new DescendingClassTreeNodeComparator());
		myOntologyGraphNode = motherNode;
		myColorScheme       = myOntologyGraphNode.getColorScheme();

		for (int i = 0; i < getNumChildren(); i++)
		{
			ClassTreeNode child = getChild(i);
			child.sortChildren( motherNode );
		}
	}
	
	public OntologyGraphNode getOntologyNode()
	{ return myOntologyGraphNode; }
	
	public boolean getIsSelected()
	{ return isSelected; }
	public void setIsSelected( boolean flag )
	{ isSelected = flag; }

	public boolean getIsHighlighted()
	{ return isHighlighted; }
	public void setIsHighlighted( boolean flag )
	{ isHighlighted = flag; }
	
	public boolean getIsListBrowsed()
	{ return isListBrowsed; }
	public void setIsListBrowsed( boolean flag )
	{ isListBrowsed = flag; }
	
	public boolean gethasRelations()
	{ return hasRelations; }
	public void sethasRelations( boolean flag)
	{ hasRelations = flag; }

	public boolean getIsOverlayed()
	{ return isOverlayed; }
	public void setIsOverlayed( boolean flag)
	{ isOverlayed = flag; }
	
	/* 
	 * 
	 * Returns the node that's selected by the point passed in.
	 *   If this node is selected, then it recursively finds if any child is selected (more
	 *   precisely selected node).
	 *      If true, then that child is returned
	 * 		else this node is returned
	 * 
	 * Invoked by GraphMouseImpl in TestVisualizationViewer
	 */	
	public ClassTreeNode getSelectedChild( Point2D p)
	{
		ClassTreeNode selectedNode = null;
		Point2D.Double myCenter = this.getLocalCenter();
		// find xform that translate p to local space
		AffineTransform xform = AffineTransform.getTranslateInstance( -myCenter.x, -myCenter.y );
		Point2D localPoint = null;
		localPoint = xform.transform( p, localPoint );
		
		for (int i  = 0; i < getNumChildren(); i++)
		{
			ClassTreeNode child = getChild( i );
			ClassTreeNode selectedChild = child.getSelectedNode( localPoint );
			if (selectedChild != null)
				selectedNode = selectedChild;
		}
		return selectedNode;
	}
	
	//
	// localPoint is in the same local space as this node
	// however, it has not been rotated correctly, since this node's parent may have rotated
	//
	public ClassTreeNode getSelectedNode( Point2D localPoint )
	{
		ClassTreeNode selectedNode = null;

		if ( isWithinMe( localPoint ) )
		{
			selectedNode = this;   // set return value to self.
			Point2D result= null;
			AffineTransform xform = null;
			
			try
			{
				xform = myXform.createInverse();
			}
			catch( Exception e)
			{ e.printStackTrace(); }
			
			Point2D newLocal = null;
			newLocal = xform.transform( localPoint, newLocal );
			
			for (int i  = 0; i < getNumChildren(); i++)
			{
				ClassTreeNode child = getChild( i );
				ClassTreeNode selectedChild = child.getSelectedNode( newLocal );
				if (selectedChild != null)
					selectedNode = selectedChild;
			}
		}
		return selectedNode;
	}
	
	//
	// point p is in local space, same space as this node's parent
	//
	private boolean isWithinMe( Point2D p )
	{
		if (Math.sqrt( (Math.pow(p.getX() - this.localX , 2)  ) +
				        Math.pow(p.getY() - this.localY , 2)  ) <= this.getRadius())
			return true;
		return false;
	}

	public String toString()
	{ return myModel.shortForm( this.getURI() ); }
	
	// differs from toString() in that this method only returns
	//   the name of the class (no qnames when qname toggle is on in swoop)
	public String getName()
	{
		String name = myModel.shortForm( this.getURI() );
		int index = name.indexOf(":");
		if (index == -1)
			return name;
		else
			return name.substring( index );
	}
}



