/*
 * Created on Oct 28, 2005
 *
 */
package org.mindswap.swoop.popup;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.JCheckBox;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JScrollPane;

import org.mindswap.swoop.SwoopModel;
import org.mindswap.swoop.TermsDisplay;

/**
 * @author Aditya
 *
 */
public class PopupExplanation extends JFrame implements ActionListener {

	SwoopModel swoopModel;
	TermsDisplay td;
	List sosStr;
	JCheckBox hlChk;
	JEditorPane ep;
	
	public PopupExplanation(TermsDisplay td, List sosStr) {
		
		this.swoopModel = td.getSwoopModel();
		this.td = td;
		this.sosStr = sosStr;
		setupUI();
	}
	
	private void setupUI() {
		
		Container cont = this.getContentPane();
		cont.setLayout(new BorderLayout());
		ep = new JEditorPane();
		ep.setContentType("text/html");
		ep.setText("<font FACE=\""+swoopModel.getFontFace()+"\" SIZE=3>"+sosStr.get(0).toString());
		ep.setEditable(false);
		ep.addHyperlinkListener(td);
		cont.add(new JScrollPane(ep), "Center");
		hlChk = new JCheckBox("Strike out irrelevant parts of axioms");
		hlChk.setFont(new Font("Tahoma", Font.PLAIN, 11));
		hlChk.addActionListener(this);
		cont.add(hlChk, "South");
		
		setSize(800, 300);
		setLocation(150, 250);
		setTitle("Explanation");		
	}

	public void actionPerformed(ActionEvent e) {
		
		if (e.getSource() == hlChk) {
			int val = 0;
			if (hlChk.isSelected()) val = 1;
			ep.setText("<font FACE=\""+swoopModel.getFontFace()+"\" SIZE=3>"+sosStr.get(val).toString());
		}
		
	}
}
