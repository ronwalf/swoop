// (c) 2003 Allen I Holub. All rights reserved.
package com.holub.ui;

import javax.swing.*;
import java.util.Date;
import java.text.*;

import java.awt.event.*;	// for testing
import javax.swing.*;		// for testing

import com.holub.ui.Input;
import com.holub.tools.DateUtil;

/***
 *  This convenience class customizes {@link Input} to handle date input.
 *  It provides a {@link Input.Customizer} and also appropriate constructors
 *  and accessors.
 *  <p>
 *  Note that you can use both the {@link #toString} overload and base-class
 *  {@link #getText} methods to get the contents of the control. They behave
 *  slightly differently, though. <code>getText()</code> returns exactly what
 *  the user typed. This string is garanteed to be valid input to
 *  {@link DateUtil#parseDate}, but may not be correctly recognized by
 *  {@link DateFormat#parse DateFormat.parse()}. The string returned from
 *  <code>toString()</code> is in a cannonical form that's guaranteed to
 *  be recognizable by both of the foregoing methods. This string is not
 *  usually what the user typed; rather, it's a string representation of
 *  the date specified by the user.
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
 * @see com.holub.ui.DateSelectorPanel
 * @see com.holub.ui.DateSelectorDialog
 * @see com.holub.ui.InteractiveDate
 */

public class DateInput extends Input
{
	private static final DateFormat formatter = DateFormat.getDateInstance( DateFormat.MEDIUM );

	/** Create a DateInput object for the given Date.
	 */

	public DateInput( Date source, final BorderStyle border, final boolean isHighlighted )
	{	super( formatter.format(source), new Behavior(), border, isHighlighted);
	}

	/** Create a DateInput object for the Date represented in the given string.
	 *  @param value String specifying the date. Extended formats described
	 *              in {@link com.holub.tools.DateUtil#parseDate} are
	 *              recognized. Use <code>null</code> or <code>""</code>
	 *              for today's date.
	 */

	public DateInput( String value, final BorderStyle border, final boolean isHighlighted )
	{	super( (value==null || value.length()== 0)
		            ? formatter.format(new Date())
		            : value, new Behavior(), border, isHighlighted);
	}

	/** Creates a DateInput input field initialized to today, boxed and not
	 *  highlighted.
	 */
	public DateInput()
	{	this( new Date(), Input.BOXED, false );
	}

	/** Creates a DateInput input field initialized to the date specified
	 *  in the argument, boxed and not highlighted.
	 */
	public DateInput( Date d )
	{	this( d, Input.BOXED, false );
	}

	/** Creates a DateInput field initialized to the date specified
	 *  in the argument, boxed and not highlighted.
	 */
	public DateInput( String str )
	{	this( str, Input.BOXED, false );
	}

	/** Return the value of the current Input object as a Date.
	 * @return the value
	 */
	public Date value()
	{	return DateUtil.parseDate( getText() );
	}

	/** Return the value of the current Input object. This is
	 *  the string that the user typed, but is guaranteed to
	 *  be recognisable by the {@link DateFormat#parse}. This
	 *  method returns the same value as {@link #getText}.
	 *
	 * Return the string the user typed translated into a
	 * "canonical" form that the DateFormat class will
	 * recognize. I do this because the internal processor
	 * recognizes a few comon input formats that aren't handled
	 * properly by {@link DateFormat}. The {@link #getText} method
	 * returns the string the user <em>actually</em>typed.
	 *
	 * @return A string representing a date the user typed.
	 */
	public String toString()
	{
		return formatter.format( DateUtil.parseDate(getText()) );
	}

	/** Replace the current value with <code>source</code>
	 *  @return the old value or null if the old
	 *          value was empty or otherwise invalid.
	 */
	public Date  assign( Date  source )
	{   String old = getText();
		setText( formatter.format(source) );
		Date value =  DateUtil.parseDate(old);
		return value;
	}

	/** An implemenation of {@link Input.Customizer} for dates.
	 */

	public static class Behavior implements Customizer
	{
		/** Check if input is valid. */
		public boolean isValid(String inputString)
		{	return DateUtil.parseDate(inputString) != null;
		}

		public String help( String badInput )
		{	return "You must enter a date in one of the following formats:<br>\n"
						+ DateUtil.supportedDateFormats(true);
		}

		public void prepare( JTextField current )
		{	current.setToolTipText("Enter a Date");
		}

		public boolean validatesOnExit(){ return true; }
	}

	//----------------------------------------------------------------------

	public static class Test
	{	public static void main( String[] args )
		{	JFrame frame = new JFrame("DateInput Test");
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			BoxLayout layout = new BoxLayout(
					frame.getContentPane(), BoxLayout.Y_AXIS);
			frame.getContentPane().setLayout(layout);

			final DateInput in1 = new DateInput();
			frame.getContentPane().add( in1 );

			final DateInput in2 = new DateInput((String)null);
			frame.getContentPane().add( in2 );

			final DateInput in3 = new DateInput("");
			frame.getContentPane().add( in3 );

			final DateInput in4 = new DateInput("1/2/34");
			frame.getContentPane().add( in4 );

			ActionListener reporter = new ActionListener()
			{   public void actionPerformed(ActionEvent e)
				{	System.out.println("----------------------------");
					System.out.println( "value: in1=" + in1.value() );
					System.out.println( "value: in2=" + in2.value() );
					System.out.println( "value: in3=" + in3.value() );
					System.out.println( "value: in4=" + in4.value() );
				}
			};
			in1.addActionListener(reporter);
			in2.addActionListener(reporter);
			in3.addActionListener(reporter);
			in4.addActionListener(reporter);

			frame.addWindowListener
			(	new WindowAdapter()
				{	public void windowClosing( WindowEvent e )
			        {	System.out.println( "value: in1=" + in1.value() );
			        	System.out.println( "value: in2=" + in2.value() );
			        	System.out.println( "value: in3=" + in3.value() );
			        	System.out.println( "value: in4=" + in4.value() );
				        System.out.println( "");

				        System.out.println( "text: in1=" + in1.getText() );
					    System.out.println( "text: in2=" + in2.getText() );
					    System.out.println( "text: in3=" + in3.getText() );
					    System.out.println( "text: in4=" + in4.getText() );
				        System.out.println( "");

				        System.out.println( "string: in1=" + in1.toString() );
				        System.out.println( "string: in2=" + in2.toString() );
				        System.out.println( "string: in3=" + in3.toString() );
				        System.out.println( "string: in4=" + in4.toString() );
				        System.exit(0);
					}
				}
			);

			frame.pack();
			frame.show();
		}
	}
}
