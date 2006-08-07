/*
 * Created on Apr 8, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.mindswap.swoop.utils.rdfapi;

import edu.unika.aifb.rdf.api.model.RDFNode;
import edu.unika.aifb.rdf.api.model.Resource;

/**
 * @author ronwalf
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class SwoopResourceImpl extends edu.unika.aifb.rdf.mainmemory.ResourceImpl implements SwoopResource {
	protected boolean anonymous;
	protected static int genid = 0;
	
	public SwoopResourceImpl() {
		super(makeGenID());
		anonymous = true;
	}
	/**
	 * @param arg0
	 */
	public SwoopResourceImpl(String arg0) {
		super(arg0);
		if (arg0 == null) {
			anonymous = true;
			m_uri= makeGenID();
		} else {
			anonymous = false;
		}
			
	}
	
	private static String makeGenID() {
		return "#swoop-genid"+(genid++);
	}
	
	public boolean isAnonymous() {
		return anonymous;
	}
	
	public static boolean isAnonymous(RDFNode resource) {
		if (resource instanceof SwoopResource) 
			return ((SwoopResource) resource).isAnonymous();
		return false;
	}
	

}
