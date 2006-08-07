//The MIT License
//
// Copyright (c) 2004 Mindswap Research Group, University of Maryland, College Park
//
// Permission is hereby granted, free of charge, to any person obtaining a copy
// of this software and associated documentation files (the "Software"), to
// deal in the Software without restriction, including without limitation the
// rights to use, copy, modify, merge, publish, distribute, sublicense, and/or
// sell copies of the Software, and to permit persons to whom the Software is
// furnished to do so, subject to the following conditions:
//
// The above copyright notice and this permission notice shall be included in
// all copies or substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
// FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS
// IN THE SOFTWARE.

package org.mindswap.swoop.renderer.ontology;

import java.awt.Component;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import org.apache.commons.lang.StringEscapeUtils;
import org.mindswap.swoop.OntologyDisplay;
import org.mindswap.swoop.SwoopDisplayPanel;
import org.mindswap.swoop.SwoopModel;
import org.mindswap.swoop.renderer.SwoopOntologyRenderer;
import org.semanticweb.owl.io.RendererException;
import org.semanticweb.owl.model.OWLException;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.helper.OntologyHelper;
import org.semanticweb.owl.validation.SpeciesValidator;
import org.semanticweb.owl.validation.SpeciesValidatorReporter;

/**
 * @author unknown
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class SwoopSpeciesValidationRenderer extends BaseOntologyRenderer implements SwoopOntologyRenderer {

	
	private static final String VALIDATOR_CONTENT_TYPE = "text/html";
	private static final String VALIDATOR_NAME = "Species Validation";	
	
	private static final String [] POPULAR_SCHEME_NAMES = {"http://"};//, "ftp://", "gopher://", "https://", "tftp://" };
	private static final String SPACE = "&nbsp;";
	private static final String LT = "&lt;";
	private static final String GT = "&gt;";	
	private SwoopModel myModel = null;

	// inherited methods from interface SwoopOntologyRenderer
	
	public String getContentType() {
		return VALIDATOR_CONTENT_TYPE;
	}

	public String getName() {
		return VALIDATOR_NAME;
	}
	
	public void render(OWLOntology ontology, SwoopModel swoopModel, Writer writer) throws RendererException {
		
		myModel = swoopModel;

		PrintWriter out = new PrintWriter(writer);

		SpeciesValidator sv = null;
		try {
			sv = new SwoopSpeciesValidator(swoopModel);
			
		} catch (OWLException e1) {
			throw new RendererException(e1.getMessage());
		}
		StringWriter lw = new StringWriter();
		StringWriter dw = new StringWriter();
		StringWriter fw = new StringWriter();
		StringWriter rw = new StringWriter();
		StringWriter mw = new StringWriter();
		final PrintWriter lpw = new PrintWriter(lw);
		final PrintWriter dpw = new PrintWriter(dw);
		final PrintWriter fpw = new PrintWriter(fw);
		final PrintWriter mpw = new PrintWriter(mw);
		
		final StringBuffer level = new StringBuffer();

		sv.setReporter(new SpeciesValidatorReporter() {
			public void ontology(OWLOntology onto) {
			}

			public void done(String str) {
				level.setLength(0);
				level.append(str);
			}

			public void message(String str) {
				mpw.println("<li>" + str + "</li>");
			}

			public void explain(int l, int code, String str) {
				str = hyperlinkizeCode(str);
				switch (l) {
				case SwoopSpeciesValidator.LITE :
					lpw.println( "<li>" + reformatInHTML(str) + "</li>");
					break;
				case SwoopSpeciesValidator.DL :
					dpw.println( "<li>" + reformatInHTML(str) + "</li>");
					break;
				case SwoopSpeciesValidator.FULL :
					fpw.println( "<li>" + reformatInHTML(str) + "</li>");
					break;
				}
			}
		});
		
		try {
			//int l = SwoopSpeciesValidator.LITE;
			// check for OWL lite so we get all the messages
			sv.isOWLLite(ontology);
			out.println("<html><body style='background-color: white; color: black'>");
			out.println("<FONT FACE=\""+swoopModel.getFontFace()+"\" SIZE="+swoopModel.getFontSize()+">");
//			out.println("<b>DL Expressivity:</b> " + swoopModel.getReasoner().getExpressivity()+"<br>");
			out.println("<h1>Level: " + level +"<h1>");		
			
			//out.println( "<li>"+"asdf adfd "+ encodeHLink("http://www.google.com", "http://www.google.com") + "</li>"); 
			
			if (!lw.toString().equals("")) {
				out.println("<p><strong>OWL-Lite features:</strong></p>");
				out.println("<ul>");
				out.println( lw.toString() );
				out.println("</ul>");
			} // end of if ()
			if (!dw.toString().equals("")) {
				out.println("<p><strong>OWL-DL features:</strong></p>");
				out.println("<ul>");
				out.println( dw.toString() );
				out.println("</ul>");
			} // end of if ()
			if (!fw.toString().equals("")) {
				out.println("<p><strong>OWL-Full features:</strong></p>");
				out.println("<ul>");
				out.println( fw.toString() );
				out.println("</ul>");
			} // end of if ()
			if (!mw.toString().equals("")) {
				out.println("<p><b>Additional Messages</b></p>");
				out.println("<ul>");
				out.println( mw.toString() );
				out.println("</ul>");
			} // end of if ()
			
			out.println("</FONT>");
			out.println("</body></html>");
		} catch (Exception e) {
			out.println("Exception: " + e.getMessage());
			e.printStackTrace();
		} // end of try-catch

	}

	public void setOptions(Map options) {
	}

	/**
	 * Search all the URIs in the current ontology for a word occuring
	 * at the end of the uri. Return uri if match found
	 * @param word 
	 * @return
	 * @throws OWLException
	 */
	private String findURI(String word) throws OWLException {
		OWLOntology onto = myModel.getSelectedOntology();
		Set ontologies = OntologyHelper.importClosure(onto);
		String uriLink = "";
		
		for (Iterator iter = ontologies.iterator(); iter.hasNext();) {
			OWLOntology ont = (OWLOntology) iter.next();
			Set allURIs = OntologyHelper.allURIs(ont);
			for (Iterator iter2 = allURIs.iterator(); iter2.hasNext(); ) {
				String uri = iter2.next().toString();
				if (uri.endsWith("#"+word) || uri.endsWith("/"+word)) {
					uriLink = uri;
					break;
				}							
			}
		}
		return uriLink;
	}
	
	/**
	 * Go through the species validation explanation string
	 * Check for expressions of the form ":XX" and replace
	 * XX with the corresponding URI from the ontology, if possible
	 * @param str
	 * @return
	 */
	private String hyperlinkizeCode(String str) {
		try {
			int pos = 0;
			do {
				int index = str.indexOf(":", pos);
				pos = index+1;
				if (index!=-1 && !str.substring(index+1, index+2).equals(" ")) {					
					int endpos = str.indexOf(" ", index+1);
					int endpos2 = str.indexOf(")", index+1);
					if (endpos==-1) endpos = endpos2;
					else if (endpos>endpos2 && endpos2!=-1) endpos = endpos2;
					if (endpos==-1) continue;
					String word = str.substring(index+1, endpos);
					String uri = this.findURI(word);
					if (!uri.equals("")) {
						// add hyperlink to word
						String hLink = this.encodeHLink(uri, word);
						String chr = str.substring(index-1, index+1);
						str = str.replaceAll(chr+word, hLink);
					}
				}
			}
			while (pos!=0);
			
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
		return str;
	}
	
	public Map getOptions() {
		return null;
	}
	
	/*
	 *  SwoopRenderer method
	 * 
	 */
	public Component getDisplayComponent( SwoopDisplayPanel panel )
	{		
		if (! (panel instanceof OntologyDisplay ))
			throw new IllegalArgumentException();
		return super.getEditorPane( this.getContentType(), (OntologyDisplay)panel );
	}
	
	
	private String reformatInHTML(String source)
	{
		// if the following is true, then we assume that the String source
		// is already properly formatted with HLinks
		if (source.indexOf("<a href=") != -1){
		    
		    // replace <http with &lt;http to escape <
			// so < will be displayed in the species validation
			// pane rather than being treated as a tag.
		    source = source.replaceAll("<http", "&lt;http");
			return source;
			
		}
		//System.out.println(source);
		source = source.replaceAll(SPACE, " ");
		try{
			StringTokenizer tokens = new StringTokenizer(source);
			String result = "";
			while (tokens.hasMoreTokens())
			{				
				String token = tokens.nextToken();
				String temp = "";
				for (int i = 0; i < POPULAR_SCHEME_NAMES.length; i++)
				{
					int index = -1;
					if ((index = token.indexOf(POPULAR_SCHEME_NAMES[i])) != -1)
					{
						String head = token.substring(0, index);
						String tail = token.substring(index);
						String tip  = "";
						
						int x = 0;
						int y = 0;
						int z = 0;
						int w = 0;
						int ind = Integer.MAX_VALUE;
						if ((x = tail.indexOf("<")) != -1)
							ind = Math.min(ind, x);
						if ((y = tail.indexOf(">")) != -1)
							ind = Math.min(ind, y);
						if ((z = tail.indexOf("(")) != -1)
							ind = Math.min(ind, z);
						if ((w = tail.indexOf(")")) != -1)
							ind = Math.min(ind, w);
						
						if (ind != Integer.MAX_VALUE)
						{
							tip = tail.substring(ind);
							tail = tail.substring(0, ind);
						}						
						temp = head + encodeHLink(tail, myModel.shortForm(new URI(tail))) + tip;					
						break;
					}
					temp = token;
				}
				result = result + SPACE + temp;
			}
			
			// replace <http with &lt;http to escape <
			// so < will be displayed in the species validation
			// pane rather than being treated as a tag.
			result = result.replaceAll("<http", "&lt;http");
			return  result ;
		}
		catch (URISyntaxException ex) 
		{ 
			ex.printStackTrace();
		}
		
		// replace <http with &lt;http to escape <
		// so < will be displayed in the species validation
		// pane rather than being treated as a tag.
		source = source.replaceAll("<http", "&lt;http");
		return source;  // exception has occurred.  No 'pretty printing' is returned
	}
	
	private String encodeHLink(String uri_string, String name) throws URISyntaxException
	{
	//System.out.println("linking: >>"+uri_string);
	  URI uri = new URI(uri_string);
	  
	  // make the uri sensitive to QName toggling.
	  return ("<a href="+StringEscapeUtils.escapeHtml(uri_string)+">"
			  +StringEscapeUtils.escapeHtml(myModel.shortForm(uri))+"</a>");
	  //return "<a href="+uri+">"+name+"</a>";
	 }

}
