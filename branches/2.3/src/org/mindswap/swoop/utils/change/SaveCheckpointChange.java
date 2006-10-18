/*
 * Created on Feb 27, 2005
 *
 */
package org.mindswap.swoop.utils.change;

import java.net.URI;
import java.util.Set;

import org.semanticweb.owl.model.OWLEntity;
import org.semanticweb.owl.model.OWLException;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.change.ChangeVisitor;
import org.semanticweb.owl.model.change.OntologyChange;

/**
 * @author Aditya 
 *
 */
public class SaveCheckpointChange extends OntologyChange {
	
	private URI ontologyURI;
	private URI entityURI;
	private Set workspaceURIs; // set of ontology uri's in workspace saved in checkpoint
	private String rdfCode; // RDF/XML encoding of the entity/ontology ("" for workspaces)
	private String locationURL; // actual location of the saved checkpoint file
	private String description; // textual description of the checkpoint
	private String snapShot; // typically HTML rendition using Concise Format
	private String timeStamp; // time stamp when the checkpoint was saved
	private int scope; // whether entity, ontology or workspace scope of checkpoint
	
	/**
	 * @param ontology
	 * @param cause
	 */
	public SaveCheckpointChange(int scope, 
			OWLOntology ontology,
			URI ontologyURI,
			OWLEntity entity,
			URI entityURI,
			Set wkspcURIs,
			String rdfCode,
			String locationURL,
			String description,
			String snapShot,
			String timeStamp,
			OntologyChange cause) {
		
		super(ontology, cause);
	
		this.ontologyURI = ontologyURI;
		this.entityURI = entityURI;
		this.workspaceURIs = wkspcURIs;
		this.rdfCode = rdfCode;
		this.locationURL = locationURL;
		this.description = description;
		this.snapShot = snapShot;
		this.timeStamp = timeStamp;
		this.scope = scope;
	}
	
	public void accept(ChangeVisitor visitor) throws OWLException {
		// not needed
	}

	public URI getOntologyURI() {
		return ontologyURI;
	}
	
	public URI getEntityURI() {
		return entityURI;
	}
	
	public Set workspaceURIs() {
		return this.workspaceURIs;
	}
	
	public String getRDFCode() {
		return this.rdfCode;
	}
	
	public String getLocationURL() {
		return locationURL;
	}
	
	public String getDescription() {
		return description;
	}
	
	public String getSnapShot() {
		if (snapShot==null) return "";
		else return this.snapShot;
	}
	
	public int getScope() {
		return scope;
	}
	
	public String getTimeStamp() {
		return this.timeStamp;
	}
}
