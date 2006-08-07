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
import java.awt.Cursor;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.StringTokenizer;
import java.util.TimeZone;
import java.util.TreeSet;
import java.util.Properties;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.table.TableColumn;
import javax.swing.text.Document;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import org.mindswap.swoop.ModelChangeEvent;
import org.mindswap.swoop.Swoop;
import org.mindswap.swoop.SwoopFrame;
import org.mindswap.swoop.SwoopModel;
import org.mindswap.swoop.SwoopModelListener;
import org.mindswap.swoop.renderer.SwoopCellRenderer;
import org.mindswap.swoop.treetable.JTreeTable;
import org.mindswap.swoop.utils.SwoopCache;
import org.mindswap.swoop.utils.ui.BrowserControl;
import org.mindswap.swoop.utils.ui.DescriptionComparator;
import org.mindswap.swoop.utils.ui.SwoopIcons;
import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLDataType;
import org.semanticweb.owl.model.OWLEntity;
import org.semanticweb.owl.model.OWLException;
import org.semanticweb.owl.model.OWLNamedObject;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.helper.OntologyHelper;

import com.hexidec.ekit.EkitCore;
import org.mindswap.swoop.utils.InstanceCreator;
// add form processing:use html pane
import com.holub.ui.HTML.HTMLPane;

// add javascript support
import org.mindswap.swoop.utils.JavaScriptHandler;

/**
 * @author Aditya Kalyanpur
 *
 */
public class AnnoteaRenderer extends JPanel implements ActionListener, TreeSelectionListener, SwoopModelListener, MouseListener, HyperlinkListener, KeyListener {
	
	SwoopModel swoopModel;
	SwoopFrame swoopHandler;
	AnnoteaServerPrefs annotPrefs;
	JTreeTable annotTreeTable;
	JButton prefBtn, refreshBtn, newBtn, replyBtn, searchBtn, submitBtn, clearBtn, deleteBtn;
	Font tahoma = new Font("Tahoma", Font.PLAIN, 11);
//        Font tahoma = new Font("SansSerif", Font.PLAIN, 11);
	Font tahomaB = new Font("Tahoma", Font.BOLD, 10);
//        Font tahomaB = new Font("SansSerif", Font.BOLD, 10);
	JScrollPane threadTableScroll, htmlPanel;
	JPanel topPanel, bottomPanel, newThreadPanel, threadBar, ekitPanel, annotBodyPanel;
	JPanel btnPanel;
	JTextField authorFld, dateFld;
	JCheckBox displayAll;
	JComboBox typeBox;
	EkitCore ekitCore;
	
	// add form processing: change to HTMLPane
	//JEditorPane htmlCore;
	HTMLPane htmlCore;
	HTMLPane maxPane;
	
	// add javascript support
	JavaScriptHandler jsHandler = null;
	
	JLabel bodyLbl, statusLbl, autoRetLbl, newAnnotLbl, attachLbl; 
	boolean showAuthenticator = true;
	JTextField subjectFld;
	public JFrame popupNew;
	JCheckBox fixContextChk;
	JButton showAllBtn, annotAttachBtn, myAttachBtn;
	List annotatedChangeSet;
	public SwoopCache annotationCache;
	Map savedDefinitions;
	
	public AnnoteaRenderer(SwoopModel swoopModel, SwoopFrame swoopHandler) {
		
		this.swoopModel = swoopModel;
		this.swoopHandler = swoopHandler;
		this.annotPrefs = new AnnoteaServerPrefs();
		this.annotatedChangeSet = new ArrayList();
		this.annotationCache = swoopModel.getAnnotationCache();
		this.savedDefinitions = new HashMap();
		
		setupUI();
		setupPopup();
		//refillAnnotTypes();
	}
	
	/***
	 * Get the root node of the Annotation Tree
	 * This tree will contain (as its nodes) any global annotations 
	 * downloaded from an Annotea Server pertaining to the current 
	 * selected SwoopModel Ontology/Entity
	 */
	private DefaultMutableTreeNode getTreeRoot() {
		
		// initialize annotation tree
		// for now set to null since no annotea server present
		Description root = new Description();
		root.setCreated("");
		root.setBody("");
		root.setAuthor("[Annotations]");
		
		DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode(root);
		return rootNode;
	}
	
	/***
	 * Correspond to default Annotation Types in the Annotea Protocol
	 * Also align with the static constants in Annotation.java
	 */
	private void refillAnnotTypes() {
		
		// INITIALIZATION OF ANNOTEA - for getting annotation types
		if (Annotea.INSTANCE==null) {
			Annotea.initializeAnnotea();
		}
		
		typeBox.removeAllItems();
		
		// Obtain all annotation types from Annotea.java
		if (Annotea.INSTANCE!=null) {
			Iterator iter = Annotea.INSTANCE.annotationTypes.iterator();
			while (iter.hasNext()) {
				OWLClass annotType = (OWLClass) iter.next();
				typeBox.addItem(annotType); 
			}
		}
	}
	
	private JButton createButton(String lbl, String tooltip) {
		JButton btn = new JButton(lbl);
		btn.setFont(tahoma);
		btn.addActionListener(this);
		btn.setToolTipText(tooltip);
		return btn;
	}
	
	private JTextField createField(String lbl) {
		JTextField fld = new JTextField(lbl);
		fld.setFont(tahoma);
		fld.addActionListener(this);
		return fld;
	}
	
	private JPanel createBox(String txt, JComponent comp) {
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		JLabel lbl = new JLabel(txt);
		//lbl.setFont(new Font("Tahoma", Font.PLAIN, 10));
                lbl.setFont(new Font("SansSerif", Font.PLAIN, 10));
		panel.add(lbl, "West");
		panel.add(comp, "Center");
		return panel;
	}
	
	public void clearAnnotationNodeTree() {
		// clear tree each time node selection changes
		// don't retrieve annotations since that can slow down Swoop considerably
		JTree annotTree = annotTreeTable.getTree();
		annotTreeTable.getTree().clearSelection();
		DefaultMutableTreeNode rootNode = (DefaultMutableTreeNode) annotTree.getModel().getRoot();
		rootNode.removeAllChildren();		
//		annotTree.updateUI();
		htmlCore.setText("");
		annotAttachBtn.setEnabled(false);
		refreshUI();
		this.setAnnotationNodeTree(new HashSet(), new HashSet());
	}
	
	private void setupUI() {
		
		topPanel = new JPanel();
		topPanel.setLayout(new BorderLayout());
		ImageIcon prefIcon = null;
		try {
			prefIcon = SwoopIcons.getImageIcon("Settings.jpg");
		}
		catch (Exception e) {
			System.out.println("Unable to find images/Settings.jpg");
		}
		// global characteristics buttons
		prefBtn = new JButton("");
		if (prefIcon!=null) prefBtn.setIcon(prefIcon);
		prefBtn.setToolTipText("Annotea Server Info");
		prefBtn.addActionListener(this);		
		showAllBtn = new JButton("");
		if (SwoopIcons.rootIcon!=null) showAllBtn.setIcon(SwoopIcons.rootIcon);
		showAllBtn.setToolTipText("Highlight All Annotated Entities in Class/Property Tree");
		showAllBtn.addActionListener(this);
		showAllBtn.setVisible(false);
		
		// local characteristics buttons
		refreshBtn = createButton("Update", "Retrieve Annotations pertaining to selected SWOOP Object");
		newBtn = createButton("New", "Create new annotation");
		deleteBtn = createButton("Delete", "Delete selected annotation");
		replyBtn = createButton("Reply", "Reply to selected annotation");
		searchBtn = createButton("Find", "Search/filter through for specific annotations");
		searchBtn.setEnabled(false);
		threadBar = new JPanel();
		threadBar.setLayout(new BorderLayout());
		threadBar.add(prefBtn, "West");
		JToolBar toolBar = new JToolBar();
		//toolBar.setLayout(new GridLayout(1,4));
		toolBar.add(prefBtn);
		toolBar.add(showAllBtn);
		
		toolBar.add(refreshBtn);
		toolBar.add(newBtn);
		toolBar.add(deleteBtn);
		toolBar.add(replyBtn);
		toolBar.add(searchBtn);
		threadBar.add(toolBar, "Center");
		autoRetLbl = new JLabel("(Auto-Update : OFF, Ctrl-U to toggle setting)");
		autoRetLbl.setFont(tahoma);
		
		displayAll = new JCheckBox("Display All");
		displayAll.setToolTipText("Display Annotations on all Entities in Ontology");
		displayAll.setFont(tahoma);
		displayAll.setVisible(true);
		displayAll.addActionListener(this);
		
		threadBar.add(displayAll, "South");
		topPanel.add(threadBar, "North");
		
		// create threaded-annotation UI using JTreeTable
		DefaultMutableTreeNode rootNode = getTreeRoot();
		AnnotationTreeModel annotModel = new AnnotationTreeModel(rootNode);
		annotTreeTable = new JTreeTable(annotModel);
		annotTreeTable.setFont(tahoma);
		
		// set preferred col widths
		TableColumn column = null;
		for (int i = 0; i < annotTreeTable.getColumnCount(); i++) {
		    column = annotTreeTable.getColumnModel().getColumn(i);
		    switch (i) {
		    	case 0: column.setPreferredWidth(60); break;
		    	case 1: column.setPreferredWidth(80); break;
		    	case 2: column.setPreferredWidth(20); break;
		    	case 3: column.setPreferredWidth(30); break;
		    } 
		}

		// set default annotation tree params
		annotTreeTable.getTree().addTreeSelectionListener(this);
		annotTreeTable.getTree().setRootVisible(false);		
		threadTableScroll = new JScrollPane(annotTreeTable);
		topPanel.add(threadTableScroll, "Center");
		
		newThreadPanel = new JPanel();
		authorFld = createField("");
		dateFld = createField("");
		
		// get system date and put it in dc:date format
		updatePopupDate();
		
		bottomPanel = new JPanel();
		bottomPanel.setLayout(new BorderLayout());
		bodyLbl = new JLabel("Annotation Body:");
		bodyLbl.setFont(tahoma);
		
		/*
		htmlCore = new JEditorPane();
		htmlCore.setContentType("text/html");
		htmlCore.setEditable(false);
		htmlCore.addHyperlinkListener(this);
		htmlCore.addMouseListener(this);
		htmlCore.setToolTipText("Double Click to Maximize Message");
		htmlPanel = new JScrollPane(htmlCore);
		*/
		
		// add form processing
		
		htmlCore = new HTMLPane();
		htmlCore.addActionListener(this);
		
		// javascript handler
		jsHandler = new JavaScriptHandler();
		htmlCore.addTag("javascript", jsHandler);
		htmlCore.setContentType("text/html");
		htmlCore.setEditable(false);
		htmlCore.addHyperlinkListener(this);
		htmlCore.addMouseListener(this);
		htmlCore.setToolTipText("Double Click to Maximize Message");
		htmlPanel = new JScrollPane(htmlCore);
		
		
		btnPanel = new JPanel();
		JToolBar btnBar = new JToolBar();
		submitBtn = createButton("POST Annotation", "Submit new annotation to server");
		clearBtn = createButton("Clear", "Clear Annotation Subject and Body");
		btnBar.add(submitBtn);
		btnBar.add(new JLabel(""));
		btnBar.add(clearBtn);
		btnPanel.setLayout(new BorderLayout());
		btnPanel.add(btnBar, "West");
		JLabel linkLbl = new JLabel("[Insert Entity Hyperlink: Press CTRL-L after Word]");
		linkLbl.setFont(tahoma);
		btnPanel.add(linkLbl, "East");
		statusLbl = new JLabel("Status:");
		statusLbl.setFont(tahoma);
		bottomPanel.add(bodyLbl, "North");
		bottomPanel.add(htmlPanel, "Center");
		
		JPanel lowestPane = new JPanel();
		lowestPane.setLayout(new BorderLayout());
		lowestPane.add(statusLbl,"North");
		fixContextChk = new JCheckBox("LOCK");
		fixContextChk.setFont(tahoma);
		fixContextChk.addActionListener(this);
		lowestPane.add(fixContextChk, "Center");
		
		annotAttachBtn = new JButton("");
		if (SwoopIcons.seeAlsoIcon!=null) annotAttachBtn.setIcon(SwoopIcons.seeAlsoIcon);
		annotAttachBtn.setToolTipText("View Attachment (Ontology Change Set)");
		annotAttachBtn.addActionListener(this);
		annotAttachBtn.setEnabled(false);
		lowestPane.add(annotAttachBtn, "East");
		
		bottomPanel.add(lowestPane, "South");
		
		JSplitPane mainPanel = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		mainPanel.setOneTouchExpandable(true);
		mainPanel.setTopComponent(topPanel);
		JPanel bottomPanels = new JPanel();
		bottomPanels.setLayout(new BorderLayout());
		bottomPanels.add(bottomPanel, "Center");
		bottomPanels.add(statusLbl, "South");
		mainPanel.setBottomComponent(bottomPanels);
		
		setLayout(new GridLayout(1,1));
		add(mainPanel);
		mainPanel.setDividerLocation(220);
	}

	/***
	 * Refresh UI to display either a threaded interface with existing
	 * annotations or a form to fill in details of a new annotation
	 *
	 */
	private void refreshUI() {
		topPanel.add(threadTableScroll, "Center");		
		bottomPanel.add(htmlPanel, "Center");
		topPanel.updateUI();
		bottomPanel.updateUI();
	}
	
	/**
	 * Create UI of popup for new annotea annotation
	 *
	 */
	public void setupPopup() {
		popupNew = new JFrame();
		Container content = popupNew.getContentPane();
		content.setLayout(new GridLayout(1,1));
		
		// create new-annotation panel
		newThreadPanel.setLayout(new BorderLayout());
		newAnnotLbl = new JLabel(" Creating New Annotation: ");
		newAnnotLbl.setFont(tahoma);
		newThreadPanel.add(newAnnotLbl, "North");
		JPanel infoPanel = new JPanel();
		infoPanel.setLayout(new GridLayout(5,1));
		newThreadPanel.add(infoPanel, "Center");
		infoPanel.add(new JLabel(""));
		infoPanel.add(createBox(" Author:", authorFld));
		infoPanel.add(createBox("     Date:", dateFld));
		
		// create annotation type box
		typeBox = new JComboBox();
		typeBox.setFont(tahoma);
		typeBox.setRenderer(new SwoopCellRenderer(swoopModel));
		infoPanel.add(createBox("    Type:", typeBox));
		
		// setup Ekit
		ekitPanel = new JPanel();
		ekitPanel.setLayout(new BorderLayout());
		ekitCore = new EkitCore(null, null, null, null, false, true, true, null, null, false, false);
		ekitCore.getTextPane().addHyperlinkListener(this);
		ekitCore.getTextPane().addKeyListener(this);
		JPanel ekitTopPanel = new JPanel();
		ekitTopPanel.setLayout(new GridLayout(2,1));
		ekitTopPanel.add(ekitCore.getMenuBar());
		ekitTopPanel.add(ekitCore.getToolBar(true));
		ekitPanel.add(ekitTopPanel, "North");
		ekitPanel.add(ekitCore, "Center");
		
		// setup annotation message panel
		annotBodyPanel = new JPanel();
		annotBodyPanel.setLayout(new BorderLayout());
		subjectFld = new JTextField();
		subjectFld.setFont(tahoma);
		JPanel annotHeadPanel = new JPanel();
		annotHeadPanel.setLayout(new GridLayout(2,1));
		annotHeadPanel.add(createBox("Subject:", subjectFld));
		myAttachBtn = new JButton("Attach Change Set:");
		if (SwoopIcons.seeAlsoIcon!=null) myAttachBtn.setIcon(SwoopIcons.seeAlsoIcon);
		myAttachBtn.setToolTipText("Attach Ontology Change Set");
		myAttachBtn.addActionListener(this);
		myAttachBtn.setFont(tahoma);
		JPanel attachPanel = new JPanel();		
		attachPanel.setLayout(new BorderLayout());
		JPanel leftAttachPane = new JPanel();
		leftAttachPane.setLayout(new BorderLayout());		
		leftAttachPane.add(myAttachBtn, "West");
		attachLbl = new JLabel("None");
		attachLbl.setFont(tahoma);
		leftAttachPane.add(attachLbl, "Center");
		attachPanel.add(leftAttachPane, "West");
		annotHeadPanel.add(attachPanel);
		annotBodyPanel.add(annotHeadPanel, "North");
		annotBodyPanel.add(ekitPanel, "Center");
		
		JPanel tPanel = new JPanel();
		tPanel.setLayout(new BorderLayout());
		tPanel.add(newThreadPanel, "Center");
		JPanel bPanel = new JPanel();
		bPanel.setLayout(new BorderLayout());
		bPanel.add(bodyLbl, "North");
		bPanel.add(annotBodyPanel, "Center");
		bPanel.add(btnPanel, "South");
		JSplitPane mainPanel = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		mainPanel.setOneTouchExpandable(true);
		mainPanel.setTopComponent(tPanel);
		mainPanel.setBottomComponent(bPanel);
		content.add(mainPanel);
		
		popupNew.setLocation(300,100);
		popupNew.setSize(550,600);
		popupNew.setTitle("New Annotea Annotation");
		popupNew.hide();		
	}
	
	public String getSubject(Description annot) {
		
		String subject = "";
		if (annot.getBody()!=null) {
    		subject = annot.getBody().trim();
	    	if (subject.indexOf("<head>")>=0) {
	    		subject = subject.substring(subject.indexOf("<head>")+6, subject.indexOf("</head>")).trim();
	    	}
		}
		return subject;
	}
	
	public void actionPerformed(ActionEvent e) {
		
		if (e.getSource()==newBtn) {
			// new-thread button clicked
			popupAnnotation(new ArrayList());
		}
		
		if (e.getSource()==replyBtn) {			
			
			//TODO make this the standard Annotea reply
			// currently its the mail style reply
			// get current selected description' subject
			if (annotTreeTable.getTree().getSelectionPath()==null) return;
			DefaultMutableTreeNode selNode = (DefaultMutableTreeNode) annotTreeTable.getTree().getSelectionPath().getLastPathComponent();
			Description selAnnot = (Description) selNode.getUserObject();
			String subject = this.getSubject(selAnnot);
			String REsubject = "Re: "+subject;
			subjectFld.setText(REsubject);
			this.refillAnnotTypes();
			ekitCore.getTextPane().setText(htmlCore.getText());
			ekitCore.getTextPane().setCaretPosition(0);
			popupNew.show();
			updatePopupDate();
			statusLbl.setText("Status: Replying to annotation");
		}
		
		if (e.getSource()==prefBtn) {
			// preferences button
			annotPrefs.show();
		}
		
		if (e.getSource()==displayAll) {
			// toggle display all annotated entities
			this.displayAnnotations();
		}
		
		if (e.getSource()==submitBtn) {
			Cursor currentCursor = this.getCursor();
			this.setCursor(new Cursor(Cursor.WAIT_CURSOR));			
			postAnnotation(); // post annotation to annotea server
			this.setCursor(currentCursor);
		}
		
		if (e.getSource()==refreshBtn && refreshBtn.getText().equals("Update")) {
			// retrieve all annotations				
			retrieveAnnotations();			
		}
		
		if (e.getSource()==clearBtn) {
			// clear annotation body
			clearBody();	
		}
		
		if (e.getSource()==deleteBtn) {
			// delete selected annotation
			if (annotTreeTable.getTree().getSelectionPath()==null) return;
			DefaultMutableTreeNode selNode = (DefaultMutableTreeNode) annotTreeTable.getTree().getSelectionPath().getLastPathComponent();
			Description selAnnot = (Description) selNode.getUserObject();
			if (selAnnot==null) {
				statusLbl.setText("Status: ERROR - need to select annotation");
				return; // displ 
			}
			deleteAnnotation(selAnnot);
		}
		
		if (e.getSource()==fixContextChk) {
			// fix context checkbox clicked
			if (fixContextChk.isSelected()) {
				this.toggleButtonEnable(false);
				try {
					statusLbl.setText("Status: "+swoopModel.shortForm(swoopModel.selectedOWLObject.getURI())+" LOCKED");
				} catch (OWLException e1) {
					e1.printStackTrace();
				} 
			}
			else {
				this.toggleButtonEnable(true);
				statusLbl.setText("Status: Context Dynamic - Need to Update");
			}
		}
		
		if (e.getSource()==annotAttachBtn) {
			popupAttachment(AnnoteaAttachment.ANNOTATION_ATTACHMENT);
		}
		
		if (e.getSource()==myAttachBtn) {
			popupAttachment(AnnoteaAttachment.MY_ATTACHMENT);
		}
		
		if (e.getSource()==searchBtn) {
			// TEMPORARY HACK REMOVE
		}
		
		// add form processing
		if (e.getSource() == htmlCore || e.getSource() == maxPane){
		  HTMLPane.FormActionEvent act = (HTMLPane.FormActionEvent)e;
          System.out.println("Form output");
          System.out.println("method " + act.method());
          System.out.println("action " + act.action());
          System.out.println("name " + act.name());
          act.data().list(System.out);
          System.out.println("End of form output");
          
          // javascript handlering
          String[] n3 = invokeJavaScript(act.name(), act.data());
          
          try{
	  	    InstanceCreator c = new InstanceCreator();
	  	    c.createInstances(popupNew, n3, swoopModel);
	  	  } catch (Exception ex){
	  	    JOptionPane.showMessageDialog(htmlCore,
	                ex.getMessage(),
	                "Instance Creation Error",
	                JOptionPane.ERROR_MESSAGE);
	        //ex.printStackTrace();
	  	  }
		}
		
		
	}
	
    //	 javascript handlering
	private String[] invokeJavaScript(String formName, Properties data){
	    boolean verified = true;
	    try{
	      verified = jsHandler.verify(formName, data);
	    } catch (Exception e){
	      JOptionPane.showMessageDialog(htmlCore,
	                e.getMessage(),
	                "Javascript Error",
	                JOptionPane.ERROR_MESSAGE);
	       //e.printStackTrace();
	       return new String[0];
	    }
	    
	    if (! verified){
	        JOptionPane.showMessageDialog(htmlCore,
	                jsHandler.getErrorMsg(),
	                "Form Error",
	                JOptionPane.ERROR_MESSAGE);
	        return new String[0];
	    }
	    
	    System.out.println("Javascript verification result is " + verified);
	    String[] n3 = null;
	    try{
	      n3 = jsHandler.generateN3();
	      for (int i=0;i<n3.length;i++){
		    System.out.println(n3[i]);
		  }
	      return n3;
	    } catch (Exception e){
	        JOptionPane.showMessageDialog(htmlCore,
	                e.getMessage(),
	                "Javascript Error",
	                JOptionPane.ERROR_MESSAGE);
	        //e.printStackTrace();
	        return new String[0];
	    }	    
	}
	
	private void toggleButtonEnable(boolean mode) {
		this.refreshBtn.setEnabled(mode);
		this.newBtn.setEnabled(mode);
		this.deleteBtn.setEnabled(mode);
		this.replyBtn.setEnabled(mode);
	}
	
	/**
	 * Check if an entityURI is present in the ontology
	 * @param ont - ontology to check
	 * @param entityURI - URI of the entity
	 * @return
	 */
	private boolean isEntityPresent(OWLOntology ont, URI entityURI) {
		
		try {
			if (ont.getClass(entityURI)!=null ||
				ont.getDataProperty(entityURI)!=null ||
				ont.getObjectProperty(entityURI)!=null ||
				ont.getIndividual(entityURI)!=null) {
				return true;			
			}
		}
		catch (OWLException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	/**
	 * Popup Annotation Frame, initialize its UI
	 */
	public void popupAnnotation(List changes) {
		
		this.refillAnnotTypes();
		this.annotatedChangeSet = new ArrayList(changes);
		attachLbl.setText(" None");
		
		String owlObj = "";
		try {
			if (swoopModel.selectedOWLObject!=null) {
				owlObj = swoopModel.shortForm(swoopModel.selectedOWLObject.getURI());
			}
		} catch (OWLException ex) {
			ex.printStackTrace();
		}
		newAnnotLbl.setText(" Creating New Annotation : " + owlObj);
		
		// also set popup annotation change set if any
		if (changes.size()>0) attachLbl.setText(" Ontology Change Set attached (size:"+annotatedChangeSet.size()+")");
		else attachLbl.setText("None");
		
		popupNew.show();
		updatePopupDate();
		statusLbl.setText("Status: Posting new annotation");
	}
	
	/**
	 * Popup window showing attachment (change set) details.
	 * attachType determines whether its the current SWOOP change set
	 * or change set attached to annotea annotation
	 * @param attachType
	 */
	protected void popupAttachment(int attachType) {
		
		switch (attachType) {
		
			case AnnoteaAttachment.MY_ATTACHMENT :
				
				// obtain current active change set
				List changeSet = new ArrayList(swoopModel.getCommittedChanges());
				
				// create modal dialog - attachment
				AnnoteaAttachment putAttachment = new AnnoteaAttachment(swoopHandler.changeLog, changeSet, attachType, annotatedChangeSet);	
				annotatedChangeSet = (ArrayList) putAttachment.selectedChangeSet;
				
				if (annotatedChangeSet!=null && annotatedChangeSet.size()>0) {
					attachLbl.setText(" Ontology Change Set attached (size:"+annotatedChangeSet.size()+")");					
				}
				else attachLbl.setText(" None");
				
				break;
			
			case AnnoteaAttachment.ANNOTATION_ATTACHMENT :
				
				// get selected description node
				if (annotTreeTable.getTree().getSelectionPath()==null) {
					statusLbl.setText("Status: ERROR - select row in table");
					return;
				}
				DefaultMutableTreeNode selNode = (DefaultMutableTreeNode) annotTreeTable.getTree().getSelectionPath().getLastPathComponent();
				Description selAnnot = (Description) selNode.getUserObject();
				AnnoteaAttachment viewAttachment = new AnnoteaAttachment(swoopHandler.changeLog, selAnnot.getOntologyChangeSet(), attachType, new ArrayList());
				break;
		}
	}
	
	/**
	 * Clear annotation body and summary
	 */
	public void clearBody() {
		subjectFld.setText("");
		ekitCore.getTextPane().setText("");
	}
	
	private void clearOntAnnotationCache(OWLOntology ont) {
		try {
			Set entities = ont.getClasses();
			entities.addAll(ont.getDataProperties());
			entities.addAll(ont.getObjectProperties());
			entities.addAll(ont.getIndividuals());
			// finally add ontology itself for checking
			entities.add(ont);
			for (Iterator iter = entities.iterator(); iter.hasNext(); ) {
				URI owlObjURI = ((OWLNamedObject) iter.next()).getURI();
				annotationCache.putAnnotationSet(owlObjURI, new HashSet());
				swoopModel.getAnnotatedObjectURIs().remove(owlObjURI);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Obtain all annotations from Annotea server
	 * pertaining to current selected OWL entity
	 *
	 */
	public void retrieveAnnotations() {
		
		// check if entity is selected first
		if (swoopModel.selectedOWLObject==null) {
			statusLbl.setText("Status: ERROR retrieving annotations - need to select OWL ontology / entity");
			return;
		}
		
		Cursor currentCursor = this.getCursor();
		this.setCursor(new Cursor(Cursor.WAIT_CURSOR));
		
		// INITIALIZATION OF ANNOTEA DURING FIRST POST/UPDATE
		if (Annotea.INSTANCE==null) {
			Annotea.initializeAnnotea();
		}
		
		// iterate through all servers in Retrieve List
		Iterator getIter = annotPrefs.serverRList.iterator();
		while (getIter.hasNext()) {
			// retrieve annotations from server
			URL serverURL;
			try {
				serverURL = new URL(getIter.next().toString());
				AnnoteaClient client = new AnnoteaClient(serverURL, swoopModel);
				
				// call findAnnotations passing it OWL object URI
				// and get a set of Description instances
				URI currentURI = swoopModel.selectedOWLObject.getURI();				
				Set descSet = client.findAnnotations(currentURI);
				// modify annotates of each description 
				if (descSet.size()>0) {
					this.normalizeAnnotates(descSet);
					swoopModel.addAnnotatedObjectURI(currentURI);
				}
				
				OWLOntology ont = swoopModel.getSelectedOntology();
				if (currentURI.equals(ont.getURI())) {
					// for an ontology,
					// annotations also contain annotations on all entities in ontology
					// hence need to run through descriptions to see which entity they annotate and add to cache
					
					// first clear all annotation caches of entities in ontology
					// since we're using HashSet and duplicate annotations can appear when added below
					this.clearOntAnnotationCache(ont); //*** crucial
					
					for (Iterator iter = descSet.iterator(); iter.hasNext();) {
						Description desc = (Description) iter.next();
						URI annotURI = this.getAnnotationURI(desc.annotates);
						this.addAnnotationCache(annotURI, desc);
						swoopModel.addAnnotatedObjectURI(annotURI);
					}
					
					// refresh display to show "A" superscript
					swoopHandler.termDisplay.updateTreeDisplay();
					swoopHandler.termDisplay.updateListDisplay();
					swoopHandler.ontDisplay.simplySelectOntology(ont);
				}
				else {
					// for an entity, directly store updated result in cache
					annotationCache.putAnnotationSet(swoopModel.selectedOWLObject.getURI(), descSet);
				}
				this.displayAnnotations();
				
				//TESTING: swoopModel.setSelectedOntology(client.findAnnotationsOnt(aboutURI));
				
				statusLbl.setText("Status: Update Completed for " + swoopModel.shortForm(currentURI));
			}
			catch (Exception e) {
				statusLbl.setText("Status: ERROR retrieving annotations - see console log");
				e.printStackTrace();
			}
		}
		
		this.setCursor(currentCursor);
	}
	
	private void addAnnotationCache(URI key, Description value) {
		Set annotSet = new HashSet();
		if (annotationCache.getAnnotationSet(key)!=null) annotSet = annotationCache.getAnnotationSet(key);
		annotSet.add(value);
		annotationCache.putAnnotationSet(key, annotSet);
	}
	
	/**
	 * Add an annotation (Description object) to its appropriate
	 * location in the node tree (also consider reply)
	 *
	 */
	private void addAnnotationNodeToTree(Description newAnnot) {		
		// TODO add annotation at its appropriate location in node tree
	}
	
	/**
	 * Create the annotation node tree again by passing it
	 * a new Set of Description objects
	 * @param descSet
	 */
	private void setAnnotationNodeTree(Set descriptionSet, Set extraSet) {
		
		// get rootnode from annotation treetable
		JTree annotTree = annotTreeTable.getTree();
		DefaultMutableTreeNode rootNode = (DefaultMutableTreeNode) annotTree.getModel().getRoot();
		rootNode.removeAllChildren();
		htmlCore.setText("");
		annotAttachBtn.setEnabled(false);
		
		// sort descriptions based on "created" value
		SortedSet descSet = new TreeSet(DescriptionComparator.INSTANCE);
		descSet.addAll(descriptionSet);
		
		// populate node tree
		for (Iterator descIter = descSet.iterator(); descIter.hasNext();) {
			Description desc = (Description) descIter.next();
			//TESTING: System.out.println(desc.getAuthor()+" "+desc.getCreated()+" "+desc.getBody());
			DefaultMutableTreeNode annotNode = new DefaultMutableTreeNode(desc);
			rootNode.add(annotNode);
		}
		
		// sort extra based on "created" value
		if (extraSet.size()>0) {
			
			// add separator description
			rootNode.add(getSeparatorNode());
			
			SortedSet extraAnnotSet = new TreeSet(DescriptionComparator.INSTANCE);
			extraAnnotSet.addAll(extraSet);
			
			// add extra descriptions to node tree
			for (Iterator descIter = extraAnnotSet.iterator(); descIter.hasNext();) {
				Description desc = (Description) descIter.next();
				//TESTING: System.out.println(desc.getAuthor()+" "+desc.getCreated()+" "+desc.getBody());
				DefaultMutableTreeNode annotNode = new DefaultMutableTreeNode(desc);
				rootNode.add(annotNode);
			}
		}
		
		// expand all tree nodes
		for (int row=0; row<descSet.size(); row++)
			annotTree.expandRow(row);
		annotTree.updateUI();
		refreshUI();
	}
	
	/** Creates description object for the annotation based on 
	 *  values provided for author (dc:creator), date (dc:date),
	 *  annotates (element being annotated), type (comment/explanation..)
	 *  and body given as an html block
	 *  
	 *  Opens HTTP URL connections to all servers in AnnoteaServerPrefs.serverPList
	 *  and POSTs descriptions
	 *  (authentication popup appears where reqd) 
	 */ 
	private void postAnnotation() {
		
		// INITIALIZATION OF ANNOTEA DURING FIRST POST/UPDATE
		if (Annotea.INSTANCE==null) {
			Annotea.initializeAnnotea();
		}
				
		// create description that wraps annotation
		Description description = new Description();
		URI[] annotates = new URI[2];
		try {
			// get selected OWLObject to annotate			  
			annotates[0] = swoopModel.selectedOWLObject.getURI();			
			annotates[1] = swoopModel.getSelectedOntology().getURI();
			description.setAnnotates(annotates);
		} 
		catch (Exception e) {
			//e.printStackTrace();
			statusLbl.setText("Status: Post ERROR - need to select OWL ontology / entity");
			return;
		}
		String htmlBody = ekitCore.getTextPane().getText();
		
		// insert summary in <head> of htmlBody
		String summary = subjectFld.getText();
		if (!summary.equals("")) {
			htmlBody = htmlBody.substring(0, htmlBody.indexOf("<head>")+6) + summary + htmlBody.substring(htmlBody.indexOf("</head>"), htmlBody.length()); 
		}
		
//		htmlBody = stripHTML(htmlBody); // Aditya 09/03/05: Dont need this because its done in the CorrectedRDFRenderer while generating RDF/XML
		description.setBody(htmlBody);
		description.setBodyType("text/html");
		description.setAuthor(authorFld.getText());
		description.setCreated(dateFld.getText());
		description.setAnnotationType((OWLClass) typeBox.getSelectedItem());
		
		// set ontology change set, if any
		if (annotatedChangeSet!=null && annotatedChangeSet.size()>0) {
			description.setOntologyChangeSet(annotatedChangeSet);
		}
		
		// set current annotated entity definition (rendered as String in Swoop)
		// similar to addEntityToComparator in TermsDisplay
		String timeStamp = swoopModel.getTimeStamp();
		String renderText = "<font face=\"Verdana\" size=2><b>Time:</b>&nbsp;</i>"+timeStamp+"</i><br></font";
		URI ontURI;
		try {
			ontURI = swoopModel.getSelectedOntology().getURI();
			renderText += "<font face=\"Verdana\" size=2><b>Ontology:</b>&nbsp;" + swoopModel.shortForm(ontURI) + "</font><br>";
		} catch (OWLException e) {			
			e.printStackTrace();
		}
		if (swoopModel.selectedOWLObject instanceof OWLOntology) {
			int rendererIndex = swoopHandler.ontDisplay.ontDescTab.getSelectedIndex();
			JEditorPane renderer = (JEditorPane) swoopHandler.ontDisplay.editors.get(rendererIndex);
			renderText += renderer.getText();
		}
		else {
			int rendererIndex = swoopHandler.termDisplay.termDisplayPane.getSelectedIndex();
			JEditorPane renderer = (JEditorPane) swoopHandler.termDisplay.editors.get(rendererIndex);
			renderText += renderer.getText();
		}
		description.setAnnotatedEntityDefinition(renderText);
		
		// build a new ontology that stores description instance
		OWLOntology annotationOntology =  null;
		try {			
			annotationOntology = description.buildOntology();
		} catch (AnnoteaException e3) {
			e3.printStackTrace();
			return;
		}
		
		// TESTING:
//		StringWriter st = new StringWriter();
//		CorrectedRDFRenderer rdfRenderer = new CorrectedRDFRenderer(annotationOntology);
//		try {
//			rdfRenderer.renderOntology(annotationOntology, st);
//		} catch (RendererException ex) {
//			ex.printStackTrace();
//		}
//		try {
//			FileWriter temp = new FileWriter("temp.xml");
//			temp.write(st.toString());
//			temp.close();
//		}
//		catch (IOException exe) {
//			exe.printStackTrace();
//		}
//		System.out.println(st.toString());
		// END OF TESTING
		
		// connect to servers and post ontology
//	    Authenticator.setDefault (new MyAuthenticator());
		Iterator postIter = annotPrefs.serverPList.iterator();
		while (postIter.hasNext()) {
			// post message to server
			URL serverURL;
			try {
				serverURL = new URL(postIter.next().toString());
				AnnoteaClient client = new AnnoteaClient(serverURL, swoopModel);
				URL location = client.post(annotationOntology);
				description.setLocation(location);
				statusLbl.setText("Status: Annotation successfully posted to server");
			} catch (Exception e1) {
				e1.printStackTrace();
				statusLbl.setText("Status: Annotation post failed");
				return;
			}			
		}

		// hide popupNew and update
		popupNew.hide();
		clearBody();
//		 retrieveAnnotations(); // NO NEED
		// just add new annotation to existing annotation set
		try {			
			Set annotSet = annotationCache.getAnnotationSet(annotates[0]);
			if (annotSet==null) annotSet = new HashSet();
			annotSet.add(description);
			annotationCache.putAnnotationSet(annotates[0], annotSet);
			this.displayAnnotations();
			swoopModel.addAnnotatedObjectURI(annotates[0]);
		} 
		catch (Exception e1) {
			e1.printStackTrace();
		}
		
	}
	
	class MyAuthenticator extends Authenticator {
	    protected PasswordAuthentication getPasswordAuthentication() {
	      final JDialog jd = new JDialog ();
	      jd.setTitle("Authentication");
	      jd.setModal(true);
	      jd.getContentPane().setLayout (new GridLayout (0, 1));
	      JLabel jl = new JLabel (getRequestingPrompt());
	      jd.getContentPane().add(jl);
	      JTextField username = new JTextField();
	      username.setFont(tahoma);
	      //username.setBackground (Color.lightGray);
	      jd.getContentPane().add(createBox("User: ", username));
	      JPasswordField password = new JPasswordField();
	      password.setFont(tahoma);
	      //password.setBackground(Color.lightGray);
	      jd.getContentPane().add(createBox("Pwd: ", password));
	      JButton jb = new JButton ("OK");
	      JButton jc = new JButton("Cancel");
	      JPanel btnPanel = new JPanel();
	      btnPanel.setLayout(new GridLayout(1,2));
	      btnPanel.add(jb);
	      btnPanel.add(jc);
	      jd.getContentPane().add(btnPanel);
	      jb.addActionListener (new ActionListener() {
	        public void actionPerformed (ActionEvent e) {
	          jd.dispose();
	        }
	      });
	      jc.addActionListener (new ActionListener() {
	        public void actionPerformed (ActionEvent e) {
	        	showAuthenticator = false;
	        	jd.hide();
	        }
	      });
	      
	      jd.pack();
	      if (showAuthenticator) jd.setVisible(true);
	      return new PasswordAuthentication (username.getText(), password.getText().toCharArray());
	    }
	  }

	/**
	 * Whenever the annotation node tree selection changes
	 * render annotation body in html pane below
	 */
	public void valueChanged(TreeSelectionEvent e) {
		
		if (e.getSource()==annotTreeTable.getTree()) {
			// get selected tree node annotation object
			// and render body in htmlCore
			if (annotTreeTable.getTree().getSelectionPath()==null) return;
			DefaultMutableTreeNode selNode = (DefaultMutableTreeNode) annotTreeTable.getTree().getSelectionPath().getLastPathComponent();
			Description selAnnot = (Description) selNode.getUserObject();
			
			// check if its a blank/separator node i.e. no Annotation type
			if (selAnnot.getAnnotationType()==null) return;
			
			String html = "";
			String header = "<FONT FACE=\"Verdana\" SIZE=3>";
			
			URI annotURI = this.getAnnotationURI(selAnnot.getAnnotates());
			header += "<i>Annotation on: <a href=\""+annotURI+"\">"+swoopModel.shortForm(annotURI)+"</a><br>";
			
			// add saved entity definition string, if any
			if (selAnnot.getAnnotatedEntityDefinition()!=null) {
				String defn = selAnnot.getAnnotatedEntityDefinition();
				this.savedDefinitions.put(String.valueOf(defn.hashCode()), defn);
				header += "<br><a href=\"<SAVED^" + annotURI + "^"+ defn.hashCode() + "\">See Original Definition</a> (in Resource Holder) when Annotation was made";
			}
			
			header += "</i><hr><br>";
			header += "<b>Author: </b>" + selAnnot.getAuthor() + "<br>";
			try {
				String type = Annotea.getEntityName(selAnnot.getAnnotationType().getURI());
				header += "<b>Annotation Type: </b>" + type.substring(0, 2).toUpperCase() + "-" + type + "<br>";
			} catch (OWLException e1) {
				e1.printStackTrace();
			}
			header += "<b>Date Created: </b>" + selAnnot.getCreated() + "<br><br>";
			header += "</FONT>";
			html += header;
						
			String subject = this.getSubject(selAnnot);
			html += "<head><b>Subject:</b>"+subject+"</head>";
			String body = selAnnot.getBody();
			if (body!=null) {
				body = body.substring(body.indexOf("<body>"), body.length());
			}
			html += "<br><br>"+body;
			
			// add form processing
			// javascript handling
			jsHandler.clear();
			htmlCore.setText(html);
			htmlCore.setCaretPosition(0);
			
			if (selAnnot.getOntologyChangeSet()!=null && selAnnot.getOntologyChangeSet().size()>0) {
				annotAttachBtn.setEnabled(true);
				annotAttachBtn.setText("Changes");
//				annotAttachBtn.setText("["+selAnnot.getOntologyChangeSet().size()+"]");
			}
			else annotAttachBtn.setEnabled(false);

		}
	}

	public void SwoopSelectionChanged() {
		
		// if context fixed is selected, return
		if (fixContextChk.isSelected()) return;
		
		// save last selected annotation, and select it again at the end if present in treetable
		Description lastSelAnnot = null;
		if (annotTreeTable.getTree().getSelectionPath()!=null) {
			DefaultMutableTreeNode selNode = (DefaultMutableTreeNode) annotTreeTable.getTree().getSelectionPath().getLastPathComponent();
			lastSelAnnot = (Description) selNode.getUserObject();	
		}
		
		this.clearAnnotationNodeTree();
		htmlCore.setText("");
		annotAttachBtn.setEnabled(false);
		String status = "Status: Need to Update";
		if (swoopModel.selectedOWLObject!=null) {
			try {
				status += " for " + swoopModel.shortForm(swoopModel.selectedOWLObject.getURI());
			} catch (OWLException e) {
				e.printStackTrace();
			}
		}
		statusLbl.setText(status);
		
		// check annotation set cache
		if (swoopModel.selectedOWLObject!=null) {
			this.displayAnnotations();							
		}
		
		// if auto-retrieve on selection change is checked
		// and annotations not in cache
		if (swoopModel.getEnableAutoRetrieve()) {
			this.retrieveAnnotations();
		}
		
		// re-select lastSelAnnotation if possible
		if (lastSelAnnot!=null) {
			// cycle through tree table and select it
			DefaultMutableTreeNode rootNode = (DefaultMutableTreeNode) annotTreeTable.getTree().getModel().getRoot();
			DefaultMutableTreeNode tempNode = rootNode;
			while (tempNode!=null) {
				Description desc = (Description) tempNode.getUserObject();
				if (desc.equals(lastSelAnnot)) {
					annotTreeTable.getTree().setSelectionPath(new TreePath(tempNode.getPath()));
//					annotTreeTable.getTree().scrollPathToVisible(new TreePath(tempNode.getPath()));
					break;
				}
				tempNode = tempNode.getNextNode();
			}
		}
	}
	
	public void modelChanged(ModelChangeEvent event) {
		
		if (event.getType()==ModelChangeEvent.AUTORETRIEVE_CHANGED) {
			if (swoopModel.getEnableAutoRetrieve()) autoRetLbl.setText("(Auto-Update : ON, Ctrl-U to toggle setting)");
			else autoRetLbl.setText("(Auto-Update : OFF, Ctrl-U to toggle setting)");
			this.SwoopSelectionChanged();
		}
		else
		if (event.getType()==ModelChangeEvent.ONTOLOGY_SEL_CHANGED
			|| event.getType()==ModelChangeEvent.ENTITY_SEL_CHANGED
			|| event.getType()==ModelChangeEvent.ONTOLOGY_LOADED
			) {
			this.SwoopSelectionChanged();
		}
		else if (event.getType()==ModelChangeEvent.ONTOLOGY_REMOVED
			|| event.getType()==ModelChangeEvent.CLEAR_SELECTIONS) {
			this.clearAnnotationNodeTree();			
		}
		else
		if (event.getType() == ModelChangeEvent.ANNOTATION_CACHE_CHANGED) {
			// get swoop annotation cache
			this.annotationCache = swoopModel.getAnnotationCache();
			this.SwoopSelectionChanged();
		}
	}
	
	/**
	 * Remove annotation from Annotea Server
	 * @param desc - Description object to be deleted
	 */
	public void deleteAnnotation(Description desc) {
		try {
			statusLbl.setText("Status: Deleting Annotation");
			String title = "Delete Annotation";
			String msg = "Do you want to delete this annotation from the server?";
			int options = JOptionPane.YES_NO_OPTION;
			int result = JOptionPane.showConfirmDialog(this, msg, title, options);
			if(result==JOptionPane.YES_OPTION) {
				
				Cursor currentCursor = this.getCursor();
				this.setCursor(new Cursor(Cursor.WAIT_CURSOR));			
				
				URL location = desc.getLocation();
				//TODO: currently only deletes from default annotatea server, not generic case
				URL serverURL = new URL(annotPrefs.serverRList.iterator().next().toString());
				AnnoteaClient client = new AnnoteaClient(serverURL, swoopModel);
				client.delete(location);
				htmlCore.setText("");
				annotAttachBtn.setEnabled(false);
//				retrieveAnnotations(); // NO NEED
				// just remove existing annotation to existing annotation set
				try {			
					URI annotURI = this.getAnnotationURI(desc.getAnnotates());
					Set annotSet = annotationCache.getAnnotationSet(annotURI);
					if (annotSet!=null) annotSet.remove(desc);
					else annotSet = new HashSet();
					annotationCache.putAnnotationSet(annotURI, annotSet);
					this.displayAnnotations();
					if (annotSet.size()==0) {
						swoopModel.getAnnotatedObjectURIs().remove(annotURI);
					}
				} 
				catch (Exception e1) {
					e1.printStackTrace();
				}
				finally {
					this.setCursor(currentCursor);
				}
			}			
		}
		catch (Exception ex) {
			statusLbl.setText("Status: Delete ERROR - see console log");
			ex.printStackTrace();
		}
	}
	
	/**
	 * Update Popup New Annotation frame with the current date
	 *
	 */
	public void updatePopupDate() {
		Calendar cal = Calendar.getInstance(TimeZone.getDefault());
	    String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
	    java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat(DATE_FORMAT);
	    sdf.setTimeZone(TimeZone.getDefault());                    
	    dateFld.setText(sdf.format(cal.getTime()));
	}

	public void hyperlinkUpdate(HyperlinkEvent e) {

//		if (e.getSource()==ekitCore.getTextPane() || 
//			e.getSource()==htmlCore) {
			
			String hLink = e.getDescription();
			
			//DISABLE TOOLTIPS BECAUSE OF DOUBLE CLICK MESSAGE
//			if (isURI(hLink)) {
//				if (e.getSource()==ekitCore.getTextPane()) ekitCore.getTextPane().setToolTipText(hLink);
//				// else htmlCore.setToolTipText(hLink);
//			}
//			else if (hLink.startsWith("ESC")) {
//				// escape Swoop and open link in web browser
//				htmlCore.setToolTipText(hLink.substring(3, hLink.length()));
//			}
			
			if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
				
				if (isURI(hLink)) {
					try {
						URI uri = new URI(hLink);
						
						try {
							// if entity already displayed, return
							if (uri.equals(swoopModel.selectedOWLObject.getURI())) return;
						} catch (OWLException e2) {
							e2.printStackTrace();
						}
						
						if (swoopModel.getOntologyURIs().contains(uri)) {
							// select ontology in main swoop UI
							swoopHandler.ontDisplay.selectOntology(swoopModel.getOntology(uri));
						}
						else {
							swoopHandler.termDisplay.selectEntity(hLink);
						}
						
					} 
					catch (URISyntaxException e1) {						
						e1.printStackTrace();
					}					
				}
				else if (hLink.startsWith("<ESC")) {					
					// open in a standard web browser?
					hLink = hLink.substring(3, hLink.length());
					BrowserControl.displayURL(hLink);
				}
				else if (hLink.startsWith("<SAVED")) {
					// link points to saved entity defn
					String annotURI = hLink.substring(hLink.indexOf("^")+1, hLink.lastIndexOf("^"));
					String hashCode = hLink.substring(hLink.lastIndexOf("^")+1, hLink.length());
					String defn = this.savedDefinitions.get(hashCode).toString();
					try {
						// add saved defn to resource holder!
						swoopHandler.termDisplay.comparator.addEntity("Original Definition of Annotated Entity", new URI(annotURI), defn);
					} catch (URISyntaxException e1) {
						e1.printStackTrace();
					}
				}
			}
		//}
	}
	
	public boolean isURI(String str) {
		try {
			URL uri = new URL(str);			
		}
		catch (Exception ex) {
			return false;
		}
		return true;
	}

	public void mouseClicked(MouseEvent e) {
		
		// maximize i.e. popup new window with annotation message
		// when user double clicks on htmlCore
		if (e.getSource()==htmlCore) {
			
			// System.out.println(e.getButton()+":"+e.getClickCount());
			
			if (e.getButton()==1 && e.getClickCount()==2) {
				JFrame maxAnnot = new JFrame("Annotation Message");
				Container content = maxAnnot.getContentPane();
				content.setLayout(new BorderLayout());
				//JEditorPane maxPane = new JEditorPane();
				maxPane = new HTMLPane();

				maxPane.addActionListener(this);
				
				// javascript handler
				JavaScriptHandler maxJSHandler = new JavaScriptHandler();
				maxPane.addTag("javascript", maxJSHandler);
				
				maxPane.setContentType("text/html");
				maxPane.setEditable(false);
				maxPane.addHyperlinkListener(this);
				maxPane.setText(htmlCore.getText());
				maxPane.setCaretPosition(0);
				content.add(new JScrollPane(maxPane), "Center");
				maxAnnot.setSize(600, 600);
				maxAnnot.setLocation(100, 100);
				maxAnnot.show();
			}
		}
	}

	public void mousePressed(MouseEvent arg0) {
	}

	public void mouseReleased(MouseEvent arg0) {
	}

	public void mouseEntered(MouseEvent e) {		
	}

	public void mouseExited(MouseEvent arg0) {
	}

	public void keyTyped(KeyEvent arg0) {
	}

	public void keyPressed(KeyEvent e) {
		
		if (e.getSource()==ekitCore.getTextPane()) {
			// user presses CTRL-L in Ekit pane, then
			// auto insert hyperlink corresponding to last word
			if (e.getKeyCode()==76 && e.getModifiers()==2) {
				Document doc = ekitCore.getTextPane().getDocument();
				String docText = "";
				try {
					docText = doc.getText(0, doc.getLength());
					int caretPos = ekitCore.getTextPane().getCaretPosition();
					
					String txt = docText.substring(0, caretPos);
					StringTokenizer tokens = new StringTokenizer(txt, " ");
					String lastWord = "";
					while (tokens.hasMoreTokens()) {
						lastWord = tokens.nextToken();
					}
					//TESTING: 
					// System.out.println(lastWord);
					// weird hack to remove funny character present in some strings (is inserted when two hyperlinkable words follow each other)
					int funnyCharIndex = -1;
					for (int i=0; i<lastWord.length(); i++) {
						if (lastWord.charAt(i)==160) {
							funnyCharIndex = i;
						}
					}
					if (funnyCharIndex!=-1) lastWord = lastWord.substring(funnyCharIndex+1, lastWord.length());
					//** end of weird hack
					
					OWLOntology ont = swoopModel.getSelectedOntology();
					String uriLink = "";
					Set allURIs = OntologyHelper.allURIs(ont);
					for (Iterator iter = allURIs.iterator(); iter.hasNext(); ) {
						String uri = iter.next().toString();
						if (uri.endsWith("#"+lastWord) || uri.endsWith("/"+lastWord)) {
							uriLink = uri;
							break;
						}							
					}
					// System.out.println(uriLink);
					if (uriLink.equals("")) {
						// return; // NO MATCHING URI FOUND
						// check if lastWord a valid URI
						if (this.isURI(lastWord)) {
							uriLink = "<ESC"+lastWord; //ESC is used to skip Swoop and open link in standard web browser
						}
						else return;
					}
					
					String insertLink = " <a href=\""+uriLink+"\">" + lastWord + "</a>&nbsp;";
					String htmlText = ekitCore.getTextPane().getText();
					htmlText = htmlText.replaceAll("&#160;", " ");
					htmlText = htmlText.replaceAll(" "+ lastWord, insertLink);
					ekitCore.getTextPane().setText(htmlText);
				} 
				catch (Exception e1) {
					e1.printStackTrace();
				}				
			}
		}
		
	}
	
	/**
	 * Return a set of Annotations (descriptions) on all Annotated OWLEntities
	 * present in the OWLOntology ont passed to it as an argument 
	 * @param ont
	 * @return
	 */
	private Set getAllEntityAnnotations(OWLOntology ont, OWLEntity skip) {
		Set entityDescSet = new HashSet();
		try {
			Set annotObjURIs = swoopModel.getAnnotatedObjectURIs();
			for (Iterator iter = annotObjURIs.iterator(); iter.hasNext();) {
				URI uri = (URI) iter.next();
				if ((ont.getClass(uri)!=null || ont.getDataProperty(uri)!=null ||
						ont.getObjectProperty(uri)!=null || ont.getIndividual(uri)!=null) 
					&& (skip==null || !uri.equals(skip.getURI())))
				{
					entityDescSet.addAll(new HashSet(annotationCache.getAnnotationSet(uri)));
				}
				
			}			
		}
		catch (OWLException ex) {
			ex.printStackTrace();
		}
		return entityDescSet;
	}
	
	/**
	 * Refresh the display of the JTreetable containing Annotations
	 * *from the annotation cache*. Take into account whether displayAll
	 * checkbox is selected or not.
	 */
	private void displayAnnotations() {
		
		try {
			// get main annotations on current selected object in Swoop
			OWLNamedObject currObj = swoopModel.selectedOWLObject;
			
			Set mainSet = new HashSet();
			if (currObj!=null && annotationCache.getAnnotationSet(currObj.getURI())!=null) {
				mainSet = new HashSet(annotationCache.getAnnotationSet(currObj.getURI()));
			}
			
			// get annotations on ontology
			Set extraSet = new HashSet();
			if (displayAll.isSelected()) {				
				OWLOntology ont = swoopModel.getSelectedOntology();
				if (ont!=null) {
					if (annotationCache.getAnnotationSet(ont.getURI())!=null) extraSet = new HashSet(annotationCache.getAnnotationSet(ont.getURI()));
					OWLEntity skip = null;
					if (!currObj.getURI().equals(ont.getURI())) skip = (OWLEntity) currObj;
					else extraSet = new HashSet(); // don't include ontology annotations in extraSet while viewing ont
					extraSet.addAll(this.getAllEntityAnnotations(ont, skip));
				}
			}
			
			// finally call setAnnotationNodeTree to display tree
			this.setAnnotationNodeTree(mainSet, extraSet);			
		}
		catch(OWLException ex) {
			ex.printStackTrace();
		}		
	}
	
	public void keyReleased(KeyEvent arg0) {
	}
	
	private DefaultMutableTreeNode getSeparatorNode() {
		Description separator = new Description();
		separator.setAuthor("-----------");
		separator.setBody("----------------------------");
		separator.setCreated("------");
		return new DefaultMutableTreeNode(separator);		 
	}
	
	private URI getAnnotationURI(URI[] annotates) {
		return annotates[0];		
	}
	
	/**
	 * Normalized annotates so that actual OWLNamedObject being annotated
	 * is present in annotates[0] and Ontology is in annotates[1]
	 * Also, if ontology *itself* was annotated, annotates[1] = null
	 * 
	 * @param descSet - set of Description objects
	 * @return
	 */
	private void normalizeAnnotates(Set descSet) {
		
		if (descSet!=null) {
			for (Iterator iter = descSet.iterator(); iter.hasNext();) {
				try {
					Description desc = (Description) iter.next();
					URI[] annotates = desc.getAnnotates();
					if (annotates.length>1 && annotates[1]!=null && annotates[0]!=null) {
						if (!annotates[0].equals(annotates[1]) && annotates[0].equals(swoopModel.getSelectedOntology().getURI())) {
							// swap the two
							URI swap = new URI(annotates[0].toString());
							annotates[0] = new URI(annotates[1].toString());
							annotates[1] = swap; 
						}
					}
					else {
						// Tricky! Suppose annotation is on ontology
						// then only one annotates value is returned							
						// System.out.println(desc);
					}
					desc.setAnnotates(annotates);
				}
				catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		}
	}
}
