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
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public interface VocabularyExplanationDeposit 
{
	public String  SPACE     = "&nbsp;";
	public String  TAB       = SPACE+SPACE+SPACE+SPACE;
	public String  NL 		 = "<br>"; // new line
	
	public VocabularyExplanation explain(URI uri);
}
