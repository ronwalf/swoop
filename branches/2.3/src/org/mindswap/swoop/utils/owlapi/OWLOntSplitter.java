/*
 * Created on Mar 9, 2006
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.mindswap.swoop.utils.owlapi;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.mindswap.swoop.SwoopModel;
import org.semanticweb.owl.model.OWLAnd;
import org.semanticweb.owl.model.OWLAnnotationInstance;
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
import org.semanticweb.owl.model.OWLObjectVisitor;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLOr;
import org.semanticweb.owl.model.OWLPropertyDomainAxiom;
import org.semanticweb.owl.model.OWLSameIndividualsAxiom;
import org.semanticweb.owl.model.OWLSubClassAxiom;
import org.semanticweb.owl.model.OWLSubPropertyAxiom;
import org.semanticweb.owl.model.OWLSymmetricPropertyAxiom;
import org.semanticweb.owl.model.OWLTransitivePropertyAxiom;
import org.semanticweb.owl.model.change.AddClassAxiom;
import org.semanticweb.owl.model.change.AddSuperClass;
import org.semanticweb.owl.model.change.ChangeVisitor;
import org.semanticweb.owl.model.change.RemoveClassAxiom;
import org.semanticweb.owl.model.change.RemoveEquivalentClass;
import org.semanticweb.owl.model.change.RemoveSuperClass;


public class OWLOntSplitter implements OWLObjectVisitor {

	OWLOntology ontology;
	SwoopModel swoopModel;
	
	public OWLOntSplitter(SwoopModel model) {
		this.swoopModel = model;
	}
	
	public OWLOntology splitAxioms(OWLOntology ont) {
		try {
			ontology = swoopModel.cloneOntology(ont);
			// visit classes
			for (Iterator iter = ontology.getClasses().iterator(); iter.hasNext();) {
				OWLClass cla = (OWLClass) iter.next();
				cla.accept(this);
			}
			// visit class axioms
			for (Iterator iter = new HashSet(ontology.getClassAxioms()).iterator(); iter.hasNext();) {
				OWLObject axiom = (OWLObject) iter.next();
				axiom.accept(this);
			}
		}
		catch (OWLException ex) {
			ex.printStackTrace();
		}
		return ontology;
	}
	
	/* (non-Javadoc)
	 * @see org.semanticweb.owl.model.OWLObjectVisitor#visit(org.semanticweb.owl.model.OWLAnd)
	 */
	public void visit(OWLAnd node) throws OWLException {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owl.model.OWLObjectVisitor#visit(org.semanticweb.owl.model.OWLAnnotationProperty)
	 */
	public void visit(OWLAnnotationProperty node) throws OWLException {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owl.model.OWLObjectVisitor#visit(org.semanticweb.owl.model.OWLAnnotationInstance)
	 */
	public void visit(OWLAnnotationInstance node) throws OWLException {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owl.model.OWLObjectVisitor#visit(org.semanticweb.owl.model.OWLDataValue)
	 */
	public void visit(OWLDataValue node) throws OWLException {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owl.model.OWLObjectVisitor#visit(org.semanticweb.owl.model.OWLDataType)
	 */
	public void visit(OWLDataType node) throws OWLException {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owl.model.OWLObjectVisitor#visit(org.semanticweb.owl.model.OWLDataEnumeration)
	 */
	public void visit(OWLDataEnumeration node) throws OWLException {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owl.model.OWLObjectVisitor#visit(org.semanticweb.owl.model.OWLDataAllRestriction)
	 */
	public void visit(OWLDataAllRestriction node) throws OWLException {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owl.model.OWLObjectVisitor#visit(org.semanticweb.owl.model.OWLDataCardinalityRestriction)
	 */
	public void visit(OWLDataCardinalityRestriction node) throws OWLException {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owl.model.OWLObjectVisitor#visit(org.semanticweb.owl.model.OWLDataProperty)
	 */
	public void visit(OWLDataProperty node) throws OWLException {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owl.model.OWLObjectVisitor#visit(org.semanticweb.owl.model.OWLDataSomeRestriction)
	 */
	public void visit(OWLDataSomeRestriction node) throws OWLException {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owl.model.OWLObjectVisitor#visit(org.semanticweb.owl.model.OWLDataValueRestriction)
	 */
	public void visit(OWLDataValueRestriction node) throws OWLException {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owl.model.OWLObjectVisitor#visit(org.semanticweb.owl.model.OWLDifferentIndividualsAxiom)
	 */
	public void visit(OWLDifferentIndividualsAxiom node) throws OWLException {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owl.model.OWLObjectVisitor#visit(org.semanticweb.owl.model.OWLDisjointClassesAxiom)
	 */
	public void visit(OWLDisjointClassesAxiom node) throws OWLException {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owl.model.OWLObjectVisitor#visit(org.semanticweb.owl.model.OWLEquivalentClassesAxiom)
	 */
	public void visit(OWLEquivalentClassesAxiom equAxiom) throws OWLException {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owl.model.OWLObjectVisitor#visit(org.semanticweb.owl.model.OWLEquivalentPropertiesAxiom)
	 */
	public void visit(OWLEquivalentPropertiesAxiom node) throws OWLException {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owl.model.OWLObjectVisitor#visit(org.semanticweb.owl.model.OWLFrame)
	 */
	public void visit(OWLFrame node) throws OWLException {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owl.model.OWLObjectVisitor#visit(org.semanticweb.owl.model.OWLIndividual)
	 */
	public void visit(OWLIndividual node) throws OWLException {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owl.model.OWLObjectVisitor#visit(org.semanticweb.owl.model.OWLObjectAllRestriction)
	 */
	public void visit(OWLObjectAllRestriction node) throws OWLException {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owl.model.OWLObjectVisitor#visit(org.semanticweb.owl.model.OWLObjectCardinalityRestriction)
	 */
	public void visit(OWLObjectCardinalityRestriction node) throws OWLException {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owl.model.OWLObjectVisitor#visit(org.semanticweb.owl.model.OWLObjectProperty)
	 */
	public void visit(OWLObjectProperty node) throws OWLException {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owl.model.OWLObjectVisitor#visit(org.semanticweb.owl.model.OWLObjectSomeRestriction)
	 */
	public void visit(OWLObjectSomeRestriction node) throws OWLException {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owl.model.OWLObjectVisitor#visit(org.semanticweb.owl.model.OWLObjectValueRestriction)
	 */
	public void visit(OWLObjectValueRestriction node) throws OWLException {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owl.model.OWLObjectVisitor#visit(org.semanticweb.owl.model.OWLNot)
	 */
	public void visit(OWLNot node) throws OWLException {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owl.model.OWLObjectVisitor#visit(org.semanticweb.owl.model.OWLOntology)
	 */
	public void visit(OWLOntology node) throws OWLException {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owl.model.OWLObjectVisitor#visit(org.semanticweb.owl.model.OWLOr)
	 */
	public void visit(OWLOr node) throws OWLException {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owl.model.OWLObjectVisitor#visit(org.semanticweb.owl.model.OWLClass)
	 */
	public void visit(OWLClass cla) throws OWLException {
		
		Set remove = new HashSet();
		// rewrite A \sub B \and C -> A \sub B, A \sub C
		for (Iterator iter = cla.getSuperClasses(ontology).iterator(); iter.hasNext();) {
			OWLDescription sup = (OWLDescription) iter.next();
			if (sup instanceof OWLAnd) {
				OWLAnd and = (OWLAnd) sup;
				for (Iterator iter2 = and.getOperands().iterator(); iter2.hasNext();) {
					OWLDescription op = (OWLDescription) iter2.next();
					AddSuperClass as = new AddSuperClass(ontology, cla, op, null);
					as.accept((ChangeVisitor) ontology);
				}
				remove.add(sup);
			}
		}
		for (Iterator iter = remove.iterator(); iter.hasNext();) {
			RemoveSuperClass rs = new RemoveSuperClass(ontology, cla, (OWLDescription) iter.next(), null);
			rs.accept((ChangeVisitor) ontology);
		}
		
		// rewrite A \equ B \and C -> A \sub B, A \sub C, B \and C \sub A
		remove.clear();
		for (Iterator iter = cla.getEquivalentClasses(ontology).iterator(); iter.hasNext();) {
			OWLDescription equ = (OWLDescription) iter.next();
			if (equ instanceof OWLAnd) {
				OWLAnd and = (OWLAnd) equ;
				for (Iterator iter2 = and.getOperands().iterator(); iter2.hasNext();) {
					OWLDescription op = (OWLDescription) iter2.next();
					AddSuperClass as = new AddSuperClass(ontology, cla, op, null);
					as.accept((ChangeVisitor) ontology);
				}
				remove.add(equ);
			}
		}
		for (Iterator iter = remove.iterator(); iter.hasNext();) {
			OWLDescription equ = (OWLDescription) iter.next();
			RemoveEquivalentClass re = new RemoveEquivalentClass(ontology, cla, equ, null);
			re.accept((ChangeVisitor) ontology);
			OWLSubClassAxiom newAxiom = ontology.getOWLDataFactory().getOWLSubClassAxiom(equ, cla);
			AddClassAxiom ac = new AddClassAxiom(ontology, newAxiom, null);
			ac.accept((ChangeVisitor) ontology);
		}
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owl.model.OWLObjectVisitor#visit(org.semanticweb.owl.model.OWLEnumeration)
	 */
	public void visit(OWLEnumeration node) throws OWLException {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owl.model.OWLObjectVisitor#visit(org.semanticweb.owl.model.OWLSameIndividualsAxiom)
	 */
	public void visit(OWLSameIndividualsAxiom node) throws OWLException {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owl.model.OWLObjectVisitor#visit(org.semanticweb.owl.model.OWLSubClassAxiom)
	 */
	public void visit(OWLSubClassAxiom axiom) throws OWLException {
		// TODO Auto-generated method stub
		OWLDescription sub = axiom.getSubClass();
		OWLDescription sup = axiom.getSuperClass();
		if (sup instanceof OWLAnd) {
			OWLAnd and = (OWLAnd) sup;
			for (Iterator iter = and.getOperands().iterator(); iter.hasNext();) {
				OWLDescription op = (OWLDescription) iter.next();
				OWLSubClassAxiom newAxiom = ontology.getOWLDataFactory().getOWLSubClassAxiom(sub, op);
				AddClassAxiom ac = new AddClassAxiom(ontology, newAxiom, null);
				ac.accept((ChangeVisitor) ontology);
			}
			RemoveClassAxiom rc = new RemoveClassAxiom(ontology, axiom, null);
			rc.accept((ChangeVisitor) ontology);
		}
		
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owl.model.OWLObjectVisitor#visit(org.semanticweb.owl.model.OWLSubPropertyAxiom)
	 */
	public void visit(OWLSubPropertyAxiom node) throws OWLException {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owl.model.OWLObjectVisitor#visit(org.semanticweb.owl.model.OWLFunctionalPropertyAxiom)
	 */
	public void visit(OWLFunctionalPropertyAxiom node) throws OWLException {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owl.model.OWLObjectVisitor#visit(org.semanticweb.owl.model.OWLInverseFunctionalPropertyAxiom)
	 */
	public void visit(OWLInverseFunctionalPropertyAxiom node) throws OWLException {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owl.model.OWLObjectVisitor#visit(org.semanticweb.owl.model.OWLTransitivePropertyAxiom)
	 */
	public void visit(OWLTransitivePropertyAxiom node) throws OWLException {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owl.model.OWLObjectVisitor#visit(org.semanticweb.owl.model.OWLSymmetricPropertyAxiom)
	 */
	public void visit(OWLSymmetricPropertyAxiom node) throws OWLException {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owl.model.OWLObjectVisitor#visit(org.semanticweb.owl.model.OWLInversePropertyAxiom)
	 */
	public void visit(OWLInversePropertyAxiom node) throws OWLException {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owl.model.OWLObjectVisitor#visit(org.semanticweb.owl.model.OWLPropertyDomainAxiom)
	 */
	public void visit(OWLPropertyDomainAxiom node) throws OWLException {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owl.model.OWLObjectVisitor#visit(org.semanticweb.owl.model.OWLObjectPropertyRangeAxiom)
	 */
	public void visit(OWLObjectPropertyRangeAxiom node) throws OWLException {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owl.model.OWLObjectVisitor#visit(org.semanticweb.owl.model.OWLDataPropertyRangeAxiom)
	 */
	public void visit(OWLDataPropertyRangeAxiom node) throws OWLException {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owl.model.OWLObjectVisitor#visit(org.semanticweb.owl.model.OWLObjectPropertyInstance)
	 */
	public void visit(OWLObjectPropertyInstance node) throws OWLException {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owl.model.OWLObjectVisitor#visit(org.semanticweb.owl.model.OWLDataPropertyInstance)
	 */
	public void visit(OWLDataPropertyInstance node) throws OWLException {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owl.model.OWLObjectVisitor#visit(org.semanticweb.owl.model.OWLIndividualTypeAssertion)
	 */
	public void visit(OWLIndividualTypeAssertion node) throws OWLException {
		// TODO Auto-generated method stub
		
	}

}
