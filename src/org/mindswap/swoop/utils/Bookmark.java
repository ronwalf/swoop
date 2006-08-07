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

package org.mindswap.swoop.utils;

/**
 * @author Aditya
 */
public class Bookmark {

	String displayed_name;
	String uri;
	String ontology_uri;
	
	
	/**
	 * @return Returns the displayed_name.
	 */
	public String getDisplayed_name() {
		return displayed_name;
	}
	/**
	 * @param displayed_name The displayed_name to set.
	 */
	public void setDisplayed_name(String displayed_name) {
		this.displayed_name = displayed_name;
	}
	/**
	 * @return Returns the ontology_uri.
	 */
	public String getOntology_uri() {
		return ontology_uri;
	}
	/**
	 * @param ontology_uri The ontology_uri to set.
	 */
	public void setOntology_uri(String ontology_uri) {
		this.ontology_uri = ontology_uri;
	}
	/**
	 * @return Returns the uri.
	 */
	public String getUri() {
		return uri;
	}
	/**
	 * @param uri The uri to set.
	 */
	public void setUri(String uri) {
		this.uri = uri;
	}
	
	public String toString() {
		return this.displayed_name + "   ("+this.ontology_uri+")";
	}
}
