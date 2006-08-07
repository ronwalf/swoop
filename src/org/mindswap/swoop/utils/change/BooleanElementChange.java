package org.mindswap.swoop.utils.change;

import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLDescription;
import org.semanticweb.owl.model.OWLException;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.change.ChangeVisitor;
import org.semanticweb.owl.model.change.OntologyChange;

/**
 * @author Aditya Kalyanpur
 *
 * New convenient type of Ontology Change object that is used to add/remove 
 * single intersection/union elements. It is a composite change that consists 
 * of two atomic changes: 
 * 1. Remove existing Intersection/Union set 
 * 2. Add new Intersection/Union set plus/minus the element 
 */
public class BooleanElementChange extends OntologyChange {

	private OWLOntology ontology;
	private OWLClass cla;
	private OWLDescription desc;
	private Class type;
	private String changeType;
	
	public BooleanElementChange(Class type, String changeType, OWLOntology ont, OWLClass cla, OWLDescription desc, OntologyChange cause) {
		super (ont, cause);
		this.type = type;
		this.ontology = ont;
		this.cla = cla;
		this.desc = desc;
		this.changeType = changeType;
	}
	
	public OWLClass getOWLClass() {
		// class that has boolean set
		return cla;
	}
	
	public OWLDescription getOWLDescription() {
		// element to be added/removed to/from boolean set
		return desc;
	}
	
	public Class getType() {
		// type of boolean set - OWLAnd or OWLOr or OWLNot
		return type;
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
