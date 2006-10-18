/*
 * Created on Apr 7, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.mindswap.swoop.utils.owlapi;

import java.net.URI;
import java.util.regex.Pattern;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * @author ronwalf
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class QNameTest extends TestCase {
	
	protected QNameShortFormProvider qnames;
	
	protected void setUp() {
		qnames = new QNameShortFormProvider();
	}
	
	public static Test suite() {
        return new TestSuite(QNameTest.class);
	}
	
	public void shortForm(String uri, String expected) throws Exception {
		String shortform = qnames.shortForm(new URI(uri));
		assertEquals(expected, shortform);
	}
	
	public void test_boogaloo() throws Exception {
		shortForm("http://www.example.com/boogaloo/boogaloo.owl", "boogaloo:boogaloo.owl");
		shortForm("http://www.example.com/boogaloo/boogaloo.owl#Boogaloo", "boogaloo0:Boogaloo");
	}
	
	public void test_filename() throws Exception {
		shortForm("http://example.com/example.owl#example", "example:example");
	}
	
	public void test_namestart() throws Exception {
		shortForm("http://example.com/#-myclass", "example:myclass");
	}
	
	public void test_nobase() throws Exception {
		shortForm("blahblah", "a:blahblah");
	}
	
	public void test_noprefix() throws Exception {
		shortForm("/foo", "a:foo");
		assertEquals("/", qnames.getURI("a"));
	}
	
	public void test_numbers() throws Exception {
		shortForm("http://www.example.com/bar/123BaBoom", "bar:BaBoom");
		assertEquals("http://www.example.com/bar/123", qnames.getURI("bar")); 
	}
	
	
	
	public void test_wordnet() throws Exception {
		shortForm("http://xmlns.com/wordnet/1.6/Agent-3", "wordnet:Agent-3");
		assertEquals("http://xmlns.com/wordnet/1.6/", qnames.getURI("wordnet"));
	}
}
