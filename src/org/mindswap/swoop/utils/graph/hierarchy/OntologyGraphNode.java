package org.mindswap.swoop.utils.graph.hierarchy;

import java.net.URI;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

import org.mindswap.swoop.utils.graph.hierarchy.colors.GraphColorScheme;
import org.mindswap.swoop.utils.owlapi.DefaultShortFormProvider;
import org.semanticweb.owl.io.ShortFormProvider;
import org.semanticweb.owl.model.OWLException;
import org.semanticweb.owl.model.OWLOntology;

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
public class OntologyGraphNode implements SizeConstants
{
	protected ShortFormProvider shortFormProvider = new DefaultShortFormProvider();
	protected URI myURI = null;
	protected OWLOntology myOntology = null;
	protected Vector myNeighbors = null;
	protected ClassTreeNode myData = null;
	protected ClassTreeNodeIndex myTreeNodeIndex = null;
	protected OntologyWithClassHierarchyGraph myGraph = null;
	
	
	public OntologyGraphNode( OWLOntology ontology, ClassTreeNode data, OntologyWithClassHierarchyGraph graph )
	{
		try
		{
			myTreeNodeIndex = new ClassTreeNodeIndex();
			myOntology = ontology;
			myURI = ontology.getURI();
			myGraph = graph;
			myNeighbors = new Vector();
			myData = data;			
			if (data == null)
			{
				System.out.println(ontology.getURI().toString() + "  has null tree");
			}
			sortMyTree();
		}
		catch (OWLException e)
		{ e.printStackTrace(); }
	}
	
	public void addNeighbor( OntologyGraphNode node )
	{
		myNeighbors.add( node );
	}
	
	public OntologyGraphNode getNeighbor(int index )
	{
		if ( (index < 0) || (index > myNeighbors.size()-1))
			return null;
		
		return (OntologyGraphNode)myNeighbors.elementAt( index );
	}

	public boolean isVisible()
	{ return ((SwoopOntologyVertex)myGraph.getVisualGraph().getUserDatum( this )).isVisible; }
	
	public URI getURI()
	{ return myURI; }
	
	public int getNumNeighbors()
	{ return myNeighbors.size(); }
	
	public int getSize()
	{ return this.myData.getSubTreeSize() + 1; }
	
	public int getRadius()
	{ return getSize() * SizeConstants.unitSize; }
	
	public int getDiameter()
	{ return getRadius() * 2; }
	
	public GraphColorScheme getColorScheme()
	{ return myGraph.getColorScheme(); }
	
	public ClassTreeNode getTreeNode()
	{ return myData; }
		
	public Vector getNeighbors()
	{ return myNeighbors; }
	
	public OWLOntology getOntology()
	{ return myOntology; }
	
	private void sortMyTree()
	{ myData.sortChildren( this ); }
	
	public void indexNode( URI uri, ClassTreeNode node)
	{
		myTreeNodeIndex.put( uri, node);
	}
	
	public Set findNodesBy( URI uri )
	{
		return myTreeNodeIndex.get( uri );
	}
	
	/*
	 * given String subText, this method finds all ClassTreeNodes
	 *  that has names that contain this subText.  A set of
	 *  these ClassTreeNodes is returned
	 */
	public Set matchNodeWithShortName( String subText )
	{
		HashSet set = new HashSet(); 
		
		Collection nodes =  myTreeNodeIndex.values();		
		for (Iterator it = nodes.iterator(); it.hasNext(); )
		{
			HashSet setOfSameNameNodes = (HashSet)it.next();
			for ( Iterator iter = setOfSameNameNodes.iterator() ; iter.hasNext(); )
			{
				ClassTreeNode n = (ClassTreeNode)iter.next();
				if ( n.getName().toLowerCase().indexOf( subText ) != -1)
					set.add( n );
			}		
		}
		return set;
	}
	
	public String toString()
	{ return shortFormProvider.shortForm( myURI ); }
}
