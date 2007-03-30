package org.mindswap.swoop.utils.owlapi;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.semanticweb.owl.impl.model.OWLDataFactoryImpl;
import org.semanticweb.owl.impl.model.OWLIndividualTypeAssertionImpl;
import org.semanticweb.owl.model.OWLAnnotationInstance;
import org.semanticweb.owl.model.OWLAnnotationProperty;
import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLDataFactory;
import org.semanticweb.owl.model.OWLDataProperty;
import org.semanticweb.owl.model.OWLDataPropertyInstance;
import org.semanticweb.owl.model.OWLDataPropertyRangeAxiom;
import org.semanticweb.owl.model.OWLDataRange;
import org.semanticweb.owl.model.OWLDataType;
import org.semanticweb.owl.model.OWLDataValue;
import org.semanticweb.owl.model.OWLDescription;
import org.semanticweb.owl.model.OWLDifferentIndividualsAxiom;
import org.semanticweb.owl.model.OWLDisjointClassesAxiom;
import org.semanticweb.owl.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owl.model.OWLEquivalentPropertiesAxiom;
import org.semanticweb.owl.model.OWLException;
import org.semanticweb.owl.model.OWLFunctionalPropertyAxiom;
import org.semanticweb.owl.model.OWLIndividual;
import org.semanticweb.owl.model.OWLIndividualTypeAssertion;
import org.semanticweb.owl.model.OWLInverseFunctionalPropertyAxiom;
import org.semanticweb.owl.model.OWLInversePropertyAxiom;
import org.semanticweb.owl.model.OWLObject;
import org.semanticweb.owl.model.OWLObjectProperty;
import org.semanticweb.owl.model.OWLObjectPropertyInstance;
import org.semanticweb.owl.model.OWLObjectPropertyRangeAxiom;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLProperty;
import org.semanticweb.owl.model.OWLPropertyDomainAxiom;
import org.semanticweb.owl.model.OWLSameIndividualsAxiom;
import org.semanticweb.owl.model.OWLSubClassAxiom;
import org.semanticweb.owl.model.OWLSubPropertyAxiom;
import org.semanticweb.owl.model.OWLSymmetricPropertyAxiom;
import org.semanticweb.owl.model.OWLTransitivePropertyAxiom;
import org.semanticweb.owl.model.helper.OWLObjectVisitorAdapter;


public class AxiomCollector extends OWLObjectVisitorAdapter {

	protected Set collectedAxioms;
	protected OWLOntology ontology;
	protected OWLDataFactory factory;
	
	/**
	 * Construct a visitor that extracts *Axiom and *Instance objects
	 * from objects it visits.
	 * 
	 * @param ontology
	 * @throws OWLException
	 */
	public AxiomCollector(OWLOntology ontology) throws OWLException {
		this.ontology = ontology;
		this.factory = ontology.getOWLDataFactory();
		reset();
	}

	public void collectAxioms() throws OWLException {
		ontology.accept(this);
		collectAxiomsFromSet(ontology.getClasses());
		collectAxiomsFromSet(ontology.getDatatypes());
		collectAxiomsFromSet(ontology.getAnnotationProperties());
		collectAxiomsFromSet(ontology.getDataProperties());
		collectAxiomsFromSet(ontology.getObjectProperties());
		collectAxiomsFromSet(ontology.getIndividuals());
		
		collectAxiomsFromSet(ontology.getClassAxioms());
		collectAxiomsFromSet(ontology.getPropertyAxioms());
		collectAxiomsFromSet(ontology.getIndividualAxioms());
	}
	
	protected void collectAxiomsFromSet(Set set) throws OWLException {
		for (Iterator iter = set.iterator(); iter.hasNext();) {
			OWLObject obj = (OWLObject) iter.next();
			obj.accept(this);
		}
	}

	public static Set axiomize(OWLOntology ontology) throws OWLException {
		AxiomCollector collector = new AxiomCollector(ontology);
		collector.collectAxioms();
		return collector.axioms();
	}
	
	public Set axioms() {
		return collectedAxioms;
	}
	
	public void reset() {
		collectedAxioms = new HashSet();
	}
	public void reset(Set set) {
		collectedAxioms = set;
	}

	protected void addAnnotations(OWLObject node) throws OWLException {
		for (Iterator iter = node.getAnnotations(ontology).iterator(); iter.hasNext(); ) {
			OWLAnnotationInstance annotation = (OWLAnnotationInstance) iter.next();
			collectedAxioms.add(annotation);
		}
	}
	
	protected void addCommonPropertyAxioms(OWLProperty node) throws OWLException {
		addAnnotations(node);
		for (Iterator iter = node.getSuperProperties(ontology).iterator(); iter.hasNext();) {
			OWLProperty superProp = (OWLProperty) iter.next();
			collectedAxioms.add(factory.getOWLSubPropertyAxiom(node, superProp));
		}
		for (Iterator iter = node.getDomains(ontology).iterator(); iter.hasNext();) {
			OWLDescription description = (OWLDescription) iter.next();
			collectedAxioms.add(factory.getOWLPropertyDomainAxiom(node, description));
		}
		if (node.isFunctional(ontology)) {
			collectedAxioms.add(factory.getOWLFunctionalPropertyAxiom(node));
		}
	}
	
	/**
	 * Adds just the annotations of the ontology.
	 */
	public void visit(OWLOntology node) throws OWLException {
		addAnnotations(node);
	}
	
	public void visit(OWLAnnotationProperty node) throws OWLException {
		addAnnotations(node);
	}

	public void visit(OWLAnnotationInstance node) throws OWLException {
		collectedAxioms.add(node);
	}

	
	public void visit(OWLDataProperty node) throws OWLException {
		addCommonPropertyAxioms(node);
		for (Iterator iter = node.getRanges(ontology).iterator(); iter.hasNext();) {
			OWLDataRange range = (OWLDataRange) iter.next();
			collectedAxioms.add(factory.getOWLDataPropertyRangeAxiom(node, range));
		}
	}
	
	public void visit(OWLDataType node) throws OWLException {
		addAnnotations(node);
	}
	
	public void visit(OWLDifferentIndividualsAxiom node) throws OWLException {
		collectedAxioms.add(node);
	}

	public void visit(OWLDisjointClassesAxiom node) throws OWLException {
		collectedAxioms.add(node);
	}

	public void visit(OWLEquivalentClassesAxiom node) throws OWLException {
		collectedAxioms.add(node);
	}

	public void visit(OWLEquivalentPropertiesAxiom node) throws OWLException {
		collectedAxioms.add(node);
	}

	public void visit(OWLIndividual node) throws OWLException {
		addAnnotations(node);
		for (Iterator iter = node.getTypes(ontology).iterator(); iter.hasNext(); ) {
			OWLDescription type = (OWLDescription) iter.next();
			collectedAxioms.add(new OWLIndividualTypeAssertionImpl((OWLDataFactoryImpl) factory, node, type));
		}
		
		Map dataMap = node.getDataPropertyValues(ontology);
		for (Iterator keyIter = dataMap.keySet().iterator(); keyIter.hasNext();) {
			OWLDataProperty prop = (OWLDataProperty) keyIter.next();
			Set values = (Set) dataMap.get(prop);
			for (Iterator iter = values.iterator(); iter.hasNext(); ) {
				OWLDataValue value = (OWLDataValue) iter.next();
				collectedAxioms.add(factory.getOWLDataPropertyInstance(node, prop, value));
			}
		}
		
		Map objectMap = node.getObjectPropertyValues(ontology);
		for (Iterator keyIter = objectMap.keySet().iterator(); keyIter.hasNext();) {
			OWLObjectProperty prop = (OWLObjectProperty) keyIter.next();
			Set values = (Set) objectMap.get(prop);
			for (Iterator iter = values.iterator(); iter.hasNext(); ) {
				OWLIndividual value = (OWLIndividual) iter.next();
				collectedAxioms.add(factory.getOWLObjectPropertyInstance(node, prop, value));
			}
		}
	}

	public void visit(OWLObjectProperty node) throws OWLException {
		addCommonPropertyAxioms(node);
		for (Iterator iter = node.getDomains(ontology).iterator(); iter.hasNext();) {
			OWLDescription description = (OWLDescription) iter.next();
			collectedAxioms.add(factory.getOWLPropertyDomainAxiom(node, description));
		}
		for (Iterator iter = node.getRanges(ontology).iterator(); iter.hasNext();) {
			OWLDescription description = (OWLDescription) iter.next();
			collectedAxioms.add(factory.getOWLObjectPropertyRangeAxiom(node, description));
		}
		for(Iterator iter = node.getInverses(ontology).iterator(); iter.hasNext();){
			OWLObjectProperty prop = (OWLObjectProperty)iter.next();
			collectedAxioms.add(factory.getOWLInversePropertyAxiom(node,prop));
		}
		
		if (node.isInverseFunctional(ontology)) {
			collectedAxioms.add(factory.getOWLInverseFunctionalPropertyAxiom(node));
		}
		if (node.isSymmetric(ontology)) {
			collectedAxioms.add(factory.getOWLSymmetricPropertyAxiom(node));
		}
		if (node.isTransitive(ontology)) {
			collectedAxioms.add(factory.getOWLTransitivePropertyAxiom(node));
		}
	}

	public void visit(OWLClass node) throws OWLException {
		addAnnotations(node);
		for (Iterator iter = node.getSuperClasses(ontology).iterator(); iter.hasNext();) {
			OWLDescription superClass = (OWLDescription) iter.next();
			collectedAxioms.add(factory.getOWLSubClassAxiom(node, superClass));
		}
		Set equivalents = new HashSet(node.getEquivalentClasses(ontology));
		equivalents.addAll(node.getEnumerations(ontology));
		equivalents.add(node);
		collectedAxioms.add(factory.getOWLEquivalentClassesAxiom(equivalents));
	}

	public void visit(OWLSameIndividualsAxiom node) throws OWLException {
		collectedAxioms.add(node);
	}

	public void visit(OWLSubClassAxiom node) throws OWLException {
		collectedAxioms.add(node);
	}

	public void visit(OWLSubPropertyAxiom node) throws OWLException {
		collectedAxioms.add(node);
	}

	public void visit(OWLFunctionalPropertyAxiom node) throws OWLException {
		collectedAxioms.add(node);
	}

	public void visit(OWLInverseFunctionalPropertyAxiom node)
			throws OWLException {
		collectedAxioms.add(node);
	}

	public void visit(OWLTransitivePropertyAxiom node) throws OWLException {
		collectedAxioms.add(node);
	}

	public void visit(OWLSymmetricPropertyAxiom node) throws OWLException {
		collectedAxioms.add(node);
	}

	public void visit(OWLInversePropertyAxiom node) throws OWLException {
		collectedAxioms.add(node);
	}

	public void visit(OWLPropertyDomainAxiom node) throws OWLException {
		collectedAxioms.add(node);
	}

	public void visit(OWLObjectPropertyRangeAxiom node) throws OWLException {
		collectedAxioms.add(node);
	}

	public void visit(OWLDataPropertyRangeAxiom node) throws OWLException {
		collectedAxioms.add(node);
	}

	public void visit(OWLObjectPropertyInstance node) throws OWLException {
		collectedAxioms.add(node);
	}

	public void visit(OWLDataPropertyInstance node) throws OWLException {
		collectedAxioms.add(node);
	}

	public void visit(OWLIndividualTypeAssertion node) throws OWLException {
		collectedAxioms.add(node);
	}

}
