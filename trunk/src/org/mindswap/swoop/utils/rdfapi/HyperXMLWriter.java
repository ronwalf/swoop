/*
 * Created on Apr 21, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.mindswap.swoop.utils.rdfapi;

import java.io.IOException;
import java.io.Writer;
import java.net.URI;

import org.semanticweb.owl.io.vocabulary.RDFVocabularyAdapter;

/**
 * @author ronwalf
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class HyperXMLWriter extends PrettyXMLWriter {

	/**
	 * @param writer
	 */
	public HyperXMLWriter(Writer writer) {
		super(writer);
	}
	
	public void addAttribute(String ns, String local, URI value) throws IOException {
		String valueString;
		if (base == null) {
			valueString = value.toString();
		} else {
			valueString = base.relativize(value).toString();
		}
		
		indentAttribute();
		write(" ");
		writeQName(ns, local);
		write("=\"");
		writer.write("<a href=\""+sanitize(value.toString(),true)+"\">");
		write(replaceEntities(sanitize(valueString, true)));
		writer.write("</a>");
		write("\"");
		
	}
	
	public void endDocument() throws IOException {
		super.endDocument();
		writer.write("</pre>\n");
	}
	
	public void startDocument() throws IOException {
		writer.write("<pre>\n");
		super.startDocument();
	}

	protected void write(String value) throws IOException {
		writer.write(sanitize(value));
	}
	
	protected String writeQName(String ns, String local) throws IOException {
		if (ns == null || ns.startsWith(RDFVocabularyAdapter.RDF)) {
			return super.writeQName(ns, local);
		}
		writer.write("<a href=\""+sanitize(ns+local, true)+"\">");
		String tag = super.writeQName(ns, local);
		writer.write("</a>");
		return tag;
	}
}
