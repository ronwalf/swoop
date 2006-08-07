
package org.mindswap.swoop.utils.ui;

import java.io.File;

import javax.swing.filechooser.FileFilter;

/**
 * File Filter class designed to create filters for Swoop. All the supported file types
 * are stored in an internal array to generate the filters.  
 * 
 * @author Evren Sirin
 */
public class SwoopFileFilter extends FileFilter {
    private String description;
    private String[] extensions;
    
    /**
     * 
     */
    public static String[][] supportedTypes = {
        {"SWOOP Workspace Files", "swp"},
        {"SWOOP Ontology Object Files", "swo"},
        {"OWL Files", "owl"},
        {"RDF Files", "rdf"},
        {"XML Files", "xml"},
		{"Abstract Syntax Files", "txt"}
    };

    /**
     * Default file filter which contains all the file extensions
     * (*.swp, *.swo, *.owl, *.rdf, *.xml, ".txt)
     */
    public SwoopFileFilter() {
        String[] ext = new String[supportedTypes.length];
        for(int i = 0; i < ext.length; i++) {
            ext[i] = supportedTypes[i][1];
        }

        init("SWOOP Files", ext);
    }
     
    /**
     * Default file filter which contains only ontology file extensions
     * (*.swo, *.owl, *.rdf, *.xml, ".txt)
     */
    public SwoopFileFilter(boolean onlyOntologyFiles) {
    	if (onlyOntologyFiles) {
	        String[] ext = new String[supportedTypes.length-1];
	        for(int i = 1; i < supportedTypes.length; i++) {
	            ext[i-1] = supportedTypes[i][1];
	        }
	
	        init("SWOOP Ontology Files", ext);
    	}
    }
    
    /**
     * A file filter that accepts only one type of extension. Extension should
     * not have '.' in it. Description is the string shown in the combo box of
     * the file chooser
     * 
     * @param desc
     * @param extension
     */
    public SwoopFileFilter(String desc, String extension) {
        init(desc, new String[] { extension });
    }

    /**
     * A file filter that accepts files whose extension matches the one element
     * in the given array.
     * 
     * @param desc
     * @param extension
     */
    public SwoopFileFilter(String desc, String[] extensions) {
        init(desc, extensions);
    }
    
    private void init(String desc, String[] ext) {    
        extensions = ext;
        
		StringBuffer strbuf = new StringBuffer(desc + " (");
		for(int i = 0; i < extensions.length; i++)
		{
			if(i > 0) { strbuf.append(", "); }
			strbuf.append("*." + extensions[i]);
		}
		strbuf.append(")");
		description = strbuf.toString();
    }
    
    /**
     * Return a file filter that only accepts Swoop workspace file (swp)
     * 
     * @return
     */
    public static FileFilter getWorkspaceFilter() {
        return new SwoopFileFilter(supportedTypes[0][0], supportedTypes[0][1]);
    }

    /**
     * Return a file filter that only accepts Swoop ontology files (swo, owl, rdf, xml)
     * 
     * @return
     */
    public static FileFilter[] getOntologyFilters() {
    	SwoopFileFilter[] filters = new SwoopFileFilter[6];
    	for(int i = 0; i < filters.length-1; i++) {
            filters[i] = new SwoopFileFilter(supportedTypes[i+1][0], supportedTypes[i+1][1]);
        }
        filters[5] = new SwoopFileFilter(true);
        
        return filters;
    }

    /**
     * Return an array of file filters for all the supported file types.
     * 
     * @return
     */
    public static FileFilter[] getAllFilters() {
        SwoopFileFilter[] filters = new SwoopFileFilter[supportedTypes.length + 1];
        for(int i = 0; i < filters.length - 1; i++) {
            filters[i] = new SwoopFileFilter(supportedTypes[i][0], supportedTypes[i][1]);
        }
        filters[filters.length - 1] = new SwoopFileFilter();
        
        return filters;
    }

	public boolean accept(File f) {
		
		if (f.isDirectory()) return true;
		
		String ext = getExtension(f);

        if (ext != null) {
            for(int i = 0; i < extensions.length; i++) {
                if(extensions[i].equals(ext))
                    return true;
            }
        }
		
        return false;
	}
	
    public static String getExtension(File f) {
        String ext = null;
        String s = f.getName();
        int i = s.lastIndexOf('.');

        if (i > 0 &&  i < s.length() - 1) {
            ext = s.substring(i+1).toLowerCase();
        }
        return ext;
    }
    
	public String getDescription() {
		return description;
	}

}
