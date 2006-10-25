//The MIT License
//
// Copyright (c) 2004 Mindswap Research Group, University of Maryland, College Park
//
// Permission is hereby granted, free of charge, to any person obtaining a copy
// of this software and associated documentation files (the "Software"), to
// deal in the Software without restriction, including without limitation the
// rights to use, copy, modify, merge, publish, distribute, sublicense, and/or
// sell copies of the Software, and to permit persons to whom the Software is
// furnished to do so, subject to the following conditions:
//
// The above copyright notice and this permission notice shall be included in
// all copies or substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
// FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS
// IN THE SOFTWARE.

package org.mindswap.swoop;

import java.awt.Font;
import java.awt.GridLayout;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URI;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TimeZone;
import java.util.Timer;
import java.util.prefs.Preferences;

import javax.swing.ButtonGroup;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import org.mindswap.swoop.annotea.Description;
import org.mindswap.swoop.change.ChangeLog;
import org.mindswap.swoop.change.SwoopChange;
import org.mindswap.swoop.reasoner.PelletReasoner;
import org.mindswap.swoop.reasoner.SwoopReasoner;
import org.mindswap.swoop.reasoner.SwoopToldReasoner;
import org.mindswap.swoop.renderer.entity.AbstractSyntaxEntityRenderer;
import org.mindswap.swoop.renderer.entity.RDFXMLEntityRenderer;
import org.mindswap.swoop.utils.RulesExpressivity;
import org.mindswap.swoop.utils.SwoopCache;
import org.mindswap.swoop.utils.SwoopStatistics;
import org.mindswap.swoop.utils.WSPolicy;
import org.mindswap.swoop.utils.change.BooleanElementChange;
import org.mindswap.swoop.utils.change.EnumElementChange;
import org.mindswap.swoop.utils.change.RevertCheckpointChange;
import org.mindswap.swoop.utils.change.SaveCheckpointChange;
import org.mindswap.swoop.utils.owlapi.CorrectedRDFRenderer;
import org.mindswap.swoop.utils.owlapi.DefaultShortFormProvider;
import org.mindswap.swoop.utils.owlapi.LabelShortFormProvider;
import org.mindswap.swoop.utils.owlapi.OWLDescriptionFinder;
import org.mindswap.swoop.utils.owlapi.OWLEntityRemover;
import org.mindswap.swoop.utils.owlapi.OWLOntBuilder;
import org.mindswap.swoop.utils.owlapi.QNameShortFormProvider;
import org.mindswap.swoop.utils.owlapi.diff.FilteredDiff;
import org.mindswap.swoop.utils.ui.SwingWorker;
import org.semanticweb.owl.impl.model.OWLConnectionImpl;
import org.semanticweb.owl.impl.model.OWLDataFactoryImpl;
import org.semanticweb.owl.impl.model.OWLDataPropertyInstanceImpl;
import org.semanticweb.owl.impl.model.OWLDataPropertyRangeAxiomImpl;
import org.semanticweb.owl.impl.model.OWLFunctionalPropertyAxiomImpl;
import org.semanticweb.owl.impl.model.OWLIndividualTypeAssertionImpl;
import org.semanticweb.owl.impl.model.OWLInverseFunctionalPropertyAxiomImpl;
import org.semanticweb.owl.impl.model.OWLInversePropertyAxiomImpl;
import org.semanticweb.owl.impl.model.OWLObjectPropertyInstanceImpl;
import org.semanticweb.owl.impl.model.OWLObjectPropertyRangeAxiomImpl;
import org.semanticweb.owl.impl.model.OWLPropertyDomainAxiomImpl;
import org.semanticweb.owl.impl.model.OWLSubPropertyAxiomImpl;
import org.semanticweb.owl.impl.model.OWLSymmetricPropertyAxiomImpl;
import org.semanticweb.owl.impl.model.OWLTransitivePropertyAxiomImpl;
import org.semanticweb.owl.io.ShortFormProvider;
import org.semanticweb.owl.io.abstract_syntax.AbstractOWLParser;
import org.semanticweb.owl.io.owl_rdf.OWLRDFErrorHandler;
import org.semanticweb.owl.io.owl_rdf.OWLRDFParser;
import org.semanticweb.owl.model.OWLAnd;
import org.semanticweb.owl.model.OWLAnnotationInstance;
import org.semanticweb.owl.model.OWLBooleanDescription;
import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLClassAxiom;
import org.semanticweb.owl.model.OWLDataFactory;
import org.semanticweb.owl.model.OWLDataProperty;
import org.semanticweb.owl.model.OWLDataRange;
import org.semanticweb.owl.model.OWLDataValue;
import org.semanticweb.owl.model.OWLDescription;
import org.semanticweb.owl.model.OWLDifferentIndividualsAxiom;
import org.semanticweb.owl.model.OWLDisjointClassesAxiom;
import org.semanticweb.owl.model.OWLEntity;
import org.semanticweb.owl.model.OWLEnumeration;
import org.semanticweb.owl.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owl.model.OWLException;
import org.semanticweb.owl.model.OWLIndividual;
import org.semanticweb.owl.model.OWLIndividualAxiom;
import org.semanticweb.owl.model.OWLNamedObject;
import org.semanticweb.owl.model.OWLNaryBooleanDescription;
import org.semanticweb.owl.model.OWLNot;
import org.semanticweb.owl.model.OWLObject;
import org.semanticweb.owl.model.OWLObjectProperty;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLOr;
import org.semanticweb.owl.model.OWLProperty;
import org.semanticweb.owl.model.OWLSameIndividualsAxiom;
import org.semanticweb.owl.model.OWLSubClassAxiom;
import org.semanticweb.owl.model.change.AddAnnotationInstance;
import org.semanticweb.owl.model.change.AddClassAxiom;
import org.semanticweb.owl.model.change.AddDomain;
import org.semanticweb.owl.model.change.AddEntity;
import org.semanticweb.owl.model.change.AddEnumeration;
import org.semanticweb.owl.model.change.AddEquivalentClass;
import org.semanticweb.owl.model.change.AddImport;
import org.semanticweb.owl.model.change.AddIndividualClass;
import org.semanticweb.owl.model.change.AddSuperClass;
import org.semanticweb.owl.model.change.AddSuperProperty;
import org.semanticweb.owl.model.change.ChangeVisitor;
import org.semanticweb.owl.model.change.OntologyChange;
import org.semanticweb.owl.model.change.RemoveClassAxiom;
import org.semanticweb.owl.model.change.RemoveEntity;
import org.semanticweb.owl.model.change.RemoveEnumeration;
import org.semanticweb.owl.model.change.RemoveEquivalentClass;
import org.semanticweb.owl.model.change.RemoveImport;
import org.semanticweb.owl.model.change.SetFunctional;
import org.semanticweb.owl.model.helper.OntologyHelper;
import org.semanticweb.owl.util.OWLConnection;
import org.xml.sax.SAXException;

public class SwoopModel implements ShortFormProvider {
    
	final public static int ALL            = 0;
	final public static int INDIVIDUALS    = 1;
	final public static int CLASSES        = 2;
	final public static int PROPERTIES     = 3;
	final public static int DATAPROPERTIES = 4;
	final public static int OBJPROPERTIES  = 5;
	final public static int IMPORTED_ONT  = 6;
	final public static int BASE_ONT  = 7;
	final public static int TRANSCLOSE_ONT  = 8;
	final public static int RDFXML_SER = 9;
	final public static int JAVA_SER = 10;
	//**********************************************
	//Added for Econnections
	//**********************************************
	final public static int FOREIGN_ENT = 11;
	//***********************************************
	final public static int GCI = 12;
	
	private boolean show_imports = true; // show imported ontologies in class/prop tree
    private boolean showQNames = false; // show QNames
    private boolean showDivisions = true; // show divisions in the entity renderer pane
    private boolean showInherited = false; // show inherited properties and individuals   
    private boolean enableAutoSaveChkPts = false; // enable auto-save checkpoints
    private boolean logChanges = true; // log all changes
    private boolean enableChangeLogDisplay = true; // enable change log display
    private boolean enableDebugging = false; // enable ontology debugging (class expressions, pellet)
    private boolean enableAutoRetrieve = false; // enable auto-retrieve of Annotea annotations whenever entity selection changes
    private boolean highlightCode = true; // highlight entity in source code
    private boolean enableRules = false; // enable definition rules
    public static int changeSharingMethod = RDFXML_SER; // change sharing method for Ontology Change sets (RDF/XML or Java objects)
    private int fontSize = 3;
    private String fontFace = "Verdana";
    private String treeThreshold = "200"; // number below this - tree is fully expanded
    private String termListFilter = "Show All";
    private boolean showChangeAnnotBar = false; // show Changes/Annotations sidebar
    private boolean openLastWorkspace = false; // open last workspace when Swoop starts
    private String lastWorkspaceURL = "";
    private boolean saveCheckPtsDisk = false;
    private boolean showCheckPts = true;
    private SwoopReasoner reasoner;
    private String defaultReasonerName;
    private Map ontologies;
    private Map ontologySettings; // key - uri, value - List of settings (imports, qnames, reasoner) in that order
    protected OWLEntity selectedEntity;
    protected OWLOntology selectedOntology;
    protected OWLNamedObject selectedOWLObject;
    private List listeners;
    private ShortFormProvider shortForms;
    private List uncommittedChanges, committedChanges;    
    private boolean editorEnabled;
    private Set changedOntologies; // ontologies changed at each check point (gets reset and created during apply changes)
    public List bufferedChanges; // changes in the uncommitted list get saved to this buffer just before uncommitted is emptied (gets reset and created during apply changes)
    private Set annotatedObjectURIs; // set of all OWLNamedObjects URI's that have one or more Annotea annotations on them
    private Hashtable ResourceAnnotHash;
    private boolean showIcons = false; // show Icons in renderer pane
    private URI currentlyLoadingURI = null;
    private boolean lookupAllOnts = false; // search across all ontologies
    private boolean viewOWLVocabularyAsRDF = false;
    private boolean viewRDFVocabularyAsRDF = false;
    private SwoopCache classTreeCache, propTreeCache; // cache for class/property trees for ontologies
    private SwoopCache annotationCache; // cache for Annotea annotations
    private SwoopCache changesCache; // cache for committed ontology changes
    private SwoopCache reasonerCache; // cache for reasoners
    private HashMap reasonerMap; // key - reasoner name, value - SwoopReasoner instance
    private File wkspcFile; // last workspace file used
    private HashMap classExprHash; // hash for class expressions
    private String userName = ""; // used for version control, general provenance info etc
    public HashMap changeMap; // map for ontology changes (used in changelog)
    public HashMap versionRepository; // map with key: repositoryURI, value: array of Description objects, each corresponding to version commit (first being header)
    public String previousTimeStamp = ""; 
    public SwoopFrame myFrame = null; // keeps track of the frame the model resides in.  Use getFrame/setFrame to access the frame.  Required so entityRenderer can have access to TermsDisplay from SwoopFrame. 
    private boolean debugGlass = false, debugBlack = false;
    private HashMap ontStatMap; // hashmap for storing ontology statistics
	private boolean useLabels; // display entities using rdfs:labels
	private String useLanguage = ""; // display entities in this language
	private String proxyHost = ""; // proxy host
	private String proxyPort = ""; // proxy port
	private boolean useHTTPProxy = false; // use HTTP proxy
	public boolean useTableau = false, findAllMUPS = false; // advanced SOS options
	private boolean autoSaveWkspc = false; // option to save workspace automatically
	private float saveWkspcTime = 1; 
	private String saveWkspcFile = "recentWkspc.swp";
	private Timer swoopTimer; // timer to carry out operations at regular intervals
	public boolean repairColor = false; // FOR Repair Purposes
	public HashSet repairSet = new HashSet(); // FOR Repair Purposes
	public RulesExpressivity ruleExpr = null;
	private Preferences preferences;
	
	//Added for Segmentation
	public Map segmentation;//A mapping from each ontology object to its segmentation object.
	//
	
    public SwoopModel() {
        
    	ontologies = new Hashtable();
    	ontologySettings = new HashMap();
    	listeners = new ArrayList();    	
    	reasoner = new SwoopToldReasoner();
    	defaultReasonerName = "No Reasoner";
    	shortForms = new QNameShortFormProvider();
    	uncommittedChanges = new ArrayList();
    	committedChanges = new ArrayList();
    	annotatedObjectURIs = new HashSet();
    	ResourceAnnotHash = new Hashtable();
    	classTreeCache = new SwoopCache();
    	propTreeCache = new SwoopCache();
    	annotationCache = new SwoopCache();
    	changesCache = new SwoopCache();
    	reasonerCache = new SwoopCache();
    	reasonerMap = new HashMap();
    	classExprHash = new HashMap();
    	changeMap = new HashMap();
    	changedOntologies = new HashSet();
    	versionRepository = new HashMap();
    	
    	//Added for Segmentation
    	segmentation = new HashMap();
    	//
    	
    	ontStatMap = new HashMap();  
    	preferences = Preferences.userNodeForPackage(this.getClass());
//    	**************************************
		// Added for rules
		//**************************************
		ruleExpr = new RulesExpressivity(this);
		//Initializing prolog, consulting programs	
//		if (ruleExpr.USE_PROLOG_ENGINE) {
//		//	Query q1 = new Query("consult('~/rules/allog/preallogPP.qlf')");
//
//			Query q1 = new Query("consult('allog/preallogPP.pl')");
//			System.out.println("consult q1 " + (q1.hasSolution() ? "succeeded" : "failed"));
//			q1 = new Query("compile");
//			System.out.println("consult q1 " + (q1.hasSolution() ? "succeeded" : "failed"));
//		}
	}

    
    public Map getSegmentation(){
    	return segmentation;
    }
    
    public void setSegmentation(Map seg){
    	segmentation = seg;
    }
    
    public void addAnnotatedObjectURI(URI annObjURI) {
    	this.annotatedObjectURIs.add(annObjURI);
    }
    
    public Set getAnnotatedObjectURIs() {
    	return this.annotatedObjectURIs;
    }
    
    public void setAnnotatedObjectURIs(Set annotURIs) {
    	this.annotatedObjectURIs = annotURIs;
    }
    
    public boolean getEnableAutoSaveChkPts() {
    	return this.enableAutoSaveChkPts;
    }
    
    public boolean getEnableChangeLogDisplay() {
    	return this.enableChangeLogDisplay;
    }
    
    public boolean getEnableRules() {
		return enableRules;
	}

    
    public boolean getEnableAutoRetrieve() {
    	return this.enableAutoRetrieve;
    }
   
    public void setEnableAutoRetrieve(boolean mode) {
    	this.enableAutoRetrieve = mode;
    	notifyListeners(new ModelChangeEvent(this, ModelChangeEvent.AUTORETRIEVE_CHANGED, null));
    }
    
    public void setEnableAutoSaveChkPts(boolean mode, boolean notify) {
    	
    	this.enableAutoSaveChkPts = mode;
    	if (notify) notifyListeners(new ModelChangeEvent(this, ModelChangeEvent.ENABLED_CHECKPOINT, null));
    }
    
    public void setEnableChangeLogDisplay(boolean mode) {
    	this.enableChangeLogDisplay = mode;
    	notifyListeners(new ModelChangeEvent(this, ModelChangeEvent.ENABLED_CHANGELOG, null));	
    }
    
    public boolean getEditorEnabled() {
    	return editorEnabled;
    }
    
    public boolean getEnableDebugging() {
    	return enableDebugging;
    }
    public void setEnableRules(boolean mode) {
		enableRules = mode;
		notifyListeners(new ModelChangeEvent(this,
				ModelChangeEvent.RULES_CHANGED, selectedOntology));
	}
    public boolean getShowDivisions() {
    	return showDivisions;
    }
    
    public URI getCurrentlyLoadingURI() {
    	return currentlyLoadingURI;
    }
    
    public void setCurrentlyLoadingURI(URI uri) {
    	this.currentlyLoadingURI = uri;
    }
    
    public void setEditorEnabled(boolean mode) {
    	editorEnabled = mode;
    	notifyListeners(new ModelChangeEvent(this, ModelChangeEvent.EDITABLE_CHANGED, selectedOntology));    	
    }
    
    public void setEnableDebugging(boolean mode) {
    	enableDebugging = mode;    	
    	notifyListeners(new ModelChangeEvent(this, ModelChangeEvent.DEBUGGING_CHANGED, selectedOntology));    	
    }
    
    public void setShowIcons(boolean mode, boolean notify) {
    	showIcons = mode;
    	if (notify) notifyListeners(new ModelChangeEvent(this, ModelChangeEvent.EDITABLE_CHANGED, selectedOntology));    	
    }
    
    public void setShowDivisions(boolean mode) {
    	showDivisions = mode;
    	notifyListeners(new ModelChangeEvent(this, ModelChangeEvent.EDITABLE_CHANGED, selectedOntology));    	
    }
    
    public List getUncommittedChanges() {
    	if (uncommittedChanges==null) uncommittedChanges = new ArrayList();
    	return uncommittedChanges;
    }
    
    public Set getEntityAnnotations(OWLEntity entity) {
    	return (HashSet) ResourceAnnotHash.get(entity);
    }
    
    public void setEntityAnnotations(OWLEntity entity, Set annotations) {
    	ResourceAnnotHash.put(entity, annotations);
    }
    
    /* Note: set*Changes(..) should not add to changesCache, only add*Changes(..) should */
    public List setUncommittedChanges(Collection changes) {
    	uncommittedChanges.clear();
    	uncommittedChanges.addAll(changes);
    	notifyListeners(new ModelChangeEvent(this, ModelChangeEvent.RESET_CHANGE, null));
    	return uncommittedChanges;
    }
    
    public List addUncommittedChanges(Collection changes) {
    	return addUncommittedChanges(changes, true);
    }
    
    public List addUncommittedChanges(Collection changes, boolean notify) {
    	uncommittedChanges.addAll(changes);
    	for (Iterator iter = changes.iterator(); iter.hasNext();) {
    		OntologyChange change = (OntologyChange) iter.next();
    		processChange(change, false);
    	}
    	if (notify) notifyListeners(new ModelChangeEvent(this, ModelChangeEvent.ADDED_CHANGE, null));
    	return uncommittedChanges;
    	
    }
    
    public List addUncommittedChange(OntologyChange change) {
    	uncommittedChanges.add(change);    	
    	processChange(change, false);
    	notifyListeners(new ModelChangeEvent(this, ModelChangeEvent.ADDED_CHANGE, null));
    	return uncommittedChanges;
    }
    
    /**
     * Process the ontology change using ChangeLog.java to obtain 
     * a string description of the change and URIs of subjects of the change.
     * Then create an instance of SwoopChange and add author/timestamp
     * and if committed or not.
     * Finally add swoopChange to changesCache
     */
    public void processChange(OntologyChange change, boolean isCommitted) {
    	try {
	    	// create instance of ChangeLog
	    	ChangeLog clog = new ChangeLog(null, this);
	    	
	    	// call method to obtain description of change and subject URIs of change
	    	List subjURIs = new ArrayList();
	    	List extraSubj = new ArrayList();
	    	String desc = clog.getChangeInformation(change, ChangeLog.CHANGE_DESCRIPTION, null, subjURIs, null).toString();
	    	/* also add ontology of change to subjURIs */
	    	if (change.getOntology()!=null) {
	    		if (!subjURIs.contains(change.getOntology().getURI())) subjURIs.add(change.getOntology().getURI());
	    		// but save copy of uris minus ontology to be used below
		    	extraSubj = new ArrayList(subjURIs);
		    	extraSubj.remove(change.getOntology().getURI());
	    	}
	    	
	    	// add timestamp and author from SwoopModel
	    	String ts = this.getTimeStamp();
	    	String author = this.getUserName();
	    	
	    	// create new instance of SwoopChange for each subject of change
	    	for (Iterator iter = subjURIs.iterator(); iter.hasNext();) {
	    		
	    		URI owlObjectURI = (URI) iter.next();
	    		
	    		// determine if change is checkpoint related or not
	    		boolean chkptRelated = false;
	    		if (change instanceof SaveCheckpointChange || change instanceof RevertCheckpointChange) {
	    			chkptRelated = true;
	    		}
	    		
	    		/* create instanceof SwoopChange passing necessary args */
	    		SwoopChange swc = new SwoopChange(author, owlObjectURI, change, ts, desc, isCommitted, chkptRelated);
	    		
	    		// add extra subject URIs for all changes
	    		// by removing current entity name
	    		List extra = new ArrayList(extraSubj);
	    		extra.remove(owlObjectURI);
	    		swc.setExtraSubjects(extra);
	    		
	    		changesCache.addChange(owlObjectURI, swc);
	    		
	    		// also if change is committed, remove any matching uncommited changes if any
	    		//TODO make this faster
	    		if (isCommitted) {
	    			changesCache.removeOntologyChange(owlObjectURI, change, false);
	    		}
	    	}
    	}
    	catch (OWLException ex) {
    		ex.printStackTrace();
    	}
    }
    
    public List addCommittedChange(OntologyChange change) {
    	if (isLogChanges()) {
	    	committedChanges.add(change);
	    	// process change to get subject and description and create a new instance of swoopchange
	    	processChange(change, true);
	    	notifyListeners(new ModelChangeEvent(this, ModelChangeEvent.ADDED_CHANGE, null));
    	}
    	return committedChanges;
    }
    
    public List addCommittedChanges(List changes) {
    	if (isLogChanges()) {
	    	committedChanges.addAll(changes);
	    	for (int i=0; i<changes.size(); i++) {
	    		processChange((OntologyChange) changes.get(i), true);
	    	}
	    	notifyListeners(new ModelChangeEvent(this, ModelChangeEvent.ADDED_CHANGE, null));
    	}
    	return committedChanges;
    }

    /**
     * Apply a boolean element change i.e. Add/Remove a Intersection/Union/Not element
     * Useful since it allows changes to these sets at a greater granularity than the default OWL-API (add/remove entire intersection/..)
     * @param oc
     */
    public boolean applyBooleanElementChange(OntologyChange oc) {
    	
    	try {
    		BooleanElementChange change = (BooleanElementChange) oc;
    		
    		// break up composite change into components
    		OWLOntology ont = change.getOntology();
    		changedOntologies.add(ont);    				
    		OWLClass cla = change.getOWLClass();
    		OWLDescription desc = change.getOWLDescription();
    		Class type = change.getType();
    		
    		if (type.equals(OWLNot.class)) {
    			// handle ADD/REMOVE of complements    			
    			if (change.getChangeType().equals("Add")) {
    				// add complement class
    				OWLNot compClass = ont.getOWLDataFactory().getOWLNot(desc);
    				AddEquivalentClass chng = new AddEquivalentClass(ont, cla, compClass, null);
    				chng.accept((ChangeVisitor) ont);
    			}
    			else {
    				// remove complement class
    				// iterate through current complements and remove it    				
    				Set nots = cla.getEquivalentClasses(ont);
    				Iterator iter = nots.iterator();
    				RemoveEquivalentClass chng = null;
    				while (iter.hasNext()) {
    					OWLDescription equClass = (OWLDescription) iter.next();
    					if (equClass instanceof OWLNot) {
    						if (((OWLNot) equClass).getOperand().equals(desc)) {
    							chng = new RemoveEquivalentClass(ont, cla, equClass, null);
        						break;
    						}
    					}
    				}    				 
    				if (chng!=null) chng.accept((ChangeVisitor) ont);
    			}
    		}
    		else {
    			// handle ADD/REMOVE of intersections/unions
    			
	    		// and create two atomic changes using components
	    		// first delete any existing boolean set
	    		// while storing existing boolean set operands
	    		Set newBoolSet = new HashSet();
	    		Set set = type.equals(OWLAnd.class) ?
	    				OWLDescriptionFinder.getIntersections(cla, cla.getOntologies()):
	    				OWLDescriptionFinder.getUnions(cla, cla.getOntologies());    						
	    		if(!set.isEmpty()) {
	    			OWLNaryBooleanDescription boolDesc = (OWLNaryBooleanDescription) set.iterator().next();
	    			if (type.equals(OWLAnd.class)) newBoolSet.addAll(((OWLAnd) boolDesc).getOperands());
	    			else newBoolSet.addAll(((OWLOr) boolDesc).getOperands());
	    			RemoveEquivalentClass change1 = new RemoveEquivalentClass(ont, cla, boolDesc, null);
	    			change1.accept((ChangeVisitor) ont);
	    		}
	    		
	    		// now add desc to set and create new boolean    				
	    		if (change.getChangeType().equals("Add")) newBoolSet.add(desc);
	    		else if (newBoolSet.contains(desc)) newBoolSet.remove(desc);
	    		
	    		//*** Only add description if new boolean set has *some* element
	    		if (newBoolSet.size()>0) {
		    		OWLDataFactory ontDF = ont.getOWLDataFactory();	    		
		    		OWLBooleanDescription newBool = null;
		    		if (type.equals(OWLAnd.class)) newBool = ontDF.getOWLAnd(newBoolSet);
		    		else newBool = ontDF.getOWLOr(newBoolSet);
		    		AddEquivalentClass change2 = new AddEquivalentClass(ont, cla, newBool, null);
		    		change2.accept((ChangeVisitor) ont);
	    		}
    		}
    		return true;
    	}
    	catch (OWLException e) {
    		e.printStackTrace();
    	}
    	return false;
    }
    
    /**
     * Same as ApplyBooleanElementChange except this handles enumerations (owl:oneOf)
     * @param oc
     */
    public boolean applyEnumElementChange(OntologyChange oc) {
    	
    	try {
    		EnumElementChange change = (EnumElementChange) oc;
    		
    		// break up composite change into components
    		OWLOntology ont = change.getOntology();
    		changedOntologies.add(ont);    				
    		OWLClass cla = change.getOWLClass();
    		OWLIndividual ind = change.getOWLIndividual();
    		
    		// and create two atomic changes using components
    		// first delete any existing enumeration
    		// while storing existing enum set operands
    		Set newEnumSet = new HashSet();
    		Set set = OWLDescriptionFinder.getEnumerations(cla, cla.getOntologies());
    		    						
    		if(!set.isEmpty()) {
    			OWLEnumeration enumElem = (OWLEnumeration) set.iterator().next();
    			newEnumSet.addAll(enumElem.getIndividuals());
    			RemoveEnumeration change1 = new RemoveEnumeration(ont, cla, enumElem, null);
    			change1.accept((ChangeVisitor) ont);
    		}
    		
    		// now add ind to set and create new enumSet
    		if (change.getChangeType().equals("Add")) newEnumSet.add(ind);
    		else if (newEnumSet.contains(ind)) newEnumSet.remove(ind);
    		
    		if (newEnumSet.size()>0) {
	    		OWLDataFactory ontDF = ont.getOWLDataFactory();
	    		OWLEnumeration newEnum = null;
	    		newEnum = ontDF.getOWLEnumeration(newEnumSet);
	    		AddEnumeration change2 = new AddEnumeration(ont, cla, newEnum, null);
	    		change2.accept((ChangeVisitor) ont);
    		}
    		return true;
    	}
    	catch (OWLException e) {
    		e.printStackTrace();
    	}
    	return false;
    }
    
    
    public void applyOntologyChanges() {
    	this.applyOntologyChanges(true, true);
    }
    
    public void applyOntologyChanges(boolean notify, boolean logger) {
    	
    	try {
    		Iterator i = uncommittedChanges.iterator();
    		changedOntologies = new HashSet();
    		bufferedChanges = new ArrayList(uncommittedChanges);
    		
    		while(i.hasNext()) {
    			OntologyChange oc = (OntologyChange) i.next(); 
    			
    			// handle special composite changes separately
    			if (oc instanceof BooleanElementChange) { 
    				applyBooleanElementChange(oc);
    			}
    			else if (oc instanceof EnumElementChange) { 
    				applyEnumElementChange(oc);
    			}
    			else {
    				OWLOntology ont = oc.getOntology();
    				oc.accept((ChangeVisitor) ont);
        			changedOntologies.add(ont);
    			}
    			
    			processChange(oc, true);
    			
    			//*** need to remove all associated reasoner instances for that ontology
    			// when a change occurs..
        		reasonerCache.removeReasoners(oc.getOntology());
        		// also remove ontStats so that it is automatically recomputed
        		this.removeOntStats(oc.getOntology());
    		}
    		
    		if(reasoner != null && selectedOntology != null) {

    			final SwoopModel model = this;
    			final boolean notifyF = notify;
    			final boolean loggerF = logger;
				
    			SwingWorker worker = new SwingWorker() {
					boolean fail = false;
					public Object construct() {
						try {
							reasoner.setOntology(model.selectedOntology);							
						} catch (Exception ex) {
							fail = true;
							if( ex != null )
							    throw new RuntimeException(ex.getMessage());
							else
							    throw new RuntimeException( "Unexpected error" );
						}	
						return null;
					}
					public void finished() {
						if (fail) {
							try {
								// remove all reasoners from cache
								model.reasonerCache.removeReasoners(model.selectedOntology);
								reasoner = null; // this will prevent it from being added to the cache again in call to model.setReasoner() below
								SwoopReasoner noReasoner = (SwoopReasoner) SwoopToldReasoner.class.newInstance();
								// select No-Reasoner
								model.setReasoner(noReasoner);
							} 
							catch (Exception e) {
								e.printStackTrace();
							}
							notifyListeners(new ModelChangeEvent(model, ModelChangeEvent.REASONER_FAIL));
						}
						else {
							// proceed as normal - logging changes if no cancellation/exception
							if (loggerF && model.isLogChanges()) committedChanges.addAll(uncommittedChanges);
				    		uncommittedChanges = new ArrayList();
				    		
				    		if (notifyF) notifyListeners(new ModelChangeEvent(model, ModelChangeEvent.ONTOLOGY_CHANGED, changedOntologies));
				    		bufferedChanges = new ArrayList();
						}
					}
				};
				worker.start();
    		}
    		    		
    	}
    	catch (Exception ex) {
    		ex.printStackTrace();
    	}				
    }
    
    public List getCommittedChanges() {
    	return committedChanges;
    }
    
    /* Note: set*Changes(..) should not add to changesCache, only add*Changes(..) should */ 
    public void setCommittedChanges(Collection allChanges) {
    	committedChanges.clear();
    	committedChanges.addAll(allChanges);
    	notifyListeners(new ModelChangeEvent(this, ModelChangeEvent.RESET_CHANGE, null));
    }
    
    /**
     * Adds an arbitrary set of ontologies to the SwoopModel
     * Currently used by the partitioning code to add the partitions back to Swoop
     * @param onts
     */
    public void addOntologies(Collection onts) {
    	Iterator i = onts.iterator();
    	while(i.hasNext()) {
			OWLOntology ont = (OWLOntology) i.next();
			try {
				ontologies.put(ont.getURI(), ont);
			} catch (OWLException e) {
				e.printStackTrace();
			}
		}
    	notifyListeners(new ModelChangeEvent(this, ModelChangeEvent.ONTOLOGY_LOADED, null));
    }
    
    /**
	 * Add an ontology to the SwoopModel Also adds the imports closure of the
	 * ontology to the SwoopModel Saves current ontology-specific user settings
	 * (imports, qnames, reasoner) Also loads settings if any (should load
	 * defaults always if the ontology being added is a new one)
	 * 
	 * @param onto
	 * @throws OWLException
	 */
	public void addOntology(OWLOntology onto) throws OWLException {
		// get imports closure
		Iterator i = OntologyHelper.importClosure(onto).iterator();
		while(i.hasNext()) {
			OWLOntology ont = (OWLOntology) i.next();
			
			OWLOntology current = (OWLOntology) ontologies.get(ont.getLogicalURI());
			if ((current != null ) && !(current.getPhysicalURI().equals(ont.getPhysicalURI()))) {
				handleExistingOntology(ont, ont.getLogicalURI());
			} else {
				ontologies.put(ont.getURI(), ont);
				// update imported ontology settings in this case as well
				this.updateImportSettings(ont);
			}
		}
		
		// save ontology settings before changing selection to new ontology
		this.saveCurrentOntSettings();
		
		this.selectedOntology = onto;
		this.selectedEntity = null;
		this.selectedOWLObject = onto;
		
		// load ontology settings after selection has changed
		// dont need to loadOntSettings here because it will be done when
		// selection changes to loaded ontology later
//		loadOntSettings(onto); // this should always load defaults
		//*** instead set default settings for new ontology ***/
		this.show_imports = true;
		this.showQNames = false;
		this.reasoner = getDefaultReasoner();;
		
		notifyListeners(new ModelChangeEvent(this,
				ModelChangeEvent.ONTOLOGY_LOADED, onto));
	}
    
    public OWLOntology addOntology(URI uri) throws OWLException {
    	
    		OWLOntology ontology = this.loadOntology(uri);
		addOntology(ontology);
		return ontology;
    }

    /**
     * Generic function to load an ontology given a URI (web or local)
     * It checks the file extension (.txt, .rdf..) and calls the appropriate
     * OWL API lexer/parser (Abstract Syntax, RDF/XML) to parse the OWL Ontology
     * @param uri
     * @return
     */
    public OWLOntology loadOntology(URI uri) throws OWLException {
    	OWLOntology ontology = null;
    	
    	if (uri.toString().endsWith(".txt")) {  		
    		ontology = this.loadOntologyInAbstractSyntax(null, uri);
    	} else if (uri.toString().endsWith(".wsp")) { //ws-policy extension
    		// if it is a ws-policy file, use the XSLT to convert it to OWL    		
    		//pass the reader and the URI
    		//need to check whether it's a ws-policy file
    		
    		Reader reader = WSPolicy.transformPolicyFile(uri);    		    		
    		ontology = this.loadOntologyInRDF(reader, uri);
    	}
    	else {
    		ontology = this.loadOntologyInRDF(null, uri);
    	}
    	return ontology;
    }
    
    /**
     * Given an ontology, and a type of entity (OWLClass/ObjProp/DataProp/Individual),
     * return a set of these entities from the ontology (BASE_ONT) or its imported ontologies (IMPORTED_ONT) or its imports closure (TRANSCLOSE_ONT).
     * @param ont
     * @param ont_type
     * @param entity_type
     * @return
     */
	public Set getEntitySet(OWLOntology ont, int ont_type, int entity_type) {
		
		Set entitySet = new HashSet();
		try {
			if(ont.getIncludedOntologies() != null) {
	        
				Set inclOntSet = new HashSet();
				if (ont_type == TRANSCLOSE_ONT) inclOntSet = OntologyHelper.importClosure(ont);
				else if (ont_type == BASE_ONT) inclOntSet.add(ont);
				else if (ont_type == IMPORTED_ONT) {
					inclOntSet = OntologyHelper.importClosure(ont);
					inclOntSet.remove(ont);					
				}
					
	            Iterator iter = inclOntSet.iterator();
	            while(iter.hasNext()) {
	            	OWLOntology inclOnt = (OWLOntology) iter.next();
	            	switch (entity_type) {
	            		case ALL: 
	            		case CLASSES:
	            			Iterator claIter = inclOnt.getClasses().iterator();
	        				while (claIter.hasNext()) {
	        					OWLDescription desc = (OWLDescription) claIter.next();
	        					if (desc instanceof OWLClass) entitySet.add(desc);
	        				}	            			
	            			if (entity_type==CLASSES) break;
	            			
	            		case DATAPROPERTIES:
	            			entitySet.addAll(inclOnt.getDataProperties());
	            			if (entity_type==DATAPROPERTIES) break;
	            			
	            		case OBJPROPERTIES:
	            			entitySet.addAll(inclOnt.getObjectProperties());
	            			if (entity_type==OBJPROPERTIES) break;
	            		
	            		case PROPERTIES:
	            			entitySet.addAll(inclOnt.getObjectProperties());
	            			entitySet.addAll(inclOnt.getDataProperties());
	            			entitySet.addAll(inclOnt.getAnnotationProperties());
//	            			********************************************************
	    					//Added for Econnections
	    					//*********************************************************
	    					//Whenever an ontology A imports another ontology B, and A is
	    					// Econencted to B, then all the links from B to A have to be shown
	    					// As object properties in A, not as links
	    					//*******************************************************
	    					Iterator it = inclOnt.getObjectProperties().iterator();
	    					while (it.hasNext()){
	    						OWLEntity ent = (OWLEntity)it.next();
	    						if (ent instanceof OWLObjectProperty){
	    							if(((OWLObjectProperty)(ent)).isLink()){
	    							   	if(((OWLObjectProperty)(ent)).getLinkTarget().equals(ont.getURI())){
	    							   		OWLObjectProperty aux = (OWLObjectProperty)ent;
	    							   		aux.setLinkTarget(null);
	    							   	    entitySet.remove(ent);
	    							   	    entitySet.add(aux);
	    							   	}
	    							   	
	    							}
	    						}
	    						
	    					}
	    					
	    					//*********************************************************
	            			if (entity_type==PROPERTIES) break;
	            			
	            		case INDIVIDUALS:
	            			Iterator indIter = inclOnt.getIndividuals().iterator();
	        				while (indIter.hasNext()) {
	        					OWLIndividual ind = (OWLIndividual) indIter.next();
//	        					if (ind.getURI()!=null) 
	        					    entitySet.add(ind);
	        				}
	            			break;
	            			
	            		case GCI:
	            			for (iter = inclOnt.getClassAxioms().iterator(); iter.hasNext();) {
	            				OWLClassAxiom axiom = (OWLClassAxiom) iter.next();
	            				if (axiom instanceof OWLSubClassAxiom) {
	            					if (!(((OWLSubClassAxiom) axiom).getSubClass() instanceof OWLClass)) {
	            						entitySet.add(axiom);
	            					}
	            				}
	            			}
	            			break;
	            			
	            		//*****************************************************	
	            		//Added for Econnections	
	            		//*****************************************************
	            		case FOREIGN_ENT:	
	            			Iterator foreignIter = this.getSelectedOntology().getForeignEntities().keySet().iterator();
	            			while(foreignIter.hasNext()){
	            				OWLEntity ent = (OWLEntity)foreignIter.next();
	            				if(ent.getURI()!=null) entitySet.add(ent);
	            			}
	            			break;
	            	}
	            	  //*********************************************************
	            }
			}
		}
		catch (OWLException e) {
			e.printStackTrace();
		}
		return entitySet;
	}
    
	/**
	 * Load an OWL Ontology serialized in the OWL Abstract Syntax format given
	 * a reader (optional) and a URI (compulsory)
	 * @param reader
	 * @param uri
	 * @return
	 * @throws OWLException 
	 */
    public OWLOntology loadOntologyInAbstractSyntax(Reader reader, URI uri) throws OWLException {
    	OWLOntology ontology;
    	
    	
    	OWLConnection connection = new OWLConnectionImpl();
    	AbstractOWLParser parser = new AbstractOWLParser(connection);
	
    	if (reader!=null) {
    		ontology = parser.parseOntology(reader, uri);			
    	} else {
    		ontology = parser.parseOntology(uri);
    	}
    	
    	return ontology;
    }

    //*************************************************
    //Added for Econnections
    //*************************************************
    public boolean isEconnectedOntology(OWLOntology ont){
    	boolean b = false;
    	
    	try {
			Iterator it = ont.getObjectProperties().iterator();
			while(it.hasNext()){
				OWLObjectProperty prop = (OWLObjectProperty)it.next();
				if(prop.isLink())
					b=true;
			}
		} catch (OWLException e) {
			e.printStackTrace();
		}
    	
    	if(!ont.getForeignEntities().isEmpty()){
    		b=true;
    	}
    		
    	return b;
    }
 
    /**
     * Remove an ontology from the swoopModel given its uri.
     * Remove it from the ontologies hashMap
     * @param uri
     */
    public void removeOntology(URI uri) {
    	OWLOntology onto = getOntology(uri);
    	
    	this.selectedOntology = null;
    	
    	if(onto != null) {
    		ontologies.remove(uri);
    		clearCaches(onto);
    		this.changesCache.removeOntology(onto);
    	   	notifyListeners(new ModelChangeEvent(this, ModelChangeEvent.ONTOLOGY_REMOVED, uri));
    	}
    	
    	this.clearSelections();
    }
    
    /*
     * Clear the reasoner, change (maybe annotations?) cache
     * of SwoopModel for a specific ontology
     */
    public void clearCaches(OWLOntology ont) {
    	this.reasonerCache.removeReasoners(ont);
//		this.changesCache.removeOntology(ont);
//		this.annotationCache.remove(ont);
		this.removeOntStats(ont);
    }
    
    public OWLOntology getOntology(URI uri) {
    	return (OWLOntology) ontologies.get(uri);
    }
    
    public Hashtable getOntologiesMap() {
    	return (Hashtable) ontologies;
    }
    
    public Collection getOntologies() {
    	return ontologies.values();
    }
    
    public Set getOntologyURIs() {
    	return ontologies.keySet();
    }
    
    public void addListener(SwoopModelListener sml) {
    	listeners.add(sml);
    }
    
    public void removeListener(SwoopModelListener sml) {
    	listeners.remove(sml);
    }
    
    /**
     * Add an OWL entity (OWL Class/Property/Individual) to the ontology passed as arguments.
     * Also make the new entity a subclass/subproperty/instanceOf of an existing entity (parent)
     * @param ont
     * @param ent
     * @param parent
     */
    public void addEntity(OWLOntology ont, OWLEntity ent, OWLEntity parent) {
    	try {
    		AddEntity change = new AddEntity(ont, ent, null);
    		change.accept((ChangeVisitor) ont);
    		
    		// add parent entity change if parent!=null
    		if (parent!=null) {
    			if (parent instanceof OWLClass) {
    				if (ent instanceof OWLClass) {
	    				// add super class
	    				OWLClass parentCla = (OWLClass) parent;
	    				// OWLOntology parentOnt = (OWLOntology) parentCla.getOntologies().iterator().next();    				
	    				AddSuperClass supChange = new AddSuperClass(this.selectedOntology, (OWLClass) ent, parentCla, null);
	    				supChange.accept((ChangeVisitor) this.selectedOntology);
    				}
    				else if (ent instanceof OWLIndividual){
    					// add instance type
    					AddIndividualClass indChange = new AddIndividualClass(this.selectedOntology, (OWLIndividual) ent, (OWLClass) parent, null);
    					indChange.accept((ChangeVisitor) this.selectedOntology);
    				}
    			}
    			else if (parent instanceof OWLProperty) {
					// add super class
    				OWLProperty parentProp = (OWLProperty) parent;
    				// OWLOntology parentOnt = (OWLOntology) parentCla.getOntologies().iterator().next();    				
    				AddSuperProperty supChange = new AddSuperProperty(this.selectedOntology, (OWLProperty) ent, parentProp, null);
    				supChange.accept((ChangeVisitor) this.selectedOntology);
        		}
    		}
    		// use SwingWorker to process ontology using reasoner
    		this.useSwingWorker(new ModelChangeEvent(this, ModelChangeEvent.ADDED_ENTITY), false, ent, change);    		
    	}
    	catch (OWLException ex) {
    		ex.printStackTrace();
    	}
    }
    
    /**
     * Add a General Concept Inclusion (GCI) axiom to an Ontology
     * Pass the LHS/RHS Class Descriptions of the GCI
     * Reasoner is refreshed and listeners are notified
     */
    public void addGCI(OWLOntology ont, OWLDescription lhs, OWLDescription rhs) {
    	try {
			OWLSubClassAxiom axiom = ont.getOWLDataFactory().getOWLSubClassAxiom(lhs, rhs);
			AddClassAxiom change = new AddClassAxiom(ont, axiom, null);
			change.accept((ChangeVisitor) ont);
			
			// use SwingWorker to process ontology using reasoner
    		this.useSwingWorker(new ModelChangeEvent(this, ModelChangeEvent.ADDED_ENTITY), false, null, null);
			
    		//TODO: also record add GCI as a change
//    		this.addCommittedChange(change);
		}
		catch (OWLException ex) {
			ex.printStackTrace();
		}
    }
    
    /**
     * Remove an OWL Entity from the ontology
     * i.e. Remove all references of the entity from the ontology. This feature
     * is not available in the OWL-API which only removes the type assertion
     * @param ont
     * @param ent
     * @param allReferences
     */
    public void removeEntity(OWLOntology ont, OWLEntity ent, boolean allReferences) {
    	try {
    		RemoveEntity change = new RemoveEntity(ont, ent, null);
    		if (!allReferences) {
    			change.accept((ChangeVisitor) ont);
    		}
    		else {
    			OWLEntityRemover remover = new OWLEntityRemover(ont);
        		remover.removeEntity(ent);	
    		}
    		this.selectedEntity = null;
    		this.selectedOntology = ont;
    		this.selectedOWLObject = ont;
    		
    		// use SwingWorker to process ontology using reasoner
    		this.useSwingWorker(new ModelChangeEvent(this, ModelChangeEvent.REMOVED_ENTITY), false, null, change);    		
    	}
    	catch (OWLException ex) {
    		ex.printStackTrace();
    	}
    }
    
    /*
     * Remove a set of GCIs from the ontology
     */
    public void removeGCI(OWLOntology ont, Object[] gcis, boolean warning) {
    	
    	try {
    		int result = -1;
			
			if (warning) {
				String title = "Remove GCI(s) from Ontology";
				int options = JOptionPane.YES_NO_OPTION;
				result = JOptionPane.showConfirmDialog(null, "This is going to remove all selected GCIs from the Ontology. Continue?", title, options);
			}
			
			if(result==JOptionPane.YES_OPTION || !warning) {		
				
				for (int i=0; i<gcis.length; i++) {
					OWLSubClassAxiom remGCI = (OWLSubClassAxiom) gcis[i];
					RemoveClassAxiom remAxiom = new RemoveClassAxiom(ont, remGCI, null);
					remAxiom.accept((ChangeVisitor) ont);
				}
				
	    		this.selectedEntity = null;
	    		this.selectedOntology = ont;
	    		this.selectedOWLObject = ont;
	    		
	    		// use SwingWorker to process ontology using reasoner
	    		this.useSwingWorker(new ModelChangeEvent(this, ModelChangeEvent.REMOVED_ENTITY), false, null, null);
	    		
	    		//TODO: also record remove GCI as a change
//	    		this.addCommittedChange(change);
			}
    	}
    	catch (OWLException ex) {
    		ex.printStackTrace();
    	}
    }
    
    /**
     * 
     * Set the flag for showing ontologies imported by the selected ontology. The selected
     * reasoner is automatically refreshed by this function to load/unload the imported
     * ontologies based on the given parameter. an exception is thrown if the reasoner
     * fails with the new setting 
     * 
     * @param value
     * @throws OWLException
     */
    public void setShowImports(boolean value) throws OWLException {
    	show_imports = value;
    	
    	// use SwingWorker to process ontology using reasoner
    	this.useSwingWorker(new ModelChangeEvent(this, ModelChangeEvent.IMPORTS_VIEW_CHANGED), true, null, null);    	
    }
    
    public boolean getShowImports() { 
    	return show_imports;
	}
    
    public String shortForm(URI uri) {
    	if (uri==null) return "Anonymous Individual";
    	if (this.useLabels) {
    		LabelShortFormProvider lsfp = new LabelShortFormProvider(this);
    		return lsfp.shortForm(uri);
    	}
    	else 
    		return shortForms.shortForm(uri);
    }
    
    public ShortFormProvider getShortForms() {
        return shortForms;
    }
    
    public void notifyListeners(ModelChangeEvent event) {
    	Iterator i = listeners.iterator();
    	while(i.hasNext()) {
    		SwoopModelListener sml = (SwoopModelListener) i.next();
    		sml.modelChanged(event);
    	}
    }   

    /**
     * 
     * Changes the current selected ontology. The reasoner is loaded with this new ontology.
     * an exception is thrown if the reasoner cannot process this ontology.
     * 
     * @param ontology
     * @throws OWLException
     */
	public void setSelectedOntology(OWLOntology ontology) throws OWLException {
		
		// save ontology settings before changing selection to new ontology
		this.saveCurrentOntSettings();
		
		this.selectedOntology = ontology;
		this.selectedEntity = null;
		
		// load ontology settings after selection has changed
		loadOntSettings(ontology);
		
//		notifyListeners(new ModelChangeEvent(this, ModelChangeEvent.ONTOLOGY_SEL_CHANGED));		   
	}
   	
	/**
	 * Clear swoopModel selections i.e. selected Ontology, Entity and OWLObject
	 * also notify listeners via CLEAR_SELECTIONS ModelChange event
	 *
	 */
	public void clearSelections() {
		this.selectedOntology = null;
		this.selectedEntity = null;
		this.selectedOWLObject = null;
		ModelChangeEvent event = new ModelChangeEvent(this, ModelChangeEvent.CLEAR_SELECTIONS);
		notifyListeners(event);
	}
	
	public OWLOntology getSelectedOntology() {
		return selectedOntology;
	}
   	
	/**
	 * Return the currently selected object, whether it is an Entity or an Ontology.
	 * @return
	 */
	public OWLNamedObject getSelectedObject() {
		OWLNamedObject selected = getSelectedEntity();
		if (selected == null) {
			selected = getSelectedOntology();
		}
		return selected;
	}
	
	public void setSelectedEntity(OWLEntity entity) {
   		
		this.selectedEntity = entity;
   		ModelChangeEvent event = new ModelChangeEvent(this, ModelChangeEvent.ENTITY_SEL_CHANGED);
   		notifyListeners(event);  
   	}
   	
	/**
	 * Returns the currrently selected entity.  Returns null if an ontology is currently selected.
	 * @return 
	 */
   	public OWLEntity getSelectedEntity() {
   		return selectedEntity;
   	}
   	   	
   	/**
   	 * Sets the SwoopModel reasoner.
   	 * Also classifies the currently selected ontology
   	 * @param reasoner
   	 * @throws OWLException
   	 */
   	public void setReasoner(SwoopReasoner selReasoner) throws Exception {

   		// when changing reasoner selection for an ontology in the UI
   		// first save current reasoner selection
   	    if( selectedOntology != null && reasoner!=null)
   	        reasonerCache.putReasoner(selectedOntology, this.reasoner.getName(), this.reasoner);
   		
   	    final SwoopReasoner previousReasoner = this.reasoner; 
   	    
   		// now select new reasoner
   		boolean loadedReasonerFromCache = false;
   		// check if reasoner already exists in cache
   		SwoopReasoner newReasoner = ( selectedOntology != null )
   		    ? reasonerCache.getReasoner(selectedOntology, selReasoner.getName())
   		    : null;
   		if (newReasoner!=null) {
   			loadedReasonerFromCache = true;
   			System.out.println("Loaded "+newReasoner.getName()+" reasoner from cache..");
   		}
   		else {
   			// else create new instance of selected reasoner
   			Class cls = selReasoner.getClass();
   			newReasoner = (SwoopReasoner) cls.newInstance();
   			System.out.println("Created new "+newReasoner.getName()+" reasoner..");   			
   		}
   		
   		try {
			if(selectedOntology != null && !loadedReasonerFromCache) {
			    newReasoner.setDoExplanation(enableDebugging);
			    newReasoner.setLoadImports(this.show_imports, false); //don't refresh here

			    final SwoopModel model = this;
			    final SwoopReasoner reas = newReasoner;
			    
			    // use SwingWorker to process ontology using reasoner 
			    SwingWorker worker = new SwingWorker() {
			    	boolean fail = false;
					public Object construct() {
						try {
							reas.setOntology(selectedOntology);							
						} 
						catch (Exception ex) {
							fail = true;
							if( ex != null )
							    throw new RuntimeException(ex);
							else
							    throw new RuntimeException( "Unexpected error" );
						}	
						return null;
					}
					public void finished() {
						// do the UI update after reasoner processes the ontology
						if (fail) {
							// remove pellet from cache
							model.reasonerCache.removeReasonerOntology(model.selectedOntology, reas.getName());
							// set reasoner to previous one
							model.reasoner = previousReasoner;
							// make reasonerCombo select previous one
							notifyListeners(new ModelChangeEvent(model, ModelChangeEvent.REASONER_FAIL));
						}
						else {
							// set new reasoner
							model.reasoner = reas;
							notifyListeners(new ModelChangeEvent(model, ModelChangeEvent.REASONER_SEL_CHANGED));
						}																   	
					}
				};				
			    worker.start();
			}
			else {
				// notify regardless (if loaded from cache)
				this.reasoner = newReasoner;		   		
		   		notifyListeners(new ModelChangeEvent(this, ModelChangeEvent.REASONER_SEL_CHANGED));		   			
			}
		} 
   		catch (Throwable e) {
//			this.clearSelections();
			e.printStackTrace();
			throw new OWLException(e.getMessage());
		}   		   
   	}   	   	
   	
   	public SwoopReasoner getReasoner() {
   		return reasoner;
   	}
   	
	public boolean getShowQNames() {
		return showQNames;
	}

	public void setShowQNames(boolean showQNames) {
		this.showQNames = showQNames;
		if(showQNames)
			shortForms = new QNameShortFormProvider();
		else
			shortForms = new DefaultShortFormProvider();
		
		ModelChangeEvent event = new ModelChangeEvent(this, ModelChangeEvent.QNAME_VIEW_CHANGED);
		notifyListeners(event);  		
	}
	
	public boolean getUseLabels() {
		return this.useLabels;
	}
	
	public void setUseLabels(boolean useLabels) {
		this.useLabels = useLabels;
		ModelChangeEvent event = new ModelChangeEvent(this, ModelChangeEvent.QNAME_VIEW_CHANGED);
		notifyListeners(event);  		
	}
		
	public boolean getShowIcons() {
		return this.showIcons;
	}
	
	public void setShowInherited(boolean showInherited) {
		this.showInherited = showInherited;
		notifyListeners(new ModelChangeEvent(this, ModelChangeEvent.SHOW_INHERITED, selectedOntology));
	}
	
	public boolean getShowInherited() {
		return this.showInherited;
	}
	
	public String getTimeStamp() {
		Calendar cal = Calendar.getInstance(TimeZone.getDefault());
	    String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
	    java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat(DATE_FORMAT);
	    sdf.setTimeZone(TimeZone.getDefault());                    
	    String ts = sdf.format(cal.getTime()).toString();
	    if (ts.equals(previousTimeStamp)) {
	    	// incr. ts by 1s
	    	int s = Integer.parseInt(ts.substring(ts.lastIndexOf(":")+1, ts.length()));
	    	s++;
	    	ts = ts.substring(0, ts.lastIndexOf(":")) + ":" + String.valueOf(s);
	    }
	    previousTimeStamp = ts;
	    return ts;
	}
	
	/**
	 * Load RDF/XML from a reader directly with the given uri used as 
	 * the xml:base. Calls overriden method with the default setting for
	 * 'importing' - true. i.e. Imported ontologies are parsed in as well.
	 * @param reader
	 * @param uri
	 * @return
	 */
	public OWLOntology loadOntologyInRDF(Reader reader, URI uri) throws OWLException {
		return this.loadOntologyInRDF(reader, uri, true);
	}
	
	/**
	 * Load RDF/XML from a reader directly with the given uri used as 
	 * the xml:base
	 * 
	 * @param reader Source of the ontology
	 * @param uri Logical URI used as xml:base
	 * @return Loaded ontology
	 */
	public OWLOntology loadOntologyInRDF(Reader reader, URI uri, boolean importing) throws OWLException {
		OWLOntology ontology = null;

		
		OWLRDFParser parser = new OWLRDFParser();
		parser.setImporting(importing);
			
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

			public void owlFullConstruct(int code, String message, Object obj)
					throws SAXException {
			}
		});

		OWLConnection connection = new OWLConnectionImpl();
		parser.setConnection(connection);

		// logging parse time
		// System.out.println("parsing ontology in RDF: start time--
		// "+Calendar.getInstance().get(Calendar.HOUR)+":"+Calendar.getInstance().get(Calendar.MINUTE)+":"+Calendar.getInstance().get(Calendar.SECOND)+":"+Calendar.getInstance().get(Calendar.MILLISECOND));

		// PARSE THE ONTOLOGY!
		if (reader != null)
			ontology = parser.parseOntology(reader, uri);
		else
			ontology = parser.parseOntology(uri);

		// System.out.println("parsing ontology in RDF: end time--
		// "+Calendar.getInstance().get(Calendar.HOUR)+":"+Calendar.getInstance().get(Calendar.MINUTE)+":"+Calendar.getInstance().get(Calendar.SECOND)+":"+Calendar.getInstance().get(Calendar.MILLISECOND));
		
		return ontology;
	}
	
	/**
	 * Remove all changes in SwoopModel (uncommitted and committed lists) 
	 * that are associated with the ontology object passed to it
	 * @param reloadOnt - ontology whose changes are removed from SwoopModel
	 */
	public void removeChanges(OWLOntology reloadOnt) {
		
		try {
			// also remove any changes in ontologyChanges and allChangeLog pertaining to reloadOnt
			List newOntologyChanges = new ArrayList();
			Iterator iter = uncommittedChanges.iterator();
			while (iter.hasNext()) {
				OntologyChange change = (OntologyChange) iter.next();
				if (!change.getOntology().getURI().equals(reloadOnt.getURI())) {
					newOntologyChanges.add(change);
				}
			}
			uncommittedChanges.clear();
			uncommittedChanges.addAll(newOntologyChanges);
			
			List newAllChangeLog = new ArrayList();
			iter = committedChanges.iterator();
			while (iter.hasNext()) {
				OntologyChange change = (OntologyChange) iter.next();
				URI changeURI = null;
				if (change.getOntology()!=null) changeURI = change.getOntology().getURI();
				else {
					if (change instanceof SaveCheckpointChange) changeURI = ((SaveCheckpointChange) change).getOntologyURI();
					else if (change instanceof RevertCheckpointChange) changeURI = ((RevertCheckpointChange) change).getCheckpointChange().getOntologyURI();
				}
				if (!changeURI.equals(reloadOnt.getURI())) {
					newAllChangeLog.add(change);
				}
			}
			committedChanges.clear();
			committedChanges.addAll(newAllChangeLog);
			
			notifyListeners(new ModelChangeEvent(this, ModelChangeEvent.RESET_CHANGE, reloadOnt));
		}
		catch (OWLException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Reload an OWL Ontology in Swoop - replace the current entry in swoopModel 
	 * with the argument passed to the method. 
	 * 
	 * @param reloadOnt - new ontology to be reloaded
	 * @param resetChanges - if true, remove all changes corresponding to new ontology
	 */
	public void reloadOntology(OWLOntology reloadOnt, boolean resetChanges) {
		try {
			
			// remove entry from hashtable
			OWLOntology existingOnt = (OWLOntology) ontologies.get(reloadOnt.getURI());
			//System.out.println("found existing "+existingOnt.getURI());
			ontologies.remove(existingOnt);
			
			// make it the selected ontology only if the one it replaced was selected
			if (selectedOntology==existingOnt)
				selectedOntology = reloadOnt;
			
			// put new ontology in hashtable
			ontologies.put(reloadOnt.getURI(), reloadOnt);
			
			// need to reclassify ontology in reasoner 
			reasoner.setOntology(reloadOnt);
			
			if (resetChanges) {
				this.removeChanges(reloadOnt);
			}
			
			notifyListeners(new ModelChangeEvent(this, ModelChangeEvent.ONTOLOGY_RELOADED, reloadOnt));			 
			
		} catch (OWLException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * @return Returns the changeSharingMethod.
	 */
	public int getChangeSharingMethod() {
		return changeSharingMethod;
	}
	/**
	 * @param changeSharingMethod The changeSharingMethod to set.
	 */
	public void setChangeSharingMethod(int changeSharingMethod) {
		this.changeSharingMethod = changeSharingMethod;
	}
	
	
	/**
	 * Replace an entity in the ontology with a new version represented by its RDF/XML code definition
	 * This is also a String operation since the OWL-API has no clean way of doing this.
	 * It serializes the entire ontology in RDF/XML except for the entity definition, then inserts
	 * the new entity definition at the end and parses the RDF/XML back into a new ontology
	 * @param ont
	 * @param entity
	 * @param newEntityRDFCode
	 * @return
	 */
	public boolean replaceEntityRDF(OWLOntology ont, OWLEntity entity, String newEntityRDFCode) {
		try {
			// get rdf for oldEntity
			StringWriter st = new StringWriter();
			RDFXMLEntityRenderer rdfRend = new RDFXMLEntityRenderer();
			rdfRend.setEditorEnabled(true);
			rdfRend.render(entity, this, st);
			String oldEntity = st.toString();
			
			// parse both oldEntity and newEntity rdf into two separate OWLOntologies
			StringReader reader = new StringReader(oldEntity);
			OWLOntology source = this.loadOntologyInRDF(reader, ont.getURI());
			StringReader reader2 = new StringReader(newEntityRDFCode);
			OWLOntology destination = this.loadOntologyInRDF(reader2, ont.getURI());
			
			if (source==null || destination==null) return false;
			
			List diff = FilteredDiff.getChanges(source, destination, ont);
			this.addUncommittedChanges(diff);
			this.applyOntologyChanges();
			
			return true;
		}
		catch (OWLException ex) {
			ex.printStackTrace();
			return false;
		}		
	}
	
	/*
	 * clone an OWL Ontology object (by serializing & parsing RDF) 
	 */
	public OWLOntology cloneOntology(OWLOntology source) {
		OWLOntology copy = null;
		try {
			CorrectedRDFRenderer rdfRend = new CorrectedRDFRenderer();
			StringWriter st = new StringWriter();
			rdfRend.renderOntology(source, st);			
			copy = this.loadOntologyInRDF(new StringReader(st.toString()), source.getURI(), true);			
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
		return copy;
	}
	
	
	public boolean replaceEntityAS(OWLOntology ont, String newEntityASCode) {
		try {
//			 get rdf for oldEntity
			StringWriter st = new StringWriter();
			AbstractSyntaxEntityRenderer rdfRend = new AbstractSyntaxEntityRenderer();
			rdfRend.setEditorEnabled(true);
			rdfRend.render(this.selectedEntity, this, st);
			String oldEntity = st.toString();
			
			System.out.println("newEntityASCode:\n"+newEntityASCode);
			System.out.println("oldEntity:\n"+oldEntity);
			// parse both oldEntity and newEntity AS into two separate OWLOntologies
			StringReader reader = new StringReader(oldEntity);
			OWLOntology ont1 = this.loadOntologyInAbstractSyntax(reader, ont.getURI());
			StringReader reader2 = new StringReader(newEntityASCode);
			OWLOntology ont2 = this.loadOntologyInAbstractSyntax(reader2, ont.getURI());
			
			if (ont2==null) return false;
			
			List diff = FilteredDiff.getChanges(ont1, ont2, ont); //this.useOWLDifferentiator(ont, this.selectedEntity, ont, ont2);
			this.addUncommittedChanges(diff);
			this.applyOntologyChanges();
			
			return true;
		}
		catch (OWLException ex) {
			ex.printStackTrace();
		}
		return false;		
	}
	
	
	/**
	 * Returns the prefered font size.  THIS DOES NOT CORRESPOND TO POINT SIZE!.
	 * Use getFontPt() for that.
	 * @return
	 */
	public String getFontSize() {
		return new Integer(this.fontSize).toString();
	}
	
	/**
	 * Returns the preferred point size for fonts.
	 * @return
	 */
	public int getFontPt() {
		return 5+2*this.fontSize;
	}
	
	/**
	 * Set the font size.  The corresponding point size is 5+2*size
	 * @param size
	 */
	public void setFontSize(int size) {
		this.fontSize = size;
		// also refresh display to show effect
		notifyListeners(new ModelChangeEvent(this, ModelChangeEvent.FONT_CHANGED, selectedOntology));		
	}
	
	/**
	 * Create and return a font corresponding to the set preferences.
	 * @return
	 */
	public Font getFont() {
		Font font = new Font(getFontFace(), Font.PLAIN, getFontPt());
		return font;
	}
	
	public String getTreeThreshold() {
		return this.treeThreshold;
	}
	
	public void setTreeThreshold(String threshold) {
		this.treeThreshold = threshold;
	}
	
	public boolean isHighlightCode() {
		return highlightCode;
	}
	
	public void setHighlightCode(boolean highlightCode) {
		this.highlightCode = highlightCode;
	}
	
	/**
	 * Tries to load bookmarks for three different places:
	 * 1) From the file 'favorites.txt' in the CWD
	 * 2) From the Preferences API
	 * 3) From the file 'favorites.txt' in the directory relative to SwoopModel.class
	 * Return the bookmarks from the preferences as a reader.
	 * If the bookmarks aren't stored there yet, return 
	 * the book marks store relative to this class ('favorites.txt')
	 * @return
	 */
	public BufferedReader getBookmarks() {
		BufferedReader reader = null;
		
		File file = new File("favorites.txt");
		if (file.exists()) {
			try {
				reader = new BufferedReader(new FileReader(file));
			} catch (IOException ex) {
				System.out.println("Can't load local favorites.txt");
				ex.printStackTrace();
			}
		}
		
		
		if (reader == null) {
			String bookmarks = preferences.get("bookmarks", null);
			if (bookmarks == null) {
				InputStream is = SwoopModel.class.getResourceAsStream("favorites.txt");
				reader = new BufferedReader(new InputStreamReader(is));
			} else {
				reader = new BufferedReader(new StringReader(bookmarks));
			}
		}
		return reader;
	}
	
	public void saveBookmarks(String bookmarks) {
		preferences.put("bookmarks", bookmarks);
		File file = new File("favorites.txt");
		if (file.exists()) {
			try {
				FileWriter writer = new FileWriter(file);
				writer.write(bookmarks);
				writer.close();
			} catch (IOException e) {
				System.out.println("Couldn't save favorites in the current working directory.");
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Load Swoop Preferences that have been saved to File "preferences"
	 * Do this at startup. Call setPreferences(..) after reading the local file
	 * and creating the necessary preferences HashMap
	 */
	public void loadPreferences() {
		File prefFile = new File("preferences");
		try {
			ObjectInputStream in = new ObjectInputStream(new FileInputStream(prefFile));
			Map prefs = (HashMap) in.readObject();
			this.setPreferences(prefs);
			
			// also load ontology settings (imports/qnames/reasoner)
			this.ontologySettings = (HashMap) in.readObject();
			
			// finally load last saved workspace file
			this.wkspcFile = new File(in.readObject().toString());
			
			in.close();
			System.out.println("Loaded SWOOP Preferences from disk..");
		}
		catch (Exception ex) {
			System.out.println("Unable to load old SWOOP Preferences from disk..");
//			ex.printStackTrace();
		}
		finally {
			this.setPreferences();
			this.setShowQNames(this.showQNames);
		}
	}
	
	private boolean getBooleanPref(Map prefs, String param, boolean deflt) {
		
		if (prefs.containsKey(param)) {
			String value = prefs.get(param).toString();
			if (value.equals("true")) return true;
			else return false;
		}
		else return deflt;
	}
	
	private int getIntegerPref(Map prefs, String param, int deflt) {
		
		if (prefs.containsKey(param)) {
			String value = prefs.get(param).toString();
			return Integer.parseInt(value);
		}
		else return deflt;
	}
	
	private String getStringPref(Map prefs, String param, String deflt) {
		
		if (prefs.containsKey(param)) {
			String value = prefs.get(param).toString();
			return value;
		}
		else return deflt;
	}
	
	/**
	 * Sets SwoopModel parameters based on the HashMap prefs passed to it
	 */
	public void setPreferences(Map prefs) {
	
		try {
			this.userName = getStringPref(prefs, "userName", this.userName);
			String proxySet = getStringPref(prefs, "httpProxy", "false>");
			String use = proxySet.substring(0, proxySet.indexOf(">"));
			if (use.equals("false")) this.useHTTPProxy = false;
			else if (use.indexOf(">")!=use.lastIndexOf(">")){
				this.useHTTPProxy = true;
				this.proxyHost = use.substring(use.indexOf(">")+1, use.lastIndexOf(">"));
				this.proxyPort = use.substring(use.lastIndexOf(">")+1, use.length());
				this.setHTTPProxy(useHTTPProxy, proxyHost, proxyPort);
			}
			this.highlightCode = getBooleanPref(prefs, "highlightCode", this.highlightCode);
			this.changeSharingMethod = getIntegerPref(prefs, "changeSharingMethod", this.changeSharingMethod);
			this.showChangeAnnotBar = getBooleanPref(prefs, "showChangeAnnotBar", this.showChangeAnnotBar);
			this.openLastWorkspace = getBooleanPref(prefs, "openLastWorkspace", this.openLastWorkspace);
//			this.setAutoSaveWkspc(getBooleanPref(prefs, "autoSaveWorkspace", this.autoSaveWkspc));
			this.lookupAllOnts = getBooleanPref(prefs, "lookupAllOnts", this.lookupAllOnts);
			this.viewOWLVocabularyAsRDF = getBooleanPref(prefs, "viewOWLVocabularyAsRDF", this.viewOWLVocabularyAsRDF);
			this.viewRDFVocabularyAsRDF = getBooleanPref(prefs, "viewRDFVocabularyAsRDF", this.viewRDFVocabularyAsRDF);
			
			this.showIcons = getBooleanPref(prefs, "showIcons", this.showIcons);
			this.showDivisions = getBooleanPref(prefs, "showDivisions", this.showDivisions);
			this.fontSize = getIntegerPref(prefs, "fontSize", this.fontSize);
			this.fontFace = getStringPref(prefs, "fontFace", this.fontFace);
			this.setEditorEnabled(getBooleanPref(prefs, "editorEnabled", this.editorEnabled));
			
			this.setShowImports(getBooleanPref(prefs, "show_imports", this.show_imports));
			this.showQNames = getBooleanPref(prefs, "showQNames", this.showQNames);
			this.setShowInherited(getBooleanPref(prefs, "showInherited", this.showInherited));
			this.setEnableAutoSaveChkPts(getBooleanPref(prefs, "enableCheckPoints", this.enableAutoSaveChkPts), true);
			this.setLogChanges(getBooleanPref(prefs, "isLogChanges", this.isLogChanges()));
			this.setEnableChangeLogDisplay(getBooleanPref(prefs, "enableChangeLogDisplay", this.enableChangeLogDisplay));
			this.setShowCheckPts(getBooleanPref(prefs, "showCheckPts", this.showCheckPts));
			this.setSaveCheckPtsDisk(getBooleanPref(prefs, "saveCheckPtsDisk", this.saveCheckPtsDisk));
			// for debugging usability study
//			this.setEnableDebugging(getBooleanPref(prefs, "enableDebugging", this.enableDebugging));
			this.setEnableAutoRetrieve(getBooleanPref(prefs, "enableAutoRetrieve", this.enableAutoRetrieve));
			this.setTreeThreshold(getStringPref(prefs, "treeThreshold", this.treeThreshold));
			this.setTermListFilter(getStringPref(prefs, "termListFilter", this.termListFilter), true);
			// debugging - only call set once at the end
			this.debugGlass = getBooleanPref(prefs, "debugGlassSOS", this.debugGlass);
			this.debugBlack = getBooleanPref(prefs, "debugBlackSOS", this.debugBlack);
			this.useTableau = getBooleanPref(prefs, "useTableauSOS", this.useTableau);
			this.setFindAllMUPS(getBooleanPref(prefs, "findAllMUPSSOS", this.findAllMUPS));
			
			this.defaultReasonerName = getStringPref(prefs, "defaultReasoner", this.defaultReasonerName);			
		}
		catch (Exception ex) {
			System.out.println("Unable to set preferences (missing key in map)");
//			ex.printStackTrace();
		}		
	}
	
	/**
	 * Sets SwoopModel parameters based on the current Preferences.
	 */
	public void setPreferences() {
	
		try {
			this.userName = preferences.get("username", this.userName);
			this.useHTTPProxy = preferences.getBoolean("useProxy", false);
			this.proxyHost = preferences.get("proxyHost", "");
			this.proxyPort = preferences.get("proxyPort", "3128");
			if (this.useHTTPProxy) {
				setHTTPProxy(useHTTPProxy, proxyHost, proxyPort);
			}
			this.highlightCode = preferences.getBoolean("highlightCode", this.highlightCode);
			this.changeSharingMethod = preferences.getInt("changeSharingMethod", this.changeSharingMethod);
			this.showChangeAnnotBar = preferences.getBoolean("showChangeAnnotBar", this.showChangeAnnotBar);
			this.openLastWorkspace = preferences.getBoolean("openLastWorkspace", this.openLastWorkspace);
//			this.setAutoSaveWkspc(getBooleanPref(prefs, "autoSaveWorkspace", this.autoSaveWkspc));
			this.lookupAllOnts = preferences.getBoolean("lookupAllOnts", this.lookupAllOnts);
			this.viewOWLVocabularyAsRDF = preferences.getBoolean("viewOWLVocabularyAsRDF", this.viewOWLVocabularyAsRDF);
			this.viewRDFVocabularyAsRDF = preferences.getBoolean("viewRDFVocabularyAsRDF", this.viewRDFVocabularyAsRDF);
			
			this.showIcons = preferences.getBoolean("showIcons", this.showIcons);
			this.showDivisions = preferences.getBoolean("showDivisions", this.showDivisions);
			this.fontSize = preferences.getInt("fontSize", this.fontSize);
			this.fontFace = preferences.get("fontFace", this.fontFace);
			this.setEditorEnabled(preferences.getBoolean("editorEnabled", this.editorEnabled));
			
			this.setShowImports(preferences.getBoolean("show_imports", this.show_imports));
			this.showQNames = preferences.getBoolean("showQNames", this.showQNames);
			this.setShowInherited(preferences.getBoolean("showInherited", this.showInherited));
			this.setEnableAutoSaveChkPts(preferences.getBoolean("enableCheckPoints", this.enableAutoSaveChkPts), true);
			this.setLogChanges(preferences.getBoolean("isLogChanges", this.isLogChanges()));
			this.setEnableChangeLogDisplay(preferences.getBoolean("enableChangeLogDisplay", this.enableChangeLogDisplay));
			this.setShowCheckPts(preferences.getBoolean("showCheckPts", this.showCheckPts));
			this.setSaveCheckPtsDisk(preferences.getBoolean("saveCheckPtsDisk", this.saveCheckPtsDisk));
			// for debugging usability study
//			this.setEnableDebugging(getBooleanPref(prefs, "enableDebugging", this.enableDebugging));
			this.setEnableAutoRetrieve(preferences.getBoolean("enableAutoRetrieve", this.enableAutoRetrieve));
			this.setTreeThreshold(preferences.get("treeThreshold", this.treeThreshold));
			this.setTermListFilter(preferences.get("termListFilter", this.termListFilter), true);
			// debugging - only call set once at the end
			this.debugGlass = preferences.getBoolean("debugGlassSOS", this.debugGlass);
			this.debugBlack = preferences.getBoolean("debugBlackSOS", this.debugBlack);
			this.useTableau = preferences.getBoolean("useTableauSOS", this.useTableau);
			this.setFindAllMUPS(preferences.getBoolean("findAllMUPSSOS", this.findAllMUPS));
			
			this.defaultReasonerName = preferences.get("defaultReasoner", this.defaultReasonerName);			
		}
		catch (OWLException ex) {
			System.out.println("Unable to set preferences");
			ex.printStackTrace();
		}		
	}
	
	/**
	 * Gets a prefs HashMap of all SwoopModel parameters 
	 */
	public Map getPreferences() {
		Map prefs = new HashMap();
		prefs.put("debugGlassSOS", String.valueOf(debugGlass));
		prefs.put("debugBlackSOS", String.valueOf(debugBlack));
		prefs.put("useTableauSOS", String.valueOf(useTableau));
		prefs.put("findAllMUPSSOS", String.valueOf(findAllMUPS));
		prefs.put("userName", userName);
		prefs.put("httpProxy", String.valueOf(useHTTPProxy)+">"+proxyHost+">"+proxyPort);
		prefs.put("editorEnabled", String.valueOf(editorEnabled));
		prefs.put("showIcons", String.valueOf(showIcons));
		prefs.put("show_imports", String.valueOf(show_imports));
		prefs.put("showQNames", String.valueOf(showQNames));
		prefs.put("showDivisions", String.valueOf(showDivisions));
		prefs.put("showInherited", String.valueOf(showInherited));
		prefs.put("enableCheckPoints", String.valueOf(enableAutoSaveChkPts));    
		prefs.put("isLogChanges", String.valueOf(isLogChanges()));
		prefs.put("enableChangeLogDisplay", String.valueOf(enableChangeLogDisplay));
		prefs.put("showCheckPts", String.valueOf(showCheckPts));
		prefs.put("saveCheckPtsDisk", String.valueOf(saveCheckPtsDisk));
		prefs.put("enableDebugging", String.valueOf(enableDebugging));
		prefs.put("enableAutoRetrieve", String.valueOf(enableAutoRetrieve));
		prefs.put("highlightCode", String.valueOf(highlightCode));
		prefs.put("changeSharingMethod", String.valueOf(changeSharingMethod));
		prefs.put("fontSize", String.valueOf(fontSize));
		prefs.put("fontFace", fontFace);
		prefs.put("treeThreshold", treeThreshold);
		prefs.put("termListFilter", termListFilter);
		prefs.put("showChangeAnnotBar", String.valueOf(showChangeAnnotBar));
		prefs.put("openLastWorkspace", String.valueOf(openLastWorkspace));
//		prefs.put("autoSaveWorkspace", String.valueOf(autoSaveWkspc));
		prefs.put("lookupAllOnts", String.valueOf(lookupAllOnts));
		prefs.put("viewOWLVocabularyAsRDF", String.valueOf(viewOWLVocabularyAsRDF));
		prefs.put("viewRDFVocabularyAsRDF", String.valueOf(viewRDFVocabularyAsRDF));
		prefs.put("defaultReasoner", this.defaultReasonerName);
		return prefs;
	}
	
	/**
	 * Save the preferences to disk at shutdown - creates local file "preferences". 
	 * First calls getPreferences() to create a HashMap prefs storing key-value pairs for all preference
	 * parameters in SwoopModel
	 */
	public void savePreferences() {
		
		try {
			Map prefs = this.getPreferences();
			File prefFile = new File("preferences");
			if (prefFile.exists()) {
				ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(prefFile));
				out.writeObject(prefs);
				out.writeObject(ontologySettings);
				out.writeObject(wkspcFile.getAbsolutePath());
				out.close();
				System.out.println("Saved SWOOP Preferences to disk..");
			}
			// also saved version control repository info
			this.saveVersionRepository();
		}
		catch (Exception ex) {
			System.out.println("Unable to save SWOOP Preferences to disk..");
			//ex.printStackTrace();
		}
		
		preferences.put("username", this.userName);
		preferences.putBoolean("useProxy", this.useHTTPProxy);
		preferences.put("proxyHost", this.proxyHost);
		preferences.put("proxyPort", this.proxyPort);
		
		preferences.putBoolean("highlightCode", this.highlightCode);
		preferences.putInt("changeSharingMethod", this.changeSharingMethod);
		preferences.putBoolean("showChangeAnnotBar", this.showChangeAnnotBar);
		preferences.putBoolean("openLastWorkspace", this.openLastWorkspace);
//			this.setAutoSaveWkspc(getBooleanPref(prefs, "autoSaveWorkspace", this.autoSaveWkspc));
		preferences.getBoolean("lookupAllOnts", this.lookupAllOnts);
		preferences.putBoolean("viewOWLVocabularyAsRDF", this.viewOWLVocabularyAsRDF);
		preferences.putBoolean("viewRDFVocabularyAsRDF", this.viewRDFVocabularyAsRDF);
		
		preferences.putBoolean("showIcons", this.showIcons);
		preferences.putBoolean("showDivisions", this.showDivisions);
		preferences.putInt("fontSize", this.fontSize);
		preferences.put("fontFace", this.fontFace);
		preferences.putBoolean("editorEnabled", this.editorEnabled);
		
		preferences.putBoolean("show_imports", this.show_imports);
		preferences.putBoolean("showQNames", this.showQNames);
		preferences.putBoolean("showInherited", this.showInherited);
		preferences.putBoolean("enableCheckPoints", this.enableAutoSaveChkPts);
		preferences.putBoolean("isLogChanges", this.isLogChanges());
		preferences.putBoolean("enableChangeLogDisplay", this.enableChangeLogDisplay);
		preferences.putBoolean("showCheckPts", this.showCheckPts);
		preferences.putBoolean("saveCheckPtsDisk", this.saveCheckPtsDisk);
		// for debugging usability study
//			this.setEnableDebugging(getBooleanPref(prefs, "enableDebugging", this.enableDebugging));
		preferences.putBoolean("enableAutoRetrieve", this.enableAutoRetrieve);
		preferences.put("treeThreshold", this.treeThreshold);
		preferences.put("termListFilter", this.termListFilter);
		// debugging - only call set once at the end
		preferences.putBoolean("debugGlassSOS", this.debugGlass);
		preferences.putBoolean("debugBlackSOS", this.debugBlack);
		preferences.putBoolean("useTableauSOS", this.useTableau);
		preferences.putBoolean("findAllMUPSSOS", this.findAllMUPS);
		
		preferences.put("defaultReasoner", this.defaultReasonerName);

	}
	
	public String getTermListFilter() {
		return termListFilter;
	}
	
	public void setTermListFilter(String termListFilter, boolean notify) {
		this.termListFilter = termListFilter;
		if (notify) notifyListeners(new ModelChangeEvent(this, ModelChangeEvent.FILTER_SEL_CHANGED, null));
	}
	
	public boolean isShowChangeAnnotBar() {
		return showChangeAnnotBar;
	}
	
	public void setShowChangeAnnotBar(boolean showChangeAnnotBar) {
		this.showChangeAnnotBar = showChangeAnnotBar;
	}
	
	public String getFontFace() {
		return fontFace;
	}
	
	public void setFontFace(String fontFace, boolean notify) {
		this.fontFace = fontFace;
		// also refresh display to show effect
		if (notify) notifyListeners(new ModelChangeEvent(this, ModelChangeEvent.FONT_CHANGED, selectedOntology));
	}

	public boolean isOpenLastWorkspace() {
		return openLastWorkspace;
	}
	
	public void setOpenLastWorkspace(boolean openLastWorkspace) {
		this.openLastWorkspace = openLastWorkspace;
	}
	
	public String getLastWorkspaceURL() {
		return lastWorkspaceURL;
	}
	
	public void setLastWorkspaceURL(String lastWorkspaceURL) {
		this.lastWorkspaceURL = lastWorkspaceURL;
	}
	
	public boolean isLookupAllOnts() {
		return lookupAllOnts;
	}
	
	public boolean isViewOWLVocabularyAsRDF(){ 
		return this.viewOWLVocabularyAsRDF;
	}
	
	public boolean isViewRDFVocabularyAsRDF(){ 
		return this.viewRDFVocabularyAsRDF;
	}
	
	public void setLookupAllOnts(boolean lookupAllOnts) {
		this.lookupAllOnts = lookupAllOnts;
	}
	
	public void setViewOWLVocabularyAsRDF(boolean flag){
		this.viewOWLVocabularyAsRDF = flag;
	}

	public void setViewRDFVocabularyAsRDF(boolean flag){
		this.viewRDFVocabularyAsRDF = flag;
	}
	
	public SwoopCache getAnnotationCache() {
		return annotationCache;
	}
	
	public void setAnnotationCache(SwoopCache annotationCache) {
		this.annotationCache = annotationCache;
		notifyListeners(new ModelChangeEvent(this, ModelChangeEvent.ANNOTATION_CACHE_CHANGED, selectedOntology));
	}	
	
	/**
	 * Save current settings for the ontology i.e. showImports
	 * showQNames and reasonerSelection in ontSettings HashMap.
	 * Done before the ontology selection changes in Swoop
	 *
	 */
	public void saveCurrentOntSettings() {
		try {
			if (selectedOntology!=null) {
				List settings = new ArrayList();
				settings.add(String.valueOf(this.show_imports));
				settings.add(String.valueOf(this.showQNames));
				settings.add(this.reasoner.getName());
				this.ontologySettings.put(selectedOntology.getURI(), settings);
				// also cache reasoner as well
				if (reasoner.getOntology()!=null) reasonerCache.putReasoner(selectedOntology, reasoner.getName(), reasoner);
			}
		}
		catch (OWLException ex) {
			ex.printStackTrace();
		}
	}
	
	/**
	 * Return the ontology-specific settings (show_imports, showQNames
	 * and reasonerSel) as an arrayList for the ontology argument
	 * @param ont
	 * @return
	 */
	public List getOntSetting(OWLOntology ont) {
		try {
			if (ontologySettings.containsKey(ont.getURI())) {
				return (ArrayList) ontologySettings.get(ont.getURI());
			}
		}
		catch (OWLException ex) {
			ex.printStackTrace();
		}
		// return defaults
		List defaults = new ArrayList();
		defaults.add("true");
		defaults.add("true");
		defaults.add(getDefaultReasoner());
		return defaults;
	}
	
	/**
	 * Set the ontology setting list for a specific ontology 
	 * @param ont
	 * @param setting
	 */
	public void setOntSetting(OWLOntology ont, List setting) {
		try {
			ontologySettings.put(ont.getURI(), setting);
		} catch (OWLException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Load ontSettings for ontology in Swoop. i.e. show_imports,
	 * showQNames and reasonerSel
	 * *** Also reclassifies ontology if not loaded from cache! see reasoner.setOntology(..)
	 * @param ont
	 */
	public void loadOntSettings (OWLOntology ont) {
		try {
			URI ontURI = ont.getURI();
			// also restore ontology-specific user settings whenever selection changes
			List settings = new ArrayList();
			
			// load reasoner from cache, if present..also then dont reclassify or set imports			
			boolean loadedReasonerFromCache = false;
			
			if (this.ontologySettings.containsKey(ontURI)) {
				settings = (ArrayList) this.ontologySettings.get(ontURI);
				
				// set reasoner selection
				// load reasoner from cache if present
				// else make it new instance of selected reasoner setting
				reasoner = this.reasonerCache.getReasoner(ont, settings.get(2).toString());
				if (reasoner!=null) {
					loadedReasonerFromCache = true;
					System.out.println("Loaded "+reasoner.getName()+" reasoner from cache..");
				}
				else {
					reasoner = this.getReasonerMap(settings.get(2).toString());					
					System.out.println("Created new "+reasoner.getName()+" reasoner..");					
				}
					
				// set imports setting, but dont notify
				if (settings.get(0).toString().equals("true")) 
					show_imports = true;
				else show_imports = false;
				if (!loadedReasonerFromCache) {
					reasoner.setLoadImports(show_imports, false);
					// also set debugging for new reasoner
					reasoner.setDoExplanation(enableDebugging);			    	
				}
				
				// set qnames setting, but dont notify
				if (settings.get(1).toString().equals("true")) 
					showQNames = true;
				else showQNames = false;
				if(showQNames)
					shortForms = new QNameShortFormProvider();
				else
					shortForms = new DefaultShortFormProvider();
				
			}
			else {
				// default settings
				this.show_imports = true;
				this.showQNames = true;
				shortForms = new QNameShortFormProvider();
				this.reasoner = getDefaultReasoner();
				reasoner.setLoadImports(true, false);
			}
			
			// SwoopFrame.updateOntologyViews() needs to be called
			// immediately after this code fragment below
			if (!loadedReasonerFromCache) {
				reasoner.setDoExplanation(enableDebugging);
//				reasoner.setOntology(this.selectedOntology);
				this.useSwingWorker(new ModelChangeEvent(this, ModelChangeEvent.ONTOLOGY_SEL_CHANGED), false, null, null);
			}
			else {
				// still notify listeners regardless (if loaded from cache)
				notifyListeners(new ModelChangeEvent(this, ModelChangeEvent.ONTOLOGY_SEL_CHANGED));
			}
									
		}
		catch (OWLException ex) {
			ex.printStackTrace();
		}
	}
	
	public SwoopReasoner getDefaultReasoner() {
		return this.getReasonerMap(defaultReasonerName);
	}
	
	public void setDefaultReasoner(SwoopReasoner defaultReasoner) {
		this.defaultReasonerName = defaultReasoner.getName();
	}
	
	public boolean isSaveCheckPtsDisk() {
		return saveCheckPtsDisk;
	}
	
	public void setSaveCheckPtsDisk(boolean saveCheckPtsDisk) {
		this.saveCheckPtsDisk = saveCheckPtsDisk;
	}
	
	public boolean isShowCheckPts() {
		return showCheckPts;
	}
	
	public void setShowCheckPts(boolean showCheckPts) {
		this.showCheckPts = showCheckPts;
		notifyListeners(new ModelChangeEvent(this, ModelChangeEvent.ENABLED_CHANGELOG, null));
	}
	
	public OWLEntity getEntity(OWLOntology ont, URI uri, boolean imports) {
		return this.getEntity(ont, uri, imports, 0);
	}
	
	/**
	 * Given an entity URI and an OWL Ontology ont, checks if a named entity (Class/Property/Individual)
	 * matching the URI is present in the ontology and returns if it found.
	 * Also considers imports closure as passed by the last boolean argument. 
	 * @param ont
	 * @param uri
	 * @param imports
	 * @param elseCreateNew - used to create new entity if it isnt found
	 * @return
	 */
	public OWLEntity getEntity(OWLOntology ont, URI uri, boolean imports, int elseCreateNew) {
		OWLEntity entity = null;
		if (ont==null) return entity;
		
		try {
			Set ontologies = new HashSet();
			
			// consider imported ontologies as well, if imports is true
			if (imports) 
				ontologies = OntologyHelper.importClosure(ont);
			else ontologies = Collections.singleton(ont);
			
			// iterate through each ont, and check for entity
			for (Iterator iter = ontologies.iterator(); iter.hasNext();) {
				OWLOntology onto = (OWLOntology) iter.next();
				entity = onto.getClass(uri);
				if (entity==null) entity = onto.getAnnotationProperty(uri);
				if (entity==null) entity = onto.getDataProperty(uri);
				if (entity==null) entity = onto.getObjectProperty(uri);
				if (entity==null) entity = onto.getIndividual(uri);
				if (entity!=null) return entity;
			}
			
			// create new entity if not found and elseCreateNew has a value 
			if (entity==null && elseCreateNew!=0) {				
				if (elseCreateNew == this.CLASSES) entity = ont.getOWLDataFactory().getOWLClass(uri);
				else if (elseCreateNew == this.DATAPROPERTIES) entity = ont.getOWLDataFactory().getOWLDataProperty(uri);
				else if (elseCreateNew == this.OBJPROPERTIES) entity = ont.getOWLDataFactory().getOWLObjectProperty(uri);
				else if (elseCreateNew == this.INDIVIDUALS) entity = ont.getOWLDataFactory().getOWLIndividual(uri);
			}
		}
		catch (OWLException ex) {
			ex.printStackTrace();
		}
		return entity;
	}
	
	/**
	 * Only returns the value of the showImports setting
	 * for the OWL Ontology argument passed to it. Used while traversing etc.
	 * @return
	 */
	public boolean getImportsSetting(OWLOntology ont) {
		
		boolean importSetting = false;
		try {
			// if ontology is currently selected, return the current value of show_imports
			// because the archived value was the one when the ont selection changed last
			if ((this.selectedOntology != null) && 
					ont.getURI().equals(this.selectedOntology.getURI())) 
				return this.show_imports;
			
			List settings = this.getOntSetting(ont);
			if (settings.size()>0 && settings.get(0).toString().equals("true")) importSetting = true;
		}
		catch (OWLException ex) {
			ex.printStackTrace();
		}
		return importSetting;
	}
	
	/**
	 * Get a **new instance** of SwoopReasoner given its name
	 * @param name - name of SwoopReasoner
	 * @return
	 */
	public SwoopReasoner getReasonerMap(String name) {
		
		Class reasonerClass = null;
		if (reasonerMap.containsKey(name))
			reasonerClass = ((SwoopReasoner) reasonerMap.get(name)).getClass();
		else
			reasonerClass = reasoner.getClass();
		try {
			return (SwoopReasoner) reasonerClass.newInstance();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * Add a pair of SwoopReasoner (name, instance) to the reasonerMap 
	 * @param name - name of SwoopReasoner ("RDFS", "Pellet"..)
	 * @param reas - instance of reasoner (SwoopRDFSReasoner, PelletReasoner)
	 */
	public void addReasonerMap(String name, SwoopReasoner reas) {
		reasonerMap.put(name, reas);
	}
	
	public HashMap getReasonerMap() {
		return this.reasonerMap;
	}
	
	public SwoopCache getReasonerCache() {
   		return reasonerCache;
   	}
	
	public File getWkspcFile() {
		return wkspcFile;
	}
	
	public void setWkspcFile(File wkspcFile) {
		this.wkspcFile = wkspcFile;
	}
	
	/**
	 * Switch a Datatype Property to a Object Property or vice versa 
	 * 
	 */
	public boolean switchProperty(OWLOntology ont, OWLProperty prop) {
		try {
			// get uri of old prop
			URI propURI = prop.getURI();
			OWLProperty newProp = null;
			OWLDataFactory df = ont.getOWLDataFactory();
			
			// store domains of old prop
			Set domains = new HashSet();
			for (Iterator iter = prop.getDomains(ont).iterator(); iter.hasNext();) {
				OWLDescription desc = (OWLDescription) iter.next();
				domains.add(desc);
			}
			// store functional attribute setting
			boolean functional = prop.isFunctional(ont);
			// store annotations on old prop
			Set annots = new HashSet(prop.getAnnotations(ont));
			// anything else?
			
			// remove old property
			OWLEntityRemover remover = new OWLEntityRemover(ont);
    		remover.removeEntity(prop);	
    		
    		// create new property
			if (prop instanceof OWLDataProperty) 
				newProp = df.getOWLObjectProperty(propURI);
			else
				newProp = df.getOWLDataProperty(propURI);
    		
    		// add new prop to ontology
			AddEntity ae = new AddEntity(ont, newProp, null);
			ae.accept((ChangeVisitor) ont);
    		
			// add domains to new prop
			for (Iterator iter = domains.iterator(); iter.hasNext();) {
				OWLDescription desc = (OWLDescription) iter.next();
				AddDomain ad = new AddDomain(ont, newProp, desc, null);
				ad.accept((ChangeVisitor) ont);
			}
			
			// add functional, if it was
			if (functional) {
				SetFunctional sf = new SetFunctional(ont, newProp, true, null);
				sf.accept((ChangeVisitor) ont);
			}
			
			// add annotations to new prop
			for (Iterator it = annots.iterator(); it.hasNext();) {
				OWLAnnotationInstance oai = (OWLAnnotationInstance) it.next();
				AddAnnotationInstance aai = new AddAnnotationInstance(ont, newProp, oai.getProperty(), oai.getContent(), null);
				aai.accept((ChangeVisitor) ont);
			}
			
			//*** remember to reclassify ontology!
			reasoner.setOntology(ont);
			
			Set changedOntologies = new HashSet();
			changedOntologies.add(ont);
			notifyListeners(new ModelChangeEvent(this, ModelChangeEvent.ONTOLOGY_CHANGED, changedOntologies));
		}
		catch (Exception ex) {
			ex.printStackTrace();
			return false;
		}
		return true;
	}
	
	public SwoopCache getClassTreeCache() {
		return classTreeCache;
	}
	
	public SwoopCache getPropTreeCache() {
		return propTreeCache;
	}
	
	public SwoopCache getChangesCache() {
		return changesCache;
	}	
	
	public boolean isLogChanges() {
		return logChanges;
	}
	
	public void setLogChanges(boolean logChanges) {
		this.logChanges = logChanges;
		notifyListeners(new ModelChangeEvent(this, ModelChangeEvent.ENABLED_CHANGELOG, null));
	}
	
	public void putCEinHash(OWLDescription ce) {
		this.classExprHash.put(String.valueOf(ce.hashCode()), ce);
	}
	
	public OWLDescription getCEfromHash(String hashcode) {
		return (OWLDescription) this.classExprHash.get(hashcode);
	}
	
	public void handleExistingOntology(OWLOntology newOnt, URI uri) {
		// if the ontology already exists in SwoopModel
    	// popup with 4 options:
    	// replace new ontology with current
    	// merge with current ontology
    	// keep it as a separate ontology: request new uri 
    	// ignore
		// call function to create and display popup
		try {
			int op = this.popupOntologyOptionPanel(uri);
			if (op!=4) {
				if (newOnt==null) {
					// load new ontology if it hasn't been loaded already
					newOnt = this.loadOntology(uri);
				}
	        	switch (op) {
	        		case 1:
	        			// replace current ontology
	        			this.removeOntology(uri);
	        			this.addOntology(newOnt);
	        			// also need to update all import settings
	        			this.updateImportSettings(newOnt);
	        			break;
	        		case 2:
	        			// merge with existing ontology
	        			break;
	        		case 3:
	        			// insert with a new URI
	        			String newURI = JOptionPane.showInputDialog(null, "Provide New Ontology URI", "Adding New Ontology", JOptionPane.YES_NO_OPTION);
	        			if (newURI!=null) {
	        				this.renameOWLObject(newOnt, uri.toString(), uri.toString(), newURI, false);
	        			}
	        			break;
	        	}
	    	}
		}
		catch (OWLException ex) {
			ex.printStackTrace();
		}
	}
	
	/*
	 * Update 'Imported Ontology' settings for any ontology in swoopModel
	 * whenever a new ontology is added to Swoop 
	 */
	private void updateImportSettings(OWLOntology newOnt) {
		try {
			Set changedOntologies = new HashSet();
			for (Iterator iter = this.getOntologies().iterator(); iter.hasNext();) {
				// for each ontology in swoopModel
				OWLOntology ont = (OWLOntology) iter.next();
				// get imported ontologies
				for (Iterator iter2 = new HashSet(ont.getIncludedOntologies()).iterator(); iter2.hasNext();) {
					OWLOntology impOnt = (OWLOntology) iter2.next();
					// check if imported ontology matches new ontology
					if (impOnt.getURI().equals(newOnt.getURI())) {
						// remove old imported ontology
						// add new ontology as the imported one
						RemoveImport ri = new RemoveImport(ont, impOnt, null);
						ri.accept((ChangeVisitor) ont);
						AddImport ai = new AddImport(ont, newOnt, null);
						ai.accept((ChangeVisitor) ont);
						// also add to changedSet
						changedOntologies.add(ont);
						// and clear reasoner cache
						this.reasonerCache.removeReasoners(ont);
					}
				}
			}
			if (changedOntologies.size()>0) notifyListeners(new ModelChangeEvent(this, ModelChangeEvent.ONTOLOGY_CHANGED, changedOntologies));
		}
		catch (OWLException ex) {
			ex.printStackTrace();
		}
	}
	
	/**
	 * Popup panel to prompt user when loading new ontology into Swoop
	 * that matches with an existing ontology on logical URI
	 * @return
	 */
	private int popupOntologyOptionPanel(URI uri) {
		JRadioButton radio1, radio2, radio3, radio4;
		String uriStr = this.shortForm(uri);
		radio1 = new JRadioButton("Replace existing ontology with new ontology");
    	radio2 = new JRadioButton("Merge existing ontology with new ontology");
    	radio3 = new JRadioButton("Load new ontology but change its logical URI");
    	radio4 = new JRadioButton("Ignore new ontology (do not add to Swoop Model)");
    	JPanel newOntPanel = new JPanel();
    	newOntPanel.setLayout(new GridLayout(4,1));
    	newOntPanel.add(radio1);
    	newOntPanel.add(radio2);
    	radio2.setEnabled(false);
    	newOntPanel.add(radio3);
    	newOntPanel.add(radio4);
    	Font tahoma = new Font("Tahoma", Font.PLAIN, 11);
    	radio1.setFont(tahoma);
    	radio2.setFont(tahoma);
    	radio3.setFont(tahoma);
    	radio4.setFont(tahoma);
    	ButtonGroup group = new ButtonGroup();
    	group.add(radio1);
    	group.add(radio2);
    	group.add(radio3);
    	group.add(radio4);
    	radio1.setSelected(true);
    	int ontOption = JOptionPane.showConfirmDialog(
    			null,
				newOntPanel,
                uriStr+" already present in Swoop Model",
                JOptionPane.OK_CANCEL_OPTION
            );
    	if (ontOption == JOptionPane.OK_OPTION) {
	    	if (radio1.isSelected()) return 1;
	    	else if (radio2.isSelected()) return 2;
	    	else if (radio3.isSelected()) return 3;
    	}
    	return 4;
	}
	
	/**
	 * Renaming an OWL Entity now done the clean way.
	 * Creates an instance of OWLEntityRemover and uses its
	 * replaceEntity(..) method
	 */
	public void renameOWLEntity(OWLOntology ontology, OWLEntity currEntity, String newName) {

		// warn user before renaming if unapplied changes exist
		if (this.getUncommittedChanges().size() > 0) {
			int result = JOptionPane
					.showConfirmDialog(
							null,
							new Object[] { "Apply uncommitted changes before renaming?" },
							"Apply changes?", JOptionPane.YES_NO_OPTION);

			if (result == JOptionPane.YES_OPTION) {
				this.applyOntologyChanges();
			}
		}

		try {
			// first add new entity to ontology
			
			// tw7: here we take care of the case that two concepts sharing the same
			//      "label" but different URI (e.g.: foaf:Person, contact:Person), and
			//      one of them is getting a name change, only the chosen one is affected.
			URI oldURI = currEntity.getURI();
			String label = shortForm( oldURI ) ;
			int index = label.indexOf(":");
			if ( index != -1 )
				label = label.substring( index + 1 );
			
			String oldURIStr = oldURI.toString();
			String goodPrefix = ontology.getURI().toString();
			if ( !oldURIStr.startsWith( goodPrefix ) )
			{
				int labelStart = oldURIStr.indexOf( label );
				goodPrefix = oldURIStr.substring( 0, labelStart );
			}
			
			
			boolean ontUsesHash = false;
			if ( oldURIStr.indexOf("#") >= 0 )
				ontUsesHash = true;
			// conforming to the style of the ontology (whether it uses a hash)
			// in creating the URI for this new OWLEntity
			URI newURI = null;
			// Check to make sure there are no path or fragment characters.
			if ( ontUsesHash &&  (newName.indexOf('#') < 0) && (newName.indexOf('/') < 0))
			{
				newName = "#" + newName;
			}
			// Resolving new uri is usually more resiliant.
			newURI = oldURI.resolve(newName);
			
			
			OWLEntity newEntity = null;
			if (currEntity instanceof OWLClass) newEntity = ontology.getOWLDataFactory().getOWLClass(newURI);
			else if (currEntity instanceof OWLObjectProperty) newEntity = ontology.getOWLDataFactory().getOWLObjectProperty(newURI);
			else if (currEntity instanceof OWLDataProperty) newEntity = ontology.getOWLDataFactory().getOWLDataProperty(newURI);
			else if (currEntity instanceof OWLIndividual) newEntity = ontology.getOWLDataFactory().getOWLIndividual(newURI);
			AddEntity ae = new AddEntity(ontology, newEntity, null);
			ae.accept((ChangeVisitor) ontology);
			
			// use OWLEntityRemover to replace entity
			OWLEntityRemover oer = new OWLEntityRemover(ontology);
			oer.replaceEntity(currEntity, newEntity);
			
			this.selectedEntity = newEntity;
			this.selectedOntology = ontology;
			this.selectedOWLObject = newEntity;
			reasoner.setOntology(ontology);
		
			notifyListeners(new ModelChangeEvent(this, ModelChangeEvent.REMOVED_ENTITY));
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	/***
     * Rename all references of an entity or the ontology itself
     * This is currently a string hack since no routine exists for this in the OWL API
     * So we render the ontology in RDF/XML. Do a search/replace for the renamed term
     * and reparse the new ontology back into Swoop
     * The boolean param is used to control whether the pre-processed ontology should be removed from Swoop or not 
     */
    public void renameOWLObject(OWLOntology ontology, String currentURI, String currName, String newName, boolean replaceOnt) {
  
    	// warn user before renaming if unapplied changes exist
		if (this.getUncommittedChanges().size() > 0) {
			int result = JOptionPane
					.showConfirmDialog(
							null,
							new Object[] { "Apply uncommitted changes before renaming?" },
							"Apply changes?", JOptionPane.YES_NO_OPTION);

			if (result == JOptionPane.YES_OPTION) {
				this.applyOntologyChanges();
			}
		}

		// obtain ontology rdf/xml source
		StringWriter st = new StringWriter();
		CorrectedRDFRenderer rdfRenderer = new CorrectedRDFRenderer();
		try {
			rdfRenderer.renderOntology(ontology, st);

			String source = st.getBuffer().toString();

			// create newURI
			String newURI = "";
			if (currName.equals(currentURI)) {
				// renaming the ontology itself! i.e. its logical URI
				newURI = "\"" + newName;
				currentURI = "\"" + currentURI;
			} 
			else {
				// renaming an OWL entity in the ontology
				newURI = currentURI.substring(0, currentURI.indexOf(currName));
				newURI += newName;
				currentURI = "\"" + currentURI + "\"";
				newURI = "\"" + newURI + "\"";
			}

			// replace all
			source = source.replaceAll(currentURI, newURI);
			StringReader reader = new StringReader(source);

			// remove current ontology and clear its cache
			if (replaceOnt)
				this.removeOntology(ontology.getURI());
			//    		termDisplay.removeFromCache(ontology);

			// add new ontology with renamed entity
			// parse the RDF version w/o loading imports (last boolean argument
			// - false)
			OWLOntology renamedOnt = this.loadOntologyInRDF(reader, ontology
					.getURI(), false);
			// apply imports changes after
			if (renamedOnt != null) {
				for (Iterator iter = ontology.getIncludedOntologies()
						.iterator(); iter.hasNext();) {
					OWLOntology impOnt = (OWLOntology) iter.next();
					AddImport change = new AddImport(renamedOnt, impOnt, null);
					change.accept((ChangeVisitor) renamedOnt);
				}
				// only add if renamedOnt != null
				this.addOntology(renamedOnt);
				this.setSelectedOntology(renamedOnt);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
    }
    
    public String getUserName() {
		return userName;
	}
    
    public void setUserName(String name) {
		this.userName = name;
	}
    
    // save version control repository info
    public void saveVersionRepository() {
    		try {
			File repFile = new File("versionRepository");
			if (repFile.exists()) {
				ObjectOutputStream out = new ObjectOutputStream(
						new FileOutputStream(repFile));
				out.writeObject(versionRepository);
				out.close();
				System.out.println("Saved Version-Control Repository(s) Info to disk");
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		try {
			ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();
			ObjectOutputStream out = new ObjectOutputStream(bytesOut);
			out.writeObject(versionRepository);
			out.close();
			preferences.putByteArray("versionRepository", bytesOut.toByteArray());
			System.out.println("Saved Version-Control Repository(s) Info to preferences");
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
    }
    
    // load version control repository info
	public void loadVersionRepository() {
		// load local file
		try {
			File repFile = new File("versionRepository");
			ObjectInputStream in = new ObjectInputStream(new FileInputStream(repFile));
			versionRepository = (HashMap) in.readObject();
			in.close();
		}
		catch (Exception ex) {
			System.out.println("Unable to load old Version Repository from disk");
			//ex.printStackTrace();
		}
		try {
			byte[] versionBytes = preferences.getByteArray("versionRepository", null);
			if (versionBytes != null) {
				InputStream bytesIn = new ByteArrayInputStream(versionBytes);
				ObjectInputStream in = new ObjectInputStream(bytesIn);
				versionRepository = (HashMap) in.readObject();
				in.close();
			}
		} catch (Exception ex) {
			System.out.println("Unalbed to load Version Repository from disk");
		}
	}
	
	// simple update of versionRepository info map
	public void updateVersionRepository(URI repURI, Description[] verDesc) {
		this.versionRepository.put(repURI, verDesc);
	}
	
	protected void setFrame(SwoopFrame frame)
	{ myFrame = frame; }
	
	public SwoopFrame getFrame()
	{ return myFrame; }
	
	/**
	 * Get setting for Black Box debugging (root/derived)
	 * @return Returns the debugBlack.
	 */
	public boolean isDebugBlack() {
		return debugBlack;
	}
	
	/**
	 * Enable/disable Black Box debugging (root/derived)
	 * @param debugBlack The debugBlack to set.
	 */
	public void setDebugBlack(boolean debugBlack) {
		this.debugBlack = debugBlack;
//		notifyListeners(new ModelChangeEvent(this, ModelChangeEvent.DEBUGGING_CHANGED, selectedOntology));
	}
	
	/**
	 * Get setting for Glass Box debugging (clash/axioms)
	 * @return Returns the debugGlass.
	 */
	public boolean isDebugGlass() {
		return debugGlass;
	}
	
	/**
	 * Enable/disable Glass Box debugging (clash/axioms)
	 * @param debugGlass The debugGlass to set.
	 */
	public void setDebugGlass(boolean debugGlass) {
		this.debugGlass = debugGlass;
		notifyListeners(new ModelChangeEvent(this, ModelChangeEvent.DEBUGGING_CHANGED, selectedOntology));
	}
	
	/*
	 * Get statistics for ontology using SwoopStatistics
	 */
	public HashMap getOntStats(OWLOntology ontology) {
		HashMap stats = new HashMap();
		try {
			if (ontStatMap.containsKey(ontology.getURI())) {
				// load it from the cache
				stats = (HashMap) ontStatMap.get(ontology.getURI());				
			}
			else {
				// re-compute stats for the ontology
				SwoopStatistics sws = new SwoopStatistics(this);
				stats = sws.computeStatistics(ontology);
				ontStatMap.put(ontology.getURI(), stats);
			}
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
		return stats;
	}
	/*
	 * remove ontology key from stat map
	 */
	public void removeOntStats(OWLOntology ontology) {
		try {
			ontStatMap.remove(ontology.getURI());
			// also notify SwoopOntologyInfo to refresh itself
			notifyListeners(new ModelChangeEvent(this, ModelChangeEvent.ONT_STATS_CHANGED, ontology));
		} catch (OWLException e) {
			e.printStackTrace();
		}
	}
	
	public void removeOntStatsWithoutNotification(OWLOntology ontology) 
	{
		try {
			ontStatMap.remove(ontology.getURI());
		} catch (OWLException e) {
			e.printStackTrace();
		}
	}

	
	public String getUseLanguage() {
		return useLanguage;
	}
	
	public void setUseLanguage(String useLanguage) {
		this.useLanguage = useLanguage;
		ModelChangeEvent event = new ModelChangeEvent(this, ModelChangeEvent.QNAME_VIEW_CHANGED);
		notifyListeners(event);
	}
	
	/*
	 * Set the HTTP Proxy if specified by user
	 */
	public void setHTTPProxy(boolean use, String host, String port) {
		this.useHTTPProxy = use;
		this.proxyHost = host;
		this.proxyPort = port;
		Properties prop = System.getProperties();
		if (use) {
			prop.put( "proxySet", "true");
			prop.put("http.proxyHost", host);
			prop.put("http.proxyPort", port);
		}
		else {
			prop.put( "proxySet", "false");
		}
	}
	/**
	 * @return Returns the proxyHost.
	 */
	public String getProxyHost() {
		return proxyHost;
	}
	/**
	 * @return Returns the proxyPort.
	 */
	public String getProxyPort() {
		return proxyPort;
	}
	/**
	 * @return Returns the useHTTPProxy.
	 */
	public boolean getUseHTTPProxy() {
		return useHTTPProxy;
	}
	
	/*
	 * Create a SwingWorker to process the ontology using the reasoner
	 * Called from the following methods in SwoopModel:
	 * - loadOntSettings()
	 * - setShowImports(..)
	 * - addEntity(..)
	 * - removeEntity(..)
	 * - addGCI(..)
	 * - removeGCI(..)
	 */
	private void useSwingWorker(ModelChangeEvent normalEvent, boolean importChanged, OWLEntity selEntity, OntologyChange change) {
		
		final SwoopModel model = this;
		final ModelChangeEvent normalEventF = normalEvent;
		final boolean importChangedF = importChanged;
		final OWLEntity selEntityF = selEntity;
		final OntologyChange changeF = change;
		
		// create SwingWorker to process ontology using reasoner
		SwingWorker worker = new SwingWorker() {
			boolean fail = false;
			public Object construct() {
				try {
					if (!importChangedF) reasoner.setOntology(model.selectedOntology);
					else reasoner.setLoadImports(show_imports, true);					
				} 
				catch (Exception ex) {
					fail = true;
					if( ex != null )
					    throw new RuntimeException(ex.getMessage());
					else
					    throw new RuntimeException( "Unexpected error" );
				}	
				return null;
			}
			public void finished() {
				if (fail) {
					try {
						// remove all reasoners from cache
						model.reasonerCache.removeReasoners(model.selectedOntology);
						reasoner = null; // this will prevent it from being added to the cache again in call to model.setReasoner() below
						SwoopReasoner noReasoner = (SwoopReasoner) SwoopToldReasoner.class.newInstance();
						// this will set No-Reasoner to be new reasoner
						// and call SwoopFrame.updateOntologyViews()
						model.setReasoner(noReasoner);
						// make reasonerCombo select noReasoner
						notifyListeners(new ModelChangeEvent(model, ModelChangeEvent.REASONER_FAIL));
						if (importChangedF) model.setShowImports(show_imports); 
					} 
					catch (Exception e) {
						e.printStackTrace();
					}					
				}
				else {
					// normal procedure if no cancellation
					// if event was Add_Entity or Remove_Entity, reset ontStats
					if (normalEventF.getType() == ModelChangeEvent.ADDED_ENTITY || normalEventF.getType() == ModelChangeEvent.REMOVED_ENTITY)
						model.removeOntStats(model.selectedOntology);
					// if entity needs to be selected..
					if (selEntityF!=null) model.setSelectedEntity(selEntityF);
					// make normal event notification
					notifyListeners(normalEventF);
					// if change needs to be logged
					if (changeF!=null) model.addCommittedChange(changeF);
				}
			}
		};				
	    worker.start();
	}
	
	/*
	 * mothership passes a set of ontologies, and here we associate
	 * a reasoner object w/ each ontology (either loaded from cache or by creating a new one)
	 * and populate the vector reas accordingly
	 */
	public void callFromMotherShip(Set onts, HashMap reasMap) {
		
		try {
			List ontList = new ArrayList(onts);
			for(Iterator i = ontList.iterator(); i.hasNext();)
	        {
	        	OWLOntology ont = (OWLOntology)i.next();
	    		// save ontology settings before accessing its reasoner
	    		this.saveCurrentOntSettings();
	        	URI ontURI = ont.getURI();
	        	// create a separate reasoner instance for processing each ont
	        	SwoopReasoner reas = null;
	        	// load from cache or make new instance
	        	boolean loadedReasonerFromCache = false;
	        	if (this.ontologySettings.containsKey(ontURI)) 
	        	{
					List settings = (ArrayList) this.ontologySettings.get(ontURI);
					reas = this.reasonerCache.getReasoner(ont, settings.get(2).toString());
					if (reas!=null) 
					{
						loadedReasonerFromCache = true;
						System.out.println("Loaded "+reas.getName()+" reasoner from cache..");
					}
					else {
						reas = this.getReasonerMap(settings.get(2).toString());					
						System.out.println("Created new "+reas.getName()+" reasoner..");					
					}
	        	}
	        	else reas = this.getDefaultReasoner();
	        	System.out.println("Reasoner type: " + reas.getClass().getName() );
	        	// and add it to vector
	        	reasMap.put(ont.getURI(), reas);
	        }
			
			// now use SwingWorker to process all ontologies inside thread
			final HashMap rm = reasMap;    	
			final List ol = ontList;
			SwingWorker worker = new SwingWorker() {
    			public Object construct() {
    				try {
    					for (int ctr = 0; ctr<ol.size(); ctr++) {
    						OWLOntology ont = (OWLOntology) ol.get(ctr);
    						SwoopReasoner reas = (SwoopReasoner) rm.get(ont.getURI());
    						// create SwingWorker to process ontology using reasoner
    			        	// only if hasnt processed it already
    						if (reas.getOntology()==null) {
    							reas.setOntology(ont);
    						}
    						reasonerCache.putReasoner(ont, reas.getName(), reas);
    					}
    				} 
    				catch (Exception ex) {
    					ex.printStackTrace();
    				}
    				return null;
    			}
    			public void finished() {
    				// notify mothership
    				notifyListeners(new ModelChangeEvent(null, ModelChangeEvent.MOTHERSHIP_DISPLAY));
    			}
			};
			worker.start();	        
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
    public void setShowImportsWithThreadBlock(boolean value) throws OWLException {
    	show_imports = value;
    	
    	this.updateWithThreadBlock(new ModelChangeEvent(this, ModelChangeEvent.IMPORTS_VIEW_CHANGED), true, null, null);    	
    }
    
    
	/*
	 * Single-mindedly updates the swoop options without the aid of a thread
	 * See useSwingWorker() for comparison
	 * Called from the following methods in SwoopModel:
	 * - setShowImportsWithThreadBlock(..)
	 */
	private void updateWithThreadBlock(ModelChangeEvent normalEvent, boolean importChanged, OWLEntity selEntity, OntologyChange change) {
		
		final SwoopModel model = this;
		final ModelChangeEvent normalEventF = normalEvent;
		final boolean importChangedF = importChanged;
		final OWLEntity selEntityF = selEntity;
		final OntologyChange changeF = change;
		boolean fail = false;
		try {
			if (!importChangedF) reasoner.setOntology(model.selectedOntology);
			else reasoner.setLoadImports(show_imports, true);					
		} 
		catch (Exception ex) {
			fail = true;
			if( ex != null )
			    throw new RuntimeException(ex.getMessage());
			else
			    throw new RuntimeException( "Unexpected error" );
		}
		if (fail) {
			try {
				// remove all reasoners from cache
				model.reasonerCache.removeReasoners(model.selectedOntology);
				reasoner = null; // this will prevent it from being added to the cache again in call to model.setReasoner() below
				SwoopReasoner noReasoner = (SwoopReasoner) SwoopToldReasoner.class.newInstance();
				// this will set No-Reasoner to be new reasoner
				// and call SwoopFrame.updateOntologyViews()
				model.setReasoner(noReasoner);
				// make reasonerCombo select noReasoner
				notifyListeners(new ModelChangeEvent(model, ModelChangeEvent.REASONER_FAIL));
				if (importChangedF) model.setShowImports(show_imports); 
			} 
			catch (Exception e) {
				e.printStackTrace();
			}					
		}
		else {
			// normal procedure if no cancellation
			// if event was Add_Entity or Remove_Entity, reset ontStats
			if (normalEventF.getType() == ModelChangeEvent.ADDED_ENTITY || normalEventF.getType() == ModelChangeEvent.REMOVED_ENTITY)
				model.removeOntStats(model.selectedOntology);
			// if entity needs to be selected..
			if (selEntityF!=null) model.setSelectedEntity(selEntityF);
			// make normal event notification
			notifyListeners(normalEventF);
			// if change needs to be logged
			if (changeF!=null) model.addCommittedChange(changeF);
		}	
	}
	
    
    
	
   	/**
   	 * Sets the SwoopModel reasoner without threading
   	 * Also classifies the currently selected ontology
   	 * @param reasoner
   	 * @throws OWLException
   	 */
   	public void setReasonerWithThreadBlock(SwoopReasoner selReasoner) throws Exception {

   		// when changing reasoner selection for an ontology in the UI
   		// first save current reasoner selection
   	    if( selectedOntology != null && reasoner!=null)
   	        reasonerCache.putReasoner(selectedOntology, this.reasoner.getName(), this.reasoner);
   		
   	    final SwoopReasoner previousReasoner = this.reasoner; 
   	    
   		// now select new reasoner
   		boolean loadedReasonerFromCache = false;
   		// check if reasoner already exists in cache
   		SwoopReasoner newReasoner = ( selectedOntology != null )
   		    ? reasonerCache.getReasoner(selectedOntology, selReasoner.getName())
   		    : null;
   		if (newReasoner!=null) {
   			loadedReasonerFromCache = true;
   			System.out.println("Loaded "+newReasoner.getName()+" reasoner from cache..");
   		}
   		else {
   			// else create new instance of selected reasoner
   			Class cls = selReasoner.getClass();
   			newReasoner = (SwoopReasoner) cls.newInstance();
   			System.out.println("Created new "+newReasoner.getName()+" reasoner..");   			
   		}
   		
   		try {
			if(selectedOntology != null && !loadedReasonerFromCache) 
			{
			    newReasoner.setDoExplanation(enableDebugging);
			    newReasoner.setLoadImports(this.show_imports, false); //don't refresh here

			    final SwoopModel model = this;
			    final SwoopReasoner reas = newReasoner;
			    
			    boolean fail = false;
				try 
				{
					// hacky stuff... need to change Reasoner Interface for progress support across the board
					if ( reas instanceof PelletReasoner )
						((PelletReasoner)reas).setOntology( selectedOntology, true, false );
					else
						reas.setOntology(selectedOntology);							
				} 
				catch (Exception ex) {
					fail = true;
					if( ex != null )
					    throw new RuntimeException(ex.getMessage());
					else
					    throw new RuntimeException( "Unexpected error" );
				}
				if (fail) {
					// remove pellet from cache
					model.reasonerCache.removeReasonerOntology(model.selectedOntology, reas.getName());
					// set reasoner to previous one
					model.reasoner = previousReasoner;
					// make reasonerCombo select previous one
					notifyListeners(new ModelChangeEvent(model, ModelChangeEvent.REASONER_FAIL));
				}
				else {
					// set new reasoner
					model.reasoner = reas;
					notifyListeners(new ModelChangeEvent(model, ModelChangeEvent.REASONER_SEL_CHANGED));
				}																   	
			}
			else 
			{
				// notify regardless (if loaded from cache)
				this.reasoner = newReasoner;		   		
		   		notifyListeners(new ModelChangeEvent(this, ModelChangeEvent.REASONER_SEL_CHANGED));		   			
			}
		} 
   		catch (Throwable e) 
		{
			e.printStackTrace();
			throw new OWLException(e.getMessage());
		}   		   
   	}


	// returns the entities in the signature of the axiom
	public Set getAxiomSignature(OWLObject axiom, OWLOntology ont) {
		
		Set entities = new HashSet();
		try {
			OWLOntBuilder ob = new OWLOntBuilder();
			axiom.accept(ob);
			OWLOntology temp = ob.currentOnt;
	
			for (Iterator iter2=temp.getClasses().iterator(); iter2.hasNext();) {
				OWLClass cla = (OWLClass) iter2.next();
				entities.add(ont.getClass(cla.getURI()));
			}
			for (Iterator iter2=temp.getDataProperties().iterator(); iter2.hasNext();) {
				OWLDataProperty prop = (OWLDataProperty) iter2.next();
				entities.add(ont.getDataProperty(prop.getURI()));
			}
			for (Iterator iter2=temp.getObjectProperties().iterator(); iter2.hasNext();) {
				OWLObjectProperty prop = (OWLObjectProperty) iter2.next();
				entities.add(ont.getObjectProperty(prop.getURI()));
			}
			for (Iterator iter2=temp.getIndividuals().iterator(); iter2.hasNext();) {
				OWLIndividual ind = (OWLIndividual) iter2.next();
				entities.add(ont.getIndividual(ind.getURI()));
			}
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
		return entities;
	}
	
	// get axioms related to an entity
	public Set getAxioms(OWLEntity entity, OWLOntology ont) {
		Set axioms = new HashSet();
		Set ontSet = new HashSet();
		ontSet.add(ont);
		try {
			OWLDataFactory ontDF = ont.getOWLDataFactory();
			if (entity instanceof OWLClass) {
				OWLClass cla = (OWLClass) entity;
				// get superclasses
				Set sup = OWLDescriptionFinder.getSuperClasses(cla, ontSet);
				for (Iterator iter = sup.iterator(); iter.hasNext();) {
					OWLDescription supCla = (OWLDescription) iter.next();
					OWLSubClassAxiom subAxiom = ontDF.getOWLSubClassAxiom(cla, supCla);
					axioms.add(subAxiom);
				}
				// get equivalent classes
				Set equ = OWLDescriptionFinder.getEquivalentClasses(cla, ontSet);
				for (Iterator iter = equ.iterator(); iter.hasNext();) {
					OWLDescription equCla = (OWLDescription) iter.next();
					Set equSet = new HashSet();
					equSet.add(cla);
					equSet.add(equCla);
					OWLEquivalentClassesAxiom equAxiom = ontDF.getOWLEquivalentClassesAxiom(equSet);
					axioms.add(equAxiom);
				}
				// get disjoints
				Set disj = OWLDescriptionFinder.getDisjoints(cla, ontSet);
				for (Iterator iter = disj.iterator(); iter.hasNext();) {
					OWLDescription disjCla = (OWLDescription) iter.next();
					Set disjSet = new HashSet();
					disjSet.add(cla);
					disjSet.add(disjCla);
					OWLDisjointClassesAxiom disAxiom = ontDF.getOWLDisjointClassesAxiom(disjSet);
					axioms.add(disAxiom);
				}
				//?? enumerations - included in OWLDescriptionFinder.equivalentClasses
			}
			else if (entity instanceof OWLProperty) {
				OWLProperty prop = (OWLProperty) entity;
				
				// get domains
				Set domSet = prop.getDomains(ont);
				for (Iterator iter = domSet.iterator(); iter.hasNext();) {
					OWLDescription domDesc = (OWLDescription) iter.next();
					OWLPropertyDomainAxiomImpl opda = new OWLPropertyDomainAxiomImpl((OWLDataFactoryImpl) ontDF, prop, domDesc);
					axioms.add(opda);
				}
				// get functional
				if (prop.isFunctional(ont)) {
					OWLFunctionalPropertyAxiomImpl ofp = new OWLFunctionalPropertyAxiomImpl((OWLDataFactoryImpl) ontDF, prop);
					axioms.add(ofp);
				}
				// get super properties
				for (Iterator iter = prop.getSuperProperties(ontSet).iterator(); iter.hasNext();) {
					OWLProperty supProp = (OWLProperty) iter.next();
					OWLSubPropertyAxiomImpl ospa = new OWLSubPropertyAxiomImpl((OWLDataFactoryImpl) ontDF, prop, supProp);
					axioms.add(ospa);
				}
				// get data range
				if (prop instanceof OWLDataProperty) {
					OWLDataProperty dprop = (OWLDataProperty) prop;
					Set ran = dprop.getRanges(ont);
					for (Iterator iter = ran.iterator(); iter.hasNext();) {
						OWLDataRange dr = (OWLDataRange) iter.next();
						OWLDataPropertyRangeAxiomImpl opra = new OWLDataPropertyRangeAxiomImpl((OWLDataFactoryImpl) ontDF, dprop, dr);
						axioms.add(opra);
					}					
				}
				else {
					// get object prop range
					OWLObjectProperty oprop = (OWLObjectProperty) prop;
					Set ran = oprop.getRanges(ont);
					for (Iterator iter = ran.iterator(); iter.hasNext();) {
						OWLDescription desc = (OWLDescription) iter.next();
						OWLObjectPropertyRangeAxiomImpl opra = new OWLObjectPropertyRangeAxiomImpl((OWLDataFactoryImpl) ontDF, oprop, desc);
						axioms.add(opra);
					}
					// get inverse
					Set inv = oprop.getInverses(ont);
					for (Iterator iter = inv.iterator(); iter.hasNext();) {
						OWLObjectProperty op = (OWLObjectProperty) iter.next();
						OWLInversePropertyAxiomImpl opra = new OWLInversePropertyAxiomImpl((OWLDataFactoryImpl) ontDF, oprop, op);
						axioms.add(opra);
					}
					// get attributes
					if (oprop.isTransitive(ont)) {
						OWLTransitivePropertyAxiomImpl ofp = new OWLTransitivePropertyAxiomImpl((OWLDataFactoryImpl) ontDF, oprop);
						axioms.add(ofp);
					}
					if (oprop.isSymmetric(ont)) {
						OWLSymmetricPropertyAxiomImpl ofp = new OWLSymmetricPropertyAxiomImpl((OWLDataFactoryImpl) ontDF, oprop);
						axioms.add(ofp);
					}
					if (oprop.isInverseFunctional(ont)) {
						OWLInverseFunctionalPropertyAxiomImpl ofp = new OWLInverseFunctionalPropertyAxiomImpl((OWLDataFactoryImpl) ontDF, oprop);
						axioms.add(ofp);
					}
				}
			}
			else if (entity instanceof OWLIndividual) {
				OWLIndividual ind = (OWLIndividual) entity;
				// add types of individual
				for (Iterator iter2 = ind.getTypes(ontSet).iterator(); iter2.hasNext();) {
					OWLDescription type = (OWLDescription) iter2.next();
					OWLIndividualTypeAssertionImpl ota = new OWLIndividualTypeAssertionImpl((OWLDataFactoryImpl) ontDF, ind, type);
					axioms.add(ota);					
				}
				// add data prop assertions
				Map dTypeVals = ind.getDataPropertyValues(ontSet);
				for (Iterator iter2 = dTypeVals.keySet().iterator(); iter2.hasNext();) {
					OWLDataProperty prop = (OWLDataProperty) iter2.next();
					Set values = (HashSet) dTypeVals.get(prop);
					for (Iterator iter3=values.iterator(); iter3.hasNext();) {
						OWLDataValue value = (OWLDataValue) iter3.next();
						OWLDataPropertyInstanceImpl odp = new OWLDataPropertyInstanceImpl((OWLDataFactoryImpl) ontDF, ind, prop, value);
						axioms.add(odp);
					}
				}
				// add object prop assertions
				Map oTypeVals = ind.getObjectPropertyValues(ontSet);
				for (Iterator iter2 = oTypeVals.keySet().iterator(); iter2.hasNext();) {
					OWLObjectProperty prop = (OWLObjectProperty) iter2.next();
					Set values = (HashSet) oTypeVals.get(prop);
					for (Iterator iter3=values.iterator(); iter3.hasNext();) {
						OWLIndividual value = (OWLIndividual) iter3.next();
						OWLObjectPropertyInstanceImpl odp = new OWLObjectPropertyInstanceImpl((OWLDataFactoryImpl) ontDF, ind, prop, value);
						axioms.add(odp);
					}
				}
				// add same and different individual axioms
				for (Iterator iter2 = ont.getIndividualAxioms().iterator(); iter2.hasNext();) {
					OWLIndividualAxiom ax = (OWLIndividualAxiom) iter2.next();
					if (ax instanceof OWLSameIndividualsAxiom) {
						OWLSameIndividualsAxiom osi = (OWLSameIndividualsAxiom) ax;
						if (osi.getIndividuals().contains(ind)) axioms.add(ax);
					}
					else if (ax instanceof OWLDifferentIndividualsAxiom) {
						OWLDifferentIndividualsAxiom osi = (OWLDifferentIndividualsAxiom) ax;
						if (osi.getIndividuals().contains(ind)) axioms.add(ax);
					} 
				}
			}
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
		return axioms;
	}
	
	/**
	 * @return Returns the findAllMUPS.
	 */
	public boolean isFindAllMUPS() {
		return findAllMUPS;
	}
	/**
	 * @param findAllMUPS The findAllMUPS to set.
	 */
	public void setFindAllMUPS(boolean findAllMUPS) {
		this.findAllMUPS = findAllMUPS;
		notifyListeners(new ModelChangeEvent(this, ModelChangeEvent.DEBUGGING_CHANGED, selectedOntology));
	}
	/**
	 * @return Returns the useTableau.
	 */
	public boolean isUseTableau() {
		return useTableau;
	}
	/**
	 * @param useTableau The useTableau to set.
	 */
	public void setUseTableau(boolean useTableau) {
		this.useTableau = useTableau;
		notifyListeners(new ModelChangeEvent(this, ModelChangeEvent.DEBUGGING_CHANGED, selectedOntology));
	}
	/**
	 * @return Returns the autoSaveWkspc.
	 */
	public boolean isAutoSaveWkspc() {
		return autoSaveWkspc;
	}
	
	/**
	 * @param autoSaveWkspc The autoSaveWkspc to set.
	 */
	public void setAutoSaveWkspc(boolean autoSaveWkspc) {
		this.autoSaveWkspc = autoSaveWkspc;
		
		if (autoSaveWkspc) {
			swoopTimer = new Timer();
			swoopTimer.schedule(new SwoopReminder(this, myFrame), 0, (int) saveWkspcTime * 60 * 1000);			
		}
		else swoopTimer = new Timer();
	}
	
	/**
	 * @return Returns the saveWkspcFile.
	 */
	public String getSaveWkspcFile() {
		return saveWkspcFile;
	}
	/**
	 * @param saveWkspcFile The saveWkspcFile to set.
	 */
	public void setSaveWkspcFile(String saveWkspcFile) {
		this.saveWkspcFile = saveWkspcFile;
	}
	/**
	 * @return Returns the saveWkspcTime.
	 */
	public float getSaveWkspcTime() {
		return saveWkspcTime;
	}
	/**
	 * @param saveWkspcTime The saveWkspcTime to set.
	 */
	public void setSaveWkspcTime(float saveWkspcTime) {
		this.saveWkspcTime = saveWkspcTime;
	}


	/**
	 * @return Returns the ruleExpr.
	 */
	public RulesExpressivity getRuleExpr() {
		return ruleExpr;
	}
	

	/**
	 * @param ruleExpr
	 *            The ruleExpr to set.
	 */
	public void setRuleExpr(RulesExpressivity ruleExpr) {
		this.ruleExpr = ruleExpr;
	}
}
