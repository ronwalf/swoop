package org.mindswap.swoop.racer;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Enumeration;
import java.util.Vector;

/** This class provides a full RACER client, providing Java methods to access RACER primitives;
	and performing simple parsing of the results obtained from RACER. */

public class RacerClient extends RacerSocketClient {

	/** This object is used to perform syntax checking for concepts and role terms. If it is set
	to null by the createTermParser method, no syntax checking is performed. */

	protected RacerTermParser termParser;

/** The constructor builds a RACER client for a given RACER server ip address, and a given RACER server
	port number.
 * @param ip java.lang.String The ip address.
 * @param port int The port number.
 */

public RacerClient(String ip, int port) {
	super(ip, port);
	termParser=createTermParser();
}
/** This method refers to the RACER abox-consistent? macro for the current tbox.
 * @return boolean true if the abox is consistent, and false otherwise.
 * @exception java.io.IOException Thrown when there is a communication problem with the RACER server.
 * @exception dl.racer.RacerException Thrown when the RACER server produces an ":error" message.
 */

public boolean aboxConsistentP() throws java.io.IOException, RacerException {
	String res=send("(abox-consistent?)",true);
	return parseBoolean(res);
}
/** This method refers to the RACER abox-consistent? macro.
 * @return boolean true if the abox is consistent, and false otherwise.
 * @param abox java.lang.String The abox name.
 * @exception java.io.IOException Thrown when there is a communication problem with the RACER server.
 * @exception dl.racer.RacerException Thrown when the RACER server produces an ":error" message.
 */

public boolean aboxConsistentP(String abox) throws java.io.IOException, RacerException {
	String res=send("(abox-consistent? "+abox+")",true);
	return parseBoolean(res);
}
/** This method refers to the RACER abox-realized-p function for the current tbox.
 * @return boolean true if the abox has already been realized, and false otherwise.
 * @exception java.io.IOException Thrown when there is a communication problem with the RACER server.
 * @exception dl.racer.RacerException Thrown when the RACER server produces an ":error" message.
 */

public boolean aboxRealizedP() throws java.io.IOException, RacerException {
	String res=send("(abox-realized-p)",true);
	return parseBoolean(res);
}
/** This method refers to the RACER abox-realized-p function.
 * @return boolean true if the abox has already been realized, and false otherwise.
 * @param abox java.lang.String The abox name.
 * @exception java.io.IOException Thrown when there is a communication problem with the RACER server.
 * @exception dl.racer.RacerException Thrown when the RACER server produces an ":error" message.
 */

public boolean aboxRealizedP(String abox) throws java.io.IOException, RacerException {
	String res=send("(abox-realized-p "+abox+")",true);
	return parseBoolean(res);
}
/** This method refers to the RACER add-concept-assertion function.
 * @param abox java.lang.String The abox name corresponding to the abox in which the assertion is added.
 * @param in java.lang.String The individual name.
 * @param c java.lang.String The concept term.
 * @exception java.io.IOException Thrown when there is a communication problem with the RACER server.
 * @exception dl.racer.RacerException Thrown when the RACER server produces an ":error" message.
 */

public void addConceptAssertion(String abox, String in, String c) throws java.io.IOException, RacerException {
	if (termParser!=null) termParser.parseConcept(c);
	send("(add-concept-assertion "+abox+" "+in+" "+c+")",false);
}
/** This method refers to the add-concept-axiom RACER function.
 * @param tbox java.lang.String The name of the TBox where the axiom is to be included.
 * @param c1 java.lang.String One concept term.
 * @param c2 java.lang.String The other concept term.
 * @param inc boolean Specifies whether the axiom is an inclusion axiom (true) or
   an equality axiom (false).
 * @exception java.io.IOException Thrown when there is a communication problem with the RACER server.
 * @exception dl.racer.RacerException Thrown when the RACER server produces an ":error" message.
 */

public void addConceptAxiom(String tbox,String c1, String c2,boolean inc) throws java.io.IOException, RacerException {
	if (termParser!=null) termParser.parseConcept(c1);
	if (termParser!=null) termParser.parseConcept(c2);
	send("(add-concept-axiom "+tbox+" "+c1+" "+c2+" :inclusion-p "+(inc ? "t)" : "nil)"),false);
}
/** This method refers to the RACER add-role-assertion function.
 * @param abox java.lang.String The name of the abox in which the role assertion is to be added.
 * @param in1 java.lang.String The individual name of the predecessor.
 * @param in2 java.lang.String The individual name of the successor.
 * @param r java.lang.String The role term.
 * @exception java.io.IOException Thrown when there is a communication problem with the RACER server.
 * @exception dl.racer.RacerException Thrown when the RACER server produces an ":error" message.
 */

public void addRoleAssertion(String abox, String in1, String in2, String r) throws java.io.IOException, RacerException {
	if (termParser!=null) termParser.parseRole(r);
	send("(add-role-assertion "+abox+" "+in1+" "+in2+" "+r+")",false);
}
/** This method refers to the RACER alc-concept-coherent function
 * @return boolean true if the concept is coherent and false otherwise.
 * @param c java.lang.String The concept term.
 * @param logic int The logic to be used. It can be RacerConstants.K_LOGIC, RacerConstants.K4_LOGIC, or
   RacerConstants.S4_LOGIC
 * @exception java.io.IOException Thrown when there is a communication problem with the RACER server.
 * @exception dl.racer.RacerException Thrown when the RACER server produces an ":error" message.
 */

public boolean alcConceptCoherent(String c, int logic) throws java.io.IOException, RacerException {
	if (termParser!=null) termParser.parseConcept(c);
	String res=send("(alc-concept-coherent '"+c+" :logic "+
				(logic==RacerConstants.K_LOGIC ? ":K)" : "")+
				(logic==RacerConstants.K4_LOGIC ? ":K4)" : "")+
				(logic==RacerConstants.S4_LOGIC ? ":S4)" : ""),true);
	return parseBoolean(res);
}
/** This method refers to the RACER all-aboxes function.
 * @return java.lang.String[] An array of strings with the names of the aboxes.
 * @exception java.io.IOException Thrown when there is a communication problem with the RACER server.
 * @exception dl.racer.RacerException Thrown when the RACER server produces an ":error" message.
 */

public String[] allABoxes() throws java.io.IOException, RacerException {
	String res=send("(all-aboxes)",true);
	return parseList(res);
}
/** This method refers to the RACER all-atomic-concepts function for the current tbox.
 * @return java.lang.String[] An array of strings with the names of the atomic concepts in the tbox.
 * @exception java.io.IOException Thrown when there is a communication problem with the RACER server.
 * @exception dl.racer.RacerException Thrown when the RACER server produces an ":error" message.
 */

public String[] allAtomicConcepts() throws java.io.IOException, RacerException {
	String res=send("(all-atomic-concepts)",true);
	return parseConceptSetList(res);
}
/** This method refers to the RACER all-atomic-concepts method.
 * @return java.lang.String[] An array of strings with the names of the atomic concepts in the tbox.
 * @param tbox java.lang.String The name of the tbox.
 * @exception java.io.IOException Thrown when there is a communication problem with the RACER server.
 * @exception dl.racer.RacerException Thrown when the RACER server produces an ":error" message.
 */

public String[] allAtomicConcepts(String tbox) throws java.io.IOException, RacerException {
	String res=send("(all-atomic-concepts "+tbox+")",true);
	return parseConceptSetList(res);
}
/**  This method refers to the all-concept-assertions RACER macro for the current ABox.
 * @return ConceptAssertion[] The set of concept assertions in the ABox.
 * @exception java.io.IOException Thrown when there is a communication problem with the RACER server.
 * @exception dl.racer.RacerException Thrown when the RACER server produces an ":error" message.
 */

public ConceptAssertion[] allConceptAssertions() throws IOException, RacerException {
	String res=send("(all-concept-assertions)",true);
	return parseConceptAssertionList(res);
}
/**  This method refers to the all-concept-assertions RACER macro.
 * @return ConceptAssertion[] An array with the concept assertions in the ABox.
 * @param abox java.lang.String The abox name.
 * @exception java.io.IOException Thrown when there is a communication problem with the RACER server.
 * @exception dl.racer.RacerException Thrown when the RACER server produces an ":error" message.
 */

public ConceptAssertion[] allConceptAssertions(String abox) throws IOException, RacerException {
	String res=send("(all-concept-assertions "+abox+")",true);
	return parseConceptAssertionList(res);
}
/** This method refers to the all-concept-assertions-for-individual RACER macro for the current ABox.
 * @return ConceptAssertion[] An array of concept assertions.
 * @param in java.lang.String The individual name.
 * @exception java.io.IOException Thrown when there is a communication problem with the RACER server.
 * @exception dl.racer.RacerException Thrown when the RACER server produces an ":error" message.
 */

public ConceptAssertion[] allConceptAssertionsForIndividual(String in) throws IOException, RacerException {
	String res=send("(all-concept-assertions-for-individual "+in+")",true);
	return parseConceptAssertionList(res);
}
/** This method refers to the all-concept-assertions-for-individual RACER macro.
 * @return ConceptAssertion[] An array of concept assertions.
 * @param abox java.lang.String The name of the abox.
 * @exception java.io.IOException Thrown when there is a communication problem with the RACER server.
 * @exception dl.racer.RacerException Thrown when the RACER server produces an ":error" message.
 */

public ConceptAssertion[] allConceptAssertionsForIndividual(String in, String abox) throws IOException, RacerException {
	String res=send("(all-concept-assertions-for-individual "+in+" "+abox+")",true);
	return parseConceptAssertionList(res);
}
/** This method refers to the RACER all-features function for the current tbox.
 * @return java.lang.String[] An array of strings with the features in the tbox.
 * @exception java.io.IOException Thrown when there is a communication problem with the RACER server.
 * @exception dl.racer.RacerException Thrown when the RACER server produces an ":error" message.
 */

public String[] allFeatures() throws java.io.IOException, RacerException {
	String res=send("(all-features)",true);
	return parseList(res);
}
/** This method refers to the RACER all-features function.
 * @return java.lang.String[] An array of strings with the features in the tbox.
 * @param tbox java.lang.String The name of the tbox.
 * @exception java.io.IOException Thrown when there is a communication problem with the RACER server.
 * @exception dl.racer.RacerException Thrown when the RACER server produces an ":error" message.
 */

public String[] allFeatures(String tbox) throws java.io.IOException, RacerException {
	String res=send("(all-features "+tbox+")",true);
	return parseList(res);
}
/** This method refers to the RACER all-individuals function for the current tbox.
 * @return java.lang.String[] An array of strings with the names of the individuals in the abox.
 * @exception java.io.IOException Thrown when there is a communication problem with the RACER server.
 * @exception dl.racer.RacerException Thrown when the RACER server produces an ":error" message.
 */

public String[] allIndividuals() throws java.io.IOException, RacerException {
	String res=send("(all-individuals)",true);
	return parseList(res);
}
/** This method refers to the RACER all-individuals function.
 * @return java.lang.String[] An array of strings with the names of the individuals in the tbox.
 * @param tbox java.lang.String The name of the tbox.
 * @exception java.io.IOException Thrown when there is a communication problem with the RACER server.
 * @exception dl.racer.RacerException Thrown when the RACER server produces an ":error" message.
 */

public String[] allIndividuals(String tbox) throws java.io.IOException, RacerException {
	String res=send("(all-individuals "+tbox+")",true);
	return parseList(res);
}
/** This method refers to the all-role-assertions RACER macro fo the current ABox.
 * @return RoleAssertion[] An array of role assertions.
 * @exception java.io.IOException Thrown when there is a communication problem with the RACER server.
 * @exception dl.racer.RacerException Thrown when the RACER server produces an ":error" message.
 */

public RoleAssertion[] allRoleAssertions() throws IOException, RacerException {
	String res=send("(all-role-assertions)",true);
	return parseRoleAssertionList(res);
}
/** This method refers to the all-role-assertions RACER macro.
 * @return RoleAssertion[] An array of role assertions.
 * @param abox java.lang.String The abox name.
 * @exception java.io.IOException Thrown when there is a communication problem with the RACER server.
 * @exception dl.racer.RacerException Thrown when the RACER server produces an ":error" message.
 */
public RoleAssertion[] allRoleAssertions(String abox) throws IOException, RacerException {
	String res=send("(all-role-assertions "+abox+")",true);
	return parseRoleAssertionList(res);
}
/** This method refers to the all-role-assertions-for-individual-in-domain RACER macro
	for the current ABox.
 * @return RoleAssertion[] An array of role assertions.
 * @exception java.io.IOException Thrown when there is a communication problem with the RACER server.
 * @exception dl.racer.RacerException Thrown when the RACER server produces an ":error" message.
 */

public RoleAssertion[] allRoleAssertionsForIndividualInDomain(String in) throws IOException, RacerException {
	String res=send("(all-role-assertions-for-individual-in-domain "+in+")",true);
	return parseRoleAssertionList(res);
}
/** This method refers to the all-role-assertions-for-individual-in-domain RACER macro.
 * @return RoleAssertion[] An array of role assertions.
 * @param in java.lang.String The individual name.
 * @param abox java.lang.String The abox name.
 * @exception java.io.IOException Thrown when there is a communication problem with the RACER server.
 * @exception dl.racer.RacerException Thrown when the RACER server produces an ":error" message.
 */

public RoleAssertion[] allRoleAssertionsForIndividualInDomain(String in, String abox) throws IOException, RacerException {
	String res=send("(all-role-assertions-for-individual-in-domain "+in+" "+abox+")",true);
	return parseRoleAssertionList(res);
}
/** This method refers to the all-role-assertions-for-individual-in-range RACER macro.
 * @return RoleAssertion[] An array of role assertions.
 * @param in java.lang.String The individual name.
 * @exception java.io.IOException Thrown when there is a communication problem with the RACER server.
 * @exception dl.racer.RacerException Thrown when the RACER server produces an ":error" message.
 */

public RoleAssertion[] allRoleAssertionsForIndividualInRange(String in) throws IOException, RacerException {
	String res=send("(all-role-assertions-for-individual-in-range "+in+")",true);
	return parseRoleAssertionList(res);
}
/** This method refers to the all-role-assertions-for-individual-in-range RACER macro.
 * @return RoleAssertion[] An array of role assertions.
 * @param in java.lang.String The individual name.
 * @param abox java.lang.String The abox name.
 * @exception java.io.IOException Thrown when there is a communication problem with the RACER server.
 * @exception dl.racer.RacerException Thrown when the RACER server produces an ":error" message.
 */
public RoleAssertion[] allRoleAssertionsForIndividualInRange(String in, String abox) throws IOException, RacerException {
	String res=send("(all-role-assertions-for-individual-in-range "+in+" "+abox+")",true);
	return parseRoleAssertionList(res);
}
/** This method refers to the RACER all-roles function for the current tbox.
 * @return java.lang.String[] An array of strings with the roles in the tbox.
 * @exception java.io.IOException Thrown when there is a communication problem with the RACER server.
 * @exception dl.racer.RacerException Thrown when the RACER server produces an ":error" message.
 */

public String[] allRoles() throws java.io.IOException, RacerException {
	String res=send("(all-roles)",true);
	return parseList(res);
}
/** This method refers to the RACER all-roles function.
 * @return java.lang.String[] An array of strings with the roles in the tbox.
 * @param tbox java.lang.String The name of the tbox.
 * @exception java.io.IOException Thrown when there is a communication problem with the RACER server.
 * @exception dl.racer.RacerException Thrown when the RACER server produces an ":error" message.
 */

public String[] allRoles(String tbox) throws java.io.IOException, RacerException {
	String res=send("(all-roles "+tbox+")",true);
	return parseList(res);
}
/** This method refers to the RACER all-tboxes function.
 * @return java.lang.String[] An array of strings with the names of the tboxes.
 * @exception java.io.IOException Thrown when there is a communication problem with the RACER server.
 * @exception dl.racer.RacerException Thrown when the RACER server produces an ":error" message.
 */

public String[] allTBoxes() throws java.io.IOException, RacerException {
	String res=send("(all-tboxes)",true);
	return parseList(res);
}
/** This method refers to the RACER all-transitive-roles function for the current tbox.
 * @return java.lang.String[] An array of strings with the transitive roles in the tbox.
 * @exception java.io.IOException Thrown when there is a communication problem with the RACER server.
 * @exception dl.racer.RacerException Thrown when the RACER server produces an ":error" message.
 */

public String[] allTransitiveRoles() throws java.io.IOException, RacerException {
	String res=send("(all-transitive-roles)",true);
	return parseList(res);
}
/** This method refers to the RACER all-transitive-roles function.
 * @return java.lang.String[] An array of strings with the transitive roles in the tbox.
 * @param tbox java.lang.String The name of the tbox.
 * @exception java.io.IOException Thrown when there is a communication problem with the RACER server.
 * @exception dl.racer.RacerException Thrown when the RACER server produces an ":error" message.
 */

public String[] allTransitiveRoles(String tbox) throws java.io.IOException, RacerException {
	String res=send("(all-transitive-roles "+tbox+")",true);
	return parseList(res);
}
/** This method refers to the RACER check-tbox-coherence function for the current tbox.
 * @return String[] An array with all the unsatisfiable atomic concepts in the tbox.
 * @exception java.io.IOException Thrown when there is a communication problem with the RACER server.
 * @exception dl.racer.RacerException Thrown when the RACER server produces an ":error" message.
 */

public String[] checkTBoxCoherence() throws java.io.IOException, RacerException {
	String res=send("(check-tbox-coherence)",true);
        return parseList(res); // RC 20020330  as ConceptSetList strips the first and last character
//	return parseConceptSetList(res);
}
/** This method refers to the RACER classify-tbox function.
 * @return String[] An array with all the unsatisfiable atomic concepts in the tbox.
 * @param tbox java.lang.String The tbox name.
 * @exception java.io.IOException Thrown when there is a communication problem with the RACER server.
 * @exception dl.racer.RacerException Thrown when the RACER server produces an ":error" message.
 */

public String[] checkTBoxCoherence(String tbox) throws java.io.IOException, RacerException {
	String res=send("(check-tbox-coherence "+tbox+")",true);
	return parseConceptSetList(res);
}
/** This method refers to the RACER classify-tbox function for the current tbox.
 * @exception java.io.IOException Thrown when there is a communication problem with the RACER server.
 * @exception dl.racer.RacerException Thrown when the RACER server produces an ":error" message.
 */

public void classifyTBox() throws java.io.IOException, RacerException {
	send("(classify-tbox)",false);
}
/** This method refers to the RACER classify-tbox function.
 * @param tbox java.lang.String The tbox name.
 * @exception java.io.IOException Thrown when there is a communication problem with the RACER server.
 * @exception dl.racer.RacerException Thrown when the RACER server produces an ":error" message.
 */

public void classifyTBox(String tbox) throws java.io.IOException, RacerException {
	send("(classify-tbox "+tbox+")",false);
}

public String[] associatedAboxes(String tbox) throws java.io.IOException, RacerException { // Method added, RC20020611
        String res = send("(associated-aboxes "+ tbox +")", true);
        return parseList(res);
}
/** This method refers to the RACER concept-ancestors macro using the current TBox.
 * @return String[] an array of strings containing all the atomic concept ancestors of a concept
	in the tbox. For every equivalence concept set returned by RACER, only the first concept (taken
	as the representative of the class) is returned. This is done in order to return a String[] and
	not an array of array of strings. In order to know the equivalent set of concepts for a given
	concept, the conceptSynonyms method can be used.
 * @param cn java.lang.String the concept name
 * @exception java.io.IOException Thrown when there is a communication problem with the RACER server.
 * @exception dl.racer.RacerException Thrown when the RACER server produces an ":error" message.
 */

public String[] conceptAncestors(String cn) throws RacerException, IOException {
	if (termParser!=null) termParser.parseConcept(cn);
	String res=send("(concept-ancestors "+cn+")",true);
	return parseConceptSetList(res);
}
/** This method refers to the RACER concept-ancestors macro using the current TBox.
 * @return String[] an array of strings containing all the atomic concept ancestors of a concept
	in the tbox. For every equivalence concept set returned by RACER, only the first concept (taken
	as the representative of the class) is returned. This is done in order to return a String[] and
	not an array of array of strings. In order to know the equivalent set of concepts for a given
	concept, the conceptSynonyms method can be used.
 * @param cn java.lang.String the concept name
 * @param tbox java.lang.String The tbox
 * @exception java.io.IOException Thrown when there is a communication problem with the RACER server.
 * @exception dl.racer.RacerException Thrown when the RACER server produces an ":error" message.
 */

public String[] conceptAncestors(String c, String tbox) throws RacerException, IOException {
	if (termParser!=null) termParser.parseConcept(c);
	String res=send("(concept-ancestors "+c+" "+tbox+")",true);
	return parseConceptSetList(res);
}
/** This method refers to the RACER concept-children macro using the current TBox.
 * @return String[] an array of strings containing all the atomic concept children of a concept
	in the tbox. For every equivalence concept set returned by RACER, only the first concept (taken
	as the representative of the class) is returned. This is done in order to return a String[] and
	not an array of array of strings. In order to know the equivalent set of concepts for a given
	concept, the conceptSynonyms method can be used.
 * @param cn java.lang.String the concept name
 * @exception java.io.IOException Thrown when there is a communication problem with the RACER server.
 * @exception dl.racer.RacerException Thrown when the RACER server produces an ":error" message.
 */

public String[] conceptChildren(String cn) throws RacerException, IOException {
	if (termParser!=null) termParser.parseConcept(cn);
	String res=send("(concept-children "+cn+")",true);
	return parseConceptSetList(res);
}
/** This method refers to the RACER concept-children macro using the current TBox.
 * @return String[] an array of strings containing all the atomic concept children of a concept
	in the tbox. For every equivalence concept set returned by RACER, only the first concept (taken
	as the representative of the class) is returned. This is done in order to return a String[] and
	not an array of array of strings. In order to know the equivalent set of concepts for a given
	concept, the conceptSynonyms method can be used.
 * @param cn java.lang.String the concept name
 * @param tbox java.lang.String The tbox
 * @exception java.io.IOException Thrown when there is a communication problem with the RACER server.
 * @exception dl.racer.RacerException Thrown when the RACER server produces an ":error" message.
 */

public String[] conceptChildren(String c, String tbox) throws RacerException, IOException {
	if (termParser!=null) termParser.parseConcept(c);
	String res=send("(concept-children "+c+" "+tbox+")",true);
	return parseConceptSetList(res);
}
/** This method refers to the RACER concept-descendants macro using the current TBox.
 * @return String[] an array of strings containing all the atomic concept descendants of a concept
	in the tbox. For every equivalence concept set returned by RACER, only the first concept (taken
	as the representative of the class) is returned. This is done in order to return a String[] and
	not an array of array of strings. In order to know the equivalent set of concepts for a given
	concept, the conceptSynonyms method can be used.
 * @param cn java.lang.String the concept name
 * @exception java.io.IOException Thrown when there is a communication problem with the RACER server.
 * @exception dl.racer.RacerException Thrown when the RACER server produces an ":error" message.
 */

public String[] conceptDescendants(String cn) throws RacerException, IOException {
	if (termParser!=null) termParser.parseConcept(cn);
	String res=send("(concept-descendants "+cn+")",true);
	return parseConceptSetList(res);
}
/** This method refers to the RACER concept-descendants macro using the current TBox.
 * @return String[] an array of strings containing all the atomic concept descendants of a concept
	in the tbox. For every equivalence concept set returned by RACER, only the first concept (taken
	as the representative of the class) is returned. This is done in order to return a String[] and
	not an array of array of strings. In order to know the equivalent set of concepts for a given
	concept, the conceptSynonyms method can be used.
 * @param cn java.lang.String the concept name
 * @param tbox java.lang.String The tbox
 * @exception java.io.IOException Thrown when there is a communication problem with the RACER server.
 * @exception dl.racer.RacerException Thrown when the RACER server produces an ":error" message.
 */

public String[] conceptDescendants(String c, String tbox) throws RacerException, IOException {
	if (termParser!=null) termParser.parseConcept(c);
	String res=send("(concept-descendants "+c+" "+tbox+")",true);
	return parseConceptSetList(res);
}
/** This method refers to the concept-disjoint? RACER macro, in which the current tbox is used.
 * @return boolean true if c1 and c2 are disjoint and false otherwise.
 * @param c1 java.lang.String one concept term.
 * @param c2 java.lang.String the other concept term.
 * @exception java.io.IOException Thrown when there is a communication problem with the RACER server.
 * @exception dl.racer.RacerException Thrown when the RACER server produces an ":error" message.
 */

public boolean conceptDisjointP(String c1, String c2) throws java.io.IOException, RacerException {
	if (termParser!=null) termParser.parseConcept(c1);
	if (termParser!=null) termParser.parseConcept(c2);
	String res=send("(concept-disjoint? "+c1+" "+c2+")",true);
	return parseBoolean(res);
}
/** This method refers to the RACER concept-disjoint-p function.
 * @return boolean true if c1 and c2 are disjoint in tbox; and false otherwise.
 * @param c1 java.lang.String one concept term.
 * @param c2 java.lang.String the other concept term.
 * @param tbox java.lang.String The tbox name.
 * @exception java.io.IOException Thrown when there is a communication problem with the RACER server.
 * @exception dl.racer.RacerException Thrown when the RACER server produces an ":error" message.
 */

public boolean conceptDisjointP(String c1, String c2, String tbox) throws java.io.IOException, RacerException {
	if (termParser!=null) termParser.parseConcept(c1);
	if (termParser!=null) termParser.parseConcept(c2);
	String res=send("(concept-disjoint-p "+c1+" "+c2+" "+tbox+")",true);
	return parseBoolean(res);
}
/** This method refers to the concept-equivalent? RACER macro, in which the current tbox is used.
 * @return boolean true if c1 equivalent to c2 and false otherwise.
 * @param c1 java.lang.String one concept term.
 * @param c2 java.lang.String the other concept term.
 * @exception java.io.IOException Thrown when there is a communication problem with the RACER server.
 * @exception dl.racer.RacerException Thrown when the RACER server produces an ":error" message.
 */

public boolean conceptEquivalentP(String c1, String c2) throws java.io.IOException, RacerException {
	if (termParser!=null) termParser.parseConcept(c1);
	if (termParser!=null) termParser.parseConcept(c2);
	String res=send("(concept-equivalent? "+c1+" "+c2+")",true);
	return parseBoolean(res);
}
/** This method refers to the RACER concept-equivalent-p function.
 * @return boolean true if c1 is equivalent to c2 in tbox; and false otherwise.
 * @param c1 java.lang.String one concept term.
 * @param c2 java.lang.String the other concept term.
 * @param tbox java.lang.String The tbox name.
 * @exception java.io.IOException Thrown when there is a communication problem with the RACER server.
 * @exception dl.racer.RacerException Thrown when the RACER server produces an ":error" message.
 */

public boolean conceptEquivalentP(String c1, String c2, String tbox) throws java.io.IOException, RacerException {
	if (termParser!=null) termParser.parseConcept(c1);
	if (termParser!=null) termParser.parseConcept(c2);
	String res=send("(concept-equivalent-p "+c1+" "+c2+" "+tbox+")",true);
	return parseBoolean(res);
}
/** This method refers to the RACER concept-instances macro.
 * @return java.lang.String[] A list of individual names.
 * @param c java.lang.String The concept term.
 * @exception java.io.IOException Thrown when there is a communication problem with the RACER server.
 * @exception dl.racer.RacerException Thrown when the RACER server produces an ":error" message.
 */

public String[] conceptInstances(String c) throws java.io.IOException, RacerException {
	if (termParser!=null) termParser.parseConcept(c);
	String res=send("(concept-instances "+c+")",true);
	return parseList(res);
}
/** This method refers to the RACER concept-instances macro.
 * @return java.lang.String[] A list of concept names (one concept name for every concept equivalence
   set returned by RACER).
 * @param c java.lang.String the concept term.
 * @param abox java.lang.String The abox name.
 * @exception java.io.IOException Thrown when there is a communication problem with the RACER server.
 * @exception dl.racer.RacerException Thrown when the RACER server produces an ":error" message.
 */

public String[] conceptInstances(String c,String abox) throws java.io.IOException, RacerException {
	if (termParser!=null) termParser.parseConcept(c);
	String res=send("(concept-instances "+c+" "+abox+")",true);
	return parseList(res);
}
/** This method refers to the concept-is-primitive-p RACER macro. The current tbox is used.
 * @return boolean true if the concept is satisfiable and false otherwise.
 * @param c java.lang.String The concept term.
 * @exception java.io.IOException Thrown when there is a communication problem with the RACER server.
 * @exception dl.racer.RacerException Thrown when the RACER server produces an ":error" message.
 */

public boolean conceptIsPrimitiveP(String c) throws java.io.IOException, RacerException {
	if (termParser!=null) termParser.parseConcept(c);
	String res=send("(concept-is-primitive-p "+c+")",true);
	return parseBoolean(res);
}
/** This method refers to the RACER concept-is-primitive-p function.
 * @return boolean True if the concept is a primitive one and false otherwise.
 * @param c java.lang.String the concept name
 * @param tbox java.lang.String The tbox
 * @exception java.io.IOException Thrown when there is a communication problem with the RACER server.
 * @exception dl.racer.RacerException Thrown when the RACER server produces an ":error" message.
 */

public boolean conceptIsPrimitiveP(String c, String tbox) throws RacerException, IOException {
	if (termParser!=null) termParser.parseConcept(c);
	String res=send("(concept-is-primitive-p "+c+" "+tbox+")",true);
	return parseBoolean(res);
}
/** This method refers to the concept-p RACER macro. The current tbox is used.
 * @return boolean true if the concept is satisfiable and false otherwise.
 * @param c java.lang.String The concept term.
 * @exception java.io.IOException Thrown when there is a communication problem with the RACER server.
 * @exception dl.racer.RacerException Thrown when the RACER server produces an ":error" message.
 */

public boolean conceptP(String c) throws java.io.IOException, RacerException {
	if (termParser!=null) termParser.parseConcept(c);
	String res=send("(concept-p "+c+")",true);
	return parseBoolean(res);
}
/** This method refers to the RACER concept-p function.
 * @return boolean
 * @param c java.lang.String
 * @param tbox java.lang.String
 * @exception java.io.IOException Thrown when there is a communication problem with the RACER server.
 * @exception dl.racer.RacerException Thrown when the RACER server produces an ":error" message.
 */

public boolean conceptP(String c, String tbox) throws RacerException, IOException {
	if (termParser!=null) termParser.parseConcept(c);
	String res=send("(concept-p "+c+" "+tbox+")",true);
	return parseBoolean(res);
}
/** This method refers to the RACER concept-parents macro using the current TBox.
 * @return String[] an array of strings containing all the atomic concept parents of a concept
	in the tbox. For every equivalence concept set returned by RACER, only the first concept (taken
	as the representative of the class) is returned. This is done in order to return a String[] and
	not an array of array of strings. In order to know the equivalent set of concepts for a given
	concept, the conceptSynonyms method can be used.
 * @param cn java.lang.String the concept name
 * @exception java.io.IOException Thrown when there is a communication problem with the RACER server.
 * @exception dl.racer.RacerException Thrown when the RACER server produces an ":error" message.
 */

public String[] conceptParents(String cn) throws RacerException, IOException {
	if (termParser!=null) termParser.parseConcept(cn);
	String res=send("(concept-parents "+cn+")",true);
	return parseConceptSetList(res);
}
/** This method refers to the RACER concept-parents macro using the current TBox.
 * @return String[] an array of strings containing all the atomic concept parents of a concept
	in the tbox. For every equivalence concept set returned by RACER, only the first concept (taken
	as the representative of the class) is returned. This is done in order to return a String[] and
	not an array of array of strings. In order to know the equivalent set of concepts for a given
	concept, the conceptSynonyms method can be used.
 * @param cn java.lang.String the concept name
 * @param tbox java.lang.String The tbox
 * @exception java.io.IOException Thrown when there is a communication problem with the RACER server.
 * @exception dl.racer.RacerException Thrown when the RACER server produces an ":error" message.
 */

public String[] conceptParents(String c, String tbox) throws RacerException, IOException {
	if (termParser!=null) termParser.parseConcept(c);
	String res=send("(concept-parents "+c+" "+tbox+")",true);
	return parseConceptSetList(res);
}
/** This method refers to the concept-satisfiable? RACER macro. The current tbox is used.
 * @return boolean true if the concept is satisfiable and false otherwise.
 * @param c java.lang.String The concept term.
 * @exception java.io.IOException Thrown when there is a communication problem with the RACER server.
 * @exception dl.racer.RacerException Thrown when the RACER server produces an ":error" message.
 */

public boolean conceptSatisfiableP(String c) throws java.io.IOException, RacerException {
	if (termParser!=null) termParser.parseConcept(c);
// RC20020321   Added Closing Bracket!
	System.out.println("test1:"+send("(instance i c)"));
    System.out.println("test2:"+send("(time (retrieve (?x) (?x c)))"));
	String res=send("(time (concept-satisfiable? "+c+"))",true);
	return parseBoolean(res);
}
/** This method refers to the RACER concept-satisfiable-p function.
 * @return boolean true if the concept is satisfiable and false otherwise.
 * @param c java.lang.String the concept.
 * @param tbox java.lang.String the tbox name.
 * @exception java.io.IOException Thrown when there is a communication problem with the RACER server.
 * @exception dl.racer.RacerException Thrown when the RACER server produces an ":error" message.
 */

public boolean conceptSatisfiableP(String c, String tbox) throws RacerException, IOException {
	if (termParser!=null) termParser.parseConcept(c);
	String res=send("(time (concept-satisfiable-p "+c+" "+tbox+"))",true);
	return parseBoolean(res);
}
/** This method refers to the concept-subsumes? RACER macro, in which the current tbox is used.
 * @return boolean true if c1 subsumes c2 and false otherwise.
 * @param c1 java.lang.String concept term for the subsumer.
 * @param c2 java.lang.String concept term for the subsumee.
 * @exception java.io.IOException Thrown when there is a communication problem with the RACER server.
 * @exception dl.racer.RacerException Thrown when the RACER server produces an ":error" message.
 */

public boolean conceptSubsumesP(String c1, String c2) throws java.io.IOException, RacerException {
	if (termParser!=null) termParser.parseConcept(c1);
	if (termParser!=null) termParser.parseConcept(c2);
	String res=send("(concept-subsumes? "+c1+" "+c2+")",true);
	return parseBoolean(res);
}
/** This method refers to the RACER concept-subsumes-p function.
 * @return boolean true if c1 subsumes c2 in tbox; and false otherwise.
 * @param c1 java.lang.String the subsumer concept term.
 * @param c2 java.lang.String the subsumee concept term.
 * @param tbox java.lang.String The tbox name.
 * @exception java.io.IOException Thrown when there is a communication problem with the RACER server.
 * @exception dl.racer.RacerException Thrown when the RACER server produces an ":error" message.
 */

public boolean conceptSubsumesP(String c1, String c2, String tbox) throws java.io.IOException, RacerException {
	if (termParser!=null) termParser.parseConcept(c1);
	if (termParser!=null) termParser.parseConcept(c2);
	String res=send("(concept-subsumes-p "+c1+" "+c2+" "+tbox+")",true);
	return parseBoolean(res);
}
/** This method refers to the RACER concept-synonyms macro using the current TBox.
 * @return String[] an array of strings containing the concept synonyms for cn.
 * @param cn java.lang.String the concept name
 * @exception java.io.IOException Thrown when there is a communication problem with the RACER server.
 * @exception dl.racer.RacerException Thrown when the RACER server produces an ":error" message.
 */

public String[] conceptSynonyms(String c) throws RacerException, IOException {
	if (termParser!=null) termParser.parseConcept(c);
	String res=send("(concept-synonyms "+c+")",true);
	return parseList(res);
}
/** This method refers to the RACER concept-synonyms macro.
 * @return String[] an array of strings containing the concept synonyms for cn.
 * @param cn java.lang.String the concept name
 * @param tbox java.lang.String The tbox
 * @exception java.io.IOException Thrown when there is a communication problem with the RACER server.
 * @exception dl.racer.RacerException Thrown when the RACER server produces an ":error" message.
 */

public String[] conceptSynonyms(String c, String tbox) throws RacerException, IOException {
	if (termParser!=null) termParser.parseConcept(c);
	String res=send("(concept-synonyms "+c+" "+tbox+")",true);
	return parseList(res);
}
/** This method creates a RacerTermParser that will be used to perform syntax checking on the concept and
	role terms.
 * @return jracer.RacerTermParser The term parser.
 */

protected RacerTermParser createTermParser() {
	return new RacerTermParser();
}
/** This method refers to the RACER KRSS macro.
 * @param cn java.lang.String The concept name being defined.
 * @param c java.lang.String A concept term.
 * @exception java.io.IOException Thrown when there is a communication problem with the RACER server.
 * @exception dl.racer.RacerException Thrown when the RACER server produces an ":error" message.
 */

public void defineConcept(String cn, String c) throws java.io.IOException, RacerException {
	if (termParser!=null) termParser.parseConcept(cn);
	if (termParser!=null) termParser.parseConcept(c);
	send("(define-concept "+cn+" "+c+")",false);
}
/** This method refers to the RACER define-distinct-individual macro.
 * @param in java.lang.String The name of the individual.
 * @exception java.io.IOException Thrown when there is a communication problem with the RACER server.
 * @exception dl.racer.RacerException Thrown when the RACER server produces an ":error" message.
 */

public void defineDistinctIndividual(String in) throws java.io.IOException, RacerException {
	send("(define-distinct-individual "+in+")",false);
}
/** This method refers to the RACER define-distinct-individual macro.
 * @param in java.lang.String The name of the individual.
 * @exception java.io.IOException Thrown when there is a communication problem with the RACER server.
 * @exception dl.racer.RacerException Thrown when the RACER server produces an ":error" message.
 */

public void defineDistinctIndividual(String in,String abox) throws java.io.IOException, RacerException {
	send("(define-distinct-individual "+in+" "+abox+")",false);
}
/** This method refers to the RACER KRSS macro.
 * @param cn java.lang.String The concept name being defined.
 * @param c java.lang.String A concept term.
 * @exception java.io.IOException Thrown when there is a communication problem with the RACER server.
 * @exception dl.racer.RacerException Thrown when the RACER server produces an ":error" message.
 */

public void definePrimitiveConcept(String cn, String c) throws java.io.IOException, RacerException {
	send("(define-primitive-concept "+cn+" "+c+")",false);
}
/** This method refers to the RACER define-primitive-role macro.
 * @param role java.lang.String The role name, possibly extended with the RACER macro keywords.
 * @exception java.io.IOException Thrown when there is a communication problem with the RACER server.
 * @exception dl.racer.RacerException Thrown when the RACER server produces an ":error" message.
 */

public void definePrimitiveRole(String role) throws java.io.IOException, RacerException {
	if (termParser!=null) termParser.parseRole(role);
	send("(define-primitive-role "+role+")",false);
}
/** This method refers to the RACER define-primitive-role macro. The RACER macro keywords have been
	converted into method parameters. If some of the values for these keywords is the default value,
	the corresponding parameter should be set to null.
 * @param rn java.lang.String The role name.
 * @param properties int An integer stating the properties of the role. It can be a bit-OR operation
   of RacerConstants.TRANSITIVE_ROLE, RacerConstants.FEATURE, RacerConstants.SYMMETRIC_ROLE, and
   RacerConstants.REFLEXIVE_ROLE. It can be also RacerConstants.PLAIN_ROLE, stating that the role
   has none of the previous properties.
 * @param inverse java.lang.String The name for the inverse role if supplied.
 * @param domain java.lang.String The domain definition.
 * @param range java.lang.String The range definition.
 * @param parents java.lang.String A list of superroles for the role. The superroles are separated
   by one or more spaces.
 * @exception java.io.IOException Thrown when there is a communication problem with the RACER server.
 * @exception dl.racer.RacerException Thrown when the RACER server produces an ":error" message.
 */

public void definePrimitiveRole(String rn, int properties, String inverse, String domain, String range, String parents) throws java.io.IOException, RacerException {
	if (termParser!=null) termParser.parseRole(rn);
	String message="(define-primitive-role "+rn;
	if ((properties & RacerConstants.TRANSITIVE_ROLE) != 0) message+=" :transitive t";
	if ((properties & RacerConstants.FEATURE) != 0) message+=" :feature t";
	if ((properties & RacerConstants.SYMMETRIC_ROLE) != 0) message+=" :symmetric t";
	if ((properties & RacerConstants.REFLEXIVE_ROLE) != 0) message+=" :reflexive t";
	if (inverse!=null) message+=" :inverse "+inverse;
	if (domain!=null) message+=" :domain "+domain;
	if (range!=null) message+=" :range "+range;
	if (parents!=null) message+=" :parents "+parents;
	message+=")";
	send(message,false);
}
/** This method refers to the disjoint RACER macro
 * @param cns java.lang.String A string containing the names of the concepts separated by one or more
   spaces.
 * @exception java.io.IOException Thrown when there is a communication problem with the RACER server.
 * @exception dl.racer.RacerException Thrown when the RACER server produces an ":error" message.
 */

public void disjoint(String cns) throws java.io.IOException, RacerException {
	send("(disjoint "+cns+")",true);
}
/** This method refers to the RACER equivalent macro.
 * @param c1 java.lang.String
 * @param c2 java.lang.String
 * @exception java.io.IOException Thrown when there is a communication problem with the RACER server.
 * @exception dl.racer.RacerException Thrown when the RACER server produces an ":error" message.
 */

public void equivalent(String c1, String c2) throws java.io.IOException, RacerException {
	if (termParser!=null) termParser.parseConcept(c1);
	if (termParser!=null) termParser.parseConcept(c2);
	send("(equivalent "+c1+" "+c2+")",true);
}
/** This method refers to the feature-p RACER function.
 * @return boolean true if r is a feature and false otherwise.
 * @param r java.lang.String a role term.
 * @param tbox java.lang.String tbox name.
 * @exception java.io.IOException Thrown when there is a communication problem with the RACER server.
 * @exception dl.racer.RacerException Thrown when the RACER server produces an ":error" message.
 */

public boolean featureP(String r, String tbox) throws java.io.IOException, RacerException {
	if (termParser!=null) termParser.parseRole(r);
	String res=send("(feature-p "+r+" "+tbox+")",true);
	return parseBoolean(res);
}
/** This method refers to the feature-p RACER function.
 * @return boolean true if r is a feature and false otherwise.
 * @param r java.lang.String a role term.
 * @exception java.io.IOException Thrown when there is a communication problem with the RACER server.
 * @exception dl.racer.RacerException Thrown when the RACER server produces an ":error" message.
 */

public boolean featureP(String r) throws java.io.IOException, RacerException {
	if (termParser!=null) termParser.parseRole(r);
	String res=send("(feature-p "+r+")",true);
	return parseBoolean(res);
}
/** This method refers to the cd-attribute-p RACER function.
 * @return boolean true if r is a concrete domain attribute and false otherwise.
 * @param r java.lang.String a concrete domain attribute name.
 * @param tbox java.lang.String tbox name.
 * @exception java.io.IOException Thrown when there is a communication problem with the RACER server.
 * @exception dl.racer.RacerException Thrown when the RACER server produces an ":error" message.
 */

public boolean CDattributeP(String r, String tbox) throws java.io.IOException, RacerException {
	if (termParser!=null) termParser.parseAttribute(r);
	String res=send("(cd-attribute-p "+r+" "+tbox+")",true);
	return parseBoolean(res);
}
/** This method refers to the cd-attribute-p RACER function.
 * @return boolean true if r is a concrete domain attribute and false otherwise.
 * @param r java.lang.String a concrete domain attribute name.
 * @exception java.io.IOException Thrown when there is a communication problem with the RACER server.
 * @exception dl.racer.RacerException Thrown when the RACER server produces an ":error" message.
 */

public boolean CDattributeP(String r) throws java.io.IOException, RacerException {
	if (termParser!=null) termParser.parseAttribute(r);
	String res=send("(cd-attribute-p "+r+")",true);
	return parseBoolean(res);
}
/** This method asserts a set of ABox statements.
 * @param assertions jracer.Assertion[] The assertion array to be asserted.
 * @exception java.io.IOException Thrown when there is a communication problem with the RACER server.
 * @exception dl.racer.RacerException Thrown when the RACER server produces an ":error" message.
 */

public void forget(Assertion[] assertions) throws java.io.IOException, RacerException {
	String message="(forget";
	for(int i=0;i<assertions.length;i++) message=message+" "+assertions[i];
	send(message+")",false);
}
/** This method refers to the RACER forget-concept-assertion function. The assertion is removed from the
	current abox.
 * @param in java.lang.String The instance name.
 * @param c java.lang.String The concept term.
 * @exception java.io.IOException Thrown when there is a communication problem with the RACER server.
 * @exception dl.racer.RacerException Thrown when the RACER server produces an ":error" message.
 */

public void forgetConceptAssertion(String in, String c) throws java.io.IOException, RacerException {
	if (termParser!=null) termParser.parseConcept(c);
	send("(forget (instance "+in+" "+c+"))",false);
}
/** This method refers to the RACER forget-concept-assertion function.
 * @param abox java.lang.String The abox name corresponding to the abox from which the assertion is to
   be removed.
 * @param in java.lang.String The individual name.
 * @param c java.lang.String The concept term.
 * @exception java.io.IOException Thrown when there is a communication problem with the RACER server.
 * @exception dl.racer.RacerException Thrown when the RACER server produces an ":error" message.
 */

public void forgetConceptAssertion(String abox, String in, String c) throws java.io.IOException, RacerException {
	if (termParser!=null) termParser.parseConcept(c);
	send("(forget-concept-assertion "+abox+" "+in+" "+c+")",false);
}
/** This method refers to the RACER forget-role-assertion function , in which the current abox is used.
 * @param in1 java.lang.String Individual name of the predecessor.
 * @param in2 java.lang.String Individual name of the successor.
 * @param r java.lang.String A role term or a feature term.
 * @exception java.io.IOException Thrown when there is a communication problem with the RACER server.
 * @exception dl.racer.RacerException Thrown when the RACER server produces an ":error" message.
 */

public void forgetRoleAssertion(String in1, String in2, String r) throws java.io.IOException, RacerException {
	if (termParser!=null) termParser.parseConcept(r);
	send("(forget (related "+in1+" "+in2+" "+r+"))",false);
}
/** This method refers to the RACER add-role-assertion function.
 * @param abox java.lang.String The name of the abox from which the role assertion is to be removed.
 * @param in1 java.lang.String The individual name of the predecessor.
 * @param in2 java.lang.String The individual name of the successor.
 * @param r java.lang.String The role term.
 * @exception java.io.IOException Thrown when there is a communication problem with the RACER server.
 * @exception dl.racer.RacerException Thrown when the RACER server produces an ":error" message.
 */

public void forgetRoleAssertion(String abox, String in1, String in2, String r) throws java.io.IOException, RacerException {
	if (termParser!=null) termParser.parseRole(r);
	send("(forget-role-assertion "+abox+" "+in1+" "+in2+" "+r+")",false);
}
/** This method returns the answer and warning messages from the RACER result.
 * @return java.lang.String
 * @param result java.lang.String
 */

protected String getError(String result) {
	String s=new String();
	int iniMessage=result.indexOf(' ',7);
	int ini=result.indexOf('"',iniMessage);
	int fi=result.indexOf('"',ini+1);
	s=iniMessage+1<ini-1 ? result.substring(iniMessage+1,ini-1) : "";
	if (iniMessage+1<ini-1 && ini+1<fi) s=s+". ";
	s=s+result.substring(ini+1,fi);
	return s;
}
/** This method returns the answer and warning messages from the RACER result.
 * @return java.lang.String
 * @param result java.lang.String
 */

protected String[] getResultAndWarningFromAnswer(String result) {
	String[] s=new String[2];
	int ini=result.indexOf('"',10);
	int fi=ini;
	boolean esFinal=false;
	while (!esFinal) {
		fi=result.indexOf('"',fi+1);
		esFinal=result.charAt(fi-1)!='\\';
	}
	s[0]=result.substring(ini+1,fi);
	if (fi+4<result.length()) s[1]=result.substring(fi+3,result.length()-1);
	return s;
}
/** This method returns the answer and warning messages from the RACER result.
 * @return java.lang.String
 * @param result java.lang.String
 */

protected String getWarningFromOK(String result) {
	String warning=null;
	int ini=result.indexOf('"',6);
	int fi=result.length()-1;
	if (ini<fi-1) warning=result.substring(ini+1,fi);
	return warning;
}
/** This method refers to the RACER implies macro.
 * @param c1 java.lang.String The subsumee
 * @param c2 java.lang.String The subsumer
 * @exception java.io.IOException Thrown when there is a communication problem with the RACER server.
 * @exception dl.racer.RacerException Thrown when the RACER server produces an ":error" message.
 */

public void implies(String c1, String c2) throws java.io.IOException, RacerException {
	if (termParser!=null) termParser.parseConcept(c1);
	if (termParser!=null) termParser.parseConcept(c2);
	send("(implies "+c1+" "+c2+")",false);
}
/** This method refers to the in-abox RACER macro.
 * @param abn java.lang.String The name of the ABox
 * @exception java.io.IOException Thrown when there is a communication problem with the RACER server.
 * @exception dl.racer.RacerException Thrown when the RACER server produces an ":error" message.
 */

public void inAbox(String abn) throws IOException, RacerException {
	send("(in-abox "+abn+")",false);
}
/** This method refers to the in-abox RACER macro.
 * @param abn java.lang.String The name of the ABox
 * @exception java.io.IOException Thrown when there is a communication problem with the RACER server.
 * @exception dl.racer.RacerException Thrown when the RACER server produces an ":error" message.
 */

public void inAbox(String abn,String tbn) throws IOException, RacerException {
	send("(in-abox "+abn+" "+tbn+")",false);
}
/** This method refers to the RACER clone-abox macro.
 * @return java.lang.String The name of the clone
 * @param abn java.lang.String The name of the ABox
 * @exception java.io.IOException Thrown when there is a communication problem with the RACER server.
 * @exception dl.racer.RacerException Thrown when the RACER server produces an ":error" message.
 */

public String cloneABox(String abn) throws RacerException, IOException {
	String res=send("(clone-abox "+abn+")",true);
	return res;
}
/** This method refers to the RACER clone-abox macro.
 * @return java.lang.String The name of the clone
 * @param abn java.lang.String The name of the ABox
 * @exception java.io.IOException Thrown when there is a communication problem with the RACER server.
 * @exception dl.racer.RacerException Thrown when the RACER server produces an ":error" message.
 */

public String cloneABox(String abn,String newabn) throws RacerException, IOException {
	String res=send("(clone-abox "+abn+" :new-name "+newabn+")",true);
	return res;
}
/** This method refers to the RACER clone-abox macro.
 * @return java.lang.String The name of the clone
 * @param abn java.lang.String The name of the ABox
 * @exception java.io.IOException Thrown when there is a communication problem with the RACER server.
 * @exception dl.racer.RacerException Thrown when the RACER server produces an ":error" message.
 */

public String cloneABox(String abn,boolean overwrite) throws RacerException, IOException {
	String res=send("(clone-abox "+abn+" :overwrite "+(overwrite ? "t" : "nil")+")",true);
	return res;
}
/** This method refers to the RACER clone-abox macro.
 * @return java.lang.String The name of the clone
 * @param abn java.lang.String The name of the ABox
 * @exception java.io.IOException Thrown when there is a communication problem with the RACER server.
 * @exception dl.racer.RacerException Thrown when the RACER server produces an ":error" message.
 */

public String cloneABox(String abn,String newabn,boolean overwrite) throws RacerException, IOException {
	String res=send("(clone-abox "+abn+" :new-name "+newabn+
	     		" :overwrite "+(overwrite ? "t" : "nil")+")",true);
	return res;
}
/** This method refers to the RACER delete-abox macro.
 * @return java.lang.String The name of the deleted ABox
 * @param abn java.lang.String The name of the ABox
 * @exception java.io.IOException Thrown when there is a communication problem with the RACER server.
 * @exception dl.racer.RacerException Thrown when the RACER server produces an ":error" message.
 */

public String deleteABox(String abn) throws RacerException, IOException {
	String res=send("(delete-abox "+abn+")",true);
	return res;
}
/** This method refers to the RACER delete-all-aboxes macro.
 * @return java.lang.String
 * @exception java.io.IOException Thrown when there is a communication problem with the RACER server.
 * @exception dl.racer.RacerException Thrown when the RACER server produces an ":error" message.
 */

public void deleteAllABoxes() throws RacerException, IOException {
	send("(delete-all-aboxes)",false);
}
/** This method refers to the RACER individual-direct-types macro.
 * @return java.lang.String[] A list of concept names (one concept name for every concept equivalence
   set returned by RACER).
 * @param in java.lang.String The individual names.
 * @exception java.io.IOException Thrown when there is a communication problem with the RACER server.
 * @exception dl.racer.RacerException Thrown when the RACER server produces an ":error" message.
 */

public String[] individualDirectTypes(String in) throws java.io.IOException, RacerException {
	String res=send("(individual-direct-types "+in+")",true);
	return parseConceptSetList(res);
}
/** This method refers to the RACER individual-direct-types macro.
 * @return java.lang.String[] A list of concept names (one concept name for every concept equivalence
   set returned by RACER).
 * @param in java.lang.String The individual names.
 * @param abox java.lang.String The abox name.
 * @exception java.io.IOException Thrown when there is a communication problem with the RACER server.
 * @exception dl.racer.RacerException Thrown when the RACER server produces an ":error" message.
 */

public String[] individualDirectTypes(String in,String abox) throws java.io.IOException, RacerException {
	String res=send("(individual-direct-types "+in+" "+abox+")",true);
	return parseConceptSetList(res);
}
/** This method refers to the RACER individual-equal? macro for the current abox.
 * @return boolean True if in1 is equal to in2, and false otherwise.
 * @param in1 java.lang.String The name of one individual.
 * @param in2 java.lang.String The name of the other individual.
 * @exception java.io.IOException Thrown when there is a communication problem with the RACER server.
 * @exception dl.racer.RacerException Thrown when the RACER server produces an ":error" message.
 */

public boolean individualEqualP(String in1, String in2) throws java.io.IOException, RacerException {
	String res=send("(individual-equal? "+in1+" "+in2+")",true);
	return parseBoolean(res);
}
/** This method refers to the RACER individual-equal? macro.
 * @return boolean True if in1 is equal to in2, and false otherwise.
 * @param in1 java.lang.String The name of one individual.
 * @param in2 java.lang.String The name of the other individual.
 * @param abox java.lang.String The abox name.
 * @exception java.io.IOException Thrown when there is a communication problem with the RACER server.
 * @exception dl.racer.RacerException Thrown when the RACER server produces an ":error" message.
 */

public boolean individualEqualP(String in1, String in2, String abox) throws java.io.IOException, RacerException {
	String res=send("(individual-equal? "+in1+" "+in2+" "+abox+")",true);
	return parseBoolean(res);
}
/** This method refers to the RACER individual-fillers macro. The current abox is used.
 * @return java.lang.String[] An array containing individual names.
 * @param in java.lang.String The name of the predecessor individual.
 * @param r java.lang.String The role term.
 * @exception java.io.IOException Thrown when there is a communication problem with the RACER server.
 * @exception dl.racer.RacerException Thrown when the RACER server produces an ":error" message.
 */

public String[] individualFillers(String in, String r) throws java.io.IOException, RacerException {
	if (termParser!=null) termParser.parseRole(r);
	String res=send("(individual-fillers "+in+" "+r+")",true);
	return parseList(res);
}

public String[] individualAttributeFillers(String in, String r) throws java.io.IOException, RacerException {
	if (termParser!=null) termParser.parseRole(r);
	String res=send("(individual-attribute-fillers "+in+" "+r+")",true);
	System.out.println(res);
	return parseList(res);
}

public String[] individualDatatypeFillers(String in, String r) throws java.io.IOException, RacerException {
	if (termParser!=null) termParser.parseRole(r);
	String res=send("(individual-told-datatype-fillers "+in+" "+r+")",true);
	System.out.println(res);
	return parseList(res);
}

/** This method refers to the RACER individual-fillers macro.
 * @return java.lang.String[] An array containing individual names.
 * @param in java.lang.String The name of the predecessor individual.
 * @param r java.lang.String The role term.
 * @param abox java.lang.String The name of the abox.
 * @exception java.io.IOException Thrown when there is a communication problem with the RACER server.
 * @exception dl.racer.RacerException Thrown when the RACER server produces an ":error" message.
 */

public String[] individualFillers(String in, String r, String abox) throws java.io.IOException, RacerException {
	if (termParser!=null) termParser.parseRole(r);
	String res=send("(individual-fillers "+in+" "+r+" "+abox+")",true);
	return parseList(res);
}
/** This method refers to the RACER individual-instance? macro in which the current abox is used.
 * @return boolean true if in is an instance of c in the abox, and false otherwise.
 * @param in java.lang.String The individual name.
 * @param c java.lang.String The concept term.
 * @exception java.io.IOException Thrown when there is a communication problem with the RACER server.
 * @exception dl.racer.RacerException Thrown when the RACER server produces an ":error" message.
 */

public boolean individualInstanceP(String in, String c) throws java.io.IOException, RacerException {
	if (termParser!=null) termParser.parseConcept(c);
	String res=send("(individual-instance? "+in+" "+c+")",true);
	return parseBoolean(res);
}
/** This method refers to the RACER individual-instance? macro.
 * @return boolean true if in is an instance of c in the abox, and false otherwise.
 * @param in java.lang.String The individual name.
 * @param c java.lang.String The concept term.
 * @param abox java.lang.String THe abox name.
 * @exception java.io.IOException Thrown when there is a communication problem with the RACER server.
 * @exception dl.racer.RacerException Thrown when the RACER server produces an ":error" message.
 */

public boolean individualInstanceP(String in, String c, String abox) throws java.io.IOException, RacerException {
	if (termParser!=null) termParser.parseConcept(c);
	String res=send("(individual-instance? "+in+" "+c+" "+abox+")",true);
	return parseBoolean(res);
}
/** This method refers to the RACER individual-not-equal? macro for the current abox.
 * @return boolean True if in1 is not equal to in2, and false otherwise.
 * @param in1 java.lang.String The name of one individual.
 * @param in2 java.lang.String The name of the other individual.
 * @exception java.io.IOException Thrown when there is a communication problem with the RACER server.
 * @exception dl.racer.RacerException Thrown when the RACER server produces an ":error" message.
 */

public boolean individualNotEqualP(String in1, String in2) throws java.io.IOException, RacerException {
	String res=send("(individual-not-equal? "+in1+" "+in2+")",true);
	return parseBoolean(res);
}
/** This method refers to the RACER individual-not-equal? macro.
 * @return boolean True if in1 is not equal to in2, and false otherwise.
 * @param in1 java.lang.String The name of one individual.
 * @param in2 java.lang.String The name of the other individual.
 * @param abox java.lang.String The abox name.
 * @exception java.io.IOException Thrown when there is a communication problem with the RACER server.
 * @exception dl.racer.RacerException Thrown when the RACER server produces an ":error" message.
 */

public boolean individualNotEqualP(String in1, String in2, String abox) throws java.io.IOException, RacerException {
	String res=send("(individual-not-equal? "+in1+" "+in2+" "+abox+")",true);
	return parseBoolean(res);
}
/** This method refers to the individual-p RACER function. The current abox is used.
 * @return boolean true if the in corresponds to an individual name for the abox, and false otherwise.
 * @param in java.lang.String The name of the individual.
 * @exception java.io.IOException Thrown when there is a communication problem with the RACER server.
 * @exception dl.racer.RacerException Thrown when the RACER server produces an ":error" message.
 */

public boolean individualP(String in) throws java.io.IOException, RacerException {
	String res=send("(individual-p "+in,true);
	return parseBoolean(res);
}
/** This method refers to the RACER individual-p macro.
 * @return boolean true if in corresponds to an individual name in the abox, and false otherwise.
 * @param in java.lang.String The name of the individual.
 * @param abox java.lang.String The name of the abox.
 * @exception java.io.IOException Thrown when there is a communication problem with the RACER server.
 * @exception dl.racer.RacerException Thrown when the RACER server produces an ":error" message.
 */

public boolean individualP(String in, String abox) throws RacerException, IOException {
	String res=send("(individual-p "+in+" "+abox+")",true);
	return parseBoolean(res);
}
/** This method refers to the cd-object-p RACER function. The current abox is used.
 * @return boolean true if on corresponds to a concrete domain object name in the abox, and false otherwise.
 * @param in java.lang.String The name of the concrete domain object.
 * @exception java.io.IOException Thrown when there is a communication problem with the RACER server.
 * @exception dl.racer.RacerException Thrown when the RACER server produces an ":error" message.
 */

public boolean CDobjectP(String on) throws java.io.IOException, RacerException {
	String res=send("(cd-object-p "+on,true);
	return parseBoolean(res);
}
/** This method refers to the RACER cd-object-p function.
 * @return boolean true if on corresponds to a concrete domain object name in the abox, and false otherwise.
 * @param in java.lang.String The name of the concrete domain object.
 * @param abox java.lang.String The name of the abox.
 * @exception java.io.IOException Thrown when there is a communication problem with the RACER server.
 * @exception dl.racer.RacerException Thrown when the RACER server produces an ":error" message.
 */

public boolean CDobjectP(String on, String abox) throws RacerException, IOException {
	String res=send("(cd-object-p "+on+" "+abox+")",true);
	return parseBoolean(res);
}
/** This method refers to the RACER individuals-related? macro in which the current abox is used.
 * @return boolean True if in1 is related to in2 via r in the abox, and false otherwise.
 * @param in1 java.lang.String The name of the predecessor.
 * @param in2 java.lang.String The name of the successor.
 * @param r java.lang.String The role term.
 * @exception java.io.IOException Thrown when there is a communication problem with the RACER server.
 * @exception dl.racer.RacerException Thrown when the RACER server produces an ":error" message.
 */

public boolean individualsRelatedP(String in1, String in2, String r) throws java.io.IOException, RacerException {
	if (termParser!=null) termParser.parseRole(r);
	String res=send("(individuals-related? "+in1+" "+in2+" "+r+")",true);
	return parseBoolean(res);
}
/** This method refers to the RACER individuals-related? macro.
 * @return boolean True if in1 is related to in2 via r in the abox, and false otherwise.
 * @param in1 java.lang.String The name of the predecessor.
 * @param in2 java.lang.String The name of the successor.
 * @param r java.lang.String The role term.
 * @param abox java.lang.String The abox name.
 * @exception java.io.IOException Thrown when there is a communication problem with the RACER server.
 * @exception dl.racer.RacerException Thrown when the RACER server produces an ":error" message.
 */

public boolean individualsRelatedP(String in1, String in2, String r, String abox) throws java.io.IOException, RacerException {
	if (termParser!=null) termParser.parseRole(r);
	String res=send("(individuals-related-p "+in1+" "+in2+" "+r+" "+abox+")",true);
	return parseBoolean(res);
}
/** This method refers to the RACER individual-types macro.
 * @return java.lang.String[] A list of concept names (one concept name for every concept equivalence
   set returned by RACER).
 * @param in java.lang.String The individual names.
 * @exception java.io.IOException Thrown when there is a communication problem with the RACER server.
 * @exception dl.racer.RacerException Thrown when the RACER server produces an ":error" message.
 */

public String[] individualTypes(String in) throws java.io.IOException, RacerException {
	String res=send("(individual-types "+in+")",true);
	return parseConceptSetList(res);
}
/** This method refers to the RACER individual-types macro.
 * @return java.lang.String[] A list of concept names (one concept name for every concept equivalence
   set returned by RACER).
 * @param in java.lang.String The individual names.
 * @param abox java.lang.String The abox name.
 * @exception java.io.IOException Thrown when there is a communication problem with the RACER server.
 * @exception dl.racer.RacerException Thrown when the RACER server produces an ":error" message.
 */

public String[] individualTypes(String in,String abox) throws java.io.IOException, RacerException {
	String res=send("(individual-types "+in+" "+abox+")",true);
	return parseConceptSetList(res);
}
/** This method refers to the RACER add-concept-assertion function. The assertion is added to the
	current abox.
 * @param in java.lang.String The instance name.
 * @param c java.lang.String The concept term.
 * @exception java.io.IOException Thrown when there is a communication problem with the RACER server.
 * @exception dl.racer.RacerException Thrown when the RACER server produces an ":error" message.
 */

public void instance(String in, String c) throws java.io.IOException, RacerException {
	if (termParser!=null) termParser.parseConcept(c);
	send("(instance "+in+" "+c+")",false);
}
/** This method refers to the RACER in-tbox macro.
 * @return java.lang.String
 * @param tbn java.lang.String The name of the TBox
 * @exception java.io.IOException Thrown when there is a communication problem with the RACER server.
 * @exception dl.racer.RacerException Thrown when the RACER server produces an ":error" message.
 */

public void inTBox(String tbn) throws RacerException, IOException {
	send("(in-tbox "+tbn+")",false);
}
/** This method refers to the RACER in-tbox macro.
 * @return java.lang.String
 * @param tbn java.lang.String The name of the TBox
 * @exception java.io.IOException Thrown when there is a communication problem with the RACER server.
 * @exception dl.racer.RacerException Thrown when the RACER server produces an ":error" message.
 */

public void inTBox(String tbn,boolean init) throws RacerException, IOException {
	send("(in-tbox "+tbn+" :init "+(init ? "t" : "nil")+")",false);
}
/** This method refers to the RACER clone-tbox macro.
 * @return java.lang.String The name of the clone
 * @param tbn java.lang.String The name of the TBox
 * @exception java.io.IOException Thrown when there is a communication problem with the RACER server.
 * @exception dl.racer.RacerException Thrown when the RACER server produces an ":error" message.
 */

public String cloneTBox(String tbn) throws RacerException, IOException {
	String res=send("(clone-tbox "+tbn+")",true);
	return res;
}

public String currentTBox() throws RacerException, IOException {
	String res=send("(current-tbox)",true);
	return res;
}

/** This method refers to the RACER clone-tbox macro.
 * @return java.lang.String The name of the clone
 * @param tbn java.lang.String The name of the TBox
 * @exception java.io.IOException Thrown when there is a communication problem with the RACER server.
 * @exception dl.racer.RacerException Thrown when the RACER server produces an ":error" message.
 */

public String cloneTBox(String tbn,String newtbn) throws RacerException, IOException {
	String res=send("(clone-tbox "+tbn+" :new-name "+newtbn+")",true);
	return res;
}
/** This method refers to the RACER clone-tbox macro.
 * @return java.lang.String The name of the clone
 * @param tbn java.lang.String The name of the TBox
 * @exception java.io.IOException Thrown when there is a communication problem with the RACER server.
 * @exception dl.racer.RacerException Thrown when the RACER server produces an ":error" message.
 */

public String cloneTBox(String tbn,boolean overwrite) throws RacerException, IOException {
	String res=send("(clone-tbox "+tbn+" :overwrite "+(overwrite ? "t" : "nil")+")",true);
	return res;
}
/** This method refers to the RACER clone-tbox macro.
 * @return java.lang.String The name of the clone
 * @param tbn java.lang.String The name of the TBox
 * @exception java.io.IOException Thrown when there is a communication problem with the RACER server.
 * @exception dl.racer.RacerException Thrown when the RACER server produces an ":error" message.
 */

public String cloneTBox(String tbn,String newtbn,boolean overwrite) throws RacerException, IOException {
	String res=send("(clone-tbox "+tbn+" :new-name "+newtbn+
	     		" :overwrite "+(overwrite ? "t" : "nil")+")",true);
	return res;
}
/** This method refers to the RACER delete-tbox macro.
 * @return java.lang.String
 * @param tbn java.lang.String The name of the TBox
 * @exception java.io.IOException Thrown when there is a communication problem with the RACER server.
 * @exception dl.racer.RacerException Thrown when the RACER server produces an ":error" message.
 */

public void deleteTBox(String tbn) throws RacerException, IOException {
	send("(delete-tbox "+tbn+")",false);
}
/** This method refers to the RACER delete-all-tboxes macro.
 * @return java.lang.String
 * @exception java.io.IOException Thrown when there is a communication problem with the RACER server.
 * @exception dl.racer.RacerException Thrown when the RACER server produces an ":error" message.
 */

public void deleteAllTBoxes() throws RacerException, IOException {
	send("(delete-all-tboxes)",false);
}
/** This method parses a boolean from a RACER answer that is supposed to be a boolean. The behaviour is
	unpredictable if the string does not represent a boolean. */

protected static boolean parseBoolean(String res) {
	return res.equalsIgnoreCase("nil") ? false : true;
}
/** This method parses a concept term. It just converts the RACER *TOP* and *BOTTOM* strings to TOP and
	BOTTOM.
 * @return java.lang.String The concept.
 * @param concept java.lang.String The concept term to be parsed.
 */

protected static String parseConcept(String concept) {
	if (concept.equalsIgnoreCase("*TOP*")) concept="TOP";
	else if (concept.equalsIgnoreCase("*BOTTOM*")) concept="BOTTOM";
	return concept;
}
/** This method parses a ConceptAssertion object from a string.
 * @return java.lang.String a ConceptAssertion object.
 * @param eqclass java.lang.String A string of the form "(I1 C1)"
 */

protected static ConceptAssertion parseConceptAssertion(String str) {
	int pos=str.indexOf(' ');
	String individual=str.substring(1,pos);
	String concept=str.substring(pos+1,str.length()-1);
	concept=parseConcept(concept);
	return new ConceptAssertion(individual,concept);
}
/** This method parses a list of concept assertions and returns an array of ConceptAssertion objects.
 * @return ConceptAssertion[] An array of concept assertions.
 * @param res java.lang.String A string of the form "((I1 C1) (I2 C2) ...)". The string
   can also be "nil".
 */

protected static ConceptAssertion[] parseConceptAssertionList(String res) {
	if (res.equalsIgnoreCase("nil")) return new ConceptAssertion[0];
	Vector v=parseLispList(res);
	ConceptAssertion[] assertions=new ConceptAssertion[v.size()];
	Enumeration enum_=v.elements();
	int i=0;
	while (enum_.hasMoreElements()) {
		String assertionStr=(String)enum_.nextElement();
		assertions[i]=parseConceptAssertion(assertionStr);
		i++;
	}
	return assertions;
}
/** This method returns a string corresponding to the first concept in an concept equivalence set. As
	the first concept will always be the same (unless, maybe, when the equivalence set changes), it can
	be taken as the equivalence class representative.
 * @return java.lang.String The first concept name from a set of concept names.
 * @param eqclass java.lang.String A lisp list containing a set of concept names.
 */

protected static String parseConceptEquivalenceSet(String eqclass) {
	int pos=eqclass.indexOf(' ');
	if (pos==-1) pos=eqclass.length()-1;
        else {
          boolean inBars = false;
          pos = -1;
          for (int i=0; i < eqclass.length() && pos == -1;i++) {
            if (eqclass.charAt(i) == '|') inBars = ! inBars;
            if ((!inBars) && eqclass.charAt(i) == ' ') pos=i;
          }
        }
	if (pos==-1) pos=eqclass.length()-1;
	String concept=eqclass.substring(1,pos);
	return parseConcept(concept);
}
/** This method parses a list of sets of equivalent concepts, and returns an array with
	a string for every set of equivalent concepts. The string for a set of equivalent concepts
	refers to the first concept in that set.
 * @return java.lang.String[] An array of concept terms.
 * @param res java.lang.String A string of the form "((A B C) (D) (E F) (G H I J) ...)". The string
   can also be "nil".
 */

protected static String[] parseConceptSetList(String res) {
	if (res.equalsIgnoreCase("nil")) return new String[0];
	Vector v=parseLispList(res);
	String[] concepts=new String[v.size()];
	Enumeration enum_=v.elements();
	int i=0;
	while (enum_.hasMoreElements()) {
		String eqclass=(String)enum_.nextElement();
		concepts[i]=parseConceptEquivalenceSet(eqclass);
		i++;
	}
	return concepts;
}
/** This method parses a string representing a lisp list into a vector. Every element in the
	vector is an element of the list. */

protected static Vector parseLispList(String s) {
	int nargs;
	int np=1;
	Vector v=new Vector();
	int posIni,posFi;
	boolean comment;
	boolean strictSymbol=false;
	boolean string=false;
	// Es salta els blancs inicials
	for(posIni=1;s.charAt(posIni)==' ' ||
					   s.charAt(posIni)==13 ||
					   s.charAt(posIni)=='\t' ||
					   s.charAt(posIni)==10;posIni++);
	for (;;) {
	  posFi=posIni;
	  for(;posFi<s.length() && (np>1 || (s.charAt(posFi)!=' '
										  && s.charAt(posFi)!=13
										  && s.charAt(posFi)!='\t'
										  && s.charAt(posFi)!=10)
									 || strictSymbol || string)
		  ;posFi++) {
		if (strictSymbol && s.charAt(posFi)=='|') strictSymbol=false;
		else if (string && s.charAt(posFi)=='"') string=false;
		else if (!string && s.charAt(posFi)=='|') strictSymbol=true;
		else if (!strictSymbol && s.charAt(posFi)=='"') string=true;
		else if (!strictSymbol && !string && s.charAt(posFi)=='(') np++;
		else if (!strictSymbol && !string && s.charAt(posFi)==')') np--;
		}
	  if (posFi>=s.length()) break;
	  v.addElement(s.substring(posIni,posFi));
	  // Es salta els blancs
	  for(posIni=posFi+1;s.charAt(posIni)==' ' ||
						 s.charAt(posIni)==13 ||
						 s.charAt(posIni)=='\t' ||
						 s.charAt(posIni)==10;posIni++);
	  }
	if (posIni<s.length()-1) {
	  posFi=s.length()-1;
	  v.addElement(s.substring(posIni,posFi));
	  }
	return v;
	}
/** This method parses a list of lisp expressions, and returns an array with
	a string for every one of those expressions.
 * @return java.lang.String[] An array of strings containing each one a lisp expression.
 * @param res java.lang.String A string of the form "(expr1 expr2 expr3 ...)". The string
   can also be "nil".
 */

protected static String[] parseList(String res) {
        if (res == null) return new String[0];
	if (res.equalsIgnoreCase("nil")) return new String[0];
	Vector v=parseLispList(res);
	String[] exprs=new String[v.size()];
	Enumeration enum_=v.elements();
	int i=0;
	while (enum_.hasMoreElements()) {
		exprs[i]=(String)enum_.nextElement();
		i++;
	}
	return exprs;
}
/** This method parses the result obtained from RACER. If RACER produced an ":ok" message,
	null is returned; if it produced an ":answer" message, the result is then parsed; and
	if there is an error message, an exception is thrown.
 * @exception dl.racer.RacerException Thrown when the RACER server produces an ":error" message.
	*/

protected String parseResult(String message,String result) throws RacerException {
	// return null if the result is ok
        if (result.length() < 2)
          throw new RacerException("RACER MESSAGE: " + message +"; produced a RACER answer unknown: '"+result+"'");
	if (result.charAt(1)=='o') {
//		String warning=getWarningFromOK(result);
//		if (warning!=null) printWarning(warning);
		return null;
	}
	// we check whether it has been an error and throw an exception
	if (result.charAt(1)=='e') {
		String error=getError(result);
		throw new RacerException(error);
	}
	// otherwise, we check whether the result is an answer and retrieve the answer
	if (result.charAt(1)=='a') {
		String[] ansAndWar=getResultAndWarningFromAnswer(result);
		if (ansAndWar[1]!=null) printWarning(ansAndWar[1]);
		return ansAndWar[0];
	}
	// unknown answer
	throw new RacerException("RACER MESSAGE: "+message+"; produced a RACER answer unknown: "+result);
}
/** This method parses a RoleAssertion object from a string.
 * @return java.lang.String a RoleAssertion object.
 * @param eqclass java.lang.String A string of the form "(I1 I2 R)"
 */

protected static RoleAssertion parseRoleAssertion(String str) {
	int p1=str.indexOf(' ');
	int p2=str.indexOf(' ',p1+1);
	String pred=str.substring(2,p1);
	String succ=str.substring(p1+1,p2-1);
	String role=str.substring(p2+1,str.length()-1);
	return new RoleAssertion(pred,role,succ);
}
/** This method parses a list of role assertions and returns an array of ConceptAssertion objects.
 * @return ConceptAssertion[] An array of role assertions.
 * @param res java.lang.String A string of the form "((I1 C1) (I2 C2) ...)". The string
   can also be "nil".
 */

protected static RoleAssertion[] parseRoleAssertionList(String res) {
	if (res.equalsIgnoreCase("nil")) return new RoleAssertion[0];
	Vector v=parseLispList(res);
	RoleAssertion[] assertions=new RoleAssertion[v.size()];
	Enumeration enum_=v.elements();
	int i=0;
	while (enum_.hasMoreElements()) {
		String assertionStr=(String)enum_.nextElement();
		assertions[i]=parseRoleAssertion(assertionStr);
		i++;
	}
	return assertions;
}
/** This method refers to the RACER realize-abox function for the current tbox.
 * @exception java.io.IOException Thrown when there is a communication problem with the RACER server.
 * @exception dl.racer.RacerException Thrown when the RACER server produces an ":error" message.
 */

public void realizeABox() throws java.io.IOException, RacerException {
	send("(realize-abox)",false);
}
/** This method refers to the RACER realize-abox function.
 * @param abox java.lang.String The abox name.
 * @exception java.io.IOException Thrown when there is a communication problem with the RACER server.
 * @exception dl.racer.RacerException Thrown when the RACER server produces an ":error" message.
 */

public void realizeABox(String abox) throws java.io.IOException, RacerException {
	send("(realize-abox "+abox+")",false);
}
/** This method refers to the RACER add-role-assertion function , in which the current abox is used.
 * @param in1 java.lang.String Individual name of the predecessor.
 * @param in2 java.lang.String Individual name of the successor.
 * @param r java.lang.String A role term or a feature term.
 * @exception java.io.IOException Thrown when there is a communication problem with the RACER server.
 * @exception dl.racer.RacerException Thrown when the RACER server produces an ":error" message.
 */

public void related(String in1, String in2, String r) throws java.io.IOException, RacerException {
	if (termParser!=null) termParser.parseRole(r);
	send("(related "+in1+" "+in2+" "+r+")",false);
}


/** This method refers to the RACER retrieve-direct-predecessors function. The current abox is used.
 * @return java.lang.String[] An array containing individual names.
 * @param r java.lang.String The role term.
 * @param in java.lang.String The name of the successor individual.
 * @exception java.io.IOException Thrown when there is a communication problem with the RACER server.
 * @exception dl.racer.RacerException Thrown when the RACER server produces an ":error" message.
 */

public String[] retrieveDirectPredecessors(String r, String in) throws java.io.IOException, RacerException {
	if (termParser!=null) termParser.parseRole(r);
	String res=send("(retrieve-direct-predecessors "+r+" "+in+")",true);
	return parseList(res);
}
/** This method refers to the RACER retrieve-direct-predecessors function.
 * @return java.lang.String[] An array containing individual names.
 * @param r java.lang.String The role term.
 * @param in java.lang.String The name of the successor individual.
 * @param abox java.lang.String The name of the abox.
 * @exception java.io.IOException Thrown when there is a communication problem with the RACER server.
 * @exception dl.racer.RacerException Thrown when the RACER server produces an ":error" message.
 */

public String[] retrieveDirectPredecessors(String r, String in, String abox) throws java.io.IOException, RacerException {
	if (termParser!=null) termParser.parseRole(r);
	String res=send("(retrieve-direct-predecessors "+in+" "+r+" "+abox+")",true);
	return parseList(res);
}
/** This method refers to the RACER retrieve-individual-filled-roles function. The current abox is used.
 * @return java.lang.String[] An array containing role terms.
 * @param in1 java.lang.String The name of the predecessor individual.
 * @param in2 java.lang.String The name of the successor individual.
 * @exception java.io.IOException Thrown when there is a communication problem with the RACER server.
 * @exception dl.racer.RacerException Thrown when the RACER server produces an ":error" message.
 */

public String[] retrieveIndividualFilledRoles(String in1, String in2) throws java.io.IOException, RacerException {
	String res=send("(retrieve-individual-filled-roles "+in1+" "+in2+")",true);
	return parseList(res);
}
/** This method refers to the RACER retrieve-individual-filled-roles function.
 * @return java.lang.String[] An array containing role terms.
 * @param in1 java.lang.String The name of the predecessor individual.
 * @param in2 java.lang.String The name of the successor individual.
 * @param abox java.lang.String The name of the abox.
 * @exception java.io.IOException Thrown when there is a communication problem with the RACER server.
 * @exception dl.racer.RacerException Thrown when the RACER server produces an ":error" message.
 */

public String[] retrieveIndividualFilledRoles(String in1, String in2, String abox) throws java.io.IOException, RacerException {
	String res=send("(retrieve-individual-filled-roles "+in1+" "+in2+" "+abox+")",true);
	return parseList(res);
}
/** This method refers to the RACER role-ancestors macro using the current TBox.
 * @return String[] an array of strings containing all the atomic role ancestors of a role
	in the tbox.
 * @param r java.lang.String the role term
 * @exception java.io.IOException Thrown when there is a communication problem with the RACER server.
 * @exception dl.racer.RacerException Thrown when the RACER server produces an ":error" message.
 */

public String[] roleAncestors(String r) throws RacerException, IOException {
	if (termParser!=null) termParser.parseRole(r);
	String res=send("(role-ancestors "+r+")",true);
	return parseList(res);
}
/** This method refers to the RACER role-ancestors macro using the current TBox.
 * @return String[] an array of strings containing all the atomic role ancestors of a role
	in the tbox.
 * @param r java.lang.String the role term
 * @param tbox java.lang.String The tbox
 * @exception java.io.IOException Thrown when there is a communication problem with the RACER server.
 * @exception dl.racer.RacerException Thrown when the RACER server produces an ":error" message.
 */

public String[] roleAncestors(String r, String tbox) throws RacerException, IOException {
	if (termParser!=null) termParser.parseRole(r);
	String res=send("(role-ancestors "+r+" "+tbox+")",true);
	return parseList(res);
}
/** This method refers to the RACER role-children macro using the current TBox.
 * @return String[] an array of strings containing all the atomic role children of a role
	in the tbox.
 * @param r java.lang.String the role term
 * @exception java.io.IOException Thrown when there is a communication problem with the RACER server.
 * @exception dl.racer.RacerException Thrown when the RACER server produces an ":error" message.
 */

public String[] roleChildren(String r) throws RacerException, IOException {
	if (termParser!=null) termParser.parseRole(r);
	String res=send("(role-children "+r+")",true);
	return parseList(res);
}
/** This method refers to the RACER role-children macro using the current TBox.
 * @return String[] an array of strings containing all the atomic role children of a role
	in the tbox.
 * @param r java.lang.String the role term
 * @param tbox java.lang.String The tbox
 * @exception java.io.IOException Thrown when there is a communication problem with the RACER server.
 * @exception dl.racer.RacerException Thrown when the RACER server produces an ":error" message.
 */

public String[] roleChildren(String r, String tbox) throws RacerException, IOException {
	if (termParser!=null) termParser.parseRole(r);
	String res=send("(role-children "+r+" "+tbox+")",true);
	return parseList(res);
}
/** This method refers to the RACER role-descendants macro using the current TBox.
 * @return String[] an array of strings containing all the atomic role descendants of a role
	in the tbox.
 * @param r java.lang.String the role term
 * @exception java.io.IOException Thrown when there is a communication problem with the RACER server.
 * @exception dl.racer.RacerException Thrown when the RACER server produces an ":error" message.
 */

public String[] roleDescendants(String r) throws RacerException, IOException {
	if (termParser!=null) termParser.parseRole(r);
	String res=send("(role-descendants "+r+")",true);
	return parseList(res);
}
/** This method refers to the RACER role-descendants macro using the current TBox.
 * @return String[] an array of strings containing all the atomic role descendants of a role
	in the tbox.
 * @param r java.lang.String the role term
 * @param tbox java.lang.String The tbox
 * @exception java.io.IOException Thrown when there is a communication problem with the RACER server.
 * @exception dl.racer.RacerException Thrown when the RACER server produces an ":error" message.
 */

public String[] roleDescendants(String r, String tbox) throws RacerException, IOException {
	if (termParser!=null) termParser.parseRole(r);
	String res=send("(role-descendants "+r+" "+tbox+")",true);
	return parseList(res);
}
/** This method refers to the role-inverse RACER macro.
 * @return a string corresponding to the role name or term for the inverse role of r.
 * @param r java.lang.String a role term.
 * @exception java.io.IOException Thrown when there is a communication problem with the RACER server.
 * @exception dl.racer.RacerException Thrown when the RACER server produces an ":error" message.
 */

public String roleInverse(String r) throws java.io.IOException, RacerException {
	if (termParser!=null) termParser.parseRole(r);
	return send("(role-inverse "+r+")",true);
}
/** This method refers to the role-inverse RACER function.
 * @return boolean true if r is a transitive role term and false otherwise.
 * @param r java.lang.String a role term.
 * @param tbox java.lang.String tbox name.
 * @exception java.io.IOException Thrown when there is a communication problem with the RACER server.
 * @exception dl.racer.RacerException Thrown when the RACER server produces an ":error" message.
 */

public String roleInverse(String r, String tbox) throws java.io.IOException, RacerException {
	if (termParser!=null) termParser.parseRole(r);
	return send("(role-inverse "+r+" "+tbox+")",true);
}
/** This method refers to the role-p RACER function.
 * @return boolean true if r is a known role term and false otherwise.
 * @param r java.lang.String a role term.
 * @exception java.io.IOException Thrown when there is a communication problem with the RACER server.
 * @exception dl.racer.RacerException Thrown when the RACER server produces an ":error" message.
 */

public boolean roleP(String r) throws java.io.IOException, RacerException {
	if (termParser!=null) termParser.parseRole(r);
	String res=send("(role-p "+r+")",true);
	return parseBoolean(res);
}
/** This method refers to the role-p RACER function.
 * @return boolean true if r is a known role term and false otherwise.
 * @param r java.lang.String a role term.
 * @param tbox java.lang.String tbox name.
 * @exception java.io.IOException Thrown when there is a communication problem with the RACER server.
 * @exception dl.racer.RacerException Thrown when the RACER server produces an ":error" message.
 */

public boolean roleP(String r, String tbox) throws java.io.IOException, RacerException {
	if (termParser!=null) termParser.parseRole(r);
	String res=send("(role-p "+r+" "+tbox+")",true);
	return parseBoolean(res);
}
/** This method refers to the RACER role-parents macro using the current TBox.
 * @return String[] an array of strings containing all the atomic role parents of a role
	in the tbox.
 * @param r java.lang.String the role term
 * @exception java.io.IOException Thrown when there is a communication problem with the RACER server.
 * @exception dl.racer.RacerException Thrown when the RACER server produces an ":error" message.
 */

public String[] roleParents(String r) throws RacerException, IOException {
	if (termParser!=null) termParser.parseRole(r);
	String res=send("(role-parents "+r+")",true);
	return parseList(res);
}
/** This method refers to the RACER role-parents macro using the current TBox.
 * @return String[] an array of strings containing all the atomic role parents of a role
	in the tbox.
 * @param r java.lang.String the role term
 * @param tbox java.lang.String The tbox
 * @exception java.io.IOException Thrown when there is a communication problem with the RACER server.
 * @exception dl.racer.RacerException Thrown when the RACER server produces an ":error" message.
 */

public String[] roleParents(String r, String tbox) throws RacerException, IOException {
	if (termParser!=null) termParser.parseRole(r);
	String res=send("(role-parents "+r+" "+tbox+")",true);
	return parseList(res);
}
/** This method refers to the role-subsumes? RACER macro. The tbox used is the current tbox.
 * @return boolean true if r1 subsumes r2 and false otherwise.
 * @param r1 java.lang.String role term of the role subsumer.
 * @param r2 java.lang.String role term of the role subsumee.
 * @exception java.io.IOException Thrown when there is a communication problem with the RACER server.
 * @exception dl.racer.RacerException Thrown when the RACER server produces an ":error" message.
 */

public boolean roleSubsumesP(String r1, String r2) throws java.io.IOException, RacerException {
	if (termParser!=null) termParser.parseRole(r1);
	if (termParser!=null) termParser.parseRole(r2);
	String res=send("(role-subsumes? "+r1+" "+r2+")",true);
	return parseBoolean(res);
}
/** This method refers to the role-subsumes? RACER macro.
 * @return boolean true if r1 subsumes r2 and false otherwise.
 * @param r1 java.lang.String role term of the role subsumer.
 * @param r2 java.lang.String role term of the role subsumee.
 * @param tbox java.lang.String tbox name.
 * @exception java.io.IOException Thrown when there is a communication problem with the RACER server.
 * @exception dl.racer.RacerException Thrown when the RACER server produces an ":error" message.
 */

public boolean roleSubsumesP(String r1, String r2, String tbox) throws java.io.IOException, RacerException {
	if (termParser!=null) termParser.parseRole(r1);
	if (termParser!=null) termParser.parseRole(r2);
	String res=send("(role-subsumes? "+r1+" "+r2+" "+tbox+")",true);
	return parseBoolean(res);
}
/** This method refers to the RACER signature macro.
 * @param concepts java.lang.String A string containing the names of the concepts separated by one or more
   spaces. The string might be null, meaning that there is no concept.
 * @param roles java.lang.String A string containing the names of the roles separated by one or more
   spaces. The string might be null, meaning that there is no concept.
 * @param individuals java.lang.String A string containing the names of the individuals separated by one or more
   spaces. The string might be null, meaning that there is no concept.
 * @exception java.io.IOException Thrown when there is a communication problem with the RACER server.
 * @exception dl.racer.RacerException Thrown when the RACER server produces an ":error" message.
 */

public void signature(String concepts, String roles, String individuals) throws RacerException, IOException {
	send("(signature "+
		(concepts==null ? "" : " :atomic-concepts "+concepts)+
		(roles==null ? "" : " :roles "+roles)+
		(individuals==null ? "" : " :individuals "+individuals)+")",false);
}
/** This method asserts a set of ABox statements.
 * @param assertions jracer.Assertion[] The assertion array to be asserted.
 * @exception java.io.IOException Thrown when there is a communication problem with the RACER server.
 * @exception dl.racer.RacerException Thrown when the RACER server produces an ":error" message.
 */

public void state(Assertion[] assertions) throws java.io.IOException, RacerException {
	String message="(state";
	for(int i=0;i<assertions.length;i++) message=message+" "+assertions[i];
	send(message+")",false);
}
/** This method refers to the RACER tbox-classified-p function for the current tbox.
 * @return boolean true if the tbox has already been classified, and false otherwise.
 * @exception java.io.IOException Thrown when there is a communication problem with the RACER server.
 * @exception dl.racer.RacerException Thrown when the RACER server produces an ":error" message.
 */

public boolean tboxClassifiedP() throws java.io.IOException, RacerException {
	String res=send("(tbox-classified-p)",true);
	return parseBoolean(res);
}
/** This method refers to the RACER tbox-classified-p function.
 * @return boolean true if the tbox has already been classified, and false otherwise.
 * @param tbox java.lang.String The tbox name.
 * @exception java.io.IOException Thrown when there is a communication problem with the RACER server.
 * @exception dl.racer.RacerException Thrown when the RACER server produces an ":error" message.
 */

public boolean tboxClassifiedP(String tbox) throws java.io.IOException, RacerException {
	String res=send("(tbox-classified-p "+tbox+")",true);
	return parseBoolean(res);
}
/** This method refers to the RACER tbox-coherent? MACRO for the current tbox.
 * @return boolean true if the tbox is coherent and false otherwise.
 * @exception java.io.IOException Thrown when there is a communication problem with the RACER server.
 * @exception dl.racer.RacerException Thrown when the RACER server produces an ":error" message.
 */

public boolean tboxCoherentP() throws java.io.IOException, RacerException {
	String res=send("(tbox-coherent?)",true);
	return parseBoolean(res);
}
/** This method refers to the RACER tbox-coherent? macro.
 * @return boolean true if the tbox is coherent and false otherwise.
 * @param tbox java.lang.String The tbox name.
 * @exception java.io.IOException Thrown when there is a communication problem with the RACER server.
 * @exception dl.racer.RacerException Thrown when the RACER server produces an ":error" message.
 */

public boolean tboxCoherentP(String tbox) throws java.io.IOException, RacerException {
	String res=send("(tbox-coherent-p "+tbox+")",true);
	return parseBoolean(res);
}
/** This method refers to the transitive-p RACER function.
 * @return boolean true if r is a transitive role term and false otherwise.
 * @param r java.lang.String a role term.
 * @exception java.io.IOException Thrown when there is a communication problem with the RACER server.
 * @exception dl.racer.RacerException Thrown when the RACER server produces an ":error" message.
 */

public boolean transitiveP(String r) throws java.io.IOException, RacerException {
	if (termParser!=null) termParser.parseRole(r);
	String res=send("(transitive-p "+r+")",true);
	return parseBoolean(res);
}
/** This method refers to the transitive-p RACER function.
 * @return boolean true if r is a transitive role term and false otherwise.
 * @param r java.lang.String a role term.
 * @param tbox java.lang.String tbox name.
 * @exception java.io.IOException Thrown when there is a communication problem with the RACER server.
 * @exception dl.racer.RacerException Thrown when the RACER server produces an ":error" message.
 */

public boolean transitiveP(String r, String tbox) throws java.io.IOException, RacerException {
	if (termParser!=null) termParser.parseRole(r);
	String res=send("(transitive-p "+r+" "+tbox+")",true);
	return parseBoolean(res);
}
/** This method reads a XML file as TBox w.r.t. the FaCT DTD.
 * @return java.lang.String Name of the TBox
 * @param FileName java.lang.String a file name.
 * @exception java.io.IOException Thrown when there is a communication problem with the RACER server.
 * @exception dl.racer.RacerException Thrown when the RACER server produces an ":error" message.
 */

public String XMLreadTBoxFile(String FileName) throws java.io.IOException, RacerException {
	String res=send("(xml-read-tbox-file "+FileName+")",false);
	return res;
}
/** This method reads a RDFS file as TBox.
 * @return java.lang.String Name of the TBox
 * @param FileName java.lang.String a file name.
 * @exception java.io.IOException Thrown when there is a communication problem with the RACER server.
 * @exception dl.racer.RacerException Thrown when the RACER server produces an ":error" message.
 */

public String RDFSreadTBoxFile(String FileName) throws java.io.IOException, RacerException {
	String res=send("(rdfs-read-tbox-file "+FileName+")",false);
	return res;
}
/** This method reads a Racer file.
 * @param FileName java.lang.String a file name.
 * @exception java.io.IOException Thrown when there is a communication problem with the RACER server.
 * @exception dl.racer.RacerException Thrown when the RACER server produces an ":error" message.
 */

public void RACERreadFile(String FileName) throws java.io.IOException, RacerException {
	send("(racer-read-file \""+FileName+"\")",false);
}

public void readOWL(String FileName) throws java.io.IOException, RacerException {
    synchronousSend("(owl-read-document \""+FileName+"\")");
}

public int nRQL( String qry, boolean print ) throws Exception {
	printMessageIntoSocket( qry );
	int count = 0;
	int c = 0;
	
	c = racerInputStream.read();
	c = racerInputStream.read();
	if( c != 'a' ) {
	    String result = readFromSocket(racerInputStream); 
	    result = ":" + ((char)c) + result;
	    System.err.println(parseResult(qry,result));    
	    return -1;
	}
	
	BufferedWriter out = new BufferedWriter( new OutputStreamWriter( System.out ) );

	if(print) out.write(c);	
	
	while( (c = racerInputStream.read()) != '"')
	    if(print) out.write(c);
	
	if(print) out.write(c);
	c = racerInputStream.read();
	if(print) out.write(c);
	
	int openBracket = 0;
	do {
		c = racerInputStream.read();
		if(print) out.write(c);
		if( c == '(' )
		    openBracket++;
		else if( c == ')' ) {
		    openBracket--;
		    if( openBracket == 0 )
		        count++;
		}
	} while( c != 10);

	
	if(print) {
	    out.write('\n');
	    out.flush();
	}
	    
	return count;
}
}
