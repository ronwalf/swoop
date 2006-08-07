package org.mindswap.swoop.reasoner;

import java.util.Set;

import org.semanticweb.owl.inference.OWLReasoner;
import org.semanticweb.owl.model.OWLException;
import org.semanticweb.owl.model.OWLObjectProperty;
import org.semanticweb.owl.model.OWLProperty;

/**
 * @author Evren Sirin
 */
public interface OWLPropertyReasoner extends OWLReasoner {
		/** Returns the collection of most specific superproperties
		 of the given property. The result of this will be a set of
		 sets, where each set in the collection represents an
		 equivalence propass. */

		public Set superPropertiesOf( OWLProperty prop ) throws OWLException;

		/** Returns the collection of all superproperties of the given
		 property. The result of this will be a set of sets, where
		 each set in the collection represents an equivalence propass. */

		public Set ancestorPropertiesOf( OWLProperty prop ) throws OWLException;

		/** Returns the collection of most general subproperties
		 of the given property. The result of this will be a set of
		 sets, where each set in the collection represents an
		 equivalence propass. */

		public Set subPropertiesOf( OWLProperty prop ) throws OWLException;

		/** Returns the collection of all subproperties of the given
		 property. The result of this will be a set of sets, where
		 each set in the collection represents an equivalence propass. */

		public Set descendantPropertiesOf( OWLProperty prop ) throws OWLException;

		/** Returns the collection of properties which are equivalent
		 * to the given property. */

		public Set equivalentPropertiesOf( OWLProperty prop ) throws OWLException;	

		/** 
		 * Returns the collection of properties which are inverse of
		 * to the given property. The result of this will be a set of sets, where
		 * each set in the collection represents an equivalence propass. 
		 */

		public Set inversePropertiesOf( OWLObjectProperty prop ) throws OWLException;	
		
		/**
		 * getRanges
		 * 
		 * @param property
		 * @return
		 */
		public Set rangesOf(OWLProperty prop) throws OWLException; 
		
		/**
		 * getRanges
		 * 
		 * @param property
		 * @return
		 */
		public Set domainsOf(OWLProperty prop) throws OWLException; 		
}
