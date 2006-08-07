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

package org.mindswap.swoop.utils;

import java.io.File;
import java.io.FilenameFilter;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarFile;

import org.mindswap.swoop.Swoop;

/**
 * @author Evren Sirin
 */
public class PluginLoader {
	private ClassLoader ucl = null;
	private List classes = new ArrayList();
	private String dir = "plugins";
	
	private static PluginLoader INSTANCE = new PluginLoader();

	
	public static PluginLoader getInstance() {
		return INSTANCE;
	}
	
	class ExtensionFilter implements FilenameFilter {
		String ext;
		
		ExtensionFilter(String ext) {
			this.ext = ext;
		}
		public boolean accept(File dir, String name) {
			return name.endsWith(ext);
		}
	}
	
	private PluginLoader() {
		URL[] urls = null;

		// scan all the jar files in the plugins directory and put their
		// URL into the array
		File modulePath = new File(dir);
		List files = new ArrayList();
		if (modulePath != null && modulePath.exists()) {
			File[] jarFiles = modulePath.listFiles(new ExtensionFilter(".jar"));
			// make one more space for the current jar
			urls = new URL[jarFiles.length + 1];
			for (int i = 0; i < jarFiles.length; i++) {
				try {
					urls[i + 1] = jarFiles[i].toURL();
					files.add(jarFiles[i]);
				} catch (Exception ex) {
				}
			}
		}
		else
			urls = new URL[1];

//		for(int i = 0; i < urls.length; i++)
//			System.out.println(urls[i]);
		
		// the first element in the list is always going to be the
		// location where swoop is in. if the classpath is set to
		// swoop.jar then it will be a jar file, if the classpath
		// is set ot the directory where the class files exist then
		// this will be the location of that directory
		urls[0] = Swoop.class.getProtectionDomain().getCodeSource().getLocation();
	
		ucl = new URLClassLoader(urls);
		try {
			File swoopLoc = new File(new URI(urls[0].toExternalForm()));
			if(swoopLoc.isDirectory()) 
				addClassesFile(swoopLoc, "");
			else
				files.add(swoopLoc);
		}
		catch (Throwable ex) {
			System.out.println(ex);
			ex.printStackTrace();
		}
		
		try {
			for (int i = 0; i < files.size(); i++) {
				Enumeration e = new JarFile((File) files.get(i)).entries();
				while (e.hasMoreElements()) {
					String file = e.nextElement().toString();
					if (file.endsWith(".class") && (file.indexOf("$") == -1)) {
						file = file.substring(0, file.length() - 6).replace('/', '.');
						try {
							Class c = ucl.loadClass(file);
							classes.add(c);
							//System.out.println("add " + c);
						} catch (Throwable e1) {
							//System.out.println("Cannot load " + file + ": " + e1);
						}
					}
				}
			}
		} catch (Throwable ex) {
			System.out.println(ex);
			ex.printStackTrace();
		}		
	}

	private void addClassesFile(File path, String name) {
		File[] files = path.listFiles();
		for (int i = 0; i < files.length; i++) {
			String file = name + files[i].getName();		
			if (files[i].isDirectory())
				addClassesFile(files[i], file + ".");
			else if(file.endsWith(".class") && (file.indexOf("$") == -1)) {
				file = file.substring(0, file.length() - 6).replace('/', '.');
				try {
					Class cls = ucl.loadClass(file.toString());
					classes.add(cls);
					//System.out.println("add " + cls);
				} catch (Throwable e) {
					//System.out.println("Cannot load " + file + ": " + e);
				}
			}
		}
	}

	/**
	 * 
	 * Return all the classes that implements the given interface. The classes that are
	 * searched are the ones loaded from the jar files in the plugins directory plus
	 * the swoop resources. 
	 * 
	 * 
	 * @param c
	 * @return
	 */
	public List getClasses(Class c) {
		List list = new ArrayList();
		try {
			for(int i = 0; i < classes.size(); i++) {
				Class cls = (Class) classes.get(i);
				Class[] interfaces = cls.getInterfaces();
				for(int j = 0; j < interfaces.length; j++) {
					if(c.isAssignableFrom(interfaces[j])) {
						list.add(cls);
						break;
					}
				}				
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return list;
	}

}
