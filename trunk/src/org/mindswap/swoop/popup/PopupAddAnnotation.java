package org.mindswap.swoop.popup;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.mindswap.swoop.SwoopModel;
import org.mindswap.swoop.renderer.CellRenderer;
import org.mindswap.swoop.renderer.SwoopCellRenderer;
import org.mindswap.swoop.utils.ui.EntityComparator;
import org.semanticweb.owl.io.vocabulary.OWLVocabularyAdapter;
import org.semanticweb.owl.io.vocabulary.RDFSVocabularyAdapter;
import org.semanticweb.owl.model.OWLAnnotationProperty;
import org.semanticweb.owl.model.OWLDataValue;
import org.semanticweb.owl.model.OWLException;
import org.semanticweb.owl.model.OWLNamedObject;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.change.AddAnnotationInstance;
import org.semanticweb.owl.model.helper.OntologyHelper;

import com.hexidec.ekit.EkitCore;

/**
 * @author Aditya
 * This class pops up when the user clicks on the 'Add Annotation' link
 * in the ConciseFormatEntityRenderer.  It allows the user to select the annotation
 * property, specify the content and add/apply the annotation instance on the
 * current selected Swoop entity
 * 
 */

public class PopupAddAnnotation extends JFrame implements ActionListener, ListSelectionListener, KeyListener {
	
	SwoopModel swoopModel;
	public List changes;
	Font tahoma = new Font("Tahoma", Font.PLAIN, 11);
	JButton applyBtn, addAnnotBtn, cancelBtn;
	JList ontList, propList;
	JComboBox defaultAP;
	JComboBox langBox;
	JTextField newPropFld;
	JTextArea annotFld;
	JTabbedPane annotContentTabs;
	EkitCore ekitCore;
	JPanel ekitPanel;
	static String RDFS = RDFSVocabularyAdapter.RDFS;
	static String OWL = OWLVocabularyAdapter.OWL;
	
	public PopupAddAnnotation(SwoopModel swoopModel, boolean isOntology) {
		// setModal(true);
		this.swoopModel = swoopModel;
		this.changes = new ArrayList();
		setDefaultAnnotProps(isOntology);
		setupUI();
	}
	
	private OWLAnnotationProperty getAnnotProp(String uriStr) {
		try {
			URI uri = new URI(uriStr);
			OWLAnnotationProperty prop = swoopModel.getSelectedOntology().getOWLDataFactory().getOWLAnnotationProperty(uri);
			return prop;
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * Set the list of default OWL annotation properties for the user 
	 * @param isOntology
	 */
	private void setDefaultAnnotProps(boolean isOntology) {
		
		defaultAP = new JComboBox();
		
		Set defaultSet = new TreeSet(EntityComparator.INSTANCE);
		if (isOntology) {
			defaultSet.add(getAnnotProp(OWL+"versionInfo"));
		}
		
		defaultSet.add(getAnnotProp(RDFS+"label"));
		defaultSet.add(getAnnotProp(RDFS+"comment"));
		defaultSet.add(getAnnotProp(RDFS+"seeAlso"));
		defaultSet.add(getAnnotProp(RDFS+"isDefinedBy"));
		
		// AK: only for Swoop 2.2 beta 2
		// list all annotation properties in selected ontology in swoopmodel
		Set ontologies = new HashSet();
		try {
			ontologies = OntologyHelper.importClosure(swoopModel.getSelectedOntology());
		} catch (OWLException e1) {
			e1.printStackTrace();
		}
		Iterator iter = ontologies.iterator();
		while (iter.hasNext()) {
			OWLOntology ont = (OWLOntology) iter.next();
			try {
				defaultSet.addAll(ont.getAnnotationProperties());
			} catch (OWLException e) {
				e.printStackTrace();
			}
		}
		
		Iterator apIter = defaultSet.iterator();
		while (apIter.hasNext()) {
			defaultAP.addItem((OWLAnnotationProperty) apIter.next());
		}
		
		// select rdfs:comment as the default annotation property
		defaultAP.setSelectedItem(getAnnotProp(RDFS+"comment"));
		
		defaultAP.setRenderer(new SwoopCellRenderer(swoopModel));
		defaultAP.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				ontList.clearSelection();
				propList.clearSelection();
			}
		});
	}
	
	private void setupUI() {
		
		JSplitPane tab1 = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		ontList = new JList();
		ontList.setFont(tahoma);
		ontList.setCellRenderer(new CellRenderer());
		propList = new JList();
		propList.setFont(tahoma);
		propList.setCellRenderer(new SwoopCellRenderer(swoopModel));
		propList.addKeyListener(this);
		applyBtn = new JButton("Add");
		applyBtn.setFont(tahoma);
		applyBtn.addActionListener(this);
		addAnnotBtn = new JButton("Add & Close");
		addAnnotBtn.setFont(tahoma);
		addAnnotBtn.addActionListener(this);
		JLabel ontLbl = new JLabel("2. OR Select Ontology:");
		ontLbl.setFont(tahoma);
		JLabel propLbl = new JLabel("Select Property:");
		propLbl.setFont(tahoma);
		
		JPanel tab1W = new JPanel();
		tab1W.setLayout(new BorderLayout());
		tab1W.add(ontLbl, "North");
		tab1W.add(new JScrollPane(ontList), "Center");		
		JPanel tab1E = new JPanel();
		tab1E.setLayout(new BorderLayout());
		tab1E.add(propLbl, "North");
		tab1E.add(new JScrollPane(propList), "Center");
		JSplitPane tab1Split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		tab1Split.setLeftComponent(tab1W);
		tab1Split.setRightComponent(tab1E);
		
		JPanel btnPanel = new JPanel();
		btnPanel.setLayout(new GridLayout(1,3));
		btnPanel.add(applyBtn);
		btnPanel.add(addAnnotBtn);
		cancelBtn = new JButton("Cancel");
		cancelBtn.setFont(tahoma);
		cancelBtn.addActionListener(this);
		btnPanel.add(cancelBtn);
		
		newPropFld = new JTextField();
		newPropFld.setFont(tahoma);
		newPropFld.addKeyListener(this);
		JPanel newPanel = new JPanel();
		newPanel.setLayout(new BorderLayout());
		JLabel newLbl = new JLabel("3. OR Specify New Property Name/URI: ");
		newLbl.setFont(tahoma);
		newPanel.add(newLbl, "West");
		newPanel.add(newPropFld, "Center");
		
		// create default annotation prop list at top
		JPanel topPanel = new JPanel();
		topPanel.setLayout(new GridLayout(2,1));
		JLabel defaultLbl = new JLabel("Select OWL Annotation Property: ");
		defaultLbl.setFont(tahoma);
		JPanel defBar = new JPanel();
		defBar.setLayout(new BorderLayout());
		defBar.add(defaultLbl,"West");
		defBar.add(defaultAP, "Center");
		
		JPanel langBar = new JPanel();
		langBar.setLayout(new BorderLayout());
		JLabel langLbl = new JLabel("Specify Language Tag (eg. en, fr, ja): ");
		langLbl.setFont(tahoma);		
		langBox = new JComboBox();
		langBox.setFont(tahoma);
		langBox.addItem("   (plain literals)");
		langBox.addItem("ar (Arabic)");
		langBox.addItem("zh (Chinese - Mandarin)");
		langBox.addItem("nl (Dutch)");
		langBox.addItem("en (English)");
		langBox.addItem("fr (French)");
		langBox.addItem("de (German)");
		langBox.addItem("he (Hebrew)");
		langBox.addItem("hi (Hindi)");
		langBox.addItem("it (Italian)");
		langBox.addItem("ja (Japanese)");
		langBox.addItem("ko (Korean)");
		langBox.addItem("fa (Persian - Farsi)");
		langBox.addItem("po (Portuguese)");
		langBox.addItem("ru (Russian)");
		langBox.addItem("es (Spanish)");
		langBox.addItem("sw (Swahili)");
		
		// bzhao
		//langBox.setSelectedIndex(3);
		langBox.setSelectedIndex(4);
		
		langBar.add(langLbl, "West");
		langBar.add(langBox, "Center");
		topPanel.add(defBar);
		topPanel.add(langBar);
		
		JPanel tab1P = new JPanel();
		tab1P.setLayout(new BorderLayout());
		tab1P.add(topPanel, "North");
		tab1P.add(tab1Split, "Center");
		tab1P.add(newPanel, "South");

		// setup Ekit
		ekitPanel = new JPanel();
		ekitPanel.setLayout(new BorderLayout());
		ekitCore = new EkitCore(null, null, null, null, false, true, true, null, null, false, false);
		JPanel ekitTopPanel = new JPanel();
		ekitTopPanel.setLayout(new GridLayout(2,1));
		ekitTopPanel.add(ekitCore.getMenuBar());
		ekitTopPanel.add(ekitCore.getToolBar(true));
		ekitPanel.add(ekitTopPanel, "North");
		ekitPanel.add(ekitCore, "Center");		
		
		annotFld = new JTextArea();
		annotFld.setFont(tahoma);
		annotContentTabs = new JTabbedPane();
		annotContentTabs.setFont(tahoma);
		annotContentTabs.add("Text", new JScrollPane(annotFld));
		annotContentTabs.add("HTML", ekitPanel);
		
		JPanel lowerPanel = new JPanel();
		lowerPanel.setLayout(new BorderLayout());
		JLabel acLbl = new JLabel("Specify Annotation Content");
		acLbl.setFont(new Font("Tahoma", Font.PLAIN, 11));
		lowerPanel.add(acLbl, "North");
		lowerPanel.add(annotContentTabs, "Center");
		lowerPanel.add(btnPanel, "South");
		
		//AK: only for Swoop 2.2 beta 2 release
		JPanel mergePanel = new JPanel();
		mergePanel.setLayout(new BorderLayout());
		mergePanel.add(topPanel, "North");
		mergePanel.add(lowerPanel, "Center");
		
		tab1.setBottomComponent(mergePanel);
		
		fillValues();
		
		// add tabbed pane to frame container
		Container content = getContentPane();
		content.setLayout(new BorderLayout());
		JLabel typeLbl = new JLabel("[ ADDING ANNOTATION.. ]");
		typeLbl.setFont(tahoma);
		content.add(typeLbl, "North");
		content.add(tab1, "Center");
		setSize(500,400);
		setTitle("Specify Annotation");
		setResizable(true);
		tab1.setDividerLocation(25);
		tab1.setOneTouchExpandable(true);
	}

	private void fillValues() {
		Set ont = swoopModel.getOntologyURIs();
    	ontList.setListData(ont.toArray());
    	// turn on listeners
    	ontList.addListSelectionListener(this);
	}
	
	/**
	 * Method that is called when add/apply annotation change button is pressed
	 * It obtains the OWL property specified by the user
	 * and passes it to makeChange(..) to create the corresponding
	 * AddAnnotationInstance Change
	 */
	private void addAnnotationChange() {
		
		try {
			OWLAnnotationProperty prop = null;
			
			if ((newPropFld.getText()!=null) && (!newPropFld.getText().trim().equals(""))) {
				// user-specified property name/uri
				String propStr = newPropFld.getText();				
				if (!isURL(propStr)) propStr = swoopModel.getSelectedOntology().getLogicalURI()+"#"+propStr;
				URI	propURI = new URI(propStr);
				// **create new object property by default
				prop = swoopModel.getSelectedOntology().getOWLDataFactory().getOWLAnnotationProperty(propURI);
				makeChange(prop);
			}
			else if (propList.getSelectedIndex()==-1) {
				// get default prop
				prop = (OWLAnnotationProperty) defaultAP.getSelectedItem();
				makeChange(prop);
			}
			else {
				// get existing property
				Object[] props = (Object[]) propList.getSelectedValues();
				for (int i=0; i<props.length; i++) {
					prop = (OWLAnnotationProperty) props[i];
					makeChange(prop);
				}
			}
			
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	/**
	 * Method that creates the AddAnnotationInstance change based on currently
	 * selected ontology, entity and the OWL Annotation Property passed to it
	 * The content is obtained from either the text area or HTML pane (Ekit)
	 * @param prop - OWL Annotation property used to specify annotation
	 */
	private void makeChange(OWLAnnotationProperty prop) {
		
		// get change parameters
		OWLOntology dispOnt = swoopModel.getSelectedOntology();
		OWLNamedObject dispObject = swoopModel.getSelectedObject();
		
		String annotationData = ""; 
		if (annotContentTabs.getSelectedIndex()==0) {
			annotationData = annotFld.getText().trim();			
		}
		else { 
			// create Datatype XML Literal
			// or escape parser using &lt; &gt; &amp;
			annotationData = ekitCore.getTextPane().getText();
		}
		
		// escape tags - No, don't! (-Ron)
//		annotationData = annotationData.replaceAll("&", "&amp;");
//		annotationData = annotationData.replaceAll("<", "&lt;");
//		annotationData = annotationData.replaceAll(">", "&gt;");
		
		OWLDataValue dVal = null;
		try {
		    
		    // bzhao
			// create owl data value
			// String langFld = langBox.getSelectedItem().toString();
			String langFld = null;
			
			if (langBox.getSelectedIndex() != 0) {
			  langFld = langBox.getSelectedItem().toString();
			  langFld = langFld.substring(0, langFld.indexOf("(")).trim();
			}
			
			//langFld = langFld.substring(0, langFld.indexOf("(")).trim();
			dVal = dispOnt.getOWLDataFactory().getOWLConcreteData(null, langFld, annotationData);
		} catch (OWLException e1) {
			e1.printStackTrace();
		}
		
		// create add-annotation-instance change
		
		AddAnnotationInstance change = null;
		change = new AddAnnotationInstance(dispOnt, dispObject, prop, dVal, null);		
		
		swoopModel.addUncommittedChange(change);		
	}
	
	private boolean isURL(String str) {		
		try {
			URL url = new URL(str);
			return true;
		}
		catch (Exception ex) {}
		return false;
	}
	
	public void actionPerformed(ActionEvent e) {
		
		if (e.getSource()==addAnnotBtn) {
			addAnnotationChange();			
			dispose();
		}
		
		if (e.getSource()==applyBtn) {
			addAnnotationChange();						
		}
		
		if (e.getSource()==cancelBtn) {
			dispose();
		}
		
	}

	private void fillPropBox() {
		try {
			// fill props in PropBox based on selected ontology
			if (ontList.getSelectedIndex()==-1) return;
			URI ontURI = (URI) ontList.getSelectedValue();
			OWLOntology currOnt = swoopModel.getOntology(ontURI); 
			Set propSet = new TreeSet(EntityComparator.INSTANCE);
			propSet.addAll(currOnt.getAnnotationProperties());
			propList.setListData(propSet.toArray());
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void keyTyped(KeyEvent arg0) {
		}

	public void keyPressed(KeyEvent e) {
		if (e.getSource()==propList) {
			String alpha = Character.toString(e.getKeyChar()).toLowerCase();
			PopupCommon.listSelector(swoopModel, (JList) e.getSource(), alpha);
		}
		
		if (e.getSource()==newPropFld) {
			// clear selection in other choices
			defaultAP.setSelectedIndex(-1);
			propList.clearSelection();
			
			if (e.getKeyCode()==10) {
				addAnnotBtn.doClick();
			}
		}
	}

	public void keyReleased(KeyEvent arg0) {
	}
	
	public void valueChanged(ListSelectionEvent e) {
		if (e.getSource()==ontList) {
    		fillPropBox();
    	}
		
	}
}
