package org.mindswap.swoop.racer;

/** This class provides an easy way to represent a role assertion. */

public class RoleAssertion extends Assertion {

	/** The predecessor individual. */
	
	private String predecessor;

	
	/** The succcessor individual. */
	
	private String successor;


	/** The role. */

	private String role;

	
/** The constructor builds a role assertion for two individuals and a role. */

public RoleAssertion(String pred,String r,String succ) {
	super();
	predecessor=pred;
	role=r;
	successor=succ;
}
/** This method retrieves the predecessor of the assertion. */

public String getPredecessor() {
	return predecessor;
}
/** This method retrieves the role of the assertion. */

public String getRole() {
	return role;
}
/** This method retrieves the successor of the assertion. */

public String getSuccessor() {
	return successor;
}
/** This method returns a string representation of the assertion. */

public String toString() {
	return "(related "+predecessor+" "+successor+" "+role+")";
}
}
