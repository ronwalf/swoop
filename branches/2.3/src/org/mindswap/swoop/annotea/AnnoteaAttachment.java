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

package org.mindswap.swoop.annotea;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import org.mindswap.swoop.SwoopFrame;
import org.mindswap.swoop.SwoopModel;
import org.mindswap.swoop.change.ChangeLog;
import org.mindswap.swoop.utils.change.RevertCheckpointChange;
import org.mindswap.swoop.utils.change.SaveCheckpointChange;
import org.semanticweb.owl.model.OWLEntity;
import org.semanticweb.owl.model.OWLNamedObject;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.change.ChangeVisitor;
import org.semanticweb.owl.model.change.OntologyChange;

/**
 * @author Aditya Kalyanpur
 *
 */
public class AnnoteaAttachment extends JDialog implements ActionListener, HyperlinkListener {

	List changeSet, renderedChangeSet, selectedChangeSet; // set of ontology change objects
	List originalAttachment;
	ChangeLog changeLog; // handler to ChangeLog.java that contains useful methods
	// JEditorPane attachPane; // main html pane that renders attachment (change set) data
	JToolBar toolbar;
	JButton applyChangesBtn, attachBtn, cancelBtn;
	//Font tahoma = new Font("Tahoma", Font.PLAIN, 11);
        Font tahoma = new Font("SansSerif", Font.PLAIN, 11);
	final static int MY_ATTACHMENT = 1;
	final static int ANNOTATION_ATTACHMENT = 2;
	int attachScope;
	int attachType;
	JRadioButton entityScope, ontScope, wkspcScope;
	JPanel scopePanel;
	AnnoteaRenderer annoteaRend;
	JCheckBox[] checks;
	List alreadyAttached;
	
	public AnnoteaAttachment(ChangeLog changeLog, List changeSet, int attachType, List alreadyAttached) {
		this.changeLog = changeLog;
		if (alreadyAttached == null) alreadyAttached = new ArrayList();
		this.alreadyAttached = alreadyAttached;
		setupUI();
		OWLNamedObject owlObj = changeLog.swoopModel.selectedOWLObject;
		initialize(changeSet, owlObj, attachType);
		renderChangeSet();
	}
	
	private void setupUI() {
		Container content = this.getContentPane();
		content.setLayout(new BorderLayout());
		entityScope = createRadioButton("Entity", "");
		ontScope = createRadioButton("Ontology", "");
		wkspcScope = createRadioButton("Workspace", "");
		ButtonGroup group = new ButtonGroup();
		group.add(entityScope);
		group.add(ontScope);
		group.add(wkspcScope);
		scopePanel = new JPanel();
		JLabel scopeLbl = new JLabel("Ontology Change Scope:");
		scopeLbl.setFont(tahoma);
		scopePanel.add(scopeLbl);
		scopePanel.add(entityScope);
		scopePanel.add(ontScope);
		scopePanel.add(wkspcScope);
		content.add(scopePanel, "North");
		toolbar = new JToolBar();
		applyChangesBtn = createButton("Apply Change Set", "Apply Changes to current Ontology");
		attachBtn = createButton("Attach Change Set", "Attach Changes to current Annotea Annotation");
		cancelBtn = createButton("Cancel", "Cancel Attachment");
		content.add(toolbar, "South");
		setTitle("Change Set in Annotation Attachment");
		setSize(400, 500);
		setLocation(200, 200);
		setModal(true);
		//setResizable(false);
	}
	
	private JRadioButton createRadioButton(String lbl, String tooltip) {
		JRadioButton btn = new JRadioButton(lbl);
		btn.setFont(tahoma);
		btn.addActionListener(this);
		btn.setToolTipText(tooltip);
		return btn;
	}
	
	private JButton createButton(String lbl, String tooltip) {
		JButton btn = new JButton(lbl);
		btn.setFont(tahoma);
		btn.addActionListener(this);
		btn.setToolTipText(tooltip);
		toolbar.add(btn);
		return btn;
	}
	
	/**
	 * Setup Annotea Attachment: parameters and UI
	 * Do this before calling renderChangeSet()
	 * @param changeSet - ontology change set in annotea attachment
	 * @param owlObj - used to determine scope of ontology changes 
	 * @param attachType - 2 types (send/receive)
	 */
	public void initialize(List changeSet, OWLNamedObject owlObj, int attachType) {
		
		// change set initialization
		this.changeSet = new ArrayList(changeSet);
		
		// modify UI and set attachScope depending on attachType
		this.attachType = attachType;
		switch (attachType) {
			case MY_ATTACHMENT :
				applyChangesBtn.setVisible(false);
				attachBtn.setVisible(true);
				scopePanel.setVisible(true);
				
				// determine scope from owl-object		
				if (owlObj!=null) {
					if (owlObj instanceof OWLOntology) {
						this.attachScope = ChangeLog.ONTOLOGY_SCOPE;
						ontScope.setSelected(true);
					}
					else if (owlObj instanceof OWLEntity) {
						this.attachScope = ChangeLog.ENTITY_SCOPE;
						entityScope.setSelected(true);
					}
				}
				else {
					this.attachScope = changeLog.WORKSPACE_SCOPE;
					wkspcScope.setSelected(true);
				}
				
				break;
			
			case ANNOTATION_ATTACHMENT :
				attachBtn.setVisible(false);
				applyChangesBtn.setVisible(true);
				scopePanel.setVisible(false);
				attachScope = changeLog.WORKSPACE_SCOPE;
				originalAttachment = new ArrayList(changeSet);
				break;
		}
	}
	
	/**
	 * Render current Change Set in Annotea Attachment
	 * as HTML in the JEditor Pane
	 * 
	 */
	public void renderChangeSet() {
		
		int saveScope = changeLog.scope; //IMPORTANT
		
		changeLog.scope = attachScope;		
//		if (attachType==MY_ATTACHMENT) changeHTML += "<u>Current Committed Changes:</u><br><br>";
//		else changeHTML += "<u>Changes in Annotation Message:</u><br><br>";
		JEditorPane[] changePanes = new JEditorPane[changeSet.size()];
		renderedChangeSet = new ArrayList();
		
		int ctr = 0;
		for (Iterator iter = changeSet.iterator(); iter.hasNext();) {
			
			// create dynamic jeditorpane
			changePanes[ctr] = new JEditorPane();
			changePanes[ctr].setContentType("text/html");
			changePanes[ctr].setEditable(false);
			changePanes[ctr].addHyperlinkListener(this);
			
			String changeHTML = "<FONT FACE=\""+changeLog.swoopModel.getFontFace()+"\" SIZE="+changeLog.swoopModel.getFontSize()+">";
			OntologyChange change = (OntologyChange) iter.next();
			
			// skip SaveCheckpointChange(s)
			if (change instanceof SaveCheckpointChange || change instanceof RevertCheckpointChange) continue;
			
			String html = changeLog.getChangeInformation(change, ChangeLog.CHANGE_DESCRIPTION, null, new ArrayList(), null).toString();
			if (!html.trim().equals("")) {
				renderedChangeSet.add(ctr, change);
				changeHTML += html;
				changeHTML = changeHTML.replaceAll("Undo", String.valueOf(ctr+1));
				changePanes[ctr].setText(changeHTML);
				changePanes[ctr].setCaretPosition(0);
				ctr++;
			}			
		}
		Container content = this.getContentPane();
		content.removeAll();
		JPanel centerPane = new JPanel();
		centerPane.setLayout(new GridLayout(ctr,1));
		checks = new JCheckBox[ctr];
		
		for (int i=0; i<ctr; i++) {
			JPanel singleChangePane = new JPanel();
			singleChangePane.setLayout(new BorderLayout());
			singleChangePane.add(new JScrollPane(changePanes[i]), "Center");
			checks[i] = new JCheckBox();
			if (alreadyAttached.contains((OntologyChange) renderedChangeSet.get(i))) checks[i].setSelected(true);
			singleChangePane.add(checks[i], "West");
			centerPane.add(singleChangePane);
		}
		content.setLayout(new BorderLayout());
		content.add(scopePanel, "North");
		content.add(new JScrollPane(centerPane), "Center");
		content.add(toolbar, "South");
		content.repaint();		
		show();
		
		changeLog.scope = saveScope; //IMPORTANT
	}

	public void actionPerformed(ActionEvent e) {
		
		if (e.getSource()==entityScope) {
			attachScope = ChangeLog.ENTITY_SCOPE;
			this.renderChangeSet();
		}
		
		if (e.getSource()==ontScope) {
			attachScope = ChangeLog.ONTOLOGY_SCOPE;
			this.renderChangeSet();
		}
		
		if (e.getSource()==wkspcScope) {
			attachScope = ChangeLog.WORKSPACE_SCOPE;
			this.renderChangeSet();
		}
		
		if (e.getSource()==cancelBtn) {
			selectedChangeSet = new ArrayList();
			dispose();
		}
		
		if (e.getSource()==attachBtn) {
			// get rendered set as final change set
			selectedChangeSet = this.getSelectedChanges();
			dispose();
		}
		
		if (e.getSource()==applyChangesBtn) {
			
			try {
				applyAnnotationChanges();
			}
			catch (Exception ex) {
				ex.printStackTrace();				
			}
			
			dispose();
			
		}
		
	}
	
	public void applyAnnotationChanges() throws Exception {
		
		// get renderedSet as final change set
		selectedChangeSet = this.getSelectedChanges();
		SwoopModel swoopModel = changeLog.swoopModel;
		
		// save current uncommitted changes
		List savedUncommittedChanges = new ArrayList(swoopModel.getUncommittedChanges());
		List savedCommittedChanges = new ArrayList(swoopModel.getCommittedChanges());
		
		// apply changes, two different methods depending on serialization method
		if (SwoopModel.changeSharingMethod == SwoopModel.JAVA_SER) {
			OWLOntology ont = null;
			for (Iterator iter = selectedChangeSet.iterator(); iter.hasNext(); ) {
				OntologyChange change = (OntologyChange) iter.next();
				ont = change.getOntology();
				change.accept((ChangeVisitor) ont);
			}
			swoopModel.reloadOntology(ont, true);
			savedCommittedChanges.addAll(selectedChangeSet);
			swoopModel.setUncommittedChanges(savedUncommittedChanges);
			swoopModel.setCommittedChanges(savedCommittedChanges);
		}
		else {
			// add annotation changes to uncommitted list and apply 
			swoopModel.setUncommittedChanges(selectedChangeSet);
			swoopModel.applyOntologyChanges();
			swoopModel.setUncommittedChanges(savedUncommittedChanges);
		}
		
		// give success message
		JOptionPane.showMessageDialog(this, "Ontology Changes applied successfully", "Annotated Changes", JOptionPane.INFORMATION_MESSAGE);
	}
	
	public List getSelectedChanges() {
		List selectedChanges = new ArrayList();
		for (int i=0; i<checks.length; i++) {
			if (checks[i].isSelected()) selectedChanges.add(renderedChangeSet.get(i));
		}
		return selectedChanges;
	}
	
	public void hyperlinkUpdate(HyperlinkEvent e) {
		
		SwoopModel swoopModel = changeLog.swoopModel;
		SwoopFrame swoopHandler = changeLog.swoopHandler;
		
		if (e.getSource() instanceof JEditorPane) {
			String hLink = e.getDescription();
			
			JEditorPane changePane = (JEditorPane) e.getSource();
			
			if (changeLog.isURI(hLink)) changePane.setToolTipText(hLink);
			else changePane.setToolTipText("");
			
			if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
				
				if (changeLog.isURI(hLink)) {
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
//				else if (hLink.startsWith("Remove from Attachment:")) {
//					// remove changes from change set
//					String hashCode = hLink.substring(23, hLink.length());
//					OntologyChange change = (OntologyChange) changeLog.changeHash.get(hashCode);					
//					this.changeSet.remove(change);
//					
//					// re-render
//					this.renderChangeSet();
//				}
			}
		}
		
	}
	
}
