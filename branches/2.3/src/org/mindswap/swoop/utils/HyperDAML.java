package org.mindswap.swoop.utils;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

// $Id: hyperdaml.java,v 1.22 2002/08/14 21:49:14 mdean Exp $


/**
 * produce a hypertext version of a DAML/RDF document.
 */
public class HyperDAML
{
	static PrintWriter out = null;
	
	static String source = null;
	static String prefix = "";
	/**
	 * are we being run as a non-parsed-header CGI script?
	 */
	static boolean nph = false;


	static int rdfDepth = 0;	// just in case rdf:RDF is erroneously nested


	static String quoted(String string)
	{
		return '"' + string + '"';
	}


	/**
	 * escape HTML characters
	 */
	static String escaped(String string)
	{
		StringBuffer retval = new StringBuffer();
		for (int i = 0; i < string.length(); i++)
		{
			char ch = string.charAt(i);
			switch (ch)
			{
			case '<':
				retval.append("&lt;");
				break;
			case '>':
				retval.append("&gt;");
				break;
			case '&':
				retval.append("&amp;");
				break;
			default:
				retval.append(ch);
			}
		}
		return retval.toString();
	}


	/**
	 * determine if this is the RDF (M&S) namespace
	 */
	static boolean rdfNamespace(String namespaceURI)
	{
		return namespaceURI.equals("http://www.w3.org/1999/02/22-rdf-syntax-ns#");
	}


	/**
	 * determine if this is the named RDF attribute.
	 */
	static boolean rdfAttribute(org.xml.sax.Attributes atts, int index, String name)
	{
		String localName = atts.getLocalName(index);

		// be liberal about namespaces
		return localName.equals(name);
	}
	

	static String maybeApplyPrefix(String uri)
	{
		// check if self-relative
		if (uri.equals("")
				|| (uri.charAt(0) == '#'))
			return uri;
		else 
			return prefix + uri;
	}


	/**
	 * turn a relative URI into an absolute URI
	 */
	static String makeAbsolute(String maybeRelative)
	{
		try {
			return new java.net.URL(new java.net.URL(source),
					maybeRelative).toString();
		} catch (java.net.MalformedURLException e) {
			return maybeRelative;
		}
	}


	static public String formatRDF(String rdf, String source) throws ParserConfigurationException, IOException, SAXException {
		HyperDAML.source = source;
		StringWriter sw = new StringWriter();
		out = new PrintWriter(sw);
		
		generate(rdf);
		
		return sw.getBuffer().toString();		
	}
	
	static void generate(String rdf) throws ParserConfigurationException, IOException, SAXException	
	{
		javax.xml.parsers.SAXParserFactory spf = javax.xml.parsers.SAXParserFactory.newInstance();
		spf.setValidating(false); // not needed for our use -- reinforce default
		javax.xml.parsers.SAXParser saxParser = spf.newSAXParser();
		org.xml.sax.XMLReader xmlReader = saxParser.getXMLReader();

		xmlReader.setFeature("http://xml.org/sax/features/namespaces", true);

		// get xmlns attributes
		xmlReader.setFeature("http://xml.org/sax/features/namespace-prefixes", true);

		org.xml.sax.helpers.DefaultHandler handler = new org.xml.sax.helpers.DefaultHandler()
		{
			boolean foundDAML = false;
			boolean startedHTML = false;
			org.xml.sax.Locator locator;
			/**
			 * stack indicating whether each level of element has children.
			 */
			java.util.Stack elementStack = new java.util.Stack();

			/**
			 * code to check if this is the first child for the parent element
			 */
			void child()
			{
				if (elementStack.empty())
					return;

				Boolean b = (Boolean) elementStack.peek();
				if (b.equals(Boolean.TRUE))
				{
					// not first child
					return;
				}
				
				// close element start
				out.print("&gt;");

				elementStack.pop();
				elementStack.push(Boolean.TRUE);
			}

			public void setLocator(org.xml.sax.Locator l)
			{
				locator = l;
			}

			public void startDocument()
			{
			}

			void startHTML()
			{
				if (startedHTML)
					return;

				out.println("<html>");
				out.println("  <body>");
				
				startedHTML = true;
			}

			public void startElement(String namespaceURI, String localName,
					String rawName, org.xml.sax.Attributes atts)
			throws org.xml.sax.SAXException
			{
				if (rdfNamespace(namespaceURI)
						&& localName.equals("RDF"))
				{
					rdfDepth++;
					if (! foundDAML)
					{
						// first time
						if (nph)
						{
							out.println("HTTP/1.1 200 OK");
							out.println("Content-type:  text/html");
							out.println();
						}
						startHTML();
						out.println("    <code>");
						out.println("      <pre>");
						out.println("<font face=\"Verdana\" size=2>");
					}
					foundDAML = true;
				}

				if (rdfDepth == 0)
					return;

				child();	// of any parent
				elementStack.push(Boolean.FALSE);

				out.print("&lt;<a href=" + quoted(maybeApplyPrefix(makeAbsolute(namespaceURI + localName))) + ">" + rawName + "</a>");
				// attributes
				for (int i = 0; i < atts.getLength(); i++)
				{
					String attrLocalName = atts.getLocalName(i);
					String attrQName = atts.getQName(i);
					String value = atts.getValue(i);
					boolean hyperlink = (rdfAttribute(atts, i, "resource")
							|| rdfAttribute(atts, i, "about"));
					boolean namespace =  (attrQName.equals("xmlns")
							|| attrQName.startsWith("xmlns:"));

					if (rdfAttribute(atts, i, "ID"))
						out.print("<a name=" + quoted(value) + " />");
					if (namespace) {
						out.print("	" + atts.getQName(i) + "=");
						out.println(quoted("<a href=" + quoted(maybeApplyPrefix(value)) + ">" + value + "</a>"));
					}
					else{
					out.print(" " + atts.getQName(i) + "=");
					if (hyperlink)
						out.print(quoted("<a href=" + quoted(maybeApplyPrefix(value)) + ">" + value + "</a>"));
					else
						out.print(quoted(value));
					}
				}
			}

			public void characters(char [] ch, int start, int length)
			{
				if (rdfDepth == 0)
					return;

				child();

				for (int i = 0; i < length; i++)
					out.print(ch[start + i]);
			}

			public void ignorableWhitespace(char [] ch, int start, int length)
			{
				// handle like text
				characters(ch, start, length);
			}

			public void endElement(String uri, String localName, String qName)
			{
				if (rdfDepth == 0)
					return;

				Boolean b = (Boolean) elementStack.pop();
				if (b.equals(Boolean.FALSE))
					out.print("/&gt;");
				else
					out.print("&lt;/<a href=" + quoted(maybeApplyPrefix(makeAbsolute(uri + localName))) + ">" + qName + "</a>&gt;");

				if (rdfNamespace(uri)
						&& localName.equals("RDF"))
				{
					rdfDepth--;
					//out.println("<br />");
					//BJP: The problem is that the HTML renderer can't handle
					//     this classic XHTML/HTML trick. So, suppress the trick.
					out.println("<br>");

				}
			}

			/**
			 * redirect to the original document if it doesn't seem to contain DAML
			 */
			void maybeRedirect()
			{
				if (nph && (! foundDAML))
				{
					out.println("HTTP/1.1 301 Redirect");
					out.println("Location:  " + source);
					out.println();
					System.exit(0);
				}
			}

			public void endDocument()
			{
				maybeRedirect();

				startHTML();
				if (foundDAML)
				{
					out.println();
					out.println("  </font>");
					out.println("        </pre>");
					out.println("      </code>");
				}
				else
				{
					out.println("    No DAML or RDF content found.");
				}
				
//				out.println("    <hr />");
//				out.println("    <address>");
//				out.println("      Produced from");
//				out.println("      <a href=" + quoted(source) + ">" + source + "</a>");
//				out.println("      using");
//				out.println("      <a href=" + quoted("http://www.daml.org/2001/04/hyperdaml/") + ">hyperdaml</a>.java");
//				out.println("    </address>");
				out.println("  </font>");
				out.println("  </body>");
				out.println("</html>");
			}

			void printException(String label,
					org.xml.sax.SAXParseException e)
			{
				maybeRedirect();
				startHTML();
				System.err.println(label + ":  " + escaped(e.toString()));
			}

			public void warning(org.xml.sax.SAXParseException e)
			{
				printException("warning", e);
			}

			public void error(org.xml.sax.SAXParseException e)
			{
				printException("recoverable error", e);
			}

			public void fatalError(org.xml.sax.SAXParseException e)
			{
				printException("fatal error", e);
				endDocument();
			}
		};
		xmlReader.setContentHandler(handler);
		xmlReader.setErrorHandler(handler);
		xmlReader.setProperty("http://xml.org/sax/properties/lexical-handler",
				new org.xml.sax.ext.LexicalHandler()
				{
			public void comment(char [] ch, int start, int length)
			{
				if (rdfDepth == 0)
					return;
				
				out.print("&lt;!--");
				for (int i = 0; i < length; i++)
					out.print(ch[start + i]);
				out.print("--&gt;");
			}
			
			public void endCDATA()
			{
			}
			
			public void endDTD()
			{
			}
			
			public void endEntity(String name)
			{
			}
			
			public void startCDATA()
			{
			}
			
			public void startDTD(String name, String publicId, String systemId)
			{
			}
			
			public void startEntity(String name)
			{
			}
		});

		xmlReader.parse(new InputSource(new StringReader(rdf)));
	}
}


