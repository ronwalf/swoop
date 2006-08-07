package org.mindswap.swoop.utils.ui;

import java.awt.Dimension;
import java.awt.Frame;
import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;

import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.mindswap.swoop.SwoopFrame;

public class ExceptionDialog extends JDialog {
	
	public static JDialog createDialog(Frame parent, String message, Exception error) {
		
		// Get full stack trace.
		ByteArrayOutputStream messageStream = new ByteArrayOutputStream();
		PrintWriter pw = new PrintWriter(messageStream);
		error.printStackTrace(pw);
		pw.close();
		
		JTextArea errorText = new JTextArea(messageStream.toString());
		//errorText.setPreferredSize(new Dimension(300,300));
		errorText.setEditable(false);
		JScrollPane textPane = new JScrollPane(errorText);
		textPane.setPreferredSize(new Dimension(500, 400));
		//JOptionPane.showMessageDialog(SwoopFrame.this, textPane, 
		//		"Cannot load ontology", JOptionPane.ERROR_MESSAGE);
		JOptionPane warning = new JOptionPane(textPane, JOptionPane.ERROR_MESSAGE);
		JDialog dialog = warning.createDialog(parent, message);
		dialog.getContentPane().add(warning);
		dialog.setResizable(true);
		
		return dialog;
	}
}
