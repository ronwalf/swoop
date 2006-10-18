package org.mindswap.swoop.refactoring;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.mindswap.swoop.utils.owlapi.OWLDescriptionFinder;
import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLDataProperty;
import org.semanticweb.owl.model.OWLEntity;
import org.semanticweb.owl.model.OWLException;
import org.semanticweb.owl.model.OWLIndividual;
import org.semanticweb.owl.model.OWLObjectProperty;
import org.semanticweb.owl.model.OWLObjectQuantifiedRestriction;
import org.semanticweb.owl.model.OWLObjectValueRestriction;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLProperty;
import org.semanticweb.owl.model.OWLRestriction;
import org.semanticweb.owl.model.helper.OntologyHelper;

/**
 * @author bernardo
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class OWLPropertyFinder {
	
	public static Set getPropertiesWithRange(OWLOntology onto, OWLClass clazz) throws OWLException{
		Set references = OntologyHelper.entityUsage(onto, clazz);
		Set propSet = new HashSet();
		Set result = new HashSet();
		for (Iterator iter = references.iterator(); iter.hasNext(); ) {
			Object obj = iter.next();
			if (obj instanceof OWLObjectProperty) propSet.add(obj);
			
		}
	    for(Iterator it = propSet.iterator(); it.hasNext(); ){
	    	OWLObjectProperty prop = (OWLObjectProperty)it.next();
	    	//Set dom = reasoner.rangesOf(prop);
	    	Set dom = prop.getRanges(onto);
			if (dom.contains(clazz)){
				result.add(prop);
		
	    }
	}
		return result;
	}
		

	public static Set getPropertiesWithRangeValues(OWLOntology onto, OWLIndividual ind, Set indSet) throws OWLException{
		Set result = new HashSet();
		Iterator i = indSet.iterator();
		while(i.hasNext()){
		   OWLIndividual indiv = (OWLIndividual)i.next();
		   Map values = indiv.getObjectPropertyValues(onto);
		   Set keys = values.keySet();
		   Iterator j = keys.iterator();
		   while(j.hasNext()){
			OWLObjectProperty prop = (OWLObjectProperty)j.next();
		   	Iterator z = ((Set)values.get(prop)).iterator();
		   	while(z.hasNext()){
		   		OWLIndividual x = (OWLIndividual)z.next();
		   		if(x.equals(ind))
		   			result.add(prop); 
		   	}
		   }
		   
		}
		return result;
	}

}
