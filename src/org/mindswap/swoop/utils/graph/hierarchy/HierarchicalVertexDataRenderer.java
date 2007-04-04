package org.mindswap.swoop.utils.graph.hierarchy;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;

/*
 * Created on Jul 17, 2005
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
public class HierarchicalVertexDataRenderer implements SizeConstants
{
		
	//private static final Color classColor = new Color(128, 128, 128);
	//private static final Color selectedClassColor = new Color(255, 255, 225);
	//private static final Color highlightedColor   = new Color(128, 255, 255);
	
	private static HierarchicalVertexDataRenderer myInstance = null;
	
	public static HierarchicalVertexDataRenderer getInstance()
	{
		if (myInstance == null)
			return new HierarchicalVertexDataRenderer();
		else
			return myInstance;
	}
	
	private HierarchicalVertexDataRenderer()
	{}
	
	public void render( Graphics2D g2d, int x, int y, ClassTreeNode node)
	{
		int r = node.getSubTreeSize() * SizeConstants.unitSize;
		int numChildren = node.getNumChildren();
		
		// center of top class node (OWL:Thing )is the same as the ontology node
		//   this setting is important.  Subsequent mouse clicks relies on this to 
		//   find its children.
		node.setLocalCenterPoint(x, y);
		
		
		Color aColor = node.getFillColor();
		
		TreeNodeSizeInfo info = node.getChildNodeSizeInfo();
		for (int i = 0; i < numChildren; i++ )
		{
			ClassTreeNode child = node.getChild( i );
			render(g2d, x, y, r, child, numChildren - 1, i, info, aColor );
		}
	}
	
	// x,y are parent's center
	public void render( Graphics2D g2d, int x, int y, int radius, ClassTreeNode node, 
			            int numSiblings, int index, TreeNodeSizeInfo siblingInfo, 
						Color currentColor)
	{
		int r = node.getRadius();
		
		// if node is selected/highlighted, use those colors :)
		// if parent is 'selected' mark this child with selected color as well
		if ( currentColor != node.getColorScheme().getTreeNodeSelectFillColor( node ) )
			currentColor = node.getFillColor();
		g2d.setColor( currentColor );
		
		// if a node is listBrowsed, we get that color and overrides the others
		if ( node.getIsListBrowsed() )
			g2d.setColor( node.getFillColor() );
		
		AffineTransform old_xform = g2d.getTransform();
		
		double rotationAngle = 0;
		
		g2d.translate(x, y);
		
		
		AffineTransform localForm = node.getLocalXForm();
		// if nodes do not have localForm yet, compute it; otherwise, reuse it
		if ( localForm == null )
		{
			if (numSiblings == 0)
			{
				localForm = AffineTransform.getTranslateInstance(0,0); // no op transform to prevent NullPointerException
			}
			else
			{
				double increAngle = 360.00/(numSiblings + 1);
				double currentAngle = increAngle * index;
				if (siblingInfo.areSameSize) // all siblings are the same size.
				{
					rotationAngle = currentAngle * 2 * Math.PI / 360.00; 
					localForm    = AffineTransform.getRotateInstance( rotationAngle );
					AffineTransform translation = AffineTransform.getTranslateInstance( radius/2 , 0 );
					localForm.concatenate( translation );
					//g2d.transform( localForm );
				}
				else if ( index == 0) // largest node
				{
					rotationAngle = currentAngle * 2 * Math.PI / 360.00; 
					localForm    = AffineTransform.getRotateInstance( rotationAngle );
					AffineTransform translation = AffineTransform.getTranslateInstance( radius - r - (SizeConstants.unitSize/2), 0 );
					localForm.concatenate( translation );
					//g2d.transform( localForm );
				}
				else // has at least one sibling that's larger
				{
					if ( siblingInfo.isFirstChildBig ) // largest sibling dominates
					{
						ClassTreeNode bigSib = node.getParent().getChild(0);
						Point2D.Double bigSibCenter = bigSib.getLocalCenter();
						if ( index == 1)
						{
							double angle = 0;
							double increment = 10;
							boolean isFitting = false;
							while ( !isFitting )
							{
								rotationAngle = (90 + 20 + angle) * 2 * Math.PI / 360.0;
								localForm = AffineTransform.getTranslateInstance( bigSibCenter.x, bigSibCenter.y );
								localForm.concatenate( AffineTransform.getRotateInstance(rotationAngle));
								localForm.concatenate( AffineTransform.getTranslateInstance( node.getRadius() + bigSib.getRadius() + SizeConstants.unitSize, 0));
								angle = angle + increment;
								
								Point2D.Double origin = new Point2D.Double(0, 0);
								origin = (Point2D.Double)localForm.transform(origin, origin);
								double dist = Math.sqrt( Math.pow( origin.x, 2) + Math.pow(origin.y, 2) );
								if ( dist <= (radius - node.getRadius()))
									isFitting = true;
							}
							
							//g2d.transform( localForm );
							
						}
						else
						{		
							/* Computing the angle needed to rotate the current node by wrt the 
							 *  largest sibling.
							 * Computing the distance needed to translate the current node by
							 *  wrt to the center of the largest sibling.
							 *  
							 * The computed distance is the same distance between current node's
							 *  previous sibling to the largest sibling.
							 * The compute rotation gives a distance between the current node's
							 *  center and its previous sibling's center of (r1 + r2 + spacing*3),
							 *  where r1 is preivous sib's radius, r2 is current node's radius, and
							 *  spaceing = sizeConstants.unitSize/2
							 * 
							 * Computing theta relies on the fact that the triangle A-B-C 
							 *  (where A is the center of the largest sibling, B is the 
							 *  center of the previous sibling, and C is the center of the 
							 *  projected current node), is isoscele.  Splitting the triangle
							 *  by bisecting its lone odd angle into 2 right triangles, we use
							 *  arcsine to calculate the required angle.
							 * 
							 */
							ClassTreeNode prevSib = node.getParent().getChild( index - 1);
							int sibRadius  = prevSib.getRadius();
							int thisRadius = node.getRadius();
							int radiiSum = sibRadius + thisRadius + SizeConstants.unitSize/2 + SizeConstants.unitSize;
							Point2D.Double sibCenter = prevSib.getLocalCenter();
							double dCenterToSib = Math.sqrt( Math.pow(bigSibCenter.x - sibCenter.x, 2) + Math.pow(bigSibCenter.y - sibCenter.y, 2) );
							double theta = Math.asin( (radiiSum/2) / dCenterToSib) * 2;
									
							rotationAngle = theta + prevSib.getRotationAngle();
							
							localForm = AffineTransform.getTranslateInstance( bigSibCenter.x, bigSibCenter.y );
							localForm.concatenate( AffineTransform.getRotateInstance(rotationAngle));
							localForm.concatenate( AffineTransform.getTranslateInstance( dCenterToSib, 0));
							//g2d.transform( localForm );
						}
					}
					else // largest sibling isn't too dominating
					{
						ClassTreeNode prevSib = node.getParent().getChild( index - 1);
						int sibRadius  = prevSib.getRadius();
						int thisRadius = node.getRadius();
						int radiiSum = sibRadius + thisRadius + SizeConstants.unitSize/2;
						Point2D.Double sibCenter = prevSib.getLocalCenter();
						double dCenterToSib = Math.sqrt( Math.pow(sibCenter.x, 2) + Math.pow(sibCenter.y, 2) );
						double theta = Math.asin( (radiiSum/2) / dCenterToSib) * 2;
						
						rotationAngle = theta + prevSib.getRotationAngle();
						
						localForm    = AffineTransform.getRotateInstance( rotationAngle );
						AffineTransform translation = AffineTransform.getTranslateInstance( dCenterToSib, 0);
						localForm.concatenate( translation );
					}
				}
			}
			//System.out.println("Computed localXForm");
		}
		
		g2d.transform( localForm );
		g2d.fillOval( -r, -r, 2*r, 2*r );
		g2d.setColor( node.getOutlineColor() );
		g2d.drawOval( -r, -r, 2*r, 2*r );

		Point2D.Double origin = new Point2D.Double(0, 0);			
		Point2D.Double result = (Point2D.Double)localForm.transform( origin, origin);
		node.setLocalCenterPoint( result.x, result.y );
		node.setGlobalTransform( g2d.getTransform() );
		node.setRotationAngle( rotationAngle );
		node.setLocalXForm( localForm );
		
		double [] mat = new double [6];
		localForm.getMatrix( mat );
		
		int numChildren = node.getNumChildren();		
		//int maxChildRadius = getMaxChildRadius( node );
		TreeNodeSizeInfo info = node.getChildNodeSizeInfo();
		for (int i = 0; i < numChildren; i++)
		{
			ClassTreeNode childNode = node.getChild( i );
			render(g2d, 0, 0, r, childNode, numChildren - 1, i, info, currentColor );
		}
		g2d.setTransform( old_xform );
	}

	/*
	private TreeNodeSizeInfo getChildNodeSizeInfo( ClassTreeNode node)
	{
		int numChildren = node.getNumChildren();
		if (numChildren == 0)
			return null;
		int maxChildRadius = Integer.MIN_VALUE;
		ClassTreeNode largestNode = null;
		boolean isFirstTime = true;
		boolean areSameSize = true;
		boolean isFirstChildLarge = false;
		for (int i = 0; i < numChildren; i++ )
		{
			ClassTreeNode child = node.getChild( i );
			if ( i == 0 ) //largest node
			{
				if ( (2 * child.getRadius()) > (node.getRadius() * 0.75) )
					isFirstChildLarge = true;
			}
			int childsize = child.getRadius();
			if ((!isFirstTime) && ( maxChildRadius != childsize))
				areSameSize = false;
			if ( maxChildRadius < childsize)
			{
				maxChildRadius = childsize;
				largestNode = child;
			}
			if ( isFirstTime )
				isFirstTime = !isFirstTime;
		}
		TreeNodeSizeInfo info = new TreeNodeSizeInfo();
		info.areSameSize = areSameSize;
		info.maxRadius = maxChildRadius;
		info.myLargestNode = largestNode;
		info.isFirstChildBig = isFirstChildLarge;
		return info;
	}
	*/
}
