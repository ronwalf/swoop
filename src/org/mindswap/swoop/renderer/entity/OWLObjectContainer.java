/*
 * Created on May 27, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.mindswap.swoop.renderer.entity;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.mindswap.swoop.renderer.SwoopRenderingVisitor;
import org.semanticweb.owl.impl.model.OWLInversePropertyAxiomImpl;
import org.semanticweb.owl.model.OWLAnd;
import org.semanticweb.owl.model.OWLAnnotationProperty;
import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLDataAllRestriction;
import org.semanticweb.owl.model.OWLDataCardinalityRestriction;
import org.semanticweb.owl.model.OWLDataEnumeration;
import org.semanticweb.owl.model.OWLDataProperty;
import org.semanticweb.owl.model.OWLDataPropertyInstance;
import org.semanticweb.owl.model.OWLDataPropertyRangeAxiom;
import org.semanticweb.owl.model.OWLDataSomeRestriction;
import org.semanticweb.owl.model.OWLDataType;
import org.semanticweb.owl.model.OWLDataValue;
import org.semanticweb.owl.model.OWLDataValueRestriction;
import org.semanticweb.owl.model.OWLDescription;
import org.semanticweb.owl.model.OWLDifferentIndividualsAxiom;
import org.semanticweb.owl.model.OWLDisjointClassesAxiom;
import org.semanticweb.owl.model.OWLEnumeration;
import org.semanticweb.owl.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owl.model.OWLEquivalentPropertiesAxiom;
import org.semanticweb.owl.model.OWLException;
import org.semanticweb.owl.model.OWLFunctionalPropertyAxiom;
import org.semanticweb.owl.model.OWLIndividual;
import org.semanticweb.owl.model.OWLIndividualTypeAssertion;
import org.semanticweb.owl.model.OWLInverseFunctionalPropertyAxiom;
import org.semanticweb.owl.model.OWLInversePropertyAxiom;
import org.semanticweb.owl.model.OWLNamedObject;
import org.semanticweb.owl.model.OWLNot;
import org.semanticweb.owl.model.OWLObject;
import org.semanticweb.owl.model.OWLObjectAllRestriction;
import org.semanticweb.owl.model.OWLObjectCardinalityRestriction;
import org.semanticweb.owl.model.OWLObjectProperty;
import org.semanticweb.owl.model.OWLObjectPropertyInstance;
import org.semanticweb.owl.model.OWLObjectPropertyRangeAxiom;
import org.semanticweb.owl.model.OWLObjectSomeRestriction;
import org.semanticweb.owl.model.OWLObjectValueRestriction;
import org.semanticweb.owl.model.OWLOr;
import org.semanticweb.owl.model.OWLProperty;
import org.semanticweb.owl.model.OWLPropertyDomainAxiom;
import org.semanticweb.owl.model.OWLSameIndividualsAxiom;
import org.semanticweb.owl.model.OWLSubClassAxiom;
import org.semanticweb.owl.model.OWLSubPropertyAxiom;
import org.semanticweb.owl.model.OWLSymmetricPropertyAxiom;
import org.semanticweb.owl.model.OWLTransitivePropertyAxiom;
import org.semanticweb.owl.model.helper.OWLObjectVisitorAdapter;

/**
 * @author Aditya
 *
 */
public class OWLObjectContainer extends OWLObjectVisitorAdapter implements SwoopRenderingVisitor
//Uncomment for explanation
//,OWLExtendedObjectVisitor
{
 
	List objects;
	List LHS, RHS;
	OWLNamedObject base;
	
	public OWLObjectContainer(OWLNamedObject base)
	{
		this.base= base;
		reset();
	}
	
	public String result() {
		return ""; // use getContainedObjects() instead
	}

	public void reset() {
		this.objects = new ArrayList();
		this.LHS = new ArrayList();
		this.RHS = new ArrayList();
	}
	
	public void visit( OWLObject obj) throws OWLException {
		if (obj instanceof OWLClass) visit((OWLClass) obj);
		else
			if (obj instanceof OWLDataProperty) visit((OWLDataProperty) obj);
		else
			if (obj instanceof OWLObjectProperty) visit((OWLObjectProperty) obj);
		else
			if (obj instanceof OWLAnnotationProperty) visit((OWLAnnotationProperty) obj);
		else
			if (obj instanceof OWLIndividual) visit((OWLIndividual) obj);
		else
			if (obj instanceof OWLAnd) visit((OWLAnd) obj);
		else
			if (obj instanceof OWLOr) visit((OWLOr) obj);
		else
			if (obj instanceof OWLNot) visit((OWLNot) obj);
		else
			if (obj instanceof OWLDataAllRestriction) visit((OWLDataAllRestriction) obj);
		else
			if (obj instanceof OWLDataSomeRestriction) visit((OWLDataSomeRestriction) obj);
		else 
			if (obj instanceof OWLDataValueRestriction) visit((OWLDataValueRestriction) obj);
		else
			if (obj instanceof OWLObjectAllRestriction) visit((OWLObjectAllRestriction) obj);
		else
			if (obj instanceof OWLObjectSomeRestriction) visit((OWLObjectSomeRestriction) obj);
		else 
			if (obj instanceof OWLObjectValueRestriction) visit((OWLObjectValueRestriction) obj);
		else
			if (obj instanceof OWLDataCardinalityRestriction) visit((OWLDataCardinalityRestriction) obj);
		else 
			if (obj instanceof OWLObjectCardinalityRestriction) visit((OWLObjectCardinalityRestriction) obj);
		else 
			if (obj instanceof OWLDataEnumeration) visit((OWLDataEnumeration) obj);
		else
			if (obj instanceof OWLDataPropertyInstance) visit((OWLDataPropertyInstance) obj);
		else
			if (obj instanceof OWLDataPropertyRangeAxiom) visit((OWLDataPropertyRangeAxiom) obj);
		else
			if (obj instanceof OWLDataType) visit((OWLDataType) obj);
		else
			if (obj instanceof OWLDifferentIndividualsAxiom) visit((OWLDifferentIndividualsAxiom) obj);
		else
			if (obj instanceof OWLDisjointClassesAxiom) visit((OWLDisjointClassesAxiom) obj);
		else
			if (obj instanceof OWLEnumeration) visit((OWLEnumeration) obj);
		else
			if (obj instanceof OWLEquivalentClassesAxiom) visit((OWLEquivalentClassesAxiom) obj);
		else
			if (obj instanceof OWLEquivalentPropertiesAxiom) visit((OWLEquivalentPropertiesAxiom) obj);
		else
			if (obj instanceof OWLFunctionalPropertyAxiom) visit((OWLFunctionalPropertyAxiom) obj);
		else
			if (obj instanceof OWLIndividualTypeAssertion) visit((OWLIndividualTypeAssertion) obj);		
		else
			if (obj instanceof OWLInversePropertyAxiom) visit((OWLInversePropertyAxiom) obj);
		else
			if (obj instanceof OWLInversePropertyAxiomImpl) visit((OWLInversePropertyAxiomImpl) obj);
		else
			if (obj instanceof OWLInverseFunctionalPropertyAxiom) visit((OWLInverseFunctionalPropertyAxiom) obj);
		else
			if (obj instanceof OWLEquivalentClassesAxiom) visit((OWLEquivalentClassesAxiom) obj);
		else
			if (obj instanceof OWLObjectPropertyInstance) visit((OWLObjectPropertyInstance) obj);
		else
			if (obj instanceof OWLObjectPropertyRangeAxiom) visit((OWLObjectPropertyRangeAxiom) obj);
		else
			if (obj instanceof OWLPropertyDomainAxiom) visit((OWLPropertyDomainAxiom) obj);
		else
			if (obj instanceof OWLSameIndividualsAxiom) visit((OWLSameIndividualsAxiom) obj);
		else
			if (obj instanceof OWLSubClassAxiom) visit((OWLSubClassAxiom) obj);
		else
			if (obj instanceof OWLSubPropertyAxiom) visit((OWLSubPropertyAxiom) obj);
		else
			if (obj instanceof OWLSymmetricPropertyAxiom) visit((OWLSymmetricPropertyAxiom) obj);
		else
			if (obj instanceof OWLTransitivePropertyAxiom) visit((OWLTransitivePropertyAxiom) obj);
	}
	
	public void visit( OWLClass clazz ) throws OWLException {
		objects.add(clazz);	
	}
	
	public void visit( OWLIndividual ind ) throws OWLException {
		objects.add(ind);
	}
	
	
	public void visit( OWLObjectProperty prop ) throws OWLException {
		objects.add(prop);
	}
	
	public void visit( OWLAnnotationProperty prop ) throws OWLException {
		objects.add(prop);
	}
	
	public void visit( OWLDataProperty prop ) throws OWLException {
		objects.add(prop);
	}
	
	public void visit( OWLDataValue cd ) throws OWLException {		
	}

	public void visit( OWLAnd and ) throws OWLException {
		for ( Iterator it = and.getOperands().iterator();
		it.hasNext(); ) {
			OWLDescription desc = (OWLDescription) it.next();
			desc.accept( this );		
		}		
	}

	public void visit( OWLOr or ) throws OWLException {
		for ( Iterator it = or.getOperands().iterator();
		it.hasNext(); ) {
			OWLDescription desc = (OWLDescription) it.next();
			desc.accept( this );			
		}		
	}

	public void visit( OWLNot not ) throws OWLException {
		OWLDescription desc = not.getOperand();
		desc.accept( this );		
	}

	public void visit( OWLEnumeration enumeration ) throws OWLException {
		for ( Iterator it = enumeration.getIndividuals().iterator();
		it.hasNext(); ) {
			OWLIndividual desc = (OWLIndividual) it.next();
			desc.accept( this );		
		}
	}

	public void visit( OWLObjectSomeRestriction restriction ) throws OWLException {
		restriction.getObjectProperty().accept( this );
		restriction.getDescription().accept( this );		
	}

	public void visit( OWLObjectAllRestriction restriction ) throws OWLException {
		restriction.getObjectProperty().accept( this );
		restriction.getDescription().accept( this );		
	}

	public void visit( OWLObjectValueRestriction restriction ) throws OWLException {
		restriction.getObjectProperty().accept( this );
		restriction.getIndividual().accept( this );		
	}

	public void visit( OWLDataSomeRestriction restriction ) throws OWLException {
		restriction.getDataProperty().accept( this );
		restriction.getDataType().accept( this );		
	}

	public void visit( OWLDataAllRestriction restriction ) throws OWLException {
		restriction.getDataProperty().accept( this );
		restriction.getDataType().accept( this );
	}

	public void visit( OWLObjectCardinalityRestriction restriction ) throws OWLException {
		restriction.getObjectProperty().accept( this );	
	}

	public void visit( OWLDataCardinalityRestriction restriction ) throws OWLException {
		restriction.getDataProperty().accept( this );		
	}

	public void visit( OWLDataValueRestriction restriction ) throws OWLException {
		restriction.getDataProperty().accept( this );
		restriction.getValue().accept( this );		
	}

	public void visit( OWLEquivalentClassesAxiom axiom ) throws OWLException {

		Set equClas = axiom.getEquivalentClasses();
		for ( Iterator it = equClas.iterator();
		it.hasNext(); ) {
			OWLDescription desc = (OWLDescription) it.next();
			desc.accept( this );
			if ((desc instanceof OWLClass) && (((OWLClass) desc).getURI().equals(base.getURI())))
				LHS.addAll(objects);
			else 
				if (desc instanceof OWLClass && LHS.size()==0) LHS.addAll(objects);
			else 
				RHS.addAll(objects);
			objects.clear();
		}		
	}

	public void visit( OWLDisjointClassesAxiom axiom ) throws OWLException {
		for ( Iterator it = axiom.getDisjointClasses().iterator();
		it.hasNext(); ) {
			OWLDescription desc = (OWLDescription) it.next();
			desc.accept( this );
			if ((desc instanceof OWLClass) && (((OWLClass) desc).getURI().equals(base.getURI())))
				LHS.addAll(objects);
			else 
				if (desc instanceof OWLClass && LHS.size()==0) LHS.addAll(objects);
			else 
				RHS.addAll(objects);
			objects.clear();
		}
	}

	public void visit( OWLSubClassAxiom axiom ) throws OWLException {
		axiom.getSubClass().accept( this );
		LHS.addAll(objects);
		objects.clear();
		axiom.getSuperClass().accept( this );
		RHS.addAll(objects);
	}

	public void visit( OWLEquivalentPropertiesAxiom axiom ) throws OWLException {
		for ( Iterator it = axiom.getProperties().iterator();
		it.hasNext(); ) {
			OWLProperty prop = (OWLProperty) it.next();
			prop.accept( this );
		}
	}

	public void visit( OWLSubPropertyAxiom axiom ) throws OWLException {
		axiom.getSubProperty().accept( this );
		LHS.addAll(objects);
		objects.clear();
		axiom.getSuperProperty().accept( this );
		RHS.addAll(objects);
	}

	public void visit( OWLDifferentIndividualsAxiom ax) throws OWLException {
		for ( Iterator it = ax.getIndividuals().iterator();
		it.hasNext(); ) {
			OWLIndividual desc = (OWLIndividual) it.next();
			desc.accept( this );		
		}
	}

	public void visit( OWLSameIndividualsAxiom ax) throws OWLException {
		for ( Iterator it = ax.getIndividuals().iterator();
		it.hasNext(); ) {
			OWLIndividual desc = (OWLIndividual) it.next();
			desc.accept( this );		
		}
	}

	public void visit( OWLDataType ocdt ) throws OWLException {		
	}

	public void visit( OWLDataEnumeration enumeration ) throws OWLException {
		for ( Iterator it = enumeration.getValues().iterator();
		it.hasNext(); ) {
			OWLDataValue desc = (OWLDataValue) it.next();
			desc.accept( this );		
		}
	}
	

//	Uncomment for explanation
	
	public void visit( OWLFunctionalPropertyAxiom axiom ) throws OWLException {
		axiom.getProperty().accept( this );
		LHS.addAll(objects);
	}
	
	public void visit( OWLPropertyDomainAxiom axiom ) throws OWLException {
		axiom.getProperty().accept( this );
		LHS.addAll(objects);
		objects.clear();
		axiom.getDomain().accept( this );
		RHS.addAll(objects);
	}
	
	public void visit( OWLObjectPropertyRangeAxiom axiom ) throws OWLException {
		axiom.getProperty().accept( this );
		LHS.addAll(objects);
		objects.clear();
		axiom.getRange().accept( this );
		RHS.addAll(objects);
	}
	
	public void visit( OWLDataPropertyRangeAxiom axiom ) throws OWLException {
		axiom.getProperty().accept( this );
		LHS.addAll(objects);
		objects.clear();
		axiom.getRange().accept( this );
		RHS.addAll(objects);		
	}
	
 /* (non-Javadoc)
  * @see org.mindswap.pellet.owlapi.OWLExtendedObjectVisitor#visit(org.mindswap.pellet.owlapi.OWLInverseFunctionalPropertyAxiom)
  */
 public void visit(OWLInverseFunctionalPropertyAxiom axiom) throws OWLException {
		axiom.getProperty().accept( this );
		LHS.addAll(objects);
 }

 /* (non-Javadoc)
  * @see org.mindswap.pellet.owlapi.OWLExtendedObjectVisitor#visit(org.mindswap.pellet.owlapi.OWLTransitivePropertyAxiom)
  */
 public void visit(OWLTransitivePropertyAxiom axiom) throws OWLException {
 	axiom.getProperty().accept( this );
 	LHS.addAll(objects);
 }

 /* (non-Javadoc)
  * @see org.mindswap.pellet.owlapi.OWLExtendedObjectVisitor#visit(org.mindswap.pellet.owlapi.OWLSymmetricPropertyAxiom)
  */
 public void visit(OWLSymmetricPropertyAxiom axiom) throws OWLException {
		axiom.getProperty().accept( this );
		LHS.addAll(objects);
 }
 
 /* (non-Javadoc)
  * @see org.mindswap.pellet.owlapi.OWLExtendedObjectVisitor#visit(org.mindswap.pellet.owlapi.OWLInversePropertyAxiom)
  */
 public void visit(OWLInversePropertyAxiomImpl axiom) throws OWLException {
		axiom.getProperty().accept( this );
		LHS.addAll(objects);
		objects.clear();
		axiom.getInverseProperty().accept( this );
		RHS.addAll(objects);
 }

 /* (non-Javadoc)
  * @see org.mindswap.pellet.owlapi.OWLExtendedObjectVisitor#visit(org.mindswap.pellet.owlapi.OWLObjectPropertyInstance)
  */
 public void visit(OWLObjectPropertyInstance axiom) throws OWLException {
		axiom.getSubject().accept( this );
		axiom.getProperty().accept( this );
		axiom.getObject().accept( this );		
 }

 /* (non-Javadoc)
  * @see org.mindswap.pellet.owlapi.OWLExtendedObjectVisitor#visit(org.mindswap.pellet.owlapi.OWLDataPropertyInstance)
  */
 public void visit(OWLDataPropertyInstance axiom) throws OWLException {
		axiom.getSubject().accept( this );
		axiom.getProperty().accept( this );
		axiom.getObject().accept( this );		
 }

 /* (non-Javadoc)
  * @see org.mindswap.pellet.owlapi.OWLExtendedObjectVisitor#visit(org.mindswap.pellet.owlapi.OWLIndividualClassAxiom)
  */
 public void visit(OWLIndividualTypeAssertion axiom) throws OWLException {
		axiom.getIndividual().accept( this );
		axiom.getType().accept( this );		
 }
 
 public List getContainedObjects() {
 	return this.objects;
 }

 public List getRHS() {
 	return this.RHS;
 }
 
 public List getLHS() {
 	return this.LHS;
 }

/* (non-Javadoc)
 * @see org.semanticweb.owl.model.OWLObjectVisitor#visit(org.semanticweb.owl.model.OWLInversePropertyAxiom)
 */
 public void visit(OWLInversePropertyAxiom arg0) throws OWLException {
	// TODO Auto-generated method stub
 	visit((OWLInversePropertyAxiomImpl) arg0);
 }
}
