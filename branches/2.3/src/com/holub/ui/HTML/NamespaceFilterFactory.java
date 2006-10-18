// (c) 2003 Allen I Holub. All rights reserved.
package com.holub.ui.HTML;

import java.io.Reader;
import java.io.IOException;

/**	A prebuilt {@link FilterFactory} that handles lets the brain-dead
 *  HTML Parser handle namespace-like tags. Looks for tags of the form
 *  &lt;holub:tagname ...&gt; and replaces the colon with an underscore.
 *  You can then provide a custom tag (with the underscore instead of the
 *  colon as the tag name) to handle the tag.
 *
 *  Install one of these using {@link HTMLPane#filterInput filterInput(...)}
 *  if want this capability.
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

public class NamespaceFilterFactory implements FilterFactory
{
	private FilterFactory sourceFilterFactory = FilterFactory.NULL_FACTORY;

	/** Create a NamespaceFilterFactory that gets its input
	 *  from the Reader returned from the sourceFilterFactory.
	 *  Use this constructor to chain filters.
	 */
	public NamespaceFilterFactory( FilterFactory sourceFilterFactory )
	{	this.sourceFilterFactory = sourceFilterFactory;
	}

	/** Convenience constructor used when you're not chaining */
	public NamespaceFilterFactory(){}

	/** Returns a Reader that decorates the <code>srcReader</code>
	 *  to replace all tags of the form &lt;package:name&gt; with
	 *  &lt;package_name&gt;.
	 */
	public Reader inputFilter( Reader srcReader )
	{
		final Reader src = (sourceFilterFactory != null)
								? sourceFilterFactory.inputFilter( srcReader )
								: srcReader
								;
		return new Reader()
		{	private boolean inTag = false; // state must span read() calls.

			public int read( char[] cbuf, int off, int length ) throws IOException
			{	int read = src.read( cbuf, off, length );
				for(int i = read; --i >= 0 ; ++off )
				{	if(	cbuf[off] == '<' )
						inTag = true;
					else if( inTag )
					{	if( cbuf[off] == ':' )
							cbuf[off] = '_';
						if( cbuf[off] == '>' || Character.isWhitespace(cbuf[off]) )
							inTag = false;
					}
				}
				return read;
			}
			public void close() throws IOException
			{	src.close();
			}
		};
	}
}
