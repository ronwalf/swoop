/*
 * Created on Apr 21, 2005
 */
package org.mindswap.swoop.utils.graph;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.mindswap.swoop.SwoopModel;
import org.mindswap.swoop.utils.owlapi.DefaultShortFormProvider;
import org.semanticweb.owl.io.ShortFormProvider;
import org.semanticweb.owl.model.OWLException;
import org.semanticweb.owl.model.OWLOntology;

/**
 * @author Evren Sirin
 *
 */
public class OntologyGraphProperties implements GraphProperties {
    private final ShortFormProvider shortForms = new DefaultShortFormProvider();

    private final SwoopModel model;
    
    public OntologyGraphProperties(SwoopModel model) {
        this.model = model;
    }

    private URI getURI(Object obj) {
        OWLOntology ont = (OWLOntology) obj;
        
        try {
            URI uri = ont.getURI();

            return uri;
        } catch(OWLException e) {
            e.printStackTrace();
        }        
        
        return null;
    }
    
    public String getShortName(Object obj) {
        URI uri = getURI( obj );
        
        return (uri == null) ? "" : shortForms.shortForm( uri );  
    }

    public String getLongName(Object obj) {
        URI uri = getURI( obj );
        
        return (uri == null) ? "" : uri.toString();    
    }

    public Collection getLinkedElements(Object obj) {
        try {
            OWLOntology ont = (OWLOntology) obj;
            
            boolean notEconn = ont.getForeignOntologies().isEmpty();
                
            if( notEconn )
                return ont.getIncludedOntologies();
            
            List onts = new ArrayList();
            
            Collection linkedOnts = ont.getForeignOntologies();
            int count = 0;
            for(Iterator i = linkedOnts.iterator(); i.hasNext();) 
            {
                URI uri = (URI) i.next();
                OWLOntology linkedOnt = model.getOntology( uri );               
               
                if ( (linkedOnt == null) || (linkedOnt.equals(ont)))
                	continue;
            	onts.add(linkedOnt);
            	//System.out.println( count + "has partition: "+ont.getURI().toString() );
            	count++;
            }
            //System.out.println("size of onts: " + onts.size() + " for [" + ont.getURI().toString() + "]");
            return onts;
        } catch(OWLException e) {
            e.printStackTrace();
        }
        
        return Collections.EMPTY_SET;
    }

    public int getSize(Object obj) {
        OWLOntology ont = (OWLOntology) obj;
        int numEntities = 0;
        
        try {
            int numberOfClasses = ont.getClasses().size();
            int numberOfDatatypeProperties = ont.getDataProperties().size();
            int numberOfObjectProperties = ont.getObjectProperties().size();
            int numberOfIndividuals = ont.getIndividuals().size();
            
            numEntities = 
                numberOfClasses + 
                numberOfDatatypeProperties + 
                numberOfObjectProperties + 
                numberOfIndividuals;                
        } catch(OWLException e) {
            e.printStackTrace();
        }
        
        return numEntities;
    }

    public String getPreferredLayout() {
        return "Spring Layout";
    }
}
