package org.mindswap.swoop.renderer.ontology;

import java.io.Reader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringEscapeUtils;
import org.mindswap.swoop.SwoopModel;
import org.semanticweb.owl.io.ParserException;
import org.semanticweb.owl.io.abstract_syntax.ObjectRenderer;
import org.semanticweb.owl.io.owl_rdf.OWLRDFErrorConstants;
import org.semanticweb.owl.io.owl_rdf.OWLRDFErrorHandler;
import org.semanticweb.owl.io.owl_rdf.OWLRDFParser;
import org.semanticweb.owl.io.vocabulary.OWLVocabularyAdapter;
import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLDataProperty;
import org.semanticweb.owl.model.OWLDataRange;
import org.semanticweb.owl.model.OWLDataType;
import org.semanticweb.owl.model.OWLDescription;
import org.semanticweb.owl.model.OWLException;
import org.semanticweb.owl.model.OWLIndividual;
import org.semanticweb.owl.model.OWLNamedObject;
import org.semanticweb.owl.model.OWLObject;
import org.semanticweb.owl.model.OWLObjectProperty;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.helper.OntologyHelper;
import org.semanticweb.owl.util.OWLConnection;
import org.semanticweb.owl.util.OWLManager;
import org.semanticweb.owl.validation.SpeciesValidatorReporter;
import org.xml.sax.SAXException;

//import uk.ac.man.cs.img.owl.validation.SpeciesValidator;

/**
 * @author Evren Sirin modiefied by Dave
 */
public class SwoopSpeciesValidator implements
		org.semanticweb.owl.validation.SpeciesValidator,
		org.semanticweb.owl.validation.OWLValidationConstants {

	// popular registered URI transfer scheme names (see
	// http://www.iana.org/assignments/uri-schemes)
	// private static final String [] POPULAR_SCHEME_NAMES = {"http://"};//,
	// "ftp://", "gopher://", "https://", "tftp://" };
	private static final String SPACE = "&nbsp;";

	private static final String LT = "&lt;";

	private static final String GT = "&gt;";

	protected static final int OTHER = 7; /* 0111 */

	protected SwoopModel myModel = null;

	private SpeciesValidatorReporter reporter;

	private Map options;

	private OWLConnection connection;

	private OWLOntology ontology;

	private Set allOntologies;

	private Set reservedVocabulary;

	/*
	 * Those things from the reserved vocabulary that can be defined as classes.
	 * rdf:Statement, rdf:Seq, rdf:Bag, and rdf:Alt
	 */
	private Set classOnlyVocabulary;

	/*
	 * Those things from the reserved vocabulary that can be defined as classes.
	 * rdf:subject, rdf:predicate, rdf:object, and all the container membership
	 * properties, i.e., rdf:_1, rdf:_2, etc.
	 */
	private Set propertyOnlyVocabulary;

	/**
	 * Indicates whether the separation of names for classes, individuals and
	 * properties has been observed
	 */
	private boolean namespacesSeparated;

	/**
	 * Indicates whether the conditions regarding the usage and redefinition of
	 * elements from the RDF, RDFS and OWL vocabularies have been observed.
	 */
	private boolean correctOWLUsage;

	private boolean correctOWLNamespaceUsage;

	/** Indicates whether all individuals have at least one explicit type. */
	private boolean individualsTyped;

	/*
	 * Indicates whether classAxioms from a particular species are used.
	 */
	private int classAxiomLevel;

	/*
	 * Indicates whether propertyAxioms from a particular species are used.
	 */
	private int propertyAxiomLevel;

	/*
	 * Indicates the level of expressivity used in expressions within the
	 * ontology.
	 */
	private int expressivityLevel;

	/*
	 * Indicates whether syntactic constraints have been violated which pushes
	 * us into a particular level, e.g. untyped URIs => Full
	 */
	private int syntaxLevel;

	/* Collections of URIs taken from the ontology. */
	private Set allURIs;

	private Set classURIs;

	private Set individualURIs;

	private Set objectPropertyURIs;

	private Set dataPropertyURIs;

	private Set annotationPropertyURIs;

	/*
	 * There may still be some nasty corner cases that slip through the net
	 * here....
	 */
	private Set datatypeURIs;

	private ObjectRenderer objectRenderer;

	private OWLRDFParser parser;

	/**
	 * Create a new validator. Will report to stdout by default.
	 * 
	 * 
	 */
	public SwoopSpeciesValidator(SwoopModel model) throws OWLException {
		myModel = model;
		/* Sets up a default reporter that writes to stdout. */
		setReporter(new SpeciesValidatorReporter() {
			public void ontology(OWLOntology onto) {
				// System.out.println( onto.getURI() );
			}

			public void done(String str) {
				// System.out.println( str );
			}

			public void message(String str) {
				System.out.println(str);
			}

			public void explain(int l, int code, String str) {
				System.out.println(level(l) + " [" + readableCode(code)
						+ "]:\t" + str);
				// System.out.println( level( l ) + ":\t" + str );
			}
		});
		reservedVocabulary = OWLVocabularyAdapter.INSTANCE.getReservedVocabulary();

		classOnlyVocabulary = new HashSet();
		classOnlyVocabulary.add(OWLVocabularyAdapter.INSTANCE.getStatement());
		classOnlyVocabulary.add(OWLVocabularyAdapter.INSTANCE.getSeq());
		classOnlyVocabulary.add(OWLVocabularyAdapter.INSTANCE.getBag());
		classOnlyVocabulary.add(OWLVocabularyAdapter.INSTANCE.getAlt());

		options = new HashMap();

		/* Get a default connection */
		connection = OWLManager.getOWLConnection();

		parser = new OWLRDFParser();
		/*
		 * Tell the parser to ignore annotation content. This is not needed for
		 * validation.
		 */
		Map options = new HashMap();
		options.put("includeAnnotationContent", new Boolean(false));
		parser.setOptions(options);
	}

	/**
	 * Set the connection (e.g. the implementation that the validator will
	 * choose to use when constructing ontologies.
	 */

	public void setConnection(OWLConnection connection) {
		this.connection = connection;
	}

	/**
	 * Set the reporter that this speciesValidator will use. By default, the
	 * validator will write to stdout. If you want to stop this happening, set
	 * the reporter to null
	 */
	public void setReporter(SpeciesValidatorReporter reporter) {
		this.reporter = reporter;
	}

	/**
	 * Provide an explanation as to why the validator considers the ontology to
	 * be in a particular species.
	 * 
	 * @param l
	 *            an <code>int</code> value
	 * @param str
	 *            a <code>String</code> value
	 */
	public void explain(int l, int code, String str) {
		if (reporter != null) {
			reporter.explain(l, code, str);
		}
	}

	/**
	 * Write a message.
	 * 
	 * @param str
	 *            a <code>String</code> value
	 */
	public void message(String str) {
		if (reporter != null) {
			reporter.message(str);
		}
	}

	protected static String level(int l) {
		if (l == LITE) {
			return "OWL-Lite";
		} else if (l == DL) {
			return "OWL-DL  ";
		} else if (l == FULL) {
			return "OWL-Full";
		} else {
			return "OTHER   ";
		}

	}

	/**
	 * Set the ontology that the validator will work with. Note that this
	 * performs some initialisation. This particular implementation does not
	 * track ontology changes, so if the ontology is changed before validation
	 * takes place, the results may not be as expected.
	 * 
	 * 
	 * 
	 * @param ontology
	 *            an <code>OWLOntology</code> value
	 * @param checkImport
	 *            if true, grab the imports closure and check the species of any
	 *            imported ontologies. If false, just look here. Allows us to
	 *            catch situations where an ontology is imported that has a
	 *            higher expressivity, but the classes involved in that aren't
	 *            explicitly used in the importer.
	 */
	private int species(OWLOntology ontology, boolean checkImport) {
		int result = LITE;

		try {
			this.ontology = ontology;

			// logger.info( "Validating: "
			// + (checkImport?"[imports] ":"")
			// + ontology.getURI() );

			if (reporter != null) {
				reporter.ontology(ontology);
			}

			/* Find the import closure */
			this.allOntologies = OntologyHelper.importClosure(ontology);
			/* Do some initial processing */

			gatherURIs();

			/* Set up all the variables */
			this.namespacesSeparated = true;
			this.correctOWLUsage = true;
			this.correctOWLNamespaceUsage = true;
			this.individualsTyped = true;
			this.classAxiomLevel = FULL;
			this.propertyAxiomLevel = FULL;
			this.expressivityLevel = FULL;

			/* A helper used when reporting stuff. */
			objectRenderer = new ObjectRenderer(ontology);

			/* Now do all the relevant checks */
			checkNamespaceSeparation();
			checkCorrectOWLUsage();
			checkCorrectOWLNamespaceUsage();
			/* This should be done during parsing */
			// checkIndividualTyping();
			checkClassAxioms();
			checkPropertyAxioms();
			checkExpressivity();

			if (!correctOWLNamespaceUsage) {
				/*
				 * If there are things in the OWL namespace, we're in OTHER. See
				 * http://lists.w3.org/Archives/Public/www-webont-wg/2003Feb/0157.html
				 */
				/*
				 * This doesn't seem right though. I think it's actually the
				 * case that any RDF document is an OWL FULL document. See
				 * Section 1.3 of the Overview.
				 */
				// result = OTHER;
				result = FULL;
			} else if (!namespacesSeparated || !correctOWLUsage
					|| !individualsTyped) {
				/*
				 * If namespaces aren't separated, or redefinitions have
				 * occurred, or individuals aren't all explicitly typed, we're
				 * in Full
				 */

				result = FULL;
			} else {
				/*
				 * Otherwise, it's the highest level that's used for classes,
				 * properties and expressivity
				 */
				result = (classAxiomLevel | propertyAxiomLevel | expressivityLevel);
			}

			if (reporter != null) {
				reporter.done(level(result));
			}

		} catch (OWLException e) {
			result = FULL;
			reporter.explain(FULL, UNKNOWN, "Exception occurred: "
					+ e.getMessage());
		}

		return result;
	}

	/**
	 * Set options for this validator
	 * 
	 * @param options
	 *            a <code>Map</code> value. Should contain a map from
	 *            {@link String String}s to {@link String String}s.
	 */
	public void setOptions(Map options) {
		options = new HashMap(options);
	};

	/**
	 * 
	 * Get options for this validator
	 * 
	 * @return a <code>Map</code> value. Contains a map from
	 *         {@link String String}s to {@link String String}s.
	 */
	public Map getOptions() {
		return options;
	}

	/** Record the fact that some particular syntax has been noticed. */
	private void setSyntaxLevel(int l) {
		syntaxLevel = l;
	}

	/**
	 * Parse an ontology from a given URI.
	 * 
	 * @param handler
	 *            an <code>OWLRDFErrorHandler</code> value
	 * @param uri
	 *            an <code>URI</code> value
	 * @return an <code>OWLOntology</code> value
	 * @exception ParserException
	 *                if an error occurs
	 * @exception OWLException
	 *                if an error occurs
	 */
	private OWLOntology parseFromURI(OWLRDFErrorHandler handler, URI uri)
			throws ParserException, OWLException {
		parser.setConnection(connection);

		/* Error handler for the parser */
		parser.setOWLRDFErrorHandler(handler);

		// OWLOntology onto = connection.createOWLOntology( uri,uri );
		OWLOntology onto = parser.parseOntology(uri);
		// message( onto.toString() );
		return onto;
	}

	/**
	 * Returns <code>true</code> if the ontology obtained by parsing the URI
	 * is in OWL Lite. Will report findings to the reporter as it goes. Note
	 * that the inner workings of the validator assume that the ontology has
	 * <strong>not</strong> already been parsed.
	 * 
	 * @param uri
	 *            an <code>URI</code> value
	 * @return a <code>boolean</code> value
	 */
	public boolean isOWLLite(URI uri) {
		boolean result = false;
		try {
			/* Handler that's strict about OWLFullExceptions */
			syntaxLevel = LITE;
			OWLRDFErrorHandler handler = new OWLRDFErrorHandler() {
				public void owlFullConstruct(int code, String message)
						throws SAXException {
					/* Doesn't throw an error, but keeps going.... */
					setSyntaxLevel(FULL);
					explain(FULL, code, message);
					// throw new OWLFullConstructRDFException( message );
				}

				public void error(String message) throws SAXException {
					throw new SAXException(message.toString());
				}

				public void warning(String message) throws SAXException {
					message(message.toString());
				}

				public void owlFullConstruct(int code, String message,
						Object obj) throws SAXException {
					// TODO Auto-generated method stub

				}
			};
			OWLOntology o = parseFromURI(handler, uri);
			int species = species(o, true) | syntaxLevel;
			result = (species == LITE);
			// releaseOntology( o );
		} catch (ParserException ex) {
			explain(OTHER, UNKNOWN, ex.getMessage());
		} catch (OWLException ex) {
			explain(OTHER, UNKNOWN, ex.getMessage());
		}
		return result;
	}

	/**
	 * Returns <code>true</code> if the ontology obtained by parsing the URI
	 * is in OWL DL. Will report findings to the reporter as it goes. Note that
	 * the inner workings of the validator assume that the ontology has
	 * <strong>not</strong> already been parsed.
	 * 
	 * @param uri
	 *            an <code>URI</code> value
	 * @return a <code>boolean</code> value
	 */
	public boolean isOWLDL(URI uri) {
		boolean result = false;
		try {
			/* Handler that's strict about OWLFullExceptions */
			syntaxLevel = LITE;
			OWLRDFErrorHandler handler = new OWLRDFErrorHandler() {
				public void owlFullConstruct(int code, String message) throws SAXException {
					/* Doesn't throw an error, but keeps going.... */
					setSyntaxLevel(FULL);
					explain(FULL, code, message);
					// throw new OWLFullConstructRDFException( message );
				}

				public void error(String message) throws SAXException {
					throw new SAXException(message.toString());
				}

				public void warning(String message) throws SAXException {
					message(message.toString());
				}

				public void owlFullConstruct(int code, String message,
						Object obj) throws SAXException {
					// TODO Auto-generated method stub

				}
			};
			OWLOntology o = parseFromURI(handler, uri);
			int species = species(o, true) | syntaxLevel;
			result = (species == DL || species == LITE);
			// releaseOntology( o );
		} catch (ParserException ex) {
			explain(OTHER, UNKNOWN, ex.getMessage());
		} catch (OWLException ex) {
			explain(OTHER, UNKNOWN, ex.getMessage());
		}
		return result;
	}

	/**
	 * Returns <code>true</code> if the ontology obtained by parsing the URI
	 * is in OWL Full. Will report findings to the reporter as it goes. Note
	 * that the inner workings of the validator assume that the ontology has
	 * <strong>not</strong> already been parsed.
	 * 
	 * @param uri
	 *            an <code>URI</code> value
	 * @return a <code>boolean</code> value
	 */
	public boolean isOWLFull(URI uri) {
		/*
		 * An ontology is OWL Full if:
		 * 
		 * 1) There are OWL Full constructs used in the syntax, e.g. things have
		 * not been explicitly typed
		 * 
		 * or
		 * 
		 * 2) The expressivity is Full.
		 * 
		 */

		boolean result = false;
		try {
			/* Handler that doesn't care about OWLFullExceptions */
			syntaxLevel = LITE;
			OWLRDFErrorHandler handler = new OWLRDFErrorHandler() {
				public void owlFullConstruct(int code, String message) throws SAXException {
					/*
					 * We know that there's some syntactic Full stuff going on,
					 * but we don't necessarily want to throw an exception as
					 * there may be stuff that comes up later that pushed us out
					 * of Full, e.g. malformed RDF.
					 */
					setSyntaxLevel(FULL);
					explain(FULL, code, message);
				}

				public void error(String message) throws SAXException {
					throw new SAXException(message);
				}

				public void warning(String message) throws SAXException {
					message(message.toString());
				}

				public void owlFullConstruct(int code, String message,
						Object obj) throws SAXException {
					// TODO Auto-generated method stub

				}
			};
			OWLOntology o = parseFromURI(handler, uri);
			int species = species(o, true) | syntaxLevel;
			result = (species == DL || species == LITE || species == FULL);
			// releaseOntology( o );
		} catch (ParserException ex) {
			explain(OTHER, UNKNOWN, ex.getMessage());
		} catch (OWLException ex) {
			explain(OTHER, UNKNOWN, ex.getMessage());
		}
		return result;

	}

	/**
	 * Returns <code>true</code> if the ontology is OWL-Lite. Will report
	 * findings to the reporter as it goes.
	 * 
	 * 
	 * @param ontology
	 *            an <code>OWLOntology</code> value
	 * @return a <code>boolean</code> value
	 * @exception OWLException
	 *                if an error occurs
	 */
	public boolean isOWLLite(OWLOntology ontology) throws OWLException {
		return species(ontology, true) == LITE;
	}

	/**
	 * Returns <code>true</code> if the ontology is OWL-DL. Will report
	 * findings to the reporter as it goes.
	 * 
	 * @param ontology
	 *            an <code>OWLOntology</code> value
	 * @return a <code>boolean</code> value
	 * @exception OWLException
	 *                if an error occurs
	 */
	public boolean isOWLDL(OWLOntology ontology) throws OWLException {
		int species = species(ontology, true);
		return (species == LITE || species == DL);
	}

	/**
	 * Returns <code>true</code> if the ontology is OWL-Full. Will report
	 * findings to the reporter as it goes.
	 * 
	 * @param ontology
	 *            an <code>OWLOntology</code> value
	 * @return a <code>boolean</code> value
	 * @exception OWLException
	 *                if an error occurs
	 */
	public boolean isOWLFull(OWLOntology ontology) throws OWLException {
		int species = species(ontology, true);
		return (species == LITE || species == DL || species == FULL);
	}

	/**
	 * Gather togther all the URIs that are used by this ontology.
	 * 
	 */
	private void gatherURIs() throws OWLException {
		/* Initialise the collections. */
		this.classURIs = new HashSet();
		this.individualURIs = new HashSet();
		this.objectPropertyURIs = new HashSet();
		this.dataPropertyURIs = new HashSet();
		this.annotationPropertyURIs = new HashSet();
		this.datatypeURIs = new HashSet();
		this.allURIs = new HashSet();

		/* Collect together all the URIs */
		for (Iterator it = allOntologies.iterator(); it.hasNext();) {
			OWLOntology onto = (OWLOntology) it.next();
			for (Iterator cit = onto.getClasses().iterator(); cit.hasNext();) {
				OWLNamedObject entity = (OWLNamedObject) cit.next();
				classURIs.add(entity.getURI());
				allURIs.add(entity.getURI());
			}
			for (Iterator cit = onto.getIndividuals().iterator(); cit.hasNext();) {
				OWLNamedObject entity = (OWLNamedObject) cit.next();
				individualURIs.add(entity.getURI());
				allURIs.add(entity.getURI());
			}
			for (Iterator cit = onto.getObjectProperties().iterator(); cit
					.hasNext();) {
				OWLNamedObject entity = (OWLNamedObject) cit.next();
				objectPropertyURIs.add(entity.getURI());
				allURIs.add(entity.getURI());
			}
			for (Iterator cit = onto.getDataProperties().iterator(); cit
					.hasNext();) {
				OWLNamedObject entity = (OWLNamedObject) cit.next();
				dataPropertyURIs.add(entity.getURI());
				allURIs.add(entity.getURI());
			}
			for (Iterator cit = onto.getAnnotationProperties().iterator(); cit
					.hasNext();) {
				OWLNamedObject entity = (OWLNamedObject) cit.next();
				annotationPropertyURIs.add(entity.getURI());
				allURIs.add(entity.getURI());
			}
			for (Iterator cit = onto.getDatatypes().iterator(); cit.hasNext();) {
				OWLDataType entity = (OWLDataType) cit.next();
				datatypeURIs.add(entity.getURI());
				allURIs.add(entity.getURI());
			}
		}
	}

	/**
	 * Check that namespace separation has been correctly obeyed. In other
	 * words, no URI has been used as both an individual and class name, or
	 * property and class name etc.
	 */
	private void checkNamespaceSeparation() {

		try {
			/* Check that the collections are all disjoint */
			for (Iterator it = classURIs.iterator(); it.hasNext();) {
				URI u = (URI) it.next();
				if (individualURIs.contains(u)) {
					namespacesSeparated = false;
					explain(FULL, SEPARATIONVIOLATION, encodeHLink(
							u.toString(), myModel.shortForm(u))
							+ "\t used as Class and Individual");
				} else if (objectPropertyURIs.contains(u)) {
					namespacesSeparated = false;
					explain(FULL, SEPARATIONVIOLATION, encodeHLink(
							u.toString(), myModel.shortForm(u))
							+ "\t used as Class and ObjectProperty");
				} else if (dataPropertyURIs.contains(u)) {
					namespacesSeparated = false;
					explain(FULL, SEPARATIONVIOLATION, encodeHLink(
							u.toString(), myModel.shortForm(u))
							+ "\t used as Class and DataProperty");
				} else if (annotationPropertyURIs.contains(u)) {
					namespacesSeparated = false;
					explain(FULL, SEPARATIONVIOLATION, encodeHLink(
							u.toString(), myModel.shortForm(u))
							+ "\t used as Class and AnnotationProperty");
				} else if (datatypeURIs.contains(u)) {
					namespacesSeparated = false;
					explain(FULL, SEPARATIONVIOLATION, encodeHLink(
							u.toString(), myModel.shortForm(u))
							+ "\t used as Class and Datatype");
				}
			}

			for (Iterator it = individualURIs.iterator(); it.hasNext();) {
				URI u = (URI) it.next();
				if (objectPropertyURIs.contains(u)) {
					namespacesSeparated = false;
					explain(FULL, SEPARATIONVIOLATION, encodeHLink(
							u.toString(), myModel.shortForm(u))
							+ "\t used as Individual and Property");
				} else if (dataPropertyURIs.contains(u)) {
					namespacesSeparated = false;
					explain(FULL, SEPARATIONVIOLATION, encodeHLink(
							u.toString(), myModel.shortForm(u))
							+ "\t used as Individual and DataProperty");
				} else if (annotationPropertyURIs.contains(u)) {
					namespacesSeparated = false;
					explain(FULL, SEPARATIONVIOLATION, encodeHLink(
							u.toString(), myModel.shortForm(u))
							+ "\t used as Individual and AnnotationProperty");
				} else if (datatypeURIs.contains(u)) {
					namespacesSeparated = false;
					explain(FULL, SEPARATIONVIOLATION, encodeHLink(
							u.toString(), myModel.shortForm(u))
							+ "\t used as Individual and Datatype");
				}
			}

			for (Iterator it = objectPropertyURIs.iterator(); it.hasNext();) {
				URI u = (URI) it.next();
				if (dataPropertyURIs.contains(u)) {
					namespacesSeparated = false;
					explain(FULL, SEPARATIONVIOLATION, encodeHLink(
							u.toString(), myModel.shortForm(u))
							+ "\t used as ObjectProperty and DataProperty");
				} else if (annotationPropertyURIs.contains(u)) {
					namespacesSeparated = false;
					explain(
							FULL,
							SEPARATIONVIOLATION,
							encodeHLink(u.toString(), myModel.shortForm(u))
									+ "\t used as ObjectProperty and AnnotationProperty");
				} else if (datatypeURIs.contains(u)) {
					namespacesSeparated = false;
					explain(FULL, SEPARATIONVIOLATION, encodeHLink(
							u.toString(), myModel.shortForm(u))
							+ "\t used as ObjectProperty and Datatype");
				}
			}

			for (Iterator it = dataPropertyURIs.iterator(); it.hasNext();) {
				URI u = (URI) it.next();
				if (annotationPropertyURIs.contains(u)) {
					namespacesSeparated = false;
					explain(FULL, SEPARATIONVIOLATION, encodeHLink(
							u.toString(), myModel.shortForm(u))
							+ "\t used as DataProperty and AnnotationProperty");
				} else if (datatypeURIs.contains(u)) {
					namespacesSeparated = false;
					explain(FULL, SEPARATIONVIOLATION, encodeHLink(
							u.toString(), myModel.shortForm(u))
							+ "\t used as DataProperty and Datatype");
				}
			}

			for (Iterator it = annotationPropertyURIs.iterator(); it.hasNext();) {
				URI u = (URI) it.next();
				if (datatypeURIs.contains(u)) {
					namespacesSeparated = false;
					explain(FULL, SEPARATIONVIOLATION, encodeHLink(
							u.toString(), myModel.shortForm(u))
							+ "\t used as AnnotationProperty and Datatype");
				}
			}
			// namespacesSeparated = true;
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return;
	}

	/**
	 * Check the level of class axioms. Involves checking whether things like
	 * disjoint axioms have been used.
	 */
	private void checkClassAxioms() throws OWLException {
		classAxiomLevel = LITE;
		/* Grab all the axioms and check their level */
		for (Iterator it = allOntologies.iterator(); it.hasNext();) {
			OWLOntology onto = (OWLOntology) it.next();
			for (Iterator axit = onto.getClassAxioms().iterator(); axit.hasNext();) {
				OWLObject oo = (OWLObject) axit.next();

				SwoopSpeciesValidatorVisitor visitor = new SwoopSpeciesValidatorVisitor(
						this, objectRenderer);
				try {
					oo.accept(visitor);

					classAxiomLevel = classAxiomLevel | visitor.getLevel();
				} catch (OWLException ex) {
					classAxiomLevel = OTHER;
				}
			}
		}
	}

	/**
	 * Check that all individuals have been given at least one type.
	 */
	private void checkIndividualTyping() throws OWLException {
		try {
			/* Grab all the individuals and check their typing */
			Set allIndividuals = new HashSet();
			for (Iterator it = allOntologies.iterator(); it.hasNext();) {
				OWLOntology onto = (OWLOntology) it.next();
				for (Iterator indit = onto.getIndividuals().iterator(); indit.hasNext();) {
					allIndividuals.add(indit.next());
				}
			}

			for (Iterator it = allIndividuals.iterator(); it.hasNext();) {
				OWLIndividual i = (OWLIndividual) it.next();
				if (i.getTypes(allOntologies).size() == 0) {
					individualsTyped = false;
					URI uri = i.getURI();
					explain(FULL, UNTYPEDINDIVIDUAL,
							"Individual with no explicit type: "
							+ encodeHLink(uri.toString(), myModel.shortForm(uri)));
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			throw new OWLException(ex.getMessage());
		}
	}

	private void checkPropertyAxioms() throws OWLException {
		/*
		 * There isn't really anything to do here. Property axioms can be in all
		 * species
		 */
		propertyAxiomLevel = LITE;
	};

	private void checkExpressivity() throws OWLException {
		try {
			/*
			 * Here, we need to look at all the expressions used anywhere within
			 * the ontology and check their level.
			 */

			/* For each ontology, we need to check everything within it */
			expressivityLevel = LITE;
			SwoopExpressionValidatorVisitor evv = new SwoopExpressionValidatorVisitor(
					this, objectRenderer);

			for (Iterator it = allOntologies.iterator(); it.hasNext();) {
				OWLOntology onto = (OWLOntology) it.next();
				for (Iterator cit = onto.getClasses().iterator(); cit.hasNext();) {
					OWLClass clazz = (OWLClass) cit.next();
					if (!clazz.getEnumerations(onto).isEmpty()) {
						/* We're in DL. */
						expressivityLevel = expressivityLevel | DL;
						URI uri = clazz.getURI();
						explain(DL, ONEOF, "Enumeration used: "
								+ encodeHLink(uri.toString(), myModel.shortForm(uri)));
					}
					for (Iterator superit = clazz.getSuperClasses(onto).iterator(); 
							superit.hasNext();) {
						/* Check the expressivity of any superclasses */

						/*
						 * Tricky bit here -- if there's an intersection used at
						 * the top level, we're still ok for LITE. This is *not*
						 * currently catered for, so we will get some stuff
						 * wrong.
						 */
						OWLDescription description = (OWLDescription) superit.next();
						evv.reset();
						evv.setTopLevelDescription(true);
						try {
							description.accept(evv);
							expressivityLevel = expressivityLevel | evv.getLevel();
						} catch (OWLException ex) {
							explain(OTHER, UNKNOWN, ex.getMessage());
							expressivityLevel = OTHER;
						}
					}
					for (Iterator superit = clazz.getEquivalentClasses(onto).iterator(); 
							superit.hasNext();) {
						/* Check the expressivity of any equivalences */
						/*
						 * This is tricky, as these expressions *can* be
						 * intersections, as long as they're intersections of
						 * Lite constructs. This is the only place that it can
						 * happen in Lite.
						 */
						OWLDescription description = (OWLDescription) superit.next();
						evv.reset();
						evv.setTopLevelDescription(true);
						try {
							description.accept(evv);
							expressivityLevel = expressivityLevel | evv.getLevel();
						} catch (OWLException ex) {
							explain(OTHER, UNKNOWN, ex.getMessage());
							expressivityLevel = OTHER;
						}
					}
				}
				for (Iterator iit = onto.getObjectProperties().iterator(); iit.hasNext();) {
					OWLObjectProperty op = (OWLObjectProperty) iit.next();

					for (Iterator dit = op.getDomains(onto).iterator(); dit.hasNext();) {
						/* Check the expressivity of any equivalences */
						OWLDescription description = (OWLDescription) dit.next();
						evv.reset();
						try {
							description.accept(evv);
							expressivityLevel = expressivityLevel | evv.getLevel();
						} catch (OWLException ex) {
							explain(OTHER, UNKNOWN, ex.getMessage());
							expressivityLevel = OTHER;
						}
					}
					for (Iterator dit = op.getRanges(onto).iterator(); dit.hasNext();) {
						/* Check the expressivity of any equivalences */
						OWLDescription description = (OWLDescription) dit.next();
						evv.reset();
						try {
							description.accept(evv);
							expressivityLevel = expressivityLevel | evv.getLevel();
						} catch (OWLException ex) {
							explain(OTHER, UNKNOWN, ex.getMessage());
							expressivityLevel = OTHER;
						}
					}
				}

				for (Iterator iit = onto.getDataProperties().iterator(); iit.hasNext();) {
					OWLDataProperty dp = (OWLDataProperty) iit.next();

					for (Iterator dit = dp.getDomains(onto).iterator(); dit.hasNext();) {
						/* Check the expressivity of any equivalences */
						OWLDescription description = (OWLDescription) dit.next();
						evv.reset();
						try {
							description.accept(evv);
							expressivityLevel = expressivityLevel | evv.getLevel();
						} catch (OWLException ex) {
							explain(OTHER, UNKNOWN, ex.getMessage());
							expressivityLevel = OTHER;
						}
					}
					for (Iterator dit = dp.getRanges(onto).iterator(); dit
							.hasNext();) {
						/* Check the expressivity of any equivalences */
						OWLDataRange description = (OWLDataRange) dit.next();
						evv.reset();
						try {
							description.accept(evv);
							expressivityLevel = expressivityLevel | evv.getLevel();
						} catch (OWLException ex) {
							explain(OTHER, UNKNOWN, ex.getMessage());
							expressivityLevel = OTHER;
						}
					}
				}

				for (Iterator iit = onto.getIndividuals().iterator(); iit.hasNext();) {
					OWLIndividual ind = (OWLIndividual) iit.next();

					for (Iterator typeit = ind.getTypes(onto).iterator(); typeit.hasNext();) {
						/* Check the expressivity of any equivalences */
						OWLDescription description = (OWLDescription) typeit.next();

						evv.reset();
						try {
							description.accept(evv);
							expressivityLevel = expressivityLevel | evv.getLevel();
						} catch (OWLException ex) {
							explain(OTHER, UNKNOWN, ex.getMessage());
							expressivityLevel = OTHER;
						}
					}
				}
			}

			Set complexProperties = evv.getComplexProperties();
			/*
			 * Gather all the properties that are known to be functional or
			 * inverse functional
			 */
			for (Iterator it = allOntologies.iterator(); it.hasNext();) {
				OWLOntology onto = (OWLOntology) it.next();
				for (Iterator pit = onto.getObjectProperties().iterator(); pit.hasNext();) {
					OWLObjectProperty prop = (OWLObjectProperty) pit.next();
					if (prop.isFunctional(onto) || prop.isInverseFunctional(onto)) {
						complexProperties.add(prop);
					}
				}
			}

			/*
			 * We aren't doing everything yet as we still need to grab those
			 * that have complex superproperties.
			 */

			/*
			 * Now check to see if they've been said to be transitive, in which
			 * case we're in FULL.
			 */
			for (Iterator pit = complexProperties.iterator(); pit.hasNext();) {
				OWLObjectProperty prop = (OWLObjectProperty) pit.next();
				for (Iterator it = allOntologies.iterator(); it.hasNext();) {
					OWLOntology onto = (OWLOntology) it.next();
					if (prop.isTransitive(onto)) {
						expressivityLevel = FULL;
						URI uri = prop.getURI();
						explain(FULL, COMPLEXTRANSITIVE, "Complex property "
								+ encodeHLink(uri.toString(), myModel.shortForm(uri))
								+ " asserted to be transitive.");
					}
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			throw new OWLException(ex.getMessage());
		}
	}

	private void checkCorrectOWLUsage() throws OWLException {
		/* Check that nothing in the OWL vocabulary has been redefined. */
		/*
		 * This was too strong. We are actually allowed to say that some things
		 * e.g. Bag, Set, Alt and Statement are classes. Also some other stuff
		 * in the RDF vocabulary. SKB.
		 */
		try {
			for (Iterator it = classURIs.iterator(); it.hasNext();) {
				URI uri = (URI) it.next();
				if (uri != null && reservedVocabulary.contains(uri.toString())) {
					if (!classOnlyVocabulary.contains(uri.toString())) {
						/*
						 * It's a redefinition of something that we can't
						 * redefine.
						 */
						correctOWLUsage = false;
						explain(FULL, BUILTINREDEFINITION, "Redefinition of: "
								+ encodeHLink(uri.toString(), myModel.shortForm(uri)));
					}
				}
			}
			for (Iterator it = individualURIs.iterator(); it.hasNext();) {
				URI uri = (URI) it.next();
				if (uri != null && reservedVocabulary.contains(uri.toString())) {
					/* It's a redefinition of something that we can't redefine. */
					correctOWLUsage = false;
					explain(FULL, BUILTINREDEFINITION, "Redefinition of: "
							+ encodeHLink(uri.toString(), myModel.shortForm(uri)));
				}
			}
			for (Iterator it = objectPropertyURIs.iterator(); it.hasNext();) {
				URI uri = (URI) it.next();
				if (uri != null && reservedVocabulary.contains(uri.toString())) {
					/* It's a redefinition of something that we can't redefine. */
					correctOWLUsage = false;
					explain(FULL, BUILTINREDEFINITION, "Redefinition of: "
							+ encodeHLink(uri.toString(), myModel.shortForm(uri)));
				}
			}
			for (Iterator it = dataPropertyURIs.iterator(); it.hasNext();) {
				URI uri = (URI) it.next();
				if (uri != null && reservedVocabulary.contains(uri.toString())) {
					/* It's a redefinition of something that we can't redefine. */
					correctOWLUsage = false;
					explain(FULL, BUILTINREDEFINITION, "Redefinition of: "
							+ encodeHLink(uri.toString(), myModel.shortForm(uri)));
				}
			}
			for (Iterator it = datatypeURIs.iterator(); it.hasNext();) {
				URI uri = (URI) it.next();
				if (uri != null
						&& reservedVocabulary.contains(uri.toString())
						||
						/*
						 * Nasty. Need to check that thing/nothing aren't
						 * redefined as datatypes.
						 */
						uri.toString().equals(OWLVocabularyAdapter.INSTANCE.getThing())
						|| uri.toString().equals(OWLVocabularyAdapter.INSTANCE.getNothing())) {
					/* It's a redefinition of something that we can't redefine. */
					correctOWLUsage = false;
					explain(FULL, BUILTINREDEFINITION, "Redefinition of: "
							+ encodeHLink(uri.toString(), myModel.shortForm(uri)));
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			throw new OWLException(ex.getMessage());
		}
	}

	private void checkCorrectOWLNamespaceUsage() throws OWLException {
		/* Check that nothing's been defined in the OWL namespace. */
		try {
			for (Iterator it = allURIs.iterator(); it.hasNext();) {
				URI uri = (URI) it.next();
				if (uri != null) {
					String str = uri.toString();
					if (str.startsWith(OWLVocabularyAdapter.OWL)
							&& !str.equals(OWLVocabularyAdapter.INSTANCE.getThing())
							&& !str.equals(OWLVocabularyAdapter.INSTANCE.getNothing())
							&&
							/* Added check for built ins */
							!OWLVocabularyAdapter.INSTANCE.getAnnotationProperties().contains(str)) {
						correctOWLNamespaceUsage = false;
						explain(FULL, OWLNAMESPACEUSED, 
								encodeHLink(str, myModel.shortForm(uri))
								+ " in OWL Namespace");
					}
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			throw new OWLException(ex.getMessage());
		}
	}

	/*
	 * Returns a readable string corresponding to the error or validation code
	 */
	public static String readableCode(int code) {
		switch (code) {
		case OWLRDFErrorConstants.OTHER:
			return "Unknown RDF Error";
		case OWLRDFErrorConstants.UNTYPED_CLASS:
			return "Untyped Class";
		case OWLRDFErrorConstants.UNTYPED_PROPERTY:
			return "Untyped Property";
		case OWLRDFErrorConstants.UNTYPED_INDIVIDUAL:
			return "Untyped Individual";
		case OWLRDFErrorConstants.UNTYPED_ONTOLOGY:
			return "Untyped Ontology";
		case OWLRDFErrorConstants.UNTYPED_DATATYPE:
			return "Untyped Datatype";
		case OWLRDFErrorConstants.UNTYPED_URI:
			return "Untyped URI";
		case OWLRDFErrorConstants.MALFORMED_LIST:
			return "Malformed List";
		case OWLRDFErrorConstants.INVERSE_FUNCTIONAL_DATA_PROPERTY:
			return "Inverse Functional Data Property";
		case OWLRDFErrorConstants.UNSPECIFIED_FUNCTIONAL_PROPERTY:
			return "Unspecified Functional Property";
		case OWLRDFErrorConstants.STRUCTURE_SHARING:
			return "Structure Sharing";
		case OWLRDFErrorConstants.CYCLICAL_BNODES:
			return "Cyclical BNodes";
		case OWLRDFErrorConstants.MULTIPLE_DEFINITIONS:
			return "Multiple Definitions";
		case OWLRDFErrorConstants.MALFORMED_RESTRICTION:
			return "Malformed Restriction";
		case OWLRDFErrorConstants.MALFORMED_DESCRIPTION:
			return "Malformed Description";
		case OWLRDFErrorConstants.UNUSED_TRIPLES:
			return "Unused Triples";
		case OWLRDFErrorConstants.ILLEGAL_SUBPROPERTY:
			return "Illegal Sub Property";
		case OWLRDFErrorConstants.MALFORMED_IMPORT:
			return "Malformed Import";
		case UNKNOWN:
			return "Unknown";
		case INTERSECTION:
			return "Intersection";
		case UNION:
			return "Union";
		case COMPLEMENT:
			return "Complement";
		case ZEROONECARDINALITY:
			return "0/1 Cardinality";
		case CARDINALITY:
			return "Cardinality";
		case ONEOF:
			return "One Of";
		case DATATYPE:
			return "DataType";
		case DATARANGE:
			return "DataRange";
		case SUBCLASS:
			return "SubClass";
		case EQUIVALENCE:
			return "Equivalence";
		case DISJOINT:
			return "Disjoint";
		case PARTIAL:
			return "Partial";
		case COMPLETE:
			return "Complete";
		case SUBPROPERTY:
			return "SubProperty";
		case EQUIVALENTPROPERTY:
			return "EquivalentProperty";
		case INVERSE:
			return "Inverse";
		case TRANSITIVE:
			return "Transitive";
		case SYMMETRIC:
			return "Symmetric";
		case FUNCTIONAL:
			return "Functional";
		case INVERSEFUNCTIONAL:
			return "InverseFunctional";
		case INDIVIDUALS:
			return "Individuals";
		case RELATEDINDIVIDUALS:
			return "RelatedIndividuals";
		case INDIVIDUALDATA:
			return "IndividualData";
		case SAMEINDIVIDUAL:
			return "SameIndividual";
		case DIFFERENTINDIVIDUAL:
			return "DifferentIndividuals";
		case SEPARATIONVIOLATION:
			return "Name Separation Violated";
		case UNTYPEDINDIVIDUAL:
			return "Untyped Individual";
		case COMPLEXTRANSITIVE:
			return "Complex Transitive Property";
		case BUILTINREDEFINITION:
			return "Redefinition of Builtin Vocabulary";
		case OWLNAMESPACEUSED:
			return "Definition in OWL Namespace";
		case EXPRESSIONINAXIOM:
			return "Expression used in Axiom";
		case EXPRESSIONINRESTRICTION:
			return "Expression used in Restriction";
		}
		return "---";
	}

	private String encodeHLink(String uri_string, String name)
			throws URISyntaxException {
		// System.out.println("linking: >>"+uri_string);
		//URI uri = new URI(uri_string);
		return ("<a href=" + StringEscapeUtils.escapeHtml(uri_string) + ">" 
				+ StringEscapeUtils.escapeHtml(name) + "</a>");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.semanticweb.owl.validation.SpeciesValidator#isOWLLite(java.io.Reader,
	 *      java.net.URI)
	 */
	public boolean isOWLLite(Reader r, URI physicalURI) throws OWLException {
		// TODO Auto-generated method stub
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.semanticweb.owl.validation.SpeciesValidator#isOWLDL(java.io.Reader,
	 *      java.net.URI)
	 */
	public boolean isOWLDL(Reader r, URI physicalURI) throws OWLException {
		// TODO Auto-generated method stub
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.semanticweb.owl.validation.SpeciesValidator#isOWLFull(java.io.Reader,
	 *      java.net.URI)
	 */
	public boolean isOWLFull(Reader r, URI physicalURI) throws OWLException {
		// TODO Auto-generated method stub
		return false;
	}
}

/*
 * public void render(OWLOntology ontology, SwoopModel swoopModel, Writer
 * writer) throws RendererException {
 * 
 * myModel = swoopModel;
 * 
 * PrintWriter out = new PrintWriter(writer);
 * 
 * SpeciesValidator sv = null; try { sv = new SpeciesValidator();
 *  } catch (OWLException e1) { throw new RendererException(e1.getMessage()); }
 * StringWriter lw = new StringWriter(); StringWriter dw = new StringWriter();
 * StringWriter fw = new StringWriter(); StringWriter rw = new StringWriter();
 * StringWriter mw = new StringWriter(); final PrintWriter lpw = new
 * PrintWriter(lw); final PrintWriter dpw = new PrintWriter(dw); final
 * PrintWriter fpw = new PrintWriter(fw); final PrintWriter rpw = new
 * PrintWriter(rw); final PrintWriter mpw = new PrintWriter(mw);
 * 
 * final StringBuffer level = new StringBuffer();
 * 
 * sv.setReporter(new SpeciesValidatorReporter() { public void
 * ontology(OWLOntology onto) { }
 * 
 * public void done(String str) { level.setLength(0); level.append(str); }
 * 
 * public void message(String str) { mpw.println( "<li>" + reformatInHTML(str) + "</li>");
 * //mpw.println("<li>" + str + "</li>"); }
 * 
 * public void explain(int l, String str) { switch (l) { case
 * SpeciesValidator.LITE : lpw.println( "<li>" + reformatInHTML(str) + "</li>");
 * //lpw.println("<li>" + (str) + "</li>"); break; case SpeciesValidator.DL :
 * dpw.println( "<li>" + reformatInHTML(str) + "</li>"); //dpw.println("<li>" +
 * (str) + "</li>"); break; case SpeciesValidator.FULL : fpw.println( "<li>" +
 * reformatInHTML(str) + "</li>"); //fpw.println("<li>" + (str) + "</li>");
 * break; } }
 * 
 * public void explain(int l, int code, String str) { switch (l) { case
 * SpeciesValidator.LITE : lpw.println( "<li>" + reformatInHTML(str) + "</li>");
 * //lpw.println("<li>" + (str) + "</li>"); break; case SpeciesValidator.DL :
 * dpw.println( "<li>" + reformatInHTML(str) + "</li>"); //dpw.println("<li>" +
 * (str) + "</li>"); break; case SpeciesValidator.FULL : fpw.println( "<li>" +
 * reformatInHTML(str) + "</li>"); //fpw.println("<li>" + (str) + "</li>");
 * break; } } });
 * 
 * try { int l = SpeciesValidator.LITE; // check for OWL lite so we get all the
 * messages sv.isOWLLite(ontology);
 * 
 * out.println("<FONT FACE=\"Verdana\" SIZE=2>"); out.println("<b>DL
 * Expressivity:</b> " + swoopModel.getReasoner().getExpressivity()+"<br>");
 * out.println("<p><b>Level: " + level +"</b>");
 * 
 * //out.println( "<li>"+"asdf adfd "+ encodeHLink("http://www.google.com",
 * "http://www.google.com") + "</li>");
 * 
 * 
 * if ((l < SpeciesValidator.LITE) && !lw.toString().equals("")) { out.println("<ul>");
 * out.println( reformatInHTML(lw.toString()) ); //out.println( lw.toString() );
 * out.println("</ul>"); } // end of if () if ((l < SpeciesValidator.DL) &&
 * !dw.toString().equals("")) { out.println("<ul>"); out.println(
 * reformatInHTML(dw.toString()) ) ; //out.println( dw.toString() ) ;
 * out.println("</ul>"); } // end of if () if ((l < SpeciesValidator.FULL) &&
 * !fw.toString().equals("")) { out.println("<ul>"); out.println(
 * reformatInHTML(fw.toString()) ); //out.println( fw.toString()); //
 * System.out.println(fw.toString() ); out.println("</ul>"); } // end of if ()
 * if (!mw.toString().equals("")) { out.println("<b>Additional Messages</b>");
 * out.println("<ul>"); out.println( reformatInHTML(mw.toString()) );
 * //out.println( mw.toString() ); out.println("</ul>"); } // end of if ()
 * 
 * out.println("</FONT>");
 *  } catch (Exception e) { out.println("Exception: " + e.getMessage());
 * e.printStackTrace(); } // end of try-catch
 *  }
 * 
 * 
 * public void setOptions(Map options) { }
 * 
 * 
 * public Map getOptions() { return null; }
 * 
 * 
 * private String reformatInHTML(String source) { source =
 * source.replaceAll(SPACE, " "); try{ StringTokenizer tokens = new
 * StringTokenizer(source); String result = ""; while (tokens.hasMoreTokens()) {
 * String token = tokens.nextToken(); String temp = ""; for (int i = 0; i <
 * POPULAR_SCHEME_NAMES.length; i++) { int index = -1; if ((index =
 * token.indexOf(POPULAR_SCHEME_NAMES[i])) != -1) { String head =
 * token.substring(0, index); String tail = token.substring(index); String tip =
 * "";
 * 
 * int x = 0; int y = 0; int z = 0; int w = 0; int ind = Integer.MAX_VALUE; if
 * ((x = tail.indexOf("<")) != -1) ind = Math.min(ind, x); //if ((y =
 * tail.indexOf(")")) != -1) // ind = Math.min(ind, y); //if ((z =
 * tail.indexOf("(")) != -1) // ind = Math.min(ind, z); if ((w =
 * tail.indexOf(">")) != -1) ind = Math.min(ind, w);
 * 
 * if (ind != Integer.MAX_VALUE) { tail = tail.substring(0, ind); if (ind == y)
 * tip = ")"; }
 * 
 * temp = head + encodeHLink(tail, myModel.shortForm(new URI(tail))) + tip;
 * break; } temp = token; } result = result + SPACE + temp; } return result ; }
 * catch (URISyntaxException ex) { ex.printStackTrace(); }
 * 
 * return source; // exception has occurred. No 'pretty printing' is returned }
 * 
 * private String encodeHLink(String uri_string, String name) throws
 * URISyntaxException { //System.out.println("linking: >>"+uri_string); URI uri =
 * new URI(uri_string); return "<a href="+uri+">"+name+"</a>"; }
 */

