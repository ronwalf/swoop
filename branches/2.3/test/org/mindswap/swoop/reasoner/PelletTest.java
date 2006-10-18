/*
 * Created on Oct 28, 2005
 *
 */
package org.mindswap.swoop.reasoner;

import java.io.PrintWriter;
import java.io.Writer;
import java.net.URI;
import java.net.URISyntaxException;

import org.semanticweb.owl.impl.model.OWLConnectionImpl;
import org.semanticweb.owl.io.owl_rdf.OWLRDFErrorHandler;
import org.semanticweb.owl.io.owl_rdf.OWLRDFParser;
import org.semanticweb.owl.model.OWLException;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.util.OWLConnection;
import org.semanticweb.owl.util.OWLManager;
import org.xml.sax.SAXException;

/**
 * @author Aditya
 *
 */
public class PelletTest {

	
	public static void main(String[] args) throws Exception {
		
		PelletTest pt = new PelletTest();
		OWLOntology ont = pt.loadOntology(new URI("http://www.mindswap.org/ontologies/galen.owl"));
		System.out.println("done parsing ontology");
		pt.classify(ont);		
	}
	
	private void classify(OWLOntology ont) {
		final OWLOntology onto = ont;
		new Thread() {
			public void run() {
				PelletReasoner reasoner = new PelletReasoner();
				try {
					System.out.println("starting classification");
					reasoner.setOntology(onto);
				} catch (OWLException e) {
					e.printStackTrace();
				}
			}
		}.start();
	}
	
	private OWLOntology loadOntology(URI uri) throws Exception {
		OWLRDFParser parser = new OWLRDFParser();
		parser.setOWLRDFErrorHandler(new OWLRDFErrorHandler() {
			public void owlFullConstruct(int code, String message)
					throws SAXException {
			}
			public void error(String message) throws SAXException {
				throw new SAXException(message.toString());
			}
			public void warning(String message) throws SAXException {
				System.out.println("RDFParser: " + message.toString());
			}
			public void owlFullConstruct(int code, String message, Object obj) throws SAXException {
			}
		});
    	
		OWLConnection connection = new OWLConnectionImpl();
		parser.setConnection(connection);
		return parser.parseOntology(uri);
	}
}
