// (c) 2003 Allen I Holub. All rights reserved.
package com.holub.ui.HTML;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.net.URL;
import java.util.Properties;
import java.io.IOException;

/**
 * 	A modal dialog that holds an {@link HTMLPane}. The dialog shuts
 *  down when the user hits a button created by either an
 *  &lt;input type=submit&gt; or (HTMLPane-specific)
 *  &lt;inputAction &gt; tag.
 *  Display the dialog by calling <code>show()</code>.
 *  For example:
 *  <PRE>
 *	HTMLDialog d = new HTMLDialog( owner,
 *	                              "com/holub/ui/HTML/test/okay.html",
 *	                              "Test HTMLDialog" );
 *	if( d.popup() )	 // Dialog not cancelled
 *		d.data().list( System.out );
 * </PRE>
 * The <a href="HTMLPane.html#customTags">default custom tags</a>
 * are all preinstalled in the underlying
 * {@link HTMLPane}.
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

public class HTMLDialog extends JDialog
{
	private Properties data = null;

	private final HTMLPane pane = new HTMLPane(true);

	/** Create and initialize a modal HTMLPane.
	 *  @param owner		The Frame that "owns" this dialog (the parent).
	 *  @param htmlPath	The CLASSPATH-relative path to the .html file
	 *  					that holds the contents. For example, if CLASSPATH
	 *  					contains the directory /usr/src, then get the
	 *  					file in /usr/src/html/test.html by specifying
	 *						<code>"html/test.html"</code> (no leading slash).
	 *  @param title		Dialog-box title-bar contents.
	 *	@throws IOException if it can't open the file on the htmlPath;
	 */

	public HTMLDialog( Frame owner, String htmlPath, String title )
															throws IOException
	{	super( owner, title, true /*it's modal*/ );

		// Set up an action listener that handles the form
		// submission. [actionPerformed() is called when the
		// user hits Submit or Cancel.

		pane.addActionListener					//{=HTMLDialog.listener}
		(	new ActionListener()
			{	public void actionPerformed(ActionEvent event )
				{	HTMLDialog.this.setVisible(false);
					HTMLDialog.this.dispose();
					data = ((HTMLPane.FormActionEvent)event).data();
				}
			}
		);

		// Load the input file from a CLASSPATH-relative directory
		// specified in the htmlPath argument, then import it into
		// the HTMLDialog for display.

		URL loadFrom = getClass().getClassLoader().getResource(htmlPath);
		if( loadFrom == null )
		{	throw new IOException("Can't find $CLASSPATH/" + htmlPath );
		}

		pane.setPage( loadFrom ); 			// {=HTMLDialog.load}
		getContentPane().add( pane );
		pack();
	}

	/** Get a {@link java.util.Properties} object that holds the
	 *  data provided by the &lt;input&gt; elements on the form
	 *  (or equivalent). The key is the element "name," the value
	 *  is either the value specified in the attribute or the
	 *  data the user typed.
	 *
	 *  @return the form data
	 *  @throws java.lang.IllegalStateException if you try to call this
	 *  	method before the user submits the form. This can only
	 *  	happen if <code>data()</code> is called from a thread
	 *  	other than the one that issued the <code>popup()</code>
	 *  	request (which blocks).
	 */

	public	Properties data()
	{	if( data == null )
			throw new java.lang.IllegalStateException(
					"Tried to access data before form was submitted");
		return data;
	}

	/** Add custom-tag processing to this dialog. Passes arguments through
	 *  to a contained HTMLPane's {@link HTMLPane#addTag addTag(...)} method.
	 *  A few out-of-the-box custom tags are already implemented for you
	 *  (see {#link HTMLPane}).
	 *
	 * @param tag
	 * @param handler
	 */
	public void addTag( String tag, TagHandler handler )
	{   pane.addTag(tag,handler);
	}

	/** Works just like {@link JFrame#show}, but returns true if the
	 *  dialog was closed with a normal close button. Returns false if
	 *  the user submitted the dialog by pressing a button specified with
	 *  the tag
	 *  <PRE>
	 *  &lt;inputAction name=cancel ... &gt;
	 *  </PRE>
	 *  (<a href=HTMLPane.java#inputAction>See</a>)
	 *  or just closed the dialog by clicking the "close" icon.
	 */
	public boolean popup()
	{	show();
		if( data == null )  // dialog aborted with "close" icon
			return false;   // treat it like a cancel.

		String cancel = data.getProperty("cancel");
		if( cancel == null )	// no cancel button
			return true;

		return !cancel.equals("true"); // true ==> cancel button pressed,
									   // so return false
	}

	static private class Test
	{
		public static void main( String[] args ) throws Exception
		{
			JFrame owner = new JFrame();
			owner.getContentPane().add( new JLabel("Parent Frame") );
			owner.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			owner.pack();
			owner.show();

			HTMLDialog d = new HTMLDialog(owner,
						"com/holub/ui/HTML/test/okay.html", "Test HTMLDialog");

			if( d.popup() )
				System.out.println("OK Pressed");
			else
				System.out.println("Cancel Pressed");

			d.data().list( System.out );
		}
	}
}
