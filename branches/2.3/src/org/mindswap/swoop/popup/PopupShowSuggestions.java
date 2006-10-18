package org.mindswap.swoop.popup;
import java.awt.BorderLayout;
import java.awt.Container;
import java.util.Iterator;
import java.util.Set;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import org.mindswap.swoop.SwoopModel;
import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLException;

/**
 * @author bernardo
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class PopupShowSuggestions extends JFrame {

	JTextArea message;
	Set suggestions;
	SwoopModel swoopModel;
	public PopupShowSuggestions(Set sug, SwoopModel model){
		this.suggestions=sug;
		swoopModel = model;
		init();
	}
	
	public void init(){
		String s = "It is suggested to partition into the following Domains \n ";
		Iterator iter = suggestions.iterator();
		while(iter.hasNext()){
			OWLClass clazz = (OWLClass)iter.next();
			try {
				s = s.concat(swoopModel.shortForm(clazz.getURI()).toString() + "\n");
			} catch (OWLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		JPanel panel = new JPanel();
		message = new JTextArea(s);
		panel.add(message);
		Container content = getContentPane();
		content.setLayout(new BorderLayout());
		content.add(panel, "Center");
		setSize(400,200);
		setTitle("Initial Suggestions");
		setResizable(true);

		
	}

}
