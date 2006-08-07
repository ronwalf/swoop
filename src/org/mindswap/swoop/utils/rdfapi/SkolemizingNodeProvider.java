package org.mindswap.swoop.utils.rdfapi;

import org.semanticweb.owl.model.OWLException;
import org.semanticweb.owl.model.OWLIndividual;

import edu.unika.aifb.rdf.api.model.ModelException;

public class SkolemizingNodeProvider extends StandardNodeProvider {

	public SkolemizingNodeProvider() {
		super();
	}

	public SkolemizingNodeProvider(SwoopNodeFactory nodeFactory) {
		super(nodeFactory);
	}

	public void visit(OWLIndividual node) throws OWLException {
		if (node.isAnonymous()) {
			try {
				resources.put(node, nodeFactory.createResource(node.getAnonId().toString()));
			} catch (ModelException e) {
				throw new OWLException(e);
			}
		} else {
			super.visit(node);
		}
	}
}
