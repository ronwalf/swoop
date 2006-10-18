/*
 * Created on Feb 14, 2005
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.mindswap.swoop.utils.explain;

import java.net.URI;


/**
 * @author Dave
 *
 * Holds DatatypeExplanations
 * 
 */
public interface DatatypeExplanationDeposit extends ExplanationDeposit
{

	
	public DatatypeExplanation explain(URI uri);
}
