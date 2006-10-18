/*
 * Created on Feb 24, 2005
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.mindswap.swoop.utils.explain;

import java.net.URI;


/**
 * @author Dave
 *
 * Holds Explanations for anything referrable by an URI
 * 
 */
public interface ExplanationDeposit 
{
	public void add( DatatypeExplanation exp );
	public DatatypeExplanation explain(URI uri);
	public int numExplanations();
}
