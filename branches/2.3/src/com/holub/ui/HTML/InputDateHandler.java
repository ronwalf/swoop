// (c) 2003 Allen I Holub. All rights reserved.
package com.holub.ui.HTML;

import java.util.Properties;
import java.util.Date;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

import com.holub.ui.DateInput;
import com.holub.ui.InteractiveDate;
import com.holub.tools.DateUtil;

/**
 * A handler to add date tags to {@link HTMLPane}. Tag syntax is:
 * <pre>
&lt;inputDate value="10/8/55" size="40" name=date1 readonly &gt;
 * </pre>
 * <ol>
 * <li>The date is read/write unless the readonly attribute is specified.
 * <li>The optional size= is the approximate width of the UI in columns.
 * <li>The optional value= attribute specifies a date other than "today."
 * <li>The name= attribute specifies the key in the key=value pair returned
 * 	on form submission.
 * </ol>
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

public class InputDateHandler implements TagHandler
{
	public JComponent handleTag( HTMLPane source, final Properties attributes )
	{   class Proxy extends JPanel implements TagBehavior
		{
			private final InteractiveDate value = new InteractiveDate();
			private final String           name  = attributes.getProperty("name");

			public Proxy()
			{   // assert (name != null) : "Required name attribute missing from date tag";

				String valueAttribute = attributes.getProperty("value");
				Date d = (valueAttribute==null) ? new Date() : DateUtil.parseDate(valueAttribute);
				// assert d != null : ("Illegal date specified in tag: " + value );
				value.setTime( d );

				JComponent control = value.getUi( attributes.getProperty("readonly") != null  );

				String sizeAttribute  = attributes.getProperty("size");
				if( sizeAttribute != null )
				{
					// Set the size. Assume six pixels per column with an additional
					// 15 for the button that pops up the date-selector dialog.
					// I hate to hard-code the default 19-pixel height, but there's
					// no easy way to get the actual height (control.getPreferredSize
					// returns garbage).
					int width = (sizeAttribute==null)
								? 20
								: Integer.parseInt(sizeAttribute)
								;
					Dimension current = new Dimension( (width * 6) + 15, 19 );
					control.setPreferredSize( current );
					control.setMinimumSize  ( current );
				}

				add(control);
				setBorder(null);
				setOpaque(false);
				setAlignmentY( HTMLPane.BASELINE_ALIGNMENT );
			}

			public String getFormData(){ return name + "=" + value; }
			public void   reset()   	 {}
			public void   destroy() 	 {}
		}
		return new Proxy();
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
			+	"<table border=1>"
			+	"<tr><td> Today (readonly)	</td><td align=left> <inputDate size=\"40\" name=date0 readonly> </td></tr>"
			+	"<tr><td> 10/8/55 (date1)	</td><td align=left> <inputDate value=\"10/8/55\" size=\"40\" name=date1> </td></tr>"
			+	"<tr><td> Today (date2)		</td><td align=left> <inputDate name=date2 > </td></tr>"
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
