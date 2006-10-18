package org.mindswap.swoop.utils.owlapi.diff;

import java.util.List;
import java.util.Vector;

import org.semanticweb.owl.model.OWLException;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.change.OntologyChange;

public class OWLDiff {
	
	/**
	 * Returns a list differences betwen the source and destination ontologies, 
	 * as applied to target ontology.
	 * 
	 * @param source - Source ontology
	 * @param destination - Destination ontology
	 * @param target - Ontology to form changes against.
	 * @return List of changes which can be applied to the target.
	 * @throws OWLException
	 */
	public static List getChanges(OWLOntology source, OWLOntology destination, OWLOntology target) throws OWLException {
		
		List changeList = NegativeDiff.getChanges(source, destination, target);
		List posChangeList = ChangePolarity.invert(NegativeDiff.getChanges(destination, source, target));
		changeList.addAll(posChangeList);
		return changeList;
	}
}
