package org.mindswap.swoop.refactoring;

import java.awt.BorderLayout;
import java.awt.Container;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;

/**
 * @author bernardo
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class PopupError extends JFrame {
	JTextArea message;
	
	public PopupError(String s){
		this.message=new JTextArea(s);
		init();
	}
	
	public void init(){
		JPanel panel = new JPanel();
		panel.add(message);
		Container content = getContentPane();
		content.setLayout(new BorderLayout());
		content.add(panel, "Center");
		setSize(400,200);
		setTitle("Error Message");
		setResizable(true);

		
	}
}
