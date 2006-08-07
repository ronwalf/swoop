// (c) 2003 Allen I Holub. All rights reserved.
package com.holub.tools;

import java.text.*;
import java.util.*;
import java.util.regex.*;

//import com.holub.io.P;  // for debugging only

/* The DateUtil utility provides several workhorse methods that
 * use Java's formatters to parse and format dates, times, etc.
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

public class DateUtil
{
	/** Return a string showing all supported time formats, useful
	 *  for help messages.
	 *  @param asHtml if true, then a <br> is used between formats
	 * rather than a newline. If false, the lines are terminated
	 * by a <code>" \n"</code>. (The space in front of the newline makes the
	 * string look reasonable, even if newlines are ignored.)
	 */
	static public String supportedDateFormats( boolean asHtml )
	{
		Date now = new Date();
		return
			  DateFormat.getDateInstance(DateFormat.FULL).format(now)
			+ (asHtml ? "<br>" : " \n")
			+ DateFormat.getDateInstance(DateFormat.LONG).format(now)
			+ (asHtml ? "<br>" : " \n")
			+ DateFormat.getDateInstance(DateFormat.MEDIUM).format(now)
			+ (asHtml ? "<br>" : " \n")
			+ DateFormat.getDateInstance(DateFormat.SHORT).format(now)
			+ (asHtml ? "<br>" : " \n")
		    + "If you leave out the year, the current year is used."
			+ (asHtml ? "<br>" : " \n")
			+ "You can use dashes instead of slashes."
			+ (asHtml ? "<br>" : " \n")
			;
	}
	/** Return a string showing all supported time formats, useful
	 *  for help messages.
	 *  @param asHtml if true, then a <br> is used between formats
	 * rather than a newline.
	 */
	static public String supportedTimeFormats( boolean asHtml )
	{
		Date now = new Date();
		return
			  DateFormat.getTimeInstance(DateFormat.FULL).format(now)
			+ (asHtml ? "<br>" : " \n")
			+ DateFormat.getTimeInstance(DateFormat.LONG).format(now)
			+ (asHtml ? "<br>" : " \n")
			+ DateFormat.getTimeInstance(DateFormat.MEDIUM).format(now)
			+ (asHtml ? "<br>" : " \n")
			+ DateFormat.getTimeInstance(DateFormat.SHORT).format(now)
			;
	}

	/** Parse the date from a string and return it. This method tries
	 *  all the formats supported by {@link DateFormat} (FULL, LONG,
	 *  MEDIUM, and SHORT in that order). It also recognises a few
	 * formats not handled by the <code>DateFormat</code> class.
	 *  <p> In an attempt to make date entry a bit more user friendly,
	 * In particular, the following are recognized correctly:
	 * <table border=1 cellspacing=0 cellpadding=1>
	 * <tr><td> Input </td><td> Recognized as if they were: </td></tr>

	 * </table>
	 *
	 *  @return an initialized {@link java.util.Date} object or
	 *              <code>null</code> if it can't parse the date.
	 *              (It does not throw <code>ParseException</code>)
	 */

	static public Date parseDate( String str )
	{   // assert str != null;

		if( str.length() == 0 )
			return null;

		str = str.replace('-', '/');			// just in case.

		Matcher m = dayFirst.matcher( str );	// 1 Jan instead of Jan 1
		if( m.find() )
		{   str = m.replaceFirst(m.group(2) + " " + m.group(1));
		}

		m = noYear.matcher( str );		// add a year to 10/15 format.
		if( m.find() )
			str += ( "/" + thisYear );

		Date parsed = tryToParse(str);
		if( parsed == null )
			parsed =  tryToParse( str + ", " + thisYear ); // try adding a year
		return parsed;
	}

	static private Date tryToParse( String str )
	{
		Date date = null;

		try
		{	DateFormat formatter = 	DateFormat.getDateInstance(DateFormat.FULL);
			formatter.setLenient( true );
			date = formatter.parse(str);
		}
		catch (ParseException pel) { try
		{	DateFormat formatter = 	DateFormat.getDateInstance(DateFormat.LONG);
			formatter.setLenient( true );
			date = formatter.parse(str);
		}
		catch (ParseException pe2) { try
		{	DateFormat formatter = 	DateFormat.getDateInstance(DateFormat.MEDIUM);
			formatter.setLenient( true );
			date = formatter.parse(str);
		}
		catch (ParseException pe3) { try
		{	DateFormat formatter =	DateFormat.getDateInstance(DateFormat.SHORT);
			formatter.setLenient( true );
			date = formatter.parse(str);
		}
		catch( ParseException pe4 )
		{   return null;
		}
		}}}

		return date;
	}

	private static final Pattern noYear = Pattern.compile(
											"\\s*[0-9]+\\/[0-9]+$");

	private static final Pattern dayFirst = Pattern.compile(
											"([0-9]+)\\s+([A-Za-z]+)");

	private static final String thisYear = String.valueOf(
							Calendar.getInstance().get(Calendar.YEAR));


	/** Parse the time from a string and return it. This method tries
	 *  all the formats supported by {@link DateFormat} (FULL, LONG,
	 *  MEDIUM, and SHORT in that order).
	 *  @return an initialized {@link java.util.Date} object or
	 *  <code>null</code> if it can't parse the time.
	 *  (It does not throw <code>ParseException</code>)
	 */
	static public Date parseTime(String str)
	{
		// assert str != null;

		Date date = null;
		try
		{	date = DateFormat.getTimeInstance(DateFormat.FULL).
															parse(str);
		}
		catch (ParseException pel)
		{	try
			{	date = DateFormat.getTimeInstance(DateFormat.LONG).
															parse(str);
			}
			catch (ParseException pe2)
			{	try
				{	date = DateFormat.getTimeInstance(DateFormat.MEDIUM).
															parse(str);
				}
				catch (ParseException pe3)
				{	try
					{	date = DateFormat.
								getTimeInstance(DateFormat.SHORT).
															parse(str);
					}
					catch( ParseException pe4 ){}
				}
			}
		}
		return date;
	}

	/** Return a string holding just the date. The following format is used:
	 *  <pre>
	 *	Thursday, May 29, 2003
	 *  </pre>
	 */
	static public String asString( Date date )
	{	return DateFormat.getDateInstance(DateFormat.FULL).format(date);
	}

	/** Return a string holding just the time. The following format is used:
	 *  <pre>
	 *	11:42:44 AM
	 *  </pre>
	 */
	static public String timeAsString( Date time )
	{	return DateFormat.getTimeInstance(DateFormat.MEDIUM).format(time);
	}

	/** Return a string holding the time and date in the form:
	 *  <pre>
	 *	Thursday, May 29, 2003 11:42:44 AM
	 *  </pre>
	 */
	static public String timestamp( Date dateAndTime )
	{	return DateFormat.getDateTimeInstance(	DateFormat.FULL,
												DateFormat.MEDIUM).
												format(dateAndTime);
	}

/*
	 *	Return a string holding a fomatted date or time.
	 *  The <code>format</code> argument specifies what
	 *  the returned string looks like. Most characters in the
	 *  format string are placed in the output, however the
	 *  following character sequences are replaced as follows:
	 *  <table border=1 cellspacing=0 cellpadding=1>
	 *  <tr><td> %m </td><td>Month (numeral)			</td></tr>
	 *  <tr><td> %M </td><td>Month (word)				</td></tr>
	 *  <tr><td> %d </td><td>day of the month (numeral)	</td></tr>
	 *  <tr><td> %y </td><td>year (two digits)			</td></tr>
	 *  <tr><td> %Y </td><td>year (four digits)			</td></tr>
	 *  <tr><td> %w </td><td>Day of week as 3-character abbreviation</td></tr>
	 *  <tr><td> %W </td><td>Day of week spelled out	</td></tr>
	 *  </table>
	 *
	 *  @format Format-specifier string.

	static public String format( String specifier, Date d )
	{	StringBuffer b			= new StringBuffer();
		int			 end		= specifier.length;
		Calendar	 calendar	= new Calendar(d);

		for( int i = 0; i < end; ++i )
		{	char c = specifier.charAt(i);
			if( c != '%' )
				b.append(c);
			else if( ++i < end )
			{	c = specifier.charAt(i);
				switch(c)
				{
				case 'm':
				case 'M':
				case 'd':
				case 'y':
				case 'Y':
				case 'w':
				case 'W':
				}
			}
		}
	}
*/

	private static class Test
	{	public static void main( String[] args )
		{/*
			P.rintln("Supported date formats are: ");
			P.rintln( supportedDateFormats(false) );

			P.rintln("\nSupported time formats are: ");
			P.rintln( supportedTimeFormats(false) );

			P.rintln("");

			Date now = new Date();
			P.rintln( asString(now) );
			P.rintln( timeAsString(now) );
			P.rintln( timestamp(now) );

			String[] tests = new String[]
			{   "1/2/34",
				"January 2, 1934",
				"1/2",
				"January 2",
				"2 January, 1934",
				"2 January",
			    "Oct 8",
			};

			for( int i = 0; i < tests.length;  ++i )
            {	Date then = parseDate( tests[i] );
				if( then == null )
					P.rintln( "FAILURE: parseDate(" + tests[i] + ")" );
				else
					P.rintln( "SUCCESS: parseDate(" + tests[i] + "): " + asString(then) );
			}

			// assert parseDate( ""   ) == null : "FAILURE: Parsing empty string";
			// assert parseDate( null ) == null : "FAILURE: Parsing null string";
		*/}
	}
}
