/*
 * Created on Oct 26, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.mindswap.swoop.annotea;

import java.net.URI;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.mindswap.swoop.annotea.Annotea;
import org.mindswap.swoop.annotea.Description;
import org.semanticweb.owl.model.OWLDataFactory;
import org.semanticweb.owl.model.OWLIndividual;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.change.AddEntity;
import org.semanticweb.owl.model.change.ChangeVisitor;
import org.semanticweb.owl.model.change.OntologyChange;
import org.semanticweb.owl.model.helper.OWLBuilder;

/**
 * @author ronwalf
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class DescriptionTest extends TestCase {

	public DescriptionTest(String name) {
		super(name);
		try {
			Annotea.initializeAnnotea();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public static Test suite() {
        return new TestSuite(DescriptionTest.class);
	}
	
	protected void setUp() {
		
	}
	
	public void test_00_Anon() throws Exception {
		URI ont_uri = new URI("");
		OWLBuilder builder = new OWLBuilder();
		builder.createOntology(ont_uri, ont_uri);
		OWLOntology ontology = builder.getOntology();
		//OWLOntology annoteaOntology = Annotea.getAnnoteaOntology();
//		 create a new OWL instance
		OWLDataFactory dataFact = ontology.getOWLDataFactory();
		OWLIndividual annotInstance = dataFact.getOWLIndividual(null);
		OntologyChange change = new AddEntity(ontology, annotInstance, null);
		change.accept((ChangeVisitor) ontology);
	}
	public void test_01_buildOnotology() throws Exception {
		Description desc = new Description();
		URI[] uris = {new URI("http://example.org")};
		desc.setAnnotates(uris);
		desc.setBody("Hi there!");
		desc.setBodyType("text/plain");
		OWLOntology ontology = desc.buildOntology();
		
	}
	
	public static void main(String[] args) {
		junit.textui.TestRunner.run(suite());
	}
}
