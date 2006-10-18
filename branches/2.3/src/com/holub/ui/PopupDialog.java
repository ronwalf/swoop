// (c) 2003 Allen I Holub. All rights reserved.
//
package com.holub.ui;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.awt.geom.Line2D;
import java.net.URL;

// Test this class by instantiating DateSelector
// TODO: Draw the close box in a paint() override using lines so that
// 		 the dependancy on the image file goes away and so that
// 		 it can change colors to match the foreground color.

/*** A PopupDialog is a clean, lightweight, "modal" window intended for
 *  simple pop-up user-interface widgets. The  frame, as shown at right,
 *  <img src="../../../images/PopupDialog.gif" align=right>
 *  is a single-pixel-wide
 *  line; the title bar holds only
 *  the title text and a small "close-window" icon.
 *  The dialog
 *  box can be dragged around on the screen by grabbing the title
 *  bar (and closed by clicking on the icon), but the user can't
 *  resize it, minimize it, etc. (Your program can do so, of course).
 *  <p>
 *  The "close" icon in the current implementation is an image
 *  loaded as a "resource" from the CLASSPATH. The file must be
 *  located at
 *	<blockquote>
 *	$CLASSPATH/images/8px.red.X.gif
 *	</blockquote>
 *	where <em>$CLASSPATH</em> is any directory on your CLASSPATH.
 *  If the class can't find the image file, it uses the character
 *  "X" instead.
 *  The main problem with this approach is that you can't change
 *  the color of the close icon to math the title-bar colors.
 *  Future versions of this class will fix the problem by rendering
 *  the image internally.
 *
 * <!-- ====================== distribution terms ===================== -->
 * <p><blockquote
 * 	style="border-style: solid; border-width:thin; padding: 1em 1em 1em 1em;">
 * <center>
 * 			Copyright &copy; 2003, Allen I. Holub. All rights reserved.
 * </center>
 * <br>
 * <br>
 * 		This code is distributed under the terms of the
 * 		<a href="http://www.gnu.org/licenses/gpl.html"
 * 		>GNU Public License</a> (GPL)
 * 		with the following ammendment to section 2.c:
 * 		<p>
 * 		As a requirement for distributing this code, your splash screen,
 * 		about box, or equivalent must include an my name, copyright,
 * 		<em>and URL</em>. An acceptable message would be:
 * <center>
 * 		This program contains Allen Holub's <em>XXX</em> utility.<br>
 * 				(c) 2003 Allen I. Holub. All Rights Reserved.<br>
 * 						http://www.holub.com<br>
 * </center>
 * 		If your progam does not run interactively, then the foregoing
 * 		notice must appear in your documentation.
 * </blockquote>
 * <!-- =============================================================== -->
 * @author Allen I. Holub
 */

public class PopupDialog extends JDialog
{
	private Color TITLE_BAR_COLOR = com.holub.ui.Colors.LIGHT_YELLOW;
	private Color CLOSE_BOX_COLOR  = com.holub.ui.Colors.DARK_RED;

	private JLabel title = new JLabel("xxxxxxxxxxxxxx");
	{	title.setHorizontalAlignment(SwingConstants.CENTER);
		title.setOpaque( false );
		title.setFont( title.getFont().deriveFont(Font.BOLD) );
	}

	private JPanel header = new JPanel();
	{	header.setBackground( TITLE_BAR_COLOR );
		header.setLayout( new BorderLayout() );
		header.setBorder( BorderFactory.createEmptyBorder(2,2,2,2) );
		header.add( title 					, BorderLayout.CENTER );
		header.add( createCloseButton()	, BorderLayout.EAST	  );
	}

	private JPanel contentPane = new JPanel();
	{	contentPane.setLayout( new BorderLayout() );
	}

	public PopupDialog( Frame owner ){ super(owner); setModal(true); }
	public PopupDialog( Dialog owner){	super(owner); setModal(true); }

	/* code common to all constructors */
	{
		initDragable();

		setUndecorated( true );
		JPanel contents = new JPanel();
		contents.setBorder( BorderFactory.createLineBorder(Color.BLACK,1) );
		contents.setLayout(new BorderLayout());
		contents.add(header,	   BorderLayout.NORTH);
		contents.add(contentPane, BorderLayout.CENTER);
		contents.setBackground( Color.WHITE );

		setContentPane( contents ); // , BorderLayout.CENTER );
		setLocation(100,100);
	}

	private JButton createCloseButton()
	{
		URL	image = getClass().getClassLoader().getResource(
												"images/8px.red.X.gif");

		JButton b = (image!=null) ? new JButton( new ImageIcon(image) )
								  : new JButton( "  X  " )
								  ;

		Border outer = BorderFactory.createLineBorder(CLOSE_BOX_COLOR,1);
		Border inner = BorderFactory.createEmptyBorder(2,2,2,2);

		b.setBorder( BorderFactory.createCompoundBorder(outer,inner) );

		b.setOpaque( false );
		b.addActionListener
		(	new ActionListener()
			{	public void actionPerformed(ActionEvent e)
				{	PopupDialog.this.setVisible(false);
					PopupDialog.this.dispose();
				}
			}
		);

		b.setFocusable( false );
		return b;
	}

	/** Set the dialog title to the indicated text */
	public void setTitle( String text ){ title.setText( text );	}

	//----------------------------------------------------------------------
	// Drag support. 							{=PopupDialog.drag.support}
	//
	private Point referencePosition = new Point(0,0);
	private MouseMotionListener movementHandler;
	private MouseListener clickHandler;

	private void initDragable()
	{
		clickHandler =
			new MouseAdapter()
			{	public void mousePressed( MouseEvent e )
				{	referencePosition = e.getPoint();	// start of the drag
				}
			};

		movementHandler =
		 	new MouseMotionAdapter()
			{	public void mouseDragged( MouseEvent e )
				{	// The reference posistion is the (window relative)
					// cursor postion when the click occured. The
					// currentMouse-position is mouse position
					// now, and the deltas represent the disance
					// moved.

					Point currentMousePosition  = e.getPoint();
					Point currentWindowLocation = getLocation();

					int deltaX=currentMousePosition.x - referencePosition.x;
					int deltaY=currentMousePosition.y - referencePosition.y;

					// Move the window over by the computed delta. This move
					// effectivly shifts the window-relative current-mouse
					// position back to the original reference position.

					currentWindowLocation.translate(deltaX, deltaY);
					setLocation(currentWindowLocation);
				}
			};

		setDragable(true);
	}

	/** Turn dragability on or off.
	 */
	public void setDragable( boolean on )
	{	if( on )
		{	title.addMouseMotionListener ( movementHandler );
			title.addMouseListener		 ( clickHandler	);
		}
		else
		{	title.removeMouseMotionListener ( movementHandler );
			title.removeMouseListener		( clickHandler	);
		}
	}

	/** Add your widgets to the window returned by this method, in
	 *  a manner similar to a JFrame. Do not modify the PoupDialog
	 *  itself. The returned container is a {@link JPanel JPanel}
	 *  with a preinstalled {@link BorderLayout}.
	 *  By default, it's colored colored dialog-box gray.
	 *  @return the content pane.
	 */
	public Container getContentPane(){ return contentPane;	}

	/** Change the color of the text and background in the title bar.
	 *  The "close" icon is always
	 *  {@linkplain com.holub.ui.Colors#DARK_RED dark red}
	 *  so it will be hard to see if the background color is also
	 *  a dark red).
	 *  @param foreground the text color
	 *  @param background the background color
	 */
	public void changeTitlebarColors( Color foreground, Color background )
	{	title.setForeground ( foreground );
		header.setBackground( background );
	}

	//----------------------------------------------------------------------
	private static class Test
	{	public static void main( String[] args )
		{
			final JFrame main 	= new JFrame("Hello");
			final JDialog dialog= new PopupDialog( main );
			final JButton b		= new JButton("close");

			b.addActionListener
			(	new ActionListener()
				{	public void actionPerformed(ActionEvent e)
					{	dialog.setVisible(false);
						dialog.dispose();
					}
				}
			);
			main.getContentPane().add(new JLabel("Main window"));
			main.pack();
			main.show();

			System.out.println("Creating dialog");
			dialog.getContentPane().add( b );
			dialog.pack();

			System.out.println("Displaying dialog");
			dialog.show();
			System.out.println("Dialog shut down");


			System.out.println("Display nondragable in different colors");

			PopupDialog d = (PopupDialog)dialog;
			d.changeTitlebarColors( Color.WHITE, Color.BLACK );
			d.setDragable( false );

			dialog.show();
			System.exit(0);
		}
	}
}
