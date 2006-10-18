/*
 * Created on Oct 28, 2005
 */
package org.mindswap.swoop.utils.owlapi;

import java.io.Serializable;
import java.net.URI;
import java.util.Iterator;
import java.util.Set;

import org.mindswap.swoop.SwoopModel;
import org.semanticweb.owl.io.ShortFormProvider;
import org.semanticweb.owl.model.OWLAnnotationInstance;
import org.semanticweb.owl.model.OWLDataValue;
import org.semanticweb.owl.model.OWLNamedObject;
import org.semanticweb.owl.model.OWLOntology;

public class LabelShortFormProvider implements ShortFormProvider, Serializable{

	SwoopModel swoopModel;
	String useLanguage = "";
	
	public LabelShortFormProvider(SwoopModel swoopModel) {
		this.swoopModel = swoopModel;
		this.useLanguage = swoopModel.getUseLanguage();
	}
	
	/*
	 * This is just broken. It should not determine display string from URI,
	 * but from the OWLNamedObject from which we can get URI, annotations etc
	 */
	public String shortForm(URI uri) {
		
		try {
			// here is Bijan code :)
		    String sf = "";
		    OWLNamedObject ent = swoopModel.getEntity(swoopModel.getSelectedOntology(), uri, true ) ;
		    if (ent!=null) {
		    	// try and guess what the ontology might be from entityURI
		    	if (swoopModel.getOntologyURIs().contains(uri)) ent = swoopModel.getOntology(uri);
		    	else if (uri.toString().indexOf("#")>=0) {
		    		String ontURI = uri.toString().substring(0, uri.toString().indexOf("#"));
		    		if (swoopModel.getOntologyURIs().contains(ontURI)) {
		    			OWLOntology ont = swoopModel.getOntology(new URI(ontURI));
		    			ent = swoopModel.getEntity(ont, uri, true);
		    		}
		    	}
		    	else if (uri.toString().indexOf("/")>=0) {
		    		String ontURI = uri.toString().substring(0, uri.toString().lastIndexOf("/"));
		    		if (swoopModel.getOntologyURIs().contains(ontURI)) {
		    			OWLOntology ont = swoopModel.getOntology(new URI(ontURI));
		    			ent = swoopModel.getEntity(ont, uri, true);
		    		}
		    	}
		    }
		    // now get rdfs:label on object if it exists
		    if (ent!=null) {
			    Set anns = ent.getAnnotations();
	            if (!anns.isEmpty()) {
	               for (Iterator it = anns.iterator(); it.hasNext();) {
		               OWLAnnotationInstance oai = (OWLAnnotationInstance) it.next();
		               if (oai.getProperty().getURI().toString() == "http://www.w3.org/2000/01/rdf-schema#label") {
		               		Object o = oai.getContent();
		               		if (o instanceof OWLDataValue && !useLanguage.equals("")) {
		               			// check if lang attribute matches with useLanguage
		               			if (((OWLDataValue) o).getLang()!=null && ((OWLDataValue) o).getLang().equalsIgnoreCase(useLanguage)) {
		               				sf = ((OWLDataValue) o).getValue().toString();
		               				break; // exit loop if match found
		               			}
		               		}
		               		else sf = o.toString();
		               }		                    
	               }            	
		    	}
	           if (!sf.equals("")) return sf;		    
		    }
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
		
		// use DefaultShortFormProvider as a backup
		DefaultShortFormProvider shortForms = new DefaultShortFormProvider();
		return shortForms.shortForm(uri);
	}
	
}
