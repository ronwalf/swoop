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
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.ListModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.mindswap.swoop.SwoopModel;
import org.mindswap.swoop.reasoner.SwoopReasoner;
import org.mindswap.swoop.renderer.CellRenderer;
import org.mindswap.swoop.renderer.SwoopCellRenderer;
import org.mindswap.swoop.utils.ui.EntityComparator;
import org.mindswap.swoop.utils.ui.OntologyComparator;
import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLDataProperty;
import org.semanticweb.owl.model.OWLDescription;
import org.semanticweb.owl.model.OWLEntity;
import org.semanticweb.owl.model.OWLEquivalentPropertiesAxiom;
import org.semanticweb.owl.model.OWLException;
import org.semanticweb.owl.model.OWLIndividual;
import org.semanticweb.owl.model.OWLObjectProperty;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLProperty;
import org.semanticweb.owl.model.change.AddDomain;
import org.semanticweb.owl.model.change.AddEntity;
import org.semanticweb.owl.model.change.AddForeignEntity;
import org.semanticweb.owl.model.change.AddIndividualClass;
import org.semanticweb.owl.model.change.AddInverse;
import org.semanticweb.owl.model.change.AddObjectPropertyRange;
import org.semanticweb.owl.model.change.AddPropertyAxiom;
import org.semanticweb.owl.model.change.AddSuperProperty;
import org.semanticweb.owl.model.change.ChangeVisitor;
import org.semanticweb.owl.model.change.OntologyChange;
import org.semanticweb.owl.model.change.RemoveEntity;

/**
 * @author Aditya Kalyanpur
 * This class pops up whenever the user needs to specify an OWL Property
 * in an Ontology Change (For example: adding a superproperty, inverse property,
 * hasDomain/Range attribute on a OWL Class, or a property-value for an instance)
 */
public class PopupAddProperty extends JFrame implements ActionListener, ListSelectionListener, KeyListener {
	
	SwoopReasoner reasoner;
	SwoopModel swoopModel;
	public List changes;
	String type;
	int typeIntCode;
	Font tahoma = new Font("Tahoma", Font.PLAIN, 11);
	Font tahomaB = new Font("Tahoma", Font.BOLD, 10);	
	JButton applyBtn, addPropBtn, cancelBtn;
	JList ontList, propList;
	JTextField newPropFld;
	JComboBox propTypeBox;
	boolean showDataProp = true, showObjectProp = true;
	
	public PopupAddProperty(SwoopReasoner reas,  String type, SwoopModel swoopModel) {
		new PopupAddProperty(reas,  type, swoopModel, true, true);
	}
	
	public PopupAddProperty(SwoopReasoner reas,  String type, SwoopModel swoopModel, boolean showDP, boolean showOP) {
		// setModal(true);
		this.reasoner = reas;
		this.type = getFullType(type);
		this.swoopModel = swoopModel;
		this.showDataProp = showDP;
		this.showObjectProp = showOP;
		changes = new ArrayList();
		setupUI();
	}
	
	/**
	 * Get the full type of the property-related-change 
	 * given its abbreviated type code
	 * @param type - code for the change type
	 * @return
	 */
	private String getFullType(String type) {
		
		// get full string type from type code
		if (type.equals("EQU")) { typeIntCode = 0; return "EQUIVALENT PROPERTY"; }
		if (type.equals("SUB")) { typeIntCode = 1; return "SUB-PROPERTY"; }
		if (type.equals("SUP")) { typeIntCode = 2; return "SUPER-PROPERTY"; }
		if (type.equals("DOM")) { typeIntCode = 3; return "DOMAIN OF"; }
		if (type.equals("RAN")) { typeIntCode = 4; return "RANGE OF"; }
		if (type.equals("INV")) { typeIntCode = 5; return "INVERSE OF"; }
		if (type.startsWith("INS")) { typeIntCode = 6; return "TO INSTANCE"; }		
		return "";
	}
	
	private void setupUI() {
		
		JPanel tab1 = new JPanel();
		tab1.setLayout(new BorderLayout());
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
		addPropBtn = new JButton("Add & Close");
		addPropBtn.setFont(tahoma);
		addPropBtn.addActionListener(this);
		
		// special case: adding property-value pair for instance
		if (typeIntCode==6) {
			applyBtn.setVisible(false);
			addPropBtn.setLabel("Select Prop & Proceed");
		}
		
		JLabel ontLbl = new JLabel("Select Ontology:");
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
		tab1Split.setOneTouchExpandable(true);
		tab1Split.setLeftComponent(tab1W);
		tab1Split.setRightComponent(tab1E);
		
		JPanel btnPanel = new JPanel();
		btnPanel.setLayout(new GridLayout(1,3));
		btnPanel.add(applyBtn);
		if (typeIntCode==6) btnPanel.add(addPropBtn);
		cancelBtn = new JButton("Cancel");
		cancelBtn.setFont(tahoma);
		cancelBtn.addActionListener(this);
		btnPanel.add(cancelBtn);
		
		newPropFld = new JTextField();
		newPropFld.setFont(tahoma);
		newPropFld.addKeyListener(this);
		JPanel newPanel = new JPanel();
		newPanel.setLayout(new BorderLayout());
		JLabel newLbl = new JLabel("OR Specify New Term/URI: ");
		newLbl.setFont(tahoma);
		JPanel newLblPanel = new JPanel();
		newLblPanel.setLayout(new BorderLayout());
		newLblPanel.add(newLbl, "West");
		propTypeBox = new JComboBox();
		propTypeBox.addItem("[D]");
		propTypeBox.addItem("[O]");
		
		newLblPanel.add(propTypeBox, "Center");
		newPanel.add(newLblPanel, "West");
		newPanel.add(newPropFld, "Center");
		
		JPanel tab1P = new JPanel();
		tab1P.setLayout(new BorderLayout());
		tab1P.add(tab1Split, "Center");
		tab1P.add(newPanel, "South");
		
		tab1.add(tab1P, "Center");
		tab1.add(btnPanel, "South");
		
		fillValues();
		
		// add tabbed pane to frame container
		Container content = getContentPane();
		content.setLayout(new BorderLayout());
		JLabel typeLbl = new JLabel("[ ADDING "+type+".. ]");
		typeLbl.setFont(tahoma);
		content.add(typeLbl, "North");
		content.add(tab1, "Center");
		setSize(450,300);
		setTitle("Specify Property");
		setResizable(true);
	}

	/**
	 * Fill the ontology combo-box with all the ontologies present in the SwoopModel
	 * Choosing a different ontology here causes a change in the property list
	 * i.e. fillPropBox()
	 */
	private void fillValues() {
		
		Set sortedOntSet = new TreeSet(OntologyComparator.INSTANCE);
		sortedOntSet.addAll(swoopModel.getOntologyURIs());
		ontList.setFont(new Font(swoopModel.getFontFace(), Font.PLAIN, 11));
		ontList.setListData(sortedOntSet.toArray());
    	
    	// turn on listeners
    	ontList.addListSelectionListener(this);
    	
    	// select current displayed ontology
    	try {
    		URI currOntURI = swoopModel.getSelectedOntology().getURI();
    		ontList.setSelectedValue(currOntURI, true);
    	}
    	catch (Exception ex) {
    		ex.printStackTrace();
    	}
	}
	
	/**
	 * Method called when the user presses the add/apply property change btn
	 * It gets the property selected and passes it to makeChange(..)
	 */
	private void addPropertyChange() {
		
		try {
			OWLProperty prop = null;
			
			if ((newPropFld.getText()!=null) && (!newPropFld.getText().trim().equals(""))) {
				// user-specified property name/uri
				String propStr = newPropFld.getText();
				propStr = propStr.replaceAll(" ","_");
				if (!isURL(propStr)) propStr = swoopModel.getSelectedOntology().getLogicalURI()+"#"+propStr;
				URI	propURI = new URI(propStr);
				// **create new property
				if (propTypeBox.getSelectedIndex()==0) prop = swoopModel.getSelectedOntology().getOWLDataFactory().getOWLDataProperty(propURI);
				else prop = swoopModel.getSelectedOntology().getOWLDataFactory().getOWLObjectProperty(propURI);
				makeChange(prop);
			}
			else {
				// get existing property
				Object[] props = (Object[]) propList.getSelectedValues();
				for (int i=0; i<props.length; i++) {
					prop = (OWLProperty) props[i];
					makeChange(prop);
				}
			}
			
			swoopModel.addUncommittedChanges(changes);
			changes = new ArrayList(); // reset it after changes have been added
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	/**
	 * Creates the Property related Ontology Change based on the 
	 * currently selected ontology, entity and OWL Property passed to it
	 * @param prop - object (OWL Property) of the ontology change
	 */
	private void makeChange(OWLProperty prop) {
		OntologyChange change = null;
		OWLOntology dispOnt = swoopModel.getSelectedOntology();
		
		switch (typeIntCode) {
			case 0: // add equivalent property
				OWLProperty dispProp = (OWLProperty) swoopModel.getSelectedEntity();
				Set equSet = new HashSet();
				equSet.add(dispProp);
				equSet.add(prop);
				try {
					OWLEquivalentPropertiesAxiom propAxiom = dispOnt.getOWLDataFactory().getOWLEquivalentPropertiesAxiom(equSet);
					change = new AddPropertyAxiom(dispOnt, propAxiom, null);
					changes.add(change);
				}
				catch (OWLException e) {
					e.printStackTrace();
				}
				
				break;
		
			case 2: // add sub-property
				OWLProperty dispProp2 = (OWLProperty) swoopModel.getSelectedEntity();
				change = new AddSuperProperty(dispOnt, prop, dispProp2, null);
				changes.add(change);
				break;
		
			case 1: // add super-property
				OWLProperty dispProp3 = (OWLProperty) swoopModel.getSelectedEntity();
				change = new AddSuperProperty(dispOnt, dispProp3, prop, null);
				changes.add(change);
				break;
			
			case 3: // add domain
				OWLClass cla = (OWLClass) swoopModel.getSelectedEntity();
				change = new AddDomain(dispOnt, prop, cla, null);
				changes.add(change);
				break;
			
			case 4: // add range
				OWLClass cla2 = (OWLClass) swoopModel.getSelectedEntity();
				if (prop instanceof OWLDataProperty) { 
					// cannot be!
					JOptionPane.showMessageDialog(this, "OWL Datatype Property cannot have a Class Range", "Range Definition Error", JOptionPane.ERROR_MESSAGE);
				}
				else {
					change = new AddObjectPropertyRange(dispOnt, (OWLObjectProperty) prop, cla2, null);
					changes.add(change);
				}
				break;
				
			case 5: // add inverse
				OWLObjectProperty propSubj = (OWLObjectProperty) swoopModel.getSelectedEntity();
				if (prop instanceof OWLDataProperty) {
					// cannot be!
					JOptionPane.showMessageDialog(this, "Inverse cannot be an OWL Datatype Property", "Inverse Definition Error", JOptionPane.ERROR_MESSAGE);
				}
				else {
					//******************************************************************
					//changed for Econnections
					//******************************************************************
					if(!propSubj.isLink()){
					 change = new AddInverse(dispOnt, propSubj, (OWLObjectProperty) prop, null);
					 changes.add(change);
					}
					else{
					   try{   
					    OntologyChange oc = 
				    		  new AddForeignEntity(dispOnt, prop, dispOnt.getURI() , null);
				    		  changes.add(oc);
					   }
					   catch (OWLException e) {}
					     
				    	 change = new AddInverse(dispOnt, propSubj, (OWLObjectProperty) prop, null);
						 changes.add(change);
					   
						 OntologyChange oc2 = 
				    		  new RemoveEntity(dispOnt, prop, null);
				    		  changes.add(oc2);
					  	
					     	
					}
					}
				//}
				break;
				
			case 6: // add property to instance
				
				// prompt user for adding value for property
				if(prop instanceof OWLObjectProperty){
					if(((OWLObjectProperty)prop).isLink()){
						URI u = ((OWLObjectProperty)prop).getLinkTarget();
						OWLOntology foreignOnto = swoopModel.getOntology(u);
						try {
							swoopModel.getReasoner().setOntology(foreignOnto);
							swoopModel.setSelectedOntology(foreignOnto);
						}
						catch (OWLException e) {
							e.printStackTrace();
						}
					}
				}
				PopupAddValue popup = new PopupAddValue(reasoner, swoopModel, prop);
				popup.setLocation(200, 200);
				popup.show();
				
				break;
		}
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
		
		if (e.getSource()==addPropBtn) {
			addPropertyChange();			
			dispose();
		}
		
		if (e.getSource()==applyBtn) {
			addPropertyChange();			
		}
		
		if (e.getSource()==cancelBtn) {
			dispose();
		}
		
	}

	/**
	 * Fill the property box with all the OWL properties (data/object)
	 * defined in the ontology selected by the user in the corresponding ont-combo box
	 */
	private void fillPropBox() {
		try {
			// fill props in PropBox based on selected ontology
			URI ontURI = (URI) ontList.getSelectedValue();
			OWLOntology currOnt = swoopModel.getOntology(ontURI);
			Set propSet = new TreeSet(EntityComparator.INSTANCE);
			if (showDataProp) propSet.addAll(currOnt.getDataProperties());
			if (showObjectProp) propSet.addAll(currOnt.getObjectProperties());
			propList.setFont(new Font(swoopModel.getFontFace(), Font.PLAIN, 11));
			propList.setListData(propSet.toArray());
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void valueChanged(ListSelectionEvent e) {
		if (e.getSource()==ontList) {
    		fillPropBox();
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
			if (e.getKeyCode()==10) {
				applyBtn.doClick();
			}
		}
	}

	public void keyReleased(KeyEvent arg0) {
	}
	
}
