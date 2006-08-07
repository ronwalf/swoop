// (c) 2003 Allen I Holub. All rights reserved.

package com.holub.tools;

import java.util.logging.*;
import java.io.*;

/** This class provides a single method that sets up logging for
 *  a particular package to go to the console window with the
 *  normal timestamp header stripped of.
 *  You can accomplish a similar thing by
 *  modifying the <i>$JAVA_HOME/jre/lib/logging.properties</i>
 *  file, setting the <code>.level</code> and
 *  <code>java.util.logging.ConsoleHandler.level</code>
 *  properties to <code>ALL</code>. This change causes all messages
 *  to go to the console, but the timestamp header will appear, too.
 *	<p>Here's an example:
 *  <pre>
 *  import com.holub.tools.Log;
 *
 *  Log.toScreen("com.holub.tools");
 *  //...
 *	private static final Logger log = Logger.getLogger("com.holub.tools");
 *	log.warning( "The sky is falling!" );
 *	</pre>
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
public class Log
{	/** Send all log messages for the indicated package to
	 *	the console (System.err). The normal header (which holds the
	 *	timestamp and package name) is not printed.
	 */

	public static void toScreen( String packageName )
	{
		// Arrange for log output to be visible on the screen.

		Logger  log = Logger.getLogger(packageName);
		Handler h   = new ConsoleHandler();
		h.setLevel    ( Level.ALL );
		h.setFormatter( new Formatter()
						{	public String format(LogRecord r)
							{	return r.getMessage() + "\n";
							}
						}
					  );
		log.setUseParentHandlers(false);
		log.setLevel(Level.ALL);
		log.addHandler(h);
	}

	/** Turn off all logging for a particular package.
	 */
	public static void off( String packageName )
	{	Logger.getLogger(packageName).setLevel(Level.OFF);
	}

	/** Convenience for error messages, return a stack trace
	 *  for the indicated exception as a string. Let's you
	 *  put a stack trace into a "logged" message.
	 */

	public static String stackTraceAsString( Exception e )
	{	StringWriter out = new StringWriter();
		e.printStackTrace( new PrintWriter(out) );
		return out.toString();
	}
}
