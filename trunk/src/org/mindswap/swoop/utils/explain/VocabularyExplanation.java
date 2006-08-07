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
 * Explains some standard vocabulary.
 * 
 */
public class VocabularyExplanation extends Explanation 
{
	protected URI mySpecificationSource = null;
	protected String myExample          = null;
	protected String myVocabName		  = null;
	
	public VocabularyExplanation(URI uri, String name, String desc, URI [] src, URI spec, String example, String vocabName)
	{
		super.myURI  = uri;
		super.myName = name;
		super.myDescription = desc;
		super.mySources     = src;
		
		this.mySpecificationSource = spec;
		this.myExample      = example;
		this.myVocabName = vocabName;
	}
	
	public URI getSpecification()
	{ return mySpecificationSource; }
	
	public String getExample()
	{ return myExample; }
	
	public String getVocabName()
	{ return myVocabName; }
}
