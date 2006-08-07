package org.mindswap.swoop.utils.owlapi;

import java.net.URI;
import java.util.Set;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.semanticweb.owl.impl.model.OWLConnectionImpl;
import org.semanticweb.owl.io.owl_rdf.OWLRDFParser;
import org.semanticweb.owl.model.OWLException;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.util.OWLConnection;

public class AxiomCollectorTest extends TestCase {
	static URI testURI = URI.create("http://example.com/ontology#");
	static URI SWEET_URI = URI.create("http://sweet.jpl.nasa.gov/ontology/earthrealm.owl");
	static URI WINE_URI = URI.create("http://www.w3.org/TR/2004/REC-owl-guide-20040210/wine.rdf");
	public static Test suite() {
        return new TestSuite(AxiomCollectorTest.class);
	}
	
	private OWLOntology createOnt() throws OWLException {
		OWLConnection connection = new OWLConnectionImpl();
		OWLOntology ont = connection.createOntology(testURI, testURI);
		return ont;
	}
	
	public void testSweetJPL() throws OWLException {
		OWLRDFParser parser = new OWLRDFParser();
		parser.setImporting(true);
		OWLConnection connection = new OWLConnectionImpl();
		parser.setConnection(connection);
		
		OWLOntology ontology = parser.parseOntology(SWEET_URI);
		System.out.println("Starting Sweet-JPL axiomization...");
		Set axioms = AxiomCollector.axiomize(ontology);
		System.out.println("Number of axioms in Sweet-JPL: "+axioms.size());
	}
	
	public void testWine() throws OWLException {
		OWLRDFParser parser = new OWLRDFParser();
		parser.setImporting(true);
		OWLConnection connection = new OWLConnectionImpl();
		parser.setConnection(connection);
		
		OWLOntology ontology = parser.parseOntology(WINE_URI);
		System.out.println("Starting wine axiomization...");
		Set axioms = AxiomCollector.axiomize(ontology);
		System.out.println("Number of axioms in wine: "+axioms.size());
	}
}
