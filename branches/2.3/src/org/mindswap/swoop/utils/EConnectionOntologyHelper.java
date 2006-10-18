/*
 * Created on Nov 10, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.mindswap.swoop.utils;

import java.net.URI;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.mindswap.swoop.SwoopModel;
import org.semanticweb.owl.model.OWLOntology;

/**
 * @author Dave Wang
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class EConnectionOntologyHelper 
{
	public static Set getEConnectedClosure( Set set, SwoopModel model, OWLOntology ontology )
	{
		if ( ontology == null )
			return set;
		Set closure = set ;
		closure.add( ontology );  // add this ontology
		Set econnected = ontology.getForeignOntologies();
		
		if ( ( econnected == null ) || ( econnected.isEmpty()) )
			return closure;
		
		for ( Iterator it = econnected.iterator(); it.hasNext(); )
		{
			URI ontURI = (URI)it.next();
			OWLOntology ont = model.getOntology( ontURI );
			// avoid cycles
			if ( !closure.contains( ont ))
				closure.addAll( EConnectionOntologyHelper.getEConnectedClosure( closure, model, ont ) );
		}
		return closure;
	}
	
}
