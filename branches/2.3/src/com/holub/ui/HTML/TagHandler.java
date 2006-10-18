// (c) 2003 Allen I Holub. All rights reserved.
//
package com.holub.ui.HTML;

import java.util.Properties;

import javax.swing.JComponent;
import java.util.Properties;

/** Define a custom tag handler. See the documentation {@link HTMLPane}
 *  for an in-depth explanation of how to use this interface
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

public interface TagHandler
{
	/***Handle a custom tag.
	 *	@param source	  The HTMLPane that's processing the link.
	 *	@param attributes The attributes of the start tag. In the
	 *	following example,
	 *	<code>attributes.get("s1")</code> returns <code>"hello"</code>
	 *	and
	 *	<code>attributes.get("s2")</code> returns <code>"world"</code>.
	 *	<pre>
	 *	&lt;mytag s1=hello s2=world&gt;
	 *	</pre>
	 *	The following predefined attribute is also available:
	 *	<table cellpadding=4>
	 *	<tr><td valign="top"><code>HTMLPane.TAG_NAME</code></td>
	 *		<td valign="top">The name of the tag itself.
	 *	 </td>
	 *	</tr>
	 *	</table>
	 *	@return a {@link JComponent} to display in place of the tag,
	 *			or <code>null</code> if nothing is to be displayed.
	 *			The returned <code>JComponent</code> should implement
	 * 			{@link TagBehavior} to get control of
	 *			reset behavior or to supply data to the form's
	 *			submit-data set.
	 *
	 *	@see com.holub.ui.HTML.HTMLPane#addTag
	 */
	JComponent handleTag( HTMLPane source, Properties attributes);
}
