/*
 * Created on May 20, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.mindswap.swoop.utils.ui;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.mindswap.swoop.TermsDisplay;
import org.semanticweb.owl.model.OWLEntity;

/**
 * @author Aditya
 *
 * 
 */

public class ComparatorFrame extends JDialog {
	
	List owlEntities = null, entitiesRendered = null, entitiesSource = null;
	TermsDisplay termHandler;
	
	public ComparatorFrame(TermsDisplay termHandler) {
		
		this.termHandler = termHandler;
		owlEntities = new ArrayList();
		entitiesRendered = new ArrayList();
		entitiesSource = new ArrayList();
		setLocation(100, 50);
		setSize(275, 400);
		setVisible(false);
		setTitle("Resource Holder");
	}
	
	public void addEntity(String source, URI entityURI, String entityRendered) {
		//if (!owlEntities.contains(entity)) {
			entitiesSource.add(source);
			owlEntities.add(entityURI);
			entitiesRendered.add(entityRendered);
		//}
		redraw();
	}
	
	public void removeEntity(String source, URI entityURI, String entityRendered) {
		entitiesSource.remove(source);
		owlEntities.remove(entityURI);
		entitiesRendered.remove(entityRendered);
		redraw();
	}
	
	public List getEntities() {
		return owlEntities;
	}
	
	public void redraw() {
		
		// re-draw resource holder panel whenever entity has been added 
		// or removed from comparator
		//this.removeAll();
		int numEntities = owlEntities.size();
		JPanel panel = new JPanel();
		panel.setLayout(new GridLayout(1, numEntities));
		
		for (int i=0; i<numEntities; i++) {
			
			// create editor pane for each entity
			JEditorPane renderer = new JEditorPane();
			renderer.addHyperlinkListener(termHandler);
			renderer.setContentType("text/html");
			renderer.setEditable(false);
			String text = "[[[ <b>Source:</b>&nbsp;"+entitiesSource.get(i).toString()+" ]]]<br><br>";
			text += entitiesRendered.get(i).toString();
			renderer.setText(text);
			renderer.setCaretPosition(0);
			JScrollPane rendererPane = new JScrollPane(renderer);
			JPanel rendererPanel = new JPanel();
			rendererPanel.setLayout(new BorderLayout());
			rendererPanel.add(rendererPane, "Center");
			
			// add remove button
			final String source = entitiesSource.get(i).toString();
			final URI entityURI = (URI) owlEntities.get(i);
			final String entityRendered = entitiesRendered.get(i).toString();
			JButton removeBtn = new JButton("Remove this Entity");
			removeBtn.setFont(new Font("Tahoma", Font.PLAIN, 11));
			removeBtn.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					removeEntity(source, entityURI, entityRendered);
				}
			});
			rendererPanel.add(removeBtn, "South");
			panel.add(rendererPanel);
		}
		
		panel.updateUI();
		getContentPane().removeAll();
		getContentPane().setLayout(new GridLayout(1,1));
		getContentPane().add(panel);
		setVisible(true);
		setSize(numEntities*275, 400);
		show();
	}
}
