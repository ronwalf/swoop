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
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.border.EtchedBorder;

/**
 * @author Aditya
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class AnnoteaServerPrefs extends JFrame {
	
	Font tahoma = new Font("Tahoma", Font.PLAIN, 11);
	List serverRList, serverPList;
	
	public AnnoteaServerPrefs() {
		serverRList = new ArrayList();
		serverRList.add("http://www.mindswap.org/2004/annotea/Annotation");
		serverPList = new ArrayList();
		serverPList.add("http://www.mindswap.org/2004/annotea/Annotation");
		setupUI();
	}
	
	private JButton createButton(String lbl, String tooltip) {
		JButton btn = new JButton(lbl);
		btn.setFont(tahoma);
		btn.setToolTipText(tooltip);
		return btn;
	}
	
	private JTextField createField(String lbl) {
		JTextField fld = new JTextField(lbl);
		fld.setFont(tahoma);
		return fld;
	}
	
	private JPanel createBox(String txt, JComponent comp) {
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		JLabel lbl = new JLabel(txt);
		lbl.setFont(tahoma);
		panel.add(lbl, "West");
		panel.add(comp, "Center");
		return panel;
	}
	
	private void setupUI() {
		
		JLabel topLbl = new JLabel("Retrieve Annotations from the following servers:");
		topLbl.setFont(new Font("Tahoma", Font.BOLD, 10));
		final JTextField serverFld = new JTextField();
		JPanel serverFldPanel = createBox("Server URL:", serverFld);
		JPanel serverTopPanel = new JPanel();
		serverTopPanel.setLayout(new BorderLayout());
		serverTopPanel.add(serverFldPanel, "Center");
		JButton addServerBtn = createButton("Add", "Add URL to Server List");
		serverTopPanel.add(addServerBtn, "East");
		serverTopPanel.add(topLbl, "North");
		JPanel retrPanel = new JPanel();
		retrPanel.setLayout(new BorderLayout());		
		retrPanel.add(serverTopPanel, "North");
		final JList serverLst = new JList();
		serverLst.setFont(tahoma);
		serverLst.setListData(serverRList.toArray());
		JScrollPane serverPane = new JScrollPane(serverLst);
		retrPanel.add(serverPane, "Center");
		final JLabel serverListLbl = new JLabel("Server List: 1 present");
		serverListLbl.setFont(tahoma);
		retrPanel.add(serverListLbl, "South");
		retrPanel.setBorder(new EtchedBorder());
		
		addServerBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (!isURL(serverFld.getText())) {
					JOptionPane.showMessageDialog(null, "Invalid URL entered", "Error", JOptionPane.ERROR_MESSAGE);
					return;
				}
				serverRList.add(serverFld.getText());
				serverLst.setListData(serverRList.toArray());
				serverListLbl.setText("Server List: "+serverRList.size()+ " present");
			}
		});
		
		serverLst.addKeyListener(new KeyListener() {

			public void keyPressed(KeyEvent e) {
				int key = e.getKeyCode();
				if (key==127) {
					// delete server URL entry
					serverRList.remove(serverLst.getSelectedIndex());
					serverLst.setListData(serverRList.toArray());
					serverListLbl.setText("Server List: "+serverRList.size()+ " present");
				}
			}

			public void keyReleased(KeyEvent arg0) {}

			public void keyTyped(KeyEvent arg0) {}
			
		});
		
		JLabel topLbl2 = new JLabel("Post Annotations to the following servers:");
		topLbl2.setFont(new Font("Tahoma", Font.BOLD, 10));
		final JTextField serverFld2 = new JTextField();
		JPanel serverFldPanel2 = createBox("Server URL:", serverFld2);
		JPanel serverTopPanel2 = new JPanel();
		serverTopPanel2.setLayout(new BorderLayout());
		serverTopPanel2.add(serverFldPanel2, "Center");
		JButton addServerBtn2 = createButton("Add", "Add URL to Server List");
		serverTopPanel2.add(addServerBtn2, "East");
		serverTopPanel2.add(topLbl2, "North");
		final JList serverLst2 = new JList();
		serverLst2.setFont(tahoma);
		serverLst2.setListData(serverPList.toArray());
		JScrollPane serverPane2 = new JScrollPane(serverLst2);
		final JLabel serverListLbl2 = new JLabel("Server List: 1 present");
		serverListLbl2.setFont(tahoma);
		JPanel postPanel = new JPanel();
		postPanel.setLayout(new BorderLayout());
		postPanel.add(serverTopPanel2, "North");
		postPanel.add(serverPane2, "Center");
		postPanel.add(serverListLbl2, "South");
		postPanel.setBorder(new EtchedBorder());
		
		addServerBtn2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (!isURL(serverFld2.getText())) {
					JOptionPane.showMessageDialog(null, "Invalid URL entered", "Error", JOptionPane.ERROR_MESSAGE);
					return;
				}
				serverPList.add(serverFld2.getText());
				serverLst2.setListData(serverPList.toArray());
				serverListLbl2.setText("Server List: "+serverPList.size()+ " present");
			}
		});
		
		serverLst2.addKeyListener(new KeyListener() {

			public void keyPressed(KeyEvent e) {
				int key = e.getKeyCode();
				if (key==127) {
					// delete server URL entry
					serverPList.remove(serverLst2.getSelectedIndex());
					serverLst2.setListData(serverPList.toArray());
					serverListLbl2.setText("Server List: "+serverPList.size()+ " present");
				}
			}

			public void keyReleased(KeyEvent arg0) {}

			public void keyTyped(KeyEvent arg0) {}
			
		});
		
		getContentPane().setLayout(new GridLayout(2,1));
		getContentPane().add(retrPanel);
		getContentPane().add(postPanel);
		
		setSize(400,400);
		setLocation(260,200);
		setResizable(false);
		setTitle("Annotea Preferences");
		hide();
	}

	private boolean isURL(String urlStr) {
		try {
			URL url = new URL(urlStr);
			return true;	
		}
		catch (Exception e) {
			return false;
		}
	}
}
