/*
 * Created on May 17, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.mindswap.swoop.utils.rdfapi;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import edu.unika.aifb.rdf.api.model.Model;
import edu.unika.aifb.rdf.api.model.ModelException;
import edu.unika.aifb.rdf.api.model.Resource;
import edu.unika.aifb.rdf.api.model.Statement;

/**
 * @author ronwalf
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class ModelChanger {

	protected Model model;
	
	public ModelChanger(Model model) {
		this.model = model;
	}
	
	public void removeSubject(Resource subject) throws ModelException {
		Set toRemove = new HashSet();
		
		Model results = model.find(subject, null, null);
		for (Iterator statementIter = results.iterator(); statementIter.hasNext();) {
			Statement statement = (Statement) statementIter.next();
			if (SwoopResourceImpl.isAnonymous(statement.object())) {
				if (model.find(null, null, statement.object()).size() <= 1) {
					toRemove.add(statement.object());
				}
			}
			model.remove(statement);
		}
		
		for (Iterator nodeIter = toRemove.iterator(); nodeIter.hasNext();) {
			Resource object = (Resource) nodeIter.next();
			removeSubject(object);
		}
		
	}
}
