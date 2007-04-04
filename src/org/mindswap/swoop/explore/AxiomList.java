package org.mindswap.swoop.explore;

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

/* A quick and dirty window to see axioms
 * 
 * 
 */
public class AxiomList extends JFrame implements WindowListener, ActionListener
{
	private JList   myList  = null;
	private JButton myCancelButton = null;
	
	public AxiomList( String name, Vector data )
	{
		super();
		System.out.println("AxiomList: data size = " + data.size());
		myList = new JList( data );	
		setupUI();
		setTitle( name  +" (" + data.size() + ")");
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
		myList.setSelectionMode( ListSelectionModel.SINGLE_INTERVAL_SELECTION );		
		JScrollPane scrolly = new JScrollPane();
		scrolly.getViewport().setView( myList );
		return scrolly;
	}
	
	private JPanel getControlPanel()
	{
		JPanel mainPanel = new JPanel();
		myCancelButton = new JButton("Cancel");		
		myCancelButton.addActionListener( this );
		mainPanel.add( myCancelButton );
		return mainPanel;
	}
	
	// ActionListener method
	public void actionPerformed(ActionEvent event) 
	{
		Object src = event.getSource();
		if ( src instanceof JButton )
		{
			if ( src == myCancelButton )
			{ this.windowClosing( null ); }
		}
	}

	// WindowListender methods
	public void windowClosed(WindowEvent arg0) 
	{}
	
	public void windowActivated(WindowEvent arg0) 
	{}
	public void windowClosing(WindowEvent arg0) 
	{ this.dispose(); }
	public void windowDeactivated(WindowEvent arg0) 
	{}
	public void windowDeiconified(WindowEvent arg0) 
	{}
	public void windowIconified(WindowEvent arg0) 
	{}
	public void windowOpened(WindowEvent arg0) 
	{}
}
