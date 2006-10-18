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

package org.mindswap.swoop.change;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
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
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.table.TableColumn;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import org.mindswap.swoop.ModelChangeEvent;
import org.mindswap.swoop.SwoopFrame;
import org.mindswap.swoop.SwoopModel;
import org.mindswap.swoop.SwoopModelListener;
import org.mindswap.swoop.renderer.entity.ConciseFormatEntityRenderer;
import org.mindswap.swoop.renderer.entity.RDFXMLEntityRenderer;
import org.mindswap.swoop.treetable.JTreeTable;
import org.mindswap.swoop.utils.change.BooleanElementChange;
import org.mindswap.swoop.utils.change.EnumElementChange;
import org.mindswap.swoop.utils.change.RevertCheckpointChange;
import org.mindswap.swoop.utils.change.SaveCheckpointChange;
import org.mindswap.swoop.utils.owlapi.CorrectedRDFRenderer;
import org.mindswap.swoop.utils.rdfapi.EConnTripleVisitor;
import org.mindswap.swoop.utils.rdfapi.PrettyXMLWriter;
import org.mindswap.swoop.utils.rdfapi.TripleVisitor;
import org.mindswap.swoop.utils.ui.ChangeComparator;
import org.semanticweb.owl.io.RendererException;
import org.semanticweb.owl.model.OWLAnd;
import org.semanticweb.owl.model.OWLAnnotationProperty;
import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLClassAxiom;
import org.semanticweb.owl.model.OWLDataProperty;
import org.semanticweb.owl.model.OWLDataRange;
import org.semanticweb.owl.model.OWLDataValue;
import org.semanticweb.owl.model.OWLDescription;
import org.semanticweb.owl.model.OWLDifferentIndividualsAxiom;
import org.semanticweb.owl.model.OWLDisjointClassesAxiom;
import org.semanticweb.owl.model.OWLEntity;
import org.semanticweb.owl.model.OWLException;
import org.semanticweb.owl.model.OWLIndividual;
import org.semanticweb.owl.model.OWLIndividualAxiom;
import org.semanticweb.owl.model.OWLNamedObject;
import org.semanticweb.owl.model.OWLObject;
import org.semanticweb.owl.model.OWLObjectProperty;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLOr;
import org.semanticweb.owl.model.OWLProperty;
import org.semanticweb.owl.model.OWLSameIndividualsAxiom;
import org.semanticweb.owl.model.OWLSubClassAxiom;
import org.semanticweb.owl.model.change.AddAnnotationInstance;
import org.semanticweb.owl.model.change.AddClassAxiom;
import org.semanticweb.owl.model.change.AddDataPropertyInstance;
import org.semanticweb.owl.model.change.AddDataPropertyRange;
import org.semanticweb.owl.model.change.AddDomain;
import org.semanticweb.owl.model.change.AddEntity;
import org.semanticweb.owl.model.change.AddEquivalentClass;
import org.semanticweb.owl.model.change.AddImport;
import org.semanticweb.owl.model.change.AddIndividualAxiom;
import org.semanticweb.owl.model.change.AddIndividualClass;
import org.semanticweb.owl.model.change.AddInverse;
import org.semanticweb.owl.model.change.AddObjectPropertyInstance;
import org.semanticweb.owl.model.change.AddObjectPropertyRange;
import org.semanticweb.owl.model.change.AddSuperClass;
import org.semanticweb.owl.model.change.AddSuperProperty;
import org.semanticweb.owl.model.change.ChangeVisitor;
import org.semanticweb.owl.model.change.OntologyChange;
import org.semanticweb.owl.model.change.RemoveAnnotationInstance;
import org.semanticweb.owl.model.change.RemoveClassAxiom;
import org.semanticweb.owl.model.change.RemoveDataPropertyInstance;
import org.semanticweb.owl.model.change.RemoveDataPropertyRange;
import org.semanticweb.owl.model.change.RemoveDomain;
import org.semanticweb.owl.model.change.RemoveEntity;
import org.semanticweb.owl.model.change.RemoveEquivalentClass;
import org.semanticweb.owl.model.change.RemoveImport;
import org.semanticweb.owl.model.change.RemoveIndividualAxiom;
import org.semanticweb.owl.model.change.RemoveIndividualClass;
import org.semanticweb.owl.model.change.RemoveInverse;
import org.semanticweb.owl.model.change.RemoveObjectPropertyInstance;
import org.semanticweb.owl.model.change.RemoveObjectPropertyRange;
import org.semanticweb.owl.model.change.RemoveSuperClass;
import org.semanticweb.owl.model.change.RemoveSuperProperty;
import org.semanticweb.owl.model.change.SetFunctional;
import org.semanticweb.owl.model.change.SetInverseFunctional;
import org.semanticweb.owl.model.change.SetSymmetric;
import org.semanticweb.owl.model.change.SetTransitive;
import org.xngr.browser.editor.XmlEditorPane;

/**
 * @author Aditya Kalyanpur
 * A file used to display Ontology Change information (i.e. change logs),
 * obtain an inverse of an Ontology Change (used for undoing committed changes)
 * and help serialize an Ontology Change in RDFXML (for sharing using Annotea)
 */
public class ChangeLog extends JPanel implements ActionListener, HyperlinkListener, SwoopModelListener, TreeSelectionListener {
	
	JEditorPane logPane;
	public SwoopModel swoopModel;
	public SwoopFrame swoopHandler;
	JCheckBoxMenuItem stayTopChk;
	JMenu saveOntMenu, saveChSetMenu, exportMenu;
	JMenuItem openMItem;
	Font tahoma = new Font("Tahoma", Font.PLAIN, 10);
	Font tahomaL = new Font("Tahoma", Font.PLAIN, 11);
	JRadioButton entityRadio, ontRadio, wkspcRadio;	
	JPanel topPanel, bottomPanel;
	JScrollPane threadTableScroll, htmlPanel;
	JCheckBox showChanges, showChkPts;
	JTreeTable changeTreeTable;
	private Map localDirty;
	JButton undoBtn, serBtn, attachBtn, deleteBtn, saveBtn;
    
	final String[] cNames  = {"Author", "Description", "Date", "Entity"};
	public int scope = 0;
	public static int ENTITY_SCOPE = 1;
	public static int ONTOLOGY_SCOPE = 2;
	public static int WORKSPACE_SCOPE = 3;
	public final static int CHANGE_DESCRIPTION = 4;
	public final static int CHANGE_RDFXML = 5;
	public final static int CHANGE_INVERSE = 6;
	public final static int CHANGE_ALIGN = 7;
	
	public ChangeLog(SwoopFrame handler, SwoopModel model) {

		this.swoopHandler = handler;
		this.swoopModel = model;
		this.localDirty = new HashMap();
		setupUI();	
	}
	
	private void setupUI() {
		
		// create radio buttons
		entityRadio = new JRadioButton("Entity");
		entityRadio.setFont(tahomaL);
		ontRadio = new JRadioButton("Ontology");
		ontRadio.setFont(tahomaL);
		wkspcRadio = new JRadioButton("Workspace");
		wkspcRadio.setFont(tahomaL);
		
		// add to button group
		ButtonGroup group = new ButtonGroup();
		group.add(entityRadio);
		group.add(ontRadio);
		group.add(wkspcRadio);
		JLabel radioLbl = new JLabel("Scope:");
		radioLbl.setFont(tahomaL);
		JPanel radioPanel = new JPanel();
		//radioPanel.setLayout(new GridLayout(1,4));
		radioPanel.add(radioLbl);
		radioPanel.add(entityRadio);
		radioPanel.add(ontRadio);
		radioPanel.add(wkspcRadio);
		
		// create checkboxes
		showChanges = new JCheckBox("Show Changes");
		showChanges.setFont(tahomaL);
		showChanges.addActionListener(this);
		showChkPts = new JCheckBox("Show Checkpoints");
		showChkPts.setFont(tahomaL);
		showChkPts.addActionListener(this);
		JPanel chkBoxPanel = new JPanel();
		chkBoxPanel.add(showChanges);
		chkBoxPanel.add(showChkPts);
		// load settings from swoopModel
		if (swoopModel!=null) {
			showChanges.setSelected(swoopModel.getEnableChangeLogDisplay());
			showChkPts.setSelected(swoopModel.isShowCheckPts());
		}
		
		// create change log pane
		logPane = new JEditorPane();
		logPane.setContentType("text/html");
		logPane.setEditable(false);
		logPane.addHyperlinkListener(this);
		
		JPanel optionPanel = new JPanel();
		optionPanel.setLayout(new GridLayout(2,1));
		optionPanel.add(radioPanel);
		optionPanel.add(chkBoxPanel);
		
		setLayout(new BorderLayout());
		add(optionPanel, "North");
		
		// create change tree table
		this.changeTreeTable = new JTreeTable(new DefaultTreeTableModel(getTreeRoot(), cNames));
		this.changeTreeTable.setFont(tahomaL);
		threadTableScroll = new JScrollPane(changeTreeTable);
		
		// set some other properties of the change tree table
		// set preferred col widths
		TableColumn column = null;
		for (int i = 0; i < changeTreeTable.getColumnCount(); i++) {
		    column = changeTreeTable.getColumnModel().getColumn(i);
		    switch (i) {
		    	case 0: column.setPreferredWidth(40); break;
		    	case 1: column.setPreferredWidth(70); break;
		    	case 2: column.setPreferredWidth(20); break;
		    	case 3: column.setPreferredWidth(60); break;
		    } 
		}

		// set default change tree params
		changeTreeTable.getTree().addTreeSelectionListener(this);
		changeTreeTable.getTree().setRootVisible(false);

		// create scroll for log pane
		htmlPanel = new JScrollPane(logPane);
		
		// create main change display pane with changeTree on top and change log pane below
		JSplitPane mainPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		topPanel = new JPanel();
		topPanel.setLayout(new BorderLayout());
		topPanel.add(threadTableScroll, "Center");
		bottomPanel = new JPanel();
		bottomPanel.setLayout(new BorderLayout());
		bottomPanel.add(htmlPanel, "Center");
		mainPane.setTopComponent(topPanel);
		mainPane.setBottomComponent(bottomPanel);
		
		// add save checkpoint button to top of topPanel
		saveBtn = this.initButton("Save Ontology Checkpoint", "Save Checkpoint");
		topPanel.add(saveBtn, "North");
		
		// add toolbar on bottom of topPanel
		undoBtn = initButton("Undo", "Undo Selected Change(s)");
		serBtn = initButton("Serialize", "Serialize Selected Change(s) in RDF/XML");
		attachBtn = initButton("Attach", "Attach Selected Change(s) in an Annotea Annotation");
		deleteBtn = initButton("Clear", "Delete Selected Change(s) from the Log Permanently");
		
		//TODO: weirdest bug ever...make the below a JToolBar and hell breaks lose!
		JPanel toolBar = new JPanel();
		toolBar.setLayout(new GridLayout(1, 4));
		toolBar.add(undoBtn);
		toolBar.add(serBtn); 		
		toolBar.add(attachBtn);
		toolBar.add(deleteBtn);
		topPanel.add(toolBar, "South");
		
		add(mainPane, "Center");
		mainPane.setDividerLocation(200);
		
		// add actionListeners
		entityRadio.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				setScope(ENTITY_SCOPE);
			}			
		});
		ontRadio.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				setScope(ONTOLOGY_SCOPE);
			}			
		});
		wkspcRadio.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				setScope(WORKSPACE_SCOPE);
			}			
		});
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
	
	/**
	 * Set the scope of the change log. Can be one of three values,
	 * Entity, Ontology or Workspace
	 * @param scope - new scope value to set
	 */
	public void setScope(int scope) {
		this.scope = scope;
		// change save checkpoint button label accordingly
		String scopeStr = "Entity";
		if (scope==this.ONTOLOGY_SCOPE) scopeStr = "Ontology";
		else if (scope==this.WORKSPACE_SCOPE) scopeStr = "Workspace";
		saveBtn.setText("Save "+scopeStr+" Checkpoint");
		this.refreshChangePane();
	}
	
	public int getScope() {
		return this.scope;
	}
	
	/**
	 * Re-render the change display pane by displaying 
	 * uncommitted and committed changes. Take into account 
	 * the current scope level
	 */
	public void refreshChangePane() {
		
		// populate the treetable based on scope and object selected in swoop
		try {
			List changeList = new ArrayList();
			switch (scope) {
			
				case 1: // ENTITY SCOPE
					if (swoopModel.getSelectedEntity()!=null) {
						URI uri = swoopModel.getSelectedEntity().getURI();
						changeList = swoopModel.getChangesCache().getChangeList(uri);						
					}
					break;
				case 2: // ONTOLOGY SCOPE
					if (swoopModel.getSelectedOntology()!=null) {
						URI uri = swoopModel.getSelectedOntology().getURI();
						changeList = swoopModel.getChangesCache().getChangeList(uri);						
					}
					break;
				case 3: // WORKSPACE SCOPE
					List changes = swoopModel.getChangesCache().getAllChanges();
					// remove all entity-scope changes
					// since entity-scope changes are subsumed by ontology-scope changes
					for (Iterator iter = changes.iterator(); iter.hasNext();) {
						SwoopChange swc = (SwoopChange) iter.next();
						if (swoopModel.getOntologyURIs().contains(swc.getOwlObjectURI())) {
							changeList.add(swc);
						}
					}
					break;
			}
			setChangeNodeTree(changeList);
		}
		catch (OWLException ex) {
			ex.printStackTrace();
		}
	}
	
	public void setChangeNodeTree(List changeList) {

		// get rootnode from changetreetable
		JTree changeTree = changeTreeTable.getTree();
		TreeTableNode rootNode = (TreeTableNode) changeTree.getModel().getRoot();
		rootNode.children = new Vector();
		logPane.setText("");
		
		// sort changes based on "timestamp" value
		SortedSet changeSet = new TreeSet(ChangeComparator.INSTANCE);
		changeSet.addAll(changeList);
		
		// populate node tree
		for (Iterator changeIter = changeSet.iterator(); changeIter.hasNext();) {
			SwoopChange swc = (SwoopChange) changeIter.next();
			
			// control adding to changeTreeTable based on checkbox selection
			// of "Show Changes" and "Show Checkpoints"
			if (swc.isCheckpointRelated && !showChkPts.isSelected()) continue;
			if (!swc.isCheckpointRelated && !showChanges.isSelected()) continue;
			
			TreeTableNode changeNode = new TreeTableNode(swc);
			rootNode.addChild(changeNode);
		}
		
		// expand all tree nodes
		for (int row=0; row<changeSet.size(); row++)
			changeTree.expandRow(row);
		changeTree.updateUI();
		
		// need to refresh UI
		this.refreshUI();
	}
	
	private void refreshUI() {
		topPanel.add(threadTableScroll, "Center");		
		bottomPanel.add(htmlPanel, "Center");
		topPanel.updateUI();
		bottomPanel.updateUI();
	}

	/**
	 * Return some ontology change related information depending on 
	 * parameter passed to it 
	 * @param change - ontology change
	 * @param function - either:
	 * 			CHANGE_DESCRIPTION : NL description of change
	 * 			CHANGE_RDFXML : Serialization of change in RDF/XML
	 * 			CHANGE_INVERSE : Inverse Ontology Change 
	 *          CHANGE_ALIGN : Align an Ontology Change with an existing SwoopModel Ontology Object
	 * @param ChangeRenderer - used to callback for serializing change (cyclic - needs to be changed)
	 * @param uris - set of URIs which are subjects of the change
	 * @return
	 */
	public Object getChangeInformation(OntologyChange change, int function, OntologyChangeRenderer changeRenderer, List uris, OWLOntology alignOnt) {
		String changeDesc = "";
		
		if (change instanceof RevertCheckpointChange) {
			
			RevertCheckpointChange revertChk = (RevertCheckpointChange) change;
			String desc = revertChk.getDescription();
			String changeStr = "["+revertChk.getTimeStamp()+"]<br>";
			changeStr += desc;
			SaveCheckpointChange chkpt = revertChk.getCheckpointChange();
			int chkPtScope = chkpt.getScope();
			OWLOntology chkPtOnt = swoopModel.getOntology(chkpt.getOntologyURI());
			OWLEntity chkPtEntity = swoopModel.getEntity(chkPtOnt, chkpt.getEntityURI(), false);
			OWLNamedObject currObj = swoopModel.selectedOWLObject;
			if (chkPtOnt==null || currObj==null || (scope == ENTITY_SCOPE && chkPtEntity==null)) return changeDesc;
			switch (function) {
				case CHANGE_DESCRIPTION :
					changeDesc += changeStr;
					break;
			
				case CHANGE_RDFXML :				
					break;
					
				case CHANGE_INVERSE :
					break;
			}			
		}
		else
		if (change instanceof SaveCheckpointChange) {
			SaveCheckpointChange saveChk = (SaveCheckpointChange) change;
			String desc = saveChk.getDescription();
			String location = saveChk.getLocationURL();
			int chkPtScope = saveChk.getScope();
			String title = "";
			switch (chkPtScope) {
				case 1: 
					title = "Entity" + "&nbsp;<a href=\""+saveChk.getEntityURI()+"\">"+swoopModel.shortForm(saveChk.getEntityURI())+"</a>&nbsp;";
					uris.add(saveChk.getEntityURI());
					break;
				case 2:
					title = "Ontology" + "&nbsp;<a href=\""+saveChk.getOntologyURI()+"\">"+swoopModel.shortForm(saveChk.getOntologyURI())+"</a>&nbsp;";
					uris.add(saveChk.getOntologyURI());
					break;
				case 3: 
					title = "Workspace"; 
					uris.addAll(saveChk.workspaceURIs());
					break;
			}
			String timeStamp = saveChk.getTimeStamp();
			
			switch (function) {
			
			case CHANGE_DESCRIPTION :
				String changeStr = "<u>[[[ CHECKPOINT ]]]</u><br>";
				changeStr += "Saved "+title+" Checkpoint";
				changeStr += "<br><b>Description:</b>&nbsp;" + desc;
				if (!location.equals("")) changeStr += "<br><b>Location:</b>&nbsp;" + location;
				changeStr += this.addLinks(change);
				changeDesc += changeStr;
				return changeDesc;
				
			case CHANGE_RDFXML :				
				break;
				
			case CHANGE_INVERSE :
				break;
			}
		}
		else
		if (change instanceof SetFunctional) {
			SetFunctional sf = (SetFunctional) change;
			OWLOntology ont = sf.getOntology();
			OWLProperty prop = sf.getProperty();
			String isFunc = sf.isFunctional()? "true": "false";
			
			switch (function) {
				
			case CHANGE_DESCRIPTION : 
				isFunc = "Functional: " + isFunc;
				changeDesc += renderChange(
						"SET ATTRIBUTE",
						change,														
						"Ontology:", ont,
						"Property:", prop,
						"Attribute", isFunc,
						uris
						);
				return changeDesc;
				
			case CHANGE_RDFXML :
				List argList = new ArrayList();
				argList.add(ont);
				argList.add(prop);
				argList.add(String.valueOf(isFunc));
				return changeRenderer.addtoOWLAPIOntology("SetFunctional", argList);
				
			case CHANGE_INVERSE :
				return new SetFunctional(ont, prop, !sf.isFunctional(), null);
			
			case CHANGE_ALIGN :
				// align a change with an existing ontology from swoopModel
				try {
					return new SetFunctional(alignOnt, (OWLProperty) swoopModel.getEntity(alignOnt, prop.getURI(), true), sf.isFunctional(), null);
				}
				catch (OWLException ex) {
					ex.printStackTrace();
				}
			}	
		}
		else
		if (change instanceof SetInverseFunctional) {
			SetInverseFunctional sf = (SetInverseFunctional) change;
			OWLOntology ont = sf.getOntology();
			OWLProperty prop = sf.getProperty();
			String isIFunc = sf.isInverseFunctional()? "true": "false";
			
			switch (function) {
			
			case CHANGE_DESCRIPTION :
				isIFunc = "InverseFunctional: " + isIFunc;
				changeDesc += renderChange(
						"SET ATTRIBUTE",
						change,
						"Ontology:", ont,
						"Property:", prop,
						"Attribute", isIFunc,
						uris
						);
				return changeDesc;
			
			case CHANGE_RDFXML :
				List argList = new ArrayList();
				argList.add(ont);
				argList.add(prop);
				argList.add(String.valueOf(isIFunc));
				return changeRenderer.addtoOWLAPIOntology("SetInverseFunctional", argList);
				
			case CHANGE_INVERSE :
				return new SetInverseFunctional(ont, (OWLObjectProperty) prop, !sf.isInverseFunctional(), null);
			
			case CHANGE_ALIGN :
				// align a change with an existing ontology from swoopModel
				try {
					return new SetInverseFunctional(alignOnt, (OWLObjectProperty) swoopModel.getEntity(alignOnt, prop.getURI(), true), sf.isInverseFunctional(), null);
				}
				catch (OWLException ex) {
					ex.printStackTrace();
				}
			}
		}
		else
		if (change instanceof SetTransitive) {
			SetTransitive sf = (SetTransitive) change;
			OWLOntology ont = sf.getOntology();
			OWLProperty prop = sf.getProperty();
			String isTran = sf.isTransitive()? "true": "false";
			
			switch (function) {
			
			case CHANGE_DESCRIPTION :
				isTran = "Transitive: " + isTran;
				changeDesc += renderChange(
						"SET ATTRIBUTE",
						change,
						"Ontology:", ont,
						"Property:", prop,
						"Attribute", isTran,
						uris
						);
				return changeDesc;
			
			case CHANGE_RDFXML :
				List argList = new ArrayList();
				argList.add(ont);
				argList.add(prop);
				argList.add(String.valueOf(isTran));
				return changeRenderer.addtoOWLAPIOntology("SetTransitive", argList);
			
			case CHANGE_INVERSE :
				return new SetTransitive(ont, (OWLObjectProperty) prop, !sf.isTransitive(), null);
			
			case CHANGE_ALIGN :
				// align a change with an existing ontology from swoopModel
				try {
					return new SetFunctional(alignOnt, (OWLObjectProperty) swoopModel.getEntity(alignOnt, prop.getURI(), true), sf.isTransitive(), null);
				}
				catch (OWLException ex) {
					ex.printStackTrace();
				}
			}
		}
		else
		if (change instanceof SetSymmetric) {
			SetSymmetric sf = (SetSymmetric) change;
			OWLOntology ont = sf.getOntology();
			OWLProperty prop = sf.getProperty();
			String isSymm = sf.isSymmetric()? "true": "false";
			
			switch (function) {
			
			case CHANGE_DESCRIPTION :
				isSymm = "Symmetric: " + isSymm;
				changeDesc += renderChange(
						"SET ATTRIBUTE",
						change,
						"Ontology:", ont,
						"Property:", prop,
						"Attribute", isSymm,
						uris
						);
				return changeDesc;
				
			case CHANGE_RDFXML :
				List argList = new ArrayList();
				argList.add(ont);
				argList.add(prop);
				argList.add(String.valueOf(isSymm));
				return changeRenderer.addtoOWLAPIOntology("SetSymmetric", argList);
				 
			case CHANGE_INVERSE :
				return new SetSymmetric(ont, (OWLObjectProperty) prop, !sf.isSymmetric(), null);
			
			case CHANGE_ALIGN :
				// align a change with an existing ontology from swoopModel
				try {
					return new SetFunctional(alignOnt, (OWLObjectProperty) swoopModel.getEntity(alignOnt, prop.getURI(), true), sf.isSymmetric(), null);
				}
				catch (OWLException ex) {
					ex.printStackTrace();
				}
			}
		}
		else
		if (change instanceof AddAnnotationInstance) {
			AddAnnotationInstance aoi = (AddAnnotationInstance) change;
			OWLOntology ont = aoi.getOntology();
			OWLObject obj = aoi.getSubject();
			OWLAnnotationProperty prop = aoi.getProperty();
			Object content = aoi.getContent();
			
			switch (function) {
			
			case CHANGE_DESCRIPTION :
				try {
						// add uri of ontology to URI list
						uris.add(change.getOntology().getURI());
					} catch (OWLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				
				changeDesc += renderChange(
						"ADD ANNOTATION",
						change,
						"OWL Object:", obj,
						"Annotation Property:", prop,
						"Content", content,
						uris
						);
				return changeDesc;
			
			case CHANGE_RDFXML :
				break;
			
			case CHANGE_INVERSE :
				return new RemoveAnnotationInstance(ont, obj, prop, aoi.getContent(), null);
			
			case CHANGE_ALIGN :
				// align a change with an existing ontology from swoopModel
				try {
					OWLObject eobj = null;
					if (obj instanceof OWLOntology) eobj = swoopModel.getOntology(((OWLOntology) obj).getURI());
					else if (obj instanceof OWLEntity) obj = swoopModel.getEntity(alignOnt, ((OWLEntity) obj).getURI(), true);
					return new AddAnnotationInstance(alignOnt, eobj, prop, aoi.getContent(), null);
				}
				catch (OWLException ex) {
					ex.printStackTrace();
				}
			}
		}
		else
		if (change instanceof RemoveAnnotationInstance) {
			RemoveAnnotationInstance aoi = (RemoveAnnotationInstance) change;
			OWLOntology ont = aoi.getOntology();
			OWLObject obj = aoi.getSubject();
			OWLAnnotationProperty prop = aoi.getProperty();
			Object content = aoi.getContent();
			
			switch (function) {
			
			case CHANGE_DESCRIPTION :
				try {
						// add uri of ontology to URI list
						uris.add(change.getOntology().getURI());
					} catch (OWLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				
				changeDesc += renderChange(
						"REMOVE ANNOTATION",
						change,
						"OWL Object:", obj,
						"Annotation Property:", prop,
						"Content", content,
						uris
						);
				return changeDesc;
				
			case CHANGE_RDFXML :
				break;
				
			case CHANGE_INVERSE :
				return new AddAnnotationInstance(ont, obj, prop, aoi.getContent(), null);
			
			case CHANGE_ALIGN :
				// align a change with an existing ontology from swoopModel
				try {
					OWLObject eobj = null;
					if (obj instanceof OWLOntology) eobj = swoopModel.getOntology(((OWLOntology) obj).getURI());
					else if (obj instanceof OWLEntity) obj = swoopModel.getEntity(alignOnt, ((OWLEntity) obj).getURI(), true);
					return new RemoveAnnotationInstance(alignOnt, eobj, prop, aoi.getContent(), null);
				}
				catch (OWLException ex) {
					ex.printStackTrace();
				}
			}
		}
		else
		if (change instanceof AddImport) {
			
			AddImport add = (AddImport) change;
			OWLOntology ont = add.getOntology();
			OWLOntology impOnt = add.getImportOntology();
			
			switch (function) {
			
			case CHANGE_DESCRIPTION :			
				changeDesc += renderChange(
						"ADD IMPORT",
						change,
						"", null,
						"Ontology:", ont,
						"Imported Ontology:", impOnt,
						uris
						);
				return changeDesc;
				
			case CHANGE_RDFXML :
				List argList = new ArrayList();
				argList.add(ont);
				argList.add(impOnt);
				return changeRenderer.addtoOWLAPIOntology("AddImport", argList);
				
			case CHANGE_INVERSE :
				return new RemoveImport(ont, impOnt, null);
			
			case CHANGE_ALIGN :
				// align a change with an existing ontology from swoopModel
				try {
					OWLOntology eimpOnt = swoopModel.getOntology(impOnt.getURI());
					return new AddImport(alignOnt, eimpOnt, null);
				}
				catch (OWLException ex) {
					ex.printStackTrace();
				}
			}
		}
		else
		if (change instanceof RemoveImport) {
			
			RemoveImport add = (RemoveImport) change;
			OWLOntology ont = add.getOntology();
			OWLOntology impOnt = add.getImportOntology();
			
			switch (function) {
			
			case CHANGE_DESCRIPTION :			
				changeDesc += renderChange(
						"REMOVE IMPORT",
						change,
						"", null,
						"Ontology:", ont,
						"Imported Ontology:", impOnt,
						uris
						);
				return changeDesc;
			
			case CHANGE_RDFXML :
				List argList = new ArrayList();
				argList.add(ont);
				argList.add(impOnt);
				return changeRenderer.addtoOWLAPIOntology("RemoveImport", argList);
				
			case CHANGE_INVERSE :
				return new AddImport(ont, impOnt, null);
			
			case CHANGE_ALIGN :
				// align a change with an existing ontology from swoopModel
				try {
					OWLOntology eimpOnt = swoopModel.getOntology(impOnt.getURI());
					return new RemoveImport(alignOnt, eimpOnt, null);
				}
				catch (OWLException ex) {
					ex.printStackTrace();
				}
			} 
		}
		else
		if (change instanceof AddEntity) {
			
			AddEntity add = (AddEntity) change;
			OWLOntology ont = add.getOntology();
			OWLEntity entity = add.getEntity();
			String entityType = "ENTITY";
			if (entity instanceof OWLClass) entityType = "CLASS";
			else if (entity instanceof OWLDataProperty) entityType = "DATATYPE_PROPERTY";
			else if (entity instanceof OWLObjectProperty) entityType = "OBJECT_PROPERTY";
			else if (entity instanceof OWLIndividual) entityType = "INDIVIDUAL";
			
			switch (function) {
			
			case CHANGE_DESCRIPTION :
				changeDesc += renderChange(
						"ADD " + entityType,
						change,
						"", null,
						"Ontology:", ont,
						"Entity:", entity,
						uris
						);
				return changeDesc;
				
			case CHANGE_RDFXML :
				List argList = new ArrayList();
				argList.add(ont);
				argList.add(entity);
				return changeRenderer.addtoOWLAPIOntology("Add"+entityType, argList);
				
			case CHANGE_INVERSE :
				return new RemoveEntity(ont, entity, null);
				
			case CHANGE_ALIGN :
				// align a change with an existing ontology from swoopModel
				try {
					return new AddEntity(alignOnt, swoopModel.getEntity(alignOnt, entity.getURI(), true), null);
				}
				catch (OWLException ex) {
					ex.printStackTrace();
				}
			} 
		}
		else
		if (change instanceof RemoveEntity) {
			
			RemoveEntity rem = (RemoveEntity) change;
			OWLOntology ont = rem.getOntology();
			OWLEntity entity = rem.getEntity();
			String entityType = "ENTITY";
			if (entity instanceof OWLClass) entityType = "CLASS";
			else if (entity instanceof OWLDataProperty) entityType = "DATATYPE_PROPERTY";
			else if (entity instanceof OWLObjectProperty) entityType = "OBJECT_PROPERTY";
			else if (entity instanceof OWLIndividual) entityType = "INDIVIDUAL";
			
			switch (function) {
			
			case CHANGE_DESCRIPTION :
				changeDesc += renderChange(
						"REMOVE " + entityType,
						change,
						"", null,
						"Ontology:", ont,
						"Entity:", entity,
						uris
						);
				return changeDesc;
			
			case CHANGE_RDFXML :
				List argList = new ArrayList();
				argList.add(ont);
				argList.add(entity);
				return changeRenderer.addtoOWLAPIOntology("Remove"+entityType, argList);
				
			case CHANGE_INVERSE :
				return new AddEntity(ont, entity, null);
			
			case CHANGE_ALIGN :
				// align a change with an existing ontology from swoopModel
				try {
					return new RemoveEntity(alignOnt, swoopModel.getEntity(alignOnt, entity.getURI(), true), null);
				}
				catch (OWLException ex) {
					ex.printStackTrace();
				}
			} 
			
		}
		else
		if (change instanceof AddClassAxiom) {
			
			AddClassAxiom add = (AddClassAxiom) change;
			OWLClassAxiom axiom = add.getAxiom();
			
			if (axiom instanceof OWLDisjointClassesAxiom) {
				OWLDisjointClassesAxiom dis = (OWLDisjointClassesAxiom) axiom;
				
				switch (function) {
				
				case CHANGE_DESCRIPTION :
					try {
						// add uri of ontology to URI list
						uris.add(change.getOntology().getURI());
						
						List clas = new ArrayList();
						clas.addAll(dis.getDisjointClasses());
						changeDesc += renderChange(
							"ADD DISJOINT CLASSES",
							change,
							clas,
							uris
							);
					}
					catch (OWLException e) {
						e.printStackTrace();
					}
					return changeDesc;
				
				case CHANGE_RDFXML :
					List argList = new ArrayList();
					argList.add(add.getOntology()); // always add ontology as first argument
					try {
							argList.addAll(dis.getDisjointClasses());
						} catch (OWLException e1) {
							e1.printStackTrace();
						}
					return changeRenderer.addtoOWLAPIOntology("AddDisjointClasses", argList);
					
				case CHANGE_INVERSE :
					return new RemoveClassAxiom(change.getOntology(), dis, null);
				
				case CHANGE_ALIGN :
					// align a change with an existing ontology from swoopModel
					try {
						Set clas = dis.getDisjointClasses();
						Set exClas = new HashSet();
						for (Iterator iter = clas.iterator(); iter.hasNext();) {
							OWLDescription desc = (OWLDescription) iter.next();
							if (desc instanceof OWLClass) {
								OWLClass cla = (OWLClass) swoopModel.getEntity(alignOnt, ((OWLClass) desc).getURI(), true);
								exClas.add(cla);
							}
							else exClas.add(desc);
						}
						OWLDisjointClassesAxiom exDis = alignOnt.getOWLDataFactory().getOWLDisjointClassesAxiom(exClas); 
						return new AddClassAxiom(alignOnt, exDis, null);
					}
					catch (OWLException ex) {
						ex.printStackTrace();
					}
				}
			}
			else if (axiom instanceof OWLSubClassAxiom) {
				OWLSubClassAxiom subAxiom = (OWLSubClassAxiom) axiom;
				
				switch (function) {
				
				case CHANGE_DESCRIPTION :
					try {
						// add uri of ontology to URI list
						uris.add(change.getOntology().getURI());
						
						List clas = new ArrayList();
						clas.add(subAxiom.getSubClass());
						clas.add(subAxiom.getSuperClass());
						changeDesc += renderChange(
							"ADD SUB CLASS",
							change,
							clas,
							uris
							);
					}
					catch (OWLException e) {
						e.printStackTrace();
					}
					return changeDesc;
				
				case CHANGE_RDFXML :
					List argList = new ArrayList();
					argList.add(add.getOntology()); // always add ontology as first argument
					try {
							argList.add(subAxiom.getSubClass());
							argList.add(subAxiom.getSuperClass());
						} catch (OWLException e1) {
							e1.printStackTrace();
						}
					return changeRenderer.addtoOWLAPIOntology("AddSubClassAxiom", argList);
					
				case CHANGE_INVERSE :
					return new RemoveClassAxiom(change.getOntology(), subAxiom, null);
				
				case CHANGE_ALIGN :
					// align a change with an existing ontology from swoopModel
					try {
						OWLDescription sub = subAxiom.getSubClass();
						OWLDescription sup = subAxiom.getSuperClass();
						
						if (sub instanceof OWLClass) {
							sub = (OWLClass) swoopModel.getEntity(alignOnt, ((OWLClass) sub).getURI(), true);
						}
						if (sup instanceof OWLClass) {
							sup = (OWLClass) swoopModel.getEntity(alignOnt, ((OWLClass) sup).getURI(), true);
						}
						
						OWLSubClassAxiom exSub = alignOnt.getOWLDataFactory().getOWLSubClassAxiom(sub, sup); 
						return new AddClassAxiom(alignOnt, exSub, null);
					}
					catch (OWLException ex) {
						ex.printStackTrace();
					}
				}
			}
		}
		else
		if (change instanceof RemoveClassAxiom) {
			
			RemoveClassAxiom add = (RemoveClassAxiom) change;
			OWLClassAxiom axiom = add.getAxiom();
			
			if (axiom instanceof OWLDisjointClassesAxiom) {
				OWLDisjointClassesAxiom dis = (OWLDisjointClassesAxiom) axiom;
				
				switch (function) {
				
				case CHANGE_DESCRIPTION :
					try {
						// add uri of ontology to URI list
						uris.add(change.getOntology().getURI());
						
						List clas = new ArrayList();
						clas.addAll(dis.getDisjointClasses());
						changeDesc += renderChange(
							"REMOVE DISJOINT CLASSES",
							change,
							clas,
							uris
							);
					}
					catch (OWLException e) {
						e.printStackTrace();
					}
					return changeDesc;
				
				case CHANGE_RDFXML :
					List argList = new ArrayList();
					argList.add(add.getOntology());
					try {
							argList.addAll(dis.getDisjointClasses());
						} catch (OWLException e1) {
							e1.printStackTrace();
						}
					return changeRenderer.addtoOWLAPIOntology("RemoveDisjointClasses", argList);
					
				case CHANGE_INVERSE :
					return new AddClassAxiom(change.getOntology(), dis, null);
				
				case CHANGE_ALIGN :
					// align a change with an existing ontology from swoopModel
					try {
						Set clas = dis.getDisjointClasses();
						Set exClas = new HashSet();
						for (Iterator iter = clas.iterator(); iter.hasNext();) {
							OWLDescription desc = (OWLDescription) iter.next();
							if (desc instanceof OWLClass) {
								OWLClass cla = (OWLClass) swoopModel.getEntity(alignOnt, ((OWLClass) desc).getURI(), true);
								exClas.add(cla);
							}
							else exClas.add(desc);
						}
						OWLDisjointClassesAxiom exDis = alignOnt.getOWLDataFactory().getOWLDisjointClassesAxiom(exClas); 
						return new RemoveClassAxiom(alignOnt, exDis, null);
					}
					catch (OWLException ex) {
						ex.printStackTrace();
					}
				}
			}
			else if (axiom instanceof OWLSubClassAxiom) {
				OWLSubClassAxiom subAxiom = (OWLSubClassAxiom) axiom;
				
				switch (function) {
				
				case CHANGE_DESCRIPTION :
					try {
						// add uri of ontology to URI list
						uris.add(change.getOntology().getURI());
						
						List clas = new ArrayList();
						clas.add(subAxiom.getSubClass());
						clas.add(subAxiom.getSuperClass());
						changeDesc += renderChange(
							"REMOVE SUB CLASS",
							change,
							clas,
							uris
							);
					}
					catch (OWLException e) {
						e.printStackTrace();
					}
					return changeDesc;
				
				case CHANGE_RDFXML :
					List argList = new ArrayList();
					argList.add(add.getOntology()); // always add ontology as first argument
					try {
							argList.add(subAxiom.getSubClass());
							argList.add(subAxiom.getSuperClass());
						} catch (OWLException e1) {
							e1.printStackTrace();
						}
					return changeRenderer.addtoOWLAPIOntology("RemoveSubClassAxiom", argList);
					
				case CHANGE_INVERSE :
					return new RemoveClassAxiom(change.getOntology(), subAxiom, null);
				
				case CHANGE_ALIGN :
					// align a change with an existing ontology from swoopModel
					try {
						OWLDescription sub = subAxiom.getSubClass();
						OWLDescription sup = subAxiom.getSuperClass();
						
						if (sub instanceof OWLClass) {
							sub = (OWLClass) swoopModel.getEntity(alignOnt, ((OWLClass) sub).getURI(), true);
						}
						if (sup instanceof OWLClass) {
							sup = (OWLClass) swoopModel.getEntity(alignOnt, ((OWLClass) sup).getURI(), true);
						}
						
						OWLSubClassAxiom exSub = alignOnt.getOWLDataFactory().getOWLSubClassAxiom(sub, sup); 
						return new AddClassAxiom(alignOnt, exSub, null);
					}
					catch (OWLException ex) {
						ex.printStackTrace();
					}
				}
			}
		}
		else
		if (change instanceof AddEquivalentClass) {

			AddEquivalentClass add = (AddEquivalentClass) change;
			OWLOntology ont = add.getOntology();
			OWLClass cla = add.getOWLClass();
			OWLDescription desc = add.getDescription();
			
			switch (function) {
			
			case CHANGE_DESCRIPTION :
				changeDesc += renderChange(
						"ADD EQUIVALENT CLASS",
						change,
						"Ontology:", ont,
						"Class:", cla,
						"Equivalent Class:", desc,
						uris
						);
				return changeDesc;
			
			case CHANGE_RDFXML :
				List argList = new ArrayList();
				argList.add(ont);
				argList.add(cla);
				argList.add(desc);
				return changeRenderer.addtoOWLAPIOntology("AddEquivalentClass", argList);
				
			case CHANGE_INVERSE :
				return new RemoveEquivalentClass(ont, cla, desc, null);
				
			case CHANGE_ALIGN :
				// align a change with an existing ontology from swoopModel
				try {
					OWLClass ecla = (OWLClass) swoopModel.getEntity(alignOnt, cla.getURI(), true);
					if (desc instanceof OWLClass) desc = (OWLClass) swoopModel.getEntity(alignOnt, ((OWLClass) desc).getURI(), true);
					return new AddEquivalentClass(alignOnt, ecla, desc, null);
				}
				catch (OWLException ex) {
					ex.printStackTrace();
				}
			}			
		}
		else
		if (change instanceof RemoveEquivalentClass) {

			RemoveEquivalentClass rem = (RemoveEquivalentClass) change;
			OWLOntology ont = rem.getOntology();
			OWLClass cla = rem.getOWLClass();
			OWLDescription desc = rem.getDescription();
			
			switch (function) {
			
			case CHANGE_DESCRIPTION :
				changeDesc += renderChange(
						"REMOVE EQUIVALENT CLASS",
						change,
						"Ontology:", ont,
						"Class:", cla,
						"Equivalent Class:", desc,
						uris
						);
				return changeDesc;
			
			case CHANGE_RDFXML :
				List argList = new ArrayList();
				argList.add(ont);
				argList.add(cla);
				argList.add(desc);
				return changeRenderer.addtoOWLAPIOntology("RemoveEquivalentClass", argList);
				
			case CHANGE_INVERSE :
				return new AddEquivalentClass(ont, cla, desc, null);
			
			case CHANGE_ALIGN :
				// align a change with an existing ontology from swoopModel
				try {
					OWLClass ecla = (OWLClass) swoopModel.getEntity(alignOnt, cla.getURI(), true);
					if (desc instanceof OWLClass) desc = (OWLClass) swoopModel.getEntity(alignOnt, ((OWLClass) desc).getURI(), true);
					return new RemoveEquivalentClass(alignOnt, ecla, desc, null);
				}
				catch (OWLException ex) {
					ex.printStackTrace();
				}
			}
		}
		else
		if (change instanceof BooleanElementChange) {
			
			BooleanElementChange bool = (BooleanElementChange) change;
			OWLOntology ont = bool.getOntology();
			OWLClass cla = bool.getOWLClass();
			OWLDescription desc = bool.getOWLDescription();
			String title = bool.getChangeType().toUpperCase();
			String inverseTitle = "";
			if (title.equals("ADD")) inverseTitle = "Remove";
			else inverseTitle = "Add";
			if (bool.getType().equals(OWLAnd.class)) title+=" INTERSECTION ";
			else if (bool.getType().equals(OWLOr.class)) title+=" UNION ";
			else title+=" COMPLEMENT ";
			title+="ELEMENT";
			
			switch (function) {
			
			case CHANGE_DESCRIPTION :
				changeDesc += renderChange(
						title,
						change,
						"Ontology:", ont,
						"Class:", cla,
						"Element:", desc,
						uris
						);
				return changeDesc;
			
			case CHANGE_RDFXML :
				List argList = new ArrayList();
				argList.add(ont);
				argList.add(cla);
				argList.add(desc);
				if (bool.getChangeType().toUpperCase().equals("ADD")) {
					if (bool.getType().equals(OWLAnd.class)) return changeRenderer.addtoOWLAPIOntology("AddIntersectionElement", argList);
					else return changeRenderer.addtoOWLAPIOntology("AddUnionElement", argList);
				}
				else { 
					if (bool.getType().equals(OWLAnd.class)) return changeRenderer.addtoOWLAPIOntology("RemoveIntersectionElement", argList);
					else return changeRenderer.addtoOWLAPIOntology("RemoveUnionElement", argList);
				}
				
			case CHANGE_INVERSE :
				return new BooleanElementChange(bool.getType(), inverseTitle, ont, cla, desc, null);
			
			case CHANGE_ALIGN :
				// align a change with an existing ontology from swoopModel
				try {
					OWLClass ecla = (OWLClass) swoopModel.getEntity(alignOnt, cla.getURI(), true);
					if (desc instanceof OWLClass) desc = (OWLClass) swoopModel.getEntity(alignOnt, ((OWLClass) desc).getURI(), true);
					return new BooleanElementChange(bool.getType(), bool.getChangeType(), alignOnt, ecla, desc, null);
				}
				catch (OWLException ex) {
					ex.printStackTrace();
				}
			}
		}
		else
		if (change instanceof EnumElementChange) {
			
			EnumElementChange enumElem = (EnumElementChange) change;
			OWLOntology ont = enumElem.getOntology();
			OWLClass cla = enumElem.getOWLClass();
			OWLIndividual ind = enumElem.getOWLIndividual();
			String title = enumElem.getChangeType().toUpperCase();
			String inverseTitle = "";
			if (title.equals("ADD")) inverseTitle = "Remove";
			else inverseTitle = "Add";
			title+=" ENUMERATION ";
			title+="ELEMENT";
			switch (function) {
			
			case CHANGE_DESCRIPTION :
				changeDesc += renderChange(
						title,
						change,
						"Ontology:", ont,
						"Class:", cla,
						"Individual:", ind,
						uris
						);
				return changeDesc;
			
			case CHANGE_RDFXML :
				List argList = new ArrayList();
				argList.add(ont);
				argList.add(cla);
				argList.add(ind);
				if (enumElem.getChangeType().toUpperCase().equals("ADD")) return changeRenderer.addtoOWLAPIOntology("AddEnumerationElement", argList);
				else return changeRenderer.addtoOWLAPIOntology("RemoveEnumerationElement", argList);
				
			case CHANGE_INVERSE :
				return new EnumElementChange(inverseTitle, ont, cla, ind, null);
			
			case CHANGE_ALIGN :
				// align a change with an existing ontology from swoopModel
				try {
					OWLClass ecla = (OWLClass) swoopModel.getEntity(alignOnt, cla.getURI(), true);
					OWLIndividual eind = (OWLIndividual) swoopModel.getEntity(alignOnt, ind.getURI(), true);
					return new EnumElementChange(enumElem.getChangeType(), alignOnt, ecla, eind, null);
				}
				catch (OWLException ex) {
					ex.printStackTrace();
				}
			}
		}
		else
		if (change instanceof AddSuperClass) {

			AddSuperClass add = (AddSuperClass) change;
			OWLOntology ont = add.getOntology();
			OWLClass cla = add.getOWLClass();
			OWLDescription desc = add.getDescription();
			
			switch (function) {
				
			case CHANGE_DESCRIPTION : 
				changeDesc += renderChange(
						"ADD SUPER CLASS",
						change,
						"Ontology:", ont,
						"Class:", cla,
						"Super Class:", desc,
						uris
						);
				return changeDesc;
				
			case CHANGE_RDFXML : 
				List argList = new ArrayList();
				argList.add(ont);
				argList.add(cla);
				argList.add(desc);
				return changeRenderer.addtoOWLAPIOntology("AddSuperClass", argList);
				
			case CHANGE_INVERSE :
				return new RemoveSuperClass(ont, cla, desc, null);
			
			case CHANGE_ALIGN :
				// align a change with an existing ontology from swoopModel
				try {
					OWLClass ecla = (OWLClass) swoopModel.getEntity(alignOnt, cla.getURI(), true);
					if (desc instanceof OWLClass) desc = (OWLClass) swoopModel.getEntity(alignOnt, ((OWLClass) desc).getURI(), true);
					return new AddSuperClass(alignOnt, ecla, desc, null);
				}
				catch (OWLException ex) {
					ex.printStackTrace();
				}
			}
		}
		else
		if (change instanceof RemoveSuperClass) {

			RemoveSuperClass rem = (RemoveSuperClass) change;
			OWLOntology ont = rem.getOntology();
			OWLClass cla = rem.getOWLClass();
			OWLDescription desc = rem.getDescription();
			
			switch (function) {
			
			case CHANGE_DESCRIPTION : 
				changeDesc += renderChange(
						"REMOVE SUPER CLASS",
						change,
						"Ontology:", ont,
						"Class:", cla,
						"Super Class:", desc,
						uris
						);
				return changeDesc;
			
			case CHANGE_RDFXML : 
				List argList = new ArrayList();
				argList.add(ont);
				argList.add(cla);
				argList.add(desc);
				return changeRenderer.addtoOWLAPIOntology("RemoveSuperClass", argList);
				
			case CHANGE_INVERSE :
				return new AddSuperClass(ont, cla, desc, null);
			
			case CHANGE_ALIGN :
				// align a change with an existing ontology from swoopModel
				try {
					OWLClass ecla = (OWLClass) swoopModel.getEntity(alignOnt, cla.getURI(), true);
					if (desc instanceof OWLClass) desc = (OWLClass) swoopModel.getEntity(alignOnt, ((OWLClass) desc).getURI(), true);
					return new RemoveSuperClass(alignOnt, ecla, desc, null);
				}
				catch (OWLException ex) {
					ex.printStackTrace();
				}
			}
		}
		else
		if (change instanceof AddSuperProperty) {

			AddSuperProperty rem = (AddSuperProperty) change;
			OWLOntology ont = rem.getOntology();
			OWLProperty prop = rem.getProperty();
			OWLProperty supProp= rem.getSuperProperty();
			
			switch (function) {
			
			case CHANGE_DESCRIPTION :
				changeDesc += renderChange(
						"ADD SUPER PROPERTY",
						change,
						"Ontology:", ont,
						"Property:", prop,
						"Super Property:", supProp,
						uris
						);
				return changeDesc;
			
			case CHANGE_RDFXML : 
				List argList = new ArrayList();
				argList.add(ont);
				argList.add(prop);
				argList.add(supProp);
				return changeRenderer.addtoOWLAPIOntology("AddSuperProperty", argList);
				
			case CHANGE_INVERSE :
				return new RemoveSuperProperty(ont, prop, supProp, null);
			
			case CHANGE_ALIGN :
				// align a change with an existing ontology from swoopModel
				try {
					OWLProperty eprop = (OWLProperty) swoopModel.getEntity(alignOnt, prop.getURI(), true);
					OWLProperty eSupProp = (OWLProperty) swoopModel.getEntity(alignOnt, supProp.getURI(), true);
					return new AddSuperProperty(alignOnt, eprop, eSupProp, null);
				}
				catch (OWLException ex) {
					ex.printStackTrace();
				}
			}
		}
		else
		if (change instanceof RemoveSuperProperty) {

			RemoveSuperProperty rem = (RemoveSuperProperty) change;
			OWLOntology ont = rem.getOntology();
			OWLProperty prop = rem.getProperty();
			OWLProperty supProp= rem.getSuperProperty();
			switch (function) {
			
			case CHANGE_DESCRIPTION :
				changeDesc += renderChange(
						"REMOVE SUPER PROPERTY",
						change,
						"Ontology:", ont,
						"Property:", prop,
						"Super Property:", supProp,
						uris
						);
				return changeDesc;
			
			case CHANGE_RDFXML : 
				List argList = new ArrayList();
				argList.add(ont);
				argList.add(prop);
				argList.add(supProp);
				return changeRenderer.addtoOWLAPIOntology("RemoveSuperProperty", argList);
				
			case CHANGE_INVERSE :
				return new AddSuperProperty(ont, prop, supProp, null);
			
			case CHANGE_ALIGN :
				// align a change with an existing ontology from swoopModel
				try {
					OWLProperty eprop = (OWLProperty) swoopModel.getEntity(alignOnt, prop.getURI(), true);
					OWLProperty eSupProp = (OWLProperty) swoopModel.getEntity(alignOnt, supProp.getURI(), true);
					return new RemoveSuperProperty(alignOnt, eprop, eSupProp, null);
				}
				catch (OWLException ex) {
					ex.printStackTrace();
				}
			}
		}
		else
		if (change instanceof AddInverse) {

			AddInverse add = (AddInverse) change;
			OWLOntology ont = add.getOntology();
			OWLObjectProperty prop = add.getProperty();
			OWLObjectProperty inv = add.getInverse();
			
			switch (function) {
			
			case CHANGE_DESCRIPTION :
				changeDesc += renderChange(
						"ADD INVERSE PROPERTY",
						change,
						"Ontology:", ont,
						"Property:", prop,
						"Inverse:", inv,
						uris
						);
				return changeDesc;
			
			case CHANGE_RDFXML : 
				List argList = new ArrayList();
				argList.add(ont);
				argList.add(prop);
				argList.add(inv);
				return changeRenderer.addtoOWLAPIOntology("AddInverse", argList);
				
			case CHANGE_INVERSE :
				return new RemoveInverse(ont, prop, inv, null);
			
			case CHANGE_ALIGN :
				// align a change with an existing ontology from swoopModel
				try {
					OWLObjectProperty eprop = (OWLObjectProperty) swoopModel.getEntity(alignOnt, prop.getURI(), true);
					OWLObjectProperty eInvProp = (OWLObjectProperty) swoopModel.getEntity(alignOnt, inv.getURI(), true);
					return new AddInverse(alignOnt, eprop, eInvProp, null);
				}
				catch (OWLException ex) {
					ex.printStackTrace();
				}
			}
		}
		else
		if (change instanceof RemoveInverse) {

			RemoveInverse add = (RemoveInverse) change;
			OWLOntology ont = add.getOntology();
			OWLObjectProperty prop = add.getProperty();
			OWLObjectProperty inv = add.getInverse();
			
			switch (function) {
			
			case CHANGE_DESCRIPTION :
				changeDesc += renderChange(
						"REMOVE INVERSE PROPERTY",
						change,
						"Ontology:", ont,
						"Property:", prop,
						"Inverse:", inv,
						uris
						);
				return changeDesc;
			
			case CHANGE_RDFXML : 
				List argList = new ArrayList();
				argList.add(ont);
				argList.add(prop);
				argList.add(inv);
				return changeRenderer.addtoOWLAPIOntology("RemoveInverse", argList);
				
			case CHANGE_INVERSE :
				return new AddInverse(ont, prop, inv, null);
				
			case CHANGE_ALIGN :
				// align a change with an existing ontology from swoopModel
				try {
					OWLObjectProperty eprop = (OWLObjectProperty) swoopModel.getEntity(alignOnt, prop.getURI(), true);
					OWLObjectProperty eInvProp = (OWLObjectProperty) swoopModel.getEntity(alignOnt, inv.getURI(), true);
					return new RemoveInverse(alignOnt, eprop, eInvProp, null);
				}
				catch (OWLException ex) {
					ex.printStackTrace();
				}
			}
		}
		else
		if (change instanceof AddDomain) {

			AddDomain add = (AddDomain) change;
			OWLOntology ont = add.getOntology();
			OWLProperty prop = add.getProperty();
			OWLDescription desc = add.getDomain();
			
			switch (function) {
			
			case CHANGE_DESCRIPTION :
				changeDesc += renderChange(
						"ADD PROPERTY DOMAIN",
						change,
						"Ontology:", ont,
						"Property:", prop,
						"Domain:", desc,
						uris
						);
				return changeDesc;
			
			case CHANGE_RDFXML : 
				List argList = new ArrayList();
				argList.add(ont);
				argList.add(prop);
				argList.add(desc);
				return changeRenderer.addtoOWLAPIOntology("AddDomain", argList);
				
			case CHANGE_INVERSE :
				return new RemoveDomain(ont, prop, desc, null);
			
			case CHANGE_ALIGN :
				// align a change with an existing ontology from swoopModel
				try {
					OWLProperty eprop = (OWLProperty) swoopModel.getEntity(alignOnt, prop.getURI(), true);
					if (desc instanceof OWLClass) desc = (OWLClass) swoopModel.getEntity(alignOnt, ((OWLClass) desc).getURI(), true);
					return new AddDomain(alignOnt, eprop, desc, null);
				}
				catch (OWLException ex) {
					ex.printStackTrace();
				}
			}
		}
		else
		if (change instanceof RemoveDomain) {

			RemoveDomain rem = (RemoveDomain) change;
			OWLOntology ont = rem.getOntology();
			OWLProperty prop = rem.getProperty();
			OWLDescription desc = rem.getDomain();
			switch (function) {
			
			case CHANGE_DESCRIPTION :
				changeDesc += renderChange(
						"REMOVE PROPERTY DOMAIN",
						change,
						"Ontology:", ont,
						"Property:", prop,
						"Domain:", desc,
						uris
						);
				return changeDesc;
			
			case CHANGE_RDFXML : 
				List argList = new ArrayList();
				argList.add(ont);
				argList.add(prop);
				argList.add(desc);
				return changeRenderer.addtoOWLAPIOntology("RemoveDomain", argList);
				
			case CHANGE_INVERSE :
				return new AddDomain(ont, prop, desc, null);
			
			case CHANGE_ALIGN :
				// align a change with an existing ontology from swoopModel
				try {
					OWLProperty eprop = (OWLProperty) swoopModel.getEntity(alignOnt, prop.getURI(), true);
					if (desc instanceof OWLClass) desc = (OWLClass) swoopModel.getEntity(alignOnt, ((OWLClass) desc).getURI(), true);
					return new RemoveDomain(alignOnt, eprop, desc, null);
				}
				catch (OWLException ex) {
					ex.printStackTrace();
				}
			}
		}
		else
		if (change instanceof AddObjectPropertyRange) {

			AddObjectPropertyRange add = (AddObjectPropertyRange) change;
			OWLOntology ont = add.getOntology();
			OWLObjectProperty prop = add.getProperty();
			OWLDescription desc = add.getRange();
			
			switch (function) {
			
			case CHANGE_DESCRIPTION :
				changeDesc += renderChange(
						"ADD (OBJECT)PROPERTY RANGE",
						change,
						"Ontology:", ont,
						"Object Property:", prop,
						"Range:", desc,
						uris
						);
				return changeDesc;
			
			case CHANGE_RDFXML : 
				List argList = new ArrayList();
				argList.add(ont);
				argList.add(prop);
				argList.add(desc);
				return changeRenderer.addtoOWLAPIOntology("AddObjectPropertyRange", argList);
				
			case CHANGE_INVERSE :
				return new RemoveObjectPropertyRange(ont, prop, desc, null);
			
			case CHANGE_ALIGN :
				// align a change with an existing ontology from swoopModel
				try {
					OWLObjectProperty eprop = (OWLObjectProperty) swoopModel.getEntity(alignOnt, prop.getURI(), true);
					if (desc instanceof OWLClass) desc = (OWLClass) swoopModel.getEntity(alignOnt, ((OWLClass) desc).getURI(), true);
					return new AddObjectPropertyRange(alignOnt, eprop, desc, null);
				}
				catch (OWLException ex) {
					ex.printStackTrace();
				}
			}
		}
		else
		if (change instanceof RemoveObjectPropertyRange) {

			RemoveObjectPropertyRange rem = (RemoveObjectPropertyRange) change;
			OWLOntology ont = rem.getOntology();
			OWLObjectProperty prop = rem.getProperty();
			OWLDescription desc = rem.getRange();
			switch (function) {
			
			case CHANGE_DESCRIPTION :
				changeDesc += renderChange(
						"REMOVE (OBJECT)PROPERTY RANGE",
						change,
						"Ontology:", ont,
						"Object Property:", prop,
						"Range:", desc,
						uris
						);
				return changeDesc;
			
			case CHANGE_RDFXML : 
				List argList = new ArrayList();
				argList.add(ont);
				argList.add(prop);
				argList.add(desc);
				return changeRenderer.addtoOWLAPIOntology("RemoveObjectPropertyRange", argList);
				
			case CHANGE_INVERSE :
				return new AddObjectPropertyRange(ont, prop, desc, null);
			
			case CHANGE_ALIGN :
				// align a change with an existing ontology from swoopModel
				try {
					OWLObjectProperty eprop = (OWLObjectProperty) swoopModel.getEntity(alignOnt, prop.getURI(), true);
					if (desc instanceof OWLClass) desc = (OWLClass) swoopModel.getEntity(alignOnt, ((OWLClass) desc).getURI(), true);
					return new RemoveObjectPropertyRange(alignOnt, eprop, desc, null);
				}
				catch (OWLException ex) {
					ex.printStackTrace();
				}
			}
		}
		else
		if (change instanceof AddDataPropertyRange) {

			AddDataPropertyRange add = (AddDataPropertyRange) change;
			OWLOntology ont = add.getOntology();
			OWLDataProperty prop = add.getProperty();
			OWLDataRange desc = add.getRange();
			switch (function) {
			
			case CHANGE_DESCRIPTION :
				changeDesc += renderChange(
						"ADD (DATA)PROPERTY RANGE",
						change,
						"Ontology:", ont,
						"Object Property:", prop,
						"Range:", desc,
						uris
						);
				return changeDesc;
			
			case CHANGE_RDFXML : 
				List argList = new ArrayList();
				argList.add(ont);
				argList.add(prop);
				argList.add(desc);
				return changeRenderer.addtoOWLAPIOntology("AddDataPropertyRange", argList);				
				
			case CHANGE_INVERSE :
				return new RemoveDataPropertyRange(ont, prop, desc, null);
			
			case CHANGE_ALIGN :
				// align a change with an existing ontology from swoopModel
				try {
					OWLDataProperty eprop = (OWLDataProperty) swoopModel.getEntity(alignOnt, prop.getURI(), true);
					return new AddDataPropertyRange(alignOnt, eprop, desc, null);
				}
				catch (OWLException ex) {
					ex.printStackTrace();
				}
			}
		}
		else
		if (change instanceof RemoveDataPropertyRange) {

			RemoveDataPropertyRange rem = (RemoveDataPropertyRange) change;
			OWLOntology ont = rem.getOntology();
			OWLDataProperty prop = rem.getProperty();
			OWLDataRange desc = rem.getRange();
			switch (function) {
			
			case CHANGE_DESCRIPTION :
				changeDesc += renderChange(
						"REMOVE (DATA)PROPERTY RANGE",
						change,
						"Ontology:", ont,
						"Object Property:", prop,
						"Range:", desc,
						uris
						);
				return changeDesc;
			
			case CHANGE_RDFXML : 
				List argList = new ArrayList();
				argList.add(ont);
				argList.add(prop);
				argList.add(desc);
				return changeRenderer.addtoOWLAPIOntology("RemoveDataPropertyRange", argList);
				
			case CHANGE_INVERSE :
				return new AddDataPropertyRange(ont, prop, desc, null);
			
			case CHANGE_ALIGN :
				// align a change with an existing ontology from swoopModel
				try {
					OWLDataProperty eprop = (OWLDataProperty) swoopModel.getEntity(alignOnt, prop.getURI(), true);
					return new RemoveDataPropertyRange(alignOnt, eprop, desc, null);
				}
				catch (OWLException ex) {
					ex.printStackTrace();
				}
			}
		}
		else
		if (change instanceof AddIndividualClass) {
		
			AddIndividualClass add = (AddIndividualClass) change;
			OWLOntology ont = add.getOntology();
			OWLDescription desc = add.getDescription();
			OWLIndividual ind = add.getIndividual();
			
			switch (function) {
			
			case CHANGE_DESCRIPTION :
				changeDesc += renderChange(
					"ADD INDIVIDUAL",
					change,
					"Ontology:", ont,
					"Class:", desc,
					"Individual:", ind,
					uris
					);
				return changeDesc;
			
			case CHANGE_RDFXML : 
				List argList = new ArrayList();
				argList.add(ont);
				argList.add(ind);
				argList.add(desc);
				return changeRenderer.addtoOWLAPIOntology("AddIndividualClass", argList);
				
			case CHANGE_INVERSE :
				return new RemoveIndividualClass(ont, ind, desc, null);
			
			case CHANGE_ALIGN :
				// align a change with an existing ontology from swoopModel
				try {
					OWLIndividual eind = (OWLIndividual) swoopModel.getEntity(alignOnt, ind.getURI(), true);
					if (desc instanceof OWLClass) desc = (OWLClass) swoopModel.getEntity(alignOnt, ((OWLClass) desc).getURI(), true);
					return new AddIndividualClass(alignOnt, eind, desc, null);
				}
				catch (OWLException ex) {
					ex.printStackTrace();
				}
			}
			
		}
		else
		if (change instanceof RemoveIndividualClass) {

			RemoveIndividualClass rem = (RemoveIndividualClass) change;
			OWLOntology ont = rem.getOntology();
			OWLDescription desc = rem.getDescription();
			OWLIndividual ind = rem.getIndividual();
			switch (function) {
			
			case CHANGE_DESCRIPTION :
				changeDesc += renderChange(
					"REMOVE INDIVIDUAL",
					change,
					"Ontology:", ont,
					"Class:", desc,
					"Individual:", ind,
					uris
					);
				return changeDesc;
			
			case CHANGE_RDFXML : 
				List argList = new ArrayList();
				argList.add(ont);
				argList.add(ind);
				argList.add(desc);
				return changeRenderer.addtoOWLAPIOntology("RemoveIndividualClass", argList);
				
			case CHANGE_INVERSE :
				return new AddIndividualClass(ont, ind, desc, null);
				
			case CHANGE_ALIGN :
				// align a change with an existing ontology from swoopModel
				try {
					OWLIndividual eind = (OWLIndividual) swoopModel.getEntity(alignOnt, ind.getURI(), true);
					if (desc instanceof OWLClass) desc = (OWLClass) swoopModel.getEntity(alignOnt, ((OWLClass) desc).getURI(), true);
					return new RemoveIndividualClass(alignOnt, eind, desc, null);
				}
				catch (OWLException ex) {
					ex.printStackTrace();
				}
			}
		}
		else
		if (change instanceof AddObjectPropertyInstance) {
		
			AddObjectPropertyInstance add = (AddObjectPropertyInstance) change;
			//OWLOntology ont = add.getOntology();
			OWLIndividual subj = add.getSubject();
			OWLObjectProperty prop = add.getProperty();
			OWLIndividual obj = add.getObject();
			
			switch (function) {
			
			case CHANGE_DESCRIPTION :
				try {
					// add uri of ontology to URI list
					uris.add(change.getOntology().getURI());
				} catch (OWLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				changeDesc += renderChange(
						"ADD OBJECT PROPERTY VALUE",
						change,
						"Individual", subj,
						"Property:", prop,
						"Value:", obj,
						uris
						);
				return changeDesc;
			
			case CHANGE_RDFXML : 
				List argList = new ArrayList();
				argList.add(add.getOntology());
				argList.add(subj);
				argList.add(prop);
				argList.add(obj);
				return changeRenderer.addtoOWLAPIOntology("AddObjectPropertyInstance", argList);
				
			case CHANGE_INVERSE :
				return new RemoveObjectPropertyInstance(change.getOntology(), subj, prop, obj, null);
				
			case CHANGE_ALIGN :
				// align a change with an existing ontology from swoopModel
				try {
					OWLIndividual esubj = (OWLIndividual) swoopModel.getEntity(alignOnt, subj.getURI(), true);
					OWLObjectProperty eprop = (OWLObjectProperty) swoopModel.getEntity(alignOnt, prop.getURI(), true);
					OWLIndividual eobj = (OWLIndividual) swoopModel.getEntity(alignOnt, obj.getURI(), true);
					return new AddObjectPropertyInstance(alignOnt, esubj, eprop, eobj, null);
				}
				catch (OWLException ex) {
					ex.printStackTrace();
				}
			}
		}
		else
		if (change instanceof RemoveObjectPropertyInstance) {
			
			RemoveObjectPropertyInstance add = (RemoveObjectPropertyInstance) change;
			//OWLOntology ont = add.getOntology();
			OWLIndividual subj = add.getSubject();
			OWLObjectProperty prop = add.getProperty();
			OWLIndividual obj = add.getObject();
			
			switch (function) {
			
			case CHANGE_DESCRIPTION :
				try {
					// add uri of ontology to URI list
					uris.add(change.getOntology().getURI());
				} catch (OWLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				changeDesc += renderChange(
						"REMOVE OBJECT PROPERTY VALUE",
						change,
						"Individual", subj,
						"Property:", prop,
						"Value:", obj,
						uris
						);
				return changeDesc;
			
			case CHANGE_RDFXML : 
				List argList = new ArrayList();
				argList.add(add.getOntology());
				argList.add(subj);
				argList.add(prop);
				argList.add(obj);
				return changeRenderer.addtoOWLAPIOntology("RemoveObjectPropertyInstance", argList);
				
			case CHANGE_INVERSE :
				return new AddObjectPropertyInstance(change.getOntology(), subj, prop, obj, null);
			
			case CHANGE_ALIGN :
				// align a change with an existing ontology from swoopModel
				try {
					OWLIndividual esubj = (OWLIndividual) swoopModel.getEntity(alignOnt, subj.getURI(), true);
					OWLObjectProperty eprop = (OWLObjectProperty) swoopModel.getEntity(alignOnt, prop.getURI(), true);
					OWLIndividual eobj = (OWLIndividual) swoopModel.getEntity(alignOnt, obj.getURI(), true);
					return new RemoveObjectPropertyInstance(alignOnt, esubj, eprop, eobj, null);
				}
				catch (OWLException ex) {
					ex.printStackTrace();
				}
			}
		}
		else
		if (change instanceof AddDataPropertyInstance) {
			
			AddDataPropertyInstance add = (AddDataPropertyInstance) change;
			//OWLOntology ont = add.getOntology();
			OWLIndividual subj = add.getSubject();
			OWLDataProperty prop = add.getProperty();
			OWLDataValue obj = add.getObject();
			
			switch (function) {
			
			case CHANGE_DESCRIPTION :
				try {
					// add uri of ontology to URI list
					uris.add(change.getOntology().getURI());
				} catch (OWLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				changeDesc += renderChange(
						"ADD DATA PROPERTY VALUE",
						change,
						"Individual", subj,
						"Property:", prop,
						"Value:", obj,
						uris
						);
				return changeDesc;
			
			case CHANGE_RDFXML : 
				List argList = new ArrayList();
				argList.add(add.getOntology());
				argList.add(subj);
				argList.add(prop);
				argList.add(obj);
				return changeRenderer.addtoOWLAPIOntology("AddDataPropertyInstance", argList);
				
			case CHANGE_INVERSE :
				return new RemoveDataPropertyInstance(change.getOntology(), subj, prop, obj, null);
			
			case CHANGE_ALIGN :
				// align a change with an existing ontology from swoopModel
				try {
					OWLIndividual esubj = (OWLIndividual) swoopModel.getEntity(alignOnt, subj.getURI(), true);
					OWLDataProperty eprop = (OWLDataProperty) swoopModel.getEntity(alignOnt, prop.getURI(), true);
					return new AddDataPropertyInstance(alignOnt, esubj, eprop, obj, null);
				}
				catch (OWLException ex) {
					ex.printStackTrace();
				}
			}
		}
		else
		if (change instanceof RemoveDataPropertyInstance) {
			
			RemoveDataPropertyInstance add = (RemoveDataPropertyInstance) change;
			//OWLOntology ont = add.getOntology();
			OWLIndividual subj = add.getSubject();
			OWLDataProperty prop = add.getProperty();
			OWLDataValue obj = add.getObject();
			
			switch (function) {
			
			case CHANGE_DESCRIPTION :
				try {
						// add uri of ontology to URI list
						uris.add(change.getOntology().getURI());
					} catch (OWLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				
				changeDesc += renderChange(
						"REMOVE DATA PROPERTY VALUE",
						change,
						"Individual", subj,
						"Property:", prop,
						"Value:", obj,
						uris
						);
				return changeDesc;
			
			case CHANGE_RDFXML : 
				List argList = new ArrayList();
				argList.add(add.getOntology());
				argList.add(subj);
				argList.add(prop);
				argList.add(obj);
				return changeRenderer.addtoOWLAPIOntology("RemoveDataPropertyInstance", argList);
				
			case CHANGE_INVERSE :
				return new AddDataPropertyInstance(change.getOntology(), subj, prop, obj, null);
			
			case CHANGE_ALIGN :
				// align a change with an existing ontology from swoopModel
				try {
					OWLIndividual esubj = (OWLIndividual) swoopModel.getEntity(alignOnt, subj.getURI(), true);
					OWLDataProperty eprop = (OWLDataProperty) swoopModel.getEntity(alignOnt, prop.getURI(), true);
					return new RemoveDataPropertyInstance(alignOnt, esubj, eprop, obj, null);
				}
				catch (OWLException ex) {
					ex.printStackTrace();
				}				
			}
		}
		else
		if (change instanceof AddIndividualAxiom) {
			
			AddIndividualAxiom add = (AddIndividualAxiom) change;
			OWLIndividualAxiom axiom = add.getAxiom();
			
			if (axiom instanceof OWLSameIndividualsAxiom) {
				OWLSameIndividualsAxiom same = (OWLSameIndividualsAxiom) axiom;
				
				switch (function) {
				
				case CHANGE_DESCRIPTION :
					try {
						// add uri of ontology to URI list
						uris.add(change.getOntology().getURI());
						
						List inds = new ArrayList();
						inds.addAll(same.getIndividuals());
						changeDesc += renderChange(
							"ADD SAME INDIVIDUALS",
							change,
							inds,
							uris
							);
					}
					catch (OWLException e) {
						e.printStackTrace();
					}
					return changeDesc;
				
				case CHANGE_RDFXML :
					List argList = new ArrayList();
					argList.add(add.getOntology());
					try {
							argList.addAll(axiom.getIndividuals());
						} catch (OWLException e1) {
							e1.printStackTrace();
						}
					return changeRenderer.addtoOWLAPIOntology("AddSameIndividuals", argList);
					
				case CHANGE_INVERSE :
					return new RemoveIndividualAxiom(change.getOntology(), same, null);
					
				case CHANGE_ALIGN :
					// align a change with an existing ontology from swoopModel
					try {
						Set inds = same.getIndividuals();
						Set exInds = new HashSet();
						for (Iterator iter = inds.iterator(); iter.hasNext();) {
							exInds.add((OWLIndividual) swoopModel.getEntity(alignOnt, ((OWLIndividual) iter.next()).getURI(), true));
						}
						OWLSameIndividualsAxiom eSame = alignOnt.getOWLDataFactory().getOWLSameIndividualsAxiom(exInds);
						return new AddIndividualAxiom(alignOnt, eSame, null);
					}
					catch (OWLException ex) {
						ex.printStackTrace();
					}
				}
			}
			else if (axiom instanceof OWLDifferentIndividualsAxiom) {
				OWLDifferentIndividualsAxiom diff = (OWLDifferentIndividualsAxiom) axiom;
				
				switch (function) {
				
				case CHANGE_DESCRIPTION :
					try {
						// add uri of ontology to URI list
						uris.add(change.getOntology().getURI());
						
						List inds = new ArrayList();
						inds.addAll(diff.getIndividuals());
						changeDesc += renderChange(
							"ADD DIFFERENT INDIVIDUALS",
							change,
							inds,
							uris
							);
					}
					catch (OWLException e) {
						e.printStackTrace();
					}
					return changeDesc;
				
				case CHANGE_RDFXML :
					List argList = new ArrayList();
					argList.add(add.getOntology());
					try {
							argList.addAll(axiom.getIndividuals());
						} catch (OWLException e1) {
							e1.printStackTrace();
						}
					return changeRenderer.addtoOWLAPIOntology("AddDifferentIndividuals", argList);
					
				case CHANGE_INVERSE :
					return new RemoveIndividualAxiom(change.getOntology(), diff, null);
					
				case CHANGE_ALIGN :
					// align a change with an existing ontology from swoopModel
					try {
						Set inds = diff.getIndividuals();
						Set exInds = new HashSet();
						for (Iterator iter = inds.iterator(); iter.hasNext();) {
							exInds.add((OWLIndividual) swoopModel.getEntity(alignOnt, ((OWLIndividual) iter.next()).getURI(), true));
						}
						OWLDifferentIndividualsAxiom eDiff = alignOnt.getOWLDataFactory().getOWLDifferentIndividualsAxiom(exInds);
						return new AddIndividualAxiom(alignOnt, eDiff, null);
					}
					catch (OWLException ex) {
						ex.printStackTrace();
					}
				}
			}
		}
		else
		if (change instanceof RemoveIndividualAxiom) {
			
			RemoveIndividualAxiom remove = (RemoveIndividualAxiom) change;
			OWLIndividualAxiom axiom = remove.getAxiom();
			
			if (axiom instanceof OWLSameIndividualsAxiom) {
				OWLSameIndividualsAxiom same = (OWLSameIndividualsAxiom) axiom;
				
				switch (function) {
				
				case CHANGE_DESCRIPTION :
					try {
						// add uri of ontology to URI list
						uris.add(change.getOntology().getURI());
						
						List inds = new ArrayList();
						inds.addAll(same.getIndividuals());
						changeDesc += renderChange(
							"REMOVE SAME INDIVIDUALS",
							change,
							inds,
							uris
							);
					}
					catch (OWLException e) {
						e.printStackTrace();
					}
					return changeDesc;
				
				case CHANGE_RDFXML :
					List argList = new ArrayList();
					argList.add(remove.getOntology());
					try {
							argList.addAll(axiom.getIndividuals());
						} catch (OWLException e1) {
							e1.printStackTrace();
						}
					return changeRenderer.addtoOWLAPIOntology("RemoveSameIndividuals", argList);
					
				case CHANGE_INVERSE :
					return new AddIndividualAxiom(change.getOntology(), same, null);
				
				case CHANGE_ALIGN :
					// align a change with an existing ontology from swoopModel
					try {
						Set inds = same.getIndividuals();
						Set exInds = new HashSet();
						for (Iterator iter = inds.iterator(); iter.hasNext();) {
							exInds.add((OWLIndividual) swoopModel.getEntity(alignOnt, ((OWLIndividual) iter.next()).getURI(), true));
						}
						OWLSameIndividualsAxiom eSame = alignOnt.getOWLDataFactory().getOWLSameIndividualsAxiom(exInds);
						return new RemoveIndividualAxiom(alignOnt, eSame, null);
					}
					catch (OWLException ex) {
						ex.printStackTrace();
					}
				}
			}
			else if (axiom instanceof OWLDifferentIndividualsAxiom) {
				OWLDifferentIndividualsAxiom diff = (OWLDifferentIndividualsAxiom) axiom;
				
				switch (function) {
				
				case CHANGE_DESCRIPTION :
					try {
						// add uri of ontology to URI list
						uris.add(change.getOntology().getURI());
						
						List inds = new ArrayList();
						inds.addAll(diff.getIndividuals());
						changeDesc += renderChange(
							"REMOVE DIFFERENT INDIVIDUALS",
							change,
							inds,
							uris
							);
					}
					catch (OWLException e) {
						e.printStackTrace();
					}
					return changeDesc;
				
				case CHANGE_RDFXML :
					List argList = new ArrayList();
					argList.add(remove.getOntology());
					try {
							argList.addAll(axiom.getIndividuals());
						} catch (OWLException e1) {
							e1.printStackTrace();
						}
					return changeRenderer.addtoOWLAPIOntology("RemoveDifferentIndividuals", argList);
					
				case CHANGE_INVERSE :
					return new AddIndividualAxiom(change.getOntology(), diff, null);
					
				case CHANGE_ALIGN :
					// align a change with an existing ontology from swoopModel
					try {
						Set inds = diff.getIndividuals();
						Set exInds = new HashSet();
						for (Iterator iter = inds.iterator(); iter.hasNext();) {
							exInds.add((OWLIndividual) swoopModel.getEntity(alignOnt, ((OWLIndividual) iter.next()).getURI(), true));
						}
						OWLDifferentIndividualsAxiom eDiff = alignOnt.getOWLDataFactory().getOWLDifferentIndividualsAxiom(exInds);
						return new RemoveIndividualAxiom(alignOnt, eDiff, null);
					}
					catch (OWLException ex) {
						ex.printStackTrace();
					}
				}
			}
		}
		
		return changeDesc;
	}

	/**
	 * Given ontology change of type axiom (e.g. OWLDisjointClassesAxiom), 
	 * containing a set of elements (e.g. DisjointClasses)
	 * render description in HTML
	 * @param title - name/type of axiom
	 * @param change - OntologyChange object
	 * @param uncommitted - whether it has been applied yet or not
	 * @param entitySet - set of elements in axiom
	 * @return
	 */
	protected String renderChange(String title, OntologyChange change, List entitySet, List refURIs) {
		
		String changeStr = "<FONT FACE=\""+swoopModel.getFontFace()+"\" SIZE="+swoopModel.getFontSize()+">";
		try {	
			changeStr = "<u>"+title+"</u>";
			changeStr+= addLinks(change);
			changeStr += "&nbsp;(";
			
			Iterator iter2 = entitySet.iterator();
			boolean match = true;
			while (iter2.hasNext()) {
				Object obj = iter2.next();
				
				// add named object to refURIs set
				if (obj instanceof OWLNamedObject) {
					if (((OWLNamedObject) obj).getURI()==null) {
						if (obj instanceof OWLIndividual && ((OWLIndividual) obj).isAnonymous())
							refURIs.add(((OWLIndividual) obj).getAnonId());
					}
					else refURIs.add(((OWLNamedObject) obj).getURI());
				}
				
				//TODO: fix this mess
				// regardless of scope, add obj to swoopModel dirty entity set
				if (obj instanceof OWLEntity) {
					localDirty.put(obj, change.getOntology());
				}
				
				changeStr += getEntityDescription(obj)+",";
			}
			
			changeStr = changeStr.substring(0, changeStr.length()-1);
			changeStr += ")";
			changeStr += "<br>";
		}
		catch (OWLException ex) {
			ex.printStackTrace();
		}
		return changeStr;
	}

	private String addLinks(OntologyChange change) {
		String rem = "";
		int hashCode = change.hashCode();
		swoopModel.changeMap.put(String.valueOf(hashCode), change);
		
		// add view snapshot link for checkpoints
		rem = "&nbsp;&nbsp;&nbsp;<font color=\"green\">";
		if (change instanceof SaveCheckpointChange) {
			if (!((SaveCheckpointChange) change).getSnapShot().equals("")) rem += "(<a href=\"<View:"+hashCode+"\">View</a>)&nbsp;&nbsp;";
		}
		
		rem += "</font><br>";
		return rem;
	}
	
	/**
	 * Get a concise HTML description of a single (OWL) Object
	 * @param obj - OWL object
	 * @return
	 */
	protected String getEntityDescription(Object obj) {
		
		String desc ="";
		StringWriter st = new StringWriter();
		ConciseFormatEntityRenderer cfRend = new ConciseFormatEntityRenderer();
		if (obj instanceof OWLOntology) {
			try {
				URI ontURI = ((OWLOntology) obj).getURI();
				desc = "<a href=\""+ontURI+"\">"+swoopModel.shortForm(ontURI)+"</a>";
			} catch (OWLException e) {
				e.printStackTrace();
			}
		}
		else if (obj instanceof OWLObject) {
			try {
				cfRend.renderObject((OWLObject) obj, swoopModel, st);
			} catch (OWLException e) {
				e.printStackTrace();
			}
			desc = st.toString();			
		}
		else if (obj!=null) desc = obj.toString();		
		return desc;
	}
	
	/**
	 * Strip the <html>, <header> and <body> tags of an HTML string
	 * @param content
	 * @return
	 */
	private String stripHTML(String content) {
		if (content.indexOf("<html>")>=0) {
			int spos = content.indexOf("<body>")+6;
			int epos = content.indexOf("</body>");
			content = content.substring(spos, epos);
			if (content.trim().indexOf("<p")>=0) {
				content = content.substring(content.indexOf(">")+1, content.length());
			}
		}
		return content;
	}
	
	public void hyperlinkUpdate(HyperlinkEvent e) {

		if (e.getSource()==logPane) {
			String hLink = e.getDescription();
			
			if (isURI(hLink)) logPane.setToolTipText(hLink);
			
			if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
				
				if (isURI(hLink)) {
					try {
						URI uri = new URI(hLink);
						if (swoopModel.getOntologyURIs().contains(uri)) {
							// select ontology in main swoop UI
							swoopHandler.ontDisplay.selectOntology(swoopModel.getOntology(uri));
						}
						else {
							swoopHandler.termDisplay.selectEntity(hLink);
						}
						
					} catch (URISyntaxException e1) {						
						e1.printStackTrace();
					}					
				}
				else if (hLink.startsWith("<CLEARLOG")) {
//					// clear the entire change log
//					int options = JOptionPane.YES_NO_OPTION;
//					int result = JOptionPane.showConfirmDialog(this, "Removing all Changes (displayed below) permanently from the Tracking Log. Continue?", "Clear Change Log", options);
//					if (result == JOptionPane.NO_OPTION) return;
					//TODO
				}
				else if (hLink.startsWith("<View:")) {
					// remove uncommitted change from swoopmodel
					String hashCode = hLink.substring(hLink.indexOf(":")+1, hLink.length());
					SaveCheckpointChange change = (SaveCheckpointChange) swoopModel.changeMap.get(hashCode);
					viewCheckPointSnapshot(change);
				}				
			}
		}
	}
	
	/**
	 * Given ontology change and its 3 component elements 
	 * render description in HTML and return string 
	 * @param title - name/type of ontology change
	 * @param change - actual OntologyChange object
	 * @param uncommitted - whether it has been applied yet or not
	 * @param entity1
	 * @param obj1
	 * @param entity2
	 * @param obj2
	 * @param entity3
	 * @param obj3
	 * @return
	 */
	protected String renderChange(
			String title,
			OntologyChange change,
			String entity1, Object obj1, 
			String entity2, Object obj2,
			String entity3, Object obj3,
			List refURIs
	) {
			
		String changeStr = "<FONT FACE=\""+swoopModel.getFontFace()+"\" SIZE="+swoopModel.getFontSize()+">";
		try {
			
			// add URIs of named objects (if any) to refURIs
			if (obj1 instanceof OWLNamedObject) {
				if (((OWLNamedObject) obj1).getURI()==null) {
					if (obj1 instanceof OWLIndividual && ((OWLIndividual) obj1).isAnonymous())
						refURIs.add(((OWLIndividual) obj1).getAnonId());
				}
				else refURIs.add(((OWLNamedObject) obj1).getURI());
			}
			if (obj2 instanceof OWLNamedObject) {
				if (((OWLNamedObject) obj2).getURI()==null) {
					if (obj2 instanceof OWLIndividual && ((OWLIndividual) obj2).isAnonymous())
						refURIs.add(((OWLIndividual) obj2).getAnonId());
				}
				else refURIs.add(((OWLNamedObject) obj2).getURI());
			}
			if (obj3 instanceof OWLNamedObject) {
				if (((OWLNamedObject) obj3).getURI()==null) {
					if (obj3 instanceof OWLIndividual && ((OWLIndividual) obj3).isAnonymous())
						refURIs.add(((OWLIndividual) obj3).getAnonId());
				}
				else refURIs.add(((OWLNamedObject) obj3).getURI());
			}
			
			// add entities to swoopModel dirty set
			//TODO: Fix this mess!
			if (change instanceof AddAnnotationInstance || change instanceof RemoveAnnotationInstance) {
				if (obj1 instanceof OWLEntity) {
					localDirty.put(obj1, change.getOntology());
				}
			}
			else {
				if (obj2 instanceof OWLEntity) {
					localDirty.put(obj2, change.getOntology());
				}
				if (obj3 instanceof OWLEntity) {
					localDirty.put(obj3, change.getOntology());
				}
			}
			
			changeStr += "<u>"+title+"</u>";
			changeStr+= addLinks(change);			
			
			if (!entity1.equals("")) changeStr += "<b>"+entity1+"</b> "+getEntityDescription(obj1)+"<br>";
			if (!entity2.equals("")) changeStr += "<b>"+entity2+"</b> "+getEntityDescription(obj2)+"<br>";
			if (!entity3.equals("")) changeStr += "<b>"+entity3+"</b> "+getEntityDescription(obj3)+"<br>";						
		}
		catch (OWLException ex) {
			ex.printStackTrace();
		}
		return changeStr;
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

	public void actionPerformed(ActionEvent e) {
		try {
			actionPerformedThrowing(e);
		} catch (OWLException ex) {
			// TODO Auto-generated catch block
			ex.printStackTrace();
		}
	}
	
	public void actionPerformedThrowing(ActionEvent e) throws OWLException {

		if (e.getSource()==showChanges) {
			swoopModel.setEnableChangeLogDisplay(showChanges.isSelected());
		}
		else
		if (e.getSource()==showChkPts) {
			swoopModel.setShowCheckPts(showChkPts.isSelected());
		}
		if (e.getSource() == saveBtn) {
			// if save checkpoint button is pressed
			saveCheckPoint(scope, swoopModel.getSelectedOntology(), swoopModel.getSelectedEntity(), null, null);
		}
		else
		if (e.getSource() instanceof JButton) {
			// if any of the buttons on the change toolbar is pressed
			// get all selected changes first
			if (changeTreeTable.getTree().getSelectionPath()==null) return;
			List changes = new ArrayList();
			TreePath[] paths = changeTreeTable.getTree().getSelectionPaths();
			for (int i=0; i<paths.length; i++) {
				TreePath path = paths[i];
				TreeTableNode selNode = (TreeTableNode) path.getLastPathComponent();
				SwoopChange swc = (SwoopChange) selNode.swoopChange;
				changes.add(swc);
			}
			
			// now do action depending on button selected
			if (e.getSource() == undoBtn) {
				
				if (undoBtn.getText().equals("Undo")) {
					List uncommittedChanges = new ArrayList(swoopModel.getUncommittedChanges());
					List inverseChanges = new ArrayList();
					for (Iterator iter = changes.iterator(); iter.hasNext();) {
						SwoopChange swc = (SwoopChange) iter.next();
						if (!swc.isCheckpointRelated) {
							if (!swc.isCommitted) {
								// simply delete change
								swoopModel.getChangesCache().removeOntologyChange(swc.getChange(), swoopModel);
								uncommittedChanges.remove(swc.getChange());
							}
							else {
								// add inverse change to swoopModel uncommitted changes
								OntologyChange inverseChange = (OntologyChange) this.getChangeInformation(swc.getChange(), CHANGE_INVERSE, null, new ArrayList(), null);
								inverseChanges.add(inverseChange);
							}
						}
					}
					//** below does not add changes back to cache **
					swoopModel.setUncommittedChanges(uncommittedChanges);
					
					// ** whereas below we WANT to add inverse changes to cache as well **
					if (inverseChanges.size()>0) swoopModel.addUncommittedChanges(inverseChanges);
				}
				else {
					// revert button has been pressed
					if (changes.size()==1) {
						SwoopChange swc = (SwoopChange) changes.iterator().next();
						SaveCheckpointChange save = (SaveCheckpointChange) swc.getChange();
						this.revertCheckPoint(save);
					}
				}
			}// end undo button click
			else				
			if (e.getSource() == deleteBtn) {
				// if delete changes button is pressed
				List uncommittedChanges = new ArrayList(swoopModel.getUncommittedChanges());
				List committedChanges = new ArrayList(swoopModel.getCommittedChanges());
				
				for (Iterator iter = changes.iterator(); iter.hasNext();) {
					SwoopChange swc = (SwoopChange) iter.next();
					// remove change from cache
					swoopModel.getChangesCache().removeOntologyChange(swc.getChange(), swoopModel);
					// also remove from (un)committed changes
					uncommittedChanges.remove(swc.getChange());
					committedChanges.remove(swc.getChange());
				}
				swoopModel.setUncommittedChanges(uncommittedChanges);
				swoopModel.setCommittedChanges(committedChanges);
			}			
			// else if serialize button is pressed
			else
			if (e.getSource() == serBtn) {
				OntologyChangeRenderer ocRend = new OntologyChangeRenderer(this);
				// extract ontology changes from swoopchanges
				//TODO: We should be able to serialize SwoopChanges with provenance info etc
				List ontChanges = new ArrayList();
				for (int i=0; i<changes.size(); i++) {
					ontChanges.add(((SwoopChange) changes.get(i)).getChange());
				}
				OWLOntology changeOnt = ocRend.serializeOntologyChanges(ontChanges);
				CorrectedRDFRenderer rdfRend = new CorrectedRDFRenderer();
				StringWriter st = new StringWriter();
				try {
					rdfRend.renderOntology(changeOnt, st);
				} catch (RendererException e1) {
					e1.printStackTrace();
				}
				String serChanges = st.toString();
				this.popupRDFXML("Serialized Changes in RDF/XML", serChanges);
			}
			// else if attach to annotea annotation button is pressed
			else
			if (e.getSource() == attachBtn) {
				// extract ontology changes from swoopchanges
				//TODO: We should be able to serialize SwoopChanges with provenance info etc
				List ontChanges = new ArrayList();
				for (int i=0; i<changes.size(); i++) {
					ontChanges.add(((SwoopChange) changes.get(i)).getChange());
				}
				// switch to annotea tab view	
				swoopHandler.advancedTabPane.setSelectedIndex(1);
				// popup new annotation with change set attached
				swoopHandler.annotRenderer.popupAnnotation(ontChanges);
				// make the popup have focus?!
//				swoopHandler.annotRenderer.popupNew.requestFocus();
			}
		}
	}
	
	public void modelChanged(ModelChangeEvent event) {
		
		if (event.getType()==ModelChangeEvent.ONTOLOGY_SEL_CHANGED
			|| event.getType()==ModelChangeEvent.ENTITY_SEL_CHANGED
			|| event.getType()==ModelChangeEvent.ENABLED_CHANGELOG
			|| event.getType()==ModelChangeEvent.EDITABLE_CHANGED
			|| event.getType()==ModelChangeEvent.ONTOLOGY_CHANGED
			|| event.getType()==ModelChangeEvent.ONTOLOGY_LOADED
			|| event.getType()==ModelChangeEvent.ONTOLOGY_REMOVED
			|| event.getType()==ModelChangeEvent.ADDED_CHANGE
			|| event.getType()==ModelChangeEvent.RESET_CHANGE
			|| event.getType()==ModelChangeEvent.ADDED_ENTITY
			|| event.getType()==ModelChangeEvent.REMOVED_ENTITY) {
		
			// if autosave Checkpoints is enabled
			if (swoopModel.getEnableAutoSaveChkPts()) {
				
				if (event.getType() == ModelChangeEvent.ONTOLOGY_LOADED) {
					OWLOntology onto = (OWLOntology) event.getSource();
					this.saveCheckPoint(this.ONTOLOGY_SCOPE, onto, null, "Ontology Loaded into Swoop", null);
					
					// not considering imports for now!					
				}
				
				else if (event.getType() == ModelChangeEvent.ONTOLOGY_CHANGED) {
					Set ontologies = (HashSet) event.getSource();
					// use ontology change descriptions to get snapshot
					for (Iterator iter = ontologies.iterator(); iter.hasNext();) {
						// for each ontology that was just changed
						OWLOntology ont = (OWLOntology) iter.next();
						int numChanges = swoopModel.bufferedChanges.size();
						this.saveCheckPoint(this.ONTOLOGY_SCOPE, ont, null, numChanges+" Ontology Changes Applied", this.getOntSnapshot(ont));
					}
					
				}
			}
			
			/*** Refresh Change Pane ***/
			this.refreshChangePane();
			
			if (swoopModel.getEnableAutoSaveChkPts() && event.getType() == ModelChangeEvent.ONTOLOGY_CHANGED) {
				// save entity checkpoints for each of the new dirty entities
				// get cachedOntologyChanges from swoopModel
				// and set localDirty = new before parsing each cached change
				localDirty = new HashMap();
				for (Iterator iter = swoopModel.bufferedChanges.iterator(); iter.hasNext();) {
					OntologyChange change = (OntologyChange) iter.next();					
					getChangeInformation(change, this.CHANGE_DESCRIPTION, null, new ArrayList(), null).toString();
				}
				
				for (Iterator iter = new HashSet(localDirty.keySet()).iterator(); iter.hasNext();) {
					OWLEntity entity = (OWLEntity) iter.next();
					OWLOntology ont = (OWLOntology) localDirty.get(entity);
					this.saveCheckPoint(this.ENTITY_SCOPE, ont, entity, "Ontology Changes Applied", null);
				}
			}
		}
	}
	
	/**
	 * Save a Swoop Checkpoint at the scope specified (Entity, Ontology or Workspace)
	 * Also save ontology, entity (if any), textual description and a snapshot of the checkpoint
	 * @param chkPtScope
	 * @param ont
	 * @param entity
	 * @param desc
	 * @param snapshot
	 */
	public void saveCheckPoint(int chkPtScope, OWLOntology ont, OWLEntity entity, String desc, String snapshot) {
		
		// create a checkpoint change based on the scope and add it to committed changes
		
		//* first check that checkpoint-scope and checkpointed object match
		Set wkspcURIs = new HashSet();
		boolean error = false;
		if (chkPtScope==WORKSPACE_SCOPE) {
			wkspcURIs = swoopModel.getOntologyURIs();
			
			// also check if saveToDisk is enabled
			// if not, cannot save WORKSPACE checkpoint so display Error and return
			if (!swoopModel.isSaveCheckPtsDisk()) {
				JOptionPane.showMessageDialog(null, "The Workspace Checkpoint cannot be saved unless 'Save Checkpoint To Disk' setting is enabled. See File->Preferences for setting.", "Save Checkpoint Error!", JOptionPane.ERROR_MESSAGE);
				return;
			}
		}
		else {
			if (ont==null) error = true;
			if (chkPtScope==ENTITY_SCOPE) {
				if (entity==null) error = true;
			}
		}		
		if (error) {
//			JOptionPane.showMessageDialog(null, "No Selected Object", "Save Checkpoint Error!", JOptionPane.ERROR_MESSAGE);
			return;
		}
		//* end of check
		
		// prompt description if required
		if (desc==null) {
			desc = JOptionPane.showInputDialog(null,
				"Short textual description of the Checkpoint:",
				"Manual Checkpoint Creation",
				JOptionPane.PLAIN_MESSAGE
				);
		}
		
		if (desc!=null) {
			
			String rdf = "";
			try {
				String timeStamp = swoopModel.getTimeStamp();
				String locationURL = "checkpoints/";
				if (chkPtScope==WORKSPACE_SCOPE) {
					// not sure what else todo here?
					locationURL += "Workspace";
				}
				else {
					//... add ontology location as subdirectory
					String uriStr = swoopModel.shortForm(ont.getURI());
					if (uriStr.indexOf(":")>=0) uriStr = uriStr.substring(uriStr.indexOf(":")+1, uriStr.length());
				  	if (uriStr.indexOf(".")>=0) uriStr = uriStr.substring(0, uriStr.lastIndexOf("."));
					locationURL += uriStr;
					
					StringWriter st = new StringWriter();
					
					if (chkPtScope==ENTITY_SCOPE) {
						//... add entity location as another subdirectory
						uriStr = swoopModel.shortForm(entity.getURI());
						if (uriStr.indexOf(":")>=0) uriStr = uriStr.substring(uriStr.indexOf(":")+1, uriStr.length());
						locationURL += "_" + uriStr;
						
						// get RDF/XML code of Entity alone
						// use RDFXMLEntityRenderer for this
						RDFXMLEntityRenderer rdfRend = new RDFXMLEntityRenderer();
						rdfRend.setEditorEnabled(true);
						rdfRend.render(entity, swoopModel, st);
						
						rdf = st.toString();
						
						// also get snapshot from Conciseformat
						if (snapshot==null) {
							StringWriter sn = new StringWriter();
							ConciseFormatEntityRenderer cfRend = new ConciseFormatEntityRenderer();
							cfRend.render(entity, swoopModel, sn);
							snapshot = sn.toString();
						}
												
					}
					else {
						// get RDF/XML code of Ontology
						// use CorrectedRDFRenderer for this
						CorrectedRDFRenderer rend = new CorrectedRDFRenderer();
						rend.renderOntology(ont, st);
						rdf = st.toString();
						
						// also get snapshot from Abstract Syntax
						if (snapshot==null) {
							snapshot = this.getOntSnapshot(ont);
						}
						// add ontology to dirtySet in swoopModel
						//??
					}
				}
				// add timestamp in the end
				locationURL += "_"+timeStamp;
				locationURL = locationURL.replaceAll(":", "-");
				locationURL = locationURL.replaceAll(" ", "_");
				if (chkPtScope!=WORKSPACE_SCOPE) locationURL += ".rdf";
				else locationURL += ".swp";
				
				if (swoopModel.isSaveCheckPtsDisk()) {
					// save checkpoint file
					switch (chkPtScope) {
	                	case 1: case 2:
	                		FileWriter writer = new FileWriter(locationURL);
	                		writer.write(rdf);
	                		writer.close();
	                		break;
	                	
	                	case 3:
	                		File chkptWkspcFile = new File(locationURL);
	                		File tempPointer = swoopHandler.wkspcFile;
	                		swoopHandler.wkspcFile = chkptWkspcFile;
	                		swoopHandler.saveWorkspace(false, true);
	                		if (tempPointer!=null) swoopHandler.wkspcFile = tempPointer;
	                		break;
	                }
				}
				else locationURL = ""; // in memory
				
				URI ontURI = null, entityURI = null;
				if (ont!=null) ontURI = ont.getURI();
				if (entity!=null) entityURI = entity.getURI();
				SaveCheckpointChange change = new SaveCheckpointChange(chkPtScope, null, ontURI, null, entityURI, wkspcURIs, rdf, locationURL, desc, snapshot, timeStamp, null);
				swoopModel.addCommittedChange(change);
			}
			catch (Exception ex) {
				ex.printStackTrace();
			}
			
		}
	}
	
	public void revertCheckPoint(SaveCheckpointChange change) {
		
		int answer = JOptionPane.showConfirmDialog(
             	swoopHandler,      
             	"Revert back to the Saved Checkpoint?", 
             	"Revert",
             	JOptionPane.YES_NO_OPTION);

		if (answer==JOptionPane.NO_OPTION) return;
		
		try {
			int chkPtScope = change.getScope();
			OWLOntology ont = swoopModel.getOntology(change.getOntologyURI());
			String rdfCode = change.getRDFCode();
			switch (chkPtScope) {
			
				case 1: // entity scope
					OWLEntity entity = swoopModel.getEntity(ont, change.getEntityURI(), false);
					// get only fragment corresponding to entity from RDF/XML code
					int spos = rdfCode.indexOf("xml:base");
					spos = rdfCode.indexOf(">", spos)+1;
					int epos = rdfCode.indexOf("</rdf:RDF>");
					epos = rdfCode.lastIndexOf(">", epos);
					rdfCode = rdfCode.substring(spos, epos+1);
					// save current uri to select it after reparsing ontology
					String currentURI = entity.getURI().toString();
					boolean update = swoopModel.replaceEntityRDF(ont, entity, rdfCode);
					if (!update) 
						JOptionPane.showMessageDialog(this, "Error parsing RDF/XML source code", "Parse Error", JOptionPane.ERROR_MESSAGE);
					break;
					
				case 2: // ontology scope
					StringReader reader = new StringReader(rdfCode);
					// reparse ontology in RDF (but dont parse imported ontologies)
					OWLOntology chkPtOnt = swoopModel.loadOntologyInRDF(reader, ont.getURI(), false);
					// instead AddImport changes after reparsing
					if (chkPtOnt!=null) {
						for (Iterator iter = ont.getIncludedOntologies().iterator(); iter.hasNext();) {
							OWLOntology impOnt = (OWLOntology) iter.next();
							AddImport ai = new AddImport(chkPtOnt, impOnt, null);
							ai.accept((ChangeVisitor) chkPtOnt);
						}
					}
					swoopModel.reloadOntology(chkPtOnt, false);
					break;
					
				case 3: // workspace scope
					String location = change.getLocationURL();
					File wkspcFile = new File(location);
					swoopHandler.clearWorkspace(false);
					swoopHandler.wkspcFile = wkspcFile;
					if (wkspcFile.getName().trim().equals("")) {
						JOptionPane.showMessageDialog(null, "The Workspace Checkpoint was not saved to disk and hence cannot be reloaded.", "Load Checkpoint Error!", JOptionPane.ERROR_MESSAGE);
						return;
					}
					swoopHandler.loadWorkspace(true);
					
					break;
					
			}
			
			// also add RevertCheckpointChange
			RevertCheckpointChange revert = new RevertCheckpointChange(null, change, swoopModel.getTimeStamp(), null);
			swoopModel.addCommittedChange(revert);
		}
		catch (OWLException ex) {
			ex.printStackTrace();
		}
	}
	
	/**
	 * Given a checkpoint change, extract the snapshot and either
	 * 1. add it to the resource holder (for an entity checkpoint)
	 * 2. display it in in a separate textarea (for an ontology checkpoint)
	 * @param change
	 */
	protected void viewCheckPointSnapshot(SaveCheckpointChange change) {
		// add snapshot of checkpoint to resource holder
		
		String snapshot = change.getSnapShot();
		URI objectURI = null;
		if (change.getScope()==this.ENTITY_SCOPE) {
			objectURI = change.getEntityURI();
			String source = "Snapshot of Checkpoint at "+change.getTimeStamp();
			source += "&nbsp;Description: "+change.getDescription();
			source += "&nbsp;Location: "+change.getLocationURL();
			swoopHandler.termDisplay.comparator.addEntity(source, objectURI, snapshot);
		}
		else {
			objectURI = change.getOntologyURI();
			String ont = swoopModel.shortForm(objectURI);
			this.popupRDFXML("Checkpoint of Ontology " + ont, snapshot);
		}
	}
	
	/**
	 * Get the snapshot for an ontology
	 * Currently this returns the OWL Abstract Syntax serialization
	 * of the OWL Ontology
	 * @param ont
	 * @return
	 */
	public String getOntSnapshot(OWLOntology ont) {
		// use Abstract Syntax for snapshot of ontology..what else??
		StringWriter st = new StringWriter();
		try {
			org.semanticweb.owl.io.abstract_syntax.Renderer ASRenderer = 
				new org.semanticweb.owl.io.abstract_syntax.Renderer();
			ASRenderer.renderOntology(ont, st); 			
		}
		catch (OWLException ex) {
			ex.printStackTrace();
		}
		return st.toString();
	}

	public void valueChanged(TreeSelectionEvent e) {
		
		if (e.getSource()==changeTreeTable.getTree()) {
			// get selected tree node change object
			// and render description in html logPane
			if (changeTreeTable.getTree().getSelectionPath()==null) return;
			TreeTableNode selNode = (TreeTableNode) changeTreeTable.getTree().getSelectionPath().getLastPathComponent();
			SwoopChange swc = (SwoopChange) selNode.swoopChange;
			
			// based on change also enable/disable certain buttons and/or change their labels
			if (swc.isCheckpointRelated) {
				undoBtn.setText("Revert");
				undoBtn.setToolTipText("Revert to the Saved Checkpoint");
				serBtn.setEnabled(false);
			}
			else {
				undoBtn.setText("Undo");
				undoBtn.setToolTipText("Undo Selected Change(s)");
				serBtn.setEnabled(true);
			}
			
			// now render HTML description of change in logPane below
			String html = "<FONT FACE=\""+swoopModel.getFontFace()+"\" SIZE="+swoopModel.getFontSize()+">";
			if (!swc.isCheckpointRelated) {
				if (swc.isCommitted) html += "<b>Committed Change</b><hr>";
				else html += "<b>Uncommitted Change</b><hr>";
			}
			else html += "<b>Checkpoint Related Change</b><hr>";
			
			html += "</FONT><FONT FACE=\""+swoopModel.getFontFace()+"\" SIZE="+swoopModel.getFontSize()+">";
			html += "<b>Author</b>:&nbsp;"+swc.getAuthor()+"<br>";
			html += "<b>Time</b>:&nbsp;"+swc.getTimeStamp()+"<br><br>";
			html += swc.getDescription();
			html += "</FONT>";
			logPane.setText(html);
		}
		
	}
	
	private JButton initButton(String lbl, String ttip) {
		JButton button = new JButton();
		button.setFont(tahomaL);
		button.setText(lbl);
		button.setToolTipText(ttip);
		button.addActionListener(this);
		return button;
	}
	
	public JRadioButton getOntRadio() {
		return this.ontRadio;
	}
	
	/**
	 * Popup a simple JFrame with the given title and XML code in it
	 * Also provide a Save Menu option
	 * @param title
	 * @param rdfxml
	 */
	private void popupRDFXML(String title, String rdfxml) {
		JFrame frame = new JFrame("View Source of " + title);
		frame.setSize(500, 500);
		frame.setLocation(300, 200);
		XmlEditorPane pane = new XmlEditorPane();
		pane.setText(rdfxml);
		pane.setCaretPosition(0);
		frame.getContentPane().setLayout(new GridLayout(1,1));
		frame.getContentPane().add(new JScrollPane(pane));
		
		// save option on the menu here 
		JMenu menu = new JMenu("File");
		JMenuItem saveMItem = new JMenuItem("Save");
		menu.add(saveMItem);
		JMenuBar mbar = new JMenuBar();
		mbar.add(menu);
		frame.setJMenuBar(mbar);
		
		frame.show();
		
		final String code = rdfxml;
		// add action listener for save menu item
		saveMItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				File saveFile = null;
				JFileChooser wrapChooser = new JFileChooser();
				int returnVal = wrapChooser.showSaveDialog(null);
	            if (returnVal == JFileChooser.APPROVE_OPTION) {
	                    saveFile = wrapChooser.getSelectedFile();
	            }
	            else {        // save cancelled
	                    return;
	            }
	            if (saveFile!=null) {
	            	try {
		            	FileWriter writer = new FileWriter(saveFile);
		                writer.write(code);
		                writer.close();
	            	}
	            	catch (IOException ex) {
	            		ex.printStackTrace();
	            	}
	            }
			}
		});
	}
}
