package org.mindswap.swoop.racer;

/** This exception is thrown by RacerTermParser objects when a construction does not correspond to a
	RACER term.
 */
 
public class RacerIllegalConstruction extends RacerException {
/**
 * RacerIllegalConstruction constructor comment.
 * @param racerError java.lang.String
 */
public RacerIllegalConstruction(String racerError) {
	super(racerError);
}
/** Builds a illegal construction exception for a vector in which the illegal construction is stored.
 * @param v java.util.Vector The vector in which the construction is stored.
 */
 
public RacerIllegalConstruction(java.util.Vector v) {
	super(listRepresentation(v)+" is not a correct "+v.elementAt(0)+" term");
}
/** This method returns the string representation of the term stored in the vector.
 * @return java.lang.String
 * @param v java.util.Vector
 */
 
private static String listRepresentation(java.util.Vector v) {
	String s="(";
	for(int i=0;i<v.size();i++) {
		s=s+v.elementAt(i);
		if (i+1<v.size()) s=s+" ";
	}
	return s+")";
}
}
