// (c) 2003 Allen I Holub. All rights reserved.
package com.holub.ui;

import javax.swing.*;
import javax.swing.event.*;
import java.text.*;

import java.awt.*; 	  		// for testing
import java.awt.event.*;	// for testing
import javax.swing.*;		// for testing

import com.holub.ui.Input;

/** This convenience class customizes {@link Input} to handle numbers.
 *  It provides a Customizer and also numeric constructors and
 *  accessors.
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

public class NumericInput extends Input
{
	/** Default precision is two decimal places */
	public NumericInput(double value, final BorderStyle border, final boolean isHighlighted)
	{	super( asString( value,2 ), new Behavior(), border, isHighlighted);
	}

	public NumericInput(long value, final BorderStyle border, final boolean isHighlighted)
	{	super( asString(value), new Behavior(), border, isHighlighted);
	}

	/** Default precision is two decimal places */
	public NumericInput(double value, Customizer c, final BorderStyle border, final boolean isHighlighted)
	{	super(asString( value, 2 ), c, border, isHighlighted);
	}

	public NumericInput(long value, Customizer c, final BorderStyle border, final boolean isHighlighted)
	{	super( asString(value), c, border, isHighlighted);
	}

	public NumericInput(double value, double min, double max, int precision,
														final BorderStyle border, final boolean isHighlighted)
	{	super(asString(value, precision), new Behavior(min,max,precision), border, isHighlighted);
	}

	public NumericInput(long value, double min, double max, int precision,
														final BorderStyle border, final boolean isHighlighted)
	{	super(asString( value ), new Behavior(min,max,precision), border, isHighlighted);
	}

	public static String asString( double value, int precision )
	{
		NumberFormat formatter = NumberFormat.getInstance();
		formatter.setMinimumFractionDigits( precision );
		return formatter.format( value );
	}

	public static String asString( long value )
	{
		NumberFormat formatter = NumberFormat.getInstance();
		return formatter.format( value );
	}

	/** Convenience method, hides call to {@link NumberFormat#parse}.
	 *  @return 0.0 if the contents of the field isn't a number.
	 */
	public double doubleValue()
	{	try
		{	return NumberFormat.getInstance().parse(getText()).doubleValue();
		}
		catch( ParseException e ){ return 0.0; }
	}

	/** Convenience method, hides call to {@link NumberFormat#parse}.
	 *  @return 0.0 if the contents of the field isn't a number.
	 */
	public float floatValue()
	{	try
		{	return NumberFormat.getInstance().parse(getText()).floatValue();
		}
		catch( ParseException e ){ return 0.0F; }
	}

	/** Convenience method, hides call to {@link NumberFormat#parse}.
	 *  @return 0L if the contents of the field isn't a number.
	 */
	public long longValue()
	{	try
		{	return NumberFormat.getInstance().parse(getText()).longValue();
		}
		catch( ParseException e ){ return 0L; }
	}

	/** Convenience method, hides call to {@link NumberFormat#parse}.
	 *  @return 0 if the contents of the field isn't a number.
	 */
	public int intValue()
	{	try
		{	return NumberFormat.getInstance().parse(getText()).intValue();
		}
		catch( ParseException e ){ return 0; }
	}

	/** Replace the current value with v. Return the old value */
	public long  assign( long   v )
	{	long old = longValue();
		setText(NumberFormat.getInstance().format(v));
		return old;
	}

	/** Replace the current value with v. Return the old value */
	public double assign( double v )
	{	double old = doubleValue();
		setText(NumberFormat.getInstance().format(v));
		return old;
	}

	/** An implemenation of {@link Input.Customizer} for numbers.
	 *  To be valid, the entire input string must acceptable
	 *  to NumberFormat.parse(). The number is right justified in the
	 *  field with the initial cursor position at the far right.
	 *  The default tooltip is the string
	 *  "Enter a number between <em>min</em> and <em>max</em>,"
	 *  (or just plain "Enter a number" if you didn't specify
	 *  any constraints).
	 *  The help string describes what a legal number looks like.
	 *  The number is parsed for value checking using the
	 *  NumberFormat.parse() and the default (current) Locale.
	 *  <p>
	 *  The text that you extract from the Input object will be
	 *  a legal number, but it will have commas in it. Extract
	 *  a value using {@link NumberFormat#parse} or call one of
	 *  the numeric-extration methods (e.g. {@link #doubleValue})
	 */
	static public class Behavior implements Customizer
	{
		private final DecimalFormat formatter = (DecimalFormat)(NumberFormat.getNumberInstance());

		private double	minValue	 = -Double.MAX_VALUE;
		private double	maxValue	 =  Double.MAX_VALUE;
		private int		precision = -1;

		private boolean isUnbounded()
		{	return (   minValue == - Double.MAX_VALUE
					&& maxValue ==   Double.MAX_VALUE );
		}

		/** Create a numeric customizer with a constrained range of values.
		 * @param minValue 	the minimum legal value
		 * @param maxValue 	the maximum legal value
		 * @param precision	the maximum number of digits to
		 * 						the right of the decimal point. 0 for
		 * 						integer values, -1 for no maximum.
		 */
		public Behavior( double minValue, double maxValue, int precision )
		{	this.minValue	= minValue;
			this.maxValue	= maxValue;
			this.precision	= precision;

			if     (precision == 0) formatter.setParseIntegerOnly(true);
			else if(precision >  0) formatter.setMaximumFractionDigits(precision);
		}

		/** Create a customizer with the entire
		 *  range of values of a Double supported and no limit
		 *  on the digits to the right of the decimal.
		 */
		public Behavior(){}

		/** This customizer check for "valid" after every character
		 *  is typed.
		 */
		public boolean validatesOnExit(){ return false; }

		/** Check if the value is within limits, contains no strange
		 *  characters, and is of the proper precision.
		 */
		public boolean isValid(String inputString)
		{	ParsePosition position = new ParsePosition(0);
			Number n = formatter.parse(inputString, position);

			int  index  = position.getIndex();
			int  length = inputString.length();
			char last   = (char)inputString.charAt(length-1);
			char point	= (char)formatter.getDecimalFormatSymbols().getDecimalSeparator();
			char comma  = (char)formatter.getDecimalFormatSymbols().getGroupingSeparator();
			char minus  = (char)formatter.getDecimalFormatSymbols().getMinusSign();

			if( index==length-1 && (last==comma || last==minus || last==point) ) // it's probably okay
				return true;

			if( index != length ) // didn't end in a separator. Garbage characters in string.
				return false;

			index = inputString.indexOf( point );

			if( index > 0 && precision > 0 && (length-index > precision+1) )
				return false; // there are characters to the right of the decimal.

			double d = n.doubleValue();
			return( minValue <= d && d <= maxValue );
		}

		public String help( String badInput )
		{	StringBuffer b = new StringBuffer();
			b.append( "You tried to type: " );
			b.append( badInput );

			if( isUnbounded() )
				b.append( "<br>You must type a number.<br>" );
			else
			{	b.append("<br>You must type a number between ");
				b.append( minAndMax() );
				b.append(".<br>");
			}

			b.append( "Commas are okay.<br>" );
			if( precision == 0)
				b.append("Numbers may not contain a decimal point.<br>");
			else
			{	b.append("Numbers may contain a decimal point");
				if( precision < 0)
					b.append(".<br>");
				else
				{	b.append(", but<br>only " );
					b.append( precision );
					b.append(" digits can go the right of the decimal.");
				}
			}
			return b.toString();
		}

		public void prepare( JTextField current )
		{	current.setHorizontalAlignment(SwingConstants.RIGHT);

			if( isUnbounded() )
				current.setToolTipText("Enter a number");
			else
				current.setToolTipText("Enter a number between " + minAndMax() );

			current.setCaretPosition(current.getText().length());
		}

		private String minAndMax()
		{ return formatter.format( minValue )
				+ " and "
				+ formatter.format( maxValue );
		}
	}

	public static class Test
	{	public static void main( String[] args )
		{
			final NumericInput n1 = new NumericInput( 99,  	   -100, 100, 0, UNDERLINED,	true );
			final NumericInput n2 = new NumericInput( 123.00F,   0, 10000,  2, BOXED,			false);
			final NumericInput n3 = new NumericInput( 1234.567,  				 BORDERLESS,	false);

			n1.setColumns(10);
			n2.setColumns(10);
			n3.setColumns(10);

			n1.setColumns(5);

			JPanel panel = new JPanel();
			panel.setLayout( new FlowLayout(FlowLayout.CENTER, 10, 10) );
			panel.setBackground( Color.WHITE );
			panel.add(new JLabel("integer -100<=n<=100:"));
			panel.add(n1);
			panel.add(new JLabel("  float (two decials) 0<=n<=10000:"));
			panel.add(n2);
			panel.add(new JLabel("  unbounded float:"));
			panel.add(n3);

			JFrame frame = new JFrame();
			frame.getContentPane().setBackground( Color.WHITE );
			frame.getContentPane().setLayout( new BorderLayout() );
			frame.getContentPane().add(panel, BorderLayout.SOUTH );
			frame.pack();
			frame.show();

			frame.addWindowListener
			(	new WindowAdapter()
				{	public void windowClosing( WindowEvent e )
					{	System.out.println( "n1=" + n1.intValue()  	 );
						System.out.println( "n2=" + n2.floatValue()  );
						System.out.println( "n3=" + n3.doubleValue() );
						System.exit(0);
					}
				}
			);
		}
	}
}
