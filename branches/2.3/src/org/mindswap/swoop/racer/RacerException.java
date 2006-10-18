package org.mindswap.swoop.racer;

/** This class represents a RACER exception. It occurs a RACER exception when the RACER server
	returns a ":error" message. */
	
public class RacerException extends Exception {


/** The constructor creates a RACER exception from the whole string returned by the racer server. */

public RacerException(String racerError) {
	super(racerError);
}
}
