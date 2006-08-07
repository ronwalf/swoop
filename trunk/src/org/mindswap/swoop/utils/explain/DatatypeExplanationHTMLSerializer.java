/*
 * Created on Feb 15, 2005
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.mindswap.swoop.utils.explain;

import java.net.URI;

import org.mindswap.swoop.SwoopModel;

/**
 * @author Dave
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class DatatypeExplanationHTMLSerializer extends ExplanationHTMLSerializer
{
	
	public static String getSerialization(SwoopModel swoopModel, DatatypeExplanation exp)
	{
		URI uri = exp.getURI();
		String header = "<FONT FACE=\""+swoopModel.getFontFace()+"\" SIZE="+swoopModel.getFontSize()+"> <b>XML Schema Built-In Datatype: </b> "
			+ "<a href=\"" + uri + "\">" + swoopModel.shortForm( uri ) + "</a>";
		String description = serializeItem("Explanation: ", exp.getDescription() );
		String example     = serializeArray("Example(s): ", exp.getExamples() );
		String source      = serializeArray("Source(s): ", exp.getSources() );
		String footer  = "</FONT>";
		return header + description + example + source + footer;
	}
	

}
