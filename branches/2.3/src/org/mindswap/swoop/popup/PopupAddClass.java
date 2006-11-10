package org.mindswap.swoop.popup;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.ListModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.mindswap.swoop.SwoopModel;
import org.mindswap.swoop.reasoner.SwoopReasoner;
import org.mindswap.swoop.renderer.CellRenderer;
import org.mindswap.swoop.renderer.ClassExprRenderer;
import org.mindswap.swoop.renderer.SwoopCellRenderer;
import org.mindswap.swoop.renderer.entity.ConciseFormat;
import org.mindswap.swoop.utils.DataValueChecker;
import org.mindswap.swoop.utils.change.BooleanElementChange;
import org.mindswap.swoop.utils.ui.AddCloseBar;
import org.mindswap.swoop.utils.ui.EntityComparator;
import org.mindswap.swoop.utils.ui.OntologyComparator;
import org.semanticweb.owl.io.vocabulary.RDFVocabularyAdapter;
import org.semanticweb.owl.model.OWLAnd;
import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLDataFactory;
import org.semanticweb.owl.model.OWLDataProperty;
import org.semanticweb.owl.model.OWLDataRange;
import org.semanticweb.owl.model.OWLDataType;
import org.semanticweb.owl.model.OWLDataValue;
import org.semanticweb.owl.model.OWLDescription;
import org.semanticweb.owl.model.OWLDisjointClassesAxiom;
import org.semanticweb.owl.model.OWLEntity;
import org.semanticweb.owl.model.OWLEnumeration;
import org.semanticweb.owl.model.OWLException;
import org.semanticweb.owl.model.OWLIndividual;
import org.semanticweb.owl.model.OWLNot;
import org.semanticweb.owl.model.OWLObjectProperty;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLOr;
import org.semanticweb.owl.model.OWLProperty;
import org.semanticweb.owl.model.OWLRestriction;
import org.semanticweb.owl.model.OWLSubClassAxiom;
import org.semanticweb.owl.model.change.AddClassAxiom;
import org.semanticweb.owl.model.change.AddDataPropertyRange;
import org.semanticweb.owl.model.change.AddDomain;
import org.semanticweb.owl.model.change.AddEquivalentClass;
import org.semanticweb.owl.model.change.AddForeignEntity;
import org.semanticweb.owl.model.change.AddIndividualClass;
import org.semanticweb.owl.model.change.AddObjectPropertyRange;
import org.semanticweb.owl.model.change.AddSuperClass;
import org.semanticweb.owl.model.change.OntologyChange;
import org.semanticweb.owl.model.change.RemoveEntity;
import org.xngr.browser.editor.XmlEditorPane;
/**
 * @author Aditya This class pops up whenever the user needs to specify an OWL
 *         Class/Restriction in an Ontology Change (For example: adding a
 *         superclass, intersection class, domain/range for a property, or type
 *         for an instance)
 *
 */
public class PopupAddClass extends JFrame
        implements
            ActionListener,
            ItemListener,
            ListSelectionListener,
            KeyListener {
    /*
     * Global UI Objects
     */
    SwoopReasoner reasoner;
    String type, fillType;
    int typeIntCode;
    Font tahoma = new Font("Tahoma", Font.PLAIN, 11);
    Font tahomaB = new Font("Tahoma", Font.BOLD, 11);
    Font tahomaI = new Font("Tahoma", Font.ITALIC, 11);
    //JButton applyClassBtn, applyResBtn, addClassBtn, addRestrictionBtn, applyCEBtn, addCEBtn, cancelBtn3,
    //        cancelBtn, cancelBtn2; // many cancel btns is bad
    JButton addCEBtn, cancelBtn3;
    AddCloseBar classBar, restrictionBar, ceBar;
    JComboBox simpleClassOntologyCombo, restrPropOntologyCombo,
            restrClassOntologyCombo, classBox, resClassBox, resDTypeBox,
            resPropBox, resNameCombo, propTypeBox;
    JList ontList, classList, dtypeList, propList, valList, ceList;
    JComboBox ontPropBox, ontValBox, valChooser;
    JTextField newClassFld, newPropFld, newValFld, dataFld;
    JButton clearSelBtn1, clearSelBtn2, clearSelBtn3;
    JTabbedPane mainTab;
    XmlEditorPane rdfArea;
    JTextField resDTypeFld;
    JPanel dTypePanel, resClassPanel;
    JLabel objResLbl;
    SwoopModel swoopModel;
    public List changes;
    JButton someBtn, allBtn, equBtn, maxBtn, minBtn, intBtn, uniBtn, negBtn, oneBtn;
    final int SOME = 0;
    final int ALL = 1;
    final int EQU = 2;
    final int MIN = 3;
    final int MAX = 4;
    final int INT = 5;
    final int UNI = 6;
    final int ONE = 7;
    final int NEG = 8;
    OWLDescription currClassExpr = null;
    List dataTypes = null, dataTypeURIs = null; // list of all XSD datatypes
    JLabel ceStatusLbl;
    OWLDescription lhsGCI = null; // LHS class expr. of GCI
    
    private static final String ADD_RESTRICTION = "ADDR";

    /*
     * overloading constructor to pass LHS of GCI
     */
    public PopupAddClass(SwoopReasoner reas, String type, SwoopModel swoopModel, OWLDescription lhsGCI) {
    	this.lhsGCI = lhsGCI;
    	this.reasoner = reas;
        this.type = getFullType(type);
        this.swoopModel = swoopModel;
        changes = new ArrayList();
        fillType = "";
        init();
        setupUI();
    }
    
    // we directly pass the changes so it can be examined here
    // for deleted stuff and add the new changes directly
    public PopupAddClass(SwoopReasoner reas, String type, SwoopModel swoopModel) {
        //		setModal(true);
        this.reasoner = reas;
        this.type = getFullType(type);
        this.swoopModel = swoopModel;
        changes = new ArrayList();
        fillType = "";
        init();
        setupUI();
    }
    
    private void init() {
    	
    	// create datatypes list
        String xmls = "http://www.w3.org/2001/XMLSchema#";
        String[] datatypes = {"anyURI", "base64Binary", "boolean", "byte", "date", "dateTime",
                "double", "decimal", "float", "gDay", "gMonth", "gMonthDay", "gYear",
                "gYearMonth", "hexBinary", "int", "integer", "language", "long", "Name",
                "normalizedString", "NCName", "NMTOKEN",
                "negativeInteger","nonNegativeInteger", "nonPositiveInteger", "positiveInteger",
                "short", "string", "time", "token",
                "unsignedLong", "unsignedInt", "unsignedShort", "unsignedByte"};
        dataTypes = new ArrayList();
        dataTypeURIs = new ArrayList();
        try {
		    for (int i = 0; i < datatypes.length; i++) {
		        String dataURI = xmls + datatypes[i];
		        OWLDataType dt = swoopModel.getSelectedOntology().getOWLDataFactory().getOWLConcreteDataType(new URI(dataURI));
				dataTypes.add(i, dt);
				dataTypeURIs.add(i, dataURI);
		    }
		    // finally add RDF XMLLiteral
		    dataTypes.add(datatypes.length, swoopModel.getSelectedOntology().getOWLDataFactory().getOWLConcreteDataType(new URI(RDFVocabularyAdapter.RDF
		            + "XMLLiteral")));
        }
        catch (Exception e) {
        	e.printStackTrace();
        }
    }
    
    /**
     * Accept a class-related-change type code and return the full type string
     *
     * @param type -
     *            abbreviated type
     * @return
     */
    private String getFullType(String type) {
    	
        // get full string type from type code
    	if (type.equals("GCI-LEFT")) {
    		typeIntCode = -1;
    		return "GENERAL CONCEPT INCLUSION AXIOM (LHS)";
    	}
    	else
		if (type.equals("GCI-RIGHT")) {
    		typeIntCode = -2;
    		return "GENERAL CONCEPT INCLUSION AXIOM (RHS)";
    	}
		else
        if (type.equals("EQU") || type.equals("EQUIVALENT CLASS")) {
            typeIntCode = 0;
            return "EQUIVALENT CLASS";
        }
        else
        if (type.equals("SUB") || type.equals("SUPER-CLASS")) {
            typeIntCode = 1;
            return "SUPER-CLASS";
        }
        else
        if (type.equals("SUP") || type.equals("SUP-CLASS")) {
            typeIntCode = 2;
            return "SUB-CLASS";
        }
        else
        if (type.equals("DIS") || type.equals("DISJOINT CLASS")) {
            typeIntCode = 3;
            return "DISJOINT CLASS";
        }
        else
        if (type.equals("INT") || type.equals("INTERSECTION ELEMENT")) {
            typeIntCode = 4;
            return "INTERSECTION ELEMENT";
        }
        else
        if (type.equals("UNI") || type.equals("UNION ELEMENT")) {
            typeIntCode = 5;
            return "UNION ELEMENT";
        }
        else
        if (type.equals("HASDOM") || type.equals("DOMAIN CLASS")) {
            typeIntCode = 6;
            return "DOMAIN CLASS";
        }
        else
        if (type.equals("HASRAN") || type.equals("RANGE CLASS")) {
            typeIntCode = 7;
            return "RANGE CLASS";
        }
        else
        if (type.equals("TYP") || type.equals("INSTANCE TYPE")) {
            typeIntCode = 8;
            return "INSTANCE TYPE";
        }
        else
        // added later
        if (type.equals("NOT") || type.equals("COMPLEMENT CLASS")) {
            typeIntCode = 9;
            return "COMPLEMENT CLASS";
        }
        return "";
    }
    
    /*
     * Setup UI for Popup - add Class, Restriction and CE tabs
     */
    private void setupUI() {
        
    	// create tabbedpane
        mainTab = new JTabbedPane();
        mainTab.setFont(tahoma);
        // tab1 - defined classes
        JPanel tab1 = new JPanel();
        tab1.setLayout(new BorderLayout());
        simpleClassOntologyCombo = new JComboBox();
        simpleClassOntologyCombo.addItemListener(this);
        simpleClassOntologyCombo.setFont(tahoma);
        ontList = new JList();
        ontList.setFont(tahoma);
        ontList.setCellRenderer(new CellRenderer());
        classBox = new JComboBox();
        classBox.setFont(tahoma);
        classBox.setEditable(true);
        classBox.setRenderer(new CellRenderer());
        classList = new JList();
        classList.setFont(tahoma);
        classList.addKeyListener(this);
        classList.setCellRenderer(new SwoopCellRenderer(swoopModel));
        //classList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        
        classBar = new AddCloseBar();
        classBar.addActionListener(this);
        tab1.setLayout(new BorderLayout());
        JLabel ontLbl = new JLabel("Select Ontology:");
        ontLbl.setFont(tahoma);
        JLabel classLbl = new JLabel("Select Class:");
        classLbl.setFont(tahoma);
        
        JPanel tab1W = new JPanel();
        tab1W.setLayout(new BorderLayout());
        tab1W.add(ontLbl, "North");
        tab1W.add(new JScrollPane(ontList), "Center");
        JPanel tab1E = new JPanel();
        tab1E.setLayout(new BorderLayout());
        tab1E.add(classLbl, "North");
        tab1E.add(new JScrollPane(classList), "Center");
        JSplitPane tab1Split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        tab1Split.setOneTouchExpandable(true);
        tab1Split.setLeftComponent(tab1W);
        tab1Split.setRightComponent(tab1E);
        newClassFld = new JTextField();
        newClassFld.setFont(tahoma);
        newClassFld.addKeyListener(this);
        JPanel newPanel = new JPanel();
        newPanel.setLayout(new BorderLayout());
        JLabel newLbl = new JLabel("OR Specify New Term/URI: ");
        newLbl.setFont(tahoma);
        newPanel.add(newLbl, "West");
        newPanel.add(newClassFld, "Center");
        JPanel tab1C = new JPanel();
        tab1C.setLayout(new BorderLayout());
        tab1C.add(tab1Split, "Center");
        tab1C.add(newPanel, "South");
        tab1.add(tab1C, "Center");
        tab1.add(classBar, "South");
        if (this.typeIntCode>=0) mainTab.addTab("Defined", tab1);
        // draw restriction panel
        JPanel restPanel = new JPanel();
        restPanel.setLayout(new BorderLayout());
        restrPropOntologyCombo = new JComboBox();
        restrPropOntologyCombo.addItemListener(this);
        restrPropOntologyCombo.setFont(tahoma);
        //Ontology source of the restriction
        JPanel ontPanel2 = createRowPanel("Select Ontology",
                restrPropOntologyCombo);
        resPropBox = new JComboBox();
        resPropBox.setFont(tahoma);
        resPropBox.setEditable(true);
        resPropBox.setRenderer(new SwoopCellRenderer(swoopModel));
        // create restriction property panel
        propTypeBox = new JComboBox();
        propTypeBox.setFont(new Font("Tahoma", Font.PLAIN, 12));
        propTypeBox.addItem("[D]");
        propTypeBox.addItem("[O]");
        propTypeBox.setVisible(false);
        propTypeBox.addItemListener(this);
        JPanel resPropLeft = createRowPanel("Select Property", propTypeBox);
        JPanel resPropPanel = new JPanel();
        resPropPanel.setLayout(new BorderLayout());
        resPropPanel.add(resPropLeft, "West");
        resPropPanel.add(resPropBox, "Center");
        // create restriction type panel
        resNameCombo = new JComboBox();
        resNameCombo.setFont(new Font("Verdana", Font.PLAIN, 10));
        resNameCombo.addItem("cardinality");
        resNameCombo.addItem("MIN cardinality");
        resNameCombo.addItem("MAX cardinality");
        resNameCombo.addItem("has-value");
        resNameCombo.addItem("some-values-from");
        resNameCombo.addItem("all-values-from");
        resNameCombo.addItemListener(this);
        JPanel resTypePanel = createRowPanel("Select Restriction Type",
                resNameCombo, "BOLD");
        restrClassOntologyCombo = new JComboBox();
        restrClassOntologyCombo.addItemListener(this);
        restrClassOntologyCombo.setFont(tahoma);
        JPanel ontPanel3 = createRowPanel("Select Ontology",
                restrClassOntologyCombo);
        resClassBox = new JComboBox();
        resClassBox.setFont(tahoma);
        resClassBox.setEditable(true);
        resClassBox.setRenderer(new SwoopCellRenderer(swoopModel));
        // add datatype value fields
        resDTypeBox = new JComboBox();
        resDTypeBox.setFont(tahoma);
        resDTypeBox.setEditable(true);
//        resDTypeBox.setRenderer(new SwoopCellRenderer(swoopModel));
        resDTypeFld = new JTextField();
        resDTypeFld.setFont(tahoma);
        dTypePanel = new JPanel();
        dTypePanel.setLayout(new BorderLayout());
        Box dTypeBox = Box.createHorizontalBox();
        dTypeBox.add(resDTypeBox);
        dTypeBox.add(resDTypeFld);
        JLabel dTypeLbl = new JLabel("Enter Datatype Value:");
        dTypeLbl.setFont(tahoma);
        dTypePanel.add(dTypeLbl, "West");
        dTypePanel.add(dTypeBox, "Center");
        
        for (int i = 0; i < dataTypeURIs.size(); i++) {
            String dt =  dataTypeURIs.get(i).toString();
            resDTypeBox.addItem(dt);
        }
        resDTypeBox.setRenderer(new CellRenderer());
        objResLbl = new JLabel("Select Class: ");
        objResLbl.setFont(tahoma);
        resClassPanel = new JPanel();
        resClassPanel.setLayout(new BorderLayout());
        resClassPanel.add(objResLbl, "West");
        resClassPanel.add(resClassBox, "Center");
        JPanel restPanelN = new JPanel();
        restPanelN.setLayout(new GridLayout(10, 1));
        JLabel propLbl = new JLabel("Subject of Restriction (Property)");
        propLbl.setFont(tahomaB);
        restPanelN.add(propLbl);
        restPanelN.add(ontPanel2);
        restPanelN.add(resPropPanel);
        restPanelN.add(resTypePanel);
        JLabel claLbl = new JLabel(
                "Object of Restriction (Integer/Class/Value)");
        claLbl.setFont(tahomaB);
        restPanelN.add(claLbl);
        restPanelN.add(ontPanel3);
        restPanelN.add(resClassPanel);

        restPanelN.add(dTypePanel);
        restrictionBar = new AddCloseBar();
        restrictionBar.addActionListener(this);
        restPanelN.add(restrictionBar);
        restPanel.add(restPanelN, "Center");
        simpleClassOntologyCombo.setRenderer(new CellRenderer());
        restrPropOntologyCombo.setRenderer(new CellRenderer());
        restrClassOntologyCombo.setRenderer(new CellRenderer());
        if (this.typeIntCode>=0) mainTab.addTab("Restriction", restPanel);            
        
        // create class expression panel
        JPanel cePanel = new JPanel();
        cePanel.setLayout(new BorderLayout());
        JPanel propBar = new JPanel();
        propBar.setLayout(new GridLayout(1,5));
        someBtn = new JButton("<html>"+ConciseFormat.EXISTS+"</html>");
        someBtn.setToolTipText("<html>Used to create: <br> - someValuesFrom(OBJECT_PROP, CLASS) <br> - hasValue(OBJECT_PROP, INDIVIDUAL) <br> - someValuesFrom(DATA_PROP, DATATYPE) <br> - hasValue(DATA_PROP, DATA_VALUE)</html>");
        allBtn = new JButton("<html>"+ConciseFormat.FORALL+"</html>");
        allBtn.setToolTipText("<html>Used to create: <br> - allValuesFrom(OBJECT_PROP, CLASS) <br> - allValuesFrom(DATA_PROP, DATATYPE)</html>");
        equBtn = new JButton("<html>"+ConciseFormat.EQU+"</html>");
        equBtn.setToolTipText("<html>Used to create: <br> - cardinality(OBJECT_PROP, INTEGER) <br> - cardinality(DATA_PROP, INTEGER)</html>");
        maxBtn = new JButton("<html>"+ConciseFormat.LESSEQU+"</html>");
        maxBtn.setToolTipText("<html>Used to create: <br> - maxCardinality(OBJECT_PROP, INTEGER) <br> - maxCardinality(DATA_PROP, INTEGER)</html>");
        minBtn = new JButton("<html>"+ConciseFormat.GREATEQU+"</html>");
        minBtn.setToolTipText("<html>Used to create: <br> - minCardinality(OBJECT_PROP, INTEGER) <br> - minCardinality(DATA_PROP, INTEGER)</html>");
        oneBtn = new JButton("{}");
        oneBtn.setToolTipText("<html>Used to create: <br> - oneOf(IND_1, IND_2...,IND_n)</html>");
        intBtn = new JButton("<html>"+ConciseFormat.INTERSECTION+"</html>");
        intBtn.setToolTipText("<html>Used to create: <br> - intersection(CLASS_1, CLASS_2...,CLASS_n)</html>");
        uniBtn = new JButton("<html>"+ConciseFormat.UNION+"</html>");
        uniBtn.setToolTipText("<html>Used to create: <br> - union(CLASS_1, CLASS_2...,CLASS_n)</html>");
        negBtn = new JButton("<html>"+ConciseFormat.COMPLEMENT+"</html>");
        negBtn.setToolTipText("<html>Used to create: <br> - complement(CLASS)</html>");
        ontPropBox = new JComboBox();
        ontPropBox.setRenderer(new CellRenderer());
        ontValBox = new JComboBox();
        ontValBox.setRenderer(new CellRenderer());
        propBar.add(someBtn);
        propBar.add(allBtn);
        propBar.add(equBtn);
        propBar.add(maxBtn);
        propBar.add(minBtn);
        JPanel valBar = new JPanel();
        valBar.setLayout(new GridLayout(1,4));
        valBar.add(intBtn);
        valBar.add(uniBtn);
        valBar.add(oneBtn);
        valBar.add(negBtn);
        propList = new JList();
        propList.setFont(tahoma);
        propList.addKeyListener(this);
        propList.setCellRenderer(new SwoopCellRenderer(swoopModel));
        valList = new JList();
        valList.setFont(tahoma);
        valList.setCellRenderer(new SwoopCellRenderer(swoopModel));
        ceList = new JList();
        ceList.setCellRenderer(new ClassExprRenderer(swoopModel));
        ceList.addKeyListener(this);
        valChooser = new JComboBox();
        valChooser.setFont(tahoma);
        valChooser.addItem("Show Classes");
        valChooser.addItem("Show Individuals");
        valChooser.addItem("Show Datatypes");
        valChooser.addItemListener(this);
        JSplitPane ceSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BorderLayout());
        JPanel propNorth = new JPanel();
        propNorth.setLayout(new BorderLayout());
        propNorth.add(propBar, "North");
        propNorth.add(ontPropBox, "Center");
        leftPanel.add(propNorth, "North");
        JPanel propListPanel = new JPanel();
        propListPanel.setLayout(new BorderLayout());
        JPanel propListNorth = new JPanel();
        propListNorth.setLayout(new BorderLayout());
        clearSelBtn1 = new JButton("Clear Sel");
        clearSelBtn1.setFont(new Font("Tahoma", Font.PLAIN, 9));
        propListNorth.add(clearSelBtn1, "East");
        propListPanel.add(propListNorth, "North");
        propListPanel.add(new JScrollPane(propList), "Center");
        dataFld = new JTextField();
        propListPanel.add(this.createRowPanel("Data", dataFld), "South");
        leftPanel.add(propListPanel, "Center");
        ceSplit.add(leftPanel, JSplitPane.LEFT);
        JSplitPane valSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        JPanel valPanel = new JPanel();
        valPanel.setLayout(new BorderLayout());
        valPanel.add(valChooser, BorderLayout.NORTH);
        valPanel.add(new JScrollPane(valList), BorderLayout.CENTER);
        valSplit.add(valPanel, JSplitPane.TOP);
        JPanel ceListPanel = new JPanel();
        ceListPanel.setLayout(new BorderLayout());
        JPanel ceListNorth = new JPanel();
        ceListNorth.setLayout(new BorderLayout());
        clearSelBtn3 = new JButton("Clear Sel");
        clearSelBtn3.setFont(new Font("Tahoma", Font.PLAIN, 9));
        ceListNorth.add(clearSelBtn3, "East");
        ceListPanel.add(ceListNorth, "North");
        ceListPanel.add(new JScrollPane(ceList), "Center");
        valSplit.add(ceListPanel, JSplitPane.BOTTOM);
        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BorderLayout());
        JPanel valNorth = new JPanel();
        valNorth.setLayout(new BorderLayout());
        valNorth.add(valBar, "North");
        valNorth.add(ontValBox, "Center");
        JPanel valSelPanel = new JPanel();
        valSelPanel.setLayout(new BorderLayout());
        clearSelBtn2 = new JButton("Clear Sel");
        clearSelBtn2.setFont(new Font("Tahoma", Font.PLAIN, 9));
        valSelPanel.add(clearSelBtn2, "East");
        valNorth.add(valSelPanel, "South");
        rightPanel.add(valNorth, "North");
        rightPanel.add(valSplit, "Center");
        ceSplit.add(rightPanel, JSplitPane.RIGHT);
        cePanel.add(ceSplit, "Center");
        
        JPanel southPanel = new JPanel();
        southPanel.setLayout(new GridLayout(2,1));
        ceStatusLbl = new JLabel();
        southPanel.add(this.createRowPanel("Status", ceStatusLbl, "BOLD"));
        
        if (this.typeIntCode >= 0) {
        	ceBar = new AddCloseBar();
        	ceBar.addActionListener(this);
        	southPanel.add(ceBar);
        } else {
			addCEBtn = new JButton("Add");
			if (this.typeIntCode == -1)
				addCEBtn.setText("Specify LHS of GCI and Proceed..");
			else if (this.typeIntCode == -2)
				addCEBtn.setText("Specify RHS of GCI & Add GCI");
			addCEBtn.setFont(tahoma);
			cancelBtn3 = new JButton("Cancel");
			cancelBtn3.setFont(tahoma);
			JPanel ceBtnPanel = new JPanel();
			ceBtnPanel.setLayout(new GridLayout(1, 3));
			ceBtnPanel.add(addCEBtn);
			ceBtnPanel.add(cancelBtn3);
			southPanel.add(ceBtnPanel);
			
			addCEBtn.addActionListener(this);
	        cancelBtn3.addActionListener(this);
		}
        
        
        
        cePanel.add(southPanel, "South");
        mainTab.addTab("Class Expression", cePanel);
        
        // setup action listeners for CE panel
        someBtn.addActionListener(this);
		allBtn.addActionListener(this);
		equBtn.addActionListener(this);
		maxBtn.addActionListener(this);
		minBtn.addActionListener(this);
		oneBtn.addActionListener(this);
        intBtn.addActionListener(this);
		uniBtn.addActionListener(this);
		negBtn.addActionListener(this);
        clearSelBtn1.addActionListener(this);
        clearSelBtn2.addActionListener(this);
        clearSelBtn3.addActionListener(this);
        
        fillValues();
        // fill values for UI in cePanel
        this.fillPropList();
        this.fillValList();
        resNameCombo.setSelectedIndex(5);
        
        // add tabbed pane to frame container
        Container content = getContentPane();
        content.setLayout(new BorderLayout());
        JLabel typeLbl = new JLabel("[ ADDING " + type + ".. ]");
        typeLbl.setFont(tahoma);
        content.add(typeLbl, "North");
        if (swoopModel.getSelectedEntity() instanceof OWLDataProperty
                && type.equals("RANGE CLASS")) {
            // special case: adding range for datatype property
            JPanel dtypePanel = new JPanel();
            dtypePanel.setLayout(new BorderLayout());
            // add list of datatypes
            dtypeList = new JList();
            dtypeList.setCellRenderer(new SwoopCellRenderer(swoopModel));
            dtypeList.setListData(dataTypes.toArray());
            dtypeList.addListSelectionListener(new ListSelectionListener() {
                public void valueChanged(ListSelectionEvent arg0) {
                    try {
						newClassFld.setText(((OWLDataType) dtypeList.getSelectedValue()).getURI().toString());
					} catch (OWLException e) {
						e.printStackTrace();
					}
                }
            });
            dtypePanel.add(new JScrollPane(dtypeList), "Center");
            dtypePanel.add(classBar, "South");
            content.add(dtypePanel);
        } 
        else {
            content.add(mainTab, "Center");
        }
        pack();
        setTitle("Specify Class");
        setSize(450, 410);
        setResizable(true);
        tab1Split.setDividerLocation(140);
        valSplit.setDividerLocation(140);
    }
    
    private JPanel createRowPanel(String lblStr, Component comp) {
        return createRowPanel(lblStr, comp, "PLAIN");
    }
    
    private JPanel createRowPanel(String lblStr, Component comp, String font) {
        JLabel lbl = new JLabel(lblStr + ": ");
        if (font.equals("PLAIN"))
            lbl.setFont(tahoma);
        else if (font.equals("BOLD"))
            lbl.setFont(tahomaB);
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.add(lbl, "West");
        panel.add(comp, "Center");
        return panel;
    }
    
    /*
     * Fill values in Ontology Combo Boxes (i.e populate it with ontologies
     * from SwoopModel)
     */
    public void fillValues() {
        // get swoopModel ontology list
        // fill ontCombos with ontology list
        // turn off listeners
        simpleClassOntologyCombo.removeItemListener(this);
        restrPropOntologyCombo.removeItemListener(this);
        restrClassOntologyCombo.removeItemListener(this);
        ontList.removeListSelectionListener(this);
        ontPropBox.removeItemListener(this);
        ontValBox.removeItemListener(this);
        
        // fill starting entries in ontCombo boxes
        simpleClassOntologyCombo.addItem("/New term or URI");
        restrPropOntologyCombo.addItem("/New term or URI");
        restrClassOntologyCombo.addItem("/New term or URI");
        Set sortedOntSet = new TreeSet(OntologyComparator.INSTANCE);
        sortedOntSet.addAll(swoopModel.getOntologyURIs());
        ontList.setListData(sortedOntSet.toArray());
        Iterator iter = sortedOntSet.iterator();
        while (iter.hasNext()) {
            String uri = iter.next().toString();
            simpleClassOntologyCombo.addItem(uri);
            restrClassOntologyCombo.addItem(uri);
            restrPropOntologyCombo.addItem(uri);
            ontPropBox.addItem(uri);
        	ontValBox.addItem(uri);
        }
        
        // turn on listeners
        simpleClassOntologyCombo.addItemListener(this);
        restrPropOntologyCombo.addItemListener(this);
        restrClassOntologyCombo.addItemListener(this);
        ontList.addListSelectionListener(this);
        ontPropBox.addItemListener(this);
        ontValBox.addItemListener(this);
        
        // select current displayed ontology
        try {
            String currOntURI = swoopModel.getSelectedOntology().getURI()
                    .toString();
            simpleClassOntologyCombo.setSelectedItem(currOntURI);
            restrPropOntologyCombo.setSelectedItem(currOntURI);
            restrClassOntologyCombo.setSelectedItem(currOntURI);
            ontList.setSelectedValue(swoopModel.getSelectedOntology().getURI(),
                    true);
            ontPropBox.setSelectedItem(currOntURI);
            ontValBox.setSelectedItem(currOntURI);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    /**
     * Create the appropriate Ontology Change (class-level) depending on the
     * type-code Default parameters of the change are current selected ontology
     * and class
     *
     * @param newDesc -
     *            new OWL description component of change
     * @throws OWLException
     */
    private void makeChange(OWLDescription newDesc) throws OWLException {
        // create the ontology change object
        OWLOntology dispOnt = swoopModel.getSelectedOntology();
        OWLEntity dispEntity = swoopModel.getSelectedEntity();
        OntologyChange change = null;
        switch (typeIntCode) {
        	case 0 :
                // add equivalent-class
                change = new AddEquivalentClass(dispOnt, (OWLClass) dispEntity,
                        newDesc, null);
                changes.add(change);
                break;
            case 9 :
                // add complement class
                addBooleanSet(dispOnt, (OWLClass) dispEntity, newDesc, "COM");
                break;
            case 1 :
                // add super-class
                change = new AddSuperClass(dispOnt, (OWLClass) dispEntity, newDesc, null);
                changes.add(change);
                break;
            case 2 :
                // add sub-class
                OWLSubClassAxiom axiom = dispOnt.getOWLDataFactory().getOWLSubClassAxiom(newDesc, (OWLClass) dispEntity);
                change = new AddClassAxiom(dispOnt, axiom, null);
                changes.add(change);
                break;
            case 3 :
                // add disjoint-class
                OWLDataFactory df = dispOnt.getOWLDataFactory();
                Set disSet = new HashSet();
                disSet.add(newDesc);
                disSet.add(dispEntity);
                OWLDisjointClassesAxiom disAxiom = df
                        .getOWLDisjointClassesAxiom(disSet);
                change = new AddClassAxiom(dispOnt, disAxiom, null);
                changes.add(change);
                break;
            case 4 :
                // add element to intersection-set
                addBooleanSet(dispOnt, (OWLClass) dispEntity, newDesc, "INT");
                break;
            case 5 :
                // add element to union-set
                addBooleanSet(dispOnt, (OWLClass) dispEntity, newDesc, "UNI");
                break;
            case 6 :
                // add domain class
                change = new AddDomain(dispOnt, (OWLProperty) dispEntity,
                        newDesc, null);
                changes.add(change);
                break;
            case 7 :
                // add range class
                OWLProperty prop = (OWLProperty) dispEntity;
                if (prop instanceof OWLDataProperty) {
                    String dTypeURIStr = newClassFld.getText();
                    if (isURI(dTypeURIStr)) {
                        URI uri = null;
                        try {
                            uri = new URI(dTypeURIStr);
                        } catch (URISyntaxException e) {
                            e.printStackTrace();
                        }
                        OWLDataType dType = swoopModel.getSelectedOntology()
                                .getOWLDataFactory()
                                .getOWLConcreteDataType(uri);
                        change = new AddDataPropertyRange(dispOnt,
                                (OWLDataProperty) prop, dType, null);
                        changes.add(change);
                    }
                } else {
                    //					************************************
                    //added for Econnections
                    //*****************************************
                    if (!((OWLObjectProperty) prop).isLink()) {
                        change = new AddObjectPropertyRange(dispOnt,
                                (OWLObjectProperty) prop, newDesc, null);
                        changes.add(change);
                    } else {
                        if (newDesc instanceof OWLClass) {
                            change = new AddObjectPropertyRange(dispOnt,
                                    (OWLObjectProperty) prop, newDesc, null);
                            changes.add(change);
                            OntologyChange change2 = new AddForeignEntity(
                                    dispOnt, (OWLClass) newDesc,
                                    ((OWLObjectProperty) prop).getLinkTarget(),
                                    null);
                            changes.add(change2);
                            OntologyChange change3 = new RemoveEntity(dispOnt,
                                    (OWLClass) newDesc, null);
                            changes.add(change3);
                        }
                    }
                }
                break;
            case 8 :
                // add instance type
                OWLIndividual ind = (OWLIndividual) dispEntity;
                change = new AddIndividualClass(dispOnt, ind, newDesc, null);
                changes.add(change);
                break;
        }
    }
    /**
     * Create and return an ADD boolean element change i.e. ADD intersection or
     * union element on a particular class
     *
     * @param dispOnt -
     *            displayed ontology
     * @param dispClass -
     *            displayed class
     * @param newDesc -
     *            OWL description of boolean element
     * @param type -
     *            INT or UNI or COM
     * @throws OWLException
     */
    private void addBooleanSet(OWLOntology dispOnt, OWLClass dispClass,
            OWLDescription newDesc, String type) throws OWLException {
        /**
         * need to change when more than one boolean set present currently adds
         * 'newDesc' to first boolean set
         */
        Class typeC = null;
        if (type.equals("INT"))
            typeC = OWLAnd.class;
        else if (type.equals("UNI"))
            typeC = OWLOr.class;
        else
            typeC = OWLNot.class;
        BooleanElementChange change = new BooleanElementChange(typeC, "Add",
                dispOnt, dispClass, newDesc, null);
        changes.add(change);
    }
    
    /**
     * Method called when the 'Add' class button is pressed in the Class Expression Pane.
     * It gets the OWL CE created by the user and passes it to makeChange(..) to create the
     * appropriate class change (e.g. AddSuperClass etc)
     *
     */
    private void addCEChange() {
    	currClassExpr = (OWLDescription) ceList.getSelectedValue();
    	if (currClassExpr!=null) {
	    	try {
				this.makeChange(currClassExpr);
			} catch (OWLException e) {
				e.printStackTrace();
			}
	    	swoopModel.addUncommittedChanges(changes);
	        changes = new ArrayList(); // reset it after changes have been added
    	}
    	else ceStatusLbl.setText("No Class Expression selected!");
    }
    
    /**
     * Method called when the 'Add' class button is pressed It gets the OWL
     * class selected by the user and passes it to makeChange(..) to create the
     * appropriate class change (e.g. AddSuperClass etc)
     *
     */
    private void addClassChange() {
        try {
            // get selected OWLClass element
            OWLClass selCla = null;
            if ((newClassFld.getText() != null)
                    && (!newClassFld.getText().trim().equals(""))) {
                // user-specified class name/uri
                String claStr = newClassFld.getText();
                claStr = claStr.replaceAll(" ","_");
                if (!isURI(claStr))
                    claStr = swoopModel.getSelectedOntology().getLogicalURI()
                            + "#" + claStr;
                URI claURI = new URI(claStr);
                selCla = swoopModel.getSelectedOntology().getOWLDataFactory()
                        .getOWLClass(claURI);
                makeChange(selCla);
            } else {
                // existing class in swoopModel
                // enable multiple selection in class list
                Object[] selClas = (Object[]) classList.getSelectedValues();
                for (int i = 0; i < selClas.length; i++) {
                    if (selClas[i] instanceof OWLClass)
                        makeChange((OWLClass) selClas[i]);
                }
            }
            swoopModel.addUncommittedChanges(changes);
            changes = new ArrayList(); // reset it after changes have been
                                       // added
            // dispose();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    private boolean isURI(String str) {
        try {
            new URI(str);
            return true;
        } catch (Exception ex) {
        }
        return false;
    }
    /**
     * Method called when the user clicks on the Add Restriction button Creates
     * the OWL Restriction based on user selections and values and passes it to
     * the makeChange(..) method to create the corresponding class change
     */
    private void addRestrictionChange() {
        try {
            // get appropriate OWL restriction
            // get property
            OWLProperty prop = null;
            URI userPropURI = null;
            //**********************************************
            //Added for Econnections
            //***********************************************
            Set foreignEnt = new HashSet();
            //***********************************************
            String propType = "";
            if (restrPropOntologyCombo.getSelectedIndex() != 0) {
                prop = (OWLProperty) resPropBox.getSelectedItem();
                if (prop instanceof OWLDataProperty)
                    propType = "Data";
                else {
                    //********************************************
                    //Changed for Econnections
                    //*******************************************
                    if (!(((OWLObjectProperty) prop).isLink()))
                        propType = "Object";
                    else
                        propType = "Link";
                }
            } else {
                // user-specified property name/uri
                String propStr = resPropBox.getSelectedItem().toString();
                propStr = propStr.replaceAll(" ","_");
                if (!isURI(propStr))
                    propStr = swoopModel.getSelectedOntology().getLogicalURI()
                            + "#" + propStr;
                userPropURI = new URI(propStr);
            }
            
            //---------------------------------------------------
            // get restriction type / name and object accordingly
            OWLClass cla = null; // some/all values object rest
            OWLDataRange dRan = null; // some/all values data rest
            OWLIndividual ind = null; // has value object rest
            OWLDataValue dVal = null; // has value data rest
            int cardinality = -1;
            OWLDataFactory ontDF = swoopModel.getSelectedOntology().getOWLDataFactory();
            
            if (resNameCombo.getSelectedIndex() >= 4) {
                // get class
                if (propType.equals("Data")) {
                    // get data range
                    // some/all values for datatype property
                    String dRanStr = resDTypeBox.getSelectedItem().toString();
                    if ((!isURI(dRanStr))
                            || (dRanStr.indexOf("XMLSchema#") == -1)) {
                        popupError("Invalid DataType Range - need XML Schema URI");
                        restrClassOntologyCombo.setSelectedIndex(0);
                        resClassBox.addItem("http://www.w3.org/2001/XMLSchema#");
                        resClassBox.setSelectedItem("http://www.w3.org/2001/XMLSchema#");
                        return;
                    }
                    URI dataURI = new URI(dRanStr);
                    dRan = ontDF.getOWLConcreteDataType(dataURI);
                } else {
                    // get class
                    if (restrClassOntologyCombo.getSelectedIndex() != 0) {
                        cla = (OWLClass) resClassBox.getSelectedItem();
                        //********************************************
                        //Added for Econn
                        //********************************************
                        if (propType.equals("Link"))
                            foreignEnt.add(cla);
                        //********************************************
                    } else {
                        // user-specified class name/uri
                        String claStr = resClassBox.getSelectedItem()
                                .toString();
                        claStr = claStr.replaceAll(" ","_");
                        if (!isURI(claStr))
                            claStr = swoopModel.getSelectedOntology()
                                    .getLogicalURI()
                                    + "#" + claStr;
                        URI claURI = new URI(claStr);
                        cla = swoopModel.getSelectedOntology()
                                .getOWLDataFactory().getOWLClass(claURI);
                        //********************************************
                        //Added for Econn
                        //********************************************
                        if (propType.equals("Link"))
                            foreignEnt.add(cla);
                        //********************************************
                    }
                }
            } else if (resNameCombo.getSelectedIndex() == 3) {
                if (propType.equals("Data")) {
                    // get data value
                    // has value for datatype property
                    String dtURIStr = resDTypeBox.getSelectedItem().toString();
                    URI dtURI = new URI(dtURIStr);
                    String dValStr = resDTypeFld.getText();
                    dVal = ontDF.getOWLConcreteData(dtURI, "EN", dValStr);
                } else {
                    // get individual
                    if (restrClassOntologyCombo.getSelectedIndex() != 0) {
                        ind = (OWLIndividual) resClassBox.getSelectedItem();
                    } else {
                        String indStr = resClassBox.getSelectedItem()
                                .toString();
                        if (!isURI(indStr))
                            indStr = swoopModel.getSelectedOntology()
                                    .getLogicalURI()
                                    + "#" + indStr;
                        URI indURI = new URI(indStr);
                        ind = swoopModel.getSelectedOntology()
                                .getOWLDataFactory().getOWLIndividual(indURI);
                    }
                    //********************************************
                    //Added for Econn
                    //********************************************
                    if (propType.equals("Link"))
                        foreignEnt.add(ind);
                    //********************************************
                }
            } else {
                // get cardinality
                try {
                    cardinality = Integer.parseInt(resClassBox
                            .getSelectedItem().toString());
                    if (cardinality < 0) {
                        popupError("Invalid Integer Argument");
                        return;
                    }
                } catch (Exception ex) {
                    popupError("Invalid Integer Argument");
                    return;
                }
            }
            //--------------------------------------------
            // create restriction based on user selections
            OWLRestriction res = null;
            switch (resNameCombo.getSelectedIndex()) {
                case 0 :
                    // cardinality restriction
                    if (userPropURI != null) {
                        // create new property as Object Property - default
                        prop = ontDF.getOWLObjectProperty(userPropURI);
                        propType = "Object";
                    }
                    if (propType.equals("Data"))
                        res = ontDF.getOWLDataCardinalityRestriction(
                                (OWLDataProperty) prop, cardinality,
                                cardinality);
                    else
                        res = ontDF.getOWLObjectCardinalityRestriction(
                                (OWLObjectProperty) prop, cardinality,
                                cardinality);
                    break;
                case 1 :
                    // minCardinality restriction
                    if (userPropURI != null) {
                        // create new property as Object Property - default
                        prop = ontDF.getOWLObjectProperty(userPropURI);
                        propType = "Object";
                    }
                    if (propType.equals("Data"))
                        res = ontDF.getOWLDataCardinalityAtLeastRestriction(
                                (OWLDataProperty) prop, cardinality);
                    else
                        res = ontDF.getOWLObjectCardinalityAtLeastRestriction(
                                (OWLObjectProperty) prop, cardinality);
                    break;
                case 2 :
                    // maxCardinality restriction
                    if (userPropURI != null) {
                        // create new property as Object Property - default
                        prop = ontDF.getOWLObjectProperty(userPropURI);
                        propType = "Object";
                    }
                    if (propType.equals("Data"))
                        res = ontDF.getOWLDataCardinalityAtMostRestriction(
                                (OWLDataProperty) prop, cardinality);
                    else
                        res = ontDF.getOWLObjectCardinalityAtMostRestriction(
                                (OWLObjectProperty) prop, cardinality);
                    break;
                case 3 :
                    // hasValue restriction
                    if (propType.equals("Data")) {
                        URI uri = new URI(resDTypeBox.getSelectedItem().toString());
                        if (dVal == null)
                        {
                            popupError("'hasValue' restriction on Datatype property must have range of Data-Value");
                            return;
                        }

                        boolean flag = DataValueChecker.isValidValue(this, uri, resDTypeFld.getText());
                        if (!flag)
                            return;

                        res = ontDF.getOWLDataValueRestriction(
                                (OWLDataProperty) prop, dVal);
                    } else {
                        if (ind == null) {
                            popupError("'hasValue' restriction on Object property must have range of Instance");
                            return;
                        }
                        res = ontDF.getOWLObjectValueRestriction(
                                (OWLObjectProperty) prop, ind);
                    }
                    break;
                case 4 :
                    // someValuesFrom restriction
                    if (propType.equals("Data")) {
                        if (userPropURI != null)
                            prop = ontDF.getOWLDataProperty(userPropURI);
                        if (dRan == null) {
                            popupError("'someValuesFrom' restriction on Datatype property must have range of Datatype");
                            return;
                        }
                        res = ontDF.getOWLDataSomeRestriction(
                                (OWLDataProperty) prop, dRan);
                    } else {
                        if (userPropURI != null)
                            prop = ontDF.getOWLObjectProperty(userPropURI);
                        if (cla == null) {
                            popupError("'someValuesFrom' restriction on Object property must have range of Class");
                            return;
                        }
                        res = ontDF.getOWLObjectSomeRestriction((OWLObjectProperty) prop, cla);
                    }
                    break;
                case 5 :
                    // allValuesFrom restriction
                    if (propType.equals("Data")) {
                        if (userPropURI != null)
                            prop = ontDF.getOWLDataProperty(userPropURI);
                        if (dRan == null) {
                            popupError("'allValuesFrom' restriction on Datatype property must have range of Datatype");
                            return;
                        }
                        res = ontDF.getOWLDataAllRestriction(
                                (OWLDataProperty) prop, dRan);
                    } else {
                        if (userPropURI != null)
                            prop = ontDF.getOWLObjectProperty(userPropURI);
                        if (cla == null) {
                            popupError("'allValuesFrom' restriction on Object property must have range of Class");
                            return;
                        }
                        res = ontDF.getOWLObjectAllRestriction((OWLObjectProperty) prop, cla);
                    }
                    break;
            }
            //************************************************
            //Added for Econn
            //************************************************
            Iterator iter = foreignEnt.iterator();
            while (iter.hasNext()) {
                OWLEntity entit = (OWLEntity) iter.next();
                OntologyChange oc = null;
                if (!(swoopModel.getSelectedOntology().getForeignEntities()
                        .containsKey(entit)))
                    oc = new AddForeignEntity(swoopModel.getSelectedOntology(),
                            entit, ((OWLObjectProperty) prop).getLinkTarget(),
                            null);
                changes.add(oc);
            }
            //******************************************

           makeChange(res);

            Iterator j = foreignEnt.iterator();
            while (j.hasNext()) {
                OWLEntity entit = (OWLEntity) j.next();
                OntologyChange oc = null;
                oc = new RemoveEntity(swoopModel.getSelectedOntology(), entit,
                        null);
                swoopModel.getSelectedOntology().getForeignEntities().put(
                        entit, ((OWLObjectProperty) prop).getLinkTarget());
                changes.add(oc);
            }

            swoopModel.addUncommittedChanges(changes);
            changes = new ArrayList(); // reset it after changes have been added
            
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    private void popupError(String msg) {
        JOptionPane.showMessageDialog(this, msg,
                "Error creating OWL Description", JOptionPane.ERROR_MESSAGE);
    }
    
    public void actionPerformed(ActionEvent e) {
    	if (e.getSource() == classBar) {
    		if (e.getActionCommand().equals(AddCloseBar.ADD)) {
    			addClassChange();
    		}
    		if (e.getActionCommand().equals(AddCloseBar.ADDCLOSE)) {
    			addClassChange();
    			dispose();
    		}
    		if (e.getActionCommand().equals(AddCloseBar.CLOSE)) {
    			dispose();
    		}
    	}
    	else if (e.getSource() == restrictionBar) {
    		if (e.getActionCommand().equals(AddCloseBar.ADD)) {
    			addRestrictionChange();
    		}
    		if (e.getActionCommand().equals(AddCloseBar.ADDCLOSE)) {
    			addRestrictionChange();
    			dispose();
    		}
    		if (e.getActionCommand().equals(AddCloseBar.CLOSE)) {
    			dispose();
    		}
    	}
    	else if (e.getSource() == ceBar) {
    		if (e.getActionCommand().equals(AddCloseBar.ADD)) {
    			addCEChange();
    		}
    		if (e.getActionCommand().equals(AddCloseBar.ADDCLOSE)) {
    			addCEChange();
        		if (currClassExpr!=null) dispose();
    		}
    		if (e.getActionCommand().equals(AddCloseBar.CLOSE)) {
    			dispose();
    		}
    	}
    	else if (e.getSource() == cancelBtn3) {
    		dispose();
    	}
    	else if (e.getSource() == addCEBtn) {
            if (this.typeIntCode == -1) {
            	// just added LHS of GCI
            	this.addLHSGCI();            	
            }
            else if (this.typeIntCode == -2) {
            	// just added RHS of GCI
            	this.addRHSGCI();
            }
        	else {
        		// adding class expression normally
        		addCEChange();
        		if (currClassExpr!=null) dispose();
        	}            
        }
    	else if ((e.getSource() instanceof AddCloseBar) 
    			&& e.getActionCommand().equals(AddCloseBar.CLOSE)) {
    		dispose();
    	}
        else if (e.getSource() == someBtn) {
        	this.createCE(SOME);
        }
        else if (e.getSource() == allBtn) {
        	this.createCE(ALL);
        }
        else if (e.getSource() == equBtn) {
        	this.createCE(EQU);			
        }
        else if (e.getSource() == maxBtn) {
        	this.createCE(MAX);
        }
        else if (e.getSource() == minBtn) {
        	this.createCE(MIN);
        }
        else if (e.getSource() == intBtn) {
        	this.createCE(INT);
        }
        else if (e.getSource() == uniBtn) {
        	this.createCE(UNI);			
        }
        else if (e.getSource() == oneBtn) {
        	this.createCE(ONE);			
        }
        else if (e.getSource() == negBtn) {
        	this.createCE(NEG);
        }
        else if (e.getSource() == clearSelBtn1) {
        	propList.clearSelection();
        }
        else if (e.getSource() == clearSelBtn2) {
        	valList.clearSelection();
        }
        else if (e.getSource() == clearSelBtn3) {
        	ceList.clearSelection();
        }        
    }
    
    /*
     * Create a CE based on user selection and button pressed
     * and add it to ceList
     */
    private void createCE(int type) {
    	
    	OWLDescription ce = null;
    	OWLOntology ont = swoopModel.getSelectedOntology();
		OWLDataFactory ontDF = null;
		try {
			ontDF = ont.getOWLDataFactory();
		} catch (OWLException e2) {
			e2.printStackTrace();
		}
		String ERROR = "";
		
		switch (type) {    	
    	
    		case INT:
    			Set descs = new HashSet();
    			if (valList.getSelectedValue() instanceof OWLDescription) {
    				for (int i = 0; i<valList.getSelectedValues().length; i++) {
    					descs.add(valList.getSelectedValues()[i]);
    				}
    			}
    			if (ceList.getSelectedIndex()!=-1) {
    				for (int i = 0; i<ceList.getSelectedValues().length; i++) {
    					descs.add(ceList.getSelectedValues()[i]);
    				}
    			}
    			try {
					if (descs.size()>0) ce = ontDF.getOWLAnd(descs);
					else ERROR = "No classes selected for intersection";
				} 
    			catch (OWLException e1) {
					e1.printStackTrace();
				}
    			break;
    		
    		case UNI:
    			descs = new HashSet();
    			if (valList.getSelectedValue() instanceof OWLDescription) {
    				for (int i = 0; i<valList.getSelectedValues().length; i++) {
    					descs.add(valList.getSelectedValues()[i]);
    				}
    			}
    			if (ceList.getSelectedIndex()!=-1) {
    				for (int i = 0; i<ceList.getSelectedValues().length; i++) {
    					descs.add(ceList.getSelectedValues()[i]);
    				}
    			}
    			try {
    				if (descs.size()>0) ce = ontDF.getOWLOr(descs);
    				else ERROR = "No classes selected for union";
    			} 
    			catch (OWLException e1) {
					e1.printStackTrace();
				}
    			break;	
    		
    		case ONE:
    			Set inds = new HashSet();
    			if (valList.getSelectedValue() instanceof OWLIndividual) {
    				for (int i = 0; i<valList.getSelectedValues().length; i++) {
    					inds.add(valList.getSelectedValues()[i]);
    				}
    			}
    			try {
    				if (inds.size()>0) ce = ontDF.getOWLEnumeration(inds);
    				else ERROR = "No individuals selected for enumeration";
				} 
    			catch (OWLException e1) {
					e1.printStackTrace();
				}
    			break;		
    			
    		case NEG:
    			OWLDescription desc = null;
    			if ((valList.getSelectedIndex()!=-1 && valList.getSelectedValue() instanceof OWLDescription)) 
    				desc = (OWLDescription) valList.getSelectedValue();
    			else if (ceList.getSelectedIndex()!=-1)
    				desc = (OWLDescription) ceList.getSelectedValue();
    				
    			if (desc!=null) {	    				
            		try {
						ce = ontDF.getOWLNot(desc);
					} catch (OWLException e) {
						e.printStackTrace();
					}
            	}
    			else ERROR = "No class selected for negation";
    			break;
    		
    		case SOME:
    			OWLProperty prop = null;
    			if (propList.getSelectedIndex()!=-1) {
    				prop = (OWLProperty) propList.getSelectedValue();
    			}
    			else {
    				ERROR = "No property selected for someValues restriction";
    				break;
    			}
    			
    			if (prop!=null && prop instanceof OWLObjectProperty) {
	    			desc = null;
	    			if ((valList.getSelectedIndex()!=-1 && valList.getSelectedValue() instanceof OWLDescription)) 
	    				desc = (OWLDescription) valList.getSelectedValue();
	    			else if (ceList.getSelectedIndex()!=-1)
	    				desc = (OWLDescription) ceList.getSelectedValue();
	    			
	    			if (desc!=null) {
		    			try {
							ce = ontDF.getOWLObjectSomeRestriction((OWLObjectProperty) prop, desc);
						} catch (OWLException e) {
							e.printStackTrace();
						}
	    			}
	    			else if (valList.getSelectedValue() instanceof OWLIndividual) {
	    				if (valList.getSelectedIndices().length>1) {
	    				  // someValues on oneOf set of selected inds
	    					Object[] selObj = valList.getSelectedValues();
	    					Set oneSet = new HashSet();
	    					for (int i=0; i<selObj.length; i++) oneSet.add(selObj[i]);
	    					try {
								OWLEnumeration one = ontDF.getOWLEnumeration(oneSet);
								ce = ontDF.getOWLObjectSomeRestriction((OWLObjectProperty) prop, one);
							} 
	    					catch (OWLException e) {
								e.printStackTrace();
							}	    					
	    				}
	    				else {
		    				// value restr. on object prop.
		    				OWLIndividual ind = (OWLIndividual) valList.getSelectedValue();
		    				try {
								ce = ontDF.getOWLObjectValueRestriction((OWLObjectProperty) prop, ind);
							} catch (OWLException e) {
								e.printStackTrace();
							}
	    				}
	    			}
	    			else ERROR = "No class/individual selected for someValues restriction on OWLObjectProperty";
    			}
    			else if (prop!=null) {
    				// data property restr.
    				if (valList.getSelectedValue() instanceof OWLDataType) {
    					OWLDataType dt = (OWLDataType) valList.getSelectedValue();
    					if (dataFld.getText().equals("")) {
	    					try {
								ce = ontDF.getOWLDataSomeRestriction((OWLDataProperty) prop, dt);
							} catch (OWLException e) {
								e.printStackTrace();
							}
    					}
    					else {
    						// value restr. on data prop.
    						String dVal = dataFld.getText();
    						try {
								OWLDataValue data = ontDF.getOWLConcreteData(dt.getURI(), "", dVal);
								ce = ontDF.getOWLDataValueRestriction((OWLDataProperty) prop, data);
    						} catch (OWLException e) {
								e.printStackTrace();
							}
    					}
    				}
    				else ERROR = "No datatype selected for someValues restriction on OWLDatatype Property";
    			}
    			break;
    			
    		case ALL:
    			prop = null;
    			if (propList.getSelectedIndex()!=-1) {
    				prop = (OWLProperty) propList.getSelectedValue();
    			}
    			else {
    				ERROR = "No property selected for allValues restriction";
    				break;
    			}
    			
    			if (prop!=null && prop instanceof OWLObjectProperty) {
	    			desc = null;
	    			if ((valList.getSelectedIndex()!=-1 && valList.getSelectedValue() instanceof OWLDescription)) 
	    				desc = (OWLDescription) valList.getSelectedValue();
	    			else if (ceList.getSelectedIndex()!=-1)
	    				desc = (OWLDescription) ceList.getSelectedValue();
	    			
	    			if (desc!=null) {
	    				try {
							ce = ontDF.getOWLObjectAllRestriction((OWLObjectProperty) prop, desc);
						} catch (OWLException e) {
							e.printStackTrace();
						}
	    			}
	    			else ERROR = "No class selected for allValues restriction on OWLObjectProperty";
    			}
    			else {
    				// data property restr.
    				if (valList.getSelectedValue() instanceof OWLDataType) {
    					OWLDataType dt = (OWLDataType) valList.getSelectedValue();
						try {
							ce = ontDF.getOWLDataAllRestriction((OWLDataProperty) prop, dt);
						} catch (OWLException e) {
							e.printStackTrace();
						}					
    				}
    				else ERROR = "No datatype selected for allValues restriction on OWLDatatypeProperty";
    			}
    			break;
    			
    		case EQU:
    			prop = null;
    			if (propList.getSelectedIndex()!=-1) {
    				prop = (OWLProperty) propList.getSelectedValue();
    			}
    			else {
    				ERROR = "No property selected for cardinality restriction";
    				break;
    			}
    			
    			int n = -1;
    			try {
    				n = Integer.parseInt(dataFld.getText());    				
    			}
    			catch (Exception ex) {}
    			
    			if (n<0) {
    				ERROR = "No non-negative integer specified for cardinality restriction";
    				break;
    			}
    			
    			try {
	    			if (prop instanceof OWLObjectProperty) {
	    				ce = ontDF.getOWLObjectCardinalityRestriction((OWLObjectProperty) prop, n, n);
	    			}
	    			else {
	    				ce = ontDF.getOWLDataCardinalityRestriction((OWLDataProperty) prop, n, n);
	    			}
    			}
    			catch (OWLException ex) {
    				ex.printStackTrace();
    			}
    			break;
    	
    		case MIN:
    			prop = null;
    			if (propList.getSelectedIndex()!=-1) {
    				prop = (OWLProperty) propList.getSelectedValue();
    			}
    			else {
    				ERROR = "No property selected for minCardinality restriction";
    				break;
    			}
    			
    			n = -1;
    			try {
    				n = Integer.parseInt(dataFld.getText());    				
    			}
    			catch (Exception ex) {}
    			
    			if (n<0) {
    				ERROR = "No non-negative integer specified for minCardinality restriction";
    				break;
    			}
    			
    			try {
	    			if (prop instanceof OWLObjectProperty) {
	    				ce = ontDF.getOWLObjectCardinalityAtLeastRestriction((OWLObjectProperty) prop, n);
	    			}
	    			else {
	    				ce = ontDF.getOWLDataCardinalityAtLeastRestriction((OWLDataProperty) prop, n);
	    			}
    			}
    			catch (OWLException ex) {
    				ex.printStackTrace();
    			}
    			break;
		
    		case MAX:
    			prop = null;
    			if (propList.getSelectedIndex()!=-1) {
    				prop = (OWLProperty) propList.getSelectedValue();
    			}
    			else {
    				ERROR = "No property selected for maxCardinality restriction";
    				break;
    			}
    			
    			n = -1;
    			try {
    				n = Integer.parseInt(dataFld.getText());    				
    			}
    			catch (Exception ex) {}
    			
    			if (n<0) {
    				ERROR = "No non-negative integer specified for maxCardinality restriction";
    				break;
    			}
    			
    			try {
	    			if (prop instanceof OWLObjectProperty) {
	    				ce = ontDF.getOWLObjectCardinalityAtMostRestriction((OWLObjectProperty) prop, n);
	    			}
	    			else {
	    				ce = ontDF.getOWLDataCardinalityAtMostRestriction((OWLDataProperty) prop, n);
	    			}
    			}
    			catch (OWLException ex) {
    				ex.printStackTrace();
    			}
    			break;
		}
    	
    	// add ce to ceList
		if (ce!=null) {
	    	List currCE = new ArrayList();
	    	currCE.add(ce);
	    	ListModel lm = ceList.getModel();
	    	for (int i=0; i<lm.getSize(); i++) currCE.add(lm.getElementAt(i));
	    	ceList.setListData(currCE.toArray());
	    	ceStatusLbl.setText("Class Expression added to list");
		}
		else {
			ceStatusLbl.setText("<html><font color=\"red\">"+ERROR+"!</font></html>");
//			System.out.println(ERROR);
		}
		
    	// clear selections
    	propList.clearSelection();
    	valList.clearSelection();
    	dataFld.setText("");
    }
    
    public void itemStateChanged(ItemEvent e) {
        if (e.getSource() == simpleClassOntologyCombo) {
            fillClassBox();
        }
        else if (e.getSource() == restrPropOntologyCombo) {
            if (restrPropOntologyCombo.getSelectedIndex() == 0)
                propTypeBox.setVisible(true);
            else
                propTypeBox.setVisible(false);
            displayFields();
            fillResPropBox();
        }
        else if (e.getSource() == restrClassOntologyCombo) {
            fillResClassBox();
        }
        else if (e.getSource() == resPropBox || e.getSource() == propTypeBox) {
            displayFields();
        }
        else if (e.getSource() == resNameCombo) {
            if (objResLbl == null)
                return; // UI hasn't been initialized
            displayFields();

            String val = (String) resNameCombo.getSelectedItem();
//            if (val.indexOf("values-from") == -1)
//                mNestedRestrictionButton.setEnabled(false);
//            else mNestedRestrictionButton.setEnabled(true);
        }
        else if (e.getSource() == ontPropBox) {
        	fillPropList();
        }
        else if (e.getSource() == ontValBox) {
        	fillValList();
        }
        else if (e.getSource() == valChooser) {
        	fillValList();
        }
    }
    /**
     * Fill the class combo box with OWL classes from the ontology selected in
     * the ont combo box
     */
    private void fillClassBox() {
        try {
            // fill classes in classBox based on selected ontology
            if (simpleClassOntologyCombo.getSelectedIndex() == 0) {
                classBox.setEditable(true);
                classBox.removeAllItems();
                return;
            }
            classBox.setEditable(false);
            classBox.setFont(new Font(swoopModel.getFontFace(), Font.PLAIN, 11));
            String uri = simpleClassOntologyCombo.getSelectedItem().toString();
            URI ontURI = new URI(uri);
            OWLOntology currOnt = swoopModel.getOntology(ontURI);
            // add classes
            Set claSet = new TreeSet(EntityComparator.INSTANCE);
            claSet.addAll(currOnt.getClasses());
            classBox.removeAllItems();
            Iterator iter = claSet.iterator();
            while (iter.hasNext()) {
                OWLClass cla = (OWLClass) iter.next();
                classBox.addItem(cla.getURI().toString());
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    private void fillClassList() {
        try {
            // fill classes in classBox based on selected ontology
            String uri = ontList.getSelectedValue().toString();
            URI ontURI = new URI(uri);
            OWLOntology currOnt = swoopModel.getOntology(ontURI);
            // add classes to list
            Set claSet = new TreeSet(EntityComparator.INSTANCE);
            claSet.addAll(currOnt.getClasses());
            // add owl:Thing and owl:Northing
            claSet.add(currOnt.getOWLDataFactory().getOWLThing());
            claSet.add(currOnt.getOWLDataFactory().getOWLNothing());
            classList.setFont(new Font(swoopModel.getFontFace(), Font.PLAIN, 11));
            classList.setListData(claSet.toArray());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    /*
     * Fill value list in CE panel depending on ontology selection box
     * and type selection box (classes/individuals)
     */
    private void fillValList() {
    	try {
	    	valList.removeListSelectionListener(this);
	    	valList.setFont(new Font(swoopModel.getFontFace(), Font.PLAIN, 11));
	        String uri = ontValBox.getSelectedItem().toString();
	        URI ontURI = new URI(uri);
	        OWLOntology currOnt = swoopModel.getOntology(ontURI);
	        Set valSet = new TreeSet(EntityComparator.INSTANCE);
	        if (valChooser.getSelectedIndex()==0) {
	        	valSet.addAll(currOnt.getClasses());
	        	// add owl:Thing and owl:Northing
	            valSet.add(currOnt.getOWLDataFactory().getOWLThing());
	            valSet.add(currOnt.getOWLDataFactory().getOWLNothing());
	        	valList.setListData(valSet.toArray());
	        }
	        else if (valChooser.getSelectedIndex()==1) {
	        	valSet.addAll(currOnt.getIndividuals());
	        	valList.setListData(valSet.toArray());
	        }
	        else valList.setListData(dataTypes.toArray());
	        
	        valList.addListSelectionListener(this);
    	}
    	catch (Exception ex) {
    		ex.printStackTrace();
    	}
    }
    
    /* 
     * Fill Property List in CE Panel when ontology selection changes
     */
    private void fillPropList() {
    	try {
	    	propList.removeListSelectionListener(this);
	    	propList.setFont(new Font(swoopModel.getFontFace(), Font.PLAIN, 11));
	        String uri = ontPropBox.getSelectedItem().toString();
	        URI ontURI = new URI(uri);
	        OWLOntology currOnt = swoopModel.getOntology(ontURI);
	        Set propSet = new TreeSet(EntityComparator.INSTANCE);
	        propSet.addAll(currOnt.getDataProperties());
	        propSet.addAll(currOnt.getObjectProperties());
	        propList.setListData(propSet.toArray());
	        propList.addListSelectionListener(this);
    	}
    	catch (Exception ex) {
    		ex.printStackTrace();
    	}
    }
    
    /**
     * Fill restriction property box with OWL properties from the ontology
     * selected in the ont-combo box
     */
    private void fillResPropBox() {
        try {
            // fill classes in resPropBox based on selected ontology
            resPropBox.removeItemListener(this);
            if (restrPropOntologyCombo.getSelectedIndex() == 0) {
                resPropBox.setEditable(true);
                resPropBox.removeAllItems();
                return;
            }
            resPropBox.setFont(new Font(swoopModel.getFontFace(), Font.PLAIN, 11));
            resPropBox.setEditable(false);
            String uri = restrPropOntologyCombo.getSelectedItem().toString();
            URI ontURI = new URI(uri);
            OWLOntology currOnt = swoopModel.getOntology(ontURI);
            Set propSet = new TreeSet(EntityComparator.INSTANCE);
            propSet.addAll(currOnt.getDataProperties());
            propSet.addAll(currOnt.getObjectProperties());
            resPropBox.removeAllItems();
            Iterator iter = propSet.iterator();
            while (iter.hasNext()) {
                OWLProperty prop = (OWLProperty) iter.next();
                resPropBox.addItem(prop);
            }
            resPropBox.addItemListener(this);
            displayFields();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    /**
     * Key function that re-renders fields of the UI depending on changes in
     * user selection for certain restriction parameters e.g. when cardinalty
     * restriction is selected --> display field for integer; or when object
     * property and hasValue restriction selected --> display field for
     * instances
     */
    private void displayFields() {
        if (resNameCombo.getSelectedIndex() < 3) {
            // cardinality restriction of some sort
            restrClassOntologyCombo.setEnabled(false);
            resClassBox.removeAllItems();
            resClassBox.setEditable(true);
            objResLbl.setText("Specify Integer: ");
            resClassPanel.setVisible(true);
            dTypePanel.setVisible(false);
            return;
        }
        restrClassOntologyCombo.setEnabled(true);
        if (resNameCombo.getSelectedIndex() == 3) {
            // has-value restriction: show datatype/value fields
            if ((restrPropOntologyCombo.getSelectedIndex() == 0 && propTypeBox
                    .getSelectedIndex() == 1)
                    || (restrPropOntologyCombo.getSelectedIndex() != 0 && resPropBox
                            .getSelectedItem() instanceof OWLObjectProperty)) {
                objResLbl.setText("Select Individual:");
                fillType = "Instances";
                fillResClassBox();
            } else {
            	dTypePanel.setVisible(true);
                resDTypeFld.setVisible(true);
            	repaint();
            }
        } else {
            // some/all values restriction
            if ((restrPropOntologyCombo.getSelectedIndex() == 0 && propTypeBox
                    .getSelectedIndex() == 1)
                    || (restrPropOntologyCombo.getSelectedIndex() != 0 && resPropBox
                            .getSelectedItem() instanceof OWLObjectProperty)) {
                objResLbl.setText("Select Class:");
                fillType = "Classes";
                fillResClassBox();
            } else {
                resDTypeFld.setVisible(false);
                repaint();
            }
        }
        // determine property type - Data or Object
        int propType = -1;
        if (restrPropOntologyCombo.getSelectedIndex() == 0) {
            propType = propTypeBox.getSelectedIndex();
        }
        //else if (resPropBox.getSelectedItem()==null) return;
        else if (resPropBox.getSelectedItem() instanceof OWLProperty) {
            OWLProperty resProp = (OWLProperty) resPropBox.getSelectedItem();
            if (resProp instanceof OWLDataProperty) {
                propType = 0;
            } else if (resProp instanceof OWLObjectProperty) {
                propType = 1;
            }
        }
        if (propType == 0) {
            // hide class/instance value box
            dTypePanel.setVisible(true);
            resClassPanel.setVisible(false);
        } else if (propType == 1) {
            // hide datarange value box
            dTypePanel.setVisible(false);
            resClassPanel.setVisible(true);
        }
    }
    /**
     * Fill restriction class box with OWL Classes whenever ontology selection
     * changes in corresponding ont-combo box
     */
    private void fillResClassBox() {
        try {
            // fill classes in resClassBox based on selected ontology
            if (restrClassOntologyCombo.getSelectedIndex() == 0) {
                resClassBox.setEditable(true);
                resClassBox.removeAllItems();
                return;
            }
            resClassBox.setFont(new Font(swoopModel.getFontFace(), Font.PLAIN, 11));
            resClassBox.setEditable(false);
            //************************************************
            //Added for Econnections
            //**************************************************
            OWLProperty prop = (OWLProperty) resPropBox.getSelectedItem();
            URI ontURI = null;
            OWLOntology currOnt = null;
            String uri = null;
            if (prop instanceof OWLObjectProperty) {
                if (!(((OWLObjectProperty) prop).isLink())) {
                    uri = restrClassOntologyCombo.getSelectedItem().toString();
                } else {
                    uri = ((OWLObjectProperty) prop).getLinkTarget().toString();
                    restrClassOntologyCombo.setEnabled(false);
                }
                ontURI = new URI(uri);
                if (!(swoopModel.getOntology(ontURI) == null)) {
                    currOnt = swoopModel.getOntology(ontURI);
                } else {
                    currOnt = swoopModel.loadOntology(ontURI);
                    //		             currOnt = swoopModel.getOntology(ontURI);
                }
            }
            if (fillType.equals("Classes")) {
                // add classes
                Set claSet = new TreeSet(EntityComparator.INSTANCE);
                claSet.addAll(currOnt.getClasses());
                resClassBox.removeAllItems();
                Iterator iter = claSet.iterator();
                while (iter.hasNext()) {
                    OWLClass cla = (OWLClass) iter.next();
                    resClassBox.addItem(cla);
                }
                // add datatypes
                String xmls = "http://www.w3.org/2001/XMLSchema#";
                String[] datatypes = {"int", "nonNegativeInteger", "long",
                        "string", "float", "boolean", "date", "time"};
                for (int i = 0; i < datatypes.length; i++) {
                    String dataURI = xmls + datatypes[i];
                    //resClassBox.addItem(dataURI);
                }
            } else if (fillType.equals("Instances")) {
                // add instances
                Set indSet = new TreeSet(EntityComparator.INSTANCE);
                indSet.addAll(currOnt.getIndividuals());
                resClassBox.removeAllItems();
                Iterator iter = indSet.iterator();
                while (iter.hasNext()) {
                    OWLIndividual ind = (OWLIndividual) iter.next();
                    resClassBox.addItem(ind);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    public void valueChanged(ListSelectionEvent e) {
        if (e.getSource() == ontList) {
            fillClassList();
        }
    }
    public void keyTyped(KeyEvent arg0) {
    }
    public void keyPressed(KeyEvent e) {
        if (e.getSource() == classList) {
            String alpha = Character.toString(e.getKeyChar()).toLowerCase();
            PopupCommon.listSelector(swoopModel, (JList) e.getSource(), alpha);
        }
        else if (e.getSource() == newClassFld) {
            if (e.getKeyCode() == 10) {
                if (mainTab.getSelectedIndex() == 0)
                    addClassChange();
                else if (mainTab.getSelectedIndex() == 1)
                    addRestrictionChange();
            }
        }
        else if (e.getSource() == ceList) {
        	if (e.getKeyCode() == KeyEvent.VK_DELETE) {
                // delete selected items from list
            	ListModel lm = ceList.getModel();
            	List newCE = new ArrayList();
            	for (int i=0; i<lm.getSize(); i++) {
            		boolean add = true;
            		for (int j=0; j<ceList.getSelectedValues().length; j++) {
            			if (lm.getElementAt(i).equals(ceList.getSelectedValues()[j])) add = false;
            		}
            		if (add) newCE.add(lm.getElementAt(i));
            	}
            	ceList.setListData(newCE.toArray());
            	ceStatusLbl.setText("Class Expression(s) deleted from list");
            }
        }
    }
    public void keyReleased(KeyEvent arg0) {
    }
    
    /*
     * after user specifies LHS of GCI, create a new popup for
     * allowing user to enter RHS of GCI
     */
    private void addLHSGCI() {
    	currClassExpr = (OWLDescription) ceList.getSelectedValue();
    	if (currClassExpr!=null) {
    		// create nested popup for RHS of GCI
    		PopupAddClass newDialog = new PopupAddClass(reasoner, "GCI-RIGHT", swoopModel, currClassExpr);
    		newDialog.setLocation(this.getLocation());
    		newDialog.show();    		
    		dispose();    		 
    	}
    	else ceStatusLbl.setText("No Class Expression selected!");
    }
    
    /*
     * after user specified RHS of GCI, create GCI and add it
     * to the ontology directly using swoopModel.addGCI which
     * will also notify listeners and refresh reasoner
     */
    private void addRHSGCI() {
    	currClassExpr = (OWLDescription) ceList.getSelectedValue();
    	if (currClassExpr!=null) {
    		// if RHS of GCI is selected, make the change here itself
    		// create GCI, make change and dispose
			OWLOntology ont = swoopModel.getSelectedOntology();
			swoopModel.addGCI(ont, lhsGCI, currClassExpr);
    		dispose();
    	}
    	else ceStatusLbl.setText("No Class Expression selected!"); 
    }
}
