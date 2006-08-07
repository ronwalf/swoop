package org.mindswap.swoop.utils.ui;

import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

import javax.swing.JButton;
import javax.swing.JPanel;

/**
 * Provides an add, add & close, and cancel button bar.
 * @author ronwalf
 *
 */
public class AddCloseBar extends JPanel implements ActionListener {
	final public static String CLOSE = "CLOSE";
	final public static String ADD = "ADD";
	final public static String ADDCLOSE = "ADD&CLOSE";
	
	private Collection listeners;
	private JButton applyBtn, addBtn, cancelBtn;
	private Font tahoma = new Font("Tahoma", Font.PLAIN, 11);
	
	public AddCloseBar() {
		super();
		
		listeners = new HashSet();

		applyBtn = new JButton("Add");
		applyBtn.setFont(tahoma);
		applyBtn.addActionListener(this);
		applyBtn.setActionCommand(ADD);
		
		addBtn = new JButton("Add & Close");
		addBtn.setFont(tahoma);
		addBtn.addActionListener(this);
		addBtn.setActionCommand(ADDCLOSE);
		
		cancelBtn = new JButton("Cancel");
		cancelBtn.setFont(tahoma);
		cancelBtn.addActionListener(this);
		cancelBtn.setActionCommand(CLOSE);
		
		setLayout(new GridLayout(1,3));
		add(applyBtn);
		add(addBtn);
		add(cancelBtn);
	}
	
	public void addActionListener(ActionListener listener) {
		listeners.add(listener);
	}

	public void actionPerformed(ActionEvent e) {
		ActionEvent newEvent = new ActionEvent(this, e.getID(), e.getActionCommand());
		for (Iterator iter = listeners.iterator(); iter.hasNext();) {
			ActionListener listener = (ActionListener) iter.next();
			listener.actionPerformed(newEvent);
		}
	}
	
}
