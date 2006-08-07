/*
 * Created on Feb 27, 2005
 *
 */
package org.mindswap.swoop.utils.explain;

import java.net.URI;


/**
 * @author Dave
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class OWLVocabularyExplanation extends VocabularyExplanation 
{
	public static String OWL_VOCAB_NAME = "OWL Vocabulary";
	
	public OWLVocabularyExplanation(URI uri, String name, String desc, URI [] src, URI spec, String example)
	{
		super(uri, name, desc, src, spec, example, OWL_VOCAB_NAME);
	}
}
