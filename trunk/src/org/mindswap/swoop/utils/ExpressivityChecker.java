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
 *
 */
public class ExpressivityChecker {
	
	public static String getExplanation(String expressivity) {
		String expr = expressivity;
//		Explanations for funky logics
		
		if(expressivity.equals("RDFS(DL)")){
			expr += "<br>" + "RDFS(DL) - DL subset of RDF-Schema: Domain, Range, Role Hierarchy, Datatypes";
			return expr;
		}
		if(expressivity.equals("DL-Lite")){
			expr += "<br>" + "DL-Lite - Role Inverses, Conjunction, Limited Existential Quantification, Limited Negation (only on Atomic classes and existentials),  Functional Roles, Datatypes";
			return expr;
		}
		if(expressivity.equals("EL++")){
			expr += "<br>" + "EL++ (OWL DL subset of EL++)- Role Hierarchy, Conjunction, Existential Quantification, Concept Disjointness, hasValue Restrictions, GCIs, Datatypes";
			return expr;
		}
		if (expressivity.indexOf("EL")>=0) expr += "<br>" + "EL - Conjunction and Full Existential Quantification";
		//
		if (expressivity.indexOf("S")>=0) expr += "<br>S == ALCR+";
		expressivity = expressivity.replaceAll("S", "ALCR");
		if (expressivity.indexOf("AL")>=0) expr += "<br>" + "AL - Attribute Logic: Conjunction, Universal Value Restriction, Limited Existential Quantification";
		if (expressivity.indexOf("C")>=0) expr += "<br>" + "C - Complement (together with AL allows Disjunction, Full Existential Quantification)";
		if (expressivity.indexOf("U")>=0) expr += "<br>" + "U - Union";
		if (expressivity.indexOf("R")>=0) expr += "<br>" + "R+ - Role Transitivity";
		if (expressivity.indexOf("H")>=0) expr += "<br>" + "H - Role Hierarchy";
		if (expressivity.indexOf("I")>=0) expr += "<br>" + "I - Role Inverse";
		if (expressivity.indexOf("O")>=0) expr += "<br>" + "O - Nominal";
		if (expressivity.indexOf("N")>=0) expr += "<br>" + "N - Unqualified Number Restrictions";
		if (expressivity.indexOf("Q")>=0) expr += "<br>" + "Q - Qualified Number Restrictions";
		if (expressivity.indexOf("(D)")>=0) expr += "<br>" + "(D) - Datatypes";
		
		return expr;
	}
}
