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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.io.File;
import java.io.StringWriter;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

import javax.swing.AbstractButton;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComboBox;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.ListModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.text.Document;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import org.mindswap.pellet.PelletOptions;
import org.mindswap.pellet.utils.Timer;
import org.mindswap.swoop.annotea.AnnoteaRenderer;
import org.mindswap.swoop.change.ChangeLog;
import org.mindswap.swoop.popup.PopupAddClass;
import org.mindswap.swoop.popup.PopupExplanation;
import org.mindswap.swoop.popup.PopupNew;
import org.mindswap.swoop.reasoner.PelletReasoner;
import org.mindswap.swoop.reasoner.SwoopRDFSReasoner;
import org.mindswap.swoop.reasoner.SwoopReasoner;
import org.mindswap.swoop.reasoner.SwoopToldReasoner;
import org.mindswap.swoop.renderer.SwoopCellRenderer;
import org.mindswap.swoop.renderer.SwoopEditableRenderer;
import org.mindswap.swoop.renderer.SwoopEntityRenderer;
import org.mindswap.swoop.renderer.SwoopRenderer;
import org.mindswap.swoop.renderer.entity.AbstractSyntaxEntityRenderer;
import org.mindswap.swoop.renderer.entity.ConciseFormatEntityRenderer;
import org.mindswap.swoop.renderer.entity.RDFXMLEntityRenderer;
import org.mindswap.swoop.renderer.entity.TurtleEntityRenderer;
import org.mindswap.swoop.renderer.entity.graph.GraphPanel;
import org.mindswap.swoop.utils.PluginLoader;
import org.mindswap.swoop.utils.SwoopCache;
import org.mindswap.swoop.utils.SwoopLoader;
import org.mindswap.swoop.utils.explain.DatatypeExplanationHTMLSerializer;
import org.mindswap.swoop.utils.explain.OWLVocabularyExplanationDeposit;
import org.mindswap.swoop.utils.explain.RDFSVocabularyExplanationDeposit;
import org.mindswap.swoop.utils.explain.RDFVocabularyExplanationDeposit;
import org.mindswap.swoop.utils.explain.VocabularyExplanationHTMLSerializer;
import org.mindswap.swoop.utils.explain.XSDDatatypeExplanationDeposit;
import org.mindswap.swoop.utils.ui.AlphaListRenderer;
import org.mindswap.swoop.utils.ui.BrowserControl;
import org.mindswap.swoop.utils.ui.ComparatorFrame;
import org.mindswap.swoop.utils.ui.EntityComparator;
import org.mindswap.swoop.utils.ui.SwoopIcons;
import org.mindswap.swoop.utils.ui.turtle2RDF.Turtle2RDFConverter;
import org.semanticweb.owl.io.RendererException;
import org.semanticweb.owl.io.vocabulary.OWLVocabularyAdapter;
import org.semanticweb.owl.io.vocabulary.RDFSVocabularyAdapter;
import org.semanticweb.owl.io.vocabulary.RDFVocabularyAdapter;
import org.semanticweb.owl.io.vocabulary.XMLSchemaSimpleDatatypeVocabulary;
import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLDataProperty;
import org.semanticweb.owl.model.OWLDescription;
import org.semanticweb.owl.model.OWLEntity;
import org.semanticweb.owl.model.OWLException;
import org.semanticweb.owl.model.OWLIndividual;
import org.semanticweb.owl.model.OWLNamedObject;
import org.semanticweb.owl.model.OWLObjectProperty;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLProperty;
import org.semanticweb.owl.model.OWLSubClassAxiom;
import org.semanticweb.owl.model.change.OntologyChange;
import org.semanticweb.owl.model.helper.OntologyHelper;
import org.xngr.browser.editor.XmlEditorPane;


/**
 * @author Aditya Kalyanpur
 *
 */
public class TermsDisplay extends SwoopDisplayPanel 
implements ActionListener, MouseListener, KeyListener, ChangeListener, ListSelectionListener, TreeSelectionListener, SwoopModelListener, HyperlinkListener {
	
	/*
	 * Global UI objects
	 */
	JButton lookupBtn;
	JButton addClassBtn, addPropBtn, addIndBtn, addGCIBtn, remTermBtn, renameTermBtn;
	JPanel termEditButtonPane, termListPanel, termDisplayEditPane;
	JButton applyChangesBtn, undoChangesBtn;
	JTextField lookupFld;
	JPopupMenu popupMenu;
	JMenuItem openBrowserMenu;
	Font tahoma = new Font("Tahoma", Font.PLAIN, 11);    
	JList termList;
	public JTabbedPane termTabPane, termDisplayPane;
	JCheckBox showImportChk, showQNamesChk, termEditableChk, showIconsChk, searchAllChk;
	JCheckBoxMenuItem debugChk, debugGlassChk, debugBlackChk;
	JCheckBox showInheritedChk;
	JComboBox reasonerCombo;
	JTree[] trees = new JTree[2];
	OWLOntology classTreeOfOntology, propTreeOfOntology;
	JComboBox filterCombo;
	JTabbedPane advancedPane;
	SwoopSearch lookupPanel, referencePanel;
	URI treeEntityURI;
	JLabel warningEditLbl;
	
	//Dave: added to support xsd datatype rendering/ owl vocabulary rendering	
	XSDDatatypeExplanationDeposit    myXSDDatatypeExplanations  = XSDDatatypeExplanationDeposit.getInstance();
	OWLVocabularyExplanationDeposit  myOWLVocabExplanations     = OWLVocabularyExplanationDeposit.getInstance();
	RDFSVocabularyExplanationDeposit myRDFSVocabExplanations    = RDFSVocabularyExplanationDeposit.getInstance();
	RDFVocabularyExplanationDeposit  myRDFVocabExplanations     = RDFVocabularyExplanationDeposit.getInstance();
	
	public final static String[] filterNames= {
	    "Show All",
	    "Show Individuals",
	    "Show Classes",
	    "Show Properties",
		"Show GCIs",
		//****************************************
		//Added for Econnections
		//****************************************
		 "Show Foreign Entities",
		//***************************************
//	    "Show DataProperties",
//	    "Show ObjectProperties",
	};
	public final static int FILTER_COUNT =  filterNames.length;;
	
	/*
	 * Important public fields
	 */
	SwoopModel swoopModel;	// model shared by all swoop components
	SwoopFrame swoopHandler; // handler for SwoopFrame instance
	TreeRenderer treeRenderer; // renders class and property trees  
	AnnoteaRenderer annoteaRenderer; // renders entity annotations 
	public ComparatorFrame comparator; // resource holder 
	boolean rightClicked = false; // used by popup menu 
	boolean enableLogging = true; // toggle enable logging of changes
	String urlClicked = ""; // used by popup menu
	SwoopCache listCache; // cache for alphabetical term list
	
	/*
	 * A list of renderers associated with each tab. The list contains SwoopEntityRenderer
	 * objects and the index of renderers correspond to index of tabs
	 */
	public List renderers = new ArrayList();
	
	/*
	 * A list of editors associated with each tab. The list contains JEditorPane
	 * objects and the index of editors correspond to index of tabs
	 */
	public List editors = new ArrayList();
	
	/*
	 * List of reasoners avaliable
	 */
	List reasoners = new ArrayList();	

	// constructor
	public TermsDisplay(SwoopFrame swf, SwoopModel swoopModel) {		
	
		this.swoopModel = swoopModel;
		this.swoopHandler = swf;
		this.treeRenderer = new TreeRenderer(swoopModel, swoopHandler);
		this.lookupPanel = new SwoopSearch(swoopModel, this, "Lookup Results");
		this.referencePanel = new SwoopSearch(swoopModel, this, "Reference Results");
		comparator = new ComparatorFrame(this);
		
		listCache = new SwoopCache();
		setupUI();
		setupPopupMenu();
		this.refreshEditorMode();		
	}
	
	
	
	public void setupUI () {
		
		
		lookupBtn = new JButton("Lookup");
		lookupBtn.setFont(tahoma);
		lookupBtn.addActionListener(this);		
		lookupFld = new JTextField();
		lookupFld.setFont(new Font(swoopModel.getFontFace(), Font.PLAIN, 11));
		lookupFld.addActionListener(this);
		searchAllChk = new JCheckBox("All Ontologies?");
		searchAllChk.setFont(tahoma);
		searchAllChk.setSelected(swoopModel.isLookupAllOnts());
		searchAllChk.addActionListener(this);
		
		// panel for term list and description pane
		JPanel centerPanel = new JPanel();
		centerPanel.setLayout(new GridLayout(1,1));
		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		splitPane.setOneTouchExpandable(true);
		
		// if termlist is already created and re-drawn, save sel-index
		int termIndex = -1;
		if (termList!=null) {
			termIndex = termList.getSelectedIndex();
		}	
		
		// creation of term list
		termList = new JList();
		termList.addListSelectionListener(this);
		termList.addMouseListener(this);
		termList.addKeyListener(this);
		termList.setCellRenderer(new SwoopCellRenderer(swoopModel));
		termList.setFont(new Font(swoopModel.getFontFace(), Font.PLAIN, 11));
//		termList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		JScrollPane termListPane = new JScrollPane(termList);
		
		// create alphabetical list pane
		JPanel alphaPane = new JPanel();
		alphaPane.setLayout(new BorderLayout());
		alphaPane.add(termListPane, "Center");
		filterCombo = new JComboBox();
		filterCombo.setFont(tahoma);
		
		filterCombo.setRenderer(new AlphaListRenderer());
		
		for( int i = 0; i < FILTER_COUNT; i++ ) {
		    filterCombo.addItem(filterNames[i]);
		}
		filterCombo.addActionListener(this);
		filterCombo.setSelectedItem(swoopModel.getTermListFilter());
		alphaPane.add(filterCombo, "North");
		
		// create tabbed pane for terms - tree hierarchy
		termTabPane = new JTabbedPane();
		termTabPane.addTab("Class Tree", new JScrollPane());
		termTabPane.addTab("Property Tree", new JScrollPane());
		termTabPane.addTab("List", alphaPane);
		termTabPane.setFont(tahoma);
		termTabPane.addChangeListener(this);
		
		// add termlist and tabpane to termPanel
		JPanel termPanel = new JPanel();
		termPanel.setLayout(new BorderLayout());
		//termPanel.setOneTouchExpandable(true);
		
		JPanel termTabPanel = new JPanel();
		termTabPanel.setLayout(new BorderLayout());
		JPanel lookupPanel = new JPanel();
		lookupPanel.setLayout(new BorderLayout());
		lookupPanel.add(lookupBtn, "West");
		lookupPanel.add(lookupFld, "Center");
		lookupPanel.add(searchAllChk, "East");
		termTabPanel.add(termTabPane, "Center");
		termTabPanel.add(lookupPanel, "South");
		termPanel.add(termTabPanel, "Center");
		
		// add termPanel to termListPanel and create add/edit buttons
		termListPanel = new JPanel();
		termListPanel.setLayout(new BorderLayout());
		JPanel tp = new JPanel();
		tp.setLayout(new BorderLayout());
		tp.add(termPanel, "Center");
		showImportChk = new JCheckBox("Show Imports");
		showImportChk.setFont(tahoma);
		showImportChk.addActionListener(this);
		showImportChk.setSelected(swoopModel.getShowImports());
		showQNamesChk = new JCheckBox("QNames");
		showQNamesChk.setFont(tahoma);
		showQNamesChk.addActionListener(this);
		showQNamesChk.setSelected(swoopModel.getShowQNames());
		
		reasoners = new ArrayList();
		
		// find all the classes that implements SwoopReasoner
		// interface and try to generate a reasoner

		PluginLoader ph = PluginLoader.getInstance();
		List list = ph.getClasses(SwoopReasoner.class);		
		if (!Swoop.isWebStart()) {
			for(int i = 0; i < list.size(); i++) {
				Class cls = (Class) list.get(i);
				if(cls.isInterface()) continue;
				try {
					System.out.println("Try creating reasoner " + cls.getName());
					SwoopReasoner reasoner = (SwoopReasoner) cls.newInstance();
					reasoners.add(reasoner);
				} 
				catch (Throwable e) {
					e.printStackTrace();
					continue;
				}
			}
		}
		else {
			// manually load reasoners for Webstart
			reasoners.add(new SwoopToldReasoner());
			reasoners.add(new SwoopRDFSReasoner());
			reasoners.add(new PelletReasoner());
		}
		
		// add set of reasoners to swoopModel.reasonerMap
		for (int i=0; i<reasoners.size(); i++) {
			SwoopReasoner reas = (SwoopReasoner) reasoners.get(i); 
			swoopModel.addReasonerMap(reas.getName(), reas);
		}
		
		// sort the reasoners alphabetically though place Told first
		Collections.sort(reasoners, new Comparator() {			
			public int compare(Object o1, Object o2) {
				SwoopReasoner r1 = (SwoopReasoner) o1;
				SwoopReasoner r2 = (SwoopReasoner) o2;
				if (r1 instanceof SwoopToldReasoner) return -1;
				if (r2 instanceof SwoopToldReasoner) return 1;
				return r1.getName().compareTo(r2.getName());				
			}			
		});
		
		reasonerCombo = new JComboBox();
		reasonerCombo.setFont(tahoma);
		for(int i = 0; i < reasoners.size(); i++) {
			SwoopReasoner reasoner = (SwoopReasoner) reasoners.get(i);
			reasonerCombo.addItem(reasoner.getName());
		}
		reasonerCombo.addActionListener(this);

		JPanel topPanel = new JPanel();
		topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.X_AXIS));
		topPanel.add(showImportChk);
		topPanel.add(Box.createHorizontalGlue());
		topPanel.add(showQNamesChk);
		topPanel.add(Box.createHorizontalGlue());		
		topPanel.add(reasonerCombo);
		tp.add(topPanel, "North");
		termListPanel.add(tp, "Center");
		
		addClassBtn = new JButton("Add", SwoopIcons.smallClassIcon);
		addClassBtn.setHorizontalTextPosition(AbstractButton.LEFT);
		addPropBtn = new JButton("Add", SwoopIcons.smallPropIcon);
		addPropBtn.setHorizontalTextPosition(AbstractButton.LEFT);
		addIndBtn = new JButton("Add", SwoopIcons.smallIndIcon);
		addIndBtn.setHorizontalTextPosition(AbstractButton.LEFT);
		addGCIBtn = new JButton("Add GCI");
		addGCIBtn.setHorizontalTextPosition(AbstractButton.LEFT);
		remTermBtn = new JButton("Remove");
		renameTermBtn = new JButton("Rename");
		
		// add buttons to Entity Tool Bar
		addClassBtn.setFont(tahoma);
		addClassBtn.setEnabled(false);
    	addClassBtn.addActionListener(this);        	        
		addPropBtn.setFont(tahoma);
		addPropBtn.setEnabled(false);
		addPropBtn.addActionListener(this);
		addIndBtn.setFont(tahoma);
		addIndBtn.setEnabled(false);
		addIndBtn.addActionListener(this);
		addGCIBtn.setFont(tahoma);
		addGCIBtn.setEnabled(false);
		addGCIBtn.addActionListener(this);
		remTermBtn.setFont(tahoma);
		remTermBtn.setEnabled(false);
		remTermBtn.addActionListener(this);
		renameTermBtn.setFont(tahoma);
		renameTermBtn.setEnabled(false);
		renameTermBtn.addActionListener(this);
		JPanel termToolBar = new JPanel();
		termToolBar.setLayout(new GridLayout(2,3));
		termToolBar.add(addClassBtn);
		termToolBar.add(addPropBtn);
		termToolBar.add(addIndBtn);
		termListPanel.add(termToolBar, "North");
		termToolBar.add(addGCIBtn);
		termToolBar.add(remTermBtn);
		termToolBar.add(renameTermBtn);
		
		// add components to splitPane
		splitPane.setLeftComponent(termListPanel);
		termDisplayPane = new JTabbedPane();
		termDisplayPane.setFont(tahoma);
		termDisplayPane.getSelectedComponent();
				
		// find all the other classes that implements entityRenderer
		// interface and try to generate a renderer for them		
		
		if (!Swoop.isWebStart()) {
			list = ph.getClasses(SwoopEntityRenderer.class);
			for(int i = 0; i < list.size(); i++) {
				Class cls = (Class) list.get(i);
				if(cls.isInterface()) continue;
				SwoopEntityRenderer renderer = null;
				try {
					renderer = (SwoopEntityRenderer) cls.newInstance();
					renderers.add(renderer);
				} catch (Throwable e) {
					e.printStackTrace();
					continue;
				}
			}
		}
		else {	
			// manually add renderers for webstart
			renderers.add(new ConciseFormatEntityRenderer());
			renderers.add(new AbstractSyntaxEntityRenderer());
			renderers.add(new RDFXMLEntityRenderer());
			renderers.add(new TurtleEntityRenderer());
		}
		
		// sort the renderers such that ConciseFormat is always first and the
		// rest is ordered alphabetically
		Collections.sort(renderers, new Comparator() {
			public int compare(Object o1, Object o2) {
				SwoopEntityRenderer r1 = (SwoopEntityRenderer) o1;
				SwoopEntityRenderer r2 = (SwoopEntityRenderer) o2;
				if(r1 instanceof ConciseFormatEntityRenderer)
					return -1;
				if(r2 instanceof ConciseFormatEntityRenderer)
					return 1;
				return r1.getName().compareTo(r2.getName());				
			}			
		});

		// add the editor for each renderer 
		for(int i = 0; i < renderers.size(); i++) {
			SwoopEntityRenderer renderer = (SwoopEntityRenderer) renderers.get(i);
			Component component = renderer.getDisplayComponent( this );
			termDisplayPane.addTab(renderer.getName(), new JScrollPane( component ));
			editors.add( component );
		}
		
		termDisplayPane.addChangeListener(this);
		
		termDisplayEditPane = new JPanel();
		termDisplayEditPane.setLayout(new BorderLayout());
		
		// add warning label while editing in RDF/XML tab
		warningEditLbl = new JLabel("", JLabel.CENTER);
		warningEditLbl.setFont(tahoma);
		
		// add apply/undo changes buttons
		applyChangesBtn = new JButton("Apply Changes");
		applyChangesBtn.addActionListener(this);
		applyChangesBtn.setFont(tahoma);
		undoChangesBtn = new JButton("Undo Changes");
		undoChangesBtn.addActionListener(this);
		undoChangesBtn.setFont(tahoma);
		termEditButtonPane = new JPanel();
		termEditButtonPane.setLayout(new BorderLayout());
		JPanel buttonPane = new JPanel();
		buttonPane.add(applyChangesBtn);
		buttonPane.add(undoChangesBtn);
		termEditButtonPane.add(warningEditLbl, "North");
		termEditButtonPane.add(buttonPane, "Center");
		termEditButtonPane.setVisible(swoopModel.getEditorEnabled());
		
		// add term editable check box option
		termEditableChk = new JCheckBox("Editable");
		termEditableChk.addActionListener(this);
		//termEditableChk.addChangeListener(this); // IMPORTANT: TURNING THIS ON CAUSES WEIRD (RANDOM) NOTIFICATION ERRORS
		termEditableChk.setFont(tahoma); 
		termEditableChk.setToolTipText("Toggle Editable Mode");
		termEditableChk.setSelected(swoopModel.getEditorEnabled());
		
		// add enable debugging check boxes
		debugChk = new JCheckBoxMenuItem("Basic");
		debugChk.addActionListener(this);
		debugChk.addVetoableChangeListener(new EnableDebugListener());
		debugChk.setFont(tahoma); 
		debugChk.setToolTipText("Basic Debugging Mode - Clash information with Class Expression highlighting");
		debugChk.setSelected(swoopModel.getEnableDebugging());
		
		debugGlassChk = new JCheckBoxMenuItem("Display Clash / SOS Axioms Inline");
		debugGlassChk.addActionListener(this);
		debugGlassChk.addVetoableChangeListener(new EnableDebugListener());
		debugGlassChk.setFont(tahoma); 
		debugGlassChk.setToolTipText("Sets of Support Axioms for an Unsatisfiable Class");
		debugGlassChk.setSelected(swoopModel.isDebugGlass());
		
		debugBlackChk = new JCheckBoxMenuItem("Display Root / Derived");
		debugBlackChk.addActionListener(this);
		debugBlackChk.addVetoableChangeListener(new EnableDebugListener());
		debugBlackChk.setFont(tahoma); 
		debugBlackChk.setToolTipText("Black Box Debugging Mode - Root and Derived Unsat. Classes");
		debugBlackChk.setSelected(swoopModel.isDebugBlack());
		
		// add show inherited check box
		showInheritedChk = new JCheckBox("Show Inherited");
		showInheritedChk.addActionListener(this);
		showInheritedChk.setFont(tahoma); 
		showInheritedChk.setToolTipText("Show Inherited Domains, Ranges and Individuals while rendering Entities");
		showInheritedChk.setSelected(swoopModel.getShowInherited());
		
		// add show icons check box
		showIconsChk = new JCheckBox("Show Icons");
		showIconsChk.addActionListener(this);
		showIconsChk.setFont(tahoma); 
		showIconsChk.setToolTipText("Show Icons for OWL Objects");
		showIconsChk.setVisible(swoopModel.getShowIcons());

		termDisplayEditPane.add(termDisplayPane, "Center");
		
		splitPane.setRightComponent(termDisplayEditPane);
		centerPanel.add(splitPane);
		
		// add components to main panel
		setLayout(new BorderLayout());
		add(centerPanel, "Center");
		
		// set previously selected term index
		if (termIndex!=-1) {
			termList.setSelectedIndex(termIndex);			
		}
		splitPane.setDividerLocation(280);
	}
	
	/*
	 * Return current class and property trees
	 */
	public JTree[] getTrees() {
		return this.trees;
	}
	
	/*
	 * Return ontology the current class tree displays (if any)
	 */
	public OWLOntology getClassTreeOfOntology() {
		return this.classTreeOfOntology;	
	}
	 
	/*
	 * Return ontology the current property tree displays (if any)
	 */
	public OWLOntology getPropTreeOfOntology() {
		return this.propTreeOfOntology;	
	}
	
	public SwoopModel getSwoopModel() {
		return this.swoopModel;
	}
	
	/**
	 * Used to check if a URL clicked on in the entity pane
	 * is a valid e-mail id or not
	 * (need better regular expression matching here)
	 * @param url
	 * @return
	 */
	private boolean isEMailID(String url) {
		
		// *** hack to check if string is a valid e-mail address or not
		if (url.indexOf("@")>=0) {
			int pos = url.indexOf("@");
			if (url.indexOf(".", pos)>=0) {
				if (url.toLowerCase().endsWith("com") || 
					url.toLowerCase().endsWith("org") || 
					url.toLowerCase().endsWith("edu") ||
					url.toLowerCase().endsWith("net") ||
					url.toLowerCase().endsWith("gov"))
					return true;
			}
		}
		return false;
	}
	
	/**
	 * Add current selected entity to the SWOOP Resource Holder
	 * The resource holder is Static - it saves a snapshot of the
	 * current state of the entity as rendered in the entity pane. 
	 * Can be used for comparing changes at a later stage
	 */
	public void addEntityToResourceHolder() {
		
		if (swoopModel.selectedOWLObject==null) return;
		
		String renderText = "";
		
		// prefix timestamp and ontology at start
		String timeStamp = swoopModel.getTimeStamp();
		renderText = "<font face=\""+swoopModel.getFontFace()+"\" size="+swoopModel.getFontSize()+"><b>Time:</b>&nbsp;</i>"+timeStamp+"</i><br></font";
		
		URI ontURI;
		try {
			ontURI = swoopModel.getSelectedOntology().getURI();
			renderText += "<font face=\""+swoopModel.getFontFace()+"\" size="+swoopModel.getFontSize()+"><b>Ontology:</b>&nbsp;" + swoopModel.shortForm(ontURI) + "</font><br>";
		} catch (OWLException e) {			
			e.printStackTrace();
		}
		
		// get current rendered text
		if (swoopModel.selectedOWLObject instanceof OWLOntology) {
			int rendererIndex = swoopHandler.ontDisplay.ontDescTab.getSelectedIndex();
			JEditorPane editor = (JEditorPane) swoopHandler.ontDisplay.editors.get(rendererIndex);
			renderText += "<font face=\""+swoopModel.getFontFace()+"\" size="+swoopModel.getFontSize()+">"+editor.getText();
		}
		else {
			int rendererIndex = termDisplayPane.getSelectedIndex();
			SwoopEntityRenderer renderer = (SwoopEntityRenderer) renderers.get(rendererIndex);
			
			// take current text in displayed editorPane
			Component editor = (Component) editors.get(rendererIndex);
			if (editor instanceof JEditorPane) renderText += ((JEditorPane) editor).getText();
			
//			System.out.println(renderText);
		}
		try {
			// add source, entity-uri and description as a string
			//TODO: maybe pass entity object itself or its RDF/XML serialization instead
			comparator.addEntity("Main SWOOP", swoopModel.selectedOWLObject.getURI(), renderText);
		} catch (OWLException e1) {			
			e1.printStackTrace();
		}		
	}
	
	/**
	 * Sets up the popup menu that gets invoked when the user
	 * right clicks over the entity pane
	 * Useful option in the popup includes a call to a web browser
	 * or mail client depending on type of selected entity (as determined from its url)
	 */
	private void setupPopupMenu() {
		
		popupMenu = new JPopupMenu();
		// add menu items
		JMenuItem refsMenu = new JMenuItem("Show References");
		JMenuItem addBMMenu = new JMenuItem("Add to Bookmarks");
		JMenuItem addCompMenu = new JMenuItem("Add to Resource Holder (for comparison)");
		openBrowserMenu = new JMenuItem();
		JMenu optionsMenu = new JMenu("Options");
		JMenuItem hideMenu = new JMenuItem("Hide");
		
		// add listeners
		refsMenu.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {				
				
				OWLOntology ontology = swoopModel.getSelectedOntology();
				OWLEntity entity = null;
				try {
					entity = ontology.getClass(new URI(urlClicked));
					if (entity==null) entity = ontology.getDataProperty(new URI(urlClicked));
					if (entity==null) entity = ontology.getObjectProperty(new URI(urlClicked));
					if (entity==null) entity = ontology.getIndividual(new URI(urlClicked));					
				}
				catch (Exception ex) {
					ex.printStackTrace();					
				}
				if (entity==null) entity = swoopModel.getSelectedEntity();
				showReferencesTerm(ontology, entity);
				popupMenu.setVisible(false);
				rightClicked = false;
				urlClicked = "";
			}	
		});
			
		addBMMenu.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {				
				swoopHandler.addBookmark();
				popupMenu.setVisible(false);
				rightClicked = false;
				urlClicked = "";
			}
		});
		
		addCompMenu.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					addEntityToResourceHolder();
					popupMenu.setVisible(false);
					rightClicked = false;
					urlClicked = "";
				}
				catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		});

		openBrowserMenu.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				if (urlClicked.equals("") && swoopModel.getSelectedEntity()!=null) {
					try {
						OWLEntity entity = swoopModel.getSelectedEntity();
						urlClicked = entity.getURI().toString();
					}
					catch (Exception ex) {
						ex.printStackTrace();
					}
				}
				
				BrowserControl.displayURL(urlClicked);
				popupMenu.setVisible(false);
				rightClicked = false;
				urlClicked = "";
			}
		});
		
		hideMenu.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				popupMenu.setVisible(false);
				rightClicked = false;
				urlClicked = "";
			}
		});
		
		// UI stuff
		popupMenu.add(refsMenu);
		popupMenu.addSeparator();
		popupMenu.add(addBMMenu);
		popupMenu.add(addCompMenu);
		popupMenu.addSeparator();
		popupMenu.add(openBrowserMenu);
		popupMenu.addSeparator();
		popupMenu.add(hideMenu);
		popupMenu.setSize(100, 200);
		popupMenu.setVisible(false);
	}
	
	/**
	 * Create a JEditorPane with the specified contentType and set default attributes
	 * @param contentType
	 * @return
	 */
	private JEditorPane getEditorPane(String contentType) {
		JEditorPane editorPane = null;
		if(contentType.equals("text/plain")) {
			editorPane = new JEditorPane();
			//editorPane.setFont(new Font("Verdana", Font.PLAIN, 10));
                        editorPane.setFont(new Font("SansSerif", Font.PLAIN, 10));
		}
		else if(contentType.equals("text/html")) {
			editorPane = new JEditorPane();
			editorPane.addHyperlinkListener(this);
			editorPane.addMouseListener(this);		
		}
		else if(contentType.equals("text/xml"))
			editorPane = new JEditorPane();
		else
			throw new RuntimeException("Cannot create an editor pane for content type " + contentType);
		
		editorPane.setEditable(false);
		editorPane.setContentType(contentType);
		return editorPane;
	}

	/**
	 * Popup panel for creating a new OWL Class/Property/Individual
	 * @param type
	 * @param ont
	 * @throws OWLException
	 */
	protected void popup(String type, OWLOntology ont) throws OWLException {
		PopupNew popupPanel = new PopupNew(this, swoopModel, type, ont);
		swoopModel.addListener(popupPanel);
		popupPanel.setLocation(200,200);
		popupPanel.setVisible( true );
		
		if (type.equals("Class")) termTabPane.setSelectedIndex(0);
		else if (type.equals("Property")) termTabPane.setSelectedIndex(1);
		else if (type.equals("Individual")) termTabPane.setSelectedIndex(2);
		
	}
	
	public void addNewClass(OWLOntology ont) {
		try {
			popup("Class", ont);
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	public void addNewProperty(OWLOntology ont) {
		try {
			popup("Property", ont);
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	public void addNewIndividual(OWLOntology ont) {
		try {
			popup("Individual", ont);
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	/*
	 * Popup to create a new GCI to add to 
	 * the currently selected ontology
	 */
	private void popupNewGCI() {
		// select GCIs List
		termTabPane.setSelectedIndex(2);
		filterCombo.setSelectedIndex(4);
		PopupAddClass popup = new PopupAddClass(swoopModel.getReasoner(), "GCI-LEFT", swoopModel);
		popup.setLocation(200, 200);
		popup.setVisible( true );		 
	}
	
	/**
	 * Remove all references of an OWL entity from the ontology
	 * @param ont
	 * @param entity
	 */
	public void removeEntity(OWLOntology ont, OWLEntity entity, boolean warning) {
		try {
			int result = -1;
			
			if (warning) {
				String title = "Remove OWL Entity";
				int options = JOptionPane.YES_NO_OPTION;
				result = JOptionPane.showConfirmDialog(this, "This is going to remove ALL References of the Entity from the Ontology. Continue?", title, options);
			}
			
			if(result==JOptionPane.YES_OPTION || !warning) {
				
				// save checkpoint before removing entity if auto-save checkpoints is enabled
				if (swoopModel.getEnableAutoSaveChkPts()) {
					swoopHandler.changeLog.saveCheckPoint(ChangeLog.ONTOLOGY_SCOPE, ont, entity, "Saving State of Ontology BEFORE removing Entity:" + swoopModel.shortForm(entity.getURI()), null);
				}
				swoopModel.removeEntity(ont, entity, true);
				// save checkpoint after removing entity if auto-save checkpoints is enabled
				if (swoopModel.getEnableAutoSaveChkPts()) {
					swoopHandler.changeLog.saveCheckPoint(ChangeLog.ONTOLOGY_SCOPE, ont, entity, "Saving State of Ontology AFTER removing Entity:" + swoopModel.shortForm(entity.getURI()), null);
				}
			}	
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	/**
	 * Clear tree and alphabetical list selections
	 * Turn off listeners while clearing selection!
	 *
	 */
	public void clearSelections() {
		this.disableUIListeners();
		if (trees[0]!=null) trees[0].clearSelection();
		if (trees[1]!=null) trees[1].clearSelection();
		if (termList!=null) termList.clearSelection();
		this.enableUIListeners();
	}
	
	/**
	 * IMPORTANT NOTE: Many bugs in Swoop loading/traversal have been caused
	 * because multiple copies of listeners are added on the same UI element
	 * e.g. multiple List/Tree selection listeners..this obviously creates all
	 * sorts of problems and to prevent this, we remove all listeners
	 * 
	 * Disable tree,list selection and tabPane change listeners
	 * Used during history traversal
	 */
	public void disableUIListeners() {
		
//		for (int safe=0; safe<5; safe++) {
			// turn off all listeners
			for (int i=0; i<trees.length; i++) if (trees[i]!=null) {
				trees[i].removeTreeSelectionListener(this);	// when tree node selected, render new entity			
			}
			termList.removeListSelectionListener(this); // when alphabetic term list selection changes, render new entity
			termTabPane.removeChangeListener(this); // when tab changes trees are redrawn
			termDisplayPane.removeChangeListener(this);	// whenever renderer tab changes, term re-rendered
//		}
	}
	
	public void enableUIListeners() {
		
		// below is a safety mechanism to ensure that multiple copies of listener aren't added
		this.disableUIListeners(); 
		
		// turn on all listeners
		for (int i=0; i<trees.length; i++) if (trees[i]!=null) {
			trees[i].addTreeSelectionListener(this);
		}
		termList.addListSelectionListener(this);
		termTabPane.addChangeListener(this);
		termDisplayPane.addChangeListener(this);
	}
	
		
	/**
	 * Apply changes when user edits text directly (abstract syntax, rdf/xml..)
	 */ 
	public void applyDirectTextChanges() {

		try {	
			// if uncommitted changes exist on the list you need to commit those instead
			if (swoopModel.getUncommittedChanges().size()>0) {
				System.out.println("Applied Uncommited Changes alone - skipping text changes...");
				return;
			}
			
			// get the entity description only 										
			int index = termDisplayPane.getSelectedIndex();
			SwoopEntityRenderer renderer = (SwoopEntityRenderer) renderers.get(index);

			// New approach for RDF/XML renderer
			if (renderer instanceof RDFXMLEntityRenderer) {
				
				OWLEntity currEntity = swoopModel.getSelectedEntity();
				OWLOntology currOnt = swoopModel.getSelectedOntology();
				
				// get new entity code
				XmlEditorPane editor = (XmlEditorPane) editors.get(index);
				String newEntityCode = editor.getText();
				//String currEntityURI = currEntity.getURI().toString();
				
				// save entity checkpoint if option is enabled BEFORE operation
    				if (swoopModel.getEnableAutoSaveChkPts()) {
    					swoopHandler.changeLog.saveCheckPoint(ChangeLog.ENTITY_SCOPE, swoopModel.selectedOntology, swoopModel.selectedEntity, "Saving State BEFORE change made directly in RDF/XML code", null);
    				}
				
    				// --- call swoopModel function to perform RDF edit ---
				boolean update = swoopModel.replaceEntityRDF(currOnt, currEntity, newEntityCode);
	    		
				if (update) {
	    			// save entity checkpoint if option is enabled AFTER operation
	    			if (swoopModel.getEnableAutoSaveChkPts()) {
	    				swoopHandler.changeLog.saveCheckPoint(ChangeLog.ENTITY_SCOPE, swoopModel.selectedOntology, swoopModel.selectedEntity, "Saving State AFTER change made directly in RDF/XML code", null);
	    			}
	    		}
	    		else {
	    			JOptionPane.showMessageDialog(this, "Error parsing RDF/XML source code", "Parse Error", JOptionPane.ERROR_MESSAGE);
	    		}
	    		
	    		applyChangesBtn.setEnabled(false);
			}
			
			else if (renderer instanceof AbstractSyntaxEntityRenderer) {
				
				// get current selections
				OWLEntity currEntity = swoopModel.getSelectedEntity();
				OWLOntology currOnt = swoopModel.getSelectedOntology();
				
				// get new entity code
				JEditorPane editor = (JEditorPane) editors.get(index);
				String newEntityStr = editor.getText();
				String newEntityCode = editor.getText(1,editor.getDocument().getLength()-1);
				// get string after entity name
				String str = "";
				if (currEntity instanceof OWLClass) str = "Class"; 
				else if (currEntity instanceof OWLObjectProperty) str = "ObjectProperty";
				else if (currEntity instanceof OWLDataProperty) str = "DataProperty";
				else if (currEntity instanceof OWLIndividual) str = "Individual";
				newEntityCode = newEntityCode.substring(newEntityCode.indexOf(str), newEntityCode.lastIndexOf(")"));
				
				// clone ontology, remove entity and then get its AS code
//				OWLOntology copy = swoopModel.cloneOntology(currOnt);
//				OWLEntity ent = swoopModel.getEntity(copy, currEntity.getURI(), true);
//				RemoveEntity re = new RemoveEntity(copy, ent, null);
//				re.accept((ChangeVisitor) copy);
				
				StringWriter st1 = new StringWriter();
//				org.semanticweb.owl.io.abstract_syntax.Renderer asRend = 
//					new org.semanticweb.owl.io.abstract_syntax.Renderer();
//				asRend.renderOntology(copy, st1);
				AbstractSyntaxEntityRenderer asRend = (AbstractSyntaxEntityRenderer) renderer;
				asRend.setNoLinks(true);
				asRend.renderOntology(swoopModel, currOnt, currEntity, newEntityCode, st1);
//				String ontStr = st1.toString();
//				ontStr = ontStr.substring(0, ontStr.lastIndexOf(")"));
//				newEntityCode = ontStr + newEntityCode + ")";;
				String newOntCode = st1.toString();
				
				// get oldEntity code as well
//				StringWriter st2 = new StringWriter();
//				org.semanticweb.owl.io.abstract_syntax.Renderer as = new org.semanticweb.owl.io.abstract_syntax.Renderer();
//				as.renderOntology(currOnt, st2);
//				String oldEntityCode = st2.toString(); 
					
//				String currEntityURI = currEntity.getURI().toString();
				
				// save entity checkpoint if option is enabled BEFORE operation
    			if (swoopModel.getEnableAutoSaveChkPts()) {
    				swoopHandler.changeLog.saveCheckPoint(ChangeLog.ENTITY_SCOPE, swoopModel.selectedOntology, swoopModel.selectedEntity, "Saving State BEFORE change made directly in RDF/XML code", null);
    			}
				
    			// --- call update method in swoopModel
				boolean update = swoopModel.replaceEntityAS(currOnt, newOntCode);
	    		
				if (update) {
	    			// save entity checkpoint if option is enabled AFTER operation
	    			if (swoopModel.getEnableAutoSaveChkPts()) {
	    				swoopHandler.changeLog.saveCheckPoint(ChangeLog.ENTITY_SCOPE, swoopModel.selectedOntology, swoopModel.selectedEntity, "Saving State AFTER change made directly in RDF/XML code", null);
	    			}
	    		}
	    		else {
	    			JOptionPane.showMessageDialog(this, "Error parsing Abstract Syntax source code", "Parse Error", JOptionPane.ERROR_MESSAGE);	    			
	    		}
	    		
	    		applyChangesBtn.setEnabled(false);
			}
				
			else if ( renderer instanceof TurtleEntityRenderer )
			{
				// save old qname state
				boolean oldQNameState = swoopModel.getShowQNames();
				
				OWLEntity currEntity = swoopModel.getSelectedEntity();
				OWLOntology currOnt = swoopModel.getSelectedOntology();
				String baseURI = currOnt.getLogicalURI().toString();
				
				// get new entity code
				JEditorPane editor = (JEditorPane) editors.get(index);
				Document d = editor.getDocument();
				String newEntityCode = d.getText( 1, d.getLength() - 1 ).trim();
								
				// removing strange characters that makes TurtleParser fail
				char c = 160;
				newEntityCode = newEntityCode.replaceAll( ""+c, " " );
				
				// segment newEntityCode to have the same line breaks as displayed
				newEntityCode = Turtle2RDFConverter.addLineInfo( newEntityCode, editor.getText() );
				
				String rdfCode = Turtle2RDFConverter.turtle2RDF( newEntityCode, baseURI, this, editor );
				
				// if parse failed, stop
				if ( rdfCode == null )
				{
					return;
				}
				
				boolean update = false;
				
				if (rdfCode!=null) {
					String currEntityURI = currEntity.getURI().toString();
					
					// save entity checkpoint if option is enabled BEFORE operation
	    			if (swoopModel.getEnableAutoSaveChkPts()) {
	    				swoopHandler.changeLog.saveCheckPoint(ChangeLog.ENTITY_SCOPE, swoopModel.selectedOntology, swoopModel.selectedEntity, "Saving State BEFORE change made directly in RDF/XML code", null);
	    			}
					
	    			// --- call swoopModel function to perform RDF edit ---
					update = swoopModel.replaceEntityRDF(currOnt, currEntity, rdfCode);
				}
				 
				if (update) {
	    			// save entity checkpoint if option is enabled AFTER operation
	    			if (swoopModel.getEnableAutoSaveChkPts()) {
	    				swoopHandler.changeLog.saveCheckPoint(ChangeLog.ENTITY_SCOPE, swoopModel.selectedOntology, swoopModel.selectedEntity, "Saving State AFTER change made directly in RDF/XML code", null);
	    			}
	    		}
	    		else {
	    			JOptionPane.showMessageDialog(this, "Error parsing RDF/XML source code converted from TURTLE ", "Parse Error", JOptionPane.ERROR_MESSAGE);
	    		}
				
				applyChangesBtn.setEnabled(false);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}									
	}
	
	public void actionPerformed(ActionEvent e) {

		// when user presses ENTER while typing in lookupFld
		if ((e.getSource()==lookupFld) || (e.getSource()==lookupBtn)) {
			lookupTerm();
		}
		else
		if (e.getSource()==addClassBtn) {
			addNewClass(swoopModel.getSelectedOntology());
		}
		else
		if (e.getSource()==addPropBtn) {
			addNewProperty(swoopModel.getSelectedOntology());
		}
		else
		if (e.getSource()==addIndBtn) {
			addNewIndividual(swoopModel.getSelectedOntology());
		}
		else
		if (e.getSource()==addGCIBtn) {
			popupNewGCI();
		}
		else
		// remove term button clicked
		if (e.getSource()==remTermBtn) {
			
			List removeEntities = new ArrayList();
			
			// if tab pane is showing a class/property tree
			// get selected tree nodes
			if (termTabPane.getSelectedIndex()<2) {
				JTree tree = trees[termTabPane.getSelectedIndex()];
				if (tree.getSelectionCount()==1) {
					removeEntities.add(swoopModel.getSelectedEntity());
				}
				else { 
					TreePath[] paths = tree.getSelectionPaths();
					for (int i=0; i<paths.length; i++) {
						TreePath path = paths[i];
						Object obj = ((DefaultMutableTreeNode) path.getLastPathComponent()).getUserObject();
						if (obj instanceof Set) removeEntities.addAll((Set) obj);
					}
				}	
			}
			else {
				// if tab pane is showing alphabetical list
				// remove GCI separately
				if (termList.getSelectedValue() instanceof OWLSubClassAxiom) {
					swoopModel.removeGCI(swoopModel.selectedOntology, termList.getSelectedValues(), true);
					return;
				}
				//*** Note RETURN statement above
				if (termList.getSelectedValues().length==1) {
					removeEntities.add(swoopModel.selectedEntity);
				}
				else {
					Object[] obj = termList.getSelectedValues();
					for (int i=0; i<obj.length; i++) {
						removeEntities.add((OWLEntity) obj[i]);
					}
				}
			}
			
			// now remove entities depending on whether single entity is to be removed
			// or a batch of selected entities are to be removed
			if (removeEntities.size()==1) {
				// remove single OWL entity
				removeEntity(swoopModel.getSelectedOntology(), swoopModel.getSelectedEntity(), true);
			}
			else {
				// batch removal of entities (selected in tree)
				String title = "Batch Removal of OWL Entities";
				int options = JOptionPane.YES_NO_OPTION;
				int result = JOptionPane.showConfirmDialog(this, "This is going to remove ALL References of the Selected Entities from the Ontology. Continue?", title, options);
				
				if (result == JOptionPane.YES_OPTION) {
					for (Iterator iter = removeEntities.iterator(); iter.hasNext();) {
						OWLEntity entity = (OWLEntity) iter.next();
						removeEntity(swoopModel.getSelectedOntology(), entity, false);
					}
				}
			}
		}
		
		if (e.getSource()==renameTermBtn) {	
			try {
				OWLEntity entity = swoopModel.getSelectedEntity();
				String name = swoopModel.shortForm(entity.getURI());
				if (name.indexOf(":")>=0) name = name.substring(name.indexOf(":")+1, name.length());
				String newTerm = JOptionPane.showInputDialog(
	                    null,
	                    new Object[] {	          
	                    		"(Note: All references of the entity will be renamed)",	
							"Current Name (ID): " + name,
							"Enter New Name (ID):"							
	                    },
	                    "Rename OWL Entity",
	                    JOptionPane.PLAIN_MESSAGE
	            );
				
				if (newTerm!=null) {
					newTerm = newTerm.replaceAll(" ", "_");
					// check if name already used in ontology
					OWLOntology ont = swoopModel.getSelectedOntology();
					URI newURI = new URI(ont.getURI().toString()+"#"+newTerm);
					if (swoopModel.getEntity(ont, newURI, true)!=null) {
						JOptionPane.showMessageDialog(this, "URI already in use for another OWL Entity", "Rename Error" , JOptionPane.ERROR_MESSAGE);
						return;
					}
					else swoopModel.renameOWLEntity(swoopModel.getSelectedOntology(), entity, newTerm);					
				}
			}
			catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		
		// if user checks on search all ontologies
		if (e.getSource()==searchAllChk) {
			swoopModel.setLookupAllOnts(searchAllChk.isSelected());
		}
		
		// if user checks on enable debugging
		if (e.getSource()==debugChk) {
			
			if (debugChk.isSelected()) {
			    int answer = JOptionPane.YES_OPTION;
			    JPanel debugPanel = new JPanel();
			    debugPanel.setLayout(new GridLayout(2,1));
			    JLabel debugLbl = new JLabel("Debugging may slow down browsing considerably. Are you sure you want to continue?");
			    debugPanel.add(debugLbl);
			    JCheckBox switchPellet = new JCheckBox("Also switch to Pellet Reasoner?");
			    switchPellet.setSelected(true);
			    if (!(swoopModel.getReasoner() instanceof PelletReasoner)) debugPanel.add(switchPellet);
			    
			    // confirm the user before enabling debugging
			    answer = JOptionPane.showConfirmDialog(swoopHandler, 
			        debugPanel);
			    
			    if (answer == JOptionPane.YES_OPTION) {		    
					// call swoopmodel method to enable debugging (also turns on icons)
					swoopModel.setEnableDebugging(true);
					// also switch to pellet if it isn't on already
					if (switchPellet.isSelected()) {
						int pellet = 0;
						for (int i=0; i<reasoners.size(); i++) {
							if (reasoners.get(i) instanceof PelletReasoner) {
								pellet = i;
								break;
							}
						}
						if (reasonerCombo.getSelectedIndex()!=pellet) {
							reasonerCombo.setSelectedIndex(pellet);					
						}				
					}
			    }
			    else
			        debugChk.setSelected(false);
			}
			else {
				// user turns off debugging
				swoopModel.setEnableDebugging(false);
				if (swoopModel.getReasoner() instanceof PelletReasoner) {
					int answer = JOptionPane.showConfirmDialog(swoopHandler, 
					        "Switch off Pellet Reasoner as well?");
					    
				    if (answer == JOptionPane.YES_OPTION) {
				    	reasonerCombo.setSelectedIndex(0);
				    }
				}					    
			}
		}
		
		// is user checks on glass box debugging
		if (e.getSource() == debugGlassChk) {
			swoopModel.setDebugGlass(debugGlassChk.isSelected());
//			swoopHandler.sosMenu.setEnabled(debugGlassChk.isSelected());
		}
		
		// is user checks on glass box debugging
		if (e.getSource() == debugBlackChk) {
			swoopModel.setDebugBlack(debugBlackChk.isSelected());
			swoopModel.setEnableDebugging(debugBlackChk.isSelected());			
		}
		
		// if user checks on termEditable
		if (e.getSource()==termEditableChk) {	
			swoopModel.setEditorEnabled(termEditableChk.isSelected());
		}
		
		// if user checks on showInherited
		if (e.getSource()==showInheritedChk) {	
			swoopModel.setShowInherited(showInheritedChk.isSelected());			
		}
		
		if(e.getSource()==applyChangesBtn) {
			
			// remove all discrete changes in swoopModel first
			if(!swoopModel.getUncommittedChanges().isEmpty()) {			
				swoopModel.applyOntologyChanges();				
			}
			
			// if editor is not concise format
			// then its direct text editing
			// and needs to be handled separately
			if (!(renderers.get(termDisplayPane.getSelectedIndex()) instanceof ConciseFormatEntityRenderer)) {
				applyDirectTextChanges();				
			}
			
			undoChangesBtn.setEnabled(false);
			applyChangesBtn.setEnabled(false);			
		}
		
		if(e.getSource()==undoChangesBtn) {
			if(!swoopModel.getUncommittedChanges().isEmpty()) {
				String title = "Undo changes";
				String msg = "Do you want to undo all the changes?";
				int options = JOptionPane.YES_NO_OPTION;
				int result = JOptionPane.showConfirmDialog(this, msg, title, options);
				if(result==JOptionPane.YES_OPTION) {
					
					// remove each uncommitted change from swoop change cache
					for (Iterator iter = swoopModel.getUncommittedChanges().iterator(); iter.hasNext();) {
						OntologyChange removeChange = (OntologyChange) iter.next();
						swoopModel.getChangesCache().removeOntologyChange(removeChange, swoopModel);
					}
					
					swoopModel.setUncommittedChanges(new ArrayList());
					undoChangesBtn.setEnabled(false);
					applyChangesBtn.setEnabled(false);
					
					ConciseFormatEntityRenderer cfRend = (ConciseFormatEntityRenderer) renderers.get(termDisplayPane.getSelectedIndex());
			
					JEditorPane cfEdit = (JEditorPane) editors.get(termDisplayPane.getSelectedIndex());
					StringWriter st = new StringWriter();
					try {
						cfRend.render(swoopModel.selectedEntity, swoopModel, st);
					} catch (RendererException e1) {
						e1.printStackTrace();
					}
					cfEdit.setText(st.toString());
					cfEdit.setCaretPosition(0);					
				}
			}
		}
		
		// filter combo changed
		if (e.getSource()==filterCombo) {
			// set filter param in SwoopModel here but DONT Notify or else infinite loop!
			if (filterCombo.getSelectedItem()!=null) {
				swoopModel.setTermListFilter(filterCombo.getSelectedItem().toString(), false);
				updateListDisplay();
			}			
		}
		
		// reasoner selection chenged
		if (e.getSource()==reasonerCombo) {
			reasonerSelChanged();							
		}
		
		// when user clicks show imports button
		if (e.getSource()==showImportChk) {
			try {
				swoopModel.setShowImports(showImportChk.isSelected());
			} catch (OWLException ex) {
				JOptionPane.showMessageDialog(null, "The selected reasoner cannot process this ontology with the new setting:\n" + ex.getMessage() + "\n\nThe reasoner will be disabled.", "Error!", JOptionPane.ERROR_MESSAGE);
				//TODO: need to switch to default reasoner??
			}	
		}
		
		// when user clicks show qnames button
		if (e.getSource()==showQNamesChk)			
			swoopModel.setShowQNames(showQNamesChk.isSelected());	
		
	}

	public void removeFromCache(OWLOntology ont) {		
		treeRenderer.removeClassTreeCacheEntry(ont);
		treeRenderer.removePropTreeCacheEntry(ont);
		listCache.removeOntology(ont);		
	}
	
	public void reasonerSelChanged() {
		
		if (swoopModel.getSelectedOntology()!=null) {
		    try {
				swoopModel.setReasoner((SwoopReasoner) reasoners.get(reasonerCombo.getSelectedIndex()));
			} 
		    catch (Exception ex) {
				// the first element is the SwoopSimpleReasoner					
				reasonerCombo.setSelectedIndex(0);		
				throw new RuntimeException(ex);
			}					
		}
	}
	
	public void refreshEditorMode() {
		
		swoopHandler.ontDisplay.ontEditableChk.setSelected(termEditableChk.isSelected());
		
		// toggle apply/undo changes button visibility
		termEditButtonPane.setVisible(termEditableChk.isSelected());
		
		// remember currently selected entity renderer tab
		int index = termDisplayPane.getSelectedIndex();
		
		termDisplayPane.removeAll(); // clear UI tab pane
		editors = new ArrayList(); // clear editors array
		
		// enable editable renderers alone
		for (int i=0; i<renderers.size(); i++) {
			SwoopRenderer rend = (SwoopRenderer) renderers.get(i);
			
			if (renderers.get(i) instanceof SwoopEditableRenderer) {										
				SwoopEditableRenderer editRend = (SwoopEditableRenderer) renderers.get(i);
				editRend.setEditorEnabled(termEditableChk.isSelected());
				// add editor enabled component now
				editors.add(rend.getDisplayComponent(this)); 
				termDisplayPane.add(rend.getName(), new JScrollPane((Component) editors.get(i)));
			}
			else {
				// not an editable renderer component (tab)
				if (i==index) index = -1;
				editors.add(rend.getDisplayComponent(this)); 
				termDisplayPane.add(rend.getName(), new JScrollPane((Component) editors.get(i)));
				termDisplayPane.setEnabledAt(i, !termEditableChk.isSelected());
			}			
		}
		
		// if selectedTab is -1, make it select ConciseFormat by default
		if (index==-1) index = 0;
		// select tab again
		termDisplayPane.setSelectedIndex(index);
		
		undoChangesBtn.setEnabled(swoopModel.getUncommittedChanges().size() > 0);
		applyChangesBtn.setEnabled(swoopModel.getUncommittedChanges().size() > 0);
		
		// re-render
		try {
			displayTerm();
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
	}


	/**
	 * Show References (i.e. entity usage) of an OWLEntity in an Ontology
	 * TODO: Needs to be extended to arbitrary class expressions (next version)
	 * @param ontology
	 * @param entity
	 */
	private void showReferencesTerm(OWLOntology ontology, OWLEntity entity) {
		
		try {
			Set references = OntologyHelper.entityUsage(ontology, entity);
			Set claSet = new HashSet();
			Set propSet = new HashSet();
			Set indSet = new HashSet();
			for (Iterator iter = references.iterator(); iter.hasNext(); ) {
				Object obj = iter.next();
				if (obj instanceof OWLClass) claSet.add(obj);
				else if (obj instanceof OWLProperty) propSet.add(obj);
				else if (obj instanceof OWLIndividual) indSet.add(obj);
			}
			
			// display referencePanel JFrame
			String scope = swoopModel.shortForm(ontology.getURI());
			referencePanel.printResults(ontology, claSet, propSet, indSet, swoopModel.shortForm(entity.getURI()), scope);
		} 
		catch (OWLException e) {
			e.printStackTrace();
		}		
	}
	
	/**
	 * Perform a lookup for a particular keyword in the an ontology. Returns any OWL named entity
	 * matches in the ontology(s) i.e. classes/properties/individuals 
	 * Currently just does a String matching operation against the URI
	 * and not the QName
	 *
	 */
	private void lookupTerm() {
		
		try {
			String lookup = lookupFld.getText();
			if (lookup.trim().equals("")) return;
			
			// get ontology(s) to search in
			if (swoopModel.selectedOntology==null) return;
			OWLOntology currOnt = swoopModel.selectedOntology;
			List searchOnts = new ArrayList();
			searchOnts.add(currOnt);
			
			// also add transitive closure of its imports
			Set ontologies = OntologyHelper.importClosure(currOnt);
			for (Iterator iter = ontologies.iterator(); iter.hasNext();) {
				OWLOntology ont = (OWLOntology) iter.next();
				if (!ont.getURI().equals(currOnt.getURI())) {
					searchOnts.add(ont);
				}
			}
			
			// also check option
			if (searchAllChk.isSelected()) {
				// search across all ontologies
				for (Iterator iter = swoopModel.getOntologies().iterator(); iter.hasNext();) {
					OWLOntology ont = (OWLOntology) iter.next();
					if (!searchOnts.contains(ont)) {
						searchOnts.add(ont);
					}
				}
			}
			
			Set classSet = new TreeSet(EntityComparator.INSTANCE);
			Set propSet = new TreeSet(EntityComparator.INSTANCE);
			Set instSet = new TreeSet(EntityComparator.INSTANCE);
			for (Iterator ontIter = searchOnts.iterator(); ontIter.hasNext();) {
				
				OWLOntology ont = (OWLOntology) ontIter.next();
				
				// find matching classes
				Set claSet = new HashSet();
				if (lookupPanel.searchImports) claSet.addAll(swoopModel.getEntitySet(ont, SwoopModel.BASE_ONT, SwoopModel.CLASSES));			
				Iterator iter = claSet.iterator();
				while (iter.hasNext()) {
					Object claObj = iter.next();
					if (claObj instanceof OWLClass) {
						if (matchEntity((OWLClass) claObj, lookup)) classSet.add(claObj);
					}
				}
				
				// find matching properties
				Set props = new HashSet();
				if (lookupPanel.searchImports) {
					props.addAll(swoopModel.getEntitySet(ont, SwoopModel.BASE_ONT, SwoopModel.PROPERTIES));				
				}
				iter = props.iterator();
				while (iter.hasNext()) {
					OWLProperty prop = (OWLProperty) iter.next();
					if (matchEntity(prop, lookup)) propSet.add(prop);				
				}
				
				// find matching individuals
				Set inds = new HashSet();
				if (lookupPanel.searchImports) inds.addAll(swoopModel.getEntitySet(ont, SwoopModel.BASE_ONT, SwoopModel.INDIVIDUALS));
				iter = inds.iterator();
				while (iter.hasNext()) {
					OWLIndividual ind = (OWLIndividual) iter.next();
					if (matchEntity(ind, lookup)) instSet.add(ind);				
				}	
			}
			
			// display lookupPanel JFrame
			String scope = "<font color=\"blue\">Ontology: <a href=\""+currOnt.getURI()+"\">"+swoopModel.shortForm(currOnt.getURI())+ "</a> OR in its Imports Closure</font>";
			if (searchAllChk.isSelected()) scope += " <font color=\"green\"><br>OR in External ontologies</font>";
			lookupPanel.printResults(swoopModel.selectedOntology, classSet, propSet, instSet, lookup, scope);
		
		} 
		catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	/**
	 * Used by lookup(..) to match entities (string-matching)
	 * @param entity
	 * @param lookup
	 * @return
	 */
	private boolean matchEntity(OWLEntity entity, String lookup) {
		
		try {
			if (entity == null) return false;
			URI entityURI = entity.getURI();
			String entityURIstr = entityURI.toString();
			String entityName = "";
			if (entityURIstr.indexOf("#")>=0) entityName = entityURIstr.substring(entityURIstr.indexOf("#")+1, entityURIstr.length());
			else entityName = entityURIstr.substring(entityURIstr.lastIndexOf("/")+1, entityURIstr.length());
			
			if (lookup.startsWith("\"") && lookup.endsWith("\"")) {
				// exact match
				lookup = lookup.substring(1, lookup.length()-1);
				if (entityName.equals(lookup)) return true;
			}
			else {
				if (entityName.toLowerCase().indexOf(lookup.toLowerCase())>=0) return true;
			}			
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}
	
	public void keyPressed(KeyEvent e) {
		
		if (e.getSource()==termList) {
			try {
				String alpha = Character.toString(e.getKeyChar()).toLowerCase();
				ListModel model = termList.getModel();
				for (int i=0; i<model.getSize(); i++) {
					if (model.getElementAt(i) instanceof OWLEntity) {
						OWLEntity entity = (OWLEntity) model.getElementAt(i);
						URI entityURI = entity.getURI();
						String sf = swoopModel.shortForm(entityURI);
						if (sf.indexOf(":")>=0) {
							sf = sf.substring(sf.indexOf(":")+1, sf.length());					
						}
						if (sf.toLowerCase().startsWith(alpha)) {
							termList.setSelectedValue(entity, true);
							break;
						}
					}
				}
			}
			catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}

	public void keyReleased(KeyEvent arg0) {
		
	}

	public void keyTyped(KeyEvent arg0) {
		// changed by vlatko
		applyChangesBtn.setEnabled(true);
	}

	public void mouseClicked(MouseEvent e) {
		
		// if right clicked over entity renderer pane, popup menu
		if (e.getSource() instanceof JEditorPane) {
			try {
				rightClicked = false;
				popupMenu.setVisible(false);
				
				// if right mouse button is clicked over renderer pane..
				// popup menu
				if (SwingUtilities.isRightMouseButton(e)) {
					rightClicked = true;
					
					if (swoopModel.getSelectedEntity()==null) return;
					Point pt = e.getPoint();
					SwingUtilities.convertPointToScreen(pt, this);
					int x = (int) pt.getX();
					int y = (int) pt.getY();
					
					// for popup menu, determine if link is webpage or e-mail id
					if (isEMailID(urlClicked)) {
						openBrowserMenu.setText("Open in Mail Client");
						if (!urlClicked.toLowerCase().startsWith("mailto:")) urlClicked = "mailto:"+urlClicked;
					}
					else openBrowserMenu.setText("Open in Web Browser");
					
					popupMenu.setLocation(x+popupMenu.getWidth(), y+(popupMenu.getHeight()/2));
					popupMenu.setVisible(true);
									
				}
				else urlClicked = "";
			}
			catch (Exception ex) {
				ex.printStackTrace();
			}	
		}
		
		// if class/property tree clicked, show term display pane in swoop frame 
		if (e.getSource() instanceof JTree) {
			swoopHandler.displayEntityPane();
		}
	}

	public void mouseEntered(MouseEvent arg0) {
		
	}

	public void mouseExited(MouseEvent arg0) {
		
	}

	public void mousePressed(MouseEvent arg0) {
		
	}

	public void mouseReleased(MouseEvent arg0) {
		
	}

	public void stateChanged(ChangeEvent event) {
		
		if (event.getSource()==termEditableChk) {
			
			refreshEditorMode();
		}
		
		if(event.getSource() == termTabPane) {
			
			//if (event.toString().indexOf("invalid")>=0) return;
			
			try {
				boolean match = true;
				int index = termTabPane.getSelectedIndex();
				switch (index) {
					case 0: // class tree pane selected
					case 1: // property tree pane selected
						updateTreeDisplay(); // display tree
						if (swoopModel.getSelectedEntity()!=null) {
							if ((swoopModel.getSelectedEntity() instanceof OWLClass && index==0) || (swoopModel.getSelectedEntity() instanceof OWLProperty && index==1)) {
								// select tree node but disable listener to prevent displayTerm from being called twice
								trees[index].removeTreeSelectionListener(this);
								match = selectTreeNode(trees[index], swoopModel.getSelectedEntity().getURI(), false);
								trees[index].addTreeSelectionListener(this); 
							}
							else {
								if (trees[index].getSelectionPath()!=null) {
									DefaultMutableTreeNode node = (DefaultMutableTreeNode) trees[index].getSelectionPath().getLastPathComponent();
									Set entitySet = (Set) node.getUserObject();
									OWLEntity entity = (OWLEntity) entitySet.iterator().next();
									swoopModel.setSelectedEntity(entity);
								}
							}
						}						
						break;
					
					case 2: // alphabetical list selected
						updateListDisplay();
						if (swoopModel.getSelectedEntity()!=null) {
							termList.removeListSelectionListener(this);
							termList.setSelectedValue(swoopModel.getSelectedEntity(), true);
							termList.addListSelectionListener(this);
						}
						break;
				}
			}
			catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		
		if(event.getSource() == termDisplayPane) {
			displayTerm();
		}

	}

	/**
	 * Whenever list selection changes, selected value is set as 
	 * swoopModel.setSelectedEntity. Changing it in swoopModel causes 
	 * notification of appropriate event ENTITY_SEL_CHANGED and hence 
	 * rendering of term using displayTerm via modelChanged
	 * Note: addHistory is handled here (along with TreeSelectionListener)
	 */
	public void valueChanged(ListSelectionEvent e) {
		
		if (e.getSource()==termList) {
			try {
				if (termList.getSelectedIndex()!=-1) {
					if (termList.getSelectedValue() instanceof OWLEntity) {
						OWLEntity entity = (OWLEntity) termList.getSelectedValue();
						swoopModel.setSelectedEntity(entity);
					}
					// else if GCI, enable remTermBtn
					else if (termList.getSelectedValue() instanceof OWLSubClassAxiom) {
						remTermBtn.setEnabled(true);
						renameTermBtn.setEnabled(false);
					}
				}
			}
			catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}

	public void hyperlinkUpdate(HyperlinkEvent event) {		
		String hLink = event.getDescription();
		
		Object source = event.getSource();
		
		urlClicked = hLink;
		// expand abbreviated URI's with respect to the base
		if (hLink.startsWith("#")) {
			try {
				hLink = swoopModel.getSelectedOntology().getURI()+hLink;
			} catch (OWLException e) {
				e.printStackTrace();
			}
		}
		else
			if (hLink.equals("PELLET-SATURATION") && event.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
				// saturate tableau
				OWLClass cla = (OWLClass) swoopModel.getSelectedEntity();
				PelletOptions.SATURATE_TABLEAU = true;
				PelletReasoner pellet = new PelletReasoner();
				try {
					pellet.setOntology(swoopModel.selectedOntology, false);
					Timer timers = new Timer("Saturation Time");
					timers.start();
					pellet.isConsistent(cla);
					timers.stop();
					System.out.println(timers);
				}
				catch (Exception ex) {
					ex.printStackTrace();
				}
				finally {
					PelletOptions.SATURATE_TABLEAU = false;
				}
				return;
			}
		
		if ( source instanceof JEditorPane )
		{
			JEditorPane sourceRend = (JEditorPane) event.getSource();
			/*if (isURI(hLink)) sourceRend.setToolTipText(hLink);
			else if (hLink.startsWith("<CE")) sourceRend.setToolTipText("Class Expression");
			else sourceRend.setToolTipText("");*/
		}
		
		if (event.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {			
			
			if (rightClicked) {
				rightClicked = false;
				return;
			}
			
			if (isURI(hLink)) 
			{
				try {
					URI uri = new URI(hLink);
					// Seems unnecessary:
					String uriName = uri.toString();
					if ( uriName.startsWith(XMLSchemaSimpleDatatypeVocabulary.XS ))
					{ //dealing with xsd datatypes here (handles it differently from normal links)
						OWLEntity entity = swoopModel.getSelectedOntology().getOWLDataFactory().getOWLClass( uri );
						swoopModel.setSelectedEntity( entity );
						return;					
					}
					else if ( uriName.startsWith( OWLVocabularyAdapter.OWL ) && !swoopModel.isViewOWLVocabularyAsRDF()
								&& !uriName.equals( OWLVocabularyAdapter.OWL ))
					{ //dealing with OWL vocabulary (handles it differently from normal links)
						OWLEntity entity = swoopModel.getSelectedOntology().getOWLDataFactory().getOWLClass( uri );
						swoopModel.setSelectedEntity( entity );
						return;					
					}
					else if ( uriName.startsWith( RDFSVocabularyAdapter.RDFS ) && !swoopModel.isViewRDFVocabularyAsRDF()
							 &&	!uriName.equals( RDFSVocabularyAdapter.RDFS) )
					{ //dealing with OWL vocabulary (handles it differently from normal links)
						OWLEntity entity = swoopModel.getSelectedOntology().getOWLDataFactory().getOWLClass( uri );
						swoopModel.setSelectedEntity( entity );
						return;					
					}
					else if ( uriName.startsWith( RDFVocabularyAdapter.RDF ) && !swoopModel.isViewRDFVocabularyAsRDF()
								&& !uriName.equals( RDFVocabularyAdapter.RDF ) )
					{ //dealing with OWL vocabulary (handles it differently from normal links)
						OWLEntity entity = swoopModel.getSelectedOntology().getOWLDataFactory().getOWLClass( uri );
						swoopModel.setSelectedEntity( entity );
						return;					
					}
					
					
					if (swoopModel.getOntologyURIs().contains(uri)) {
						// select ontology in main swoop UI
						swoopHandler.ontDisplay.selectOntology(swoopModel.getOntology(uri));						
					}
					else {
						SwoopLoader loader = new SwoopLoader(swoopHandler, swoopModel);
						loader.selectEntity(hLink);
					}
				}
				catch (Exception e) {
					e.printStackTrace();
				}
			}			
			else		{
				if (hLink.equals("<SwitchProp")) {
					if (swoopModel.getSelectedEntity()!=null && swoopModel.getSelectedEntity() instanceof OWLProperty) {
						int answer = JOptionPane.showConfirmDialog(
		                 	swoopHandler,      
		                 	"Switching type of the Property. Continue?",
					        "Switch (Object<->Datatype) Property",
		                 	JOptionPane.YES_NO_OPTION);

			             if(answer == JOptionPane.YES_OPTION) {
			             	swoopModel.switchProperty(swoopModel.getSelectedOntology(), (OWLProperty) swoopModel.getSelectedEntity());
			             }
					}
				}				
				else
				if (hLink.startsWith("<CE:")) {
					
					// user has clicked on a class expression link
					ConciseFormatEntityRenderer cfRend = (ConciseFormatEntityRenderer) renderers.get(0);
					String hashCode = hLink.substring(hLink.indexOf(":")+1, hLink.length());					
					// obtain OWL desc from concise format renderer
					OWLDescription claExpr = (OWLDescription) cfRend.OWLDescHash.get(hashCode);
					// add OWL desc to swoopModel classExpr hash
					swoopModel.putCEinHash(claExpr);
					
					// create a new OWL class equivalent to description
					OWLOntology ontology = swoopModel.selectedOntology;					
					try {						
						URI claExprURI = new URI(ontology.getURI().toString()+"#ClassExpression"+hashCode);						
						OWLClass newClass = ontology.getOWLDataFactory().getOWLClass(claExprURI);
//						if (newClass==null) {
//							newClass = ontology.getOWLDataFactory().getOWLClass(claExprURI);
//							AddEntity change = new AddEntity(ontology, newClass, null);
//							change.accept((ChangeVisitor) ontology);
//							AddEquivalentClass change2 = new AddEquivalentClass(ontology, newClass, claExpr, null);
//							change2.accept((ChangeVisitor) ontology);
//							swoopModel.getReasoner().setOntology(ontology);
//						}
						swoopModel.setSelectedEntity(newClass);
						trees[0].clearSelection();
					} 
					catch (Exception e) {
						e.printStackTrace();
					}					
				}
				else
				// Show References (Usage) Link clicked in Swoop Lookup
			    if (hLink.startsWith("<USAGE")) {
			    	String hashCode = hLink.substring(hLink.indexOf(":")+1, hLink.length());
			    	OWLEntity entity = (OWLEntity) lookupPanel.OWLObjectHash.get(hashCode);
			    	if (entity==null) entity = (OWLEntity) referencePanel.OWLObjectHash.get(hashCode);
			    	if (entity!=null) {
				    	OWLOntology ont = null;
						try {
							ont = (OWLOntology) this.checkSwoopModel(entity.getURI());
						} catch (OWLException e) {
							e.printStackTrace();
						}
						if (ont!=null) this.showReferencesTerm(ont, entity);
			    	}
			    }
				else
				// either Add or Delete hyperlink pressed
				if (hLink.startsWith("<Add")) {
					try {
						ConciseFormatEntityRenderer cfRend = (ConciseFormatEntityRenderer) renderers.get(0);
						List changes = cfRend.handleAddLink(hLink);	
						// if (changes!=null) swoopModel.addUncommittedChanges(changes);						
					}
					catch (Exception ex) {
						ex.printStackTrace();
					}
				}
			 else if (hLink.startsWith("<Edit")) {
				try {
					ConciseFormatEntityRenderer cfRend = (ConciseFormatEntityRenderer) renderers
							.get(0);
					List changes = cfRend.handleEditLink(hLink);
					swoopModel.addUncommittedChanges(changes);
					this.changesAltered();
				} catch (Exception ex) {
					ex.printStackTrace();
				}
				
			}  else if (hLink.startsWith("<Publish")) {
				try {
					ConciseFormatEntityRenderer cfRend = (ConciseFormatEntityRenderer) renderers
							.get(0);
					List changes = cfRend.handlePublishLink(hLink);
					// no changes here
					//swoopModel.addUncommittedChanges(changes);
					//this.changesAltered();
				} catch (Exception ex) {
					ex.printStackTrace();
				}
				
			} 
				else if (hLink.startsWith("<Delete")){
					try {
						ConciseFormatEntityRenderer cfRend = (ConciseFormatEntityRenderer) renderers.get(0);
						List changes = cfRend.handleDeleteLink(hLink);
						swoopModel.addUncommittedChanges(changes);
						this.changesAltered();
					}
					catch (Exception ex) {
						ex.printStackTrace();
					}
				}
				else if (hLink.startsWith("<Why")){
					ConciseFormatEntityRenderer cfRend = (ConciseFormatEntityRenderer) renderers.get(0);
					try {
						List sosStr = cfRend.handleWhyLink(hLink);
						// display popup with explanation (axioms)
						PopupExplanation popupExpl = new PopupExplanation(this, sosStr);
						popupExpl.setVisible( true );												
					} 
					catch (Exception e) {
						e.printStackTrace();
					}
				}
				else if (hLink.startsWith("<Undo")){
					try {
						List changes = new ArrayList(swoopModel.getUncommittedChanges());	

						System.out.println("Undo " + hLink);
						// parse Undo hyper-link
						int pos1 = hLink.indexOf(":");
						int pos2 = hLink.indexOf(":", pos1+1);
						int pos3 = hLink.indexOf(":", pos2+1);
						String operation = hLink.substring(pos1+1, pos2);
						int hashCode = Integer.parseInt(hLink.substring(pos2+1, pos3));
						String titleCode = hLink.substring(pos3+1, hLink.length());
						int index = 0;
						while(index < changes.size()) {
							if(changes.get(index).hashCode() == hashCode) 
								break;							
							index++;
						}
						OntologyChange removeChange = (OntologyChange) changes.get(index);
						System.out.println("Undo " + index + " of " + changes.size() + " " + removeChange);
						changes.remove(removeChange);
						swoopModel.getChangesCache().removeOntologyChange(removeChange, swoopModel);
						
						swoopModel.setUncommittedChanges(new ArrayList(changes));
						
						this.changesAltered(); // need to updateTreeDisplay??
					}
					catch (Exception ex) {
						ex.printStackTrace();
					}
				}				
			}
		}
	}
	
	/**
	 * This method is called whenever changes are added or deleted from SwoopModel
	 * It updates the button status, tree display and re-renders term (with changes highlighted accordingly) 
	 */
	protected void changesAltered() {
		// System.out.println(); for (int ch=0; ch<swoopModel.getOntologyChanges().size(); ch++) System.out.println(swoopModel.getOntologyChanges().get(ch).toString());
		undoChangesBtn.setEnabled(swoopModel.getUncommittedChanges().size() > 0);
		applyChangesBtn.setEnabled(swoopModel.getUncommittedChanges().size() > 0);				
		
		updateTreeDisplay(); // to show * next to changed classes
		displayTerm();						
	}
	
	public boolean isURI(String str) {
		try {
			URI uri = new URI(str);			
		}
		catch (Exception ex) {
			return false;
		}
		return true;
	}

	
	public void selectExistingClass(URI uri) {
		
		// disable change listener and select class tree pane
		if (termTabPane.getSelectedIndex()!=0) {
			termTabPane.removeChangeListener(this);
			termTabPane.setSelectedIndex(0);
			termTabPane.addChangeListener(this);
			// now forcibly call updateTreeDisplay 
			updateTreeDisplay();
		}
		
		// now call select tree node after the class tree has been rendered
		boolean match = selectTreeNode(trees[0], uri, false);
		if (!match) {
			// even if tree node cannot be selected, if entity exists render it
			// eg. Class Expressions (CE)
			try {
				OWLClass entity = swoopModel.selectedOntology.getClass(uri);
				swoopModel.setSelectedEntity(entity);
				trees[0].clearSelection();
			} 
			catch (OWLException e) {
				e.printStackTrace();
			}			
		}
	}
	
	public void selectExistingProperty(URI uri) {
		// disable change listener and select property tree pane
		if (termTabPane.getSelectedIndex()!=1) {
			termTabPane.removeChangeListener(this);
			termTabPane.setSelectedIndex(1);
			termTabPane.addChangeListener(this);
			// now forcibly call updateTreeDisplay 
			updateTreeDisplay();
		}
		// now call select tree node after the property tree has been rendered
		boolean match = selectTreeNode(trees[1], uri, false);
		if (!match) {
			// even if tree node cannot be selected, if entity exists render it
			// eg. Annotation Properties
			try {
				OWLEntity entity;
				entity = swoopModel.selectedOntology.getAnnotationProperty(uri);
				if (entity==null) entity = swoopModel.selectedOntology.getDataProperty(uri);
				if (entity==null) entity = swoopModel.selectedOntology.getObjectProperty(uri);
				swoopModel.setSelectedEntity(entity);
				trees[1].clearSelection();
			} 
			catch (OWLException e) {
				e.printStackTrace();
			}			
		}
	}
	
	/**
	 * This method must be called after the entity in which the 
	 * ontology occurs (foundOnt) is selected and updated in Swoop.
	 * The method then selects the class/property/individual (foundEntity) in the ontology accordingly.
	 * i.e. either selects corresponding tree node by calling selectTreeNode()
	 * or, if individual, selects entry in alphabetic list
	 * 
	 * @param ont - current OWL Ontology
	 * @param uri - entity URI
	 * @return
	 * @throws OWLException
	 */
	public void displayFoundEntity(OWLOntology foundOnt, OWLEntity foundEntity) throws OWLException {
		
		if (foundEntity instanceof OWLClass) {
			// if trees are being shown, select tree node
			if (termTabPane.getSelectedIndex()<=1) {
				selectExistingClass(foundEntity.getURI());
			}
			else {
				// select entry in alphabetic list
				termList.clearSelection();
				termList.setSelectedValue((OWLClass) foundEntity, true);
				// if match not found in list (due to filter or otherwise), switch to property tree view
				if (termList.getSelectedIndex()==-1) {
					selectExistingClass(foundEntity.getURI());
				}
			}
		}
		else if (foundEntity instanceof OWLProperty) {			
			// if trees are being shown, select property node in tree			
			if (termTabPane.getSelectedIndex()<=1) {
				selectExistingProperty(foundEntity.getURI());
			}
			else {
				// select entry in alphabetic list
				termList.clearSelection();
				termList.setSelectedValue((OWLProperty) foundEntity, true);
				// if match not found in list (due to filter or otherwise), switch to property tree view
				if (termList.getSelectedIndex()==-1) {
					selectExistingProperty(foundEntity.getURI());
				}
			}
		}
		else if (foundEntity instanceof OWLIndividual) {
			// select entry in alphabetic list
			termTabPane.setSelectedIndex(2);
			termList.clearSelection();
			termList.setSelectedValue((OWLIndividual) foundEntity, true);
			// if match not found in list (due to filter or otherwise), switch to filter individuals
			if (filterCombo.getSelectedIndex() != SwoopModel.ALL && 
			    filterCombo.getSelectedIndex() != SwoopModel.INDIVIDUALS) {
				filterCombo.setSelectedIndex(SwoopModel.INDIVIDUALS);
				updateListDisplay();
				termList.setSelectedValue((OWLIndividual) foundEntity, true);
			}
		}			
	}
	
	/**
	 * Simply check if URI is present in *any* ontology in SwoopModel
	 * Currently checks Classes, Data/Object Properties and Individuals
	 * @param uri - entity URI to be checked
	 * @return The ontology it was found in.
	 */
	public OWLNamedObject checkSwoopModel(URI uri) {
		try {
			Collection ontologySet = swoopHandler.swoopModel.getOntologies();
			Iterator iter = ontologySet.iterator();
			while (iter.hasNext()) {
				OWLOntology ont = (OWLOntology) iter.next();
				if ((ont.getClass(uri)!=null) || (ont.getDataProperty(uri)!=null) || (ont.getObjectProperty(uri)!=null) || (ont.getIndividual(uri)!=null)) 
					return ont;				
			}			
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}
	
	/**
	 * Select Tree Node whose URI (partially or completely) matches the URI argument passed
	 * @param tree - search inside this tree object (class/property tree)
	 * @param searchURI - URI of entity to be selected
	 * @param partial - if true, do a substring match instead of precise
	 * @return
	 * No rendering is done here, instead tree selection listener calls swoopModel.setSelectedEntity which does rendering via listener notification
	 */
	public boolean selectTreeNode(JTree tree, URI searchURI, boolean partial) {
		
		TreePath matchPath = null;
			
		TreeModel tModel = tree.getModel();
		DefaultMutableTreeNode tNode = null;
		
		tNode = ((DefaultMutableTreeNode) tModel.getRoot());
		
		boolean match = false;
		
		try {			
			do {
				Set entitySet = (Set) tNode.getUserObject();
				Iterator iter = entitySet.iterator();
				while (iter.hasNext()) {
					OWLEntity entityNode = (OWLEntity) iter.next();
					URI entityURI = entityNode.getURI();
					String entityURIstr = entityURI.toString();
					String entityName = "";
					if (entityURIstr.indexOf("#")>=0) entityName = entityURIstr.substring(entityURIstr.indexOf("#")+1, entityURIstr.length());
					else entityName = entityURIstr.substring(entityURIstr.lastIndexOf("/")+1, entityURIstr.length()); 
					
					if (((!partial) && (entityURI.equals(searchURI))) ||
							((partial) && (entityName.toLowerCase().startsWith(searchURI.toString().toLowerCase()))))
					{
						match = true;
						matchPath = new TreePath(tNode.getPath());
						break;
					}
				}
				if (!match) {
					tNode = tNode.getNextNode();
				}
				if (tNode==null) break;
			}			
			while (!match);
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}

		
		if (match) {
			tree.clearSelection();
			tree.setExpandsSelectedPaths(true);
			// save entity URI to global var
			// used in TreeSelection to solve equivalence problem
			treeEntityURI = searchURI;
			tree.setSelectionPath(matchPath);
			tree.scrollPathToVisible(matchPath);
		}
		
		return match;
	}

	/**
	 * Whenever tree selection changes, selected node is set as 
	 * swoopModel.setSelectedEntity. Changing it in swoopModel causes 
	 * notification of appropriate event ENTITY_SEL_CHANGED and hence 
	 * rendering of term using displayTerm via modelChanged
	 * Note: addHistory is handled here (along with ListSelectionListener)
	 */
	public void valueChanged(TreeSelectionEvent event) {
		
		JTree tree = (JTree) event.getSource();
		if (tree.getSelectionPath()==null) return;
		DefaultMutableTreeNode selNode = (DefaultMutableTreeNode) tree.getSelectionPath().getLastPathComponent();
		Set set = (Set) selNode.getUserObject();
		Iterator iter = set.iterator();
		OWLEntity matchTreeEntity = null;
		while (iter.hasNext()) {
			OWLEntity entity = (OWLEntity) iter.next();
			try {
				if (entity.getURI().equals(treeEntityURI)) {
					matchTreeEntity = entity;
					break;
				}
			} catch (OWLException e) {
				e.printStackTrace();
			}
		}
		if (matchTreeEntity==null) matchTreeEntity = (OWLEntity) set.iterator().next();
		
		swoopModel.setSelectedEntity(matchTreeEntity);
	}
	
	/**
	 * Renders the current selected entity in the SWOOP Entity Pane
	 * Calls the render method of the appropriate SwoopRenderer 
	 * Does not add entity to History 
	 */
	public void displayTerm() {	
		int index = termDisplayPane.getSelectedIndex();
		if (index==-1) return;
		
		Component comp = (Component) editors.get( index );
		OWLEntity entity = swoopModel.getSelectedEntity();
		
		
		boolean trapped = trapKnownVocabulary( comp, entity );
		if ( trapped ) // if entity is known vocabulary, we skip the rest of the method
		{
			return;
		}

		if ( comp instanceof GraphPanel)
		{
			try
			{
				GraphPanel panel = (GraphPanel)comp;
				URI uri = entity.getURI();

				panel.setMode( GraphPanel.GRAPH_MODE );
				SwoopEntityRenderer renderer = (SwoopEntityRenderer) renderers.get(index);
				renderer.render(entity, swoopModel, null);
			}
			catch (Exception e) 
			{
				JOptionPane.showMessageDialog(null, "An error occured during display:\n" + e.getMessage(), "Error!", JOptionPane.ERROR_MESSAGE);
			}
		}
		else if (comp instanceof JEditorPane)
		{
			JEditorPane editorPane = (JEditorPane)comp;
			try {
				swoopHandler.disableUIListeners();
				// get URI
				URI uri = entity.getURI();				
				if( uri == null && (entity instanceof OWLIndividual))
				    uri = ((OWLIndividual)entity).getAnonId();
				// update address bar 
//				if( uri != null)
//				    swoopHandler.updateAddressBar(uri.toString());
				// below not needed since current ontology is already selected
	//			swoopHandler.ontDisplay.simplySelectOntology(selOnto); 
				
			} catch (OWLException e1) {
				e1.printStackTrace();
			}
			finally {
				swoopHandler.enableUIListeners();
			}
			
			SwoopEntityRenderer renderer = (SwoopEntityRenderer) renderers.get(index);
			StringWriter sw = new StringWriter();
			try {
				// get entity description from renderer
				renderer.render(entity, swoopModel, sw);
				String rendStr = sw.getBuffer().toString();	
		
				// display Warning when editing in RDF/XML tab
				if ((renderer instanceof RDFXMLEntityRenderer || renderer instanceof AbstractSyntaxEntityRenderer) && swoopModel.getEditorEnabled()) 
					warningEditLbl.setText("Warning: Shifting between OWL Entities will cause all changes in code to be lost.");
				else 
					warningEditLbl.setText("");
				
				editorPane.setText(rendStr);
				editorPane.setCaretPosition(0);
				
				// set caret position for RDF/XML editing tab (skip large ns declarations)
							
			} catch (RendererException e) {
				editorPane.setText("");
				JOptionPane.showMessageDialog(null, "An error occured during display:\n" + e.getMessage(), "Error!", JOptionPane.ERROR_MESSAGE);
			}			
		}
		else
		{
			System.err.println("The component returned by the current Renderer is not recognized.  You might want to add code to TermsDisplay.DisplayTerms() (HERE)");
		}
	}
	
	/*
	 * Traps for known vocabulary: owl, rdf, rdfs, xsd 
	 *   If it is trapped (one of the above vocabulary or a null entity), then an explanation page will
	 *   be displayed (if the preference is set as such).
	 * 
	 * If a particular renderer's display component is not of type JEditorPane, then code will need to 
	 *   be added here to handle those cases correctly.
	 * 
	 */
	private boolean trapKnownVocabulary( Component comp, OWLEntity entity )
	{
		if (comp instanceof JEditorPane)
		{
			JEditorPane editorPane = (JEditorPane) comp;
			if(entity == null) {
				editorPane.setText("");
				return true;
			}
			
			try
			{
				URI uri = entity.getURI();
				
				if( uri == null )
				    return false;
				
				// XSD Datatype
				if ( uri.toString().startsWith( XMLSchemaSimpleDatatypeVocabulary.XS ) )
				{
					editorPane.setText( DatatypeExplanationHTMLSerializer.getSerialization(swoopModel, 
											myXSDDatatypeExplanations.explain(uri)) );
					// update address bar
					return true;
				} 
				// OWL vocabulary
				else if (uri.toString().startsWith(OWLVocabularyAdapter.OWL) && !swoopModel.isViewOWLVocabularyAsRDF()
							&& !uri.toString().equals(OWLVocabularyAdapter.OWL))
				{
					System.out.println( uri );
					editorPane.setText( VocabularyExplanationHTMLSerializer.getSerialization(swoopModel, 
							myOWLVocabExplanations.explain(uri), uri ) );
					return true;
				}
				// RDFS vocabulary
				else if (uri.toString().startsWith(RDFSVocabularyAdapter.RDFS) && !swoopModel.isViewRDFVocabularyAsRDF() 
							&& !uri.toString().equals(RDFSVocabularyAdapter.RDFS))
				{
					editorPane.setText( VocabularyExplanationHTMLSerializer.getSerialization(swoopModel, 
							myRDFSVocabExplanations.explain(uri), uri ) );
					return true;
				}
				// RDF vocabulary
				else if (uri.toString().startsWith(RDFVocabularyAdapter.RDF) && !swoopModel.isViewRDFVocabularyAsRDF()
							&& !uri.toString().equals(RDFVocabularyAdapter.RDF))
				{
					editorPane.setText( VocabularyExplanationHTMLSerializer.getSerialization(swoopModel, 
							myRDFVocabExplanations.explain(uri), uri ) );
					return true;
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		else
		{
			System.err.println(" Code is needed in TermsDisplay.trapKnownVocabulary(...) to handle this particular display component: " + comp);
		}
		return false;
	}
	
	
	/**
	 * Updates the alphabetical list (of entities in the ontology)
	 *
	 */
	public void updateListDisplay() {
		try {
			// update alphabetical list
			termList.removeListSelectionListener(this);
			termList.removeAll();
			if (swoopModel.getSelectedOntology()==null) return;
			
			OWLOntology currOnt = swoopModel.getSelectedOntology();
			
			// check if term list is in cache
			Vector entitySet = null;
			
			//***************************************************
			//Changed for Econnections
			//*****************************************************
			int ENTITY_TYPE = 0;		
			if(filterCombo.getSelectedIndex()<4){
				ENTITY_TYPE = filterCombo.getSelectedIndex();
			}
			else if(filterCombo.getSelectedIndex()==4){
				ENTITY_TYPE = SwoopModel.GCI; // 12
			}
			else if(filterCombo.getSelectedIndex()==5){
				ENTITY_TYPE = SwoopModel.FOREIGN_ENT; // 11
			}
			if (listCache.getList(currOnt, ENTITY_TYPE, swoopModel.getReasoner())!=null) {
				entitySet = new Vector(listCache.getList(currOnt, ENTITY_TYPE, swoopModel.getReasoner()));
				
				System.out.println("loading alphabetic list from list cache "+ENTITY_TYPE);
			}
			else {
				//***************************************************************
				//Changed for Econnections
				//***************************************************************
				
				entitySet = new Vector();
				if (showImportChk.isSelected()){
					entitySet.addAll(swoopModel.getEntitySet(currOnt, SwoopModel.TRANSCLOSE_ONT, ENTITY_TYPE));					
				}
				else{ 
					if(ENTITY_TYPE!=SwoopModel.FOREIGN_ENT){
					    entitySet.addAll(swoopModel.getEntitySet(currOnt, SwoopModel.BASE_ONT, ENTITY_TYPE));}
					else{
						entitySet.addAll(swoopModel.getEntitySet(currOnt, SwoopModel.BASE_ONT, SwoopModel.FOREIGN_ENT));
					}
				}
				
				// put list in cache		
				listCache.putList(currOnt, ENTITY_TYPE, swoopModel.getReasoner(), new Vector(entitySet));
			    
			}
			
			Collections.sort(entitySet, EntityComparator.INSTANCE);
			
			//			 rules stuff
			if (!swoopModel.getRuleExpr().getRuleMap().isEmpty()) {
				
				// find instances of variables,and filter them
				URI swrlVariableURI = new URI(
				"http://www.w3.org/2003/11/swrl#Variable");
				URI swrlClassPredURI = new URI(
				"http://www.w3.org/2003/11/swrl#classPredicate");
				URI swrlPropertyPredURI = new URI(
				"http://www.w3.org/2003/11/swrl#propertyPredicate");
				// set of instances of variables to be removed
				Set setVariables = swoopModel.getReasoner().allInstancesOf(
						swoopModel.getSelectedOntology().getClass(swrlVariableURI));
				entitySet.removeAll(setVariables);
				Set setPredicates = new HashSet();
				// filter classPredicates
				OWLObjectProperty prop1 = (OWLObjectProperty) swoopModel
				.getSelectedOntology().getObjectProperty(swrlClassPredURI);
				if (!(prop1 == null)) {
					Set objects1 = prop1.getUsage(swoopModel.getSelectedOntology());
					for (Iterator it = objects1.iterator(); it.hasNext();) {
						OWLEntity entity = (OWLEntity) it.next();
						if (entity instanceof OWLIndividual) {
							Map objects2 = ((OWLIndividual) entity)
							.getObjectPropertyValues(swoopModel
									.getSelectedOntology());
							for (Iterator it1 = ((Set) objects2.get(prop1))
									.iterator(); it1.hasNext();) {
								setPredicates.add(it1.next());
							}
						}
					}
				}
				// filter property predicate
				prop1 = (OWLObjectProperty) swoopModel.getSelectedOntology()
				.getObjectProperty(swrlPropertyPredURI);
				if (!(prop1 == null)) {
					Set objects1 = prop1.getUsage(swoopModel.getSelectedOntology());
					for (Iterator it = objects1.iterator(); it.hasNext();) {
						OWLEntity entity = (OWLEntity) it.next();
						if (entity instanceof OWLIndividual) {
							Map objects2 = ((OWLIndividual) entity)
							.getObjectPropertyValues(swoopModel
									.getSelectedOntology());
							for (Iterator it1 = ((Set) objects2.get(prop1))
									.iterator(); it1.hasNext();) {
								setPredicates.add(it1.next());
							}
						}
					}
				}
				entitySet.removeAll(setPredicates);
				// filter swrl classes and properties
				for (Iterator it = entitySet.iterator(); it.hasNext();) {
					OWLEntity entity = (OWLEntity) it.next();
					if (entity.getURI() != null) {
						if (entity.getURI().getScheme().equals("http")) {
							if (entity.getURI().getPath().equals("/2003/11/swrl")) {
								it.remove();
							}
						}
					}
				}
			}
			
			/*
			 * end Rules filtering code
			 * 
			 */
			termList.setListData(entitySet);
			termList.setFont(swoopModel.getFont());
			termList.addListSelectionListener(this);
			// Evren: it works without updating UI. I suspect updateUI function
			// causes thos weird exceptions
			//termList.updateUI();			
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	/*
	 * Convenience method to safely add only ONE listener of each type
	 * to a Jtree argument passed to it
	 */
	public void addTreeListeners(JTree tree) {
		if (tree!=null) {
			for (int safe=0; safe<3; safe++) {
				tree.removeTreeSelectionListener(this);
				tree.removeMouseListener(this);
			}
			tree.addTreeSelectionListener(this);
			tree.addMouseListener(this);
		}
	}
	
	/**
	 * Updates the tree display - uses the TreeRenderer to get Class
	 * or Property Tree depending on tabpane selection
	 */
	public void updateTreeDisplay() {
		
		if (termTabPane.getSelectedIndex()>1) return;
		
		int selectedTree = termTabPane.getSelectedIndex();
		
		if (swoopModel.getSelectedOntology()==null) {
			termTabPane.setComponentAt(selectedTree, new JScrollPane());
			return;
		}
		
		try {
			treeRenderer.setSwoopModel(swoopModel);			
			treeRenderer.setShortFormProvider(swoopHandler.swoopModel);
			trees[selectedTree] = selectedTree == 0 ? treeRenderer.getClassTree(trees[0]) : treeRenderer.getPropertyTree(trees[1]);			
			//trees[selectedTree].updateUI(); // dont put this on, causes lots of UI issues
			this.addTreeListeners(trees[selectedTree]);
			
			// termTabPane refresh error corrected by repainting both tabs			
			termTabPane.setComponentAt(0, new JScrollPane(trees[0]));
			termTabPane.setComponentAt(1, new JScrollPane(trees[1]));
			
			// set current class/prop ontology (useful for stats)
			if (selectedTree==0) this.classTreeOfOntology = swoopModel.selectedOntology;
			else this.propTreeOfOntology = swoopModel.selectedOntology;
			swoopModel.removeOntStats(swoopModel.selectedOntology);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}	
	
	public void exportCurrentTree( File file, String format )
	{
		int selectedTree = termTabPane.getSelectedIndex();
		treeRenderer.export( trees[selectedTree ], format , file, swoopModel );
	}
	
	public JTree getCurrentTree()
	{ return trees[ termTabPane.getSelectedIndex() ]; }
	
	public void modelChanged(ModelChangeEvent event)  {
		
		if (event.getType() == ModelChangeEvent.REASONER_FAIL) {
			// make selection in reasonerCombo
			// based on swoopModel.reasoner
			SwoopReasoner reas = swoopModel.getReasoner();
			for (int i=0; i<reasonerCombo.getItemCount(); i++) {
				if (reasonerCombo.getItemAt(i).toString().equals(reas.getName())) {
					reasonerCombo.setSelectedIndex(i);
					break;
				}
			}
		}
		else
		if (event.getType()==ModelChangeEvent.FILTER_SEL_CHANGED){
			// change in termList Filter Selection
			filterCombo.setSelectedItem(swoopModel.getTermListFilter());
		}
		else
		if (event.getType()==ModelChangeEvent.SHOW_INHERITED){
			this.displayTerm();
		}
		else if (event.getType()==ModelChangeEvent.ADDED_CHANGE || event.getType()==ModelChangeEvent.RESET_CHANGE) {
			this.changesAltered();
		}
		else if ((event.getType()==ModelChangeEvent.ADDED_ENTITY) || (event.getType()==ModelChangeEvent.REMOVED_ENTITY)) {
			
			try {
				OWLOntology ont = swoopModel.getSelectedOntology();
				this.removeFromCache(ont);
				
				// remove last entry from history (as it was just removed)
//				if (event.getType()==ModelChangeEvent.REMOVED_ENTITY) {
//					this.historyEntity[historyCtr] = null;
//					this.historyCtr--;
//				}
				
				treeRenderer.useOldClassTreeReferenceforExpansion = true;
				treeRenderer.useOldPropertyTreeReferenceforExpansion = true;
				updateTreeDisplay();
				updateListDisplay();
				//displayTerm();
				
				if (swoopModel.getSelectedEntity()!=null) {
					if (!(swoopModel.getSelectedEntity() instanceof OWLIndividual)) {
						selectTreeNode(trees[termTabPane.getSelectedIndex()], swoopModel.getSelectedEntity().getURI(), false);
					}
					else {
						termList.setSelectedValue(swoopModel.getSelectedEntity(), true);
					}
				}
			}
			catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		else
		if (event.getType() == ModelChangeEvent.EDITABLE_CHANGED) {
			refreshEditorMode();
		}
		else if (event.getType() == ModelChangeEvent.FONT_CHANGED) {
			refreshEditorMode();
			updateTreeDisplay();
			updateListDisplay();
		}
		else
		if (event.getType() == ModelChangeEvent.DEBUGGING_CHANGED) {
			this.debugBlackChk.setSelected(swoopModel.isDebugBlack());
			this.debugGlassChk.setSelected(swoopModel.isDebugGlass());
			refreshEditorMode();
			this.updateTreeDisplay();
			this.updateListDisplay();
		}
		else if (event.getType() == ModelChangeEvent.CLEAR_SELECTIONS) {
			 
			// clear trees and list
			for (int i=0; i<2; i++)
				termTabPane.setComponentAt(i, new JScrollPane());
			termList.setListData(new Object[]{});
			
			// clear entity renderer panes
			for (int j=0; j<termDisplayPane.getTabCount(); j++) {
				Component display = (Component) editors.get(j);
				if (display instanceof JEditorPane) ((JEditorPane) display).setText("");
			}
						
			// disable entity mod buttons
			addClassBtn.setEnabled(false);
			addPropBtn.setEnabled(false);
			addIndBtn.setEnabled(false);
			addGCIBtn.setEnabled(false);
			remTermBtn.setEnabled(false);
			renameTermBtn.setEnabled(false);
			return;
		}
		else if (event.getType() == ModelChangeEvent.ONTOLOGY_SEL_CHANGED) {
			this.revertUIOntologySettings();
		}
		else
		if (event.getType() == ModelChangeEvent.ONTOLOGY_RELOADED) {
			OWLOntology reloadOnt = (OWLOntology) event.getSource();
			this.removeFromCache(reloadOnt);
			if (swoopModel.getSelectedOntology().equals(reloadOnt)) {
				treeRenderer.useOldClassTreeReferenceforExpansion = true;
				treeRenderer.useOldPropertyTreeReferenceforExpansion = true;
				updateTreeDisplay();
				updateListDisplay();
				// displayTerm();
				if (termTabPane.getSelectedIndex()<2)
					try {
						// Note: After applying changes, selecting the tree node 
						// is necessary in the new swoop because we don't expand 
						// all the nodes in the tree by default
						// However, this may slow down performance because selectTreeNode 
						// runs through all the nodes in the tree					
						if (swoopModel.selectedEntity!=null) this.selectTreeNode(trees[termTabPane.getSelectedIndex()], swoopModel.getSelectedEntity().getURI(), false);
						// also note that selectTreeNode invokes displayTerm via listener interaction
					} catch (OWLException e) {
						e.printStackTrace();
					}
				else displayTerm(); // for alphabetical list
			}
		}
		else
		if (event.getType() == ModelChangeEvent.ONTOLOGY_CHANGED) {
			
			Set changedOntologies = (HashSet) event.getSource();
			Iterator coIter = changedOntologies.iterator();
			while (coIter.hasNext()) {
				OWLOntology ont = (OWLOntology) coIter.next();
				this.removeFromCache(ont);				
			}
			treeRenderer.useOldClassTreeReferenceforExpansion = true;
			treeRenderer.useOldPropertyTreeReferenceforExpansion = true;
			updateTreeDisplay();
			updateListDisplay();
			// displayTerm();
			
			if (termTabPane.getSelectedIndex()<2)
				try {
					// Note: After applying changes, selecting the tree node 
					// is necessary in the new swoop because we don't expand 
					// all the nodes in the tree by default
					// However, this may slow down performance because selectTreeNode 
					// runs through all the nodes in the tree					
					if (swoopModel.selectedEntity!=null) this.selectTreeNode(trees[termTabPane.getSelectedIndex()], swoopModel.getSelectedEntity().getURI(), false);
					// also note that selectTreeNode invokes displayTerm via listener interaction
				} catch (OWLException e) {
					e.printStackTrace();
				}
			else displayTerm(); // for alphabetical list
		}
		else
		if(event.getType() == ModelChangeEvent.IMPORTS_VIEW_CHANGED) {
			
			System.out.println("Imports changed to: " + swoopModel.getShowImports());
			
			Iterator ontIter = swoopModel.getOntologies().iterator();
			while (ontIter.hasNext()) {
				OWLOntology ont = (OWLOntology) ontIter.next();
				try {
					if (ont.getIncludedOntologies().size()>0) {
						treeRenderer.removeClassTreeCacheEntry(ont);
						treeRenderer.removePropTreeCacheEntry(ont);
						listCache.removeOntology(ont);						
					}
				} catch (OWLException e) {
					e.printStackTrace();
				}
			}
			updateTreeDisplay();
			updateListDisplay();
			displayTerm();
		}
		else
		if (event.getType() == ModelChangeEvent.QNAME_VIEW_CHANGED)		 
		{
			System.out.println("QName changed to: " + swoopModel.getShowQNames());									
			
			updateTreeDisplay();
			updateListDisplay();
			// TODO: the selected entity could be in the imported one which is hidden now
			// so we should only reselect the entity if it is not coming from imports			
			displayTerm();			
		}
		
		// below is done in ontology display by calling swoopHandler.updateOntologyViews
		// since both, ont and term display are listening on swoopModel,
		// it suffices to call it in only place
		
//		else if  (event.getType() == ModelChangeEvent.REASONER_SEL_CHANGED) {
//			
//			System.out.println("Reasoner changed to: " + swoopModel.getReasoner().getName());			
//			swoopHandler.updateOntologyViews(true);				
//		}		
		else if(event.getType() == ModelChangeEvent.ENTITY_SEL_CHANGED){
			//System.out.println("Update the entity renderer");
			displayTerm();
			
			// display entity pane in swoop frame
			swoopHandler.displayEntityPane();
			
		}
		else {
			System.out.println("Model changed " + event.getType());
			//updateTreeDisplay();
			//updateListDisplay();
		}
		
		// enable/disable buttons depends on ontology/entity selections
		if (swoopModel.getSelectedOntology()!=null) {
			addClassBtn.setEnabled(true);
			addPropBtn.setEnabled(true);
			addIndBtn.setEnabled(true);
			addGCIBtn.setEnabled(true);
		}
		else {
			addClassBtn.setEnabled(false);
			addPropBtn.setEnabled(false);
			addIndBtn.setEnabled(false);
			addGCIBtn.setEnabled(false);
		}
		
		if (swoopModel.getSelectedEntity()!=null) {
			remTermBtn.setEnabled(true);
			try {
				if (swoopModel.getSelectedOntology()!=null && swoopModel.getSelectedEntity().getOntologies().contains(swoopModel.getSelectedOntology())) renameTermBtn.setEnabled(true);
				else renameTermBtn.setEnabled(false);
			} catch (OWLException e) {				
				e.printStackTrace();
			}
		}
		else if (termList.getSelectedValue() instanceof OWLSubClassAxiom) {
			// else if GCI is selected, enable remTermBtn
			remTermBtn.setEnabled(true);
			renameTermBtn.setEnabled(false);
		}
		else {
			remTermBtn.setEnabled(false);
			renameTermBtn.setEnabled(false);
		}
	}
	
	/**
	 * Critical method that rectifies mismatches in selection
	 * between swoopModel and UI settings related to a ontology
	 * i.e. showImports, showQNames and reasoner selection.
	 * Called after loading new ontSettings in
	 * 1) updateOntologyViews() -- SwoopFrame
	 * 2) modelChanged()==ONT_LOADED -- OntDisplay
	 * 3) modelChanged()==ONT_SEL_CHANGED -- TermsDisplay
	 */
	public void revertUIOntologySettings() {
		
		// rectify mismatch in show imports/qnames
		showImportChk.setSelected(swoopModel.getShowImports());
		showQNamesChk.setSelected(swoopModel.getShowQNames());
		
		// rectify mismatch in reasoner selection in UI
		if (reasonerCombo.getSelectedIndex()>=0) {
			SwoopReasoner selReasoner = (SwoopReasoner) reasoners.get(reasonerCombo.getSelectedIndex());
			if (!selReasoner.getClass().equals(swoopModel.getReasoner().getClass())) {
				reasonerCombo.removeActionListener(this);
				for (int i=0; i<reasoners.size(); i++) {
					if (reasoners.get(i).getClass().equals(swoopModel.getReasoner().getClass())) {
						reasonerCombo.setSelectedIndex(i);
						break;
					}
				}
				reasonerCombo.addActionListener(this);
			}
		}
	}
	
	class EnableDebugListener implements VetoableChangeListener {
	    public void vetoableChange(PropertyChangeEvent e)
	                            throws PropertyVetoException {
	       String name = e.getPropertyName();

	       if(name.equals("enabled")) {
	          Boolean oldValue = (Boolean)e.getOldValue(),
	                  newValue = (Boolean)e.getNewValue();

	          if(oldValue == Boolean.FALSE &&
	             newValue == Boolean.TRUE) {
	             int answer = JOptionPane.showConfirmDialog(
	                 	swoopHandler,      
	                 	"Debugging may slow down browsing considerably. " +
				        "Are you sure you want to continue?", 
	                 	"Enable debugging",
	                 	JOptionPane.YES_NO_CANCEL_OPTION);

	             if(answer != JOptionPane.YES_OPTION) {
	                throw new PropertyVetoException("debugging cancelled", e);
	             }
	          }
	       }
	    }
	 }
}
