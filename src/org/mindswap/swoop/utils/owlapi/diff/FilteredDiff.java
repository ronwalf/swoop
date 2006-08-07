package org.mindswap.swoop.utils.owlapi.diff;

import java.net.URI;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.semanticweb.owl.model.OWLException;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.change.ChangeVisitorAdapter;
import org.semanticweb.owl.model.change.OntologyChange;
import org.semanticweb.owl.model.change.RemoveDataType;
import org.semanticweb.owl.model.change.RemoveEntity;
import org.semanticweb.owl.model.change.RemoveIndividualClass;

public class FilteredDiff extends ChangeVisitorAdapter {
	private boolean result;
	OWLOntology destination;
	OWLOntology source;
	OWLOntology target;
	
	private FilteredDiff(OWLOntology source, OWLOntology destination, OWLOntology target) throws OWLException {
		this.destination = destination;
		this.source = source;
		this.target = target;
	}
	
	/**
	 * Diffs two ontologies, filtering out RemoveEntity and RemoveDataType changes.
	 * @param source
	 * @param dest
	 * @param target
	 * @return Filtered list of ontology changes.
	 */
	public static List getChanges(OWLOntology source, OWLOntology dest, OWLOntology target) throws OWLException {
		List diff = OWLDiff.getChanges(source, dest, target);
		List filtered = new Vector();
		FilteredDiff fdiff = new FilteredDiff(source, dest, target);
		
		for (Iterator iter = diff.iterator(); iter.hasNext(); ) {
			OntologyChange change = (OntologyChange) iter.next();

			fdiff.result = true;
			change.accept(fdiff);
			if (fdiff.result) {
				filtered.add(change);
			}
		}
		return filtered;
	}

	/*
	 * Don't accept entity removal events (non-Javadoc)
	 * @see org.semanticweb.owl.model.change.ChangeVisitor#visit(org.semanticweb.owl.model.change.RemoveEntity)
	 */
	public void visit(RemoveEntity event) throws OWLException {
		result = false;
	}

	/*
	 * Don't accept datatype removal events (non-Javadoc)
	 * @see org.semanticweb.owl.model.change.ChangeVisitor#visit(org.semanticweb.owl.model.change.RemoveDataType)
	 */
	public void visit(RemoveDataType event) throws OWLException {
		result = false;
	}

	/*
	 * Ignore inividual's type remove if they are not in the destination ontology (non-Javadoc)
	 * @see org.semanticweb.owl.model.change.ChangeVisitor#visit(org.semanticweb.owl.model.change.RemoveIndividualClass)
	 */
	public void visit(RemoveIndividualClass event) throws OWLException {
		URI indURI;
		if (event.getIndividual().isAnonymous()) {
			indURI = event.getIndividual().getAnonId();
		} else {
			indURI = event.getIndividual().getURI();
		}
		if (destination.getIndividual(indURI) == null) {
			result = false;
		}
	}
	
}
