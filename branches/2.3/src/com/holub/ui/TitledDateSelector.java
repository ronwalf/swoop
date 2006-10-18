// (c) 2003 Allen I Holub. All rights reserved.
package com.holub.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

import java.util.Date;
import java.util.Calendar;

import com.holub.ui.DateSelector;

/************************************************************************
 *  This class is a GoF "Decorator" that augements the "raw"
 *  </code>DateSelectorPanel</code> with
 *  a title that displays the month name and year.
 *  The title updates automatically as the user navigates.
 *  Here's a picture:
 *  <blockquote>
 *	<img style="border: 0 0 0 0;" src="../../../images/TitledNavigableDateSelector.gif">
 *  </blockquote>
 *  Create a titled date selector like this:
 *  <pre>
 *  DateSelector selector = new DateSelectorPanel(); // or other constructor.
 *  selector = new TitledDateSelector(selector);
 *  </pre>
 *  This wrapper absorbs the {@link DateSelector#CHANGE_ACTION}
 *  events: listeners that you register on the wrapper will be sent
 *  only {@link DateSelector#SELECT_ACTION} events.
 *  (Listeners that are registered on the wrapped
 *  <code>DateSelector</code> object will be notified of all events,
 *  however.
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
 *  @see NavigableDateSelector
 */

public class TitledDateSelector extends JPanel implements DateSelector
{	private 	  DateSelector selector;
	private final JLabel title = new JLabel("XXXX");

	/** Wrap an existing DateSelector to add a title bar showing
	 *  the displayed month and year. The title changes as the
	 *  user navigates.
	 */

	public TitledDateSelector( DateSelector selector ) //{=TitledDateSelector.ctor}
	{	this.selector = selector;

		title.setHorizontalAlignment(SwingConstants.CENTER);
		title.setOpaque		( true 										);
		title.setBackground	( com.holub.ui.Colors.LIGHT_YELLOW 			);
		title.setFont		( title.getFont().deriveFont( Font.BOLD )	);

		selector.addActionListener //{=TitledDateSelector.listener}
		(	new ActionListener()
			{	public void actionPerformed( ActionEvent e )
				{	if( e.getID() == DateSelectorPanel.CHANGE_ACTION )
						title.setText( e.getActionCommand() );
					else
						mySubscribers.actionPerformed(e);
				}
			}
		);

		setOpaque(false);
		setLayout( new BorderLayout() );
		add( title,	 BorderLayout.NORTH	);
		add( (JPanel)selector, BorderLayout.CENTER );
	}

	/** This constructor lets you specify the background color of the
	 *  title strip that holds the month name and year (the default
	 *  is light yellow).
	 *
	 *  @param labelBackgroundColor the color of the title bar, or
	 *  	null to make it transparent.
	 */
	public TitledDateSelector( DateSelector selector, Color labelBackgroundColor )
	{	this(selector);
		if( labelBackgroundColor == null )
			title.setOpaque( false );
		else
			title.setBackground( labelBackgroundColor );
	}

	private ActionListener mySubscribers = null;
	public synchronized void addActionListener(ActionListener l)
	{	mySubscribers = AWTEventMulticaster.add(mySubscribers, l);
	}
	public synchronized void removeActionListener(ActionListener l)
	{	mySubscribers = AWTEventMulticaster.remove(mySubscribers, l);
	}

	public Calendar getCalendarRepresentation()	{ return selector.getCalendarRepresentation();}
	public Date     getDateRepresentation()		{ return selector.getDateRepresentation();	}
	public void		displayDate( Calendar c){        selector.displayDate(c);		}
	public void		displayDate( Date 	  d){        selector.displayDate(d);		}
	public void     roll(int f, boolean up)	{        selector.roll(f,up);			}
	public int      get(int f)				{ return selector.get(f);				}
}
