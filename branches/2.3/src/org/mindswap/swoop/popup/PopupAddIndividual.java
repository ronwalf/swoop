package org.mindswap.swoop.popup;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.InputVerifier;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
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
import org.mindswap.swoop.utils.change.EnumElementChange;
import org.mindswap.swoop.utils.ui.EntityComparator;
import org.mindswap.swoop.utils.ui.OntologyComparator;
import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLDataProperty;
import org.semanticweb.owl.model.OWLDescription;
import org.semanticweb.owl.model.OWLDifferentIndividualsAxiom;
import org.semanticweb.owl.model.OWLEntity;
import org.semanticweb.owl.model.OWLEquivalentPropertiesAxiom;
import org.semanticweb.owl.model.OWLException;
import org.semanticweb.owl.model.OWLIndividual;
import org.semanticweb.owl.model.OWLObjectProperty;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLProperty;
import org.semanticweb.owl.model.OWLSameIndividualsAxiom;
import org.semanticweb.owl.model.change.AddDomain;
import org.semanticweb.owl.model.change.AddEntity;
import org.semanticweb.owl.model.change.AddForeignEntity;
import org.semanticweb.owl.model.change.AddIndividualAxiom;
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
 * This class pops up whenever the user needs to specify an OWL Individual
 * in an Ontology Change (For example: adding a sameAs or differentFrom axiom)
 */
public class PopupAddIndividual extends JFrame implements ActionListener, ListSelectionListener, KeyListener {
	
	SwoopReasoner reasoner;
	SwoopModel swoopModel;
	public List changes;
	String type;
	int typeIntCode;
	Font tahoma = new Font("Tahoma", Font.PLAIN, 11);
	Font tahomaB = new Font("Tahoma", Font.BOLD, 10);	
	JButton applyBtn, addIndBtn, cancelBtn;
	JList ontList, indList;
	JTextField newIndFld;	
	
	
	public PopupAddIndividual(SwoopReasoner reas,  String type, SwoopModel swoopModel) {
		// setModal(true);
		this.reasoner = reas;
		this.type = getFullType(type);
		this.swoopModel = swoopModel;
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
		if (type.equals("SAM")) { typeIntCode = 0; return "SAME-AS INDIVIDUAL"; }
		if (type.equals("DIF")) { typeIntCode = 1; return "DIFFERENT-FROM INDIVIDUAL"; }
		if (type.equals("ONE")) { typeIntCode = 2; return "ONE-OF"; }
		if (type.equals("INS")) { typeIntCode = 3; return "NEW INDIVIDUAL"; }
		return "";
	}
	
	private void setupUI() {
		
		JPanel tab1 = new JPanel();
		tab1.setLayout(new BorderLayout());
		ontList = new JList();
		ontList.setFont(tahoma);
		ontList.setCellRenderer(new CellRenderer());
		indList = new JList();
		indList.setFont(tahoma);
		indList.setCellRenderer(new SwoopCellRenderer(swoopModel));
		indList.addKeyListener(this);
		applyBtn = new JButton("Add");
		applyBtn.setFont(tahoma);
		applyBtn.addActionListener(this);
		addIndBtn = new JButton("Add & Close");
		addIndBtn.setFont(tahoma);
		addIndBtn.addActionListener(this);
		JLabel ontLbl = new JLabel("Select Ontology:");
		ontLbl.setFont(tahoma);
		JLabel propLbl = new JLabel("Select Individual:");
		propLbl.setFont(tahoma);
		
		JPanel tab1W = new JPanel();
		tab1W.setLayout(new BorderLayout());
		tab1W.add(ontLbl, "North");
		tab1W.add(new JScrollPane(ontList), "Center");
		JPanel tab1E = new JPanel();
		tab1E.setLayout(new BorderLayout());
		tab1E.add(propLbl, "North");
		tab1E.add(new JScrollPane(indList), "Center");
		JSplitPane tab1Split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		tab1Split.setOneTouchExpandable(true);
		tab1Split.setLeftComponent(tab1W);
		tab1Split.setRightComponent(tab1E);
		
		JPanel btnPanel = new JPanel();
		btnPanel.setLayout(new GridLayout(1,3));
		btnPanel.add(applyBtn);
		btnPanel.add(addIndBtn);
		cancelBtn = new JButton("Cancel");
		cancelBtn.setFont(tahoma);
		cancelBtn.addActionListener(this);
		btnPanel.add(cancelBtn);
		
		newIndFld = new JTextField();
		newIndFld.setFont(tahoma);
		newIndFld.addKeyListener(this);
		JPanel newPanel = new JPanel();
		newPanel.setLayout(new BorderLayout());
		JLabel newLbl = new JLabel("OR Specify New Term/URI: ");
		newLbl.setFont(tahoma);
		JPanel newLblPanel = new JPanel();
		newLblPanel.setLayout(new BorderLayout());
		newLblPanel.add(newLbl, "West");
		newPanel.add(newLblPanel, "West");
		newPanel.add(newIndFld, "Center");
		
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
		setTitle("Specify Individual");
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
	 * Method called when the user presses the add/apply individual change btn
	 * It gets the individual selected and passes it to makeChange(..) 
	 */
	private boolean addIndividualChange() {
		
		try {
			OWLIndividual ind = null;
			
			if ((newIndFld.getText()!=null) && (!newIndFld.getText().trim().equals(""))) {
				// user-specified property name/uri
				String indStr = newIndFld.getText();
				indStr = indStr.replaceAll(" ","_");
				
				URI ontURI = swoopModel.getSelectedOntology().getLogicalURI();
				URI indURI = null;
				if (isFragmentable(indStr)) {
					String scheme = ontURI.getScheme();
					String ssp = ontURI.getSchemeSpecificPart();
					indURI = new URI(scheme, ssp, indStr);
				} else {
					indURI = ontURI.resolve(new URI(indStr));
				}
				
				// **create new individual
				ind = swoopModel.getSelectedOntology().getOWLDataFactory().getOWLIndividual(indURI);
				makeChange(ind);
			}
			else {
				// get existing individual
				Object[] inds = (Object[]) indList.getSelectedValues();
				for (int i=0; i<inds.length; i++) {
					ind = (OWLIndividual) inds[i];
					makeChange(ind);
				}
			}
			
			swoopModel.addUncommittedChanges(changes);
			changes = new ArrayList(); // reset it after changes have been added
		}
		catch (URISyntaxException ex) {
			JOptionPane.showMessageDialog(this, "Invalid URI!:\n"+ex, "Warning",
					JOptionPane.ERROR_MESSAGE);
			ex.printStackTrace();
			return false;
		}
		catch (OWLException ex) {
			JOptionPane.showMessageDialog(this, "Could not create OWL Individual:\n"+ex, "Warning",
					JOptionPane.ERROR_MESSAGE);
			ex.printStackTrace();
			return false;
		}
		
		return true;
	}
	
	/**
	 * Creates the Individual related Ontology Change based on the 
	 * currently selected ontology, entity and the OWL Individual passed to it
	 * @param ind - object (OWL Property) of the ontology change
	 */
	private void makeChange(OWLIndividual ind) {
		OntologyChange change = null;
		OWLOntology dispOnt = swoopModel.getSelectedOntology();
		
		switch (typeIntCode) {
			case 0: // add sameAs				
				Set sameSet = new HashSet();
				sameSet.add((OWLIndividual) swoopModel.getSelectedEntity());
				sameSet.add(ind);
				try {
					OWLSameIndividualsAxiom indAxiom = dispOnt.getOWLDataFactory().getOWLSameIndividualsAxiom(sameSet);
					change = new AddIndividualAxiom(dispOnt, indAxiom, null);
					changes.add(change);
				}
				catch (OWLException e) {
					e.printStackTrace();
				}
				
				break;
		
			case 1: // add differentFrom				
				Set diffSet = new HashSet();
				diffSet.add((OWLIndividual) swoopModel.getSelectedEntity());
				diffSet.add(ind);
				try {
					OWLDifferentIndividualsAxiom indAxiom = dispOnt.getOWLDataFactory().getOWLDifferentIndividualsAxiom(diffSet);
					change = new AddIndividualAxiom(dispOnt, indAxiom, null);
					changes.add(change);
				}
				catch (OWLException e) {
					e.printStackTrace();
				}
				
				break;
				
			case 2: // add owl:oneOf
				change = new EnumElementChange("Add", dispOnt, (OWLClass) swoopModel.getSelectedEntity(), ind, null);
				changes.add(change);
				
				break;
			
			case 3: // add new individual
				change = new AddIndividualClass(dispOnt, ind, (OWLClass) swoopModel.getSelectedEntity(), null);
				changes.add(change);
				break;
		}
	}
	
	/**
	 * Returns true if the string can be used as the fragment of a URI (post #).
	 * @param str
	 * @return
	 */
	
	private boolean isFragmentable(String str) {
		
		if ((str.indexOf('#') >= 0) || (str.indexOf('/') >= 0)) {
			// String contains path characters
			return false;
		}
		try {
			new URL(str);
			// Valid URL
			return false;
		} catch (MalformedURLException ex) {}
		
		return true;
	}
	
	public void actionPerformed(ActionEvent e) {
		
		if (e.getSource()==addIndBtn) {
			if (addIndividualChange()) {		
				dispose();
			}
		}
		
		if (e.getSource()==applyBtn) {
			addIndividualChange();			
		}
		
		if (e.getSource()==cancelBtn) {
			dispose();
		}
		
	}

	/**
	 * Fill the individual box with all the OWL Individuals
	 * defined in the ontology selected by the user in the corresponding ont-combo box
	 */
	private void fillIndBox() {
		try {
			// fill individuals in IndBox based on selected ontology
			URI ontURI = (URI) ontList.getSelectedValue();
			OWLOntology currOnt = swoopModel.getOntology(ontURI);
			Set indSet = new TreeSet(EntityComparator.INSTANCE);
			indSet.addAll(currOnt.getIndividuals());
			indList.setFont(new Font(swoopModel.getFontFace(), Font.PLAIN, 11));
			indList.setListData(indSet.toArray());
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void valueChanged(ListSelectionEvent e) {
		if (e.getSource()==ontList) {
    		fillIndBox();
    	}
	}

	public void keyTyped(KeyEvent arg0) {
		}

	public void keyPressed(KeyEvent e) {
		if (e.getSource()==indList) {
			String alpha = Character.toString(e.getKeyChar()).toLowerCase();
			PopupCommon.listSelector(swoopModel, (JList) e.getSource(), alpha);
		}
		
		if (e.getSource()==newIndFld) {
			if (e.getKeyCode()==10) {
				applyBtn.doClick();
			}
		}
	}

	public void keyReleased(KeyEvent arg0) {
	}
	
}
