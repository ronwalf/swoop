/*
 * Created on Apr 7, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.mindswap.swoop;

import org.mindswap.swoop.annotea.AnnoteaClientTest;
import org.mindswap.swoop.annotea.DescriptionTest;
import org.mindswap.swoop.utils.owlapi.CorrectedRDFRendererTest;
import org.mindswap.swoop.utils.owlapi.QNameTest;
import org.mindswap.swoop.utils.owlapi.diff.OWLDiffTest;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * @author ronwalf
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class AllTests extends TestCase {
	public static Test suite() {
		TestSuite suite= new TestSuite("All Swoop JUnit Tests");
        	suite.addTest(DescriptionTest.suite());
        	suite.addTest(AnnoteaClientTest.suite());
        	suite.addTest(QNameTest.suite());
        	suite.addTest(CorrectedRDFRendererTest.suite());
        	suite.addTest(OWLDiffTest.suite());
        	return suite;
	}
	
	public static void main(String args[]) { 
	    junit.textui.TestRunner.run(suite());
	}
}
