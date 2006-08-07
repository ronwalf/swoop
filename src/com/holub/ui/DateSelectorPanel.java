// (c) 2003 Allen I Holub. All rights reserved.
package com.holub.ui;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.event.*;
import java.awt.*;
import java.util.Date;
import java.util.Calendar;
import java.util.Locale;
import java.text.DateFormatSymbols;
import java.net.URL;

/***
 * 	A calendar-dispaly/date-selection widget.
 *  <p>
 *  Here's what it looks like:
 *  <blockquote>
 *	<img src="../../../images/DateSelector.gif">
 *  </blockquote>
 *	"Today" is highlighted.
 *	Select a date by clicking on it.
 *	The background is transparant by default &mdash; it's grey here because
 *	the underlying window is grey.
 *	<p>
 	<img src="../../../images/NavigableDateSelector.gif">
 *  This "raw" date selector can be "decorated" in several
 *  ways to make it more useful.
 *  First, you can add a navigation bar to the bottom
 *	to advances the
 *	calandar by one month (single arrow) or one year (double arrow)
 *	forwards (right-pointing arrow)	or backwards (left-pointing arrow).
 *	"Today" is highlighted.
 *	Navigation bars are specified using a Gang-of-Four "Decorator"
 *	object that wraps the raw <code>DateSelectorPanel</code>
 *	Both the wrapper and the underlying panel implement the
 *	<code>DateSelectory</code> interface, so can be use
 *	used interchangably. The following code creates the
 *	date selector at right.
 *	<pre>
 *	DateSelector selector = new DateSelectorPanel();
 *	selector = new NavigableDateSelector( selector );
 *	</pre>
 *	The same thing can be accomplished with a convenience constuctor that
 *	creates the wrapped DateSelectorPanel for you:
 *	<pre>
 *	DateSelector selector = new NavigableDateSelector();
 *	</pre>
 *	<p>
 *	<img src="../../../images/TitledNavigableDateSelector.gif">
 *	The other augmentation of interest is a title that shows the
 *  month name and year that's displayed. (there's an example at right).
 *  Use the same decoration strategy as before to add the title:
 *	<pre>
 *	DateSelector selector = new DateSelectorPanel();
 *	selector = new NavigableDateSelector( selector );
 *	selector = new TitledDateSelector   ( selector );
 *	</pre>
 *	You can leave out the navigation bar by ommiting the
 *	second line of the foregoing code.
 *	Again, a convenience constructor is provided to create a
 *	titled date selector (without the navigation bar) as follows:
 *	<pre>
 *	DateSelector selector = new TitledDateSelector();
 *	</pre>
 *	<p>
 *	<img src="../../../images/DateSelectorDialog.gif">
 *	The final variant is the lightweight popup dialog shown at right.
 *	It can be dragged around by the title bar (though dragging can
 *	be disabled) and closed by clicking on the "close" icon on the
 *	upper right. As before, use a decorator to manufacture a dialog:
 *	<pre>
 *	DateSelector selector = new DateSelectorPanel();
 *	selector = new NavigableDateSelector( selector ); // add navigation
 *	selector = new DateSelectorDialog   ( selector );
 *	</pre>
 *	Note that you don't need a title because one is supplied for you
 *	in the dialog-box title bar. Also as before, a convenience
 *	constructor to create a navigable dialog box like the one at
 *	right:
 *	<pre>
 *	DateSelector = new DateSelectcorDialog();
 *	<pre>
 *	All the earlier examples create a claendar for the current
 *	month. Several methods are provided, below, to change the date
 *	in your program. For the most part, they work like simliar
 *	methods of the {@link Calendar} class.
 * <DL>
 * <DT><b>Revisions</b>
 * <DD>
 * 2003/6/9: Allen Holub added a column heading holding two-character
 *			 day-name abbreviations.
 * <br>
 * </DD>
 *
 * </DL>
 * <DL>
 * <DT><b>Known Problems</b>
 * <DD>
 * The month and day names are hard coded (in English). Future versions
 * will load these strings from a resource bundle. The week layout
 * (S M T W Th F Sa Su) is the default layout for the underlying
 * {@link Calendar}, which should change with Locale as appropriate.
 * This feature has not been tested, however.
 * </DD>
 * </DL>
 *
 * </DL>
 * <DL>
 * <DT><b>Revisions</b>
 * <DD>
 * <table cellspacing=0 cellpadding=3>
 * <tr>	<td>2003-07-10</td>
 * 		<td>Added day-of-week labels to tops of columns.
 * 		</td>
 * </tr>
 * <tr>	<td>2003-07-17</td>
 * 		<td>Modified to use Java's DateFormatSymbols class to get
 * 			month and weekday names for current default locale.
 * 		</td>
 * </tr>
 * </table>
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
 *
 *	@see com.holub.ui.DateSelector
 *	@see com.holub.ui.DateSelectorDialog
 *  @see com.holub.ui.DateInput
 *  @see com.holub.ui.InteractiveDate
 *	@see com.holub.ui.NavigableDateSelector
 *	@see com.holub.ui.TitledDateSelector
 */

public class DateSelectorPanel extends JPanel implements DateSelector
{
	private static String[] weekdays;
	private static String[] months;
	static
	{	// Initialize weekdays and months with Local-specific
		// strings.

		DateFormatSymbols symbols
			= new DateFormatSymbols( Locale.getDefault() );
		months	 = symbols.getMonths();
		weekdays = symbols.getShortWeekdays();
	}

	private static final int DAYS_IN_WEEK = 7,	// days in a week
							 MAX_WEEKS    = 6;	// maximum weeks in any month

	private Calendar calendar = Calendar.getInstance();
	{	calendar.set( Calendar.HOUR, 	0 );
		calendar.set( Calendar.MINUTE,	0 );
		calendar.set( Calendar.SECOND,	0 );
	}

	// The calendar that's displayed on the screen

	private final Calendar today = Calendar.getInstance();

	// An ActionListener that fields all events coming in from the
	// calendar
	//
	private final ButtonHandler dayListener  = new ButtonHandler();

	// "days" is not a two-dimensional array. I drop buttons into
	// a gridLayout and let the layout manager worry about
	// what goes where. The first buttion is the first day of the
	// first week on the grid, the 8th button is the first day of the
	// second week of the grid, and so forth.

	private JButton[] days = new JButton[ DAYS_IN_WEEK * MAX_WEEKS ];
	{	for( int i = 0; i <days.length; i++ )
		{	JButton day = newDayButton("--");
			day.addActionListener( dayListener );
			day.setActionCommand("D");
			days[i] = day;
		}
	}

	public static JButton newDayButton( String text )
	{	JButton day = new JButton(text);
		day.setBorder			(new EmptyBorder(1,2,1,2));
		day.setFocusPainted		(false);
		day.setOpaque			(false);
		return day;
	}

	public static JButton newDayButton( char c )
	{	return newDayButton( "" + c );
	}

	private JButton[] dayNames =
	{	newDayButton( weekdays[1].charAt(0) ),
		newDayButton( weekdays[2].charAt(0) ),
		newDayButton( weekdays[3].charAt(0) ),
		newDayButton( weekdays[4].charAt(0) ),
		newDayButton( weekdays[5].charAt(0) ),
		newDayButton( weekdays[6].charAt(0) ),
		newDayButton( weekdays[7].charAt(0) )
	};

	/** Create a DateSelector representing the current date.
	 */
	public DateSelectorPanel()						//{=DateSelectorPanel.noarg}
	{
		JPanel calendarDisplay = new JPanel();
		calendarDisplay.setOpaque(false);
		calendarDisplay.setBorder( BorderFactory.createEmptyBorder(5,3,0,1) );

		// Need enough rows to hold the maximum number of weeks in a month
		// plus one more for the week names.
		calendarDisplay.setLayout(new GridLayout(  MAX_WEEKS + 1 /*rows*/,
													DAYS_IN_WEEK /*columns*/ ));
		for( int i = 0; i < dayNames.length; ++i ) //{=DateSelectorPanel.init.grid}
			calendarDisplay.add(dayNames[i]);

		for( int i = 0; i < days.length; ++i )
			calendarDisplay.add(days[i]);

		setOpaque( false );
		setLayout( new BorderLayout() );
		add(calendarDisplay, BorderLayout.CENTER);
		updateCalendarDisplay();
	}

	/** Create a DateSelectorPanel for an arbitrary date.
	 *  @param initialDate Calendar will display this date. The specified
	 *  					date is highlighted as "today".
	 *  @see #DateSelectorPanel(int,int,int)
	 */

	public DateSelectorPanel(Date initialDate)
	{	this();
		calendar.setTime( initialDate );
		today.	 setTime( initialDate );
		updateCalendarDisplay();
	}

	/** Create a DateSelectorPanel for an arbitrary date.
	 * @param year the full year (e.g. 2003)
	 * @param month the month id (0=january, 1=feb, etc. [this is the
	 * 			convention supported by the other date classes])
	 * @param day the day of the month. This day will be highlighted
	 * 			as "today" on the displayed calendar. Use 0 to suppress
	 * 			the highlighting.
	 *  @see #DateSelectorPanel(Date)
	 */

	public DateSelectorPanel( int year, int month, int day )
	{	this();
		calendar.set(year,month,day);
		if( day != 0 )
			today.set(year,month,day);
		updateCalendarDisplay();
	}

	/************************************************************************
	 * List of observers.
	 */

	private ActionListener subscribers = null;

	/** Add a listener that's notified when the user scrolls the
	 *  selector or picks a date.
	 *  @see DateSelector
	 */
    public synchronized void addActionListener(ActionListener l)
	{	subscribers = AWTEventMulticaster.add(subscribers, l);
    }

	/** Remove a listener.
	 *  @see DateSelector
	 */
    public synchronized void removeActionListener(ActionListener l)
	{	subscribers = AWTEventMulticaster.remove(subscribers, l);
	}

	/** Notify the listeners of a scroll or select
	 */
	private void fire_ActionEvent( int id, String command )
	{	if (subscribers != null)
			 subscribers.actionPerformed(new ActionEvent(this, id, command) );
	}

	/***********************************************************************
	 * Handle clicks from the buttons that represent calendar days.
	 */
	private class ButtonHandler implements ActionListener
	{	public void actionPerformed(ActionEvent e)
		{
			if (e.getActionCommand().equals("D"))
			{	String text = ((JButton) e.getSource()).getText();

				if(text.length() > 0)  //  <=0 means click on blank square. Ignore.
				{	calendar.set
					(	calendar.get(Calendar.YEAR),	// Reset the calendar
						calendar.get(Calendar.MONTH),	// to be the choosen
						Integer.parseInt(text)			// date.
					);
					fire_ActionEvent( SELECT_ACTION,
										calendar.getTime().toString() );
				}
			}
		}
	}

	//----------------------------------------------------------------------

	private JButton highlighted = null;

	private void clearHighlight()
	{
		if( highlighted != null )
		{	highlighted.setBackground( Color.WHITE );
			highlighted.setForeground( Color.BLACK );
			highlighted.setOpaque(false);
			highlighted = null;
		}
	}

	private void highlight( JButton cell )
	{
		highlighted = cell;
		cell.setBackground( com.holub.ui.Colors.DARK_RED );
		cell.setForeground( Color.WHITE );
		cell.setOpaque( true );
	}
	//----------------------------------------------------------------------

	/** Redraw the buttons that comprise the calandar to display the current
	 *  month
	 */

	private void updateCalendarDisplay()
	{
		setVisible(false);	// improves paint speed & reduces flicker

		clearHighlight();

		// The buttons that comprise the calendar are in a single
		// dimentioned array that was added to a 6x7 grid layout in
		// order. Because of the linear structure, it's easy to
		// lay out the calendar just by changing the labels on
		// the buttons. Here's the algorithm used below
		//
		// 	1) find out the offset to the first day of the month.
		// 	2) clear everything up to that offset
		// 	3) add the days of the month
		// 	4) clear everything else

		int month = calendar.get(Calendar.MONTH);
		int year  = calendar.get(Calendar.YEAR);

		fire_ActionEvent( CHANGE_ACTION, months[month] + " " + year );

		calendar.set( year, month, 1 ); // first day of the current month.

		int firstDayOffset = calendar.get(Calendar.DAY_OF_WEEK);		/* 1 */

		// assert firstDayOffset < days.length;

		int i = 0;
		while( i < firstDayOffset-1 )									/* 2 */
			days[i++].setText("");

		int dayOfMonth = 1;
		for(; i < days.length; ++i )									/* 3 */
		{
			// Can't get calendar.equals(today) to work, so do it manually

			if(	calendar.get(Calendar.MONTH)==today.get(Calendar.MONTH)
			&&	calendar.get(Calendar.YEAR )==today.get(Calendar.YEAR )
			&&	calendar.get(Calendar.DATE )==today.get(Calendar.DATE ) )
			{	highlight( days[i] );
			}

			days[i].setText( String.valueOf(dayOfMonth) );

			calendar.roll( Calendar.DATE, /*up=*/ true );	// forward one day

			dayOfMonth = calendar.get(Calendar.DATE);
			if( dayOfMonth == 1 )
				break;
		}

		// Note that we break out of the previous loop with i positioned
		// at the last day we added, thus the following ++ *must* be a
		// preincrement becasue we want to start clearing at the cell
		// after that.

		while( ++i < days.length )										/* 4 */
			days[i].setText("");

		setVisible(true);
	}

	/** Create a naviagion button with an image appropriate to the caption.
	 *	The <code>caption</code> argument is used as the button's "action
	 *	command." This method is public only because it has to be.
	 *	(It overrides a public	method.) Pretend it's not here.
	 */

	public void addNotify()
	{
		super.addNotify();
		int month = calendar.get(Calendar.MONTH);
		int year  = calendar.get(Calendar.YEAR);
		fire_ActionEvent( CHANGE_ACTION, months[month] + " " + year );
	}

	/**	Returns the {@link Date Date} selected by the user or null if
	 *  the window was closed without selecting a date. The returned
	 *  Date has hours, minutes, and seconds values of 0. Modifying
	 *  the returned Date has no effect on the displayed date.
	 */

	public Date getDateRepresentation()
	{	return calendar.getTime();
	}

	/** Returns a Calendar that represents the currently selected
	 *  date. This object is not hooked up to the widget in any
	 *  way. Modifying the returned Calendar
	 *  will not affect the displayed date, for example.
	 *  This calender represents the most recently selected date.
	 */
	public Calendar getCalendarRepresentation()
	{	Calendar clone = Calendar.getInstance();
		clone.setTime( calendar.getTime() );
		return clone;
	}

	/** Display the specified date.
	 *  @param src display the date represented by this Calendar.
	 *  		   Modifying the Calendar after this call will
	 *  		   not affect the displayed date in any way.
	 */
	public void		displayDate( Calendar src )
	{	calendar.setTime( src.getTime() );
		updateCalendarDisplay();
	}

	/** Display the specified date.
	 *  @param src display the date represented by this Date object.
	 *  		   Modifying the Calendar after this call will
	 *  		   not affect the displayed date in any way.
	 */
	public void		displayDate( Date src )
	{	calendar.setTime( src );
		updateCalendarDisplay();
	}

	/** Works just like {@link Calendar#roll(int,boolean)}.  */
	public void roll(int field, boolean up)
	{	calendar.roll(field,up);
		updateCalendarDisplay();
	}

	/** Works just like {@link Calendar#roll(int,int)}.  */
	public void roll(int field, int amount)
	{	calendar.roll(field,amount);
		updateCalendarDisplay();
	}

	/** Works just like {@link Calendar#set(int,int,int)}
	 *	Sets "today" (which is higlighted) to the indicated day.
	 */
	public void set( int year, int month, int date )
	{	calendar.set(year,month,date);
		today.set(year,month,date);
		updateCalendarDisplay();
	}

	/** Works just like {@link Calendar#get(int)} */
	public int get( int field )
	{	return calendar.get(field);
	}

	/** Works just like {@link Calendar#setTime(Date)},
	 *	Sets "today" (which is higlighted) to the indicated day.
	 */
	public void setTime( Date d )
	{	calendar.setTime(d);
		today.setTime(d);
		updateCalendarDisplay();
	}

	/** Works just like {@link Calendar#getTime} */
	public Date getTime( )
	{	return calendar.getTime();
	}

	/** Return a Calendar object that represents the currently-displayed
	 *  month and year. Modifying this object will not affect the
	 *  current panel.
	 *  @return a Calendar representing the panel's state.
	 */

	public Calendar getCalendar()
	{	Calendar c = Calendar.getInstance();
		c.setTime( calendar.getTime() );
		return c;
	}

	/** Change the display to match the indicated calendar. This Calendar
	 *  argument is used only to provide the new date/time information.
	 *  Modifying it after a call to the current method will not affect
	 *  the DateSelectorPanel at all.
	 *	Sets "today" (which is higlighted) to the indicated day.
	 *  @param calendar A calendar positioned t the date to display.
	 */

	public void setFromCalendar(Calendar calendar)
	{	this.calendar.setTime( calendar.getTime() );
		today.setTime( calendar.getTime() );
		updateCalendarDisplay();
	}
	//----------------------------------------------------------------------
	private static class Test
	{	public static void main( String[] args )
		{	JFrame frame = new JFrame();
			frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
			frame.getContentPane().setLayout( new FlowLayout() );

			DateSelector left 	 = new TitledDateSelector(new NavigableDateSelector());
			DateSelector center = new NavigableDateSelector();
			DateSelector right  = new DateSelectorPanel(1900,1,2);

			((NavigableDateSelector)center).changeNavigationBarColor(
										Colors.TRANSPARENT );
			ActionListener l =
				new ActionListener()
				{	public void actionPerformed(ActionEvent e)
					{	System.out.println( e.getActionCommand() );
					}
				};

			left.addActionListener	(l);
			center.addActionListener(l);
			right.addActionListener	(l);

			JPanel white = new JPanel();		// proove that it's transparent.
			white.setBackground(Color.WHITE);
			white.add( (JPanel)center );

			// I hate these casts, but they're
			// mandated by the fact that
			// Component is not an interface.
			//
			frame.getContentPane().add( (JPanel)left    );
			frame.getContentPane().add( 		white );
			frame.getContentPane().add( (JPanel)right    );

			frame.pack();
			frame.show();
		}
	}
}
