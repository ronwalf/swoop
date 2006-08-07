/*
 * Created on Aug 28, 2005
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.mindswap.swoop.utils.graph.hierarchy.popup;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;

import org.mindswap.swoop.SwoopModel;
import org.mindswap.swoop.utils.graph.hierarchy.OntologyWithClassHierarchyGraph;
import org.semanticweb.owl.model.OWLObject;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLSubClassAxiom;



/**
 * @author Dave
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class ListSelectionPopup extends JFrame implements WindowListener, ActionListener
{
	
	private Vector  myData = null;
	private JList   myList  = null;
	private JButton myHighlightButton = null;
	private JButton myClearButton  = null;
	private JButton myCancelButton = null;
	private SwoopModel myModel     = null;
	
	private OntologyWithClassHierarchyGraph myGraph = null;

	public ListSelectionPopup(OntologyWithClassHierarchyGraph graph,
							  String name, Vector data )
	{
		super();

		myGraph = graph;
		myModel = myGraph.getModel();
		myData = data;
		myList = new JList( data );
		
		setupUI();
		setTitle( name );
		setSize(350, 650);
		setVisible( true );
		addWindowListener( this );
	}

	private void setupUI()
	{
		Container contentPane = getContentPane();
		contentPane.setLayout( new BorderLayout() );
		contentPane.add( getListPanel(), BorderLayout.CENTER );
		contentPane.add( getControlPanel(), BorderLayout.SOUTH);
		
	}

	private JScrollPane getListPanel()
	{
		//JPanel mainPanel = new JPanel();
		myList.setSelectionMode( ListSelectionModel.SINGLE_INTERVAL_SELECTION );		
		JScrollPane scrolly = new JScrollPane();
		scrolly.getViewport().setView( myList );
		
		//mainPanel.add( scrolly );
		return scrolly;
	}
	
	private JPanel getControlPanel()
	{
		JPanel mainPanel = new JPanel();
		myHighlightButton = new JButton("Highlight");
		myCancelButton = new JButton("Cancel");
		
		myHighlightButton.addActionListener( this );
		myCancelButton.addActionListener( this );
		
		mainPanel.add( myHighlightButton );
		mainPanel.add( myCancelButton );
		return mainPanel;
	}
	
	// ActionListener method
	public void actionPerformed(ActionEvent event) 
	{
		Object src = event.getSource();
		if ( src instanceof JButton )
		{
			myGraph.resetPartitionFocus();
			if ( src == myHighlightButton )
			{
				AxiomContainer container = (AxiomContainer)myList.getSelectedValue();
				if ( container == null )
					return;
				OWLObject obj = container.getAxiom();
				if  ( obj instanceof OWLSubClassAxiom)
				{
					OWLOntology ont = OntologyFinder.findContainingOntologyBySubclassAxiom( myModel, (OWLSubClassAxiom)obj );
					myGraph.setPartitionFocus( ont );
					myGraph.setColorMode( OntologyWithClassHierarchyGraph.PARTITION_FOCUS_COLOR );
				}
			}
			else if  ( src == myCancelButton )
			{
				this.windowClosing( null ); 
			}
		}
	}

	// WindowListender methods
	public void windowClosed(WindowEvent arg0) 
	{}
	
	public void windowActivated(WindowEvent arg0) 
	{}
	public void windowClosing(WindowEvent arg0) 
	{		
		myGraph.setColorMode( OntologyWithClassHierarchyGraph.BASIC_COLOR );
		this.dispose();
	}
	public void windowDeactivated(WindowEvent arg0) 
	{}
	public void windowDeiconified(WindowEvent arg0) 
	{}
	public void windowIconified(WindowEvent arg0) 
	{}
	public void windowOpened(WindowEvent arg0) 
	{}





	
	
}
