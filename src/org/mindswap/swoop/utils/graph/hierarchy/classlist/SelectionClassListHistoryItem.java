/*
 * Created on Nov 13, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.mindswap.swoop.utils.graph.hierarchy.classlist;

import javax.swing.ListModel;

import org.mindswap.swoop.utils.graph.hierarchy.ClassTreeNode;
import org.mindswap.swoop.utils.graph.hierarchy.OntologyVisualizationViewer;
import org.mindswap.swoop.utils.graph.hierarchy.OntologyWithClassHierarchyGraph;

/**
 * @author Dave Wang
 *
 * represents a class list item created via a user selection
 * 
 */

public 	class SelectionClassListHistoryItem extends ClassListHistoryItem
{
	private ClassTreeNode myNode = null;
	
	public SelectionClassListHistoryItem( OntologyWithClassHierarchyGraph graph,ListModel model, ClassTreeNode node, String label )
	{ 
		super( graph, model, label);  
		myNode = node;
	}
	
	public void executeLoadHistory()
	{
		myGraph.loadClassListHistory( this );
		OntologyVisualizationViewer vv = myGraph.getVV(); 
		vv.setSelectedNodeWithoutAddingHistory( this.myNode );
		vv.repaint();
		//myBackListButton.setEnabled( !myClassListHistoryManager.isAtBeginning() );
		//myForwardListButton.setEnabled( !myClassListHistoryManager.isAtEnd() );
	}
}