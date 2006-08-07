/*
 * Created on Aug 30, 2005
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.mindswap.swoop.utils.graph.hierarchy.popup;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

import org.mindswap.swoop.SwoopModel;
import org.semanticweb.owl.model.OWLAnd;
import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLDataProperty;
import org.semanticweb.owl.model.OWLDescription;
import org.semanticweb.owl.model.OWLEnumeration;
import org.semanticweb.owl.model.OWLException;
import org.semanticweb.owl.model.OWLIndividual;
import org.semanticweb.owl.model.OWLNot;
import org.semanticweb.owl.model.OWLObjectProperty;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLOr;
import org.semanticweb.owl.model.OWLProperty;
import org.semanticweb.owl.model.OWLRestriction;
import org.semanticweb.owl.model.OWLSubClassAxiom;

/**
 * @author Dave
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class OntologyFinder 
{
	
	/* 
	 * Given an axiom, we find which ontology this particular axiom belongs to.
	 * 
	 * pseudocode:
	 *   if desc is atomic, then find where desc is 
	 *   if an enumeration, find where any individual belongs  to (they should all belong to the same one)
	 *   if is a {union, intersection, not}, then do recursion on a subpart 
	 *   if a restriction, find where relation is from
	 */
	public static OWLOntology findContainingOntologyBySubclassAxiom(SwoopModel model, OWLSubClassAxiom axe)
	{
		try
		{
			OWLDescription subDesc = axe.getSubClass();
			OWLDescription supDesc = axe.getSubClass();
			boolean isSubAtomic = ( subDesc instanceof OWLClass);
			boolean isSupAtomic = ( supDesc instanceof OWLClass);
						
			if ( isSubAtomic )
				return findOntologyByNamedClass( model, (OWLClass)subDesc );
			else if ( isSupAtomic )
				return findOntologyByNamedClass( model, (OWLClass)supDesc );
			else
				return findOntologyByDescription( model, subDesc );

		}
		catch ( Exception e )
		{ e.printStackTrace(); }
		return null;
	}
	

	
	protected static OWLOntology findOntologyByDescription( SwoopModel model, OWLDescription desc )
	{
		try
		{
			if ( desc instanceof OWLClass)
				return findOntologyByNamedClass( model, (OWLClass)desc );
			else if ( desc instanceof OWLAnd )
				return findOntologyByDescription( model, (OWLDescription)((OWLAnd)desc).getOperands().iterator().next() );
			else if ( desc instanceof OWLOr )
				return findOntologyByDescription( model, (OWLDescription)((OWLOr)desc).getOperands().iterator().next() );
			else if ( desc instanceof OWLNot )
				return findOntologyByDescription( model, (OWLDescription)((OWLNot)desc).getOperand() );
			else if ( desc instanceof OWLEnumeration )
				return findOntologyByEnumeration( model, (OWLEnumeration)desc );
			else if ( desc instanceof OWLRestriction )
				return findOntologyByProperty( model, ((OWLRestriction)desc).getProperty() );
		}
		catch (OWLException e)
		{ e.printStackTrace(); }
		return null;
	}
		
	protected static OWLOntology findOntologyByNamedClass( SwoopModel model, OWLClass concept )
	{
		try
		{
			Collection ontologies = model.getOntologies(); 
			for ( Iterator it = ontologies.iterator(); it.hasNext(); )
			{
				OWLOntology ont = (OWLOntology)it.next();
				OWLClass c = ont.getClass( concept.getURI() );
				if ( c != null) // this means c is found in ont
					return ont;
			}
		}
		catch ( OWLException e)
		{ e.printStackTrace(); }
		return null;
	}
	
	protected static OWLOntology findOntologyByEnumeration( SwoopModel model, OWLEnumeration enum_ )
	{
		try
		{
			Set individuals = enum_.getIndividuals();
			if ( individuals.isEmpty() )
				System.err.println("[OntologyFinder.findOntologyByEnumeration] Enumeration is empty! ");
			OWLIndividual ind = (OWLIndividual)individuals.iterator().next();
			return findOntologyByIndividual( model, ind);
		}
		catch ( OWLException e )
		{ e.printStackTrace(); }
		return null;
	}
	
	protected static OWLOntology findOntologyByProperty( SwoopModel model, OWLProperty prop )
	{
		if ( prop instanceof OWLDataProperty )
			return findOntologyByDataProperty( model, (OWLDataProperty)prop );
		else if (prop instanceof OWLObjectProperty )
			return findOntologyByObjectProperty( model, (OWLObjectProperty)prop );

		return null;
	}
	
	protected static OWLOntology findOntologyByIndividual( SwoopModel model, OWLIndividual individual )
	{
		try
		{
			Collection ontologies = model.getOntologies(); 
			for ( Iterator it = ontologies.iterator(); it.hasNext(); )
			{
				OWLOntology ont = (OWLOntology)it.next();
				OWLIndividual ind = ont.getIndividual( individual.getURI() );
				if ( ind != null) // this means ind is found in ont
					return ont;
			}
		}
		catch ( OWLException e)
		{ e.printStackTrace(); }
		return null;
	}
	
	protected static OWLOntology findOntologyByDataProperty( SwoopModel model, OWLDataProperty property )
	{
		try
		{
			Collection ontologies = model.getOntologies();
			for ( Iterator it = ontologies.iterator(); it.hasNext(); )
			{
				OWLOntology ont = (OWLOntology)it.next();
				OWLDataProperty prop = ont.getDataProperty( property.getURI() );
				if ( prop != null) // this means ind is found in ont
					return ont;
			}
		}
		catch ( OWLException e )
		{ e.printStackTrace(); }
		return null;
	}
	
	protected static OWLOntology findOntologyByObjectProperty( SwoopModel model, OWLObjectProperty property )
	{
		try
		{
			Collection ontologies = model.getOntologies();
			for ( Iterator it = ontologies.iterator(); it.hasNext(); )
			{
				OWLOntology ont = (OWLOntology)it.next();
				OWLDataProperty prop = ont.getDataProperty( property.getURI() );
				if ( prop != null) // this means ind is found in ont
					return ont;
			}
		}
		catch ( OWLException e )
		{ e.printStackTrace(); }
		return null;
	}
	
	/*
	public static Set findRelatedOntologes(SwoopModel model, OWLObject obj)
	{
		Collection ontologies = model.getOntologies();		
		return findOntologiesFromAxiom( (OWLObject)obj, model, ontologies );
	}
	
	private static Set findOntologiesFromAxiom( OWLObject axe, SwoopModel model, Collection ontologies)
	{
		HashSet ontologiesToHighLight = new HashSet();
		ConciseFormatEntityRenderer renderer = new ConciseFormatEntityRenderer();
		OWLEntityCollectorVisitor visitor = new OWLEntityCollectorVisitor( renderer, model);
		try
		{
			axe.accept( visitor );
			OWLEntityStorage storage = visitor.getStoredResult();
			
			Set keys = storage.keySet();
			for ( Iterator it = keys.iterator(); it.hasNext(); )
			{
				OWLEntity entity = (OWLEntity)it.next();
				
				if ( entity instanceof OWLClass )
				{						
					OWLClass concept = (OWLClass)entity;
					for (Iterator iter = ontologies.iterator(); iter.hasNext(); )
					{
						OWLOntology ont = (OWLOntology)iter.next();
						if ( ont.getClass( concept.getURI() ) != null )
							ontologiesToHighLight.add( ont );
					}
				}
				// more ifs here!
				
			}
			return ontologiesToHighLight;
		}
		catch (OWLException ex)
		{ ex.printStackTrace(); }
		
		return null;
	}	
	*/
	
}
