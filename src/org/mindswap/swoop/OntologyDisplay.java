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

package org.mindswap.swoop;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.StringWriter;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.SpringLayout;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.mindswap.swoop.popup.PopupAddAnnotation;
import org.mindswap.swoop.popup.PopupAddOntology;
import org.mindswap.swoop.popup.PopupNew;
import org.mindswap.swoop.renderer.SwoopEditableRenderer;
import org.mindswap.swoop.renderer.SwoopOntologyRenderer;
import org.mindswap.swoop.renderer.SwoopOptionalRenderer;
import org.mindswap.swoop.renderer.ontology.OntologyListRenderer;
import org.mindswap.swoop.renderer.ontology.SwoopOntologyInfo;
import org.mindswap.swoop.renderer.ontology.SwoopSpeciesValidationRenderer;
import org.mindswap.swoop.utils.DavUtil;
import org.mindswap.swoop.utils.PluginLoader;
import org.mindswap.swoop.utils.SwoopStatistics;
import org.mindswap.swoop.utils.owlapi.CorrectedRDFRenderer;
import org.mindswap.swoop.utils.ui.ExceptionDialog;
import org.mindswap.swoop.utils.ui.OntologyComparator;
import org.mindswap.swoop.utils.ui.SpringUtilities;
import org.mindswap.swoop.utils.ui.SwingWorker;
import org.mindswap.swoop.utils.ui.SwoopIcons;
import org.mindswap.swoop.utils.ui.SwoopProgressDialog;
import org.semanticweb.owl.io.RendererException;
import org.semanticweb.owl.model.OWLAnnotationInstance;
import org.semanticweb.owl.model.OWLDisjointClassesAxiom;
import org.semanticweb.owl.model.OWLException;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.change.AddImport;
import org.semanticweb.owl.model.change.OntologyChange;
import org.semanticweb.owl.model.change.RemoveAnnotationInstance;
import org.semanticweb.owl.model.change.RemoveImport;
import org.semanticweb.owl.model.helper.OntologyHelper;

public class OntologyDisplay extends SwoopDisplayPanel implements
		ActionListener, ListSelectionListener, SwoopModelListener,
		HyperlinkListener, ChangeListener, MouseListener {

	/*
	 * Global UI objects
	 */
	JButton hideOntBtn;

	boolean editorEnabled, reasonerMode;

	Font tahoma = new Font("Tahoma", Font.PLAIN, 11);

	JCheckBox ontEditableChk;

	JList ontList;

	public JTabbedPane ontDescTab;

	JPopupMenu popupMenu;

	JComboBox ontHideBox;

	JScrollPane ontListPane;

	JToolBar ontToolBar;

	String webDavURI = "http://www.mindswap.org/dav/ontologies/";

	String webDavLogin = "", webDavPwd = "";

	/*
	 * Important public fields
	 */
	SwoopModel swoopModel; // model shared by all swoop components

	public SwoopFrame swoopHandler; // handler for SwoopFrame instance

	SwoopOntologyInfo species; // used to get OWL Species Validation

	/*
	 * A list of renderers associated with each tab. The list contains
	 * SwoopentityRenderer objects and the index of renderers correspond to
	 * index of tabs
	 */
	List renderers = new ArrayList();

	/*
	 * A list of renderers associated with each tab. The list contains
	 * JEditorPane objects and the index of editors correspond to index of tabs
	 */
	public List editors = new ArrayList();

	// constructor
	public OntologyDisplay(SwoopModel model, SwoopFrame swf) {

		this.swoopModel = model;
		this.swoopHandler = swf;
		editorEnabled = false;
		reasonerMode = false;
		species = new SwoopOntologyInfo();

		setupUI();
	}

	public void setupUI() {

		// panel for ontology list header
		JLabel ontListLbl = new JLabel(" Ontology List");
		ontListLbl.setFont(tahoma);
		SwoopIcons swoopIcons = new SwoopIcons();
		if (swoopIcons.upIcon != null)
			hideOntBtn = new JButton((ImageIcon) swoopIcons.upIcon);
		else
			hideOntBtn = new JButton("Collapse");
		hideOntBtn.setToolTipText("Collapse Ontology List");
		hideOntBtn.setFont(tahoma);
		hideOntBtn.setBorder(null);
		hideOntBtn.addActionListener(this);

		// create toolbar for ontology list pane
		ontToolBar = new JToolBar();
		ontToolBar.add(hideOntBtn);
		ontToolBar.add(ontListLbl);

		// create row panel for ontology list and description pane
		JSplitPane splitpane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		splitpane.setOneTouchExpandable(true);

		ontList = new JList();
		ontList.setCellRenderer(new OntologyListRenderer(swoopModel));
		ontList.setFont(new Font(swoopModel.getFontFace(), Font.PLAIN, 11));
		ontListPane = new JScrollPane(ontList);

		/*
		 * create ontology combo box, which is hidden by default but appears
		 * when collapse is clicked
		 */
		ontHideBox = new JComboBox();
		ontHideBox.setFont(new Font(swoopModel.getFontFace(), Font.PLAIN, 11));
		ontHideBox.setEditable(false);
		ontHideBox.setRenderer(new OntologyListRenderer(swoopModel));

		ontDescTab = new JTabbedPane();
		ontDescTab.setFont(tahoma);
		// *** critical UI stuff: add change listener after adding tabs

		if (!Swoop.isWebStart()) {
			PluginLoader ph = PluginLoader.getInstance();
			// find all the other classes that implements SwoopOntologyRenderer
			// interface and try to generate a renderer for them
			List list = ph.getClasses(SwoopOntologyRenderer.class);
			for (int i = 0; i < list.size(); i++) {
				Class cls = (Class) list.get(i);
				if (cls.isInterface())
					continue;
				SwoopOntologyRenderer renderer = null;

				// if is abstract class, don't try to instantiate. see JVM specs
				// Table 4.1:
				// (http://java.sun.com/docs/books/vmspec/2nd-edition/html/ClassFile.doc.html#75734)
				if ((cls.getModifiers() >> 10) != 1) {
					try {
						System.out.println("Try creating renderer "
								+ cls.getName());
						// if
						// (cls.getName().equals("org.mindswap.swoop.SwoopSpeciesValidator"))
						// continue;
						renderer = (SwoopOntologyRenderer) cls.newInstance();
						renderers.add(renderer);
					} catch (Throwable e) {
						e.printStackTrace();
						continue;
					}
				}
			}
		} else {
			// manually load Ontology Renderers for the webstart
			renderers.add(new SwoopOntologyInfo());
			renderers.add(new SwoopSpeciesValidationRenderer());
		}

		// sort the renderers such that SwoopOntologyInfo is always first and
		// the
		// rest is ordered alphabetically
		Collections.sort(renderers, new Comparator() {
			public int compare(Object o1, Object o2) {
				SwoopOntologyRenderer r1 = (SwoopOntologyRenderer) o1;
				SwoopOntologyRenderer r2 = (SwoopOntologyRenderer) o2;
				if (r1 instanceof SwoopOntologyInfo)
					return -1;
				if (r2 instanceof SwoopOntologyInfo)
					return 1;
				return r1.getName().compareTo(r2.getName());
			}
		});

		// add the editor for each renderer
		for (int i = 0; i < renderers.size(); i++) {
			SwoopOntologyRenderer renderer = (SwoopOntologyRenderer) renderers
					.get(i);
			Component component = renderer.getDisplayComponent(this);
			ontDescTab.addTab(renderer.getName(), new JScrollPane(component));
			if (renderers.get(i) instanceof SwoopOptionalRenderer)
				ontDescTab.setEnabledAt(i, false);
			editors.add(component);
		}

		JPanel chkPanel = new JPanel();
		chkPanel.setLayout(new BorderLayout());
		ontEditableChk = new JCheckBox("Editable");
		ontEditableChk.setFont(tahoma);
		ontEditableChk.addActionListener(this);

		JPanel rightOntPanel = new JPanel();
		rightOntPanel.setLayout(new BorderLayout());
		rightOntPanel.add(ontDescTab, "Center");
		splitpane.setRightComponent(rightOntPanel);

		// add components to main panel
		setLayout(new BorderLayout());
		add(splitpane, "Center");

		// add listeners at the end of setup!
		ontList.addListSelectionListener(this);
		ontList.addMouseListener(this);
		ontHideBox.addActionListener(this);
		ontHideBox.addMouseListener(this);
		ontDescTab.addChangeListener(this);

		refreshOntList();
	}

	public void disableUIListeners() {
		// for (int safe=0; safe<5; safe++) {
		ontList.removeListSelectionListener(this); // change ontList selection
		// causes rendering ontology
		// info
		ontHideBox.removeActionListener(this);
		// }
	}

	public void enableUIListeners() {

		// below is a safety mechanism to ensure that multiple copies of
		// listener aren't added
		this.disableUIListeners();

		// turn on listeners
		ontList.addListSelectionListener(this);
		ontHideBox.addActionListener(this);
	}

	/**
	 * Useful method for selecting an OWL Ontology without activating any of the
	 * listeners. Simply refreshes UI of the list and selects ontology
	 * (updateUI() throws funky exceptions)
	 * 
	 * @param ont
	 */
	public void simplySelectOntology(OWLOntology ont) {

		this.disableUIListeners();
		refreshOntList();
		if (ont != null)
			ontList.setSelectedValue(ont, true);
		else
			ontList.clearSelection();
		refreshOntBox();
		ontHideBox.setSelectedItem(ont);
		this.enableUIListeners();
	}

	public void refreshOntList() {

		// display ontology list based on swoopModel's ontologies
		Set sortedOntSet = new TreeSet(OntologyComparator.INSTANCE);
		sortedOntSet.addAll(swoopModel.getOntologies());
		ontList.setFont(new Font(swoopModel.getFontFace(), Font.PLAIN, 11));
		ontList.setListData(sortedOntSet.toArray());
		// we don't need the following line, it is causing an error
		// ontList.updateUI();
	}

	/**
	 * Refresh ontology combo box each time an ontology is added/removed from
	 * SwoopModel
	 */
	public void refreshOntBox() {
		ontHideBox.removeAllItems();
		ontHideBox.setFont(new Font(swoopModel.getFontFace(), Font.PLAIN, 11));
		for (int i = 0; i < ontList.getModel().getSize(); i++) {
			OWLOntology ont = (OWLOntology) ontList.getModel().getElementAt(i);
			ontHideBox.addItem(ont);
		}
	}

	public void modelChanged(ModelChangeEvent event) {

		if (event.getType() == ModelChangeEvent.ONT_STATS_CHANGED) {
			renderOntology();
		} else if (event.getType() == ModelChangeEvent.QNAME_VIEW_CHANGED) {
			// System.out.println("OntologyDisplay: QName changed to: " +
			// swoopModel.showQNames());
			displayOntology();
		} else if (event.getType() == ModelChangeEvent.ONTOLOGY_LOADED) {

			// refresh ontology list/box
			this.disableUIListeners();
			refreshOntList();
			refreshOntBox();
			this.enableUIListeners();

			// add ontology element to history as well
			if (swoopModel.getSelectedOntology() != null)
				swoopHandler.termDisplay.addToHistory(swoopModel
						.getSelectedOntology());

			// finally, revert UI ontology settings (qnames, imports, reasoner)
			swoopHandler.termDisplay.revertUIOntologySettings();
		} else if (event.getType() == ModelChangeEvent.ONTOLOGY_REMOVED) {

			this.simplySelectOntology(null);
		} else if (event.getType() == ModelChangeEvent.CLEAR_SELECTIONS) {

			// clear the entire ontology display pane
			ontList.clearSelection();
			ontHideBox.setSelectedIndex(-1);
			for (int i = 0; i < ontDescTab.getTabCount(); i++) {
				JEditorPane currEditor = (JEditorPane) editors.get(i);
				currEditor.setText("");
			}
		} else if (event.getType() == ModelChangeEvent.ONTOLOGY_SEL_CHANGED
				|| event.getType() == ModelChangeEvent.REASONER_SEL_CHANGED) {
			// in this case, don't need to loadOntSettings
			// because it has already been loaded in
			// swoopModel.setSelectedOntology that fires ONT_SEL_CHANGED
			if (swoopModel.selectedEntity != null) {
				// ?? still need to refresh ontology info pane when entity is
				// selected
				renderOntology();
			}
			if (swoopModel.selectedOntology != null)
				swoopHandler.updateOntologyViews();
		} else if (event.getType() == ModelChangeEvent.ONTOLOGY_CHANGED) {
			this.disableUIListeners();
			ontList.repaint();
			ontHideBox.repaint();
			this.enableUIListeners();
			// changes may fix inconsistencies or may introduce new ones
			// so update the onotlogy dusplay
			displayOntology();
		} else if (event.getType() == ModelChangeEvent.EDITABLE_CHANGED) {
			System.out.println("editable changed");
			refreshEditorMode();
		} else if (event.getType() == ModelChangeEvent.ADDED_CHANGE) {
			refreshEditorMode();
		} else if (event.getType() == ModelChangeEvent.DEBUGGING_CHANGED) {
			this.renderOntology();
		}
	}

	/**
	 * Refresh the ontology display whenever "editable" changes in SwoopModel
	 */
	public void refreshEditorMode() {
		editorEnabled = swoopModel.getEditorEnabled();
		displayOntology();
	}

	public void actionPerformed(ActionEvent e) {

		if (e.getSource() == ontEditableChk) {
			refreshEditorMode();
		}

		if (e.getSource() == hideOntBtn) {
			if (hideOntBtn.getToolTipText().indexOf("Expand") >= 0) {
				swoopHandler.expandOntPanel();
				SwoopIcons swoopIcons = new SwoopIcons();
				if (swoopIcons.upIcon != null)
					hideOntBtn.setIcon((ImageIcon) swoopIcons.upIcon);
				else
					hideOntBtn.setText("Collapse");
				hideOntBtn.setToolTipText("Collapse Ontology List");
			} else {
				swoopHandler.collapseOntPanel();
				SwoopIcons swoopIcons = new SwoopIcons();
				if (swoopIcons.downIcon != null)
					hideOntBtn.setIcon((ImageIcon) swoopIcons.downIcon);
				else
					hideOntBtn.setText("Expand");
				hideOntBtn.setToolTipText("Expand Ontology List");
			}
		}

		if (e.getSource() == ontHideBox
				&& e.getActionCommand().equals("comboBoxChanged")) {
			// select corresponding ontology
			OWLOntology ont = (OWLOntology) ontHideBox.getSelectedItem();
			selectOntology(ont);
		}
	}

	/**
	 * Reload an OWL Ontology from its physical location (URL). Remove the
	 * existing entry from SwoopModel and reload/add the new one
	 * 
	 * @param ont
	 */
	public void reloadOntologyFromPhysical(OWLOntology ont) {
		try {
			String msg = "Reloading Ontology from its physical URL - all changes will be lost! Continue?";
			int result = JOptionPane.showConfirmDialog(this, msg,
					"Reload Ontology", JOptionPane.YES_NO_OPTION);

			if (result == JOptionPane.YES_OPTION) {
				// reload ontology ** from its physical location **
				URI physicalURI = ont.getPhysicalURI();
				swoopModel.removeOntology(ont.getURI());
				// also remove ontology from cache (tree/list)
				swoopHandler.termDisplay.removeFromCache(ont);
				// add new ontology and select it
				swoopModel.addOntology(physicalURI);
				OWLOntology onto = swoopModel.getOntology(ont.getURI());
				swoopModel.setSelectedOntology(onto);
				// remove changes associated with old ontology
				swoopModel.removeChanges(onto);
				System.out.println("Ontology reloaded from " + physicalURI);
			}

		} catch (OWLException ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * Save Ontology to a Remote Location (Server) that has WebDAV enabled
	 * Ontology is stored at location as an RDF/XML file
	 */
	public void saveRemoteOntology() {
		try {
			// get current ontology to save
			OWLOntology ontology = swoopModel.getSelectedOntology();
			URI uri = ontology.getPhysicalURI();
			if (!uri.getScheme().matches("https?")) {
				uri = ontology.getLogicalURI();
			}
			if (!uri.getScheme().matches("https?")) {
				uri = URI.create(webDavURI);
			}
			uri = uri.normalize();
			uri = new URI(uri.getScheme(), uri.getSchemeSpecificPart(), null);
			final String uriFinal = uri.toString();
			JTextField urlField = new JTextField(uriFinal);
			/* Add browse Web Dav button */
//			JButton browse = new JButton("Browse..");
//			browse.addActionListener(new ActionListener() {
//				public void actionPerformed(ActionEvent e) {
//					BrowserControl.displayURL(uriFinal);
//				}
//			});
			JPanel urlPanel = new JPanel();
			urlPanel.setLayout(new BorderLayout());
			urlPanel.add(urlField, "Center");
			//urlPanel.add(browse, "East");
			JTextField userField = new JTextField(webDavLogin);
			JTextField passwordField = new JPasswordField(webDavPwd);
			JPanel panel = new JPanel(new SpringLayout());
			panel.add(new JLabel("URL"));
			panel.add(urlPanel);
			panel.add(new JLabel("User"));
			panel.add(userField);
			panel.add(new JLabel("Password"));
			panel.add(passwordField);
			SpringUtilities.makeCompactGrid(panel, 3, 2, // rows, cols
					6, 6, // initX, initY
					6, 6); // xPad, yPad

			int option = JOptionPane.showConfirmDialog(null, new Object[] {
					"Server must have accept HTTP PUT or have WebDAV enabled.", panel },
					"Save to WebDAV Store", JOptionPane.OK_CANCEL_OPTION,
					JOptionPane.PLAIN_MESSAGE);
			if (option != JOptionPane.OK_OPTION)
				return;

			String url = urlField.getText().trim();
			if (url == null)
				return;
			if (url.indexOf("/") >= 0)
				webDavURI = url.substring(0, url.lastIndexOf("/") + 1);

			webDavLogin = userField.getText().trim();
			webDavPwd = passwordField.getText().trim();

			// write rdf or html depending on extension
			String output = "";
			if (url.endsWith(".html")) {
				output = swoopHandler.getObjectHTML(swoopModel.selectedOWLObject);
			} else {
				StringWriter rdfBuffer = new StringWriter();
				CorrectedRDFRenderer rdfRenderer = new CorrectedRDFRenderer();
				rdfRenderer.renderOntology(ontology, rdfBuffer);
				output = rdfBuffer.toString();
			}
			
			
			// write output to webDav location
			if (DavUtil.saveString(output, url, webDavLogin, webDavPwd)) {
				System.out.println("Ontology saved to " + url);
				JOptionPane.showMessageDialog(this, "Ontology Saved at " + url,
						"Remote Export Success",
						JOptionPane.INFORMATION_MESSAGE);
			} else {
				System.out.println("Error saving ontology to " + url);
				JOptionPane.showMessageDialog(this,
						"Unable to Save Ontology Remotely at " + url,
						"Remote Export Error", JOptionPane.ERROR_MESSAGE);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void addNewOntology() {
		try {
			PopupNew popupPanel = new PopupNew(this, swoopModel, "Ontology",
					null);
			popupPanel.setLocation(200, 200);
			popupPanel.show();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * Remove an ontology and its imports closure (if specified) from Swoop
	 * 
	 * @param ont
	 */
	public void removeOntology(OWLOntology ont, boolean warning) {

		try {
			int result = -1;
			JCheckBox impChk = new JCheckBox(
					"Also remove all its imported ontologies (if any)?");

			if (warning) {
				String title = "Remove OWL Ontology";
				String ontName = swoopModel.shortForm(ont.getURI());
				if (ontName.indexOf(":") >= 0)
					ontName = ontName.substring(ontName.indexOf(":") + 1,
							ontName.length());
				String msg = "Do you want to remove this ontology ("
						+ (ontName) + ") from SWOOP?";
				JPanel msgPanel = new JPanel();
				msgPanel.setLayout(new GridLayout(2, 1));
				msgPanel.add(new JLabel(msg));
				msgPanel.add(impChk);
				int options = JOptionPane.YES_NO_OPTION;
				result = JOptionPane.showConfirmDialog(this, msgPanel, title,
						options);
			}

			if (result == JOptionPane.YES_OPTION || !warning) {
				Set ontURISet = new HashSet();
				if (impChk.isSelected()) {
					for (Iterator iter = OntologyHelper.importClosure(ont)
							.iterator(); iter.hasNext();) {
						swoopModel.removeOntology(((OWLOntology) iter.next())
								.getURI());
					}
				} else
					swoopModel.removeOntology(ont.getURI());
			}
		} catch (Exception ex) {

		}
	}

	/***************************************************************************
	 * Doesn't call SwoopModel Ontology_Sel_Changed Event Instead directly calls
	 * updateOntologyDisplay
	 */
	public void valueChanged(ListSelectionEvent event) {

		if (event.getValueIsAdjusting())
			return; // multiple notification calls

		if (event.getSource() == ontList) {

			// save settings for current ontology
			swoopModel.saveCurrentOntSettings();

			// check for ont list selection
			if (ontList.getSelectedValue() != null) {
				OWLOntology ont = (OWLOntology) ontList.getSelectedValue();
				swoopModel.selectedOWLObject = ont;
				swoopModel.selectedOntology = ont;
				swoopModel.selectedEntity = null;

				// add ontology element to history as well
				if (ont != null)
					swoopHandler.termDisplay.addToHistory(ont);

				// whenever ontology selection changes, need to revert
				// to user-specific ontology settings
				// *** below also reclassifies ontology!
				swoopModel.loadOntSettings(ont);

				// updateOntologyViews() refreshes ontology (via
				// od.displayOntology()->od.renderOntology())
				// swoopHandler.updateOntologyViews();
				swoopHandler.ontRemoveMItem.setEnabled(true);
			} else {
				swoopHandler.ontRemoveMItem.setEnabled(false);
			}
			swoopHandler.changeLog.refreshChangePane();
		}
	}

	public void selectOntology(OWLOntology ont) {
		ontList.clearSelection();
		ontList.setSelectedValue(ont, true);
		this.disableUIListeners();
		ontHideBox.setSelectedItem(ont);
		this.enableUIListeners();
	}

	/**
	 * Refreshes the Ontology Display Pane whenever the currently selected
	 * ontology in SwoopModel is changed Does not add ontology to History, that
	 * is only done in valueChanged (when ontList selection changes)
	 */
	protected void displayOntology() {

		System.out.println("Display ontology info");
		OWLOntology ont = swoopModel.getSelectedOntology();
		if (ont == null) {
			System.out.println(" but no ontology selected");
			return;
		}

		swoopHandler.ontRemoveMItem.setEnabled(true);

		try {
			// now that ontology is loaded we can update the address bar
			swoopHandler.disableUIListeners();
			swoopHandler.updateAddressBar(ont.getURI().toString());
		} catch (OWLException e) {
			e.printStackTrace();
		} finally {
			swoopHandler.enableUIListeners();
		}

		if ((ontList.getSelectedIndex() != -1)
				&& (!((OWLOntology) ontList.getSelectedValue()).equals(ont))
				|| (ontHideBox.getSelectedIndex() != -1 && !((OWLOntology) ontHideBox
						.getSelectedItem()).equals(ont))) {
			// sometimes when traversing hyperlinks, user reaches element in
			// another ontology
			// element displayed but ontology is not selected in list
			// this fragment corrects the mismatch b/w
			// swoopModel.selectedOntology and ontList.selection
			this.simplySelectOntology(ont);
			// System.out.println("mismatch corrected");
		}

		for (int index = 0; index < renderers.size(); index++) {
			SwoopOntologyRenderer renderer = (SwoopOntologyRenderer) renderers
					.get(index);
			StringWriter sw = new StringWriter();
			JEditorPane editorPane = (JEditorPane) editors.get(index);

			if (renderer instanceof SwoopOptionalRenderer)
				ontDescTab.setEnabledAt(index,
						((SwoopOptionalRenderer) renderer)
								.isVisible(swoopModel));
			else if (renderer instanceof SwoopSpeciesValidationRenderer) {
				// special check for validator - always make it enabled
				ontDescTab.setEnabledAt(index, true);
			} else if (renderer instanceof SwoopEditableRenderer) {
				SwoopEditableRenderer editableRenderer = (SwoopEditableRenderer) renderer;
				editableRenderer
						.setEditorEnabled(swoopModel.getEditorEnabled());
			} else if (swoopModel.getEditorEnabled())
				ontDescTab.setEnabledAt(index, false);
			else
				ontDescTab.setEnabledAt(index, true);

			// if(ont == null || !ontDescTab.isEnabledAt(index)) continue;
		}

		renderOntology();

	}

	/**
	 * This method is called by displayOntology while refreshing ontology pane.
	 * It renders the currently selected OWL Ontology by calling the render()
	 * method on the appropriate SwoopOntologyRenderer
	 * 
	 */
	public void renderOntology() {

		if (ontDescTab.getSelectedIndex() == -1 || renderers.size() == 0
				|| editors.size() == 0)
			return; // during initialization
		OWLOntology ont = swoopModel.getSelectedOntology();
		SwoopOntologyRenderer renderer = (SwoopOntologyRenderer) renderers
				.get(ontDescTab.getSelectedIndex());
		JEditorPane editorPane = (JEditorPane) editors.get(ontDescTab
				.getSelectedIndex());

		if (ont == null) {
			// *** below is critical to avoid weird HTML exception ***
			editorPane.setText("<html><body><br/></body></html>");
			editorPane.setCaretPosition(0);
			return;
		}

		StringWriter sw = new StringWriter();
		try {
			renderer.render(ont, swoopModel, sw);
			String html = sw.getBuffer().toString();
			if (html.length() < 20) {
				System.out.println("HTML:");
				System.out.println(html); // error checking
			} else {
				System.out.println("HTML Length: " + html.length());
			}

			editorPane.setText(html);
			editorPane.setCaretPosition(0);
		} catch (RendererException e) {
			editorPane.setText("");
			JOptionPane.showMessageDialog(null,
					"An error occured during display:\n" + e.getMessage(),
					"Error!", JOptionPane.ERROR_MESSAGE);
		}
	}

	public boolean isURL(String str) {
		try {
			URL uri = new URL(str);
		} catch (Exception ex) {
			return false;
		}
		return true;
	}

	public void hyperlinkUpdate(HyperlinkEvent event) {
		String hLink = event.getDescription();
		JEditorPane sourceRend = (JEditorPane) event.getSource();

		if (isURL(hLink))
			sourceRend.setToolTipText(hLink);
		else
			sourceRend.setToolTipText("");

		// if hyperlink is clicked
		if (event.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
			// if hyperlink is URI
			if (isURI(hLink)) {

				try {
					URI uri = new URI(hLink);
					OWLOntology ont = swoopModel.getOntology(uri);
					if (ont != null) {
						ontHideBox.removeActionListener(this);
						ontList.setSelectedValue(ont, true);
						ontHideBox.setSelectedItem(ont);
						ontHideBox.addActionListener(this);
					} else {
						swoopHandler.termDisplay.selectEntity(hLink);
					}
				} catch (Exception e) {
					System.out.println(e.getMessage());
				}
			} else if (hLink.startsWith("<EditURL")) {
				// edit the url of the ontology!
				editOntologyURL(true);
			} else if (hLink.startsWith("<AddO-IMP")) {
				// add imports to an ontology
				addImports();
			} else if (hLink.startsWith("<AddO-ANN")) {
				// add annotation to an ontology
				PopupAddAnnotation popup = new PopupAddAnnotation(swoopModel,
						true);
				popup.setLocation(200, 200);
				popup.show();
			}
			// else
			// if (hLink.startsWith("<RESTAT")) {
			// // recompute stats for ontology
			// swoopModel.removeOntStats(swoopModel.selectedOntology);
			// // refresh display which will cause a recompute
			// this.renderOntology();
			// }
			else if (hLink.startsWith("<PSTAT:")) {
				// display prop stats
				String statKey = hLink.substring(hLink.indexOf(":") + 1, hLink
						.length());
				HashMap statMap = swoopModel
						.getOntStats(swoopModel.selectedOntology);
				Set statVal = new HashSet();
				if (statKey.equals(SwoopStatistics.MAX_DEPTH_PROP_TREE)
						|| statKey.equals(SwoopStatistics.MIN_DEPTH_PROP_TREE)) {
					List list = (List) statMap.get(statKey);
					statVal = (Set) list.get(1);
				} else
					statVal = (HashSet) statMap.get(statKey); // prop attribs
				// or multiple
				// inherited
				// props

				try {
					swoopHandler.termDisplay.lookupPanel.printResults(
							swoopModel.selectedOntology, new HashSet(),
							statVal, new HashSet(), statKey, swoopModel
									.shortForm(swoopModel.selectedOntology
											.getURI()));
				} catch (OWLException e) {
					e.printStackTrace();
				}
			} else if (hLink.startsWith("<CSTAT:")) {
				// display class stats
				String statKey = hLink.substring(hLink.indexOf(":") + 1, hLink
						.length());
				HashMap statMap = swoopModel
						.getOntStats(swoopModel.selectedOntology);
				Set statVal = new HashSet();

				// if set of disjoint axioms, expand to get classes in set
				if (statKey.equals(SwoopStatistics.NO_DISJOINT)) {
					Set disjAxioms = (HashSet) statMap.get(statKey);
					for (Iterator iter = disjAxioms.iterator(); iter.hasNext();) {
						OWLDisjointClassesAxiom axiom = (OWLDisjointClassesAxiom) iter
								.next();
						try {
							statVal.addAll(axiom.getDisjointClasses());
						} catch (OWLException e1) {
							e1.printStackTrace();
						}
					}
				} else if (statKey.equals(SwoopStatistics.MAX_DEPTH_CLASS_TREE)
						|| statKey.equals(SwoopStatistics.MIN_DEPTH_CLASS_TREE)
						|| statKey.equals(SwoopStatistics.MAX_BRANCHING_FACTOR)
						|| statKey.equals(SwoopStatistics.MIN_BRANCHING_FACTOR)) {
					List list = (List) statMap.get(statKey);
					statVal = (Set) list.get(1);
				} else
					statVal = (HashSet) statMap.get(statKey); // multiple
				// inherited
				// classes

				try {
					swoopHandler.termDisplay.lookupPanel.printResults(
							swoopModel.selectedOntology, statVal,
							new HashSet(), new HashSet(), statKey, swoopModel
									.shortForm(swoopModel.selectedOntology
											.getURI()));
				} catch (OWLException e) {
					e.printStackTrace();
				}
			} else if (hLink.startsWith("<DLEXP:")) {
				// display DL expressivity
				String express = hLink.substring(hLink.indexOf(":") + 1, hLink
						.length());
				JEditorPane ep = new JEditorPane();
				ep.setEditable(false);
				ep.setContentType("text/html");
				ep.setText(express);
				JOptionPane.showMessageDialog(this, new JScrollPane(ep));
			} else if (hLink.startsWith("<Undo")) {
				try {
					List changes = new ArrayList(swoopModel
							.getUncommittedChanges());

					System.out.println("Undo " + hLink);
					// parse Undo hyper-link
					int pos = hLink.lastIndexOf(":") + 1;
					int hashCode = Integer.parseInt(hLink.substring(pos, hLink
							.length()));
					int index = 0;
					while (index < changes.size()) {
						if (changes.get(index).hashCode() == hashCode)
							break;
						index++;
					}
					OntologyChange removeChange = (OntologyChange) changes
							.get(index);
					System.out.println("Undo " + index + " of "
							+ changes.size() + " " + removeChange);
					changes.remove(removeChange);

					swoopModel.setUncommittedChanges(new ArrayList(changes));

					swoopHandler.termDisplay.applyChangesBtn
							.setEnabled(swoopModel.getUncommittedChanges()
									.size() > 0);
					swoopHandler.termDisplay.undoChangesBtn
							.setEnabled(swoopModel.getUncommittedChanges()
									.size() > 0);

					this.refreshEditorMode();
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			} else if (hLink.startsWith("<Delete")) {
				handleDeleteLink(hLink);
			}
		}

	}

	/**
	 * Add a Remove* OntologyChange when the user clicks on the "Delete"
	 * hyperlink in the SwoopOntologyInfo pane. Get the components required for
	 * the change from the hashCode stored in the hLink and the current selected
	 * Ontology
	 * 
	 * @param hLink
	 */
	private void handleDeleteLink(String hLink) {

		try {
			// parse DELETE hyper-link
			int pos1 = hLink.indexOf("-");
			int pos2 = hLink.lastIndexOf(":");
			String hashCode = hLink.substring(pos2 + 1, hLink.length());
			String titleCode = hLink.substring(pos1 + 1, pos2);
			SwoopOntologyInfo ontInfoRend = (SwoopOntologyInfo) renderers
					.get(0);
			Object obj = ontInfoRend.OWLObjectHash.get(hashCode);

			if (titleCode.equals("IMP")) {
				// Remove Imports Change
				OWLOntology currOnt = swoopModel.getSelectedOntology();
				OWLOntology impOnt = (OWLOntology) obj;
				RemoveImport change = new RemoveImport(currOnt, impOnt, null);
				swoopModel.addUncommittedChange(change);
			} else if (titleCode.equals("ANN")) {
				// Remove Annotation Instance
				OWLOntology currOnt = swoopModel.getSelectedOntology();
				if (obj instanceof OWLAnnotationInstance) {
					OWLAnnotationInstance oai = (OWLAnnotationInstance) obj;
					RemoveAnnotationInstance change = new RemoveAnnotationInstance(
							currOnt, currOnt, oai.getProperty(), oai
									.getContent(), null);
					swoopModel.addUncommittedChange(change);
				}
			}

			swoopHandler.termDisplay.applyChangesBtn.setEnabled(swoopModel
					.getUncommittedChanges().size() > 0);
			swoopHandler.termDisplay.undoChangesBtn.setEnabled(swoopModel
					.getUncommittedChanges().size() > 0);

			displayOntology();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * Load an Ontology into Swoop when its specified in a AddImports change and
	 * the ontology doesn't currently exist in Swoop. Called by addImports(..)
	 * 
	 * @param ontURI
	 */
	private void loadImport(URI ontURI) {

		System.out.println("Load imported uri " + ontURI.toString());
		final URI uri = ontURI;
		final SwoopProgressDialog progress = new SwoopProgressDialog(swoopHandler,
				"Loading new ontology...");
		progress.show();
		final OWLOntology currentOntology = swoopModel.getSelectedOntology();
		SwingWorker worker = new SwingWorker() {
			OWLOntology importOnt = null;

			Exception error = null;

			public Object construct() {

				try {
					if (swoopModel.getOntology(uri) != null) {
						importOnt = swoopModel.getOntology(uri);
					} else {
						importOnt = swoopModel.addOntology(uri);
					}
				} catch (Exception e) {
					error = e;
				}
				return null;
			}

			public void finished() {
				progress.dispose();
				if (error != null) {
					ExceptionDialog.createDialog(swoopHandler, "Can't import ontology", error).show();
					return;
				}
				
				AddImport change = new AddImport(currentOntology, importOnt, null);
				swoopModel.addUncommittedChange(change);
				swoopHandler.termDisplay.applyChangesBtn.setEnabled(swoopModel
						.getUncommittedChanges().size() > 0);
				swoopHandler.termDisplay.undoChangesBtn.setEnabled(swoopModel
						.getUncommittedChanges().size() > 0);

				displayOntology();
				// change.accept((ChangeVisitor) currOnt);
				// swoopModel.setSelectedOntology(currOnt);

			}
		};
		worker.start();

	}

	/**
	 * AddImports change to an OWL Ontology, Suppose the ontology to be imported
	 * is not in Swoop, it calls loadImport(ont) to load this ontology into
	 * Swoop.
	 */
	public void addImports() {
		try {
			// add imports
			PopupAddOntology popup = new PopupAddOntology(swoopModel);
			popup.setLocation(200, 200);
			popup.show();
			URI ontURI = popup.ontologyURI;
			if (ontURI != null)
				loadImport(ontURI);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void stateChanged(ChangeEvent e) {

		if (e.getSource() == ontDescTab) {
			renderOntology();
		}

	}

	/**
	 * Edit the logical URL of the Ontology i.e. replace the base URI's for all
	 * entities in the ontology. Warning - use carefully as lots of references
	 * may be lost (eg. from external ontology)!
	 */
	public void editOntologyURL(boolean displayWarning) {

		if (displayWarning) {
			String title = "Change URL";
			String msg = "You are going to change the logical URI of the ontology. All entity references in the ontology will be modified. Continue?";
			int options = JOptionPane.YES_NO_OPTION;
			int result = JOptionPane.showConfirmDialog(this, msg, title,
					options);
			if (result == JOptionPane.NO_OPTION)
				return;
		}
		try {
			OWLOntology currOnt = swoopModel.selectedOntology;
			String ontURI = currOnt.getURI().toString();
			String newURI = JOptionPane.showInputDialog(this,
					"Specify New Logical URI:", ontURI
			// "Changing Ontology URI",
					// JOptionPane.PLAIN_MESSAGE
					);
			if (newURI != null && isURI(newURI)) {
				swoopModel.renameOWLObject(currOnt, ontURI, ontURI, newURI,
						true);
			} else {
				JOptionPane.showMessageDialog(this,
						"Invalid URI Error: Operation could not be completed.",
						"Error", JOptionPane.ERROR_MESSAGE);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private boolean isURI(String uriStr) {
		try {
			URI uri = new URI(uriStr);
		} catch (Exception ex) {
			return false;
		}
		return true;
	}

	public void mouseClicked(MouseEvent e) {

		// if mouse clicked over ontList, display ontology pane in swoop frame
		if (e.getSource() == ontList || e.getSource() == ontHideBox) {
			this.disableUIListeners();
			swoopHandler.displayOntologyPane();
			// displayOntology();
			// swoopHandler.changeLog.ontRadio.setSelected(true);
			// swoopHandler.changeLog.setScope(ChangeLog.ONTOLOGY_SCOPE);
			swoopHandler.annotRenderer.SwoopSelectionChanged();

			// add ontology element to history as well
			if (swoopModel.getSelectedOntology() != null) {
				swoopHandler.termDisplay.addToHistory(swoopModel
						.getSelectedOntology());

				try {
					// now that ontology is loaded we can update the address bar
					swoopHandler.disableUIListeners();
					swoopHandler.updateAddressBar(swoopModel
							.getSelectedOntology().getURI().toString());
				} catch (OWLException e1) {
					e1.printStackTrace();
				} finally {
					swoopHandler.enableUIListeners();
				}
			}
			this.enableUIListeners();
		}
	}

	public void mousePressed(MouseEvent arg0) {
	}

	public void mouseReleased(MouseEvent arg0) {
	}

	public void mouseEntered(MouseEvent arg0) {
	}

	public void mouseExited(MouseEvent arg0) {
	}

}