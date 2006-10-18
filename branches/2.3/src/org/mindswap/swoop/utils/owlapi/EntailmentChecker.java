/*
 * Created on Mar 2, 2006
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.mindswap.swoop.utils.owlapi;

import java.util.Set;

import org.mindswap.swoop.reasoner.SwoopReasoner;
import org.mindswap.swoop.utils.SetUtils;
import org.semanticweb.owl.model.OWLAnd;
import org.semanticweb.owl.model.OWLAnnotationInstance;
import org.semanticweb.owl.model.OWLAnnotationProperty;
import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLClassAxiom;
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
import org.semanticweb.owl.model.OWLIndividualAxiom;
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
import org.semanticweb.owl.model.OWLProperty;
import org.semanticweb.owl.model.OWLPropertyAxiom;
import org.semanticweb.owl.model.OWLPropertyDomainAxiom;
import org.semanticweb.owl.model.OWLSameIndividualsAxiom;
import org.semanticweb.owl.model.OWLSubClassAxiom;
import org.semanticweb.owl.model.OWLSubPropertyAxiom;
import org.semanticweb.owl.model.OWLSymmetricPropertyAxiom;
import org.semanticweb.owl.model.OWLTransitivePropertyAxiom;

/**
 * @author UMD
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class EntailmentChecker implements OWLObjectVisitor {

	SwoopReasoner reasoner;
	OWLOntology ontology;
	boolean result = false;
	
	public EntailmentChecker(SwoopReasoner reasoner, OWLOntology ontology) {
		this.reasoner = reasoner;
		this.ontology = ontology;
	}
	
	public boolean isEntailed(OWLObject entailment) {
		try {
			if (entailment instanceof OWLClassAxiom) ((OWLClassAxiom) entailment).accept(this);
			else if (entailment instanceof OWLPropertyAxiom) ((OWLPropertyAxiom) entailment).accept(this);
			else if (entailment instanceof OWLIndividualAxiom) ((OWLIndividualAxiom) entailment).accept(this);
		} catch (OWLException e) {
			e.printStackTrace();
		}
		return result;
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owl.model.OWLClassAxiomVisitor#visit(org.semanticweb.owl.model.OWLDisjointClassesAxiom)
	 */
	public void visit(OWLDisjointClassesAxiom axiom) throws OWLException {
		Set dis = axiom.getDisjointClasses();
		OWLClass cla1 = (OWLClass) dis.iterator().next();
		dis.remove(cla1);
		OWLClass cla2 = (OWLClass) dis.iterator().next();
		Set newDis = SetUtils.union(reasoner.disjointClassesOf(cla1));			
		result = newDis.contains(cla2);		
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owl.model.OWLClassAxiomVisitor#visit(org.semanticweb.owl.model.OWLEquivalentClassesAxiom)
	 */
	public void visit(OWLEquivalentClassesAxiom axiom) throws OWLException {
		Set equ = axiom.getEquivalentClasses();
		OWLDescription cla1 = (OWLDescription) equ.iterator().next();
		equ.remove(cla1);
		OWLDescription cla2 = (OWLDescription) equ.iterator().next();
		Set newEqu = reasoner.equivalentClassesOf(cla1);			
		result = newEqu.contains(cla2);
//		}
//		else if (cla2 instanceof OWLClass) {
//			Set newEqu = reasoner.equivalentClassesOf(cla2);			
//			result = newEqu.contains(cla1);
//		}
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owl.model.OWLClassAxiomVisitor#visit(org.semanticweb.owl.model.OWLSubClassAxiom)
	 */
	public void visit(OWLSubClassAxiom axiom) throws OWLException {
		OWLDescription sub = axiom.getSubClass();
		Set anc = SetUtils.union(reasoner.ancestorClassesOf(sub));
		result = anc.contains(axiom.getSuperClass());
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owl.model.OWLPropertyAxiomVisitor#visit(org.semanticweb.owl.model.OWLEquivalentPropertiesAxiom)
	 */
	public void visit(OWLEquivalentPropertiesAxiom axiom) throws OWLException {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owl.model.OWLPropertyAxiomVisitor#visit(org.semanticweb.owl.model.OWLSubPropertyAxiom)
	 */
	public void visit(OWLSubPropertyAxiom axiom) throws OWLException {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owl.model.OWLIndividualAxiomVisitor#visit(org.semanticweb.owl.model.OWLDifferentIndividualsAxiom)
	 */
	public void visit(OWLDifferentIndividualsAxiom axiom) throws OWLException {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owl.model.OWLIndividualAxiomVisitor#visit(org.semanticweb.owl.model.OWLSameIndividualsAxiom)
	 */
	public void visit(OWLSameIndividualsAxiom axiom) throws OWLException {
		// TODO Auto-generated method stub
		
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
	public void visit(OWLClass node) throws OWLException {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owl.model.OWLObjectVisitor#visit(org.semanticweb.owl.model.OWLEnumeration)
	 */
	public void visit(OWLEnumeration node) throws OWLException {
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
		OWLProperty prop = node.getProperty();
		OWLDescription dom = node.getDomain();
		result = reasoner.domainsOf(prop).contains(dom);
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
