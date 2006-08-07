/*
 * Created on Mar 1, 2005
 *
 */
package org.mindswap.swoop.utils.change;

import org.mindswap.swoop.change.ChangeLog;
import org.semanticweb.owl.model.OWLException;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.change.ChangeVisitor;
import org.semanticweb.owl.model.change.OntologyChange;

/**
 * @author Aditya
 *
 */
public class RevertCheckpointChange extends OntologyChange {

	private SaveCheckpointChange change;
	private String description;
	private String timeStamp;
	
	public RevertCheckpointChange(OWLOntology ontology, SaveCheckpointChange change, String timeStamp, OntologyChange cause) {
		super(ontology, cause);
		this.change = change;
		this.timeStamp = timeStamp;
		parseChange();
	}

	public void accept(ChangeVisitor visitor) throws OWLException {
		// not needed
	}
	
	public SaveCheckpointChange getCheckpointChange() {
		return change;
	}
	
	public String getDescription() {
		return description;
	}

	public String getTimeStamp() {
		return timeStamp;
	}
	
	private void parseChange() {
		
		// get scope of checkpoint
		String scope = "Entity";
		if (change.getScope()==ChangeLog.ONTOLOGY_SCOPE) scope = "Ontology";
		if (change.getScope()==ChangeLog.WORKSPACE_SCOPE) scope = "Workspace";
		
		// print description of checkpoint
		description = "[[[ <b>Reverted to "+scope+" Checkpoint</b> ";
		// create a description describing the checkpoint reverted back to
		description += " <b>created</b> at: "+ change.getTimeStamp();
		description += " with <b>description</b>: "+change.getDescription();
		if (!change.getLocationURL().equals("")) description += " at <b>location</b>: "+change.getLocationURL();
		description += " ]]]<br>";
	}
}
