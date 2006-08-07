package org.mindswap.swoop.racer;

/** This class just provides a set of constants to be used with methods from RacerClient. */

public class RacerConstants {

	/** Role properties. */

	public static final int PLAIN_ROLE = 0;
	public static final int TRANSITIVE_ROLE = 1;
	public static final int FEATURE = 2;
	public static final int SYMMETRIC_ROLE = 4;
	public static final int REFLEXIVE_ROLE = 8;


	/** Logic types. */

	public static final int K_LOGIC = 1;
	public static final int K4_LOGIC = 2;
	public static final int S4_LOGIC = 3;

	
	/** Synchronous mode constant. If the synchronous mode is set to this value, the RACER server will
	be accessed synchronously. */

	public static final int SYNCHRONOUS_MODE = 0;


	/** Asynchronous mode constant. If the synchronous mode is set to this value, the RACER server will
	be accessed asynchronously. */

	public static final int ASYNCHRONOUS_MODE = 1;


	/** Mixed synchronous mode constant. If the synchronous mode is set to this value, the RACER server will
	be accessed synchronously for queries and asynchronously for knowledge base updating primitives. */

	public static final int MIXED_SYNCHRONOUS_MODE = 2;


/**
 * RacerConstants constructor comment.
 */
public RacerConstants() {
	super();
}
}
