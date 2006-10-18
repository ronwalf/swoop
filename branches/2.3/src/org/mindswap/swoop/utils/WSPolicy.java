/*
 * Created on Jul 5, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.mindswap.swoop.utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URLConnection;

import org.semanticweb.owl.io.ParserException;

/**
 * @author kolovski
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class WSPolicy {
	
	static final String WSPOLICY_XSLT_URI =  "http://www.mindswap.org/2005/services-policies/wsp2owl.xsl";
	
	public static Reader transformPolicyFile(URI uri) {
		
		Reader r = new InputStreamReader(System.in);
		try {
			
			File xsltFile = new File(WSPOLICY_XSLT_URI);
			
			javax.xml.transform.Source xmlSource =
				new javax.xml.transform.stream.StreamSource(uri.toString());
			javax.xml.transform.Source xsltSource =
				new javax.xml.transform.stream.StreamSource(WSPOLICY_XSLT_URI);
			
			StringWriter writer = new StringWriter();
			
			javax.xml.transform.Result result =
				new javax.xml.transform.stream.StreamResult(writer);
						
			
			// create an instance of TransformerFactory
			javax.xml.transform.TransformerFactory transFact =
				javax.xml.transform.TransformerFactory.newInstance(  );
			
			javax.xml.transform.Transformer trans =
				transFact.newTransformer(xsltSource);
			
			trans.transform(xmlSource, result);		
			
						
			return new StringReader(writer.toString());
			
		} catch (Exception e) {
			e.printStackTrace();
			return r;
		}	
		
	}
	
}
