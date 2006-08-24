package org.mindswap.swoop.utils.owlapi;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.semanticweb.owl.impl.model.OWLDataFactoryImpl;
import org.semanticweb.owl.impl.model.OWLIndividualTypeAssertionImpl;
import org.semanticweb.owl.model.OWLAnd;
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
import org.semanticweb.owl.model.OWLEntity;
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
import org.semanticweb.owl.model.OWLObjectProperty;
import org.semanticweb.owl.model.OWLObjectPropertyInstance;
import org.semanticweb.owl.model.OWLObjectPropertyRangeAxiom;
import org.semanticweb.owl.model.OWLObjectQuantifiedRestriction;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLOr;
import org.semanticweb.owl.model.OWLProperty;
import org.semanticweb.owl.model.OWLPropertyDomainAxiom;
import org.semanticweb.owl.model.OWLRestriction;
import org.semanticweb.owl.model.OWLSameIndividualsAxiom;
import org.semanticweb.owl.model.OWLSubClassAxiom;
import org.semanticweb.owl.model.OWLSubPropertyAxiom;
import org.semanticweb.owl.model.OWLSymmetricPropertyAxiom;
import org.semanticweb.owl.model.OWLTransitivePropertyAxiom;
import org.semanticweb.owl.model.helper.OWLObjectVisitorAdapter;


public class SignatureCollector extends OWLObjectVisitorAdapter {

	protected Map axiomsToSignature;
	protected Set allAxioms;
		
	/**
	 * Construct a visitor that extracts a map from axioms in the ontology to their signature
	 * 
	 * @param ontology
	 * @throws OWLException
	 */
	public SignatureCollector(Set allAxioms) throws OWLException {
		this.allAxioms = allAxioms;
		reset();
	}
	
	private static void addToMap(Map map, Object key, Object value) {
		Set index = new HashSet();
		if (map.containsKey(key)) index.addAll((HashSet) map.get(key));
		if (value instanceof Set)
			index.addAll((HashSet) value);
		else 
			index.add(value);
		map.put(key, index);
	}
	
	
	public Map getAxiomsToSignature(){
		return axiomsToSignature;
	}
	
	public static Map buildSignatureToAxiom(Map axToSignature){
		Map result = new HashMap();
		Iterator iter = axToSignature.keySet().iterator();
		while(iter.hasNext()){
			OWLObject axiom = (OWLObject)iter.next();
			Set signature = new HashSet();
			signature = (Set)axToSignature.get(axiom);
			Iterator it = signature.iterator();
			while(it.hasNext()){
				OWLEntity ent = (OWLEntity)it.next();
				addToMap(result, ent, axiom);
			}
			
		}
		return result;
	}
	
	public static Map buildSignatureMap(Set allAxioms) throws OWLException {
		SignatureCollector collector = new SignatureCollector(allAxioms);
		
		Set toVisit = new HashSet();
		toVisit.addAll(allAxioms);
		
		for (Iterator iter = toVisit.iterator(); iter.hasNext();) {
			OWLObject obj = (OWLObject) iter.next();
			if(obj != null)
				obj.accept(collector);
		}
		
		return collector.axiomsToSignature;
	}
	
	
	
	public void reset() {
		axiomsToSignature = new HashMap();
		
	}

	
	
	public void visit(OWLDifferentIndividualsAxiom node) throws OWLException {
		Set signature = new HashSet();
		Set sameInds = ((OWLDifferentIndividualsAxiom)node).getIndividuals();
		signature.addAll(sameInds);
		axiomsToSignature.put(node,signature);
	}
	
	public Set visit(OWLDescription desc) throws OWLException{
		Set result = new HashSet();
		if (desc instanceof OWLClass){
			result.add(desc);		
			return result;
		}
		if (desc instanceof OWLNot){
			return(visit(
					((OWLNot)desc).getOperand()));
		}
		if (desc instanceof OWLAnd){
			Iterator iter = ((OWLAnd)desc).getOperands().iterator();
			while(iter.hasNext()){
				OWLDescription d = (OWLDescription)iter.next();
				result.addAll(visit(d));
			}
			return result;
		}
		
		if (desc instanceof OWLOr){
			Iterator iter = ((OWLOr)desc).getOperands().iterator();
			while(iter.hasNext()){
				OWLDescription d = (OWLDescription)iter.next();
				result.addAll(visit(d));
			}
			return result;
		}
		
		if(desc instanceof OWLRestriction){
			result.add(((OWLRestriction)desc).getProperty());
			if(desc instanceof OWLObjectQuantifiedRestriction)
				result.addAll(visit(((OWLObjectQuantifiedRestriction)desc).getDescription()));
			return result;
		}
		
			
		return result;
	}
	
	public void visit(OWLDisjointClassesAxiom node) throws OWLException {
		Set signature = new HashSet();
		Set disjointConcepts = ((OWLDisjointClassesAxiom)node).getDisjointClasses();
		Iterator iter = disjointConcepts.iterator();
		while(iter.hasNext()){
			OWLDescription desc = (OWLDescription)iter.next();
			signature.addAll(visit(desc));
		}
		axiomsToSignature.put(node,signature);
	}

	public void visit(OWLEquivalentClassesAxiom node) throws OWLException {
		Set signature = new HashSet();
		Set eqConcepts = ((OWLEquivalentClassesAxiom)node).getEquivalentClasses();
		Iterator iter = eqConcepts.iterator();
		while(iter.hasNext()){
			OWLDescription desc = (OWLDescription)iter.next();
			signature.addAll(visit(desc));
		}
		axiomsToSignature.put(node,signature);
	}

	public void visit(OWLEquivalentPropertiesAxiom node) throws OWLException {
		Set signature = new HashSet();
		Set eqProperties = ((OWLEquivalentPropertiesAxiom)node).getProperties();
		Iterator iter = eqProperties.iterator();
		while(iter.hasNext()){
			OWLProperty prop = (OWLProperty)iter.next();
			signature.add(prop);
		}
		axiomsToSignature.put(node,signature);
	}

	
	public void visit(OWLSameIndividualsAxiom node) throws OWLException {
		Set signature = new HashSet();
		Set sameInds = ((OWLSameIndividualsAxiom)node).getIndividuals();
		signature.addAll(sameInds);
		axiomsToSignature.put(node,signature);
	}

	public void visit(OWLSubClassAxiom node) throws OWLException {
		Set signature = new HashSet();
		OWLDescription sub = ((OWLSubClassAxiom)node).getSubClass();
		OWLDescription sup = ((OWLSubClassAxiom)node).getSuperClass();
		signature.addAll(visit(sub));
		signature.addAll(visit(sup));
		axiomsToSignature.put(node,signature);
	}
	
	public void visit(OWLAnnotationInstance node) throws OWLException {
	}
	

	public void visit(OWLSubPropertyAxiom node) throws OWLException {
		Set signature = new HashSet();
		OWLProperty sub = ((OWLSubPropertyAxiom)node).getSubProperty();
		OWLProperty sup = ((OWLSubPropertyAxiom)node).getSuperProperty();
		signature.add(sub);
		signature.add(sup);
		axiomsToSignature.put(node,signature);
		}

	public void visit(OWLFunctionalPropertyAxiom node) throws OWLException {
		Set signature = new HashSet();
		OWLProperty prop = ((OWLFunctionalPropertyAxiom)node).getProperty();
		signature.add(prop);
		axiomsToSignature.put(node,signature);
		
	}

	public void visit(OWLInverseFunctionalPropertyAxiom node)
			throws OWLException {
		Set signature = new HashSet();
		OWLObjectProperty prop = ((OWLInverseFunctionalPropertyAxiom)node).getProperty();
		signature.add(prop);
		axiomsToSignature.put(node,signature);
	}

	public void visit(OWLTransitivePropertyAxiom node) throws OWLException {
		Set signature = new HashSet();
		OWLProperty prop = ((OWLTransitivePropertyAxiom)node).getProperty();
		signature.add(prop);
		axiomsToSignature.put(node,signature);
	}

	public void visit(OWLSymmetricPropertyAxiom node) throws OWLException {
		Set signature = new HashSet();
		OWLProperty prop = ((OWLSymmetricPropertyAxiom)node).getProperty();
		signature.add(prop);
		axiomsToSignature.put(node,signature);
	}

	public void visit(OWLInversePropertyAxiom node) throws OWLException {
		Set signature = new HashSet();
		OWLProperty prop = ((OWLInversePropertyAxiom)node).getProperty();
		OWLProperty inv = ((OWLInversePropertyAxiom)node).getInverseProperty();
		signature.add(prop);
		signature.add(inv);
		axiomsToSignature.put(node,signature);
	}

	public void visit(OWLPropertyDomainAxiom node) throws OWLException {
		Set signature = new HashSet();
		OWLProperty prop = ((OWLPropertyDomainAxiom)node).getProperty();
		signature.add(prop);
		OWLDescription desc = ((OWLPropertyDomainAxiom)node).getDomain();
		signature.addAll(visit(desc));
		axiomsToSignature.put(node,signature);
	}

	public void visit(OWLObjectPropertyRangeAxiom node) throws OWLException {
		Set signature = new HashSet();
		OWLProperty prop = ((OWLObjectPropertyRangeAxiom)node).getProperty();
		signature.add(prop);
		OWLDescription desc = ((OWLObjectPropertyRangeAxiom)node).getRange();
		signature.addAll(visit(desc));
		axiomsToSignature.put(node,signature);
	}

	public void visit(OWLDataPropertyRangeAxiom node) throws OWLException {
		Set signature = new HashSet();
		OWLProperty prop = ((OWLDataPropertyRangeAxiom)node).getProperty();
		signature.add(prop);
		axiomsToSignature.put(node,signature);
	
	}
	
	public void visit(OWLIndividualAxiom node) throws OWLException{
		Set signature = new HashSet();
		signature.addAll(((OWLIndividualAxiom)node).getIndividuals());
		axiomsToSignature.put(node,signature);
	}

	public void visit(OWLObjectPropertyInstance node) throws OWLException {
		Set signature = new HashSet();
		signature.add(((OWLObjectPropertyInstance)node).getSubject());
		signature.add(((OWLObjectPropertyInstance)node).getObject());
		signature.add(((OWLObjectPropertyInstance)node).getProperty());
		axiomsToSignature.put(node,signature);
	}

	public void visit(OWLDataPropertyInstance node) throws OWLException {
		Set signature = new HashSet();
		signature.add(((OWLDataPropertyInstance)node).getSubject());
		//signature.add(((OWLObjectPropertyInstance)node).getObject());
		signature.add(((OWLDataPropertyInstance)node).getProperty());
		axiomsToSignature.put(node,signature);
	}

	public void visit(OWLIndividualTypeAssertion node) throws OWLException {
		Set signature = new HashSet();
		signature.add(((OWLIndividualTypeAssertion)node).getIndividual());
		OWLDescription desc = ((OWLIndividualTypeAssertion)node).getType();
		signature.addAll(visit(desc));
		axiomsToSignature.put(node,signature);
	}

}
