package org.mindswap.swoop.locality;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.mindswap.pellet.debug.utils.Timer;
import org.mindswap.swoop.SwoopModel;
import org.mindswap.swoop.utils.owlapi.CorrectedRDFRenderer;
import org.semanticweb.owl.model.OWLOntology;

public class FindImports {

	SwoopModel swoopModel = new SwoopModel();
	boolean DEBUG = true;
	Map entTest = new HashMap();
	String NEWLINE = System.getProperty("line.separator");
	Timer testTimer;
	String logFile = "";
	List testOnt;
	
public FindImports() throws Exception {
		
		// load ontologies
		this.testOnt = new ArrayList();		
		String loc2 = "C:/ontologies/ontologies32.txt";
        File fi = new File( loc2 );
        StringBuffer contents = new StringBuffer();
        BufferedReader input = null;
        try {
          input = new BufferedReader( new FileReader(fi) );
          String line = null; 
          while (( line = input.readLine()) != null){
        	 if(DEBUG){
        		 System.out.println("Reading Ontology " + line);
        	 }
        	  OWLOntology ont = swoopModel.loadOntology(new URI(line));
              if(!ont.getIncludedOntologies().isEmpty())
            	  testOnt.add(ont);        	
          }
        }
        catch (FileNotFoundException ex) {
          ex.printStackTrace();
        }
        catch (IOException ex){
          ex.printStackTrace();
        }
        finally {
          try {
            if (input!= null) {
              //flush and close both "input" and its underlying FileReader
              input.close();
            }
          }
          catch (IOException ex) {
            ex.printStackTrace();
          }
        }
	           
	    System.out.println("DONE: Ontologies Loaded");
	    
	 }

public List getOntologies() 
{
	return this.testOnt;
}


public static void main(String[] args) {
	try {
		FindImports t = new FindImports();
		List l = t.getOntologies();
		Iterator iter = l.iterator();
		int number = 0;
		while (iter.hasNext()){		
			number = number +1;
			OWLOntology ont = (OWLOntology)iter.next();
			String loc = "C:/ontologies/imports/" + number + ".owl";
			String output;
			StringWriter rdfBuffer = new StringWriter();
			CorrectedRDFRenderer rdfRenderer = new CorrectedRDFRenderer();
			rdfRenderer.renderOntology(ont, rdfBuffer);
			output = rdfBuffer.toString();
			File f = new File(loc);
	        BufferedWriter out = new BufferedWriter(new FileWriter(f));
	        out.write(output);
	        out.close();
			}
	}
	catch (Exception e) {
		e.printStackTrace();
	}		
}
}
