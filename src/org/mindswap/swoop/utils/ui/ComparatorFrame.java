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
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.mindswap.swoop.SwoopModel;
import org.mindswap.swoop.TermsDisplay;
import org.mindswap.swoop.refactoring.Segmentation;
import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLEntity;
import org.semanticweb.owl.model.OWLException;
import org.semanticweb.owl.model.OWLOntology;

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
		setSize(275, 500);
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
		//
		//JPanel panelBig = new JPanel(new GridLayout(2, 1));
		JPanel panelBig = new JPanel(new BorderLayout());
		//
		JPanel panelButtons = new JPanel();
		panelButtons.setLayout(new GridLayout(2, 1));
		
		//
		JPanel panel = new JPanel();
		panel.setLayout(new GridLayout(1, numEntities));
		
//		This is for Segmentation purposes
		//Add Extract module button
		JButton moduleBtn = new JButton("Extract module for All Entities in the holder");
		moduleBtn.setFont(new Font("Tahoma", Font.PLAIN, 11));
		moduleBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)  {
				
				Segmentation seg = null;
				SwoopModel swoopModel = termHandler.getSwoopModel();
				if(!swoopModel.segmentation.containsKey(swoopModel.getSelectedOntology())){
					try {
						seg = new Segmentation(swoopModel.getSelectedOntology(), false, false);
					} catch (URISyntaxException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					} catch (OWLException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					//No dual roles
					Map aux = new HashMap();
					aux = swoopModel.getSegmentation();
					aux.put(swoopModel.getSelectedOntology(), seg);
					swoopModel.setSegmentation(aux);
				}
				else{
					seg = (Segmentation) swoopModel.getSegmentation().get(swoopModel.getSelectedOntology());
					seg.setDualConcepts(false);
					seg.setDualRoles(false);
				}
			
				Set allClasses;
				try {
					allClasses = swoopModel.getSelectedOntology().getClasses();
					Set allProperties = swoopModel.getSelectedOntology().getObjectProperties();
					allProperties.addAll(swoopModel.getSelectedOntology().getDataProperties());
					Set allEntities = new HashSet();
					allEntities.addAll(allClasses);
					allEntities.addAll(allProperties);
					Set allAxioms = seg.getAllAxioms();
					Map axSignature = seg.getAxiomsToSignature();
					Map sigToAxioms = seg.getSignatureToAxioms();
					
					Set sig = new HashSet();
					Iterator iter = getEntities().iterator();
					while(iter.hasNext()){
						URI uri = (URI)iter.next();
						OWLEntity ent = swoopModel.getEntity(swoopModel.getSelectedOntology(),uri,false);
						sig.add(ent);
					}
					//sig.addAll(getEntities());
					
					URI uriOntology = swoopModel.getSelectedOntology().getURI();
					 
					System.out.println("Getting Module");
					OWLOntology module = seg.getModule(allAxioms, sig, axSignature, sigToAxioms, uriOntology, (OWLClass)swoopModel.getSelectedEntity());
					System.out.println("Got Module");
					
					swoopModel.addOntology(module);
				} catch (OWLException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (Exception e2) {
					// TODO Auto-generated catch block
					e2.printStackTrace();
				}  
				
				
				
				
			}
			
		});
		
		panelButtons.add(moduleBtn);
		
		
		JButton moduleDualBtn = new JButton("Extract Dual module for All Entities in the holder");
		moduleDualBtn.setFont(new Font("Tahoma", Font.PLAIN, 11));
		moduleDualBtn.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e)  {
				
				Segmentation seg = null;
				SwoopModel swoopModel = termHandler.getSwoopModel();
				if(!swoopModel.segmentation.containsKey(swoopModel.getSelectedOntology())){
					try {
						seg = new Segmentation(swoopModel.getSelectedOntology(), true, true);
					} catch (URISyntaxException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					} catch (OWLException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					//No dual roles
					Map aux = new HashMap();
					aux = swoopModel.getSegmentation();
					aux.put(swoopModel.getSelectedOntology(), seg);
					swoopModel.setSegmentation(aux);
				}
				else{
					seg = (Segmentation) swoopModel.getSegmentation().get(swoopModel.getSelectedOntology());
					seg.setDualConcepts(true);
					seg.setDualRoles(true);
				}
			
				Set allClasses;
				try {
					allClasses = swoopModel.getSelectedOntology().getClasses();
					Set allProperties = swoopModel.getSelectedOntology().getObjectProperties();
					allProperties.addAll(swoopModel.getSelectedOntology().getDataProperties());
					Set allEntities = new HashSet();
					allEntities.addAll(allClasses);
					allEntities.addAll(allProperties);
					Set allAxioms = seg.getAllAxioms();
					Map axSignature = seg.getAxiomsToSignature();
					Map sigToAxioms = seg.getSignatureToAxioms();
					
					Set sig = new HashSet();
					Iterator iter = getEntities().iterator();
					while(iter.hasNext()){
						URI uri = (URI)iter.next();
						OWLEntity ent = swoopModel.getEntity(swoopModel.getSelectedOntology(),uri,false);
						sig.add(ent);
					}
					//sig.addAll(getEntities());
					
					URI uriOntology = swoopModel.getSelectedOntology().getURI();
					 
					System.out.println("Getting Module");
					OWLOntology module = seg.getModule(allAxioms, sig, axSignature, sigToAxioms, uriOntology, (OWLClass)swoopModel.getSelectedEntity());
					System.out.println("Got Module");
					
					swoopModel.addOntology(module);
				} catch (OWLException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (Exception e2) {
					// TODO Auto-generated catch block
					e2.printStackTrace();
				}  
				
				
				
				
			}
			
		});
		
		panelButtons.add(moduleDualBtn);
		
		
		
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
			//panel.add(rendererPanel);
			panel.add(rendererPanel);
			//
			
		}
		panelButtons.updateUI();
		panel.updateUI();
		/*
		getContentPane().removeAll();
		getContentPane().setLayout(new GridLayout(1,1));
		getContentPane().add(panel);
		*/
		//
		panelBig.add(panel, "Center");
		
		panelBig.add(panelButtons, "North");
		panelBig.updateUI();
		getContentPane().removeAll();
		//getContentPane().setLayout(new GridLayout(1,1));
		getContentPane().add(panelBig);
		
		//panelBig.updateUI();
		//
		setVisible(true);
		setSize(numEntities*275, 400);
		show();
	}
}
