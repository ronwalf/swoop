/*
 * Created on Mar 31, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.mindswap.swoop.utils.rdfapi;

import java.io.IOException;
import java.io.Writer;
import java.net.URI;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.TreeSet;
import java.util.Vector;

import org.semanticweb.owl.io.vocabulary.OWLVocabularyAdapter;
import org.semanticweb.owl.io.vocabulary.XMLSchemaSimpleDatatypeVocabulary;
import org.semanticweb.owl.model.OWLAnd;
import org.semanticweb.owl.model.OWLAnnotationInstance;
import org.semanticweb.owl.model.OWLAnnotationProperty;
import org.semanticweb.owl.model.OWLBooleanDescription;
import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLClassAxiom;
import org.semanticweb.owl.model.OWLDataAllRestriction;
import org.semanticweb.owl.model.OWLDataCardinalityRestriction;
import org.semanticweb.owl.model.OWLDataEnumeration;
import org.semanticweb.owl.model.OWLDataProperty;
import org.semanticweb.owl.model.OWLDataPropertyInstance;
import org.semanticweb.owl.model.OWLDataPropertyRangeAxiom;
import org.semanticweb.owl.model.OWLDataSomeRestriction;
import org.semanticweb.owl.model.OWLDataType;
import org.semanticweb.owl.model.OWLDataValue;
import org.semanticweb.owl.model.OWLDataValueRestriction;
import org.semanticweb.owl.model.OWLDescription;
import org.semanticweb.owl.model.OWLDifferentIndividualsAxiom;
import org.semanticweb.owl.model.OWLDisjointClassesAxiom;
import org.semanticweb.owl.model.OWLEnumeration;
import org.semanticweb.owl.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owl.model.OWLEquivalentPropertiesAxiom;
import org.semanticweb.owl.model.OWLException;
import org.semanticweb.owl.model.OWLFrame;
import org.semanticweb.owl.model.OWLFunctionalPropertyAxiom;
import org.semanticweb.owl.model.OWLIndividual;
import org.semanticweb.owl.model.OWLIndividualAxiom;
import org.semanticweb.owl.model.OWLIndividualTypeAssertion;
import org.semanticweb.owl.model.OWLInverseFunctionalPropertyAxiom;
import org.semanticweb.owl.model.OWLInversePropertyAxiom;
import org.semanticweb.owl.model.OWLNamedObject;
import org.semanticweb.owl.model.OWLNot;
import org.semanticweb.owl.model.OWLObject;
import org.semanticweb.owl.model.OWLObjectAllRestriction;
import org.semanticweb.owl.model.OWLObjectCardinalityRestriction;
import org.semanticweb.owl.model.OWLObjectProperty;
import org.semanticweb.owl.model.OWLObjectPropertyInstance;
import org.semanticweb.owl.model.OWLObjectPropertyRangeAxiom;
import org.semanticweb.owl.model.OWLObjectSomeRestriction;
import org.semanticweb.owl.model.OWLObjectValueRestriction;
import org.semanticweb.owl.model.OWLObjectVisitor;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLOr;
import org.semanticweb.owl.model.OWLProperty;
import org.semanticweb.owl.model.OWLPropertyAxiom;
import org.semanticweb.owl.model.OWLPropertyAxiomVisitor;
import org.semanticweb.owl.model.OWLPropertyDomainAxiom;
import org.semanticweb.owl.model.OWLRestriction;
import org.semanticweb.owl.model.OWLSameIndividualsAxiom;
import org.semanticweb.owl.model.OWLSubClassAxiom;
import org.semanticweb.owl.model.OWLSubPropertyAxiom;
import org.semanticweb.owl.model.OWLSymmetricPropertyAxiom;
import org.semanticweb.owl.model.OWLTransitivePropertyAxiom;
import org.semanticweb.owl.model.helper.OWLPropertyAxiomVisitorAdapter;

import edu.unika.aifb.rdf.api.model.Model;
import edu.unika.aifb.rdf.api.model.ModelException;
import edu.unika.aifb.rdf.api.model.ModelWarning;
import edu.unika.aifb.rdf.api.model.RDFNode;
import edu.unika.aifb.rdf.api.model.Statement;
import edu.unika.aifb.rdf.api.util.RDFConstants;
import edu.unika.aifb.rdf.mainmemory.TransactionableModelImpl;

/**
 * @author ronwalf
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class TripleVisitor implements OWLObjectVisitor {

	protected Model model;
	protected SwoopNodeFactory nodeFactory;
	protected NodeProvider nodeProvider;
	protected OWLOntology ontology;
	protected OWLVocabularyAdapter owl = OWLVocabularyAdapter.INSTANCE;
	protected XMLSchemaSimpleDatatypeVocabulary xsd = XMLSchemaSimpleDatatypeVocabulary.INSTANCE;
	//protected QNameShortFormProvider qnames = new QNameShortFormProvider();
	//protected Set rendered;
	protected Set rendered;
	protected Map resources;
	protected Set serialized;
	
	protected OWLDescription currentClass;
	protected OWLProperty currentProperty;
	protected boolean replaceParent;
	protected Stack currentStack;
	protected Stack objectStack;
	
	
	
	public TripleVisitor(OWLOntology ontology) throws OWLException {
		this.ontology = ontology;
		nodeProvider = new StandardNodeProvider();
		createModel(ontology.getURI().toString());
		reset();
	}
	
	
	public TripleVisitor(OWLOntology ontology, NodeProvider nodeProvider) throws OWLException {
		this.ontology = ontology;
		this.nodeProvider = nodeProvider;
		createModel(ontology.getURI().toString());
		reset();
	}
	
	public TripleVisitor(OWLOntology ontology, Model model) throws OWLException {
		this.ontology = ontology;
		this.model = model;
		try {
			this.nodeFactory = (SwoopNodeFactory) model.getNodeFactory();
			nodeProvider = new StandardNodeProvider(nodeFactory);
		} catch (ModelException e) {
			throw new OWLException(e);
		}
		reset();
	}
	
	protected void add(OWLNamedObject subject, OWLProperty property, OWLNamedObject value) throws OWLException {
		add(getResource(subject), getResource(property), getResource(value));
	}
	
	protected void add(OWLNamedObject subject, OWLProperty property, OWLDataValue value) throws OWLException {
		add(getResource(subject), getResource(property), getRDFNode(value));
	}
	
	protected void add(SwoopResource subject, SwoopResource predicate, RDFNode object) throws OWLException {
		try {
			Statement statement = nodeFactory.createStatement(subject, predicate, object);
			model.add(statement);
		} catch (ModelWarning e) {
			// Do nothing
		} catch (ModelException e) {
			throw new OWLException(e);
		}
	}
	
	protected void addAnnotations(OWLObject obj) throws OWLException {
		for (Iterator annotationIter = obj.getAnnotations(ontology).iterator(); annotationIter.hasNext();) {
			OWLAnnotationInstance annotation = (OWLAnnotationInstance) annotationIter.next();
			
			annotation.accept(this);
		}
	}
	
	protected void addEquivalences(String equivalenceProperty, Collection items) throws OWLException {
		SwoopResource equivalence = getResource(equivalenceProperty);
		SwoopResource center = null;
		if ((currentObject() != null) && items.contains(currentObject())) {
			center = getResource(currentObject());
		}
		for (Iterator itemIter = items.iterator(); itemIter.hasNext();) {
			SwoopResource item = getResource((OWLObject) itemIter.next());
			if (center == null) {
				center = item;
			}
			
			if (center != item) {
				add(center, equivalence, item);
			}
		}
	}
	
	protected SwoopResource addList(Collection items) throws OWLException {
		if (items.size() == 0) {
			return getResource(owl.getNil());
		}
		
		//Sort changes
		Collection list = new TreeSet(new NodeComparator(ontology.getURI().toString()));
		for (Iterator objectIter = items.iterator(); objectIter.hasNext();) {
			OWLObject element = (OWLObject) objectIter.next();
			list.add(getRDFNode(element));
		}
		
		SwoopResource start = (SwoopResource) nodeFactory.createResource();
		SwoopResource current = null;
		for (Iterator objectIter = list.iterator(); objectIter.hasNext();) {
			RDFNode element = (RDFNode) objectIter.next();
			
			if (current == null) {
				current = start;
			} else {
				SwoopResource previous = current;
				current = (SwoopResource) nodeFactory.createResource();
				add(previous, getResource(RDFConstants.RDF_REST), current);
			}
			add(current, getResource(RDFConstants.RDF_FIRST), element);
		}
		add(current, getResource(RDFConstants.RDF_REST), getResource(owl.getNil()));
		return start;
	}

	
//	protected void addNamespace(SwoopResource uri) throws ModelException {
//		
//		try {
//			qnames.shortForm(new URI(uri.getURI()));
//		} catch (URISyntaxException e) {
//			e.printStackTrace();
//		} catch (ModelException e) {
//			e.printStackTrace();
//		}
//	}
	
//	public void addNamespace(String prefix, String namespace) {
//		qnames.setMapping(prefix, namespace);
//	}
	
	protected void addProperties(SwoopResource node, Map properties) throws OWLException {
		for (Iterator propIter = properties.keySet().iterator(); propIter.hasNext();) {
			String property = (String) propIter.next();
			Set values = (Set) properties.get(property);
			for (Iterator valueIter = values.iterator(); valueIter.hasNext();) {
				OWLObject value = (OWLObject) valueIter.next();
				add(node, getResource(property), getRDFNode(value));
			}
		}
	}
	
	/**
	 * Adds given property from each individual to the rest unless currentObject()
	 * is not null and is contained in 'individuals'.  In this case, it adds
	 * the prop link from currentObject() to the rest of the individuals.
	 * @param prop
	 * @param individuals
	 */
	
	protected void addSymmetric(SwoopResource prop, Collection individuals) throws OWLException {
		SwoopResource center = null;
		if (currentObject() != null && individuals.contains(currentObject())) {
			center = getResource(currentObject());
		}
		
		for (Iterator objectIter = individuals.iterator(); objectIter.hasNext();) {
			SwoopResource object = getResource((OWLObject)objectIter.next());
			
			if ((center != null)){
				if (!center.equals(object)) {
					add(center, prop, object);
				}
			} else {
				for (Iterator subjectIter = individuals.iterator(); subjectIter.hasNext();) {
					SwoopResource subject = getResource((OWLObject) subjectIter.next());

					if (!subject.equals(object)) {
						add(subject, prop, object);
					}
				}
			}
		}
	}
	
	protected void addType(OWLObject obj, String type) throws OWLException {
		add(getResource(obj), getResource(owl.getInstanceOf()), getResource(type));
	}
	
	protected void addType(OWLObject obj) throws OWLException {
		if (obj instanceof OWLClass) {
			OWLClass class_ = (OWLClass)obj;
			if ((class_.getURI() == null) || !owl.getThing().equals(class_.getURI().toString())) {
				addType(obj, owl.getClass_());
			}
		}
		if (obj instanceof OWLBooleanDescription) {
			addType(obj, owl.getClass_());
		} else if (obj instanceof OWLRestriction) {
			addType(obj, owl.getRestriction());
		} else if (obj instanceof OWLOntology) {
			addType(obj, owl.getOntology());
		} else if (obj instanceof OWLObjectProperty) {
			addType(obj, owl.getObjectProperty());
		} else if (obj instanceof OWLDataProperty) {
			addType(obj, owl.getDatatypeProperty());
		} else if (obj instanceof OWLAnnotationProperty) {
			addType(obj, owl.getAnnotationProperty());
		} else if (obj instanceof OWLDataEnumeration) {
			addType(obj, owl.getDataRange());
		} else if (obj instanceof OWLDataType) {
			addType(obj, owl.getDatatype());
		} else if (obj instanceof OWLDifferentIndividualsAxiom) {
			addType(obj, owl.getAllDifferent());
		} else if (obj instanceof OWLIndividual) {
			boolean needOWLThing = true;
			OWLIndividual individual = (OWLIndividual) obj;
			for (Iterator typeIter = individual.getTypes(ontology).iterator(); typeIter.hasNext();) {
				needOWLThing = false;
				add(getResource(obj), getResource(owl.getInstanceOf()), getRDFNode((OWLObject)typeIter.next()));
			}
			if (needOWLThing) {
				add(getResource(obj), getResource(owl.getInstanceOf()), getResource(owl.getThing()));
			}
		}
	}
	
	/**
	 * Check if object has already been rendererd.
	 * @param parent
	 * @param object
	 * @return
	 */
	protected boolean checkRendered(OWLObject parent, OWLObject object) {
		return checkRendered(parent, object, true);
	}
	
	protected boolean checkRendered(OWLObject parent, OWLObject object, boolean change) {
		
		Object objKey = getKey(object);
		
		if (rendered.contains(objKey)) {
			return true;
		}
		
		if (change) {
			rendered.add(objKey);
		}
		return false;
	}
	
	protected void createModel(String physicalURI) throws OWLException {
		try {
			
			nodeFactory = nodeProvider.getNodeFactory();
			
			
			// rdfapi is dumb and tries to make a URL out of the URI.  It often fails and returns null.
			// model = RDFManager.createModel(physicalURI, nodeFactory); 
			// So we just make it by hand.
			model=new TransactionableModelImpl(nodeFactory);
			model.setPhysicalURI(physicalURI);
			model.setLogicalURI(physicalURI);
			
		} catch (ModelException e) {
			e.printStackTrace();
			throw new OWLException(e);
		}
	}
	
	protected OWLObject currentObject() {
		if (objectStack.size() == 0) {
			return null;
		}
		return (OWLObject) objectStack.peek();
	}
	
	/**
	 * Returns a key representing the object.  
	 * @param obj
	 * @return
	 */
	protected Object getKey(OWLObject obj) {
		Object objKey = obj;
		if (((obj instanceof OWLDescription) && !(obj instanceof OWLNamedObject)) 
				|| (obj instanceof OWLDataEnumeration)) {

			List v = new Vector();
			v.add(0, objectStack.clone());
			v.add(1, currentClass);
			v.add(2, currentProperty);
			v.add(3, obj);
			objKey = v;
		}
		return objKey;
	}
	
	
	/**
	 * Returns the  model being added to.
	 * @return
	 */
	public Model getModel() {
		return this.model;
	}
	
	/*
	protected RDFNode getRDFNode(OWLObject obj) throws OWLException {
		RDFNode node = null;
		if (obj instanceof OWLDataValue) {
			OWLDataValue dv = (OWLDataValue) obj;
			
			try {
				String data = null;
				if (dv.getValue() != null) 
					data = dv.getValue().toString();
				String dtype = null;
				if (dv.getURI() != null)
					dtype = dv.getURI().toString();
				node = nodeFactory.createLiteral(data, dtype, dv.getLang());
			} catch (ModelException e) {
				throw new OWLException(e);
			}
		} else {
			node = getResource(obj);
		}
		return node;
	}
	
	*/
	
	protected RDFNode getRDFNode(OWLObject obj) throws OWLException {
		Object objKey = getKey(obj);
		RDFNode objNode = null;
		
		
		if (((obj instanceof OWLDescription) && !(obj instanceof OWLNamedObject)) 
				|| (obj instanceof OWLDataEnumeration)) {
			
			if (replaceParent) {
				OWLDescription parent = currentClass;
				pushCurrent(null, null, false);
				objNode = getResource(parent);
				popCurrent();
				return objNode;
			}
			
			if (resources.containsKey(objKey)) {
				objNode = (RDFNode) resources.get(objKey);
			} else {
				objNode = nodeFactory.createResource();
			}
		} else {

			if (resources.containsKey(objKey)) {
				objNode = (RDFNode) resources.get(objKey);
			} else {
				objNode = nodeProvider.getNode(obj);
				
			}
		}
		if (!resources.containsKey(objKey)) {
			resources.put(objKey, objNode);
			if (objNode instanceof SwoopResource) {
				SwoopResource objRes = (SwoopResource) objNode;
				if (objRes.isAnonymous()) {
					obj.accept(this);
				}
				addType(obj);
			}
		}
		return objNode;
	}
	
	protected SwoopResource getResource(OWLObject obj) throws OWLException {
		RDFNode node = getRDFNode(obj);
		if (!(node instanceof SwoopResource)) {
			throw new OWLException("Cannot use "+obj+" as a resource.");
		}
		
		return (SwoopResource) node;
	}
	
	protected SwoopResource getResource(URI uri) throws OWLException {
			return getResource(uri.toString());
	}
	
	protected SwoopResource getResource(String uri) throws OWLException {
		if (resources.containsKey(uri)) {
			return (SwoopResource) resources.get(uri);
		}
		SwoopResource objres;
		try {
			objres = (SwoopResource) nodeFactory.createResource(uri);
			resources.put(uri, objres);
			return objres;
		} catch (ModelException e) {
			throw new OWLException(e);
		}
		
	}
	

	protected void popCurrent() {

		if ((currentClass != null) || (currentProperty != null)) {
			objectStack.pop();
		}
		
		List v = (List) currentStack.pop();
		currentClass = (OWLDescription) v.get(0);
		currentProperty = (OWLProperty) v.get(1);
		replaceParent = ((Boolean) v.get(2)).booleanValue();
		
	}
	
	protected void pushCurrent(OWLDescription class_, OWLProperty prop, boolean replace) {
		List v = new Vector();
		v.add(0, currentClass);
		v.add(1, currentProperty);
		v.add(2, new Boolean(replaceParent));
		currentStack.push(v);
		
		currentClass = class_;
		currentProperty = prop;
		replaceParent = replace;
		
		if (class_ != null) {
			objectStack.push(class_);
		} else if (prop != null) {
			objectStack.push(prop);
		}
	}
	
	protected void renderAnonymousClass(OWLDescription node, String property, Collection values) throws OWLException {
		
		if (checkRendered(currentClass, node)) {
			return;
		}
		addAnnotations(node);
		SwoopResource resource = getResource(node);
		
		pushCurrent(node, null, false);
		add(resource, getResource(property), addList(values));
		popCurrent();
	}
	
	protected void renderProperty(final OWLProperty node) throws OWLException {
		addAnnotations(node);
		SwoopResource resource = getResource(node);
		
		pushCurrent(null, node, false);
		
		if (node.isFunctional(ontology)) {
			addType(node, owl.getFunctionalProperty());
		}
		if (node.isDeprecated(ontology)) {
			addType(node, owl.getDeprecatedProperty());
		}
		
		Map properties = new HashMap();
		
		for (Iterator iter = ontology.getPropertyAxioms().iterator(); iter.hasNext();) {
			OWLPropertyAxiom axiom = (OWLPropertyAxiom) iter.next();
			
			OWLPropertyAxiomVisitor visitor = new OWLPropertyAxiomVisitor() {

				public void visit(OWLDataPropertyRangeAxiom axiom) throws OWLException {
					if (axiom.getProperty().equals(node)) {
						axiom.accept(TripleVisitor.this);
					}
				}

				public void visit(OWLEquivalentPropertiesAxiom axiom) throws OWLException {
					if (axiom.getProperties().contains(node)) {
						axiom.accept(TripleVisitor.this);
					}
				}

				public void visit(OWLFunctionalPropertyAxiom axiom) throws OWLException {
					if (axiom.getProperty().equals(node)) {
						axiom.accept(TripleVisitor.this);
					}
				}

				public void visit(OWLInverseFunctionalPropertyAxiom axiom) throws OWLException {
					if (axiom.getProperty().equals(node)) {
						axiom.accept(TripleVisitor.this);
					}
				}

				public void visit(OWLInversePropertyAxiom axiom) throws OWLException {
					if (axiom.getProperty().equals(node)) {
						axiom.accept(TripleVisitor.this);
					}
				}

				public void visit(OWLObjectPropertyRangeAxiom axiom) throws OWLException {
					if (axiom.getProperty().equals(node)) {
						axiom.accept(TripleVisitor.this);
					}
				}

				public void visit(OWLPropertyDomainAxiom axiom) throws OWLException {
					if (axiom.getProperty().equals(node)) {
						axiom.accept(TripleVisitor.this);
					}
				}

				public void visit(OWLSubPropertyAxiom axiom) throws OWLException {
					if (axiom.getSubProperty().equals(node)) {
						axiom.accept(TripleVisitor.this);
					}
				}

				public void visit(OWLSymmetricPropertyAxiom axiom) throws OWLException {
					if (axiom.getProperty().equals(node)) {
						axiom.accept(TripleVisitor.this);
					}
				}

				public void visit(OWLTransitivePropertyAxiom axiom) throws OWLException {
					if (axiom.getProperty().equals(node)) {
						axiom.accept(TripleVisitor.this);
					}
				}

			};
			axiom.accept(visitor);
		}
		
		properties.put(owl.getSubPropertyOf(), node.getSuperProperties(ontology));
		properties.put(owl.getDomain(), node.getDomains(ontology));
		properties.put(owl.getRange(), node.getRanges(ontology));
		
		addProperties(resource, properties);
		popCurrent();
		
		
		
	}
		
	public void reset() throws OWLException {
//		qnames = new QNameShortFormProvider();
		currentProperty = null;
		currentClass = null;
		currentStack = new Stack();
		objectStack = new Stack();
		rendered = new HashSet();
		resources = new HashMap();
		serialized = new HashSet();
	}
	

	public void serialize(Writer writer) throws IOException, OWLException {
		XMLWriter xml = new PrettyXMLWriter(writer);
		serialize(xml);
	}
	
	public void serialize(XMLWriter xml) throws IOException, OWLException {
		RDFSerializer serializer = new OntologySerializer(getResource(ontology));
		serialize(xml, serializer);
	}
	
	public void serialize(XMLWriter xml, RDFSerializer serializer) throws IOException, OWLException {
		serializer.serialize(xml, model);
	}
		
	
	protected void visitAll(Collection objects) throws OWLException {
		visitAll(objects.iterator());
	}
	
	protected void visitAll(Iterator iterator) throws OWLException {
		for (; iterator.hasNext();) {
			OWLObject object = (OWLObject) iterator.next();
			object.accept(this);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.semanticweb.owl.model.OWLObjectVisitor#visit(org.semanticweb.owl.model.OWLAnd)
	 */
	public void visit(OWLAnd node) throws OWLException {
		renderAnonymousClass(node, owl.getIntersectionOf(), node.getOperands());
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owl.model.OWLObjectVisitor#visit(org.semanticweb.owl.model.OWLAnnotationProperty)
	 */
	public void visit(OWLAnnotationProperty node) throws OWLException {
		addType(node);
		addAnnotations(node);
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owl.model.OWLObjectVisitor#visit(org.semanticweb.owl.model.OWLAnnotationInstance)
	 */
	public void visit(OWLAnnotationInstance node) throws OWLException {
		SwoopResource subject = getResource(node.getSubject());
		SwoopResource property = getResource(node.getProperty());
		
		Object content = node.getContent();
		RDFNode contentResource = null;
		
		if (content instanceof OWLObject) {
			contentResource = getRDFNode((OWLObject)content);
		} else if (content instanceof URI) {
			contentResource = getResource(((URI)content).toString());
		} else if (content instanceof String) {
			try {
				contentResource = nodeFactory.createLiteral((String) content);
			} catch (ModelException e) {
				throw new OWLException(e);
			}
		} else {
			throw new OWLException("Unknown type of owl annotation: "+content.getClass());
		}
		
		add(subject, property, contentResource);
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owl.model.OWLObjectVisitor#visit(org.semanticweb.owl.model.OWLDataValue)
	 */
	public void visit(OWLDataValue node) throws OWLException {
		// nothing to do
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owl.model.OWLObjectVisitor#visit(org.semanticweb.owl.model.OWLDataType)
	 */
	public void visit(OWLDataType node) throws OWLException {
		addAnnotations(node);
		addType(node);
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owl.model.OWLObjectVisitor#visit(org.semanticweb.owl.model.OWLDataEnumeration)
	 */
	public void visit(OWLDataEnumeration node) throws OWLException {
		//System.out.println("DataEnumeration: "+node.getValues());
		addAnnotations(node);
		add(getResource(node), getResource(owl.getOneOf()), addList(node.getValues()));
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owl.model.OWLObjectVisitor#visit(org.semanticweb.owl.model.OWLDataAllRestriction)
	 */
	public void visit(OWLDataAllRestriction node) throws OWLException {
		addAnnotations(node);
		addType(node);
		add(getResource(node), getResource(owl.getOnProperty()), getResource(node.getDataProperty()));
		add(getResource(node), getResource(owl.getAllValuesFrom()), getResource(node.getDataType()));
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owl.model.OWLObjectVisitor#visit(org.semanticweb.owl.model.OWLDataCardinalityRestriction)
	 */
	public void visit(OWLDataCardinalityRestriction node) throws OWLException {
		addAnnotations(node);
		addType(node);
		add(getResource(node), getResource(owl.getOnProperty()), getResource(node.getDataProperty()));
		
		SwoopResource property = null;
		int value = 0;
		if (node.isExactly()) {
			property = getResource(owl.getCardinality());
			value = node.getAtLeast();
		} else if (node.isAtLeast()) {
			property = getResource(owl.getMinCardinality());
			value = node.getAtLeast();
		} else if (node.isAtMost()) {
			property = getResource(owl.getMaxCardinality());
			value = node.getAtMost();
		}
		
		if (property == null) {
			return;
		}
		
		RDFNode valueNode;
		try {
			valueNode = nodeFactory.createLiteral(Integer.toString(value), 
					"http://www.w3.org/2001/XMLSchema#nonNegativeInteger", null);
		} catch (ModelException e) {
			throw new OWLException(e);
		}
		add(getResource(node), property, valueNode);
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owl.model.OWLObjectVisitor#visit(org.semanticweb.owl.model.OWLDataProperty)
	 */
	public void visit(OWLDataProperty node) throws OWLException {
		addType(node);
		renderProperty(node);
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owl.model.OWLObjectVisitor#visit(org.semanticweb.owl.model.OWLDataSomeRestriction)
	 */
	public void visit(OWLDataSomeRestriction node) throws OWLException {
		addAnnotations(node);
		addType(node);
		add(getResource(node), getResource(owl.getOnProperty()), getResource(node.getDataProperty()));
		add(getResource(node), getResource(owl.getSomeValuesFrom()), getResource(node.getDataType()));
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owl.model.OWLObjectVisitor#visit(org.semanticweb.owl.model.OWLDataValueRestriction)
	 */
	public void visit(OWLDataValueRestriction node) throws OWLException {
		addAnnotations(node);
		addType(node);
		add(getResource(node), getResource(owl.getOnProperty()), getResource(node.getDataProperty()));
		add(getResource(node), getResource(owl.getHasValue()), getRDFNode(node.getValue()));
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owl.model.OWLObjectVisitor#visit(org.semanticweb.owl.model.OWLDifferentIndividualsAxiom)
	 */
	public void visit(OWLDifferentIndividualsAxiom node) throws OWLException {
//		if (checkRendered(null, node, false)) {
//			return;
//		}
//		
//		if (node.getIndividuals().contains(currentObject()) || node.getIndividuals().size() <= 2) {
//			addSymmetric(getResource(owl.getDifferentFrom()), node.getIndividuals());
//		} else {
			if (checkRendered(null, node)) {
				return;
			}
			addType(node);
			addAnnotations(node);
			add(getResource(node), getResource(owl.getDistinctMembers()), addList(node.getIndividuals()));
//		}
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owl.model.OWLObjectVisitor#visit(org.semanticweb.owl.model.OWLDisjointClassesAxiom)
	 */
	public void visit(OWLDisjointClassesAxiom node) throws OWLException {
		addSymmetric(getResource(owl.getDisjointWith()), node.getDisjointClasses());
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owl.model.OWLObjectVisitor#visit(org.semanticweb.owl.model.OWLEquivalentClassesAxiom)
	 */
	public void visit(OWLEquivalentClassesAxiom node) throws OWLException {
		pushCurrent(null,null,false);
		addEquivalences(owl.getEquivalentClass(), node.getEquivalentClasses());
		popCurrent();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.semanticweb.owl.model.OWLObjectVisitor#visit(org.semanticweb.owl.model.OWLEquivalentPropertiesAxiom)
	 */
	public void visit(OWLEquivalentPropertiesAxiom node) throws OWLException {
		if ((currentProperty == null) || node.getProperties().add(node)) {
			addEquivalences(owl.getEquivalentProperty(), node.getProperties());	
		} else {
			for (Iterator iter = node.getProperties().iterator(); iter.hasNext();) {
				OWLObject prop = (OWLObject) iter.next();
				add(getResource(currentProperty), getResource(owl.getEquivalentProperty()), getResource(prop));
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owl.model.OWLObjectVisitor#visit(org.semanticweb.owl.model.OWLFrame)
	 */
	public void visit(OWLFrame node) throws OWLException {
		// TODO Auto-generated method stub
		throw new OWLException("Not implemented yet");
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owl.model.OWLObjectVisitor#visit(org.semanticweb.owl.model.OWLIndividual)
	 */
	public void visit(OWLIndividual node) throws OWLException {
		objectStack.push(node);
		addAnnotations(node);
		
		addType(node);
		
		// Datatype values
		Map dataValueMap = node.getDataPropertyValues(ontology);
		for (Iterator propertyIter = dataValueMap.keySet().iterator(); propertyIter.hasNext();) {
			OWLProperty property = (OWLProperty) propertyIter.next();
			
			for (Iterator dvIter = ((Set) dataValueMap.get(property)).iterator(); dvIter.hasNext();) {
				OWLDataValue dataValue = (OWLDataValue) dvIter.next();
				add(node, property, dataValue);
			}
		}
		
		// Object values
		Map objectValueMap = node.getObjectPropertyValues(ontology);
		for (Iterator propertyIter = objectValueMap.keySet().iterator(); propertyIter.hasNext();) {
			OWLProperty property = (OWLProperty) propertyIter.next();
			
			for (Iterator objectIter = ((Set) objectValueMap.get(property)).iterator(); objectIter.hasNext();) {
				OWLIndividual objectValue = (OWLIndividual) objectIter.next();
				add(node, property, objectValue);
			}
		}
		
		// Individual axioms
		for (Iterator axiomIter = ontology.getIndividualAxioms().iterator(); axiomIter.hasNext();) {
			OWLIndividualAxiom axiom = (OWLIndividualAxiom) axiomIter.next();
			if (axiom.getIndividuals().contains(node)) {
				axiom.accept(this);
			}
		}
		
		objectStack.pop();
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owl.model.OWLObjectVisitor#visit(org.semanticweb.owl.model.OWLObjectAllRestriction)
	 */
	public void visit(OWLObjectAllRestriction node) throws OWLException {
		addAnnotations(node);
		addType(node);
		add(getResource(node), getResource(owl.getOnProperty()), getResource(node.getObjectProperty()));
		add(getResource(node), getResource(owl.getAllValuesFrom()), getResource(node.getDescription()));
		
		OWLDescription description = node.getDescription();
		if (!(description instanceof OWLClass)) {
			description.accept(this);
		}
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owl.model.OWLObjectVisitor#visit(org.semanticweb.owl.model.OWLObjectCardinalityRestriction)
	 */
	public void visit(OWLObjectCardinalityRestriction node) throws OWLException {
		addAnnotations(node);
		addType(node);
		add(getResource(node), getResource(owl.getOnProperty()), getResource(node.getObjectProperty()));
		
		SwoopResource property = null;
		int value = 0;
		if (node.isExactly()) {
			property = getResource(owl.getCardinality());
			value = node.getAtLeast();
		} else if (node.isAtLeast()) {
			property = getResource(owl.getMinCardinality());
			value = node.getAtLeast();
		} else if (node.isAtMost()) {
			property = getResource(owl.getMaxCardinality());
			value = node.getAtMost();
		}
		
		if (property == null) {
			return;
		}
		
		RDFNode valueNode;
		try {
			valueNode = nodeFactory.createLiteral(Integer.toString(value), 
					"http://www.w3.org/2001/XMLSchema#nonNegativeInteger", null);
		} catch (ModelException e) {
			throw new OWLException(e);
		}
		add(getResource(node), property, valueNode);
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owl.model.OWLObjectVisitor#visit(org.semanticweb.owl.model.OWLObjectProperty)
	 */
	public void visit(OWLObjectProperty node) throws OWLException {
		addType(node);
		addAnnotations(node);
		
		if (node.isInverseFunctional(ontology)) {
			addType(node, owl.getInverseFunctionalProperty());
		}
		if (node.isSymmetric(ontology)) {
			addType(node, owl.getSymmetricProperty());
		}
		if (node.isTransitive(ontology)) {
			addType(node, owl.getTransitive());
		}
		
		Map properties = new HashMap();
		properties.put(owl.getInverseOf(), node.getInverses(ontology));
		
		addProperties(getResource(node), properties);
		
		renderProperty(node);
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owl.model.OWLObjectVisitor#visit(org.semanticweb.owl.model.OWLObjectSomeRestriction)
	 */
	public void visit(OWLObjectSomeRestriction node) throws OWLException {
		addAnnotations(node);
		addType(node);
		add(getResource(node), getResource(owl.getOnProperty()), getRDFNode(node.getObjectProperty()));
		add(getResource(node), getResource(owl.getSomeValuesFrom()), getRDFNode(node.getDescription()));
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owl.model.OWLObjectVisitor#visit(org.semanticweb.owl.model.OWLObjectValueRestriction)
	 */
	public void visit(OWLObjectValueRestriction node) throws OWLException {
		addAnnotations(node);
		addType(node, owl.getRestriction());
		add(getResource(node), getResource(owl.getOnProperty()), getResource(node.getObjectProperty()));
		add(getResource(node), getResource(owl.getHasValue()), getRDFNode(node.getIndividual()));
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owl.model.OWLObjectVisitor#visit(org.semanticweb.owl.model.OWLNot)
	 */
	public void visit(OWLNot node) throws OWLException {
		addType(node);
		addAnnotations(node);
		SwoopResource resource = getResource(node);
		
		pushCurrent(node, null, false);
		add(resource, getResource(owl.getComplementOf()), getRDFNode(node.getOperand()));
		popCurrent();
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owl.model.OWLObjectVisitor#visit(org.semanticweb.owl.model.OWLOntology)
	 */
	public void visit(OWLOntology node) throws OWLException {
		addType(node);
		addAnnotations(node);
		
		Map properties = new HashMap();
		//properties.put(owl.getImports(), node.getIncludedOntologies());
		properties.put(owl.getIncompatibleWith(), node.getIncompatibleWith());
		properties.put(owl.getBackwardCompatibleWith(), node.getBackwardCompatibleWith());
		properties.put(owl.getPriorVersion(), node.getPriorVersion());
		addProperties(getResource(node), properties);
		
		for (Iterator ontIter = node.getIncludedOntologies().iterator(); ontIter.hasNext();) {
			OWLOntology included = (OWLOntology) ontIter.next();
			URI physicalURI = included.getPhysicalURI();
			add(getResource(node), getResource(owl.getImports()), getResource(physicalURI));
			add(getResource(physicalURI), 
					getResource(owl.getInstanceOf()), 
					getResource(owl.getOntology()));
		}
		
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owl.model.OWLObjectVisitor#visit(org.semanticweb.owl.model.OWLOr)
	 */
	public void visit(OWLOr node) throws OWLException {
		renderAnonymousClass(node, owl.getUnionOf(), node.getOperands());
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owl.model.OWLObjectVisitor#visit(org.semanticweb.owl.model.OWLClass)
	 */
	public void visit(OWLClass node) throws OWLException {
		addType(node);
		addAnnotations(node);
		if (node.isDeprecated(ontology)) {
			addType(node, owl.getDeprecatedClass());
		}
		SwoopResource resource = getResource(node);
		
		Set equivs = new HashSet();
		equivs.addAll(node.getEquivalentClasses(ontology));
		equivs.addAll(node.getEnumerations(ontology));
		
		Set named = new HashSet();
		
		pushCurrent(node, null, true);
		for (Iterator iter = equivs.iterator(); iter.hasNext();) {
			OWLDescription d = (OWLDescription) iter.next();
			
			if ((d instanceof OWLEnumeration) || (d instanceof OWLBooleanDescription)) {
				d.accept(this);
			} else {
				named.add(d);
			}
		}
		popCurrent();
		
		
		pushCurrent(node, null, false);
		
		
		for (Iterator iter = ontology.getClassAxioms().iterator(); iter.hasNext();) {
			OWLClassAxiom axiom = (OWLClassAxiom) iter.next();
			if (axiom instanceof OWLDisjointClassesAxiom) {
				OWLDisjointClassesAxiom disjoints = (OWLDisjointClassesAxiom) axiom;
				if (disjoints.getDisjointClasses().contains(node)) {
					disjoints.accept(this);
				}
			}
		}
		
		
		Map properties = new HashMap();
		properties.put(owl.getEquivalentClass(), named);
		properties.put(owl.getSubClassOf(), node.getSuperClasses(ontology));
		addProperties(resource, properties);
		
		popCurrent();
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owl.model.OWLObjectVisitor#visit(org.semanticweb.owl.model.OWLEnumeration)
	 */
	public void visit(OWLEnumeration node) throws OWLException {
		addType(node);
		renderAnonymousClass(node, owl.getOneOf(), node.getIndividuals());
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owl.model.OWLObjectVisitor#visit(org.semanticweb.owl.model.OWLSameIndividualsAxiom)
	 */
	public void visit(OWLSameIndividualsAxiom node) throws OWLException {
		addEquivalences(owl.getSameAs(), node.getIndividuals());
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owl.model.OWLObjectVisitor#visit(org.semanticweb.owl.model.OWLSubClassAxiom)
	 */
	public void visit(OWLSubClassAxiom node) throws OWLException {
		SwoopResource subject, object;
		
		pushCurrent(null, null, false);
		subject = getResource(node.getSubClass());
		popCurrent();
		
		pushCurrent(node.getSubClass(), null, false);
		object = getResource(node.getSuperClass());
		popCurrent();
		
		add(subject, getResource(owl.getSubClassOf()), object);
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owl.model.OWLObjectVisitor#visit(org.semanticweb.owl.model.OWLSubPropertyAxiom)
	 */
	public void visit(OWLSubPropertyAxiom node) throws OWLException {
		add(getResource(node.getSubProperty()), 
				getResource(owl.getSubPropertyOf()), 
				getResource(node.getSuperProperty()));
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owl.model.OWLObjectVisitor#visit(org.semanticweb.owl.model.OWLFunctionalPropertyAxiom)
	 */
	public void visit(OWLFunctionalPropertyAxiom node) throws OWLException {
		addType(node.getProperty(), owl.getFunctionalProperty());
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owl.model.OWLObjectVisitor#visit(org.semanticweb.owl.model.OWLInverseFunctionalPropertyAxiom)
	 */
	public void visit(OWLInverseFunctionalPropertyAxiom node) throws OWLException {
		addType(node.getProperty(), owl.getInverseFunctionalProperty());
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owl.model.OWLObjectVisitor#visit(org.semanticweb.owl.model.OWLTransitivePropertyAxiom)
	 */
	public void visit(OWLTransitivePropertyAxiom node) throws OWLException {
		addType(node.getProperty(), owl.getTransitive());
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owl.model.OWLObjectVisitor#visit(org.semanticweb.owl.model.OWLSymmetricPropertyAxiom)
	 */
	public void visit(OWLSymmetricPropertyAxiom node) throws OWLException {
		addType(node.getProperty(), owl.getSymmetricProperty());
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owl.model.OWLObjectVisitor#visit(org.semanticweb.owl.model.OWLInversePropertyAxiom)
	 */
	public void visit(OWLInversePropertyAxiom node) throws OWLException {
		add(getResource(node.getProperty()), 
				getResource(owl.getInverseOf()), 
				getResource(node.getInverseProperty()));
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owl.model.OWLObjectVisitor#visit(org.semanticweb.owl.model.OWLPropertyDomainAxiom)
	 */
	public void visit(OWLPropertyDomainAxiom node) throws OWLException {
		add(getResource(node.getProperty()), 
				getResource(owl.getDomain()), 
				getResource(node.getDomain()));
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owl.model.OWLObjectVisitor#visit(org.semanticweb.owl.model.OWLObjectPropertyRangeAxiom)
	 */
	public void visit(OWLObjectPropertyRangeAxiom node) throws OWLException {
		SwoopResource range;
		
		pushCurrent(null, node.getProperty(), false);
		range = getResource(node.getRange());
		popCurrent();
		
		add(getResource(node.getProperty()), getResource(owl.getRange()), range); 
				
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owl.model.OWLObjectVisitor#visit(org.semanticweb.owl.model.OWLDataPropertyRangeAxiom)
	 */
	public void visit(OWLDataPropertyRangeAxiom node) throws OWLException {
		SwoopResource range;
		
		pushCurrent(null, node.getProperty(), false);
		range = getResource(node.getRange());
		popCurrent();
		
		add(getResource(node.getProperty()), getResource(owl.getRange()), range);
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owl.model.OWLObjectVisitor#visit(org.semanticweb.owl.model.OWLObjectPropertyInstance)
	 */
	public void visit(OWLObjectPropertyInstance node) throws OWLException {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owl.model.OWLObjectVisitor#visit(org.semanticweb.owl.model.OWLDataPropertyInstance)
	 */
	public void visit(OWLDataPropertyInstance node) throws OWLException {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owl.model.OWLObjectVisitor#visit(org.semanticweb.owl.model.OWLIndividualTypeAssertion)
	 */
	public void visit(OWLIndividualTypeAssertion node) throws OWLException {
		// TODO Auto-generated method stub
		
	}

}
