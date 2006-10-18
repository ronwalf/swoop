/*
 * Created on Nov 13, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.mindswap.swoop.utils.graph.hierarchy.classlist;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JList;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.mindswap.swoop.utils.graph.hierarchy.ClassTreeNode;
import org.mindswap.swoop.utils.graph.hierarchy.OntologyVisualizationViewer;
import org.mindswap.swoop.utils.graph.hierarchy.OntologyWithClassHierarchyGraph;

/**
 * @author Dave Wang
 *
 * Listens to list and mouse events on a list
 *  
 */

public class ClassListMouseListener extends MouseAdapter implements ListSelectionListener
{
	private JList myList = null;
	private OntologyVisualizationViewer myVV = null;
	
	public ClassListMouseListener( JList list, OntologyVisualizationViewer vv )
	{ 
		super();
		myList = list;
		myVV = vv;
	}
	
	public void mouseClicked(MouseEvent e)
	{
		if ( e.getClickCount() == 2)
		{
			int index = myList.locationToIndex(e.getPoint());
			Object obj = myList.getSelectedValue();
			ClassTreeNode node = (ClassTreeNode)obj;			
			myVV.setSelectedNode( node );
			myVV.panZoomToFitNode( node, node.getParent() );
			//myVV.repaint();
		}
	}

	public void valueChanged(ListSelectionEvent e) 
	{
		Object obj = myList.getSelectedValue();
		if ( obj == null) // as this happens when the entire list is updated (no current selection)
			return;
		
		// get/set/repaint the new listBrowsed node
		ClassTreeNode node = (ClassTreeNode)obj;
		myVV.setListBrowsedNode( node );
		myVV.repaint();
	}

	/*
	public void keyTyped(KeyEvent e) 
	{
		System.out.println( "key pressed: " +e.getKeyCode() +" " + e.getKeyChar() );
		if ( (e.getKeyChar() == KeyEvent.VK_LEFT ) || ( e.getKeyChar() == KeyEvent.VK_KP_LEFT))
			System.out.println("left key pressed");
	}

	public void keyPressed(KeyEvent e) 
	{}

	public void keyReleased(KeyEvent e) 
	{}
	*/
}

