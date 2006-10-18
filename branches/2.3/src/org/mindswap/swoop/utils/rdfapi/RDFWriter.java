/*
 * Created on Mar 28, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.mindswap.swoop.utils.rdfapi;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Vector;

import org.mindswap.swoop.utils.owlapi.QNameShortFormProvider;
import org.semanticweb.owl.io.vocabulary.RDFVocabularyAdapter;
import org.semanticweb.owl.io.vocabulary.XMLSchemaSimpleDatatypeVocabulary;
import org.semanticweb.owl.model.OWLException;

import edu.unika.aifb.rdf.api.model.Literal;
import edu.unika.aifb.rdf.api.model.Model;
import edu.unika.aifb.rdf.api.model.ModelException;
import edu.unika.aifb.rdf.api.model.NodeFactory;
import edu.unika.aifb.rdf.api.model.RDFNode;
import edu.unika.aifb.rdf.api.model.Resource;
import edu.unika.aifb.rdf.api.model.Statement;
import edu.unika.aifb.rdf.api.util.RDFConstants;

/**
 * @author ronwalf
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class RDFWriter {
	protected URI base;
	protected Model model;
	protected XMLWriter xml;
	
	private Map bnodes;
	private Map bnodeCount;
	private Set namespaces;
	private Comparator nodeComparator;
	private NodeFactory nodeFactory;
	private RDFVocabularyAdapter rdf = RDFVocabularyAdapter.INSTANCE;
	private Set rendered;
	private QNameShortFormProvider qnames;
	private Map subjects; /* Map of subjects to property maps*/
	
	private int level;
	private int maxLevel = 1;
	private static int bnodeNumber = 0;
	
	public RDFWriter(XMLWriter xml, Model model, QNameShortFormProvider qnames) throws OWLException {
		reset();
		this.model = model;
		this.nodeComparator = new NodeComparator();
		try {
			this.nodeFactory = model.getNodeFactory();
		} catch (ModelException e) {
			throw new OWLException(e);
		}
		this.qnames = qnames;
		this.xml = xml;
	}
	
	public void addNamespacePrefix(String prefix, String namespace) {
		qnames.setMapping(prefix, namespace.toString());
	}
	
	protected void addNamespaces() throws URISyntaxException, ModelException {
		for (Iterator resourceIter = namespaces.iterator(); resourceIter.hasNext();) {
			Resource resource = (Resource) resourceIter.next();
			
			String qname = qnames.shortForm(new URI(resource.getURI()), false);
			if (qname == null) 
				continue;
			
			String prefix = qname.substring(0, qname.indexOf(":"));
			String namespace = qnames.getURI(prefix);
			xml.addNamespace(prefix, namespace);
			xml.addEntity(prefix, namespace);
		}
		
	}
	
	protected Map buildPropertyMap(Resource subject) throws ModelException {
		Map properties = new TreeMap(nodeComparator);
		
		Model propModel = model.find(subject, null, null);
		for (Iterator statementIter = propModel.iterator(); statementIter
				.hasNext();) {
			Statement statement = (Statement) statementIter.next();
			Resource property = statement.predicate();
			RDFNode object = statement.object();

			Collection objects = (Collection) properties.get(property);
			if (objects == null) {
				objects = new TreeSet(nodeComparator);
			}
			objects.add(object);
			properties.put(property, objects);
		}
		
		return properties;
	}
	
	protected void collectInformation() throws URISyntaxException, ModelException {
		subjects = new TreeMap(nodeComparator);
		for (Iterator iter = model.iterator(); iter.hasNext();) {
			Statement statement = (Statement) iter.next();
			
			Resource subject = statement.subject();
			Resource predicate = statement.predicate();
			RDFNode object = statement.object();
			
			if (subjects.get(subject) == null) {
				subjects.put(subject, buildPropertyMap(subject));
			}
			
			if (SwoopResourceImpl.isAnonymous(subject) && (bnodes.get(subject) == null)) {
					bnodes.put(subject, "b"+(bnodeNumber++));
			}
			
			
			/* Count BNodes referenced as objects */
			if (SwoopResourceImpl.isAnonymous(object)) {
				if (bnodes.get(object) == null) {
					bnodes.put(object, "b"+(bnodeNumber++));
				}
				
				Integer count = (Integer) bnodeCount.get(object);
				if (count == null) {
					count = new Integer(1);
				} else {
					count = new Integer(count.intValue() + 1);	
				}
				bnodeCount.put(object, count);
			}
			
			namespaces.add(predicate);
			if (predicate.getURI().equals(rdf.getInstanceOf())) {
				
				if (!SwoopResourceImpl.isAnonymous(object)) {
					namespaces.add(object);
				}
			}
			
			
		}
	}

	private Resource genResource(String uri) throws OWLException {
		try {
			return nodeFactory.createResource(uri);
		} catch (ModelException e) {
			throw new OWLException(e);
		}
	}
	
	protected boolean isList(RDFNode node) throws ModelException {
		if (!(node instanceof Resource)) 
			return false;
		
		
		// Node is either anonymous or rdf:Nil
		Resource resource = (Resource) node;
		if (!SwoopResourceImpl.isAnonymous(node)) {
			if (rdf.getNil().equals(resource.getURI())) {
				return true;
			}
			return false;
		}
		
		// Node is referenced exactly once.
		Integer count = (Integer) bnodeCount.get(resource);
		if ((count == null) || (count.intValue() != 1)) {
			return false;
		}
		
		Map propertyMap = (Map) subjects.get(resource);
		// Can only be two properties off of subject
		if (propertyMap == null || propertyMap.size() != 2) {
			return false;
		}
		
		// Should be only one element for rdf:first
		Set firstSet = (Set) propertyMap.get(nodeFactory.createResource(RDFConstants.RDF_FIRST));
		if (firstSet == null || firstSet.size() != 1) {
			return false;
		} 
		
		// Of that one element, it should be a resource
		for (Iterator firstIter = firstSet.iterator(); firstIter.hasNext();) {
			RDFNode first = (RDFNode) firstIter.next();
			if (!(first instanceof Resource)) {
				return false;
			}
		}
		
		// There should only be one element for rdf:rest
		Set nextSet = (Set) propertyMap.get(nodeFactory.createResource(RDFConstants.RDF_REST));
		if (nextSet == null || nextSet.size() != 1) {
			return false;
		} 
		
		RDFNode child = null;
		for (Iterator nextIter = nextSet.iterator(); nextIter.hasNext();) {
			child = (RDFNode) nextIter.next();
			break;
		}
		
		// child should not be rendered all ready.
		if (rendered.contains(child)) {
			return false;
		}
		
		// rdf:rest should be a list.
		return isList(child);
	}
	
	protected boolean propertyBNode(RDFNode value) throws IOException {
		if (SwoopResourceImpl.isAnonymous(value)) {
			xml.addAttribute(RDFVocabularyAdapter.RDF, "nodeID", (String) bnodes.get(value));
			return true;
		}
		return false;
	}
	
	protected boolean propertyList(RDFNode value) throws OWLException, IOException, URISyntaxException, ModelException {
		if (!isList(value)) {
			return false;
		}
		
		Vector elements = new Vector();
		Resource current = (Resource) value;
		while (!rdf.getNil().equals(current.getURI())) {
			rendered.add(current);
			Map propertyMap = (Map) subjects.get(current);
			Set firstSet = (Set) propertyMap.get(nodeFactory.createResource(RDFConstants.RDF_FIRST));
			for (Iterator firstIter = firstSet.iterator(); firstIter.hasNext();) {
				elements.add(firstIter.next());
			}
			Set restSet = (Set) propertyMap.get(nodeFactory.createResource(RDFConstants.RDF_REST));
			for (Iterator restIter = restSet.iterator(); restIter.hasNext();) {
				current = (Resource) restIter.next();
				break;
			}
		}
		
		// Write list
		xml.addAttribute(RDFVocabularyAdapter.RDF, "parseType", "Collection");
		for (Iterator elementIter = elements.iterator(); elementIter.hasNext();) {
			Resource element = (Resource) elementIter.next();
			if (shouldNest(element)) {
				serializeSubject(element);
			} else {
				xml.startElement(RDFVocabularyAdapter.RDF, "Description");
				if (SwoopResourceImpl.isAnonymous(element)) {
					xml.addAttribute(RDFVocabularyAdapter.RDF, "nodeID", (String)bnodes.get(element));
				} else {		
					xml.addAttribute(RDFVocabularyAdapter.RDF, "about", new URI(element.getURI()));
				}
				xml.endElement();
			}
		}
		
		return true;
	}
	
	protected boolean propertyLiteral(RDFNode value) throws ModelException, IOException, URISyntaxException {
		if (value instanceof Literal) {
			Literal valueLit = (Literal) value;
			String datatype = valueLit.getDatatype();
			String language = valueLit.getLanguage();
			if (datatype != null) {
				xml.addAttribute(RDFVocabularyAdapter.RDF, "datatype", new URI(datatype));
			} else if (language != null) {
				xml.addAttribute(null, "xml:lang", language);
			}
			xml.writeData(valueLit.getLabel());
			return true;
		}
		return false;
	}
	
	protected boolean propertyNested(RDFNode value) throws IOException, OWLException {
		if (!(value instanceof Resource) || !shouldNest(value)) { 
			return false;
		}
		serializeSubject((Resource) value);
		return true;
	}
	
	protected boolean propertyReference(RDFNode value) throws URISyntaxException, ModelException, IOException {
		if ((value instanceof Resource) && !SwoopResourceImpl.isAnonymous(value)) {
			URI valueURI = new URI(((Resource)value).getURI());
			xml.addAttribute(RDFVocabularyAdapter.RDF, "resource", valueURI);
			return true;
		}
		return false;
	}
	
	protected boolean propertyXMLLiteral(RDFNode value) {
		return false;
	}
	
	private void reset() {
		bnodes = new HashMap();
		bnodeCount = new HashMap();
		namespaces = new HashSet();
		rendered = new HashSet();
		level = 0;
	}
	
	public void serializeAll() throws IOException, OWLException {
		serializeAll(null);
	}

	public void serializeAll(String comment)  throws IOException, OWLException {
		boolean started = false;
		for (Iterator subjectIter = subjects.keySet().iterator(); subjectIter.hasNext();) {
			Resource subject = (Resource) subjectIter.next();
			if (!started && !rendered.contains(subject)) {
				if (comment != null) {
					xml.writeComment(comment);
				}
				started = true;
			}
			serializeSubject(subject);
		}
	}
	
	
	protected void serializeProperty(Resource property, RDFNode value) throws URISyntaxException, ModelException, IOException, OWLException {
		URI propertyURI = new URI(property.getURI());
		startElement(propertyURI);
		
		if (!propertyList(value)
				&& !propertyNested(value)
				&& !propertyBNode(value)
				&& !propertyReference(value)
				&& !propertyXMLLiteral(value)
				&& !propertyLiteral(value)) {
			throw new OWLException("Could not serialize property "+property+" with value "+value);
		}
		
		xml.endElement();
	}
	
	public void serializeSubject(Resource subject) throws IOException, OWLException {

		if (rendered.contains(subject) || (subjects.get(subject) == null)) {
			return;
		}
		try {
			level++;
			rendered.add(subject);
			Map propertyMap = (Map) subjects.get(subject);
			
			Collection types = (Collection) propertyMap.get(genResource(rdf
					.getInstanceOf()));
			if (types == null) {
				types = Collections.EMPTY_SET;
			}
			boolean started = false;
			for (Iterator typeIter = types.iterator(); typeIter.hasNext();) {
				RDFNode typeNode = (RDFNode) typeIter.next();
				if (typeNode instanceof Resource
						&& !SwoopResourceImpl.isAnonymous(typeNode)) {
					started = true;
					types.remove(typeNode);
					startElement(new URI(((Resource) typeNode).getURI()));
					break;
				}
			}
			if (!started) {
				xml.startElement(RDFVocabularyAdapter.RDF, "Description");
			}
			
			/* Add identifier */
			if (SwoopResourceImpl.isAnonymous(subject)) {
				String nodeid = (String) bnodes.get(subject);
				Integer count = (Integer) bnodeCount.get(subject);
				if ((nodeid != null) && (count != null)) {
					if ((count.intValue() > 1) || ((count.intValue() == 1) && (level == 1))) {
						xml.addAttribute(RDFVocabularyAdapter.RDF, "nodeID", nodeid);
					}
				}
			} else {
				xml.addAttribute(RDFVocabularyAdapter.RDF, "about", new URI(subject.getURI()));
			}
			
			/* Write short literal properties */
			for (Iterator propIter = propertyMap.keySet().iterator(); propIter.hasNext();) {
				Resource property = (Resource) propIter.next();
				URI propertyURI = new URI(property.getURI());
				Collection values = (Collection) propertyMap.get(property);
				if (values.size() > 1) {
					continue;
				}
				for (Iterator valueIter = values.iterator(); valueIter.hasNext();) {
					RDFNode value = (RDFNode) valueIter.next();
					if (value instanceof Literal) {
						Literal valueLit = (Literal) value;
						if (valueLit.getDatatype()==null 
								&& valueLit.getLanguage()==null 
								&& valueLit.toString().length() < 40) {
							
							xml.addAttribute(qnames.findPrefixURI(propertyURI), 
									qnames.findLocal(propertyURI), valueLit.toString());
							values.remove(valueLit);
							break;
						}
					}
				}
			}
			
			/* Write all properties */
			for (Iterator propIter = propertyMap.keySet().iterator(); propIter.hasNext();) {
				Resource property = (Resource) propIter.next();
				Collection values = (Collection) propertyMap.get(property);
				for (Iterator valueIter = values.iterator(); valueIter.hasNext();) {
					RDFNode value = (RDFNode) valueIter.next();
					serializeProperty(property, value);
				}
			}
			
			xml.endElement();
		} catch (URISyntaxException e) {
			throw new OWLException(e);
		} catch (ModelException e) {
			throw new OWLException(e);
		} finally {
			level--;
		}
	}
	
	public void serializeSubjects(Collection subjects) throws IOException, OWLException {
		serializeSubjects(subjects, null);
	}
	
	public void serializeSubjects(Collection subjects, String comment) throws IOException, OWLException {
		boolean started = false;
		for (Iterator subjectIter = subjects.iterator(); subjectIter.hasNext();) {
			Resource subject = (Resource) subjectIter.next();
			if (!started && !rendered.contains(subject)) {
				if (comment != null) {
					xml.writeComment(comment);
				}
				started = true;
			}
			serializeSubject(subject);
		}
	}
	
	public void setMaxLevel(int level) {
		maxLevel = level;
	}
	
	private boolean shouldNest(RDFNode node) {
		if (node instanceof Resource)
			return shouldNest((Resource) node);
		return false;
	}
	
	private boolean shouldNest(Resource resource) {
		if (rendered.contains(resource) || (subjects.get(resource) == null)) {
			return false;
		}
		
		if (SwoopResourceImpl.isAnonymous(resource)) {
			return true;
		}
		
		if (level >= maxLevel) {
			return false;
		}
		return true;
	}
	public void startDocument() throws IOException, OWLException {
		
		try {
			collectInformation();
			addNamespaces();
			xml.addEntity("xsd", XMLSchemaSimpleDatatypeVocabulary.XS);
			
			xml.startDocument();
			xml.startElement(RDFVocabularyAdapter.RDF, "RDF");
		} catch (ModelException e) {
			throw new OWLException(e);
		} catch (URISyntaxException e) {
			throw new OWLException(e);
		}
		
	}
	
	private void startElement(URI uri) throws IOException {
		xml.startElement(qnames.findPrefixURI(uri), qnames.findLocal(uri));
	}
	
	public void endDocument() throws OWLException {
		try {
			xml.endDocument();
		} catch (Exception e) {
			throw new OWLException(e);
		}
	}

	/**
	 * @param base
	 */
	public void setBase(URI base) {
		this.base = base;
		nodeComparator = new NodeComparator(base.toString());
		xml.setBase(base);
	}
	
	
}
