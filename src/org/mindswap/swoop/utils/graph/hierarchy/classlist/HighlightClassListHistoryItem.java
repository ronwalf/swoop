/*
 * Created on Nov 13, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.mindswap.swoop.utils.graph.hierarchy.classlist;

import java.util.Set;

import javax.swing.ListModel;

import org.mindswap.swoop.utils.graph.hierarchy.OntologyVisualizationViewer;
import org.mindswap.swoop.utils.graph.hierarchy.OntologyWithClassHierarchyGraph;

/**
 * @author Dave Wang
 *
 * Represents a class list item created via a search
 * 
 */

public 	class HighlightClassListHistoryItem extends ClassListHistoryItem
{
	private Set myNodeSet = null; 
	
	public HighlightClassListHistoryItem( OntologyWithClassHierarchyGraph graph, ListModel model, Set nodeSet, String label )
	{ 
		super( graph, model, label);  
		myNodeSet = nodeSet;
	}
	
	public void executeLoadHistory()
	{
		myGraph.loadClassListHistory( this );
		OntologyVisualizationViewer vv = myGraph.getVV();
		vv.setHighlightedNodeWithoutAddingHistory( this.myNodeSet );
		vv.repaint();
	}
}
