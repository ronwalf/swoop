/*
 * Created on Mar 8, 2005
 *
 */
package org.mindswap.swoop.utils.owlapi;

import java.io.Serializable;
import java.net.URI;

/**
 * @author Aditya
 *
 * Convenience class similar to the AddImport change in the OWL API
 * except that this can be serialized easily since it only has references to 
 * the Ontology URI's and not the Ontology instances themselves.
 * (Used while saving workspaces)
 */
public class ImportChange implements Serializable {
	
	private URI ontologyURI, importedOntologyURI;
	
	public ImportChange(URI ontologyURI, URI importedOntologyURI) {
		this.ontologyURI = ontologyURI;
		this.importedOntologyURI = importedOntologyURI;
	}

	/**
	 * @return Returns the importedOntologyURI.
	 */
	public URI getImportedOntologyURI() {
		return importedOntologyURI;
	}
	/**
	 * @return Returns the ontologyURI.
	 */
	public URI getOntologyURI() {
		return ontologyURI;
	}
}
