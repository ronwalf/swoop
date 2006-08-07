// (c) 2003 Allen I Holub. All rights reserved.
package com.holub.ui;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
import java.util.Date;
import java.util.Calendar;
import java.net.URL;

import com.holub.ui.DateSelector;

/***
 *  This class is wrapper for a {@link DateSelector} that adds a
 *  navigation bar to manipulate the wrapped selector.
 *  See {@link DateSelectorPanel} for a description and picture
 *  of date selectors.
 * <DL>
 * <DT><b>Images</b>
 * <DD>
 *	<P>
 *  The navigation-bar arrows in the current implementation are images
 *  loaded as a "resource" from the CLASSPATH. Four files are used:
 *	<blockquote>
 *	$CLASSPATH/images/10px.red.arrow.right.double.gif<br>
 *	$CLASSPATH/images/10px.red.arrow.left.double.gif<br>
 *	$CLASSPATH/images/10px.red.arrow.right.gif<br>
 *	$CLASSPATH/images/10px.red.arrow.left.gif
 *	</blockquote>
 *	where <em>$CLASSPATH</em> is any directory on your CLASSPATH.
 *  If the <code>DateSelectorPanel</code>
 *  can't find the image file, it uses character representations
 *  (<code>"&gt;"</code>, <code>"&gt;&gt;"</code>,
 *  <code>"&lt;"</code>, <code>"&lt;&lt;"</code>).
 *  The main problem with this approach is that you can't change
 *  the color of the arrows without changing the image files. On
 *  the plus side, arbitrary images can be used for the movement
 *  icons.
 *  Future versions of this class will provide some way for you
 *  to specify that the arrows be rendered internally in colors
 *  that you specify at run time.
 * </DD>
 * </DL>
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
 *  @see DateSelector
 *  @see DateSelectorPanel
 *  @see DateSelectorDialog
 *  @see TitledDateSelector
 */

public class NavigableDateSelector extends JPanel implements DateSelector
{	private 	  DateSelector selector;

	// Names of images files used for the navigator bar.
	private static final String
		NEXT_YEAR_IMAGE		 = "images/10px.red.arrow.right.double.gif", //{=NavigableDateSelector.NEXT_YEAR}
		NEXT_MONTH_IMAGE	 = "images/10px.red.arrow.right.gif",
		PREVIOUS_YEAR_IMAGE	 = "images/10px.red.arrow.left.double.gif",
		PREVIOUS_MONTH_IMAGE = "images/10px.red.arrow.left.gif";

	// These constants are used both to identify the button, and
	// as the button caption in the event that the appropriate
	// image file can't be located.

	private static final String FORWARD_MONTH 	= ">"	,
								FORWARD_YEAR	= ">>"	,
								BACK_MONTH		= "<"	,
								BACK_YEAR		= "<<"	;


	private JPanel navigation = null;

	/** Wrap an existing DateSelector to add a a navigation bar
	 *  modifies the wrapped DateSelector.
	 */

	public NavigableDateSelector( DateSelector selector )
	{	this.selector = selector;
		setBorder( null  );
		setOpaque( false );
		setLayout( new BorderLayout() );
		add( (JPanel)selector, BorderLayout.CENTER );

		// Create the actual navigation bar: a JPanel that holds
		// four buttons whose labels are immages of arrows.

		navigation = new JPanel();
		navigation.setLayout(new FlowLayout());
		navigation.setBorder( null );
		navigation.setBackground( com.holub.ui.Colors.LIGHT_YELLOW );
		navigation.add( makeNavigationButton(BACK_YEAR	) );
		navigation.add( makeNavigationButton(BACK_MONTH	) );
		navigation.add( makeNavigationButton(FORWARD_MONTH) );
		navigation.add( makeNavigationButton(FORWARD_YEAR ) );

		add(navigation,	BorderLayout.SOUTH);
	}
	/**
	 * Create a navigable date selector by wrapping the indicated one.
	 * @param selector the raw date selector to wrap;
	 * @param backgroundColor the background color of the navigation
	 * 		bar (or null for transparent). The default color is
	 * 		{@link com.holub.ui.Colors#LIGHT_YELLOW}.
	 * @see #setBackground
	 */

	public NavigableDateSelector( DateSelector selector, Color backgroundColor )
	{	this(selector);
		navigation.setBackground( backgroundColor );
	}

	/** Convenience constructor. Creates the wrapped DateSelector
	 *  for you. (It creates a {@link DateSelectorPanel} using
	 *  the no-arg constructor.
	 */

	public NavigableDateSelector()
	{	this( new DateSelectorPanel() );
	}

	/** Change the Navigation-Bar Color. Colors.TRANSPARENT is
	 *  recognized as a legitimate background color.
	 */
	public void changeNavigationBarColor( Color backgroundColor )
	{	if( backgroundColor != null )
			navigation.setBackground( backgroundColor );
		else
			navigation.setOpaque(false);
	}

	private final NavigationHandler navigationListener
										= new NavigationHandler();

	/** Handle clicks from the navigation-bar buttons. */

	private class NavigationHandler implements ActionListener
	{	public void actionPerformed(ActionEvent e)
		{	String direction = e.getActionCommand();

			if 	   (direction==FORWARD_YEAR )selector.roll(Calendar.YEAR,true);
			else if(direction==BACK_YEAR    )selector.roll(Calendar.YEAR,false);
			else if(direction==FORWARD_MONTH)
			{
				selector.roll(Calendar.MONTH,true);
				if( selector.get(Calendar.MONTH) == Calendar.JANUARY )
					selector.roll(Calendar.YEAR,true);
			}
			else if (direction==BACK_MONTH 	)
			{
				selector.roll(Calendar.MONTH,false);
				if( selector.get(Calendar.MONTH) == Calendar.DECEMBER )
					selector.roll(Calendar.YEAR,false);
			}
			else
			{	// assert false:  "Unexpected direction";
			}
		}
	}

	private JButton makeNavigationButton(String caption)
	{
		// Get the resource from the class loader, which will search
		// using the same algorithm that it uses to get .class files.
		// As long as the indicated images files are on the CLASSPATH
		// (or if the program is running from a .jar file, are
		// in the jar), then we'll find them.

		ClassLoader loader = getClass().getClassLoader();
		URL	image =
			(caption==FORWARD_YEAR	)? loader.getResource(NEXT_YEAR_IMAGE):
			(caption==BACK_YEAR   	)? loader.getResource(PREVIOUS_YEAR_IMAGE):
			(caption==FORWARD_MONTH	)? loader.getResource(NEXT_MONTH_IMAGE):
									   loader.getResource(PREVIOUS_MONTH_IMAGE) ;

		JButton b = (image!=null) ? new JButton( new ImageIcon(image) )
								  : new JButton(caption)
								  ;
		b.setBorder(new EmptyBorder(0,4,0,4));
		b.setFocusPainted(false);
		b.setActionCommand(caption);
		b.addActionListener( navigationListener );
		b.setOpaque( false );
		return b;
	}

	public synchronized void addActionListener(ActionListener l)
	{	selector.addActionListener(l);
	}
	public synchronized void removeActionListener(ActionListener l)
	{	selector.removeActionListener(l);
	}
	public Calendar getCalendarRepresentation(){return selector.getCalendarRepresentation();	}
	public Date		getDateRepresentation()	   {return selector.getDateRepresentation();		}
	public void		displayDate( Calendar c){       selector.displayDate(c);		}
	public void		displayDate( Date 	  d){       selector.displayDate(d);		}
	public void		roll(int f, boolean up)	{       selector.roll(f,up);			}
	public int		get(int f)				{return selector.get(f);				}
}
