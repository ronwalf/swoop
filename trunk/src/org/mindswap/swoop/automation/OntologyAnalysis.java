/*
 * Created on Jan 1, 2006
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.mindswap.swoop.automation;

import java.util.HashMap;
import java.util.Hashtable;


/**
 * @author Dave Wang
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class OntologyAnalysis 
{
	 
	private Hashtable myBasicStats  = null;
	private HashMap myToldStats   = null;
	private HashMap myPelletStats = null;
	
	/* 
	 * properties important only when reasoner's turned on
	 */
	private boolean isSatisfiable = false; // whether ontology's concepts are all satisfiable
	private boolean isConsistent  = true;  // whether ontology is consistent (all concets are 
	                                       // satisfiable OR contains no instances of unstasfiable 
	                                       // concepts)
	
	public OntologyAnalysis( Hashtable basic, HashMap told, HashMap pellet )
	{
		myBasicStats  = basic;
		myToldStats   = told;
		myPelletStats = pellet;		
	}
	
	public Hashtable getBasicStats()
	{ return myBasicStats; }

	public HashMap getToldStats()
	{ return myToldStats; }

	public HashMap getPelletStats()
	{ return myPelletStats; }
}
