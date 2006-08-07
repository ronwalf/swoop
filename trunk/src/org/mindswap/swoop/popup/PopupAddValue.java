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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.mindswap.swoop.SwoopModel;
import org.mindswap.swoop.reasoner.SwoopReasoner;
import org.mindswap.swoop.renderer.SwoopCellRenderer;
import org.mindswap.swoop.utils.DataValueChecker;
import org.mindswap.swoop.utils.SetUtils;
import org.mindswap.swoop.utils.ui.AddCloseBar;
import org.mindswap.swoop.utils.ui.EntityComparator;
import org.semanticweb.owl.impl.model.OWLConcreteDataTypeImpl;
import org.semanticweb.owl.impl.model.OWLDataEnumerationImpl;
import org.semanticweb.owl.io.vocabulary.OWLVocabularyAdapter;
import org.semanticweb.owl.io.vocabulary.RDFVocabularyAdapter;
import org.semanticweb.owl.io.vocabulary.XMLSchemaSimpleDatatypeVocabulary;
import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLDataProperty;
import org.semanticweb.owl.model.OWLDataRange;
import org.semanticweb.owl.model.OWLDataValue;
import org.semanticweb.owl.model.OWLDescription;
import org.semanticweb.owl.model.OWLEnumeration;
import org.semanticweb.owl.model.OWLException;
import org.semanticweb.owl.model.OWLIndividual;
import org.semanticweb.owl.model.OWLObjectProperty;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLProperty;
import org.semanticweb.owl.model.change.AddDataPropertyInstance;
import org.semanticweb.owl.model.change.AddIndividualClass;
import org.semanticweb.owl.model.change.AddObjectPropertyInstance;
import org.semanticweb.owl.model.change.RemoveEntity;
import org.semanticweb.owl.model.helper.OntologyHelper;


/**
 * @author Aditya
 * This class pops up whenever the user needs to specify an Property-Value pair
 * in a Ontology Change on an OWL Individual
 *
 */
public class PopupAddValue extends JFrame implements ActionListener, ListSelectionListener, KeyListener {
	
	SwoopReasoner reasoner;
	SwoopModel swoopModel;
	OWLProperty prop;
	public List changes;
	Font tahoma = new Font("Tahoma", Font.PLAIN, 11);
	JList rangeBox, valueBox;
	JTextField newValueFld;
	AddCloseBar actionBar;
	boolean isRDFXMLLiteral = false;
	
	public PopupAddValue(SwoopReasoner reasoner, SwoopModel swoopModel, OWLProperty prop) {		
		// setModal(true);
		this.reasoner = reasoner;
		this.swoopModel = swoopModel;
		this.prop = prop;
		this.changes = new ArrayList();
		setupUI();
	}

	private void setupUI() {
		
		JLabel propLbl = new JLabel("");
		List rangeList = new ArrayList();
		rangeBox = new JList();
		rangeBox.setFont(tahoma);
		rangeBox.setCellRenderer(new RangeRenderer(swoopModel));
		rangeBox.addKeyListener(this);
		
		Set valueList = new TreeSet(EntityComparator.INSTANCE);;
		valueBox = new JList();
		valueBox.setFont(tahoma);
		valueBox.setCellRenderer(new SwoopCellRenderer(swoopModel));
		valueBox.addKeyListener(this);
		
		boolean shrink = false;
		
		try {
			String propName = swoopModel.shortForm(prop.getURI());
			propLbl = new JLabel("Specify value for Property '"+propName+"'", JLabel.LEFT);
			propLbl.setFont(tahoma);
		
			// fill range box with property range's
			OWLOntology ontology = swoopModel.getSelectedOntology();
			Set ontologies;
			if (swoopModel.getImportsSetting(ontology)) {
				ontologies = OntologyHelper.importClosure(ontology);
			} else {
				ontologies = Collections.singleton(ontology);
			}
			
			Set rangeSet = prop.getRanges(ontologies);
			if (rangeSet.size()>0) {
				Iterator iter = rangeSet.iterator();
				while (iter.hasNext()) {
					if (prop instanceof OWLObjectProperty) {
						// range of ObjectProperty is a class
						Object obj = iter.next();
						if (obj instanceof OWLClass) {
							OWLClass desc = (OWLClass) obj;
							rangeList.add(desc);
							
							/* add owl:Thing regardless? */
							OWLClass thing = swoopModel.getSelectedOntology().getOWLDataFactory().getOWLClass( URI.create( OWLVocabularyAdapter.INSTANCE.getThing()));
							rangeList.add(thing);
							
							addRangeSubClasses(desc, rangeList);
						}
						else if (obj instanceof OWLDescription) {
							// one-of range of values
							if (obj instanceof OWLEnumeration) {
								OWLEnumeration enumElem = (OWLEnumeration) obj;
								Iterator indIter = enumElem.getIndividuals().iterator();
								Set indList = new TreeSet(EntityComparator.INSTANCE);
								while (indIter.hasNext()) {
									OWLIndividual ind = (OWLIndividual) indIter.next();
									indList.add(ind);
								}
								valueList.addAll(indList);
							}
						}
					}
					else {
						// range of DatatypeProperty is a 
						OWLDataRange desc = (OWLDataRange) iter.next();
						if (desc instanceof OWLConcreteDataTypeImpl) {
							OWLConcreteDataTypeImpl dt = (OWLConcreteDataTypeImpl) desc;
							rangeList.add(dt);
							shrink = true;
						}
						else if (desc instanceof OWLDataEnumerationImpl) {
							OWLDataEnumerationImpl dt = (OWLDataEnumerationImpl) desc;
							rangeList.add("OWL Data Range");
							Iterator deIter = dt.getValues().iterator();
							while (deIter.hasNext()) {
								String val = deIter.next().toString();
								String dType = val.substring(val.lastIndexOf("^")+1, val.length());
								// dType = "("+dType.substring(dType.indexOf("#")+1, dType.length())+")";//      ["+dType+"]";
								val = val.substring(0, val.indexOf("^"));
								valueList.add(val+" "+"("+dType+")");
							}
						}
					}					
				}
			}
			else {
				// ** no range specified **
				// default - String? for Datatype Properties
				// default - OWLThing for Object Properties
				if (prop instanceof OWLDataProperty) {
					URI xsdString = new URI("http://www.w3.org/2001/XMLSchema#string");
					OWLConcreteDataTypeImpl dt = (OWLConcreteDataTypeImpl) swoopModel.getSelectedOntology().getOWLDataFactory().getOWLConcreteDataType(xsdString);
					rangeList.add(dt);
					shrink = true;
				}
				else {
					OWLClass thing = swoopModel.getSelectedOntology().getOWLDataFactory().getOWLClass( URI.create( OWLVocabularyAdapter.INSTANCE.getThing()));
					rangeList.add(thing);
					addRangeSubClasses(thing, rangeList);
				}
			}
			
			rangeBox.setListData(rangeList.toArray());
			valueBox.setListData(valueList.toArray());
			
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	
		rangeBox.addListSelectionListener(this);
		rangeBox.setSelectedIndex(-1);
		
		if (rangeBox.getModel().getSize()>0) rangeBox.setSelectedIndex(0);		
		
		/*
		applyBtn = new JButton("Add");
		applyBtn.setFont(tahoma);
		applyBtn.addActionListener(this);
		addBtn = new JButton("Add & Close");
		addBtn.setFont(tahoma);
		addBtn.addActionListener(this);
		cancelBtn = new JButton("Cancel");
		cancelBtn.setFont(tahoma);
		cancelBtn.addActionListener(this);
		
		JPanel box = new JPanel();
		box.setLayout(new GridLayout(1,3));
		box.add(applyBtn);
		box.add(addBtn);
		box.add(cancelBtn);
		*/
		
		actionBar = new AddCloseBar();
		actionBar.addActionListener(this);
		
		
		Container content = getContentPane();
		content.setLayout(new BorderLayout());
	
		JPanel tab1W = new JPanel();
		tab1W.setLayout(new BorderLayout());
		JLabel propRangeLbl = new JLabel("Property Range");
		propRangeLbl.setFont(tahoma);
		tab1W.add(propRangeLbl, "North");
		tab1W.add(new JScrollPane(rangeBox), "Center");
		JPanel tab1E = new JPanel();
		tab1E.setLayout(new BorderLayout());
		String valText = "  Datatype Value:";
		if (prop instanceof OWLObjectProperty) valText = "        Existing Instance(s):";
		JLabel valueLbl = new JLabel(valText);
		tab1E.add(valueLbl, "North");
		tab1E.add(new JScrollPane(valueBox), "Center");
		JSplitPane tab1Split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		tab1Split.setOneTouchExpandable(true);
		tab1Split.setLeftComponent(tab1W);
		if (prop instanceof OWLObjectProperty) tab1Split.setRightComponent(tab1E);
		tab1Split.setDividerLocation(150);
		
		newValueFld = new JTextField();
		newValueFld.addKeyListener(this);
		JPanel newValuePanel = null;
		
		if (prop instanceof OWLObjectProperty) {
			newValuePanel = createWidget("OR Specify New Value/URI: ", newValueFld, null);
			tab1E.add(newValuePanel, "South");
		}
		else {
			if (rangeList.get(0).toString().indexOf("OWL Data Range")==-1) {
				newValuePanel = createWidget("Specify Data Value: ", newValueFld, null);
			}
			else {
				newValuePanel = createWidget("Specify Data Value: ", new JScrollPane(valueBox), null);
			}
			tab1W.add(newValuePanel, "South"); 
		}
			
		JPanel tab1I = new JPanel();
		tab1I.setLayout(new BorderLayout());		
		tab1I.add(tab1Split, "Center");
		
		content.add(tab1I, "Center");
		content.add(actionBar, "South");
		
		setTitle("Adding Property Value");
		if (shrink) setSize(445,130);
		else setSize(445, 300);
	}
	
	private void addRangeSubClasses(OWLClass range, List rangeList) {

		// add all subclasses of desc to range as well
		//***********************************************
		//Changed for Econnections
		// in order not to get an exception with link prop.
		//***********************************************
		try {
			if(!((OWLObjectProperty)prop).isLink()){
				Set allSubCla = SetUtils.union(reasoner.descendantClassesOf(range));
				Iterator allSubIter = allSubCla.iterator();
				OWLClass nothing = swoopModel.getSelectedOntology().getOWLDataFactory().getOWLClass( URI.create( OWLVocabularyAdapter.INSTANCE.getNothing()));
				while (allSubIter.hasNext()) {
					Object subDesObj = allSubIter.next();
					if (!(subDesObj instanceof OWLClass)) continue;
					OWLClass subCla = (OWLClass) subDesObj;							
					if (!subCla.equals(nothing) && !rangeList.contains(subCla)) rangeList.add(subCla);	
											
				}
			}
		}
		catch (OWLException ex) {
			ex.printStackTrace();
		}
	}
	
	private JPanel createWidget(Object title, JComponent comp, JComponent comp2) {
		JPanel widget = new JPanel();
		widget.setLayout(new BorderLayout());
		
		if (title!=null) {
			if (title instanceof String) {
				String titleStr = title.toString();
				JLabel lbl = new JLabel(titleStr);
				lbl.setFont(tahoma);
				widget.add(lbl, "West");
			}
			else widget.add((Component) title, "West");
		}
		widget.add(comp, "Center");
		if (comp2!=null) widget.add(comp2, "East");
		return widget;
	}
	
	/**
	 * Check for the validity of the property-value pair based on the range of the 
	 * property and the data value specified by the user
	 * @param dt - XSD DataType (or rdfs:Literal)
	 * @param value - string value specified by user
	 * @return
	 */
	private boolean checkValidValue(OWLConcreteDataTypeImpl dt, String value) {
		boolean valid = false;
		String xsd = XMLSchemaSimpleDatatypeVocabulary.XS;
		String errorMsg = "Invalid Value for Specified Datatype - require ";
		
		if (dt.getURI().toString().equals(RDFVocabularyAdapter.RDF+"XMLLiteral"))
			this.isRDFXMLLiteral = true;
		
		return DataValueChecker.isValidValue( this, dt, value);

	}
	
	private boolean addValueChange() {
		
		try {
			isRDFXMLLiteral = false;
			
			if (prop instanceof OWLDataProperty) {
				// create datevalue from user-typed text
				String val = newValueFld.getText();
				OWLDataValue dVal = null;
			
				if (!rangeBox.getModel().getElementAt(0).toString().equals("OWL Data Range")) {
					// range is not data value range
					OWLConcreteDataTypeImpl dt = (OWLConcreteDataTypeImpl) rangeBox.getSelectedValue();
					URI datatypeURI = dt.getURI(); 
					boolean valid = checkValidValue(dt, val);
			
					if (valid) {
												
						if (this.isRDFXMLLiteral) {
//							 //TODO AK: need to add parseType="Literal"
							 // currently escapeChars implemented below
						}
						
						// Evren: Do not use a language identifier becuase language identifiers can
						// only be used with plain literals (but here we might have a different
						// datatype) and also there is no need to assume that English is the default 
						// language. 
						dVal = swoopModel.getSelectedOntology().getOWLDataFactory().getOWLConcreteData(datatypeURI, null, val);
					}
					else {
						return false;
					}
				}
				else {
					val = valueBox.getSelectedValue().toString();
					String dType = val.substring(val.lastIndexOf("(")+1, val.length()-1);
					//val = val.substring(0, val.lastIndexOf("(")).trim();
					URI datatypeURI = new URI(dType); 
					// Evren: Do not use a language identifier becuase language identifiers can
					// only be used with plain literals (but here we might have a different
					// datatype) and also there is no need to assume that English is the default 
					// language. 					
					dVal = swoopModel.getSelectedOntology().getOWLDataFactory().getOWLConcreteData(datatypeURI, null, val);
				}
				
				// create data property change
				OWLIndividual dispInst = (OWLIndividual) swoopModel.getSelectedEntity();
				AddDataPropertyInstance change = new AddDataPropertyInstance(swoopModel.getSelectedOntology(), dispInst, (OWLDataProperty) prop, dVal, null);
				changes.add(change);
			}
			else {
				// prop is an object property
				Object[] individuals = null;
				
				if ((newValueFld.getText()!=null) && (!newValueFld.getText().trim().equals(""))) {
					// create new instance
					String indID = newValueFld.getText();
					// check for invalid chars in indID
					indID = indID.replaceAll(" ","_");
					if (indID.indexOf("#") < 0) {
						indID = "#"+indID;
					}
					URI indURI = null;
					if (!isURL(indID)) indURI =swoopModel.getSelectedOntology().getLogicalURI().resolve(indID);
					else indURI = new URI(indID);
					
					OWLIndividual ind = swoopModel.getSelectedOntology().getOWLDataFactory().getOWLIndividual(indURI);
					individuals = new Object[] {ind};
					OWLClass cla = (OWLClass) rangeBox.getSelectedValue();
					AddIndividualClass change = new AddIndividualClass(swoopModel.getSelectedOntology(), ind, cla, null);
					changes.add(change);
				} else {
					// get existing instance
					individuals = valueBox.getSelectedValues();
				}
				// create object property instance
				OWLIndividual dispInst = (OWLIndividual) swoopModel.getSelectedEntity();
				for (int i = 0; i < individuals.length; i++) {
					OWLIndividual ind = (OWLIndividual) individuals[i];
					AddObjectPropertyInstance change = new AddObjectPropertyInstance(swoopModel.getSelectedOntology(), dispInst, (OWLObjectProperty) prop, ind, null);
					changes.add(change);
					// **************************************************
					// Added for Econnections
					// ***************************************************
					if (prop instanceof OWLObjectProperty) {
						if (((OWLObjectProperty) prop).isLink()) {
							RemoveEntity oc2 = new RemoveEntity(swoopModel
									.getSelectedOntology(), ind, null);
							changes.add(oc2);
						}
					}
					// *********************************
				}
			}
			
			swoopModel.addUncommittedChanges(changes);
			changes = new ArrayList(); // reset it after changes have been added			
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return true;
	}
	
	public void actionPerformed(ActionEvent e) {
		
		if (e.getSource() == actionBar) {
			if (AddCloseBar.ADD.equals(e.getActionCommand())) {
				addValueChange();
			}
			if (AddCloseBar.ADDCLOSE.equals(e.getActionCommand())) {
				if (addValueChange()) {
					dispose();
				}
			}
			if (AddCloseBar.CLOSE.equals(e.getActionCommand())) {
				dispose();
			}
		}
		
		if (e.getSource()==rangeBox) {
			fillValueBox();
		}
		
	}
	
	private void fillValueBox() {
		Object selItem = rangeBox.getSelectedValue();
		Set valueList = new TreeSet(EntityComparator.INSTANCE);
		
		if (selItem instanceof OWLClass) {
			OWLClass cla = (OWLClass) selItem;
			// fill valueBox with existing instances of cla
			try {
				Set instSet = new HashSet();
				
				if (!cla.getURI().toString().equals(OWLVocabularyAdapter.INSTANCE.getThing())) {
					instSet = reasoner.instancesOf(cla);
					// also get any enumerations of the class
					Set enums = cla.getEnumerations(swoopModel.getSelectedOntology());
					for (Iterator it = enums.iterator(); it.hasNext();) {
						OWLEnumeration en = (OWLEnumeration) it.next();
						instSet.addAll(en.getIndividuals());
					}
				}
				else {
					// add all individuals in the ontology for owl:Thing
					instSet.addAll(swoopModel.getEntitySet(swoopModel.getSelectedOntology(), swoopModel.TRANSCLOSE_ONT, swoopModel.INDIVIDUALS));
				}
				
				Iterator iter = instSet.iterator();
				while (iter.hasNext()) {
					OWLIndividual ind = (OWLIndividual) iter.next();
					valueList.add(ind);
				}
				valueBox.setFont(new Font(swoopModel.getFontFace(), Font.PLAIN, 11));
				valueBox.setListData(valueList.toArray());
			}
			catch (OWLException ex) {
				ex.printStackTrace();
			}
		}
	}
	
	private boolean isURL(String str) {		
		try {
			URL url = new URL(str);
			return true;
		}
		catch (Exception ex) {  
			ex.printStackTrace();
		}
		return false;
	}
		
	class RangeRenderer extends JLabel implements ListCellRenderer {
		private SwoopModel swoopModel;

		RangeRenderer(SwoopModel swoopModel) {
			this.swoopModel = swoopModel;
		}

		public Component getListCellRendererComponent(
			JList list,
			Object value,
			int index,
			boolean isSelected,
			boolean cellHasFocus) {

			try {
				URI dispURI = null;
				if (value instanceof OWLClass) {
					OWLClass cla = (OWLClass) value;
					dispURI = cla.getURI();
				}
				else if (value instanceof OWLConcreteDataTypeImpl) {
					OWLConcreteDataTypeImpl dt = (OWLConcreteDataTypeImpl) value;
					dispURI = dt.getURI();
				}
				else if (value instanceof OWLIndividual) {
					OWLIndividual ind = (OWLIndividual) value;
					dispURI = ind.getURI();
				}
				
				if (dispURI!=null) setText(swoopModel.shortForm(dispURI));
				else setText(value.toString());
				
			} catch (OWLException e) {
				setText("Anonymous");
			}
			if (isSelected) {
				setBackground(list.getSelectionBackground());
				setForeground(list.getSelectionForeground());
			} else {
				setBackground(list.getBackground());
				setForeground(list.getForeground());
			}
			setEnabled(list.isEnabled());
			setFont(list.getFont());
			setOpaque(true);
			return this;
		}
	}

	public void valueChanged(ListSelectionEvent e) {

		if (e.getSource()==rangeBox) {
			fillValueBox();
		}		
	}

	public void keyTyped(KeyEvent arg0) {	
	}

	public void keyPressed(KeyEvent e) {
		if (e.getSource()==rangeBox || e.getSource()==valueBox) {
			String alpha = Character.toString(e.getKeyChar()).toLowerCase();
			PopupCommon.listSelector(swoopModel, (JList) e.getSource(), alpha);
		}
		
		if (e.getSource()==newValueFld) {
			if (e.getKeyCode()==10) {
				addValueChange();
			}
		}
		
	}

	public void keyReleased(KeyEvent arg0) {
	}
}
