/*
 * Created on Nov 13, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.mindswap.swoop.utils.graph.hierarchy.classlist;

import javax.swing.ListModel;

import org.mindswap.swoop.utils.graph.hierarchy.OntologyWithClassHierarchyGraph;

/**
 * @author Dave Wang
 *
 *  Represents an abstract list item on the class list seen in MotherShip
 * 
 */
public abstract class ClassListHistoryItem
{	
	protected ListModel myModel     = null;
	protected String    myLabel = null;
	protected OntologyWithClassHierarchyGraph myGraph = null;
	
	public ClassListHistoryItem( OntologyWithClassHierarchyGraph graph, ListModel model, String label)
	{
		myModel = model;
		myLabel = label;
		myGraph = graph;
	}
	
	public String getLabel()
	{ return myLabel; }
	
	public ListModel getModel()
	{ return myModel; }
	
	abstract public void executeLoadHistory();
	
}