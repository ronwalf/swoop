/*
 * Created on May 31, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.mindswap.swoop.utils.owlapi;

import java.io.PrintWriter;
import java.io.Writer;
import java.net.URI;

import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.util.OWLConnection;
import org.semanticweb.owl.util.OWLManager;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * @author ronwalf
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class CorrectedRDFRendererTest extends TestCase {
	protected CorrectedRDFRenderer renderer;
	
	public void setUp() {
		renderer = new CorrectedRDFRenderer();
	}
	
	public static Test suite() {
        return new TestSuite(CorrectedRDFRendererTest.class);
	}
	
	public void test_emptyOntology() throws Exception {
		OWLConnection connection = OWLManager.getOWLConnection();
		OWLOntology ontology = connection.createOntology(new URI(""), new URI(""));
		Writer pw = new PrintWriter(System.out);
		renderer.renderOntology(ontology, pw);
		pw.close();
	}
}
