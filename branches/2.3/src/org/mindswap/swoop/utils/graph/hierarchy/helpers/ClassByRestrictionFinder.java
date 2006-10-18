/*
 * Created on Sep 19, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.mindswap.swoop.utils.graph.hierarchy.helpers;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.mindswap.swoop.SwoopModel;
import org.mindswap.swoop.reasoner.SwoopReasoner;
import org.mindswap.swoop.renderer.SwoopEntityRenderer;
import org.semanticweb.owl.model.OWLAnd;
import org.semanticweb.owl.model.OWLAnnotationInstance;
import org.semanticweb.owl.model.OWLAnnotationProperty;
import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLDataAllRestriction;
import org.semanticweb.owl.model.OWLDataCardinalityRestriction;
import org.semanticweb.owl.model.OWLDataProperty;
import org.semanticweb.owl.model.OWLDataPropertyInstance;
import org.semanticweb.owl.model.OWLDataPropertyRangeAxiom;
import org.semanticweb.owl.model.OWLDataSomeRestriction;
import org.semanticweb.owl.model.OWLDataValueRestriction;
import org.semanticweb.owl.model.OWLDescription;
import org.semanticweb.owl.model.OWLDifferentIndividualsAxiom;
import org.semanticweb.owl.model.OWLDisjointClassesAxiom;
import org.semanticweb.owl.model.OWLEnumeration;
import org.semanticweb.owl.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owl.model.OWLEquivalentPropertiesAxiom;
import org.semanticweb.owl.model.OWLException;
import org.semanticweb.owl.model.OWLFrame;
import org.semanticweb.owl.model.OWLFunctionalPropertyAxiom;
import org.semanticweb.owl.model.OWLIndividual;
import org.semanticweb.owl.model.OWLIndividualTypeAssertion;
import org.semanticweb.owl.model.OWLInverseFunctionalPropertyAxiom;
import org.semanticweb.owl.model.OWLInversePropertyAxiom;
import org.semanticweb.owl.model.OWLNot;
import org.semanticweb.owl.model.OWLObject;
import org.semanticweb.owl.model.OWLObjectAllRestriction;
import org.semanticweb.owl.model.OWLObjectCardinalityRestriction;
import org.semanticweb.owl.model.OWLObjectProperty;
import org.semanticweb.owl.model.OWLObjectPropertyInstance;
import org.semanticweb.owl.model.OWLObjectPropertyRangeAxiom;
import org.semanticweb.owl.model.OWLObjectSomeRestriction;
import org.semanticweb.owl.model.OWLObjectValueRestriction;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLOr;
import org.semanticweb.owl.model.OWLPropertyDomainAxiom;
import org.semanticweb.owl.model.OWLSameIndividualsAxiom;
import org.semanticweb.owl.model.OWLSubClassAxiom;
import org.semanticweb.owl.model.OWLSubPropertyAxiom;
import org.semanticweb.owl.model.OWLSymmetricPropertyAxiom;
import org.semanticweb.owl.model.OWLTransitivePropertyAxiom;
import org.semanticweb.owl.model.helper.OWLEntityFinder;

/**
 * @author Dave Wang
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class ClassByRestrictionFinder extends OWLEntityFinder
{
	/* Simply trawls through expressions and stops when it can. */

	protected Set myRelatedClasses = null;
	protected SwoopModel myModel   = null;
	
	public ClassByRestrictionFinder( SwoopModel model )
	{
		super(null);
		myModel = model;
		reset();
	}

	/** Reset the collector */
	public void reset() {
		myRelatedClasses = new HashSet();
	}

	/** Returns the entities collected */
	public boolean found() {
		return !myRelatedClasses.isEmpty();
	}

	public Set getResult() {
		return myRelatedClasses;
	}

	// AND
	public void visit(OWLAnd node) throws OWLException 
	{
		for (Iterator it = node.getOperands().iterator(); it.hasNext();) 
		{
			OWLObject oo = (OWLObject) it.next();
			oo.accept(this);
		}
	}
	
	// OR
	public void visit(OWLOr node) throws OWLException 
	{
		for (Iterator it = node.getOperands().iterator(); it.hasNext();) 
		{
			OWLObject oo = (OWLObject) it.next();
			oo.accept(this);
		}
	}
	
	// NOT
	public void visit(OWLNot node) throws OWLException 
	{ node.getOperand().accept(this); }
	
	// DataRestriction
	public void visit(OWLDataAllRestriction node) throws OWLException 
	{ }
	public void visit(OWLDataCardinalityRestriction node) throws OWLException 
	{ }
	public void visit(OWLDataProperty node) throws OWLException 
	{ }
	public void visit(OWLDataSomeRestriction node) throws OWLException 
	{ }
	public void visit(OWLDataValueRestriction node) throws OWLException 
	{ }

	
	public void visit(OWLDifferentIndividualsAxiom node) throws OWLException 
	{}
	public void visit(OWLDisjointClassesAxiom node) throws OWLException 
	{}

	public void visit(OWLEquivalentClassesAxiom node) throws OWLException 
	{		
		for (Iterator it = node.getEquivalentClasses().iterator(); it.hasNext();) 
		{
			OWLObject oo = (OWLObject) it.next();
			oo.accept(this);
		}
	}

	public void visit(OWLEquivalentPropertiesAxiom node) throws OWLException 
	{}

	public void visit(OWLFrame node) throws OWLException 
	{}

	public void visit(OWLIndividual node) throws OWLException 
	{
		/*
		SwoopReasoner reasoner = myModel.getReasoner();
		Set classes = reasoner.allTypesOf( node );
		for ( Iterator it = classes.iterator(); it.hasNext(); )
		{
			Object obj = it.next();
			if ( obj instanceof HashSet)
			{
				HashSet newSet = (HashSet)obj;
				for (Iterator iter = newSet.iterator(); iter.hasNext(); )
				{
					OWLClass classs = (OWLClass)iter.next();
					if ( !classs.getURI().toString().equals( SwoopEntityRenderer.OWL_THING) )
						myRelatedClasses.add( classs );
					//System.out.println("RelatedClasses adds ( in set )" + OWLClassToString( classs ));
				}
			}
			else
			{
				myRelatedClasses.add( obj );
				//System.out.println("RelatedClasses adds " + OWLClassToString((OWLClass)obj));
			}
		}
		*/
	}

	
	// Object Restrictions
	public void visit(OWLObjectAllRestriction node) throws OWLException 
	{
		OWLDescription desc = node.getDescription();
		if ( desc instanceof OWLClass)
		{
			//System.out.println("RelatedClasses adds (all restriction)" + OWLClassToString((OWLClass)desc));
			OWLClass cla = (OWLClass)desc;
			if ( !cla.getURI().toString().equals( SwoopEntityRenderer.OWL_THING ))
				myRelatedClasses.add( desc );
		}
		else
			node.getDescription().accept(this); 
	}
	
	public void visit(OWLObjectSomeRestriction node) throws OWLException 
	{ 
		OWLDescription desc = node.getDescription();
		if ( desc instanceof OWLClass)
		{
			//System.out.println("RelatedClasses adds (some restiction)" + OWLClassToString((OWLClass)desc));
			OWLClass cla = (OWLClass)desc;
			if ( !cla.getURI().toString().equals( SwoopEntityRenderer.OWL_THING ))
				myRelatedClasses.add( desc );		}
		else
			node.getDescription().accept(this);
	}

	public void visit(OWLObjectValueRestriction node) throws OWLException 
	{ node.getIndividual().accept(this); }

	public void visit(OWLObjectCardinalityRestriction node) throws OWLException 
	{ }

	
	public void visit(OWLObjectProperty node) throws OWLException 
	{ }
	public void visit(OWLOntology node) throws OWLException 
	{ }
	public void visit(OWLClass node) throws OWLException 
	{ }

	public void visit(OWLEnumeration node) throws OWLException 
	{
		for (Iterator it = node.getIndividuals().iterator(); it.hasNext();) 
		{
			OWLObject oo = (OWLObject) it.next();
			oo.accept(this);
		}
	}

	public void visit(OWLSameIndividualsAxiom node) throws OWLException 
	{ }

	// ???
	public void visit(OWLSubClassAxiom node) throws OWLException 
	{
		//node.getSubClass().accept(this);
		//node.getSuperClass().accept(this);
	}

	public void visit(OWLSubPropertyAxiom node) throws OWLException 
	{ }

	public void visit(OWLAnnotationProperty node) throws OWLException 
	{ }

	public void visit(OWLAnnotationInstance node) throws OWLException 
	{ }

	/* (non-Javadoc)
	 * @see org.semanticweb.owl.model.OWLObjectVisitor#visit(org.semanticweb.owl.model.OWLFunctionalPropertyAxiom)
	 */
	public void visit(OWLFunctionalPropertyAxiom node) throws OWLException 
	{ }

	/* (non-Javadoc)
	 * @see org.semanticweb.owl.model.OWLObjectVisitor#visit(org.semanticweb.owl.model.OWLInverseFunctionalPropertyAxiom)
	 */
	public void visit(OWLInverseFunctionalPropertyAxiom node) throws OWLException 
	{ }

	/* (non-Javadoc)
	 * @see org.semanticweb.owl.model.OWLObjectVisitor#visit(org.semanticweb.owl.model.OWLTransitivePropertyAxiom)
	 */
	public void visit(OWLTransitivePropertyAxiom node) throws OWLException 
	{ }

	/* (non-Javadoc)
	 * @see org.semanticweb.owl.model.OWLObjectVisitor#visit(org.semanticweb.owl.model.OWLSymmetricPropertyAxiom)
	 */
	public void visit(OWLSymmetricPropertyAxiom node) throws OWLException 
	{ }

	/* (non-Javadoc)
	 * @see org.semanticweb.owl.model.OWLObjectVisitor#visit(org.semanticweb.owl.model.OWLInversePropertyAxiom)
	 */
	public void visit(OWLInversePropertyAxiom node) throws OWLException 
	{ }

	/* (non-Javadoc)
	 * @see org.semanticweb.owl.model.OWLObjectVisitor#visit(org.semanticweb.owl.model.OWLPropertyDomainAxiom)
	 */
	public void visit(OWLPropertyDomainAxiom node) throws OWLException 
	{ }

	/* (non-Javadoc)
	 * @see org.semanticweb.owl.model.OWLObjectVisitor#visit(org.semanticweb.owl.model.OWLObjectPropertyRangeAxiom)
	 */
	public void visit(OWLObjectPropertyRangeAxiom node) throws OWLException 
	{ }

	/* (non-Javadoc)
	 * @see org.semanticweb.owl.model.OWLObjectVisitor#visit(org.semanticweb.owl.model.OWLDataPropertyRangeAxiom)
	 */
	public void visit(OWLDataPropertyRangeAxiom node) throws OWLException 
	{ }

	/* (non-Javadoc)
	 * @see org.semanticweb.owl.model.OWLObjectVisitor#visit(org.semanticweb.owl.model.OWLObjectPropertyInstance)
	 */
	public void visit(OWLObjectPropertyInstance node) throws OWLException 
	{ }

	/* (non-Javadoc)
	 * @see org.semanticweb.owl.model.OWLObjectVisitor#visit(org.semanticweb.owl.model.OWLDataPropertyInstance)
	 */
	public void visit(OWLDataPropertyInstance node) throws OWLException 
	{ }

	/* (non-Javadoc)
	 * @see org.semanticweb.owl.model.OWLObjectVisitor#visit(org.semanticweb.owl.model.OWLIndividualTypeAssertion)
	 */
	public void visit(OWLIndividualTypeAssertion node) throws OWLException 
	{ }
	
	public String OWLClassToString( OWLClass c )
	{
		try
		{
			return myModel.shortForm( c.getURI() );
		}
		catch (Exception e )
		{ e.printStackTrace(); }
		return null;
	}
}
