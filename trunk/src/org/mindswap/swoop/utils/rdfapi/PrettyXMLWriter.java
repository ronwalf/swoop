package org.mindswap.swoop.utils.rdfapi;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * @author ronwalf
 *
 * An implementation of XMLWriter to write nicely indented xml
 */
public class PrettyXMLWriter implements XMLWriter {
	protected static String INDENT = "  ";
	protected int level;
	protected Writer writer;
	
	private String encoding;
	private Map entities;
	private Map namespaces;
	private Set nextNamespaces;
	
	private Stack tags;
	private Stack bases;
	protected URI base;
	
	private boolean inTag = false;
	private boolean firstAttribute = false;
	private boolean dataWritten = false;
	private boolean wasEmpty = true;
	
	private int tagLength = 0;
	
	public PrettyXMLWriter(Writer writer) {
		this.writer = writer;
		encoding = "UTF-8";
		reset();
	}
	
	public PrettyXMLWriter(Writer writer, String encoding) {
		this.writer = writer;
		this.encoding = encoding;
		reset();
	}
	
	/* (non-Javadoc)
	 * @see org.mindswap.swoop.serializers.XMLWriter#addAttribute(java.net.URI, java.lang.String, java.lang.String)
	 */
	public void addAttribute(String ns, String local, String value) throws IOException {
		indentAttribute();
		write(" ");
		writeQName(ns, local);
		write("=\""+replaceEntities(sanitize(value, true))+"\"");
	}

	/* (non-Javadoc)
	 * @see org.mindswap.swoop.serializers.XMLWriter#addAttribute(java.net.URI, java.lang.String, java.net.URI)
	 */
	public void addAttribute(String ns, String local, URI value) throws IOException {
		if (base == null) {
			addAttribute(ns, local, value.toString());
		} else {
			addAttribute(ns, local, base.relativize(value).toString());
		}
	}

	/* (non-Javadoc)
	 * @see org.mindswap.swoop.serializers.XMLWriter#addEntity(java.lang.String, java.lang.String)
	 */
	public void addEntity(String name, String value) {
		if ((name != null) && (value != null) && !value.equals(""))
			entities.put(name, value);
	}

	/* (non-Javadoc)
	 * @see org.mindswap.swoop.serializers.XMLWriter#addNamespace(java.lang.String, java.net.URI)
	 */
	public void addNamespace(String name, String namespace) {
		namespaces.put(namespace, name);
		nextNamespaces.add(namespace);
	}

	private void addNamespaces() throws IOException {
		Map additions = new TreeMap();
		for (Iterator nameIter = nextNamespaces.iterator(); nameIter.hasNext();) {
			String namespace = (String) nameIter.next();
			String prefix = (String) namespaces.get(namespace);
			
			additions.put(prefix, namespace);
		}
		nextNamespaces.clear();
		
		for (Iterator prefixIter = additions.keySet().iterator(); prefixIter.hasNext();) {
			String prefix = (String) prefixIter.next();
			String namespace = (String) additions.get(prefix);
			writeAttribute("xmlns:"+prefix, namespace.toString());
		}
	}
	
	/* (non-Javadoc)
	 * @see org.mindswap.swoop.serializers.XMLWriter#endDocument()
	 */
	public void endDocument() throws IOException {
		while (!tags.empty()) {
			endElement();
		}
		write("\n");
	}

	/* (non-Javadoc)
	 * @see org.mindswap.swoop.serializers.XMLWriter#endElement()
	 */
	public void endElement() throws IOException {
		if (inTag) {
			finishTag(true);
			tags.pop();
			level--;
			wasEmpty = true;
		} else {
			level--;
			
			if (dataWritten)
				dataWritten = false;
			else 
				indent();
			String[] tag = (String[]) tags.pop();
			write("</");
			writeQName(tag[0], tag[1]);
			write(">");
			wasEmpty = false;
		}
		
		base = (URI) bases.pop();
		/*
		bases.pop();
		if (bases.isEmpty())
			base = null;
		else
			base = (URI) bases.peek();
		*/
	}
	
	private void finishTag(boolean empty) throws IOException {
		addNamespaces();
		
		if (empty) {
			write("/>");
		} else {
			write(">");
		}
		inTag = false;
		level++;
	}
	
	protected String getName(String namespace) {
		String name = (String) namespaces.get(namespace);
		if (name == null) {
			int i = 0;
			
			for (i=0; namespaces.containsValue("a"+i);i++) {}
			
			name = "a"+i;
			namespaces.put(namespace, name);
			nextNamespaces.add(namespace);
		}
		return name;
	}
	
	protected void indent() throws IOException {
		indent(0);
	}
	
	protected void indent(int extra) throws IOException {
		write("\n");
		for (int i=0; i < level; i++) {
			write(INDENT);
		}
		for (int i=0; i < extra; i++) {
			write(" ");
		}
	}
	
	protected void indentAttribute() throws IOException {
		if (firstAttribute) {
			firstAttribute = false;
		} else {
			indent(tagLength+1);
		}
	}
	
	protected String replaceEntities(String value) {
		for (Iterator entityIter = entities.keySet().iterator(); entityIter.hasNext();) {
			String entityName = (String) entityIter.next();
			String entityValue = (String) entities.get(entityName);
			
			if (value.indexOf(entityValue) >= 0) {
				//System.out.println("Replacing "+entityValue);
				value = value.replaceAll(entityValue, "&"+entityName+";");
				//break;
			}
		}
		return value;
	}
	
	protected void reset() {
		inTag = false;
		wasEmpty = true;
		base = null;
		bases = new Stack();
		entities = new TreeMap();
		namespaces = new HashMap();
		nextNamespaces = new TreeSet();
		tags = new Stack();
	}
	
	protected static String sanitize(String str) {
		return sanitize(str, false);
	}
	
	protected static String sanitize(String str, boolean quote) {
		StringBuffer result = new StringBuffer();
		char str_chars[] = str.toCharArray();
		for (int index=0; index < str_chars.length; index++) {
			char character = str_chars[index];
			if (character=='&') 
				result.append("&amp;");
			else if (character=='<')
				result.append("&lt;");
			else if (quote && character=='"')
				result.append("&quot;");
			else
				result.append(character);
		}
		return result.toString();
	}
	
	
	/* (non-Javadoc)
	 * @see org.mindswap.swoop.serializers.XMLWriter#setBase()
	 */
	public void setBase(URI base) {
		this.base = base;
	}
	
	/* (non-Javadoc)
	 * @see org.mindswap.swoop.serializers.XMLWriter#startDocument()
	 */
	public void startDocument() throws IOException {
		
		write("<?xml version=\"1.0\" encoding=\""+encoding+"\"?>\n");
		if (!entities.isEmpty()) {
			write("<!DOCTYPE rdf:RDF [\n");
			for (Iterator entIter = entities.keySet().iterator(); entIter.hasNext();) {
				String name = (String) entIter.next();
				String value = (String) entities.get(name);
				write(INDENT+"<!ENTITY "+name+" \""+sanitize(value, true)+"\">\n");
			}
			write("]>");
		}
	}
	
	

	/* (non-Javadoc)
	 * @see org.mindswap.swoop.serializers.XMLWriter#startElement(java.net.URI, java.lang.String)
	 */
	public void startElement(String ns, String local) throws IOException {
		if (inTag) {
			finishTag(false);
		} else  if (!wasEmpty && (level < 2)) {
			write("\n");
		}
		
		indent();
		write("<");
		String tag = writeQName(ns, local);
		tagLength = tag.length();
		tags.push(new String[]{ns, local});
		inTag = true;
		firstAttribute = true;
		
		
		if (base != null && (bases.isEmpty() || base != bases.peek())) {
			writeAttribute("xml:base", base.toString());
		}
		bases.push(base);
	}

	protected void write(String data) throws IOException {
		writer.write(data);
	}
	
	protected void writeAttribute(String name, String value) throws IOException {
		indentAttribute();
		value = sanitize(value, true);
		value = replaceEntities(value);
		write(" "+name+"=\""+value+"\"");
		
	}
	
	
	public void writeComment(String comment) throws IOException {
		if (inTag) {
			finishTag(false);
		} 
		write("\n\n<!-- "+sanitize(comment)+" -->");
		wasEmpty=true;
	}
	
	/* (non-Javadoc)
	 * @see org.mindswap.swoop.serializers.XMLWriter#writeData(java.lang.String)
	 */
	public void writeData(String data) throws IOException {
		writeData(data, false);
	}

	/* (non-Javadoc)
	 * @see org.mindswap.swoop.serializers.XMLWriter#writeData(java.lang.String, boolean)
	 */
	public void writeData(String data, boolean raw) throws IOException {
		if (inTag) {
			finishTag(false);
		}
		
		if (raw)
			write(data);
		else
			write(sanitize(data));
		dataWritten = true;
	}
	
	protected String writeQName(String ns, String local) throws IOException {
		String tag = local;
        if (ns != null) {
			String name = getName(ns);
			tag = name + ":" + local;
		}
			
		write(tag);
		return tag;
	}
	
	/** 
	 * Simple test driver
	 * @param args - Ignored.
	 * @throws URISyntaxException
	 */
	public static void main(String args[]) throws IOException, URISyntaxException {
		Writer writer = new OutputStreamWriter(System.out);
		XMLWriter xml = new PrettyXMLWriter(writer);
		
		URI base1 = new URI("http://www.example.com/");
		URI base2 = new URI("http://www.example.com/ns2/");
		URI ns1 = new URI("http://www.example.com/ns1/");
		URI ns2 = new URI("http://www.example.com/ns2/");
		
		xml.addEntity("ex", "http://www.example.com/");
		xml.startDocument();
		
		xml.addNamespace("ns1", ns1.toString());
		xml.setBase(base1);
		xml.startElement(ns1.toString(), "Foo");
		
		
		xml.startElement(ns2.toString(), "Bar");
		xml.addAttribute(ns2.toString(), "attr1", "pleep");
		xml.addAttribute(ns2.toString(), "attr2", ns2.resolve("plorb"));
		
		xml.setBase(base2);
		xml.startElement(ns2.toString(), "BarPlorb");
		xml.addAttribute(ns2.toString(), "attr3", ns2.resolve("plorb"));
		xml.endElement();
		
		xml.endElement();
		
		xml.startElement(ns2.toString(), "Baz");
		xml.writeData("Foobar");
		
		xml.endDocument();
		writer.close();
	}
}
