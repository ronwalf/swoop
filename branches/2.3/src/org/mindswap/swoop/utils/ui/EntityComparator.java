/*
 * Created on May 15, 2004
 */
package org.mindswap.swoop.utils.ui;

import java.net.URI;
import java.util.Comparator;

import org.mindswap.swoop.utils.owlapi.DefaultShortFormProvider;
import org.semanticweb.owl.io.ShortFormProvider;
import org.semanticweb.owl.model.OWLEntity;

/**
 * @author Evren Sirin
 */
public class EntityComparator implements Comparator {
	// always sort using local names so we have more consistency
	private ShortFormProvider shortForms = new DefaultShortFormProvider();
	
	public static EntityComparator INSTANCE = new EntityComparator();
	
	/* (non-Javadoc)
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
	public int compare(Object o1, Object o2) {
		try {
			URI uri1 = ((OWLEntity) o1).getURI();
			URI uri2 = ((OWLEntity) o2).getURI();
			int cmp = shortForms.shortForm(uri1).compareToIgnoreCase(
				      shortForms.shortForm(uri2));
			// if short forms are equal then just return the comparison of
			// whole URI otherwise a:C and b:C are treated 
			if(cmp == 0)
				cmp = uri1.toString().compareToIgnoreCase(uri2.toString());
			return cmp;
		} catch (Exception ex) {
			// if something fails compare them as string
			return o1.toString().compareTo(o2.toString());
		}
	}

}
