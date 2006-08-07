/*
 * Created on Oct 19, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.mindswap.swoop.utils.graph.debug;

import java.awt.geom.AffineTransform;

/**
 * @author Dave Wang
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class Utilities 
{
	public static void errPrintAffineTransform( AffineTransform transform )
	{
    	double [] mat = new double[6];
    	transform.getMatrix( mat );
    	System.err.println("matrix: ");
    	System.err.println( mat[0] + " " + mat[2]+ " " + mat[4]);
    	System.err.println( mat[1] + " " + mat[3]+ " " + mat[5]);

	}
}
