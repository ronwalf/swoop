// (c) 2003 Allen I Holub. All rights reserved.
package com.holub.ui.HTML;

import com.holub.ui.HTML.TagHandler;
import com.holub.ui.HTML.HTMLPane;

import javax.swing.*;
import java.awt.*;
import java.util.*;

/** Handles the &lt;size...&gt; tag described in the documentation for
 *  {@link HTMLPane}.
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

public class SizeHandler implements TagHandler
{	public JComponent handleTag(HTMLPane source, Properties attributes)
	{	source.setPreferredSize
		(	new Dimension
			( Integer.parseInt(attributes.getProperty("width")),
			  Integer.parseInt(attributes.getProperty("height"))
			)
		);
		return null;
	}
}
