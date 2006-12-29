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
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.Vector;

import javax.swing.Box;
import javax.swing.JCheckBox;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.filechooser.FileFilter;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.DefaultHighlighter.DefaultHighlightPainter;

import org.mindswap.swoop.annotea.AnnoteaRenderer;
import org.mindswap.swoop.change.ChangeLog;
import org.mindswap.swoop.change.OntologyChangeRenderer;
import org.mindswap.swoop.change.SwoopChange;
import org.mindswap.swoop.change.VersionControl;
import org.mindswap.swoop.debugging.RepairFrame;
import org.mindswap.swoop.explore.AxiomExtractor;
import org.mindswap.swoop.explore.AxiomIndexer;
import org.mindswap.swoop.explore.AxiomList;
import org.mindswap.swoop.fun.Sudoku;
import org.mindswap.swoop.popup.PopupOntologySource;
import org.mindswap.swoop.reasoner.PelletReasoner;
import org.mindswap.swoop.refactoring.AutoEconnPartitioning;
import org.mindswap.swoop.refactoring.Segmentation;
import org.mindswap.swoop.renderer.entity.ConciseFormatEntityRenderer;
import org.mindswap.swoop.renderer.ontology.SwoopOntologyInfo;
import org.mindswap.swoop.utils.Bookmark;
import org.mindswap.swoop.utils.QueryInterface;
import org.mindswap.swoop.utils.SwoopCache;
import org.mindswap.swoop.utils.SwoopLoader;
import org.mindswap.swoop.utils.SwoopPreferences;
import org.mindswap.swoop.utils.VersionInfo;
import org.mindswap.swoop.utils.graph.hierarchy.ClassHierarchyGraph;
import org.mindswap.swoop.utils.graph.hierarchy.ui.MotherShipFrame;
import org.mindswap.swoop.utils.owlapi.CorrectedRDFRenderer;
import org.mindswap.swoop.utils.owlapi.ImportChange;
import org.mindswap.swoop.utils.owlapi.OWLOntSplitter;
import org.mindswap.swoop.utils.treeexport.STFileFilter;
import org.mindswap.swoop.utils.treeexport.TM3FileFilter;
import org.mindswap.swoop.utils.treeexport.VisualizationFileFilter;
import org.mindswap.swoop.utils.ui.BookmarkComparator;
import org.mindswap.swoop.utils.ui.BrowserControl;
import org.mindswap.swoop.utils.ui.LaunchBar;
import org.mindswap.swoop.utils.ui.LocationBar;
import org.mindswap.swoop.utils.ui.SwingWorker;
import org.mindswap.swoop.utils.ui.SwoopFileFilter;
import org.mindswap.swoop.utils.ui.SwoopHTMLFileFilter;
import org.mindswap.swoop.utils.ui.SwoopProgressDialog;
import org.mindswap.swoop.utils.ui.TextFileFilter;
import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLDataProperty;
import org.semanticweb.owl.model.OWLEntity;
import org.semanticweb.owl.model.OWLException;
import org.semanticweb.owl.model.OWLIndividual;
import org.semanticweb.owl.model.OWLNamedObject;
import org.semanticweb.owl.model.OWLObject;
import org.semanticweb.owl.model.OWLObjectProperty;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.change.AddImport;
import org.semanticweb.owl.model.change.ChangeVisitor;
import org.semanticweb.owl.model.change.OntologyChange;
import org.semanticweb.owl.model.helper.OntologyHelper;

public class SwoopFrame extends JFrame implements ActionListener, WindowListener, SwoopModelListener {

	private JMenu bookmarkMenu;
	private JMenuItem JMenuFileNew, newOntMItem, loadOntMItem, loadWkspMItem,
			saveWkspMItem, saveAsMItem, ontSaveMItem, ontCodeMenu,
			ontCodeASMenu, exportStatsMItem, exportTreeMItem, exportHTMLMItem;
	private JMenuItem clearMItem, exportMItem, prefMItem, addResHoldMItem, 
			viewResHoldMItem, queryMItem, repairMItem, versionMItem, sudokuMItem;
	private JMenuItem addBookmarkMenu, remBookmarkMenu, sortBookmarkMenu;
	private JMenuItem JMenuAutomatic, browserMenu, reloadOntMenu, refreshOntMenu, extractModMenu, extractModDualMenu;
//	public  JMenu sosMenu;
	private JMenuItem tableauSOSMenu, findAllMUPSMenu, splitOntMItem;	
	private JMenu JMenuAdvanced; // location divider between ontology and term display

	//*****************************************************
	private JCheckBoxMenuItem viewSideBarMenu, viewChangeBarMenu, viewOptionBarMenu, showEnableRules;
	private JCheckBox showChangeBarChk;

	private JSplitPane rendererPanel, sidePanel, centerPanel, rendererAdvancedPane;
	private JPanel optionPanel, ontPanel;
	
	private Font tahoma = new Font("Tahoma", Font.PLAIN, 11);
	private List bookmarks;
	private File openFile = null, saveFile = null;

	private int sideBarPos = 260;
	private int advancedPanePos = 440;
	private int dividerLocation = 250;

	/*
	 * Important public fields
	 */
	public SwoopModel swoopModel; // main model object shared by all swoop components
	public OntologyDisplay ontDisplay; // handler for ontology display
	public TermsDisplay termDisplay; // handler for terms display
	public ChangeLog changeLog; // pane that records all ontology changes
	public LaunchBar launchBar; // can be used to launch a Swoogle/Google search
	public AnnoteaRenderer annotRenderer; // pane that displays Annotea annotations
	public VersionControl versionControl; // pane that displayed version controlled ontologies
	public File ontFile = null, wkspcFile = null;
	public PopupOntologySource srcWindow = null;
	public JTabbedPane advancedTabPane;
	public JCheckBoxMenuItem launchBarMenu;;
	public JMenuItem ontRemoveMItem;
	private LocationBar locationBar;
	private HashMap fileOntMap; // map: ontology URI --> local file location
	private SwoopLoader loader;
	
	public SwoopFrame(SwoopModel swoopModel) {

		this.swoopModel = swoopModel;
		loader = new SwoopLoader(this, swoopModel);
		this.changeLog = new ChangeLog(this, swoopModel);
		this.launchBar = new LaunchBar(this);
		this.fileOntMap = new HashMap();
		swoopModel.setFrame(this);

		
		swoopModel.loadPreferences();

		setupUI();
		defaultUI();

		addSwoopModelListeners();
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		this.addWindowListener(this);

		// finally do this
		// check option in SwoopModel for opening last Workspace (also saved as default.swp)
		this.loadBookmarkFile();
		if (!Swoop.isWebStart()) {
			// normal app loaded
			
			if (swoopModel.isOpenLastWorkspace()) {
				wkspcFile = swoopModel.getWkspcFile();
				this.loadWorkspace(false);
				// enable menu options such as Save, Export etc
				this.enableMenuOptions();
				if (wkspcFile != null)
					saveWkspMItem.setText("Save Workspace (../"
							+ wkspcFile.getName() + ")");
			}
		} 
	}

	private void setupUI() {

		// setup look & feel of UI
		setupLookFeel();

		// draw all panels
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BorderLayout());

		// create ontology display panel
		ontDisplay = new OntologyDisplay(swoopModel, this);

		// create terms display panel
		termDisplay = new TermsDisplay(this, swoopModel);

		// add row panel for Address URL bar
		locationBar = new LocationBar(this, swoopModel);
		
		// create side panel containing ontList and entityTree/List
		sidePanel = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		ontPanel = new JPanel();
		ontPanel.setLayout(new BorderLayout());
		ontPanel.add(ontDisplay.ontToolBar, "North");
		ontPanel.add(ontDisplay.ontListPane, "Center");
		sidePanel.setTopComponent(ontPanel);
		sidePanel.setBottomComponent(termDisplay.termListPanel);

		// create renderer panel containing renderers for ontology and entities
		rendererPanel = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		rendererPanel.setTopComponent(ontDisplay.ontDescTab);
		rendererPanel.setBottomComponent(termDisplay.termDisplayEditPane);

		// add show advanced check box
		showChangeBarChk = new JCheckBox("Changes/Annotations");
		showChangeBarChk.addActionListener(this);
		showChangeBarChk.setFont(tahoma);
		showChangeBarChk
				.setToolTipText("Show Change Tracking and Annotea Annotations");

		// add advanced panel
		advancedTabPane = new JTabbedPane();
		advancedTabPane.setFont(tahoma);
		advancedTabPane.addTab("Change Tracking", changeLog);
		advancedTabPane.setVisible(false);

		// add annotea renderer to tab
		annotRenderer = new AnnoteaRenderer(swoopModel, this);
		advancedTabPane.addTab("Annotea Annotations", annotRenderer);

		// add renderer and advanced to a joint split pane
		rendererAdvancedPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		rendererAdvancedPane.setLeftComponent(rendererPanel);
		rendererAdvancedPane.setRightComponent(advancedTabPane);

		// create option tool bar
		optionPanel = new JPanel();
		optionPanel.setLayout(new BorderLayout());
		JToolBar optionBar = new JToolBar();
		optionBar.add(termDisplay.showInheritedChk);
		optionBar.add(showChangeBarChk);
		optionBar.add(termDisplay.termEditableChk);
		optionPanel.add(optionBar, "East");

		// create browserpanel containing rendererPanel and optionPanel
		JPanel browserPanel = new JPanel();
		browserPanel.setLayout(new BorderLayout());
		browserPanel.add(optionPanel, "North");
		browserPanel.add(rendererAdvancedPane, "Center");
		browserPanel.add(termDisplay.termEditButtonPane, "South");

		// add side panel and browser panel to center
		centerPanel = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		centerPanel.setLeftComponent(sidePanel);
		centerPanel.setRightComponent(browserPanel);
		//                centerPanel.setOneTouchExpandable(true);

		// add panels to mainPanel
		mainPanel.add(locationBar, "North");
		mainPanel.add(centerPanel, "Center");

		// add mainPanel to contentPane
		getContentPane().setLayout(new GridLayout(1, 1));
		getContentPane().add(mainPanel);

		setupMenuBar();

		// add advanced menu option: version control
		versionControl = new VersionControl(swoopModel, this);
		versionControl.setVisible(false);

		// initialize JFrame
		//setTitle("SWOOP v2.3 beta 3.1 (Jan 2006)"); // + Swoop.getVersionInfo().getVersionString());
		setTitle("SWOOP "+Swoop.getVersionInfo().getVersionString());
		setSize(1024, 740);

		// need to set divider location in splitpane after it is drawn and repainted on screen
		rendererPanel.setDividerLocation(dividerLocation);
	}

	public void addSwoopModelListeners() {

		swoopModel.addListener(this);
		swoopModel.addListener(ontDisplay);
		swoopModel.addListener(termDisplay);
		swoopModel.addListener(changeLog);
		swoopModel.addListener(annotRenderer);
	}

	/**
	 * Default UI settings on startup
	 *
	 */
	public void defaultUI() {
		sidePanel.setVisible(true); // display sidebar
		centerPanel.setDividerLocation(sideBarPos);
		termDisplay.termDisplayEditPane.setVisible(false); // display advanced entity pane ?
		optionPanel.setVisible(true); //display option bar
		if (swoopModel.isShowChangeAnnotBar())
			this.toggleChangeAnnotationPane(true);
		else
			this.viewChangeBarMenu.setSelected(false);
		this.viewSideBarMenu.setSelected(true);
		this.viewOptionBarMenu.setSelected(true);
		this.changeLog.getOntRadio().setSelected(true);
		this.changeLog.scope = ChangeLog.ONTOLOGY_SCOPE;
	}

	/*
	 * Expand ontology combo box into a list box displaying all ontologies
	 */
	public void expandOntPanel() {
		ontPanel.removeAll();
		ontPanel.setLayout(new BorderLayout());
		ontPanel.add(ontDisplay.ontToolBar, "North");
		ontPanel.add(ontDisplay.ontListPane, "Center");
		sidePanel.setDividerLocation(sidePanel.getLastDividerLocation());
		repaint();
	}

	/*
	 * Collapse ontology list box into a drop down list
	 */
	public void collapseOntPanel() {
		ontPanel.removeAll();
		ontPanel.setLayout(new BorderLayout());
		ontPanel.add(ontDisplay.ontToolBar, "North");
		ontPanel.add(ontDisplay.ontHideBox, "Center");
		sidePanel.setDividerLocation(sidePanel.getMinimumDividerLocation());
		repaint();
	}

	/*
	 * Display ontology pane in swoop frame when ontology is selected
	 */
	public void displayOntologyPane() {

		swoopModel.selectedOWLObject = swoopModel.selectedOntology;
		swoopModel.selectedEntity = null;
		ontDisplay.ontDescTab.setVisible(true);
		termDisplay.termDisplayEditPane.setVisible(false);
		termDisplay.showIconsChk.setVisible(false);
		termDisplay.clearSelections();
	}

	/*
	 * Display entity pane in swoop frame when entity is selected
	 */
	public void displayEntityPane() {
		ontDisplay.ontDescTab.setVisible(false);
		termDisplay.termDisplayEditPane.setVisible(true);
		termDisplay.showIconsChk.setVisible(true);
		swoopModel.selectedOWLObject = swoopModel.selectedEntity;
	}

	/**
	 * setup look and feel of UI depending on OS (Windows/Mac etc)
	 */
	private void setupLookFeel() {

		try {
			UIManager
					.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
		} catch (Exception ex) {
			try {
				UIManager
						.setLookAndFeel("com.sun.java.swing.plaf.mac.MacLookAndFeel");
			} catch (Exception ex2) {
				try {
					UIManager.setLookAndFeel(UIManager
							.getSystemLookAndFeelClassName());
				} catch (Exception ex3) {
				}
			}
		}
	}

	private void setupMenuBar() {

		// setup menu bar
		JMenuBar JmenuBar = new JMenuBar();

		// create file menu
		JMenu JMenuFile = new JMenu("File");
		newOntMItem = new JMenuItem("New Ontology");
		newOntMItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N,
				ActionEvent.CTRL_MASK));
		newOntMItem.setToolTipText("Create New Ontology in SWOOP");
		JMenu loadMItem = new JMenu("Load");
		loadOntMItem = new JMenuItem("Ontology");
		loadOntMItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L,
				ActionEvent.CTRL_MASK));
		loadWkspMItem = new JMenuItem("Workspace");
		loadWkspMItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L,
				ActionEvent.CTRL_MASK + ActionEvent.SHIFT_MASK));
		loadMItem.add(loadOntMItem);
		loadMItem.add(loadWkspMItem);
		JMenu saveMItem = new JMenu("Save");
		ontSaveMItem = new JMenuItem("Ontology ");
		ontSaveMItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S,
				ActionEvent.CTRL_MASK));
		saveWkspMItem = new JMenuItem("Workspace");
		saveWkspMItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S,
				ActionEvent.CTRL_MASK + ActionEvent.SHIFT_MASK));
		saveMItem.add(ontSaveMItem);
		saveMItem.add(saveWkspMItem);
		ontRemoveMItem = new JMenuItem("Remove Ontology");
		ontRemoveMItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R,
				ActionEvent.CTRL_MASK));
		ontRemoveMItem.setToolTipText("Remove Selected Ontology from SWOOP");
		clearMItem = new JMenuItem("Clear All");
		clearMItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R,
				ActionEvent.CTRL_MASK + ActionEvent.SHIFT_MASK));
		saveAsMItem = new JMenuItem("Save As..");
		exportMItem = new JMenuItem("Export Remotely");
		exportMItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E,
				ActionEvent.CTRL_MASK));
		exportMItem
				.setToolTipText("Export Selected Ontology to remote WebDav Store");
		exportStatsMItem = new JMenuItem("Export Ontology Statistics");
		exportStatsMItem.setToolTipText("Export Ontology Statistics to a .txt file");
		
		exportTreeMItem = new JMenuItem("Export Class Tree");
		exportTreeMItem.setToolTipText("Export the current class tree to treemap files (tm3) for visualization");		
		
		exportHTMLMItem = new JMenuItem("Export HTML");
		exportHTMLMItem.setToolTipText("Export HTML for Currently Selected Entity / Ontology");
		
		JMenuItem exitMItem = new JMenuItem("Exit");
		JMenuAdvanced = new JMenu("Advanced");

		// by default need to disable certain menu options (save etc) on startup
		this.disableMenuOptions();

		JMenuFile.add(newOntMItem);
		JMenuFile.add(ontRemoveMItem);
		JMenuFile.addSeparator();
		JMenuFile.add(loadMItem);
		JMenuFile.add(saveMItem);
		JMenuFile.add(saveAsMItem);
		JMenuFile.addSeparator();
		JMenuFile.add(exportMItem);
		JMenuFile.add(exportHTMLMItem);
//		JMenuFile.add(exportStatsMItem);
		JMenuFile.add(exportTreeMItem);		
		JMenuFile.addSeparator();
		JMenuFile.add(clearMItem);
		JMenuFile.addSeparator();

		prefMItem = new JMenuItem("Preferences");
		prefMItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P,
				ActionEvent.CTRL_MASK));
		JMenuFile.add(prefMItem);
		JMenuFile.addSeparator();

		JMenuFile.add(exitMItem);
		JmenuBar.add(JMenuFile);

		// create View menu
		JMenu viewPanel = new JMenu("View");
		JMenu sideBars = new JMenu("SideBars");
		viewSideBarMenu = new JCheckBoxMenuItem("Navigation SideBar");
		viewChangeBarMenu = new JCheckBoxMenuItem("Changes/Annotations SideBar");
		viewOptionBarMenu = new JCheckBoxMenuItem("Option-Bar");
		sideBars.add(viewSideBarMenu);
		sideBars.add(viewChangeBarMenu);
		sideBars.add(viewOptionBarMenu);

		launchBarMenu = new JCheckBoxMenuItem("Launch Bar");
		launchBarMenu.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0));

		refreshOntMenu = new JMenuItem("Refresh");
		refreshOntMenu.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R,
				ActionEvent.ALT_MASK));
		reloadOntMenu = new JMenuItem("Reload..");
		reloadOntMenu.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L,
				ActionEvent.ALT_MASK));
		ontCodeMenu = new JMenuItem("Source - RDF/XML");
		ontCodeMenu.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F3, 0));
		ontCodeASMenu = new JMenuItem("Source - Abstract Syntax");
		ontCodeASMenu.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F4, 0));
		browserMenu = new JMenuItem("Current URL (page) in default Web Browser");
		browserMenu.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F10, 0));

		viewPanel.add(sideBars);
		viewPanel.add(launchBarMenu);
		viewPanel.addSeparator();
		viewPanel.add(refreshOntMenu);
		viewPanel.add(reloadOntMenu);
		viewPanel.addSeparator();
		viewPanel.add(ontCodeMenu);
		viewPanel.add(ontCodeASMenu);
		viewPanel.addSeparator();
		viewPanel.add(browserMenu);
		JmenuBar.add(viewPanel);

		// create bookmark menu
		bookmarkMenu = new JMenu("Bookmarks");
		addBookmarkMenu = new JMenuItem("Add Bookmark");
		addBookmarkMenu.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_B,
				ActionEvent.CTRL_MASK));
		addBookmarkMenu.addActionListener(this);
		remBookmarkMenu = new JMenuItem("Remove Bookmark");
		remBookmarkMenu.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_B,
				ActionEvent.CTRL_MASK + ActionEvent.SHIFT_MASK));
		remBookmarkMenu.addActionListener(this);
		sortBookmarkMenu = new JMenuItem("Sort Bookmarks Alphabetically");
		sortBookmarkMenu.addActionListener(this);
		bookmarkMenu.add(addBookmarkMenu);
		bookmarkMenu.add(remBookmarkMenu);
		bookmarkMenu.add(sortBookmarkMenu);
		bookmarkMenu.addSeparator();
		JmenuBar.add(bookmarkMenu);

		JMenu RHMenu = new JMenu("Resource Holder");
		addResHoldMItem = new JMenuItem("Add Current Entity to Holder");
		addResHoldMItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F11,
				0));
		RHMenu.add(addResHoldMItem);
		viewResHoldMItem = new JMenuItem("View Resource Holder");
		viewResHoldMItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F12,
				0));
		RHMenu.add(viewResHoldMItem);
		JmenuBar.add(RHMenu);

		// create advanced menu		
		// add debug menu
		JMenu debugMenu = new JMenu("Debugging / Explanation");
//		debugMenu.add(termDisplay.debugChk);
		tableauSOSMenu = new JCheckBoxMenuItem("Compute Explanation: Black Box Method");
		tableauSOSMenu.setSelected(!swoopModel.isUseTableau());
		debugMenu.add(tableauSOSMenu);
		debugMenu.addSeparator();
		debugMenu.add(termDisplay.debugGlassChk);
		JMenu sosMenu = new JMenu("Advanced Clash / SOS");
		sosMenu.setEnabled(swoopModel.isDebugGlass());		
//		sosMenu.add(tableauSOSMenu);
		tableauSOSMenu.addActionListener(this);
		findAllMUPSMenu = new JCheckBoxMenuItem("Display All SOS (only done inline)");
		findAllMUPSMenu.setSelected(swoopModel.isFindAllMUPS());
		sosMenu.add(findAllMUPSMenu);		
		findAllMUPSMenu.addActionListener(this);
//		debugMenu.add(sosMenu);
		debugMenu.add(findAllMUPSMenu);
		debugMenu.add(termDisplay.debugBlackChk);
		JMenuAdvanced.add(debugMenu);
		//Module extraction
		extractModMenu = new JMenuItem("Extract Module");
		extractModMenu.addActionListener(this);
		JMenuAdvanced.add(extractModMenu);
		
		//
		//Dual module extraction
		extractModDualMenu = new JMenuItem("Extract (Dual) Module");
		extractModDualMenu.addActionListener(this);
		JMenuAdvanced.add(extractModDualMenu);
		
		//
		JMenuItem showOntGraph = new JMenuItem("Fly The MotherShip");
		showOntGraph.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) 
			{
				flyTheMothership();
			}
		});
		JMenuAdvanced.add(showOntGraph);
		
		
		// axiomatic view
		/*
		JMenuItem showClassAxioms= new JMenuItem("Show Told Class Axioms");
		showClassAxioms.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) 
			{
				showToldClassAxioms();
			}
		});
		JMenuAdvanced.add(showClassAxioms);
		*/
		
		// axiomatic view
		JMenuItem showClassAxioms2= new JMenuItem("Show Class Expression Table");
		showClassAxioms2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) 
			{
				showInferredClassAxioms();
			}
		});
		JMenuAdvanced.add(showClassAxioms2);
		
		
		
		
		JMenuAutomatic = new JMenuItem("Partition Automatically");		
		JMenuAutomatic.setToolTipText("Partition the Selected Ontology using the E-connections Framework");
		JMenuAdvanced.add(JMenuAutomatic);
		showEnableRules = new JCheckBoxMenuItem("Enable Rules");	
		JMenuAdvanced.add(showEnableRules);
		queryMItem = new JMenuItem("Pellet Query");
		JMenuAdvanced.add(queryMItem);
		repairMItem = new JMenuItem("Repair Ontology");
		JMenuAdvanced.add(repairMItem);
		versionMItem = new JMenuItem("Version Control");
		JMenuAdvanced.add(versionMItem);
		sudokuMItem = new JMenuItem("Play Sudoku");
		JMenuAdvanced.add(sudokuMItem);
		splitOntMItem = new JMenuItem("Split Ontology");
		JMenuAdvanced.add(splitOntMItem);
		splitOntMItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				OWLOntSplitter os = new OWLOntSplitter(swoopModel);
				OWLOntology ont = swoopModel.getSelectedOntology();
				OWLOntology newOnt = os.splitAxioms(ont);
				try {
					swoopModel.removeOntology(ont.getURI());
	        		termDisplay.removeFromCache(ont);
	        		swoopModel.addOntology(newOnt);
	        		swoopModel.setSelectedOntology(newOnt);
	    			JOptionPane.showMessageDialog(null, "Ontology model updated successfully", "Update", JOptionPane.INFORMATION_MESSAGE);
				} 
				catch (OWLException e) {
					e.printStackTrace();
				}	
			}
		});
		
		JMenuItem showClsGraph = new JMenuItem("Show class hierarchy graph");
		showClsGraph.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				try {
					Box msgPanel = Box.createHorizontalBox();
					OWLClass top = null;

					OWLEntity entity = swoopModel.getSelectedEntity();
					if (entity != null && entity instanceof OWLClass) {
						top = (OWLClass) entity;
					} else {
						OWLOntology ont = swoopModel.getSelectedOntology();
						if (ont == null) {
							JOptionPane.showMessageDialog(null,
									"No ontology selected");
							return;
						} else
							top = ont.getOWLDataFactory().getOWLThing();
					}
					msgPanel.add(new ClassHierarchyGraph(swoopModel, top));

					JFrame frame = new JFrame("Class Hierarchy");
					frame.getContentPane().add(msgPanel);
					frame.setSize(600, 500);
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
//		JMenuAdvanced.add(showClsGraph);

		// *** For debugging purposes only (TEMPORARY STUFF) ****
		JMenuItem debugMItem = new JMenuItem("Run Debug Tests");
		JMenuAdvanced.add(debugMItem);
		debugMItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				try {
					((ConciseFormatEntityRenderer) termDisplay.renderers.get(0)).runDebugTests();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		JmenuBar.add(JMenuAdvanced);

		// add about at the end of the menu
		JMenu aboutMenu = new JMenu("About");
		JMenuItem aboutMItem = new JMenuItem("About SWOOP..");
		aboutMenu.add(aboutMItem);
		JmenuBar.add(aboutMenu);

		// add menu bar to frame
		setJMenuBar(JmenuBar);

		// add action listeners for menu items
		newOntMItem.addActionListener(this);
		loadOntMItem.addActionListener(this);
		ontRemoveMItem.addActionListener(this);
		loadWkspMItem.addActionListener(this);
		saveWkspMItem.addActionListener(this);
		saveAsMItem.addActionListener(this);
		clearMItem.addActionListener(this);
		ontSaveMItem.addActionListener(this);
		exportMItem.addActionListener(this);
		exportStatsMItem.addActionListener(this);
		exportTreeMItem.addActionListener(this);
		exportHTMLMItem.addActionListener(this);
		prefMItem.addActionListener(this);
		addResHoldMItem.addActionListener(this);
		viewResHoldMItem.addActionListener(this);
		launchBarMenu.addActionListener(this);
		refreshOntMenu.addActionListener(this);
		reloadOntMenu.addActionListener(this);
		browserMenu.addActionListener(this);
		JMenuAutomatic.addActionListener(this);
		queryMItem.addActionListener(this);
		repairMItem.addActionListener(this);
		showEnableRules.addActionListener(this);
		versionMItem.addActionListener(this);
		sudokuMItem.addActionListener(this);
		aboutMItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				VersionInfo vinfo = Swoop.getVersionInfo();
				// add information in the About.. message panel
				JPanel msgPanel = new JPanel();
				msgPanel.setLayout(new GridLayout(6, 1));
				msgPanel.add(createLabel("SWOOP "+vinfo.getVersionString()));
				msgPanel.add(createLabel("Release Date: "+vinfo.getReleaseDate()));
				msgPanel.add(createLabel(""));
				msgPanel.add(createLabel("MINDSWAP Research Group"));
				msgPanel.add(createLabel("University of Maryland, College Park"));
				//                	msgPanel.add(new JLabel(""));
				msgPanel.add(createLabel("http://www.mindswap.org/2004/SWOOP/"));
				JOptionPane.showMessageDialog(null, msgPanel);
			}
		});
		exitMItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				SwoopClose();
			}
		});
		ontCodeMenu.addActionListener(this);
		ontCodeASMenu.addActionListener(this);
		viewSideBarMenu.addActionListener(this);
		viewChangeBarMenu.addActionListener(this);
		viewOptionBarMenu.addActionListener(this);

		// setup shortcuts (hidden from user)
		JMenu shortcutsMenu = new JMenu();
		JMenuItem showIconsMenu, showDivMenu, enableAutoRetMenu, prevMenu, nextMenu;
		showIconsMenu = new JMenuItem();
		showIconsMenu.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				swoopModel.setShowIcons(!swoopModel.getShowIcons(), true);
			}
		});
		showIconsMenu.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_I,
				ActionEvent.CTRL_MASK));
		shortcutsMenu.add(showIconsMenu);

		showDivMenu = new JMenuItem();
		showDivMenu.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				swoopModel.setShowDivisions(!swoopModel.getShowDivisions());
			}
		});
		showDivMenu.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D,
				ActionEvent.CTRL_MASK));
		shortcutsMenu.add(showDivMenu);

		enableAutoRetMenu = new JMenuItem();
		enableAutoRetMenu.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				swoopModel.setEnableAutoRetrieve(!swoopModel
						.getEnableAutoRetrieve());
			}
		});
		enableAutoRetMenu.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_U,
				ActionEvent.CTRL_MASK));
		shortcutsMenu.add(enableAutoRetMenu);

		prevMenu = new JMenuItem();
		prevMenu.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT,
				ActionEvent.ALT_MASK));
		prevMenu.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				locationBar.previousHistory();
			}
		});
		shortcutsMenu.add(prevMenu);

		nextMenu = new JMenuItem();
		nextMenu.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT,
				ActionEvent.ALT_MASK));
		nextMenu.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				locationBar.nextHistory();
			}
		});
		shortcutsMenu.add(nextMenu);

		shortcutsMenu.setVisible(false);
		JmenuBar.add(shortcutsMenu);

	}

	public void clearWorkspace(boolean notify) {

		// clear workspace
		String msg = "All Ontologies in the Workspace will be removed. Also, the Change Log and Annotation Cache will be cleared. Continue?";
		int result = -1;
		if (notify)
			result = JOptionPane.showConfirmDialog(this, msg,
					"Clear Workspace", JOptionPane.YES_NO_OPTION);

		if (result == JOptionPane.YES_OPTION || !notify) {

			// clear swoopModel selections
			swoopModel.selectedOntology = null;
			swoopModel.selectedEntity = null;
			swoopModel.selectedOWLObject = null;

			// reset change logs                
			swoopModel.setUncommittedChanges(new ArrayList());
			swoopModel.setCommittedChanges(new ArrayList());

			// remove all ontologies from swoopModel
			Set uriList = new HashSet(swoopModel.getOntologyURIs());
			Iterator iter = uriList.iterator();
			while (iter.hasNext()) {
				URI uri = (URI) iter.next();
				swoopModel.removeOntology(uri);
			}


			// clear changes cache
			swoopModel.getChangesCache().removeAllChanges();

			// clear annotation cache and annotated object uris
			swoopModel.setAnnotatedObjectURIs(new HashSet());
			swoopModel.setAnnotationCache(new SwoopCache());
			// also clear annotation node tree
			if (termDisplay.annoteaRenderer != null) {
				termDisplay.annoteaRenderer.clearAnnotationNodeTree();
			}

			// reset current workspace and ontology files
			ontFile = null;
			wkspcFile = null;
			fileOntMap = new HashMap();

			// reset file menu names
			this.saveWkspMItem.setText("Save Workspace");

			//                if (notify) JOptionPane.showMessageDialog(this, "Workspace Cleared", "Reset", JOptionPane.INFORMATION_MESSAGE);

			this.disableMenuOptions();
		}
	}

	protected void disableMenuOptions() {
		// disable menu options
		saveWkspMItem.setEnabled(false);
		saveAsMItem.setEnabled(false);
		ontSaveMItem.setEnabled(false);
		ontRemoveMItem.setEnabled(false);
		exportMItem.setEnabled(false);
		clearMItem.setEnabled(false);
		exportTreeMItem.setEnabled(false);
		exportStatsMItem.setEnabled(false);
		exportHTMLMItem.setEnabled(false);
	}

	protected void enableMenuOptions() {
		saveWkspMItem.setEnabled(true);
		saveAsMItem.setEnabled(true);
		ontSaveMItem.setEnabled(true);
		exportMItem.setEnabled(true);
		clearMItem.setEnabled(true);
		exportTreeMItem.setEnabled(true);
		exportStatsMItem.setEnabled(true);
		exportHTMLMItem.setEnabled(true);
	}

	/***
	 * Generic load file method using which user can load
	 * SWOOP Workspace (.swp), SWOOP Ontology Object File (.swp)
	 * or standard OWL Ontology (.owl, .rdf)
	 * This method invokes LoadWorkspace(), LoadOntologyFile() or
	 * LoadOWLFile() depending on extension of file selected by user
	 */
	public void loadFile(boolean workspace) {

		JFileChooser wrapChooser = new JFileChooser();
		if (workspace) {
			wrapChooser.addChoosableFileFilter(SwoopFileFilter
					.getWorkspaceFilter());
		} else {
			FileFilter[] filters = SwoopFileFilter.getOntologyFilters();
			for (int i = 0; i < filters.length; i++)
				wrapChooser.addChoosableFileFilter(filters[i]);
		}

		int returnVal;

		if (openFile != null)
			wrapChooser.setCurrentDirectory(openFile);

		// allow multiple selection while loading ontology files
		wrapChooser.setMultiSelectionEnabled(true);

		openFile = null;

		if (openFile == null) {
			returnVal = wrapChooser.showOpenDialog(this);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				openFile = wrapChooser.getSelectedFile();
			} else { // save cancelled
				return;
			}
		}

		// get selected files
		File[] openFiles = wrapChooser.getSelectedFiles();

		if (openFiles.length == 1) {
			// for a single file load, use the standard mechanism
			openFile = openFiles[0];
			String name = openFile.getName().substring(
					openFile.getName().lastIndexOf("/") + 1,
					openFile.getName().length());
			String extension = openFile.getName().substring(
					openFile.getName().lastIndexOf(".") + 1,
					openFile.getName().length());
			if (extension.equals("swp") || extension.equals("SWP")) {
				// load java object file for workspace
				wkspcFile = openFile;
				saveWkspMItem.setText("Save Workspace (../" + name + ")");
				loadWorkspace(true);
			} else if (extension.equals("swo") || extension.equals("SWO")) {
				// load java object file for ontology
				ontFile = openFile;
				loadOntologyFile();
			} else { // load owl file in rdf/txt
				ontFile = openFile;
				loadOWLFile();
			}
		} else {
			// load a batch of OWL files in RDF/Abstract Syntax
			List ontologyList = new ArrayList();
			for (int i = 0; i < openFiles.length; i++) {
				openFile = openFiles[i];
				String name = openFile.getName().substring(
						openFile.getName().lastIndexOf("/") + 1,
						openFile.getName().length());
				String extension = openFile.getName().substring(
						openFile.getName().lastIndexOf(".") + 1,
						openFile.getName().length());
				// exclude java object files
				if (!extension.equals("swp") && !extension.equals("swo")
						&& !extension.equals("SWP") && !extension.equals("SWO")) {
					try {
						FileInputStream in = new FileInputStream(openFile);
						InputStreamReader reader = new InputStreamReader(in);
						URI uri = new URI("file:///" + name);
						OWLOntology ont = null;
						if (extension.equals("txt"))
							ont = swoopModel.loadOntologyInAbstractSyntax(
									reader, uri);
						else
							ont = swoopModel.loadOntologyInRDF(reader, uri);
						ontologyList.add(ont);
						// add ontology to fileOntMap
						this.fileOntMap.put(ont.getPhysicalURI(), openFile);
						
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				}
			} // end opening multiple file loop
			if (ontologyList.size() > 0)
				swoopModel.addOntologies(ontologyList);
		}
	}

	/***
	 * Generic File SaveAs method that allows user to save either the
	 * current SWOOP workspace, selected ontology as SWOOP Ontology Object file
	 * or selected ontology as OWL file in RDF/XML
	 * @param onlyWkspcFilter - if true, user can select only workspace files
	 * @param onlyOntFilter - if true, user can select only ontology files
	 */
	public void saveAsFile(boolean onlyWkspcFilter, boolean onlyOntFilter) {

		JFileChooser wrapChooser = new JFileChooser();
		FileFilter[] filters;

		// add file filters
		if (onlyWkspcFilter) {
			filters = new FileFilter[1];
			filters[0] = SwoopFileFilter.getWorkspaceFilter();
			wrapChooser.setFileFilter(filters[0]);
		} else if (onlyOntFilter) {
			filters = SwoopFileFilter.getOntologyFilters();
			for (int i = 0; i < filters.length; i++)
				wrapChooser.addChoosableFileFilter(filters[i]);
		} else {
			filters = SwoopFileFilter.getAllFilters();
			for (int i = 0; i < filters.length; i++)
				wrapChooser.addChoosableFileFilter(filters[i]);
		}

		if (saveFile != null)
			wrapChooser.setCurrentDirectory(saveFile);

		int returnVal = wrapChooser.showSaveDialog(this);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			saveFile = wrapChooser.getSelectedFile();
		} else { // save cancelled
			return;
		}

		String name = saveFile.getName().substring(
				saveFile.getName().lastIndexOf("/") + 1,
				saveFile.getName().length());

		String extension = "";
		int match = -1;
		for (int i = 0; i < filters.length; i++) {
			if (filters[i].equals(wrapChooser.getFileFilter())) {
				match = i;
				break;
			}
		}
		if (onlyOntFilter)
			match++;

		switch (match) {
		case 0: // swoop workspace filter alone
			extension = "swp";
			break;
		case 1: // swoop ontology file filter
			extension = "swo";
			break;
		case 2: // owl
			extension = "owl";
			break;
		case 3: // rdf
			extension = "rdf";
			break;
		case 4: // xml
			extension = "xml";
			break;
		case 5: // abstract syntax
			extension = "txt";
			break;
		}

		// if no extension determined yet and name contains .
		if (name.indexOf(".") >= 0 && extension.equals("")) {
			extension = name
					.substring(name.lastIndexOf(".") + 1, name.length());
		}

		// finally append extension to chosen file
		if (name.indexOf(".") == -1 && !extension.equals("")) {
			saveFile = new File(saveFile.getAbsolutePath() + "." + extension);
			name += "." + extension;
		}

		// act upon chosen file extension
		if (extension.equals("swp") || extension.equals("SWP")) {
			wkspcFile = saveFile;
			saveWkspMItem.setText("Save Workspace (../" + name + ")");
			saveWorkspace(true, true);
			return;
		} else {
			// save ontology type file
			ontFile = saveFile;
		}

		if (extension.equals("swo") || extension.equals("SWO")) {
			saveOntologyFile();
		} else if (extension.equals("owl") || extension.equals("OWL")
				|| extension.equals("rdf") || extension.equals("RDF")
				|| extension.equals("xml") || extension.equals("XML")) {
			saveOWLFile();
		} else if (extension.toLowerCase().equals("txt")) {
			saveAbstractSyntaxFile();
		} else { // no file extension
			saveOWLFile();
		}
	}

	public void actionPerformed(ActionEvent e)  {

		if (e.getSource() == extractModMenu) {
			// extract module for selected entity
			if (swoopModel.selectedOntology!=null && swoopModel.selectedEntity!=null) {
				try {
					//No dual concepts
					Segmentation seg = null;
					if(!swoopModel.segmentation.containsKey(swoopModel.selectedOntology)){
						seg = new Segmentation(swoopModel.getSelectedOntology(), false, false);
						//No dual roles
						Map aux = new HashMap();
						aux = swoopModel.getSegmentation();
						aux.put(swoopModel.getSelectedOntology(), seg);
						swoopModel.setSegmentation(aux);
					}
					else{
						seg = (Segmentation) swoopModel.getSegmentation().get(swoopModel.selectedOntology);
						seg.setDualConcepts(false);
						seg.setDualRoles(false);
					}
				
					Set allClasses = swoopModel.getSelectedOntology().getClasses();  
					Set allProperties = swoopModel.getSelectedOntology().getObjectProperties();
					allProperties.addAll(swoopModel.getSelectedOntology().getDataProperties());
					Set allEntities = new HashSet();
					allEntities.addAll(allClasses);
					allEntities.addAll(allProperties);
					
					Set allAxioms = seg.getAllAxioms();
					Map axSignature = seg.getAxiomsToSignature();
					Map sigToAxioms = seg.getSignatureToAxioms();
					/*
					System.out.println("Getting the axioms in the ontology");
					Set allAxioms = seg.getAxiomsInOntology(swoopModel.getSelectedOntology());
					System.out.println("Total number of axioms in the Ontology: " + allAxioms.size());
					System.out.println("Getting signature of axioms");
					Map axSignature = seg.axiomsToSignature(allAxioms);
					System.out.println("Got signature of the axioms");
					System.out.println("Creating Map from concept names to axioms");
					Map sigToAxioms = seg.signatureToAxioms(allAxioms, axSignature);
					System.out.println("Got map from concept names to axioms");
					*/
						
					Set sig = new HashSet();
					sig.add(swoopModel.getSelectedEntity());
					
					URI uriOntology = swoopModel.getSelectedOntology().getURI();
					 
					System.out.println("Getting Module");
					OWLOntology module = seg.getModule(allAxioms, sig, axSignature, sigToAxioms, uriOntology, (OWLClass)swoopModel.getSelectedEntity());
					System.out.println("Got Module");
					
					swoopModel.addOntology(module);
					
				} catch (URISyntaxException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (OWLException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				
				/*
				AutoEconnPartitioning partitions = new AutoEconnPartitioning(
						this, swoopModel, swoopModel.getSelectedOntology(), false);
				partitions.findModule(swoopModel.selectedEntity);
				*/
			}				
		}
		else
			if (e.getSource() == extractModDualMenu) {
				// extract dual module for selected entity
				if (swoopModel.selectedOntology!=null && swoopModel.selectedEntity!=null) {
					try {
						Segmentation seg = null;
						if(!swoopModel.segmentation.containsKey(swoopModel.selectedOntology)){
							seg = new Segmentation(swoopModel.getSelectedOntology(), true, true);
							//No dual roles
							Map aux = new HashMap();
							aux = swoopModel.getSegmentation();
							aux.put(swoopModel.getSelectedOntology(), seg);
							swoopModel.setSegmentation(aux);
						}
						else{
							seg = (Segmentation) swoopModel.getSegmentation().get(swoopModel.selectedOntology);
							seg.setDualConcepts(true);
							seg.setDualRoles(true);
						}
					
						Set allClasses = swoopModel.getSelectedOntology().getClasses();  
						Set allProperties = swoopModel.getSelectedOntology().getObjectProperties();
						allProperties.addAll(swoopModel.getSelectedOntology().getDataProperties());
						Set allEntities = new HashSet();
						allEntities.addAll(allClasses);
						allEntities.addAll(allProperties);
						
						Set allAxioms = seg.getAllAxioms();
						Map axSignature = seg.getAxiomsToSignature();
						Map sigToAxioms = seg.getSignatureToAxioms();
						/*
						System.out.println("Getting the axioms in the ontology");
						Set allAxioms = seg.getAxiomsInOntology(swoopModel.getSelectedOntology());
						System.out.println("Total number of axioms in the Ontology: " + allAxioms.size());
						System.out.println("Getting signature of axioms");
						Map axSignature = seg.axiomsToSignature(allAxioms);
						System.out.println("Got signature of the axioms");
						System.out.println("Creating Map from concept names to axioms");
						Map sigToAxioms = seg.signatureToAxioms(allAxioms, axSignature);
						System.out.println("Got map from concept names to axioms");
						*/
							
						Set sig = new HashSet();
						sig.add(swoopModel.getSelectedEntity());
						
						URI uriOntology = swoopModel.getSelectedOntology().getURI();
						 
						System.out.println("Getting Module");
						OWLOntology module = seg.getModule(allAxioms, sig, axSignature, sigToAxioms, uriOntology, (OWLClass)swoopModel.getSelectedEntity());
						System.out.println("Got Module");
						
						swoopModel.addOntology(module);
						
					} catch (URISyntaxException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					} catch (OWLException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					} catch (Exception e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					
					/*
					AutoEconnPartitioning partitions = new AutoEconnPartitioning(
							this, swoopModel, swoopModel.getSelectedOntology(), false);
					partitions.findModule(swoopModel.selectedEntity);
					*/
				}				
			}	
		
		else
		if (e.getSource() == tableauSOSMenu) {
			swoopModel.setUseTableau(!tableauSOSMenu.isSelected());
		}
		else
		if (e.getSource() == findAllMUPSMenu) {
			swoopModel.setFindAllMUPS(findAllMUPSMenu.isSelected());
		}
		else
		if (e.getSource() == newOntMItem) {
			// New Swoop Ontology
			ontDisplay.addNewOntology();
		}
		else
		if (e.getSource() == ontRemoveMItem) {
			// Remove Swoop Ontology
			JList ontList = ontDisplay.ontList;
			if (ontList.getSelectedValues().length == 1) {
				// remove single OWL Ontology
				if (swoopModel.selectedOntology != null)
					ontDisplay
							.removeOntology(swoopModel.selectedOntology, true);
			} else if (ontList.getSelectedValues().length > 1) {
				// batch removal of OWL Ontologies
				String title = "Batch Removal of OWL Ontologies";
				int options = JOptionPane.YES_NO_OPTION;
				int result = JOptionPane
						.showConfirmDialog(
								this,
								"This is going to remove ALL Selected Ontologies from SWOOP. Continue?",
								title, options);
				if (result == JOptionPane.YES_OPTION) {
					Object[] selOnts = ontList.getSelectedValues();
					for (int i = 0; i < selOnts.length; i++) {
						OWLOntology ont = (OWLOntology) selOnts[i];
						ontDisplay.removeOntology(ont, false);
					}
				}
			}
		}
		else
		if (e.getSource() == JMenuFileNew) {
			// New SWOOP workspace
			this.clearWorkspace(true);
		}
		else
		if (e.getSource() == loadOntMItem) {
			// load SWOOP Ontology
			loadFile(false);
		}
		else
		if (e.getSource() == loadWkspMItem) {
			// load SWOOP workspace
			loadFile(true);
		}
		else
		if (e.getSource() == saveWkspMItem) {
			// save SWOOP workspace
			if (wkspcFile != null)
				saveWorkspace(true, true);
			else
				saveAsFile(true, false);
		}
		else
		if (e.getSource() == saveAsMItem) {
			// save as SWOOP workspace
			saveAsFile(false, false);
		}
		else
		if (e.getSource() == clearMItem) {
			// clear workspace
			clearWorkspace(true);
		}
		else
		if (e.getSource() == exportStatsMItem)
		{
			// export ontology statistics
			JFileChooser chooser = new JFileChooser();
		    chooser.addChoosableFileFilter(new TextFileFilter());
		    chooser.removeChoosableFileFilter( chooser.getAcceptAllFileFilter() );
		    int returnVal = chooser.showSaveDialog( this );		    
		    if(returnVal == JFileChooser.APPROVE_OPTION) 
		    {
		    	try {
		    		File f = chooser.getSelectedFile();
		    		// prompt user if overwriting
					if (f.exists()) {
						int result = JOptionPane.showConfirmDialog(this, "Saving File at " + f.getAbsolutePath() + ". Overwrite?", "Save Ontology Statistics", JOptionPane.YES_NO_OPTION);
						if (result != JOptionPane.YES_OPTION) return;
					}
		    		FileWriter fw = new FileWriter(f);
		    		// get swoop ontology info pane
		    		SwoopOntologyInfo soi = (SwoopOntologyInfo) ontDisplay.renderers.get(0);
		    		fw.write(soi.statsText);
		    		fw.close();
		    	}
		    	catch (IOException ex) {
		    		ex.printStackTrace();
		    	}
		    }
		}
		else
		if (e.getSource() == exportTreeMItem)
		{
			JFileChooser chooser = new JFileChooser();
//		    chooser.setDialogType( JFileChooser.SAVE_DIALOG );
		    chooser.addChoosableFileFilter( new TM3FileFilter() );
		    chooser.addChoosableFileFilter( new STFileFilter() );
		    chooser.removeChoosableFileFilter( chooser.getAcceptAllFileFilter() );
		    int returnVal = chooser.showSaveDialog( this );
		    if(returnVal == JFileChooser.APPROVE_OPTION) 
		    {
		       System.out.println("You chose to open this file: " + 
		       					chooser.getSelectedFile().getName());
		       VisualizationFileFilter filter = (VisualizationFileFilter)chooser.getFileFilter();
				this.termDisplay.exportCurrentTree( chooser.getSelectedFile(), filter.getExtension() );
		    }		    			
		}
		else
		if (e.getSource() == exportHTMLMItem)
		{
			// export HTML of currently selected entity/ontology
			this.exportHTML(swoopModel.selectedOWLObject);			
		}
		else
		if (e.getSource() == prefMItem) {
			// show preferences
			SwoopPreferences preferences = new SwoopPreferences(this,
					swoopModel);
			preferences.loadPreferences();
			preferences.setVisible(true);
		}
		else
		if (e.getSource() == ontSaveMItem) {
			// save SWOOP ontology object/owl file
			try {
				// get current ontFile based on ontology selection in Swoop
				ontFile = (File) this.fileOntMap.get(swoopModel.getSelectedOntology().getPhysicalURI());
			} catch (OWLException e1) {
				e1.printStackTrace();
			}
			
			if (ontFile != null) {
				String extension = ontFile.getName().substring(
						ontFile.getName().lastIndexOf(".") + 1,
						ontFile.getName().length());
				if (extension.equals("swo") || extension.equals("SWO"))
					saveOntologyFile();
				else if (extension.equals("owl") || extension.equals("OWL")
						|| extension.equals("rdf") || extension.equals("RDF"))
					saveOWLFile();
			} else
				saveAsFile(false, true);
		}
		else
		if (e.getSource() == exportMItem) {
			ontDisplay.saveRemoteOntology();
		}
		else
		if (e.getSource() == addBookmarkMenu) {
			addBookmark();
		}
		else
		if (e.getSource() == remBookmarkMenu) {
			removeBookmark();
		}
		else
		if (e.getSource() == sortBookmarkMenu) {
			sortBookmarks();
			this.addBookmarksMenu();
		}
		else
		if (e.getSource() == browserMenu) {
			if (swoopModel.selectedOWLObject != null) {
				try {
					String openuri = swoopModel.selectedOWLObject.getURI()
							.toString();
					if (openuri.startsWith("file:/")
							&& !openuri.startsWith("file:///"))
						openuri = "file:///"
								+ openuri.substring(6, openuri.length());
					BrowserControl.displayURL(openuri);
				} catch (OWLException e1) {
					e1.printStackTrace();
				}
			}
		}
		else
		if (e.getSource() == launchBarMenu) {
			// toggle launch bar visibility
			launchBar.setVisible(launchBarMenu.isSelected());
		}
		else
		if (e.getSource() == refreshOntMenu) {
			// refresh ontology in swoop
			OWLEntity copySel = null;
			// also save copy of selected entity if any
			if (swoopModel.selectedOWLObject != null
					&& swoopModel.selectedOWLObject instanceof OWLEntity)
				copySel = (OWLEntity) swoopModel.selectedOWLObject;
			if (swoopModel.selectedOntology != null) {
				// call reloadOntology, but do not reset changes associated with ontology
				swoopModel.reloadOntology(swoopModel.selectedOntology, false);
				if (copySel != null)
					swoopModel.setSelectedEntity(copySel);
			}
		}
		else
		if (e.getSource() == reloadOntMenu) {
			// reload ontology from its physical location
			if (swoopModel.selectedOntology != null) {
				ontDisplay
						.reloadOntologyFromPhysical(swoopModel.selectedOntology);
			}
		}
		else
		if (e.getSource() == ontCodeMenu) {
			viewSource(0); //0 is for RDF/XML
		}
		else
		if (e.getSource() == ontCodeASMenu) {
			viewSource(1); // 1 is for AS format
		}
		else
		if (e.getSource() == addResHoldMItem) {

			termDisplay.addEntityToResourceHolder();
		}
		else
		if (e.getSource() == viewResHoldMItem) {
			termDisplay.comparator.setVisible(true);
		}
		else
		if (e.getSource() == viewSideBarMenu) {
			if (viewSideBarMenu.isSelected()) {
				sidePanel.setVisible(true);
				centerPanel.setDividerLocation(sideBarPos);
				repaint();
			} else {
				sideBarPos = centerPanel.getDividerLocation();
				sidePanel.setVisible(false);
				centerPanel.setDividerLocation(0);
				repaint();
			}
		}
		else
		if (e.getSource() == viewOptionBarMenu) {
			optionPanel.setVisible(viewOptionBarMenu.isSelected());
		}
		else
		if (e.getSource() == viewChangeBarMenu) {
			this.toggleChangeAnnotationPane(viewChangeBarMenu.isSelected());
		}
		else
		if (e.getSource() == showChangeBarChk) {
			this.toggleChangeAnnotationPane(showChangeBarChk.isSelected());
		}

		//****************************************************
		//Added for Partitioning with Econnections
		//****************************************************
		else
		if (e.getSource() == JMenuAutomatic) {

			try {
				OWLOntology ont = swoopModel.getSelectedOntology();
				// we dont handle imports yet! flash warning and return
				if (ont.getIncludedOntologies().size() > 0) {
					JOptionPane
							.showMessageDialog(
									this,
									"This version of SWOOP does not support Partitioning of Ontologies which IMPORT other Ontologies.",
									"Unsupported", JOptionPane.ERROR_MESSAGE);
					return;
				}

				int options = JOptionPane.YES_NO_OPTION;
				int result = JOptionPane
						.showConfirmDialog(
								this,
								"Transforming an OWL Ontology into an E-Connection. This may take time (few secs to mins) depending on the ontology size. Continue?",
								"Warning", options);
				if (result == JOptionPane.YES_OPTION) {
					// depending on some threshold (typically size of the ontology),
					// determine whether the partitions should be saved to disk
					// or opened in Swoop directly (careful memory usage)
					boolean saveToDisk = false;
					int threshold = ont.getClasses().size();
					threshold += ont.getDataProperties().size();
					threshold += ont.getObjectProperties().size();
					threshold += ont.getIndividuals().size();
					if (threshold > 40000) {
						saveToDisk = true;
						JOptionPane
								.showMessageDialog(
										this,
										"Because of memory/speed constraints Ontology Partitions will be written to disk.",
										"Partitions",
										JOptionPane.INFORMATION_MESSAGE);
					}
					AutoEconnPartitioning partitions = new AutoEconnPartitioning(
							this, swoopModel, ont, false);

					// pass applychanges?, debug?, saveToDisk? (all boolean)
					partitions.findPartitions(true, false, saveToDisk, false);
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		else
		if (e.getSource() == queryMItem) {
			this.popupQuery();
		}
		else
		if (e.getSource() == repairMItem) {
			// check to see if pellet reasoner is currently selected			
			if (swoopModel.getReasoner() instanceof PelletReasoner) {
				RepairFrame repFrame = new RepairFrame(this, swoopModel, (PelletReasoner) swoopModel.getReasoner());
				repFrame.launch();
			}
			else {
				// otherwise create new instance of pellet
				// and process ontology (all inside new SwingWorker thread)
				final SwoopFrame swoopHandler = this;
				SwingWorker worker = new SwingWorker() {
					PelletReasoner pellet = new PelletReasoner();
					public Object construct() {
	    				try {
	    					pellet.setOntology(swoopModel.getSelectedOntology());
	    				} 
	    				catch (Exception ex) {
	    					ex.printStackTrace();
	    				}
	    				return null;
	    			}
	    			public void finished() {
	    				RepairFrame repFrame = new RepairFrame(swoopHandler, swoopModel, pellet);
	    				repFrame.launch();
	    				// also generate plan on startup
	    				repFrame.genPlan();
	    				repFrame.refreshPlan();
	    			}
				};
				worker.start();				
			}								
		}
		else
		if (e.getSource() == versionMItem) {
			versionControl.refresh();
			versionControl.setVisible(true);
		}
		else
		if (e.getSource() == sudokuMItem) {
			// launch Sudoku UI
			new Sudoku(swoopModel, this, 4);				
		}
//		 ***************************************************
		// Added for enabling rules
		// ***************************************************
		if (e.getSource() == showEnableRules) {
			if (showEnableRules.isSelected()) { 
				swoopModel.setEnableRules(true);
				}
				else {
					swoopModel.setEnableRules(false);
				}
			ontDisplay.displayOntology();
			termDisplay.displayTerm();
		}
			
	}

	/**
	 * Popup the Pellet Query panel
	 *
	 */
	public void popupQuery() {

		// popup the query interface
		JFrame frame = new JFrame("Pellet Query");
		QueryInterface qi = new QueryInterface(this, swoopModel);
		frame.getContentPane().add(qi);
		frame.setSize(500, 500);
		frame.setVisible(true);
		frame.toFront();
		qi.rdqlText.requestFocus();
	}

	/*
	 * Toggle the visibility of the 'advanced' (current Change/Annotation sidepane)
	 */
	public void toggleChangeAnnotationPane(boolean isVisible) {
		if (isVisible) {
			advancedTabPane.setVisible(true);
			rendererAdvancedPane.setDividerLocation(advancedPanePos);
			repaint();
			showChangeBarChk.setSelected(true);
			viewChangeBarMenu.setSelected(true);
		} else {
			advancedPanePos = rendererAdvancedPane.getDividerLocation();
			advancedTabPane.setVisible(false);
			repaint();
			showChangeBarChk.setSelected(false);
			viewChangeBarMenu.setSelected(false);
		}
		swoopModel.setShowChangeAnnotBar(isVisible);
	}

	/**
	 * Highlight the definition of the selected OWL Entity in the 
	 * Abstract Syntax source code
	 */
	public void highlightEntityAS() {

		if (swoopModel.getSelectedEntity() != null) {

			String entityURI = "";
			try {
				entityURI = swoopModel.shortForm(
						swoopModel.getSelectedEntity().getURI()).toString();
				entityURI = entityURI.substring(entityURI.lastIndexOf(":") + 1);
			} catch (OWLException e) {
			}

			// determine type of entity
			String entityTag = "Class";
			if (swoopModel.getSelectedEntity() instanceof OWLObjectProperty)
				entityTag = "ObjectProperty";
			else if (swoopModel.getSelectedEntity() instanceof OWLDataProperty)
				entityTag = "DatatypeProperty";
			else if (swoopModel.getSelectedEntity() instanceof OWLIndividual)
				entityTag = "Individual";

			boolean saveCaseSetting = srcWindow.matchCase;
			boolean saveWordSetting = srcWindow.matchWord;
			srcWindow.matchCase = false;
			srcWindow.matchWord = false;

			// get the ontology URI
			String strOntologyURI = "";
			try {
				strOntologyURI = swoopModel.selectedOntology.getURI()
						.toString();
			} catch (OWLException e) {
			}

			// with the ontologyURI , search in the text for a match
			String strOntologyPrefix = srcWindow.originalSrc.substring(
					srcWindow.originalSrc.substring(0,
							srcWindow.originalSrc.indexOf(strOntologyURI))
							.lastIndexOf("Namespace(") + 10,
					srcWindow.originalSrc.indexOf(strOntologyURI));

			//now read the first token from strFound 
			// and that's the prefix we are looking for
			String[] tokens = strOntologyPrefix.split("(\\s)+");
			strOntologyPrefix = tokens[0];

			// match starting position of entity definition in source code
			String find = entityTag + "(" + strOntologyPrefix + ":" + entityURI;

			srcWindow.findMatch = find;
			int startMatch = srcWindow.matchString(true, false);

			// if match found, find ending position and select entire code fragment for selected entity
			if (startMatch != -1) {
				//look for the matching closing paren                		
				int endMatch = srcWindow.findClosingParent(startMatch);
				// endMatch += srcWindow.findMatch.length();
				try {
					// srcWindow.codePane.select(startMatch, endMatch);	
					DefaultHighlightPainter defhp = new DefaultHighlighter.DefaultHighlightPainter(
							Color.BLUE);
					srcWindow.dh.addHighlight(startMatch, endMatch, defhp);
				} catch (BadLocationException e) {
					e.printStackTrace();
				}
			}

			// reset settings
			srcWindow.matchCase = saveCaseSetting;
			srcWindow.matchWord = saveWordSetting;
		}
	}

	/***
	 * View RDF/XML source of current selected ontology
	 * Also highlight code fragment corresponding to current selected entity
	 * based on user preference setting
	 */
	public void viewSource(int format) {

		// display ontology source in RDF/XML
		if (swoopModel.getSelectedOntology() != null) {
			if (srcWindow != null)
				srcWindow.dispose();

			// get current preferences object from swoopModel
			// and check setting for code_highlighting
			srcWindow = new PopupOntologySource(this, swoopModel, format);
			srcWindow.displaySrcCode(swoopModel.getSelectedOntology());

			if (swoopModel.isHighlightCode()) {
				if (format == 1) { //AS 
					highlightEntityAS();
				} else { //RDF/XML
					// *** HACKY STUFF TO SELECT CODE FRAGMENT ***
					// depends on how the RDF/XML code is generated by the OWL API
					// jump to currently selected entity source code
					String entityURI = null;
					try {
						OWLNamedObject selected = swoopModel.getSelectedEntity();
						if (selected != null && selected.getURI() != null) {
							entityURI = selected.getURI().toString();
						}
					} catch (OWLException e) {
						e.printStackTrace();
					}
					if (entityURI != null) {

						// select entity defn.
						// determine type of entity
						String entityTag = "owl:Class";
						if (swoopModel.getSelectedEntity() instanceof OWLObjectProperty)
							entityTag = "owl:ObjectProperty";
						else if (swoopModel.getSelectedEntity() instanceof OWLDataProperty)
							entityTag = "owl:DatatypeProperty";
						else if (swoopModel.getSelectedEntity() instanceof OWLIndividual)
							entityTag = "rdf:Description";

						boolean saveCaseSetting = srcWindow.matchCase;
						boolean saveWordSetting = srcWindow.matchWord;
						srcWindow.matchCase = false;
						srcWindow.matchWord = false;

						// match starting position of entity definition in source code
						String find = "";
						try {
							String entityName = swoopModel.shortForm(new URI(
									entityURI));
							if (entityName.indexOf(":") >= 0)
								entityName = entityName.substring(entityName
										.indexOf(":") + 1, entityName.length());
							find = "\n<" + entityTag + " rdf:about=\"#"
									+ entityName + "\">";
						} catch (URISyntaxException e1) {
							e1.printStackTrace();
							return;
						}
						srcWindow.findMatch = find;
						int startMatch = srcWindow.matchString(true, false);

						// if match found, find ending position and select entire code fragment for selected entity
						if (startMatch != -1) {
							srcWindow.findMatch = "\n</" + entityTag + ">";
							int endMatch = srcWindow.matchString(true, false);
							endMatch += srcWindow.findMatch.length();
							try {
								// srcWindow.codePane.select(startMatch, endMatch);
								srcWindow.dh.addHighlight(startMatch, endMatch,
										srcWindow.dhp);
							} catch (BadLocationException e) {
								e.printStackTrace();
							}
						}

						// reset settings
						srcWindow.matchCase = saveCaseSetting;
						srcWindow.matchWord = saveWordSetting;
					}
				}
			}
			srcWindow.setVisible(true);
		}
	}

	public void addBookmark() {

		try {
			final String uri = swoopModel.selectedOWLObject.getURI().toString();
			if (uri == null) {
				return;
			}
//			if (bookmarks.contains(uri))
//				return;
			String name = JOptionPane.showInputDialog(this, "Specify Name:",
					"Adding Bookmark", JOptionPane.PLAIN_MESSAGE);

			if (name == null)
				return;
			Bookmark newBM = new Bookmark();
			name = name.replaceAll(" ", "_");
			newBM.setDisplayed_name(name);

			String physicalURI = null;
			try {
				physicalURI = swoopModel.getSelectedOntology().getPhysicalURI().toString();
			} catch (Exception e) {
				System.err.println("No physical URI for this ontology to bookmark, using logical URI.");
				physicalURI = JOptionPane.showInputDialog(this, "Specify Physical Location of Ontology:",
						"OWL Object Bookmark", JOptionPane.ERROR_MESSAGE);
			}
			if (physicalURI == null) return;
			final String phyURI = physicalURI;
			
			newBM.setOntology_uri(physicalURI);
			newBM.setUri(uri);
			
			bookmarks.add(newBM);
			saveBookmarks();
			JMenuItem bmItem = new JMenuItem(name);
			bookmarkMenu.add(bmItem);
			bmItem.setToolTipText(physicalURI);
			bmItem.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
//					updateAddressBar(uri);
					loader.loadURIInModel(phyURI, uri);
				}
			});
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	
	/**
	 * Simple method to load a single OWL file written in RDF/XML
	 * or in Abstract Syntax format
	 */
	private void loadOWLFile() {
		if (ontFile != null) {
			String filePath = ontFile.toURI().toString();
			
			loader.loadURIInModel(filePath, filePath);
			// add ontology to fileOntMap
			this.fileOntMap.put(ontFile.toURI(), ontFile);
			
		}
	}

	/**
	 * Load a Swoop Workspace
	 * 1. Load each ontology in the Swoop Model
	 * 2. Load uncommitted change list
	 * 3. Load committed change list
	 */
	public void loadWorkspace(boolean displaySuccess) {

		if (wkspcFile != null) {
			try {
				ObjectInputStream ins = new ObjectInputStream(
						new FileInputStream(wkspcFile));

				// read in each OWLOntology into SwoopModel
				// see Save Workspace for serialization format
				Object ontrdfList = ins.readObject();
				Object onturiList = ins.readObject();
				Object impChngList = ins.readObject();

				if (ontrdfList instanceof ArrayList) {
					List ontRDFList = (ArrayList) ontrdfList;
					List ontURIList = (ArrayList) onturiList;
					Map ontologyList = new HashMap();
					// contains a list of ontology strings all serialized as RDF/XML
					for (int i = 0; i < ontRDFList.size(); i++) {
						String rdf = ontRDFList.get(i).toString();
						String ontURI = ontURIList.get(i).toString();
						StringReader reader = new StringReader(rdf);
						OWLOntology ont = swoopModel.loadOntologyInRDF(reader,
								new URI(ontURI), false);
						ontologyList.put(ont.getURI(), ont);
					}

					// also apply imports changes (see SaveWorkspace for details)
					List importChanges = (ArrayList) impChngList;
					for (Iterator iter = importChanges.iterator(); iter.hasNext();) {
						ImportChange change = (ImportChange) iter.next();
						OWLOntology ont = (OWLOntology) ontologyList.get(change.getOntologyURI());
						OWLOntology impOnt = (OWLOntology) ontologyList.get(change.getImportedOntologyURI());
						AddImport newChange = new AddImport(ont, impOnt, null);
						newChange.accept((ChangeVisitor) ont);
					}

					// add collection of ontologies w/o notifying swoopModel
					if (ontologyList.size() > 0)
						swoopModel.addOntologies(ontologyList.values());
				}

				// load all changes
				Map changeMap = (HashMap) ins.readObject();

				//*** process based on how it was saved i.e. if the ontologychange is null, deserialize it from the rdfxml
				List uncommittedChanges = new ArrayList();
				List committedChanges = new ArrayList();
				for (Iterator iter2 = changeMap.values().iterator(); iter2
						.hasNext();) {
					ArrayList list = (ArrayList) iter2.next();
					for (Iterator iter3 = list.iterator(); iter3.hasNext();) {
						SwoopChange swc = (SwoopChange) iter3.next();
						if (swc.getChange() == null) {
							// deserialize change from rdf/xml string
							OntologyChangeRenderer ocRend = new OntologyChangeRenderer(changeLog);
							String rdfxml = swc.getRDFXML();
							OWLOntology serOnt = swoopModel.loadOntologyInRDF(
									new StringReader(rdfxml), new URI(""));
							List onlyChange = ocRend.deserializeOntologyChanges(serOnt);
							if (onlyChange.size()==0) continue;
							OntologyChange ontChange = (OntologyChange) onlyChange.iterator().next();
							// set ontologyChange object in SwoopChange
							swc.setChange(ontChange);
							// also add it to (un)committed change lists
							if (!swc.isCommitted())
								uncommittedChanges.add(ontChange);
							else
								committedChanges.add(ontChange);
						}
					}
				}
				swoopModel.getChangesCache().putChangeMap(changeMap);
				swoopModel.setUncommittedChanges(uncommittedChanges);
				swoopModel.setCommittedChanges(committedChanges);

				// load swoop annotation cache
				swoopModel.setAnnotationCache((SwoopCache) ins.readObject());

				// load annotated object uri's
				swoopModel.setAnnotatedObjectURIs((Set) ins.readObject());

				//System.out.println("SWOOP Workspace loaded from "+wkspcFile.getName());
				if (displaySuccess)
					JOptionPane.showMessageDialog(this, "Workspace File '"
							+ wkspcFile.getName() + "' Loaded Successfully",
							"File Loaded", JOptionPane.INFORMATION_MESSAGE);

				// enable menu options
				this.enableMenuOptions();
			} catch (Exception e) {
				if (displaySuccess)
					JOptionPane.showMessageDialog(this,
							"Error reading Workspace File '"
									+ wkspcFile.getName() + "'", "Load Error",
							JOptionPane.ERROR_MESSAGE);
				System.out.println("Unable to load " + wkspcFile.getName()
						+ " Workspace");
				e.printStackTrace();
			}
		}
	}

	/**
	 * Save Swoop Workspace as a .swp file
	 * 1. Save all ontologies in Swoop Model
	 * 2. Save all uncommitted changes
	 * 3. Save all committed changes
	 */
	public void saveWorkspace(boolean displaySuccess, boolean overwrite) {

		if (wkspcFile != null) {
			try {
				if (!(wkspcFile.getName().endsWith(".swp"))
						&& !(wkspcFile.getName().endsWith(".SWP"))) {
					wkspcFile = new File(wkspcFile.getAbsolutePath() + ".swp");
				}

				// prompt user if overwriting
				if (wkspcFile.exists() && overwrite) {
					int result = JOptionPane.showConfirmDialog(this, "Saving File at " + wkspcFile.getAbsolutePath() + ". Overwrite?", "Save Workspace", JOptionPane.YES_NO_OPTION);
					if (result != JOptionPane.YES_OPTION) return;
				}				
				
				// save all ontologies in workspace to disk
				// since Java serialization is not working because of the equals problem in the OWL-API
				// we now serialize each ontology into its RDF/XML string
				// and save a list of ontology strings
				// Also save a list of ontURIs with it
				List ontRDFList = new ArrayList(); // list of ontology strings (serialized as RDF/XML)
				List ontURIList = new ArrayList(); // corresponding list of ontology URIs
				ObjectOutputStream outs = new ObjectOutputStream(
						new FileOutputStream(wkspcFile));
				Iterator iter = swoopModel.getOntologies().iterator();

				// also save imported information in a separate change list
				List importChanges = new ArrayList();

				while (iter.hasNext()) {
					OWLOntology ont = (OWLOntology) iter.next();
					ontURIList.add(ont.getURI().toString());
					CorrectedRDFRenderer rend = new CorrectedRDFRenderer();
					StringWriter st = new StringWriter();
					rend.renderOntology(ont, st);
					ontRDFList.add(st.getBuffer().toString());

					// also add imports information to importChanges
					for (Iterator impOntIter = ont.getIncludedOntologies().iterator(); impOntIter.hasNext();) {
						OWLOntology impOnt = (OWLOntology) impOntIter.next();
						ImportChange change = new ImportChange(ont.getURI(),impOnt.getURI());
						importChanges.add(change);
					}
				}
				outs.writeObject(ontRDFList);
				outs.writeObject(ontURIList);

				//*** Since saving/reloading imported ontologies is funky because of the logical vs physical URI discrepancy
				// its best to save the import changes separately and during the reloading phase
				// turn OFF imports while parsing and apply the imports changes in the end
				outs.writeObject(importChanges);

				// save all changes from swoopModel
				Map changeMap = swoopModel.getChangesCache().getChangeMap();
				Map tempChangeMap = new HashMap();
				//*** while saving changes, you need to remove OntologyChange objects where problematic
				// and serialize them into RDF/XML (this is applicable for NON CHECKPOINT changes)
				for (Iterator iter2 = changeMap.values().iterator(); iter2
						.hasNext();) {
					ArrayList list = (ArrayList) iter2.next();
					for (Iterator iter3 = list.iterator(); iter3.hasNext();) {
						SwoopChange swc = (SwoopChange) iter3.next();
						if (!swc.isCheckpointRelated()) {
							// serialize into RDF/XML if it hasnt been generated already
							if (swc.getRDFXML() == null
									|| swc.getRDFXML().equals("")) {
								OntologyChangeRenderer ocRend = new OntologyChangeRenderer(changeLog);
								List onlyChange = new ArrayList();
								onlyChange.add(swc.getChange());
								OWLOntology serOnt = ocRend.serializeOntologyChanges(onlyChange);
								CorrectedRDFRenderer rdfRend = new CorrectedRDFRenderer();
								StringWriter st = new StringWriter();
								try {
									rdfRend.renderOntology(serOnt, st);
								} catch (Exception e) {
									e.printStackTrace();
								}
								swc.setRDFXML(st.toString());
							}
							// nullify ontology change TEMPORARILY
							OntologyChange ch = swc.getChange();
							swc.setChange(null);
							tempChangeMap.put(swc, ch);							
						}
					}
				}
				outs.writeObject(changeMap);
				// restore tempChangeMap
				for (Iterator t = tempChangeMap.keySet().iterator(); t.hasNext();) {
					SwoopChange swc = (SwoopChange) t.next();
					swc.setChange((OntologyChange) tempChangeMap.get(swc));
				}
				
				// write swoop annotation cache
				outs.writeObject(swoopModel.getAnnotationCache());

				// write annotated object uri's
				outs.writeObject(swoopModel.getAnnotatedObjectURIs());

				//System.out.println("SWOOP Workspace saved in "+wkspcFile.getName());
				if (displaySuccess)
					JOptionPane.showMessageDialog(this, "Workspace File '"
							+ wkspcFile.getName() + "' Saved Successfully",
							"File Saved", JOptionPane.INFORMATION_MESSAGE);
				outs.close();
			} catch (Exception e) {
				JOptionPane.showMessageDialog(this,
						"Error writing Workspace File '" + wkspcFile.getName()
								+ "'", "Save Error", JOptionPane.ERROR_MESSAGE);
				e.printStackTrace();
			}
		}
	}

	/**
	 * Load Swoop Ontology File
	 * 1. Load Java Object Ontology and add it in SwoopModel
	 * 2. Load uncommitted changes
	 * 3. Load committed changes
	 */
	private void loadOntologyFile() {

		if (ontFile != null) {
			try {
				ObjectInputStream ins = new ObjectInputStream(
						new FileInputStream(ontFile));
				List ontologyList = (ArrayList) ins.readObject();
				System.out.println("SWOOP Ontology loaded from "
						+ ontFile.getName());

				// list contains a pair of items - ontology serialized in RDF/XML and its URI
				StringReader reader = new StringReader(ontologyList.get(0).toString());
				URI ontURI = new URI(ontologyList.get(1).toString());
				OWLOntology ont = swoopModel.loadOntologyInRDF(reader, ontURI);
				// add ontology to fileOntMap
				this.fileOntMap.put(ont.getPhysicalURI(), ontFile);
				// add ontology to swoopmodel
				swoopModel.addOntology(ont);

				// load all ontology changes
				List ontChanges = (ArrayList) ins.readObject();
				//*** process based on how it was saved i.e. if the ontologychange is null, deserialize it from the rdfxml
				List uncommittedChanges = new ArrayList(swoopModel.getUncommittedChanges());
				List committedChanges = new ArrayList(swoopModel.getCommittedChanges());
				for (int i = 0; i < ontChanges.size(); i++) {
					SwoopChange swc = (SwoopChange) ontChanges.get(i);
					if (swc.getChange() == null) {
						// deserialize change from rdf/xml string
						OntologyChangeRenderer ocRend = new OntologyChangeRenderer(changeLog);
						OWLOntology serOnt = swoopModel.loadOntologyInRDF(
								new StringReader(swc.getRDFXML()), new URI(""));
						List onlyChange = ocRend.deserializeOntologyChanges(serOnt);
						OntologyChange ontChange = (OntologyChange) onlyChange.iterator().next();
						// set ontologyChange object in SwoopChange
						swc.setChange(ontChange);
						// also add it to (un)committed change lists
						if (!swc.isCommitted())
							uncommittedChanges.add(ontChange);
						else
							committedChanges.add(ontChange);
					}
				}
				swoopModel.getChangesCache().putAllChanges(ontChanges);
				swoopModel.setUncommittedChanges(uncommittedChanges);
				swoopModel.setCommittedChanges(committedChanges);

				// load swoop annotation cache
				swoopModel.setAnnotationCache((SwoopCache) ins.readObject());

				// load annotated object uri's
				swoopModel.setAnnotatedObjectURIs((Set) ins.readObject());

				JOptionPane.showMessageDialog(this, "Ontology File '"
						+ ontFile.getName() + "' Loaded Successfully",
						"File Loaded", JOptionPane.INFORMATION_MESSAGE);

				// enable menu options
				this.enableMenuOptions();

			} catch (Exception e) {
				JOptionPane.showMessageDialog(this,
						"Error reading Ontology File '" + ontFile.getName()
								+ "'", "Load Error", JOptionPane.ERROR_MESSAGE);
				e.printStackTrace();
			}
		}
	}

	/**
	 * Save current ontology in Abstract Syntax format
	 * File extension (*.txt)
	 */
	private void saveAbstractSyntaxFile() {

		if (ontFile != null) {
			try {

				// save current selected ontology to disk
				
				// prompt user?
				int result = JOptionPane.showConfirmDialog(this, "Saving File at " + ontFile.getAbsolutePath() + ". Overwrite?", "Save Ontology", JOptionPane.YES_NO_OPTION);
				if (result != JOptionPane.YES_OPTION) return;
				
				// in Abstract Syntax format
				StringWriter st = new StringWriter();

				org.semanticweb.owl.io.abstract_syntax.Renderer absRenderer = new org.semanticweb.owl.io.abstract_syntax.Renderer();
				absRenderer
						.renderOntology(swoopModel.getSelectedOntology(), st);

				this.saveOntologyAndCleanup(st);

			} catch (Exception e) {
				JOptionPane.showMessageDialog(this,
						"Error writing Ontology File '" + ontFile.getName()
								+ "'", "Save Error", JOptionPane.ERROR_MESSAGE);
				e.printStackTrace();
			}
		}
	}

	/**
	 * Save ontology string (in either RDF or txt) to file
	 * Perform cleanup - remove ontology from SwoopModel dirty set
	 * Repaint lists and update trees
	 * Reset committed change log
	 * @param st - StringWriter containing ontology string
	 * @throws IOException
	 */
	private void saveOntologyAndCleanup(StringWriter st) throws IOException {
		OutputStream fileStream = new FileOutputStream(ontFile);
		Writer writer = new OutputStreamWriter(fileStream, Charset
				.forName("UTF-8"));
		writer.write(st.toString());
		writer.close();

		// reset all change log in swoopModel
		swoopModel.setCommittedChanges(new ArrayList());

		JOptionPane.showMessageDialog(this, "Ontology File '"
				+ ontFile.getName() + "' Saved Successfully", "File Saved",
				JOptionPane.INFORMATION_MESSAGE);
	}

	/**
	 * Save Ontology in OWL (in RDF/XML format) locally
	 *
	 */
	private void saveOWLFile() {

		if (ontFile != null) {
			try {
				// save current selected ontology to disk
				
				// prompt user if overwriting
				if (ontFile.exists()) {
					int result = JOptionPane.showConfirmDialog(this, "Saving File at " + ontFile.getAbsolutePath() + ". Overwrite?", "Save Ontology", JOptionPane.YES_NO_OPTION);
					if (result != JOptionPane.YES_OPTION) return;
				}
				
				// save reference in fileOntMap
				this.fileOntMap.put(swoopModel.getSelectedOntology().getPhysicalURI(), ontFile);
				
				// in RDF/XML
				StringWriter st = new StringWriter();
				CorrectedRDFRenderer rdfRenderer = new CorrectedRDFRenderer();
				rdfRenderer.renderOntology(swoopModel.getSelectedOntology(), st);

				this.saveOntologyAndCleanup(st);
								
			} 
			catch (Exception e) {
				JOptionPane.showMessageDialog(this,
						"Error writing Ontology File '" + ontFile.getName()
								+ "'", "Save Error", JOptionPane.ERROR_MESSAGE);
				e.printStackTrace();
			}
		}
	}

	/**
	 * Advantage of using save SWOOP Ontology Object file is
	 * a) Reloading the saved ontology is faster because the
	 * RDF/XML does not have to be parsed again (also, some errors may occur when parsing local RDF/XML files due to bugs in OWL API)
	 * b) Additional parameters of SWOOP Model such as ontology changes are saved as well
	 *
	 */
	private void saveOntologyFile() {

		if (ontFile != null) {
			try {

				if (!(ontFile.getName().endsWith(".swo"))
						&& !(ontFile.getName().endsWith(".SWO"))) {
					ontFile = new File(ontFile.getAbsolutePath() + ".swo");
				}

				// prompt user if overwriting
				if (ontFile.exists()) {
					int result = JOptionPane.showConfirmDialog(this, "Saving File at " + ontFile.getAbsolutePath() + ". Overwrite?", "Save Ontology", JOptionPane.YES_NO_OPTION);
					if (result!= JOptionPane.YES_OPTION) return;
				}
				
				// save current selected ontology to disk
				ObjectOutputStream outs = new ObjectOutputStream(
						new FileOutputStream(ontFile));
				OWLOntology ont = swoopModel.getSelectedOntology();
				CorrectedRDFRenderer rend = new CorrectedRDFRenderer();
				StringWriter st = new StringWriter();
				rend.renderOntology(ont, st);
				List ontologyList = new ArrayList();
				ontologyList.add(st.getBuffer().toString());
				ontologyList.add(ont.getURI().toString());
				outs.writeObject(ontologyList);

				// save all changes related to ontology
				List ontChanges = new ArrayList(swoopModel.getChangesCache().getChangeList(ont.getURI()));
				Map tempChangeMap = new HashMap();
				//*** while saving changes, you need to remove OntologyChange objects where problematic
				// and serialize them into RDF/XML (this is applicable for NON CHECKPOINT changes)
				for (int i = 0; i < ontChanges.size(); i++) {
					SwoopChange swc = (SwoopChange) ontChanges.get(i);
					if (!swc.isCheckpointRelated()) {

						// serialize into RDF/XML if it hasnt been generated already
						if (swc.getRDFXML().equals("")) {
							OntologyChangeRenderer ocRend = new OntologyChangeRenderer(
									changeLog);
							List onlyChange = new ArrayList();
							onlyChange.add(swc.getChange());
							OWLOntology serOnt = ocRend
									.serializeOntologyChanges(onlyChange);
							CorrectedRDFRenderer rdfRend = new CorrectedRDFRenderer();
							st = new StringWriter();
							try {
								rdfRend.renderOntology(serOnt, st);
							} catch (Exception e) {
								e.printStackTrace();
							}
							swc.setRDFXML(st.toString());
						}
						// nullify ontology change TEMPORARILY
						OntologyChange ch = swc.getChange();
						swc.setChange(null);
						tempChangeMap.put(swc, ch);
					}
				}
				outs.writeObject(ontChanges);
				// restore tempChangeMap
				for (Iterator t = tempChangeMap.keySet().iterator(); t.hasNext();) {
					SwoopChange swc = (SwoopChange) t.next();
					swc.setChange((OntologyChange) tempChangeMap.get(swc));
				}
				
				// write swoop annotation cache
				outs.writeObject(swoopModel.getAnnotationCache());

				// write annotated object uri's
				outs.writeObject(swoopModel.getAnnotatedObjectURIs());

				//System.out.println("SWOOP Ontology saved in "+ontFile.getName());
				JOptionPane.showMessageDialog(this, "Ontology File '"
						+ ontFile.getName() + "' Saved Successfully",
						"File Saved", JOptionPane.INFORMATION_MESSAGE);

			} catch (Exception e) {
				JOptionPane.showMessageDialog(this,
						"Error writing Ontology File '" + ontFile.getName()
								+ "'", "Save Error", JOptionPane.ERROR_MESSAGE);
				e.printStackTrace();
			}
		}
	}

	/**
	 * Save Bookmarks locally on shutdown
	 */
	private void saveBookmarks() {

		try {
			StringWriter bufStream = new StringWriter();
			for (int i = 0; i < bookmarks.size(); i++) {
				Bookmark bm = (Bookmark) bookmarks.get(i);
				bufStream.write(bm.getDisplayed_name() + " " + bm.getUri()
						+ " " + bm.getOntology_uri());
				bufStream.write(System.getProperty("line.separator"));
			}
			bufStream.close();
			swoopModel.saveBookmarks(bufStream.toString());
		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}

	/**
	 * Load bookmarks from the local file "Favorites.txt"
	 *
	 */
	private void loadBookmarkFile() {
		try {
			// load bookmarks from favorites.txt (lowercase!)
			// remember that case matters for Unix systems
			bookmarks = new ArrayList();
			int ctr = 0;
			BufferedReader bufStream = swoopModel.getBookmarks();
			String temp = "";
			while ((temp = bufStream.readLine()) != null) {
				String bmLine = temp;
				if (bmLine.trim().equals(""))
					continue;
				if (bmLine.startsWith("%%"))
					continue;
				int pos1 = bmLine.indexOf(" ");
				int pos2 = bmLine.indexOf(" ", pos1 + 1);
				int pos3 = bmLine.length();
				if (pos1 == -1 || pos2 == -1 || pos2 == pos3 || pos1 == pos3)
					continue;

				Bookmark newBM = new Bookmark();
				newBM.setDisplayed_name(bmLine.substring(0, bmLine.indexOf(" ")));
				newBM.setUri(bmLine.substring(bmLine.indexOf(" ") + 1, bmLine.lastIndexOf(" ")));
				newBM.setOntology_uri(bmLine.substring(
						bmLine.lastIndexOf(" ") + 1, bmLine.length()));

				bookmarks.add(ctr++, newBM);
			}
			// now add bookmarks to menu
			this.addBookmarksMenu();
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	/*
	 * Add Bookmarks list to "Bookmarks" Menu
	 */
	private void addBookmarksMenu() {

		try {
			bookmarkMenu.removeAll();
			bookmarkMenu.add(addBookmarkMenu);
			bookmarkMenu.add(remBookmarkMenu);
			bookmarkMenu.add(sortBookmarkMenu);
			bookmarkMenu.addSeparator();
			
			for (Iterator iter = bookmarks.iterator(); iter.hasNext();) {
				Bookmark newBM = (Bookmark) iter.next();
				final String loadURI = newBM.getOntology_uri();
				final String bmURI = newBM.getUri();

				JMenuItem bmItem = new JMenuItem(newBM.getDisplayed_name());
				bookmarkMenu.add(bmItem);
				bmItem.setToolTipText(newBM.getOntology_uri());
				bmItem.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						loader.loadURIInModel(loadURI, bmURI);
					}
				});
			}
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	/*
	 * Sort bookmarks alphabetically based on displayed name
	 */
	private void sortBookmarks() {
		SortedSet sortedBM = new TreeSet(BookmarkComparator.INSTANCE);
		for (Iterator iter = bookmarks.iterator(); iter.hasNext();) {
			sortedBM.add(iter.next());
		}
		bookmarks.clear();
		for (Iterator iter = sortedBM.iterator(); iter.hasNext();) {
			bookmarks.add(iter.next());
		}
		saveBookmarks();
	}
	
	/*
	 * Remove a bookmark
	 */
	private void removeBookmark() {
		
		Object sel = JOptionPane.showInputDialog(this, null, "Select Bookmark to Remove", JOptionPane.QUESTION_MESSAGE, null, bookmarks.toArray(), bookmarks.get(0));
		if (sel!=null) {
			bookmarks.remove(sel);
//			List temp = new ArrayList();
//			for (int j=0; j<bookmarks.size(); j++) {
//				Bookmark bm = (Bookmark) bookmarks.get(j);
//				boolean match = false;
//				for (int i=0; i<list.getSelectedValues().length; i++) {
//					Bookmark sel = (Bookmark) list.getSelectedValues()[i];
//					if (bm.equals(sel)) match = true;
//				}
//				if (!match) temp.add(bm);
//			}
//			bookmarks = temp;			
			this.addBookmarksMenu();
			saveBookmarks();
		}
	}
	
	/**
	 * Default Bookmarks in Java Webstart version
	 *
	 */
	private void defaultBookmarksForWS() {

		final String[][] defaultBookmarks = {
				{ "AKT-Portal",
						"AKT-Portal http://www.aktors.org/ontology/portal" },
				{ "Congo Web Service",
						"http://www.daml.org/services/owl-s/1.0/CongoService.owl" },
				{ "FLA",
						"http://www.flacp.fujitsulabs.com/tce/ontologies/2004/03/object.owl" },
				{ "FOAF", "http://xmlns.com/foaf/0.1/index.rdf" },
				{ "Galen", "http://www.mindswap.org/ontologies/galen.owl" },
				{ "Koala",
						"http://protege.stanford.edu/plugins/owl/owl-library/koala.owl" },
				//						 {"Mad Cow", "http://www.cs.man.ac.uk/~horrocks/OWL/Ontologies/mad_cows.owl"},
				{ "Mindswappers",
						"http://www.mindswap.org/2004/owl/mindswappers" },
				{ "Pizza",
						"http://www.co-ode.org/ontologies/pizza/pizza_20041007.owl" },
				{ "SWEET-JPL",
						"http://sweet.jpl.nasa.gov/ontology/earthrealm.owl" },
				{ "Tambis",
						"http://www.mindswap.org/ontologies/tambis-full.owl" },
				{ "Wine", "http://www.w3.org/2001/sw/WebOnt/guide-src/wine.owl" }

		};

		for (int i = 0; i < defaultBookmarks.length; i++) {
			JMenuItem bmItem = new JMenuItem(defaultBookmarks[i][0]);
			bmItem.setToolTipText(defaultBookmarks[i][1]);
			final int ctr = i;
			bmItem.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					loader.loadURIInModel(defaultBookmarks[ctr][1],
							defaultBookmarks[ctr][1]);
				}
			});
			bookmarkMenu.add(bmItem);
		}

	}

	/**
	 * This critical method updates the views for the ontology along w/ the class/property
	 * trees and alphabetic list and selects the entity if any. 
	 * It must be called after the ontology/entity to be updated are set as SwoopModel.selectedOntology/Entity
	 * and just after SwoopModel.loadOntSettings(..) is carried out in which the reasoner 
	 * classifies the new ontology.
	 *
	 */
	public void updateOntologyViews() {

		try {
			System.out.println("Ontology selection changed to: "
					+ swoopModel.getSelectedOntology().getURI());
			// need to revert UI settings based on change in per-ontology-setting preference
			termDisplay.revertUIOntologySettings();
		} catch (OWLException e) {
			e.printStackTrace();
		}

		SwoopProgressDialog progress = new SwoopProgressDialog(this, "Rendering Ontology...");
		progress.show();
		
		long startTime = Calendar.getInstance().getTimeInMillis();

		OntologyDisplay od = ontDisplay;
		TermsDisplay td = termDisplay;

		// if swoopModel.selectedEntity==null, it implies
		// this method has been called with the intention
		// of finally displaying ontology (not the entity)
		if (swoopModel.selectedEntity == null) {
			od.displayOntology();
			displayOntologyPane();
		}

		// update both displays to be safe (?!)
		td.updateTreeDisplay();
		td.updateListDisplay();

		// finally, if swoopModel.selectedEntity != null
		// render term description in entity pane
		if (swoopModel.selectedEntity != null) {
			
			// select ontology in list (turn off listeners)
			od.simplySelectOntology(swoopModel.selectedOntology);
			
			try {
				td.displayFoundEntity(
						swoopModel.selectedOntology,
						swoopModel.selectedEntity);
			} catch (OWLException e) {
				
				e.printStackTrace();
			}
			// td.displayTerm();
		}

		repaint();
		
		long endTime = Calendar.getInstance().getTimeInMillis();
		System.out.println("rendering views: "
				+(endTime - startTime)+" milliseconds");
	
		progress.dispose();

	}

	public void windowOpened(WindowEvent arg0) {
	}

	public void windowClosing(WindowEvent arg0) {

		SwoopClose();
	}

	public void windowClosed(WindowEvent arg0) {
	}

	public void windowIconified(WindowEvent arg0) {
	}

	public void windowDeiconified(WindowEvent arg0) {
	}

	public void windowActivated(WindowEvent arg0) {
	}

	public void windowDeactivated(WindowEvent arg0) {
	}

	/**
	 * Method called just before Swoop is shutdown.
	 * Prompts the user to save current workspace
	 * Saves the current user preferences
	 * Finally, if preference for "Open with Last Workspace" is ON,
	 * saves the current workspace OR "default.swp"
	 *
	 */
	private void SwoopClose() {

		if (!Swoop.isWebStart()) {
			
			// save boomarks file
			this.saveBookmarks();
			
			// popup save request
			JCheckBox savePrefsChk = new JCheckBox("Always ask on closing");
			savePrefsChk.setFont(tahoma);
			int result = JOptionPane
					.showConfirmDialog(
							this,
							new Object[] { "Do you want to save the current SWOOP workspace?" },
							//savePrefsChk}, 
							"Closing SWOOP", JOptionPane.YES_NO_CANCEL_OPTION);

			if (result == JOptionPane.YES_OPTION) {
				// save workspace
				if (wkspcFile != null)
					saveWorkspace(true, true);
				else
					saveAsFile(true, false);
			}

			if (result != JOptionPane.CANCEL_OPTION) {
				// save preferences to disk just before closing
				// pass wkspcFile to swoopModel before saving preferences
				if (wkspcFile == null)
					wkspcFile = new File("defaultWkspc.swp");
				swoopModel.setWkspcFile(wkspcFile);
				swoopModel.savePreferences();
				// also check if option "open last workspace" is ON
				//	            	if (swoopModel.isOpenLastWorkspace()) {
				//	            		saveWorkspace(false);
				//	            	}

				dispose();
				System.exit(0);
			} else
				return;
		} else {
			// webstart launched
			dispose();
			swoopModel.savePreferences();
			System.exit(0);
		}
	}

	public void disableUIListeners() {
		//		for (int safe=0; safe<5; safe++);
		//addrCombo.removeActionListener(this);
	}

	public void enableUIListeners() {
		//this.disableUIListeners();
		//addrCombo.addActionListener(this);
	}

	private JLabel createLabel(String text) {
		JLabel lbl = new JLabel(text);
		lbl.setFont(tahoma);
		return lbl;
	}

	public void flyTheMothership()
	{
		try {
			OWLOntology ont = swoopModel.getSelectedOntology();
			if (ont == null) {
				JOptionPane.showMessageDialog(null,
						"No ontology selected");
				return;
			}
			Vector vec = new Vector();
			vec.add(ont);

			MotherShipFrame frame = new MotherShipFrame( swoopModel, vec, ont.getURI() );
		} catch (Exception e) 
		{ e.printStackTrace(); }
	}
	
/*	
	public void showToldClassAxioms()
	{
		try {
			OWLOntology ont = swoopModel.getSelectedOntology();
			if (ont == null) {
				JOptionPane.showMessageDialog(null,
						"No ontology selected");
				return;
			}
			AxiomExtractor extractor = new AxiomExtractor( ont, swoopModel, swoopModel.getReasoner() );
			Vector axioms = extractor.extractSubclassAxioms( false );
			AxiomList view = new AxiomList( "Told Axioms for " + swoopModel.shortForm( ont.getLogicalURI() ), axioms );
			
		} catch (Exception e) 
		{ e.printStackTrace(); }
	}
*/
	
	public void showInferredClassAxioms()
	{
		try {
			OWLOntology ont = swoopModel.getSelectedOntology();
			if (ont == null) {
				JOptionPane.showMessageDialog(null,
						"No ontology selected");
				return;
			}
			AxiomIndexer indexer = new AxiomIndexer( ont, swoopModel, swoopModel.getReasoner() );
			indexer.index();
			indexer.print();
			indexer.view();
			/*
			AxiomExtractor extractor = new AxiomExtractor( ont, swoopModel, swoopModel.getReasoner() );
			Vector axioms = extractor.extractClassAxioms(  );
			AxiomList view = new AxiomList( "Inferred Axioms for " + swoopModel.shortForm( ont.getLogicalURI() ), axioms );
			*/
			
		} catch (Exception e) 
		{ e.printStackTrace(); }
	}
	
	
	
	public void modelChanged(ModelChangeEvent event) {
		
		try {
			if (event.getType() == ModelChangeEvent.DEBUGGING_CHANGED) {
				this.tableauSOSMenu.setSelected(!swoopModel.isUseTableau());
				this.findAllMUPSMenu.setSelected(swoopModel.isFindAllMUPS());
//				this.sosMenu.setEnabled(swoopModel.isDebugGlass());
			}
			else
			if (event.getType() == ModelChangeEvent.ONT_STATS_CHANGED) {
				// change save-file name depending on ontology selection
				if (swoopModel.getSelectedOntology()!=null) {
					OWLOntology ont = swoopModel.getSelectedOntology();
					if (this.fileOntMap.get(ont.getPhysicalURI())!=null) {
						File f = (File) this.fileOntMap.get(ont.getPhysicalURI());
						String fs = "\\"+f.getName(); //f.getAbsolutePath();
//						if (fs.indexOf("\\")>=0) fs = fs.substring(fs.lastIndexOf("\\")+1, fs.length());
						ontSaveMItem.setText("Ontology (.."+fs+")");
					}
					else ontSaveMItem.setText("Ontology");
				}
			}
			if (swoopModel.getOntologies().size() == 0) {
				disableMenuOptions();
			} else {
				enableMenuOptions();
			}
		}
		catch (OWLException ex) {
			ex.printStackTrace();
		}
	}
	
	/*
	 * Export Ontology in HTML format
	 */
	public void exportHTML(OWLObject obj) {
		
		try {
			JFileChooser wrapChooser = new JFileChooser();
			wrapChooser.addChoosableFileFilter(new SwoopHTMLFileFilter());
			int returnVal = wrapChooser.showSaveDialog(this);
			File file = null;
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				file = wrapChooser.getSelectedFile();
			} else { // save cancelled
				return;
			}
			
			if (file.exists()) {
				// overwrite prompt
				int result = JOptionPane.showConfirmDialog(this, "Saving File at " + file.getAbsolutePath() + ". Overwrite?", "Save HTML", JOptionPane.YES_NO_OPTION);
				if (result != JOptionPane.YES_OPTION) return;
			}
			
			// render HTML for ontology or individual entity
			String html = getObjectHTML(obj);
			
			FileWriter fw = new FileWriter(file);
			fw.write(html);
			fw.close();
			System.out.println("Save HTML file at: "+file.getAbsolutePath());
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	// returns HTML for an OWL Object
	public String getObjectHTML(OWLObject obj) {
		String html = "";
		try {
			if (obj instanceof OWLOntology) {
				OWLOntology ont = (OWLOntology) obj;
				html = ((JEditorPane) ontDisplay.editors.get(ontDisplay.ontDescTab.getSelectedIndex())).getText();
				html = html.substring(0, html.lastIndexOf("</body>"));
				// how about adding concise format for all entities in ontology!
				ConciseFormatEntityRenderer cfRend = (ConciseFormatEntityRenderer) termDisplay.renderers.get(0);
				List entities = new ArrayList();
				entities.addAll(ont.getClasses());
				entities.addAll(ont.getDataProperties());
				entities.addAll(ont.getObjectProperties());
				entities.addAll(ont.getIndividuals());
				for (Iterator iter = entities.iterator(); iter.hasNext();) {
					OWLEntity ent = (OWLEntity) iter.next();
					StringWriter st = new StringWriter();
					cfRend.render(ent, swoopModel, st);
					String uri = swoopModel.shortForm(ent.getURI());
					if (uri.indexOf(":")>=0) uri = uri.substring(uri.indexOf(":")+1, uri.length());
					// add anchors in html for local referencing
					html += "<a name=\""+uri+"\"></a>"+st.toString()+"<hr>";
				}
				html += "</body></html>";
				
				// make all uri hrefs local
				Set uris = OntologyHelper.allURIs(ont);
				for (Iterator iter = uris.iterator(); iter.hasNext();) {
					URI uri = (URI) iter.next();
					String uriStr = swoopModel.shortForm(uri);
					if (uriStr.indexOf(":")>=0) uriStr = uriStr.substring(uriStr.indexOf(":")+1, uriStr.length());
					String href = "<a href=\""+uri+"\">";
					html = html.replaceAll(href, "<a href=\"#"+uriStr+"\">");
				}
			}
			else {
				html = ((JEditorPane) termDisplay.editors.get(termDisplay.termDisplayPane.getSelectedIndex())).getText();
			}
	//		System.out.println(html);
		}
		catch (OWLException ex) {
			ex.printStackTrace();
		}
		return html;
	}
}