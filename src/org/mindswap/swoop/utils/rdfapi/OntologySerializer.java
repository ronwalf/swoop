/*
 * Created on Apr 11, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.mindswap.swoop.utils.rdfapi;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import org.mindswap.swoop.utils.owlapi.QNameShortFormProvider;
import org.semanticweb.owl.io.vocabulary.OWLVocabularyAdapter;
import org.semanticweb.owl.model.OWLException;
import org.xml.sax.InputSource;

import edu.unika.aifb.rdf.api.model.Model;
import edu.unika.aifb.rdf.api.model.ModelException;
import edu.unika.aifb.rdf.api.model.NodeFactory;
import edu.unika.aifb.rdf.api.model.Resource;
import edu.unika.aifb.rdf.api.model.Statement;
import edu.unika.aifb.rdf.api.syntax.RDFParser;
import edu.unika.aifb.rdf.api.util.ModelConsumer;
import edu.unika.aifb.rdf.api.util.RDFManager;

/**
 * @author ronwalf
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class OntologySerializer implements RDFSerializer {
	
	
	protected Model model;
	protected NodeFactory nodeFactory;
	protected OWLVocabularyAdapter owl = OWLVocabularyAdapter.INSTANCE;
	protected QNameShortFormProvider qnames;
	protected RDFWriter rdfWriter;
	
	protected URI base;
	protected Resource baseOntology;
	protected NodeComparator nodeComparator;
	
	public OntologySerializer(Resource baseOntology) {
		qnames = new QNameShortFormProvider();
		this.baseOntology = baseOntology;
		try {
			setBase(new URI(baseOntology.getURI()));
		} catch (URISyntaxException e) {
			// Don't care
		} catch (ModelException e) {
			// Don't care
		}
	}
	
	public OntologySerializer(QNameShortFormProvider qnames) {
		this.qnames = qnames;
	}
	
	protected void gatherEntities(XMLWriter xml) throws URISyntaxException, ModelException {
		
		Model ontModel = model.find(null, 
				nodeFactory.createResource(owl.getInstanceOf()), 
				nodeFactory.createResource(owl.getOntology()));
		for (Iterator ontIter = ontModel.iterator(); ontIter.hasNext();) {
			Statement statement = (Statement) ontIter.next();
			Resource ontologyResource = statement.subject();
			if (!SwoopResourceImpl.isAnonymous(ontologyResource)) {
				String prefix = qnames.getPrefix(ontologyResource.getURI());
				if (prefix == null) {
					URI ontURI = new URI(ontologyResource.getURI());
					URI testName = ontURI.resolve("#none");
					
					qnames.shortForm(testName);
					String prefixURI = qnames.findPrefixURI(testName);
					prefix = qnames.getPrefix(prefixURI);
				}
				if (prefix != null) {
					xml.addEntity(prefix, ontologyResource.getURI());
				}
			}
		}
	}
	
	/**
	 * @param xml
	 * @param model
	 */
	public void serialize(XMLWriter xml, Model model) throws IOException, OWLException {
		this.model = model;
		try {
			this.nodeFactory = model.getNodeFactory();
			gatherEntities(xml);
		} catch (ModelException e) {
			throw new OWLException(e);
		} catch (URISyntaxException e) {
		}
		
		rdfWriter = new RDFWriter(xml, model, qnames);
		if (base != null) {
			rdfWriter.setBase(base);
		}
		
		rdfWriter.startDocument();
		rdfWriter.setMaxLevel(10);
		serializeType("Ontology Information", owl.getOntology());
		rdfWriter.setMaxLevel(1);
		serializeType("Classes", owl.getClass_());
		serializeType("Datatypes", owl.getDatatype());
		serializeType("Annotation Properties", owl.getAnnotationProperty());
		serializeType("Datatype Properties", owl.getDatatypeProperty());
		serializeType("Object Properties", owl.getObjectProperty());
		rdfWriter.serializeAll("Instances");
		
		rdfWriter.endDocument();
	}

	/**
	 * @param class_
	 */
	protected void serializeType(String description, String class_) throws IOException, OWLException {
		//System.out.println("Serializing type "+class_);
		Set subjects = new TreeSet(nodeComparator);
		boolean started = false;
		try {
			Model typeModel = model.find(null, 
					nodeFactory.createResource(owl.getInstanceOf()), 
					nodeFactory.createResource(class_));
			
			for (Iterator statementIter = typeModel.iterator(); statementIter.hasNext();) {
				Statement statement = (Statement) statementIter.next();
				subjects.add(statement.subject());
			}
			//System.out.println("Serializing "+subjects);
			rdfWriter.serializeSubjects(subjects, description);
			
		} catch (ModelException e) {
			throw new OWLException(e);
		}
	}
	
	/**
	 * @param logicalURI
	 */
	public void setBase(URI logicalURI) {
		base = logicalURI;
		nodeComparator = new NodeComparator(base.toString());
	}

}
