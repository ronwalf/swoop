/*
 * Created on Apr 6, 2005
 *
 */
package org.mindswap.swoop.change;

import java.io.Serializable;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.semanticweb.owl.model.change.OntologyChange;

/**
 * @author Aditya
 *
 */
public class SwoopChange implements Serializable {

	OntologyChange change; // actual change object
	String timeStamp; // time the change was created
	String author; // author of the change
	String desc; // concise HTML description of change (auto-generated)
	URI owlObjectURI; // URI of the subject of the change (ontology/entity)
	boolean isCommitted; // is change committed or not?
	String rdfXML; // RDF/XML serialization of the change (NOT APPLICABLE FOR CHECKPOINT CHANGES)
	List extraSubjects; // suppose the change refers to more than one subject, this stores extra subjects for later reference (eg. for an ontology-scope change, we store all entities in change as extra)
	boolean isCheckpointRelated; // if change is a Save/Revert Checkpoint change
	
	// special purpose params for repository versioning
	public boolean isOnRepository = false; // if change is already committed to repository
	public boolean isRedundant = false; // if change is redundant
	public boolean isTopNode = false; // is a commit top node containing committed changes as its children
	public String comment = ""; // manual comment on change
	
	public SwoopChange() {
		
	}
	
	public SwoopChange(
			String author, 
			URI owlObjectURI, 
			OntologyChange change, 
			String time, 
			String desc,
			boolean isCommitted,
			boolean isCheckpointRelated
			) {
		
		this.author = author;
		this.owlObjectURI = owlObjectURI;
		this.change = change;
		this.timeStamp = time;
		this.desc = desc;
		this.isCommitted = isCommitted;
		this.extraSubjects = new ArrayList();
		this.isCheckpointRelated = isCheckpointRelated;
		this.rdfXML = "";
	}
	
	/**
	 * @return Returns the author.
	 */
	public String getAuthor() {
		return author;
	}
	/**
	 * @param author The author to set.
	 */
	public void setAuthor(String author) {
		this.author = author;
	}
	/**
	 * @return Returns the change.
	 */
	public OntologyChange getChange() {
		return change;
	}
	/**
	 * @param change The change to set.
	 */
	public void setChange(OntologyChange change) {
		this.change = change;
	}
	/**
	 * @return Returns the owlObjectURI.
	 */
	public URI getOwlObjectURI() {
		return owlObjectURI;
	}
	/**
	 * @param owlObjectURI The owlObjectURI to set.
	 */
	public void setOwlObjectURI(URI owlObjectURI) {
		this.owlObjectURI = owlObjectURI;
	}
	/**
	 * @return Returns the description.
	 */
	public String getDescription() {
		return desc;
	}
	/**
	 * @param desc The description to set.
	 */
	public void setDescription(String desc) {
		this.desc = desc;
	}
	/**
	 * @return Returns the timeStamp.
	 */
	public String getTimeStamp() {
		return timeStamp;
	}
	/**
	 * @param timeStamp The timeStamp to set.
	 */
	public void setTimeStamp(String timeStamp) {
		this.timeStamp = timeStamp;
	}
	
	
	/**
	 * @return Returns the isCommitted.
	 */
	public boolean isCommitted() {
		return isCommitted;
	}
	/**
	 * @param isCommitted The isCommitted to set.
	 */
	public void setCommitted(boolean isCommitted) {
		this.isCommitted = isCommitted;
	}
	
	/**
	 * @return Returns the rdfXML.
	 */
	public String getRDFXML() {
		return rdfXML;
	}
	/**
	 * @param rdfxml The rdfXML to set.
	 */
	public void setRDFXML(String rdfxml) {
		rdfXML = rdfxml;
	}
	
	
	/**
	 * @return Returns the extraSubjects.
	 */
	public List getExtraSubjects() {
		return extraSubjects;
	}
	/**
	 * @param extraSubjects The extraSubjects to set.
	 */
	public void setExtraSubjects(List extraSubjects) {
		this.extraSubjects = extraSubjects;
	}
	
	public boolean isCheckpointRelated() {
		return isCheckpointRelated;
	}
	
	public void setCheckpointRelated(boolean isOntologyScopeChange) {
		this.isCheckpointRelated = isOntologyScopeChange;
	}
	
	public String toString() {
		return this.author;
	}
	
	public Object clone() {
		SwoopChange cloneSwc = new SwoopChange(
				this.author,
				this.owlObjectURI,
				this.change,
				this.timeStamp,
				this.desc,
				this.isCommitted,
				this.isCheckpointRelated
				);
		
		if (this.extraSubjects!=null) cloneSwc.extraSubjects = this.extraSubjects;
		
		// copy the below stuff??
		cloneSwc.isOnRepository = this.isOnRepository;
		cloneSwc.isTopNode = this.isTopNode;
		cloneSwc.comment = this.comment;
		// dont copy redundant cos thats scope specific!
		return cloneSwc;
	}
}
