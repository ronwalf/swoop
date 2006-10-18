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
import java.util.Map;
import java.util.Set;

import org.mindswap.swoop.SwoopModel;
import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLEntity;
import org.semanticweb.owl.model.OWLException;
import org.semanticweb.owl.model.OWLOntology;

/**
 * @author Evren Sirin
 *
 */
public class ClassGraphProperties implements GraphProperties {
    private final SwoopModel model;
    
    public ClassGraphProperties(SwoopModel model) {
        this.model = model;
    }

    private String getName(Object obj, boolean shortName) {
        Set set = (Set) obj;
        
        StringBuffer name = new StringBuffer();        
        for(Iterator i = set.iterator(); i.hasNext();) {
            OWLEntity cls = (OWLEntity) i.next();
            URI uri = null;
            try {
                uri = cls.getURI();
            } catch(OWLException e) {
                e.printStackTrace();
            }        

            if( uri == null )
                name.append("Anon Class");
            else if( shortName )
                name.append( model.shortForm( uri ) );
            else
                name.append( uri );                
        }
        
        return null;
    }
    
    public String getShortName(Object obj) {
        return getName( obj, true );  
    }

    public String getLongName(Object obj) {
        return getName( obj, false );  
    }

    public Collection getLinkedElements(Object obj) {
        try {
            OWLClass cls = (OWLClass) ((Set) obj).iterator().next();
            
            return model.getReasoner().subClassesOf( cls );
        } catch(OWLException e) {
            e.printStackTrace();
        }
        
        return Collections.EMPTY_SET;
    }

    public int getSize(Object obj) {
        return 1;
    }

    public String getPreferredLayout() {
        return "DAG Layout";
    }
}
