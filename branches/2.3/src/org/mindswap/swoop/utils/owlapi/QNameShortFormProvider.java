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

package org.mindswap.swoop.utils.owlapi;

import java.io.Serializable;
import java.net.URI;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.semanticweb.owl.io.ShortFormProvider;
import org.semanticweb.owl.io.vocabulary.OWLVocabularyAdapter;
import org.semanticweb.owl.io.vocabulary.RDFSVocabularyAdapter;
import org.semanticweb.owl.io.vocabulary.RDFVocabularyAdapter;
import org.semanticweb.owl.io.vocabulary.XMLSchemaSimpleDatatypeVocabulary;

public class QNameShortFormProvider implements ShortFormProvider, Serializable {


	// A pattern to match a reversed URL
	// Roughly corresponds to .*<name>?.*<name>
	private static Pattern splitPattern = Pattern.compile("^([\\w[-.]]*[A-Za-z_])((?:[^A-Za-z_][^A-Za-z_]*?([\\w[-.]]*[A-Za-z_]).*)|(?:[^A-Za-z_]*))?$");//([\\w[-.]]*[A-Za-z_]).*)$");
	
	private static int BASE_GROUP = 2;
	private static int PREFIX_GROUP = 3;
	private static int NAME_GROUP = 1;
	private static String OWL = OWLVocabularyAdapter.OWL;
	private static String RDFS = RDFSVocabularyAdapter.RDFS;
	private static String RDF = RDFVocabularyAdapter.RDF;
	private static String XSD = XMLSchemaSimpleDatatypeVocabulary.XS;
	private static String DC = "http://purl.org/dc/elements/1.1/";

	// stores a map of uri -> prefix
	Map uriToPrefix;

	Map prefixToUri;

	public Map touchedPrefixes;

	/**
	 *  
	 */
	public QNameShortFormProvider() {
		uriToPrefix = new Hashtable();
		prefixToUri = new Hashtable();
		touchedPrefixes = new Hashtable();

		// initialize it with standard stuff
		setMapping("owl", OWL);
		setMapping("rdf", RDF);
		setMapping("rdfs", RDFS);
		setMapping("xsd", XSD);
		setMapping("dc", DC);
		//setMapping("foaf", FOAF);
	}

	/**
	 * Reverse a string
	 * @param string
	 * @return A reversed copy of the string
	 */
	protected static String reverse(String string) {
		String reversed = null;
		if (string != null) {
			StringBuffer sb = new StringBuffer(string);
			reversed = sb.reverse().toString();
		}
		return reversed;
	}
	
	/**
	 * Splits a uri into three parts.
	 * 
	 * @param uri
	 * @return An array of three strings: 1) The base uri, 2) A prefix from that
	 *         base, 3) the local name
	 */
	protected static String[] splitURI(URI uri) {
		String base, prefix, name;
		String[] bpn = new String[3];

		Matcher splitMatcher = splitPattern.matcher(reverse(uri.toString()));
		if (splitMatcher.matches()) {
			base = reverse(splitMatcher.group(BASE_GROUP));
			prefix = reverse(splitMatcher.group(PREFIX_GROUP));
			name = reverse(splitMatcher.group(NAME_GROUP));
			
//			System.out.println("============");
//			System.out.println("URI: " +uri);
//			System.out.println("Base: "+base);
//			System.out.println("Prefix: "+prefix);
//			System.out.println("Name: "+name);
			
			if (base == null) {
				base = "";
			}
			if (prefix == null) {
				prefix = "a";
			}
			prefix = removeExtension(prefix);
			
			bpn[0] = base;
			bpn[1] = prefix;
			bpn[2] = name;
			
//			System.out.println("============");
//			System.out.println("Base: "+base);
//			System.out.println("Prefix: "+prefix);
//			System.out.println("Name: "+name);
			
		} else {
//			System.out.println("No match for "+uri);
			bpn = null;
		}
		
		return bpn;
	}

	public String getPrefix(String uri) {
		return (String) uriToPrefix.get(uri);
	}

	/**
	 * Get the URI associated with a given prefix.
	 * @param prefix
	 * @return The uri associated, or null of none can be found.
	 */
	public String getURI(String prefix) {
		return (String) prefixToUri.get(prefix);
	}

	/**
	 * Associates a prefix with a URI, if no association has already been made.
	 * @param prefix
	 * @param uri
	 * @return True if successful, or false if the mapping already existed.
	 */
	public boolean setMapping(String prefix, String uri) {
		String currentUri = getURI(prefix);
		if (currentUri == null) {
			//the (prefix, uri) pair is not stored in the provider, we add it.
			prefixToUri.put(prefix, uri);
			uriToPrefix.put(uri, prefix);
			return true;
		} else if (currentUri == uri) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Return all the prefixes known about.
	 * @return
	 */
	public Set getPrefixSet() {
		return prefixToUri.keySet();
	}

	public String shortForm(URI uri) {
		//System.out.println("Shortform for " + uri);
		return shortForm(uri, true);
	}

	/**
	 * 
	 * @param uri
	 * @param default_to_uri
	 * @return The shortform of the uri.  If default_to_uri is set to false, null
	 * will be returned if a shortform cannot be fabricated.
	 */
	public String shortForm(URI uri, boolean default_to_uri) {
		String[] bpn = splitURI(uri);
		String base, possible_prefix, prefix, name;
		String qname;

		if (uri.toString().indexOf("ClassExpression") >= 0) {
			String uriStr = uri.toString();
			return uriStr.substring(uriStr.indexOf("#") + 1, uriStr.length());
		}

		if (bpn == null) {
			if (default_to_uri) {
				return uri.toString();
			} else {
				return null;
			}
		}

		base = bpn[0];
		possible_prefix = bpn[1];
		name = bpn[2];

		prefix = getPrefix(base);
		if (prefix == null) {
			// Check prefix for uniqueness
			prefix = possible_prefix;
			int mod = 0;
			while (!setMapping(prefix, base)) {
				prefix = possible_prefix + mod;
				mod++;
			}
		}

		touchedPrefixes.put(prefix, base);
		qname = prefix + ":" + name;
		return qname;
	}
	
	
	/**
	 * Find the local part of a URI
	 * @param uri
	 * @return The local portion, or null if none can be found.
	 */
	public String findLocal(URI uri) {
		String local = null;
		String bpn[] = splitURI(uri);
		
		if (bpn != null) {
			local = bpn[2];
		}
		return local;
	}
	
	/**
	 * Find the portion of a uri that is left over after removing the local part.
	 * @param uri
	 * @return The results, or null if the uri cannot be split.
	 */
	public String findPrefixURI(URI uri) {
		String prefix = null;
		String bpn[] = splitURI(uri);
		
		if (bpn != null) {
			prefix = bpn[0];
		}
		
		return prefix;
	}
	
	/**
	 * Returns a prefix associated non-localname portion of a uri.
	 * @param uri
	 * @return
	 */
	public String findPrefix(URI uri) {
		shortForm(uri);
		String prefixURI = findPrefixURI(uri);
		return getPrefix(prefixURI);
	}

	private static String removeExtension(String prefix) {
		//	remove extension on prefix
		if (prefix.indexOf(".") >= 0) {
			prefix = prefix.substring(0, prefix.lastIndexOf("."));
		} //BJP BUG: sometimes, for, I guess, new prefixes, you can get a .owl or something. Shouldn't be an else.
		return prefix;
	}
}
