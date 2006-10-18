package org.mindswap.swoop.utils.rdfapi;

import org.semanticweb.owl.model.OWLException;
import org.semanticweb.owl.model.OWLObject;

import edu.unika.aifb.rdf.api.model.RDFNode;

public interface NodeProvider {

	RDFNode getNode(OWLObject obj) throws OWLException;
	SwoopNodeFactory getNodeFactory();
	
}
