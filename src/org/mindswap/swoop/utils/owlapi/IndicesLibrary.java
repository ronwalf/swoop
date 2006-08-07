/*
 * Created on Feb 14, 2005
 *
 */
package org.mindswap.swoop.utils.owlapi;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import org.mindswap.swoop.SwoopModel;
import org.semanticweb.owl.model.OWLOntology;

/**
 * @author Aditya 
 *
 * Build a library of ontology_indices for cross-referencing information. 
 * Used in econn refactoring and search
 * 
 */
public class IndicesLibrary {

	SwoopModel swoopModel;
	private HashMap indexedLibrary;
	
	
	public IndicesLibrary(SwoopModel swoopModel) {
		this.swoopModel = swoopModel;
		indexedLibrary = new HashMap();
	}
	
	
	
	/**
	 * Compute indices for a single OWL ontology and add the indexed 
	 * information to a local library (HashMap)
	 * @param ontology
	 */
	public void computeIndices(OWLOntology ontology) {
		OntologyIndices index = new OntologyIndices(swoopModel.getDefaultReasoner());
		index.buildIndex(ontology, swoopModel.getShowImports(), false);
		indexedLibrary.put(ontology, index);
	}
	
	/** 
	 * Compute indices for a set of ontologies. Iteratively call computeIndices(..)
	 * for each ontology in the set
	 * @param ontologies
	 */
	public void computeIndices(Set ontologies) {
		for (Iterator iter = ontologies.iterator(); iter.hasNext();) {
			OWLOntology ont = (OWLOntology) iter.next();
			this.computeIndices(ont);
		}
	}
	
	/**
	 * Get the ontology index information for a single ontology 
	 * @param ontology
	 * @return
	 */
	public OntologyIndices getIndices(OWLOntology ontology) {
		return (OntologyIndices) indexedLibrary.get(ontology);
	}
}
