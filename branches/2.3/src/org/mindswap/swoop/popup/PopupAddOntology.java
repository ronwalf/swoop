package org.mindswap.swoop.popup;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.net.URI;
import java.util.Iterator;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.mindswap.swoop.SwoopModel;
import org.mindswap.swoop.renderer.CellRenderer;
import org.semanticweb.owl.model.OWLException;

/**
 * @author Aditya
 *
 */
public class PopupAddOntology extends JDialog implements ActionListener, ItemListener {
	
	SwoopModel swoopModel;
	Font tahoma = new Font("Tahoma", Font.PLAIN, 11);
	JButton addOntBtn, cancelBtn;
	JComboBox ontCombo;
	JTextField ontBox;
	public URI ontologyURI;
	
	public PopupAddOntology(SwoopModel swoopModel) {
		setModal(true);
		this.swoopModel = swoopModel;
		setupUI();
	}
	
	private void setupUI() {
		
		JPanel tab1 = new JPanel();
		tab1.setLayout(new BorderLayout());
		ontCombo = new JComboBox();
		ontCombo.addItemListener(this);
		ontCombo.setFont(tahoma);
		ontCombo.setRenderer(new CellRenderer());
		ontBox = new JTextField();
		ontBox.setFont(tahoma);
		ontBox.setEditable(true);
		addOntBtn = new JButton("Add Imports");
		addOntBtn.setFont(tahoma);
		addOntBtn.addActionListener(this);
		JPanel tab1N = new JPanel();
		tab1N.setLayout(new GridLayout(6,1));
		JLabel ontLbl = new JLabel("Select Ontology:");
		ontLbl.setFont(tahoma);
		JLabel ontLbl2 = new JLabel("OR Specify Ontology URI:");
		ontLbl2.setFont(tahoma);
		JPanel btnPanel = new JPanel();
		btnPanel.setLayout(new GridLayout(1,2));
		btnPanel.add(addOntBtn);
		cancelBtn = new JButton("Cancel");
		cancelBtn.setFont(tahoma);
		cancelBtn.addActionListener(this);
		btnPanel.add(cancelBtn);
		tab1N.add(ontLbl);
		tab1N.add(ontCombo);
		tab1N.add(ontLbl2);
		tab1N.add(ontBox);
		tab1N.add(new JLabel(""));
		tab1N.add(btnPanel);
		tab1.add(tab1N, "North");
		
		fillValues();
		
		// add tabbed pane to frame container
		Container content = getContentPane();
		content.setLayout(new BorderLayout());
		JLabel typeLbl = new JLabel("[ ADDING IMPORTS..]");
		typeLbl.setFont(tahoma);
		content.add(typeLbl, "North");
		content.add(tab1, "Center");
		setSize(450,200);
		setTitle("Specify Ontology");
		setResizable(true);
	}

	private void fillValues() {
		ontCombo.removeItemListener(this);
    	ontCombo.setFont(new Font(swoopModel.getFontFace(), Font.PLAIN, 11));
    	
    	Set ont = swoopModel.getOntologyURIs();    	
    	Iterator iter = ont.iterator();
    	while (iter.hasNext()) {
    		String uri = iter.next().toString();
    		try {
				if (!uri.equals(swoopModel.getSelectedOntology().getURI().toString()))
					ontCombo.addItem(uri);
			} catch (OWLException e) {
				e.printStackTrace();
			}
    	}
		
	}
	
	public void actionPerformed(ActionEvent e) {
		
		if (e.getSource()==addOntBtn) {
			try {
				if ((ontBox.getText()!=null) && (!ontBox.getText().trim().equals(""))) {
					ontologyURI = new URI(ontBox.getText().trim());
				}
				else {
					ontologyURI = new URI(ontCombo.getSelectedItem().toString());
				}
			}
			catch (Exception ex) {
				ex.printStackTrace();
			}
			dispose();
		}
		
		if (e.getSource()==cancelBtn) {
			dispose();
		}
		
	}

	public void itemStateChanged(ItemEvent e) {
		if (e.getSource()==ontCombo) {
    		if (ontCombo.getSelectedIndex()==0) ontBox.setEnabled(true);
    		else ontBox.setEnabled(false);
    	}
	}
}
