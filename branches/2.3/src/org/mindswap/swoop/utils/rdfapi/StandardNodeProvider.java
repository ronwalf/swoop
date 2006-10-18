package org.mindswap.swoop.utils.rdfapi;

import java.net.URI;
import java.util.Map;
import java.util.WeakHashMap;

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
import org.semanticweb.owl.model.OWLObjectVisitor;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLOr;
import org.semanticweb.owl.model.OWLPropertyDomainAxiom;
import org.semanticweb.owl.model.OWLSameIndividualsAxiom;
import org.semanticweb.owl.model.OWLSubClassAxiom;
import org.semanticweb.owl.model.OWLSubPropertyAxiom;
import org.semanticweb.owl.model.OWLSymmetricPropertyAxiom;
import org.semanticweb.owl.model.OWLTransitivePropertyAxiom;

import edu.unika.aifb.rdf.api.model.ModelException;
import edu.unika.aifb.rdf.api.model.RDFNode;

public class StandardNodeProvider implements OWLObjectVisitor,
		NodeProvider {

	SwoopNodeFactory nodeFactory;
	Map resources;
	
	public StandardNodeProvider() {
		nodeFactory = new NodeFactoryImpl();
		resources = new WeakHashMap();
	}
	
	public StandardNodeProvider(SwoopNodeFactory nodeFactory) {
		this.nodeFactory = nodeFactory;
		resources = new WeakHashMap();
	}
	
	public void visit(OWLAnd node) throws OWLException {
		// TODO Auto-generated method stub

	}

	public void visit(OWLAnnotationProperty node) throws OWLException {
		handle(node);
	}

	public void visit(OWLAnnotationInstance node) throws OWLException {
		// TODO Auto-generated method stub

	}

	public void visit(OWLDataValue node) throws OWLException {
		try {
			String data = null;
			if (node.getValue() != null) 
				data = node.getValue().toString();
			String dtype = null;
			if (node.getURI() != null)
				dtype = node.getURI().toString();
			resources.put(node, nodeFactory.createLiteral(data, dtype, node.getLang()));
		} catch (ModelException e) {
			throw new OWLException(e);
		}
	}

	public void visit(OWLDataType node) throws OWLException {
		handle(node);
	}

	public void visit(OWLDataEnumeration node) throws OWLException {
		// TODO Auto-generated method stub

	}

	public void visit(OWLDataAllRestriction node) throws OWLException {
		// TODO Auto-generated method stub

	}

	public void visit(OWLDataCardinalityRestriction node) throws OWLException {
		// TODO Auto-generated method stub

	}

	public void visit(OWLDataProperty node) throws OWLException {
		handle(node);
	}

	public void visit(OWLDataSomeRestriction node) throws OWLException {
		// TODO Auto-generated method stub

	}

	public void visit(OWLDataValueRestriction node) throws OWLException {
		// TODO Auto-generated method stub

	}

	public void visit(OWLDifferentIndividualsAxiom node) throws OWLException {
		// TODO Auto-generated method stub

	}

	public void visit(OWLDisjointClassesAxiom node) throws OWLException {
		// TODO Auto-generated method stub

	}

	public void visit(OWLEquivalentClassesAxiom node) throws OWLException {
		// TODO Auto-generated method stub

	}

	public void visit(OWLEquivalentPropertiesAxiom node) throws OWLException {
		// TODO Auto-generated method stub

	}

	public void visit(OWLFrame node) throws OWLException {
		// TODO Auto-generated method stub

	}

	public void visit(OWLIndividual node) throws OWLException {
		handle(node);
	}

	public void visit(OWLObjectAllRestriction node) throws OWLException {
		// TODO Auto-generated method stub

	}

	public void visit(OWLObjectCardinalityRestriction node) throws OWLException {
		// TODO Auto-generated method stub

	}

	public void visit(OWLObjectProperty node) throws OWLException {
		handle(node);
	}

	public void visit(OWLObjectSomeRestriction node) throws OWLException {
		// TODO Auto-generated method stub

	}

	public void visit(OWLObjectValueRestriction node) throws OWLException {
		// TODO Auto-generated method stub

	}

	public void visit(OWLNot node) throws OWLException {
		// TODO Auto-generated method stub

	}

	public void visit(OWLOntology node) throws OWLException {
		handle(node);
	}

	public void visit(OWLOr node) throws OWLException {
		// TODO Auto-generated method stub

	}

	public void visit(OWLClass node) throws OWLException {
		handle(node);
	}

	public void visit(OWLEnumeration node) throws OWLException {
		// TODO Auto-generated method stub

	}

	public void visit(OWLSameIndividualsAxiom node) throws OWLException {
		// TODO Auto-generated method stub

	}

	public void visit(OWLSubClassAxiom node) throws OWLException {
		// TODO Auto-generated method stub

	}

	public void visit(OWLSubPropertyAxiom node) throws OWLException {
		// TODO Auto-generated method stub

	}

	public void visit(OWLFunctionalPropertyAxiom node) throws OWLException {
		// TODO Auto-generated method stub

	}

	public void visit(OWLInverseFunctionalPropertyAxiom node)
			throws OWLException {
		// TODO Auto-generated method stub

	}

	public void visit(OWLTransitivePropertyAxiom node) throws OWLException {
		// TODO Auto-generated method stub

	}

	public void visit(OWLSymmetricPropertyAxiom node) throws OWLException {
		// TODO Auto-generated method stub

	}

	public void visit(OWLInversePropertyAxiom node) throws OWLException {
		// TODO Auto-generated method stub

	}

	public void visit(OWLPropertyDomainAxiom node) throws OWLException {
		// TODO Auto-generated method stub

	}

	public void visit(OWLObjectPropertyRangeAxiom node) throws OWLException {
		// TODO Auto-generated method stub

	}

	public void visit(OWLDataPropertyRangeAxiom node) throws OWLException {
		// TODO Auto-generated method stub

	}

	public void visit(OWLObjectPropertyInstance node) throws OWLException {
		// TODO Auto-generated method stub

	}

	public void visit(OWLDataPropertyInstance node) throws OWLException {
		// TODO Auto-generated method stub

	}

	public void visit(OWLIndividualTypeAssertion node) throws OWLException {
		// TODO Auto-generated method stub

	}

	public RDFNode getNode(OWLObject obj) throws OWLException {
		obj.accept(this);
		if (!resources.containsKey(obj)) {
			obj.accept(this);
			if (!resources.containsKey(obj)) {
				// Just make a new one up.
				resources.put(obj, nodeFactory.createResource());
			}
		}
		return (RDFNode) resources.get(obj);
	}
	
	public SwoopNodeFactory getNodeFactory() {
		return nodeFactory;
	}
	
	public void handle(OWLNamedObject obj) throws OWLException {
		URI uri = obj.getURI();
		if (uri != null) {
			try {
				resources.put(obj, nodeFactory.createResource(uri.toString()));
			} catch (ModelException e) {
				throw new OWLException(e);
			}
		}
	}

}
