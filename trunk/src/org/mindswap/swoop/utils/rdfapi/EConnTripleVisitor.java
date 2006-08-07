/*
 * Created on Apr 22, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.mindswap.swoop.utils.rdfapi;

import java.net.URI;

import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLDataProperty;
import org.semanticweb.owl.model.OWLException;
import org.semanticweb.owl.model.OWLIndividual;
import org.semanticweb.owl.model.OWLObject;
import org.semanticweb.owl.model.OWLObjectProperty;
import org.semanticweb.owl.model.OWLOntology;

import edu.unika.aifb.rdf.api.model.Model;

/**
 * @author ronwalf
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class EConnTripleVisitor extends TripleVisitor {

	/**
	 * @param ontology
	 * @throws OWLException
	 */
	public EConnTripleVisitor(OWLOntology ontology) throws OWLException {
		super(ontology);
		// TODO Auto-generated constructor stub
	}


	public EConnTripleVisitor(OWLOntology ontology, NodeProvider nodeProvider) throws OWLException {
		super(ontology, nodeProvider);
		// TODO Auto-generated constructor stub
	}


	/**
	 * @param ontology
	 * @param model
	 * @throws OWLException
	 */
	public EConnTripleVisitor(OWLOntology ontology, Model model)
			throws OWLException {
		super(ontology, model);
		// TODO Auto-generated constructor stub
	}
	
	public void addType(OWLObject node) throws OWLException {
		if (ontology.getForeignEntities().containsKey(node)) {
			if (node instanceof OWLClass)
				addType(node, owl.getForeignClass());
			else if (node instanceof OWLDataProperty) 
				addType(node, owl.getForeignDatatypeProperty());
			else if (node instanceof OWLIndividual) 
				addType(node, owl.getForeignIndividual());
			else if (node instanceof OWLObjectProperty) {
				if (((OWLObjectProperty) node).isLink())
					addType(node, owl.getForeignLinkProperty());
				else
					addType(node, owl.getForeignObjectProperty());
			}
			URI foreignURI = (URI) ontology.getForeignEntities().get(node);
			add(getResource(node), getResource(owl.getForeignOntologies()), getResource(foreignURI));
		} else if ((node instanceof OWLObjectProperty) && (((OWLObjectProperty) node).isLink())) {
			addType(node, owl.getLinkProperty());
			add(getResource(node), getResource(owl.getForeignOntologies()), 
					getResource(((OWLObjectProperty)node).getLinkTarget()));
		} else {
			super.addType(node);
		}
	}
	
}
