// (c) 2003 Allen I Holub. All rights reserved.
package com.holub.ui.HTML;

import java.io.Reader;

/************************************************************************
 *	A hook for preprocessing input before the HTML parser sees it. The
 *	<code>inputFilter()</code> override is passsed the {@link Reader}
 *	that the raw input comes from. It should return a <code>Reader</code>
 *	for the parser to use. The returned <code>Reader</code>
 *	presumably does some processing on the raw input. Register
 *	implementations of this interface with
 *	{@link HTMLPane#filterInput filterInput(...)}.
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

public interface FilterFactory
{	Reader inputFilter( final Reader r );

	public static FilterFactory NULL_FACTORY =
		new FilterFactory()
		{	public Reader inputFilter(final Reader r)
			{	return r;
			}
		};
}
