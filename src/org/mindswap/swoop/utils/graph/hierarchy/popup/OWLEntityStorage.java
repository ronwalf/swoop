/*
 * Created on Aug 29, 2005
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.mindswap.swoop.utils.graph.hierarchy.popup;

import java.util.Hashtable;

import org.semanticweb.owl.model.OWLEntity;

/**
 * @author Dave
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class OWLEntityStorage extends Hashtable 
{
	
	public static final String OWLCLASS          = "OWLClass";
	public static final String OWLDATAPROPERTY   = "OWLDataProperty";
	public static final String OWLOBJECTPROPERTY = "OWLObjectProperty";
	public static final String OWLANNOTATIONPROPERTY = "OWLANNOTATIONProperty";
	public static final String OWLINDIVIDUAL     = "OWLIndividual";
	
	public OWLEntityStorage() {
		super();
	}

	public OWLEntityStorage(int size) 
	{ super( size); }

	public void put( OWLEntity entity, String type)
	{
		super.put( entity, type );
	}
	
	public String getType( OWLEntity entity )
	{
		return (String)( super.get( entity ) );
	}
	
}
