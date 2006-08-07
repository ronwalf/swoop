package org.mindswap.swoop.refactoring;

import java.awt.BorderLayout;
import java.awt.Container;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;

import org.mindswap.swoop.SwoopModel;
import org.mindswap.swoop.utils.graph.OntologyGraph;
import org.mindswap.swoop.utils.graph.hierarchy.OntologyWithClassHierarchyGraph;
import org.semanticweb.owl.model.OWLEntity;
import org.semanticweb.owl.model.OWLException;
import org.semanticweb.owl.model.OWLOntology;



/**
 * @author Evren Sirin
 */
public class RefactoringSummary extends JFrame  {
    SwoopModel swoopModel;
    JTabbedPane tabPane;
    JEditorPane logPane, tracePane;
    JPanel vizPanel;
	
    public RefactoringSummary(JFrame parent, SwoopModel swoopModel, Collection partitions, String statistics, String traceSummary) {
        super();
        
        this.swoopModel = swoopModel;
                
        setupUI();
        render(statistics, traceSummary, partitions);
        show();
    }
    
    public void render(String statistics, String traceSummary, Collection partitions) {
		logPane.setText(statistics);
		logPane.setCaretPosition(0);
		tracePane.setText(traceSummary);
		tracePane.setCaretPosition(0);
		
		try 
		{
			tabPane.add("Partition Graph", new OntologyGraph(swoopModel, partitions));
			tabPane.add("New Partition Graph", new OntologyWithClassHierarchyGraph( swoopModel, new Vector(partitions) ) );
        } 
		catch(Exception e) 
		{
            e.printStackTrace();
        }
    }
    
   
   
	private void setupUI() {
		Container content = getContentPane();
		content.setLayout(new BorderLayout());
		
		logPane = new JEditorPane();
		logPane.setContentType("text/html");
		logPane.setEditable(false);
		
		tracePane = new JEditorPane();
		tracePane.setContentType("text/html");
		tracePane.setEditable(false);
				
		tabPane = new JTabbedPane();
		tabPane.add("Partitioning Statistics", new JScrollPane(logPane));
		tabPane.add("Trace of Partitioning changes", new JScrollPane(tracePane));
				
		content.add(tabPane, "Center");
		setSize(700,600);
		setLocation(200,200);
		setTitle("Partitioning Summary");
	}
}
    