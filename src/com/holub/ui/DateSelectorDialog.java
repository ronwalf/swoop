// (c) 2003 Allen I Holub. All rights reserved.
package com.holub.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Date;
import java.util.Calendar;

import com.holub.ui.PopupDialog;
import com.holub.ui.DateSelector;
import com.holub.ui.DateSelectorPanel;

/***
 *  The DateSelectorDialog, shown below,
 *  combines a {@link DateSelector} and
 *  a {@link PopupDialog} to provide a standalone, popup dialog
 *  for choosing dates.
 *  <blockquote>
 * 	<img style="borderStyle:none" src="../../../images/DateSelectorDialog.gif">
 *  </blockquote>
 *  The dialog is a free-floating top-level window. You can drag it around by the
 *  title bar and  close it by clicking on the "close" icon.
 *  <p>
 *  The class does implement the {@link DateSelector} interface, but
 *  bear in mind that the window closes when the user selects a date.
 *  Unlike the {@link TitledDateSelector} wrapper class,
 *  both of the action events are sent to listeners, however.
 * 	Create one the hard way like this:
 *	<pre>
 *	DateSelector calendar = new DateSelectorPanel( selector );
 *	calendar = new NavigableDateSelector( calendar ); // add navigation
 *	DateSelectorDialog chooser = new DateSelectorDialog(parentFrame, calendar);
 *	//...
 *	Date d = chooser.select();	// Pops up chooser; returns selected Date.
 *	</pre>
 *	You can leave out the navigation bar by omitting the second line of the
 *	previous example. The following convenience constructor has exactly
 *	the same effect as the earlier code:
 *	<pre>
 *	DateSelectorDialog chooser = new DateSelectorDialog(parentFrame);
 *	<pre>
 *	You can also pop up the dialog like this:
 *	<pre>
 *	chooser.setVisible(true);		// blocks until dialog closed
 *	Date d = chooser.getDateRepresentation();
 *	</pre>
 * This class is a stand-alone dialog. For a version
 * that you can embed into another window, see {@link DateSelectorPanel}.
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
 *
 * @see DateSelector
 * @see DateSelectorPanel
 * @see DateInput
 * @see com.holub.ui.InteractiveDate
 * @see NavigableDateSelector
 * @see TitledDateSelector
 * @see PopupDialog
 */

public class DateSelectorDialog extends PopupDialog implements DateSelector
{
	private DateSelector selector = new DateSelectorPanel();

	/** Creates a dialog box with the indicated parent that holds
	 *  a standard {@link DateSelectorPanel DateSelectorPanel}
	 *  (as created using the no-arg constructor).
	 */
	public DateSelectorDialog( Frame parent )
	{	super(parent);
		selector = new NavigableDateSelector( new DateSelectorPanel() );
		init();
	}

	/* Like {@link #DateSelectorDialog(Frame),
	 * but for a {@link Dialog} parent.
	 */
	public DateSelectorDialog( Dialog parent )
	{	super(parent);
		selector = new NavigableDateSelector( new DateSelectorPanel() );
		init();
	}

	/** Creates a dialog box with the indicated parent that holds
	 *  the indicated DateSelector.
	 *  Note that the current month and year is displayed in the
	 *  dialog-box title bar, so there's no need to display it in
	 *  the selector too.
	 */
	public DateSelectorDialog( Frame parent, DateSelector toWrap )
	{	super(parent);
		selector = toWrap;
		init();
	}

	/* Like {@link #DateSelectorDialog(Frame,DateSelector),
	 * but for a {@link Dialog} parent.
	 */

	public DateSelectorDialog( Dialog parent, DateSelector toWrap )
	{	super(parent);
		selector = toWrap;
		init();
	}

	/** Code comon to all constructors
	 */
	private void init()
	{	getContentPane().add( (Container)selector, BorderLayout.CENTER );
		selector.addActionListener
		(	new ActionListener()
			{	public void actionPerformed( ActionEvent event )
				{	if( event.getID() == DateSelector.CHANGE_ACTION )
					{	setTitle( event.getActionCommand() );
					}
					else
					{	setVisible(false);
						dispose();
					}
				}
			}
		);
		((Container)selector).setVisible(true);
		pack();
	}

	/** For use when you pop up a dialog using
	 * <code>setVisible(true)</code> rather than {@link #select}.
	 * Since the dialog stays in existance
	 * until the user closes it or selects a date, so this
	 * method can be used to see what month the user has
	 * scrolled to.
	 * @return the selected date or null if the dialog was closed
	 * 			without selecting anything.
	 */
	public Date getDateRepresentation()
	{	return selector.getDateRepresentation();
	}

	/** Like getDateRepresentation(), but returns a java.util.Calendar.
	 *  Modifying this calendar has no effect on the one
	 *  displayed on the screen.
	 */

	public Calendar getCalendarRepresentation()
	{	return selector.getCalendarRepresentation();
	}

	public void	displayDate(Calendar c){selector.displayDate(c);}
	public void	displayDate(Date 	 d){selector.displayDate(d);}

	/** Add an action listner for both
	 *  {@link DateSelector#CHANGE_ACTION} and
	 *  {@link DateSelector#SELECT_ACTION} action events.
	 */
    public void addActionListener(ActionListener l)
	{	selector.addActionListener(l);
	}

	/** Remove a previously-added listener */
    public void removeActionListener(ActionListener l)
	{	selector.removeActionListener(l);
	}

	/** Pops up the chooser and blocks until the user selects
	 *  a date.
	 * @return the selected date or null if the dialog was closed
	 * 			without selecting anything.
	 */
	public Date select()
	{
		setVisible(true);
		return selector.getDateRepresentation();
	}

	public void roll(int f, boolean up)	{        selector.roll(f,up);		}
	public int  get(int f)				{ return selector.get(f);			}

	//----------------------------------------------------------------------
	public static class Test
	{	public static void main(String[] args) throws Exception
		{
			final JFrame frame = new JFrame();
			frame.getContentPane().add( new JLabel("Main Frame") );
			frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
			frame.pack();
			frame.show();

			DateSelectorDialog chooser = new DateSelectorDialog(frame);
			chooser.setLocation(10,10);
			System.out.println("Displaying Selector");

			System.out.println(chooser.select());

			// No navigation bar
			chooser = new DateSelectorDialog(frame,
									new DateSelectorPanel(1900,1,2));

			chooser.setLocation(10,10);
			System.out.println("Displaying Selector");

			System.out.println(chooser.select());

			System.exit(-1);
		}
	}
}
