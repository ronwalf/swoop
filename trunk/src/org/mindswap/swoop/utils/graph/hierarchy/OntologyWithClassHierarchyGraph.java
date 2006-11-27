/*
 * Created on Jul 19, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.mindswap.swoop.utils.graph.hierarchy;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.io.StringWriter;
import java.net.URI;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;

import org.mindswap.swoop.ModelChangeEvent;
import org.mindswap.swoop.SwoopModel;
import org.mindswap.swoop.SwoopModelListener;
import org.mindswap.swoop.reasoner.SwoopReasoner;
import org.mindswap.swoop.renderer.SwoopEntityRenderer;
import org.mindswap.swoop.renderer.entity.ConciseFormatEntityRenderer;
import org.mindswap.swoop.utils.EConnectionOntologyHelper;
import org.mindswap.swoop.utils.graph.hierarchy.classlist.ClassList;
import org.mindswap.swoop.utils.graph.hierarchy.classlist.ClassListHistoryItem;
import org.mindswap.swoop.utils.graph.hierarchy.classlist.ClassListHistoryManager;
import org.mindswap.swoop.utils.graph.hierarchy.classlist.HighlightClassListHistoryItem;
import org.mindswap.swoop.utils.graph.hierarchy.classlist.SelectionClassListHistoryItem;
import org.mindswap.swoop.utils.graph.hierarchy.colors.DefaultColorScheme;
import org.mindswap.swoop.utils.graph.hierarchy.colors.GraphColorScheme;
import org.mindswap.swoop.utils.graph.hierarchy.colors.OverlayGraphScheme;
import org.mindswap.swoop.utils.graph.hierarchy.colors.PartitionFocusColorScheme;
import org.mindswap.swoop.utils.graph.hierarchy.layout.SizeAwareCircleLayout;
import org.mindswap.swoop.utils.graph.hierarchy.popup.ListSelectionPopup;
import org.mindswap.swoop.utils.graph.hierarchy.popup.OntologyGraphAxiomWalker;
import org.mindswap.swoop.utils.graph.hierarchy.ui.CCGraphPanel;
import org.mindswap.swoop.utils.ui.EntityComparator;
import org.mindswap.swoop.utils.ui.SwoopIcons;
import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLEntity;
import org.semanticweb.owl.model.OWLException;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.helper.OntologyHelper;

import edu.uci.ics.jung.graph.Edge;
import edu.uci.ics.jung.graph.Vertex;
import edu.uci.ics.jung.graph.decorators.ConstantVertexAspectRatioFunction;
import edu.uci.ics.jung.graph.decorators.ConstantVertexStringer;
import edu.uci.ics.jung.graph.decorators.EdgeShape;
import edu.uci.ics.jung.graph.decorators.EdgeShapeFunction;
import edu.uci.ics.jung.graph.decorators.EllipseVertexShapeFunction;
import edu.uci.ics.jung.graph.decorators.VertexAspectRatioFunction;
import edu.uci.ics.jung.graph.decorators.VertexFontFunction;
import edu.uci.ics.jung.graph.decorators.VertexPaintFunction;
import edu.uci.ics.jung.graph.decorators.VertexShapeFunction;
import edu.uci.ics.jung.graph.decorators.VertexSizeFunction;
import edu.uci.ics.jung.graph.decorators.VertexStringer;
import edu.uci.ics.jung.graph.impl.DirectedSparseEdge;
import edu.uci.ics.jung.graph.impl.DirectedSparseGraph;
import edu.uci.ics.jung.utils.Pair;
import edu.uci.ics.jung.utils.UserDataContainer;
import edu.uci.ics.jung.visualization.FRLayout;
import edu.uci.ics.jung.visualization.GraphZoomScrollPane;
import edu.uci.ics.jung.visualization.ISOMLayout;
import edu.uci.ics.jung.visualization.Layout;
import edu.uci.ics.jung.visualization.PickSupport;
import edu.uci.ics.jung.visualization.PickedInfo;
import edu.uci.ics.jung.visualization.SpringLayout;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.ZoomPanGraphMouse;
import edu.uci.ics.jung.visualization.contrib.DAGLayout;
import edu.uci.ics.jung.visualization.contrib.KKLayout;

/**
 * @author Dave Wang
 * 
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
public class OntologyWithClassHierarchyGraph extends JPanel 
	implements ActionListener, SwoopModelListener, KeyListener 
{

	class SwoopOntologyVertexComparator implements Comparator {

		public int compare(Object o1, Object o2) {
			try {
				SwoopOntologyVertex ontv1 = (SwoopOntologyVertex) o1;
				SwoopOntologyVertex ontv2 = (SwoopOntologyVertex) o2;
				String name1 = ((OntologyGraphNode) ontv1.getUserDatum(OntologyWithClassHierarchyGraph.DATA)).getURI().toString();
				String name2 = ((OntologyGraphNode) ontv2.getUserDatum(OntologyWithClassHierarchyGraph.DATA)).getURI().toString();
				return name1.compareTo(name2);
			} catch (Exception e) {
				System.out.println(o1.getClass().getName());
				System.out.println(o2.getClass().getName());
				e.printStackTrace();
			}
			return -1;
		}

	}

	class OntologyPanel extends JPanel implements ActionListener {
		private VisualizationViewer myVV = null;

		private SwoopOntologyVertex myVertex = null;

		private JCheckBox myCheckBox = null;

		public OntologyPanel(SwoopOntologyVertex vertex, VisualizationViewer vv) {
			super();
			myVertex = vertex;
			myCheckBox = new JCheckBox(vertex.toString());
			myCheckBox.addActionListener(this);
			myCheckBox.getModel().setSelected(true);
			myCheckBox.setBackground(Color.WHITE);
			myVV = vv;

			setLayout(new BorderLayout());
			add(myCheckBox, BorderLayout.WEST);

			setMaximumSize(new Dimension(150, 26));
			setPreferredSize(new Dimension(150, 26));
			setSize(new Dimension(150, 26));
			setVisible(true);
			this.setBackground(Color.WHITE);
		}

		public void actionPerformed(ActionEvent e) {
			myVertex.setVisible(myCheckBox.getModel().isSelected());
			myVV.repaint();
		}
	}
	
	// just an index variable used a key for OntologyGraphNode for a Vertex
	public static final String DATA = "DATA";

	// just a name for the popup meny for Vetices
	public static final String VERTEX_POPUP_NAME = "Vertex Popup Menu";

	public static final String CLASS_POPUP_NAME = "Class Popup Menu";

	// some string constants for menu items
	public static final String SHOW_SUBCLASS_AXIOMS = "Show Subclass Axioms";

	public static final String SHOW_DOMAIN_AXIOMS = "Show Domain Axioms";

	public static final String SHOW_RANGE_AXIOMS = "Show Range Axioms";

	public static final String SHOW_INDIVIDUAL_AXIOMS = "Show Individual Axioms";

	public static final String SUBCLASS_AXIOM_LIST = "Subclass Axiom List";

	public static final String DOMAIN_AXIOM_LIST = "Domain Axiom List";

	public static final String RANGE_AXIOM_LIST = "Range Axiom List";

	public static final String INDIVIDUAL_LIST = "Individual List";

	public static final String SHOW_RELATED_CLASSES = "Show Related Classes";

	public static final int BASIC_COLOR = 0;

	public static final int PARTITION_FOCUS_COLOR = 1;

	public static final int OVERLAY_GRAPH_COLOR = 2;

	public static final int NORMAL_VIEW = 0;

	public static final int RELATION_VIEW = 1;
	
	public static final int CONCISEVIEW = 1;

	private static int MIN = 10;

	private static int MAX = 100;

	private static final EdgeShapeFunction LINE = new EdgeShape.Line();

	private static final EdgeShapeFunction CURVE = new EdgeShape.QuadCurve();

	private final EdgeShapeFunction CURVED_LINE = new EdgeShapeFn();

	private final UserDataContainer.CopyAction SHARE = new UserDataContainer.CopyAction.Shared();

	private final VertexStringer SHORT_LABEL = new VertexLabel(true);

	private final VertexStringer LONG_LABEL = new VertexLabel(false);

	private final VertexStringer NO_LABEL = new ConstantVertexStringer("");

	protected DirectedSparseGraph graph;

	protected OntologyVisualizationViewer vv;

	private OntologyWithClassHierarchyRenderer pr;

	private VertexSize vSize = new VertexSize();

	private VertexFont vFont = new VertexFont();

	private JCheckBox v_content = null;

	private JSplitPane mySplitPane = null; // separates selection panes on left
										   // and visualization graph on right

	private JEditorPane myEntityRendererPane = null;
	
	private JScrollPane myListScrollPane = null;

	private JList myClassList = null;

	private JLabel myCurrentClassLabel = null;

	private JButton myForwardListButton = null;

	private JButton myBackListButton = null;

	private JButton myFocusButton = null;
	private JButton myViewButton  = null;
	
	private int currentView = NORMAL_VIEW;

	private JTabbedPane myTabs = null;
	
	private JScrollPane myOntologyScrollPane = null;

	private JSplitPane myLeftSplitPane = null;

	private JButton mySearchButton = null;

	private JTextField mySearchField = null;

	private JPopupMenu myVertexPopupMenu = null;

	private JMenuItem myShowSubclassAxioms = null;

	private JMenuItem myShowDomainAxioms = null;

	private JMenuItem myShowRangeAxioms = null;

	private JMenuItem myShowIndividualAxioms = null;

	private JPopupMenu myClassPopupMenu = null;

	private JMenuItem myShowRef = null;

	private ClassListHistoryManager myClassListHistoryManager = null;

	private VertexAspectRatioFunction vAspect = new ConstantVertexAspectRatioFunction(
			1.0f);

	private VertexShapeFunction vShape = new EllipseVertexShapeFunction(vSize,
			vAspect);

	private EdgeShapeFunction eShape = CURVED_LINE;

	private OntologyWithClassHierarchyGraphProperties myProps = null;

	private SwoopModel myModel = null;

	private OWLClass owlThing = null;

	private OWLClass owlNothing = null;

	// vertex color objects
	private BasicVertexColor myBasicColor = null;

	// color scheme that oversees the colors in the graph
	private GraphColorScheme myColorScheme = null;

	// OverlayGraph object
	private OverlayGraph myOverlayGraph = null;

	private Vector partitions = null;

	private HashMap reasonerMap = null;

	// partitions is a vector of OWLOntologies
	public OntologyWithClassHierarchyGraph(SwoopModel model, Vector partitions) {
		this.myModel = model;
		this.myModel.addListener(this);
		this.myProps = new OntologyWithClassHierarchyGraphProperties(myModel,
				partitions);
		this.graph = new DirectedSparseGraph();
		this.myColorScheme = new DefaultColorScheme();
		//this.myOverlayGraph = new OverlayGraph(this.graph, this.myModel, this);
		this.reasonerMap = new HashMap();
		this.partitions = partitions;

		Set allOnts = new HashSet();
		for (Iterator i = partitions.iterator(); i.hasNext();) {
			OWLOntology ont = (OWLOntology) i.next();
			allOnts.add(ont);
			boolean isNotEconn = ont.getForeignOntologies().isEmpty();
			if (isNotEconn) // get import closure
			{
				try {
					allOnts.addAll(OntologyHelper.importClosure(ont));
				} catch (OWLException e) {
					e.printStackTrace();
				}
			} else // get econnected closure
			{
				allOnts.addAll(EConnectionOntologyHelper.getEConnectedClosure( new HashSet(), myModel, ont) );
			}
		}
		this.partitions.clear();
		for (Iterator iter = allOnts.iterator(); iter.hasNext();)
			this.partitions.add(iter.next());

		// now call swoopModel to create reasoner instance for each ontology,
		// process ontology and put it back in the vector: reasoners
		// finally, swoopModel directly notifies this class instance
		// see modelChanged(..) in this class
		myModel.callFromMotherShip(allOnts, reasonerMap);
	}

	/*
	 * display mothership UI after swoopModel has processed ontologies that are
	 * going to be displayed and updates the reasoners vector accordingly
	 */
	private void displayMotherShip() {
		// preserve the currently selected ontology
		for (Iterator i = partitions.iterator(); i.hasNext();) {
			OWLOntology ont = (OWLOntology) i.next();
			// if a node containing this ontology already exists, we skip it
			if (graph.getUserDatum(ont) != null)
				continue;
			OntologyGraphNode node = buildOntologyNode(ont);
			addToGraph(graph, node);
		}

		this.setupUI();
		this.getParent().validate();
		vv.autoPanZoom();
		
		//myOverlayGraph.precomputeAllRelations(); // precompute for the relations
												 // in the class trees
	}

	/*
	 * Reads in an OWLOntology and build a ontology node with a class tree
	 * inside, where the tree represents the subclass tree structure :) The
	 * class tree is a tree by current SWOOP reasoner (so told with no reasoner,
	 * but inferred if with Pellet).
	 *  
	 */
	private OntologyGraphNode buildOntologyNode(OWLOntology ontology) {
		try {
			owlThing = ontology.getOWLDataFactory().getOWLThing();
			owlNothing = ontology.getOWLDataFactory().getOWLNothing();
			ClassTreeNode root = buildRootTreeNode(ontology);
			return new OntologyGraphNode(ontology, root, this);
		} catch (OWLException e) {
			e.printStackTrace();
		}

		return null;
	}

	public ClassTreeNode buildRootTreeNode(OWLOntology ontology)
			throws OWLException {
		SwoopReasoner reasoner = (SwoopReasoner) reasonerMap.get(ontology
				.getURI());
		Set set = reasoner.equivalentClassesOf(owlThing);
		set.add(owlThing);
		ClassTreeNode root = buildTreeNode( ontology, set, 0);
		return root;
	}

	public ClassTreeNode buildTreeNode(OWLOntology ontology, Set concepts, int depth )
			throws OWLException {

		if (concepts.contains(owlNothing))
			return null;

		// would the set ever be empty?
		if (concepts.isEmpty())
			return null;

		OWLClass clss = (OWLClass) concepts.iterator().next();

		// hacky hacky hack!!!!! (BUGBUGBUG) ~ Bernardo?
		if (((ontology.isForeign(clss)))
				&& (!clss.getURI().toString().equals(
						SwoopEntityRenderer.OWL_THING))) {
			return null;
		}

		ClassTreeNode root = createNode( clss, depth );

		SwoopReasoner reasoner = (SwoopReasoner) this.reasonerMap.get(ontology
				.getURI());
		Object obj = concepts.iterator().next();

		if (obj instanceof OWLClass) 
		{
			OWLClass c = (OWLClass) obj;
			Set subs = reasoner.subClassesOf(c);
			Iterator i = subs.iterator();
			
			int maxSubtreeDepth = -2;
			while (i.hasNext()) 
			{
				Set set = (Set) i.next();
				if (set.contains(c))
					continue;

				//SortedSet sortedSet = orderedEntities(set);
				ClassTreeNode node = buildTreeNode(ontology, set, depth + 1);
				
				// do not add owl:Nothing to the tree
				if (node != null) 
				{
					root.addChild(node);
					if ( node.getSubtreeDepth() > maxSubtreeDepth )
						maxSubtreeDepth = node.getSubtreeDepth();
				}
			}
			root.setSubtreeDepth( maxSubtreeDepth + 1 );
		}
		return root;
	}

	private ClassTreeNode createNode(OWLEntity entity, int depth ) {
		try {
			return new ClassTreeNode(myModel, entity.getURI(), depth);
		} catch (OWLException e) {
			e.printStackTrace();
		}
		return null;
	}

	private SortedSet orderedEntities(Set entities) 
	{
		SortedSet ss = new TreeSet(EntityComparator.INSTANCE);
		ss.addAll(entities);
		return ss;
	}

	public void showOntologyPopupMenu(MouseEvent e, Vertex pickedVertex) {
		myVertexPopupMenu.show(mySplitPane.getRightComponent(), e.getX(), e.getY());
	}

	public void showClassPopupMenu(MouseEvent e, ClassTreeNode node) {
		myClassPopupMenu.show(mySplitPane.getRightComponent(), e.getX(), e.getY());
	}

	public void populateClassList(ClassTreeNode node) {

		myClassList.setListData(node.getChildren());
		String label = null;
		// If selected node is top node (owl:Thing), use the ontology name as
		// label
		if (node.getURI().toString().equals("http://www.w3.org/2002/07/owl#Thing"))
			label = node.getOntologyNode().toString();
		else
			label = node.toString();

		// if current selection = the new selection, do nothing
		ClassListHistoryItem item = myClassListHistoryManager.getCurrent();
		if (item != null) {
			if (myClassListHistoryManager.getCurrent().getLabel().equals(label))
				return;
		}

		label = label + " (" + node.getChildren().size() + ")";
		myClassListHistoryManager.addItem(new SelectionClassListHistoryItem(
				this, myClassList.getModel(), node, label));
		myCurrentClassLabel.setText(label);
		this.myForwardListButton.setEnabled(false);
		this.myBackListButton.setEnabled(true);
		
		renderEntity();
	}

	public void populateClassListWithSearchTerm(Set nodes, String subText) 
	{
		myClassList.setListData(new Vector(nodes));
		String label = "search on: \"" + subText + "\"" + " (" + nodes.size() + ")";
		myClassListHistoryManager.addItem(new HighlightClassListHistoryItem(
				this, myClassList.getModel(), nodes, label));
		myCurrentClassLabel.setText(label);
		vv.repaint();
		this.myForwardListButton.setEnabled(false);
		this.myBackListButton.setEnabled(true);
		renderEntity();
	}

	public void loadClassListHistory(ClassListHistoryItem item) 
	{
		myCurrentClassLabel.setText(item.getLabel());
		myClassList.setModel(item.getModel());
		renderEntity();
	}

	private void forwardClassListHistory() 
	{
		ClassListHistoryItem item = myClassListHistoryManager.goForward();
		if (item != null) {
			item.executeLoadHistory();
			myBackListButton.setEnabled(!myClassListHistoryManager.isAtBeginning());
			myForwardListButton.setEnabled(!myClassListHistoryManager.isAtEnd());
			renderEntity();
		}
	}

	private void backClassListHistory() {
		ClassListHistoryItem item = myClassListHistoryManager.goBack();
		if (item != null) {
			item.executeLoadHistory();
			myBackListButton.setEnabled(!myClassListHistoryManager.isAtBeginning());
			myForwardListButton.setEnabled(!myClassListHistoryManager.isAtEnd());
			renderEntity();
		}
	}

	protected void setupUI() {

		myClassListHistoryManager = new ClassListHistoryManager();

		pr = new OntologyWithClassHierarchyRenderer();

		Layout layout = new SizeAwareCircleLayout(graph);
		vv = new OntologyVisualizationViewer(myModel, this, layout, pr);
		vv.setPickSupport(new SwoopPickSupport(vv));

		myBasicColor = new BasicVertexColor(vv);

		pr.setVertexPaintFunction(myBasicColor);
		pr.setVertexShapeFunction(vShape);
		pr.setVertexStringer(NO_LABEL);
		pr.setVertexLabelCentering(true);
		pr.setVertexFontFunction(vFont);
		pr.setEdgeShapeFunction(eShape);

		//vSize.setGraph( graph );

		CCGraphPanel gPanel = new CCGraphPanel(vv);
		
		ZoomPanGraphMouse gm = new ZoomPanGraphMouse(vv);
		vv.setGraphMouse(gm);
		vv.setToolTipListener(new VertexTips());
		vv.setBackground(Color.white);

		myOntologyScrollPane = getOntologyListPane();

		myClassList = new ClassList(this);

		myListScrollPane = new JScrollPane();
		myListScrollPane.getViewport().setView(myClassList);

		JPanel org1 = new JPanel();
		org1.setLayout(new BorderLayout());
		org1.add(getButtonPanel(), BorderLayout.NORTH);
		JPanel org2 = new JPanel();                    // contains label
		org2.setLayout(new BorderLayout());
		org2.add(getLabelPanel(), BorderLayout.NORTH);
		JPanel org3 = new JPanel();                    // contains classlist
		org3.setLayout(new BorderLayout());
		org3.add(myListScrollPane, BorderLayout.CENTER);
		org3.add(getSearchPanel(), BorderLayout.SOUTH);

		org2.add(org3, BorderLayout.CENTER);

		myTabs = new JTabbedPane();
		myTabs.add("Class List ", org2 );
		myTabs.add("Concise Format", this.getRendererPane() );
		org1.add( myTabs, BorderLayout.CENTER );
		
		myLeftSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, true, myOntologyScrollPane, org1);
		myLeftSplitPane.setOneTouchExpandable(true);

		mySplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, false,
				myLeftSplitPane, gPanel);
		mySplitPane.setOneTouchExpandable(true);
		setLayout(new BorderLayout());
		add(mySplitPane);
		add(getControlPanel(), BorderLayout.SOUTH);

		// set up popup menus
		myVertexPopupMenu = getVertexPopupMenu();
		myClassPopupMenu = getClassPopupMenu();
		
		this.addKeyListener( this );
	}

	protected JPanel getButtonPanel() {
		JPanel buttonPanel = new JPanel();

		// load history button images (code from TermsDisplay)
		SwoopIcons swoopIcons = new SwoopIcons();
		ImageIcon prevIcon = (ImageIcon) SwoopIcons.prevIcon;
		if (SwoopIcons.prevIcon != null)
			myBackListButton = new JButton(prevIcon);
		else
			myBackListButton = new JButton("<");
		ImageIcon nextIcon = (ImageIcon) SwoopIcons.nextIcon;
		if (SwoopIcons.nextIcon != null)
			myForwardListButton = new JButton(nextIcon);
		else
			myForwardListButton = new JButton(">");

		myFocusButton = new JButton("Focus");

		myForwardListButton.addActionListener(this);
		myBackListButton.addActionListener(this);
		myFocusButton.addActionListener(this);

		buttonPanel.add(myBackListButton);
		buttonPanel.add(myForwardListButton);
		buttonPanel.add(new JLabel("  "));
		buttonPanel.add(myFocusButton);

		JPanel buttonContainer = new JPanel();
		buttonContainer.setLayout(new BorderLayout());
		buttonContainer.add(buttonPanel, BorderLayout.WEST);

		return buttonContainer;
	}

	protected JPanel getLabelPanel() {
		myCurrentClassLabel = new JLabel(" ");
		JPanel labelPanel = new JPanel();
		myCurrentClassLabel.setFont(new Font("Tahoma", Font.BOLD, 11));
		myCurrentClassLabel.setOpaque(true);
		myCurrentClassLabel.setBackground(new Color(10, 25, 84));
		myCurrentClassLabel.setForeground(Color.WHITE);

		//labelPanel.setBackground( Color.WHITE );
		labelPanel.setLayout(new BorderLayout());
		labelPanel.add(myCurrentClassLabel, BorderLayout.CENTER);

		return labelPanel;
	}

	protected JScrollPane getOntologyListPane() {
		JPanel hostPanel = new JPanel();
		hostPanel.setBackground(Color.WHITE);
		hostPanel.setLayout(new BoxLayout(hostPanel, BoxLayout.Y_AXIS));

		Set ontologySet = this.graph.getVertices();
		TreeSet orderedOntologySet = new TreeSet(
				new SwoopOntologyVertexComparator());
		orderedOntologySet.addAll(ontologySet);
		for (Iterator it = orderedOntologySet.iterator(); it.hasNext();) {
			SwoopOntologyVertex v = (SwoopOntologyVertex) it.next();
			OntologyPanel aPanel = new OntologyPanel(v, vv);
			hostPanel.add(aPanel);
		}
		return new JScrollPane(hostPanel);
	}

	protected JPanel getSearchPanel() {
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		mySearchButton = new JButton("Search");
		mySearchField = new JTextField();
		panel.add(mySearchField, BorderLayout.CENTER);
		panel.add(mySearchButton, BorderLayout.EAST);
		mySearchButton.addActionListener(this);

		return panel;
	}

	protected JPanel getControlPanel() {
		JPanel controlPanel = new JPanel();
		controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.Y_AXIS));

		String[] layouts = { "KK Layout", "FR Layout", "Circle Layout",
				"Spring Layout", "ISOM Layout", "DAG Layout" };
		JComboBox jcb = new JComboBox(layouts);
		jcb.setActionCommand("layout");
		jcb.addActionListener(this);
		jcb.setSelectedItem(myProps.getPreferredLayout());
		Box layoutPanel = Box.createHorizontalBox();
		layoutPanel.add(new JLabel("Graph Layout"));
		layoutPanel.add(jcb);

		Box labelPanel = Box.createHorizontalBox();
		
		JCheckBox v_labels = new JCheckBox("Show partition labels");
		v_labels.setAlignmentX(Component.LEFT_ALIGNMENT);
		v_labels.setActionCommand("label");
		v_labels.addActionListener(this);
		v_labels.setSelected(false);

		JCheckBox v_font = new JCheckBox("Use bold font");
		v_font.setActionCommand("font");
		v_font.addActionListener(this);
		v_font.setSelected(false);
		labelPanel.add(v_labels);
		labelPanel.add(v_font);

		JCheckBox v_size = new JCheckBox(
				"Scale nodes with respect to number of entities in the partition");
		v_size.setAlignmentX(Component.LEFT_ALIGNMENT);
		v_size.addActionListener(this);
		v_size.setActionCommand("scale");
		v_size.setSelected(true);

		JCheckBox eShape = new JCheckBox("Do not overlap inverse edges");
		eShape.setAlignmentX(Component.LEFT_ALIGNMENT);
		eShape.addActionListener(this);
		eShape.setActionCommand("inverseEdge");
		eShape.setSelected(true);

		v_content = new JCheckBox("Draw content of the nodes");
		v_content.setAlignmentX(Component.LEFT_ALIGNMENT);
		v_content.addActionListener(this);
		v_content.setActionCommand("content");
		v_content.setSelected(true);

		JButton plus = new JButton("+");
		plus.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				vv.scale(1.1, 1.1);
			}
		});
		JButton minus = new JButton("-");
		minus.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				vv.scale(0.9, 0.9);
			}
		});
		JLabel zoomLabel = new JLabel("Zoom");
		zoomLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
		Box zoomPanel = Box.createHorizontalBox();
		zoomPanel.add(zoomLabel);
		zoomPanel.add(plus);
		zoomPanel.add(minus);

		//controlPanel.add(labelPanel);
		//controlPanel.add(v_size);
		//controlPanel.add(v_content);
		//controlPanel.add(eShape);
		controlPanel.add(zoomPanel);
		//controlPanel.add(layoutPanel);

		return controlPanel;
	}
	
	protected JComponent getRendererPane()
	{
		myEntityRendererPane = new JEditorPane();
		myEntityRendererPane.setContentType("text/html");
		myEntityRendererPane.setText("No class selected.");
		myEntityRendererPane.setEditable( false );
		JScrollPane scrollpane = new JScrollPane();
		scrollpane.getViewport().setView( myEntityRendererPane );
		return scrollpane;
	}
	
	private void renderEntity()
	{
		// only render if concise format view is currently selected
		if ( myTabs.getSelectedIndex() != CONCISEVIEW)
			return; 
		ConciseFormatEntityRenderer rend = new ConciseFormatEntityRenderer();
		ClassTreeNode node = vv.getCurrentSelectedNode();
		URI uri = node.getURI();
		StringWriter writer = new StringWriter();
		try 
		{
			OWLOntology ont = myModel.getOntology(node.getOntologyNode().getURI());
			if ( myModel.getSelectedOntology() != ont)
				myModel.setSelectedOntology( ont );
			
			// if uri is owl:Thing, we create the entity.  Else, we get it from ontologies
			
			OWLEntity ent = ont.getOWLDataFactory().getOWLThing();			
			if ( !uri.toString().equals( ent.getURI().toString() ) )			
				ent = myModel.getEntity( ont ,uri, true);
				
			rend.render( ent, myModel, writer );
			myEntityRendererPane.setText( writer.toString() );
			myEntityRendererPane.setCaretPosition(0);
		} 
		catch (Exception e) 
		{ 
			e.printStackTrace();
			myEntityRendererPane.setText("I am sorry.  I don't understand " + uri);
		}
		
		//safsads
	}
	
	protected JPopupMenu getVertexPopupMenu() {
		JPopupMenu pop = new JPopupMenu(VERTEX_POPUP_NAME);
		myShowSubclassAxioms = new JMenuItem(SHOW_SUBCLASS_AXIOMS);
		myShowDomainAxioms = new JMenuItem(SHOW_DOMAIN_AXIOMS);
		myShowRangeAxioms = new JMenuItem(SHOW_RANGE_AXIOMS);
		myShowIndividualAxioms = new JMenuItem(SHOW_INDIVIDUAL_AXIOMS);

		myShowSubclassAxioms.addActionListener(this);
		myShowDomainAxioms.addActionListener(this);
		myShowRangeAxioms.addActionListener(this);
		myShowIndividualAxioms.addActionListener(this);

		pop.add(myShowSubclassAxioms);
		pop.add(myShowDomainAxioms);
		pop.add(myShowRangeAxioms);
		pop.add(myShowIndividualAxioms);

		return pop;
	}

	protected JPopupMenu getClassPopupMenu() {
		JPopupMenu pop = new JPopupMenu(VERTEX_POPUP_NAME);
		myShowRef = new JMenuItem(SHOW_RELATED_CLASSES);
		myShowRef.addActionListener(this);

		pop.add(myShowRef);

		return pop;
	}

	protected void searchForNodes(String subText) {
		subText = subText.trim();
		subText = subText.toLowerCase();

		if (subText.equals(""))
			return;

		HashSet treeNodes = new HashSet();
		Set vertexSet = graph.getVertices();

		for (Iterator it = vertexSet.iterator(); it.hasNext();) {
			OntologyGraphNode graphNode = (OntologyGraphNode) (((Vertex) it
					.next()).getUserDatum(OntologyWithClassHierarchyGraph.DATA));
			Set s = graphNode.matchNodeWithShortName(subText);
			treeNodes.addAll(s);
		}
		vv.setHighlightedNode(treeNodes, subText);
		mySearchField.setText("");
	}

	public OntologyVisualizationViewer getVV() {
		return vv;
	}

	public void actionPerformed(ActionEvent e) {
		String cmd = e.getActionCommand();
		//class history managing
		if (e.getSource() instanceof JButton) {
			JButton source = (JButton) e.getSource();
			if (source == myBackListButton) {
				backClassListHistory();
			} else if (source == myForwardListButton) {
				forwardClassListHistory();
			} 
			else if (source == myViewButton) 
			{				
				if (currentView == NORMAL_VIEW)
					this.setColorMode(OVERLAY_GRAPH_COLOR);
				else
					this.setColorMode(BASIC_COLOR);
			}
			else if (source == myFocusButton) 
			{
				ClassTreeNode node = vv.getCurrentSelectedNode();
				vv.panZoomToFitNode( node, node.getOntologyNode().getTreeNode() );
			}
			else if (source == mySearchButton) 
			{
				searchForNodes(mySearchField.getText());
			}
			return;
		}

		// menu item (and popup menu items)
		if (e.getSource() instanceof JMenuItem) {
			JMenuItem src = (JMenuItem) e.getSource();
			SwoopOntologyVertex picked = vv.getSelectedVertex();
			ClassTreeNode node = vv.getRightSelectedNode();

			// Vertex popup menu items
			if (src == myShowSubclassAxioms) {
				// ontain subclass axioms (Vector of SubclassAxiomContainer
				OntologyGraphAxiomWalker walker = new OntologyGraphAxiomWalker(
						picked, myModel);
				Vector subAxioms = walker.getSubclassAxioms();
				ListSelectionPopup popup = new ListSelectionPopup(this,
						SUBCLASS_AXIOM_LIST, subAxioms);
			} else if (src == myShowDomainAxioms) {
			} else if (src == myShowRangeAxioms) {
			} else if (src == myShowIndividualAxioms) {
			}
			// Class popup menu items
			else if (src == myShowRef) {
				System.out.println(node.getURI());
				myOverlayGraph.addNode(node);
				this.setColorMode(OVERLAY_GRAPH_COLOR);
			}
			return;
		}

		// controlpanel stuff
		if (cmd.equals("label")) {
			VertexStringer vs = ((JCheckBox) e.getSource()).isSelected() ? SHORT_LABEL
					: NO_LABEL;
			pr.setVertexStringer(vs);
		} else if (cmd.equals("inverseEdge")) {
			EdgeShapeFunction es = ((JCheckBox) e.getSource()).isSelected() ? CURVED_LINE
					: LINE;
			pr.setEdgeShapeFunction(es);
		} else if (cmd.equals("scale")) {
			vSize.setScaling(((JCheckBox) e.getSource()).isSelected());
			if (vSize.getScaling() == false) {
				pr.setIsDrawContent(false);
				v_content.setSelected(false);
			}
		} else if (cmd.equals("font")) {
			vFont.setBold(((JCheckBox) e.getSource()).isSelected());
		} else if (cmd.equals("content")) {
			pr.setIsDrawContent(((JCheckBox) e.getSource()).isSelected());
		} else if (cmd.equals("layout")) {
			String layoutName = ((JComboBox) e.getSource()).getSelectedItem()
					.toString();
			if (layoutName.startsWith("KK"))
				vv.setGraphLayout(new KKLayout(graph));
			else if (layoutName.startsWith("Spring"))
				vv.setGraphLayout(new SpringLayout(graph));
			else if (layoutName.startsWith("Circle"))
				vv.setGraphLayout(new SizeAwareCircleLayout(graph));
			else if (layoutName.startsWith("FR"))
				vv.setGraphLayout(new FRLayout(graph));
			else if (layoutName.startsWith("ISOM"))
				vv.setGraphLayout(new ISOMLayout(graph));
			else if (layoutName.startsWith("DAG"))
				vv.setGraphLayout(new DAGLayout(graph));
			else
				throw new RuntimeException("Unknown layout");
		}

		vv.repaint();
	}

	public void setColorMode(int mode) {
		if (mode == OntologyWithClassHierarchyGraph.PARTITION_FOCUS_COLOR) {
			myColorScheme = new PartitionFocusColorScheme();
			//pr.setVertexPaintFunction( myPartitionFocusColor );
		} else if (mode == OntologyWithClassHierarchyGraph.OVERLAY_GRAPH_COLOR) {
			myColorScheme = new OverlayGraphScheme();
			currentView = RELATION_VIEW;
		} else {
			myColorScheme = new DefaultColorScheme();
			currentView = NORMAL_VIEW;
			//this.myOverlayGraph.clear(); // reset edges/graphs
			//pr.setVertexPaintFunction( myBasicColor );
		}
		vv.repaint();
	}

	public void resetPartitionFocus() {
		Set vertexSet = this.graph.getVertices();
		for (Iterator it = vertexSet.iterator(); it.hasNext();) {
			SwoopOntologyVertex v = (SwoopOntologyVertex) it.next();
			v.setPartitionState(SwoopOntologyVertex.NONE);
			v.setPartitionDirtyBit(false);
		}
	}

	public void setPartitionFocus(OWLOntology ont) {
		SwoopOntologyVertex v = (SwoopOntologyVertex) this.graph
				.getUserDatum(ont);
		v.setPartitionState(SwoopOntologyVertex.FOCUS);
		v.setPartitionDirtyBit(true);
		LinkedList upList = new LinkedList();
		upList.addAll(v.getPredecessors());
		LinkedList downList = new LinkedList();
		downList.addAll(v.getSuccessors());
		markUpStream(upList);
		markDownStream(downList);
	}

	protected void markUpStream(LinkedList list) {
		if (list.isEmpty())
			return;
		SwoopOntologyVertex head = (SwoopOntologyVertex) list.removeFirst();
		head.setPartitionState(SwoopOntologyVertex.UPSTREAM);
		head.setPartitionDirtyBit(true);
		Set incomingNeighbors = head.getPredecessors();
		for (Iterator it = incomingNeighbors.iterator(); it.hasNext();) {
			SwoopOntologyVertex n = (SwoopOntologyVertex) it.next();
			if (n.isPartitionStateSet) // if the list already contains it...
									   // (cycles)
				continue;
			else
				list.add(n);
		}
		markUpStream(list);
	}

	protected void markDownStream(LinkedList list) {
		if (list.isEmpty())
			return;
		SwoopOntologyVertex head = (SwoopOntologyVertex) list.removeFirst();
		head.setPartitionState(SwoopOntologyVertex.DOWNSTREAM);
		head.setPartitionDirtyBit(true);
		Set outgoingNeighbors = head.getSuccessors();
		for (Iterator it = outgoingNeighbors.iterator(); it.hasNext();) {
			SwoopOntologyVertex n = (SwoopOntologyVertex) it.next();
			if (n.isPartitionStateSet) // if the list already contains it...
									   // (cycles)
				continue;
			else
				list.add(n);
		}
		markDownStream(list);
	}

	protected SwoopOntologyVertex addToGraph(DirectedSparseGraph graph, OntologyGraphNode node) 
	{
		SwoopOntologyVertex vertex = (SwoopOntologyVertex) graph.getUserDatum(node.getOntology());

		if (vertex == null) 
		{
			vertex = new SwoopOntologyVertex(this);
			vertex.setUserDatum(DATA, node, SHARE); // vertex.DATA =OntologyGraphNode
			graph.setUserDatum(node, vertex, SHARE); // graph.OntologyNode = SwoopOntologyVertex
			graph.setUserDatum(node.getOntology(), vertex, SHARE); // graph.onto = vertex (unique identifier)
			graph.addVertex(vertex); // add vertex to graph

			OWLOntology ont = node.getOntology();
			Collection linkedOnts = myProps.getLinkedElements(ont);
			for (Iterator i = linkedOnts.iterator(); i.hasNext();) 
			{
				Object linkedObj = i.next();
				OWLOntology linkedOnt = (OWLOntology) linkedObj;
				SwoopOntologyVertex linkedVertex = addToGraph(graph, buildOntologyNode(linkedOnt));
				DirectedSparseEdge edge = new DirectedSparseEdge(vertex, linkedVertex);
				graph.addEdge(edge);
			}
		}
		return vertex;
	}

	public DirectedSparseGraph getVisualGraph() 
	{ return graph; }

	public SwoopModel getModel() {
		return myModel;
	}

	public GraphColorScheme getColorScheme() {
		return myColorScheme;
	}

	public OverlayGraph getOverlayGraph() {
		return myOverlayGraph;
	}

	private final class VertexSize implements VertexSizeFunction {
		boolean scale = true;

		int maxSize = Integer.MIN_VALUE;

		int minSize = Integer.MAX_VALUE;

		double factor = 1.0;

		public VertexSize() {
		}

		/*
		 * public void setGraph( Graph g ) { for(Iterator i =
		 * g.getVertices().iterator(); i.hasNext();) { Vertex vertex = (Vertex)
		 * i.next(); int size = ((TestGraphNode)vertex.getUserDatum( DATA
		 * )).getSize() ; maxSize = Math.max( maxSize, size ); minSize =
		 * Math.min( minSize, size ); }
		 * 
		 * if( maxSize == minSize ) factor = 0.0; else factor = (double) (MAX -
		 * MIN) / (maxSize - minSize); }
		 */

		public void setScaling(boolean scale) {
			this.scale = scale;
		}

		public boolean getScaling() {
			return scale;
		}

		public int getSize(Vertex vertex) {
			if (scale) {
				//int size = ((TestGraphNode)vertex.getUserDatum( DATA
				// )).getSize() ;
				//return ((int) ((size - minSize) * factor)) + MIN;
				return ((OntologyGraphNode) vertex.getUserDatum(DATA))
						.getDiameter();
			} else
				return MIN;
		}
	}

	private class VertexLabel implements VertexStringer {
		private boolean shortLabel;

		public VertexLabel(boolean qname) {
			this.shortLabel = qname;
		}

		public String getLabel(Vertex vertex) {
			OntologyGraphNode node = (OntologyGraphNode) vertex
					.getUserDatum(DATA);

			if (shortLabel)
				return myProps.getShortName(node.getOntology());
			else
				return myProps.getLongName(node.getOntology());
		}
	}

	public class VertexTips implements VisualizationViewer.ToolTipListener {
		public VertexTips() {
		}

		// shows name of the ontology in graph vertex
		//   or if mouse is over a class inside the ontology, show the short form
		// name
		//	 of the class
		public String getToolTipText(MouseEvent e) {
			PickSupport pickSupport = vv.getPickSupport();
			Point2D p = vv.transform(e.getPoint());

			Vertex v = pickSupport.getVertex(p.getX(), p.getY());
			if (v != null) {
				OntologyWithClassHierarchyRenderer rend = (OntologyWithClassHierarchyRenderer) vv
						.getRenderer();
				if (rend.getIsDrawContent()) {
					ClassTreeNode topNode = ((OntologyGraphNode) v
							.getUserDatum(OntologyWithClassHierarchyGraph.DATA))
							.getTreeNode();
					ClassTreeNode selectedNode = topNode.getSelectedChild(p);
					if (selectedNode != null) {
						//ConciseFormatEntityRenderer cfer = new
						// ConciseFormatEntityRenderer();
						//ConciseFormatVisitor vis = new ConciseFormatVisitor(
						// cfer, myModel);
						//vis.reset();
						//selectedNode.get
						return "<html> <b> &nbsp;" + selectedNode.toString() + "</b> <br>" +
						       "&nbsp; &nbsp; depth: " + selectedNode.getDepth() + "</b> <br>" +
							   "&nbsp; &nbsp; size: "  + (selectedNode.getSubTreeSize() ) + "</b> <br>" +
							   " </html>";
					}
				}

				return LONG_LABEL.getLabel(v);
			} else {
				Edge edge = pickSupport.getEdge(p.getX(), p.getY());
				if (edge != null) {
					return edge.toString();
				}

				return "<html><center>Use the mouse wheel to zoom<p>Click and Drag the mouse to pan</center></html>";
			}
		}
	}

	private final class BasicVertexColor implements VertexPaintFunction {
		protected PickedInfo pi;

		public BasicVertexColor(VisualizationViewer vv) {
			this.pi = vv.getPickedState();
		}

		public Paint getDrawPaint(Vertex v) {
			return pi.isPicked(v) ? myColorScheme
					.getOntologyNodeSelectOutlineColor((SwoopOntologyVertex) v)
					: myColorScheme
							.getOntologyNodeOutlineColor((SwoopOntologyVertex) v);
		}

		public Paint getFillPaint(Vertex v) {
			return myColorScheme
					.getOntologyNodeFillColor((SwoopOntologyVertex) v);
		}
	}

	private final static class VertexFont implements VertexFontFunction {
		protected boolean bold = false;

		Font f = new Font("Helvetica", Font.PLAIN, 12);

		Font b = new Font("Helvetica", Font.BOLD, 12);

		public void setBold(boolean bold) {
			this.bold = bold;
		}

		public Font getFont(Vertex v) {
			return bold ? b : f;
		}
	}

	private class EdgeShapeFn implements EdgeShapeFunction {
		public Shape getShape(Edge edge) {
			Pair pair = edge.getEndpoints();
			Vertex from = (Vertex) pair.getFirst();
			Vertex to = (Vertex) pair.getSecond();
			if (to.findEdge(from) == null)
				return LINE.getShape(edge);
			else
				return CURVE.getShape(edge);
		}

		public void setControlOffsetIncrement(float inc) {
			LINE.setControlOffsetIncrement(inc);
			CURVE.setControlOffsetIncrement(inc);
		}

	}

	public void modelChanged(ModelChangeEvent event) {

		if (event.getType() == ModelChangeEvent.MOTHERSHIP_DISPLAY) 
		{
			this.displayMotherShip();
		}
	}

	public void keyTyped(KeyEvent e) 
	{
		if (e.getKeyCode() == KeyEvent.VK_A )
			System.out.println("a");
	}

	public void keyPressed(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}

	public void keyReleased(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}
	
}
