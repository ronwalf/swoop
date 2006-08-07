/*
 * Created on Feb 27, 2005
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
public class RDFSVocabularyExplanation extends VocabularyExplanation
{

	public static String RDFS_VOCAB_NAME = "RDFS Vocabulary";
		
	public RDFSVocabularyExplanation(URI uri, String name, String desc, URI [] src, URI spec, String example)
	{
		super(uri, name, desc, src, spec, example, RDFS_VOCAB_NAME);
	}

}
