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

/*
 * Created on Apr 9, 2005
 *
 */
package org.mindswap.swoop.change;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Font;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.net.URI;
import java.util.Iterator;

import javax.swing.JFrame;
import javax.swing.JTabbedPane;

import org.mindswap.swoop.SwoopFrame;
import org.mindswap.swoop.SwoopModel;
import org.mindswap.swoop.annotea.Description;

/**
 * @author Aditya
 *
 */
public class VersionControl extends JFrame implements WindowListener {

	JTabbedPane versionTabs;
	Font tahoma = new Font("Tahoma", Font.PLAIN, 11);
	SwoopModel swoopModel;
	SwoopFrame swoopHandler;
	
	public VersionControl(SwoopModel swoopModel, SwoopFrame swoopHandler) {
		
		this.swoopModel = swoopModel;
		this.swoopHandler = swoopHandler;
		
		this.setupUI();
		this.init();
		
		this.addWindowListener(this);
	}
	
	private void setupUI() {
	
		// setup version control tabs, one tab for each versioned ontology
		versionTabs = new JTabbedPane();
		versionTabs.setFont(tahoma);
		
		// create main frame
		Container content = this.getContentPane();
		content.setLayout(new BorderLayout());
		content.add(versionTabs, "Center");
		
		setTitle("Web Ontology Version Control");
		setSize(600, 700);
		setLocation(100, 10);
				
	}
	
	/*
	 * Initialize: load saved repository(s) info and create version tabs accordingly
	 */
	private void init() {
		
		// load version control repository(s) info from disk
		swoopModel.loadVersionRepository();
		
		if (swoopModel.versionRepository.keySet().size()>0) {
			// add as many version tabs as there are repositoryURLs in versionRepository
			for (Iterator iter = swoopModel.versionRepository.keySet().iterator(); iter.hasNext();) {
				try {
					URI repURI = (URI) iter.next();
					Description[] verDesc = (Description[]) swoopModel.versionRepository.get(repURI);
					String verTitle = "VC:"+swoopModel.shortForm(repURI);
					VersionedOntology versionOnt = new VersionedOntology(swoopModel, this, repURI, verDesc);
					versionTabs.addTab(verTitle, versionOnt);
				}
				catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		}
		else {
			// setup default versioned ontology
			VersionedOntology defVersion = new VersionedOntology(swoopModel, this);
			// add tab
			versionTabs.addTab("Default", defVersion);			
		}
	}
	
	// do some refresh common across all versioned ontology tabs
	public void refresh() {
		for (int i=0; i<versionTabs.getComponentCount(); i++) {
			VersionedOntology vo = (VersionedOntology) versionTabs.getComponentAt(i);
			vo.refreshOntBox();
		}
	}

	/* (non-Javadoc)
	 * @see java.awt.event.WindowListener#windowOpened(java.awt.event.WindowEvent)
	 */
	public void windowOpened(WindowEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	public void windowClosing(WindowEvent arg0) {
		swoopModel.saveVersionRepository();
	}

	/* (non-Javadoc)
	 * @see java.awt.event.WindowListener#windowClosed(java.awt.event.WindowEvent)
	 */
	public void windowClosed(WindowEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see java.awt.event.WindowListener#windowIconified(java.awt.event.WindowEvent)
	 */
	public void windowIconified(WindowEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see java.awt.event.WindowListener#windowDeiconified(java.awt.event.WindowEvent)
	 */
	public void windowDeiconified(WindowEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see java.awt.event.WindowListener#windowActivated(java.awt.event.WindowEvent)
	 */
	public void windowActivated(WindowEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see java.awt.event.WindowListener#windowDeactivated(java.awt.event.WindowEvent)
	 */
	public void windowDeactivated(WindowEvent arg0) {
		// TODO Auto-generated method stub
		
	}
}
