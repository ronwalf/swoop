// (c) 2003 Allen I Holub. All rights reserved.
package com.holub.ui.HTML;

import java.util.*;
import javax.swing.text.Element;
import javax.swing.text.BadLocationException;
import javax.swing.text.AttributeSet;
import javax.swing.text.html.HTML;
import javax.swing.text.Segment;
import javax.swing.JEditorPane;

/** Methods in this class are useful for debugging.
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

class HTMLUtils
{
	/** Pring the contents of the element passed in as an argument
	 *  to standard output.
	 */
	private static boolean dump_Element( Element element, HTMLPane requester )
	{	System.out.println
		( "<"
		  + (element.getAttributes().getAttribute(HTML.Attribute.ENDTAG)
														!= null ? "/" : "")
		  + element.getName() 			 + "> (instanceof "
		  + element.getClass().getName() + ")"
		);

		AttributeSet attributes = element.getAttributes();

		Enumeration enum_= attributes.getAttributeNames();
		while( enum_.hasMoreElements() )
		{	Object attributeName	= enum_.nextElement();
			Object attributeValue	= attributes.getAttribute(attributeName);

			System.out.println
			(	"\t"		+ attributeName.toString()
				+ "\t-->\t"	+ attributeValue.toString()
				+ " ("		+ attributeValue.getClass().getName() +")"
			);
		}

		String content = getContent( element, requester );
		if( content.length() > 0 )
			System.out.println( "\tText content: [" + content +"]" );

		return true;
	}

	// Return the text content of the current element. Useful
	// primarily for a <code>&lt;content&gt;</code> element found
	// inside a custom tag.
	//	@param element	The element containing the text.
	//	@param requester The JEditorPane that's handling the input.
	//
	//  @return the text content or an empty (not null) string if
	//  		there isn't any.

	private static String getContent( Element element, HTMLPane requester )
	{	try
		{	int start = element.getStartOffset();
			int end   = element.getEndOffset();
			if(end > start)
			{	Segment segment = new Segment();
				requester.getDocument().getText( start, end - start, segment );
				return segment.toString().trim();
			}
		}
		catch(BadLocationException e)
		{	e.printStackTrace();
		}

		return "";
	}
}
