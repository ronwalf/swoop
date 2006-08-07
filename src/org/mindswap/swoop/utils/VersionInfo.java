/*
 * Created on Mar 28, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.mindswap.swoop.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.ResourceBundle;

/**
 * @author ronwalf
 *
 * Automatic (from ant) version information for Swoop
 */
public class VersionInfo {
	private Properties versionProperties = null;
	private static String UNKNOWN = "(unknown)";
	
	public VersionInfo() {
		versionProperties = new Properties();
		System.out.print(VersionInfo.class.getResource(""));
		
		InputStream vstream = VersionInfo.class.getResourceAsStream("/org/mindswap/swoop/version.properties");
		 if (vstream != null) {
		 	try {
		 		versionProperties.load(vstream);
		 	} catch (IOException e) {
		 		System.err.println("Could not load version properties:");
		 		e.printStackTrace();
		 	}
		 }
	}
	
	public String getVersionString() {
		return versionProperties.getProperty("org.mindswap.swoop.version", "(unreleased)");
	}
	
	public String getReleaseDate() {
		return versionProperties.getProperty("org.mindswap.swoop.releaseDate", UNKNOWN);
	}
	
	public static void main(String[] args) {
	}
}
