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
import java.awt.Container;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URI;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JScrollPane;

import org.mindswap.swoop.utils.ui.JTabbedPaneWithCloseIcons;
import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLEntity;
import org.semanticweb.owl.model.OWLIndividual;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLProperty;

/**
 * @author Aditya Kalyanpur
 *
 */
public class SwoopSearch extends JFrame {
	
	SwoopModel swoopModel;
	TermsDisplay termHandler;
	//Font tahoma = new Font("Tahoma", Font.PLAIN, 11);
        Font tahoma = new Font("SansSerif", Font.PLAIN, 11);
	JCheckBoxMenuItem appendMenu;
	boolean searchImports = true;
	JTabbedPaneWithCloseIcons lookupTabs;
	OWLOntology currentOntology;
	Map OWLObjectHash;
	String title;
	
	public SwoopSearch(SwoopModel swoopModel, TermsDisplay termHandler, String title) {
		this.swoopModel = swoopModel;
		this.termHandler = termHandler;
		this.OWLObjectHash = new HashMap();
		this.title = title;
		setupUI();
		setTitle(title);
	}

	private void setupUI() {
		
		// setup central UI
		lookupTabs = new JTabbedPaneWithCloseIcons();
		lookupTabs.setFont(tahoma);
		Container content = this.getContentPane();
		content.setLayout(new BorderLayout());
		content.add(lookupTabs, "Center");
		
		// setup menu
		JMenu optionMenu = new JMenu("File");
		JMenuItem clearMenu = new JMenuItem("Clear All");
		appendMenu = new JCheckBoxMenuItem("Store History");
		appendMenu.setSelected(true);
		clearMenu.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				lookupTabs.removeAll();
			}		
		});
		JMenuBar mBar = new JMenuBar();
		mBar.add(optionMenu);
		//optionMenu.add(appendMenu);
		optionMenu.add(clearMenu);		
		this.setJMenuBar(mBar);
				
		setSize(400, 400);
		if (title.indexOf("Reference")==-1) setLocation(150, 250);
		else setLocation(250, 300);
		hide();
	}
	
	/**
	 * Print results in the Swoop Search Pane
	 * @param classSet - classes in the result set
	 * @param propSet - propertes in the result set
	 * @param instSet - instances in the result set
	 * @param lookup - word/entity being looked up
	 * @param ont - ontology in which search is being conducted
	 */
	public void printResults(OWLOntology ont, Set classSet, Set propSet, Set instSet, String lookup, String scope) {

		this.currentOntology = ont;
		
		String resultText = "<FONT FACE=\""+swoopModel.getFontFace()+"\" SIZE="+swoopModel.getFontSize()+">";
		if (title.indexOf("Reference")>=0) 
			resultText += "<b>References of "+lookup+":</b><br>";
		else
			resultText += "<b>Matches for "+lookup+":</b><br>";
		
		resultText += "in "+scope;
		resultText += "<br><br>";
		
		resultText += "<b>Classes found..</b>";		
		Iterator iter = classSet.iterator();
		if (!iter.hasNext()) {
			resultText += "NONE<br>";
		}
		else {
			resultText += "<br>";
			while (iter.hasNext()) {
				OWLClass cla = (OWLClass) iter.next();
				resultText += getConciseObject(cla);
				resultText += printReferences(cla);
				resultText += "<br>";
			}
		}
		
		resultText += "<br><b>Properties found..</b>";
		iter = propSet.iterator();
		if (!iter.hasNext()) {
			resultText += "NONE<br>";
		}
		else {
			resultText += "<br>";
			while (iter.hasNext()) {
				OWLProperty prop = (OWLProperty) iter.next();
				resultText += getConciseObject(prop);
				resultText += printReferences(prop);
				resultText += "<br>";
			}
		}
		
		resultText += "<br><b>Individuals found..</b>";
		iter = instSet.iterator();
		if (!iter.hasNext()) {
			resultText += "NONE<br>";
		}
		else {
			resultText += "<br>";
			while (iter.hasNext()) {
				OWLIndividual ind = (OWLIndividual) iter.next();
				resultText += getConciseObject(ind);
				resultText += printReferences(ind);
				resultText += "<br>";
			}
		}
		
		resultText += "</FONT>";
		JEditorPane resultPane = new JEditorPane();
		resultPane.setContentType("text/html");
		resultPane.setEditable(false);
		resultPane.addHyperlinkListener(termHandler);
		resultPane.setText(resultText);
		resultPane.setCaretPosition(0);
		
		if (!appendMenu.isSelected()) lookupTabs.removeAll();
		JScrollPane resultScroll = new JScrollPane(resultPane);
		removePreviousTab(lookup);
		lookupTabs.addTab(lookup, resultScroll);
		lookupTabs.setSelectedComponent(resultScroll);
		show();
	}
	
	/*
	 * Remove any old search for the current word/entity
	 */
	private void removePreviousTab(String lookup) {
		int removeIndex = -1;
		for (int i=0; i<lookupTabs.getTabCount(); i++) {
			if (lookupTabs.getTitleAt(i).equals(lookup)) {
				removeIndex = i;
				break;
			}
		}
		if (removeIndex!=-1) lookupTabs.remove(removeIndex);
	}
	
	private String printReferences(OWLEntity entity) {
		OWLObjectHash.put(String.valueOf(entity.hashCode()), entity);
		int fontSize = Integer.parseInt(swoopModel.getFontSize());
		if (fontSize>1) fontSize--;
		String resultText = "&nbsp;&nbsp;&nbsp;<font color=\"red\" size="+String.valueOf(fontSize)+">";
		resultText += "[<a href=\"<USAGE:"+entity.hashCode()+"\">Show References</a>]";
		resultText += "</font>";
		return resultText;
	}
	
	/*
	 * Print concise string representation for a single OWL entity
	 * i.e. icon with hyperlinked shortForm
	 */
	private String getConciseObject(OWLEntity obj) {
		
		String concise = "";
		try {
			String objURI = obj.getURI().toString();
			String objName = swoopModel.shortForm(obj.getURI());
			
			// don't add qnames for entity in current ontology
			// and highlight them in a different color (green)
			if (swoopModel.getEntity(currentOntology, new URI(objURI), true)!=null) {
				concise += "<font color=\"blue\">";			
				concise += "<a href=\""+objURI+"\">"+objName+"</a>";
			}
			else {
				concise += "<font color=\"green\">";
				String qName = "";
				String ontURI = "";
				if (objURI.indexOf("#")>=0) ontURI = objURI.substring(0, objURI.lastIndexOf("#"));
				else ontURI = objURI.substring(0, objURI.lastIndexOf("/"));
				qName = swoopModel.shortForm(new URI(ontURI)); //ontURI.substring(ontURI.lastIndexOf("/")+1, ontURI.length());
				concise += "<a href=\""+objURI+"\">"+qName+":"+objName+"</a>";
			}
			
			
			if (concise.indexOf("font color")>=0) concise += "</font>";
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return concise;
	}
}
