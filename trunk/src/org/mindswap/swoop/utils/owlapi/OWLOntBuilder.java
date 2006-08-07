//The MIT License
//
// Copyright (c) 2004 Mindswap Research Group, University of Maryland, College Park
//
// Permission is hereby granted, free of charge, to any person obtaining a copy
// of this software and associated documentation files (the "Software"), to
// deal in the Software without restriction, including without limitation the
// rights to use, copy, modify, merge, publish, distribute, sublicense, and/or
// sell copies of the Software, and to permit persons to whom the Software is
// furnished to do so, subject to the following conditions:
//
// The above copyright notice and this permission notice shall be included in
// all copies or substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
// FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS
// IN THE SOFTWARE.

/*
 * Created on Oct 29, 2005
 * Author: Aditya Kalyanpur
 */
package org.mindswap.swoop.utils.owlapi;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.mindswap.swoop.renderer.SwoopRenderingVisitor;
import org.semanticweb.owl.impl.model.OWLInversePropertyAxiomImpl;
import org.semanticweb.owl.model.OWLAnd;
import org.semanticweb.owl.model.OWLAnnotationProperty;
import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLClassAxiom;
import org.semanticweb.owl.model.OWLDataAllRestriction;
import org.semanticweb.owl.model.OWLDataCardinalityRestriction;
import org.semanticweb.owl.model.OWLDataEnumeration;
import org.semanticweb.owl.model.OWLDataFactory;
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
import org.semanticweb.owl.model.change.AddClassAxiom;
import org.semanticweb.owl.model.change.AddDataPropertyInstance;
import org.semanticweb.owl.model.change.AddDataPropertyRange;
import org.semanticweb.owl.model.change.AddDomain;
import org.semanticweb.owl.model.change.AddEntity;
import org.semanticweb.owl.model.change.AddIndividualAxiom;
import org.semanticweb.owl.model.change.AddIndividualClass;
import org.semanticweb.owl.model.change.AddInverse;
import org.semanticweb.owl.model.change.AddObjectPropertyInstance;
import org.semanticweb.owl.model.change.AddObjectPropertyRange;
import org.semanticweb.owl.model.change.AddPropertyAxiom;
import org.semanticweb.owl.model.change.ChangeVisitor;
import org.semanticweb.owl.model.change.OntologyChange;
import org.semanticweb.owl.model.change.RemoveClassAxiom;
import org.semanticweb.owl.model.change.RemoveDataPropertyInstance;
import org.semanticweb.owl.model.change.RemoveDataPropertyRange;
import org.semanticweb.owl.model.change.RemoveDomain;
import org.semanticweb.owl.model.change.RemoveEquivalentClass;
import org.semanticweb.owl.model.change.RemoveIndividualAxiom;
import org.semanticweb.owl.model.change.RemoveIndividualClass;
import org.semanticweb.owl.model.change.RemoveInverse;
import org.semanticweb.owl.model.change.RemoveObjectPropertyInstance;
import org.semanticweb.owl.model.change.RemoveObjectPropertyRange;
import org.semanticweb.owl.model.change.RemovePropertyAxiom;
import org.semanticweb.owl.model.change.RemoveSuperClass;
import org.semanticweb.owl.model.change.SetFunctional;
import org.semanticweb.owl.model.change.SetInverseFunctional;
import org.semanticweb.owl.model.change.SetSymmetric;
import org.semanticweb.owl.model.change.SetTransitive;
import org.semanticweb.owl.model.helper.OWLBuilder;
import org.semanticweb.owl.model.helper.OWLObjectVisitorAdapter;

/**
 * @author Aditya
 * This class is used to build a new ontology given axioms from another ontology.
 * It passes each axiom to visit(OWLObject) which aligns axiom parameters 
 * relative to the new ontology -> creates a new axiom change and applies the change
 *
 */

public class OWLOntBuilder extends OWLObjectVisitorAdapter implements SwoopRenderingVisitor
//Uncomment for explanation
//,OWLExtendedObjectVisitor
{

	public OWLOntology currentOnt;
	public OWLDataFactory currentDF;
	public boolean addAxiom = true; // toggle whether to add or remove axiom
	public List changes = new ArrayList();
	
	public OWLOntBuilder() {
		try {
			OWLBuilder builder = new OWLBuilder();
			builder.createOntology(new URI("http://www.mindswap.org/test.owl"), new URI("http://www.mindswap.org/test.owl"));
			this.currentOnt = builder.getOntology();
			this.currentDF = currentOnt.getOWLDataFactory();
			this.changes = new ArrayList();
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	public OWLOntBuilder(URI uri) {
		try {
			OWLBuilder builder = new OWLBuilder();
			builder.createOntology(uri, uri);
			this.currentOnt = builder.getOntology();
			this.currentDF = currentOnt.getOWLDataFactory();
			this.changes = new ArrayList();
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	public OWLOntology getCurrentOntology(){
		return this.currentOnt;
	}
	
	public OWLDataFactory getCurrentDataFactory(){
		return this.currentDF;
	}
	
	
	
	public OWLOntBuilder(OWLOntology ont) {
		try {
			this.currentOnt = ont;
			this.currentDF = currentOnt.getOWLDataFactory();
			this.changes = new ArrayList();
		}
		catch (OWLException ex) {
			ex.printStackTrace();
		}
	}
	
	public OWLOntology buildOntologyFromAxioms(Set axioms) {
		try {
			for (Iterator iter = axioms.iterator(); iter.hasNext();) {
				OWLObject axiom = (OWLObject) iter.next();
				if(axiom instanceof OWLEquivalentClassesAxiom)
					this.visit((OWLEquivalentClassesAxiom)axiom);
				if(axiom instanceof OWLDisjointClassesAxiom)
					this.visit((OWLDisjointClassesAxiom)axiom);
				if(axiom instanceof OWLSubClassAxiom)
					this.visit((OWLSubClassAxiom)axiom);
				if(axiom instanceof OWLEquivalentPropertiesAxiom)
					this.visit((OWLEquivalentPropertiesAxiom)axiom);
				if(axiom instanceof OWLSubPropertyAxiom)
					this.visit((OWLSubPropertyAxiom)axiom);
				if(axiom instanceof OWLFunctionalPropertyAxiom)
					this.visit((OWLFunctionalPropertyAxiom)axiom);
				if(axiom instanceof OWLInverseFunctionalPropertyAxiom)
					this.visit((OWLInverseFunctionalPropertyAxiom)axiom);
				if(axiom instanceof OWLTransitivePropertyAxiom)
					this.visit((OWLTransitivePropertyAxiom)axiom);
				if(axiom instanceof OWLSymmetricPropertyAxiom)
					this.visit((OWLSymmetricPropertyAxiom)axiom);
				if(axiom instanceof OWLPropertyDomainAxiom)
					this.visit((OWLPropertyDomainAxiom)axiom);
				if(axiom instanceof OWLObjectPropertyRangeAxiom)
					this.visit((OWLObjectPropertyRangeAxiom)axiom);
				if(axiom instanceof OWLInversePropertyAxiom)
					this.visit((OWLInversePropertyAxiom)axiom);
			}
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
		return this.currentOnt;
	}
	
	public String result() {
		return "";
	}

	public OWLClass visitClass( OWLClass clazz ) throws OWLException {
		OWLClass currentDesc = null;
		// special bypass for owl:Thing, or owl:Nothing
		if (clazz.getURI().equals(currentDF.getOWLNothing().getURI())) {
			return currentDF.getOWLNothing();
		}
		else
		if (clazz.getURI().equals(currentDF.getOWLThing().getURI())) {
			return currentDF.getOWLThing();
		}
		else
		if ((currentDesc = currentOnt.getClass(clazz.getURI()))==null) {
			// create a new class
			currentDesc = currentDF.getOWLClass(clazz.getURI());
			AddEntity ae = new AddEntity(currentOnt, (OWLClass) currentDesc, null);
			ae.accept((ChangeVisitor) currentOnt);
			this.changes.add(ae);
		}
		return currentDesc;
	}
	
	public OWLIndividual visitIndividual( OWLIndividual ind ) throws OWLException {
		OWLIndividual currentInd = null;
		if ((currentInd = currentOnt.getIndividual(ind.getURI()))==null) {
			// create a new individual
			if (ind.isAnonymous()) 
				currentInd = currentDF.getAnonOWLIndividual(ind.getAnonId());
			else 
				currentInd = currentDF.getOWLIndividual(ind.getURI());
			AddEntity ae = new AddEntity(currentOnt, currentInd, null);
			ae.accept((ChangeVisitor) currentOnt);
			this.changes.add(ae);
		}
		return currentInd;
	}
	
	
	public OWLObjectProperty visitObjectProperty( OWLObjectProperty prop ) throws OWLException {
		OWLObjectProperty currentOProp = null;
		if ((currentOProp = currentOnt.getObjectProperty(prop.getURI()))==null) {
			// create a new object property
			currentOProp = currentDF.getOWLObjectProperty(prop.getURI());
			AddEntity ae = new AddEntity(currentOnt, currentOProp, null);
			ae.accept((ChangeVisitor) currentOnt);
			this.changes.add(ae);
		}
		return currentOProp;
	}
	
	public OWLAnnotationProperty visitAnnotationProperty( OWLAnnotationProperty prop ) throws OWLException {
		OWLAnnotationProperty currentAProp = null;
		if ((currentAProp = currentOnt.getAnnotationProperty(prop.getURI()))==null) {
			// create a new annotation property
			currentAProp = currentDF.getOWLAnnotationProperty(prop.getURI());
			AddEntity ae = new AddEntity(currentOnt, currentAProp, null);
			ae.accept((ChangeVisitor) currentOnt);
			this.changes.add(ae);
		}
		return currentAProp;
	}
	
	public OWLDataProperty visitDataProperty( OWLDataProperty prop ) throws OWLException {
		OWLDataProperty currentDProp = null;
		if ((currentDProp = currentOnt.getDataProperty(prop.getURI()))==null) {
			// create a new data property
			currentDProp = currentDF.getOWLDataProperty(prop.getURI());
			AddEntity ae = new AddEntity(currentOnt, currentDProp, null);
			ae.accept((ChangeVisitor) currentOnt);
			this.changes.add(ae);
		}
		return currentDProp;
	}
	
	public OWLDataValue visitDataValue( OWLDataValue cd ) throws OWLException {
		return currentDF.getOWLConcreteData(cd.getURI(), cd.getLang(), cd.getValue());
	}

	public OWLDescription visitAnd( OWLAnd and ) throws OWLException {
		Set andSet = new HashSet();
		for ( Iterator it = and.getOperands().iterator();
		it.hasNext(); ) {
			OWLDescription desc = (OWLDescription) it.next();
			OWLDescription currentDesc = this.visitDescription(desc);
			andSet.add(currentDesc);
		}
		return currentDF.getOWLAnd(andSet);
	}

	public OWLDescription visitDescription(OWLDescription desc) throws OWLException {
		if (desc instanceof OWLClass) return visitClass((OWLClass) desc);
		else if (desc instanceof OWLAnd) return visitAnd((OWLAnd) desc);
		else if (desc instanceof OWLOr) return visitOr((OWLOr) desc);
		else if (desc instanceof OWLNot) return visitNot((OWLNot) desc);
		else if (desc instanceof OWLDataSomeRestriction) return visitDataSomeRestriction((OWLDataSomeRestriction) desc);
		else if (desc instanceof OWLDataAllRestriction) return visitDataAllRestriction((OWLDataAllRestriction) desc);
		else if (desc instanceof OWLDataValueRestriction) return visitDataValueRestriction((OWLDataValueRestriction) desc);
		else if (desc instanceof OWLObjectSomeRestriction) return visitObjectSomeRestriction((OWLObjectSomeRestriction) desc);
		else if (desc instanceof OWLObjectAllRestriction) return visitObjectAllRestriction((OWLObjectAllRestriction) desc);
		else if (desc instanceof OWLObjectValueRestriction) return visitObjectValueRestriction((OWLObjectValueRestriction) desc);
		else if (desc instanceof OWLDataCardinalityRestriction) return visitDataCardinalityRestriction((OWLDataCardinalityRestriction) desc);
		else if (desc instanceof OWLObjectCardinalityRestriction) return visitObjectCardinalityRestriction((OWLObjectCardinalityRestriction) desc);
		else if (desc instanceof OWLEnumeration) return visitEnumeration((OWLEnumeration) desc);
		return null;
	}
	
	public OWLDescription visitOr( OWLOr or ) throws OWLException {
		Set orSet = new HashSet();
		for ( Iterator it = or.getOperands().iterator();
		it.hasNext(); ) {
			OWLDescription desc = (OWLDescription) it.next();
			OWLDescription currentDesc = this.visitDescription(desc);
			orSet.add(currentDesc);
		}
		return currentDF.getOWLOr(orSet);
	}

	public OWLDescription visitNot( OWLNot not ) throws OWLException {
		OWLDescription desc = not.getOperand();
		OWLDescription currentDesc = this.visitDescription(desc);
		return currentDF.getOWLNot(currentDesc);
	}

	public OWLEnumeration visitEnumeration( OWLEnumeration enumeration ) throws OWLException {
		Set indSet = new HashSet();
		for ( Iterator it = enumeration.getIndividuals().iterator();
		it.hasNext(); ) {
			OWLIndividual ind = (OWLIndividual) it.next();
			indSet.add(this.visitIndividual(ind));
		}
		return currentDF.getOWLEnumeration(indSet);
	}

	public OWLDescription visitObjectSomeRestriction( OWLObjectSomeRestriction restriction ) throws OWLException {		
		OWLObjectProperty prop = this.visitObjectProperty(restriction.getObjectProperty());
		OWLDescription currentDesc = this.visitDescription(restriction.getDescription());
		return currentDF.getOWLObjectSomeRestriction(prop, currentDesc);
	}

	public OWLDescription visitObjectAllRestriction( OWLObjectAllRestriction restriction ) throws OWLException {
		OWLObjectProperty prop = this.visitObjectProperty(restriction.getObjectProperty());
		OWLDescription currentDesc = this.visitDescription(restriction.getDescription());
		return currentDF.getOWLObjectAllRestriction(prop, currentDesc);		
	}

	public OWLDescription visitObjectValueRestriction( OWLObjectValueRestriction restriction ) throws OWLException {
		OWLObjectProperty prop = this.visitObjectProperty(restriction.getObjectProperty());
		OWLIndividual currentInd = this.visitIndividual(restriction.getIndividual());
		return currentDF.getOWLObjectValueRestriction(prop, currentInd);
	}

	public OWLDescription visitDataSomeRestriction( OWLDataSomeRestriction restriction ) throws OWLException {
		OWLDataProperty prop = this.visitDataProperty(restriction.getDataProperty());
		OWLDataType dRange = this.visitDataType((OWLDataType) restriction.getDataType());
		return currentDF.getOWLDataSomeRestriction(prop, dRange);
	}

	public OWLDescription visitDataAllRestriction( OWLDataAllRestriction restriction ) throws OWLException {
		OWLDataProperty prop = this.visitDataProperty(restriction.getDataProperty());
		OWLDataType dRange = this.visitDataType((OWLDataType) restriction.getDataType());
		return currentDF.getOWLDataAllRestriction(prop, dRange);
	}

	public OWLDescription visitObjectCardinalityRestriction( OWLObjectCardinalityRestriction restriction ) throws OWLException {
		OWLObjectProperty prop = this.visitObjectProperty(restriction.getObjectProperty());
		return currentDF.getOWLObjectCardinalityRestriction(prop, restriction.getAtLeast(), restriction.getAtMost());
	}

	public OWLDescription visitDataCardinalityRestriction( OWLDataCardinalityRestriction restriction ) throws OWLException {
		OWLDataProperty prop = this.visitDataProperty(restriction.getDataProperty());
		return currentDF.getOWLDataCardinalityRestriction(prop, restriction.getAtLeast(), restriction.getAtMost());
	}

	public OWLDescription visitDataValueRestriction( OWLDataValueRestriction restriction ) throws OWLException {
		OWLDataProperty prop = this.visitDataProperty(restriction.getDataProperty());
		return currentDF.getOWLDataValueRestriction(prop, restriction.getValue());		
	}

	public void visit( OWLEquivalentClassesAxiom axiom ) throws OWLException {

		Set equClas = axiom.getEquivalentClasses();
		Set alignEQ = new HashSet();
		for ( Iterator it = equClas.iterator(); it.hasNext(); ) {
			OWLDescription desc = (OWLDescription) it.next();
			OWLDescription currentDesc = this.visitDescription(desc);
			alignEQ.add(currentDesc);
		}
		
		OWLClassAxiom currentObject = currentDF.getOWLEquivalentClassesAxiom(alignEQ);
		
		OntologyChange acx = null;
		if (addAxiom) {
			acx = new AddClassAxiom(currentOnt, (OWLClassAxiom) currentObject, null);
			acx.accept((ChangeVisitor) currentOnt);
			this.changes.add(acx);
		}
		else {
			acx = new RemoveClassAxiom(currentOnt, (OWLClassAxiom) currentObject, null);
			// remove equivalent class
			acx.accept((ChangeVisitor) currentOnt);
			OWLDescription cla1 = (OWLDescription) alignEQ.iterator().next();
			alignEQ.remove(cla1);
			OWLDescription cla2 = (OWLDescription) alignEQ.iterator().next();
			if (cla1 instanceof OWLClass) {
				acx = new RemoveEquivalentClass(currentOnt, (OWLClass) cla1, cla2, null);
				acx.accept((ChangeVisitor) currentOnt);
				this.changes.add(acx);
			}
			if (cla2 instanceof OWLClass) {
				acx = new RemoveEquivalentClass(currentOnt, (OWLClass) cla2, cla1, null);
				acx.accept((ChangeVisitor) currentOnt);
				this.changes.add(acx);
			}
		}
		
	}

	public void visit( OWLDisjointClassesAxiom axiom ) throws OWLException {
		
		Set alignDI = new HashSet();
		for ( Iterator it = axiom.getDisjointClasses().iterator();
		it.hasNext(); ) {
			OWLDescription desc = (OWLDescription) it.next();
			OWLDescription currentDesc = this.visitDescription(desc);
			// alignDesc has now been updated
			alignDI.add(currentDesc);
		}
		OWLClassAxiom currentObject = currentDF.getOWLDisjointClassesAxiom(alignDI);
		OntologyChange acx = null;
		if (addAxiom)
			acx = new AddClassAxiom(currentOnt, (OWLClassAxiom) currentObject, null);
		else 
			acx = new RemoveClassAxiom(currentOnt, (OWLClassAxiom) currentObject, null);
		acx.accept((ChangeVisitor) currentOnt);
		this.changes.add(acx);
	}

	public void visit( OWLSubClassAxiom axiom ) throws OWLException {
		OWLClassAxiom currentObject = currentDF.getOWLSubClassAxiom(this.visitDescription(axiom.getSubClass()), this.visitDescription(axiom.getSuperClass()));
		OntologyChange acx = null;
		if (addAxiom) {
			acx = new AddClassAxiom(currentOnt, (OWLClassAxiom) currentObject, null);
			this.changes.add(acx);
		}
		else {
			acx = new RemoveClassAxiom(currentOnt, (OWLClassAxiom) currentObject, null);
			// also remove superclass assertion
			OWLSubClassAxiom subAx = (OWLSubClassAxiom) currentObject;
			OWLDescription sub = subAx.getSubClass();
			OWLDescription sup = subAx.getSuperClass();
			if (sub instanceof OWLClass) {
				RemoveSuperClass rsc = new RemoveSuperClass(currentOnt, currentOnt.getClass(((OWLClass) sub).getURI()), sup, null);
				rsc.accept((ChangeVisitor) currentOnt);
				this.changes.add(rsc);
			}
		}
		acx.accept((ChangeVisitor) currentOnt);		
	}

	public void visit( OWLEquivalentPropertiesAxiom axiom ) throws OWLException {
		
		Set alignEP = new HashSet();
		for ( Iterator it = axiom.getProperties().iterator();
		it.hasNext(); ) {
			OWLProperty prop = (OWLProperty) it.next();
			if (prop instanceof OWLDataProperty)
				alignEP.add(this.visitDataProperty((OWLDataProperty) prop));
			else 
				alignEP.add(this.visitObjectProperty((OWLObjectProperty) prop));
		}
		OWLPropertyAxiom currentObject = currentDF.getOWLEquivalentPropertiesAxiom(alignEP);
		OntologyChange apx = null;
		if (addAxiom)
			apx = new AddPropertyAxiom(currentOnt, (OWLPropertyAxiom) currentObject, null);
		else 
			apx = new RemovePropertyAxiom(currentOnt, (OWLPropertyAxiom) currentObject, null);
		apx.accept((ChangeVisitor) currentOnt);
		this.changes.add(apx);
	}

	public void visit( OWLSubPropertyAxiom axiom ) throws OWLException {
		OWLProperty sub, sup;
		if (axiom.getSubProperty() instanceof OWLDataProperty) { 
			sub = this.visitDataProperty((OWLDataProperty) axiom.getSubProperty()); 
		}
		else {
			sub = this.visitObjectProperty((OWLObjectProperty) axiom.getSubProperty());
		}
		
		if (axiom.getSuperProperty() instanceof OWLDataProperty) {
			sup = this.visitDataProperty((OWLDataProperty) axiom.getSuperProperty()); 
		}
		else {
			sup = this.visitObjectProperty((OWLObjectProperty) axiom.getSuperProperty());
		}
		
		OWLPropertyAxiom currentObject = currentDF.getOWLSubPropertyAxiom(sub, sup);
		OntologyChange apx = null;
		if (addAxiom)
			apx = new AddPropertyAxiom(currentOnt, (OWLPropertyAxiom) currentObject, null);
		else 
			apx = new RemovePropertyAxiom(currentOnt, (OWLPropertyAxiom) currentObject, null);
		apx.accept((ChangeVisitor) currentOnt);
		this.changes.add(apx);
	}

	public void visit( OWLDifferentIndividualsAxiom ax) throws OWLException {
		Set alignDI = new HashSet();
		for ( Iterator it = ax.getIndividuals().iterator();
		it.hasNext(); ) {
			OWLIndividual desc = (OWLIndividual) it.next();
			alignDI.add(this.visitIndividual(desc));
		}
		OWLIndividualAxiom currentObject = currentDF.getOWLDifferentIndividualsAxiom(alignDI);
		OntologyChange aix = null;
		if (addAxiom)
			aix = new AddIndividualAxiom(currentOnt, (OWLIndividualAxiom) currentObject, null);
		else 
			aix = new RemoveIndividualAxiom(currentOnt, (OWLIndividualAxiom) currentObject, null);
		aix.accept((ChangeVisitor) currentOnt);
		this.changes.add(aix);
	}

	public void visit( OWLSameIndividualsAxiom ax) throws OWLException {
		Set alignDI = new HashSet();
		for ( Iterator it = ax.getIndividuals().iterator();
		it.hasNext(); ) {
			OWLIndividual desc = (OWLIndividual) it.next();
			alignDI.add(this.visitIndividual(desc));
		}
		OWLIndividualAxiom currentObject = currentDF.getOWLSameIndividualsAxiom(alignDI);
		OntologyChange aix = null;
		if (addAxiom)
			aix = new AddIndividualAxiom(currentOnt, (OWLIndividualAxiom) currentObject, null);
		else 
			aix = new RemoveIndividualAxiom(currentOnt, (OWLIndividualAxiom) currentObject, null);
		aix.accept((ChangeVisitor) currentOnt);
		this.changes.add(aix);
	}

	public OWLDataType visitDataType( OWLDataType ocdt ) throws OWLException {
		return currentDF.getOWLConcreteDataType(ocdt.getURI());
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
		OWLProperty prop = null;
		if (axiom.getProperty() instanceof OWLDataProperty) 
			prop = this.visitDataProperty(((OWLDataProperty) axiom.getProperty()));
		else
			prop = this.visitObjectProperty(((OWLObjectProperty) axiom.getProperty()));
		// apply set functional change
		SetFunctional sf = new SetFunctional(currentOnt, prop, true, null);
		sf.accept((ChangeVisitor) currentOnt);
		this.changes.add(sf);
	}
	
	public void visit( OWLPropertyDomainAxiom axiom ) throws OWLException {
		OWLProperty prop = null;
		if (axiom.getProperty() instanceof OWLDataProperty) 
			prop = this.visitDataProperty(((OWLDataProperty) axiom.getProperty()));
		else
			prop = this.visitObjectProperty(((OWLObjectProperty) axiom.getProperty()));
		
		OWLDescription domain = this.visitDescription(axiom.getDomain());
		OntologyChange ad = null;
		if (addAxiom)
			ad = new AddDomain(currentOnt, prop, domain, null);
		else 
			ad = new RemoveDomain(currentOnt, prop, domain, null);
		ad.accept((ChangeVisitor) currentOnt);
		this.changes.add(ad);
	}
	
	public void visit( OWLObjectPropertyRangeAxiom axiom ) throws OWLException {
		OWLObjectProperty prop = this.visitObjectProperty(axiom.getProperty()); 
		OWLDescription range = this.visitDescription(axiom.getRange());
		OntologyChange ad = null;
		if (addAxiom)
			ad = new AddObjectPropertyRange(currentOnt, prop, range, null);
		else 
			ad = new RemoveObjectPropertyRange(currentOnt, prop, range, null);
		ad.accept((ChangeVisitor) currentOnt);
		this.changes.add(ad);
	}
	
	public void visit( OWLDataPropertyRangeAxiom axiom ) throws OWLException {
		OWLDataProperty prop = this.visitDataProperty(axiom.getProperty()); 
		OWLDataType range = this.visitDataType((OWLDataType) axiom.getRange());
		OntologyChange ad = null;
		if (addAxiom)
			ad = new AddDataPropertyRange(currentOnt, prop, range, null);
		else 
			ad = new RemoveDataPropertyRange(currentOnt, prop, range, null);
		ad.accept((ChangeVisitor) currentOnt);
		this.changes.add(ad);
	}
	
	/* (non-Javadoc)
	* @see org.mindswap.pellet.owlapi.OWLExtendedObjectVisitor#visit(org.mindswap.pellet.owlapi.OWLInverseFunctionalPropertyAxiom)
	*/
	public void visit(OWLInverseFunctionalPropertyAxiom axiom) throws OWLException {
		if (axiom instanceof OWLInversePropertyAxiomImpl) {
			visit((OWLInversePropertyAxiomImpl) axiom);
			return;
		}
		SetInverseFunctional sif = new SetInverseFunctional(currentOnt, this.visitObjectProperty(axiom.getProperty()), addAxiom, null);
		sif.accept((ChangeVisitor) currentOnt);
		this.changes.add(sif);
	}
	
	/* (non-Javadoc)
	* @see org.mindswap.pellet.owlapi.OWLExtendedObjectVisitor#visit(org.mindswap.pellet.owlapi.OWLTransitivePropertyAxiom)
	*/
	public void visit(OWLTransitivePropertyAxiom axiom) throws OWLException {
		SetTransitive st = new SetTransitive(currentOnt, this.visitObjectProperty((OWLObjectProperty) axiom.getProperty()), addAxiom, null);
		st.accept((ChangeVisitor) currentOnt);
		this.changes.add(st);
	}
	
	/* (non-Javadoc)
	* @see org.mindswap.pellet.owlapi.OWLExtendedObjectVisitor#visit(org.mindswap.pellet.owlapi.OWLSymmetricPropertyAxiom)
	*/
	public void visit(OWLSymmetricPropertyAxiom axiom) throws OWLException {
		SetSymmetric ss = new SetSymmetric(currentOnt, this.visitObjectProperty(axiom.getProperty()), addAxiom, null);
		ss.accept((ChangeVisitor) currentOnt);
		this.changes.add(ss);
	}
	
	/* (non-Javadoc)
	* @see org.mindswap.pellet.owlapi.OWLExtendedObjectVisitor#visit(org.mindswap.pellet.owlapi.OWLInversePropertyAxiom)
	*/
	public void visit(OWLInversePropertyAxiomImpl axiom) throws OWLException {
		OWLObjectProperty prop = this.visitObjectProperty(axiom.getProperty());
		OWLObjectProperty inv = this.visitObjectProperty(axiom.getInverseProperty());
		OntologyChange ai = null;
		if (addAxiom) {
			ai = new AddInverse(currentOnt, prop, inv, null);
			ai.accept((ChangeVisitor) currentOnt);
		}
		else {
			ai = new RemoveInverse(currentOnt, prop, inv, null);
			ai.accept((ChangeVisitor) currentOnt);
			ai = new RemoveInverse(currentOnt, inv, prop, null);
			ai.accept((ChangeVisitor) currentOnt);
		}
		this.changes.add(ai);
	}
	
	/* (non-Javadoc)
	* @see org.mindswap.pellet.owlapi.OWLExtendedObjectVisitor#visit(org.mindswap.pellet.owlapi.OWLObjectPropertyInstance)
	*/
	public void visit(OWLObjectPropertyInstance axiom) throws OWLException {
		OWLIndividual subj = this.visitIndividual(axiom.getSubject());
		OWLObjectProperty prop = this.visitObjectProperty(axiom.getProperty());
		OWLIndividual obj = this.visitIndividual(axiom.getObject());
		OntologyChange aopi = null;
		if (addAxiom)
			aopi = new AddObjectPropertyInstance(currentOnt, subj, prop, obj, null);
		else 
			aopi = new RemoveObjectPropertyInstance(currentOnt, subj, prop, obj, null);
		aopi.accept((ChangeVisitor) currentOnt);
		this.changes.add(aopi);
	}
	
	/* (non-Javadoc)
	* @see org.mindswap.pellet.owlapi.OWLExtendedObjectVisitor#visit(org.mindswap.pellet.owlapi.OWLDataPropertyInstance)
	*/
	public void visit(OWLDataPropertyInstance axiom) throws OWLException {
		OWLIndividual subj = this.visitIndividual(axiom.getSubject());
		OWLDataProperty prop = this.visitDataProperty(axiom.getProperty());
		OWLDataValue obj = axiom.getObject();
		OntologyChange adpi = null;
		if (addAxiom)
			adpi = new AddDataPropertyInstance(currentOnt, subj, prop, obj, null);
		else 
			adpi = new RemoveDataPropertyInstance(currentOnt, subj, prop, obj, null);
		adpi.accept((ChangeVisitor) currentOnt);
		this.changes.add(adpi);
	}
	
	public void visit(OWLIndividualTypeAssertion axiom) throws OWLException {
			OWLIndividual ind = this.visitIndividual(axiom.getIndividual());
			OWLDescription currentDesc = this.visitDescription(axiom.getType());
			OntologyChange aic = null;
			if (addAxiom)
				aic = new AddIndividualClass(currentOnt, ind, currentDesc, null);
			else 
				aic = new RemoveIndividualClass(currentOnt, ind, currentDesc, null);
			aic.accept((ChangeVisitor) currentOnt);
			this.changes.add(aic);
	}
	
	public void visit(OWLInversePropertyAxiom arg0) throws OWLException {
		visit((OWLInversePropertyAxiomImpl) arg0);
	}
	
	public void reset() {		
	}

}
