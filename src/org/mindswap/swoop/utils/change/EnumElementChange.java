package org.mindswap.swoop.utils.change;

import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLException;
import org.semanticweb.owl.model.OWLIndividual;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.change.ChangeVisitor;
import org.semanticweb.owl.model.change.OntologyChange;

/**
 * @author Aditya Kalyanpur
 *
 * New convenient type of Ontology Change object that is used to add/remove 
 * single enumeration elements. It is a composite change that consists 
 * of two atomic changes: 
 * 1. Remove existing Enumeration
 * 2. Add new Enumeration set plus/minus the element (Individual)
 */

public class EnumElementChange extends OntologyChange {

	private OWLOntology ontology;
	private OWLClass cla;
	private OWLIndividual ind;
	private String changeType;
	
	public EnumElementChange(String changeType, OWLOntology ont, OWLClass cla, OWLIndividual ind, OntologyChange cause) {
		super (ont, cause);
		this.ontology = ont;
		this.cla = cla;
		this.ind = ind;
		this.changeType = changeType;
	}

	public OWLClass getOWLClass() {
		// class that has enumeration
		return cla;
	}
	
	public OWLIndividual getOWLIndividual() {
		// individual to be added/removed to/from enumeration
		return ind;
	}

	public String getChangeType() {
		// type of change "Add" or "Remove"
		return changeType;
	}
	
	public void accept(ChangeVisitor visitor) throws OWLException {
		/** we don't write an accept visitor for this, 
		 *  instead handle it separately while applying changes
		 *  inside SwoopModel 
		*/	
	}
}
