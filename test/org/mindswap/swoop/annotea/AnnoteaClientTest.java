/*
 * Created on Oct 22, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.mindswap.swoop.annotea;

import java.net.URI;
import java.net.URL;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.mindswap.swoop.annotea.Annotea;
import org.mindswap.swoop.annotea.AnnoteaClient;
import org.mindswap.swoop.annotea.Description;

/**
 * @author ronwalf
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class AnnoteaClientTest extends TestCase {
	protected AnnoteaClient client = null;
	protected URL serverURL = null;
	
	public AnnoteaClientTest(String testname) {
		super(testname);
		try {
			serverURL = new URL("http://localhost:8080/Annotation");
			Annotea.initializeAnnotea();
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	public static Test suite() {
        return new TestSuite(AnnoteaClientTest.class);
	}
	
	protected void setUp() {
		client = new AnnoteaClient(serverURL, null);
	}
	
	public void test_01_Post() throws Exception {
		Description description = new Description();
	
		URI[] uris = {new URI("http://www.mindswap.org/")};
		description.setAnnotates(uris);
		description.setBody("Hi there!");
		description.setBodyType("text/plain");
		
		URL location = client.post(description);
		//client.delete(location);
	}

	public static void main(String[] args) {
		junit.textui.TestRunner.run(suite());
	}
}
