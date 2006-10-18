// (c) 2003 Allen I Holub. All rights reserved.
package com.holub.ui.HTML;

import java.util.Properties;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

import com.holub.ui.NumericInput;

/**
 * Handle the inputAction tag for an {@link HTMLPane}. Tag syntax is:
 * <pre>
 * &lt;inputNumber name="fred" value="0.0" min="0" max="100" precision="2" size="10"&gt;
 * </pre>
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

public class InputNumberHandler implements TagHandler
{	public JComponent handleTag( HTMLPane source, Properties attributes )
	{	class NumericTag extends NumericInput implements TagBehavior
		{	private String name;
			private String resetValue;

			public NumericTag( String name,
								String value, String min, String max, String precision, String size )
			{	super (
						(value==null	) ? 0.0  : Double.parseDouble (value),
						(min==null  	) ? -Double.MAX_VALUE: Double.parseDouble (min),
						(max==null  	) ? Double.MAX_VALUE : Double.parseDouble (max),
						(precision==null) ? 100 			 : Integer.parseInt	 (precision),
						BOXED,
						false
					);

				this.name = name;
				resetValue = value;
				setAlignmentY( HTMLPane.BASELINE_ALIGNMENT );
				setColumns   ( (size==null) ? 20 : Integer.parseInt(size) );
			}
			public String getFormData(){ return name + "=" + doubleValue(); }
			public void   reset()   { setText(resetValue); }
			public void   destroy() {}
		}

		JTextField control = new NumericTag(
				attributes.getProperty("name"),
				attributes.getProperty("value"),
				attributes.getProperty("min"),
				attributes.getProperty("max"),
				attributes.getProperty("precision"),
				attributes.getProperty("size")			);

		return control;
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
			+	"<form>"
			+	"&lt;inputNumber name=\"myName\" value=\"0.0\" min=\"0\" max=\"100\" precision=\"2\" size=10&gt;"
			+	"<table border=1>"
			+	"<tr><td> Input Number: </td><td align=left>"
			+	"<inputNumber name=\"myName\" value=\"0.0\" min=\"0\" max=\"100\" precision=\"2\" size=10>"
			+   "</td></tr>"
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
