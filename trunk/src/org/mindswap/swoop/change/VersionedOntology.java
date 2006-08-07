/*
 * Created on Apr 9, 2005
 *
 */
package org.mindswap.swoop.change;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.Vector;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.SpringLayout;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreePath;

import org.mindswap.swoop.ModelChangeEvent;
import org.mindswap.swoop.SwoopModel;
import org.mindswap.swoop.SwoopModelListener;
import org.mindswap.swoop.annotea.Annotea;
import org.mindswap.swoop.annotea.AnnoteaClient;
import org.mindswap.swoop.annotea.Description;
import org.mindswap.swoop.renderer.ontology.OntologyListRenderer;
import org.mindswap.swoop.treetable.JTreeTable;
import org.mindswap.swoop.utils.change.BooleanElementChange;
import org.mindswap.swoop.utils.change.EnumElementChange;
import org.mindswap.swoop.utils.owlapi.CorrectedRDFRenderer;
import org.mindswap.swoop.utils.ui.ChangeComparator;
import org.mindswap.swoop.utils.ui.OntologyComparator;
import org.mindswap.swoop.utils.ui.SpringUtilities;
import org.mindswap.swoop.utils.ui.SwoopIcons;
import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLEntity;
import org.semanticweb.owl.model.OWLException;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.change.ChangeVisitor;
import org.semanticweb.owl.model.change.OntologyChange;
import org.semanticweb.owl.model.helper.OWLBuilder;

/**
 * @author Aditya
 *
 */
public class VersionedOntology extends JPanel implements ActionListener, KeyListener, TreeSelectionListener, HyperlinkListener, MouseListener, SwoopModelListener {
	
	// constants
	public static int ANNOTATED_CHANGES = 0;
	public static String serverURL = "http://www.mindswap.org/2004/annotea/Annotation";
	public static int REDUNDANT_CHANGE = 1;
	public static int REPOSITORY_CHANGE = 2;
	public static int LOCAL_CHANGE = 3;
	public static String separator = "SWOOP-ANNOTATED-CHANGE-SET";
	
	AnnoteaClient client;
	OWLClass annotType;
	SwoopModel swoopModel;
	VersionControl controlHandler;
	
	// repository info
	URI repositoryURI;
	String repositoryAuthor;
	String reposityCreatedDate;
	public URI baseOntologyURI;
	public int headVersionNumber;
	
	// versioning info	
	List repChanges; // only corresponds to new changes to be added to repository, (those already at the repository are directly added to the tree)
	TreeTableNode repRoot; // root of repChange tree
	TreeTableNode newCommitNode; // new commit being made
	int versioningFormat;
	
	// for caching purposes
	URL repHeaderLoc; // actual URL location of repository header annotation
	TreeTableNode[] versionNodes; // array of version commit TreeTableNodes
	Description[] versionDescriptions; // array of version commit Descriptions
									   // 0 - rep. header Description, 1..headVersionNum: version Descriptions
	// swoop ontology info
	OWLOntology swoopOnt, syncOntology;
	URI swoopOntURI;
	List ontChanges;
	JComboBox ontBox;
	
	// UI info 
	Font tahoma = new Font("Tahoma", Font.PLAIN, 11);
	final String[] cNames  = {"Author", "Description", "Date", "Entity"};
	JTextField swoopOntFld;
	JLabel swoopOntLbl;
	JRadioButton existingOntRadio, newOntRadio;
	JButton refreshBtn, swoUpdateBtn, swoCommitBtn, swoDeleteBtn, swoSelAllBtn, swoTestBtn, swoFilterBtn;
	JSplitPane splitPane, ontTablePane, repTablePane;
	JCheckBox advancedChk;
	boolean advanced = true;
	JPanel leftPanel, rightPanel;
	
	JTextField repAuthorFld, repDateFld, repURLFld, repBaseOntFld;
	JComboBox repTypeBox;
	JTreeTable ontChangeTT, repChangeTT;
	JEditorPane ontChangeEdPane, repChangeEdPane;
	JButton repUpdateBtn, repCommitBtn, repDeleteBtn, repSelAllBtn, repTestBtn, repFilterBtn;		
	
	JButton moveRBtn, moveLBtn;
	JPopupMenu swoFilterMenu, repFilterMenu;
	JMenuItem swoRedFMenu, swoRepFMenu, swoLocFMenu, repRedFMenu;
	
	public boolean DEBUG = true;
	JLabel statusBar;
	int splitPaneDivPos;
	
	// cache info
	Map nodeMap;
	
	public VersionedOntology(SwoopModel swoopModel, VersionControl controlHandler) {
		
		init(swoopModel, controlHandler);		
	}
	
	// another constructor which accepts a preloaded versionDescriptions[] array and
	// parses it to obtain all saved versioning info
	public VersionedOntology(SwoopModel swoopModel, VersionControl controlHandler, URI repURI, Description[] verDesc) {

		init(swoopModel, controlHandler);
		
		this.repositoryURI = repURI;
		this.versionDescriptions = verDesc;
		// parse version Description array
		try {
			// first parse header
			Description header = versionDescriptions[0];

			// parse header description to get author, date, baseOntologyURI
			this.repositoryAuthor = header.getAuthor();
			this.reposityCreatedDate = header.getCreated();
			this.baseOntologyURI = new URI(header.getAnnotatedEntityDefinition());
			// and headVersionNumber
			this.headVersionNumber = Integer.parseInt(header.getBody());
			
			// also get actual URL location of annotation
			this.repHeaderLoc = header.getLocation();
			
			// set UI accordingly
			this.repURLFld.setText(this.repositoryURI.toString());
			this.repAuthorFld.setText(this.repositoryAuthor);
			this.repDateFld.setText(this.reposityCreatedDate);
			this.repBaseOntFld.setText(this.baseOntologyURI.toString());
			this.toggleRepOptions(false);
		
			// now for each version, parse and add node to repRoot
			for (int ctr=1; ctr<=this.headVersionNumber; ctr++) {
				Description version = versionDescriptions[ctr];
				TreeTableNode mainNode = this.parseSingleCommit(version);
				// set params on mainNode
				mainNode.swoopChange.isOnRepository = true;
				mainNode.location = version.getLocation();
				versionNodes[ctr] = mainNode;
			}
			// again set UI accordingly
			this.refreshRepTreeTable(true);
			
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	/*
	 * Simple initialization of this class
	 */
	private void init(SwoopModel swoopModel, VersionControl controlHandler) {
		
		this.swoopModel = swoopModel;
		this.swoopModel.addListener(this);
		this.controlHandler = controlHandler;
		this.versionNodes = new TreeTableNode[99999];
		this.versionDescriptions = new Description[99999];
		this.nodeMap = new HashMap();
		
		this.setupUI();
		this.refreshOntBox();
		this.setupFilterPopupMenus();
		this.toggleAdvanced(false);
	}
	
	/**
	 * @return Returns the headVersionNumber.
	 */
	public int getHeadVersionNumber() {
		return headVersionNumber;
	}
	/**
	 * @param headVersionNumber The headVersionNumber to set.
	 */
	public void setHeadVersionNumber(int headVersionNumber) {
		this.headVersionNumber = headVersionNumber;
	}
	/**
	 * @return Returns the repositoryAuthor.
	 */
	public String getRepositoryAuthor() {
		return repositoryAuthor;
	}
	/**
	 * @param repositoryAuthor The repositoryAuthor to set.
	 */
	public void setRepositoryAuthor(String repositoryAuthor) {
		this.repositoryAuthor = repositoryAuthor;
	}
	/**
	 * @return Returns the repositoryURL.
	 */
	public URI getRepositoryURL() {
		return repositoryURI;
	}
	/**
	 * @param repositoryURL The repositoryURL to set.
	 */
	public void setRepositoryURL(URI repositoryURL) {
		this.repositoryURI = repositoryURL;
	}
	/**
	 * @return Returns the reposityCreatedDate.
	 */
	public String getReposityCreatedDate() {
		return reposityCreatedDate;
	}
	/**
	 * @param reposityCreatedDate The reposityCreatedDate to set.
	 */
	public void setReposityCreatedDate(String reposityCreatedDate) {
		this.reposityCreatedDate = reposityCreatedDate;
	}
	/**
	 * @return Returns the versioningFormat.
	 */
	public int getVersioningFormat() {
		return versioningFormat;
	}
	/**
	 * @param versioningFormat The versioningFormat to set.
	 */
	public void setVersioningFormat(int versioningFormat) {
		this.versioningFormat = versioningFormat;
	}
	
	private void setupUI() {
		
		// setup border
		Border border = new EtchedBorder();
		
		// explorer style interface - left: swoop ontology, right: repository ontology
		// LEFT PANEL
		// setup ontology panel
		swoopOntLbl = (JLabel) this.initComponent(JLabel.class, "Specify Swoop Ontology");
		swoopOntFld = (JTextField) this.initComponent(JTextField.class, "");
		ontBox = new JComboBox();
		ontBox.setRenderer(new OntologyListRenderer(swoopModel));
		existingOntRadio = (JRadioButton) this.initComponent(JRadioButton.class, "Existing: ");
		newOntRadio = (JRadioButton) this.initComponent(JRadioButton.class, "New URI:");
		ButtonGroup group = new ButtonGroup();
		group.add(existingOntRadio);
		group.add(newOntRadio);
		existingOntRadio.setSelected(true);
		refreshBtn = (JButton) this.initComponent(JButton.class, "Refresh");
		JPanel ontSelPanel = this.createPanel(null, ontBox, refreshBtn);
		JPanel radio1 = this.createPanel(existingOntRadio, ontSelPanel, null);
		JPanel radio2 = this.createPanel(newOntRadio, swoopOntFld, null);
		JPanel ontPanel = new JPanel();
		ontPanel.setLayout(new GridLayout(5,1));
		ontPanel.add(swoopOntLbl);
		ontPanel.add(radio1);
		ontPanel.add(radio2);
		ontPanel.add(new JLabel(""));
		ontPanel.add(new JLabel(""));
		
		// create swoop ontology toolbar
		swoDeleteBtn = (JButton) this.initComponent(JButton.class, "Delete");
		swoSelAllBtn = (JButton) this.initComponent(JButton.class, "Select All");
		swoTestBtn = (JButton) this.initComponent(JButton.class, "Test All");
		swoFilterBtn = (JButton) this.initComponent(JButton.class, "Filter");
		swoFilterBtn.addMouseListener(this);
		JToolBar swoToolBar = new JToolBar();		
		swoToolBar.add(swoSelAllBtn);
		swoToolBar.add(swoDeleteBtn);
		swoToolBar.add(swoTestBtn);
		swoToolBar.add(swoFilterBtn);
//		ontPanel.setBorder(border);
		
		// create ont changes panel
		JPanel ontChangePanel = new JPanel();
		ontChangePanel.setLayout(new BorderLayout());
		// initialize change treetable and change editorpane
		ontChangeTT = new JTreeTable(new DefaultTreeTableModel(getTreeRoot(), cNames));
		ontChangeTT.setFont(tahoma);
		ontChangeTT.getTree().setRootVisible(false);
		ontChangeTT.getTree().addTreeSelectionListener(this);
		ontTablePane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		ontTablePane.setTopComponent(new JScrollPane(ontChangeTT));
		ontChangeEdPane = new JEditorPane();
		ontChangeEdPane.setContentType("text/html");
		ontChangeEdPane.setEditable(false);
		ontChangeEdPane.addHyperlinkListener(this);
		ontTablePane.setBottomComponent(new JScrollPane(ontChangeEdPane));
		ontChanges = new ArrayList();
		
		// initialize button panel
		JPanel ontBtnPanel = new JPanel();
		ontBtnPanel.setLayout(new GridLayout(1,4));
		ontBtnPanel.add(new JLabel(""));		
		swoUpdateBtn = (JButton) this.initComponent(JButton.class, "Update");
		swoCommitBtn = (JButton) this.initComponent(JButton.class, "Commit");
		ontBtnPanel.add(swoUpdateBtn);
		ontBtnPanel.add(swoCommitBtn);		
		ontBtnPanel.add(new JLabel(""));
		ontChangePanel.add(ontTablePane, "Center");
		ontChangePanel.add(ontBtnPanel, "South");
		moveRBtn = (JButton) this.initComponent(JButton.class, "");
		SwoopIcons swi = new SwoopIcons();
		moveRBtn.setIcon(swi.nextIcon);
		moveLBtn = (JButton) this.initComponent(JButton.class, "");
		moveLBtn.setIcon(swi.prevIcon);
		JPanel movePanel = this.createColPanel(moveRBtn, moveLBtn);
		ontChangePanel.add(movePanel, "East");
		
		leftPanel = new JPanel();
		leftPanel.setLayout(new BorderLayout());
		JPanel northPnl = new JPanel();
		northPnl.setLayout(new BorderLayout());
		northPnl.add(ontPanel, "Center");
		northPnl.add(swoToolBar, "South");
		leftPanel.add(northPnl, "North");
		leftPanel.add(ontChangePanel, "Center");
		
		// RIGHT PANEL
		// setup repository info
		JPanel repPanel = new JPanel();
		JLabel repAuthorLbl = (JLabel) initComponent(JLabel.class, "Repository Author:");
		JLabel repDateLbl = (JLabel) initComponent(JLabel.class, "Repository Created Date:");
		JLabel repURLLbl = (JLabel) initComponent(JLabel.class, "Repository URL:");
		repAuthorFld = (JTextField) initComponent(JTextField.class, "");
		repDateFld = (JTextField) initComponent(JTextField.class, "");
		repURLFld = (JTextField) initComponent(JTextField.class, "");
		repURLFld.addKeyListener(this);
		JLabel repBaseOntLbl = (JLabel) initComponent(JLabel.class, "Repository Base Ontology URI:");
		repBaseOntFld = (JTextField) initComponent(JTextField.class, "");
		repTypeBox = (JComboBox) initComponent(JComboBox.class, "");
		repTypeBox.addItem(this.getVersioningType());
		repPanel.setLayout(new SpringLayout());
		repPanel.add(repURLLbl);
		repPanel.add(repURLFld);
		repPanel.add(repAuthorLbl);
		repPanel.add(repAuthorFld);
		repPanel.add(repDateLbl);
		repPanel.add(repDateFld);		
		repPanel.add(repBaseOntLbl);
		repPanel.add(repBaseOntFld);
		SpringUtilities.makeCompactGrid(repPanel,
                4, 2, // rows, cols
                6, 6, //initX, initY
                6, 6); //xPad, yPad
		repPanel.setBorder(border);
		
		// create toolbar panel
		JToolBar repToolBar = new JToolBar();
		repDeleteBtn = (JButton) this.initComponent(JButton.class, "Delete");
		repSelAllBtn = (JButton) this.initComponent(JButton.class, "Select All");
		repTestBtn = (JButton) this.initComponent(JButton.class, "Test All");
		repFilterBtn = (JButton) this.initComponent(JButton.class, "Filter");
		repFilterBtn.addMouseListener(this);
		repToolBar.add(repSelAllBtn);
		repToolBar.add(repDeleteBtn);
		repToolBar.add(repTestBtn);
		repToolBar.add(repFilterBtn);
		
		// create changes panel
		JPanel repChangePanel = new JPanel();
		repChangePanel.setLayout(new BorderLayout());
		// initialize change treetable and change editorpane
		repRoot = getTreeRoot();
		// also initialize newCommitNode
		newCommitNode = new TreeTableNode(new SwoopChange(swoopModel.getUserName(), null, null, swoopModel.getTimeStamp(), "New Version Commit", true, true));
		newCommitNode.swoopChange.isTopNode = true;
		// add newCommitNode to root
		repRoot.addChild(newCommitNode);
		repChangeTT = new JTreeTable(new DefaultTreeTableModel(repRoot, cNames));
		this.repChangeTT.setFont(tahoma);
		repChangeTT.getTree().setRootVisible(false);
		repChangeTT.getTree().addTreeSelectionListener(this);
		repTablePane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		repTablePane.setTopComponent(new JScrollPane(repChangeTT));
		repChangeEdPane = new JEditorPane();
		repChangeEdPane.setContentType("text/html");
		repChangeEdPane.setEditable(false);
		repChangeEdPane.addHyperlinkListener(this);
		repTablePane.setBottomComponent(new JScrollPane(repChangeEdPane));
		repChanges = new ArrayList();
		
		// initialize button panel
		JPanel repBtnPanel = new JPanel();
		repBtnPanel.setLayout(new GridLayout(1,4));
		repBtnPanel.add(new JLabel(""));
		repUpdateBtn = (JButton) this.initComponent(JButton.class, "Update");
		repCommitBtn = (JButton) this.initComponent(JButton.class, "Commit");
		repBtnPanel.add(repUpdateBtn);
		repBtnPanel.add(repCommitBtn);
		repBtnPanel.add(new JLabel(""));
		repChangePanel.add(repTablePane, "Center");
		repChangePanel.add(repBtnPanel, "South");
		
		// right panel setup
		rightPanel = new JPanel();
		rightPanel.setLayout(new BorderLayout());
		JPanel northPanel = new JPanel();
		northPanel.setLayout(new BorderLayout());
		northPanel.add(repPanel, "Center");
		northPanel.add(repToolBar, "South");
		rightPanel.add(northPanel, "North");
		rightPanel.add(repChangePanel, "Center");
		
		splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		splitPane.setLeftComponent(leftPanel);
		splitPane.setRightComponent(rightPanel);
		
		// main panel setup
		setLayout(new BorderLayout());
		// create topPanel with advanced check box
		JPanel topPanel = new JPanel();
		topPanel.setLayout(new BorderLayout());
		this.advancedChk = (JCheckBox) this.initComponent(JCheckBox.class, "Advanced");
		topPanel.add(advancedChk, "East");
		add(topPanel, "North");
		add(splitPane, "Center");
		// add status bar at the south
		statusBar = (JLabel) this.initComponent(JLabel.class, "Status:");
		statusBar.setFont(new Font("Verdana", Font.BOLD, 10));
		add(statusBar, "South");
		// set dividers after adding to ui
		ontTablePane.setDividerLocation(200);
		repTablePane.setDividerLocation(200);
		splitPane.setDividerLocation(300);
	}
	
	/*
	 * Create PopupMenus for the Filtering options for each changeTreeTable
	 */
	private void setupFilterPopupMenus() {
		
		// setup swoop ontology filter menu
		this.swoFilterMenu = new JPopupMenu();
		this.swoRedFMenu = (JMenuItem) this.initComponent(JMenuItem.class, "Remove Redundant Changes");
		this.swoRepFMenu = (JMenuItem) this.initComponent(JMenuItem.class, "Remove Repository Changes");
		this.swoLocFMenu = (JMenuItem) this.initComponent(JMenuItem.class, "Remove Local Changes");
		this.swoFilterMenu.add(swoRedFMenu);
		this.swoFilterMenu.add(swoRepFMenu);
		this.swoFilterMenu.add(swoLocFMenu);
		
		// setup repository ontology filter menu
		this.repFilterMenu = new JPopupMenu();
		this.repRedFMenu = (JMenuItem) this.initComponent(JMenuItem.class, "Remove Redundant Changes");
		this.repFilterMenu.add(repRedFMenu);		
	}
	
	/**
	 * Common method to initialize JComponents in the UI
	 */
	private JComponent initComponent(Class compClass, String label) {
		JComponent comp = null;
		if (compClass == JLabel.class) {
			comp = new JLabel(label);			
		}
		else if (compClass == JTextField.class) {
			comp = new JTextField();			
		}
		else if (compClass == JComboBox.class) {
			comp = new JComboBox();
			((JComboBox) comp).addActionListener(this);
		}
		else if (compClass == JButton.class) {
			comp = new JButton(label);
			((JButton) comp).addActionListener(this);
		}
		else if (compClass == JRadioButton.class) {
			comp = new JRadioButton(label);
			((JRadioButton) comp).addActionListener(this);
		}
		else if (compClass == JMenuItem.class) {
			comp = new JMenuItem(label);
			((JMenuItem) comp).addActionListener(this);
		}
		else if (compClass == JCheckBox.class) {
			comp = new JCheckBox(label);
			((JCheckBox) comp).addActionListener(this);
		}
		comp.setFont(tahoma);
		return comp;
	}
	
	private JPanel createPanel(JComponent left, JComponent center, JComponent right) {
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		if (left!=null) panel.add(left, "West");
		panel.add(center, "Center");
		if (right!=null) panel.add(right, "East");
		return panel;
	}
	
	private String getVersioningType() {
		
		switch (versioningFormat) {
			case 0 :
				return "Annotated Change Set";
			// maybe other types of formats: ontology as a whole etc
		}
		return "";
	}
	
	public TreeTableNode getTreeRoot() {
		SwoopChange root = null;
		try {
			root = new SwoopChange("", new URI("/"), null, "", "", true, true);
		}
		catch (Exception ex) {
		}
		return new TreeTableNode(root);
	}

	public void actionPerformed(ActionEvent e) {
		
		// if ontology box is selected 
		if (e.getSource()==ontBox && e.getActionCommand().equals("comboBoxChanged")) {
			// load changes from Swoop into this pane
			
		}
		// if swoop ontology update button is pressed
		else if (e.getSource() == swoUpdateBtn) {
			this.updateOntChanges();
		}
		// if swoop ontology commit button is pressed
		else if (e.getSource() == swoCommitBtn) {
			this.commitOntChanges(false);
		}
		// if ont -> rep transfer button is pressed
		else if (e.getSource() == moveRBtn) {
			this.transferChanges(ontChangeTT, repChangeTT);
		}
		// if rep -> ont transfer button is pressed
		else if (e.getSource() == moveLBtn) {
			this.transferChanges(repChangeTT, ontChangeTT);
		}		
		// if rep commit button is pressed
		else if (e.getSource() == repCommitBtn) {
			this.commitRepChanges();
		}
		// if rep update button is pressed
		else if (e.getSource() == repUpdateBtn) {
			this.updateRepChanges(true);
		}
		// if delete swoop ontology annotations button is pressed
		else if (e.getSource() == swoDeleteBtn) {
			this.deleteChanges(ontChangeTT);
		}
		// if delete repository annotations button is pressed
		else if (e.getSource() == repDeleteBtn) {
			this.deleteChanges(repChangeTT);
//			this.deleteRepository(); // TESTING PURPOSES ONLY			
		}
		else if (e.getSource() == swoSelAllBtn) {
			ontChangeTT.selectAll();
			ontChangeTT.selectAll(); // need to do it twice?! Fix this
		}
		else if (e.getSource() == repSelAllBtn) {
			repChangeTT.selectAll();
			repChangeTT.selectAll();
		}
		// swoop ontology: test changes
		else if (e.getSource() == swoTestBtn) {
			this.testChanges(false);
		}
		// repository: test changes
		else if (e.getSource() == repTestBtn) {
			this.testChanges(true);
		}
		
		// filter menus
		else if (e.getSource() == swoRedFMenu) {
			this.filter(false, this.REDUNDANT_CHANGE);
		}
		else if (e.getSource() == swoRepFMenu) {
			this.filter(false, this.REPOSITORY_CHANGE);
		}
		else if (e.getSource() == swoLocFMenu) {
			this.filter(false, this.LOCAL_CHANGE);
		}
		else if (e.getSource() == repRedFMenu) {
			this.filter(true, this.REDUNDANT_CHANGE);
		}
		// advanced check box
		else if (e.getSource() == advancedChk) {
			this.toggleAdvanced(advancedChk.isSelected());
		}
	}
	
	/*
	 * Toggle the Advanced Check Box UI setting
	 */
	private void toggleAdvanced(boolean enable) {
		
		this.advanced = enable;
		if (enable) {
			this.leftPanel.setVisible(true);
			splitPane.setDividerLocation(splitPaneDivPos);			
//			controlHandler.setSize(800, 600);			
		}
		else {
			splitPaneDivPos = splitPane.getDividerLocation();
			this.leftPanel.setVisible(false);
			splitPane.setDividerLocation(0);
//			controlHandler.setSize(400, 600);			
		}
	}
	
	/*
	 * Refresh Ontology Selection Box from SwoopModel
	 *
	 */
	protected void refreshOntBox() {
		ontBox.removeAllItems();
		
		Set sortedOntSet = new TreeSet(OntologyComparator.INSTANCE);
		sortedOntSet.addAll(swoopModel.getOntologies());
		for (Iterator iter=sortedOntSet.iterator(); iter.hasNext();) {
			OWLOntology ont = (OWLOntology) iter.next();
			ontBox.addItem(ont);
		}
	}
	
	/* Create column panel for transfer buttons */
	private JPanel createColPanel(JButton btn1, JButton btn2) {
		JPanel colPanel = new JPanel();
		colPanel.setLayout(new GridLayout(10,1));
		for (int i=0; i<3; i++) colPanel.add(new JLabel(""));
		colPanel.add(btn1);
		colPanel.add(btn2);
		for (int i=0; i<5; i++) colPanel.add(new JLabel(""));
		return colPanel;
	}
	
	// update changes list from swoop ontology
	private void updateOntChanges() {
		try {
			String status = "Status: [ACTION - Update Local Ontology]...";
			statusBar.setText(status);
			if (existingOntRadio.isSelected()) {
				OWLOntology ont = (OWLOntology) ontBox.getSelectedItem();
				List changes = swoopModel.getChangesCache().getChangeList(ont.getURI());
				for (int i=0; i<changes.size(); i++) {
					SwoopChange swc = (SwoopChange) changes.get(i);
					if (!isPresent(ontChanges, swc)) ontChanges.add(swc.clone());
				}
				this.sortChanges(ontChanges);
				this.refreshOntTreeTable(); 
			}
			statusBar.setText(status+"DONE");
		}
		catch (OWLException ex) {
			ex.printStackTrace();
		}
	}
	
	public void refreshOntTreeTable() {

		// get rootnode from changetreetable
		JTree changeTree = ontChangeTT.getTree();
		TreeTableNode rootNode = (TreeTableNode) changeTree.getModel().getRoot();
		rootNode.children = new Vector();
		this.ontChangeEdPane.setText("");
		
		// populate node tree
		for (Iterator changeIter = ontChanges.iterator(); changeIter.hasNext();) {
			SwoopChange swc = (SwoopChange) changeIter.next();
			
			// skip checkpoint related changes
			if (!swc.isCheckpointRelated /*&& swc.isCommitted*/) {
				TreeTableNode changeNode = new TreeTableNode(swc);
				rootNode.addChild(changeNode);				
			}
		}
		
		ontChangeTT.getTree().updateUI();
		this.refreshUI();		
	}
	
	private void refreshUI() {
		int loc = ontTablePane.getDividerLocation();
		ontTablePane.setTopComponent(new JScrollPane(ontChangeTT));
		ontTablePane.setDividerLocation(loc);
		loc = repTablePane.getDividerLocation();
		repTablePane.setTopComponent(new JScrollPane(repChangeTT));
		repTablePane.setDividerLocation(loc);
	}
	
	/**
	 * transfer selected changes from Swoop Ont list to Repository List
	 * or vice versa
	 */
	public void transferChanges(JTreeTable source, JTreeTable target) {
		
		// add to changes in target based on source
		JEditorPane changePane = null;
		List changes = null;
		boolean refreshOntTT = true;
		if (target == ontChangeTT) {
			changes = ontChanges;
			changePane = ontChangeEdPane;
		}
		else {
			refreshOntTT = false;
			changes = repChanges;
			changePane= repChangeEdPane;
		}
		
		// get selected changes in source
		if (source.getTree().getSelectionPath()==null) return;
		TreePath[] paths = source.getTree().getSelectionPaths();
		for (int i=0; i<paths.length; i++) {
			TreePath path = paths[i];
			TreeTableNode selNode = (TreeTableNode) path.getLastPathComponent();
			if (selNode.swoopChange.isTopNode) {
				// skip version top nodes, but transfer its children
				for (Iterator iter = selNode.children.iterator(); iter.hasNext();) {
					TreeTableNode childNode = (TreeTableNode) iter.next();
					SwoopChange swc = (SwoopChange) childNode.swoopChange;
					// check if it already exists in changes (prevent duplicates) 
					if (!isPresent(changes, swc)) changes.add(swc.clone());
				}
			}
			else { 
				SwoopChange swc = (SwoopChange) selNode.swoopChange;
				// check if it already exists in changes (prevent duplicates) 
				if (!isPresent(changes, swc)) changes.add(swc.clone());
			}
		}
		
		this.sortChanges(changes);
		
		// refresh UI of target after adding changes
		if (refreshOntTT) this.refreshOntTreeTable();
		else {
			// add (new) repChanges to newCommit node directly
			newCommitNode.children = new Vector();
			for (Iterator iter = changes.iterator(); iter.hasNext();) {
				SwoopChange swc = (SwoopChange) iter.next();
				TreeTableNode swcNode = new TreeTableNode(swc);
				newCommitNode.addChild(swcNode);	
			}
			this.refreshRepTreeTable(true);
		}
	}
	
	/*
	 * Check if a swoopChange is present in the changes list.
	 * Check if the author, ontologychange and timestamp matches
	 * since these 3 params determine uniqueness
	 */
	private boolean isPresent(List changes, SwoopChange check) {
		for (Iterator iter = changes.iterator(); iter.hasNext();) {
			SwoopChange swc = (SwoopChange) iter.next();
			if (swc.getAuthor().equals(check.getAuthor()) &&
				swc.getDescription().equals(check.getDescription()) && 
//				swc.getChange().equals(check.getChange()) &&
				swc.getTimeStamp().equals(check.getTimeStamp()))
				return true;
		}
		return false;
	}
	
	/***
	 * Make a commit to the selected repository
	 * 1. Check if the repository already exists
	 * 2. Check if all changes on the repository are in Swoop
	 * 3. Check that there are no clashes involving changes (testChanges(..))
	 * 4. Commit new version alone!
	 */
	private void commitRepChanges() {
		
		// get repository info
		String repURL = repURLFld.getText();
		String boURI = repBaseOntFld.getText();
		this.repositoryAuthor = repAuthorFld.getText();
		this.reposityCreatedDate = repDateFld.getText();
		
		String status = "Status: [ACTION - Commit to Repository]...";
		try {
			this.repositoryURI = new URI(repURL);
			this.baseOntologyURI = new URI(boURI);
		}
		catch (URISyntaxException ex) {
			statusBar.setText(status+"Invalid Repository URL and/or Base Ontology URI");
			return;
		}
		
		try {
			// check if repository already exists
			boolean repExists = this.loadRepositoryHeader();
			
			if (!repExists) {
				// creating new repository
				// form repository header
				URI[] headerURI = new URI[1];
				headerURI[0] = new URI(this.repositoryURI+"#header");
				
				if (this.repositoryAuthor.equals("") || this.reposityCreatedDate.equals("")) {
					int opt = JOptionPane.showConfirmDialog(null, "Repository Author and/or Date not specified. Continue?", "Creating Repository", JOptionPane.YES_NO_OPTION);
					if (opt == JOptionPane.NO_OPTION) return;
				}
				// create annotea description for header
				Description header = new Description();
				header.setAnnotates(headerURI);
				header.setAuthor(this.repositoryAuthor);
				header.setCreated(this.reposityCreatedDate);
				header.setAnnotatedEntityDefinition(this.baseOntologyURI.toString());
				this.headVersionNumber = 0;
				header.setBody(String.valueOf(this.headVersionNumber));
				header.setBodyType("text/html");
				header.setAnnotationType(this.annotType);
				client.post(header);
				statusBar.setText(status+"Ontology Repository Header posted at "+headerURI[0]);
				toggleRepOptions(false);
				versionDescriptions[0] = header;
				swoopModel.updateVersionRepository(repositoryURI, versionDescriptions);
			}
			
			// test changes
			boolean success = this.testChanges(true);
			if (success){
				// prompt user for comment on commit
				String commitComment = JOptionPane.showInputDialog(this, "COMMIT New Version (Details):");
				
				if (commitComment==null) {
					statusBar.setText(status+"CANCELLED");
					return;
				}
				// transform SwoopChange list to Description list (to save author, date, uris etc)
				List descList = transformChangeList(repChanges);
				// serialize each description into RDF/XML
				// and make it one large string with separator ("[SWOOP-ANNOTATED-CHANGE-SET]") in the middle
				String largeChangeSetString = "";
				for (Iterator iter = descList.iterator(); iter.hasNext();) {
					Description desc = (Description) iter.next();
					String descStr = desc.serializeIntoString(swoopModel);					
					largeChangeSetString += descStr + separator;
				}
				
				// ENCODE CDATA
				largeChangeSetString = largeChangeSetString.replaceAll("CDATA", "ENCODED-CDATA");
				largeChangeSetString = largeChangeSetString.replaceAll("]]>", "]ENCODED]>");
				
				// finally commit a single annotation with the entire changeset string in the body
				// also allow author to make annotation on commit
				Description commit = new Description();
				commit.setAuthor(swoopModel.getUserName());
				commit.setCreated(swoopModel.getTimeStamp());
				commit.setBody(largeChangeSetString);
				commit.setBodyType("text/html");
				commit.setAnnotatedEntityDefinition(commitComment);
				commit.setAnnotationType(this.annotType);
				
				// increment headVersionNum and write new commit
				// at repURL+"#"+headVersionNum
				this.headVersionNumber++;
				URI[] annotates = new URI[1];
				annotates[0] = new URI(repositoryURI.toString()+"#"+String.valueOf(this.headVersionNumber));
				commit.setAnnotates(annotates);
				// COMMIT!
				client.post(commit);
				
				// post-process:
				// 1. rewrite rep header added newly incremented headVersionNumber 
				this.rewriteRepHeader();
				// 2. set newCommitNode params to current commit
				newCommitNode.swoopChange.setAuthor(commit.getAuthor());
				newCommitNode.swoopChange.setTimeStamp(commit.getCreated());
				newCommitNode.swoopChange.setDescription(commit.getAnnotatedEntityDefinition());
				// 3. save newCommitNode in versionCommits array and Description in versionDescriptions array
				versionNodes[this.headVersionNumber] = newCommitNode;
				versionDescriptions[this.headVersionNumber] = commit;
				swoopModel.updateVersionRepository(repositoryURI, versionDescriptions);
				// 4. for each child of newCommitNode, set its swoopChange.onRepository value to true
				for (Iterator iter3=newCommitNode.children.iterator(); iter3.hasNext();) {
					TreeTableNode child = (TreeTableNode) iter3.next();
					child.swoopChange.isOnRepository = true;
				}
				// 5. create newCommitNode and add to root at the end
				newCommitNode = new TreeTableNode(new SwoopChange(swoopModel.getUserName(), null, null, swoopModel.getTimeStamp(), "New Version Commit", true, false));
				newCommitNode.swoopChange.isTopNode = true;
				repRoot.addChild(newCommitNode);
				repChanges = new ArrayList(); // also clear this
				statusBar.setText(status+"Committed New Version "+this.headVersionNumber);
			}			
		}
		catch (Exception ex) {
			ex.printStackTrace();
			statusBar.setText(status+"FAILED");
		}
		this.refreshRepTreeTable(false);
	}
	
	/*
	 * Test the changes in the ontChange list or repChange list
	 * i.e. Take the initial base ontology, apply changes,
	 * see if clash occurs, and remove all redundant changes (that do not cause change in ontology) 
	 * @return
	 */
	private boolean testChanges(boolean testRepository) {
		
		if (testRepository) {
			
			String status = "Status: [ACTION - Testing Repository Changes]...";
			
			// test changes in repository for clashes etc.
			statusBar.setText(status);
			boolean testSuccess = true;
			int failCount = 0;
			
			try {
				// do an update first to make sure rep-side changes are all there
				boolean updateSuccess = this.updateRepChanges(false);
				
				if (!updateSuccess) {
					statusBar.setText(status+"Update FAILED");
					return false;
				}
				
				// load baseOntology using URI
				statusBar.setText(status+"Loading Base Ontology");
				OWLOntology baseOntology = swoopModel.loadOntology(baseOntologyURI);
				if (baseOntology == null) {
					// there is no base ontology, changes start from scratch
					// create new ontology using OWLBuilder
					OWLBuilder builder = new OWLBuilder();
					builder.createOntology(baseOntologyURI, baseOntologyURI);
					baseOntology = builder.getOntology();
				}
				
				// get all repository side changes: under repRoot
				List allRepChanges = this.getDescendantChanges(repRoot);
				statusBar.setText(status+"Testing "+allRepChanges.size()+" repository-side changes");
				
				// apply allRepChanges to baseOntology
				for (int i=0; i<allRepChanges.size(); i++) {
					SwoopChange swc = (SwoopChange) allRepChanges.get(i);
					OntologyChange oc = swc.getChange();
					//*** need to align changes with ontology! ***/
					try {
						OntologyChange alignOC = (OntologyChange) controlHandler.swoopHandler.changeLog.getChangeInformation(oc, ChangeLog.CHANGE_ALIGN, null, new ArrayList(), baseOntology);
						
						// save state of baseOntology and see if it changes
						// use Abstract Syntax since its more concise than rdf/xml?
						String before = controlHandler.swoopHandler.changeLog.getOntSnapshot(baseOntology);
						
						// APPLY CHANGE
						boolean check = true;
						if (alignOC instanceof BooleanElementChange) { 
		    				check = swoopModel.applyBooleanElementChange(alignOC);
		    			}
		    			else if (alignOC instanceof EnumElementChange) { 
		    				check = swoopModel.applyEnumElementChange(alignOC);
		    			}
						else alignOC.accept((ChangeVisitor) baseOntology);
						
						// need to see if any change in the ontology occured
						String after = controlHandler.swoopHandler.changeLog.getOntSnapshot(baseOntology);
						
						if (before.equals(after) || !check) {
							// nothing has changed!
							System.out.println("Found redundant change: "+oc);
							swc.isRedundant = true;
							testSuccess = false;
							failCount++;
						}
						else swc.isRedundant = false;
					}
					catch (Exception ex) {
						swc.isRedundant = true;
						System.out.println("Change Error for: "+oc);
						testSuccess = false;
						failCount++;
						ex.printStackTrace();
					}
				}								
			}
			catch (Exception ex) {
				ex.printStackTrace();
				testSuccess = false;
			}
			if (testSuccess) statusBar.setText(status+"Test PASSED");
			else {
				statusBar.setText(status+"Test FAILED");
				JOptionPane.showMessageDialog(this, "Change Test FAILED: "+failCount+" change(s) is/are redundant or cause clashes. Please Fix/Remove them and try again.");
			}
			// refresh repTreeTable
			this.refreshRepTreeTable(false);
			return testSuccess;
		}
		else {
			// test changes in Swoop Ontology for clashes etc.
			String status = "Status: [ACTION - Testing LOCAL Ontology Changes]...";
			
			statusBar.setText(status);
			boolean testSuccess = true;
			int failCount = 0;
			
			// get base ontology
			statusBar.setText(status+"Loading Base Ontology");
			OWLOntology baseOntology = null;
			if (existingOntRadio.isSelected()) {
				// get existing ontology from swoopModel
				baseOntology = (OWLOntology) ontBox.getSelectedItem();
				// but clone it to prevent applying changes on *it*
				baseOntology = this.cloneOntology(baseOntology);
			}
			else {
				URI ontURI = null;
				try {
					// load ontology with URI
					ontURI = new URI(swoopOntFld.getText());
					baseOntology = swoopModel.loadOntology(ontURI);
				}
				catch (Exception ex) {
					// create new ontology
					OWLBuilder builder = new OWLBuilder();
					try {
						builder.createOntology(ontURI, ontURI);
					} catch (OWLException e) {
						e.printStackTrace();						
					}
					baseOntology = builder.getOntology();
				}
			}
			
			if (baseOntology == null) {
				statusBar.setText(status+"Unable to load base ontology");
				return false;
			}
			
			try {
				for (int i=0; i<ontChanges.size(); i++) {
					SwoopChange swc = (SwoopChange) ontChanges.get(i);
					OntologyChange oc = swc.getChange();
					//*** need to align changes with ontology! ***/
					try {
						OntologyChange alignOC = (OntologyChange) controlHandler.swoopHandler.changeLog.getChangeInformation(oc, ChangeLog.CHANGE_ALIGN, null, new ArrayList(), baseOntology);
						
						// save state of baseOntology and see if it changes
						// use Abstract Syntax since its more concise than rdf/xml?
						String before = controlHandler.swoopHandler.changeLog.getOntSnapshot(baseOntology);
						
						// APPLY CHANGE
						boolean check = true;
						if (alignOC instanceof BooleanElementChange) { 
		    				check = swoopModel.applyBooleanElementChange(alignOC);
		    			}
		    			else if (alignOC instanceof EnumElementChange) { 
		    				check = swoopModel.applyEnumElementChange(alignOC);
		    			}
						else alignOC.accept((ChangeVisitor) baseOntology);
						
						// need to see if any change in the ontology occured
						String after = controlHandler.swoopHandler.changeLog.getOntSnapshot(baseOntology);
						
						if (before.equals(after) || !check) {
							// nothing has changed!
							System.out.println("Found redundant change: "+oc);
							swc.isRedundant = true;
							testSuccess = false;
							failCount++;
						}
						else swc.isRedundant = false;
					}
					catch (Exception ex) {
						swc.isRedundant = true;
						System.out.println("Change Error for: "+oc);
						testSuccess = false;
						failCount++;
						ex.printStackTrace();
					}
				}								
			}
			catch (Exception ex) {
				ex.printStackTrace();
				testSuccess = false;
			}
			
			if (testSuccess) statusBar.setText(status+"Test PASSED");
			else {
				statusBar.setText(status+"Test FAILED");
				JOptionPane.showMessageDialog(this, "Change Test FAILED: "+failCount+" change(s) is/are redundant or cause clashes. Please Fix/Remove them and try again.");
			}
			
			// refresh ontTreeTable
			this.refreshOntTreeTable();
			return testSuccess;	
		}
		
	}
	
	/*
	 * Perform an update on the Repository. This is the only place
	 * in the code which updates versions from the repository
	 * and saves them in the local data structure versionCommits
	 */
	private boolean updateRepChanges(boolean sync) {
		
		try {				
			// get repository header since we need count of versions 
			// i.e. headVersionNum
			String status = "Status: [ACTION - Update Repository]...";
			
			statusBar.setText(status+"Loading repository header to find headVersionNumber");
			boolean existsRep = this.loadRepositoryHeader();
			if (!existsRep) {
				if (DEBUG) System.out.println("NOT FOUND");
				// update from local Swoop log anyway if uri matches ontURI
				this.updateRepFromLog(new URI(this.repBaseOntFld.getText()));
				return false;
			}
			statusBar.setText(status+"HeadVersionNum="+this.headVersionNumber);
		
			// note: all version commits have been made to URLs:
			// repositoryURL+"#1"..."#headVersionNum"
			// so iterate through versionCommits and see if value is null, implying it hasn't been updated
			for (int ctr=1; ctr<=this.headVersionNumber; ctr++) {
				if (versionNodes[ctr]==null) {
					// form URI using ctr
					URI versionURI = new URI(this.repositoryURI+"#"+ctr);
					statusBar.setText(status+"Updating version at "+versionURI);
					Set commitSet = client.findAnnotations(versionURI);
					// get single Description (version) at URI
					Description version = (Description) commitSet.iterator().next();
					versionDescriptions[ctr] = version;
					TreeTableNode mainNode = this.parseSingleCommit(version);
					// set params on mainNode
					mainNode.swoopChange.isOnRepository = true;
					mainNode.location = version.getLocation();
					versionNodes[ctr] = mainNode;
				}
			}
			
			// also if advanced is off, update from local copy as well
//			if (!advanced) {
//				this.updateRepFromLog(new URI(this.repBaseOntFld.getText()));
//			}
			
			// resort all nodes under root
			this.refreshRepTreeTable(true);
			
			// update version repository cache
			swoopModel.updateVersionRepository(repositoryURI, versionDescriptions);
			
			// if sync is true, commit all changes to the synchronized ontology
			if (sync) {
				statusBar.setText(status+"Synching with Local Swoop Ontology");
				this.commitOntChanges(true);				
			}
			statusBar.setText(status+"DONE");
			return true;
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
		return false;
	}
	
	/*
	 * Parse a single 'Commit' Description to obtain the main top node
	 * and all the SwoopChange nodes included in that commit 
	 * (put them as children of the main node)
	 */
	private TreeTableNode parseSingleCommit(Description commit) throws OWLException {

		// make the main annotation the top header
		SwoopChange main = new SwoopChange();
		main.setAuthor(commit.getAuthor());
		main.setCommitted(true); // not needed really
		main.isTopNode = true; /* this is key */
		main.setTimeStamp(commit.getCreated());
		main.setDescription(commit.getAnnotatedEntityDefinition());
		TreeTableNode mainNode = new TreeTableNode(main);
		
		// parse change set associated with single commit
		String changeSetString = commit.getBody();
		if (changeSetString!=null) {
			// decode CDATA stuff
			changeSetString = changeSetString.replaceAll("ENCODED-CDATA", "CDATA");
			changeSetString = changeSetString.replaceAll("]ENCODED]>", "]]>"); 
				
			// now split based on separator
			String[] changeStr = changeSetString.split(separator);
			
			// each split component is a separate change
			List repDesc = new ArrayList(); // list of Description (changes)
			for (int i=0; i<changeStr.length; i++) {
				String swcDesc = changeStr[i]; // SwoopChange mapped to a Description serialized in RDF/XML
				Description desc = new Description();
				desc.deserializeFromString(swcDesc, swoopModel);
				repDesc.add(desc);							
			}
			
			// transform Description list to SwoopChange list
			List swcChangeList = this.transformChangeList(repDesc);
			
			// add each swoopChange as a child of mainNode
			this.sortChanges(swcChangeList);
			for (Iterator iter2 = swcChangeList.iterator(); iter2.hasNext();) {
				SwoopChange swc = (SwoopChange) iter2.next();
				TreeTableNode swcNode = new TreeTableNode(swc);
				swcNode.swoopChange.isOnRepository = true;
				mainNode.addChild(swcNode);
			}
		}
		
		return mainNode;
	}
	
	/*
	 * Load repository header annotation
	 * and parse annotation to obtain repository information such as
	 * author, date, baseOntologyURI, and no of version commits
	 */
	private boolean loadRepositoryHeader() {
		try {
			// annotation with URI: repositoryURL + "#header"
			repositoryURI = new URI(repURLFld.getText());
			URI repHeaderURI = new URI(repositoryURI+"#header");
			
			if (client==null) this.initAnnoteaClient();
			
			// get annotation using Annotea
			Set headerSet = client.findAnnotations(repHeaderURI);
			if (headerSet.size()>0) {
				Description header = (Description) headerSet.iterator().next();
				versionDescriptions[0] = header;
				
				// parse header description to get author, date, baseOntologyURI
				this.repositoryAuthor = header.getAuthor();
				this.reposityCreatedDate = header.getCreated();
				this.baseOntologyURI = new URI(header.getAnnotatedEntityDefinition());
				// and headVersionNumber
				this.headVersionNumber = Integer.parseInt(header.getBody());
				
				// also get actual URL location of annotation
				this.repHeaderLoc = header.getLocation();
				
				// set UI accordingly
				this.repAuthorFld.setText(this.repositoryAuthor);
				this.repDateFld.setText(this.reposityCreatedDate);
				this.repBaseOntFld.setText(this.baseOntologyURI.toString());
				this.toggleRepOptions(false);
				System.out.println("Ontology Repository header exists at "+repHeaderURI);
				return true;
			}	
			
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
		return false;
	}
	
	/*
	 * Transform a list of changes from SwoopChange -> Description objects
	 * or vice versa
	 */
	private List transformChangeList(List source) {
		
		List target = new ArrayList();
		for (int i=0; i<source.size(); i++) {
			Object change = source.get(i);
			if (change instanceof SwoopChange) {
				SwoopChange swc = (SwoopChange) change;
				// change SwoopChange to a Description object
				Description desc = new Description();
				desc.setAuthor(swc.getAuthor());
				desc.setCreated(swc.getTimeStamp());
				desc.setBody(swc.getDescription());
				desc.setBodyType("text/html");
				desc.setAnnotatedEntityDefinition(swc.comment);
				// create annotates URI from
				// 1. repository URL
				// 2. owlObjectURI
				// 3. extra URIs
				URI[] uris = new URI[2+swc.getExtraSubjects().size()];
				uris[0] = repositoryURI;
				uris[1] = swc.getOwlObjectURI();
				for (int j=0; j<swc.getExtraSubjects().size(); j++) {
					uris[j+2] = (URI) swc.getExtraSubjects().get(j);
				}
				desc.setAnnotates(uris);
				// attach single ontology change to description
				List chngList = new ArrayList();
				chngList.add(swc.getChange());
				desc.setOntologyChangeSet(chngList);				
				target.add(desc);
			}
			else if (change instanceof Description) {
				Description desc = (Description) change;
				// change Description to SwoopChange object
				SwoopChange swc = new SwoopChange();
				swc.setAuthor(desc.getAuthor());
				swc.setTimeStamp(desc.getCreated());
				swc.setDescription(desc.getBody());
				swc.comment = desc.getAnnotatedEntityDefinition();
				swc.setCommitted(true); // set committed
				// get URIs from desc for swc
				URI[] uris = desc.getAnnotates();
				swc.setOwlObjectURI(uris[1]);
				List extraSubjects = new ArrayList(); 
				for (int j=2; j<uris.length; j++) {
					extraSubjects.add(uris[j]);
				}
				swc.setExtraSubjects(extraSubjects);
				// get single ontology change object
				List chngList = desc.getOntologyChangeSet();
				swc.setChange((OntologyChange) chngList.iterator().next());
				target.add(swc);
			}
		}
		return target;		
	}
	
	/*
	 * Toggle UI enable/disable of Repository Information fields
	 */
	private void toggleRepOptions(boolean enable) {
		Color color = Color.WHITE;
		if (!enable) color = Color.LIGHT_GRAY;
		this.repAuthorFld.setBackground(color);
		this.repBaseOntFld.setBackground(color);
		this.repDateFld.setBackground(color);
		this.repAuthorFld.setEditable(enable);
		this.repBaseOntFld.setEditable(enable);
		this.repDateFld.setEditable(enable);
	}

	public void keyTyped(KeyEvent e) {
		if (e.getSource() == repURLFld) this.toggleRepOptions(true);
	}

	public void keyPressed(KeyEvent arg0) {
		
	}

	public void keyReleased(KeyEvent arg0) {
	}
	
	private void initAnnoteaClient() {
		if (Annotea.INSTANCE==null) Annotea.initializeAnnotea();
		try {
			client = new AnnoteaClient(new URL(serverURL), swoopModel);
			// also set annotType for all Description objects created here
			// to the Change type in Annotea
			Set annotTypes = Annotea.annotationTypes;
			for (Iterator iter = annotTypes.iterator(); iter.hasNext();) {
				OWLClass type = (OWLClass) iter.next();
				if (type.getURI().toString().toLowerCase().indexOf("change")>=0) {
					this.annotType = type;
				}
			}			
		} 
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/*
	 * Used only for TESTING Purposes. Delete contents of repository
	 */
	private void deleteRepository() {
		try {
			
			int opt = JOptionPane.showConfirmDialog(this, "Delete entire Repository (all change sets) at "+ repURLFld.getText()+" ?", "Delete All", JOptionPane.YES_NO_OPTION);
			if (opt == JOptionPane.NO_OPTION) return;
			
			// delete repository header
			if (repHeaderLoc!=null) {
				client.delete(this.repHeaderLoc);
				System.out.println("Deleted Repository Header at " + repHeaderLoc);
			}
			else System.out.println("URL location of Repository Header not known");
			
			// delete all commits at Version Controlled Repository
			for (int ctr=1; ctr<=this.headVersionNumber; ctr++) {
				URL loc = null;
				if (versionNodes[ctr]!=null) {
					loc = versionNodes[ctr].location;										
				}
				else {
					URI versionURI = new URI(this.repositoryURI.toString()+"#"+String.valueOf(ctr));
					Set versionSet = client.findAnnotations(versionURI);
					loc = ((Description) versionSet.iterator().next()).getLocation();					
				}
				client.delete(loc);
				System.out.println("Deleted Version at "+loc);
			}			
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	/*
	 * Sort all changes in a changeList based on timestamp
	 */
	private void sortChanges(List changeList) {
		SortedSet changeSet = new TreeSet(ChangeComparator.INSTANCE);
		changeSet.addAll(changeList);
		changeList.clear();
		for (Iterator iter = changeSet.iterator(); iter.hasNext();) {
			changeList.add(iter.next());
		}
	}
	
	/*
	 * Refresh all topNodes (version commits) under (children of) repRoot	 * 
	 */
	private void refreshRepTreeTable(boolean refreshRoot) {
		
		if (refreshRoot) {
			repRoot.children = new Vector();
			for (int ctr=1; ctr<=this.headVersionNumber; ctr++) {
				repRoot.addChild(this.versionNodes[ctr]);
			}
			// add new commit node at the end
			repRoot.addChild(newCommitNode);
		}
		
		// expand the newCommit node
		JTree changeTree = repChangeTT.getTree();
		int rows = changeTree.getRowCount();
		changeTree.expandRow(rows-1);
		changeTree.updateUI();
		
		// need to refresh UI
		this.refreshUI();
		
		// also set repChanges to equal all changes beneath newCommitNode
		this.repChanges = this.getDescendantChanges(newCommitNode);
	}
	
	/*
	 * Delete selected changes from a given TreeTable 
	 */
	private void deleteChanges(JTreeTable source) {
		
		List changes = null;
		boolean selectedOntChangeTT = true;
		if (source == ontChangeTT) {
			changes = new ArrayList(ontChanges);			
		}
		else {
			selectedOntChangeTT = false;
			changes = new ArrayList(repChanges);
		}
		
		// get selected changes in source
		if (source.getTree().getSelectionPath()==null) return;
		TreePath[] paths = source.getTree().getSelectionPaths();
		for (int i=0; i<paths.length; i++) {
			TreePath path = paths[i];
			TreeTableNode selNode = (TreeTableNode) path.getLastPathComponent();
			SwoopChange swc = selNode.swoopChange;
			changes.remove(swc);
		}
		
		// refresh UI of source after removing changes
		if (selectedOntChangeTT) this.refreshOntTreeTable();
		else {
			// add (new) repChanges to newCommit node directly
			newCommitNode.children = new Vector();
			for (Iterator iter = changes.iterator(); iter.hasNext();) {
				SwoopChange swc = (SwoopChange) iter.next();
				TreeTableNode swcNode = new TreeTableNode(swc);
				newCommitNode.addChild(swcNode);	
			}
			this.refreshRepTreeTable(false);
		}
	}
	
	/*
	 * Recursive method to find all descendant SwoopChanges of a particular TreeTableNode
	 */
	private List getDescendantChanges(TreeTableNode node) {
		List changes = new ArrayList();
		// start with node and recurse over children
		// dont add the top node which has children
		if (node.children.size()>0) {
			for (Iterator iter = node.children.iterator(); iter.hasNext();) {
				TreeTableNode child = (TreeTableNode) iter.next();
				changes.addAll(getDescendantChanges(child));
			}				
		}
		else if (!node.swoopChange.isTopNode) changes.add(node.swoopChange);
		return changes;
	}
	
	/*
	 * Rewrite Repository Header..
	 */
	private void rewriteRepHeader() {
		// assume that the value of headVersionNum has been 
		// incremented before coming here
		try {
			// 1. delete current header
			if (client==null) this.initAnnoteaClient();
			client.delete(this.repHeaderLoc);
			// 2. post new header
			Description header = new Description();
			URI[] headerURI = new URI[1];
			headerURI[0] = new URI(this.repositoryURI.toString() + "#header");
			header.setAnnotates(headerURI);
			header.setAuthor(this.repositoryAuthor);
			header.setCreated(this.reposityCreatedDate);
			header.setAnnotatedEntityDefinition(this.baseOntologyURI.toString());
			header.setBody(String.valueOf(this.headVersionNumber));
			header.setBodyType("text/html");
			header.setAnnotationType(this.annotType);
			// 3. update new header location URL
			repHeaderLoc = client.post(header);
			// 4. update value in versionDescriptions array and update swoopModel' versionRepository
			this.versionDescriptions[0] = header;
			swoopModel.updateVersionRepository(this.repositoryURI, versionDescriptions);
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	/*
	 * Commit Changes to the Swoop Ontology (LOCALLY)
	 */
	private void commitOntChanges(boolean sync) {
		
		String status = "Status: [ACTION - Commit to Local Ontology]...";
		
		// load base ontology first
		OWLOntology baseOntology = null;
		if (!sync) {
			if (existingOntRadio.isSelected()) {
				// get existing ontology from swoopModel
				baseOntology = (OWLOntology) ontBox.getSelectedItem();
				try {
					baseOntology = swoopModel.getOntology(baseOntology.getURI());
				} catch (OWLException e) {
					e.printStackTrace();
				}
			}
			else {
				URI ontURI = null;
				try {
					// load ontology with URI
					ontURI = new URI(swoopOntFld.getText());
					baseOntology = swoopModel.loadOntology(ontURI);
				}
				catch (Exception ex) {
					// create new ontology
					OWLBuilder builder = new OWLBuilder();
					try {
						builder.createOntology(ontURI, ontURI);
					} catch (OWLException e) {
						e.printStackTrace();						
					}
					baseOntology = builder.getOntology();
				}
				try {
					//*** remember to add new baseOntology (not currently in SwoopModel) to the model
					swoopModel.addOntology(baseOntology);
				} catch (OWLException e) {
					e.printStackTrace();
				}
			}
		}
		else {
			// if sync ontology is specified (i.e. not advanced mode)
			if (syncOntology == null) {
				try {
					URI ontURI = new URI(this.repBaseOntFld.getText());
					// check if ontology is already in Swoop 
					if (swoopModel.getOntology(ontURI)!=null) {
						//TODO: currently commit changes to present ontology
						syncOntology = swoopModel.getOntology(ontURI);
					}
					else {
						// add new ontology to swoop
						syncOntology = swoopModel.loadOntology(ontURI);
						swoopModel.addOntology(syncOntology);
					}
				}
				catch (Exception ex) {
					ex.printStackTrace();
				}
			}
			baseOntology = syncOntology;
		}
		
		if (baseOntology == null) {
			statusBar.setText(status+"Unable to load base ontology");
			return;
		}
		
		// test changes
		boolean success = true;
		List commitChanges = null;
		if (!sync) {
			// if sync is not set, i.e. advanced mode is enabled
			// commit only ontside changes
			commitChanges = ontChanges;
			success = this.testChanges(false);
		}
		else {
			// if sync is set, commit all repository-side changes
			commitChanges = new ArrayList(this.getDescendantChanges(repRoot));
			// need to test any repChanges to check for changes already applied to ontology
			try {
				List alreadyOntChanges = swoopModel.getChangesCache().getChangeList(syncOntology.getURI());
				for (Iterator iter = new ArrayList(commitChanges).iterator(); iter.hasNext();) {
					SwoopChange swc = (SwoopChange) iter.next();
					if (this.isPresent(alreadyOntChanges, swc)) commitChanges.remove(swc);
				}
			} 
			catch (OWLException e) {
				e.printStackTrace();
			}			
		}
		
		if (success){
			try {
				// commit all changes in commitChanges List
				for (int i=0; i<commitChanges.size(); i++) {
					
					SwoopChange swc = (SwoopChange) commitChanges.get(i);
					
					if (sync && !swc.isOnRepository) continue; //*** dont commit anything but repository changes to synched ontology
					
					OntologyChange oc = swc.getChange();
					
					//*** need to align changes with base ontology! ***/
					OntologyChange alignOC = (OntologyChange) controlHandler.swoopHandler.changeLog.getChangeInformation(oc, ChangeLog.CHANGE_ALIGN, null, new ArrayList(), baseOntology);
					
					// APPLY CHANGE
					if (alignOC instanceof BooleanElementChange) { 
	    				swoopModel.applyBooleanElementChange(alignOC);
	    			}
	    			else if (alignOC instanceof EnumElementChange) { 
	    				swoopModel.applyEnumElementChange(alignOC);
	    			}
					else alignOC.accept((ChangeVisitor) baseOntology);
					
					// update changesCache in swoopModel
					// first reset aligned ontology change in swc 
					swc.setChange(alignOC);
					// and add *clones* to changesCache in SwoopModel
					swoopModel.getChangesCache().addChange(swc.owlObjectURI, (SwoopChange) swc.clone());
					
					// do the same for entities referred to by swc.ontologyChange
					// & also add changed entity URI to swoopModel dirty set
					OWLEntity entity = swoopModel.getEntity(baseOntology, swc.owlObjectURI, true);
					if (swc.getOwlObjectURI().equals(baseOntology.getURI()) && swc.extraSubjects!=null) {
						for (Iterator iter = swc.extraSubjects.iterator(); iter.hasNext();) {
							URI entityURI = (URI) iter.next();
							// add to cache
							swoopModel.getChangesCache().addChange(entityURI, (SwoopChange) swc.clone());
							// add to dirty set
							entity = swoopModel.getEntity(baseOntology, entityURI, true);							
						}
					}
				}
				
				//* notify swoopModel!
				Set changedOntologies = new HashSet();
				changedOntologies.add(baseOntology);
				swoopModel.notifyListeners(new ModelChangeEvent(swoopModel, ModelChangeEvent.ONTOLOGY_CHANGED, changedOntologies));
				
				statusBar.setText(status+"Committed "+commitChanges.size()+" changes");
			}
			catch (OWLException ex) {
				ex.printStackTrace();
			}
		}
	}
	
	public void valueChanged(TreeSelectionEvent e) {
		
		if (e.getSource() instanceof JTree) {
			// get tree whose selection has changed
			JTree source = (JTree) e.getSource();
			if (source.getSelectionPath()==null) return;
			TreePath path = source.getSelectionPath();
			// get selected tree(table) node
			TreeTableNode selNode = (TreeTableNode) path.getLastPathComponent();
			// display change information based on selected node
			this.displayChange(source, selNode);			
		}				
	}

	public void hyperlinkUpdate(HyperlinkEvent e) {
		
		if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
			if (e.getSource() == ontChangeEdPane || e.getSource() == repChangeEdPane) {
				String hLink = e.getDescription();
				// edit comment of a swoopchange (in a treetablenode)
				if (hLink.startsWith("EDIT")) {
					String hashCode = hLink.substring(hLink.indexOf(":")+1, hLink.length());
					TreeTableNode node = (TreeTableNode) nodeMap.get(hashCode);
					String comment = node.swoopChange.comment;
					// prompt for new comment
					String newComment = (String) JOptionPane.showInputDialog(this, "Specify New Comment", "Comment on Change", JOptionPane.INFORMATION_MESSAGE, null, null, comment);
					if (newComment!=null) node.swoopChange.comment = newComment;
					String sourceStr = hLink.substring(4, hLink.indexOf(":"));
					// depending on local/remote change, refresh display
					if (sourceStr.equals("LOCAL")) this.displayChange(ontChangeTT.getTree(), node);
					else this.displayChange(repChangeTT.getTree(), node);
				}
			}
		}
	}
	
	private void displayChange(JTree source, TreeTableNode node) {

		// create HTML description of selected TreeTableNode
		// (based on its SwoopChange value)
		SwoopChange swc = node.swoopChange;
		String html = "<FONT FACE=\""+swoopModel.getFontFace()+"\" SIZE="+swoopModel.getFontSize()+">";
		html += "<b>Author</b>:&nbsp;"+swc.getAuthor()+"<br>";
		html += "<b>Time</b>:&nbsp;"+swc.getTimeStamp()+"<br>";
		html += "<b>Comment</b>:&nbsp;"+swc.comment;
		// add 'Edit' Comment link
		if (!swc.isOnRepository && !swc.isTopNode) {
			String sourceStr = "LOCAL";
			if (source == repChangeTT.getTree()) sourceStr = "REMOTE";
			html += "&nbsp;&nbsp;&nbsp;<font color=\"green\"><a href=\"EDIT"+sourceStr+":"+node.hashCode()+"\">Edit</a></font>";
			nodeMap.put(String.valueOf(node.hashCode()), node);
		}
		html += "<br><br>";
		html += swc.getDescription();
		html += "</FONT>";
		if (source == ontChangeTT.getTree()) {			
			this.ontChangeEdPane.setText(html);
		}
		else if (source == repChangeTT.getTree()) {
			this.repChangeEdPane.setText(html);				
		}		
	}
	
	/*
	 * Clone an OWL Ontology by serializing it into RDF/XML
	 * and deserializing it!
	 */
	public OWLOntology cloneOntology(OWLOntology source) {
		OWLOntology copy = null;
		try {
			CorrectedRDFRenderer rdfRend = new CorrectedRDFRenderer();
			StringWriter st = new StringWriter();
			rdfRend.renderOntology(source, st);
			copy = swoopModel.loadOntologyInRDF(new StringReader(st.toString()), source.getURI(), true);			
		}
		catch (OWLException ex) {
			ex.printStackTrace();
		}
		return copy;
	}

	public void mouseClicked(MouseEvent arg0) {
	}

	public void mousePressed(MouseEvent evt) {
		
		if (evt.getSource() == swoFilterBtn) {
			this.swoFilterMenu.show(evt.getComponent(), evt.getX(), evt.getY());
		}
		else if (evt.getSource() == repFilterBtn) {
			this.repFilterMenu.show(evt.getComponent(), evt.getX(), evt.getY());
		}
	}

	public void mouseReleased(MouseEvent evt) {
		
		if (evt.getSource() == swoFilterBtn) {
			this.swoFilterMenu.show(evt.getComponent(), evt.getX(), evt.getY());
		}
		else if (evt.getSource() == repFilterBtn) {
			this.repFilterMenu.show(evt.getComponent(), evt.getX(), evt.getY());
		}
	}

	public void mouseEntered(MouseEvent arg0) {
	}
	public void mouseExited(MouseEvent arg0) {
	}
	
	/*
	 * Filter out all nodes from the corresponding changeTreeTable (swo or rep)
	 * depending on boolean and type passed to it.
	 * type is one of REDUNDANT_CHANGE, REPOSITORY_CHANGE, LOCAL_CHANGE 
	 */
	public void filter(boolean onRepository, int type) {
		
		List changes = null;
		if (!onRepository) {
			// filter from ontChangeTT
			changes = this.ontChanges;
		}
		else {
			// filter from repChangeTT
			changes = this.repChanges;			
		}
		// iterate through changes and remove any that match type
		for (Iterator iter = new ArrayList(changes).iterator(); iter.hasNext();) {
			SwoopChange swc = (SwoopChange) iter.next();
			if (swc.isRedundant && type==this.REDUNDANT_CHANGE) changes.remove(swc);
			else if (swc.isOnRepository && type==this.REPOSITORY_CHANGE) changes.remove(swc);
			else if (!swc.isOnRepository && type==this.LOCAL_CHANGE) changes.remove(swc);
		}
		
		// sort changes??
		
		// refresh UI of target after adding changes
		if (!onRepository) this.refreshOntTreeTable();
		else {
			// add (new) repChanges to newCommit node directly
			newCommitNode.children = new Vector();
			for (Iterator iter = changes.iterator(); iter.hasNext();) {
				SwoopChange swc = (SwoopChange) iter.next();
				TreeTableNode swcNode = new TreeTableNode(swc);
				newCommitNode.addChild(swcNode);	
			}
			this.refreshRepTreeTable(true);
		}		
	}

	/*
	 * Update repository treetable
	 * based on changes made in Swoop for ontology
	 * with PHYSICAL uri - ontURI
	 */
	private void updateRepFromLog(URI phyOntURI) {
		// first get ontology whose physical uri matches 
		URI logOntURI = null;
		for (Iterator iter = swoopModel.getOntologies().iterator(); iter.hasNext();) {
			OWLOntology ont = (OWLOntology) iter.next(); 
			try {
				if (ont.getPhysicalURI().equals(phyOntURI)) {
					logOntURI = ont.getLogicalURI();
				}
			}
			catch (OWLException ex) {
				ex.printStackTrace();
			}
		}
		if (logOntURI==null) {
			System.out.println("No Ontology in Swoop has physical URI: "+phyOntURI);
			return;
		}
		
		// get changes from swoopModel.changeCache
		List changes = swoopModel.getChangesCache().getChangeList(logOntURI);
		List allRepChanges = this.getDescendantChanges(repRoot);
		allRepChanges.removeAll(this.getDescendantChanges(newCommitNode));
		for (Iterator iter = new ArrayList(changes).iterator(); iter.hasNext();) {
			SwoopChange swc = (SwoopChange) iter.next();
			if (this.isPresent(allRepChanges, swc)) changes.remove(swc);
		}
		
		this.sortChanges(changes);
		
		// add (new) repChanges to newCommit node directly
		newCommitNode.children = new Vector();
		for (Iterator iter = changes.iterator(); iter.hasNext();) {
			SwoopChange swc = (SwoopChange) iter.next();
			TreeTableNode swcNode = new TreeTableNode(swc);
			newCommitNode.addChild(swcNode);	
		}					
		this.refreshRepTreeTable(true);	
	}
	
	public void modelChanged(ModelChangeEvent event) {
		
		if (event.getType() == ModelChangeEvent.ONTOLOGY_CHANGED) {
			
			if (!advanced && baseOntologyURI!=null) {// && syncOntology!=null) {
				try {
					this.updateRepFromLog(new URI(this.repBaseOntFld.getText()));
				} 
				catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
}
