package org.mindswap.swoop.popup;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.net.URI;
import java.net.URL;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.mindswap.swoop.ModelChangeEvent;
import org.mindswap.swoop.OntologyDisplay;
import org.mindswap.swoop.SwoopModel;
import org.mindswap.swoop.SwoopModelListener;
import org.mindswap.swoop.TermsDisplay;
import org.mindswap.swoop.renderer.SwoopCellRenderer;
import org.mindswap.swoop.utils.ui.EntityComparator;
import org.mindswap.swoop.utils.ui.SpringUtilities;
import org.semanticweb.owl.io.vocabulary.OWLVocabularyAdapter;
import org.semanticweb.owl.model.OWLAnnotationProperty;
import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLDataFactory;
import org.semanticweb.owl.model.OWLDataProperty;
import org.semanticweb.owl.model.OWLEntity;
import org.semanticweb.owl.model.OWLException;
import org.semanticweb.owl.model.OWLObjectProperty;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLProperty;
import org.semanticweb.owl.model.change.AddAnnotationInstance;
import org.semanticweb.owl.model.change.AddEntity;
import org.semanticweb.owl.model.change.ChangeVisitor;
import org.semanticweb.owl.model.helper.OWLBuilder;

/**
 * @author Aditya
 * This class pops up whenever the user needs to add a New Ontology, Class,
 * Property or Individual 
 */
public class PopupNew extends JFrame implements ActionListener, DocumentListener, KeyListener, SwoopModelListener {

	Font tahoma = new Font("Tahoma", Font.PLAIN, 11);
	String type;
	JButton addBtn, cancelBtn;
	JTextField idFld, uriFld, labelFld;
	JTextArea commentArea;
	JComboBox headerBox, propType, parentBox;
	String NEWLINE = System.getProperty("line.separator");
	OWLOntology ontology;
	OWLEntity newEntity;
	SwoopModel swoopModel;
	JPanel SwoopHandler;
	OWLEntity lastSelectedParent;
	private JButton addCloseBtn;
	
	public PopupNew(JPanel handler, SwoopModel swoopModel, String type, OWLOntology ontology) {
		
		this.SwoopHandler = handler;
		this.swoopModel = swoopModel;
		this.type = type;
		if (!type.equals("Ontology")) this.ontology = ontology;
		this.lastSelectedParent = swoopModel.getSelectedEntity();
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		//setModal(true);
		try {
			setupUI();			
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	private void setupUI() throws OWLException {
		
		JPanel mainPanel = new JPanel(new SpringLayout());
		
		JLabel headerLbl = new JLabel();
		headerBox = new JComboBox();
		headerBox.setFont(tahoma);
		
		headerLbl.setFont(tahoma);
		if (type.equals("Ontology")) {
			headerLbl.setText("Adding OWL Ontology");
			headerBox.addItem("Adding OWL Ontology");
		}
		else if (type.equals("Class")) {
			headerLbl.setText("Adding OWL Class");
			headerBox.addItem("Adding OWL Class");
			headerBox.addItem("Adding OWL Property");
			headerBox.addItem("Adding OWL Individual");
		}
		else if (type.equals("Property")) {
			headerLbl.setText("Adding OWL Property");
			headerBox.addItem("Adding OWL Property");
			headerBox.addItem("Adding OWL Class");
			headerBox.addItem("Adding OWL Individual");
		}
		else if (type.equals("Individual")) {
			headerLbl.setText("Adding OWL Individual");			
			headerBox.addItem("Adding OWL Individual");
			headerBox.addItem("Adding OWL Class");
			headerBox.addItem("Adding OWL Property");
		}
		headerBox.addActionListener(this);
		
		mainPanel.add(new JLabel(""));
		mainPanel.add(headerBox);
		
		JLabel propLbl = new JLabel("Property Type:");
		propLbl.setFont(tahoma);
		propType = new JComboBox();
		propType.setFont(tahoma);
		propType.addItem("OWL Datatype Property");
		propType.addItem("OWL Object Property");
		propType.addItem("OWL Annotation Property");
		
		//*****************************************
		//Added for Econn
		//*****************************************
		propType.addItem("OWL Link Property");
				
		//****************************************
		propType.addActionListener(this);
		
		propLbl.setLabelFor(propType);
		if (type.equals("Property")) {
			mainPanel.add(propLbl);
			mainPanel.add(propType);
			propType.setSelectedIndex(1); // DEFAULT PROPERTY TYPE
		}		
		
		// add parent box
		parentBox = new JComboBox();
		parentBox.setFont(tahoma);
		parentBox.setRenderer(new SwoopCellRenderer(swoopModel));
		JLabel parentLbl = new JLabel();
		parentLbl.setFont(tahoma);
		if (type.equals("Class")) parentLbl.setText("subClass-of");
		else if (type.equals("Property")) parentLbl.setText("subProperty-of");
		else parentLbl.setText("Instance-of");
		if (!type.equals("Ontology")) {
			mainPanel.add(parentLbl);
			mainPanel.add(parentBox);
		}
		fillParentBox();
		
		JLabel idLbl = new JLabel("ID:");
		if (type.equals("Ontology")) idLbl.setText("Version-info:");
		idLbl.setFont(tahoma);
		idFld = new JTextField();
		idFld.setFont(tahoma);
		idFld.getDocument().addDocumentListener(this);
		idFld.addKeyListener(this);
		idLbl.setLabelFor(idFld);
		mainPanel.add(idLbl);
		mainPanel.add(idFld);
		
		JLabel uriLbl = new JLabel("Logical URI:");
		uriLbl.setFont(tahoma);
		uriFld = new JTextField();
		uriFld.setFont(tahoma);
		uriFld.addKeyListener(this);
		if (!type.equals("Ontology")) uriFld.setText(ontology.getLogicalURI().toString()+"#");
		else uriFld.setText("");
		uriLbl.setLabelFor(uriFld);
		mainPanel.add(uriLbl);
		mainPanel.add(uriFld);
		
		JLabel lbl = new JLabel("Label:");
		lbl.setFont(tahoma);
		labelFld = new JTextField("");
		labelFld.setFont(tahoma);
		labelFld.addKeyListener(this);
		lbl.setLabelFor(labelFld);
		mainPanel.add(lbl);
		mainPanel.add(labelFld);
		
		JLabel comm = new JLabel("Comment:");
		comm.setFont(tahoma);
		commentArea = new JTextArea();
		commentArea.setFont(tahoma);
		commentArea.setText(NEWLINE+NEWLINE+NEWLINE);
		commentArea.setCaretPosition(0);
		commentArea.addKeyListener(this);
		JScrollPane commentPane = new JScrollPane(commentArea);
		mainPanel.add(comm);
		comm.setLabelFor(commentPane);
		mainPanel.add(commentPane);		
		
		addBtn = new JButton("Add");
		addBtn.setFont(tahoma);
		addBtn.setEnabled(false);
		addBtn.addActionListener(this);
		addCloseBtn = new JButton("Add & Close");
		addCloseBtn.setFont(tahoma);
		addCloseBtn.setEnabled(false);
		addCloseBtn.addActionListener(this);
		cancelBtn = new JButton("Cancel");
		cancelBtn.setFont(tahoma);
		cancelBtn.addActionListener(this);
		JPanel btnPanel = new JPanel();
		btnPanel.setLayout(new GridLayout(1,2));
		btnPanel.add(addBtn);
		btnPanel.add(addCloseBtn);
		btnPanel.add(cancelBtn);
		mainPanel.add(new JLabel(""));
		mainPanel.add(btnPanel);
		
		int rows = 6;
		if (type.equals("Class") || type.equals("Individual")) rows = 7;
		if (type.equals("Property")) rows = 8;
		
		SpringUtilities.makeCompactGrid(mainPanel,
                rows, 2, //rows, cols
                6, 6,        //initX, initY
                6, 6);       //xPad, yPad
		
		Container content = getContentPane();
		content.setLayout(new BorderLayout());
		content.add(mainPanel, "Center");
		
		if (type.equals("Ontology")) {
			setSize(350, 230);
			setResizable(false);
			setTitle("New OWL Ontology");
			headerBox.setVisible(false);
		}
		else {
			if (type.equals("Class")) {
				setSize(350, 260);
			}
			else if (type.equals("Property")){
				setSize(350, 290);
			}
			else if (type.equals("Individual")){
				setSize(350, 265);
			}
			setResizable(true);
			setTitle("New Entity");
			headerBox.setVisible(true);			
		}
		
	}
	
	private JPanel createRowPanel(String lblStr, Component comp) {
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		JLabel lbl = new JLabel(lblStr);
		lbl.setFont(tahoma);
		panel.add(lbl, "West");
		panel.add(comp, "Center");
		return panel;
	}
	
	public void actionPerformed(ActionEvent e) {
		
		if (e.getSource()==parentBox && e.getActionCommand().equals("comboBoxChanged")) {
			
			if (parentBox.getSelectedItem() instanceof OWLDataProperty) {
				propType.setSelectedIndex(0);				
			}
			else if (parentBox.getSelectedItem() instanceof OWLObjectProperty) {
				//******************************************
				//Changed for Econnections
				//*********************************************
				if(((OWLObjectProperty)parentBox.getSelectedItem()).isLink())
					propType.setSelectedIndex(3);
				else
					propType.setSelectedIndex(1);
				//************************************************				
			}			
		}
		
		if (e.getSource()==propType && e.getActionCommand().equals("comboBoxChanged")) {
			if (propType.getSelectedIndex()==2) {
				// annotation property selected, disable parentBox
				if (parentBox!=null) parentBox.setEnabled(false);
			}
			else if (parentBox!=null) parentBox.setEnabled(true);
		}
		
		if (e.getSource()==headerBox && e.getActionCommand().equals("comboBoxChanged")) {
			// if header box selection is not same as current type
			if ((!type.equals("Ontology")) && (headerBox.getSelectedItem().toString().indexOf(type)==-1)) {
				// switch type 
				String newType = headerBox.getSelectedItem().toString();
				if (newType.indexOf("Class")>=0) {
					type = "Class"; 
					((TermsDisplay) SwoopHandler).termTabPane.setSelectedIndex(0);
				}
				else if (newType.indexOf("Property")>=0){
					type = "Property";
					((TermsDisplay) SwoopHandler).termTabPane.setSelectedIndex(1);
				}
				else if (newType.indexOf("Individual")>=0){
					type = "Individual";
					((TermsDisplay) SwoopHandler).termTabPane.setSelectedIndex(2);
				}
				
				this.redrawUI();
			}
		}
		
		if (e.getSource()==addBtn || e.getSource() == addCloseBtn) {
			try {
				
				String uriStr = uriFld.getText();
				
				// check for invalid uri
				if (uriStr.trim().equals("") || !(isURI(uriStr))) {
					JOptionPane.showMessageDialog(null, "A Valid Logical URI needs to be specified for the new OWL Ontology", "Creation Error!", JOptionPane.ERROR_MESSAGE);
					return;
				}
				URI uri = new URI(uriStr);
				OWLDataFactory df = null;
				
				if (type.equals("Ontology")) {
					// new ontology
					OWLBuilder builder = new OWLBuilder();
					
					// check for existing uri
					if (swoopModel.getOntologyURIs().contains(uri)) {
						JOptionPane.showMessageDialog(null, "An Ontology with that Logical URI already exists in SWOOP", "Creation Error!", JOptionPane.ERROR_MESSAGE);
						return;
					}
					// if no errors, proceed to building new ontology
					builder.createOntology(uri, uri);
					ontology = builder.getOntology();
					df = ontology.getOWLDataFactory();
					addAnnotations(df);
					
					/* also add owl:Thing to the ontology */
					// otherwise thing appears as an imported class in the tree?!
					OWLClass thing = df.getOWLThing();
					AddEntity ae = new AddEntity(ontology, thing, null);
					ae.accept((ChangeVisitor) ontology);
					
					swoopModel.addOntology(ontology);
					((OntologyDisplay) SwoopHandler).swoopHandler.enableMenuOptions();
					swoopModel.setSelectedOntology(ontology);					
				}					
				else {
					if (type.equals("Class")) {
					
						// new class
						df = ontology.getOWLDataFactory();
						newEntity = df.getOWLClass(uri);					
					}
					else if (type.equals("Property")) {
						df = ontology.getOWLDataFactory();
						if (propType.getSelectedIndex()==0) {
							// new datatype property
							newEntity = df.getOWLDataProperty(uri);
						}
						else if (propType.getSelectedIndex()==2) {
							// new annotation property
							newEntity = df.getOWLAnnotationProperty(uri);
						}
						else {
							// new object property
							newEntity = df.getOWLObjectProperty(uri);
							//**************************************
							//Added for Econnections
							//***************************************
							if (propType.getSelectedIndex()==2) {
								PopupAddForeignOntology popup = new PopupAddForeignOntology(swoopModel);
								popup.setLocation(200, 200);
								popup.show();
								
								((OWLObjectProperty)newEntity).setLinkTarget(popup.ontologyURI);
								if (!(swoopModel.getOntologiesMap().containsKey(popup.ontologyURI)))
								   swoopModel.addOntology(popup.ontologyURI);
							}
							//*************************************
							
						}				
					}
					else if (type.equals("Individual")) {
						// new individual
						df = ontology.getOWLDataFactory();
						newEntity = df.getOWLIndividual(uri);					
					}
					
					// add rdfs:label and comment if any
					addAnnotations(df);
					
					createEntity();
				}
				
				addBtn.setEnabled(false);
				addCloseBtn.setEnabled(false);
				if (e.getSource() == addCloseBtn) {
					dispose();
				}
				idFld.setText("");
				labelFld.setText("");
				commentArea.setText("");
			}
			catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		
		if (e.getSource()==cancelBtn) {
			dispose();
		}
		
	}
	
	private void redrawUI() {
		hide();
		getContentPane().removeAll();
		getContentPane().repaint();			
		try {
			setupUI();
			show();
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	private boolean isURI(String str) {		
		try {
			URI url = new URI(str);
			return true;
		}
		catch (Exception ex) {}
		return false;
	}
	
	private void createEntity() {
		if (newEntity!=null) {
			OWLEntity parent = null;
			if (parentBox.getSelectedIndex()!=0) parent = (OWLEntity) parentBox.getSelectedItem();
			this.lastSelectedParent = parent;
			swoopModel.addEntity(ontology, newEntity, parent);
		}
	}

	private void addAnnotations(OWLDataFactory df) {
		
		try {
			String lbl = "", comment = "", version = "";
			if (labelFld.getText()!=null) lbl = labelFld.getText().trim();
			if (commentArea.getText()!=null) comment = commentArea.getText().trim();
			if (idFld.getText()!=null) version = idFld.getText().trim();
			
			// get annotation properties
			URI lblURI = new URI("http://www.w3.org/2000/01/rdf-schema#label");
			OWLAnnotationProperty lblProp = df.getOWLAnnotationProperty(lblURI);			
			URI commentURI = new URI("http://www.w3.org/2000/01/rdf-schema#comment");
			OWLAnnotationProperty commentProp = df.getOWLAnnotationProperty(commentURI);			
			URI versionInfoURI = new URI("http://www.w3.org/2002/07/owl#versionInfo");
			OWLAnnotationProperty versionInfoProp = df.getOWLAnnotationProperty(versionInfoURI);			
			
			if (type.equals("Ontology")) {
				
				AddAnnotationInstance annot = null;
				
				// add version info
				if (version.length()>0) {
					annot = new AddAnnotationInstance(ontology, ontology, versionInfoProp, version, null);
					annot.accept((ChangeVisitor) ontology);
				}
				
				// add label
				if(lbl.length() > 0) {
					annot = new AddAnnotationInstance(ontology, ontology, lblProp, lbl, null);
					annot.accept((ChangeVisitor) ontology);
				}
				
				// add comment
				if(comment.length() > 0) {
					annot = new AddAnnotationInstance(ontology, ontology, commentProp, comment, null);
					annot.accept((ChangeVisitor) ontology);
				}
			}
			else {
				AddAnnotationInstance annot;
				
				// add label
				if(lbl.length() > 0) {				
					annot = new AddAnnotationInstance(ontology, newEntity, lblProp, lbl, null);
					annot.accept((ChangeVisitor) ontology);
				}
				
				// add comment
				if(comment.length() > 0) {				
					annot = new AddAnnotationInstance(ontology, newEntity, commentProp, comment, null);
					annot.accept((ChangeVisitor) ontology);
				}
			}
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	public void changedUpdate(DocumentEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	private void updateURI() {
		
		if ((!type.equals(("Ontology"))) && (idFld.getText()!=null)) {
			String uri = "";
			if (uriFld.getText()!=null) uri = uriFld.getText();
			if (uri.indexOf("#")>=0) uri = uri.substring(0, uri.indexOf("#"));
			else if (uri.indexOf("/")>=0) uri = uri.substring(0, uri.lastIndexOf("/"));
			else uri = "";
			uri+="#"+idFld.getText();
			uriFld.setText(uri);
		}
	}
	
	public void insertUpdate(DocumentEvent arg0) {
		updateURI();
	}

	public void removeUpdate(DocumentEvent arg0) {
		updateURI();
	}

	public void keyPressed(KeyEvent e) {
		
		if ((!type.equals("Ontology")) && 
				((e.getSource()==idFld)) || (e.getSource()==uriFld)) {
			addBtn.setEnabled(true);		
			addCloseBtn.setEnabled(true);		
		}
		if ((type.equals("Ontology")) && (e.getSource()==uriFld)) {
			addBtn.setEnabled(true);
			addBtn.setEnabled(true);		
		}
		
		if (e.getKeyCode()==10) {
			// enter key pressed
			if (addBtn.isEnabled()) {
				addBtn.doClick();
				if (type.equals("Ontology")) uriFld.requestFocus();
				else idFld.requestFocus();				
			}			
		}
	}

	public void keyReleased(KeyEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	public void keyTyped(KeyEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	public void fillParentBox() {
		
		try {
			
			parentBox.setFont(new Font(swoopModel.getFontFace(), Font.PLAIN, 11));
			parentBox.removeActionListener(this);
			parentBox.removeAllItems();
			
			// fill values in parentBox
			if (type.equals("Class") || (type.equals("Individual"))) {
				OWLClass thing = swoopModel.getSelectedOntology().getOWLDataFactory().getOWLClass( URI.create( OWLVocabularyAdapter.INSTANCE.getThing()));
				parentBox.addItem(thing);
				Set claSet = new TreeSet(EntityComparator.INSTANCE);
				claSet.addAll(swoopModel.getReasoner().getClasses());
				Iterator claSetIter = claSet.iterator();
				while (claSetIter.hasNext()) {
					OWLClass cla = (OWLClass) claSetIter.next();
					if (!cla.getURI().equals(thing.getURI()))
							parentBox.addItem(cla);
				}				
			}
			else if (type.equals("Property")) {
				parentBox.addItem("None");
				Set propSet = new TreeSet(EntityComparator.INSTANCE);
				propSet.addAll(swoopModel.getReasoner().getDataProperties());
				propSet.addAll(swoopModel.getReasoner().getObjectProperties());
				Iterator propSetIter = propSet.iterator();
				while (propSetIter.hasNext()) {
					OWLProperty prop = (OWLProperty) propSetIter.next();
					parentBox.addItem(prop);
				}							
			}
		}
		catch (OWLException e) {
			e.printStackTrace();
		}
		finally {
			try {
				if (!type.equals("Ontology")) {
					OWLClass thing = null;
					if (swoopModel.getSelectedOntology()!=null) thing = swoopModel.getSelectedOntology().getOWLDataFactory().getOWLClass( URI.create( OWLVocabularyAdapter.INSTANCE.getThing()));
					parentBox.addActionListener(this);
					if (this.lastSelectedParent!=null) {
						if (!this.lastSelectedParent.equals(thing)) parentBox.setSelectedItem(this.lastSelectedParent);
						else parentBox.setSelectedIndex(0);
					}
				}
			} 
			catch (OWLException e1) {
				e1.printStackTrace();
			}			
		}
	}

	public void modelChanged(ModelChangeEvent event) {
		
		if (event.getType()==ModelChangeEvent.ADDED_ENTITY) {
			this.redrawUI();
		}
		
	}

	public void dispose() {
		super.dispose();
		swoopModel.removeListener(this);
	}
	

}
