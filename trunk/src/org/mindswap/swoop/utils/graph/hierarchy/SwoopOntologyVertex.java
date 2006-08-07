/*
 * Created on Jul 28, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.mindswap.swoop.utils.graph.hierarchy;

import org.mindswap.swoop.utils.graph.hierarchy.colors.GraphColorScheme;
import org.semanticweb.owl.model.OWLOntology;

import edu.uci.ics.jung.graph.impl.DirectedSparseVertex;

/**
 * @author Dave Wang
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class SwoopOntologyVertex extends DirectedSparseVertex 
{
	public static final int NONE       = 0;
	public static final int FOCUS      = 2;
	public static final int UPSTREAM   = -1;
	public static final int DOWNSTREAM = 1;
	
	protected boolean isVisible     = true;
	protected boolean isSelected    = false;
	protected boolean isHighlighted = false;
	protected int     partitionState = 0;
	protected boolean isPartitionStateSet = false;
	
	protected OntologyWithClassHierarchyGraph myGraph = null;
	
	public SwoopOntologyVertex( OntologyWithClassHierarchyGraph graph )
	{ 
		super(); 
		myGraph = graph;
	}
	
	public void setVisible( boolean flag )
	{ isVisible = flag; }
	
	public boolean isVisible()
	{ return isVisible; }
	
	public void setHighlighted( boolean flag )
	{ isHighlighted = flag; }
	public boolean isHighlighted()
	{ return isHighlighted; }
	public void setPartitionState( int state )
	{ partitionState = state; }
	public int  getPartitionState()
	{ return partitionState; }
	public boolean isPartitionStateSet()
	{ return isPartitionStateSet; }
	public void setPartitionDirtyBit( boolean flag )
	{ isPartitionStateSet = flag; }
	
	public GraphColorScheme getColorScheme()
	{ return myGraph.getColorScheme(); }
	
	public OWLOntology getOntology()
	{ return ((OntologyGraphNode)this.getUserDatum( OntologyWithClassHierarchyGraph.DATA)).getOntology(); }
	
	public String toString()
	{
		return ((OntologyGraphNode)this.getUserDatum(OntologyWithClassHierarchyGraph.DATA)).toString();		
	}
	
	
}
