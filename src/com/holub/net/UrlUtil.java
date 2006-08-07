// (c) 2003 Allen I Holub. All rights reserved.
//
package com.holub.net;

import java.net.*;
import java.io.*;

import com.holub.tools.DateUtil;

/** This utility provides methods for building relative URLs.
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

public class UrlUtil
{
	/** Create a URL that adjusts the <i>context</i> URL to
	 *  reflect the relative path specified by the <i>spec</i>.
	 *  Here are some examples:
	 *  <table border=1 cellspacing=0 cellpadding=3>
	 *  <tr>
	 *  	<td><code>context</code></td>
	 *  	<td><code>spec</code></td>
	 *  	<td>Result</td>
	 *  </tr>
	 *  <tr> 
	 *		<td><code>http://www.holub.com/x/y/REMOVE.EXT</code></td>
	 *		<td><code>../../b/ADD.EXT</code></td>
	 *		<td><code>http://www.holub.com/b/ADD.EXT</code></td>
	 *	</tr>
	 *	<tr>
	 *		<td><code>http://www.holub.com/</code></td>
	 *		<td><code>/b/ADD.EXT</code></td>
	 *		<td><code>http://www.holub.com/b/ADD.EXT</code></td>
	 *	</tr>
	 *	<tr>
	 *		<td><code>http://www.holub.com/a/</code></td>
	 *		<td><code>./b/ADD.EXT</code></td>
	 *		<td><code>http://www.holub.com/a/b/ADD.EXT</code></td>
	 *	</tr>
	 *	<tr>
	 *		<td><code>http://host/</code></td>
	 *		<td><code>/b</code></td>
	 *		<td><code>http://host/b</code></td>
	 *	</tr>
	 *	<tr>
	 *		<td><code>http://host/x</code></td>
	 *		<td><code>/b</code></td>
	 *		<td><code>http://host/b</code></td>
	 *	</tr>
	 *	<tr>
	 *		<td><code>http://host/</code></td>
	 *		<td><code>b</code></td>
	 *		<td><code>http://host/b</code></td>
	 *	</tr>
	 *	<tr>
	 *		<td><code>http://host/x/</code></td>
	 *		<td><code>../b</code></td>
	 *		<td><code>//host/b</code></td>
	 *  </tr>
	 *  </table>
	 *  <p>
	 *  @throws MalformedURLException in the case of a bad URL, including
	 *  	impossible relative paths. For example, the following call
	 *  	throws an exception:
	 * 	<pre>
	 * 	URL context = new URL("http://www.holub.com/a/" );
	 * 	URL relative = UrlUtil.relative( context, "../../b" );
	 * 	</pre>
	 *
	 *  @param context the URL that serves as the base point
	 *  	of the relative address. If the path component of
	 *  	this URL ends in a slash (which is the case if you
	 *  	put a slash into the original URL constructor), then
	 *  	the URL is assumed to represent a file, otherwise
	 *  	it's assumed to represent a directory.
	 *
	 *  @param spec the relative part of the file spec. If it
	 *  	starts with a /, then it's relative to the
	 *  	host root directory and this construnctor
	 *  	will work just like {@link java.net.URL#URL(URL,String)}.
	 */

	public static URL relative( URL context, String spec )
										throws MalformedURLException
	{	
		String path  		 = context.getPath();
		String originalPath = path;
		String originalSpec = spec;
		int    pathEnd 	 = path.length()-1;
		int    specStart	 = 0;

		if( !path.startsWith("/") )
			path = "/" + path ;

		/*D.ebug( "Working on " 
				+ context.getProtocol()
				+ "://" + context.getHost() + path
				+ " + " + spec );
        */
		if( spec.startsWith("/") )			// it's off the root directory
			return new URL( context, spec );

		if( !path.endsWith("/" ) )	// it's a file
			pathEnd = path.lastIndexOf('/', pathEnd );

		if( spec.startsWith("./") )	// harmless, doesn't mean anything
			specStart += 2;

		while( spec.startsWith("..", specStart) )
		{	specStart += 2;
			pathEnd   = path.lastIndexOf('/', pathEnd-1 );
			if( pathEnd == -1 )
				throw new MalformedURLException(
						"Illegal relative path. Cannot get to "
						+ originalSpec
						+ " from "
						+ originalPath
						);

			if( spec.startsWith("/", specStart) )
				++specStart;
		}

		return new URL(	context.getProtocol()
						+ "://"
						+ context.getHost()
						+ path.substring(0,pathEnd+1)
						+ spec.substring(specStart)	);
	}

	public static String decodeUrlEncoding( String data )
	{
		StringReader in 	= new StringReader( data );
		StringBuffer result = new StringBuffer();
		int c;

		for( int cursor = 0; cursor < data.length(); ++cursor )
		{	switch( c = data.charAt(cursor) )
			{
			default	 : result.append((char)c);	break;
			case '&' : result.append('\n');		break;
			case '+' : result.append(' ');		break;
			case '%' :
				int high = data.charAt(++cursor);
				int low  = data.charAt(++cursor);

				high = (high >= 'a') ? (high - 'a') + 0xa :
					   (high >= 'A') ? (high - 'A') + 0xa :
									   (high - '0') ;
				low  = (low >= 'a')  ? (low  - 'a') + 0xa :
					   (low >= 'A')  ? (low  - 'A') + 0xa :
									   (low  - '0') ;
				c = (high << 4) + low;

				if( c == '\n' ) result.append(" ");
				else			result.append((char)c);
				break;
			}
		}
		return result.toString();
	}

	public static class Test
	{
		public static void main(String[] args) throws Exception
		{
			/*Tester t = new Tester( args.length > 0,
 										com.holub.io.Std.out() );
 			
			URL uri = UrlUtil.relative(
						new URL("http://www.holub.com/x/y/REMOVE.EXT"),
						"../../add/ADD.EXT" );

			t.check( "UrlUtil.1", "http://www.holub.com/add/ADD.EXT",
												uri.toExternalForm() );
					 
			uri = UrlUtil.relative(new URL("http://www.holub.com/"),
													"/add/ADD.EXT");
			t.check( "UrlUtil.2", "http://www.holub.com/add/ADD.EXT",
												uri.toExternalForm());

			uri = UrlUtil.relative(new URL("http://www.holub.com/fred/"),
													"./add/ADD.EXT");
			t.check( "UrlUtil.3", "http://www.holub.com/fred/add/ADD.EXT",
												uri.toExternalForm() );

			uri = UrlUtil.relative(new URL("http://h/"), "/b");
			t.check( "UrlUtil.4", "http://h/b", 	uri.toExternalForm() );

			uri = UrlUtil.relative(new URL("http://h/x"), "/b");
			t.check( "UrlUtil.5", "http://h/b", 	uri.toExternalForm() );

			uri = UrlUtil.relative(new URL("http://h/"), "b");
			t.check( "UrlUtil.6", "http://h/b", 	uri.toExternalForm() );

			uri = UrlUtil.relative(new URL("http://h/x/"), "../b");
			t.check( "UrlUtil.7", "http://h/b",		uri.toExternalForm() );

			try
			{	uri = UrlUtil.relative(new URL("http://a/"), "../b");
				t.failure("UrlUtil.8.1", "Failed to catch exception");
			}
			catch( MalformedURLException e)
			{	t.success("UrlUtil.8.1", "Caught Exception: "+e.getMessage() );
			}

			try
			{	uri = UrlUtil.relative(new URL("http://a/b"), "../../c");
				t.failure("UrlUtil.8.2", "Failed to catch exception");
			}
			catch( MalformedURLException e)
			{	t.success("UrlUtil.8.2", "Caught Exception: "+e.getMessage() );
			}

			try
			{	uri = UrlUtil.relative(new URL("http://a/b/c"), "../../c");
				t.failure("UrlUtil.8.3", "Failed to catch exception");
			}
			catch( MalformedURLException e)
			{	t.success("UrlUtil.8.3", "Caught Exception: "+e.getMessage() );
			}*/
		}
	}
}
