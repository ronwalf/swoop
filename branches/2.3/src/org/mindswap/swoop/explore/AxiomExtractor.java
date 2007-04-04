package org.mindswap.swoop.explore;

import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Hashtable;
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
import org.mindswap.swoop.utils.graph.hierarchy.popup.ClassAxiomContainer;
import org.mindswap.swoop.utils.graph.hierarchy.popup.ConcisePlainVisitor;
import org.mindswap.swoop.utils.graph.hierarchy.popup.SubclassAxiomContainerComparator;
import org.mindswap.swoop.utils.owlapi.AxiomCollector;
import org.mindswap.swoop.utils.owlapi.OWLDescriptionFinder;
import org.semanticweb.owl.impl.model.OWLDataFactoryImpl;
import org.semanticweb.owl.impl.model.OWLDisjointClassesAxiomImpl;
import org.semanticweb.owl.impl.model.OWLEquivalentClassesAxiomImpl;
import org.semanticweb.owl.impl.model.OWLSubClassAxiomImpl;
import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLClassAxiom;
import org.semanticweb.owl.model.OWLDescription;
import org.semanticweb.owl.model.OWLDisjointClassesAxiom;
import org.semanticweb.owl.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owl.model.OWLException;
import org.semanticweb.owl.model.OWLNot;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLSubClassAxiom;

import aterm.pure.ATermApplImpl;

public class AxiomExtractor 
{
	private Set myOntologies = null;
	private OWLOntology myOntology = null;
	private SwoopModel myModel = null;
	private SwoopReasoner myReasoner = null;
	private ConciseFormatEntityRenderer myShortform = null;
	
	private OWLClass owlThing = null;
	private OWLClass owlNothing = null;
	
	public AxiomExtractor( OWLOntology ont, SwoopModel model, SwoopReasoner reasoner )
	{
		init( ont, model, reasoner );
	}
	
	private void init( OWLOntology ont, SwoopModel model, SwoopReasoner reasoner )
	{
		try
		{
			myModel     = model;
			myShortform = new ConciseFormatEntityRenderer();
			myShortform.setSwoopModel( myModel );	
			myOntologies = new HashSet();
			myOntologies.add( ont );
			myReasoner = reasoner;
			
			owlThing   = myReasoner.getOntology().getOWLDataFactory().getOWLThing();
			owlNothing = myReasoner.getOntology().getOWLDataFactory().getOWLNothing();
		}
		catch ( OWLException e )
		{
			e.printStackTrace();
		}
	}

	public Vector extractSubclassAxioms( boolean withReasoner )
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
					Set supers = OWLDescriptionFinder.getSuperClasses(clazz, Collections.singleton(ont));
					//if(myReasoner.isConsistent(clazz)) 
					//{
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
								subclassAxioms.add( new ClassAxiomContainer( 
										              new OWLSubClassAxiomImpl( factory, (OWLDescription)clazz, desc),
													  myShortform, myModel ));
							}
						}
					//}
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

	/*
	 * extracts class axioms in the import closure of the currently selected ontology in Swoop
	 * 
	 */
	public Vector extractClassAxioms( )
	{
		// maps a class/class expression (key) to its set of equivalent classes/class expressions (values)
		// each equivvalent set contains the key as well.
		Hashtable equivalenceMap = new Hashtable();

		// use a sorted set
		Set classAxiomContainers = new TreeSet( SubclassAxiomContainerComparator.getInstance() );
		
		try
		{
			Set importClosure = myReasoner.getOntologies();
			/* 	for each ontology in the import closure
			 *	find all told class axioms
			 */	
			for ( Iterator ontIter = importClosure.iterator(); ontIter.hasNext(); )
			{ 
				OWLOntology ont = (OWLOntology)ontIter.next();
				Set axioms = AxiomCollector.axiomize( ont );
				for ( Iterator it = axioms.iterator(); it.hasNext(); )
				{
					Object obj = it.next();
					if ( (obj instanceof OWLClassAxiom))
					{
						if ( obj instanceof OWLEquivalentClassesAxiom)
						{
							OWLEquivalentClassesAxiom qui = (OWLEquivalentClassesAxiom)obj;
							Set equivalentClasses = qui.getEquivalentClasses();
							if ( equivalentClasses.size() == 1 )
								continue;
							else
							{ addToEquivalents( equivalenceMap, (HashSet)equivalentClasses ); }
						}
						else //subclass or disjoint axioms
							classAxiomContainers.add( new ClassAxiomContainer( (OWLClassAxiom)obj, myShortform, myModel ) );
					}
				}
			}
			
			
			/*
			 * now get the inferred axioms
			 * 
			 */
			for ( Iterator ontIter = importClosure.iterator(); ontIter.hasNext(); )
			{
				OWLOntology ont = (OWLOntology)ontIter.next();
				OWLDataFactoryImpl factory = (OWLDataFactoryImpl)ont.getOWLDataFactory();
				
				for ( Iterator classIter = ont.getClasses().iterator(); classIter.hasNext(); )
				{
					OWLClass c = (OWLClass)classIter.next();					
					// superclasses
					Set supers = c.getSuperClasses( importClosure );
					if( myReasoner.isConsistent(c) )
					{
						supers.addAll( myReasoner.superClassesOf(c));						
						// remove owl:Thing from the superclass set
						for (Iterator i = supers.iterator(); i.hasNext(); ) 
						{
							Object o = i.next();
							if(o instanceof Set && ((Set)o).contains(owlThing))
								i.remove();
							else
							{
								OWLDescription desc = null;
								if ( o instanceof Set)								
									desc = (OWLDescription)((Set)o).iterator().next();								
								else
									desc = (OWLDescription)o;
								classAxiomContainers.add( new ClassAxiomContainer( 
										              new OWLSubClassAxiomImpl( factory, (OWLDescription)c, desc),
													  myShortform, myModel ));
							}
						}
					}
					
					// equivalents ( intersections, unions, enumerations )
					Set equivalences =  new HashSet();
					equivalences.add( c );
					if ( myReasoner.isConsistent(c) )
					{
						// get all named equivalent classes (intersections, unions, enumerations )
						Set reasonedEquiSet = myReasoner.equivalentClassesOf(c);
						equivalences.addAll( reasonedEquiSet );
						
						// get complements classes (and make them into OWLNot, so they are equivalent to c)
						Set reasonedCompSet = myReasoner.complementClassesOf( c );
						for ( Iterator it = reasonedCompSet.iterator(); it.hasNext(); )
						{	
							OWLClass desc =  (OWLClass)it.next();
							OWLNot complement = factory.getOWLNot( desc );
							equivalences.add( complement );
						}
					}
					else // if not consistent, it is equivalent to owlNothing
					{ equivalences.add(owlNothing); }
					// only add to equivalenceMap if #equivalent classes is greater than 1 
					// ( only one means itself -- "A equivalentTo A")
					if ( equivalences.size() > 1 )
					{ addToEquivalents( equivalenceMap, (HashSet)equivalences ); }
					
					/* get all named disjoint classes for class 'c'
					 *   if c is inconsistent, then nothing is done
					 *   if c is consistent, then
					 *    - retrieave all disjoint classes from reasoner
					 * 
					 */
					HashSet disjointsForClass = new HashSet();
					if ( myReasoner.isConsistent(c) )
					{
						Set dis = myReasoner.disjointClassesOf( c );
						for (Iterator it = dis.iterator(); it.hasNext(); )
						{
							Set classes  = (Set)it.next();
							// only get the first class, since the rest are equivalents
							Iterator iter = classes.iterator();
							OWLClass aClass = (OWLClass)iter.next();
							// add to set only if cClass is consistent
							if ( myReasoner.isConsistent(aClass) )
								disjointsForClass.add( aClass );
						}
					}
					// now refine disjointsForClass so that every class in disjointsForClass
					// is not subsumed by another member in set.
					 findRootDisjoints( disjointsForClass );
					// now go over the disjointsForClass set and add disjoint axioms
					 for ( Iterator it = disjointsForClass.iterator(); it.hasNext(); )
					 {
						 OWLClass aClass = (OWLClass)it.next();
						 HashSet mutuallyDisjointClasses = new HashSet();
						 mutuallyDisjointClasses.add( c );
						 mutuallyDisjointClasses.add( aClass );
						 classAxiomContainers.add( new ClassAxiomContainer( 
					              new OWLDisjointClassesAxiomImpl( factory, mutuallyDisjointClasses ),
								  myShortform, myModel )); 
					 }
					 
				}
				
				// build equivalence axioms from equivalence maps
				for ( Iterator it = equivalenceMap.keySet().iterator(); it.hasNext(); )
				{
					OWLDescription desc = (OWLDescription)it.next();
					Set equivalentClasses = (Set)equivalenceMap.get( desc );
					classAxiomContainers.add( new ClassAxiomContainer( 
				              new OWLEquivalentClassesAxiomImpl( factory, equivalentClasses ),
							  myShortform, myModel ));
				}
			}
			
			return new Vector( classAxiomContainers );
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
		return null;
	}
	
	
	/* 
	 * PRIVATE helper methods
	 */
	
	/* Given a set of equivalent classes/expressions, merge them into known ones if known ones
	 * exist.  If not, create new places for them in the equivalenceMap
	 * 
	 */
	private void addToEquivalents( Hashtable equivalenceMap, HashSet equivalentClasses ) throws OWLException
	{
		// equiClone only used for iterating through the equivalentClasses set
		HashSet equiClone = (HashSet)equivalentClasses.clone();
		
		for ( Iterator eqIt = equiClone.iterator(); eqIt.hasNext(); )
		{
			OWLDescription desc = (OWLDescription)eqIt.next();
			
//			ConcisePlainVisitor walker = new ConcisePlainVisitor( myShortform, myModel );
			// check to see if desc already in map as key
			if ( equivalenceMap.keySet().contains( desc ) )
			{
//				desc.accept( walker );
//				String descStr = walker.result();
//				walker.reset();
//				System.err.println("adding additional equivalentClasses of [" + descStr +"]" );
				
				Set oldEquiSet = (Set)equivalenceMap.get( desc );
				
				/*
				for ( Iterator it = oldEquiSet.iterator(); it.hasNext(); )
				{
					OWLDescription equiDesc = (OWLDescription)it.next();
					
					equiDesc.accept( walker );
					String someDesc = walker.result();
					walker.reset();					
					System.err.println("   [o] equivalent to [" + someDesc +"]" );
				}
				for ( Iterator it = equivalentClasses.iterator(); it.hasNext(); )
				{
					OWLDescription equiDesc = (OWLDescription)it.next();
					
					equiDesc.accept( walker );
					String someDesc = walker.result();
					walker.reset();					
					System.err.println("   [n] equivalent to [" + someDesc +"]" );
				}
				*/
				
				addKnownEquivalences( equivalenceMap, oldEquiSet, equivalentClasses );
				
				// for the updated oldEquiSet, update to its members
				for ( Iterator it = oldEquiSet.iterator(); it.hasNext(); )
				{
					Object obj = it.next();
					equivalenceMap.put( obj, oldEquiSet );
				}
				//equivalentClasses.addAll( oldEquiSet );
				//equivalenceMap.put( desc, oldEquiSet );
			}
			else 
			{
				// not in map, construct a new set, find 
				// known euivalent classes of equivalent classes, 
				// and put those in the new set.
				Set newEquivalences = new HashSet();
				newEquivalences.addAll( equivalentClasses );
				
//				desc.accept( walker );
//				String descStr = walker.result();
//				walker.reset();
//				System.err.println("adding new equivalentClasses of [" + descStr +"]" );
				
				addKnownEquivalences( equivalenceMap, newEquivalences, equivalentClasses );

//				System.err.println(" * putting in map with set of size " + newEquivalences.size() );
				// for the updated newEquivalences, update to its members
				for ( Iterator it = newEquivalences.iterator(); it.hasNext(); )
				{
					Object obj = it.next();
					equivalenceMap.put( obj, newEquivalences );
				}
				equivalenceMap.put( desc, newEquivalences );
			}
		}
	}

	/* 
	 * Given the equivalenceMap, a set of known equivalences, and a set of new equivalences.
	 *  for each member X of new equivalence, attempts to find to see if it already is registered
	 *  in the map.  If it is, then find the existing equivalent set for that member from the
	 *  map (calling 'get').  Add the members of the existing equivalences to known equivalences,
	 *  and update the Map for X.
	 * 
	 */
	private void addKnownEquivalences( Hashtable equivalenceMap, Set knownEquivalences, Set equivalentClasses ) throws OWLException
	{
//		ConcisePlainVisitor walker = new ConcisePlainVisitor( myShortform, myModel );
//		System.err.println("  adding known euqivalences");
		
		// for each equivalent class, find if it exists already in equivalenceMap.
		// if it does, then we get its known equivalent classes and add to newEquivalences
		// set its equivalent set to this newEquivalences set
		for ( Iterator it = equivalentClasses.iterator(); it.hasNext(); )
		{
			OWLDescription equiDesc = (OWLDescription)it.next();
			
// 			equiDesc.accept( walker );
//			String equiStr = walker.result();
//			walker.reset();
			
//			System.err.println("   equivalent to: [" + equiStr +"]" );
			if ( equivalenceMap.keySet().contains( equiDesc ) )
			{
				Set knownEquiSet = (Set)equivalenceMap.get( equiDesc );
//			System.err.println("   ["+ equiStr +"] exists in map, with old size = " + knownEquiSet.size() );
				knownEquivalences.addAll( knownEquiSet );
				equivalenceMap.put( equiDesc, knownEquivalences );
//			System.err.println("   ["+ equiStr +"] exists in map, with new size = " + newEquivalences.size() );
			}
		}
	}

	
	/* 
	 * refine input disjointsForClass so that every class in disjointsForClass
	 * is not subsumed by another member in set.
	 */ 
	private void findRootDisjoints( HashSet disjointsForClass ) throws OWLException
	{
		HashSet subclassSet = new HashSet();
		for ( Iterator it = disjointsForClass.iterator(); it.hasNext(); )
		{
			OWLClass iClass = (OWLClass)it.next();
			if ( subclassSet.contains( iClass ) ) // if is already known to be a subclass, then don't process it
				continue;
			for ( Iterator jt = disjointsForClass.iterator(); jt.hasNext(); )
			{
				OWLClass jClass = (OWLClass)jt.next();
				if ( !myReasoner.isEquivalentClass( jClass, iClass) )
				{
					if ( myReasoner.isSubClassOf(jClass, iClass) )
					{
						//System.out.println( " -" + myShortform.shortForm(jClass.getURI())+ " subsumed by " + myShortform.shortForm(iClass.getURI()));
						subclassSet.add( jClass );
					}
				}
			}
		}
		disjointsForClass.removeAll( subclassSet );
	}
}
