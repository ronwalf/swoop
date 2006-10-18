// (c) 2003 Allen I Holub. All rights reserved.
//
package com.holub.ui.HTML;

/** This interface provides the wherewithal for a <code>JComponent</code>
 *  to act like an HTML &lt;input&gt; tag in an {@link HTMLPane}.
 *  <p>
 *  Note that the methods of this interface are lower level
 *  than those of the Provider. For example, the <code>destroy()</code>
 *  method will be called every time the form shuts
 *  down for whatever reason.
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
 *	@see TagBehavior.Adapter
 */

public interface TagBehavior
{	/** This method is called to get <em>name=value</em> pairs used
	 *	as form data when the user hits the Submit button.
	 *	@return a list of one or more newline-delimited
	 *		name=value pairs or an empty string (<code>""</code>)
	 *		if there is no data to add. These pairs are
	 *		added to the form data.
	 */
	public String getFormData();

	/** This method is called when the user hits the "reset" button.
	 *  It should restore the object to its initial state.
	 */
	public void   reset();

	/** This method is called when the user moves on to the next
	 *  page or the window containing the form is shut down.
	 *  Use this hook to release any global resources that
	 *  the TagBehavior object might be using.
	 */
	public void   destroy();

	/** A convenience class, implements {@link TagBehavior} with
	 *  methods that do nothing. You can extend this class instead
	 *  of implementing {@link TagBehavior} when you don't need
	 *  to override all the methods of the interface.
	 */
	public static class Adapter
	{	public String getFormData(){ return ""; }
		public void   reset(){};
		public void   destroy(){};
	}
}
