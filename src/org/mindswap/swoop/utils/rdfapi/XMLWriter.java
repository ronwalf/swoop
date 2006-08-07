/*
 * Created on Mar 28, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.mindswap.swoop.utils.rdfapi;

import java.io.IOException;
import java.net.URI;

/**
 * @author ronwalf
 *
 * XML Writer interface.  Provide basic functions for writing an xml document
 */
public interface XMLWriter {
	
	/**
	 * Start the xml document.
	 */
	public void startDocument() throws IOException;
	/**
	 * End the xml document
	 */
	public void endDocument() throws IOException;
	
	public void addEntity(String name, String value);
	public void addNamespace(String name, String namespace);
	
	public void startElement(String ns, String local) throws IOException;
	public void endElement() throws IOException;
	
	public void addAttribute(String ns, String local, String value) throws IOException;
	public void addAttribute(String ns, String local, URI value) throws IOException;
	
	public void writeComment(String comment) throws IOException;
	public void writeData(String data) throws IOException;
	public void writeData(String data, boolean raw) throws IOException;
	
	
	/**
	 * @param base - Set the base uri of the document.
	 */
	public void setBase(URI base);
}
