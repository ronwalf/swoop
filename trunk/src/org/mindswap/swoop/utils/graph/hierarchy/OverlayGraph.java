/*
 * Created on Sep 19, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.mindswap.swoop.utils.graph.hierarchy;

import java.awt.BasicStroke;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.util.Collection;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

import org.mindswap.swoop.SwoopModel;
import org.mindswap.swoop.reasoner.SwoopReasoner;
import org.mindswap.swoop.utils.graph.hierarchy.colors.GraphColorScheme;
import org.mindswap.swoop.utils.graph.hierarchy.helpers.ClassByRestrictionFinder;
import org.mindswap.swoop.utils.owlapi.OWLDescriptionFinder;
import org.semanticweb.owl.model.OWLAnd;
import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLDescription;
import org.semanticweb.owl.model.OWLEnumeration;
import org.semanticweb.owl.model.OWLException;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLOr;
import org.semanticweb.owl.model.OWLRestriction;

import edu.uci.ics.jung.graph.impl.DirectedSparseGraph;

/**
 * @author Dave Wang
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class OverlayGraph implements OntologyVisualizationViewer.Paintable
{
	
	protected class OverlayEdge
	{
		public ClassTreeNode mySource = null;
		public ClassTreeNode myDestination   = null;
		
		public OverlayEdge( ClassTreeNode src, ClassTreeNode dest )
		{
			mySource = src;
			myDestination   = dest;
		}
		
		public String toString()
		{ return mySource.getURI() + "-" + myDestination.getURI(); }
		public int hashCode()
		{ return this.toString().hashCode(); }
				
		public boolean equals(Object obj)
		{
			if (!(obj instanceof OverlayEdge))
				return false;
			return (obj.hashCode() == this.hashCode());
		}
		
		public void paint( Graphics2D g)
		{
			AffineTransform oldTransform = g.getTransform();
			Stroke          oldStroke    = g.getStroke();
			
			double x1 = mySource.getGlobalX();
			double y1 = mySource.getGlobalY();
			double x2 = myDestination.getGlobalX();
			double y2 = myDestination.getGlobalY();
			
			int srcRadius = mySource.getRadius();
			int destRadius = myDestination.getRadius();
			double xdiff      = x2 - x1;
			double ydiff      = y2 - y1;
			double full_length = Math.sqrt( Math.pow(xdiff, 2) + Math.pow(ydiff, 2) );			
			double phi = Math.acos( xdiff/full_length);
			double theta = 0;
			
			//System.out.println("("+x1+","+y1+"); ("+x2+","+y2+")");
			//System.out.println("phi = " + phi);
			
			if ( ydiff == 0 ) // slope is infinite
			{
				if ( y1 > y2)
					theta = 0.5 * Math.PI;
				else
					theta = 2/3 * Math.PI;
			}
			else
			{
				if ( (ydiff < 0) && (xdiff <= 0 ))       // quadrant 2
					theta =  -phi ;
				else if ( (ydiff > 0) && ( xdiff < 0 ) ) // quadrant 3
					theta = 1/2 * Math.PI + phi;
				else if ( (ydiff > 0) && ( xdiff > 0 ) ) // quadrant 4
					theta = -( 2 * Math.PI  - phi );
				else if ( (ydiff < 0) && ( xdiff >= 0 ) ) // quadrant 1
					theta = -phi;
			}
			g.setColor( myColorScheme.getOverlayGraphEdgeColor() );
			g.translate( x1, y1 );
			g.rotate( theta );
			
			// get the actual scaling factor used in visualization viewer.
			// assuming scaleX = scaleY
			double scaler = myGraphUI.getVV().getScaleX();
			double scaledSrcRad = srcRadius * scaler;
			double scaledDestRad = destRadius * scaler;
						
			int endX = (int)(full_length - scaledDestRad);
			int [] xCoords = { endX - 7, endX -7, endX};
			int [] yCoords = { 5, -5, 0};
			
			if ( scaler > 0.3 )
				g.setStroke( new BasicStroke(2));
			else
				g.setStroke( new BasicStroke( 1 ));
			
			g.drawLine((int)scaledSrcRad, 0, endX, 0);
			g.fillPolygon( xCoords, yCoords, 3);
			
			g.setTransform( oldTransform );
			g.setStroke( oldStroke );
		}
	}
	
	protected HashSet myNodes = null;      // set of nodes in the overlay graph
	protected HashSet myIndexNodes = null; // keeps track of nodes that have been added via addNodes
	protected Hashtable myEdges = null;    // set of edges in the overlay graph
	
	protected DirectedSparseGraph parentGraph = null;      
	protected SwoopModel          myModel     = null;
	protected OntologyWithClassHierarchyGraph myGraphUI = null;
	protected GraphColorScheme myColorScheme = null;
	
	protected Hashtable myCompleteRelations = null;
	
	public OverlayGraph( DirectedSparseGraph graph, SwoopModel model, OntologyWithClassHierarchyGraph ui )
	{
		this.parentGraph = graph; 
		this.myModel     = model;
		this.myGraphUI   = ui;
		this.myColorScheme = ui.getColorScheme();
		init();		
	}
	
	/*
	 * Iterating through all ontology vertices and bulid relations over the classes
	 *  in these ontologies.  Called in constructor of OntologyWithClassHierarchyGraph
	 */
	public void precomputeAllRelations()
	{
		try 
		{
			Set vertices = myGraphUI.getVisualGraph().getVertices();
			for (Iterator it = vertices.iterator(); it.hasNext();) 
			{
				SwoopOntologyVertex v = (SwoopOntologyVertex) it.next();
				OntologyGraphNode ontNode = (OntologyGraphNode) v.getUserDatum(OntologyWithClassHierarchyGraph.DATA);
//				myModel.setSelectedOntology(ontNode.getOntology());
				ClassTreeNode rootNode = ontNode.getTreeNode(); // root is OWL:Thing 
				traverseAndPrebuild(rootNode);
			}
		} catch (Exception e) 
		{
			e.printStackTrace();
		}
	}

	/*
	 * Traverses the tree and build relation for each node in the class tree
	 */
	private void traverseAndPrebuild( ClassTreeNode parentNode )
	{
		Vector children = parentNode.getChildren();
		for ( Iterator iter = children.iterator(); iter.hasNext(); )
		{
			ClassTreeNode node = (ClassTreeNode)iter.next(); // get a child
			precomputeEdges( node );                         // precompute its edges
			traverseAndPrebuild( node );                     // now do its children  
		}
	}
	
	/* 
	 * Given a class node, find all the relations it has to other classes, 
	 *   store these relations, and set the node to 'has relations'. 
	 */
	private void precomputeEdges( ClassTreeNode node )
	{
		try 
		{
			//System.out.println("precomputing for " + node.getURI() );
			if ( node.getOWLClass() == null )
				return;
			Set relatedClasses = findRelatedClasses(node.getOWLClass());
			//System.out.println("  -  has " + relatedClasses.size() + " many related classes: ");
			//for (Iterator it = relatedClasses.iterator(); it.hasNext(); )
			//	System.out.println( "  ++ " + ((ClassTreeNode)it.next()).getURI() );
			if ( relatedClasses.size() > 0 )
			{
				myCompleteRelations.put( node, relatedClasses );
				node.sethasRelations( true );
			}
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}
	}

	
	private void init()
	{
		this.myNodes = new HashSet( 100 );
		this.myIndexNodes = new HashSet( 100 );
		this.myEdges = new Hashtable( 500 );  // indexed by source node, managed by a linked list
		this.myCompleteRelations = new Hashtable( 500 ); 
	}
	
	/*
	 *  clear nodes and edges of the overlaygraph
	 *  clear the overlay mark on overlaied nodes
	 */
	public void clear()
	{
		for ( Iterator it = myNodes.iterator(); it.hasNext(); )
			((ClassTreeNode)it.next()).setIsOverlayed( false );
		this.myNodes = new HashSet( 100 );
		this.myIndexNodes = new HashSet( 100 );
		this.myEdges = new Hashtable( 500 );  // indexed by source node, managed by a linked list 
	}
	
	public void addNode( ClassTreeNode node )
	{
		// check to see if it has been passed in as an argument to this method 
		boolean isInserted = myIndexNodes.add(node);
		
		// if not, we insert it to myNodes, and grow edges out of it
		if ( isInserted )
		{
			myNodes.add( node );
			addEdges( node );
		}
	}
	
	private void addEdges( ClassTreeNode node )
	{
		try
		{
			node.setIsOverlayed( true );
			//myModel.setSelectedOntology( node.getOntologyNode().getOntology() );
			Set relatedClasses = (Set)myCompleteRelations.get( node );   // get from precomputed relations
			if ( relatedClasses == null)
				return;
			for ( Iterator it = relatedClasses.iterator(); it.hasNext(); )
			{
				Object obj = it.next();
				OWLClass cls = (OWLClass)obj;
				//System.out.println( cls.getURI().toString() );
			}
			Set parentVertices = parentGraph.getVertices();
			// walk through all ontologies to see if it contains any of the related classes
			// once found, put them into the 'myNodes' set
			// note that duplicate circles will be entered in duplications
			for (Iterator iter = parentVertices.iterator(); iter.hasNext(); )
			{
				SwoopOntologyVertex vertex = (SwoopOntologyVertex)iter.next();
				Object obj  = vertex.getUserDatum( OntologyWithClassHierarchyGraph.DATA );
				//System.out.println( obj.getClass().getName() );
				
				OntologyGraphNode ontoNode = (OntologyGraphNode)obj;
				for (Iterator it = relatedClasses.iterator(); it.hasNext(); )
				{
					OWLClass concept = (OWLClass)it.next();
					Set classTreeNodes = ontoNode.findNodesBy( concept.getURI() );
					if ( classTreeNodes != null )
					{				
						myNodes.addAll( classTreeNodes );
						for (Iterator i = classTreeNodes.iterator(); i.hasNext(); )
						{
							ClassTreeNode neighbor = (ClassTreeNode)i.next();
							neighbor.setIsOverlayed( true );
							//System.out.println("For " + myModel.shortForm( node.getURI()) + " - adding " + myModel.shortForm( neighbor.getURI() ) );
							
							HashSet edgeSet = (HashSet)myEdges.get( node );
							if ( edgeSet == null )
								edgeSet = new HashSet();
							edgeSet.add( new OverlayEdge(node, neighbor) );
							myEdges.put( node, edgeSet );
						}
					}
				}
			}
		}
		catch (OWLException e)
		{ e.printStackTrace(); }
	}	
	
	// code lifted from org.mindswap.swoop.TermsDisplay
	/* Need to do 3 things.
	 *  - look into subclass/equivalent axioms of this class for restrictions (arrows pointing out)
	 *  - look into properties for domains/ranges that relate this class to another one (arrows pointing in/out)
	 *  - look into show ref to see if other classes relate to this class (arrows pointing in)
	 * 
	 */
	/*
	public Set findRefs( OWLEntity entity )
	{
		try 
		{			
			Set claSet = new HashSet();
			//Set propSet = new HashSet();
			//Set indSet = new HashSet();

			Set vertices = parentGraph.getVertices();
			for ( Iterator iterator = vertices.iterator(); iterator.hasNext(); )
			{
				SwoopOntologyVertex vertex = (SwoopOntologyVertex)iterator.next();
				OntologyGraphNode node = (OntologyGraphNode)vertex.getUserDatum( OntologyWithClassHierarchyGraph.DATA );
				OWLOntology ontology = node.getOntology();
				Set references = OntologyHelper.entityUsage(ontology, entity);
				for (Iterator iter = references.iterator(); iter.hasNext(); ) 
				{
					Object obj = iter.next();
					if (obj instanceof OWLClass) claSet.add(obj);
					//else if (obj instanceof OWLProperty) propSet.add(obj);
					//else if (obj instanceof OWLIndividual) indSet.add(obj);
				}
			}
			for (Iterator it = claSet.iterator(); it.hasNext(); )
				System.out.println( it.next() );
			return claSet;

		} 
		catch (OWLException e) {
			e.printStackTrace();
		}	
		return null;
	}
	*/
	
	// returns a set of OWL classes that is related to clazz via restrictions/property domains/ranges
	protected Set findRelatedClasses( OWLClass clazz )
	{
		try
		{
			HashSet relatedClasses = new HashSet();

			SwoopReasoner reasoner = myModel.getReasoner();
			OWLClass owlThing = reasoner.getOntology().getOWLDataFactory().getOWLThing();
			ClassByRestrictionFinder finder =  new ClassByRestrictionFinder( myModel );
			
			// walk the INTERSECTIONs equivalent to this class
			Iterator it = OWLDescriptionFinder.getIntersections(clazz, reasoner.getOntologies()).iterator();		
			while(it.hasNext()) 
			{
				OWLAnd intersection = (OWLAnd) it.next();
				intersection.accept( finder );
				if ( finder.found() )
					relatedClasses.addAll( finder.getResult() );
				finder.reset();
			}
			
			// walk the UNIONs equivalent to this class	
			it = OWLDescriptionFinder.getUnions(clazz, reasoner.getOntologies()).iterator();
			while(it.hasNext()) 
			{
				OWLOr union = (OWLOr) it.next();
				union.accept( finder );
				if ( finder.found() )
					relatedClasses.addAll( finder.getResult() );
				finder.reset();
			}
	
			// walk ENUMERATIONs of classes that are equivalent to this class	
			it = OWLDescriptionFinder.getEnumerations(clazz, reasoner.getOntologies()).iterator();
			while(it.hasNext()) 
			{
				OWLEnumeration oneOf = (OWLEnumeration) it.next();
				oneOf.accept( finder );
				if ( finder.found() )
					relatedClasses.addAll( finder.getResult() );	
				finder.reset();
			}
	
			// print EQUIVALENT classes	
			Set eqs = OWLDescriptionFinder.getEquivalentClasses(clazz, reasoner.getOntologies());
			it = eqs.iterator();
			
			// removing the ones from above (and, or, oneof) and named classes
			while(it.hasNext()) 
			{
				OWLDescription desc = (OWLDescription) it.next();
				if(!(desc instanceof OWLRestriction))
					it.remove();
			}
			
			it = eqs.iterator();
			while (it.hasNext())
			{
				OWLDescription eq = (OWLDescription)it.next();
				eq.accept( finder );
				if ( finder.found() )
					relatedClasses.addAll( finder.getResult() );	
				finder.reset();
			}
			
			// SUBCLASS OF			
			// acquiring all non-inferred super classes (named or anon)
			Set supers = OWLDescriptionFinder.getSuperClasses(clazz, reasoner.getOntologies());
			Set ontos = reasoner.getOntologies();
			//System.out.println( " SUPER SET = " + supers.size() );
			if(reasoner.isConsistent(clazz)) 
			{
				// remove all the named classes
				it = supers.iterator();
				while(it.hasNext())
					if(it.next() instanceof OWLClass)
						it.remove();		
				// remove owl:Thing from the superclass set
				it = supers.iterator();
				while(it.hasNext()) {
					Object o = it.next();
					if(o instanceof Set && ((Set)o).contains(owlThing))
						it.remove();
				}
			}

			for (it = supers.iterator(); it.hasNext(); )
			{
				Object obj = (Object)it.next();				
				// if there is more than one element for this line
				// we only print the first one. rest are either
				// equivalent classes (or properties) 
				if(obj instanceof Collection)
					obj = ((Collection)obj).iterator().next();
				OWLDescription desc = (OWLDescription)obj;
				desc.accept( finder );
				if ( finder.found() )
					relatedClasses.addAll( finder.getResult() );
				finder.reset();
			}
			return relatedClasses;
		}
		catch ( OWLException e)
		{ e.printStackTrace(); }
		return null;
	}
	
	
	public void paint( Graphics g )
	{
		Graphics2D g2d = (Graphics2D)g;
		Set keys = myEdges.keySet();
		for ( Iterator it = keys.iterator(); it.hasNext(); )
		{
			ClassTreeNode node = (ClassTreeNode)it.next();
			HashSet edges = (HashSet)myEdges.get( node );
			for (Iterator iter = edges.iterator(); iter.hasNext(); )
			{
				OverlayEdge edge = (OverlayEdge)iter.next();
				edge.paint( g2d );
			}
		}
	}
	
	// whether this Paintable uses its own transformation. (no)
	public boolean useTransform() 
	{
		return false;
	}
}
