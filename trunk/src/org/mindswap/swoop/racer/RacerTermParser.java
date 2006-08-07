package org.mindswap.swoop.racer;

import java.util.*;


/** This class provides an interface to parse RACER concept and role terms. The behaviour provided here
	performs no parsing at all; but provides syntax checking capabilities. The class can be extended in
	order to provide also term parsing.<p>
	In order to be able to provide a parser that returns object representations for concept and role
	terms, only the build methods need to be redefined.
 */

public class RacerTermParser {

/**
 * RacerTermParser constructor comment.
 */
public RacerTermParser() {
	super();
}
/** This method builds a representation for an all term. For this class,
	the returned value is null.
 * @return java.lang.Object The representation of the all term (null for this class).
 * @param role Object The role term.
 * @param concept Object The concept term.
 */

protected Object buildAll(Object role,Object concept) {
	return null;
}
/** This method builds a representation for an at-least term. For this class,
	the returned value is null.
 * @return java.lang.Object The representation of the at-least term (null for this class).
 * @param n int An integer.
 * @param role Object The role term.
 */

protected Object buildAtLeast(int n,Object role) {
	return null;
}
/** This method builds a representation for an at-least term. For this class,
	the returned value is null.
 * @return java.lang.Object The representation of the at-least term (null for this class).
 * @param n int An integer.
 * @param role Object The role term.
 * @param concept Object The concept term.
 */

protected Object buildAtLeast(int n,Object role,Object concept) {
	return null;
}
/** This method builds a representation for an at-most term. For this class,
	the returned value is null.
 * @return java.lang.Object The representation of the at-most term (null for this class).
 * @param n int An integer.
 * @param role Object The role term.
 */

protected Object buildAtMost(int n,Object role) {
	return null;
}
/** This method builds a representation for an at-least term. For this class,
	the returned value is null.
 * @return java.lang.Object The representation of the at-most term (null for this class).
 * @param n int An integer.
 * @param role Object The role term.
 * @param concept Object The concept term.
 */

protected Object buildAtMost(int n,Object role,Object concept) {
	return null;
}
/** This method builds an atomic concept.
 * @return java.lang.Object The representation for the atomic concept (null for this class).
 */

protected Object buildAtomicConcept(String s) {
	return null;
}
/** This method builds an atomic role.
 * @return java.lang.Object The representation for the atomic role (null for this class).
 */

protected Object buildAtomicRole(String s) {
	return null;
}
/** This method builds the bottom concept.
 * @return java.lang.Object The representation for the bottom concept (null for this class).
 */

protected Object buildBottom() {
	return null;
}
/** This method builds a representation for a conjunction term. For this class,
	the returned value is null.
 * @return java.lang.Object The representation of the conjunction term (null for this class).
 * @param terms Object[] The subconcepts.
 */

protected Object buildConjunction(Object[] terms) {
	return null;
}
/** This method builds a representation for a conjunction term. For this class,
	the returned value is null.
 * @return java.lang.Object The representation of the conjunction term (null for this class).
 * @param terms Object[] The subconcepts.
 */

protected Object buildDisjunction(Object[] terms) {
	return null;
}
/** This method builds a representation for an exactly term. For this class,
	the returned value is null.
 * @return java.lang.Object The representation of the exactly term (null for this class).
 * @param n int An integer.
 * @param role Object The role term.
 */

protected Object buildExactly(int n,Object role) {
	return null;
}
/** This method builds a representation for an exactly term. For this class,
	the returned value is null.
 * @return java.lang.Object The representation of the exactly term (null for this class).
 * @param n int An integer.
 * @param role Object The role term.
 * @param concept Object The concept term.
 */

protected Object buildExactly(int n,Object role,Object concept) {
	return null;
}
/** This method builds the inverse of a role.
 * @return java.lang.Object The representation for the inverse role (null for this class).
 */

protected Object buildInverse(Object role) {
	return null;
}
/** This method builds the negation of a concept.
 * @return java.lang.Object The representation for the negated concept (null for this class).
 */

protected Object buildNot(Object concept) {
	return null;
}
/** This method builds a representation for a some term. For this class,
	the returned value is null.
 * @return java.lang.Object The representation of the some term (null for this class).
 * @param role Object The role term.
 * @param concept Object The concept term.
 */

protected Object buildSome(Object role,Object concept) {
	return null;
}
/** This method builds the top concept.
 * @return java.lang.Object The representation for the top concept (null for this class).
 */

protected Object buildTop() {
	return null;
}
/** This method builds a litteral concept.
 * @return java.lang.Object The representation for the litteral concept (null for this class).
 */

protected Object buildLitteral() {
	return null;
}
/** This method returns the representation of the all concept represented by the vector. For this class,
	the returned value is null.
 * @return java.lang.Object The representation of the all term (null for this class).
 * @param v java.util.Vector A vector of strings. The first position is filled with "all", the second
   with the role term and the third with a concept term.
 * @exception jracer.RacerIllegalConstruction If the term is not a racer term.
 */

protected Object parseAll(Vector v) throws RacerIllegalConstruction {
	if (v.size()!=3) throw new RacerIllegalConstruction(v);
	Object role=parseRole((String)v.elementAt(1));
	Object concept=parseConcept((String)v.elementAt(2));
	return buildAll(role,concept);
}
/** This method returns the representation of the at-least concept represented by the vector. For this class,
	the returned value is null.
 * @return java.lang.Object The representation of the at-least term (null for this class).
 * @param v java.util.Vector A vector of strings. The first position is filled with "at-least", the second
   with an integer and the third with a role term.
 * @exception jracer.RacerIllegalConstruction If the term is not a racer term.
 */

protected Object parseAtLeast(Vector v) throws RacerIllegalConstruction {
	if (v.size()<3 || v.size()>4) throw new RacerIllegalConstruction(v);
	int n=parseInteger((String)v.elementAt(1));
	Object role=parseRole((String)v.elementAt(2));
	if (v.size()==3) return buildAtLeast(n,role);
	Object concept=parseConcept((String)v.elementAt(3));
	return buildAtLeast(n,role,concept);
}
/** This method returns the representation of the at-most concept represented by the vector. For this class,
	the returned value is null.
 * @return java.lang.Object The representation of the at-most term (null for this class).
 * @param v java.util.Vector A vector of strings. The first position is filled with "at-most", the second
   with an integer and the third with a role term.
 * @exception jracer.RacerIllegalConstruction If the term is not a racer term.
 */

protected Object parseAtMost(Vector v) throws RacerIllegalConstruction {
	if (v.size()<3 || v.size()>4) throw new RacerIllegalConstruction(v);
	int n=parseInteger((String)v.elementAt(1));
	Object role=parseRole((String)v.elementAt(2));
	if (v.size()==3) return buildAtMost(n,role);
	Object concept=parseConcept((String)v.elementAt(3));
	return buildAtMost(n,role,concept);
}
/** This method parses a concept term and returns the parsed object (that should correspond to a
	representation of the term). The behaviour provided in this base class is to return always null.
	Anyway, the method can be used for syntax checking purposes.
 * @return java.lang.Object The parsed concept.
 * @param c java.lang.String The string representation of the concept.
 * @exception jracer.RacerIllegalConstruction Thrown when the term does not correspond to a RACER
   term.
 */

public Object parseConcept(String c) throws RacerIllegalConstruction {
	if (c.equalsIgnoreCase("top")) return buildTop();
	if (c.equalsIgnoreCase("bottom")) return buildBottom();
        if (c.charAt(0) == '|' && c.charAt(c.length()-1) == '|') return buildLitteral();
	if (c.indexOf(' ')==-1 && c.indexOf('(')==-1 && c.indexOf(')')==-1) return buildAtomicConcept(c);
	Vector v=RacerClient.parseLispList(c);
	String constructor=(String)v.elementAt(0);
	if (constructor.equalsIgnoreCase("and")) return parseConjunction(v);
	if (constructor.equalsIgnoreCase("or")) return parseDisjunction(v);
	if (constructor.equalsIgnoreCase("some")) return parseSome(v);
	if (constructor.equalsIgnoreCase("all")) return parseAll(v);
	if (constructor.equalsIgnoreCase("at-least")) return parseAtLeast(v);
	if (constructor.equalsIgnoreCase("at-most")) return parseAtMost(v);
	if (constructor.equalsIgnoreCase("not")) return parseNot(v);
	throw new RacerIllegalConstruction(c+" is not a correct concept term.");
}
/** This method returns the representation of the conjunction represented by the vector. For this class,
	the returned value is null.
 * @return java.lang.Object The representation of the conjunction term (null for this class).
 * @param v java.util.Vector A vector of strings. The first position is filled with "and", and the rest
	of positions are filled by the subterms.
 * @exception jracer.RacerIllegalConstruction If the term is not a racer term.
 */

protected Object parseConjunction(Vector v) throws RacerIllegalConstruction {
	Object[] terms=new Object[v.size()-1];
	for(int i=1;i<v.size();i++) {
		String subterm=(String)v.elementAt(i);
		terms[i-1]=parseConcept(subterm);
	}
	return buildConjunction(terms);
}
/** This method returns the representation of the conjunction represented by the vector. For this class,
	the returned value is null.
 * @return java.lang.Object The representation of the conjunction term (null for this class).
 * @param v java.util.Vector A vector of strings. The first position is filled with "and", and the rest
	of positions are filled by the subterms.
 * @exception jracer.RacerIllegalConstruction If the term is not a racer term.
 */

protected Object parseDisjunction(Vector v) throws RacerIllegalConstruction {
	Object[] terms=new Object[v.size()-1];
	for(int i=1;i<v.size();i++) {
		String subterm=(String)v.elementAt(i);
		terms[i-1]=parseConcept(subterm);
	}
	return buildDisjunction(terms);
}
/** This method returns the representation of the at-most concept represented by the vector. For this class,
	the returned value is null.
 * @return java.lang.Object The representation of the at-most term (null for this class).
 * @param v java.util.Vector A vector of strings. The first position is filled with "at-most", the second
   with an integer and the third with a role term.
 * @exception jracer.RacerIllegalConstruction If the term is not a racer term.
 */

protected Object parseExactly(Vector v) throws RacerIllegalConstruction {
	if (v.size()<3 || v.size()>4) throw new RacerIllegalConstruction(v);
	int n=parseInteger((String)v.elementAt(1));
	Object role=parseRole((String)v.elementAt(2));
	if (v.size()==3) return buildExactly(n,role);
	Object concept=parseConcept((String)v.elementAt(3));
	return buildExactly(n,role,concept);
}
/** This method parses an integer from a string.
 * @return int The integer value.
 * @param s java.lang.String The string representation of the integer.
 * @exception jracer.RacerIllegalConstruction When the string does not correspond to an integer.
 */

protected int parseInteger(String s) throws RacerIllegalConstruction {
	int i;
	try { i=Integer.parseInt(s); }
	catch (NumberFormatException e) { throw new RacerIllegalConstruction(s+" is not a correct integer term"); }
	return i;
}
/** This method returns the representation of the inverse role represented by the vector. For this class,
	the returned value is null.
 * @return java.lang.Object The representation of the inverse term (null for this class).
 * @param v java.util.Vector A vector of strings. The first position is filled with "inv", the second
   with the role term.
 * @exception jracer.RacerIllegalConstruction If the term is not a racer term.
 */

protected Object parseInverse(Vector v) throws RacerIllegalConstruction {
	if (v.size()!=2) throw new RacerIllegalConstruction(v);
	Object role=parseRole((String)v.elementAt(1));
	return buildInverse(role);
}
/** This method returns the representation of the not concept represented by the vector. For this class,
	the returned value is null.
 * @return java.lang.Object The representation of the not term (null for this class).
 * @param v java.util.Vector A vector of strings. The first position is filled with "not", the second
   with the concept term.
 * @exception jracer.RacerIllegalConstruction If the term is not a racer term.
 */

protected Object parseNot(Vector v) throws RacerIllegalConstruction {
	if (v.size()!=2) throw new RacerIllegalConstruction(v);
	Object c=parseConcept((String)v.elementAt(1));
	return buildNot(c);
}
/** This method parses a role term and returns the parsed object (that should correspond to a
	representation of the term). The behaviour provided in this base class is to return always null.
	Anyway, the method can be used for syntax checking purposes.
 * @return java.lang.Object The parsed role.
 * @param c java.lang.String The string representation of the role.
 * @exception jracer.RacerIllegalConstruction Thrown when the term does not correspond to a RACER
   term.
 */

public Object parseRole(String c) throws RacerIllegalConstruction {
	if (c.indexOf(' ')==-1 && c.indexOf('(')==-1 && c.indexOf(')')==-1) return buildAtomicRole(c);
	Vector v=RacerClient.parseLispList(c);
	String constructor=(String)v.elementAt(0);
	if (constructor.equalsIgnoreCase("inv")) return parseInverse(v);
	throw new RacerIllegalConstruction(c+" is not a correct role term.");
}
/** This method returns the representation of the some concept represented by the vector. For this class,
	the returned value is null.
 * @return java.lang.Object The representation of the some term (null for this class).
 * @param v java.util.Vector A vector of strings. The first position is filled with "some", the second
   with the role term and the third with a concept term.
 * @exception jracer.RacerIllegalConstruction If the term is not a racer term.
 */

protected Object parseSome(Vector v) throws RacerIllegalConstruction {
	if (v.size()!=3) throw new RacerIllegalConstruction(v);
	Object role=parseRole((String)v.elementAt(1));
	Object concept=parseConcept((String)v.elementAt(2));
	return buildSome(role,concept);
}
/** This method parses a concrete domain attribute and returns the parsed object (that should correspond to a
	representation of the term). The behaviour provided in this base class is to return always null.
	Anyway, the method can be used for syntax checking purposes.
 * @return java.lang.Object The parsed attribute.
 * @param c java.lang.String The string representation of the attribute.
 * @exception jracer.RacerIllegalConstruction Thrown when the term does not correspond to a RACER
   term.
 */

public Object parseAttribute(String c) throws RacerIllegalConstruction {
	if (c.indexOf(' ')==-1 && c.indexOf('(')==-1 && c.indexOf(')')==-1) return buildAtomicRole(c);
	throw new RacerIllegalConstruction(c+" is not a correct attribute name.");
}
}
