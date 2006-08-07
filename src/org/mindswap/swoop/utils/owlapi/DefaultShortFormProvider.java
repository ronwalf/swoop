/*
 * Created on May 7, 2004
 */
package org.mindswap.swoop.utils.owlapi;

import java.net.URI;
import java.util.Hashtable;
import java.util.Iterator;

import org.semanticweb.owl.io.ShortFormProvider;
import org.semanticweb.owl.io.vocabulary.OWLVocabularyAdapter;
import org.semanticweb.owl.io.vocabulary.RDFSVocabularyAdapter;
import org.semanticweb.owl.io.vocabulary.RDFVocabularyAdapter;
import org.semanticweb.owl.io.vocabulary.XMLSchemaSimpleDatatypeVocabulary;

/**
 * @author Evren Sirin
 */
public class DefaultShortFormProvider implements ShortFormProvider {

	Hashtable alwaysOn;
	
	/* (non-Javadoc)
	 * @see org.mindswap.swoop.ShortFormProvider#shortForm(java.net.URI)
	 */
	
	public DefaultShortFormProvider() {
		
		// AK: adding namespaces that should always be displayed 
		alwaysOn = new Hashtable();
		alwaysOn.put(OWLVocabularyAdapter.OWL, "owl");
		alwaysOn.put(RDFSVocabularyAdapter.RDFS, "rdfs");
		alwaysOn.put(RDFVocabularyAdapter.RDF, "rdf");
		alwaysOn.put(XMLSchemaSimpleDatatypeVocabulary.XS, "xsd");
	}
	
	 public String shortForm(URI uri) {
		String label = uri.toString();
		String label2 = uri.toString();
		
		// AK: adding namespaces that should always be displayed
		String prefix = "";
		Iterator iter = alwaysOn.keySet().iterator();
		while (iter.hasNext()) {
			String alwaysOnURI = iter.next().toString();
			if (label.startsWith(alwaysOnURI)) prefix = alwaysOn.get(alwaysOnURI).toString()+":";
		}
		
		if (label.endsWith("#")) {
			label = label.substring(0, label.length()-1);
			label2 = label;
		}
		
		// urn support
		if ((label.startsWith("urn")) || ((label.indexOf("#")==-1) && (label.indexOf("/")==-1))) {
			label = label.substring(label.lastIndexOf(":")+1, label.length());
			return label;
		}
		
		if (label.indexOf("#")>=0) {
			label = label.substring(label.indexOf("#")+1, label.length());
		}
		else {
			label2= label.substring(0, label.lastIndexOf("/"));	
			label = label.substring(label.lastIndexOf("/")+1, label.length());
		    if (label.length()==0) {
		        label2 = label2.substring(label2.lastIndexOf("/")+1, label2.length());    	
		    	return(prefix+label2);
		    }
		}
		return prefix+label;
	}
}
