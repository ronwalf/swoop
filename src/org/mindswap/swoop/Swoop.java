//The MIT License
//
// Copyright (c) 2004 Mindswap Research Group, University of Maryland, College Park
//
// Permission is hereby granted, free of charge, to any person obtaining a copy
// of this software and associated documentation files (the "Software"), to
// deal in the Software without restriction, including without limitation the
// rights to use, copy, modify, merge, publish, distribute, sublicense, and/or
// sell copies of the Software, and to permit persons to whom the Software is
// furnished to do so, subject to the following conditions:
//
// The above copyright notice and this permission notice shall be included in
// all copies or substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
// FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS
// IN THE SOFTWARE.

package org.mindswap.swoop;

import java.net.URI;

import org.mindswap.swoop.utils.VersionInfo;

public class Swoop {
    
    static SwoopFrame frame;
    private static VersionInfo vinfo = null;
	/*
	 * Global UI objects
	 */
	//public final static boolean isWebStart = false;
    
	public static boolean isWebStart() {
		String webstart = System.getProperty("jnlp.swoop.isWebStart");
		if (webstart != null && webstart.toLowerCase().equals("true")) {
			return true;
		}
		return false;
	}

	public static void loadArgs(SwoopModel model, String[] args) {
		for (int i = 0; i < args.length; i++) {
			try {
				model.addOntology(new URI(args[i]));
			} catch (Exception exception) {
				System.out.println("Could not load ontology " + args[i]);
			}

		}
	}
	
	public static VersionInfo getVersionInfo() {
		if (vinfo == null) 
			vinfo = new VersionInfo();
		return vinfo;
	}
    
    public static void main(String[] args) {
    	// Create model
		SwoopModel model = new SwoopModel();
		if (isWebStart()) {
			System.out.println("In WebStart mode.");
		}
		// Create application frame.
		frame = new SwoopFrame(model);
		
		// Show frame
		frame.setVisible(true);
		loadArgs(model, args);
		System.out.println("Loaded ontologies: " + model.getOntologies());
		
	}
}
