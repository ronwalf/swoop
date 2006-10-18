package org.mindswap.swoop.racer;

/** This class provides an easy representation for concept assertions. */
 
public class ConceptAssertion extends Assertion {

	/** The individual. */

	private String individual;
	

	/** The concept. */

	private String concept;


/** The constructor builds a concept assertion from an individual name and a concept term. */

public ConceptAssertion(String x,String c) {
	super();
	individual=x;
	concept=c;
}
/** This method returns the concept corresponding to the assertion. */

public String getConcept() { return concept; }
/** This method returns the individual corresponding to the assertion. */

public String getIndividual() { return individual; }
/** This method returns a string representation of the assertion. */

public String toString() {
	return "(instance "+individual+" "+concept+")";
}
}
