/*
 * Created on Nov 13, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.mindswap.swoop.utils.graph.hierarchy.classlist;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JList;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;

import org.mindswap.swoop.utils.graph.hierarchy.ClassTreeNode;
import org.mindswap.swoop.utils.graph.hierarchy.OntologyVisualizationViewer;
import org.mindswap.swoop.utils.graph.hierarchy.OntologyWithClassHierarchyGraph;
import org.mindswap.swoop.utils.graph.hierarchy.colors.DefaultColors;

/**
 * @author Dave Wang
 *
 * Widget for a list of classes.  A simple extension to JList.  Receives events from
 *  mouse and keyboard ( {up, down}(from list selection listener) 
 *  {left, right, enter} (fromkey binding)).
 * 
 */

public class ClassList extends JList 
{
	class DrillDownAction extends AbstractAction
	{
		public void actionPerformed(ActionEvent e) 
		{
			Object obj = getSelectedValue(); // check this list's selection
			if ( obj == null) // as this happens when the entire list is updated (no current selection)
				return;
			ClassTreeNode node = (ClassTreeNode)obj;
			myVV.setSelectedNode( node );
			myVV.smartPanZoomToNode( node, node.getOntologyNode().getTreeNode() );
			myVV.repaint();
		}		
	}
	class DrillUpAction extends AbstractAction
	{
		// find currently selected node and drill up to its parent
		public void actionPerformed(ActionEvent e) 
		{
			ClassTreeNode node = myVV.getCurrentSelectedNode();
			ClassTreeNode parent = node.getParent();
			if ( parent == null ) // node is owl:Thing
				return;
			myVV.setSelectedNode( parent );
			
			if ( !myVV.isNodeOnScreen( parent) )
				myVV.panZoomToFitNode( parent, parent.getOntologyNode().getTreeNode() );
			
			myVV.setListBrowsedNode( node );
			setSelectedValue( node, true  );
		}		
	}
	
	private static String DRILL_DOWN_ACTION = "Drill down";
	private static String DRILL_UP_ACTION   = "Drill up";
	
	private OntologyWithClassHierarchyGraph myGraph = null;
	private OntologyVisualizationViewer myVV        = null;
	
	public ClassList( OntologyWithClassHierarchyGraph graph )
	{
		// data
		myGraph = graph;
		myVV    = myGraph.getVV();
		
		// listener
		ClassListMouseListener listener = new ClassListMouseListener( this, graph.getVV()  );
		addMouseListener( listener);
		addListSelectionListener( listener );
		
		// aesthetics
		setSelectionBackground( DefaultColors.LIST_BROWSED_FILL_COLOR );
		setSelectionForeground( Color.BLACK );
		setSelectionMode( ListSelectionModel.SINGLE_INTERVAL_SELECTION );		
		
		// key bindings
		setupKeyBindings();
	}

	private void setupKeyBindings()
	{
		// setting up key actions
		ActionMap amap = getActionMap();
		InputMap  imap = getInputMap();
		imap.put( KeyStroke.getKeyStroke( KeyEvent.VK_LEFT, 0 ),  DRILL_UP_ACTION );
		imap.put( KeyStroke.getKeyStroke( KeyEvent.VK_RIGHT, 0 ), DRILL_DOWN_ACTION );
		imap.put( KeyStroke.getKeyStroke( KeyEvent.VK_ENTER, 0 ), DRILL_DOWN_ACTION );
		amap.put( DRILL_UP_ACTION, new DrillUpAction() ); 
		amap.put( DRILL_DOWN_ACTION, new DrillDownAction() );
	}
	
}
