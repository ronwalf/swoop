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

package org.mindswap.swoop.renderer.entity;

import org.semanticweb.owl.io.vocabulary.OWLVocabularyAdapter;

/**
 * @author Evren Sirin
 */
public class ConciseFormat {
	private static OWLVocabularyAdapter owl = OWLVocabularyAdapter.INSTANCE;
	
//	public static String FORALL = "<font face=\"Symbol\" SIZE=3><a href=\"All values (if present) of this property..\">&#8704;</a></font>";
//	public static String EXISTS = "<font face=\"Symbol\" SIZE=3><a href=\"Some values (atleast one) of this property..\">&#8707;</a></font>";
//	//public static String MEMBEROF = "<font face=\"Symbol\" SIZE=3><a href=\"..must belong to a member of class..\">&#8712;</a></font>";
//	public static String MEMBEROF = "<font face=\"Verdana\" SIZE=3><a href=\"..must belong to a member of class..\">.</a></font>";
//	public static String GREATEQU = "<font face=\"Symbol\" SIZE=3><a href=\"Cardinality of property must be greater than or equal to..\">&#8805;</a></font>";
//	public static String LESSEQU = "<font face=\"Symbol\" SIZE=3><a href=\"Cardinality of property must be lesser than or equal to..\">&#8804;</a></font>";  
//	public static String EQU = "<a href=\"Cardinality of property must equal to..\">=</a>";  
	public static String FORALL = "<font face='Symbol' SIZE=3><a href='"+owl.getAllValuesFrom()+"'>&#8704;</a></font>";
	public static String EXISTS = "<font face=\"Symbol\" SIZE=3><a href='"+owl.getSomeValuesFrom()+"'>&#8707;</a></font>";
	public static String MEMBEROF = "<font face=\"Verdana\" SIZE=3>.</font>";
	public static String GREATEQU = "<font face=\"Symbol\" SIZE=3><a href='"+owl.getMaxCardinality()+"'>&#8805;</a></font>";
	public static String LESSEQU = "<font face=\"Symbol\" SIZE=3><a href='"+owl.getMinCardinality()+"'>&#8804;</a></font>";  
	public static String EQU = "<a href='"+owl.getCardinality()+"'>=</a>";  
	public static String EQUIVALENT = "<font face=\"Symbol\" SIZE=3>&#8801;</font>";
	public static String INTERSECTION = "<font face=\"Symbol\" SIZE=3>&#8745;</font>";
	public static String UNION = "<font face=\"Symbol\" SIZE=3>&#8746;</font>";
	public static String COMPLEMENT = "&not;&nbsp;";
	public static String SUBSET = "<font face=\"Symbol\" SIZE=3>&#8838;</font>";
	public static String SUPERSET = "<font face=\"Symbol\" SIZE=3>&#8839;</font>";
	public static String DISJOINT = "<font face=\"Symbol\" SIZE=3>&#8800;</font>";
	
}
