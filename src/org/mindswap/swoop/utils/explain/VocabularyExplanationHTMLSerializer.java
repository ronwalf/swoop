/*
 * Created on Feb 24, 2005
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
public class VocabularyExplanationHTMLSerializer extends ExplanationHTMLSerializer
{
	
	public static String getSerialization(SwoopModel swoopModel, VocabularyExplanation exp, URI entity)
	{
		if ( exp == null ) // missed a definition somewhere... urge user to send a comment to SWOOP-dev team
		{
			String header = "<FONT FACE=\""+swoopModel.getFontFace()+"\" SIZE="+swoopModel.getFontSize()+"> <b><br>";
			String body   = "<p>Oops, we must have misplaced the explanation for " + swoopModel.shortForm(entity) + "</p>" 
						  +	"<p>If you believe there should be an explanation for this entity, please bring this issue up to swoop-devel@lists.mindswap.org so we may address it accordingly. </p>";
			String footer = "</FONT>";
			return header + body + footer;
		}
		URI uri = exp.getURI();
		String header = "<FONT FACE=\""+swoopModel.getFontFace()+"\" SIZE="+swoopModel.getFontSize()+"> <b>" + exp.getVocabName() + ":" + "</b> "
			+ "<a href=\"" + uri + "\">" + swoopModel.shortForm( uri ) + "</a>";
		String description = serializeItem("Explanation: ", exp.getDescription() );
		String example     = serializeItem("Example: ", exp.getExample() );
		String spefication = serializeItem("Specification: ", exp.getSpecification() );
		String source      = serializeArray("Source(s): ", exp.getSources() );

		String footer  = "</FONT>";
		return header + description + example + source + footer;
	}
}
