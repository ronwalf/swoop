// (c) 2003 Allen I Holub. All rights reserved.
package com.holub.ui.HTML;

import java.util.Properties;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;


/***
 * Handle the inputAction tag for an {@link HTMLPane}. Tag syntax is:
 * <PRE>
 * &lt;inputAction name="fred" value="Button Text"&gt;
 * </PRE>
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

public class InputActionHandler implements TagHandler
{
	private final HTMLPane pane;
	InputActionHandler( HTMLPane pane ){ this.pane=pane; }

	// This method is called when the <inputAction...> tag is
	// first processed, when the HTML input is imported into
	// the HTMLPane.
	//
	public JComponent handleTag(HTMLPane source, Properties attributes)
	{
		final String name  = attributes.getProperty("name");
		final String value = attributes.getProperty("value");

		// Pressed must be declared final because it's accessed
		// from an inner-class object. Implement it as a one-element
		// array so that it can nonetheless be modified.
		//
		final boolean pressed[] = new boolean[]{ false } ;

		// This class is the JComponent that will appear on the
		// screen in place of the tag.
		//
		class ButtonTag extends JButton implements TagBehavior
		{	ButtonTag(String text){ super(text); }
			public void reset(){}
			public void destroy(){}

			// Called by the HTMLPane when it assembles the
			// Properties object that holds the form data
			// in order to send that data to its listeners.
			//
			public String getFormData()
			{	
				// If the button is pressed, the form data holds
				// a name=value pair that holds the value "true"
				// The name is specified in the tag.
				
				return	(name == null)
						? ""
						: (name + "=" + pressed[0])
						;
			}
		}

		ButtonTag proxy = new ButtonTag(value);
		proxy.setAlignmentY( HTMLPane.BASELINE_ALIGNMENT );

		// Set up to handle the button click. Set the "pressed"
		// state true and call HTMLPane's handleInputActionTag(...)
		// method, which gathers up the form data and sends it
		// to the HTMLPane's listeners with an actionPerformed(...)
		// message. The form data for the "proxy" is fetched
		// from the ButtonTag object's getFormData(...) method,
		// declared about 10 lines up.
		//
		proxy.addActionListener
		(	new ActionListener()
			{	public void actionPerformed( ActionEvent e )
				{	pressed[0]=true;
					pane.handleInputActionTag(name);
				}
			}
		);
		return proxy;
	}

	private static class Test
	{	public static void main( String[] args )
		{	HTMLPane pane = new HTMLPane(true);

			pane.addActionListener
			(	new ActionListener()
				{	public void actionPerformed(ActionEvent e)
					{	HTMLPane.FormActionEvent
							event = (HTMLPane.FormActionEvent)e;

						event.data().list( System.out );
					}
				}
			);

			pane.addTag( "inputDate", new InputDateHandler() );

			pane.setText
			(	"<html>"
			+	"<head>"
			+	"</head>"
			+	"<body>"
			+	"&lt;inputAction name=myName value=\"initialValue\" &gt;"
			+	"<form>"
			+	"<table border=1>"
			+	"<tr><td> Input Action: </td><td align=left> <inputAction name=myName value=\"initialValue\" > </td></tr>"
			+	"</table>"
			+	"<br><input type=submit value=Submit>"
			+	"</form>"
			+	"</body>"
			+	"</html>"
			);

			JFrame frame = new JFrame();
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			frame.getContentPane().add( pane );
			frame.pack();
			frame.show();
		}
	}
}
