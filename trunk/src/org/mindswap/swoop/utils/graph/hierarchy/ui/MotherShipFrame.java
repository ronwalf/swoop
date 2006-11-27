/*
 * Created on Dec 1, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.mindswap.swoop.utils.graph.hierarchy.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.net.URI;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.KeyStroke;

import org.mindswap.swoop.Swoop;
import org.mindswap.swoop.SwoopModel;
import org.mindswap.swoop.utils.graph.hierarchy.OntologyWithClassHierarchyGraph;
import org.mindswap.swoop.utils.ui.SwoopIcons;

/**
 * @author Dave Wang
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class MotherShipFrame extends JFrame implements LaunchListener, WindowListener
{
	
	class ImagePanel extends JPanel
	{	
		private Image myImage = null;
		public ImagePanel()
		{			
			Toolkit toolkit = Toolkit.getDefaultToolkit();
			myImage = toolkit.getImage(SwoopIcons.getImageURL("cropcircleLogo.gif"));
			MediaTracker mediaTracker = new MediaTracker(this);
			mediaTracker.addImage(myImage, 0);
			try
			{
				mediaTracker.waitForID(0);
			}
			catch (InterruptedException ie)
			{
				System.err.println(ie);
				System.exit(1);
			}			
			
			add( new JLabel( new ImageIcon( myImage )));
			int width = myImage.getWidth( null );
			int height= myImage.getHeight( null );
			setBackground( Color.white );
			setSize( width, height );
			setPreferredSize( new Dimension( width, height) );
			repaint();
			setVisible( true );			
		}
		/*
		public void paint( Graphics g)
		{
			super.paint( g );
			Graphics2D g2d = (Graphics2D)g;
			g2d.drawImage( myImage, new AffineTransform(), null );			
		}
		*/		
	}
	
	private class AnimationRunner extends Thread
	{
		private MotherShipFrame myFrame = null;
		
		public AnimationRunner( MotherShipFrame frame )
		{ myFrame = frame; }
				
		public void run()
		{
	        for (int i = 0; i < 1;) 
	        {
	        	myPanel.repaint();
	            try 
				{ sleep(100); } 
	            catch (InterruptedException e) 
				{ break; }
	        }
		}
	}
	
	private OntologyWithClassHierarchyGraph myGraph = null;
	private ImagePanel myPanel;
	private Thread myAnimator = null;
	private boolean isLoading = true;
	private Image myImage = null;
	private JLabel myProgressLabel = null;
	
	public MotherShipFrame( SwoopModel model, Vector connections, URI uri )
	{
		super( "Flying over " + uri.toString() );
		myGraph = new OntologyWithClassHierarchyGraph( model, connections ); 
		getContentPane().add( myGraph );
		setSize( 1024, 740);
		this.validate();
		this.repaint();
		this.addWindowListener( this );
		this.setVisible( true );
	}
	
	public void launchStateChanged(LaunchEvent e) 
	{
		System.out.println( e.getChange() );
		myProgressLabel.setText( e.getChange() );
		if ( e.getChange().equals( LaunchEvent.BUILDING_GUI ) )
		{
			this.getContentPane().removeAll();
			setSize( 800, 700 );
			getContentPane().add( myGraph );
			setVisible( true );
			repaint();
			validate();
		}
		myProgressLabel.repaint();
	}

	public void windowOpened(WindowEvent arg0) 
	{}

	public void windowClosing(WindowEvent arg0) 
	{
		this.removeWindowListener( this );
		this.setEnabled( false );
		this.dispose();
	}

	public void windowClosed(WindowEvent arg0) 
	{}

	public void windowIconified(WindowEvent arg0) 
	{}

	public void windowDeiconified(WindowEvent arg0) 
	{}

	public void windowActivated(WindowEvent arg0) 
	{}
	
	public void windowDeactivated(WindowEvent arg0) 
	{}
		
}
