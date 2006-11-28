package org.mindswap.swoop.explore;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

import org.mindswap.swoop.SwoopModel;
import org.mindswap.swoop.reasoner.PelletReasoner;
import org.mindswap.swoop.reasoner.SwoopReasoner;
import org.mindswap.swoop.reasoner.SwoopToldReasoner;
import org.mindswap.swoop.renderer.entity.ConciseFormatEntityRenderer;
import org.mindswap.swoop.utils.graph.hierarchy.OntologyGraphNode;
import org.mindswap.swoop.utils.graph.hierarchy.OntologyWithClassHierarchyGraph;
import org.mindswap.swoop.utils.graph.hierarchy.SwoopOntologyVertex;
import org.mindswap.swoop.utils.graph.hierarchy.popup.SubclassAxiomContainer;
import org.mindswap.swoop.utils.graph.hierarchy.popup.SubclassAxiomContainerComparator;
import org.mindswap.swoop.utils.owlapi.OWLDescriptionFinder;
import org.semanticweb.owl.impl.model.OWLDataFactoryImpl;
import org.semanticweb.owl.impl.model.OWLSubClassAxiomImpl;
import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLDescription;
import org.semanticweb.owl.model.OWLException;
import org.semanticweb.owl.model.OWLOntology;

public class AxiomExtractor 
{
	private Set myOntologies = null;
	private SwoopModel myModel = null;
	private SwoopReasoner myReasoner = null;
	private ConciseFormatEntityRenderer myShortform = null;
	
	public AxiomExtractor( OWLOntology ont, SwoopModel model )
	{
		init( model );
		
		myOntologies = new HashSet();
		myOntologies.add( ont );
	}
	
	private void init( SwoopModel model )
	{
		myModel     = model;
		myShortform = new ConciseFormatEntityRenderer();
		myShortform.setSwoopModel( myModel );
		
	}

	public Vector extractClassAxioms( boolean withReasoner )
	{
		// use  a sorted set
		Set subclassAxioms = new TreeSet( SubclassAxiomContainerComparator.getInstance() );
		try
		{
			for (Iterator it = myOntologies.iterator(); it.hasNext(); )
			{
				OWLOntology ont = (OWLOntology)it.next();
				myModel.setSelectedOntology( ont );
				try
				{
					if ( withReasoner ) // load pellet for each ontology
					{	
						System.err.println("AxiomExtractor: setting Pellet reasoner");
						PelletReasoner reasoner = new PelletReasoner();
						// use pellet.  Place it in model's reasonerCache.
						myModel.setReasonerWithThreadBlock( reasoner );						
						myReasoner = myModel.getReasonerCache().getReasoner(ont, reasoner.getName() );
					}
					else // use told reasoner
					{
						System.err.println("AxiomExtractor: setting told reasoner");
						SwoopToldReasoner reasoner = new SwoopToldReasoner();
						myModel.setReasoner( reasoner );
						myReasoner = myModel.getReasonerCache().getReasoner(ont, reasoner.getName() );
					}
				}
				catch ( Exception e )
				{
					System.err.println("** Cannot set reasoner **");
					e.printStackTrace();
				}
				
				OWLClass owlThing = ont.getOWLDataFactory().getOWLThing();
				OWLDataFactoryImpl factory = (OWLDataFactoryImpl)ont.getOWLDataFactory();
				Set classSet = ont.getClasses();
				
				for (Iterator iter = classSet.iterator(); iter.hasNext(); ) 
				{
					OWLClass clazz = (OWLClass)iter.next();
					
					System.out.println( clazz );
					
					Set supers = OWLDescriptionFinder.getSuperClasses(clazz, Collections.singleton(ont));
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
}
