/*
 * Created on Feb 23, 2005
 *
 */
package org.mindswap.swoop.utils.ui;

import java.net.URI;
import java.util.Comparator;

import org.mindswap.swoop.utils.owlapi.DefaultShortFormProvider;
import org.semanticweb.owl.io.ShortFormProvider;
import org.semanticweb.owl.model.OWLOntology;

/**
 * @author Aditya
 */
public class OntologyComparator implements Comparator {
	// always sort using local names so we have more consistency
	private ShortFormProvider shortForms = new DefaultShortFormProvider();
	
	public static OntologyComparator INSTANCE = new OntologyComparator();
	
	public int compare(Object o1, Object o2) {
		try {
			int cmp = 0;
			
			// check if they are both URI's
			if (o1 instanceof URI && o2 instanceof URI) {
				// check the fragment identifier
				String frag1 = "", frag2 = "";
				if (o1.toString().indexOf("#")>=0) frag1 = o1.toString().substring(o1.toString().indexOf("#")+1, o1.toString().length());
				else frag1 = o1.toString().substring(o1.toString().lastIndexOf("/")+1, o1.toString().length());
				if (o2.toString().indexOf("#")>=0) frag2 = o2.toString().substring(o2.toString().indexOf("#")+1, o2.toString().length());
				else frag2 = o2.toString().substring(o2.toString().lastIndexOf("/")+1, o2.toString().length());
				cmp = frag1.compareToIgnoreCase(frag2);
				
				if(cmp == 0)
					cmp = o1.toString().compareToIgnoreCase(o2.toString());
			}
			else {
				// both are instances of OWLOntology
				URI uri1 = ((OWLOntology) o1).getURI();
				URI uri2 = ((OWLOntology) o2).getURI();
				String ontName1 = shortForms.shortForm(uri1);
				if (ontName1.indexOf(":")>=0) ontName1 = ontName1.substring(ontName1.indexOf(":")+1, ontName1.length());
				String ontName2 = shortForms.shortForm(uri2);
				if (ontName2.indexOf(":")>=0) ontName1 = ontName2.substring(ontName2.indexOf(":")+1, ontName2.length());
				cmp = ontName1.compareToIgnoreCase(
					      ontName2);
				
				// if short forms are equal then just return the comparison of
				// whole URI  
				if(cmp == 0)
					cmp = uri1.toString().compareToIgnoreCase(uri2.toString());
			}
			return cmp;
		} 
		catch (Exception ex) {
			// if something fails compare them as string
			return o1.toString().compareTo(o2.toString());
		}
	}

}

