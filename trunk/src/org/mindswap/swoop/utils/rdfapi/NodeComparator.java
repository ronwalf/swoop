/*
 * Created on Apr 13, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.mindswap.swoop.utils.rdfapi;

import java.io.Serializable;
import java.util.Comparator;

import edu.unika.aifb.rdf.api.model.Literal;
import edu.unika.aifb.rdf.api.model.ModelException;
import edu.unika.aifb.rdf.api.model.RDFNode;
import edu.unika.aifb.rdf.api.model.Resource;

/**
 * @author ronwalf
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class NodeComparator implements Comparator, Serializable {

	protected String base;
	
	public NodeComparator(String base) {
		this.base = base;
	}
	/**
	 * 
	 */
	public NodeComparator() {
		this.base = "";
	}
	/* (non-Javadoc)
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
	public int compare(Object arg0, Object arg1) {
		if (arg0 instanceof Literal) {
			if (arg1 instanceof Literal) {
				return arg0.toString().compareTo(arg1.toString());
			} else if (arg1 instanceof Resource) {
				return -1;
			}
		} else if (arg0 instanceof Resource) {
			
			if (arg1 instanceof Resource) {
				Resource resource0 = (Resource) arg0; 
				Resource resource1 = (Resource) arg1;
				if ((SwoopResourceImpl.isAnonymous(resource0)) 
						&& !(SwoopResourceImpl.isAnonymous(resource1))) {
					return 1;
				} else if (!(SwoopResourceImpl.isAnonymous(resource0)) 
						&& (SwoopResourceImpl.isAnonymous(resource1))) {
					return -1;
				}
				try {
					String uri0 = resource0.getURI();
					String uri1 = resource1.getURI();
					
					if (uri0.startsWith(base) && !uri1.startsWith(base)) {
						return -1;
					} else if (!uri0.startsWith(base) && uri1.startsWith(base)) {
						return 1;
					}
					return resource0.getURI().compareTo(resource1.getURI());
				} catch (ModelException e) {
					throw new RuntimeException(e);
				}
			} else if (arg1 instanceof Literal) {
				return 1;
			}
		}
		throw new ClassCastException("Comparing types "+arg0.getClass()+" and "+arg1.getClass());
	}

}
