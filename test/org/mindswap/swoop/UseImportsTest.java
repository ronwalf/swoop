package org.mindswap.swoop;

import java.net.URI;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.semanticweb.owl.impl.model.OWLConnectionImpl;
import org.semanticweb.owl.io.owl_rdf.OWLRDFParser;
import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLDataFactory;
import org.semanticweb.owl.model.OWLException;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.change.AddEntity;
import org.semanticweb.owl.model.change.AddImport;
import org.semanticweb.owl.model.change.AddSuperClass;
import org.semanticweb.owl.model.change.ChangeVisitor;
import org.semanticweb.owl.util.OWLConnection;

public class UseImportsTest extends TestCase {
	OWLConnection connection;
	OWLDataFactory factory;
	static URI testURI1 = URI.create("http://www.mindswap.org/2006/owl/tests/imports/ont1#");
	static URI testURI2 = URI.create("http://www.mindswap.org/2006/owl/tests/imports/ont2#");
	
	public static Test suite() {
        return new TestSuite(UseImportsTest.class);
	}
	
	public void setUp() throws OWLException {
		connection = new OWLConnectionImpl();
		factory = connection.getDataFactory();
	}
	
	public void testImportsChange() throws Exception {
		OWLOntology ont1 = connection.createOntology(testURI1, testURI1);
		OWLOntology ont2 = connection.createOntology(testURI2, testURI2);
		
		OWLClass classA = factory.getOWLClass(testURI2.resolve("#A"));
		OWLClass classB = factory.getOWLClass(testURI2.resolve("#B"));
		
		new AddEntity(ont1, classA, null).accept((ChangeVisitor) ont1);
		new AddEntity(ont2, classA, null).accept((ChangeVisitor) ont2);
		new AddEntity(ont2, classB, null).accept((ChangeVisitor) ont2);
		new AddSuperClass(ont2, classA, classB, null);
		
		
		new AddImport(ont1, ont2, null).accept((ChangeVisitor) ont1);
		assertFalse(ont1.getClasses().contains(classB));
		assertFalse(classB.getOntologies().contains(ont1));
	}
	
	public void testImportsParsing() throws Exception {
		OWLRDFParser parser = new OWLRDFParser();
		parser.setImporting(true);
		parser.setConnection(connection);
		
		OWLClass classB = factory.getOWLClass(testURI2.resolve("#B"));
		
		OWLOntology ont1 = parser.parseOntology(testURI1);
		System.out.println(ont1.getClasses().size());
		assertFalse(ont1.getClasses().contains(classB));
		assertFalse(classB.getOntologies().contains(ont1));
	}
}
