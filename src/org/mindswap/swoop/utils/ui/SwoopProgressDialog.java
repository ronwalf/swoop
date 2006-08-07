/*
 * Created on May 23, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.mindswap.swoop.utils.ui;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;

import org.mindswap.swoop.SwoopFrame;

/**
 * @author ronwalf
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class SwoopProgressDialog extends JDialog {
	private JButton cancelBtn;
	
	public SwoopProgressDialog(JFrame parent, String message) {
		super(parent, message);
		JProgressBar progressBar = new JProgressBar();
		progressBar.setIndeterminate(true);

		cancelBtn = new JButton("Cancel");
		cancelBtn.setFont(new Font("Tahoma", Font.PLAIN, 11));
		
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		panel.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));
		panel.add(new JLabel(message));
		panel.add(Box.createVerticalStrut(5));
		panel.add(progressBar);
		panel.add(Box.createVerticalStrut(5));
		panel.add(cancelBtn);
		getContentPane().add(panel);
		
		pack();
		setSize(400, getHeight());
		setLocation(80,100);
		setResizable(false);
		
	}
	
	
	public void show() {
		this.getParent().setEnabled(false);
		super.show();
	}
	
	public void dispose() {
		this.getParent().setEnabled(true);
		super.dispose();
	}
	public void setWorker(final SwingWorker worker) {
		addWindowListener
		(
				new WindowAdapter() {
					public void windowClosing(WindowEvent e) {
						worker.interrupt();
						//progressDialog.dispose();
					}
				}
		);
		
		
		
		
		cancelBtn.addActionListener (
			new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					SwoopProgressDialog.this.hide();
					worker.interrupt();
					SwoopProgressDialog.this.dispose();
				}
			}
		);
	}
	
	
}
