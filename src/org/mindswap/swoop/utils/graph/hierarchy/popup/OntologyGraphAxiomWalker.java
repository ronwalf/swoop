/*
 * Created on Aug 28, 2005
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.mindswap.swoop.utils.graph.hierarchy.popup;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

import org.mindswap.swoop.SwoopModel;
import org.mindswap.swoop.reasoner.SwoopReasoner;
import org.mindswap.swoop.renderer.entity.ConciseFormatEntityRenderer;
import org.mindswap.swoop.utils.graph.hierarchy.OntologyGraphNode;
import org.mindswap.swoop.utils.graph.hierarchy.OntologyWithClassHierarchyGraph;
import org.mindswap.swoop.utils.graph.hierarchy.SwoopOntologyVertex;
import org.mindswap.swoop.utils.owlapi.OWLDescriptionFinder;
import org.semanticweb.owl.impl.model.OWLDataFactoryImpl;
import org.semanticweb.owl.impl.model.OWLSubClassAxiomImpl;
import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLDescription;
import org.semanticweb.owl.model.OWLException;
import org.semanticweb.owl.model.OWLOntology;

import edu.uci.ics.jung.graph.Vertex;


/**
 * @author Dave
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class OntologyGraphAxiomWalker 
{

	private Set myVertexSet = null;
	private SwoopModel myModel = null;
	private SwoopReasoner myReasoner = null;
	private ConciseFormatEntityRenderer myShortform = null;
	
	public OntologyGraphAxiomWalker( Vertex v, SwoopModel model )
	{
		myVertexSet = new HashSet();
		myVertexSet.add( v );
		init( model );
	}
	
	public OntologyGraphAxiomWalker( Set selectedVertices, SwoopModel model ) 
	{
		myVertexSet = selectedVertices;
		init( model );
	}
	
	private void init( SwoopModel model )
	{
		myModel     = model;
		myReasoner  = myModel.getReasoner();
		myShortform = new ConciseFormatEntityRenderer();
		myShortform.setSwoopModel( myModel );
	}
	
	public Vector getSubclassAxioms()
	{ 
		// use  a sorted set
		Set subclassAxioms = new TreeSet( SubclassAxiomContainerComparator.getInstance() );
		try
		{
			for (Iterator it = myVertexSet.iterator(); it.hasNext(); )
			{
				SwoopOntologyVertex vertex = (SwoopOntologyVertex)it.next();
				OWLOntology ont = ((OntologyGraphNode)vertex.getUserDatum( OntologyWithClassHierarchyGraph.DATA )).getOntology();
				//System.out.println("walking on " + ont.getURI());
				myReasoner.setOntology(ont);
				OWLClass owlThing = ont.getOWLDataFactory().getOWLThing();
				OWLDataFactoryImpl factory = (OWLDataFactoryImpl)ont.getOWLDataFactory();
				Set classSet = ont.getClasses();
				
				for (Iterator iter = classSet.iterator(); iter.hasNext(); ) 
				{
					OWLClass clazz = (OWLClass)iter.next();
					//System.out.println(" looking at " + clazz.getURI() );
					Set supers = OWLDescriptionFinder.getSuperClasses(clazz, Collections.singleton(ont));
					//System.out.println(" supers has " + supers.size() + " elements.");
					if(myReasoner.isConsistent(clazz)) 
					{
						// remove all the named classes because reasoner will eventually add them
						Iterator iterator = supers.iterator();
						while(iterator.hasNext())
							if(iterator.next() instanceof OWLClass)
								iterator.remove();		
						// add all the named superclasses (including inferred)
						supers.addAll(myReasoner.superClassesOf(clazz));
						// remove owl:Thing from the superclass set
						iterator = supers.iterator();
						while(iterator.hasNext()) 
						{
							Object o = iterator.next();
							if(o instanceof Set && ((Set)o).contains(owlThing))
								iterator.remove();
							else
							{
								OWLDescription desc = null;
								if ( o instanceof Set)
								{
									 desc = (OWLDescription)((Set)o).iterator().next();
								}
								else
									desc = (OWLDescription)o;
								subclassAxioms.add( new SubclassAxiomContainer( 
										              new OWLSubClassAxiomImpl( factory, (OWLDescription)clazz, desc),
													  myShortform, myModel ));
							}
						}						
					}
				}				
			}
			// return the sorted Vector
			return new Vector( subclassAxioms );
		}
		catch (OWLException ex)
		{
			ex.printStackTrace();
		}
		return null; 		
	}
	
	public Vector getDomainAxioms()
	{ 
		// use  a sorted set
		/*
		Set subclassAxioms = new TreeSet( DomainAxiomContainerComparator.getInstance() );
		try
		{
			for (Iterator it = myVertexSet.iterator(); it.hasNext(); )
			{
				SwoopOntologyVertex vertex = (SwoopOntologyVertex)it.next();
				OWLOntology ont = ((OntologyGraphNode)vertex.getUserDatum( OntologyWithClassHierarchyGraph.DATA )).getOntology();
				//System.out.println("walking on " + ont.getURI());
				myReasoner.setOntology(ont);
				OWLClass owlThing = ont.getOWLDataFactory().getOWLThing();
				OWLDataFactoryImpl factory = (OWLDataFactoryImpl)ont.getOWLDataFactory();
				Set classSet = ont.getClasses();
				
				for (Iterator iter = classSet.iterator(); iter.hasNext(); ) 
				{
					OWLClass clazz = (OWLClass)iter.next();
					//System.out.println(" looking at " + clazz.getURI() );
					Set supers = OWLDescriptionFinder.getSuperClasses(clazz, Collections.singleton(ont));
					//System.out.println(" supers has " + supers.size() + " elements.");
					if(myReasoner.isConsistent(clazz)) 
					{
						// remove all the named classes because reasoner will eventually add them
						Iterator iterator = supers.iterator();
						while(iterator.hasNext())
							if(iterator.next() instanceof OWLClass)
								iterator.remove();		
						// add all the named superclasses (including inferred)
						supers.addAll(myReasoner.superClassesOf(clazz));
						//System.out.println(" supers has " + supers.size() + " elements (after remove/add)");
						// remove owl:Thing from the superclass set
						iterator = supers.iterator();
						while(iterator.hasNext()) 
						{
							Object o = iterator.next();
							if(o instanceof Set && ((Set)o).contains(owlThing))
								iterator.remove();
							else
							{
								OWLDescription desc = null;
								if ( o instanceof Set)
								{
									 desc = (OWLDescription)((Set)o).iterator().next();
								}
								else
									desc = (OWLDescription)o;
								subclassAxioms.add( new SubclassAxiomContainer( 
										              new OWLSubClassAxiomImpl( factory, (OWLDescription)clazz, desc),
													  myShortform, myModel ));
							}
						}						
					}
				}				
			}
			// return the sorted Vector
			return new Vector( subclassAxioms );
		}
		catch (OWLException ex)
		{
			ex.printStackTrace();
		}
		*/
		//for (Iterator it = myVertexSet.iterator(); it.hasNext(); )
		//{
		//	SwoopOntologyVertex vertex = (SwoopOntologyVertex)it.next();
		//	OWLOntology ont = ((OntologyGraphNode)vertex.getUserDatum( OntologyWithClassHierarchyGraph.DATA )).getOntology();
		//}
		return null; 
	}
	
	public Vector getRangeAxioms()
	{ 
		for (Iterator it = myVertexSet.iterator(); it.hasNext(); )
		{
			SwoopOntologyVertex vertex = (SwoopOntologyVertex)it.next();
			OWLOntology ont = ((OntologyGraphNode)vertex.getUserDatum( OntologyWithClassHierarchyGraph.DATA )).getOntology();
		}
		return null; 
	}
	
	public Vector getIndividualAxioms()
	{ 
		for (Iterator it = myVertexSet.iterator(); it.hasNext(); )
		{
			SwoopOntologyVertex vertex = (SwoopOntologyVertex)it.next();
			OWLOntology ont = ((OntologyGraphNode)vertex.getUserDatum( OntologyWithClassHierarchyGraph.DATA )).getOntology();
		}
		return null; 
	}
	
	
}
