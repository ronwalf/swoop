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

package org.mindswap.swoop.utils;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Timer;

import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.border.EtchedBorder;

import org.mindswap.swoop.Swoop;
import org.mindswap.swoop.SwoopFrame;
import org.mindswap.swoop.SwoopModel;

/**
 * @author Aditya Kalyanpur
 *
 */
public class SwoopPreferences extends JDialog {

	SwoopFrame swoopHandler;
	SwoopModel swoopModel;
	JRadioButton srcPref1, srcPref2, srcPref3, srcPref4;  
	Font tahoma = new Font("Tahoma", Font.PLAIN, 12);
    Font tahomaB = new Font("Tahoma", Font.BOLD, 12);
    JLabel langLbl;
    JButton saveBtn, cancelBtn;
	JCheckBox iconChk, divChk, codeSelChk, savePtDiskChk, pointChk, logChangesChk, autoRetChk, openLastChk, 
				viewOWLVocabularyAsRDFChk, viewRDFVocabularyAsRDFChk,
				useLabelChk, useProxyChk,
				saveWkspcChk;
	JRadioButton rdfxmlRadio, javaRadio;
	JTextField countFld, userNameFld, proxyHostFld, proxyPortFld, saveWkspcFileFld, saveWkspcTimeFld;
	JComboBox fontsBox, fontSizeBox, reasonerBox, langBox;
	
	public SwoopPreferences(SwoopFrame handler, SwoopModel swoopModel) {
		
		this.swoopHandler = handler;
		this.swoopModel = swoopModel;
		setModal(true);
		setupUI();
		loadPreferences();
	}
	
	private String getFontSizeDisplay() {
		switch (Integer.parseInt(swoopModel.getFontSize())) {
			case 1: return "Very Small";
			case 2: return "Small"; 
			case 3: return "Medium";
			case 4: return "Large";
			case 5: return "Very Large";
		}
		return "Medium";
	}
	
	public void loadPreferences() {
		
		// update UI based on current preference values
		iconChk.setSelected(swoopModel.getShowIcons()); 
		divChk.setSelected(swoopModel.getShowDivisions());
		
		fontSizeBox.setSelectedItem(this.getFontSizeDisplay());
		fontsBox.setSelectedItem(swoopModel.getFontFace());
		countFld.setText(" "+swoopModel.getTreeThreshold()+" ");
		codeSelChk.setSelected(swoopModel.isHighlightCode());
		useLabelChk.setSelected(swoopModel.getUseLabels());
		boolean found = false;
		for (int i=0; i<langBox.getItemCount(); i++) {
			String l = langBox.getItemAt(i).toString();
			if (l.startsWith(swoopModel.getUseLanguage())) {
				langBox.setSelectedIndex(i);
				found = true;
				break;
			}
		}
		if (!found) {
			langBox.addItem(swoopModel.getUseLanguage());
			langBox.setSelectedItem(swoopModel.getUseLanguage());
		}
		
		langLbl.setEnabled(useLabelChk.isSelected());
		langBox.setEnabled(useLabelChk.isSelected());
		
		HashMap reasMap = swoopModel.getReasonerMap();
		reasonerBox.removeAllItems();
		for (Iterator iter = reasMap.keySet().iterator(); iter.hasNext();) {
			String name = iter.next().toString();
			reasonerBox.addItem(name);			
		}
		reasonerBox.setSelectedItem(swoopModel.getDefaultReasoner().getName());
		
		// load general settings
		userNameFld.setText(swoopModel.getUserName());
		openLastChk.setSelected(swoopModel.isOpenLastWorkspace());
		saveWkspcChk.setSelected(swoopModel.isAutoSaveWkspc());
		saveWkspcFileFld.setText(swoopModel.getSaveWkspcFile());
		saveWkspcTimeFld.setText(String.valueOf(swoopModel.getSaveWkspcTime()));
		useProxyChk.setSelected(swoopModel.getUseHTTPProxy());
		proxyHostFld.setEnabled(swoopModel.getUseHTTPProxy());
		proxyPortFld.setEnabled(swoopModel.getUseHTTPProxy());
		proxyHostFld.setText(swoopModel.getProxyHost());
		proxyPortFld.setText(swoopModel.getProxyPort());
		logChangesChk.setSelected(swoopModel.isLogChanges());
		pointChk.setEnabled(logChangesChk.isSelected());
		pointChk.setSelected(swoopModel.getEnableAutoSaveChkPts());
		if (Swoop.isWebStart()) savePtDiskChk.setEnabled(false);
		savePtDiskChk.setSelected(swoopModel.isSaveCheckPtsDisk());
		autoRetChk.setSelected(swoopModel.getEnableAutoRetrieve());
		viewOWLVocabularyAsRDFChk.setSelected( swoopModel.isViewOWLVocabularyAsRDF() );
		viewRDFVocabularyAsRDFChk.setSelected( swoopModel.isViewRDFVocabularyAsRDF() );
		
		if (swoopModel.getChangeSharingMethod()==SwoopModel.RDFXML_SER) rdfxmlRadio.setSelected(true);
		else if (swoopModel.getChangeSharingMethod()==SwoopModel.JAVA_SER) javaRadio.setSelected(true);
	}
	
	private void savePreferences() {
		
		// save UI prefs
		swoopModel.setShowIcons(iconChk.isSelected(), true);
		swoopModel.setShowDivisions(divChk.isSelected());
		String size = String.valueOf(fontSizeBox.getSelectedIndex()+1);
		swoopModel.setFontFace(fontsBox.getSelectedItem().toString(), false);
		if (size.equals("")) size = "2";
		if (Integer.parseInt(size)<=0) size = "2";
		swoopModel.setFontSize(size);
		String count = countFld.getText().trim();
		if (count.equals("")) count = "200";
		if (Integer.parseInt(count)<=0) count = "200";
		swoopModel.setTreeThreshold(count);
		swoopModel.setHighlightCode(codeSelChk.isSelected());
		swoopModel.setUseLabels(useLabelChk.isSelected());
		if (langBox.getSelectedIndex() >=0) {
		  String langFld = langBox.getSelectedItem().toString();
		  if (langFld.indexOf("(")>=0) langFld = langFld.substring(0, langFld.indexOf("(")).trim();
		  swoopModel.setUseLanguage(langFld);
		}
		
		// save general prefs
		swoopModel.setUserName(userNameFld.getText());
		swoopModel.setOpenLastWorkspace(openLastChk.isSelected());
		swoopModel.setAutoSaveWkspc(saveWkspcChk.isSelected());
		swoopModel.setSaveWkspcFile(saveWkspcFileFld.getText());
		float time = Float.parseFloat(saveWkspcTimeFld.getText());
		swoopModel.setSaveWkspcTime(time);
		swoopModel.setHTTPProxy(useProxyChk.isSelected(), proxyHostFld.getText(), proxyPortFld.getText());
		swoopModel.setDefaultReasoner(swoopModel.getReasonerMap(reasonerBox.getSelectedItem().toString()));
		swoopModel.setLogChanges(logChangesChk.isSelected());
		swoopModel.setEnableAutoSaveChkPts(pointChk.isSelected(), true);
		swoopModel.setSaveCheckPtsDisk(savePtDiskChk.isSelected());
		swoopModel.setEnableAutoRetrieve(autoRetChk.isSelected());
		swoopModel.setViewOWLVocabularyAsRDF( viewOWLVocabularyAsRDFChk.isSelected() );
		swoopModel.setViewRDFVocabularyAsRDF( viewRDFVocabularyAsRDFChk.isSelected() );
		
		if (rdfxmlRadio.isSelected()) swoopModel.setChangeSharingMethod(SwoopModel.RDFXML_SER);
		else swoopModel.setChangeSharingMethod(SwoopModel.JAVA_SER);
		
		// also save preferences to disk!
		swoopModel.savePreferences();
	}
	
	private void setupUI() {
		Container content = getContentPane();
		content.setLayout(new BorderLayout());
		
		// setup UI options panel
		JPanel uiPanel = new JPanel();
		uiPanel.setLayout(new BorderLayout());		
		JLabel uiPrefLbl = new JLabel(" UI Options");		
		uiPrefLbl.setFont(tahomaB);
		uiPanel.add(uiPrefLbl, "North");
		iconChk = new JCheckBox("Show Icons in Entity Pane (Ctrl-I)");
		iconChk.setFont(tahoma);
		divChk = new JCheckBox("Show Divisions in Entity Pane (Ctrl-D)");
		divChk.setFont(tahoma);
		JLabel fontSizeLbl = new JLabel(" Rendered Text Font Size: ");
		fontSizeLbl.setFont(tahoma);
		JPanel sizePanel = new JPanel();
		sizePanel.setLayout(new BorderLayout());
		sizePanel.add(fontSizeLbl, "Center");
		fontSizeBox = new JComboBox();
		fontSizeBox.setFont(tahoma);
		fontSizeBox.addItem("Very Small");
		fontSizeBox.addItem("Small");
		fontSizeBox.addItem("Medium");
		fontSizeBox.addItem("Large");
		fontSizeBox.addItem("Very Large");
		sizePanel.add(fontSizeBox, "East");
		fontsBox = new JComboBox();
		fontsBox.addItem("Ariel");
		fontsBox.addItem("Comic Sans MS");
		fontsBox.addItem("Courier New");
		fontsBox.addItem("Sans Serif");
		fontsBox.addItem("Tahoma");
		fontsBox.addItem("Times New Roman");
		fontsBox.addItem("Verdana");
		fontsBox.setFont(tahoma);
		JPanel fontFacePanel = new JPanel();
		fontFacePanel.setLayout(new BorderLayout());
		fontFacePanel.add(fontsBox, "Center");
		JLabel fontFaceLbl = new JLabel("   Font Face: ");
		fontFaceLbl.setFont(tahoma);
		fontFacePanel.add(fontFaceLbl, "West");
		JPanel fontPanel = new JPanel();
		fontPanel.setLayout(new GridLayout(1,2));
		fontPanel.add(sizePanel);
		fontPanel.add(fontFacePanel);
		
		JLabel countLbl = new JLabel(" Expand Class/Property Tree fully when entity count is below this (Default 200)");
		countLbl.setFont(tahoma);
		JPanel countPanel = new JPanel();
		countPanel.setLayout(new BorderLayout());
		countFld = new JTextField();
		countFld.setFont(tahoma);		
		countPanel.add(countFld, "West");
		countPanel.add(countLbl, "Center");
		codeSelChk = new JCheckBox("Highlight code fragment in Source Pane corresponding to entity");
		codeSelChk.setFont(tahoma);
		useLabelChk = new JCheckBox("Display entities using rdfs:label instead of their URI if possible (defaults to URI)");
		useLabelChk.setFont(tahoma);
		langBox = new JComboBox();
		langBox.setEditable(true);
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
		langBox.addItem("tr (Turkish)");
		langBox.setSelectedIndex(0);
		JPanel langPanel = new JPanel();
		langPanel.setLayout(new GridLayout(1,2));
		langLbl = new JLabel("          Select language to display labels:");
		langLbl.setFont(tahoma);
		langPanel.add(langLbl);
		langPanel.add(langBox);
		langLbl.setEnabled(false);
		langBox.setEnabled(false);
		
		JPanel uigrpPanel = new JPanel();
		uigrpPanel.setLayout(new GridLayout(8,1));		
		uigrpPanel.add(iconChk);
		uigrpPanel.add(divChk);
		uigrpPanel.add(fontPanel);
		uigrpPanel.add(countPanel);
		uigrpPanel.add(codeSelChk);
		uigrpPanel.add(useLabelChk);
		uigrpPanel.add(langPanel);
		uiPanel.add(uigrpPanel, "Center");
		uiPanel.setBorder(new EtchedBorder());
		
		// setup general panel
		JPanel genPanel = new JPanel();
		genPanel.setLayout(new BorderLayout());		
		JLabel verPrefLbl = new JLabel(" Feature Options");		
		verPrefLbl.setFont(tahomaB);
		genPanel.add(verPrefLbl, "North");
		userNameFld = new JTextField("");
		userNameFld.setFont(tahoma);
		JLabel nameLbl = new JLabel(" Current Username: ");
		nameLbl.setFont(tahoma);
		JLabel nameLbl2 = new JLabel(" (for tracking authorship of changes etc.)");
		nameLbl2.setFont(tahoma);
		JPanel namePanel = new JPanel();
		namePanel.setLayout(new BorderLayout());
		namePanel.add(nameLbl, "West");
		namePanel.add(userNameFld, "Center");
		namePanel.add(nameLbl2, "East");
		openLastChk = new JCheckBox("Start SWOOP with the last opened Workspace");
		openLastChk.setFont(tahoma);
		JPanel saveWkspcPanel = new JPanel();
		saveWkspcPanel.setLayout(new BorderLayout());
		JPanel saveWkspcPanelL = new JPanel();
		saveWkspcPanelL.setLayout(new BorderLayout());
		JPanel saveWkspcPanelR = new JPanel();
		saveWkspcPanelR.setLayout(new BorderLayout());
		saveWkspcChk = new JCheckBox("Auto Save Workspace");
		saveWkspcChk.setFont(tahoma);
		saveWkspcPanelL.add(saveWkspcChk, "West");
		saveWkspcFileFld = new JTextField();
		saveWkspcPanelL.add(saveWkspcFileFld, "Center");		
		JLabel everyLbl = new JLabel("every");
		everyLbl.setFont(tahoma);
		saveWkspcPanelL.add(everyLbl, "East");
		saveWkspcPanel.add(saveWkspcPanelL, "Center");
		saveWkspcTimeFld = new JTextField();
		JLabel minsLbl = new JLabel("mins");
		minsLbl.setFont(tahoma);
		saveWkspcPanelR.add(saveWkspcTimeFld, "Center");
		saveWkspcPanelR.add(minsLbl, "East");
		saveWkspcPanel.add(saveWkspcPanelR, "East");
		
		JLabel reasLbl = new JLabel(" Default SWOOP Reasoner: ");
		reasLbl.setFont(tahoma);
		reasonerBox = new JComboBox();
		reasonerBox.setFont(tahoma);
		JPanel reasPanel = new JPanel();
		reasPanel.setLayout(new GridLayout(1,3));
		reasPanel.add(reasLbl);
		reasPanel.add(reasonerBox);
		reasPanel.add(new JLabel(""));
		logChangesChk = new JCheckBox("Log (Committed) Ontology Changes");
		logChangesChk.setFont(tahoma);
		pointChk = new JCheckBox("Auto-save Ontology Checkpoints");
		pointChk.setFont(tahoma);
		savePtDiskChk = new JCheckBox("Save checkpoints to disk (in /SWOOP/checkpoints/)");
		savePtDiskChk.setFont(tahoma);
//		savePtDiskChk.setEnabled(false);
		Box verPanel = Box.createHorizontalBox();
		verPanel.add(logChangesChk);
		verPanel.add(pointChk);
		autoRetChk = new JCheckBox("Auto-retrieve (Annotea) Annotations upon SWOOP selection change (Ctrl-U)");
		autoRetChk.setFont(tahoma);
		JLabel shareChanges = new JLabel("  Share Annotea Change Sets by serializing.. ");
		rdfxmlRadio = new JRadioButton("RDF/XML");
		javaRadio = new JRadioButton("Java Objects");		
		ButtonGroup group = new ButtonGroup();
		group.add(rdfxmlRadio);
		group.add(javaRadio);
		shareChanges.setFont(tahoma);
		rdfxmlRadio.setFont(tahoma);
		javaRadio.setFont(tahoma);
		JPanel changePanel = new JPanel();
		changePanel.setLayout(new BorderLayout());
		changePanel.add(shareChanges, "Center");
		JPanel radioPane = new JPanel();
		radioPane.setLayout(new GridLayout(1,2));
		radioPane.add(rdfxmlRadio);
		radioPane.add(javaRadio);
		changePanel.add(radioPane, "East");
		useProxyChk = new JCheckBox("Use HTTP Proxy");
		useProxyChk.setFont(tahoma);
		proxyHostFld = new JTextField();
		proxyHostFld.setFont(tahoma);
		proxyPortFld = new JTextField();
		proxyPortFld.setFont(tahoma);
		JLabel proxyHostLbl = new JLabel("Host:");
		proxyHostLbl.setFont(tahoma);
		JLabel proxyPortLbl = new JLabel("Port:");
		proxyPortLbl.setFont(tahoma);
		JPanel proxyPanel1 = new JPanel();
		proxyPanel1.setLayout(new BorderLayout());
		proxyPanel1.add(proxyHostLbl, "West");
		proxyPanel1.add(proxyHostFld, "Center");
		JPanel proxyPanel2 = new JPanel();
		proxyPanel2.setLayout(new BorderLayout());
		proxyPanel2.add(proxyPortLbl, "West");
		proxyPanel2.add(proxyPortFld, "Center");
		JPanel proxyPanel = new JPanel();
		proxyPanel.setLayout(new GridLayout(1,3));
		proxyPanel.add(useProxyChk);
		proxyPanel.add(proxyPanel1);
		proxyPanel.add(proxyPanel2);
		
		JPanel generalPanel = new JPanel();
		generalPanel.setLayout(new GridLayout(9,1));
		generalPanel.add(namePanel);
		generalPanel.add(openLastChk);
		generalPanel.add(saveWkspcPanel);
		generalPanel.add(proxyPanel);
		generalPanel.add(reasPanel);
		generalPanel.add(verPanel);
		generalPanel.add(savePtDiskChk);
		generalPanel.add(autoRetChk);
//		generalPanel.add(changePanel); not needed anymore		
		genPanel.add(generalPanel, "Center");
		genPanel.setBorder(new EtchedBorder());
		
		// setup standard vocabulary view panel

		JLabel vocabPrefLbl = new JLabel(" Standard Vocabulary View Options");
		vocabPrefLbl.setFont(tahomaB);
		viewOWLVocabularyAsRDFChk  = new JCheckBox("View OWL vocabulary as RDF");
		viewOWLVocabularyAsRDFChk.setFont(tahoma);
		viewRDFVocabularyAsRDFChk = new JCheckBox("View RDF/RDFS vocabulary as RDF");
		viewRDFVocabularyAsRDFChk.setFont(tahoma);
		
		JPanel vocabViewPanel = new JPanel();		
		vocabViewPanel.setLayout( new BorderLayout() );
		JPanel checkboxesPanel = new JPanel();
		checkboxesPanel.setLayout( new GridLayout(2, 1));
		checkboxesPanel.add( viewOWLVocabularyAsRDFChk );
		checkboxesPanel.add( viewRDFVocabularyAsRDFChk );
		vocabViewPanel.setBorder(new EtchedBorder());

		vocabViewPanel.add( vocabPrefLbl, BorderLayout.NORTH);
		vocabViewPanel.add( checkboxesPanel, BorderLayout.CENTER);
		
		// setup final top panel
		JPanel topPanel = new JPanel();
		//topPanel.setLayout(new GridLayout(3,1));
		topPanel.setLayout( new BorderLayout() );
		JPanel orgPanel1 = new JPanel();
		JPanel orgPanel2 = new JPanel();
		JPanel orgPanel3 = new JPanel();
		orgPanel1.setLayout( new BorderLayout());
		orgPanel2.setLayout( new BorderLayout());
		orgPanel3.setLayout( new BorderLayout());
		
		orgPanel1.add( uiPanel, BorderLayout.NORTH);
		orgPanel2.add( genPanel, BorderLayout.NORTH);
		orgPanel3.add( vocabViewPanel, BorderLayout.NORTH);
		
		orgPanel1.add(orgPanel2, BorderLayout.CENTER);
		orgPanel2.add(orgPanel3, BorderLayout.CENTER);
		
		topPanel.add(orgPanel1);
		
		// setup button panel
		JPanel btnPanel = new JPanel();
		saveBtn = new JButton("Save");
		saveBtn.setFont(tahoma);
		cancelBtn = new JButton("Cancel");
		cancelBtn.setFont(tahoma);
		btnPanel.add(saveBtn);
		btnPanel.add(cancelBtn);
		
		content.add(topPanel, "Center");
		content.add(btnPanel, "South");
		
		setTitle("SWOOP Preferences");
		
		setSize(500, 550);
		setLocation(150, 100);
		
		// add action listeners
		logChangesChk.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				pointChk.setEnabled(logChangesChk.isSelected());
			}			
		});
		saveBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				savePreferences();
				dispose();
			}			
		});
		cancelBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				dispose();
			}	
		});
		useLabelChk.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				langLbl.setEnabled(useLabelChk.isSelected());
				langBox.setEnabled(useLabelChk.isSelected());
			}			
		});
		useProxyChk.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				proxyHostFld.setEnabled(useProxyChk.isSelected());
				proxyPortFld.setEnabled(useProxyChk.isSelected());
			}			
		});
	}
}
